/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.TextoFormatado;

/**
 *
 * @author p_7174
 */
public class TextoFormatadoImpl extends ObjetoSimbolicoSimplesImpl
    implements TextoFormatado, Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String xhtmlFragment;

    public TextoFormatadoImpl() {
    }

    public TextoFormatadoImpl(TextoFormatado tex) {
        super(tex);
        setXhtmlFragment(tex.getXhtmlFragment());
    }
    
    public String getXhtmlFragment() {
        return xhtmlFragment;
    }

    public void setXhtmlFragment(String xhtmlFragment) {
        this.xhtmlFragment = xhtmlFragment;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
}
