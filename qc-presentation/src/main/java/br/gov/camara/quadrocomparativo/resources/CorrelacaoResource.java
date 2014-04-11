/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.resources;

import br.gov.camara.quadrocomparativo.model.ComentarioImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.gov.camara.quadrocomparativo.model.ConfiguracaoImpl;
import br.gov.camara.quadrocomparativo.model.Correlacao;
import br.gov.camara.quadrocomparativo.model.ProvenienciaUsuarioImpl;
import br.gov.camara.quadrocomparativo.model.QuadroComparativo;
import br.gov.camara.quadrocomparativo.model.RelacaoImpl;
import br.gov.camara.quadrocomparativo.model.Texto;
import br.gov.lexml.symbolicobject.Comentario;
import br.gov.lexml.symbolicobject.Documento;
import br.gov.lexml.symbolicobject.Relacao;
import br.gov.lexml.symbolicobject.comp.CPT_TextToText;
import br.gov.lexml.symbolicobject.comp.Comparator;
import br.gov.lexml.symbolicobject.comp.CompareProcessConfiguration;
import br.gov.lexml.symbolicobject.comp.CompareProcessSpec;

import com.sun.jersey.api.NotFoundException;
import java.net.URI;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Path("/correlacao")
public class CorrelacaoResource extends AbstractResource {

    @Context
    HttpServletRequest request; 	//System.out.println("request: "+request.getSession(true).getId());

    @Context 
    UriInfo uriInfo;
            
    @GET
    @Path("/{qcid}/{urn1}/{urn2}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Correlacao getCorrelacao(@PathParam("qcid") String qcId,
            @PathParam("urn1") String urn1, @PathParam("urn2") String urn2) {

        QuadroComparativo qc = getExistingQuadro(qcId, request);

        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);

        if (correlacao == null) {
            correlacao = new Correlacao(urn1, urn2);
            // correlacoes = getCorrelacoesAutomaticas(texto1, texto2);
            // correlacao.setCorrelacaoList(correlacoes);
        }

        Texto texto1 = qc.getTexto(correlacao.getUrn1());
        Texto texto2 = qc.getTexto(correlacao.getUrn2());

        if (texto1 != null && texto1.getDocumento() == null) {
            TextoResource textoRes = new TextoResource();
            texto1.setDocumento(textoRes.getEstruturaTextoInQuadro(qcId, urn1)
                    .getDocumento());
        }

        if (texto2 != null && texto2.getDocumento() == null) {
            TextoResource textoRes = new TextoResource();
            texto2.setDocumento(textoRes.getEstruturaTextoInQuadro(qcId, urn2)
                    .getDocumento());
        }

        correlacao.setTexto1(texto1);
        correlacao.setTexto2(texto2);

        return correlacao;
    }

    @GET
    @Path("/comentario/{qcid}/{urn1}/{urn2}/{alvo}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Comentario> getComentarios(@PathParam("qcid") String qcId,
            @PathParam("urn1") String urn1, @PathParam("urn2") String urn2,
            @PathParam("alvo") Long alvo) {

        Correlacao correlacao = getExistingCorrelacao(qcId, urn1, urn2, request);
        
        if (correlacao == null) {
            return new ArrayList<Comentario>();
        }
        
        return correlacao.getComentarios(alvo);
    }
    
    @POST
    @Path("/comentario/{qcid}/{urn1}/{urn2}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveComentario(ComentarioImpl comentario,
            @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
            @PathParam("urn2") String urn2) {
        
        QuadroComparativo qc = getExistingQuadro(qcid, request);
        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);
        
        if (correlacao == null) {
            correlacao = new Correlacao(urn1, urn2);
            qc.addCorrelacao(correlacao);
        }
        
        correlacao.addComentario(comentario, qc);
        
        RelacaoImpl relacao = (RelacaoImpl) correlacao.getRelacao(comentario.getAlvo());
        if (relacao != null) {
            relacao.setProveniencia(new ProvenienciaUsuarioImpl());
        }
        
        if (!QuadroComparativoController.saveQuadroComparativo(request, qc)) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        URI uri = uriInfo.getAbsolutePathBuilder().path(comentario.getId() + "").build();
        return Response.created(uri).build();
    }
    
    /*@PUT
    @Path("/{qcid}/{urn1}/{urn2}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateComentario(ComentarioImpl comentario,
            @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
            @PathParam("urn2") String urn2) {
        
        QuadroComparativo qc = getExistingQuadro(qcid);
        RelacaoImpl relacao = (RelacaoImpl) qc.getRelacao(urn1, urn2, comentario.getAlvo());
        relacao.setProveniencia(new ProvenienciaUsuarioImpl());
        
        if (!relacao.getComentarios().contains(comentario)) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        relacao.addComentario(comentario);
        
        if (!QuadroComparativoController.saveQuadroComparativo(request, qc)) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        String result = "Relacao saved: " + relacao;
        return Response.status(201).entity(result).build();
    }*/

    @POST
    @Path("/updateid/{qcid}/{urn1}/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateIdCorrelacao(String urnNova,
            @PathParam("qcid") String qcid, @PathParam("urn1") String urnAntiga) {

        QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcid);

        List<Correlacao> listCorrel = qc.getCorrelacoes(urnAntiga);

        if (listCorrel == null || listCorrel.isEmpty()) {
            throw new NotFoundException("Nenhuma correlacao encontrada para urn = " + urnAntiga);
        }

        for (Correlacao correl : listCorrel) {

            if (correl.getUrn1().equals(urnAntiga)) {
                correl.setUrn1(urnNova);
            }

            if (correl.getUrn2().equals(urnAntiga)) {
                correl.setUrn2(urnNova);
            }
        }

        if (!QuadroComparativoController.saveQuadroComparativo(request, qc)) { //saveQuadroComparativo(request, qc, false);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        URI uri = uriInfo.getAbsolutePathBuilder().path(qcid + "/" + urnNova).build();
        return Response.created(uri).build();
    }

    @GET
    @Path("/relacao/{qcid}/{urn1}/{urn2}/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Relacao> getRelacoes(@PathParam("qcid") String qcId,
            @PathParam("urn1") String urn1, @PathParam("urn2") String urn2) {

        Correlacao correlacao = getExistingCorrelacao(qcId, urn1, urn2, request);

        if (correlacao == null) {
            return new ArrayList<Relacao>();
        }

        return correlacao.getRelacoes();
    }

    @POST
    @Path("/relacao/{qcid}/{urn1}/{urn2}/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveRelacao(RelacaoImpl relacao,
            @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
            @PathParam("urn2") String urn2) {

        relacao.setProveniencia(new ProvenienciaUsuarioImpl());

        QuadroComparativo qc = getExistingQuadro(qcid, request);

        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);

        if (correlacao == null) {
            correlacao = new Correlacao(urn1, urn2);
            qc.addCorrelacao(correlacao);
        }

        // define algum tipo de relação caso nenhuma tenha sido informada.
        if (relacao.getRefTipo() == null) {
            relacao.setRefTipo(RelacaoImpl.TiposRelacao.getRefTipoRelacao(relacao));
        }

        if (relacao.getOrigem() == null || relacao.getOrigem().isEmpty()) {
            Texto texto = qc.getTexto(urn1);
            Set<Long> origem = new HashSet<Long>();
            origem.add(texto.getDocumento().getObjetoSimbolico().getId());
            relacao.setOrigem(origem);
        }

        if (relacao.getAlvo() == null || relacao.getAlvo().isEmpty()) {
            Texto texto = qc.getTexto(urn2);
            Set<Long> alvo = new HashSet<Long>();
            alvo.add(texto.getDocumento().getObjetoSimbolico().getId());
            relacao.setAlvo(alvo);
        }

        correlacao.addRelacao(relacao, qc);

        if (!QuadroComparativoController.saveQuadroComparativo(request, qc)) { //saveQuadroComparativo(request, qc, false);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        URI uri = uriInfo.getAbsolutePathBuilder().path(relacao.getId() + "").build();
        return Response.created(uri).build();
    }

    @DELETE
    @Path("/relacao/{qcid}/{urn1}/{urn2}/{idRelacao}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response deleteRelacao(@PathParam("idRelacao") String idRelacao,
            @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
            @PathParam("urn2") String urn2) {

        QuadroComparativo qc = getExistingQuadro(qcid, request);
        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);

        if (correlacao == null) {
            throw new NotFoundException();
        }

        correlacao.removeRelacao(idRelacao);

        if (!QuadroComparativoController.saveQuadroComparativo(request, qc)) { //saveQuadroComparativo(request, qc, false);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        String result = "Relacao deleted: " + idRelacao;
        return Response.ok(result).build();
    }

    /**
     * Retorna a lista de tipos de relação. 1:0 tipo00 descricao_do_tipo 0:1
     * tipo0 descricao_do_tipo 1:1 tipo1 descricao_do_tipo 1:1 tipo2
     * descricao_do_tipo 1:n tipo3 descricao_do_tipo n:1 tipo4 descricao_do_tipo
     * n:n tipo5 descricao_do_tipo
     *
     * @return
     */
    @GET
    @Path("/relacao/tipos/")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, String>> getTiposRelacao() {
        return RelacaoImpl.TiposRelacao.mapsTiposRelacao;
    }

    @GET
    @Path("/config/{qcid}/{urn1}/{urn2}/")
    @Produces(MediaType.APPLICATION_JSON)
    public ConfiguracaoImpl getConfiguracao(@PathParam("qcid") String qcId,
            @PathParam("urn1") String urn1, @PathParam("urn2") String urn2) {

        Correlacao correlacao = getExistingCorrelacao(qcId, urn1, urn2, request);

        if (correlacao == null) {
            throw new NotFoundException();
        }

        return correlacao.getConfiguracao();
    }

    @GET
    @Path("/processar/{qcid}/{urn1}/{urn2}/{porcentagem}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response processarRoboCorrelacao(/*ConfiguracaoImpl config,*/
            @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
            @PathParam("urn2") String urn2,
            @PathParam("porcentagem") final int slider) {

        QuadroComparativo qc = getExistingQuadro(qcid, request);

        Documento leftDoc = qc.getTexto(urn1).getDocumento();
        Documento rightDoc = qc.getTexto(urn2).getDocumento();

        final List<Relacao> lRelacao = new ArrayList<Relacao>();
        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);
        
        if (correlacao == null) {
            correlacao = new Correlacao(urn1, urn2);
            qc.addCorrelacao(correlacao);
            
        } else {
            List<Relacao> relacoesAtuais = correlacao.getRelacoes();
            if (relacoesAtuais != null) {
                lRelacao.addAll(relacoesAtuais);
            }

            correlacao.removeAllRelacoes();
        }

        CompareProcessConfiguration conf = new CompareProcessConfiguration() {
            @Override
            public double minSimilarity() {
                return slider / 100.0;
            }
        };

        CompareProcessSpec spec = new CompareProcessSpec(leftDoc, rightDoc, conf, qc, true, lRelacao, new CPT_TextToText("alt", "alt"));

        List<Relacao> resultado = Comparator.compareJ(spec);

        for (Relacao r : resultado) {
            correlacao.addRelacao(RelacaoImpl.newFromRelacao(r), qc);
        }

        if (!QuadroComparativoController.saveQuadroComparativo(request, qc)) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok().build();
    }
    
}
