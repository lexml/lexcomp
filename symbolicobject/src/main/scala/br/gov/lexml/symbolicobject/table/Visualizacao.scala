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




class Visualizacao(indexer : IIndexer) {

    // de SymbolicObject e outro de Relacao
    private implicit val cellRendererSO = new CellRenderer[Either[PosicaoComCtx, NodeSeq]] {
      def render(x: Either[PosicaoComCtx, NodeSeq]): RenderedCell = x match {
        case Right(comentario) => RenderedCell(comentario, List("css-vis-comentario"))
        case Left(pos) => pos.objetoSimbolico.get match {
          case tp: TextoPuro[Contexto] => RenderedCell(<span>{ tp.texto }</span>, List("css-vis-texto-puro"), Some("tp-" + tp.id))
          case tf: TextoFormatado[Contexto] => RenderedCell(tf.frag.ns, List("css-vis-texto-formatado"), Some("tf-" + tf.id))
          case os: ObjetoSimbolicoComplexo[Contexto] => RenderedCell(<span>{ pos.rotulo.toString }</span>, List("css-vis-texto-obj-simbolico", "css-vis-os-" + os.tipo.nomeTipo), Some("os-" + os.id))
          case _ => RenderedCell(<span>Outro tipo de Objeto Simbolico. Rotulo = { pos.rotulo }</span>)
        }
      }
    }

    // de SymbolicObject e outro de Relacao
    private implicit val cellRendererRelation = new CellRenderer[Either[RelacaoComCtx, NodeSeq]] {
      def render(x: Either[RelacaoComCtx, NodeSeq]): RenderedCell = x match {
        case Left(r : RelacaoDiferenca[ContextoRelacao]) => {
          val diff : NodeSeq = r.data.textos.map { case (t1,t2) =>
            val d = LexmlDiff.diffAsXML(t1.text, t2.text, 0.8, true)
            d
          } getOrElse(Text(""))
          RenderedCell(<span> { diff }</span>)
        }
        case Left(_) => RenderedCell()
        case Right(ns) => RenderedCell(ns)
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

    def produceRootCorrelations : List[RootCorrelation[List[Either[PosicaoComCtx, NodeSeq]], List[Either[RelacaoComCtx, NodeSeq]]]] = {
      
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

      novoRootCorrelations
    }



    val table = Transforms.rootCorrelationToTable(produceRootCorrelations)

    val result = table.renderTable(List("css-vis-table"))

    val resHtml =
      (<html>
         <head>
           <style>
             .css-vis-comentario {{
    			  
  			}}
   			.css-vis-texto-puro {{
    			  
   			}}
   			.css-vis-texto-formatado {{
    			  
   			}}
   			.css-vis-texto-obj-simbolico {{
    			  
   			}}
   			.css-vis-os-ARTIGO {{
    			  
   			}}
   			.css-vis-table {{
   			  border: 1px solid black collapse;
   			}}
    		td {{
    		  border: 1px solid black;
    		}}
           </style>
         </head>
         <body>{ result }</body>
       </html>)

    resHtml.toString
  }
}