package br.gov.camara.quadrocomparativo.lexml;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import br.gov.camara.quadrocomparativo.configuracaourn.Autoridade;
import br.gov.camara.quadrocomparativo.configuracaourn.Categoria;
import br.gov.camara.quadrocomparativo.configuracaourn.Configuracao;
import br.gov.camara.quadrocomparativo.configuracaourn.Evento;
import br.gov.camara.quadrocomparativo.configuracaourn.Localidade;
import br.gov.camara.quadrocomparativo.configuracaourn.TipoDocumento;
import br.gov.camara.quadrocomparativo.configuracaourn.Versao;

public class ConfiguracaoUrn implements Serializable {
	
	private static final long serialVersionUID = -645057557519573335L;
	
	private static ConfiguracaoUrn instance;

	public static synchronized ConfiguracaoUrn getInstance() {
		if (instance == null) {
			try {
				instance = new ConfiguracaoUrn();
				String packageName = Configuracao.class.getPackage().getName();
				JAXBContext jc = JAXBContext.newInstance(packageName);
				Unmarshaller u = jc.createUnmarshaller();
				Configuracao cfg = ((JAXBElement<Configuracao>) u
						.unmarshal(Configuracao.class.getClassLoader()
								.getResourceAsStream("xml/configuracao.xml")))
						.getValue();
                                System.err.println("cfg = " + cfg);
				instance.load(cfg);

			} catch (Exception ex) {
                               ex.printStackTrace();
			}

		}
		return instance;
	}

	private final Map<String, Categoria> categorias = new HashMap<String, Categoria>();
	private final Map<String, Localidade> localidades = new HashMap<String, Localidade>();
	private final Map<String, Autoridade> autoridades = new HashMap<String, Autoridade>();
	private final Map<String, TipoDocumento> tiposDocumentos = new HashMap<String, TipoDocumento>();
	private final Map<String, Versao> versoes = new HashMap<String, Versao>();
	private final Map<String, Evento> eventos = new HashMap<String, Evento>();

	private ConfiguracaoUrn() {

	}

	private void load(Configuracao cfg) {
		for (Categoria voc : cfg.getCategorias().getCategoria()) {
			addCategoria(voc);
		}
		for (Localidade voc : cfg.getLocalidades().getLocalidade()) {
			addLocalidade(voc);
		}
		for (Autoridade voc : cfg.getAutoridades().getAutoridade()) {
                        //System.err.println("Autoridade : " + voc.getNome() + ", id  = " + voc.getXmlid());
			addAutoridade(voc);
		}
		for (TipoDocumento voc : cfg.getTiposDocumentos().getTipoDocumento()) {
			addTipoDocumento(voc);
		}
		for (Versao voc : cfg.getVersoes().getVersao()) {
			addVersao(voc);
		}
		for (Evento voc : cfg.getEventos().getEvento()) {
			addEvento(voc);
		}
	}

	public Categoria getCategoria(String id) {
		return categorias.get(id);
	}

	public Localidade getLocalidade(String id) {
		return localidades.get(id);
	}

	public Autoridade getAutoridade(String id) {
		return autoridades.get(id);
	}

	public TipoDocumento getTipoDocumento(String id) {
		return tiposDocumentos.get(id);
	}

	public Versao getVersao(String id) {
		return versoes.get(id);
	}

	public Evento getEvento(String id) {
		return eventos.get(id);
	}

	private void addCategoria(Categoria voc) {
		this.categorias.put(voc.getXmlid(), voc);
	}

	private void addLocalidade(Localidade voc) {
		this.localidades.put(voc.getXmlid(), voc);
	}

	private void addAutoridade(Autoridade voc) {
		this.autoridades.put(voc.getXmlid(), voc);
	}

	private void addTipoDocumento(TipoDocumento voc) {
		this.tiposDocumentos.put(voc.getXmlid(), voc);
	}

	private void addVersao(Versao voc) {
		this.versoes.put(voc.getXmlid(), voc);
	}

	private void addEvento(Evento voc) {
		this.eventos.put(voc.getXmlid(), voc);
	}
}
