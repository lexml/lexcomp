/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.RefTipo;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class RefTipoImpl implements RefTipo, Serializable {
    
	private static final long serialVersionUID = 4849576188652564654L;

	private String nomeTipo;

    public RefTipoImpl() {
    }

    public RefTipoImpl(String nomeTipo) {
        this.nomeTipo = nomeTipo;
    }
    
    public RefTipoImpl(RefTipo refTipo) {
        
        if (refTipo != null) {
            nomeTipo = refTipo.getNomeTipo();
        }
    }

    @Override
    public String getNomeTipo() {
        return nomeTipo;
    }

    public void setNomeTipo(String nomeTipo) {
        this.nomeTipo = nomeTipo;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
