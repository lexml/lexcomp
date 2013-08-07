package br.gov.lexml.symbolicobject.table

import scala.xml.NodeSeq
import br.gov.lexml.{symbolicobject => I}
import br.gov.lexml.symbolicobject.indexer.Contexto
import br.gov.lexml.symbolicobject.indexer.Types._
import br.gov.lexml.symbolicobject.impl.ObjetoSimbolicoComplexo
import br.gov.lexml.symbolicobject.impl.RelacaoDiferenca
import br.gov.lexml.symbolicobject.impl.TextoFormatado
import br.gov.lexml.symbolicobject.impl.TextoPuro
import br.gov.lexml.symbolicobject.impl.RelacaoDiferenca
import br.gov.lexml.symbolicobject.impl.TextoFormatado
import br.gov.lexml.symbolicobject.indexer.IIndexer
import br.gov.lexml.symbolicobject.indexer.ContextoRelacao
import br.gov.lexml.lexmldiff.LexmlDiff
import scala.xml.Null
import scala.xml.Text
import br.gov.lexml.symbolicobject.util.CollectionUtils
import br.gov.lexml.symbolicobject.impl.Caminho


trait OpcoesVisualizacao {
  def getMaxUpdateRatio() : Double
} 

abstract class BaseRenderer[T] extends CellRenderer[T] {
      def empty : RenderedCell = RenderedCell(NodeSeq.Empty, List("css-vis-empty-right"))
}

class Visualizacao(indexer : IIndexer, opcoes : OpcoesVisualizacao) {
    def renderCaminho(c : Caminho) : NodeSeq = { 
      var r = c.render2 match {
	      case ("","") => NodeSeq.Empty
	      case ("",t) => <span class="rotuloPrincipal">{t + " "}</span>
	      case (p,t) => NodeSeq fromSeq (Seq(<span class="rotuloContexto">{p}</span> , <span> </span>, <span class="rotuloPrincipal">{t + " "}</span>))
      }
      println("renderCaminho: caminho = " + c + ", r = " + r)
      r
    }
    // de SymbolicObject e outro de Relacao
    private implicit val cellRendererSO = new BaseRenderer[Either[PosicaoComCtx, NodeSeq]] {
      def render(x: Either[PosicaoComCtx, NodeSeq]): RenderedCell = x match {
        case Right(comentario) => RenderedCell(comentario, List("css-vis-comentario"))
        case Left(pos) => pos.objetoSimbolico.get match {
          case tp: TextoPuro[Contexto] => RenderedCell(<span><span class="css-vis-span-rotulo">{renderCaminho(tp.data.caminho)}</span>{ tp.texto }</span>, List("css-vis-texto-puro"), Some("tp-" + tp.id))
          case tf: TextoFormatado[Contexto] => RenderedCell(
              NodeSeq.fromSeq(Seq(<span class="css-vis-span-rotulo">{renderCaminho(tf.data.caminho)}</span>) ++ tf.frag.ns ) 
              , 
              List("css-vis-texto-formatado"), Some("tf-" + tf.id))
          case os: ObjetoSimbolicoComplexo[Contexto] => RenderedCell(<span>{ pos.rotulo.toString }</span>, List("css-vis-texto-obj-simbolico", "css-vis-os-" + os.tipo.nomeTipo), Some("os-" + os.id))
          case _ => RenderedCell(<span>Outro tipo de Objeto Simbolico. Rotulo = { pos.rotulo }</span>)
        }
      }
    }

    // de SymbolicObject e outro de Relacao
    private implicit val cellRendererRelation = new BaseRenderer[Either[RelacaoComCtx, NodeSeq]] {
      import br.gov.lexml.lexmldiff._
      private[this] def renderDiffCase(dc : DiffCase) : NodeSeq = dc match {
        case i : Insert => <span class="diff diffInsert">{i.text}</span>
        case d : Delete => <span class="diff diffDelete">{d.text}</span>
        case c : Change => Seq(<span class="diff diffDelete">{c.oldText}</span>,
                               <span class="diff diffInsert">{c.newText}</span>)
        case e : EqualOther => Text(e.text)
        case EqualSpace => Text(" ")
      }
      private[this] def renderDiffCases(l : List[DiffCase]) : NodeSeq = l.toSeq.flatMap(renderDiffCase)
      private[this] def diff(t1 : String, t2 : String) : Option[NodeSeq] = {
        val dl = LexmlDiff.diff(t1, t2, opcoes.getMaxUpdateRatio, true)
        if(dl.exists(c => !c.isInstanceOf[Equal])) {
          Some(renderDiffCases(dl))
        } else {
          None
        }        
      }
      def render(x: Either[RelacaoComCtx, NodeSeq]): RenderedCell = x match {
        case Left(r : RelacaoDiferenca[ContextoRelacao]) => {
          val d : NodeSeq = r.data.textos.flatMap { case (t1,t2) =>
            diff(t1.text,t2.text)            
          } getOrElse(Text(""))
          RenderedCell(<span> { d }</span>,List("css-vis-diff","css-vis-diff-diferenca"))
        }
        case Left(_) => RenderedCell(NodeSeq.Empty,List("css-vis-diff","css-vis-diff-sem-diferenca"))
        case Right(ns) => RenderedCell(ns,List("css-vis-diff-comentario"))
      }
    }

    private implicit val cellRenderer =
      CorrelationCellData.cellRenderer[Either[PosicaoComCtx, NodeSeq], Either[RelacaoComCtx, NodeSeq]]
  
  
  private def produceDocumentoComCtx(doc: I.Documento): DocumentoComCtx = 
    		produceDocumentoComCtx(doc.getId())
   
  
  private def produceDocumentoComCtx(docId : Long): DocumentoComCtx = 
    indexer.getDocumento(docId).getOrElse(sys.error("Documento não encontrado: " + docId))
  
  
  import java.util.{List => JList}
    
  def createHtmlTable(indexOrder: JList[Integer], columns: JList[JList[I.Documento]]): String = {
    import scala.collection.{JavaConverters => JC}
  
    def toScalaList[A](l : JList[A]) : List[A] = {
      JC.collectionAsScalaIterableConverter(l).asScala.toList
    }
    
    createHtmlTable(
        toScalaList(indexOrder).map(_.toInt), 
        toScalaList(columns).map( x => toScalaList(x)) )
  }
  
  def createHtmlTable(indexOrder: List[Int], columns: List[List[I.Documento]]): String = {

    //preparing plan
    val cols: List[Column] = columns.map(x => Column(x.map(produceDocumentoComCtx): _*))
    val plan = Plan(indexOrder, cols: _*)

    //creating rootCorrelations
    val (rootCorrelations, todosNaoCitados) = PlanToCorrelation.createCorrelations(plan)

    def eitherPosicao(pos: PosicaoComCtx): List[Either[PosicaoComCtx, NodeSeq]] =
      Left(pos) :: pos.objetoSimbolico.get.data.comentarios.toList.map(a => Right(<span>{ a.texto }</span>))
    //Left(pos) :: relacaoDB.commentsOfSymbolicObject(pos.objetoSimbolico.get.id).map(Right(_))

    def eitherRelacao(relation: RelacaoComCtx): List[Either[RelacaoComCtx, NodeSeq]] =
      Left(relation) :: relation.data.comentarios.toList.map(a => Right(<span>{ a.texto }</span>))
    //Left(relation) :: relacaoDB.commentsOfRelation(relation.id).map(Right(_))

    val novoRootCorrelations: List[RootCorrelation[List[Either[PosicaoComCtx, NodeSeq]], List[Either[RelacaoComCtx, NodeSeq]]]] =
      rootCorrelations.map(_.map(eitherPosicao, eitherRelacao))
        

    val table = Transforms.rootCorrelationToTable(novoRootCorrelations)

    def toNodeData(p : Option[PosicaoComCtx]) : 
        Cell[CorrelationCellData[Either[PosicaoComCtx,NodeSeq],Either[RelacaoComCtx,NodeSeq]]] = p match {
        case Some(v) => Cell(NodeData(Left(v)),2)
	    case None => Cell(NoData,2)
    }
    
    val transposedNaoCitados = CollectionUtils.zipAll(todosNaoCitados).map(_.map(toNodeData).toList).toList
    
    val resultTable = if (transposedNaoCitados.isEmpty) {
        table
    } else {
    	val table2 = Table(List(Cell(Other( <span>Outros dispositivos</span>,List("css-vis-outros-dispositivos") ),columns.length*2)) :: transposedNaoCitados)
        table + table2
    }
    val result = resultTable.renderTable(List("css-vis-table"))

    val resHtml =
      (<html>
         <head> 
    		<link rel="stylesheet" href="css/visualizacao.css" type="text/css" media="all"/>         
         </head>
         <body>{ result }</body>
       </html>)

    resHtml.toString
  }
}