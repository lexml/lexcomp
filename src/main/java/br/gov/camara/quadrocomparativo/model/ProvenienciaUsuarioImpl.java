/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.ProvenienciaUsuario;
import java.io.Serializable;

/**
 *
 * @author p_7174
 */
public class ProvenienciaUsuarioImpl extends ProvenienciaImpl
        implements ProvenienciaUsuario, Serializable {

    private static final long serialVersionUID = 782494371607661947L;

    private int noneUsuario = 0;
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

	public int getNoneUsuario() {
		return noneUsuario;
	}

	public void setNoneUsuario(int noneUsuario) {
		this.noneUsuario = noneUsuario;
	}

    
}
