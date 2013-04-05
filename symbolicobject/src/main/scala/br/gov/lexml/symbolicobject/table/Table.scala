package br.gov.lexml.symbolicobject.table

import scala.xml.Elem
import scala.xml.NodeSeq
import scala.xml.Null
import scala.xml.UnprefixedAttribute

/**
 * cs colspan
 */
final case class Cell[+A](content : A, cs : Int)  {
  def map[B](f : A => B) : Cell[B] = Cell(f(content),cs)  
 
}

final case class Table[A](rows : List[List[Cell[A]]] = Nil) {
  def map[B](f : A => B) : Table[B] = Table(rows.map(_.map(_.map(f))))
  def +[B >: A](t : Table[B]) : Table[B] = Table(rows ++ t.rows)
 
  def renderTable(cssClasses : List[String] = Nil, id : Option[String] = None )
                    (implicit cellRenderer : CellRenderer[A]) = {
    import Table._
    
    addClassesId(
        <table> 
        { rows.map(row => {
          <tr>{row.map(c => cellRenderer.render(c.content).td % new UnprefixedAttribute("colspan", c.cs.toString, Null) )}</tr>
        })
        }
    	</table>, cssClasses, id)
  }
  
}



final case class RenderedCell(
    xml : NodeSeq = NodeSeq.Empty, 
    classes : List[String] = Nil,
    id : Option[String] = None) {
   lazy val td = Table.addClassesId(<td>{xml}</td> , classes, id)  
     
}

trait CellRenderer[N] {
  def render(x : N) : RenderedCell
}

object Table {  
  
  def vconcat[A](l : Table[A]*) = l.foldLeft[Table[A]](Table())(_ + _)
  
  def addClassesId(e : Elem, classes : List[String], id : Option[String]) = {
     var el = e 
     el = if(!classes.isEmpty) { el % new UnprefixedAttribute("class",classes.mkString(" "),Null) } else { el }
     el = id.map(el % new UnprefixedAttribute("id",_,Null)).getOrElse(el)
     el
  }   
}
