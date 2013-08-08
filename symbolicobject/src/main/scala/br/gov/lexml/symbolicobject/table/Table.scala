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

final case class Table[A](rows : IndexedSeq[IndexedSeq[Cell[A]]] = IndexedSeq()) {
  def map[B](f : A => B) : Table[B] = Table(rows.map(_.map(_.map(f))))
  def +[B >: A](t : Table[B]) : Table[B] = Table(rows ++ t.rows)
 
  def renderTable(cssClasses : IndexedSeq[String] = IndexedSeq(), id : Option[String] = None,
      columnNames : IndexedSeq[(String,Int)] = IndexedSeq())
                    (implicit cellRenderer : CellRenderer[A]) = {
    import Table._
    
    def totalColSpan(r : IndexedSeq[Cell[A]]) = r.map(_.cs).sum
    
    val maxSpan = rows.map(totalColSpan).max
      
    val renderedRows = rows.map(row => {
          val ts = totalColSpan(row)
          val rs = maxSpan - ts
          <tr>{
            row.map(c => cellRenderer.render(c.content).td % new UnprefixedAttribute("colspan", c.cs.toString, Null) )
            }
            {
            if(rs <= 0) { NodeSeq.Empty } else { cellRenderer.empty.td % new UnprefixedAttribute("colspan", rs.toString, Null) }
            }
            </tr>            
          }).filter(row => (row \ "td").map(_.text.trim.length()).sum > 0)
    
    addClassesId(
        <table> {
        	if(columnNames.isEmpty) { NodeSeq.Empty } else {        
    		  <thead>
        		{        		  
        		  if(columnNames.isEmpty) { NodeSeq.Empty } else {
	        		<tr class="columnNameRow">
        			   { columnNames.flatMap { case (cn,cs) => <td colSpan={cs.toString}>{cn}</td> } }
        		    </tr>
        		  }
        		}
    		  </thead>
        	}
        }
    		<tbody>
        { renderedRows
        }
        	</tbody>
    	</table>, cssClasses, id)
  }
  
}



final case class RenderedCell(
    xml : NodeSeq = NodeSeq.Empty, 
    classes : IndexedSeq[String] = IndexedSeq(),
    id : Option[String] = None) {
   lazy val td = Table.addClassesId(<td>{xml}</td> , classes, id)  
     
}

trait CellRenderer[N] {
  def render(x : N) : RenderedCell
  def empty : RenderedCell
}

object Table {  
  
  def vconcat[A](l : Table[A]*) = l.foldLeft[Table[A]](Table())(_ + _)
  
  def addClassesId(e : Elem, classes : IndexedSeq[String], id : Option[String]) = {
     var el = e 
     el = if(!classes.isEmpty) { el % new UnprefixedAttribute("class",classes.mkString(" "),Null) } else { el }
     el = id.map(el % new UnprefixedAttribute("id",_,Null)).getOrElse(el)
     el
  }   
}
