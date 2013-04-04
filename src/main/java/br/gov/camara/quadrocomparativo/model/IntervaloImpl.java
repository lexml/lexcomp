/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Intervalo;

/**
 *
 * @author p_7174
 */
public class IntervaloImpl implements Intervalo {
    
    private long primeiroId;
    private long ultimoId;

    public IntervaloImpl() {
    }

    public IntervaloImpl(Intervalo intervalo) {
        
        if (intervalo != null) {
            primeiroId = intervalo.getPrimeiroId();
            ultimoId = intervalo.getUltimoId();
        }
    }
    
    public long getPrimeiroId() {
        return primeiroId;
    }

    public void setPrimeiroId(long primeiroId) {
        this.primeiroId = primeiroId;
    }

    public long getUltimoId() {
        return ultimoId;
    }

    public void setUltimoId(long ultimoId) {
        this.ultimoId = ultimoId;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
