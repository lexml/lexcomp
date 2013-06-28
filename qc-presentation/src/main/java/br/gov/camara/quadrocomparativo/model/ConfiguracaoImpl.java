/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import br.gov.lexml.symbolicobject.comp.Configuracao;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class ConfiguracaoImpl implements Configuracao {

    private double limiarSimilaridade;

    @Override
    public double getLimiarSimilaridade() {
        return limiarSimilaridade;
    }

    public void setLimiarSimilaridade(double limiarSimilaridade) {
        this.limiarSimilaridade = limiarSimilaridade;
    }
    
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
