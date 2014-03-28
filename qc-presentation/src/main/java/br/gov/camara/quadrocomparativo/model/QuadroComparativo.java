/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;

import br.gov.lexml.symbolicobject.Documento;
import br.gov.lexml.symbolicobject.Relacao;
import br.gov.lexml.symbolicobject.parser.IdSource;
import br.gov.lexml.symbolicobject.tipos.STipo;

/**
 *
 * @author p_7174
 */
@XmlRootElement
public class QuadroComparativo implements Serializable, IdSource {

	public static final String QUADROS_HOMEDIR = System.getProperty("user.home")+"/LexComp/";
	public static final String FILENAME_PREFIX = "";
	
    private static final long serialVersionUID = -6227052315334283202L;
    private String id;
    private String titulo;
    private List<Coluna> colunas;
    private List<Conexao> conexoes;
    private String dataModificacao;
    private List<Correlacao> correlacoes;
    private long idSourceCurrentPosition = 0;
    private boolean articulacoesExcluidas = false;

    private void setArticulacoesExcluidas(boolean articulacoesExcluidas) {
        this.articulacoesExcluidas = articulacoesExcluidas;
    }

    public boolean isArticulacoesExcluidas() {
        return articulacoesExcluidas;
    }

    public QuadroComparativo() {
        this.id = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    public QuadroComparativo(String id) {
        this.id = id;
    }

    public QuadroComparativo(String id, String titulo) {
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

    public String getDataModificacao() {
        return dataModificacao;
    }

    public void setDataModificacao(String dataModificacao) {
        this.dataModificacao = dataModificacao;
    }

    @XmlTransient
    public void setDataModificacao2(Date date) {
        this.dataModificacao = new SimpleDateFormat("yyyyMMddHHmmssSSS")
                .format(date);
    }

    @XmlElement
    public List<Coluna> getColunas() {
        return colunas;
    }

    public void addColuna(Coluna coluna) {

        if (colunas == null) {
            colunas = new ArrayList<Coluna>();
        }

        if (!colunas.contains(coluna)) {

            if (coluna.getId() == null) {
                coluna.setId(getId() + "-" + (colunas.size() + 1));
            }

            if (coluna.getTitulo() == null) {
                coluna.setTitulo("Nova coluna");
            }
            colunas.add(coluna);
        }
    }

    @XmlElement
    public List<Conexao> getConexoes() {
        return conexoes;
    }

    public void addConexao(Conexao conexao) {

        if (conexoes == null) {
            conexoes = new ArrayList<Conexao>();
        }

        if (!conexoes.contains(conexao)) {

            conexoes.add(conexao);
        }
    }

    @XmlElement
    public List<Correlacao> getCorrelacoes() {
        return correlacoes;
    }

    public void addCorrelacao(Correlacao correlacao) {

        if (correlacoes == null) {
            correlacoes = new ArrayList<Correlacao>();
        }

        if (!correlacoes.contains(correlacao)) {

            correlacoes.add(correlacao);
        }
    }

    @XmlTransient
    public String getFileName() {
        return getFileName(getId());
    }

    public static String getFileName(String id) {
        return QUADROS_HOMEDIR + FILENAME_PREFIX+"qc-" + id + ".xml";
    }

    public Texto getTexto(String urn) {
        
        urn = urn.replaceAll("__", ";");

        for (Texto txt : getAllTextos()) {

            if (txt.getUrn() != null && txt.getUrn().equals(urn)) {
                return txt;
            }
        }

        return null;
    }

    public Texto getTexto(String colId, String urn) {

        Coluna col = getColuna(colId);

        if (col != null && col.getTextos() != null) {
            for (Texto txt : col.getTextos()) {

                if (txt.getUrn() != null && txt.getUrn().equals(urn)) {
                    return txt;
                }
            }
        }
        return null;
    }

    public Coluna getColuna(String colId) {

        for (Coluna col : getColunas()) {

            if (col.getId() != null && col.getId().equals(colId)) {
                return col;
            }
        }

        return null;
    }

    public void addTexto(String colId, Texto texto) {

        Coluna col = getColuna(colId);
        if (col != null) {
            if (getTexto(colId, texto.getUrn()) != null) {

                Texto txt = getTexto(colId, texto.getUrn());
                texto.setOrder(txt.getOrder());
                int index = col.getTextos().indexOf(txt);
                col.getTextos().set(index, texto);

            } else {
                col.addTexto(texto);
            }
        }
    }

    public Correlacao getCorrelacao(String urn1, String urn2) {
        
        urn1 = urn1.replaceAll("__", ";");
        urn2 = urn2.replaceAll("__", ";");

        if (getCorrelacoes() != null) {

            for (Correlacao cor : getCorrelacoes()) {

                if (cor.getUrn1() != null && cor.getUrn2() != null && ((cor.getUrn1().equals(urn1) && cor.getUrn2().equals(urn2))
                        || cor.getUrn1().equals(urn2) && cor.getUrn2().equals(urn1))) {

                    return cor;
                }
            }
        }

        return null;
    }
    
    public Correlacao getCorrelacao(Conexao conexao) {
        
        if (getCorrelacoes() != null) {

            for (Correlacao cor : getCorrelacoes()) {

                if (cor.getUrn1() != null && cor.getUrn2() != null) {
                    String urn1 = cor.getUrn1().replaceAll("\\.|;|\\:|@", "_");
                    String urn2 = cor.getUrn2().replaceAll("\\.|;|\\:|@", "_");
                    
                    if (conexao.getSourceId().equals(urn1) && conexao.getTargetId().equals(urn2)
                        || conexao.getSourceId().equals(urn2) && conexao.getTargetId().equals(urn1)) {

                        return cor;
                    }
                }
            }
        }

        return null;
    }

    public List<Correlacao> getCorrelacoes(String urn) {
        
        urn = urn.replaceAll("__", ";");

        if (getCorrelacoes() != null) {

            List<Correlacao> result = new ArrayList<Correlacao>();

            for (Correlacao cor : getCorrelacoes()) {

                if (cor.getUrn1() != null && cor.getUrn2() != null
                        && (cor.getUrn1().equals(urn) || cor.getUrn2().equals(urn))) {

                    result.add(cor);
                }
            }

            return result;
        }

        return null;
    }
    
    /**
     * Mantem integridade entre correlacoes e conexoes
     */
    public void limpaCorrelacoesSemConexao() {
        
        if (getConexoes() == null || getConexoes().isEmpty()) {
            setCorrelacoes(new ArrayList<Correlacao>());
        
        } else {
            
            List<Correlacao> correls = new ArrayList<Correlacao>();
            for (Conexao conexao : getConexoes()) {
                
                Correlacao correlacao = getCorrelacao(conexao);
                
                if (correlacao != null) {
                    correls.add(correlacao);
                }
            }
            
            setCorrelacoes(correls);
        }
    }

    @XmlTransient
    public List<Documento> getAllDocumentos() {
        List<Documento> r = new ArrayList<Documento>();

        for (Texto txt : getAllTextos()) {
            r.add(txt.getDocumento());
        }

        return r;
    }

    @XmlTransient
    public List<Texto> getAllTextos() {
        List<Texto> r = new ArrayList<Texto>();

        if (getColunas() != null) {
            for (Coluna col : getColunas()) {

                r.addAll(col.getTextos());

            }
        }

        return r;
    }

    public void setColunas(List<Coluna> colunas) {
        this.colunas = colunas;
    }

    public void setConexoes(List<Conexao> conexoes) {
        this.conexoes = conexoes;
    }

    public void setCorrelacoes(List<Correlacao> correlacoes) {
        this.correlacoes = correlacoes;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    synchronized public long nextId(STipo tipo) {
        return ++idSourceCurrentPosition;
    }

    synchronized public long getCurrentPosition() {
        return idSourceCurrentPosition;
    }

    synchronized public void setCurrentPosition(long currentPosition) {
        this.idSourceCurrentPosition = currentPosition;
    }
    
    public Relacao getRelacao(String urn1, String urn2, Long relacaoId) {
        Correlacao correl = getCorrelacao(urn1, urn2);
        
        if (correl != null) {
            return correl.getRelacao(relacaoId);
        }
        
        return null;
    }
    
    /*
     * ARTICULACOES
     */
    
    public QuadroComparativo removeArticulacoes() {

        if (getColunas() != null) {
            for (Coluna col : getColunas()) {

                if (col.getTextos() != null) {
                    for (Texto tex : col.getTextos()) {

                        tex.setDocumentoParseado(tex.getDocumento() != null);
                        tex.setArticulacao(null);
                        tex.setArticulacaoXML(null);
                        tex.setDocumento(null);
                    }
                }
            }
        }

        setArticulacoesExcluidas(true);

        return this;
    }

    public boolean restauraArticulacoes(QuadroComparativo quadroComArticulacoes) {

    	// recupera articulacoes salvas anteriormente
        if (quadroComArticulacoes != null) {

            if (getColunas() != null) {
            	for (Coluna col : getColunas()) {
                    if (col.getTextos() != null) {
                        for (Texto tex : col.getTextos()) {

                            if (tex.getArticulacao() == null || tex.getDocumento() == null) {

                                Texto texAtual = quadroComArticulacoes.getTexto(tex.getUrn());

                                if (texAtual != null) {
                                    tex.setArticulacao(texAtual.getArticulacao());
                                    tex.setDocumento(texAtual.getDocumento());
                                }
                            }
                        }
                    }
                }
            }
            setArticulacoesExcluidas(false);
            
        }

        return !isArticulacoesExcluidas();
    }

}
