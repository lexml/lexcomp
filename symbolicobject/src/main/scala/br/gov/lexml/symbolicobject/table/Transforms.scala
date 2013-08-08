package br.gov.lexml.symbolicobject.table

object Transforms {

  /* Cada célula tem um conteúdo (opcional), posição (coluna) e tamanho (colspan) */
  type InterCell[X] = (Option[X],Int,Int)
  type InterRow[X] = IndexedSeq[InterCell[X]]
  
  def extractLines[N,E](fc : FlatCorrelation[N,E]) : (InterRow[N],InterRow[E]) = {    
    type T = (Int,IndexedSeq[(InterCell[N],Option[InterCell[E]])])
    
    def f(d : Option[N], nc : Int,p : Option[(Option[E],T)] ) : T = p match {
      case None => (nc,IndexedSeq(((d,nc,1),None)))
      case Some((mr,(nc1,a))) => (nc,((d,nc,1),Some((mr,nc,nc1 - nc))) +: a)
    }    
	val (x,y) = fc.fold(f)._2.unzip
	(x,y.flatten)
  }
  
  /* Substituimos Option[IndexedSeq[A]] => IndexedSeq[A], collapsando None => IndexedSeq()*/
  type InterCell2[A] = (IndexedSeq[A],Int,Int)
  type InterRow2[A] = IndexedSeq[InterCell2[A]]
  
  def filterEmpty[A](x : InterRow[IndexedSeq[A]]) : InterRow2[A] = x.map {
    case (None,a,n)  => (IndexedSeq(),a,n)
    case (x,a,n) => (x.toIndexedSeq.flatten,a,n)
  }
  
  /* Acrescenta-se células vazias com tamanho apropriado entre células vizinhas que possuem um espaço entre si. */
  type InterCell3[A] = (IndexedSeq[A],Int)
  type InterRow3[A] = IndexedSeq[InterCell3[A]]
  
  def completeH[A](l : InterRow2[A]) : InterRow3[A] =
	l.foldLeft((0,IndexedSeq[(IndexedSeq[A],Int)]())) {
       case ((curPos,curL),(d,start,len)) 
    	  if curPos < start => (start + len, (d,len) +: (IndexedSeq(),start - curPos) +: curL)
       case ((curPos,curL),(d,start,len)) => (start + len, (d,len) +: curL)	   
    }._2.reverse
  
  /* Acrescenta uma coluna, representada como um vetor, à uma matriz, representada como uma lista de linhas, 
   * completando o que falta na coluna com uma célula vazia e o na matriz com uma linha vazia
   */  
  def complete[A](blank : A, col : IndexedSeq[A], rows : IndexedSeq[IndexedSeq[A]]) : IndexedSeq[IndexedSeq[A]] = 
    	col.map(List(_)).zipAll(rows,IndexedSeq(blank),IndexedSeq()).map { case(x,y) => (x ++ y).toIndexedSeq }
  
  /* Alinha as partes de cada célula da matriz, formando as linhas da tabela */  
  type InterCell4[A] = (Option[A],Int)
  type InterRow4[A] = IndexedSeq[InterCell4[A]]
  type InterTable4[A] = IndexedSeq[InterRow4[A]]
    
  def expand[A](l : InterRow3[A]) = l.foldRight[InterTable4[A]](IndexedSeq()) {
    case ((al,n),r) => complete((None,n),al.map ( x => (Some(x),n) ),r)    
  }  

  type CorrelationCell[N,E] = Cell[CorrelationCellData[N,E]]
  
  type InterTable5[N,E] = IndexedSeq[IndexedSeq[CorrelationCell[N,E]]]
  
  /* Cria a estrutura matricial da tabela */
  def makeCells[A,N,E](toCellData : A => CorrelationCellData[N,E])(
                      t : InterTable4[A]) : InterTable5[N,E] = 
     t.map(_.map { case (oa,cs) => Cell(oa.map(toCellData).getOrElse(NoData),cs)})
  
  def replicate[A](n : Int, x : A) : IndexedSeq[A] = (0 until n).map(_ => x).toIndexedSeq
     
  /** Transforma células com colspan > 1 em uma célula com colspan = 1 e várias celulas vazias de colspan=1 */
  def expandH[N,E](t : InterTable5[N,E]) : InterTable5[N,E] = 
    	t.map(_.flatMap { cell =>
    		Cell(cell.content,1) +: replicate(cell.cs - 1,Cell(NoData,1))
    	})     
  
  /** duplica o tamanho (colspan) das células */ 
  def dupWidth[N,E](l : InterTable5[N,E]) : InterTable5[N,E] = 
    	l.map(_.map { case Cell(x,cs) => Cell(x,cs*2)})
    	
  def rootCorrelationToTable[N,E](rootCorrelations : IndexedSeq[RootCorrelation[IndexedSeq[N],IndexedSeq[E]]]) : Table[CorrelationCellData[N,E]] = {
    val lines = rootCorrelations.flatMap{ rc => 
      
	    val fcs = rc.flatCorrelations
	    
	    fcs.flatMap { fc =>  
		    
		    /** extrai a linha de dispositivos e de relações de cada linha de correlação */    
		    val (irowN,irowE) = extractLines(fc)
		    
		    def f[A](toCellData : A => CorrelationCellData[N,E]) =
		      ((filterEmpty[A] _) andThen (completeH _) andThen expand andThen makeCells[A,N,E](toCellData)) 
		    	//makeCells(toCellData,expand(completeH(filterEmpty(x))))
		    	
		    val irowN1 = (f((x : N) => NodeData(x)) andThen (expandH _) andThen (dupWidth _))(irowN) 
		    
		    val irowE1 = (f((x : E) => EdgeData(x)) andThen (dupWidth _) andThen (_.map(x => Cell(NoData,1) +: x)))(irowE)
		    
		    irowN1 ++ irowE1 
	    }
    }
    
    Table(lines)
  }
}