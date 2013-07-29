/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Nome;
import br.gov.lexml.symbolicobject.NomeRelativo;
import br.gov.lexml.symbolicobject.Rotulo;

/**
 *
 * @author p_7174
 */
public class NomeRelativoImpl extends NomeImpl implements NomeRelativo, Serializable {
    
    private static final long serialVersionUID = 7808486069804849497L;
    	
    @XmlElement
    private RotuloImpl rotulo;
    @XmlElement
    private NomeImpl sobreNome;

    public NomeRelativoImpl() {
    }

    public NomeRelativoImpl(NomeRelativo nome) {
        super(nome);
        
        if (nome != null) {
            rotulo = RotuloImpl.getInstance(nome.getRotulo());
            sobreNome = NomeImpl.getInstance(nome.getSobreNome());
        }
    }

    @Override
    public Rotulo getRotulo() {
        return rotulo;
    }

    public void setRotulo(RotuloImpl rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public Nome getSobreNome() {
        return sobreNome;
    }

    public void setSobreNome(NomeImpl sobreNome) {
        this.sobreNome = sobreNome;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
    
    @Override
    String getRealJavaType() {
    	return "nomeRelativo";
    }
}
