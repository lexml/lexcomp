package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Comentario;

public class ComentarioImpl extends IdentificavelImpl implements Comentario, Serializable {

    private static final long serialVersionUID = -135221691017401227L;

    private String xhtmlFragment;
    private Long alvo;
    private TipoComentario tipo;

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

    public TipoComentario getTipo() {
        return tipo;
    }

    public void setTipo(TipoComentario tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public enum TipoComentario {
        NOTA("Nota"), OBSERVACAO("Observação");
        
        private final String text;

        private TipoComentario(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
