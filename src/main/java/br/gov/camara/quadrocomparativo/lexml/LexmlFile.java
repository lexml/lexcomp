package br.gov.camara.quadrocomparativo.lexml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

public class LexmlFile {
	
	private static final Logger log = Logger.getLogger(LexmlFile.class.getName());

	private byte[] textoXML;
	private byte[] proposicaoXML;
	
	public static LexmlFile newLexmlFileFromURN(String urn) {
		byte[] zip = DocumentoLexmlUtil.downloadLexmlZip(urn);
		if (zip!= null){
			return newLexmlFileFromCompreesedData(zip);
		}
		return null;
	}
	
	public static LexmlFile newLexmlFileFromCompreesedData(byte[] compressedData) {
		
		LexmlFile retorno = new LexmlFile();

		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(compressedData));

		try {
			ZipEntry zipEntry;
			
			while ((zipEntry = zipStream.getNextEntry()) != null) {
				
				log.log(Level.INFO, "Extraindo: "+zipEntry.getName());
				
				if (zipEntry.getName().equals("texto.xml")) {
					retorno.textoXML = IOUtils.toByteArray(zipStream);
				}
				if (zipEntry.getName().equals("proposicao.xml")) {
					retorno.proposicaoXML = IOUtils.toByteArray(zipStream);
				}

				zipStream.closeEntry();

			}
			zipStream.close();
			
			if (retorno.textoXML!= null && retorno.proposicaoXML!= null) {
				return retorno;
			}

		} catch (IOException e) {
			log.log(Level.WARNING, "IOException when extracting files from Lexml ZIP", e);
		}
		return null;
	}
	
	public byte[] getProposicao() {
		return proposicaoXML;
	}
	
	public String getProposicaoAsString() {
		if (proposicaoXML == null){
			return null;
		}
		return new String(proposicaoXML);
	}
	
	public byte[] getTexto() {
		return textoXML;
	}
	
	public String getTextoAsString() {
		if (textoXML == null){
			return null;
		}
		return new String(textoXML);
	}
}