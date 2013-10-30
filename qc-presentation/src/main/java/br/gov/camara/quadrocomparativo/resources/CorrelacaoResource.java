/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.resources;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.gov.camara.quadrocomparativo.model.ConfiguracaoImpl;
import br.gov.camara.quadrocomparativo.model.Correlacao;
import br.gov.camara.quadrocomparativo.model.ProvenienciaUsuarioImpl;
import br.gov.camara.quadrocomparativo.model.QuadroComparativo;
import br.gov.camara.quadrocomparativo.model.RelacaoImpl;
import br.gov.camara.quadrocomparativo.model.Texto;
import br.gov.lexml.symbolicobject.Documento;
import br.gov.lexml.symbolicobject.Relacao;
import br.gov.lexml.symbolicobject.comp.CPT_TextToText;
import br.gov.lexml.symbolicobject.comp.Comparator;
import br.gov.lexml.symbolicobject.comp.CompareProcessConfiguration;
import br.gov.lexml.symbolicobject.comp.CompareProcessSpec;

import com.sun.jersey.api.NotFoundException;

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 
// The browser requests per default the HTML MIME type.
@Path("/correlacao")
public class CorrelacaoResource {

	@Context
	HttpServletRequest request; 	//System.out.println("request: "+request.getSession(true).getId());
	
    @GET @Path("/{qcid}/{urn1}/{urn2}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Correlacao getCorrelacao(@PathParam("qcid") String qcId,
        @PathParam("urn1") String urn1, @PathParam("urn2") String urn2) {
        
        QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcId);
        
        urn1 = urn1.replaceAll("__", ";");
        urn2 = urn2.replaceAll("__", ";");
        
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
            texto1.setDocumento(textoRes.getEstruturaTextoInQuadro(qcId, urn1).getDocumento());
        }
        
        if (texto2 != null && texto2.getDocumento() == null) {
            TextoResource textoRes = new TextoResource();
            texto2.setDocumento(textoRes.getEstruturaTextoInQuadro(qcId, urn2).getDocumento());
        }
        
        correlacao.setTexto1(texto1);
        correlacao.setTexto2(texto2);
        
        return correlacao;
    }
    
    @POST @Path("/updateid/{qcid}/{urn1}/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateIdCorrelacao(String urnNova,
        @PathParam("qcid") String qcid, @PathParam("urn1") String urnAntiga) {
        
    	
        QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcid);
        
        urnAntiga = urnAntiga.replaceAll("__", ";");
        urnNova = urnNova.replaceAll("__", ";");
        
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
        
        QuadroComparativoController.saveQuadroComparativo(request, qc); //saveQuadroComparativo(request, qc, false);
        
        String result = "Quadro saved: " + qc;
        return Response.status(201).entity(result).build();
    }
    
    @GET @Path("/relacao/{qcid}/{urn1}/{urn2}/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Relacao> getRelacoes(@PathParam("qcid") String qcId,
        @PathParam("urn1") String urn1, @PathParam("urn2") String urn2) {
        
    	QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcId);
        
        urn1 = urn1.replaceAll("__", ";");
        urn2 = urn2.replaceAll("__", ";");
        
        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);
        
        if (correlacao == null) {
            return new ArrayList<Relacao>();
        }
        
        return correlacao.getRelacoes();
    }
    
    @POST @Path("/relacao/{qcid}/{urn1}/{urn2}/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveRelacao(RelacaoImpl relacao,
        @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
        @PathParam("urn2") String urn2) {
        
    	relacao.setProveniencia(new ProvenienciaUsuarioImpl());
    	
        QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcid);
        
        urn1 = urn1.replaceAll("__", ";");
        urn2 = urn2.replaceAll("__", ";");
        
        Correlacao correl = qc.getCorrelacao(urn1, urn2);
        
        if (correl == null) {
            correl = new Correlacao(urn1, urn2);
            qc.addCorrelacao(correl);
        }

        // define algum tipo de relação caso nenhuma tenha sido informada.
        if (relacao.getRefTipo() == null){
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
        
        correl.addRelacao(relacao, qc);
       
        QuadroComparativoController.saveQuadroComparativo(request, qc); //saveQuadroComparativo(request, qc, false);
        
        String result = "Relacao saved: " + relacao;
        return Response.status(201).entity(result).build();
    }
    
    @DELETE @Path("/relacao/{qcid}/{urn1}/{urn2}/{idRelacao}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response deleteRelacao(@PathParam("idRelacao") String idRelacao,
        @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
        @PathParam("urn2") String urn2) {
        
        QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcid);
        
        urn1 = urn1.replaceAll("__", ";");
        urn2 = urn2.replaceAll("__", ";");
        
        Correlacao correl = qc.getCorrelacao(urn1, urn2);
        
        if (correl == null) {
            throw new NotFoundException();
        }
        
        correl.removeRelacao(idRelacao);
        
        QuadroComparativoController.saveQuadroComparativo(request, qc); //saveQuadroComparativo(request, qc, false);
        
        String result = "Relacao deleted: " + idRelacao;
        return Response.status(200).entity(result).build();
    }
    
    /**
     * Retorna a lista de tipos de relação.
     * 1:0 tipo00 descricao_do_tipo
     * 0:1 tipo0 descricao_do_tipo
     * 1:1 tipo1 descricao_do_tipo
     * 1:1 tipo2 descricao_do_tipo
     * 1:n tipo3 descricao_do_tipo
     * n:1 tipo4 descricao_do_tipo
     * n:n tipo5 descricao_do_tipo
     * @return
     */
    @GET @Path("/relacao/tipos/")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String,String>> getTiposRelacao(){
        return RelacaoImpl.TiposRelacao.mapsTiposRelacao;
    }
            
    @GET @Path("/config/{qcid}/{urn1}/{urn2}/")
    @Produces(MediaType.APPLICATION_JSON)
    public ConfiguracaoImpl getConfiguracao(@PathParam("qcid") String qcId,
        @PathParam("urn1") String urn1, @PathParam("urn2") String urn2) {
        
    	QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcId);
        
        urn1 = urn1.replaceAll("__", ";");
        urn2 = urn2.replaceAll("__", ";");
        
        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);
        
        if (correlacao == null) {
            throw new NotFoundException();
        }
        
        return correlacao.getConfiguracao();
    }
    
    @POST @Path("/processar/{qcid}/{urn1}/{urn2}/{porcentagem}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response processarRoboCorrelacao(/*ConfiguracaoImpl config,*/
        @PathParam("qcid") String qcid, @PathParam("urn1") String urn1,
        @PathParam("urn2") String urn2,
        @PathParam("porcentagem") final int slider){
    	
        QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcid);
        
        urn1 = urn1.replaceAll("__", ";");
        urn2 = urn2.replaceAll("__", ";");

        
        Documento leftDoc = qc.getTexto(urn1).getDocumento();
        Documento rightDoc = qc.getTexto(urn2).getDocumento(); 
        
        final List<Relacao> lRelacao = new ArrayList<Relacao>();
        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);
        if (correlacao == null){
        	correlacao = new Correlacao(urn1, urn2);
            qc.addCorrelacao(correlacao);
        } else {
        	List<Relacao> relacoesAtuais = correlacao.getRelacoes();
	        if(relacoesAtuais != null) { 
	        	lRelacao.addAll(relacoesAtuais); 
	        }
	        			        
	        correlacao.removeAllRelacoes();
        }
        
        CompareProcessConfiguration conf = new CompareProcessConfiguration(){
        	@Override
        	public double minSimilarity() {
        		return slider / 100.0;
        	}
        };               
        
        CompareProcessSpec spec = new CompareProcessSpec(leftDoc, rightDoc, conf, qc, true, lRelacao, new CPT_TextToText("alt","alt"));
        		
        List<Relacao> resultado = Comparator.compareJ(spec);
        
        for(Relacao r : resultado) {
        	correlacao.addRelacao(RelacaoImpl.newFromRelacao(r), qc);
        }
                
        QuadroComparativoController.saveQuadroComparativo(request, qc); 
                        
        /*
        String result = "Config saved: ";
        return Response.status(201).entity(result).build();
        */
        
        return Response.status(201).build();
    }
    

    
}