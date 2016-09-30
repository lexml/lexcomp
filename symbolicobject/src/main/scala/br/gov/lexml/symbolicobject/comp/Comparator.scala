package br.gov.lexml.symbolicobject.comp

import scala.language.postfixOps

import scala.Option.option2Iterable
import scala.collection.mutable.ArrayBuilder
import scala.collection.mutable.{Map => MMap}
import scala.util.hashing.MurmurHash3.orderedHash
import scala.util.hashing.MurmurHash3.stringHash
import br.gov.lexml.lexmldiff.LexmlDiff
import br.gov.lexml.symbolicobject.impl.Attributes
import br.gov.lexml.symbolicobject.impl.Caminho
import br.gov.lexml.symbolicobject.impl.Documento
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolicoComplexo
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolicoSimples
import br.gov.lexml.symbolicobject.impl.RotuloSelector.roleToRS
import br.gov.lexml.symbolicobject.impl.Strategies.collectbu
import br.gov.lexml.symbolicobject.impl.TextoFormatado
import br.gov.lexml.symbolicobject.impl.TextoPuro
import br.gov.lexml.symbolicobject.tipos.Tipos
import grizzled.slf4j.Logger
import br.gov.lexml.symbolicobject.impl.Strategies
import br.gov.lexml.symbolicobject.tipos.STipo
import br.gov.lexml.scalautils.CollectionUtils
import br.gov.lexml.symbolicobject.impl.Relacao
import br.gov.lexml.symbolicobject.impl.RelacaoAusenteNaOrigem
import br.gov.lexml.symbolicobject.impl.RelacaoAusenteNoAlvo
import br.gov.lexml.symbolicobject.impl.RelacaoIgualdade
import br.gov.lexml.symbolicobject.impl.RelacaoDiferenca
import br.gov.lexml.symbolicobject.impl.RelacaoDiferenca
import br.gov.lexml.symbolicobject.impl.RelacaoDivisao
import br.gov.lexml.symbolicobject.impl.RelacaoFusao
import br.gov.lexml.symbolicobject.parser.IdSource
import br.gov.lexml.symbolicobject.impl.RelacaoIgualdade
import br.gov.lexml.symbolicobject.impl.ProvenienciaSistema
import grizzled.slf4j.Logging
import java.util.Collection
import scala.collection.JavaConverters
import br.gov.lexml.symbolicobject.ProvenienciaUsuario
import br.gov.lexml.symbolicobject.impl.Omissis
import br.gov.lexml.symbolicobject.impl.RotuloOrdenado
  import br.gov.lexml.symbolicobject.{Documento => IDocumento,Relacao => IRelacao}
import java.util.{List => JList}
import br.gov.lexml.symbolicobject.impl.Properties

class CompareProcessConfiguration(
    ) {
  def normalize(t: String): String = {
    import br.gov.lexml.parser.pl.text.normalizer
    normalizer.normalize(t.trim.replaceAll("\\s+"," ").replaceAll("\"","").replaceAll("[,.;]",".").replaceAll("[0-9]+", "#").toLowerCase())
  }
  
  val minSimilarity = 0.75
  val maxDiff = 1.0 - minSimilarity
  val minRatio = maxDiff
  val maxRatio = 1.0 / maxDiff

  def diffText(t1: String, t2: String) = LexmlDiff.diffAsText(t1, t2, minSimilarity, true)

  def diff(t1: String, t2: String): Double = {
    LexmlDiff.diff(t1, t2, minSimilarity, true) match {
      case Nil => 0
      case diff => 1 - LexmlDiff.proportionL(diff)
    }
  }

  def diffX(t1: String, t2: String) = LexmlDiff.diffAsXML(t1, t2, minSimilarity, true)
}

final case class EqContext(objMap: Map[Long, (Caminho, String)], left: IndexedSeq[Long], right: IndexedSeq[Long], equalSoFar: Map[Long, Long] = Map(), unmatched: Set[Long]) {
  def +(m1: Map[Long, Long]) = 
    EqContext(objMap, left, right, equalSoFar ++ m1, unmatched -- m1.keySet -- m1.values)
    
  lazy val matched = equalSoFar.keySet ++ equalSoFar.values.to[Set]
  override def toString() : String = {
    val total = objMap.size
    val onLeft = left.length
    val onRight = right.length
    val numMatched = equalSoFar.size * 2
    val numUnmatched = total - numMatched
    val perMatched = numMatched.toDouble * 100.0 / total.toDouble
    val perUnmatched = numUnmatched.toDouble * 100.0 / total.toDouble    
    f"EqContext: total = $total, left = $onLeft, right = $onRight, matched = $numMatched, unmatched=$numUnmatched, ($perMatched%03.2f%%/$perUnmatched%03.2f%%)"     
  } 
}

abstract sealed class Side
case object SL extends Side
case object SR extends Side

abstract sealed class CompareProcessType {
  trait SideSpec {
    val includeAlt : Boolean = true
    val includeText : Boolean = true
    val defaultAltUrn : String = ""
  }
  val leftSpec : SideSpec
  val rightSpec : SideSpec
  def side(s : Side) = s match {
    case SL => leftSpec
    case SR => rightSpec
  }
}

final case class CPT_TextToText(defaultLeftAltUrn : String, defaultRightAltUrn : String) extends CompareProcessType {
  override val leftSpec = new SideSpec {
    override val defaultAltUrn = defaultLeftAltUrn
  }
  override val rightSpec = new SideSpec {
    override val defaultAltUrn = defaultRightAltUrn
  }
}

final case class CPT_AltToText(defaultLeftAltUrn : String) extends CompareProcessType {
  override val leftSpec = new SideSpec {    
    override val includeText = false
    override val defaultAltUrn = defaultLeftAltUrn
  }
  override val rightSpec = new SideSpec {    
    override val includeAlt = false
  }
}

final case class CPT_TextToAlt(defaultRightAltUrn : String) extends CompareProcessType {
  override val rightSpec = new SideSpec {    
    override val includeText = false
    override val defaultAltUrn = defaultRightAltUrn
  }
  override val leftSpec = new SideSpec {    
    override val includeAlt = false
  }
}

final case class CompareProcessSpec(
    leftDoc: Documento[_],     
    rightDoc: Documento[_], 
    idSource : IdSource,
    compareType : CompareProcessType,    
    emmitOnlyForTexts : Boolean = false,
    rels : Iterable[Relacao[_]] = Seq(), 
    conf: CompareProcessConfiguration = new CompareProcessConfiguration()) {  
  def this(
      leftDoc: IDocumento, 
      rightDoc: IDocumento, 
      conf: CompareProcessConfiguration,
      idSource : IdSource,
      emmitOnlyForTexts : Boolean,      
      rels : Collection[IRelacao],
      compareType : CompareProcessType) = 
	  this(
	      Documento.fromDocumento(leftDoc),
	      Documento.fromDocumento(rightDoc),
	      idSource,
	      compareType,
	      emmitOnlyForTexts,
	      JavaConverters.collectionAsScalaIterableConverter(rels).asScala.map(Relacao.fromRelacao), 
	      conf)
	  
  
}


object Comparator extends Logging {

  def compare[T](instance : CompareProcessSpec) : Seq[Relacao[Unit]] = {
        
	  import instance._
	    
	  import org.kiama.rewriting.Rewriter.{debug => _,_}
	  import scala.util.hashing.MurmurHash3._
	
	  import Strategies._
	  import conf._
	
	  val diff_ = diff _
	  
	  import CollectionUtils._
	
	  def fromObjetoSimbolico(o : ObjetoSimbolico[_], docUrn : String) : Map[Long,ObjectData] = {
	    import scala.collection.mutable.{Map=>MMap}
	    val objMap : MMap[Long,ObjectData] = MMap() 
	    def make(o : ObjetoSimbolico[_],c : Caminho,parente : Option[ObjetoSimbolicoComplexo[_]]) : ObjectData = { 
	      val pl = o match {
		      case oc : ObjetoSimbolicoComplexo[_] => oc.posicoes.map { p => make(p.objeto,c + p.rotulo,Some(oc))}
		      case _ => IndexedSeq()
	      }
		  val od = ObjectData(o,None,c,pl,docUrn)
		  pl foreach { p => p.objetoParente = Some(od) }
		  objMap += od.id -> od 
		  od      	  
	    }
	    make(o,Caminho(),None)
	    objMap.toMap
	  }
	  
	  def caminhos(o : ObjetoSimbolico[_]) : IndexedSeq[Caminho] = {
	     
	    def caminhos_(o : ObjetoSimbolico[_],c : Caminho) : IndexedSeq[Caminho] = { 
	      o match {
		      case oc : ObjetoSimbolicoComplexo[_] => c +: oc.posicoes.flatMap { p => caminhos_(p.objeto,c + p.rotulo)}
		      case _ => IndexedSeq(c)
	      }	        	 
	    }
	    caminhos_(o,Caminho())
	  }
	  
	  final case class ObjectData(o: ObjetoSimbolico[_], var objetoParente : Option[ObjectData] = None, caminho : Caminho = Caminho(), children : IndexedSeq[ObjectData],
	      docUrn : String) {
	    val A = Attributes
	    val id = o.id
	    
	    def chain : IndexedSeq[ObjectData] = this +: objetoParente.toIndexedSeq.flatMap { _.chain }
	    
	    def inAlt = chain exists { od => od.o.tipo == Tipos.Alteracao } 
	    
	    def altUrn = chain flatMap { 
	      od => od.o match { 
	        case o : ObjetoSimbolicoComplexo[_] 
	        	if o.tipo == Tipos.Alteracao => o.properties.get(Properties.URN_ALTERACAO)
	        case _ => None
	      } 
	    } headOption
	    
	    val textPids = o match {
	      case oc: ObjetoSimbolicoComplexo[_] => (oc / "texto").result.map(_.objeto.id).toIndexedSeq
	      case _ => IndexedSeq()
	    }
	    /*lazy val hash: Int = o match {
	      case o: ObjetoSimbolicoComplexo[_] => orderedHash(o.posicoes.map(_.objeto -> hash))
	      case _ => stringHash(normalize(texto))
	    }*/
	    lazy val textoLocal = o match {
	      case oc: ObjetoSimbolicoComplexo[_] => ""
	      case t: TextoPuro[_] => t.texto
	      case t: TextoFormatado[_] => t.frag.ns.text
	      case _ : Omissis[_] => ""
	    } 
	    lazy val textoLocalNormalizado = normalize(textoLocal)
	    lazy val textos = o match {
	      case oc: ObjetoSimbolicoComplexo[_] => (oc / "texto").result.map(_.objeto).collect {
	        case t: TextoPuro[_] => t.texto
	        case t: TextoFormatado[_] => t.frag.ns.text
	      }.toIndexedSeq
	      case t: TextoPuro[_] => IndexedSeq(t.texto)
	      case t: TextoFormatado[_] => IndexedSeq(t.frag.ns.text)
	      case _ : Omissis[_] => IndexedSeq("")
	    }
	    lazy val texto = textos.mkString(" ")
	    lazy val textosNormalizados = textos.map(normalize)
	    lazy val textoNormalizado = normalize(texto)
	    lazy val textHash = stringHash(textoNormalizado)
	    lazy val textsHash = textos.map(stringHash)    
	    lazy val nomeTipo : String = o match {
	      case _: ObjetoSimbolicoComplexo[_] => o.tipo.nomeTipo
	      case os: ObjetoSimbolicoSimples[_] => objetoParente.get.o.tipo.nomeTipo + "_text"
	    }
	    lazy val tipoBase : STipo = o match {
	      case _: ObjetoSimbolicoComplexo[_] => o.tipo
	      case os: ObjetoSimbolicoSimples[_] => objetoParente.get.o.tipo      
	    }    
	    lazy val nivel : Int = {
	      val n =  if(tipoBase == Tipos.OsArtigo) { 0 } 
	                else if(tipoBase.superTipos contains Tipos.Agrupador) { 1 }
	                //else if(tipoBase.superTipos == Tipos.OsParagrafo || tipoBase.superTipos == Tipos.OsCaput) { 2 }
	                else if (tipoBase.superTipos contains Tipos.Dispositivo) { 3 }
	                else { 4 }
	      val l = if (isSimple) { 10 } else { 0 }
	      n + l
	    }
	    def equalBy(m: Map[Long, Long], od: ObjectData): Boolean = {
	      m.get(id) == Some(od.id) || ((o, od.o) match {
	        case (lo: ObjetoSimbolicoComplexo[_], ro: ObjetoSimbolicoComplexo[_]) =>
	          (lo.posicoes.length == ro.posicoes.length) && lo.posicoes.zip(ro.posicoes).forall {
	            case (pl, pr) => m.get(pl.objeto.id) == Some(pr.objeto.id)
	          }
	        case _ => textoNormalizado == od.textoNormalizado
	      })
	    }
	    lazy val isSimple = o.isInstanceOf[ObjetoSimbolicoSimples[_]]
	    
	    lazy val cpids: IndexedSeq[Long] = children.map(_.id)
	    def dist(od: ObjectData) = {
	      val res = for {
	        t1 <- if (textoNormalizado.isEmpty) { None } else { Some(textoNormalizado) }
	        t2 <- if (od.textoNormalizado.isEmpty) { None } else { Some(od.textoNormalizado) }
	      } yield {
	        diff_(t1, t2)
	      }      
	      res
	    }
	    def diff(od: ObjectData) = for {
	      t1 <- if (textoNormalizado.isEmpty) { None } else { Some(textoNormalizado) }
	      t2 <- if (od.textoNormalizado.isEmpty) { None } else { Some(od.textoNormalizado) }
	    } yield {
	      diffText(t1, t2)
	    }
	    override def toString() = {
	      f"ObjectData(o.id = ${o.id}  objetoParente = ${objetoParente.map(_.id)}, caminho = ${caminho}, children = ${children}"
	    }
	  }
	
	  //Inicializa rotinas do Kiama 
	  leftDoc.os.initTreeProperties()
	  rightDoc.os.initTreeProperties()
	
	  val objMap = fromObjetoSimbolico(leftDoc.os,leftDoc.urn.getOrElse("text")) ++ 
	               fromObjetoSimbolico(rightDoc.os,rightDoc.urn.getOrElse("text")) 
	  
	  //Coleta objetos na ordem TopDown
	  val collectTD: ObjetoSimbolico[_] => IndexedSeq[ObjectData] = collect[IndexedSeq, ObjectData] {
	    case o: ObjetoSimbolico[_] => objMap(o.id)
	  }
	
	  //Coleta objetos na ordem BottomUp
	  val collectBU: ObjetoSimbolico[_] => IndexedSeq[ObjectData] = collectbu[IndexedSeq, ObjectData] {
	    case o: ObjetoSimbolico[_] => objMap(o.id)
	  }
	
	  val leftTopDownObjs = collectTD(leftDoc.os)
	  val rightTopDownObjs = collectTD(rightDoc.os)
	  val leftBottomUpObjs = collectBU(leftDoc.os)
	  val rightBottomUpObjs = collectBU(rightDoc.os)
	   
	  val parentOf: Map[Long, ObjectData] = objMap.values.flatMap { od => od.cpids.map(x => (x, od)) }.toMap
	  
	  def equalByHash(ctx : EqContext) = {
	    val unmatched = ctx.unmatched    
	    val allObjs = leftBottomUpObjs ++ rightBottomUpObjs
	    
	    val hashOf: Map[Long, Int] = allObjs.foldLeft(Map[Long, Int]()) {
		    case (m, o) =>
		      val h = if (o.isSimple) { o.textHash } else { orderedHash(o.cpids.map(m)) }
		      m + (o.id -> h)
	    }
	    val rightHashMap = rightTopDownObjs
	    		.filter(ctx.unmatched contains _.id)
	    		.groupBy(o => (o.nivel, hashOf(o.id)))
	    
	    leftBottomUpObjs.foldLeft(Map[Long, Long](), ctx.matched) {
	      case ((em, um), lo) if !(em contains lo.id) =>
	        rightHashMap.get((lo.nivel, hashOf(lo.id)))
	          .flatMap(_.view.filterNot { um contains _.id }
	            .filter(lo.equalBy(em, _)).headOption) match {
	            case None => (em, um)
	            case Some(ro) => {
	              val parentPairs = if (lo.isSimple && ro.isSimple) {
	                for {
	                  lpid <- parentOf.get(lo.id).map(_.id)
	                  if !(um contains lpid)
	                  rpid <- parentOf.get(ro.id).map(_.id)
	                  if !(um contains rpid)
	                } yield (lpid, rpid)
	              } else { None }
	              (em + (lo.id -> ro.id) ++ parentPairs, um + ro.id ++ parentPairs.toList.flatMap(x => List(x._1, x._2)))
	            }
	          }
	      case (acc, _) => acc
	    }._1
	  }
	
	  def equalByTextSimiliarity(ctx: EqContext): Map[Long, Long] = {
	    def getUnmatchedTexts(i: IndexedSeq[ObjectData]) = i.filter(o => o.isSimple && (ctx.unmatched contains o.id)).groupBy(_.nivel)
	    val textsByNivel = getUnmatchedTexts(leftTopDownObjs).intersect(getUnmatchedTexts(rightTopDownObjs))
	
	    val numCompares = textsByNivel.values.map { case (x, y) => x.length * y.length }.sum
	    val numCompares5p = (numCompares * 5) / 100
	    var compareCount = 0
	    var comparePerc = 0
	    def incCompareCount() {
	      compareCount = compareCount + 1
	      if (compareCount >= numCompares5p) {
	        compareCount = 0
	        comparePerc = comparePerc + 5
	        trace(comparePerc + "% ...")
	      }
	    }
	    val unsortedBuilder = ArrayBuilder.make[(Long, Int, ObjectData, ObjectData)]
	    unsortedBuilder.sizeHint(numCompares)
	    for {
	      (ltexts, rtexts) <- textsByNivel.values.to[IndexedSeq]
	      ltext <- ltexts
	      rtext <- rtexts
	      _ = incCompareCount()
	      lt = ltext.textoNormalizado
	      rt = rtext.textoNormalizado
	      if (!lt.isEmpty() && !rt.isEmpty())
	      ratio = lt.length.toDouble / rt.length.toDouble
	      if (ratio >= minRatio && ratio <= maxRatio)
	      d = diff(lt, rt)
	      if d < 0.99
	      levelDiff = if (ltext.nivel != rtext.nivel) { 1 } else { 0 }
	    } {
	      unsortedBuilder += ((math.round(d * 100), levelDiff, ltext, rtext))
	    }
	
	    val unsorted = unsortedBuilder.result
	
	    val sorted = unsorted.sortBy(x => (x._1, x._2))
	
	    sorted.foldLeft((Map[Long, Long](), Set[Long]())) {
	      case ((sofar, matched), (_, _, ltext, rtext)) if !((matched contains ltext.id) || (matched contains rtext.id)) =>
	        val parentPairs = for {
	          lpid <- parentOf.get(ltext.id).map(_.id)
	          if (ctx.unmatched contains lpid)
	          rpid <- parentOf.get(rtext.id).map(_.id)
	          if (ctx.unmatched contains rpid)
	        } yield (lpid, rpid)
	        (sofar + (ltext.id -> rtext.id) ++ parentPairs, matched + ltext.id + rtext.id)
	      case ((sofar, matched), _) => (sofar, matched)
	    }._1
	  }
	
	  def rightParents(ctx: EqContext, o: ObjectData): IndexedSeq[Long] = {
	    val countMap = (for {
	      cpid <- o.cpids
	      rcid <- ctx.equalSoFar.get(cpid)
	      rchild <- objMap.get(rcid)
	      rparente <- parentOf.get(rchild.id)
	      rpid = rparente.id
	    } yield (rpid)).groupBy(x => x).mapValues(_.length)
	    countMap.to[IndexedSeq].map { case (x, y) => (y, x) }.sorted.reverse.map(_._2)
	  }
	
	  def equalByCommonChildren(ctx: EqContext) = {
	    leftBottomUpObjs.filter(o => o.isSimple && (ctx.unmatched contains o.id))
	      .foldLeft(Map[Long, Long](), ctx.matched) {
	        case ((m, s), lo) => {
	          val lid = lo.id
	          val rids = rightParents(ctx, lo).filterNot(s.contains)
	          rids.headOption.map { ch =>
	            val textPids = for {
	              ro <- objMap.get(ch).toSeq
	              (ltextid,rtextid) <- lo.textPids.zip(ro.textPids)              
	              if ctx.unmatched contains ltextid
	              if ctx.unmatched contains rtextid
	            } yield { (ltextid, rtextid) }
	            (m + (lid -> ch) ++ textPids, s + ch)
	          }.getOrElse((m, s))
	        }
	      }._1
	  }
	  	 
    val rels1 = rels.toList
    val rels2 = rels1 filter  { _.proveniencia.isInstanceOf[ProvenienciaUsuario] }
    val (pairs,matched) = rels2.map {
      case r : RelacaoAusenteNaOrigem[_] => (Seq(),Seq(r.dir))
      case r : RelacaoAusenteNoAlvo[_] => (Seq(),Seq(r.esq))
      case r : RelacaoIgualdade[_] => (Seq((r.esq,r.dir)),Seq(r.esq,r.dir))
      case r : RelacaoDiferenca[_] => (Seq((r.esq,r.dir)),Seq(r.esq,r.dir))
      case r : RelacaoDivisao[_] => 
        val dir = r.alvo.to[Seq]
        (Seq((r.esq,dir.head)),r.esq +: dir)
      case r : RelacaoFusao[_] => 
        val esq = r.origem.to[Seq]
        (Seq((esq.head,r.dir)),r.dir +: esq)
    }.unzip
    
    val base = pairs.flatten.toMap
    val matched2 = matched.flatten.toSet
    val unmatched = objMap.values.map(_.id).toSet -- matched2
    

    
    def classify(o : ObjectData, s : Side) : Option[String] = {
      val ct = instance.compareType
      val spec = ct.side(s)
      (ct,s,o.inAlt,o.altUrn) match {
        case (t : CPT_TextToText,_,false,_) => Some("text")
        /*case (t : CPT_TextToText,_,true,urn) => Some(urn.getOrElse(spec.defaultAltUrn))
        case (t : CPT_AltToText,SL,false,_) => None
        case (t : CPT_AltToText,SL,true,urn) => Some(urn.getOrElse(spec.defaultAltUrn))
        case (t : CPT_AltToText,SR,false,_) => Some(o.)
        case (t : CPT_AltToText,SL,true,urn) => Some(urn.getOrElse(spec.defaultAltUrn)) */
        
        case (t : CPT_TextToText,_,true,urn) => Some("alt")
        case (t : CPT_AltToText,SL,true,urn) => Some("text")
        case (t : CPT_AltToText,SR,false,_) => Some("text")        
        case (t : CPT_TextToAlt,SL,false,_) => Some("text")
        case (t : CPT_TextToAlt,SR,true,urn) => Some("text")
        case _ => None
      }      
    }
    
    def classifyAll(s : Side, l : Seq[ObjectData]) =
       l.flatMap(od => classify(od,SL).map(t => (t -> od)))
        .groupBy(_._1)
        .mapValues(_.map(_._2))
        .withDefault(_ => IndexedSeq())
        
    val classifiedLeftObjs = classifyAll(SL,leftTopDownObjs)
    val classifiedRightObjs = classifyAll(SR,rightTopDownObjs)    
    val classifiedKeys = classifiedLeftObjs.keySet ++ classifiedRightObjs.keySet
    
    val r = for {
      key <- classifiedKeys.toSeq      
    } yield {
        println("Considering key: " + key)
        val leftObjs = classifiedLeftObjs(key)        
        val leftIds = leftObjs.map(_.id).toSet
        val rightObjs = classifiedRightObjs(key)
        val rightIds = rightObjs.map(_.id).toSet
        println("LeftIds = " + leftIds)
        println("RightIds = " + rightIds)
        val allids = leftIds ++ rightIds
        val specificObjMap = objMap.filterKeys(allids)
        val specificLeftTopDownObjs = leftTopDownObjs.filter(o => leftIds(o.id))
        val specificRightTopDownObjs = rightTopDownObjs.filter(o => rightIds(o.id))
        val specificUnmatched = unmatched.filter(allids)
        val specificBase = base.filter { case (from,to) => allids(from) && allids(to) }
        
	    var ctx = EqContext(
	        specificObjMap.mapValues(o => (o.caminho, o.textoLocal)), 
	        specificLeftTopDownObjs.map(_.id), 
	        specificRightTopDownObjs.map(_.id), 
	        specificBase, 
	        specificUnmatched)
	    println("ctx[0] = " + ctx)
	    ctx = ctx + equalByHash(ctx)
	    println("ctx[1] = " + ctx)
	    ctx = ctx + equalByTextSimiliarity(ctx)
	    println("ctx[2] = " + ctx)
	    ctx = ctx + equalByCommonChildren(ctx)
	    println("ctx[3] = " + ctx)
	    val result1 = ctx.equalSoFar -- specificBase.keySet
	    val result = 
	      if (emmitOnlyForTexts) {
	    	result1.filter { 
	    	  case (id1,id2) => specificObjMap.get(id1).map(_.isSimple).getOrElse(false) && 
	    	                    specificObjMap.get(id2).map(_.isSimple).getOrElse(false) 
	    	}
	      } else { result1 }
	    val relacoes =  result.toSeq.map { 
	      case (l,r) =>  
	        val lo = specificObjMap(l)
	        val ro = specificObjMap(r)
	        val diff = diffText(lo.textoLocal.trim, ro.textoLocal.trim)        
	        if(diff.isEmpty()) {
	          RelacaoIgualdade(idSource.nextId(Tipos.RelacaoIgualdade),lo.id,ro.id,ProvenienciaSistema,())         
	        } else {
	          RelacaoDiferenca(idSource.nextId(Tipos.RelacaoIgualdade),lo.id,ro.id,diff,ProvenienciaSistema,())
	        }
	    } 
	    
	    println("relacoes = " + relacoes)
	    
	    val userRelIndex = rels1.map { r => (r.origem.toList.sorted,r.alvo.toList.sorted) }.toSet
	    def isNotSpecifiedByUser(r : Relacao[Unit]) : Boolean = !(userRelIndex contains (r.origem.toList.sorted,r.alvo.toList.sorted)) 
	    
	    rels1.map(_.setData(())).toSeq ++ relacoes.filter(isNotSpecifiedByUser)
    }
    r.flatten
  }
  
  import br.gov.lexml.symbolicobject.{Documento => IDocumento,Relacao => IRelacao}
  import java.util.{List => JList}
  
  def compareJ[T](instance : CompareProcessSpec) : JList[IRelacao] = {
      val res = compare(instance)
	  JavaConverters.seqAsJavaListConverter(res.map(_.asInstanceOf[IRelacao])).asJava
  }
}  
  
