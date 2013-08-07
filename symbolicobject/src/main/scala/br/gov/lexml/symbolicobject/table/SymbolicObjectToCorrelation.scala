package br.gov.lexml.symbolicobject.table

import br.gov.lexml.{symbolicobject => I}
import br.gov.lexml.symbolicobject.indexer.Contexto
import br.gov.lexml.symbolicobject.indexer.Types._
import br.gov.lexml.symbolicobject.impl._

final case class Plan(indexOrder: List[Int], columns: Column*)
final case class Column(docs: DocumentoComCtx*) {
  val s = docs
}

object PlanToCorrelation {
  
  type CorrelationType = Correlation[PosicaoComCtx, RelacaoComCtx]
  type RelationType = Relation[PosicaoComCtx, RelacaoComCtx]
  type RootCorrelationType = RootCorrelation[PosicaoComCtx, RelacaoComCtx]

  private[this] def traversal(column: Column): Stream[PosicaoComCtx] = {

      def traversalP(posicao: PosicaoComCtx): Stream[PosicaoComCtx] = posicao.objetoSimbolico match {
        case Some(os: ObjetoSimbolicoSimples[_]) => Stream(posicao) 
        case Some(os: ObjetoSimbolicoComplexo[_]) => /*posicao +:*/ os.posicoes.toStream.flatMap(traversalP)
        case _ => Stream()
      }

      def traversalD(doc: DocumentoComCtx): Stream[PosicaoComCtx] = doc.os match {
        case e: ObjetoSimbolicoComplexo[_] => e.posicoes.toStream.flatMap(traversalP)
        case e: ObjetoSimbolicoSimples[_] => sys.error("Objeto simbolico simples não é esperado na raiz do documento.")
      }

      column.docs.toStream.flatMap(traversalD)
    }

  /**
   * Devolve as correlações do plano e a relação de Posicao não citadas
   */
  def createCorrelations(plan: Plan): (List[RootCorrelationType], Seq[Stream[PosicaoComCtx]]) = {    
    
    /**
     * Retorna a lista de objetos simbolicos em uma coluna
     * osToInfixList
     */
    
    /**
     * Ids dos objetos que aparecem nas correlações
     */
    def objetosCitados(rootCorrelation: RootCorrelationType): Set[SymbolicObjectId] = {

      def objetosCitados(correlacao: CorrelationType): Set[SymbolicObjectId] =
        correlacao.rels.flatMap(a => objetosCitados(a.next)).toSet + correlacao.value.objetoSimbolico.get.id

      rootCorrelation.leftRels.flatMap(a => objetosCitados(a.next)).toSet ++
        rootCorrelation.rightRels.flatMap(a => objetosCitados(a.next)).toSet +
        rootCorrelation.value.objetoSimbolico.get.id
    }

    /**
     * Produz os RootCorrelations da coluna indexada em colIndex
     */
    def rootCorrelationFromColumn(colIndex: Int): Stream[RootCorrelationType] = {      
      val DIRECAO_DIREITA = 1
      val DIRECAO_ESQUERDA = -1

      /**
       * Produz todas as correlações da coluna indexada em relacao as demais colunas
       */
      def allCorrelationsFromColumns(allCols: List[Column], numColunaInicial: Int, direcao: Int): Map[SymbolicObjectId, CorrelationType] = {         

        /**
         * Função usada no fold
         * m: mapa de correlações que inclui as correçãoes de todos os obj simbolicos presentes em col
         */
        def f(col: Column, acc: (Int, List[Column], Map[SymbolicObjectId, CorrelationType])): (Int, List[Column], Map[SymbolicObjectId, CorrelationType]) = {                    
          val (numColuna, colsAlvo, m) = acc
          val m2: Map[SymbolicObjectId, CorrelationType] = {

            def correlationsFromPosicao(pos: PosicaoComCtx, colsAlvo: List[Column]): CorrelationType = {
              val relacoes = for {
                colAlvo <- colsAlvo
                doc <- colAlvo.docs
                (dir,rl) <- pos.objeto.data.relacoes.getOrElse(doc.id,Map()) // **** rdb.relationsFrom(pos.objetoSimbolico.get.id, doc.id)
                r <- rl
                if(r match {
                  case _ : RelacaoAusenteNaOrigem[_] => false
                  case _ : RelacaoAusenteNoAlvo[_] => false
                  case _ => true
                })
                target = dir.to(r)
                alvo <- target
              } yield {                
                Relation(r, m(alvo))
              }
              Correlation(pos, numColuna, relacoes)
            }
            
            val novasCorrelacoes = traversal(col).map(pos => (pos.objetoSimbolico.get.id, correlationsFromPosicao(pos, colsAlvo))).toMap
            val res = novasCorrelacoes ++ m            
            res
          }
          (numColuna - direcao, col :: colsAlvo, m2)
        }

        val ultimaColuna = numColunaInicial + (allCols.size - 1) * direcao

        allCols.foldRight((ultimaColuna, List[Column](), Map[SymbolicObjectId, CorrelationType]()))(f)._3
      }

      //correlações à direita da coluna
      val direita = allCorrelationsFromColumns(plan.columns.toList, 0, DIRECAO_DIREITA)

      
      //correlações à esquerda da coluna
      val esquerda = allCorrelationsFromColumns(plan.columns.reverse.toList, plan.columns.size - 1, DIRECAO_ESQUERDA)


      //devolve lista de RootCorrelationType para cada objeto simbólico da coluna indexada
      val posicaoColIndexed = traversal(plan.columns(colIndex))
      val result: Stream[RootCorrelationType] =
        posicaoColIndexed.map(pos => RootCorrelation(pos, colIndex, esquerda(pos.objetoSimbolico.get.id).rels, direita(pos.objetoSimbolico.get.id).rels))
              
      result
    }
    val indexOrder = plan.indexOrder match {
      case Nil => List(0)
      case x => x
    }
  
    val rootCorrelations: List[RootCorrelationType] = indexOrder.flatMap(rootCorrelationFromColumn)
  
    val todosCitados: Set[SymbolicObjectId] = rootCorrelations.foldLeft(Set[SymbolicObjectId]())((acc, v) => objetosCitados(v) ++ acc)
  
    val todosNaoCitados = plan.columns.map(traversal(_).filterNot(x => todosCitados(x.objetoSimbolico.get.id)))
  
    (rootCorrelations, todosNaoCitados)
  }
}