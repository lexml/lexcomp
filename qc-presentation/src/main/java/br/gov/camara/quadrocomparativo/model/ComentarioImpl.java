package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Comentario;
import br.gov.lexml.symbolicobject.RefTipo;

public class ComentarioImpl implements Comentario, Serializable {

	private static final long serialVersionUID = -135221691017401227L;

	@XmlElement
	private RefTipoImpl refTipo;
	private long id;
	private String xhtmlFragment;
	private Long alvo;
	
	@Override
	public RefTipo getRefTipo() {
		return refTipo;
	}
	public void setRefTipo(RefTipoImpl refTipo) {
		this.refTipo = refTipo;
	}
	
	@Override
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	public String getXhtmlFragment() {
		return xhtmlFragment;
	}
	public void setXhtmlFragment(String xhtmlFragment) {
		this.xhtmlFragment = xhtmlFragment;
	}
	
	@Override
	public Long getAlvo() {
		return alvo;
	}
	public void setAlvo(Long alvo) {
		this.alvo = alvo;
	}
	
	public String toString() {
	  	return ToStringBuilder.reflectionToString(this);
	}
	
}
