package playground.dhosse.scenarioGeneration;

import java.awt.GridLayout;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshConnector {

	private static final Logger log = Logger.getLogger(SshConnector.class);
	
	public static void connect(Configuration configuration) throws JSchException, IOException{
		
		log.info("Trying to establish ssh tunnel to mobility database server...");
		
		String[] sshData = PasswordDialog.run();
		String sshuser = sshData[0];
		String sshpassword = sshData[1];
		configuration.setDatabaseUser(sshData[2]);
		configuration.setDatabasePassword(sshData[3]);
		String sshhost = "playground";
		String remoteHost = "localhost";
		int nLocalPort = configuration.getLocalPort();
		int nRemotePort = configuration.getRemotePort();
		
		final JSch jsch = new JSch();
		
		Session session = jsch.getSession(sshuser, sshhost);
		session.setPassword(sshpassword);
		
		final Properties config = new Properties();
	    config.put( "StrictHostKeyChecking", "no" );
	    session.setConfig( config );
	     
	    session.connect();
	    session.setPortForwardingL(nLocalPort, remoteHost, nRemotePort);
	    
	    log.info("Ssh tunnel established.");
		
	}

	private static class PasswordDialog{

		static String[] run(){

			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(3, 1));
			
			JPanel sshPanel = new JPanel();
			JLabel sshUser = new JLabel("Enter your ssh user name:");
			sshPanel.add(sshUser);
			JTextField sshTextField = new JTextField(20);
			sshPanel.add(sshTextField);
			JLabel sshPassword = new JLabel("Enter your ssh password:");
			sshPanel.add(sshPassword);
			JPasswordField sshPasswordField = new JPasswordField(20);
			sshPanel.add(sshPasswordField);
			
			JPanel dbPanel = new JPanel();
			dbPanel.add(new JLabel("Enter your MobilityDatabase user name:"));
			JTextField dbTextField = new JTextField(20);
			dbPanel.add(dbTextField);
			dbPanel.add(new JLabel("Enter your MobilityDatabse password:"));
			JPasswordField dbPasswordField = new JPasswordField(20);
			dbPanel.add(dbPasswordField);
			
			panel.add(sshPanel);
			panel.add(new JSeparator(SwingConstants.HORIZONTAL));
			panel.add(dbPanel);
			
			String[] options = {"Ok","Cancel"};
			int option = JOptionPane.showOptionDialog(null, panel, "User verification", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			
			if(option == 0){
				
				return new String[]{sshTextField.getText(), new String(sshPasswordField.getPassword()), dbTextField.getText(),
						new String(dbPasswordField.getPassword())};
				
			} else return null;
			
		}
		
	}
	
}