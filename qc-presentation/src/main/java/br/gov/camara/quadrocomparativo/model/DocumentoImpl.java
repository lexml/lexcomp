/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Documento;
import br.gov.lexml.symbolicobject.Nome;
import br.gov.lexml.symbolicobject.ObjetoSimbolico;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class DocumentoImpl extends IdentificavelImpl implements Documento, Serializable {
    
    private static final long serialVersionUID = -1574980283434994409L;

    @XmlElement
    private ObjetoSimbolicoImpl objetoSimbolico;
    @XmlElement
    private NomeImpl nome;
    
    public DocumentoImpl() {
    }

    public DocumentoImpl(Documento doc) {
        
        if (doc != null) {
            objetoSimbolico = (ObjetoSimbolicoImpl)ObjetoSimbolicoImpl.getInstance(doc.getObjetoSimbolico());
            nome = NomeImpl.getInstance(doc.getNome());
            refTipo = new RefTipoImpl(doc.getRefTipo());
            id = doc.getId();
        }
    }
    
    public ObjetoSimbolico getObjetoSimbolico() {
        return (ObjetoSimbolico) objetoSimbolico;
    }

    public void setObjetoSimbolico(ObjetoSimbolicoImpl objetoSimbolico) {
        this.objetoSimbolico = objetoSimbolico;
    }

    public Nome getNome() {
        return nome;
    }

    public void setNome(NomeImpl nome) {
        this.nome = nome;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    /**
     * Retorna o conjunto de id de todos os ObjetoSimbolico recursivamente até os nós não complexos, inclusive.
     * TODO OTIMIZAR Salvar o conjunto quando criar o documento no construtor
     * @return
     */
    @XmlTransient
    public Set<Long> getObjetoSimbolicoIdSet(){
    	return objetoSimbolico.produceObjetoSimbolicoIdSetFromPosicoes();
    }
}
