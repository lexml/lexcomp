/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.RoleRotulo;
import br.gov.lexml.symbolicobject.Rotulo;
import br.gov.lexml.symbolicobject.RotuloClassificado;
import br.gov.lexml.symbolicobject.RotuloOrdenado;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author p_7174
 */
@XmlSeeAlso({RotuloClassificadoImpl.class, RotuloOrdenadoImpl.class})
public class RoleRotuloImpl extends RotuloImpl implements RoleRotulo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String nomeRole;

    public RoleRotuloImpl() {
    }

    public RoleRotuloImpl(RoleRotulo role) {
        super(role);

        if (role != null) {
            this.nomeRole = role.getNomeRole();
        }
    }
    
    @Override
    String getRealJavaType() {
    	return "roleRotulo";
    }

    @Override
    public String getNomeRole() {
        return nomeRole;
    }

    public void setNomeRole(String nomeRole) {
        this.nomeRole = nomeRole;
    }
    
    public static RoleRotuloImpl getInstance(Rotulo obj) {
        
        RoleRotuloImpl rotulo;
        
        if (obj instanceof RotuloClassificado) {
                
            rotulo = new RotuloClassificadoImpl((RotuloClassificado) obj);

        } else if (obj instanceof RotuloOrdenado) {

            rotulo = new RotuloOrdenadoImpl((RotuloOrdenado) obj);

        } else {

            rotulo = new RoleRotuloImpl((RoleRotulo) obj);
        }
        
        return rotulo;
    }

    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
