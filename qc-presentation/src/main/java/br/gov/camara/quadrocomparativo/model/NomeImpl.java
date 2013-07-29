/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import br.gov.lexml.symbolicobject.Nome;
import br.gov.lexml.symbolicobject.NomeContexto;
import br.gov.lexml.symbolicobject.NomeRelativo;

/**
 *
 * @author p_7174
 */
@XmlSeeAlso({NomeRelativoImpl.class, NomeContextoImpl.class})
@JsonTypeInfo(
		use=JsonTypeInfo.Id.NAME,
		include=JsonTypeInfo.As.PROPERTY,
		property="javaType"
		)
@JsonSubTypes({
	@JsonSubTypes.Type(value=NomeContextoImpl.class,name="nomeContexto"),
	@JsonSubTypes.Type(value=NomeRelativoImpl.class,name="nomeRelativo"),
})
abstract class NomeImpl implements Nome {
    
	private String representacao;
	private String javaType;
    
    protected NomeImpl() {
    }
    
    protected NomeImpl(Nome nome) {
        
        if (nome != null) {
            representacao = nome.getRepresentacao();
        }
        
        javaType = getRealJavaType();
    }

    public String getRepresentacao() {
        return representacao;
    }

    public void setRepresentacao(String representacao) {
        this.representacao = representacao;
    }
    
    public static NomeImpl getInstance(Nome obj) {
        
        NomeImpl nome = null;
        
        if (obj instanceof NomeRelativo) {
                
            nome = new NomeRelativoImpl((NomeRelativo) obj);

        } else if (obj instanceof NomeContexto) {

            nome = new NomeContextoImpl((NomeContexto) obj);

        } /*else {
            nome = new NomeImpl(obj);
        }
        */
        else {
        	nome = null;
        }
        return nome;
    }
    
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}
	
	abstract String getRealJavaType();
    
}