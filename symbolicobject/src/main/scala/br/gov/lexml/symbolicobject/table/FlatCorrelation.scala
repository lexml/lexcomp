package br.gov.lexml.symbolicobject.table

final case class FlatCorrelation[+Node,+Edge](
		value : Option[Node], column : Int, rel : Option[FlatRelation[Node,Edge]]) {
  def fold[A](f : (Option[Node],Int,Option[(Option[Edge],A)]) => A) : A = {
    
    f(value,column,rel.map { case FlatRelation(data,next) => (data,next.fold(f))})
  }
  
  def map[Node1,Edge1](f : Node => Node1, g : Edge => Edge1) : FlatCorrelation[Node1,Edge1] =
    FlatCorrelation(value.map(f),column,rel.map(_.map(f,g)))
    
  def mapColumn(f : Int => Int) : FlatCorrelation[Node,Edge] = 
    FlatCorrelation(value,f(column),rel.map {
      case FlatRelation(data,next) => FlatRelation(data,next.mapColumn(f))
    })
    
  def ++[N1 >: Node,E1 >: Edge](f : FlatRelation[N1,E1]) : FlatCorrelation[N1,E1] =
    FlatCorrelation(
        value,column,
    	rel.map { case FlatRelation(data,next) => FlatRelation(data,next ++ f) }
        	.orElse(Some(f))
    )
    
  def next : Option[FlatCorrelation[Node,Edge]] = rel.map(_.next) 
  
  private def rev[N1 >: Node,E1 >: Edge](f : Option[FlatRelation[N1,E1]]) : FlatCorrelation[N1,E1] =
    rel.map { case FlatRelation(data,next) => next.rev(Some(FlatRelation(data,FlatCorrelation(value,column,f))))}
  	   .getOrElse(FlatCorrelation(value,column,f))
  
  def reverse : FlatCorrelation[Node,Edge] = rev(None)
    	       
}

final case class FlatRelation[+Node,+Edge]( data : Option[Edge], next : FlatCorrelation[Node,Edge]) {
  def map[Node1,Edge1](f : Node => Node1, g : Edge => Edge1) : FlatRelation[Node1,Edge1] = 
		  FlatRelation(data.map(g),next.map(f,g))
  
}
