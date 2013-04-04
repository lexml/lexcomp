/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import br.gov.lexml.symbolicobject.RefTipo;
import br.gov.lexml.symbolicobject.Relacao;
import br.gov.lexml.symbolicobject.tipos.Tipos;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class RelacaoImpl implements Relacao, Serializable {

    private static final long serialVersionUID = 763798331027456618L;
    private Set<Long> origem;
    private Set<Long> alvo;
    @XmlElement
    private RefTipoImpl refTipo;
    private long id;
    private ProvenienciaImpl proveniencia;

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
    public Set<Long> getOrigem() {
        return origem;
    }

    public void setOrigem(Set<Long> origem) {
        this.origem = origem;
    }

    @Override
    public Set<Long> getAlvo() {
        return alvo;
    }

    public void setAlvo(Set<Long> alvo) {
        this.alvo = alvo;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.refTipo != null ? this.refTipo.hashCode() : 0);
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RelacaoImpl other = (RelacaoImpl) obj;
        /*
         * if (this.refTipo != other.refTipo && (this.refTipo == null ||
         * !this.refTipo.equals(other.refTipo))) { return false; }
         */
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public static class TiposRelacao {

        public static final Map<String, Map<String, String>> mapsTiposRelacao = Tipos.tiposRelacaoJava();

        public static RefTipoImpl getRefTipoRelacao(Relacao r) {

            int sO = r.getOrigem().size();
            int sA = r.getAlvo().size();

            String k = (sO > 1 ? "n" : sO + "") + ":"
                    + (sA > 1 ? "n" : sA + "");

            if (k.equals("0:0")) {
                return null;
            }

            String primeiroTipo = null;
            for (String s : mapsTiposRelacao.get(k).keySet()) {
                primeiroTipo = s;
                break;
            }

            return new RefTipoImpl(primeiroTipo);
        }
    }

    @XmlElement
    @Override
    public ProvenienciaImpl getProveniencia() {
        return proveniencia;
    }

    public void setProveniencia(ProvenienciaImpl proveniencia) {
        this.proveniencia = proveniencia;
    }
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}
