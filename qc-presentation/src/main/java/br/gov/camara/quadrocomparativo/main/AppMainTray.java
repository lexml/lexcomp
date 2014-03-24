package br.gov.camara.quadrocomparativo.main;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import br.gov.camara.quadrocomparativo.model.QuadroComparativo;

public class AppMainTray {

	public static void main(String[] args) {
    	System.setProperty("file.encoding", "UTF-8");

		//se for a única instância...
		if (SingleInstance.lockInstance()){
			
			try {
				//testa se é possível criar o diretório home
				verificarHomeDir();
					
				//abre tray
				new TrayConfig(AppNoFX.getURL()).configurarTray();
		
				//executa serviço
				AppNoFX.startServer();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Erro", e.getMessage(), JOptionPane.ERROR_MESSAGE);
			}
		}					
	}
	
	private static void verificarHomeDir() throws IOException{
		File lexcompHome = new File(QuadroComparativo.QUADROS_HOMEDIR);
		if (!lexcompHome.exists()){	
			try {
				FileUtils.forceMkdir(lexcompHome);
			} catch (IOException e) {
				throw new IOException("Não foi possível criar as pasta que conteria o repositório de dados da aplicação em "+QuadroComparativo.FILENAME_PREFIX, e);
			}
		}
	}
	

}