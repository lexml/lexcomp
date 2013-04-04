package br.gov.camara.quadrocomparativo.resources;

import java.util.ArrayList;
import java.util.List;

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
import br.gov.lexml.symbolicobject.table.Visualizacao;

@Path("/visualizacao/")
public class VisualizacaoResource {

	@Context
	HttpServletRequest request;

	@GET
	@Path("/{qcid}")
	@Produces(MediaType.TEXT_HTML)
	public String getVisualizacao(@PathParam("qcid") String idQuadro) {

		System.out.println("ID DO QUADRO: " + idQuadro);

		QuadroComparativo qc = QuadroComparativoController
				.getQuadroComparativo(request, idQuadro);

		// monta as colunas nos documentos
		List<List<Documento>> colunas = new ArrayList<List<Documento>>();
		for (Coluna c : qc.getColunas()) {
			List<Documento> documentos = new ArrayList<Documento>();
			for (Texto t : c.getTextos()) {
				if (t != null) {
					if (t.getIncluidoVisualizacao() || true) {
						documentos.add(t.getDocumento());
					}
				}
			}
			colunas.add(documentos);
		}

		// montando colunas
		String saidaHtml = "";
		Visualizacao visualizacao = new Visualizacao(makeIndexer(qc));
		saidaHtml = visualizacao.createHtmlTable(getIndexOrder(qc), colunas);

		return "sucesso: " + idQuadro + " saida html: " + saidaHtml;
	}

	/**
	 * TODO implementar a obtencao da ordem de exibicao das colunas
	 * 
	 * @param qc
	 * @return
	 */
	private List<Integer> getIndexOrder(QuadroComparativo qc) {

		List<Integer> res= new ArrayList<Integer>();
		
		for (int i = 0; i < qc.getColunas().size(); i++) {
			if (qc.getColunas().get(i).getColunaPrincipal()) {
				res.add(i);
			}
		}
		
		if (res.isEmpty()){
			res.add(0);
		}

		return res;
	}

	private Indexer makeIndexer(QuadroComparativo qc) {
		Indexer indexer = new Indexer();

            for (Correlacao c : qc.getCorrelacoes()) {

            	System.out.println("QUANT: "+qc.getAllDocumentos().size());
            	
                    // documentos
                    for (Documento d : qc.getAllDocumentos()) {
                            indexer.addDocumento(d);
                    }

                    // relações
                    if (c.getRelacoes() != null){
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

            return indexer;
	}

}
