package br.gov.camara.quadrocomparativo.main;
import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class AppMainTray {

	private static final Logger logger = Logger.getLogger(AppMainTray.class.getName());

	public static void main(String[] args) {
		
		//se for a única instância...
		if (SingleInstance.lockInstance()){
			//abre tray
			new AppMainTray().configurarTray(AppNoFX.getURL());
	
			//executa serviço
			AppNoFX.startServer();
		}
	}
	
	public void configurarTray(final String url){
		final TrayIcon trayIcon; 

		if (SystemTray.isSupported()) {

			SystemTray tray = SystemTray.getSystemTray();


			Image imageICO = null;
			try {
				imageICO = ImageIO.read(AppMainTray.class.getResource("/tray.jpg"));
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Não foi possível obter ícone do formulário principal.", e);
			} finally {
				if (imageICO == null){
					logger.log(Level.SEVERE, "Não foi possível obter ícone do formulário principal.");
				}
			}
			

			// Criamos um listener para escutar os eventos de mouse
			MouseListener mouseListener = new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					abrirJanelaPrincipal(url);
				}

				public void mouseEntered(MouseEvent e) {

				}

				public void mouseExited(MouseEvent e) {

				}

				public void mousePressed(MouseEvent e) {

				}

				public void mouseReleased(MouseEvent e) {

				}

			};

			// Criando um objeto PopupMenu
			PopupMenu popup = new PopupMenu();

			// criando itens do menu
			MenuItem mostramsg = new MenuItem("Abrir endereço do Lexcomp");
			mostramsg.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					abrirJanelaPrincipal(url);
				}
			});

			MenuItem defaultItem = new MenuItem("Sair da aplicação");
			defaultItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//JOptionPane.showMessageDialog(null, "Saindo...");
					System.exit(0);
				}
			});

			// Adicionando itens ao PopupMenu
			popup.add(mostramsg);

			// adiconando um separador
			popup.addSeparator();

			popup.add(defaultItem);

			// criando um objeto do tipo TrayIcon
			trayIcon = new TrayIcon(imageICO, "Lexcomp", popup);
			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					trayIcon.displayMessage("Action Event", "Um Evento foi disparado", TrayIcon.MessageType.INFO);
				}
			};

			// Na linha a seguir a imagem a ser utilizada como icone sera redimensionada
			trayIcon.setImageAutoSize(true);

			// Seguida adicionamos os actions listeners
			trayIcon.addActionListener(actionListener);
			trayIcon.addMouseListener(mouseListener);

			// Tratamento de erros
			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				logger.log(Level.SEVERE, "Erro, TrayIcon não será adicionado.", e);
			}
			
			//abrir a janela principal
			abrirJanelaPrincipal(url);
			
		} else {
			// Caso o item System Tray não for suportado
			JOptionPane.showMessageDialog(null, "Não é possível abrir o Lexcomp no seu sistema. Por favor, procure apoio técnico.");
		}
	}
	
	private void abrirJanelaPrincipal(final String url){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainTratWindow frame = new MainTratWindow(url);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		System.out.println("Tray Icon - O Mouse foi pressionado!");
	}
}