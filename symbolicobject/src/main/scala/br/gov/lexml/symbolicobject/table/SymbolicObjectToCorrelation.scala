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

  /**
   * Devolve as correlações do plano e a relação de Posicao não citadas
   */
  def createCorrelations(plan: Plan): (List[RootCorrelationType], Seq[Stream[PosicaoComCtx]]) = {
	println("createCorrelations: starting")
    /**
     * Retorna a lista de objetos simbolicos em uma coluna
     * osToInfixList
     */
    def traversal(column: Column): Stream[PosicaoComCtx] = {

      def traversalP(posicao: PosicaoComCtx): Stream[PosicaoComCtx] = posicao.objetoSimbolico match {
        case Some(os: ObjetoSimbolicoSimples[_]) => Stream(posicao) 
        case Some(os: ObjetoSimbolicoComplexo[_]) => posicao +: os.posicoes.toStream.flatMap(traversalP)
        case _ => Stream()
      }

      def traversalD(doc: DocumentoComCtx): Stream[PosicaoComCtx] = doc.os match {
        case e: ObjetoSimbolicoComplexo[_] => e.posicoes.toStream.flatMap(traversalP)
        case e: ObjetoSimbolicoSimples[_] => sys.error("Objeto simbolico simples não é esperado na raiz do documento.")
      }

      column.docs.toStream.flatMap(traversalD)
    }

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

      println("rootCorrelationFromColumn: starting. colIndex = " + colIndex)
      val DIRECAO_DIREITA = 1
      val DIRECAO_ESQUERDA = -1

      /**
       * Produz todas as correlações da coluna indexada em relacao as demais colunas
       */
      def allCorrelationsFromColumns(allCols: List[Column], numColunaInicial: Int, direcao: Int): Map[SymbolicObjectId, CorrelationType] = {
    	println("allCorrelations: numColunaInicial = " + numColunaInicial + ", direcao = " + direcao)
        /**
         * Função usada no fold
         * m: mapa de correlações que inclui as correçãoes de todos os obj simbolicos presentes em col
         */
        def f(col: Column, acc: (Int, List[Column], Map[SymbolicObjectId, CorrelationType])): (Int, List[Column], Map[SymbolicObjectId, CorrelationType]) = {
          
          val (numColuna, colsAlvo, m) = acc
          println("allCorrelations:  numColuna = " + numColuna + ", m = " + m)

          val m2: Map[SymbolicObjectId, CorrelationType] = {

            def correlationsFromPosicao(pos: PosicaoComCtx, colsAlvo: List[Column]): CorrelationType = {
              println("correlationsFromPosicao: pos = " + pos + ":") 	 
              val relacoes = for {
                colAlvo <- colsAlvo
                doc <- colAlvo.docs
                r <- pos.objetoSimbolico.get.data.relacoes.getOrElse(doc.id,Map()).values.flatten // **** rdb.relationsFrom(pos.objetoSimbolico.get.id, doc.id)
                if(r match {
                  case _ : RelacaoAusenteNaOrigem[_] => false
                  case _ : RelacaoAusenteNoAlvo[_] => false
                  case _ => true
                })
                alvo <- r.alvo
              } yield {
                println("      r = " + r + ", m(alvo) = " + m(alvo))                
                Relation(r, m(alvo))
              }

              Correlation(pos, numColuna, relacoes)
            }
            
            val novasCorrelacoes = traversal(col).map(pos => (pos.objetoSimbolico.get.id, correlationsFromPosicao(pos, colsAlvo))).toMap
            val res = novasCorrelacoes ++ m
            println("f: numColuna = " + numColuna + ", map = ")
            novasCorrelacoes.seq.foreach { case (k,v) => println("     %05d:%s".format(k,v.toString))}
            res
          }

          (numColuna - direcao, col :: colsAlvo, m2)
        }

        val ultimaColuna = numColunaInicial + (allCols.size - 1) * direcao
        println("numColunaInicial =  " + numColunaInicial + ", allCols.size = "+ allCols.size + ", direcao = " + direcao + ", ultimaColuna = " + ultimaColuna)
        println("allCols (docIds) = " + allCols.map(_.docs.map(_.id)))
        allCols.foldRight((ultimaColuna, List[Column](), Map[SymbolicObjectId, CorrelationType]()))(f)._3
      }

      //correlações à direita da coluna
      val direita = allCorrelationsFromColumns(plan.columns.toList, 0, DIRECAO_DIREITA)

      println("direita: " + direita)
      
      //correlações à esquerda da coluna
      val esquerda = allCorrelationsFromColumns(plan.columns.reverse.toList, plan.columns.size - 1, DIRECAO_ESQUERDA)
      println("esquerda: " + esquerda)

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
    println("plan.columns.length: " + plan.columns.length)
    println("column lengths: "  + plan.columns.map(_.docs.length))
	println("plan.indexOrder.length: " + plan.indexOrder.length)
	println("indexOrder: " + indexOrder)
    val rootCorrelations: List[RootCorrelationType] = indexOrder.flatMap(rootCorrelationFromColumn)
    println("rootCorrelations: " + rootCorrelations)
    val todosCitados: Set[SymbolicObjectId] = rootCorrelations.foldLeft(Set[SymbolicObjectId]())((acc, v) => objetosCitados(v) ++ acc)
    println("todosCitados: " + todosCitados)
    val todosNaoCitados = plan.columns.map(traversal(_).filterNot(x => todosCitados(x.objetoSimbolico.get.id)))
    println("todosNaoCitados: " + todosNaoCitados)
    (rootCorrelations, todosNaoCitados)
  }
}