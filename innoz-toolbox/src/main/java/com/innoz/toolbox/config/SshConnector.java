package com.innoz.toolbox.config;

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

import jline.console.ConsoleReader;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * 
 * Class that uses the jCraft library to establish a secure shell (ssh) connection to a remote server (in our case the MobilityDatahub
 * server).
 * 
 * @author dhosse
 *
 */
public class SshConnector {

	private static final Logger log = Logger.getLogger(SshConnector.class);
	static Session session;
	
	private static boolean connected = false;
	
	public static boolean connect(Configuration configuration) throws JSchException, IOException{
		
		log.info("Trying to establish ssh tunnel to mobility database server...");
		
		// Run the swing dialog so the user can enter his / her data
		String[] sshData = PasswordDialog.run();
		// Set user names and passwords for ssh and database
		String sshuser = sshData[0];
		String sshpassword = sshData[1];
		configuration.psql().setPsqlUser(sshData[2]);
		configuration.psql().setPsqlPassword(sshData[3]);
		
		// Set hosts and ports for the connection
		String sshhost = "playground";
		String remoteHost = "localhost";
		int nLocalPort = configuration.psql().getPsqlPort();
		int nRemotePort = 5432;
		
		final JSch jsch = new JSch();

		// Start a new session with the predefined settings
		session = jsch.getSession(sshuser, sshhost);
		session.setPassword(sshpassword);
		
		final Properties config = new Properties();
	    config.put( "StrictHostKeyChecking", "no" );
	    session.setConfig( config );
	    
	    session.connect();
	    session.setPortForwardingL(nLocalPort, remoteHost, nRemotePort);
	    
	    log.info("Ssh tunnel established.");
		
	    connected = true;
	    
	    return session != null;
	    
	}
	
	static void connect(String sshUser, String sshPassword, int localPort, int remotePort) throws JSchException{
		
		// Set hosts and ports for the connection
		String sshhost = "playground";
		String remoteHost = "localhost";
		int nLocalPort = localPort;
		int nRemotePort = remotePort;
		
		final JSch jsch = new JSch();

		// Start a new session with the predefined settings
		session = jsch.getSession(sshUser, sshhost);
		session.setPassword(sshPassword);
		
		final Properties config = new Properties();
	    config.put( "StrictHostKeyChecking", "no" );
	    session.setConfig( config );
	    
	    session.connect();
	    session.setPortForwardingL(nLocalPort, remoteHost, nRemotePort);
	    
	}
	
	public static void disconnect(){
		
		log.info("Closing existing ssh connection...");
		session.disconnect();
		
		connected = false;
		
	}
	
	public static boolean connectShell(Configuration configuration, ConsoleReader reader) throws JSchException, IOException{
		
		log.info("Trying to establish ssh tunnel to mobility database server...");
		
		// Set user names and passwords for ssh and database
		String sshuser = reader.readLine("> Enter ssh user name: ");
		String sshpassword = new String(reader.readLine("> Enter ssh password: ", new Character('*')));
		
		// Set hosts and ports for the connection
		String sshhost = "playground";
		String remoteHost = "localhost";
		int nLocalPort = configuration.psql().getPsqlPort();
		int nRemotePort = 5432;
		
		final JSch jsch = new JSch();

		// Start a new session with the predefined settings
		session = jsch.getSession(sshuser, sshhost);
		session.setPassword(sshpassword);
		
		final Properties config = new Properties();
	    config.put( "StrictHostKeyChecking", "no" );
	    session.setConfig( config );
	    
	    session.connect();
	    session.setPortForwardingL(nLocalPort, remoteHost, nRemotePort);
	    
	    log.info("Ssh tunnel established.");
	    
	    connected = true;
		
	    return session != null;
		
	}
	
	public static void setDbUserData(Configuration configuration, ConsoleReader reader) throws IOException{
		configuration.psql().setPsqlUser(reader.readLine("> Enter database user name: "));
		configuration.psql().setPsqlPassword(new String(reader.readLine("> Enter database password: ", new Character('*'))));
//		if(!connected){
//			ConfigurationUtils.set(configuration, Configuration.LOCAL_PORT, 5432);
//		}
	}

	/**
	 * 
	 * Utility class that runs an OptionDialog.
	 * 
	 * @author dhosse
	 *
	 */
	private static class PasswordDialog{

		/**
		 * 
		 * Runs the OptionDialog to retrieve ssh and database user data.
		 * 
		 * @return An array of four strings (ssh user name, ssh password, database user name, database password)
		 */
		private static String[] run(){

			// Create a new panel as "background" and divide it into three columns and one row
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(3, 1));
			
			// Create a panel for ssh fields
			JPanel sshPanel = new JPanel();
			JLabel sshUser = new JLabel("Enter your ssh user name:");
			sshPanel.add(sshUser);
			JTextField sshTextField = new JTextField(20);
			sshPanel.add(sshTextField);
			JLabel sshPassword = new JLabel("Enter your ssh password:");
			sshPanel.add(sshPassword);
			JPasswordField sshPasswordField = new JPasswordField(20);
			sshPanel.add(sshPasswordField);
			
			// Create a panel for database fields
			JPanel dbPanel = new JPanel();
			dbPanel.add(new JLabel("Enter your MobilityDatabase user name:"));
			JTextField dbTextField = new JTextField(20);
			dbPanel.add(dbTextField);
			dbPanel.add(new JLabel("Enter your MobilityDatabse password:"));
			JPasswordField dbPasswordField = new JPasswordField(20);
			dbPanel.add(dbPasswordField);
			
			// Add the ssh and database panels to the first panel and divide them with a horizontal line
			panel.add(sshPanel);
			panel.add(new JSeparator(SwingConstants.HORIZONTAL));
			panel.add(dbPanel);
			
			// Initialize the option dialog and add the panel
			String[] options = {"Ok","Cancel"};
			int option = JOptionPane.showOptionDialog(null, panel, "User verification", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			
			// If "Ok" has been pressed, return the input data
			if(option == 0){
				
				return new String[]{sshTextField.getText(), new String(sshPasswordField.getPassword()), dbTextField.getText(),
						new String(dbPasswordField.getPassword())};
				
			} else return null;
			
		}
		
	}
	
}