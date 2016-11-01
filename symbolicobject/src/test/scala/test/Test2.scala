package test

import br.gov.lexml.symbolicobject.impl._
import br.gov.lexml.symbolicobject.parser._
import br.gov.lexml.symbolicobject.tipos._
import scalaz._

object Test2 extends App {
  import Scalaz._

  val idSource = new IdSource {
    var _nextId = 0L
    def nextId(tipo : STipo) = {
      val x = _nextId
      _nextId = _nextId + 1
      x
    }
  }

  val parser = new Parser(idSource)

  val doc = LexMLDocument(FileSource(new java.io.File("src/test/resources/texto.xml")))

  val id = InputDocument(Tipos.DocProjetoLei,doc,"urn:lex:br:senado.federal:projeto.lei;plc:2011;98@data.evento;apresentacao.substitutivo;2011-11-01t00.00")

  final case class Row[+T](c : Caminho, o : ObjetoSimbolicoComplexo[T],texts : IndexedSeq[String])

  implicit val showRow = new Show[Row[Unit]] {
    override def shows(v : Row[Unit]) : String = {
      s"${v.c.render} [${v.c.render2}] (${v.o.getClass.getName}): { ${v.texts.mkString("|")} }"
    }
  }

  def objetoSimbolicoToTree[T](os : ObjetoSimbolico[T]) : Option[Tree[Row[T]]] =
    os.fold[Option[Tree[Row[T]]]] {
      case (c,ts,os : ObjetoSimbolicoComplexo[T]) =>
        val t : Tree[Row[T]] =
          Row( c,os,
             os.childrenSimples.map(_.getRepresentacao()).to[IndexedSeq]
             ).node(ts.flatten : _*)
             Some(t)
      case _ => None
    }

  for {
    doc <- parser.parse(id)
    tree <- objetoSimbolicoToTree(doc.getObjetoSimbolico())
  } {
    println(tree.drawTree)
  }

}
