package br.gov.lexml.symbolicobject.table

import scala.xml.NodeSeq

abstract sealed class CorrelationCellData[+Node,+Edge]

case object NoData extends CorrelationCellData[Nothing,Nothing]

case class NodeData[+Node](value : Node) extends CorrelationCellData[Node,Nothing]

case class EdgeData[+Edge](value : Edge) extends CorrelationCellData[Nothing,Edge]

object CorrelationCellData {
  implicit def cellRenderer[Node,Edge](implicit nodeRenderer : CellRenderer[Node], edgeRenderer : CellRenderer[Edge]) : 
	  				CellRenderer[CorrelationCellData[Node,Edge]] = 
  		  new CellRenderer[CorrelationCellData[Node,Edge]] { 
  			override def render(c : CorrelationCellData[Node,Edge]) = c match {  
  				case NoData => RenderedCell(NodeSeq.Empty)
  				case NodeData(value) => nodeRenderer.render(value)
  				case EdgeData(value) => edgeRenderer.render(value)
  			}
  }
}