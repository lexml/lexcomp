/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import br.gov.lexml.symbolicobject.ObjetoSimbolico;
import br.gov.lexml.symbolicobject.ObjetoSimbolicoComplexo;
import br.gov.lexml.symbolicobject.RefTipo;
import br.gov.lexml.symbolicobject.TextoFormatado;
import br.gov.lexml.symbolicobject.TextoPuro;

/**
 * 
 * @author p_7174
 */
@XmlSeeAlso({ ObjetoSimbolicoComplexoImpl.class,
		ObjetoSimbolicoSimplesImpl.class,
		TextoFormatadoImpl.class, TextoPuroImpl.class})
@JsonTypeInfo(
		use=JsonTypeInfo.Id.NAME,
		include=JsonTypeInfo.As.PROPERTY,
		property="javaType"
		)
@JsonSubTypes({
	@JsonSubTypes.Type(value=ObjetoSimbolicoComplexoImpl.class,name="objetoSimbolicoComplexo"),
	@JsonSubTypes.Type(value=TextoPuroImpl.class,name="textoPuro"),
	@JsonSubTypes.Type(value=TextoFormatadoImpl.class,name="textoFormatado")
})
public abstract class ObjetoSimbolicoImpl extends IdentificavelImpl implements ObjetoSimbolico, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String javaType;

	public ObjetoSimbolicoImpl() {
		javaType = getRealJavaType();
	}

	public ObjetoSimbolicoImpl(ObjetoSimbolico obj) {
		this();
		if (obj != null) {
			id = obj.getId();
			refTipo = new RefTipoImpl(obj.getRefTipo());
		}
	}

	public static ObjetoSimbolicoImpl getInstance(ObjetoSimbolico obj) {

		ObjetoSimbolicoImpl objeto = null;

		if (obj instanceof TextoPuro) {

			objeto = new TextoPuroImpl((TextoPuro) obj);

		} else if (obj instanceof TextoFormatado) {

			objeto = new TextoFormatadoImpl((TextoFormatado) obj);

		} else if (obj instanceof ObjetoSimbolicoComplexo) {

			objeto = new ObjetoSimbolicoComplexoImpl(
					(ObjetoSimbolicoComplexo) obj);
		}

		/*
		 * } else if (obj instanceof ObjetoSimbolicoSimples) {
		 * 
		 * objetoOuRef = new ObjetoSimbolicoSimplesImpl(
		 * (ObjetoSimbolicoSimples) obj);
		 * 
		 * } else if (obj instanceof ObjetoSimbolico) { objetoOuRef = new
		 * ObjetoSimbolicoImpl((ObjetoSimbolico) obj);
		 * 
		 * } else { objetoOuRef = new ObjetoSimbolicoOuRefImpl(obj);
		 */

		return objeto;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}
	
	/**
     * Produz o conjunto de id de todos os ObjetoSimbolico das posicoes
     * a partir de this, inclusive este, recursivamente até os nós não complexos, inclusive.
     * @return
     */
    abstract public Set<Long> produceObjetoSimbolicoIdSetFromPosicoes();


    abstract protected String getRealJavaType();
    
     
}
