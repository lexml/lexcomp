/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class Conexao implements Serializable{

	private static final long serialVersionUID = 7790908911700692335L;
	
	private String sourceId;
    private String targetId;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
