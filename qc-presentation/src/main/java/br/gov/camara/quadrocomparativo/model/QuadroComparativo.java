/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Documento;
import br.gov.lexml.symbolicobject.parser.IdSource;
import br.gov.lexml.symbolicobject.tipos.STipo;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class QuadroComparativo implements Serializable, IdSource{
    
	private static final long serialVersionUID = -6227052315334283202L;
	private String id;
    private String titulo;
    private List<Coluna> colunas;
    private List<Conexao> conexoes;
    private String dataModificacao;
    private List<Correlacao> correlacoes;
    private long currentPosition = 0;
    
    private boolean articulacoesExcluidas = false;
    
    public void setArticulacoesExcluidas(boolean articulacoesExcluidas) {
		this.articulacoesExcluidas = articulacoesExcluidas;
	}
    
    public boolean isArticulacoesExcluidas() {
		return articulacoesExcluidas;
	}
    
	public QuadroComparativo() {
        this.id = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    public QuadroComparativo(String id) {
        this.id = id;
    }

    public QuadroComparativo(String id, String titulo) {
        this.id = id;
        this.titulo = titulo;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDataModificacao() {
        return dataModificacao;
    }

    public void setDataModificacao(String dataModificacao) {
        this.dataModificacao = dataModificacao;
    }
    
    @XmlTransient
    public void setDataModificacao2(Date date) {
        this.dataModificacao = new SimpleDateFormat("yyyyMMddHHmmssSSS")
                .format(date);
    } 
    
    @XmlElement
    public List<Coluna> getColunas() {
        return colunas;
    }

    public void addColuna(Coluna coluna) {
        
        if (colunas == null) {
            colunas = new ArrayList<Coluna>();
        }
        
        if (!colunas.contains(coluna)) {
            
            if (coluna.getId() == null) {
                coluna.setId(getId() + "-" + (colunas.size() + 1));
            }
            
            if (coluna.getTitulo() == null) {
                coluna.setTitulo("Nova coluna");
            }
            colunas.add(coluna);
        }
    }

    @XmlElement
    public List<Conexao> getConexoes() {
        return conexoes;
    }
    
    public void addConexao(Conexao conexao) {
        
        if (conexoes == null) {
            conexoes = new ArrayList<Conexao>();
        }
        
        if (!conexoes.contains(conexao)) {
            
            conexoes.add(conexao);
        }
    }

    @XmlElement
    public List<Correlacao> getCorrelacoes() {
        return correlacoes;
    }
    
    public void addCorrelacao(Correlacao correlacao) {
        
        if (correlacoes == null) {
            correlacoes = new ArrayList<Correlacao>();
        }
        
        if (!correlacoes.contains(correlacao)) {
            
            correlacoes.add(correlacao);
        }
    }
    
    @XmlTransient
    public String getFileName() {
        return getFileName(getId());
    }
    
    public static String getFileName(String id) {
        return "qc-" + id + ".xml";
    }
    
	public Texto getTexto(String urn) {

		for (Texto txt : getAllTextos()) {

			if (txt.getUrn() != null && txt.getUrn().equals(urn)) {
				return txt;
			}
		}

		return null;
	}
    
    public Texto getTexto(String colId, String urn) {
        
        Coluna col = getColuna(colId);
        
        if(col != null && col.getTextos() != null){
            for (Texto txt : col.getTextos()) {

                if (txt.getUrn()!= null && txt.getUrn().equals(urn)) {
                    return txt;
                }
            }
        }
        return null;
    }
    
    public Coluna getColuna(String colId) {
        
        for (Coluna col : getColunas()) {
                
            if (col.getId() != null && col.getId().equals(colId)) {
                return col;
            }
        }
        
        return null;
    }

    public void addTexto(String colId, Texto texto) {
        
        Coluna col = getColuna(colId);
        if (col != null){
	        if (getTexto(colId, texto.getUrn()) != null) {
	        
	            Texto txt = getTexto(colId, texto.getUrn());
	            texto.setOrder(txt.getOrder());
	            int index = col.getTextos().indexOf(txt);
	            col.getTextos().set(index, texto);
	        
	        } else {
	            col.addTexto(texto);
	        }
        }
    }

    public Correlacao getCorrelacao(String urn1, String urn2) {
        
        if (getCorrelacoes() != null) {
            
            for (Correlacao cor : getCorrelacoes()) {
            	
            	if (cor.getUrn1() != null && cor.getUrn2() != null && (
            			(cor.getUrn1().equals(urn1) && cor.getUrn2().equals(urn2)) ||
            			 cor.getUrn1().equals(urn2) && cor.getUrn2().equals(urn1))) {
            	
                    return cor;
                }
            }
        }
        
        return null;
    }
    
    @XmlTransient
    public List<Documento> getAllDocumentos(){
   		List<Documento> r = new ArrayList<Documento>();
   		
   		for (Texto txt : getAllTextos()) {
   			r.add(txt.getDocumento());
   		}
    		
   		return r;
    }
    
    @XmlTransient
    public List<Texto> getAllTextos(){
   		List<Texto> r = new ArrayList<Texto>();
   		
   		if (getColunas() != null) {
            for (Coluna col : getColunas()) {
                
            	r.addAll(col.getTextos());
            	
            }
   		}
    		
   		return r;
    }

    public void setColunas(List<Coluna> colunas) {
        this.colunas = colunas;
    }

    public void setConexoes(List<Conexao> conexoes) {
        this.conexoes = conexoes;
    }

    public void setCorrelacoes(List<Correlacao> correlacoes) {
        this.correlacoes = correlacoes;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    
    
    synchronized public long nextId(STipo tipo) {
		return ++currentPosition;
	}
    synchronized public long getCurrentPosition() {
		return currentPosition;
	}
    synchronized public void setCurrentPosition(long currentPosition) {
		this.currentPosition = currentPosition;
	}

}
