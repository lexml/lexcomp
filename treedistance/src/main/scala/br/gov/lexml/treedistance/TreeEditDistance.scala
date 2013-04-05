package br.gov.lexml.treedistance
import scalaz._
import Scalaz._

trait Theory[Th] {
  val True: Th
  def and(th1: Th, th2: Th): Th
  def neg(th: Th): Th
  def isConsistent(th: Th): Boolean
  def cost(th: Th): Double
}

trait Problem[Prob, Th] {
  implicit val theory: Theory[Th] = implicitly[Theory[Th]]
  import theory._
  def reduce(prob: Prob): List[(Th, List[Prob])]
}

object Solver {
  def solve[Th, Prob](p: Prob, baseAssumptions : Option[Th] = None)(implicit problem: Problem[Prob, Th]): Set[Th] = {
    import problem._
    import theory._
    var bestCost: Option[Double] = None
    var bestSols: Set[Th] = Set()
    def doSolve(ps: List[Prob], assumptions: Th): Unit = {
      ps match {	    
	      case Nil => {	        
	        val c = cost(assumptions)
	        val bs = if (bestCost == Some(c)) { bestSols } else { Set[Th]() }
	        bestCost = Some(c)
	        bestSols = bs + assumptions
	      }
	      case (p::ps1) => {	          
	          val alts = reduce(p)	          
	          alts.foldLeft(assumptions) {
	              case (as, (th, subProbs)) =>
	                val as1 = and(as, th)
	                val c = cost(as1)	                
	                if (isConsistent(as1) && bestCost.map(_ >= c).getOrElse(true)) {	                  
	                	doSolve(subProbs ++ ps1,as1)
	                }
	                and(as, neg(th))
	            }	          
	        }        
	    }
    }
    doSolve(List(p), baseAssumptions.getOrElse(True))
    bestSols
  }
}

trait TreeOps[T,A] {
    def value(t : T) : A
    def subForest(t : T) : List[T]    
}

trait TED {

  type A
  
  type T
  
  val treeOps :TreeOps[T,A]
  
  import treeOps._
  
  abstract sealed class Proposition 

  final case object OnlyLeft extends Proposition
  
  final case object OnlyRight extends Proposition

  final case class OnBoth(other : A) extends Proposition
  
  def propositionCost(value : A, prop : Proposition) : Double
  
  def mergeMaps[K,V](m1 : Map[K,Set[V]], m2 : Map[K,Set[V]]) : Map[K,Set[V]] = 
    (m1.toSeq ++ m2.toSeq).
    	groupBy(_._1).
    	mapValues(_.map(_._2).foldLeft(Set[V]())(_ ++ _))
  
  
  final case class PropSet(
      positive : Map[A,Set[Proposition]] = Map(),
      negative : Map[A,Set[Proposition]] = Map()
      ) {
      lazy val cost = positive.toSeq.flatMap{ case(k,l) => (l.toSeq.map(p => propositionCost(k,p)))}.sum[Double]
	  lazy val isConsistent = positive.forall { case (k,ps) =>
	      ps.size <= 1 && 
	      negative.getOrElse(k,Set()).intersect(ps).isEmpty
	    }
      lazy val neg = PropSet(Map(), positive)
      def &&(ps : PropSet) = PropSet(
    		  positive = mergeMaps(positive,ps.positive),
    		  negative = mergeMaps(negative,ps.negative)
          )
  }
  
  final case class TEDTheory(
      psLeft : PropSet = PropSet(),
      psRight : PropSet = PropSet()) {
    lazy val cost = psLeft.cost + psRight.cost
    lazy val isConsistent = psLeft.isConsistent && psRight.isConsistent
    lazy val neg = TEDTheory(psLeft.neg,psRight.neg)
    def &&(t : TEDTheory) = TEDTheory(
    		psLeft = psLeft && t.psLeft,
    		psRight = psRight && t.psRight
        )
  }
  
  def onLeft(v : A) = TEDTheory(psLeft = PropSet(Map(v -> Set(OnlyLeft))))
  def onRight(v : A) = TEDTheory(psRight = PropSet(Map(v -> Set(OnlyRight))))
  def onBoth(v1 : A,v2 : A) = TEDTheory(
        psLeft = PropSet(Map(v1-> Set(OnBoth(v2)))),
        psRight = PropSet(Map(v2-> Set(OnBoth(v1)))))
  
  implicit object theoryInst extends Theory[TEDTheory] {
    override val True = TEDTheory()    
    def and(th1: TEDTheory, th2: TEDTheory) = th1 && th2
    def neg(th: TEDTheory): TEDTheory = th.neg
    def isConsistent(th: TEDTheory): Boolean = th.isConsistent      
    def cost(th: TEDTheory): Double = th.cost
  }
  
  final case class TEDProblem(left : List[T], right : List[T])
  
  def splits1[X](l : List[X]) = l.inits.toList.reverse.zip(l.tails.toList) collect {
    case (left,c :: right) => (left,c,right)
  }
  
  def extracts(l : List[T]) = splits1(l) map {
    case (l,c,r) => (value(c),l,subForest(c),r)
  }
  
  implicit object problemInst extends Problem[TEDProblem,TEDTheory] {
    override val theory = theoryInst

    import theoryInst._

    def reduce(prob: TEDProblem): List[(TEDTheory, List[TEDProblem])] = prob match {
      case TEDProblem(Nil,Nil) => List((True,Nil))
      case TEDProblem(left,right) => {
        val onlyLefts = for {
          (v,ll,c,rr) <- extracts(left)
        } yield (onLeft(v),List(TEDProblem(ll ++ c ++ rr,right)))
        val onlyRights = for {
          (v,ll,c,rr) <- extracts(right)
        } yield (onRight(v),List(TEDProblem(left,ll ++ c ++ rr)))
        val splittedOrReplaced = (left,right) match {
          case (List(ln),List(rn)) => {            
              val vl = value(ln)
              val li = subForest(ln)
              val vr = value(rn)
              val ri = subForest(rn)
            List((onBoth(vl,vr), List(TEDProblem(li,ri))))
          }
          case _ => {
            for {
              (ll,tl,lr) <- splits1(left)
              (rl,tr,rr) <- splits1(right)
            } yield (True,List(TEDProblem(List(tl),List(tr)),TEDProblem(ll ++ lr, rl ++ rr)))   
          }
        }
        onlyLefts ++ onlyRights ++ splittedOrReplaced
      }
    }
  }
  
  def solve(p : TEDProblem, th : Option[TEDTheory] = None) = Solver.solve(p,th)(problemInst)
}

case class MyTree[+A](value : A, subForest : List[MyTree[A]])

object MyTree {
  def node[A](v : A, ts : MyTree[A]*) = MyTree(v,ts.toList)
  
}

trait MyTreeTED[V] extends TED {
  
  type A = V
  type T = MyTree[A]
  val treeOps = new TreeOps[MyTree[A],A] {
    override def value(t : MyTree[A]) = t.value
    override def subForest(t : MyTree[A]) = t.subForest 
  }
  import MyTree._
  
}

object TestTED extends MyTreeTED[String] {
 
  def propositionCost(v: String,prop : Proposition) : Double = prop match {    
    case OnBoth(other) if v == other => 0.0
    case OnBoth(_) => 2.1 
    case _ => 1.0
  }  
  import MyTree._
  val t1 = node("A",node("B"),node("C"))
  val t2 = node("A",node("B"),node("D"))
  val p1 = TEDProblem(List(t1),List(t2))
  
  def main(args : Array[String]) : Unit = {
    println(t1)
    println(t2)
    println(solve(p1))
  }
}