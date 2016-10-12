package com.innoz.toolbox.config;

import org.junit.Ignore;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class SshConnectionTest {

	/**
	 * 
	 * Tests the {@code SshConnector} for exceptions.
	 * 
	 * @throws JSchException If any problem with the ssh connection should occur, e.g.
	 * wrong host name, wrong user name or password.
	 */
	@Test
	@Ignore
	public void testSshConnectionForException() throws JSchException{
		
			String sshUser = "innoz";
			String sshPassword = "Winter2015!";
			
			SshConnector.connect(sshUser, sshPassword, 3200, 22);
		
	}
	
}
