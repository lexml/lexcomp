package br.gov.lexml.symbolicobject.comp

import br.gov.lexml.symbolicobject.impl._
import br.gov.lexml.symbolicobject.tipos._
import scala.collection.immutable.SortedSet
import scala.collection.generic.CanBuildFrom
import org.kiama.attribution.Attributable
import br.gov.lexml.symbolicobject.parser.IdSource
import br.gov.lexml.symbolicobject.parser.InputDocument
import br.gov.lexml.symbolicobject.parser.Parser
import scala.io.Source
import java.io.File
import br.gov.lexml.lexmldiff.LexmlDiff
import scala.collection.SortedMap
import java.io.PrintWriter
import java.io.FileWriter
import scala.xml.NodeSeq
import br.gov.lexml.symbolicobject.pretty.Empty
import scala.collection.GenTraversableLike
import scala.collection.generic.IsTraversableOnce
import scala.collection.mutable.ArrayBuilder
import org.apache.commons.io.FileUtils
import scala.xml.Text
import scala.xml.PrettyPrinter
import scala.collection.parallel.ForkJoinTaskSupport

trait CompareProcessConfiguration {
  def normalize(t: String): String = {
    import br.gov.lexml.parser.pl.text.normalizer
    normalizer.normalize(t.replaceAll("[0-9]+", "#").toLowerCase())
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

object CollectionUtils {
  implicit class SelectableCollection[A, Repr](val r: GenTraversableLike[A, Repr]) {
    final def select[T <: A](implicit m: Manifest[T],
      fr: IsTraversableOnce[Repr],
      cbf: CanBuildFrom[Repr, T, Repr { type A = T }]): Repr { type A = T } =
      r collect { case x: T => x }
  }

  implicit class IntersectingMap[K, V1](val m: Map[K, V1]) {
    final def intersect[V2](m1: Map[K, V2]): Map[K, (V1, V2)] = m.seq.flatMap { case (k, v1) => m1.get(k).map(v2 => (k, (v1, v2))) }
  }
}

final case class EqContext(objMap: Map[Long, (Caminho, String)], left: IndexedSeq[Long], right: IndexedSeq[Long], equalSoFar: Map[Long, Long] = Map(), unmatched: Set[Long]) {
  def +(m1: Map[Long, Long]) = {
    val c = EqContext(objMap, left, right, equalSoFar ++ m1, unmatched -- m1.keySet -- m1.values)
    val added = c.equalSoFar.size * 2 - equalSoFar.size * 2
    val addedPer = added.toDouble * 100.0 / objMap.size.toDouble
    println(f"Added $added ($addedPer%03.2f%)")
    println("now : " + c)
    c
  }
  lazy val matched = equalSoFar.keySet ++ equalSoFar.values.to[Set]
  override def toString() : String = {
    val total = objMap.size
    val onLeft = left.length
    val onRight = right.length
    val numMatched = equalSoFar.size * 2
    val numUnmatched = total - numMatched
    val perMatched = numMatched.toDouble * 100.0 / total.toDouble
    val perUnmatched = numUnmatched.toDouble * 100.0 / total.toDouble
    f"EqContext: total = $total, left = $onLeft, right = $onRight, matched = $numMatched, unmatched=$numUnmatched, ($perMatched%03.2f%/$perUnmatched%03.2f%)" 
  }
}

object CompareProcess {
  def compare(conf: CompareProcessConfiguration, docs: Documento[_]*): Map[Long, Map[Long, EqContext]] = {        
    val compPairs = (for {
      tails <- docs.tails.filter(_.length >= 2)
      left = tails.head
      right <- tails.tail
    } yield {
      (left, right)
    }).to[List]
    val total = compPairs.length.toDouble / 100.0
    var num = 0.0
    import scala.concurrent._
    val ec = ExecutionContext.global
    val cp = compPairs.par
    cp.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(8))
    val rp = cp.map { 
      case (d1,d2) => 
        val pref = "[" + d1.nome.repr + " -> " + d2.nome.repr + "] :"        
        val cp = new CompareProcess(pref,d1, d2, conf)
        val res = cp.compare()       
        (d1.id,d2.id,res)
    }
    rp.seq.foldLeft(Map[Long, Map[Long, EqContext]]().withDefault(Map())) {
      case (m, (d1id,d2id,res)) =>                
        m + (d1id -> (m.getOrElse(d1id,Map()) + (d2id -> res)))
    }
  }
}

class CompareProcess(desc : String,leftDoc: Documento[_], rightDoc: Documento[_], conf: CompareProcessConfiguration) {
  import CompareProcess._
  import org.kiama.attribution.Attribution._
  import org.kiama.rewriting.Rewriter._
  import scala.util.hashing.MurmurHash3._
  import Attributes._
  import Strategies._
  import conf._

  val diff_ = diff _

  def pr(t : String) = println(desc + t)
  import CollectionUtils._

  final case class ObjectData(o: ObjetoSimbolico[_]) {
    val A = Attributes
    val id = o.id

    val caminho: Caminho = o -> A.caminho
    val textPid = o match {
      case oc: ObjetoSimbolicoComplexo[_] => (oc / "texto").result.headOption.map(_.objeto.id)
      case _ => None
    }
    /*lazy val hash: Int = o match {
      case o: ObjetoSimbolicoComplexo[_] => orderedHash(o.posicoes.map(_.objeto -> hash))
      case _ => stringHash(normalize(texto))
    }*/
    lazy val textoLocal = o match {
      case oc: ObjetoSimbolicoComplexo[_] => ""
      case t: TextoPuro[_] => t.texto
      case t: TextoFormatado[_] => t.frag.ns.text
    } 
    lazy val textoLocalNormalizado = normalize(textoLocal)
    lazy val texto = o match {
      case oc: ObjetoSimbolicoComplexo[_] => (oc / "texto").result.headOption.map(_.objeto).collect {
        case t: TextoPuro[_] => t.texto
        case t: TextoFormatado[_] => t.frag.ns.text
      }.getOrElse("")
      case t: TextoPuro[_] => t.texto
      case t: TextoFormatado[_] => t.frag.ns.text
    }
    lazy val textoNormalizado = normalize(texto)
    lazy val textHash = stringHash(textoNormalizado)
    lazy val tipo = o match {
      case _: ObjetoSimbolicoComplexo[_] => o.tipo.nomeTipo
      case os: ObjetoSimbolicoSimples[_] => (o -> objetoParente).get.tipo.nomeTipo + "_text"
    }
    def equalBy(m: Map[Long, Long], od: ObjectData): Boolean = {
      m.get(id) == Some(od.id) || ((o, od.o) match {
        case (lo: ObjetoSimbolicoComplexo[_], ro: ObjetoSimbolicoComplexo[_]) =>
          (lo.posicoes.length == ro.posicoes.length) && lo.posicoes.zip(ro.posicoes).forall {
            case (pl, pr) => m.get(pl.objeto.id) == Some(pr.objeto.id)
          }
        case _ => normalize(texto) == normalize(od.texto)
      })
    }
    lazy val isSimple = o.isInstanceOf[ObjetoSimbolicoSimples[_]]
    lazy val children: IndexedSeq[ObjetoSimbolico[_]] = o match {
      case oc: ObjetoSimbolicoComplexo[_] => oc.posicoes.map(_.objeto)
      case _ => IndexedSeq()
    }
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
  }

  //Inicializa rotinas do Kiama 
  leftDoc.os.initTreeProperties()
  rightDoc.os.initTreeProperties()

  //Coleta objetos na ordem TopDown
  val collectTD: ObjetoSimbolico[_] => IndexedSeq[ObjectData] = collect[IndexedSeq, ObjectData] {
    case o: ObjetoSimbolico[_] => ObjectData(o)
  }

  //Coleta objetos na ordem BottomUp
  val collectBU: ObjetoSimbolico[_] => IndexedSeq[ObjectData] = collectbu[IndexedSeq, ObjectData] {
    case o: ObjetoSimbolico[_] => ObjectData(o)
  }

  val leftTopDownObjs = collectTD(leftDoc.os)
  val rightTopDownObjs = collectTD(rightDoc.os)
  val leftBottomUpObjs = collectBU(leftDoc.os)
  val rightBottomUpObjs = collectBU(rightDoc.os)

  pr("   Comparing " + leftTopDownObjs.length + " objects on the left with " +
      rightTopDownObjs.length + " objects on the right")
  
  val objMap = {
    var m: Map[Long, ObjectData] = Map()
    leftTopDownObjs foreach { o => m = m + (o.id -> o) }
    rightTopDownObjs foreach { o => m = m + (o.id -> o) }
    m
  }

  val parentOf: Map[Long, ObjectData] = objMap.values.flatMap { od => od.cpids.map(x => (x, od)) }.toMap

  val hashOf: Map[Long, Int] = (leftBottomUpObjs ++ rightBottomUpObjs).foldLeft(Map[Long, Int]()) {
    case (m, o) =>
      val h = if (o.isSimple) { o.textHash } else { orderedHash(o.cpids.map(m)) }
      m + (o.id -> h)
  }

  def equalByHash() = {
    val rightHashMap = rightTopDownObjs.groupBy(o => (o.tipo, hashOf(o.id)))

    leftBottomUpObjs.foldLeft(Map[Long, Long](), Set[Long]()) {
      case ((em, um), lo) if !(em contains lo.id) =>
        rightHashMap.get((lo.tipo, hashOf(lo.id)))
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
    def getUnmatchedTexts(i: IndexedSeq[ObjectData]) = i.filter(o => o.isSimple && (ctx.unmatched contains o.id)).groupBy(_.tipo)
    val textsByTipo = getUnmatchedTexts(leftTopDownObjs).intersect(getUnmatchedTexts(rightTopDownObjs))

    val numCompares = textsByTipo.values.map { case (x, y) => x.length * y.length }.sum
    val numCompares5p = (numCompares * 5) / 100
    var compareCount = 0
    var comparePerc = 0
    def incCompareCount() {
      compareCount = compareCount + 1
      if (compareCount >= numCompares5p) {
        compareCount = 0
        comparePerc = comparePerc + 5
        pr(comparePerc + "% ...")
      }
    }
    val unsortedBuilder = ArrayBuilder.make[(Long, Int, ObjectData, ObjectData)]
    unsortedBuilder.sizeHint(numCompares)
    for {
      (ltexts, rtexts) <- textsByTipo.values.to[IndexedSeq]
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
      levelDiff = if (ltext.tipo != rtext.tipo) { 1 } else { 0 }
    } {
      unsortedBuilder += ((math.round(d * 100), levelDiff, ltext, rtext))
    }

    pr("sorting....")
    val unsorted = unsortedBuilder.result
    pr("sorted.")

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
          val rids = rightParents(ctx, lo).filter(id => !s.contains(id))
          rids.filterNot(s.contains).headOption.map { ch =>
            val textPids = for {
              ro <- objMap.get(ch)
              ltextid <- lo.textPid
              rtextid <- ro.textPid
              if ctx.unmatched contains ltextid
              if ctx.unmatched contains rtextid
            } yield { (ltextid, rtextid) }
            (m + (lid -> ch) ++ textPids, s + ch)
          }.getOrElse((m, s))
        }
      }._1
  }

  /*def equalByCommonSiblings(ctx : EqContext) = {
    val prev = (objMap.values.flatMap { 
      case o if !o.cpids.isEmpty => { 
    	  val init = o.cpids.init
    	  val tail = o.cpids.tail
    	  tail.zip(init)
      } 
      case _ => IndexedSeq()        
    }).toMap 
   val next = objMap.map { case (x,y) => (y,x) }
   
   
  }*/

  def compare() = {
    var ctx = EqContext(objMap.mapValues(o => (o.caminho, o.textoLocal)), leftTopDownObjs.map(_.id), rightTopDownObjs.map(_.id), Map(), objMap.keySet)
    ctx = ctx + equalByHash()
    ctx = ctx + equalByTextSimiliarity(ctx)
    ctx = ctx + equalByCommonChildren(ctx)
    ctx
  }

}

class TesteComparator {

  var nextId_ : Long = 0
  val idSource = new IdSource {
    override def nextId(tipo: STipo): Long = {
      val id = nextId_
      nextId_ = id + 1
      id
    }
  }

  val parser = new Parser(idSource)

  import scalaz._

  def readDocument(id: InputDocument): ValidationNel[String, Documento[Unit]] =
    parser.parse(id).toValidationNel[String, Documento[Unit]]

}

object TestComparator {
  def time[A](f: => A) = {
    val s = System.nanoTime()
    val ret = f
    System.err.println("time: " + (System.nanoTime - s) / 1e6 + "ms")
    ret
  }

  import br.gov.lexml.symbolicobject.impl.Attributes._

  def main(args: Array[String]): Unit = {

    import br.gov.lexml.symbolicobject.parser._

    import br.gov.lexml.symbolicobject.tipos._
    import br.gov.lexml.symbolicobject.impl._

    var nextId_ : Long = 0
    val idSource = new IdSource {
      override def nextId(tipo: STipo): Long = {
        val id = nextId_
        nextId_ = id + 1
        id
      }
    }

    val parser = new Parser(idSource)

    import scalaz.{ Source => _, _ }

    def readDocument(id: InputDocument): ValidationNel[String, Documento[Unit]] =
      parser.parse(id).toValidationNel[String, Documento[Unit]]

    val tp = Tipos.DocProjetoLei

    val srcDir = new File("src/test/constituicao")
    val dstDir = new File("target/result")
    dstDir.mkdirs()

    val srcFiles = Source.fromFile(new File(srcDir, "texts.lst")).getLines().toIndexedSeq.zipWithIndex.map {
      case (fname, n) =>
        val urn = "urn:lex:br:federal:lei:1905-01-01;" + (n + 1)        
        val inpDoc = InputDocument(tp, Paragrafos(Source.fromFile(new File(srcDir, fname)).getLines()), urn)
        val doc = readDocument(inpDoc).getOrElse(sys.error("Error parsing: " + fname))
        val inpDoc2 = InputDocument(tp, Paragrafos(Source.fromFile(new File(srcDir, fname)).getLines()), urn)
        val xml = inpDoc2.doc.elem().getOrElse(sys.error("Wrong!"))        
        (fname, xml, doc,urn)
    }

    
    val idToFname = srcFiles.map { case (fname, _, doc,_) => (doc.id, fname) }.toMap

    for {
      (fname, xml, _,_) <- srcFiles
    } {
      val dstFile = new File(dstDir, fname + ".xml")
      FileUtils.writeStringToFile(dstFile, xml.toString)
    }

    val docs = srcFiles.map(_._3)

    val docsById = docs.map(d => (d.id, d)).toMap

    val conf = new CompareProcessConfiguration {}

    val res = CompareProcess.compare(conf, docs: _*)

    val result = (<ComparisionReport>
    				<Fontes> {
    					for {
    					  (fname,xml,_,urn) <- srcFiles
    					} yield {
    					  <Fonte fileName={fname} generatedUrn={urn}/>    					  
    					}
    				} </Fontes>
                    {
                      for {
                        (srcId, sm) <- res.toSeq
                        val srcDoc = docsById(srcId)
                        srcFile = idToFname(srcId)
                        (dstId, ctx) <- sm.toSeq
                        val dstDoc = docsById(dstId)
                        dstFile = idToFname(dstId)
                        objMap = ctx.objMap
                        numTotal = objMap.size
                        numMatched = ctx.matched.size
                        numUnmatched = numTotal - numMatched
                        perMatched = "%03.2f%%".format(100.0 * numMatched / numTotal)
                        perUnmatched = "%03.2f%%".format(100.0 * numUnmatched / numTotal)
                      } yield {
                        <Comparision>
                          <Source>{ srcFile }</Source>
                          <Target>{ dstFile }</Target>
                          <Statistics>
                            <Total>{ numTotal }</Total>
                            <Matched>{ numMatched }</Matched>
                            <MatchedPerc>{ perMatched }</MatchedPerc>
                            <Unmatched>{ numUnmatched }</Unmatched>
                            <UnmatchedPerc>{ perUnmatched }</UnmatchedPerc>
                          </Statistics>
                          <Matches>
                            {
                            for {
                              fid <- ctx.left
                              tid <- ctx.equalSoFar.get(fid)
                              (c1, t1) <- ctx.objMap.get(fid)
                              (c2, t2) <- ctx.objMap.get(tid)
                            } yield {
                              (<Match from={ c1.render } to={ c2.render }>
                                { conf.diffX(t1, t2) }
                              </Match>)
                            }
                          } 
 						  </Matches>
                          <NonMatches>
                            <Left> {
                              for {
                                id <- ctx.left
                                if ctx.unmatched contains id
                                (c, t) <- ctx.objMap.get(id)
                              } yield {
                                <NonMatch nid={ c.render }>
                                { t }</NonMatch>
                              }
                            } </Left>
                            <Right> {
                              for {
                                id <- ctx.right
                                if ctx.unmatched contains id
                                (c, t) <- ctx.objMap.get(id)
                              } yield {
                                <NonMatch nid={ c.render }>
                                { t }</NonMatch>
                              }
                            } </Right>
                          </NonMatches>
                        </Comparision>
                      }
                    }
                  </ComparisionReport>)
    val pp = new PrettyPrinter(130,4)
    val s = "<?xml version='1.0' encoding='UTF-8'>\n" + pp.format(result)
    FileUtils.writeStringToFile(new File(dstDir,"result.xml"),s)
  }

}

