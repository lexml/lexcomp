package br.gov.lexml.symbolicobject.comp

import br.gov.lexml.symbolicobject.impl._
import br.gov.lexml.symbolicobject.tipos._

class Comparator(left : ObjetoSimbolico[_], right : ObjetoSimbolico[_]) {
    val objs = (left.toStream ++ right.toStream).toIndexedSeq
	val enumList = objs.map(_.id).zipWithIndex
	val internalToExternal = enumList.map { case (x,y) => (y,x) }.toMap
	val externalToInternal = enumList.toMap
	val data = objs map {
	  case oc : ObjetoSimbolicoComplexo[_] => 
	    (externalToInternal(oc.id),oc.posicoes.map(o => externalToInternal(o.objeto.id)))
	}
}