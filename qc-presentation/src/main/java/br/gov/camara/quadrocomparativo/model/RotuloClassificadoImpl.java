/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import br.gov.lexml.symbolicobject.RotuloClassificado;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author p_7174
 */
public class RotuloClassificadoImpl extends RoleRotuloImpl
    implements RotuloClassificado {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<String> classificacao;

    public RotuloClassificadoImpl() {
    }
    
    public RotuloClassificadoImpl(RotuloClassificado rotulo) {
        super(rotulo);
        
        if (rotulo != null && !rotulo.getClassificacao().isEmpty()) {
            
            classificacao = new ArrayList<String>(rotulo.getClassificacao());
        }
    }

    @Override
    public List<String> getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(List<String> classificacao) {
        this.classificacao = classificacao;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
