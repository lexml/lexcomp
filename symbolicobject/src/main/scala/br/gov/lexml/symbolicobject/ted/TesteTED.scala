package br.gov.lexml.symbolicobject.ted

import br.gov.lexml.symbolicobject.impl.Documento
import br.gov.lexml.symbolicobject.impl.Relacao
import br.gov.lexml.symbolicobject.indexer.Indexer
import br.gov.lexml.symbolicobject.parser.IdSource
import br.gov.lexml.symbolicobject.parser.InputDocument
import br.gov.lexml.symbolicobject.parser.Parser
import br.gov.lexml.symbolicobject.tipos.STipo
import br.gov.lexml.treedistance._
import scalaz._
import scalaz.Scalaz._
import scala.io.Source
import java.io.File

class TesteTED {
  var index: Map[Long, STipo] = Map()
  var nextId_ : Long = 0
  val idSource = new IdSource {
    override def nextId(tipo: STipo): Long = {
      val id = nextId_
      nextId_ = id + 1
      index = index + (id -> tipo)
      id
    }
  }
  val indexer = new Indexer

  val parser = new Parser(idSource)

  val cor = new Correlacionador(indexer)

  var docsByUrn: Map[String, (Long, Node)] = Map()

  def addDocument(id: InputDocument): ValidationNel[String, Long] = {
    for {
      doc <- parser.parse(id).toValidationNel[String, Documento[Unit]]
      _ = println("doc parsed: " + doc)
      artNode <- Node.fromArticulacao(doc)
      _ = println("node: " + artNode)
    } yield {
      docsByUrn = docsByUrn + (id.urn -> (doc.id, artNode))
      println("adding to indexer")
      indexer.addDocumento(doc)
      println("document added")
      doc.id
    }
  }
  
  def extractDelta(th : TEDTheory[NodeData]) : (IndexedSeq[String],IndexedSeq[String],IndexedSeq[(String,String)]) = {
    val left = th.psLeft.positive
    val right = th.psRight.positive    
    
    val left1 = left.toIndexedSeq.map { case (nd,s) => 
      (nd.path.list.reverse.mkString(","),s.head.map(_.path.list.reverse.mkString(",")))
      }.sortBy(_._1)
    val right1 = right.toIndexedSeq.map { case (nd,s) => 
      (nd.path.list.reverse.mkString(","),s.head.map(_.path.list.reverse.mkString(",")))
      }.sortBy(_._1)
              
    val onlyOnLeft = left1 collect { case (x,OnlyLeft) => x }
    val onlyOnRight = right1 collect { case (x,OnlyRight) => x }
    val onBoth = left1.collect { case (x,OnBoth(y)) => (x,y) }
    (onlyOnLeft,onlyOnRight,onBoth) 
  }

  def correlate(): List[Relacao[Unit]] = {
    val dl = docsByUrn.values.toSeq.map(_._1)
    val result = cor.correlacionar(dl(0), dl(1))
    result foreach { relacoes =>
      relacoes.foreach { r =>
        println("Alternativa: custo = " + r.cost)
        val (onlyLeft,onlyRight,onBoth) = extractDelta(r)
        println("   Somente à esquerda: ")
        onlyLeft.foreach(x => println("        " + x))
        println("   Somente à direita: ")
        onlyRight.foreach(x => println("        " + x))
        println("   Em ambos: ")
        onBoth.foreach(x => println("        " + x._1 + " => " + x._2))        
      }
        
    }
    List()
  }

}

object TestTEDMain {
  def time[A](f : => A) = {
    val s = System.nanoTime()
    val ret = f
    println("time: "+(System.nanoTime-s)/1e6+"ms")
    ret
  }
  def main(args: Array[String]): Unit = {
    val test = new TesteTED()
    import br.gov.lexml.symbolicobject.parser._
    import br.gov.lexml.symbolicobject.tipos._
    import br.gov.lexml.symbolicobject.impl._
    val tp = Tipos.DocProjetoLei
    //val inpDoc1 = InputDocument(tp, LexMLDocument(FileSource(new java.io.File("documento1.xml"))), "urn:lex:br:federal:lei:1990-12-11;8112")
    val inpDoc1 = InputDocument(tp, Paragrafos(Source.fromFile(new File("constituicao_t.txt")).getLines()), "urn:lex:br:federal:lei:1990-12-11;8112")    
    //val inpDoc2 = InputDocument(tp, LexMLDocument(FileSource(new java.io.File("documento2.xml"))), "urn:lex:br:federal:lei:1990-12-11;8113")
    val inpDoc2 = InputDocument(tp, Paragrafos(Source.fromFile(new File("constituicao_v.txt")).getLines()), "urn:lex:br:federal:lei:1990-12-11;8113")
    test.addDocument(inpDoc1)
    test.addDocument(inpDoc2)
    
    time(test.correlate())
  }
}