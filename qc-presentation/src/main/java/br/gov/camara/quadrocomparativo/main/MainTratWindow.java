package br.gov.camara.quadrocomparativo.main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class MainTratWindow extends JFrame{
	
	private static final Logger logger = Logger.getLogger(MainTratWindow.class.getName());

	private static final long serialVersionUID = 4825266919343576506L;
	private JTextField textFieldEndereco;

	
	/**
	 * Create the frame.
	 */
	public MainTratWindow(String endereco) {
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setForeground(Color.WHITE);
		setTitle("Lexcomp");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setBounds(100, 100, 371, 287);
		
		setResizable(false);
		
		centreWindow(this);
		getContentPane().setLayout(null);
		
		textFieldEndereco = new JTextField();
		textFieldEndereco.setFont(new Font("Dialog", Font.BOLD, 12));
		textFieldEndereco.setEditable(false);
		textFieldEndereco.setBounds(22, 137, 326, 19);
		textFieldEndereco.setText(endereco);
		getContentPane().add(textFieldEndereco);
		textFieldEndereco.setColumns(10);
		
		JLabel lblAbraOEndereo = new JLabel("<html>Abra o endereço abaixo no <b>Firefox</b> ou no <b>Chrome</b>:</html>");
		lblAbraOEndereo.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblAbraOEndereo.setBounds(22, 118, 356, 15);
		getContentPane().add(lblAbraOEndereo);
		
		JButton btnCopiarEndereo = new JButton("Copiar endereço e fechar esta janela");
		btnCopiarEndereo.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnCopiarEndereo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//copia conteúdo para a área de transferência
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents(new StringSelection(textFieldEndereco.getText()), null);
				//fecha janela
				dispose();
			}
		});
		btnCopiarEndereo.setBounds(22, 168, 326, 25);
		getContentPane().add(btnCopiarEndereo);
		
		JPanel panel = new JPanel(){
			@Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        Image imageMarca = null;
				try {
					imageMarca = ImageIO.read(AppMainTray.class.getResource("/marca_lexcomp_Horizontal.jpg"));
					g.drawImage(imageMarca, 0, 0, null);
				} catch (IOException e) {
					logger.log(Level.ALL, "Não foi possível obter as imagens do formulário principal.", e);
				}
		    }
		};
		panel.setForeground(Color.WHITE);
		panel.setBackground(Color.WHITE);
		panel.setBounds(22, 16, 326, 88);
		getContentPane().add(panel);
		
		JLabel lbloLexcompContinuar = new JLabel("<html>(O Lexcomp continuará ativo. Para fechá-lo, acione o menu disponível no ícone na bandeja do sistema. Use o botão direito do mouse)</html>");
		lbloLexcompContinuar.setHorizontalAlignment(SwingConstants.CENTER);
		lbloLexcompContinuar.setFont(new Font("Dialog", Font.PLAIN, 9));
		lbloLexcompContinuar.setBounds(22, 205, 326, 41);
		getContentPane().add(lbloLexcompContinuar);
	}
	
	public static void centreWindow(Window frame) {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
}
