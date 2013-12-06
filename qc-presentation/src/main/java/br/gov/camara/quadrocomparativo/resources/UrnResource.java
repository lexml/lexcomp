/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.resources;

// POJO, no interface no extends
import br.gov.camara.quadrocomparativo.model.Texto;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 
// The browser requests per default the HTML MIME type.
@Path("/urn")
public class UrnResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    
    public Texto getUrn(@QueryParam("tipoTexto") String tipoTexto,
       
        @QueryParam("categorias") String categorias,
        @QueryParam("localidades") String localidades,
        @QueryParam("autoridades") String autoridades,
        @QueryParam("autoridadesFilhas") String autoridadesFilhas,
        @QueryParam("comissaoespecial") String comissaoespecial,
        @QueryParam("tiposDocumento") String tiposDocumento,
        @QueryParam("numero") String numero,
        @QueryParam("dataAssinatura") String dataAssinatura,
        @QueryParam("componenteDocumento") String componenteDocumento,
        @QueryParam("numeroComponente") String numeroComponente,
        @QueryParam("anoComponente") String anoComponente,
        @QueryParam("siglaColegiado") String SiglaColegiado,
        @QueryParam("versao") String versao,
        @QueryParam("dataVigente") String dataVigente,
        @QueryParam("evento") String evento,
        @QueryParam("dataEvento") String dataEvento) {
    
        Texto tex = new Texto();
        tex.setCategorias(categorias);
        tex.setLocalidades(localidades);
        tex.setAutoridades(autoridades);
        tex.setAutoridadesFilhas(autoridadesFilhas);
        tex.setComissaoespecial(comissaoespecial);
        tex.setTiposDocumento(tiposDocumento);
        tex.setNumero(numero);
        tex.setDataAssinatura(dataAssinatura);
        tex.setComponenteDocumento(componenteDocumento);
        tex.setNumeroComponente(numeroComponente);
        tex.setAnoComponente(anoComponente);
        tex.setSiglaColegiado(SiglaColegiado);
        tex.setVersao(versao);
        tex.setDataVigente(dataVigente);
        tex.setEvento(evento);
        tex.setDataEvento(dataEvento);  
        
        tex.geraTitulo();
        
        return tex;
    }    
}