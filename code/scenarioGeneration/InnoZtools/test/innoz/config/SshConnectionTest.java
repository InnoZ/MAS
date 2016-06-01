package innoz.config;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class SshConnectionTest {

	@Test
	public void testSshConnection(){
		
		try {
		
			String sshUser = "innoz";
			String sshPassword = "Winter_2016";
			
			SshConnector.connect(sshUser, sshPassword, 3200, 22);
			
		} catch (JSchException e) {
			
			e.printStackTrace();
			
		}
		
		finally{
			
			assertTrue(SshConnector.session != null);
			
		}
		
	}
	
}
