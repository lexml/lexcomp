package br.gov.camara.quadrocomparativo.lexml;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.sun.jersey.api.NotFoundException;

public class DocumentoLexmlUtil {

	private static final Logger log = Logger.getLogger(DocumentoLexmlUtil.class.getName());

	private static final String LEXML_URL_PREFIX = "http://www.lexml.gov.br/urn/";
	private static final Pattern ZIP_PATTERN = Pattern.compile("\\[ Arquivo no formato LexML \\].*?href=\"(.*?)\">");

	/**
	 * Cria uma URL com o padrão do LEXML
	 * @param urn
	 * @return
	 * @throws MalformedURLException
	 */
	private static URL makeURLLexml(String urn) throws MalformedURLException {
		return new URL(LEXML_URL_PREFIX + urn);
	}

	/**
	 * Retorna a URL do arquivo ZIP de uma URN ou null caso a página ou a URL
	 * não existam.
	 * 
	 * @param urn
	 * @return
	 */
	public static String getZipFileUrlFromLexmlPage(String urn) {
		byte[] lexmlPage = downloadLexmlPage(urn);

		if (lexmlPage != null) {
			Matcher m = ZIP_PATTERN.matcher(new String(lexmlPage));
			if (m.find()) {
				return m.group(1);
			}
		}

		return null;
	}
	
	public static String getTextoCamaraUrlFromLexmlPage(String urn){
		try {
            if (urn == null) {
                throw new NotFoundException();
            }

            urn = urn.replaceAll("__", ";");
            Document docLexml = Jsoup.connect("http://www.lexml.gov.br/urn/"+ urn).get();

            Elements camara = docLexml.select("a[href*=camara.gov.br/legin/]").select("a[href*=publicacaooriginal]");

            if (camara.isEmpty()) {
                return null;
            }

            return camara.attr("href");
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
		return null;
	}
	
	public static String downloadTextoCamaraFromUrn(String urn){
		
		String hrefCamara = getTextoCamaraUrlFromLexmlPage(urn);
		
		if (hrefCamara == null){
			return null;
		}

		try {
			Document doc = Jsoup.connect(hrefCamara).get();
		    Elements elem = doc.select(".textoNorma");
		    return elem.html();
		} catch (IOException e) {
            log.log(Level.SEVERE, null, e);
		}
		return null;
	}
	
	/**
	 * Retorna a página LEXML de uma URN
	 * 
	 * @param urn
	 * @return
	 */
	public static byte[] downloadLexmlPage(String urn) {
		try {
			return IOUtils.toByteArray(makeURLLexml(urn));

		} catch (MalformedURLException e) {
			log.log(Level.WARNING,
					"MalformedURLException when downloading LEXML zip file", e);
		} catch (IOException e) {
			log.log(Level.WARNING,
					"IOException when downloading LEXML zip file", e);
		}

		return null;
	}

	/**
	 * Download a zip file from a URN
	 * 
	 * @param urn
	 * @return
	 */
	public static byte[] downloadLexmlZip(String urn) {
		try {
			String s = getZipFileUrlFromLexmlPage(urn);
			if (s != null) {
				System.out.println("Zip file: "+s);
				return IOUtils.toByteArray(new URL(s));
			}
		} catch (MalformedURLException e) {
			log.log(Level.WARNING,
					"MalformedURLException when downloading LEXML zip file", e);
		} catch (IOException e) {
			log.log(Level.WARNING,
					"IOException when downloading LEXML zip file", e);
		}

		return null;
	}
	

	
	
	
	/**
	 * Teste
	 * @param args
	 */
	public static void main(String args[]) {

		String urn = "urn:lex:br:camara.deputados:projeto.lei;pl:2003-02-18;54";
		
		LexmlFile zipLexml = LexmlFile.newLexmlFileFromURN(urn);
		if (zipLexml!= null){
			System.out.println("Texto:");
			System.out.println(zipLexml.getTextoAsString());
			
			System.out.println("proposicao.xml");
			System.out.println(zipLexml.getProposicaoAsString());
		}
		
		System.out.println("Fim");		
	}


}