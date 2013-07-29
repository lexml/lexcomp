/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.ObjetoSimbolicoSimples;

/**
 *
 * @author p_7174
 */
@XmlSeeAlso({TextoFormatadoImpl.class, TextoPuroImpl.class})
public abstract class ObjetoSimbolicoSimplesImpl extends ObjetoSimbolicoImpl
    implements ObjetoSimbolicoSimples {

    private static final long serialVersionUID = -4419082532977943470L;

    private String representacao;

    public ObjetoSimbolicoSimplesImpl() {
    }

    public ObjetoSimbolicoSimplesImpl(ObjetoSimbolicoSimples obj) {
        super(obj);
        
        if (obj != null) {
            representacao = obj.getRepresentacao();
        }
    }
    
    public String getRepresentacao() {
        return representacao;
    }

    public void setRepresentacao(String representacao) {
        this.representacao = representacao;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    public Set<Long> produceObjetoSimbolicoIdSetFromPosicoes(){
    	
	    Set<Long> r = new HashSet<Long>();
		
		r.add(id);
		
		return r;
    }

}
