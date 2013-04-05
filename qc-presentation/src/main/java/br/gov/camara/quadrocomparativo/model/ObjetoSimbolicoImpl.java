/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.lang.builder.ToStringBuilder;

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
		ObjetoSimbolicoSimplesImpl.class })
abstract class ObjetoSimbolicoImpl implements ObjetoSimbolico, Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement
	private RefTipoImpl refTipo;
	protected long id;

	public ObjetoSimbolicoImpl() {
	}

	public ObjetoSimbolicoImpl(ObjetoSimbolico obj) {

		if (obj != null) {
			id = obj.getId();
			refTipo = new RefTipoImpl(obj.getRefTipo());
		}
	}

	public RefTipo getRefTipo() {
		return refTipo;
	}

	public void setRefTipo(RefTipoImpl refTipo) {
		this.refTipo = refTipo;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
	
	/**
     * Produz o conjunto de id de todos os ObjetoSimbolico das posicoes
     * a partir de this, inclusive este, recursivamente até os nós não complexos, inclusive.
     * @return
     */
    abstract public Set<Long> produceObjetoSimbolicoIdSetFromPosicoes();

}
