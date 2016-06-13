package innoz.gui.actionListeners;

import innoz.gui.MainFrame;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ButtonChangeActionListener implements ActionListener {

	private final MainFrame mainFrame;
	private final JButton button;
	private final JPanel parent;
	
	public ButtonChangeActionListener(final MainFrame mainFrame, final JButton button, final JPanel parent) {
	
		this.mainFrame = mainFrame;
		this.button = button;
		this.parent = parent;
	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String oldKey = this.button.getText().split(", ")[0];
		
		JPanel message = new JPanel();
		message.setLayout(new GridLayout());
		message.add(new JLabel("Id"));
		JTextField id = new JTextField(this.button.getText().split(", ")[0]);
		message.add(id);
		message.add(new JLabel("Number of households"));
		JTextField n = new JTextField(this.button.getText().split(", ")[1]);
		message.add(n);
		
		String[] options = {"Ok", "Cancel"};
		int option = JOptionPane.showOptionDialog(this.mainFrame.getFrame(), message, "Add a new administrative unit", JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
		
		if(option == 0){

			this.mainFrame.getSurveyAreaMap().remove(oldKey);
			
			this.mainFrame.getSurveyAreaMap().put(id.getText(), n.getText());
			
			this.button.setText(id.getText() + ", " + n. getText());
			
			this.parent.revalidate();
			this.mainFrame.getFrame().repaint();
			
		}
		
	}

}
