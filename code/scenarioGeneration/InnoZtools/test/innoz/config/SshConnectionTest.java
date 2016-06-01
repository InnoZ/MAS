package innoz.config;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class SshConnectionTest {

	@Test
	public void testSshConnection(){
		
		try {
		
			String sshUser = "innoz";
			String sshPassword = "Winter2015";
			
			SshConnector.connect(sshUser, sshPassword, 3200, 22);
			
			assertTrue(SshConnector.session.getServerAliveCountMax() != 0);
			
		} catch (JSchException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
}
