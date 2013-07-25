package br.gov.lexml.symbolicobject.table

import scala.xml.NodeSeq

abstract sealed class CorrelationCellData[+Node,+Edge]

case object NoData extends CorrelationCellData[Nothing,Nothing]

case class NodeData[+Node](value : Node) extends CorrelationCellData[Node,Nothing]

case class EdgeData[+Edge](value : Edge) extends CorrelationCellData[Nothing,Edge]

case class Other(ns : NodeSeq, classes : List[String]) extends CorrelationCellData[Nothing,Nothing]

object CorrelationCellData {
  implicit def cellRenderer[Node,Edge](implicit nodeRenderer : CellRenderer[Node], edgeRenderer : CellRenderer[Edge]) : 
	  				CellRenderer[CorrelationCellData[Node,Edge]] = 
  		  new BaseRenderer[CorrelationCellData[Node,Edge]] { 
  			override def render(c : CorrelationCellData[Node,Edge]) = c match {  
  				case NoData => RenderedCell(NodeSeq.Empty,List("css-vis-diff-vazio"))
  				case NodeData(value) => nodeRenderer.render(value)
  				case EdgeData(value) => edgeRenderer.render(value)
  				case Other(ns,classes) => RenderedCell(ns,classes)
  			}
  }
}