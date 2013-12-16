package br.gov.camara.quadrocomparativo.main;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;


public class MainTratWindow extends JFrame {

	private static final long serialVersionUID = 4825266919343576506L;
	private JTextField textFieldEndereco;

	/**
	 * Create the frame.
	 */
	public MainTratWindow(String endereco) {
		setTitle("Lexcomp");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setBounds(100, 100, 450, 300);
		
		setResizable(false);
		
		centreWindow(this);
		getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Lexcomp");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		lblNewLabel.setBounds(50, 46, 157, 19);
		getContentPane().add(lblNewLabel);
		
		textFieldEndereco = new JTextField();
		textFieldEndereco.setFont(new Font("Dialog", Font.BOLD, 12));
		textFieldEndereco.setEditable(false);
		textFieldEndereco.setBounds(50, 126, 356, 19);
		textFieldEndereco.setText(endereco);
		getContentPane().add(textFieldEndereco);
		textFieldEndereco.setColumns(10);
		
		JLabel lblAbraOEndereo = new JLabel("Abra o endereço abaixo no Firefox ou no Chrome");
		lblAbraOEndereo.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblAbraOEndereo.setBounds(50, 99, 356, 15);
		getContentPane().add(lblAbraOEndereo);
		
		JButton btnCopiarEndereo = new JButton("Copiar endereço");
		btnCopiarEndereo.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnCopiarEndereo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents(new StringSelection(textFieldEndereco.getText()), null);
			}
		});
		btnCopiarEndereo.setBounds(50, 166, 168, 25);
		getContentPane().add(btnCopiarEndereo);
	}
	
	public static void centreWindow(Window frame) {
	    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	    frame.setLocation(x, y);
	}
}
