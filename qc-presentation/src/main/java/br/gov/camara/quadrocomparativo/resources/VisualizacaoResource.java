package br.gov.camara.quadrocomparativo.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import br.gov.camara.quadrocomparativo.model.Coluna;
import br.gov.camara.quadrocomparativo.model.Correlacao;
import br.gov.camara.quadrocomparativo.model.QuadroComparativo;
import br.gov.camara.quadrocomparativo.model.Texto;
import br.gov.lexml.symbolicobject.Comentario;
import br.gov.lexml.symbolicobject.Documento;
import br.gov.lexml.symbolicobject.Relacao;
import br.gov.lexml.symbolicobject.indexer.Indexer;
import br.gov.lexml.symbolicobject.table.ColumnSpec;
import br.gov.lexml.symbolicobject.table.OpcoesVisualizacao;
import br.gov.lexml.symbolicobject.table.Visualizacao;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/visualizacao/")
public class VisualizacaoResource {

    private static final Logger log = Logger.getLogger(VisualizacaoResource.class.getName());

    @Context
    HttpServletRequest request;

    @GET
    @Path("/{qcid}/{porcentagem}")
    @Produces(MediaType.TEXT_HTML)
    public String getVisualizacao(@PathParam("qcid") String idQuadro, @PathParam("porcentagem") final int porcentagem) {

        QuadroComparativo qc = QuadroComparativoController
                .getQuadroComparativo(request, idQuadro);

        if (qc == null) {
            //return "Falha ao obter o quadro comparativo.";
            throw new WebApplicationException(Response.serverError().entity(
                    "Falha ao obter o quadro comparativo.").build());
        
        } else if (qc.getCorrelacoes() == null || qc.getCorrelacoes().isEmpty()) {
            throw new WebApplicationException(Response.serverError().entity(
                    "Não existem correlações a serem visualizadas.").build());
        
        } else {
            boolean hasCorrel = false;
            for (Correlacao c: qc.getCorrelacoes()) {
                
                if (c.getRelacoes() != null && !c.getRelacoes().isEmpty()) {
                    hasCorrel = true;
                }
            }
            
            if (!hasCorrel) {
                throw new WebApplicationException(Response.serverError().entity(
                    "Não existem correlações a serem visualizadas.").build());
            }
        }

        // monta as colunas nos documentos
        List<ColumnSpec> colunas = new ArrayList<ColumnSpec>();
        for (Coluna c : qc.getColunas()) {
            List<Documento> documentos = new ArrayList<Documento>();
            for (Texto t : c.getTextos()) {
                if (t != null) {
                    if (t.getIncluidoVisualizacao()) {
                        documentos.add(t.getDocumento());
                        ColumnSpec cs = new ColumnSpec(c.getTitulo(), documentos);
                        colunas.add(cs);
                    }
                }
            }
        }

        // montando colunas
        String saidaHtml;
        Visualizacao visualizacao = new Visualizacao(makeIndexer(qc), new OpcoesVisualizacao() {
            @Override
            public double getMaxUpdateRatio() {
                return porcentagem / 100.0;
            }
        });
        saidaHtml = visualizacao.createHtmlTable(getIndexOrder(qc), colunas, qc.getTitulo());

        return saidaHtml;
    }

    /**
     * TODO implementar a obtencao da ordem de exibicao das colunas
     *
     * @param qc
     * @return
     */
    private List<Integer> getIndexOrder(QuadroComparativo qc) {

        List<Integer> res = new ArrayList<Integer>();

        for (int i = qc.getColunas().size() - 1; i >= 0; i--) {
        	for (Texto t : qc.getColunas().get(i).getTextos()){
        		if (t.getIncluidoVisualizacao()){
        			res.add(i);
        			break;
        		}
        	}
        }

        return res;
    }

    private Indexer makeIndexer(QuadroComparativo qc) {

        if (qc == null) {
            return null;
        }

        Indexer indexer = new Indexer();

        if (qc.getCorrelacoes() != null) {

            for (Correlacao c : qc.getCorrelacoes()) {

                // documentos
                for (Documento d : qc.getAllDocumentos()) {
                    indexer.addDocumento(d);
                }

                // relações
                if (c.getRelacoes() != null) {
                    for (Relacao r : c.getRelacoes()) {
                        indexer.addRelacao(r);
                    }
                }
                
                // comentários
                if (c.getComentarios() != null) {
                    for (Comentario m : c.getComentarios()) {
                        indexer.addComentario(m);
                    }
                }

            }
        }

        return indexer;
    }

}
