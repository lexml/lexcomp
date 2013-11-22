package br.gov.lexml.symbolicobject.parser

import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico
import scalaz.Validation
import br.gov.lexml.parser.pl.ProjetoLeiParser
import br.gov.lexml.parser.pl.metadado.Metadado
import br.gov.lexml.parser.pl.profile.DocumentProfileRegister
import br.gov.lexml.parser.pl.errors.ParseProblem
import scala.xml.NodeSeq
import br.gov.lexml.parser.pl.ProjetoLei
import br.gov.lexml.parser.pl.xhtml.XHTMLProcessor.defaultConverter
import br.gov.lexml.parser.pl.xhtml.XHTMLProcessor.pipeline
import br.gov.lexml.parser.pl.output.LexmlRenderer
import br.gov.lexml.parser.pl.errors.ParseException
import scala.xml.Node
import br.gov.lexml.parser.pl.block.Block
import scala.xml.Elem
import org.apache.commons.io.IOUtils
import java.io.InputStream
import br.gov.lexml.parser.pl.block.Paragraph
import scala.xml.Text
import br.gov.lexml.parser.pl.profile.ProjetoDeLeiDoSenadoNoSenado
import br.gov.lexml.symbolicobject.tipos.STipo
import br.gov.lexml.symbolicobject.impl.RotuloRole
import scala.xml._
import br.gov.lexml.{ symbolicobject => S }
import br.gov.lexml.symbolicobject.{ tipos => T }
import java.lang.{ Character => C }
import br.gov.lexml.{ symbolicobject => S }
import br.gov.lexml.symbolicobject.impl.Documento
import br.gov.lexml.symbolicobject.impl.Nome
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico
import br.gov.lexml.symbolicobject.impl.Posicao
import br.gov.lexml.symbolicobject.impl.Rotulo
import br.gov.lexml.symbolicobject.{ tipos => T }
import java.lang.{ Character => C }
import scala.Option.option2Iterable
import br.gov.lexml.symbolicobject.impl._
import br.gov.lexml.symbolicobject.xml.WrappedNodeSeq
import br.gov.lexml.symbolicobject.util.CollectionUtils

trait IdSource {
  def nextId(tipo: STipo): Long
}

class Parser(idSource: IdSource) {

  private val lexmlParser = new LexmlParser(idSource)

  def parse(source: InputDocument): Validation[Throwable, Documento[Unit]] = {
    for {
      xml <- source.doc.elem()
      doc <- lexmlParser.parse(source.urn, source.tipo, xml).leftMap(new RuntimeException(_))
    } yield (doc)
  }
}

class LexmlParser(idSource: IdSource) {

  def nomeFromUrn(urn: String): Nome = {
    NomeRelativo(RotuloClassificado("urn", urn), NomeContexto(T.Tipos.TodosDocumentos))
  }

  def onlyElements(l: Seq[Node]) = l collect { case e: Elem => e }

  def rotuloFromIdComp(idComp: String): Rotulo = {
    val (nome, resto) = idComp.span(C.isLetter)
    if (resto == "1u") {
      RotuloClassificado(nome, "unico")
    } else {
      val numL = resto.split("-").toList.filter(!_.isEmpty).map(_.replaceAll("u", "").toInt)
      numL match {
        case Nil => RotuloRole(nome)
        case l => RotuloOrdenado(nome, numL: _*)
      }
    }
  }

  def tipoFromLabel(label: String): Option[T.STipo] =
    T.Tipos.tipos.get("os_" + label.toLowerCase)

  def processaDispositivo(e: Elem, tipo: T.STipo, rotulo: Rotulo, els: Seq[Elem]): Posicao[Unit] = {
    val (textEls, subElems) = els.span(x => x.label == "p" || x.label == "NomeAgrupador")
    val id = idSource.nextId(tipo)
    val text = textEls.flatMap(_.child)
    val posL = subElems.flatMap(processaAgrupadorOuDispositivo)    
    val properties : Map[String,String] = if(e.label.toLowerCase == "alteracao") {
      val base = e.attributes.get("http://www.w3.org/XML/1998/namespace",e,"base")
      base.map(x => (Properties.URN_ALTERACAO,x.text)).toMap
    } else { Map () }
    val posText = (text.isEmpty, (NodeSeq fromSeq text).text.trim) match {
      case (true, _) => Seq()
      case (_, "") => Seq()
      case (_, t) =>        
        val splitText = text.flatMap {
          case Text(t) => 
            CollectionUtils.intersperse[Node,Seq[Node]](
                t.split("#S#").toSeq.map(Text(_)),Seq(Text("#S#")))
          case x => Seq(x)
        } 
        val textLists = CollectionUtils.splitBy[Node](splitText, _ == Text("#S#"))
        
        val makeRotulo = if (textLists.size > 1) {
          (idx : Int) => RotuloOrdenado("texto",idx + 1) 
        } else {
          (_ : Int) => RotuloRole("texto")
        }
        
        textLists.zipWithIndex map { case (l,idx) =>
          val txtid = idSource.nextId(T.Tipos.TextoFormatado)
          val o = TextoFormatado(txtid, new WrappedNodeSeq(NodeSeq fromSeq l), ())
          Posicao[Unit](makeRotulo(idx), o)
        }        
    }
    val os = ObjetoSimbolicoComplexo(id, tipo, (), (posText ++ posL).toIndexedSeq, properties = properties)

    Posicao[Unit](rotulo, os)
  }

  def processaAgrupadorOuDispositivo(e: Elem): Option[Posicao[Unit]] =
    for {
      tipo <- tipoFromLabel(e.label)
      xmlid <- e.attributes.get("id")
      lastComp <- NodeSeq.fromSeq(xmlid).text.split("_").toSeq.lastOption
      rotulo = rotuloFromIdComp(lastComp)
      els = onlyElements(e.child).dropWhile(x => x.label == "Rotulo" || x.label == "TituloDispositivo")
      p = processaDispositivo(e, tipo, rotulo, els)
      /*pos <- if(tipo.superTipos.contains(T.Tipos.Agrupador)) {
	              Some(processaAgrupador(e,tipo,rotulo,els))
	           } else if(tipo.superTipos.contains(T.Tipos.Dispositivo)){
	              val p = processaDispositivo(e,tipo,rotulo,els) 
	              Some((IndexedSeq(p),p))
	           } else {
	             None
	           }*/
    } yield (p)

  def parse(urn: String, tipo: STipo, doc: NodeSeq): Validation[String, Documento[Unit]] = for {
    articulacao <- Validation.fromEither(
      (doc \\ "Articulacao").collect({ case e: Elem => e.child })
        .toSeq.headOption
        .toRight("Elemento articulação não encontrado"))
    nome = nomeFromUrn(urn)
    docId = idSource.nextId(T.Tipos.Documento)
    osId = idSource.nextId(T.Tipos.RaizDocumento)
    artId = idSource.nextId(T.Tipos.Articulacao)
    agrupId = idSource.nextId(T.Tipos.Agrupadores)
    disps = onlyElements(articulacao).flatMap(processaAgrupadorOuDispositivo).toIndexedSeq
    artObj = ObjetoSimbolicoComplexo(artId, T.Tipos.Articulacao, (), disps)
    posL = IndexedSeq(
      Posicao[Unit](RotuloRole("articulacao"), artObj))
    os = ObjetoSimbolicoComplexo(osId, T.Tipos.RaizDocumento, (), posL)
  } yield (Documento[Unit](docId, tipo, nome, os))

}