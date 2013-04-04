/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.ObjetoSimbolico;
import br.gov.lexml.symbolicobject.Posicao;
import br.gov.lexml.symbolicobject.Rotulo;

/**
 *
 * @author p_7174
 */
class PosicaoImpl implements Posicao, Serializable{
    
    private static final long serialVersionUID = -381481279414275644L;

    @XmlElement
    private ObjetoSimbolicoImpl objeto;
    @XmlElement
    private RotuloImpl rotulo;

    public PosicaoImpl() {
    }
    
    public PosicaoImpl(Posicao pos) {
        
        if (pos != null) {
            
            objeto = ObjetoSimbolicoImpl.getInstance(pos.getObjeto());
            rotulo = RotuloImpl.getInstance(pos.getRotulo());
        }
    }

    public ObjetoSimbolico getObjeto() {
        return objeto;
    }

    public void setObjeto(ObjetoSimbolicoImpl ObjetoSimbolico) {
        this.objeto = ObjetoSimbolico;
    }
    
    @XmlTransient
    public ObjetoSimbolicoImpl getObjetoSimbolicoImpl() {
        return objeto;
    }

    @Override
    public Rotulo getRotulo() {
        return rotulo;
    }

    public void setRotulo(RotuloImpl rotulo) {
        this.rotulo = rotulo;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
}
