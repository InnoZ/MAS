package innoz.gui.actionListeners;

import innoz.config.SshConnector;
import innoz.gui.MainFrame;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jcraft.jsch.JSchException;

public class SshConnectionListener implements ActionListener{
	
	final MainFrame mainFrame;
	final JButton button;
	final JLabel status;
	
	public SshConnectionListener(final MainFrame mainFrame, JButton button, JLabel status){
		
		this.mainFrame = mainFrame;
		this.button = button;
		this.status = status;
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(this.status.getText().equals("<html><font color='red'>Not connected</font></html>")){
			
			boolean established = false;
			
			try {
				
				established = SshConnector.connect(this.mainFrame.getConfiguration());
			
			} catch (JSchException | IOException e1) {

				e1.printStackTrace();
			
			}
			
			if(established){
				
				this.status.setText("<html><font color='green'>Connected</font></html>");
				this.button.setText("Disconnect");
				this.mainFrame.enableComponents();
				
			} else {
				
				JPanel message = new JPanel();
				message.setPreferredSize(new Dimension(400,50));
				message.add(new JLabel("Could not connect to MobilityDataHub!"));
				message.add(new JLabel("Did you enter correct user names and passwords?"));
				
				JOptionPane.showConfirmDialog(this.mainFrame.getFrame(), message, "Error!", JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE);
				
			}
			
		} else {
	
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(300,50));
			panel.add(new JLabel("Disconnect from MobilityDataHub?"));
			String[] options = {"Yes","No"};
				
			int option = JOptionPane.showOptionDialog(this.mainFrame.getFrame(), panel, "",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			
			if(option == 0){

				SshConnector.disconnect();
				this.status.setText("<html><font color='red'>Not connected</font></html>");
				this.button.setText("Connect");
				this.mainFrame.disableComponents();
				
			}
			
		}
		
	}

}
