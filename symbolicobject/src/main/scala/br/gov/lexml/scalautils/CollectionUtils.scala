package br.gov.lexml.scalautils

import scala.collection.generic.CanBuildFrom
import scala.collection.generic.IsTraversableOnce
import scala.collection.GenTraversableLike

object CollectionUtils {
  implicit class SelectableCollection[A, Repr](val r: GenTraversableLike[A, Repr]) {
    final def select[T <: A](implicit m: Manifest[T],
      fr: IsTraversableOnce[Repr],
      cbf: CanBuildFrom[Repr, T, Repr { type A = T }]): Repr { type A = T } =
      r collect { case x: T => x }
  }

  implicit class IntersectingMap[K, V1](val m: Map[K, V1]) {
    final def intersect[V2](m1: Map[K, V2]): Map[K, (V1, V2)] = m.seq.flatMap { case (k, v1) => m1.get(k).map(v2 => (k, (v1, v2))) }
  }
}