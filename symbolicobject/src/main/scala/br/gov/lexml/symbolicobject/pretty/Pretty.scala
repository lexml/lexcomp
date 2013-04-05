package br.gov.lexml.symbolicobject.pretty

// --------------------------------------------------------------------------
// --                         Pretty Print Library                         --
// --------------------------------------------------------------------------
// TODO: renderCompact

import java.io.Writer
// ---------------------------- Datatypes
sealed trait Doc  {
  import Doc._

  override def toString = {
    val writer = new java.io.StringWriter(2048)
    layout(writer, 0.4, 78, this).toString
  }
  
  def show(ribbon_frac:Double, page_width:Int):String =  {
    val wr = new java.io.StringWriter
    layout(wr, ribbon_frac, page_width, this).toString
  }
  
  def show(out:Writer,ribbon_frac:Double,page_width:Int):Writer = 
    layout(out,ribbon_frac, page_width, this)

  // OPERATORS:
  // infixr 6: :: :+:
  //     originally was: <> <+>
  def ::  (lhs:Doc):Doc = Cons(lhs, this)
  def :+: (lhs:Doc):Doc = lhs :: space :: this

  // infixr 6: :|: :||: :#: :##:
  //     originally was: infixr 5 </> <//> <$> <$$>
  def :|:  (lhs:Doc):Doc = lhs :: softline  :: this  
  def :||: (lhs:Doc):Doc = lhs :: softbreak :: this
  def :#:  (lhs:Doc):Doc = lhs :: line      :: this
  def :##: (lhs:Doc):Doc = lhs :: linebreak :: this
  
  // infixr 6: line with empty as right and left unit
  // like Lindig's (^^^)
  def :%: (lhs:Doc):Doc = if      (this == Empty) lhs 
                          else if (lhs  == Empty) this 
                          else lhs :#: this
}

case object Empty                 extends Doc
case class Cons    (h:Doc,t:Doc)  extends Doc
case class Text    (s:String)     extends Doc
case class Nest    (i:Int, d:Doc) extends Doc
case class Break   (s:String)     extends Doc
case class Group   (d:Doc)        extends Doc
case class Column  (f:Int=>Doc)   extends Doc
case class Nesting (f:Int=>Doc)   extends Doc

// Mode (flat/break)
sealed trait Mode
case object FLT extends Mode 
case object BRK extends Mode 

// ------------------------------ Module
object Doc {
  
  // implicits
  // NOTE: in general `string` is better, but for verbatim
  // multi-line strings `text` is better
  implicit def toDoc(s: String  ):Doc = string(s)
  //implicit def toDoc(s: String  ):Doc = text(s)
  implicit def toDoc(v: Byte    ):Doc = text(v toString)
  implicit def toDoc(v: Short   ):Doc = text(v toString)
  implicit def toDoc(v: Char    ):Doc = text(v toString)
  implicit def toDoc(v: Int     ):Doc = text(v toString)
  implicit def toDoc(v: Long    ):Doc = text(v toString)
  implicit def toDoc(v: Float   ):Doc = text(v toString)
  implicit def toDoc(v: Double  ):Doc = text(v toString)
  implicit def toDoc(v: Boolean ):Doc = text(v toString)


  // low-level combinators:
  
  // string: replaces all '\n' to `line`
  def string(s:String):Doc = s.split("\n").toList match {
    case Nil   => empty
    case x::xs => xs.foldLeft(text(x)){ _ :#: text(_) }
  }

  def softString(s:String):Doc = s.split("\n").toList match {
    case Nil   => empty
    case x::xs => xs.foldLeft(text(x)){ _ :|: text(_) }
  }


  def empty:Doc               = Empty
  def text(s:String):Doc      = if (s == "") empty else Text(s)
  def nest(i:Int, d:Doc):Doc  = Nest(i,d)
  def column                  = Column(_)
  def nesting                 = Nesting(_)
  def group(doc:Doc):Doc      = Group(doc)


  def line         = Break(" ")
  def linebreak    = Break("")
  def softline     = group(line)
  def softbreak    = group(linebreak)

  // encloses
  def squotes    = enclose (squote,     squote)_
  def dquotes    = enclose (dquote,     dquote)_
  def braces     = enclose (lbrace,     rbrace)_
  def parens     = enclose (lparen,     rparen)_
  def angles     = enclose (langle,     rangle)_
  def brackets   = enclose (lbracket, rbracket)_
  def enclose(l:Doc,r:Doc)(x:Doc)  = l :: x :: r

  // symbols
  def lparen     = text( "(" )
  def rparen     = text( ")" )
  def langle     = text( "<" )
  def rangle     = text( ">" )
  def lbrace     = text( "{" )
  def rbrace     = text( "}" )
  def lbracket   = text( "[" )
  def rbracket   = text( "]" )
  def squote     = text( "\'")
  def dquote     = text( "\"")
  def semi       = text( ";" )
  def colon      = text( ":" )
  def comma      = text( "," )
  def space      = text( " " )
  def dot        = text( "." )
  def backslash  = text( "\\")
  def assign     = text( "=" )

  // semi-low level combinators
  def fold (op:(Doc,Doc)=>Doc) (ld:List[Doc]):Doc = ld match {
    case Nil =>  empty
    case _   =>  ld reduceLeft { op(_:Doc, _:Doc) } 
  }

  // semi primitive: fill and fillBreak
  def fillBreak(fl:Int, d:Doc):Doc = 
    width(d, w => if (w > fl) nest(fl, linebreak) else text(spaces(fl-w)))

  def fill(fl:Int,d:Doc):Doc = 
    width(d, w => if (w >= fl) empty else text(spaces(fl-w)))

  def width(d:Doc,f:Int=>Doc):Doc = column(k1 => d :: column(k2 => f(k2-k1)))

  // semi primitive: alignment and indentation 
  def indent(i:Int,d:Doc):Doc = hang(i, text(spaces(i)) :: d)
  def hang(i:Int,d:Doc):Doc   = align(nest(i,d))
  def align(d:Doc):Doc = column(k => nesting(i => nest(k-i, d)))


  // high-level combinators
  def sep     = group _ compose vsep
  def fillSep = fold (_ :|: _) _
  def hsep    = fold (_ :+: _) _
  def vsep    = fold (_ :#: _) _

  def cat     = group _ compose vcat
  def fillCat = fold (_ :||: _) _
  def hcat    = fold (_  ::  _) _
  def vcat    = fold (_ :##: _) _

  //  punctuate p [d1,d2,...,dn] => [d1 <> p,d2 <> p, ... ,dn] 
  def punctuate(p:Doc,docs:List[Doc]):List[Doc] = docs match {
    case Nil          => Nil
    case one @ d::Nil => one
    case d::ds        => (d :: p) :: punctuate(p,ds)
  }

  // list, tupled and semiBraces pretty-print a list of 
  // documents either horizontally or vertically aligned
  def list       = encloseSep(lbracket, rbracket, comma)_
  def tupled     = encloseSep(lparen, rparen, comma)_ 
  def semiBraces = encloseSep(lbrace, rbrace, semi)_

  def encloseSep(l:Doc,r:Doc,sep:Doc)(ds:List[Doc]):Doc = ds match {
    case Nil    => l :: r
    case x::Nil => l :: x :: r
    case x::xs  => align(cat((l :: x) :: xs.map(sep :: _)) :: r)
  }
  
  // IMPLEMENTATION   ========================================
  // layout
  // frac -  ratio of ribbon to full page width
  // w - full page width
  def layout(out:Writer, frac:Double, w:Int, doc:Doc):Writer = {
    
    type Cells = List[(Int,Mode,Doc)]

    val ribbon = (frac * w round).asInstanceOf[Int] min w max 0

    // fits
    // w - ribbon space left
    // k - current column
    def fits(w:Int, k:Int, cs:Cells):Boolean = cs match {
      case _ if w < 0              => false
      case Nil                     => true
      case (i,_,Empty)        :: z => fits(w,k, z)
      case (i,m,Cons(x,y))    :: z => fits(w,k, (i,m,x)::(i,m,y)::z)
      case (i,m,Nest(j,x))    :: z => fits(w,k, (i+j,m,x)::z)
      case (i,m,Text(s))      :: z => fits(w-s.length, k+s.length, z)
      case (i,FLT,Break(s))   :: z => fits(w-s.length, k+s.length, z)
      case (i,BRK,Break(_))   :: z => true
      case (i,m,Group(x))     :: z => fits(w,k, (i,m,x)::z)
      case (i,m,Column(f))    :: z => fits(w,k, (i,m,f(k))::z)
      case (i,m,Nesting(f))   :: z => fits(w,k, (i,m,f(i))::z)
    }
    
    def emit(w:Writer,s:String):Writer = {w write s; w}
    
    def nl(acc:Writer,i:Int):Writer = spaces(emit(acc,"\n"),i)
    
    // best
    // n - indentation of current line
    // k - current column
    def best(acc:Writer, n:Int, k:Int, cs:Cells):Writer = cs match {
      case Nil                    => acc
      case (i,_,Empty)       :: z => best(acc,n,k,z)
      case (i,m,Cons(x,y))   :: z => best(acc,n,k,(i,m,x)::(i,m,y)::z)
      case (i,m,Nest(j,x))   :: z => best(acc,n,k,(i+j,m,x)::z)
      case (i,_,Text(s))     :: z => best(emit(acc,s),n,k+s.length,z)
      case (i,FLT,Break(s))  :: z => best(emit(acc,s),n,k+s.length,z)
      case (i,BRK,Break(s))  :: z => best(nl(acc,i),i,i,z)
      case (i,FLT,Group(x))  :: z => best(acc,w,k,(i,FLT,x)::z)
      case (i,BRK,Group(x))  :: z => {
        val ribbonleft = (w - k) min (ribbon - k + n)
        if (fits(ribbonleft, k, (i,FLT,x)::z))
          best(acc, n, k, (i,FLT,x)::z)
        else
          best(acc, n, k, (i,BRK,x)::z)
       }
      case (i,m,Column(f))   :: z => best(acc,n,k,(i,m,f(k))::z)
      case (i,m,Nesting(f))  :: z => best(acc,n,k,(i,m,f(i))::z)
    }
    best(out, 0, 0, (0,BRK,doc)::Nil)
  }
  
  // misc
  def spaces(acc:Writer, n:Int):Writer =  {
    var i = n
    while(i > 0) { acc write ' ' ; i -= 1}
    acc
  }
  
  def spaces(n:Int):String =  {
    val acc = new StringBuffer(80)
    var i = n
    while(i > 0) {acc append ' ' ; i -= 1}
    acc toString
  }

} // Module Doc

