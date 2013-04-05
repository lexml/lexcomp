/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.RefTipo;
import br.gov.lexml.symbolicobject.RoleRotulo;
import br.gov.lexml.symbolicobject.Rotulo;

/**
 *
 * @author p_7174
 */
@XmlSeeAlso({RoleRotuloImpl.class})
class RotuloImpl implements Rotulo, Serializable {
    
	private static final long serialVersionUID = -8342064672726515866L;
	
	private String representacao;
    @XmlElement
    private RefTipoImpl refTipo;

    public RotuloImpl() {
    }
    
    public RotuloImpl(Rotulo rotulo) {
        
        if (rotulo != null) {
            representacao = rotulo.getRepresentacao();
            refTipo = new RefTipoImpl(rotulo.getRefTipo());
        }
    }

    @Override
    public String getRepresentacao() {
        return representacao;
    }

    public void setRepresentacao(String representacao) {
        this.representacao = representacao;
    }

    @Override
    public RefTipo getRefTipo() {
        return refTipo;
    }

    public void setRefTipo(RefTipoImpl refTipo) {
        this.refTipo = refTipo;
    }
    
    public static RotuloImpl getInstance(Rotulo obj) {
        
        RotuloImpl rotulo;
        
        if (obj instanceof RoleRotulo) {

            rotulo = RoleRotuloImpl.getInstance((RoleRotulo) obj);

        } else {
            rotulo = new RotuloImpl(obj);
        }
        
        return rotulo;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
