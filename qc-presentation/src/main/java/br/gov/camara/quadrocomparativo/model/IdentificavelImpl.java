package br.gov.camara.quadrocomparativo.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import br.gov.lexml.symbolicobject.Identificavel;
import br.gov.lexml.symbolicobject.RefTipo;

public class IdentificavelImpl implements Identificavel {
	@XmlElement
	protected RefTipoImpl refTipo;
	protected long id;
	
	protected Map<String,String> properties = new HashMap<String,String>();
	
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
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}	
	
}
