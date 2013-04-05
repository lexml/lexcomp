package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

public class CorrelacaoEstatisticaTexto implements Serializable{

	private static final long serialVersionUID = -6164315961615108650L;
	
	private int quantDispTotal;
	private int quantDispRelacionados;
	private int quantDispNaoRelacionados;
	
	public CorrelacaoEstatisticaTexto(){
		
	}
	
	public CorrelacaoEstatisticaTexto(int quantDispTotal, int quantDispRelacionados){
		this.quantDispTotal = quantDispTotal;
		this.quantDispRelacionados = quantDispRelacionados;
		this.quantDispNaoRelacionados = quantDispTotal - quantDispRelacionados;
	}
	
	public int getQuantDispTotal() {
		return quantDispTotal;
	}
	public int getQuantDispRelacionados() {
		return quantDispRelacionados;
	}
	public int getQuantDispNaoRelacionados() {
		return quantDispNaoRelacionados;
	}

	public void setQuantDispTotal(int quantDispTotal) {
		this.quantDispTotal = quantDispTotal;
	}

	public void setQuantDispRelacionados(int quantDispRelacionados) {
		this.quantDispRelacionados = quantDispRelacionados;
	}

	public void setQuantDispNaoRelacionados(int quantDispNaoRelacionados) {
		this.quantDispNaoRelacionados = quantDispNaoRelacionados;
	}
	
	
	
}
