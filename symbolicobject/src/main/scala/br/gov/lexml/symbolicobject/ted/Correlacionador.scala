package br.gov.lexml.symbolicobject.ted

import br.gov.lexml.symbolicobject.indexer.IIndexer
import java.util.{List=>JList,ArrayList}
import br.gov.lexml.{symbolicobject => I}
import br.gov.lexml.symbolicobject.impl._
import scalaz._
import Scalaz._
import br.gov.lexml.treedistance.TEDTheory

trait Configuracao {
	def getLimiarSimilaridade() : Double	
}

case class SConfiguracao(limiarSimilaridade : Double = 0.9) extends Configuracao {
  def this(cfg : Configuracao) = this(cfg.getLimiarSimilaridade)
  override def getLimiarSimilaridade = limiarSimilaridade
}

trait ICorrelacionador {    
	def setConfiguracao(conf : Configuracao)
	def getConfiguracao() : Configuracao
	def resetConfiguracao() 
	def correlacionarJ(docId1 : Long, docId2 : Long) : JList[I.Relacao]
}

class Correlacionador(idx: IIndexer) extends ICorrelacionador {
    private var conf : SConfiguracao = SConfiguracao()
    def setConfiguracao(config : Configuracao) {
      conf = new SConfiguracao(conf)
    }
	def getConfiguracao() : Configuracao = conf 
	def resetConfiguracao() { conf = SConfiguracao()}
	//def correlacionar(docId1 : Long, docId2 : Long) : List[Relacao[Unit]] = Nil
	override def correlacionarJ(docId1 : Long, docId2 : Long) : JList[I.Relacao] = {
	  //import scala.collection.{JavaConverters => JC}
	  //JC.seqAsJavaListConverter(correlacionar(docId1,docId2).toOption.get.toList.map(_.asInstanceOf[I.Relacao])).asJava
	  ???
	} 
	
	def correlacionar(docId1 : Long, docId2 : Long, params : NodeTEDParams = NodeTEDParams()) : ValidationNel[String,Set[TEDTheory[NodeData]]]= {	  
	  val solver = new NodeTED(params)
	  import solver._
	  for { 
	    doc1 <- idx.getDocumento(docId1).toSuccess("Doc1: " + docId1 + " not found.").toValidationNel[String,Documento[_]]
	    node1 <- Node.fromArticulacao(doc1)
	    doc2 <- idx.getDocumento(docId2).toSuccess("Doc2: " + docId2 + " not found.").toValidationNel[String,Documento[_]]
	    node2 <- Node.fromArticulacao(doc2)	    
	  } yield { 
	    solve(TEDProblem(List(node1),List(node2)))	    
	  }	  
	}
	  
}