/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.TextoPuro;

/**
 *
 * @author p_7174
 */
public class TextoPuroImpl extends ObjetoSimbolicoSimplesImpl
    implements TextoPuro {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String texto;

    public TextoPuroImpl() {
    }

    public TextoPuroImpl(TextoPuro tex) {
        super(tex);
        setTexto(tex.getTexto());
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    @Override
	protected String getRealJavaType() {
		return "textoPuro";
	}
}
