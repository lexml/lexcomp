package br.gov.lexml.symbolicobject.xml

import scala.xml.Elem
import scala.xml.NodeSeq
import org.kiama.rewriting.Rewriter
import org.kiama.rewriting.Rewritable
import scala.xml.XML
import Rewriter.Term
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.ls.DOMImplementationLS
import org.w3c.dom.DocumentFragment

abstract sealed class WrappedXML[T <: WrappedXML[T]] extends Rewritable {
  type Term = Any
  final override def arity = 1
  final override def deconstruct : Seq[Term] = asText
  
  final override def reconstruct (components : Array[Term]) : Term = {
    val x = buildXMLString(components(0).asInstanceOf[String])
    val e = XML.loadString(x)
    buildFromParsedElem(e)           
  }
  
  def buildXMLString(subString : String) : String
  def buildFromParsedElem(e : Elem) : T  
  val asText : String
}

final class WrappedNodeSeq(val ns : NodeSeq) extends WrappedXML[WrappedNodeSeq] {      
  override def buildXMLString(ss : String) = "<a>" + ss + "</a>"
  override def buildFromParsedElem(e : Elem) = new WrappedNodeSeq(e.child)    
  override lazy val asText = ns.toString
  def toFragment : DocumentFragment = Helpers.toXmlFragment(ns)
  override def toString : String = asText
}

object WrappedNodeSeq { 
  def fromString(strXml : String) = new WrappedNodeSeq(XML.loadString("<a>" + strXml + "</a>").child)
}

final class WrappedElem(val el : Elem) extends WrappedXML[WrappedElem] {  
  override def buildXMLString(ss : String) = ss
  override def buildFromParsedElem(e : Elem) = new WrappedElem(e)    
  lazy val asText = el.toString
}

object Helpers {
    def toXmlFragment(frag : NodeSeq) : DocumentFragment = {
    import javax.xml.parsers.DocumentBuilderFactory
    import org.xml.sax.InputSource
    import java.io.StringReader
    
    val f = DocumentBuilderFactory.newInstance()
    val b = f.newDocumentBuilder()    
    val src = (<a>{frag}</a>).toString
    val srcDoc = b.parse(new InputSource(new StringReader(src)))
    val tgtDoc = b.newDocument()
    val xfrag = tgtDoc.createDocumentFragment()
    val e = srcDoc.getDocumentElement()
    while(e.hasChildNodes) {
      xfrag.appendChild(e.removeChild(e.getFirstChild()))
    }
    xfrag
  }
  
  def toXmlFragment(frag : String) : DocumentFragment = {
    import javax.xml.parsers.DocumentBuilderFactory
    import org.xml.sax.InputSource
    import java.io.StringReader
    
    val f = DocumentBuilderFactory.newInstance()
    val b = f.newDocumentBuilder()    
    val src = "<a>" + frag + "</a>"
    val srcDoc = b.parse(new InputSource(new StringReader(src)))
    val tgtDoc = b.newDocument()
    val xfrag = tgtDoc.createDocumentFragment()
    val e = srcDoc.getDocumentElement()
    while(e.hasChildNodes) {
      xfrag.appendChild(e.removeChild(e.getFirstChild()))
    }
    xfrag
  }
 
  def toXml(df : DocumentFragment) : NodeSeq = {
    val res = toXmlString(df)
    val x = XML.loadString("<a>" + res + "</a>")
    x.child    
  }
  
  def toXmlString(df : DocumentFragment) : String = {
    val lsSerializer = lsImpl.createLSSerializer()
    lsSerializer.getDomConfig().setParameter("xml-declaration",false)
    lsSerializer.writeToString(df)
  }
  
  lazy val lsImpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation().asInstanceOf[DOMImplementationLS]
  
}