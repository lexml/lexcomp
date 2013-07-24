package br.gov.lexml.symbolicobject.impl

import java.io.StringReader
import scala.collection.{JavaConversions => JC}
import br.gov.lexml.{symbolicobject => I}
import br.gov.lexml.symbolicobject.{pretty => P}
import br.gov.lexml.symbolicobject.pretty.Doc.braces
import br.gov.lexml.symbolicobject.pretty.Doc.brackets
import br.gov.lexml.symbolicobject.pretty.Doc.empty
import br.gov.lexml.symbolicobject.pretty.Doc.fillSep
import br.gov.lexml.symbolicobject.pretty.Doc.hang
import br.gov.lexml.symbolicobject.pretty.Doc.hsep
import br.gov.lexml.symbolicobject.pretty.Doc.indent
import br.gov.lexml.symbolicobject.pretty.Doc.linebreak
import br.gov.lexml.symbolicobject.pretty.Doc.list
import br.gov.lexml.symbolicobject.pretty.Doc.punctuate
import br.gov.lexml.symbolicobject.pretty.Doc.semiBraces
import br.gov.lexml.symbolicobject.pretty.Doc.sep
import br.gov.lexml.symbolicobject.pretty.Doc.text
import br.gov.lexml.symbolicobject.pretty.Doc.toDoc
import br.gov.lexml.symbolicobject.tipos.STipo
import br.gov.lexml.symbolicobject.tipos.{Tipos => T}
import br.gov.lexml.symbolicobject.tipos.Tipos
import javax.xml.parsers.DocumentBuilderFactory
import org.kiama.attribution.Attributable
import br.gov.lexml.symbolicobject.xml._
import org.w3c.dom.DocumentFragment
import org.w3c.dom.ls.DOMImplementationLS
import scala.xml.NodeSeq
import br.gov.lexml.symbolicobject.xml.WrappedNodeSeq
import scala.xml.XML
import scala.xml.Elem
import br.gov.lexml.parser.pl.output.LexmlRenderer


trait PrettyPrintable {
  val pretty : P.Doc
  final override def toString() = pretty.show(0.6,78)
}

trait Identificavel extends I.Identificavel {
  val id : SymbolicObjectId
  override final def getId() = id
}

trait Tipado extends I.Tipado {  
  val tipo : STipo
  override final def getRefTipo() = tipo
}

object Strategies {
  import org.kiama._   
  import org.kiama.rewriting.Rewriter._
  type Term = Any
  
  def collects[T](s : Term ==> T) : Term => Stream[T] = collect[Stream,T](s)     
  
  import scala.collection.generic.CanBuildFrom
  def collectbu[CC[U] <: Traversable[U], T](f : Term ==> T)(implicit cbf: CanBuildFrom[CC[T], T, CC[T]]): (Term) ⇒ CC[T] = { term =>
    val b = cbf()
    everywherebu { query { f andThen { b += _ } } } (term)
    b.result()
  }
}

object Attributes {
  import org.kiama._   
  import org.kiama.attribution.Attribution._
  
  val caminho : Attributable => Caminho = childAttr {
    case o : ObjetoSimbolico[_] => {
      case p : Posicao[_] => p -> caminho
      case _ => Caminho()
    }
    case p : Posicao[_] => {
      case o : ObjetoSimbolicoComplexo[_] => o -> caminho + p.rotulo
    }
  } 
  
  def objetoParente[T] : Attributable => Option[ObjetoSimbolicoComplexo[T]] = childAttr {
    case o : ObjetoSimbolico[_] => {
      case p : Posicao[_] => p -> objetoParente
      case _ => None
    }
    case p : Posicao[_] => {
      case o : ObjetoSimbolicoComplexo[T] => Some(o)
    }
  }
  
  val rotulo : Attributable => Option[Rotulo] = childAttr {
    case o : ObjetoSimbolico[_] => {
      case p : Posicao[_] => Some(p.rotulo)
      case _ => None      
    }
    case _ => { case _ => None }
  }
}

abstract sealed class ObjetoSimbolico[+A] extends I.ObjetoSimbolico with Tipado with Identificavel with PrettyPrintable with Attributable {

  val data : A
  import Strategies._
  
  def makeStream[T] : ObjetoSimbolico[T]  => Stream[ObjetoSimbolico[T]] = collects { 
    	case o : ObjetoSimbolico[T] => o
    }
    
  final def toStream : Stream[ObjetoSimbolico[A]] = makeStream(this)
  final def childrenStream : Stream[ObjetoSimbolico[A]] = toStream.tail
  
  def / (rs : RotuloSelector) : Query[A] = Query(this,IndexedSeq(rs))
  def changeContext[B](f : ObjetoSimbolico[A] => B) : ObjetoSimbolico[B] 
}

object ObjetoSimbolico {
  def fromObjetoSimbolico(o : I.ObjetoSimbolico) : ObjetoSimbolico[Unit] = o match {
    case os : I.ObjetoSimbolicoComplexo => ObjetoSimbolicoComplexo.fromObjetoSimbolicoComplexo(os)
    case os : I.ObjetoSimbolicoSimples => ObjetoSimbolicoSimples.fromObjetoSimbolicoSimples(os)
  }
}

final case class Posicao[+A](rotulo : Rotulo,objeto : ObjetoSimbolico[A]) extends I.Posicao with Attributable { 
  override def getRotulo() = rotulo
  override def getObjeto() = objeto
  def objetoSimbolico : Option[ObjetoSimbolico[A]] = objeto match {
     case os : ObjetoSimbolico[A] => Some(os)
     case _ => None
  }

  override def toString : String = "Posicao {rotulo: " + rotulo + ", objid: " + objeto.id + "}"
  def changeContext[B](f : ObjetoSimbolico[A] => B) : Posicao[B] = copy(objeto = objeto.changeContext(f))
}

object Posicao {
  def fromPosicao(p : I.Posicao) : Posicao[Unit] = 
	   Posicao(Rotulo.fromRotulo(p.getRotulo),ObjetoSimbolico.fromObjetoSimbolico(p.getObjeto))
  
}

trait Representavel  {
  self : I.Representavel =>
  final lazy val repr = getRepresentacao()
}

abstract sealed class Gender

case object Male extends Gender
case object Female extends Gender

final case class GenderName(name : String, gender : Gender) {
  override def toString() = name
}

abstract sealed class Rotulo extends I.Rotulo with Tipado with Representavel with PrettyPrintable   


object Rotulo {
  def fromRotulo(r : I.Rotulo) : Rotulo = r match {    
    case ro : I.RotuloOrdenado => RotuloOrdenado.fromRotuloOrdenado(ro)
    case rc : I.RotuloClassificado => RotuloClassificado.fromRotuloClassificado(rc)
    case rr : I.RoleRotulo => RotuloRole.fromRoleRotulo(rr)
  }
  
  def render(r : Rotulo) : Option[GenderName] = r match {
    case ro : RotuloOrdenado => RotuloOrdenado.render(ro)
    case rr : RotuloRole => RotuloRole.render(rr)
    case rc : RotuloClassificado => RotuloClassificado.render(rc)    
  }
}

object NumberRenderer {
  def ordinal(n : Int) = 
    if (n < 10) {
      n.toString + "º"
    } else {
      n.toString
    }
  
  def alfa(n : Int) = { 
    val d0 = n % 26 
    val n1 = n / 26 
    def h(x : Int) : List[Int] =
      if(x == 0) { List() }
      else if (x > 26) { (x % 26) :: h (x / 26) } 
      else { List(x) }
    val tl = h(n1).reverse.dropWhile(_ == 0)
    val (first :: rest) = tl :+ d0
    val ll = if(rest.isEmpty) { first :: rest } else { (first - 1) :: rest }
    ll.map(n => ('a'.toInt + n).toChar).mkString("")
  }
  
  def alfaUpper(n : Int) = alfa(n).toUpperCase
  
  
}

/**
 * 
 */
final case class RotuloRole(nomeRole : String) extends Rotulo with I.RoleRotulo {
  override val tipo = T.RotuloRole
  override final def getNomeRole() = nomeRole
  override final def getRepresentacao() = "{" + nomeRole + "}"
  override lazy val pretty = {
    import P.Doc._
    braces(text(nomeRole))
  }
}

object RotuloRole {
  def fromRoleRotulo(rr : I.RoleRotulo) = RotuloRole(rr.getNomeRole)
  def fromXML : PartialFunction[Elem,RotuloRole] = {
    case e : Elem if e.label == "RotuloRole" =>
      val nomeRole = (e \\ "@nomeRole").text.trim()
      RotuloRole(nomeRole)
  }
  def render(r : RotuloRole) : Option[GenderName] = r.nomeRole match {
    case _ => None
  }
}

/**
 * 
 */
final case class RotuloOrdenado(nomeRole : String, posicaoRole : Int*) extends Rotulo with I.RotuloOrdenado {
  override val tipo = T.RotuloOrdenado
  override final def getNomeRole() = nomeRole 
  override def getPosicaoRole() = JC.seqAsJavaList(posicaoRole.map(new java.lang.Integer(_)))
  override final def getRepresentacao() = "{" + nomeRole + posicaoRole.mkString("[",",","]") + "}"
  override lazy val pretty = {
    import P.Doc._
    val l = text(nomeRole) +: posicaoRole.toList.map(n => text(n.toString)) 
    semiBraces(l)
  }
}

object RotuloOrdenado {
  def fromRotuloOrdenado(ro : I.RotuloOrdenado) =
		RotuloOrdenado(ro.getNomeRole,JC.collectionAsScalaIterable(ro.getPosicaoRole).toSeq.map(_.toInt): _*)
  
  def render(r : RotuloOrdenado) : Option[GenderName] = {
    val nr = r.nomeRole
    val firstNum = r.posicaoRole(0)
    val secondNum = r.posicaoRole.tail.headOption
    val comp = LexmlRenderer.renderComp(secondNum).toUpperCase
    nr match {
      case "art" => Some(GenderName(
          "Art. " + LexmlRenderer.renderOrdinal(firstNum) + comp,Male))
      case "par" => Some(GenderName(
          "§ " + LexmlRenderer.renderOrdinal(firstNum) + comp,Male))
      case "inc" => Some(GenderName(
          LexmlRenderer.renderRomano(firstNum).toUpperCase + comp,Male))
      case "ali" => Some(GenderName(
          LexmlRenderer.renderAlphaSeq(firstNum).toLowerCase + comp,Female))
      case "ite" => Some(GenderName(firstNum.toString + comp,Male))
      case "prt" => Some(GenderName("Parte " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      case "liv" => Some(GenderName("Livro " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      case "tit" => Some(GenderName("Título " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      //case "subtitulo" => Some(GenderName("Sub-Título " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      case "cap" => Some(GenderName("Capítulo " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      //case "subcapitulo" => Some(GenderName("Sub-Capítulo " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      case "sec" => Some(GenderName("Seção " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      case "sub" => Some(GenderName("Sub-Seção " + LexmlRenderer.renderRomano(firstNum).toUpperCase + comp, Male))
      case _ => Some(GenderName("ops: " + nr + " " + r,Male))
    }    
  }
}

/**
 * 
 */
final case class RotuloClassificado(nomeRole : String, classificacao : String*) extends Rotulo with I.RotuloClassificado {
  override val tipo = T.RotuloClassificado
  override def getNomeRole() = nomeRole
  override def getClassificacao() = JC.seqAsJavaList(classificacao)
  override def getRepresentacao() = (nomeRole +: classificacao).mkString(";")
  override lazy val pretty = {
    import P.Doc._
    val l = text(nomeRole) +: classificacao.toList.map(text) 
    semiBraces(l)
  }
}

object RotuloClassificado {
  def fromRotuloClassificado(rc : I.RotuloClassificado) =
		  RotuloClassificado(rc.getNomeRole,JC.collectionAsScalaIterable(rc.getClassificacao).toSeq : _*)
  def render(r : RotuloClassificado) : Option[GenderName] = {
    (r.nomeRole,r.classificacao.toList) match {
      case ("paragrafo",List("unico")) => Some(GenderName("par. único",Male))
      case _ => None
    }
  }
}

/**
 * Todos excetos os textos
 */
final case class ObjetoSimbolicoComplexo[+A](id : SymbolicObjectId, tipo : STipo, data : A, posicoes : IndexedSeq[Posicao[A]]) extends ObjetoSimbolico[A] with I.ObjetoSimbolicoComplexo {
  lazy val objMap : Map[Rotulo,List[ObjetoSimbolico[A]]] = posicoes.groupBy(_.rotulo).mapValues(a => a.toList.map(_.objeto))
  lazy val javaPosicoes = JC.seqAsJavaList(posicoes : Seq[I.Posicao])
  override def getPosicoes() = javaPosicoes
  override lazy val pretty = {
    import P.Doc._
    val subels = hang(4,sep(punctuate(text(","),posicoes.toList.map { p => fillSep(List(p.rotulo.pretty,"=>",p.objeto.pretty)) }) :+ text(")")))  
    tipo.nomeTipo :: ("[" + id + "](") :: (if(posicoes.isEmpty) { empty } else { linebreak}) :: (text(data.toString)) :: subels    
  }
  override def changeContext[B](f : ObjetoSimbolico[A] => B) : ObjetoSimbolico[B] = 
    copy(data = f(this), posicoes = posicoes.map(_.changeContext(f)))
}

object ObjetoSimbolicoComplexo {
  def fromObjetoSimbolicoComplexo(os : I.ObjetoSimbolicoComplexo) : ObjetoSimbolico[Unit] = {
    val posicoes = JC.collectionAsScalaIterable(os.getPosicoes).toIndexedSeq
    if(posicoes.exists(_.getObjeto == null)) {
      sys.error("Posição com objeto nulo. Parente: id = " + os.getId + ", tipo = " + os.getRefTipo.getNomeTipo + ", posicoes = " + posicoes)
    }
    ObjetoSimbolicoComplexo(os.getId, Tipos.tipos.get(os.getRefTipo.getNomeTipo).get, (), posicoes.map(Posicao.fromPosicao))
  }
}

/**
 * Usados para representar textos
 */
abstract sealed class ObjetoSimbolicoSimples[+A] extends ObjetoSimbolico[A] with  Representavel with I.ObjetoSimbolicoSimples

object ObjetoSimbolicoSimples {
  def fromObjetoSimbolicoSimples(os : I.ObjetoSimbolicoSimples) : ObjetoSimbolicoSimples[Unit] = os match {
    case tt : I.TextoFormatado => TextoFormatado.fromTextoFormatado(tt)
    case tp : I.TextoPuro => TextoPuro.fromTextoPuro(tp)
  }
}

final case class TextoFormatado[+A](id : SymbolicObjectId, frag : WrappedNodeSeq, data : A) extends ObjetoSimbolicoSimples[A] with I.TextoFormatado {
  override val tipo = T.TextoFormatado
  override def getRepresentacao() = xhtmlFragment
  lazy val xhtmlFragment = frag.toString
  override def getXhtmlFragment() = xhtmlFragment
  override lazy val pretty = {
    import P.Doc._
    ("XHTML["+id+"]") :: text(data.toString) :: brackets(hsep(frag.toString.split(" ").toList.map(text)))    
  }
  override def changeContext[B](f : ObjetoSimbolico[A] => B) : ObjetoSimbolico[B] = copy(data = f(this))
}

object TextoFormatado {        
  def fromTextoFormatado(tf : I.TextoFormatado) : TextoFormatado[Unit] = 
		  TextoFormatado(tf.getId, WrappedNodeSeq.fromString(tf.getXhtmlFragment()), ())
}



final case class TextoPuro[+A](id : SymbolicObjectId, texto : String, data : A) extends ObjetoSimbolicoSimples[A] with I.TextoPuro {
  override val tipo = T.TextoFormatado
  override def getRepresentacao() = texto
  override def getTexto() = texto
  override lazy val pretty = {
    import P.Doc._
    ("XHTML["+id+"]") :: brackets(hsep(texto.split(" ").toList.map(text)))    
  }
  override def changeContext[B](f : ObjetoSimbolico[A] => B) : ObjetoSimbolico[B] = copy(data = f(this))
}

object TextoPuro {
  def fromTextoPuro(tp : I.TextoPuro) : TextoPuro[Unit] = 
		  TextoPuro(tp.getId,tp.getTexto,())
}

abstract sealed class Nome extends I.Nome with Representavel with PrettyPrintable

object Nome {
  def fromNome(n : I.Nome) : Nome = n match { 
    case nr : I.NomeRelativo => NomeRelativo.fromNomeRelativo(nr)
    case nc : I.NomeContexto => NomeContexto.fromNomeContexto(nc)
  }
}

final case class NomeRelativo(rotulo : Rotulo, sobreNome : Nome) extends Nome with I.NomeRelativo {
  override def getRotulo() = rotulo
  override def getSobreNome() = sobreNome
  override def getRepresentacao() = sobreNome.getRepresentacao() + "/" + rotulo.getRepresentacao()
  lazy val pretty = {
    import P.Doc._
    rotulo.pretty :+: text("<do(a)>") :+: sobreNome.pretty
  }
}

object NomeRelativo {
  def fromNomeRelativo(nr : I.NomeRelativo) : NomeRelativo = 
		  NomeRelativo(Rotulo.fromRotulo(nr.getRotulo), Nome.fromNome(nr.getSobreNome))
  
}

final case class NomeContexto(tipo : STipo) extends Nome with I.NomeContexto {
  override def getRefTipoContexto() = tipo
  override def getRepresentacao() = tipo.getNomeTipo
  lazy val pretty = {
    import P.Doc._
    text("<" + tipo.nomeTipo + ">")    
  }
}

object NomeContexto {
  def fromNomeContexto(nc : I.NomeContexto) : NomeContexto = 
		NomeContexto(Tipos.tipos.get(nc.getRefTipoContexto().getNomeTipo).get)
}

final case class Documento[+A](id : SymbolicObjectId, tipo : STipo, nome : Nome, os : ObjetoSimbolico[A]) extends I.Documento with Identificavel with Tipado with PrettyPrintable {
  override def getObjetoSimbolico() = os
  override def getNome() = nome
  override lazy val pretty = {
    import P.Doc._
    val m : Map[String,P.Doc] = Map("tipo" -> text(tipo.nomeTipo), "nome" -> nome.pretty, "os" -> os.pretty)
    val l = indent(4,sep(punctuate(text(","),m.toList.map({ case (k,v) => text(k) :+: "=>" :+: v}) :+ text(")")))) 
    (text("DOC[" + id + "](") :: linebreak :: l)
  }
}

object Documento {
  def fromDocumento(d : I.Documento) : Documento[Unit] = d match {    
    case _ => Documento(
        d.getId, 
        Tipos.tipos.get(
            d.getRefTipo
             .getNomeTipo)
         .get,
         Nome.fromNome(d.getNome),
         ObjetoSimbolico.fromObjetoSimbolico(
             d.getObjetoSimbolico))
  }
}

abstract sealed class Relacao[+A] extends I.Relacao with Identificavel with Tipado {  
  val origem : Set[SymbolicObjectId]  	//Ids de objetos simbolicos de um mesmo documento, sendo documento diferente de alvo
  val alvo : Set[SymbolicObjectId]		//Ids de objetos simbolicos de um mesmo documento, sendo documento diferente de origem
  val data : A
  val proveniencia : Proveniencia
  
  final override def getOrigem() = JC.setAsJavaSet(origem.map(Long.box)) 
  final override def getAlvo() = JC.setAsJavaSet(alvo.map(Long.box))
  final override def getProveniencia() : I.Proveniencia = proveniencia
  
  def setData[B](d : B) : Relacao[B]
  def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] 
  def setId(newId : Long) : Relacao[A]
  
}

object Relacao {
  import scala.collection.{ JavaConverters => JC }
  
  def toScala(a : java.util.Set[java.lang.Long]) : Set[SymbolicObjectId] = {
    def s = JC.asScalaSetConverter(a).asScala.toSet
    s.map(_.toLong)
  }
  
  def fromRelacao(r : I.Relacao) : Relacao[Unit] = Tipos.tipos(r.getRefTipo.getNomeTipo) match {
    case Tipos.RelacaoIgualdade => RelacaoIgualdade(r.getId,r.getOrigem.iterator().next(),r.getAlvo.iterator().next(),
    														Proveniencia.fromProveniencia(r.getProveniencia),())
    case Tipos.RelacaoAusenteNaOrigem => RelacaoAusenteNaOrigem(r.getId,r.getOrigem.iterator().next(),r.getAlvo.iterator().next(),
    														Proveniencia.fromProveniencia(r.getProveniencia),())
    case Tipos.RelacaoAusenteNoAlvo => RelacaoAusenteNoAlvo(r.getId,r.getOrigem.iterator().next(),r.getAlvo.iterator().next(),
    														Proveniencia.fromProveniencia(r.getProveniencia),())
    case Tipos.RelacaoDiferenca => RelacaoDiferenca(r.getId,r.getOrigem.iterator().next(),r.getAlvo.iterator().next(),"diff",
    														Proveniencia.fromProveniencia(r.getProveniencia),()) //FIXME: diff 
    case Tipos.RelacaoFusao => RelacaoFusao(r.getId,toScala(r.getOrigem()),r.getAlvo.iterator().next(),
    														Proveniencia.fromProveniencia(r.getProveniencia),())
    case Tipos.RelacaoDivisao => RelacaoDivisao(r.getId,r.getOrigem().iterator().next(),toScala(r.getAlvo),
    														Proveniencia.fromProveniencia(r.getProveniencia),())
/*    case Tipos.RelacaoNParaN => RelacaoNParaN(r.getId,toScala(r.getOrigem),toScala(r.getAlvo),
    														Proveniencia.fromProveniencia(r.getProveniencia),()) */
  }
}

final case class RelacaoIgualdade[+A](id : RelationId, esq : SymbolicObjectId, dir : SymbolicObjectId, proveniencia : Proveniencia, data : A) extends Relacao[A]  {
	val origem = Set(esq)
	val alvo = Set(dir)
	val tipo = Tipos.RelacaoIgualdade
	override def setData[B](d : B) = copy(data = d)
	override def setId(newId : Long) = copy(id = newId)
	override def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] =
	  	if (f(esq) && f(dir)) { Some(this) } else { None }
}

final case class RelacaoDiferenca[+A](id : RelationId, esq : SymbolicObjectId, dir : SymbolicObjectId, diff : String, proveniencia : Proveniencia, data : A) extends Relacao[A]  {
	val origem = Set(esq)
	val alvo = Set(dir)
	val tipo = Tipos.RelacaoDiferenca
	override def setData[B](d : B) = copy(data = d)
	override def setId(newId : Long) = copy(id = newId)
	override def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] =
	  	if (f(esq) && f(dir)) { Some(this) } else { None }
}

final case class RelacaoAusenteNoAlvo[+A](id : RelationId, esq : SymbolicObjectId, dir : SymbolicObjectId, proveniencia : Proveniencia, data : A) extends Relacao[A]  {
	val origem = Set(esq)
	val alvo = Set(dir)
	val tipo = Tipos.RelacaoAusenteNoAlvo
	override def setData[B](d : B) = copy(data = d)
	override def setId(newId : Long) = copy(id = newId)
	override def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] =
	  	if (f(esq) && f(dir)) { Some(this) } else { None }
}

final case class RelacaoAusenteNaOrigem[+A](id : RelationId, esq : SymbolicObjectId, dir : SymbolicObjectId, proveniencia : Proveniencia, data : A) extends Relacao[A]  {
	val origem = Set(esq)
	val alvo = Set(dir)
	val tipo = Tipos.RelacaoAusenteNaOrigem
	override def setData[B](d : B) = copy(data = d)
	override def setId(newId : Long) = copy(id = newId)
	override def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] =
	  	if (f(esq) && f(dir)) { Some(this) } else { None }
}

final case class RelacaoFusao[+A](id: RelationId, origem : Set[SymbolicObjectId], dir : SymbolicObjectId, proveniencia : Proveniencia, data : A) extends Relacao[A] {
	val alvo = Set(dir)
	val tipo = Tipos.RelacaoFusao
	override def setData[B](d : B) = copy(data = d)
	override def setId(newId : Long) = copy(id = newId)
	override def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] =
	  	if (!origem.filter(f).isEmpty && f(dir)) { Some(this) } else { None }
}

final case class RelacaoDivisao[+A](id: RelationId, esq : SymbolicObjectId, alvo : Set[SymbolicObjectId], proveniencia : Proveniencia, data : A) extends Relacao[A] {
  	val origem = Set(esq)
	val tipo = Tipos.RelacaoDivisao
	override def setData[B](d : B) = copy(data = d)
	override def setId(newId : Long) = copy(id = newId)
	override def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] =
	  	if (f(esq) && !alvo.filter(f).isEmpty) { Some(this) } else { None }
}

/*final case class RelacaoNParaN[+A](id: RelationId, origem : Set[SymbolicObjectId], alvo : Set[SymbolicObjectId], proveniencia : Proveniencia, data : A) extends Relacao[A] {
	val tipo = Tipos.RelacaoNParaN
	override def setData[B](d : B) = copy(data = d)
	override def setId(newId : Long) = copy(id = newId)
	override def filterIds(f : SymbolicObjectId => Boolean) : Option[Relacao[A]] =
	  	if (!origem.filter(f).isEmpty && !alvo.filter(f).isEmpty) { Some(this) } else { None }
}*/


final case class Caminho(rotulos : IndexedSeq[Rotulo] = IndexedSeq()) {
  def +(r : Rotulo) = Caminho(rotulos :+ r)
  def render : String = {
    val l = rotulos.reverse.toList.takeWhile { 
      case r : RotuloRole => r.nomeRole != "articulacao"
      case _ => true
    }.span {
      case r : RotuloOrdenado => r.nomeRole != "art"
      case _ => true
    } match {
      case (befArt,art::_) => befArt :+ art
      case (x,_) => x
    }
    l.map {
      case r : RotuloOrdenado => r.nomeRole + r.posicaoRole.map(_.toString).mkString("-")
      case r : RotuloClassificado => r.nomeRole + (r.classificacao.toList match {
        case "unico":: _ => "1u"
        case _ => "??"
      })
      case r : RotuloRole => r.nomeRole      
    }.reverse.mkString("_")
  }
  
  def render2 : String = {
     val l = rotulos.reverse.toList.takeWhile { 
      case r : RotuloRole => r.nomeRole != "articulacao"
      case _ => true
    }.span {
      case r : RotuloOrdenado => r.nomeRole != "art"
      case _ => true
    } match {
      case (befArt,art::_) => befArt :+ art
      case (x,_) => x
    }
    l.reverse.flatMap(Rotulo.render).mkString(", ") 
  }
}

final case class Comentario(id : Long, tipo : STipo, alvo : Long, texto : NodeSeq) extends I.Comentario with Identificavel with Tipado {
  lazy val xhtmlFragment = texto.toString
  override def getXhtmlFragment() = xhtmlFragment
  override def getAlvo() = alvo
}

object Comentario {
  def fromComentario(c : I.Comentario) : Comentario =
    Comentario(c.getId(),Tipos.tipos(c.getRefTipo.getNomeTipo),c.getAlvo,XML.loadString("<a>" + c.getXhtmlFragment + "</a>").child)
}

abstract sealed class Proveniencia extends I.Proveniencia with Tipado

object Proveniencia {
  def fromProveniencia(p : I.Proveniencia) : Proveniencia = Option(p).map(fromProveniencia1).getOrElse(ProvenienciaUsuario)
  
  def fromProveniencia1(p : I.Proveniencia) : Proveniencia = p match {
    case _ : I.ProvenienciaUsuario => ProvenienciaUsuario
    case _ : I.ProvenienciaSistema => ProvenienciaSistema
  }
}

case object ProvenienciaUsuario extends Proveniencia with I.ProvenienciaUsuario {
  val tipo = Tipos.ProvenienciaUsuario
}

case object ProvenienciaSistema extends Proveniencia with I.ProvenienciaSistema {
  val tipo = Tipos.ProvenienciaSistema
}

case class Query[+T](root : ObjetoSimbolico[T], selectors : IndexedSeq[RotuloSelector] = IndexedSeq()) {
  def query[Q >: T](elem : ObjetoSimbolico[Q], sels : IndexedSeq[RotuloSelector],pos : Option[Posicao[Q]] = None) : IndexedSeq[Posicao[Q]] = 
    if(sels.isEmpty) { pos.toIndexedSeq } else {
      val selector = sels.head
      val subSelectors = sels.tail
      elem match {
        case os : ObjetoSimbolicoComplexo[T] =>
          for {
        	  (subPos,n) <- os.posicoes.toIndexedSeq.zipWithIndex
        	  if selector.isAccepted(subPos.rotulo, n)
        	  res <- query(subPos.objeto,subSelectors,Some(subPos))
          } yield (res)
        case _ => IndexedSeq()
      }
    }
  lazy val result : IndexedSeq[Posicao[T]] = query(root,selectors)
  def /(r : RotuloSelector) : Query[T] = Query(root, selectors :+ r)
}


abstract sealed class RotuloSelector {
  def isAccepted(r : Rotulo, pos : Int) : Boolean
}

object RotuloSelector {
  implicit def functionToRS(f : (Rotulo,Int) => Boolean) : RotuloSelector = RSFromFunction(f)
  implicit def functionRotuloToRS(f : Rotulo => Boolean) : RotuloSelector = RSFromFunction { case (r,_) => f(r) }
  implicit def functionPosToRS(f : Int => Boolean) : RotuloSelector = RSFromFunction { case (_,p) => f(p) }
  implicit def rotuloToRS(r : Rotulo) : RotuloSelector = RSFromRotulo(r)
  implicit def posToRS(p : Int) : RotuloSelector = RSFromPos(p)
  implicit def roleToRS(r : String) : RotuloSelector = RSFromRole(r)  
}

case object AnyChild extends RotuloSelector {
  def isAccepted(r : Rotulo, pos : Int) = true
}

case object NoChild extends  RotuloSelector {
  def isAccepted(r : Rotulo, pos : Int) = false
}

final case class RSFromFunction(f : (Rotulo,Int) => Boolean) extends RotuloSelector {
  def isAccepted(r : Rotulo, pos : Int) = f(r,pos)
}

final case class RSFromRotulo(r : Rotulo) extends RotuloSelector {
  def isAccepted(rr : Rotulo,pos : Int) = rr == r 
}

final case class RSFromPos(p : Int) extends RotuloSelector {
  def isAccepted(r : Rotulo,pos : Int) = pos == p
}

final case class RSFromRole(roleName : String) extends RotuloSelector {
  def isAccepted(r : Rotulo,pos : Int) = r match {
    case rr : RotuloRole => rr.nomeRole == roleName
    case _ => false
  }
}

