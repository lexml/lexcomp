package br.gov.lexml.symbolicobject.table

import scala.collection.LinearSeqLike
import scala.collection.generic.CanBuildFrom



final case class RootCorrelation[+Node,+Edge](
    value : Node, column : Int, leftRels : IndexedSeq[Relation[Node,Edge]],
    rightRels : IndexedSeq[Relation[Node,Edge]]) {
  
  override def toString : String = "RootCorrelation = value : {" + value + "} ; column : {" + column + "} ; leftRels : {" + leftRels + "} ; rightRels : {" + rightRels + "}" 
  
  lazy val flatCorrelations = {
    val lc = Correlation(value,column,leftRels)
    			.mapColumn(n => column - n)
    			.flatCorrelations
    			.map(_.mapColumn(n => column - n))    			
    			.map(_.reverse)    			
    val rc = Correlation(value,column,rightRels).flatCorrelations.map(_.rel)
    val result = lc.zipAll(rc,FlatCorrelation(Some(value),column,None),None) map {
      case (fc1,None) => fc1
      case (fc1,Some(r)) => fc1 ++ r
    }
    result
  } 
  def map[Node1,Edge1](f : Node => Node1, g : Edge => Edge1) : RootCorrelation[Node1,Edge1] = {
    RootCorrelation(f(value),column, leftRels.map(_.map(f,g)), rightRels.map(_.map(f,g)))
  }   
}

final case class Correlation[+Node,+Edge](value : Node, column : Int, rels : IndexedSeq[Relation[Node,Edge]]) {
  
  override def toString : String = "Coorelation = value : {" + value + "} ; column : {" + column + "} ; rels : {" + rels + "}"
  
  def fold[A](f : (Node,Int,IndexedSeq[(Edge,A)]) => A) : A = {
    f(value,column,rels.map { case Relation(data,next) => (data,next.fold(f))} )
  }
  def map[Node1,Edge1](f : Node => Node1, g : Edge => Edge1) : Correlation[Node1,Edge1] = {
    Correlation(f(value),column,rels.map(_.map(f,g)))
  }   
  def mapColumn(f : Int => Int) : Correlation[Node,Edge] = 
		  Correlation(value,f(column),rels.map {
		    case Relation(ed,c) => Relation(ed,c.mapColumn(f)) 
		  })
  
  lazy val flatCorrelations = fold[IndexedSeq[FlatCorrelation[Node,Edge]]] { (d,nc,l) =>
    type F = FlatCorrelation[Node,Edge]
    type R = IndexedSeq[F]
    def add(v : Option[Node], c : (Edge,R)) : R = {
      def add1(d: Option[Node], v : Option[Edge],f : F) = FlatCorrelation(d,nc,Some(FlatRelation(v,f)))
      val (rd,n) = c
      n match {
        case IndexedSeq() => IndexedSeq()
        case _ => add1(v,Some(rd),n.head) +: n.tail.map(add1(None,None,_))
      }
    }
    l match {   
       case IndexedSeq() => IndexedSeq(FlatCorrelation(Some(d),nc,None))
       case _ =>(add(Some(d),l.head) +: l.tail.map(add(None,_))).flatten
  } }
}

/**
 * Node deve ser o objeto Relacao
 * 
 */
final case class Relation[+Node,+Edge](data : Edge, next : Correlation[Node,Edge]) {
  def map[Node1,Edge1](f : Node => Node1, g : Edge => Edge1) : Relation[Node1,Edge1] = 
		  	Relation(g(data),next.map(f,g))
		  	
  override def toString : String = "Relation = data : {" + data + "} ; next : {" + next + "}"
}
