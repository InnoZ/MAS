package simulation;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

public class MyFirstControler {
	
	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig("/home/yasemin/Dokumente/MatSim/workspace/Garmisch/input/configGarmisch.xml");
		Controler controler = new Controler(config) ;
		controler.setOverwriteFiles(true) ;
		controler.run() ;
	}
	
}
