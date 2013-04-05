/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.ProvenienciaSistema;
import java.io.Serializable;

/**
 *
 * @author p_7174
 */
public class ProvenienciaSistemaImpl extends ProvenienciaImpl
        implements ProvenienciaSistema, Serializable {
    
    
    private static final long serialVersionUID = 1241325679911358412L;
    
    private int noneSistema = 1;
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

	public int getNoneSistema() {
		return noneSistema;
	}

	public void setNoneSistema(int noneSistema) {
		this.noneSistema = noneSistema;
	}
    
    
}
