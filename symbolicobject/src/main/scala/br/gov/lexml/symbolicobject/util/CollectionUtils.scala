package br.gov.lexml.symbolicobject.util

import scala.collection.IterableLike
import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom

object CollectionUtils {
    final implicit class M2Merge[K,V](m1 : Map[K,V]) {
      def mergeWith(f : (V,V) => V) = (m2 : Map[K,V]) => mergeMapWith(f)(m1,m2)		   
    }
	def mergeMapWith[K,V](f : (V,V) => V) = (m1 : Map[K,V], m2 : Map[K,V]) =>
		(m1.toSeq ++ m2.toSeq).groupBy(_._1).mapValues {
		  case Seq(kv) => kv._2
		  case Seq(kv1,kv2) => f(kv1._2,kv2._2)
		}   
	def mergeMapsWith[K,V](f : (V,V) => V) = (ml : Iterable[Map[K,V]]) => 	  
      ml.foldLeft(Map[K,V]())(_.mergeWith(f)(_))
	
	final implicit class I2G[K,V](l : Iterable[(K,V)]) {
	  def groupBy1on2with[C](f : Iterable[V] => C) = l.groupBy(_._1).mapValues(l => f(l.map(_._2)))
	}
	/*def groupBy1on2with[K,V,C](f : Iterable[V] => C) = (l : Iterable[(K,V)]) =>
	  l.groupBy(_._1).mapValues(l => f(l.map(_._2))) */
	
    def zipAll[A](l : Seq[Seq[A]]) : IndexedSeq[IndexedSeq[Option[A]]] = {
      val ll = l.map(_.toIndexedSeq).toIndexedSeq
      val numRows = ll.map(_.length).max
      (0 until numRows) map { row =>
        ll.map(d => d.lift(row))
      }      
    }
    
    def intersperse[T, C <: IterableLike[T,C]](l : C, middle : C)(implicit cb : CanBuildFrom[C,T,C]) = {
      val b = cb()
      var l1 = l
      while(!l1.isEmpty) {
        val h = l1.head
        b += h
        val t = l1.tail
        if(!t.isEmpty) {
          b ++= middle
        }
    	l1 = t
      }
      
      b.result()
    }
    
    def splitBy[T](l : Seq[T], f : T => Boolean) : IndexedSeq[IndexedSeq[T]] = {
            
      val (ll,rest) = l.foldLeft(IndexedSeq[IndexedSeq[T]](),IndexedSeq[T]()) { 
        case ((ll,yy),x) if f(x) => (ll :+ yy, IndexedSeq[T]())           
        case ((ll,yy),x) => (ll, yy :+ x)                 
      }
      if(rest.isEmpty) { ll }
      else (ll :+ rest)
    }
}