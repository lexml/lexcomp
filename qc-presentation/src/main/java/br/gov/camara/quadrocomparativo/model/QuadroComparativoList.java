/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class QuadroComparativoList {
    
    @XmlElement
    private List<QuadroComparativo> quadros;
    
    public List<QuadroComparativo> getQuadros() {
        return quadros;
    }

    public void addColuna(QuadroComparativo quadro) {
        
        if (quadros == null) {
            quadros = new ArrayList<QuadroComparativo>();
        }
        
        if (!quadros.contains(quadro)) {
            
            quadros.add(quadro);
        }
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
