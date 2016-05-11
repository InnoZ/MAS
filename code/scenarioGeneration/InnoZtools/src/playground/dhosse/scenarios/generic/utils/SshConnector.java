package playground.dhosse.scenarios.generic.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

import playground.dhosse.scenarios.generic.Configuration;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshConnector {

	private static final Logger log = Logger.getLogger(SshConnector.class);
	
	public static void connect(Configuration configuration) throws JSchException, IOException{
		
		log.info("Trying to establish ssh tunnel to mobility database server...");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Enter user name for ssh tunnel: ");
		String sshuser = br.readLine();
		System.out.println("Enter your password: ");
		String sshpassword = br.readLine();
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
	
}