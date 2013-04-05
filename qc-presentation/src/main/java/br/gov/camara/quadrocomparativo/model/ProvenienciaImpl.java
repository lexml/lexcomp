/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import br.gov.lexml.symbolicobject.Proveniencia;
import br.gov.lexml.symbolicobject.RefTipo;

/**
 *
 * @author p_7174
 */
@XmlSeeAlso({ProvenienciaSistemaImpl.class, ProvenienciaUsuarioImpl.class})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class ProvenienciaImpl implements Proveniencia {

    @XmlElement
    private RefTipoImpl refTipo;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public RefTipo getRefTipo() {
        return refTipo;
    }

    public void setRefTipo(RefTipoImpl refTipo) {
        this.refTipo = refTipo;
    }
}
