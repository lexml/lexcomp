/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.resources;

import br.gov.camara.quadrocomparativo.model.Conexao;
import br.gov.camara.quadrocomparativo.model.Correlacao;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import br.gov.camara.quadrocomparativo.model.QuadroComparativo;

import com.sun.jersey.api.NotFoundException;
import java.net.URI;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 
// The browser requests per default the HTML MIME type.
@Path("/qc")
public class QuadroComparativoResource {

    @Context
    HttpServletRequest request; 	//System.out.println("request: "+request.getSession(true).getId());

    @Context 
    UriInfo uriInfo;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public QuadroComparativo getNewQuadroComparativo() {

        return QuadroComparativoController.createQuadroComparativo(request);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public QuadroComparativo getQuadroComparativoSemArticulacoes(@PathParam("id") String id) {

        QuadroComparativo quadro = QuadroComparativoController.getQuadroComparativo(request, id);

        if (quadro == null) {
            throw new NotFoundException();
        }

        // articulacoes nao serao transmitidas ao realizar operacoes com o
        // quadro comparativo para que o trafego nao fique pesado
        return quadro.removeArticulacoes();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveQuadroComparativo(QuadroComparativo quadro) {

        if (!QuadroComparativoController.saveQuadroComparativo(request, quadro)) {

            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } else {
            URI uri = uriInfo.getAbsolutePathBuilder().path(quadro.getId() + "").build();
            return Response.created(uri).build();
        }
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<QuadroComparativo> getQuadroComparativoList() {

        File dir = new File(QuadroComparativo.QUADROS_HOMEDIR); 
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File directory, String fileName) {
                return fileName.startsWith("qc-") && fileName.endsWith(".xml");
            }
        };

        File[] files = dir.listFiles(filter);
        List<QuadroComparativo> quadros = new ArrayList<QuadroComparativo>();

        if (files != null) {

            for (File f : files) {
                QuadroComparativo qc = QuadroComparativoController.getQuadroComparativoFromFile(f);
                QuadroComparativo qcEnvio = new QuadroComparativo(qc.getId(), qc.getTitulo());
                qcEnvio.setDataModificacao2(new Date(f.lastModified()));
                quadros.add(qcEnvio);
            }
        }

        return quadros;
    }

    @DELETE
    @Path("/{qcid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteQuadroComparativo(@PathParam("qcid") String qcid) {

        if (QuadroComparativoController.deleteQuadroComparativo(request, qcid)) {

            String result = "Quadro deleted: " + qcid;

            return Response.ok(result).build();
        }
        return Response.status(404).build();
    }

}
