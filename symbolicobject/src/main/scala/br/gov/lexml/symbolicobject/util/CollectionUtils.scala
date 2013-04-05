package br.gov.lexml.symbolicobject.util

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
	
}