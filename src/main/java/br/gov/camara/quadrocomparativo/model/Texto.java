/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.model;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import br.gov.camara.quadrocomparativo.configuracaourn.Autoridade;
import br.gov.camara.quadrocomparativo.configuracaourn.Evento;
import br.gov.camara.quadrocomparativo.configuracaourn.Localidade;
import br.gov.camara.quadrocomparativo.configuracaourn.TipoDocumento;
import br.gov.camara.quadrocomparativo.configuracaourn.Versao;
import br.gov.camara.quadrocomparativo.lexml.ConfiguracaoUrn;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;



/**
 *
 * @author p_7174
 */

public class Texto implements Serializable {

    private static final long serialVersionUID = 4748727520878762630L;

    private String urn;
    private String categorias;
    private String localidades;
    private String autoridades;
    private String autoridadesFilhas;
    private String comissaoespecial;
    private String tiposDocumento;
    private String numero;
    private String dataAssinatura;
    private String componenteDocumento;
    private String numeroComponente;
    private String anoComponente;
    private String siglaColegiado;
    private String versao;
    private String dataVigente;
    private String evento;
    private String dataEvento;
    private String preambulo;
    private String fecho;
    private String titulo;
    private String urnIdDIV;
    private String order;
    private boolean incluidoVisualizacao;
    @XmlTransient
    private boolean documentoParseado;

    /**
     * Texto não parseado
     */
    private String articulacao;

    /**
     * Texto em LexML
     */
    private String articulacaoXML;

    /**
     * Documento Objeto Simbólico (Texto LeXML parseado em Objeto Simbólico)
     */
    private DocumentoImpl documento;
    
    //Utilizados na geração do título
    
    StringBuilder novoTitulo =  new StringBuilder();

    public Texto() {
    }

    public Texto(String urn, String titulo) {
        this.urn = urn;
        this.titulo = titulo;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrnIdDIV() {
        return urnIdDIV;
    }

    public void setUrnIdDIV(String urnIdDIV) {
        this.urnIdDIV = urnIdDIV;

    }

    public String getDataAssinatura() {
        return dataAssinatura;
    }

    public void setDataAssinatura(String dataAssinatura) {
        this.dataAssinatura = dataAssinatura;
        calculaURN();
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
        calculaURN();
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getPreambulo() {
        return preambulo;
    }

    public void setPreambulo(String preambulo) {
        this.preambulo = preambulo;
    }

    public String getArticulacao() {
        return articulacao;
    }

    public void setArticulacao(String articulacao) {
        this.articulacao = articulacao;
    }
    
    public String getArticulacaoXML() {
        return articulacaoXML;
    }

    public void setArticulacaoXML(String articulacaoXML) {
        this.articulacaoXML = articulacaoXML;
    }

    public String getFecho() {
        return fecho;
    }

    public void setFecho(String fecho) {
        this.fecho = fecho;
    }

    
    

    private void calculaURN() {

        setUrn("urn:lex:");
        
        
        if(getLocalidades()!= null){
            setUrn(getUrn() + getLocalidades() + ":");            
        }
        
        if(getAutoridades()!= null){
            setUrn(getUrn() + getAutoridades() + ":");
            
        }
        
        if(getTiposDocumento()!=null){            
            if(getTiposDocumento().lastIndexOf(":")>0){
                String tempTipoDocumento = getTiposDocumento().substring(getTiposDocumento().lastIndexOf(":")+1);  
                setUrn(getUrn() + tempTipoDocumento + ":");
            }else{
                setUrn(getUrn() + getTiposDocumento() + ":");                
            }           
        }
        
        if(getDataAssinatura()!=null){
            setUrn(getUrn() + getDataAssinatura() + ";");           
        }
        
        if(getNumero()!=null){       
            setUrn(getUrn() + getNumero());           
        }
        
           
        

        //LEGISLAÇÃO
        if (getCategorias() != null && getCategorias().equals("legislacao")) {        
                     
            
            if (getVersao() != null && !getVersao().equals("")) {

                setUrn(getUrn() + "@");

                if (getVersao().equals("versao.original")) {
                    setUrn(getUrn() + getVersao());
                } else {
                    if (getDataVigente() != null && !getDataVigente().equals("")) {
                        setUrn(getUrn() + getVersao() + ";" + getDataVigente());
                        
                    }
                }
            }
            
            
       
        
        } else if (getCategorias() != null && getCategorias().equals(
                "proposicoes.legislativas.substitutivos")
                && getAutoridades() != null && !getAutoridades().equals("")
                && getAutoridades().contains("senado.federal")
                        && !getAutoridades().contains("congresso.nacional")) {
                    
            //Senado 
            //urn:lex:br:senado.federal:projeto.lei;pls:2012;14@2012-05-03;leitura;2012-05-03
            //urn:lex:LOCALIDADE:AUTORIDADE:TIPODOCUMENTO:DESCRITOR_ANO_OU_DATA;DESCRITOR_NUMERO@DATA-EVENTO;EVENTO;DATA-EVENTO

            if (getDataEvento() != null && !"".equals(getDataEvento())) {
                setUrn(getUrn() + "@" + getDataEvento());
            }
            
            if (getDataEvento() != null && !"".equals(getDataEvento())) {
                setUrn(getUrn() + ";" + getEvento());
            }
            
            if (getDataEvento() != null && !"".equals(getDataEvento())) {
                setUrn(getUrn() + ";" + getDataEvento());
            }
            
        } else {
            //PROPOSIÇÕES LEGISLATIVAS OU SUBSTITUTIVOS da CAMARA
            //EMENDAS e DESTAQUES
            
            //Câmara
            //urn:lex:br:camara.deputados:projeto.lei;pl:2007;2125;ems.2007.2125
            //urn:lex:LOCALIDADE:AUTORIDADE:TIPODOCUMENTO:DESCRITOR_ANO_OU_DATA;DESCRITOR_NUMERO;COMPONENTE_DOCUMENTO_SIGLA.COMPONENTE_ANO.COMPONENTE_NUMERO.COMPONENTE_COLEGIADO_SIGLA
            //EXEMPLO RECURSIVO - neste caso, repete-se o componente separando por ";" - urn:lex:br:camara.deputados;comissao.especial;pl803510:projeto.lei;pl:2010;8035;sbt.1.pl803510;esb.439.pl803510

            if (getComponenteDocumento() != null
                    && !"".equals(getComponenteDocumento())) {
                
                String[] splitComponenteDoc = getComponenteDocumento().split(";");
                
                if (splitComponenteDoc != null
                        && splitComponenteDoc.length == 1) {
                
                    setUrn(getUrn() + ";" + splitComponenteDoc[0]);
                
                } else if (splitComponenteDoc != null
                        && splitComponenteDoc.length == 2) {
                    
                    setUrn(getUrn() + ";" + splitComponenteDoc[1]);
                }
            }

            if (getAnoComponente() != null && !"".equals(getAnoComponente())) {
                setUrn(getUrn() + "." + getAnoComponente());
            }

            if (getNumeroComponente() != null && !"".equals(getNumeroComponente())) {
                setUrn(getUrn() + "." + getNumeroComponente());
            }

            if (getSiglaColegiado() != null && !"".equals(getSiglaColegiado())) {
                setUrn(getUrn() + "." + getSiglaColegiado());
            }
        }
        
        //Resolução + no NNNN  / Ano ou DD/MM/AA + Autoridade + Localidade <> Brasil 

        urnToDIV();
    }

    //Converte a URN para um ID da DIV
    private void urnToDIV() {
        
        setUrnIdDIV(getUrn().replaceAll("[^a-zA-Z0-9 ]", "_"));
        setUrnIdDIV(getUrnIdDIV().replaceAll("\\.", "_"));
        //System.out.println("URN: " + getUrn());
        //System.out.println("URNTODIV: " + getUrnIdDIV());
    }

    /**
     * @return the AutoridadesFilhas
     */
    public String getAutoridadesFilhas() {
        return autoridadesFilhas;
    }

    /**
     * @param AutoridadesFilhas the AutoridadesFilhas to set
     */
    public void setAutoridadesFilhas(String AutoridadesFilhas) {
        this.autoridadesFilhas = AutoridadesFilhas;
        calculaURN();
    }

    /**
     * @return the comissaoespecial
     */
    public String getComissaoespecial() {
        return comissaoespecial;
    }

    /**
     * @return the TiposDocumento
     */
    public String getTiposDocumento() {
        return tiposDocumento;
    }

    /**
     * @return the ComponenteDocumento
     */
    public String getComponenteDocumento() {
        return componenteDocumento;
    }

    /**
     * @return the numeroComponente
     */
    public String getNumeroComponente() {
        return numeroComponente;
    }

    /**
     * @return the anoComponente
     */
    public String getAnoComponente() {
        return anoComponente;
    }

    /**
     * @return the SiglaColegiado
     */
    public String getSiglaColegiado() {
        return siglaColegiado;
    }

    /**
     * @return the versao
     */
    public String getVersao() {
        return versao;
    }

    /**
     * @return the dataVigente
     */
    public String getDataVigente() {
        return dataVigente;
    }

    /**
     * @return the Evento
     */
    public String getEvento() {
        return evento;
    }

    /**
     * @return the dataEvento
     */
    public String getDataEvento() {
        return dataEvento;
    }

    /**
     * @return the Categorias
     */
    public String getCategorias() {
        return categorias;
    }

    /**
     * @param Categorias the Categorias to set
     */
    public void setCategorias(String Categorias) {
        this.categorias = Categorias;
    }

    /**
     * @return the Localidades
     */
    public String getLocalidades() {
        return localidades;
    }

    /**
     * @param Localidades the Localidades to set
     */
    public void setLocalidades(String Localidades) {
        this.localidades = Localidades;
        calculaURN();
    }

    /**
     * @return the Autoridades
     */
    public String getAutoridades() {
        return autoridades;
    }

    /**
     * @param Autoridades the Autoridades to set
     */
    public void setAutoridades(String Autoridades) {
        this.autoridades = Autoridades;
        calculaURN();
    }

    /**
     * @param comissaoespecial the comissaoespecial to set
     */
    public void setComissaoespecial(String comissaoespecial) {
        this.comissaoespecial = comissaoespecial;
        calculaURN();
    }

    /**
     * @param TiposDocumento the TiposDocumento to set
     */
    public void setTiposDocumento(String TiposDocumento) {
        this.tiposDocumento = TiposDocumento;
        calculaURN();
    }

    /**
     * @param ComponenteDocumento the ComponenteDocumento to set
     */
    public void setComponenteDocumento(String ComponenteDocumento) {
        this.componenteDocumento = ComponenteDocumento;
        calculaURN();
    }

    /**
     * @param numeroComponente the numeroComponente to set
     */
    public void setNumeroComponente(String numeroComponente) {
        this.numeroComponente = numeroComponente;
        calculaURN();
    }

    /**
     * @param anoComponente the anoComponente to set
     */
    public void setAnoComponente(String anoComponente) {
        this.anoComponente = anoComponente;
        calculaURN();
    }

    /**
     * @param siglaColegiado the SiglaColegiado to set
     */
    public void setSiglaColegiado(String siglaColegiado) {
        this.siglaColegiado = siglaColegiado;
        calculaURN();
    }

    /**
     * @param versao the versao to set
     */
    public void setVersao(String versao) {
        this.versao = versao;
        calculaURN();
    }

    /**
     * @param dataVigente the dataVigente to set
     */
    public void setDataVigente(String dataVigente) {
        this.dataVigente = dataVigente;
        calculaURN();
    }

    /**
     * @param Evento the Evento to set
     */
    public void setEvento(String Evento) {
        this.evento = Evento;
        calculaURN();
    }

    /**
     * @param dataEvento the dataEvento to set
     */
    public void setDataEvento(String dataEvento) {
        this.dataEvento = dataEvento;
    }

    @XmlElement
    public DocumentoImpl getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoImpl documento) {
        this.documento = documento;
    }

    public boolean getIncluidoVisualizacao() {
        return incluidoVisualizacao;
    }

    public void setIncluidoVisualizacao(boolean incluidoVisualizacao) {
        this.incluidoVisualizacao = incluidoVisualizacao;
    }
    
    public void geraTitulo() {
             
        
       TipoDocumento tipoDocumento = ConfiguracaoUrn.getInstance().getTipoDocumento(getTiposDocumento());
       
      
       if(tipoDocumento!=null){         
           novoTitulo.append(tipoDocumento.getNome());
       }  
       
      if(getNumero()!=null){          
          novoTitulo.append(" nº " + getNumero());
      }
      
      
      if(getDataAssinatura()!=null){
          try {               
             String dataAssinatura  = getDataAssinatura();
             dataAssinatura = dataAssinatura.replaceAll("[^a-zA-Z0-9]", "/");
             novoTitulo.append("/");             
             novoTitulo.append(dataAssinatura.substring(0, 4));
             novoTitulo.append("<br><p class=subtitulo>");
             
             //System.out.println("#######"+dataAssinatura.toString());
             //Date data = new SimpleDateFormat("dd/MM/yyyy).parse(dataAssinatura);              
             //novoTitulo.append(" " + data.toString());
          } catch (Exception e) {              
              e.printStackTrace();
          }
      }
      
       
       
       
       
      
       
      Versao versao = ConfiguracaoUrn.getInstance().getVersao(getVersao());
      if(versao!=null){
          novoTitulo.append(" ");
          novoTitulo.append(versao.getNome());
          
          novoTitulo.append(" ").append(getDataVigente());
      }
      
      Evento evento = ConfiguracaoUrn.getInstance().getEvento(getEvento());
      if(evento!=null){
          novoTitulo.append(" ");
          novoTitulo.append(evento.getNome());
          novoTitulo.append(" " + getDataEvento());
      }       
      
      Localidade localidade = ConfiguracaoUrn.getInstance().getLocalidade(getLocalidades());
      Autoridade autoridade = ConfiguracaoUrn.getInstance().getAutoridade(getAutoridades());
      
      if(localidade!=null || autoridade != null){
          
      novoTitulo.append(" [");
      
      
       
       if(localidade!=null){           
           novoTitulo.append(localidade.getNome());
       }
       
            if(autoridade != null){
                novoTitulo.append(" | ");
            }
      
       if(autoridade!=null){
           
           novoTitulo.append(autoridade.getNome());
           
       }
       
       novoTitulo.append("]");
       
      }
      
      
      
      novoTitulo.append("</p>");
      
       setTitulo(novoTitulo.toString());     
        
    }

    public boolean isDocumentoParseado() {
        return documentoParseado;
    }

    public void setDocumentoParseado(boolean documentoParseado) {
        this.documentoParseado = documentoParseado;
    }
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this);
    }
}  
    
    
 