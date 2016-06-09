package innoz.gui;

import java.awt.BorderLayout;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextAreaOutputStreamContainer extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7893636213293000634L;
	
	final JTextArea textArea;
	final TextAreaOutputStream taos;
	
	public TextAreaOutputStreamContainer(final JTextArea textArea, final TextAreaOutputStream taos){
		
		this.textArea = textArea;
		this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
		this.textArea.setEditable(false);
		this.taos = taos;
		
		System.setOut(new PrintStream(taos));
		
		this.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setAutoscrolls(true);
		this.add(scrollPane);
		
	}
	
}