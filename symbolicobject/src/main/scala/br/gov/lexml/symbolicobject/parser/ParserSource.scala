package br.gov.lexml.symbolicobject.parser

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.CharArrayReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import scala.xml.Elem
import scala.xml.Elem
import scala.xml.Text
import scala.xml.XML
import org.apache.commons.io.IOUtils
import org.apache.commons.io.IOUtils
import org.xml.sax.InputSource
import br.gov.lexml.parser.pl.ProjetoLeiParser
import br.gov.lexml.parser.pl.block.Block
import br.gov.lexml.parser.pl.block.Block
import br.gov.lexml.parser.pl.block.Paragraph
import br.gov.lexml.parser.pl.errors.ParseProblem
import br.gov.lexml.parser.pl.metadado.Metadado
import br.gov.lexml.parser.pl.metadado.Metadado
import br.gov.lexml.parser.pl.xhtml.XHTMLProcessor.defaultConverter
import br.gov.lexml.parser.pl.xhtml.XHTMLProcessor.pipeline
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolico
import br.gov.lexml.symbolicobject.tipos.STipo
import scalaz._
import br.gov.lexml.parser.pl.ProjetoLei
import br.gov.lexml.parser.pl.output.LexmlRenderer
import br.gov.lexml.parser.pl.profile.ProjetoDeLeiDoSenadoNoSenado
import br.gov.lexml.parser.pl.text.normalizer


final case class InputDocument(tipo: STipo, doc: XMLSource, urn: String)

final case class TextoArticulacao(streamSource: StreamSource, mime: String) extends HasStreamSource with StreamToBlocks with BlocksToXMLSomenteArticulacao

final case class TextoCompleto(streamSource: StreamSource, mime: String, metadado: Metadado) extends HasStreamSource with StreamToBlocks with BlocksToXMLCompleto

final case class Paragrafos(paragrafos: TraversableOnce[String]) extends ParagrafosSource with ParagrafosToBlocks with BlocksToXMLSomenteArticulacao {
  def this(c: java.util.Collection[String]) = this(scala.collection.JavaConverters.collectionAsScalaIterableConverter(c).asScala)
}

trait XMLSource {
  def elem(): Validation[Throwable, Elem]
}

final case class LexMLDocument(streamSource: StreamSource) extends XMLSource {
  override def elem() = Validation.fromTryCatch(XML.load(streamSource.stream()))
}

trait StreamSource {
  def stream(): InputStream
}

trait HasStreamSource {
  def streamSource(): StreamSource
  def mime: String
}

trait BlockSource {
  def blocks(): Validation[String, List[Block]]
}

trait StreamToBlocks extends BlockSource {
  self: HasStreamSource =>
  override final def blocks(): Validation[String, List[Block]] = {
    for {
      ba <- Validation.fromTryCatch(IOUtils.toByteArray(streamSource.stream())).leftMap(t => "Erro lendo fonte: " + t)
      mnodes <- Validation.fromTryCatch(pipeline(ba, mime, defaultConverter)).leftMap(t => "Erro na conversão para xhtml: " + t)
      nodes <- Validation.fromEither(mnodes.toRight("Falha na conversão para xhtml"))
    } yield { Block.fromNodes(nodes) }
  }
}

trait ParagrafosSource {
  def paragrafos: TraversableOnce[String]
}

trait ParagrafosToBlocks extends BlockSource {
  self: ParagrafosSource =>
  def paragrafos: TraversableOnce[String]
  override final def blocks() =
    Success(paragrafos.map((x: String) => Paragraph(Seq(Text(normalizer.removeDuplicateSpace(x.trim))))).toList)
}

trait BlocksToXMLCompleto extends XMLSource {
  self: BlockSource =>
  def metadado: Metadado
  lazy val parser = new ProjetoLeiParser(metadado.profile)

  override def elem() = {
    def parse(bl: List[Block]) = {
      val (mpl1, falhas) = parser.fromBlocks(metadado, bl)
      if (falhas.isEmpty) {
        Validation.fromEither(mpl1.toRight("Erro na análise do texto"))
      } else {
        Validation.failure("Erros na análise do texto: " + falhas.mkString(", "))
      }
    }
    val r = (for {
      bl <- blocks()
      pl <- parse(bl)
    } yield {
      LexmlRenderer.render(pl)
    })
    r.leftMap(new RuntimeException(_))
  }
}

trait BlocksToXMLSomenteArticulacao extends XMLSource {
  self: BlockSource =>

  lazy val parser = new ProjetoLeiParser(ProjetoDeLeiDoSenadoNoSenado)

  override def elem() = {
    val r = (for {
      bl <- blocks()
      articulacao <- Validation.fromTryCatch(parser.parseArticulacao(bl, false)).leftMap(t => "Erro durante análise da articulação: " + t)
    } yield {
      LexmlRenderer.renderArticulacao(articulacao)
    })
    r.leftMap(new RuntimeException(_))
  }
}
      
  
final case class StringSource(s : String) extends StreamSource {
  override def stream() = IOUtils.toInputStream(s,"utf-8")
}

final case class FileSource(f : File) extends StreamSource {
  override def stream() = new FileInputStream(f)
}