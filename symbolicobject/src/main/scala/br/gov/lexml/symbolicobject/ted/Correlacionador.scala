package br.gov.lexml.symbolicobject.ted

import br.gov.lexml.symbolicobject.indexer.IIndexer
import java.util.{List=>JList,ArrayList}
import br.gov.lexml.symbolicobject.Relacao


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
	def correlaciar(docId1 : Long, docId2 : Long) : JList[Relacao]
}

class Correlacionador(idx: IIndexer) extends ICorrelacionador {
    private var conf : SConfiguracao = SConfiguracao()
    def setConfiguracao(config : Configuracao) {
      conf = new SConfiguracao(conf)
    }
	def getConfiguracao() : Configuracao = conf 
	def resetConfiguracao() { conf = SConfiguracao()}
	def correlaciar(docId1 : Long, docId2 : Long) : JList[Relacao] = {
	  new ArrayList[Relacao]()
	}
}