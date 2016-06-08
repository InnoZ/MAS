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
		this.taos = taos;
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		System.setOut(new PrintStream(taos));
		
	}
	
}