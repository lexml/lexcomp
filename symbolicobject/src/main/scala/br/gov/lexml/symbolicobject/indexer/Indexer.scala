package br.gov.lexml.symbolicobject.indexer

import javax.xml.transform.Source
import scalaz.Validation
import br.gov.lexml.symbolicobject.impl.Documento
import br.gov.lexml.symbolicobject.impl.NomeRelativo
import br.gov.lexml.symbolicobject.impl.Caminho
import br.gov.lexml.symbolicobject.tipos.STipo
import br.gov.lexml.symbolicobject.impl.Comentario
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico
import br.gov.lexml.symbolicobject.impl.Relacao
import scala.ref.WeakReference
import br.gov.lexml.symbolicobject.impl.Rotulo
import br.gov.lexml.symbolicobject.impl.Posicao
import br.gov.lexml.symbolicobject.util.CollectionUtils._
import br.gov.lexml.symbolicobject.parser.Parser
import br.gov.lexml.symbolicobject.parser.InputDocument
import br.gov.lexml.symbolicobject.parser.IdSource
import org.scalastuff.proto.JsonFormat
import br.gov.lexml.{ symbolicobject => S }

abstract sealed class Direction
case object SourceToTarget extends Direction
case object TargetToSource extends Direction

final case class Contexto(
  caminho: Caminho = Caminho(),
  parenteId: Option[Long] = None,
  relacoes: Map[Long, Map[Direction, IndexedSeq[Relacao[ContextoRelacao]]]] = Map(),
  comentarios: IndexedSeq[Comentario] = IndexedSeq())

final case class ContextoRelacao(comentarios: IndexedSeq[Comentario] = IndexedSeq())

final case class Relacoes(origemParaAlvo: IndexedSeq[Types.RelacaoComCtx], alvoParaOrigem: IndexedSeq[Types.RelacaoComCtx])

trait IIndexer {
  type DocumentoId = Long
  type ComentarioId = Long
  type ObjetoSimbolicoId = Long
  type RelacaoId = Long

  //def addDocumentoLexML(source : InputDocument) : Validation[String,DocumentoId]
  def addDocumento(doc: S.Documento): Unit
  def getDocumento(id: DocumentoId): Option[Documento[Contexto]]
  def removeDocumento(id: DocumentoId): Unit

  def addComentario(c: S.Comentario): Unit
  def removeComentario(idComentario: ComentarioId): Unit
  def getComentarios(idAlvo: Long): IndexedSeq[Comentario]

  def getRelacoes(idObjetoSimbolico: ObjetoSimbolicoId, idDocumento: DocumentoId): Relacoes

  def addRelacao(r: S.Relacao): Unit
  def removeRelacao(relId: RelacaoId): Unit
}

final case class DBState(
  documents: Map[Long, Documento[Unit]] = Map(),
  relations: Map[Long, Relacao[Unit]] = Map(),
  comentarios: Map[Long, Comentario] = Map())

final case class ObjetoSimbolicoIndexData(
  documentos: Set[Long] = Set(),
  comentarios: Set[Long] = Set(),
  relacoesPorDocumento: Map[Long, Map[Direction, Set[Long]]] = Map()) {

  type M = Map[Long, Map[Direction, Set[Long]]]
  private val mergeRelacoes: (M, M) => M = mergeMapWith(mergeMapWith((_: Set[Long]) ++ (_: Set[Long])))

  def +(d: ObjetoSimbolicoIndexData) = ObjetoSimbolicoIndexData(
    documentos ++ d.documentos, comentarios ++ d.comentarios,
    mergeRelacoes(relacoesPorDocumento, d.relacoesPorDocumento))
}

final case class RelacaoIndexData(
  comentarios: Set[Long] = Set())

final case class DBIndex(
  objetosSimbolicos: Map[Long, ObjetoSimbolicoIndexData] = Map(),
  comentariosPorAlvo: Map[Long, Set[Long]] = Map(),
  relacoes: Map[Long, RelacaoIndexData] = Map())

final class Indexer extends IIndexer {

  private var state = DBState()
  private var _index: Option[DBIndex] = None

  private def queryAndChangeState[A](f: DBState => (Option[DBState], A)): A = synchronized {
    val (s, r) = f(state)
    s.foreach { newState =>
      state = newState
      _index = None
    }
    r
  }
  private def changeState(f: DBState => Option[DBState]): Unit = queryAndChangeState(x => (f(x), ()))

  private def index: DBIndex = synchronized {
    val idx = _index.getOrElse(buildIndex)
    _index = Some(idx)
    idx
  }

  def invalidadeIndex() = synchronized { _index = None }

  def buildIndex: DBIndex = {
    import scalaz._
    import Scalaz.{ state => _, _ }
    val comentariosPorAlvo = state.comentarios.values.map(c => (c.alvo, c.id)).groupBy(_._1).mapValues(_.map(_._2).toSet)
    /*val (comentariosRelacoesI,comentariosObjetosI) = 
			  state.comentarios.values.par
			  	.map(x => (x.ref.isInstanceOf[RefRelacao],(x.ref.id,x)))
			  	.partition(_._1)
	  val comentariosRelacoes = comentariosRelacoesI.map(_._2).seq.groupBy1on2with(x => RelacaoIndexData(comentarios = x.map(_.comentario.id).toSet))//groupBy(_._1).mapValues(_.map(_._2))
	  val comentariosObjetos = 
	    	comentariosObjetosI.map(_._2).seq.groupBy1on2with(x => ObjetoSimbolicoIndexData(comentarios = x.map(_.comentario.id).toSet))*/

    val docsPerObject = (for {
      doc <- state.documents.values.toStream
      oid <- doc.os.toStream.collect { case o: ObjetoSimbolico[_] => o.id }
    } yield (oid, doc.id)) groupBy (_._1) mapValues (_.map(_._2).toSet)

    val docsIdx = docsPerObject.mapValues(s => ObjetoSimbolicoIndexData(documentos = s))

    val parRel = state.relations.values.toSeq.par
    val rel1 = for {
      rel <- parRel
      src <- rel.origem
      tgt <- rel.origem
    } yield (rel.id, src, tgt)
    val s2t = for {
      rel <- rel1
      (r, s, t) = rel
      d <- docsPerObject.getOrElse(t, Set())
    } yield (s, (d, (SourceToTarget, Set(r))))
    val t2s = for {
      rel <- rel1
      (r, s, t) = rel
      d <- docsPerObject.getOrElse(s, Set())
    } yield (t, (d, (TargetToSource, Set(r))))
    val relsL: Seq[(Long, (Long, (Direction, Set[Long])))] = s2t.seq ++ t2s.seq
    val rels = relsL.groupBy1on2with(_.groupBy1on2with(_.groupBy1on2with(_.flatten.toSet)))
    val relsIdx = rels.mapValues(v => ObjetoSimbolicoIndexData(relacoesPorDocumento = v))
    val comentariosObjetos = comentariosPorAlvo.filterKeys(docsPerObject.contains).mapValues(s => ObjetoSimbolicoIndexData(comentarios = s))
    val objIdx = mergeMapsWith((_: ObjetoSimbolicoIndexData) + (_: ObjetoSimbolicoIndexData))(Seq(relsIdx, comentariosObjetos, docsIdx))
    val relIdx = state.relations.map { case (rid, _) => (rid, RelacaoIndexData(comentarios = comentariosPorAlvo.getOrElse(rid, Set()))) }.toMap
    DBIndex(objetosSimbolicos = objIdx, comentariosPorAlvo = comentariosPorAlvo, relacoes = relIdx)
  }

  override def addDocumento(d: S.Documento): Unit = {
    addDocumento(Documento.fromDocumento(d))
  }

  private def addDocumento(d: Documento[Unit]) =
    changeState(st => Some(st copy (documents = state.documents + (d.id -> d))))

  private def addContextoToRelacao(r: Relacao[_]): Relacao[ContextoRelacao] =
    r.setData(index.relacoes.get(r.id).map(d => ContextoRelacao(d.comentarios.toIndexedSeq.map(state.comentarios))).getOrElse(ContextoRelacao()))

  private def addContextoToDocumento(d: Documento[_]): Documento[Contexto] = {
    type Acc = (Caminho, Option[Long])
    val acc0 = (Caminho(), None)
    val osid0 = new ObjetoSimbolicoIndexData()

    def osid(id: Long) = index.objetosSimbolicos.getOrElse(id, osid0)

    def f(acc: Acc, os: ObjetoSimbolico[_]): (Contexto, Acc) = {
      val data = osid(os.id)
      val ctx = Contexto(
        acc._1,
        acc._2,
        data.relacoesPorDocumento.mapValues(_.mapValues(_.toIndexedSeq.map(
          state.relations andThen addContextoToRelacao))),
        data.comentarios.toIndexedSeq.map(state.comentarios))
      (ctx, (acc._1, Some(os.id)))
    }
    def g(acc: Acc, r: Rotulo): Acc = (acc._1 + r, acc._2)
    d copy (os = d.os.topDownMap(acc0, f, g))
  }

  override def getDocumento(id: Long): Option[Documento[Contexto]] = state.documents.get(id).map(addContextoToDocumento _)
  override def removeDocumento(id: Long): Unit = changeState { state =>
    state.documents.get(id).map { d =>
      val oids = d.os.toStream.collect { case o: ObjetoSimbolico[_] => o.id }.toSet
      val documents = state.documents - id
      val relations = state.relations.mapValues { _.filterIds(x => !oids(x)) }.filterNot(_._2.isDefined).mapValues(_.get)
      val comentariosAremover = index.objetosSimbolicos.filterKeys(oids).values.flatMap(_.comentarios).toSet
      val comentarios = state.comentarios.filterKeys(comentariosAremover)
      DBState(documents = documents, relations = relations, comentarios = comentarios)
    }
  }

  override def addComentario(c: S.Comentario): Unit = changeState { state =>
    val com = Comentario.fromComentario(c)
    Some(state copy (comentarios = state.comentarios + (com.id -> com)))
  }
  override def removeComentario(idComentario: Long): Unit = changeState { state =>
    if (state.comentarios.contains(idComentario)) {
      Some(state copy (comentarios = state.comentarios - idComentario))
    } else {
      None
    }
  }
  override def getComentarios(alvo: Long): IndexedSeq[Comentario] = index.comentariosPorAlvo.getOrElse(alvo, Set()).toIndexedSeq.map(state.comentarios)

  override def getRelacoes(idObjetoSimbolico: Long, idDocumento: Long) = {
    val r = index.objetosSimbolicos.get(idObjetoSimbolico)
      .flatMap(_.relacoesPorDocumento
        .get(idDocumento))
      .map(_.mapValues(_.toIndexedSeq.collect(state.relations).map(addContextoToRelacao)))
      .getOrElse(Map())
    val s2t = r.getOrElse(SourceToTarget, IndexedSeq())
    val t2s = r.getOrElse(TargetToSource, IndexedSeq())
    Relacoes(s2t, t2s)
  }

  def addRelacao(r: S.Relacao): Unit = changeState { state =>
    val r1 = Relacao.fromRelacao(r)
    Some(state copy (relations = state.relations + (r1.id -> r1)))
  }

  def removeRelacao(relId: RelacaoId): Unit = changeState { state =>
    if (state.relations.contains(relId)) {
      Some(state copy (relations = state.relations - relId))
    } else { None }
  }

}

object Types {
  type ObjetoSimbolicoComCtx = ObjetoSimbolico[Contexto]
  type DocumentoComCtx = Documento[Contexto]
  type RelacaoComCtx = Relacao[ContextoRelacao]
  type PosicaoComCtx = Posicao[Contexto]
}