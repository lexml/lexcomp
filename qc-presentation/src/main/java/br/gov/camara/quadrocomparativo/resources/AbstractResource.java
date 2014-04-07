/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.gov.camara.quadrocomparativo.resources;

import br.gov.camara.quadrocomparativo.model.Correlacao;
import br.gov.camara.quadrocomparativo.model.QuadroComparativo;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import br.gov.lexml.symbolicobject.Relacao;

/**
 *
 * @author marciofonseca
 */
public abstract class AbstractResource {
    
    protected QuadroComparativo getExistingQuadro(String qcId, HttpServletRequest request) {
        QuadroComparativo qc = QuadroComparativoController.getQuadroComparativo(request, qcId);

        if (qc == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        return qc;
    }
    
    protected Correlacao getExistingCorrelacao(String qcId, String urn1, String urn2, HttpServletRequest request) {
        
        QuadroComparativo qc = getExistingQuadro(qcId, request);

        Correlacao correlacao = qc.getCorrelacao(urn1, urn2);

        if (correlacao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        return correlacao;
    }

    protected Relacao getExistingRelacao(String qcId, String urn1, String urn2, Long relacaoId, HttpServletRequest request) {
        Correlacao correlacao = getExistingCorrelacao(qcId, urn1, urn2, request);
        Relacao relacao = correlacao.getRelacao(relacaoId);
        
        if (relacao == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
        return relacao;
    }
    
    //abstract protected HttpServletRequest getRequest();
}
