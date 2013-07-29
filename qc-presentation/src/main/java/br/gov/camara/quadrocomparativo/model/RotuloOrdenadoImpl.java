/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import br.gov.lexml.symbolicobject.RotuloOrdenado;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author p_7174
 */
public class RotuloOrdenadoImpl extends RoleRotuloImpl
        implements RotuloOrdenado {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<Integer> posicaoRole;

    public RotuloOrdenadoImpl() {
    }

    public RotuloOrdenadoImpl(RotuloOrdenado rotulo) {
        super(rotulo);

        if (rotulo != null && !rotulo.getPosicaoRole().isEmpty()) {

            posicaoRole = new ArrayList<Integer>(rotulo.getPosicaoRole());
        }
    }
    
	@Override
	String getRealJavaType() {
		return "rotuloOrdenado";
	}

    @Override
    public List<Integer> getPosicaoRole() {
        return posicaoRole;
    }

    public void setPosicaoRole(List<Integer> posicaoRole) {
        this.posicaoRole = posicaoRole;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
