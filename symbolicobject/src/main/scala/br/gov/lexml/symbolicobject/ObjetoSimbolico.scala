package br.gov.lexml.symbolicobject

import java.util.{List => JList, Set => JSet}
import java.lang.{Long => JLong, Integer => JInteger}


/**
 * Referencia nominal a tipos. 
 */
trait RefTipo {
  def getNomeTipo() : String
}

trait Tipado {
  def getRefTipo() : RefTipo
}

trait Identificavel extends Tipado {
  def getId() : Long
}
  
trait ObjetoSimbolico extends Identificavel 

trait ObjetoSimbolicoComplexo extends ObjetoSimbolico {
  def getPosicoes() : JList[Posicao]
}

trait Posicao {
  def getRotulo() : Rotulo
  def getObjeto() : ObjetoSimbolico
}

trait Representavel {
  def getRepresentacao() : String
}

trait Rotulo extends Tipado with Representavel 

trait RoleRotulo extends Rotulo {
  def getNomeRole() : String
}

trait RotuloOrdenado extends RoleRotulo {
  def getPosicaoRole() : JList[java.lang.Integer]
}

trait RotuloClassificado extends RoleRotulo {
  def getClassificacao() : JList[java.lang.String]
}

trait ObjetoSimbolicoSimples extends ObjetoSimbolico with Representavel

trait TextoFormatado extends ObjetoSimbolicoSimples {
  def getXhtmlFragment() : String
}

trait TextoPuro extends ObjetoSimbolicoSimples {
  def getTexto() : String
}

trait Intervalo {  
  def getPrimeiroId() : Long
  def getUltimoId() : Long
}

trait Nome extends Representavel

trait NomeRelativo extends Nome {
  def getRotulo() : Rotulo
  def getSobreNome() : Nome
}

trait NomeContexto extends Nome {
  def getRefTipoContexto() : RefTipo
}

trait Documento extends Identificavel {
  def getObjetoSimbolico() : ObjetoSimbolico
  def getNome() : Nome
} 

trait Relacao extends Identificavel {
  def getOrigem() : JSet[java.lang.Long]
  def getAlvo() : JSet[java.lang.Long]
  def getProveniencia() : Proveniencia
}

trait Comentario extends Identificavel {
  def getXhtmlFragment() : String
  def getAlvo() : java.lang.Long
}

trait Proveniencia extends Tipado 

trait ProvenienciaUsuario extends Proveniencia

trait ProvenienciaSistema extends Proveniencia

