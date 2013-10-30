package br.gov.lexml.symbolicobject.comp

import br.gov.lexml.symbolicobject.tipos.STipo
import scala.reflect.ClassTag
import br.gov.lexml.symbolicobject.impl.Rotulo
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolicoComplexo
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolicoSimples

final case class TrasverseOp[Ctx](classify : Option[String] = None, descend : Boolean = true,
    updateContext : Option[Ctx] = None) {
  def newContext(ctx : Ctx) = updateContext.getOrElse(ctx)
}

trait Classifier[Ctx,-T] {
  def classify(ctx : Ctx,o : ObjetoSimbolico[T]) : TrasverseOp[Ctx]
}

object Classifier {
  type ClassMap[T] = Map[String,IndexedSeq[ObjetoSimbolico[T]]]
  
  type PairClassMap[T] = Map[String,(IndexedSeq[ObjetoSimbolico[T]],IndexedSeq[ObjetoSimbolico[T]])]
  
  def classify[Ctx,T](
      initialContext : Ctx,
      controlFunc : Classifier[Ctx,T])(o : ObjetoSimbolico[T]) : ClassMap[T] = {
    def trv(o : ObjetoSimbolico[T], ctx : Ctx) : ClassMap[T] = {
      val op = controlFunc.classify(ctx,o)
      val newCtx = op.newContext(ctx)
      lazy val kids : ClassMap[T] = if(op.descend) { 
        o match {      
	        case oc : ObjetoSimbolicoComplexo[T] =>
	          oc.posicoes.map(_.objeto).flatMap(o => trv(o,newCtx).toIndexedSeq).groupBy(_._1).mapValues(_.flatMap(_._2))
	        case o : ObjetoSimbolicoSimples[T] => Map()
       }
      } else { Map() }
      op.classify match {
        case None => kids
        case Some(c) => kids.get(c) match {
          case None => kids + (c -> IndexedSeq(o))
          case Some(l) => kids + (c -> (o +: l))
        }
      }      
    }
    trv(o,initialContext)
  }
  
  def zipResults[T](left : ClassMap[T], right : ClassMap[T]) : PairClassMap[T] = {
    val l1 = left.mapValues(x => (x,IndexedSeq[ObjetoSimbolico[T]]()))
    right.foldLeft(l1) {
      case (m,(k,v)) => m.get(k) match {
        case None => m + (k -> ((IndexedSeq(),v)))
        case Some((l,_)) => m + (k -> ((l,v)))
      }         
    }    
  }
}

