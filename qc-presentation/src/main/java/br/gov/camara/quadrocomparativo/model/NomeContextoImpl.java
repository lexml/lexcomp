/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.NomeContexto;
import br.gov.lexml.symbolicobject.RefTipo;

/**
 *
 * @author p_7174
 */
public class NomeContextoImpl extends NomeImpl implements NomeContexto, Serializable {
    
	private static final long serialVersionUID = 940205623041355099L;
	
	@XmlElement
    private RefTipoImpl refTipoContexto;

    public NomeContextoImpl() {
    }

    public NomeContextoImpl(NomeContexto nome) {
        
        super(nome);
        
        if (nome != null) {
            refTipoContexto = new RefTipoImpl(nome.getRefTipoContexto());
        }
    }

    public RefTipo getRefTipoContexto() {
        return refTipoContexto;
    }

    public void setRefTipoContexto(RefTipoImpl refTipoContexto) {
        this.refTipoContexto = refTipoContexto;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    @Override
    String getRealJavaType() {
    	return "nomeContexto";
    }
}
