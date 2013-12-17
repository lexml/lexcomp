package br.gov.camara.quadrocomparativo.main;
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
import java.awt.Color;


public class MainTratWindow extends JFrame {
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
		
		setBounds(100, 100, 371, 226);
		
		setResizable(false);
		
		centreWindow(this);
		getContentPane().setLayout(null);
		
		textFieldEndereco = new JTextField();
		textFieldEndereco.setFont(new Font("Dialog", Font.BOLD, 12));
		textFieldEndereco.setEditable(false);
		textFieldEndereco.setBounds(22, 123, 326, 19);
		textFieldEndereco.setText(endereco);
		getContentPane().add(textFieldEndereco);
		textFieldEndereco.setColumns(10);
		
		JLabel lblAbraOEndereo = new JLabel("<html>Abra o endereço abaixo no <b>Firefox</b> ou no <b>Chrome</b>:</html>");
		lblAbraOEndereo.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblAbraOEndereo.setBounds(22, 105, 356, 15);
		getContentPane().add(lblAbraOEndereo);
		
		JButton btnCopiarEndereo = new JButton("Copiar endereço");
		btnCopiarEndereo.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnCopiarEndereo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents(new StringSelection(textFieldEndereco.getText()), null);
			}
		});
		btnCopiarEndereo.setBounds(22, 154, 168, 25);
		getContentPane().add(btnCopiarEndereo);
		
		JPanel panel = new JPanel(){
			@Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        Image imageMarca = null;
				try {
					imageMarca = ImageIO.read(MainTray.class.getResource("/marca_lexcomp_Horizontal.jpg"));
					g.drawImage(imageMarca, 0, 0, null);
				} catch (IOException e) {
					logger.log(Level.ALL, "Não foi possível obter as imagens do formulário principal.", e);
				}
		    }
		};
		panel.setForeground(Color.WHITE);
		panel.setBackground(Color.WHITE);
		panel.setBounds(22, 12, 326, 72);
		getContentPane().add(panel);
	}
	
	public static void centreWindow(Window frame) {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
}
