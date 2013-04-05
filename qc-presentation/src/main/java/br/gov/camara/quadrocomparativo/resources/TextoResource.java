/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.resources;

// POJO, no interface no extends
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import scala.runtime.BoxedUnit;
import scalaz.Validation;
import br.gov.camara.quadrocomparativo.lexml.DocumentoLexmlUtil;
import br.gov.camara.quadrocomparativo.lexml.LexmlFile;
import br.gov.camara.quadrocomparativo.model.DocumentoImpl;
import br.gov.camara.quadrocomparativo.model.QuadroComparativo;
import br.gov.camara.quadrocomparativo.model.Texto;
import br.gov.lexml.parser.pl.ArticulacaoParser;
import br.gov.lexml.renderer.plaintext.RendererPlainText;
import br.gov.lexml.symbolicobject.parser.InputDocument;
import br.gov.lexml.symbolicobject.parser.LexMLDocument;
import br.gov.lexml.symbolicobject.parser.Parser;
import br.gov.lexml.symbolicobject.parser.StringSource;
import br.gov.lexml.symbolicobject.tipos.Tipos;

import com.sun.jersey.api.NotFoundException;

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 
// The browser requests per default the HTML MIME type.
@Path("/texto")
public class TextoResource {

    @Context
    HttpServletRequest request;
    private static Logger log = Logger.getLogger(TextoResource.class.getName());

    @GET
    @Path("/{urn}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Texto importarTexto(@PathParam("urn") String urn) {

        Texto t = new Texto(urn, null);

        // tenta obter o zip
        LexmlFile zip = LexmlFile.newLexmlFileFromURN(urn);
        if (zip != null) {
            
            t.setArticulacaoXML(zip.getTextoAsString());
            t.setArticulacao(getArticulacaoPlainText(t.getArticulacaoXML()));

        } else {
            //tenta obter o texto da camara
            String textoCamara = DocumentoLexmlUtil.downloadTextoCamaraFromUrn(urn);
            if (textoCamara == null) {
                throw new NotFoundException("Texto " + urn + " não encontrado.");
            }
            t.setArticulacao(textoCamara);
        }

        return t;
    }
    
    @GET
    @Path("/{urn}/qc/{qcid}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Texto getTextoInQuadro(@PathParam("qcid") String qcId,
            @PathParam("urn") String urn) {

        if (urn == null) {
            throw new NotFoundException();

        } else if (qcId == null) {
            return importarTexto(urn);
        }

        urn = urn.replaceAll("__", ";");

        QuadroComparativo qc = QuadroComparativoController
                .getQuadroComparativo(request, qcId);

        Texto texto = qc.getTexto(urn);

        if (texto == null || texto.getArticulacao() == null
                || texto.getArticulacao().equals("")) {
            Texto textoComArticulacao = importarTexto(urn);

            if (textoComArticulacao != null) {
                texto.setArticulacao(textoComArticulacao.getArticulacao());
            }
        }

        return texto;
    }

    @GET
    @Path("/{urn}/qc/{qcid}/estrutura/")
    @Produces(MediaType.APPLICATION_JSON)
    public Texto getEstruturaTextoInQuadro(@PathParam("qcid") String qcId,
            @PathParam("urn") String urn) {

        if (urn == null) {
            throw new NotFoundException();

        } else if (qcId == null) {
            // return getTexto(urn);
        }

        urn = urn.replaceAll("__", ";");

        QuadroComparativo qc = QuadroComparativoController
                .getQuadroComparativo(request, qcId);

        Texto texto = qc.getTexto(urn);

        if (texto == null) {
            texto = new Texto(urn, null);
        }

        if (texto.getDocumento() != null) {
            return texto;
        }

        if (texto.getArticulacao() == null || texto.getArticulacao().equals("")) {

            Texto textoComArticulacao = importarTexto(urn);
            texto.setArticulacao(textoComArticulacao.getArticulacao());
            texto.setArticulacaoXML(getArticulacaoXML(texto));
        }

        if (texto.getArticulacaoXML() == null
                || texto.getArticulacaoXML().equals("")) {

            texto.setArticulacaoXML(getArticulacaoXML(texto));
        }

        Parser tParser = new Parser(qc);
        Validation<String, br.gov.lexml.symbolicobject.impl.Documento<BoxedUnit>> validation = tParser
                .parse(new InputDocument(Tipos.DocProjetoLei(),
                new LexMLDocument(new StringSource(texto
                .getArticulacaoXML())), urn));

        if (validation.isSuccess()) {
            DocumentoImpl doc = new DocumentoImpl(validation.toOption().get());
            texto.setDocumento(doc);
            texto.setArticulacao(null);
            texto.setArticulacaoXML(null);

            return texto;
        } else {
            log.log(Level.SEVERE, "Validação não executada com sucesso.");
        }

        return null;
    }

    private DocumentoImpl getEstruturaTexto(QuadroComparativo qc, Texto texto) {

        if (texto.getDocumento() != null) {
            return texto.getDocumento();
        }

        if (texto.getArticulacao() == null || texto.getArticulacao().equals("")) {

            Texto textoComArticulacao = importarTexto(texto.getUrn());
            texto.setArticulacao(textoComArticulacao.getArticulacao());
            texto.setArticulacaoXML(getArticulacaoXML(texto));
        }

        if (texto.getArticulacaoXML() == null
                || texto.getArticulacaoXML().equals("")) {

            texto.setArticulacaoXML(getArticulacaoXML(texto));
        }

        Parser tParser = new Parser(qc);
        Validation<String, br.gov.lexml.symbolicobject.impl.Documento<BoxedUnit>> validation = tParser
                .parse(new InputDocument(Tipos.DocProjetoLei(),
                new LexMLDocument(new StringSource(texto
                .getArticulacaoXML())), texto.getUrn()));

        if (validation.isSuccess()) {
            DocumentoImpl doc = new DocumentoImpl(validation.toOption().get());
            return doc;
        } else {
            log.log(Level.SEVERE, "Validação não executada com sucesso.");
        }
        return null;
    }

    @POST
    @Path("/qc/{qcid}/col/{colid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveTexto(Texto texto, @PathParam("qcid") String qcId,
            @PathParam("colid") String colId) {

        QuadroComparativo qc = QuadroComparativoController
                .getQuadroComparativo(request, qcId);

        // texto.setArticulacaoXML(getArticulacaoXML(texto));
        
        // verifica se o usuario nao inseriu um arquivo lexml no lugar da
        // articulacao
        String articulacao = cleanString(texto.getArticulacao());
        
        if (isXML(articulacao)) {
            texto.setArticulacaoXML(articulacao);
            texto.setArticulacao(getArticulacaoPlainText(articulacao));
        }
        
        texto.setDocumento(getEstruturaTexto(qc, texto));
        texto.setArticulacaoXML(null);

        qc.addTexto(colId, texto);
        QuadroComparativoController.saveQuadroComparativo(request, qc);

        String result = "Texto saved: " + texto;
        return Response.status(201).entity(result).build();
    }
    
    private String cleanString(String str) {
        
        str = str.replace(String.valueOf((char) 160), " ").trim();
        str = str.replace("^[^\\w^<]*", "");
        return str;
    }
    
    private boolean isXML(String str) {
        
        if (str.startsWith("<")) {
            return true;
        }
        
        return false;
    }

    private String getArticulacaoXML(Texto texto) {

        if (texto != null && texto.getArticulacaoXML() != null) {

            return texto.getArticulacaoXML();

        } else if (texto != null && texto.getArticulacao() != null) {

            ArticulacaoParser p = new ArticulacaoParser();
            String[] paragrafosArray = texto.getArticulacao().split("\n");
            List<String> paragrafosRaw = Arrays.asList(paragrafosArray);

            List<String> paragrafos = new ArrayList<String>();

            for (String par : paragrafosRaw) {

                // tratando non-breaking spaces
                // http://stackoverflow.com/questions/4728625/why-trim-is-not-working
                par = par.replace(String.valueOf((char) 160), " ").trim();
                if (par != null && !"".equals(par)) {
                    paragrafos.add(par);
                }
            }

            return p.parseJList(paragrafos);
        }

        return null;
    }
    
    private String getArticulacaoPlainText(String articulacaoXML) {
        
        //cria a articulação em plain text
        RendererPlainText rpt = new RendererPlainText();
        try {
            return rpt.render(articulacaoXML);
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
