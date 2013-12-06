/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.ObjetoSimbolicoComplexo;
import br.gov.lexml.symbolicobject.Posicao;

/**
 *
 * @author p_7174
 */
public class ObjetoSimbolicoComplexoImpl extends ObjetoSimbolicoImpl
        implements ObjetoSimbolicoComplexo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @XmlElement
    protected List<PosicaoImpl> posicoes;

    public ObjetoSimbolicoComplexoImpl() {
    }

    public ObjetoSimbolicoComplexoImpl(ObjetoSimbolicoComplexo obj) {
        super(obj);

        if (obj != null && obj.getPosicoes() != null) {

            List<PosicaoImpl> posicaoImplList = new ArrayList<PosicaoImpl>();

            for (Posicao pos : obj.getPosicoes()) {

                if (pos != null) {
                    posicaoImplList.add(new PosicaoImpl(pos));
                }
            }

            if (!posicaoImplList.isEmpty()) {
                posicoes = posicaoImplList;
            }
        }
    }

    public List<Posicao> getPosicoes() {
        return (List) posicoes;
    }

    public void setPosicoes(List<PosicaoImpl> posicoes) {
        this.posicoes = posicoes;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Set<Long> produceObjetoSimbolicoIdSetFromPosicoes() {

        Set<Long> r = new HashSet<Long>();

        r.add(id);
        
        if (this.posicoes != null) {
            for (PosicaoImpl pi : this.posicoes) {
                r.addAll(pi.getObjetoSimbolicoImpl().produceObjetoSimbolicoIdSetFromPosicoes());
            }
        }

        return r;
    }

    @Override
    protected String getRealJavaType() {
        return "objetoSimbolicoComplexo";
    }

}
