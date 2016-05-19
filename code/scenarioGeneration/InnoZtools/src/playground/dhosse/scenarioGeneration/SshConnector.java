package playground.dhosse.scenarioGeneration.utils;

import java.io.IOException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import playground.dhosse.scenarioGeneration.Configuration;

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
			JLabel user = new JLabel("Enter your ssh user name:");
			JTextField textField = new JTextField(20);
			JLabel label = new JLabel("Enter your ssh password:");
			JPasswordField pwField = new JPasswordField(20);
			panel.add(user);
			panel.add(textField);
			panel.add(label);
			panel.add(pwField);
			String[] options = {"Ok","Cancel"};
			int option = JOptionPane.showOptionDialog(null, panel, "Password, please", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			
			if(option == 0){
				
				return new String[]{textField.getText(), new String(pwField.getPassword())};
				
			} else return null;
			
		}
		
	}
	
}