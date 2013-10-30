package br.gov.lexml.symbolicobject.tipos

import java.util.{Set => JSet}

import scala.beans.BeanProperty
import scala.collection.{JavaConversions => JC}

import br.gov.lexml.symbolicobject.RefTipo

/**
 * Representa os conceitos usados no modelo. Conceitos como "artigo" e "parágrafo" não possuem
 * uma representação explícita como classe no pacote, mas são representáveis como uma instância
 * de ObjetoSimbolico com o tipo "artigo" como um dos elementos retornados por [[Tipo.getRefTipo]].
 */
trait Tipo {  
  def getNomeTipo(): String
  def getTiposParentes() : JSet[Tipo]
  def getSuperTipos(): JSet[Tipo]
  def getDescricaoTipo() : String
}

final case class STipo(@BeanProperty nomeTipo : String, @BeanProperty descricaoTipo : String, _tiposParentes : STipo*) extends Tipo with RefTipo {
  lazy val tiposParentes : Set[STipo] = _tiposParentes.toSet
  lazy val javaTiposParentes : JSet[Tipo] = JC.setAsJavaSet(tiposParentes.map(x => x : Tipo))
  lazy val superTipos : Set[STipo] = 
    tiposParentes ++ tiposParentes.flatMap(_.superTipos)
  lazy val javaSuperTipos : JSet[Tipo] = JC.setAsJavaSet(superTipos.map(x => (x : Tipo)))  
  override def getSuperTipos() = javaSuperTipos
  override def getTiposParentes() = javaTiposParentes
  def tipoESuperTipos() = superTipos + this
}

object Tipos {
  
  var tipos : Map[String,STipo] = Map()
  
  private[this] def newTipo(nomeTipo : String, descricaoTipo : String, _superTipos : STipo*) : STipo = {
    val t = STipo(nomeTipo, descricaoTipo, _superTipos : _*)
    tipos = tipos + (nomeTipo -> t)
    t
  }
  
  final val ObjetoSimbolico = newTipo("objeto_simbolico", "Objeto simbólico")
  final val ObjetoSimbolicoSimples  = newTipo("objeto_simbolico_simples", "Objeto simbólico simples", ObjetoSimbolico)
  final val ObjetoSimbolicoComplexo  = newTipo("objeto_simbolico_complexo","Objeto simbólico complexo", ObjetoSimbolico)
  final val Rotulo = newTipo("rotulo", "Rótulo")
  final val RotuloRole = newTipo("role_rotulo","Rótulo role", Rotulo)
  final val RotuloOrdenado  = newTipo("rotulo_ordenado","Rótulo ordenado", RotuloRole)
  final val RotuloClassificado  = newTipo("rotulo_classificado","Rótudo classificado", RotuloRole)
  final val TextoFormatado  = newTipo("texto_formatado","Texto formatado", ObjetoSimbolicoSimples)
  final val TextoPuro  = newTipo("texto_puro","Texto puro", ObjetoSimbolicoSimples)
  final val Omissis  = newTipo("omissis","Omissis", ObjetoSimbolicoSimples)
  final val Contexto = newTipo("contexto", "Contexto")
  final val TodosDocumentos = newTipo("todos_documentos", "Todos os documentos", Contexto)
  final val Documento  = newTipo("documento", "Documento")
  final val RaizDocumento = newTipo("raiz_documento","Raiz do documento", Contexto)
  final val Alteracao = newTipo("os_alteracao","Alteração", Contexto)
  final val Articulacao = newTipo("articulacao","Articulação", ObjetoSimbolicoComplexo)  
  final val ElementoArticulacao = newTipo("elemento_articulacao","Elemento articulação", ObjetoSimbolicoComplexo)
  final val Dispositivo = newTipo("dispositivo","Dispositivo",ElementoArticulacao)
  final val OsArtigo = newTipo("os_artigo","Artigo", Dispositivo)
  final val OsCaput = newTipo("os_caput","Caput", Dispositivo)
  final val OsParagrafo = newTipo("os_paragrafo","Parágrafo",Dispositivo)
  final val OsInciso = newTipo("os_inciso","Inciso",Dispositivo)
  final val OsAlinea = newTipo("os_alinea","Alínea",Dispositivo)
  final val OsItem = newTipo("os_item","Item", Dispositivo)
  final val Agrupadores = newTipo("agrupadores","Agrupadores",ObjetoSimbolicoComplexo)
  final val Agrupador = newTipo("agrupador","Agrupador",ObjetoSimbolicoComplexo)
  final val OsTitulo = newTipo("os_titulo","Título",Agrupador)
  final val OsLivro = newTipo("os_livro","Livro",Agrupador)
  final val OsParte= newTipo("os_parte","Parte",Agrupador)
  final val OsCapitulo = newTipo("os_capitulo","Capítulo",Agrupador)
  final val OsSecao = newTipo("os_secao","Seção",Agrupador)
  final val OsSubSecao = newTipo("os_subsecao","Seção",Agrupador)
  final val DocProjetoLei = newTipo("doc_proj_lei","Projeto de Lei",Documento)
  final val Proveniencia = newTipo("proveniencia","Proveniencia");
  final val ProvenienciaUsuario = newTipo("proveniencia_usuario","Proveniente do Usuário");
  final val ProvenienciaSistema = newTipo("proveniencia_sistema","Proveniente do Sistema");
  //relações
  
  final val Relacao = newTipo("relacao", "Relação")
  
  private[this] var listTipoRelacao : List[(String, STipo)] = List()
  
  private[this] def newRelacao(nomeTipo : String, descricaoTipo : String, cardinalidadeInicial: String) : STipo = {
    val sTipo = newTipo(nomeTipo, descricaoTipo, Relacao)
    listTipoRelacao = (cardinalidadeInicial, sTipo) :: listTipoRelacao
    sTipo
  }

  import java.util.{Map => JMap}
  
  lazy val tiposRelacaoJava: JMap[String, JMap[String, String]] = {
    import scala.collection.JavaConversions._
    
    val tipos = listTipoRelacao.
                   groupBy(_._1).
                   map{ case (k, v) => k -> v.map(
                		   	f => f._2.nomeTipo -> f._2.descricaoTipo).toMap }

    tipos.map { case (k, v) => k -> (v : JMap[String,String]) }
  }

  
  final val RelacaoIgualdade = newRelacao("relacao_igualdade","Relação de igualdade", "1:1")
  final val RelacaoDiferenca = newRelacao("relacao_diferenca","Relação de diferença", "1:1")
  final val RelacaoAusenteNoAlvo = newRelacao("relacao_ausente_alvo","Relação de ausente no alvo", "1:0")
  final val RelacaoAusenteNaOrigem = newRelacao("relacao_ausente_origem","Relação de ausente na origem", "0:1")
  final val RelacaoFusao = newRelacao("relacao_fusao","Relação de fusão", "n:1")
  final val RelacaoDivisao = newRelacao("relacao_divisao","Relação de divisão", "1:n")
  final val RelacaoNParaN = newRelacao("relacao_n_para_n","Relação n para n", "n:n")
  
}
