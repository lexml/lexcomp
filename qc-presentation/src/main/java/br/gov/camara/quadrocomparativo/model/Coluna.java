/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;
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
public class Coluna implements Serializable{
    
	private static final long serialVersionUID = 4464717490759737337L;
	
	private String id;
    private String titulo;
    @XmlElement
    private List<Texto> textos = new ArrayList<Texto>();
    private String order;
    private boolean colunaPrincipal;

    public Coluna() {
    }

    public Coluna(String id, String titulo) {
        this.id = id;
        this.titulo = titulo;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<Texto> getTextos() {
        return textos;
    }

    public void addTexto(Texto texto) {
        
        if (textos == null) {
            textos = new ArrayList<Texto>();
        }
        
        if (!textos.contains(texto)) {
            textos.add(texto);
        }
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
    
    public boolean getColunaPrincipal() {
        return colunaPrincipal;
    }

    public void setColunaPrincipal(Boolean colunaPrincipal) {
        this.colunaPrincipal = colunaPrincipal;
    }
    
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
