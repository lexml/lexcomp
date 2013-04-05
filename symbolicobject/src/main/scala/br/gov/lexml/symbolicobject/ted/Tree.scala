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
  
  def fromObjetoSimbolicoComplexo(os : ObjetoSimbolicoComplexo[_],path : NonEmptyList[Rotulo]) =  
    Node(NodeData.fromObjetoSimbolicoComplexo(os, path),
        os.posicoes.toList.flatMap(fromPosicao(_,path.list)))
  
  def fromPosicao(p : Posicao[_], parentPath : List[Rotulo]) : Option[Node] = p.objeto match {
    case os : ObjetoSimbolicoComplexo[_] => Some(fromObjetoSimbolicoComplexo(os,nel(p.rotulo,parentPath)))
    case _ => None
  }
  
  def fromArticulacao(d : Documento[_]) = (d.os / "articulacao").result.headOption.flatMap(fromPosicao(_,Nil))
}

final case class NodeData(
  id: Long,
  path: NonEmptyList[Rotulo],
  tipo: STipo,
  texto: Option[(Long, NodeSeq)] = None) 

object NodeData {
  def fromObjetoSimbolicoComplexo(os : ObjetoSimbolicoComplexo[_], path : NonEmptyList[Rotulo]) = {
    val texto : Option[(Long,NodeSeq)] = 
      (os / "texto").result.headOption.map(_.objeto).collect { 
        	case t : TextoPuro[_] => (t.id,Text(t.texto))
        	case t : TextoFormatado[_] => (t.id,t.frag)
    }
    NodeData(os.id,path,os.tipo,texto)
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
    val diff = LexmlDiff.diff(leftText, rightText, params.maxPartialDiff, params.diffIgnoreCase)
    val proportion = LexmlDiff.proportionL(diff)
    if (proportion >= 0.0) { 1.0 } else { 1.0 / proportion }
  }

  def replaceAverage(tipo: Double, rotulo: Double, diff: Double) = {
    (params.tipoWeight * tipo + params.rotuloWeight * rotulo + params.diffWeight * diff) /
      params.totalWeight * params.replaceFactor
  }

  override def propositionCost(value: NodeData, prop: Proposition): Double = prop match {
    case OnlyRight => params.onlyOnRightCost
    case OnlyLeft => params.onlyOnLeftCost
    case OnBoth(other: NodeData) => {
      replaceAverage(replaceCost(value.tipo, other.tipo),
        replaceCost(value.path.head, other.path.head),
        replaceCost(value.texto.map(_._2), other.texto.map(_._2)))
    }
  }
}    

object NodeTED {
  
}