/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Comentario;
import br.gov.lexml.symbolicobject.Relacao;
import br.gov.lexml.symbolicobject.parser.IdSource;
import br.gov.lexml.symbolicobject.tipos.STipo;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class Correlacao implements Serializable {
    
    private static final long serialVersionUID = 1286987453288856189L;

    private String urn1;
    private String urn2;
    private Texto texto1;
    private Texto texto2;
    @XmlElement
    private List<RelacaoImpl> relacoes;
    @XmlElement
    private List<ComentarioImpl> comentarios;
    private ConfiguracaoImpl configuracao;

    private CorrelacaoEstatisticaTexto estatisticaTexto1;
    private CorrelacaoEstatisticaTexto estatisticaTexto2;
    
    public Correlacao() {
    }

    public Correlacao(String urn1, String urn2) {
        this.urn1 = urn1;
        this.urn2 = urn2;
    }
    
    public String getUrn1() {
        return urn1;
    }

    public void setUrn1(String urn1) {
        
        if (urn1 != null) {
            urn1 = urn1.replaceAll("__", ";");
        }
        this.urn1 = urn1;
    }

    public String getUrn2() {
        return urn2;
    }

    public void setUrn2(String urn2) {
        if (urn2 != null) {
            urn2 = urn2.replaceAll("__", ";");
        }
        this.urn2 = urn2;
    }

    public Texto getTexto1() {
        return texto1;
    }

    public void setTexto1(Texto texto1) {
        this.texto1 = texto1;
    }

    public Texto getTexto2() {
        return texto2;
    }

    public void setTexto2(Texto texto2) {
        this.texto2 = texto2;
    }

    public List<Relacao> getRelacoes() {
        return (List)relacoes;
    }
    
    public void setRelacoes(List<RelacaoImpl> relacoes) {
        this.relacoes = relacoes;
    }
        
    public List<Comentario> getComentarios() {
    	return (List)comentarios;
   	}

    public void setComentarios(List<ComentarioImpl> comentarios) {
    	this.comentarios = comentarios;
   	}
    
    public void addRelacao(RelacaoImpl relacao, IdSource idSource) {
    	
    	//obriga recalcular a estatística
    	estatisticaTexto1 = null;
    	estatisticaTexto2 = null;
        
        if (relacoes == null) {
            relacoes = new ArrayList<RelacaoImpl>();
        }
        
        // ID sequencial
        if (relacao.getId() == 0) {
        	relacao.setId(idSource.nextId(null));
            
            while (relacoes.contains(relacao)) {
                relacao.setId(relacao.getId() + 1);
            }
        }
        
        if (relacoes.contains(relacao)) {
            relacoes.remove(relacao);
        }
        
        relacoes.add(relacao);
    }

    public void removeRelacao(String idRelacao) {
        
    	//obriga recalcular a estatística
    	estatisticaTexto1 = null;
    	estatisticaTexto2 = null;

        if (relacoes == null) {
            return;
        }
        
        List<RelacaoImpl> relacoesToRemove = new ArrayList<RelacaoImpl>();
        
        for (RelacaoImpl rel : relacoes) {
            
            if (rel.getId() == Long.parseLong(idRelacao)) {
                
                relacoesToRemove.add(rel);
            }
        }
        
        for (RelacaoImpl rel : relacoesToRemove) {
            
            relacoes.remove(rel);
        }
    }
    
    public void removeAllRelacoes(){
    	if (relacoes != null){
    		relacoes.clear();
    	}
    }

    @XmlElement
    public ConfiguracaoImpl getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(ConfiguracaoImpl configuracao) {
        this.configuracao = configuracao;
    }

    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    public void setEstatisticaTexto1(CorrelacaoEstatisticaTexto estatisticaTexto1) {
		this.estatisticaTexto1 = estatisticaTexto1;
	}

	public void setEstatisticaTexto2(CorrelacaoEstatisticaTexto estatisticaTexto2) {
		this.estatisticaTexto2 = estatisticaTexto2;
	}

	@XmlElement
    public CorrelacaoEstatisticaTexto getEstatisticaTexto1(){
    	if (estatisticaTexto1 == null){
    		estatisticaTexto1 = produceCorrelacaoEstaticaTexto(texto1);
    	}
    	return estatisticaTexto1;
    }
    
	@XmlElement
    public CorrelacaoEstatisticaTexto getEstatisticaTexto2(){
    	if (estatisticaTexto2 == null){
    		estatisticaTexto2 = produceCorrelacaoEstaticaTexto(texto2);
    	}
    	return estatisticaTexto2;
    }
    
    private CorrelacaoEstatisticaTexto produceCorrelacaoEstaticaTexto(Texto t){
    	
    	if (t == null){
    		return null;
    	}
    	
    	//produz o conjunto de id presentes em relacoes
    	Set<Long> res = new HashSet<Long>();
    	if (relacoes != null && !relacoes.isEmpty()){
	    	if (t == texto1){
	    		for (RelacaoImpl r : relacoes){
	    			res.addAll(r.getOrigem());
		    	}
		    } else {
		    	for (RelacaoImpl r : relacoes){
		    		res.addAll(r.getAlvo());
		    	}
		    }
    	}
	    	
	    return new CorrelacaoEstatisticaTexto(t.getDocumento().getObjetoSimbolicoIdSet().size(), res.size()); 
    }
    
}