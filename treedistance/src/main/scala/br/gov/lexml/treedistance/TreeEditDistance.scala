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

abstract sealed trait Proposition[+A] {
  def map[B](f : A => B) : Proposition[B]
}

final case object OnlyLeft extends Proposition[Nothing] {
  override def map[B](f : Nothing => B) : Proposition[B] = OnlyLeft
  override def hashCode() = 0
}
  
final case object OnlyRight extends Proposition[Nothing] {
  override def map[B](f : Nothing => B) : Proposition[B] = OnlyRight
  override def hashCode() = 1  
}

final case class OnBoth[A](other : A) extends Proposition[A] {
  override def map[B](f : A => B) : Proposition[B] = OnBoth(f(other))
  val _hash = other.hashCode | 2
  override def hashCode() = _hash
  override def equals(a : Any)  = a match {
    case OnBoth(c) => other == c
    case _ => false
  }
}
  
trait PropCostFunc[-A] {
  def propositionCost(value : A, prop : Proposition[A]) : Double
}

final case class PropSet[A](
    positive : Map[A,Set[Proposition[A]]] = Map[A,Set[Proposition[A]]](),
    negative : Map[A,Set[Proposition[A]]] = Map[A,Set[Proposition[A]]]()
    )(implicit costFunc : PropCostFunc[A]) {
  lazy val cost = positive.toSeq.flatMap{ case(k,l) => (l.toSeq.map(p => costFunc.propositionCost(k,p)))}.sum[Double]
  lazy val isConsistent = positive.forall { case (k,ps) =>
     ps.size <= 1 && 
     negative.getOrElse(k,Set()).intersect(ps).isEmpty
  }
  lazy val neg = PropSet[A](Map(), positive)
  import TED._
  def &&(ps : PropSet[A]) = PropSet(
	  positive = mergeMaps(positive,ps.positive),
	  negative = mergeMaps(negative,ps.negative))
  override def toString() = "pos: " + positive + "\nneg: " + negative 
}

final case class TEDTheory[A](
      psLeft : PropSet[A],
      psRight : PropSet[A])(implicit costFunc : PropCostFunc[A]) {
  lazy val cost = psLeft.cost + psRight.cost
  lazy val isConsistent = psLeft.isConsistent && psRight.isConsistent
  lazy val neg = TEDTheory(psLeft.neg,psRight.neg)
  def &&(t : TEDTheory[A]) = TEDTheory(
   		psLeft = psLeft && t.psLeft,
   		psRight = psRight && t.psRight)
  override def toString() = "Theory: cost = "+ cost + ", psLeft = " + psLeft + ", psRight = " + psRight   		
}

object TEDTheory {
    def emptyM[A] = Map[A,Set[Proposition[A]]]()
    def theory[A](psLeft : Option[PropSet[A]] = None, psRight : Option[PropSet[A]] = None)(implicit costFunc : PropCostFunc[A]) : TEDTheory[A] =
      TEDTheory(psLeft getOrElse PropSet[A](emptyM[A],emptyM[A]), psRight getOrElse PropSet[A](emptyM[A],emptyM[A]) )
}

object TED {    
  def mergeMaps[K,V](m1 : Map[K,Set[V]], m2 : Map[K,Set[V]]) : Map[K,Set[V]] = 
    (m1.toSeq ++ m2.toSeq).
    	groupBy(_._1).
    	mapValues(_.map(_._2).foldLeft(Set[V]())(_ ++ _))  
}

trait TED {

  type A
  
  type T
  
  val treeOps :TreeOps[T,A]
  
  import treeOps._
  
  import TED._  
  
  def propCost(value : A, prop : Proposition[A]) : Double
  
  final implicit val costFunc : PropCostFunc[A] = new PropCostFunc[A] {
    override def propositionCost(value : A, prop : Proposition[A]) : Double = propCost(value,prop)      
  }
  
  
  
  final def onLeft(v : A) = TEDTheory(PropSet(Map(v -> Set(OnlyLeft : Proposition[A]))),PropSet())
  final def onRight(v : A) = TEDTheory(PropSet(),PropSet(Map(v -> Set(OnlyRight : Proposition[A]))))
  final def onBoth(v1 : A,v2 : A) = TEDTheory(
        PropSet(Map(v1-> Set(OnBoth(v2) : Proposition[A]))),
        PropSet(Map(v2-> Set(OnBoth(v1) : Proposition[A]))))
  
  implicit object theoryInst extends Theory[TEDTheory[A]] {
    override val True = TEDTheory(PropSet(),PropSet())    
    def and(th1: TEDTheory[A], th2: TEDTheory[A]) = th1 && th2
    def neg(th: TEDTheory[A]): TEDTheory[A] = th.neg
    def isConsistent(th: TEDTheory[A]): Boolean = th.isConsistent      
    def cost(th: TEDTheory[A]): Double = th.cost
  }
  
  final case class TEDProblem(left : List[T], right : List[T])
  
  def splits1[X](l : List[X]) = l.inits.toList.reverse.zip(l.tails.toList) collect {
    case (left,c :: right) => (left,c,right)
  }
  
  def extracts(l : List[T]) = splits1(l) map {
    case (l,c,r) => (value(c),l,subForest(c),r)
  }
  
  implicit object problemInst extends Problem[TEDProblem,TEDTheory[A]] {
    override val theory = theoryInst

    import theoryInst._

    def reduce(prob: TEDProblem): List[(TEDTheory[A], List[TEDProblem])] = prob match {
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
  
  def solve(p : TEDProblem, th : Option[TEDTheory[A]] = None) = Solver.solve(p,th)(problemInst)
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
  
  import TED._
  
  override def propCost(v : String, prop : Proposition[A]) : Double = prop match {
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