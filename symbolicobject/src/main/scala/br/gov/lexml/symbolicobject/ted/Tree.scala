package br.gov.lexml.symbolicobject.ted

import scala.xml.NodeSeq
import org.kiama.attribution.Attribution._
import scalaz.NonEmptyList
import br.gov.lexml.symbolicobject.tipos.STipo
import br.gov.lexml.symbolicobject.impl.Rotulo
import br.gov.lexml.treedistance._
import br.gov.lexml.symbolicobject.tipos.Tipos
import br.gov.lexml.symbolicobject.impl.RotuloOrdenado
import br.gov.lexml.symbolicobject.impl.RotuloClassificado
import br.gov.lexml.lexmldiff.DiffCase
import br.gov.lexml.lexmldiff.LexmlDiff
import org.kiama.attribution.Attributable
import br.gov.lexml.symbolicobject.impl.Posicao
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolicoComplexo
import br.gov.lexml.symbolicobject.impl.TextoPuro
import scala.xml.Text
import br.gov.lexml.symbolicobject.impl.TextoFormatado
import scalaz._
import Scalaz._
import NonEmptyList._
import br.gov.lexml.symbolicobject.impl.Documento

final case class Node(nd: NodeData, subForest: List[Node] = List())

object Node {

  def fromObjetoSimbolicoComplexo(os: ObjetoSimbolicoComplexo[_], path: NonEmptyList[Rotulo]): ValidationNel[String, Node] = {
    val posicoes = os.posicoes.toList
    val subNodes = posicoes.map(fromPosicao(_, path.list).map(List(_))).concatenate.map(_.flatten)
    val node = subNodes.map(sns => Node(NodeData.fromObjetoSimbolicoComplexo(os, path), sns))
    node
  }

  def fromPosicao(p: Posicao[_], parentPath: List[Rotulo]): ValidationNel[String, Option[Node]] = p.objeto match {
    case os: ObjetoSimbolicoComplexo[_] => fromObjetoSimbolicoComplexo(os, nel(p.rotulo, parentPath)).map(Some(_))
    case _ => None.successNel[String] //("fromPosicao: expecting ObjetoSimbolicoComplexo, got:  " + p.objeto).failNel[Node]
  }

  def fromArticulacao(d: Documento[_]): ValidationNel[String, Node] =
    (d.os / "articulacao")
      .result
      .headOption
      .toSuccess("Articulação não encontrada em :  " + d)
      .toValidationNel[String, Posicao[_]]
      .flatMap(fromPosicao(_, Nil)).map(_.get)
   
}

final case class NodeData(
  id: Long,
  path: NonEmptyList[Rotulo],
  tipo: STipo,
  texto: Option[(Long, NodeSeq)] = None) {
  override def toString() = "[%s:%d] %s".format(
    tipo.nomeTipo, id, texto.map(x => ": \"" + x._2.toString.take(12) + "\"").getOrElse(""))
  override def hashCode() = id.toInt
  override def equals(a : Any) = a match {
    case nd : NodeData => id == nd.id
    case _ => false
  }  
}

object NodeData {
  def fromObjetoSimbolicoComplexo(os: ObjetoSimbolicoComplexo[_], path: NonEmptyList[Rotulo]) = {
    val texto: Option[(Long, NodeSeq)] =
      (os / "texto").result.headOption.map(_.objeto).collect {
        case t: TextoPuro[_] => (t.id, Text(t.texto))
        case t: TextoFormatado[_] => (t.id, t.frag.ns)
      }
    NodeData(os.id, path, os.tipo, texto)
  }
}

case class NodeTEDParams(
  tipoWeight: Double = 5.0,
  rotuloWeight: Double = 1.0,
  diffWeight: Double = 3.0,
  replaceFactor: Double = 1.5,
  onlyOnLeftCost: Double = 1.0,
  onlyOnRightCost: Double = 1.0,
  replaceDispsDifTipos: Double = 0.2,
  replaceAgrupsDifTipos: Double = 0.1,
  replaceRotuloDifNums: Double = 0.5,
  replaceRotuloDifRoles: Double = 1.0,
  rotuloUnicoPor1: Double = 0.1,
  maxPartialDiff: Double = 0.8,
  diffIgnoreCase: Boolean = true) {
  val totalWeight = tipoWeight + rotuloWeight + diffWeight
}

class NodeTED(val params: NodeTEDParams) extends TED {
  
  val doDebug = false
      
  def debug(msg : => String) = if(doDebug) { println(msg) } else { }
    
  
  type A = NodeData
  type T = Node
  override implicit val treeOps = new TreeOps[Node, NodeData] {
    override def value(n: Node) = n.nd
    override def subForest(n: Node) = n.subForest
  }
  def replaceCost(tl: STipo, tr: STipo): Double = {
    import Tipos._
    if (tl == tr) { 0.0 }
    else if (tl == OsArtigo || tr == OsArtigo) { 1.0 }
    else if (tl.superTipos.contains(Dispositivo) && tr.superTipos.contains(Dispositivo)) { params.replaceDispsDifTipos }
    else if (tl.superTipos.contains(Agrupador) && tr.superTipos.contains(Agrupador)) { params.replaceAgrupsDifTipos }
    else { 1.0 }
  }
  def replaceCost(rl: Rotulo, rr: Rotulo): Double = (rl, rr) match {
    case (r1: RotuloOrdenado, r2: RotuloOrdenado) => {
      if (r1.nomeRole == r2.nomeRole) {
        if (r1.posicaoRole == r2.posicaoRole) { 0.0 }
        else { params.replaceRotuloDifNums }
      } else {
        params.replaceRotuloDifRoles
      }
    }
    case (r1: RotuloClassificado, r2: RotuloClassificado) => {
      if (r1.nomeRole == r2.nomeRole) {
        if (r1.classificacao == r2.classificacao) { 0.0 }
        else { params.replaceRotuloDifNums }
      } else {
        params.replaceRotuloDifRoles
      }
    }
    case (RotuloClassificado(nr1, "unico"), r2: RotuloOrdenado) => {
      if (nr1 == r2.nomeRole) {
        if (r2.posicaoRole == Seq(1)) { params.rotuloUnicoPor1 }
        else { params.replaceRotuloDifNums }
      } else {
        params.replaceRotuloDifRoles
      }
    }
    case (r2: RotuloOrdenado, RotuloClassificado(nr1, "unico")) => {
      if (nr1 == r2.nomeRole) {
        if (r2.posicaoRole == Seq(1)) { params.rotuloUnicoPor1 }
        else { params.replaceRotuloDifNums }
      } else {
        params.replaceRotuloDifRoles
      }
    }
    case _ => 1.0
  }
  def normalize(text: String) = text.trim()

  def replaceCost(ol: Option[NodeSeq], or: Option[NodeSeq]): Double = {
    val leftText = normalize(ol.map(_.text).getOrElse(""))
    val rightText = normalize(or.map(_.text).getOrElse(""))
    LexmlDiff.diff(leftText, rightText, params.maxPartialDiff, params.diffIgnoreCase) match {
      case Nil => debug("          empty diff") ; 0.0
      case diff => 
        	debug("           diff = " + diff)
      		val proportion = (1 - LexmlDiff.proportionL(diff))*2.0      		
      		debug("           proportion =  "+ proportion)
      		//if (proportion >= 0.0) { 1.0 } else { 1.0 / proportion }
        	proportion
    }       
  }

  def replaceAverage(tipo: Double, rotulo: Double, diff: Double) = {
    (params.tipoWeight * tipo + params.rotuloWeight * rotulo + params.diffWeight * diff) /
      params.totalWeight * params.replaceFactor
  }

  import TED._

  val cache = scala.collection.mutable.Map[(Long,Proposition[Long]),Double]()
  
  override def propCost(value: NodeData, prop: Proposition[NodeData]): Double = {
    val id = value.id
    val prop1 = prop.map(_.id)
    val k = (id,prop1)
    cache.get(k) match {
      case Some(v) => v
      case None => 
        val v = propCost_(value,prop)
        cache.put(k,v)
        v
    }
  } 
  
  var totalCount = 0L
  var propCostCount = 0
  var lastTime : Option[Long] = None
  
  def propCost_(value: NodeData, prop: Proposition[NodeData]): Double = {
    propCostCount = propCostCount + 1
    if(propCostCount == 1000) {
      totalCount = totalCount + 1000L
      val ct = System.nanoTime()
      val ellapsed = lastTime.map(ct - _)      
      ellapsed.foreach(x => println("total: " + totalCount + ", speed: " + (1000.0 / (x.toDouble / 1e9)) + " costs/sec"))
      propCostCount = 0
      lastTime = Some(ct)
    }
    
    
    val cost = prop match {
      case OnlyRight => params.onlyOnRightCost
      case OnlyLeft => params.onlyOnLeftCost
      case OnBoth(other: NodeData) => {
        val v1 = replaceCost(value.tipo, other.tipo)
        debug("    v1 = " + v1)
        val v2 = replaceCost(value.path.head, other.path.head)
        debug("    v2 = " + v2)
        debug("       value.texto.map(_._2.take(10)) = " + value.texto.map(_._2.take(10))) 
        debug("       other.texto.map(_._2.take(10)) = " + other.texto.map(_._2.take(10)))
        val v3 = replaceCost(value.texto.map(_._2), other.texto.map(_._2))                     
        debug("    v3 = " + v3)
        replaceAverage(v1,v2,v3)
      }
    }
    debug("propCost: value = "+ value + ", prop = " + prop + ", cost = " + cost)
    cost
  }
}

object NodeTED {

}