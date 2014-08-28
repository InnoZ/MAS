package CreateNetwork;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
//import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import trafo.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.core.config.ConfigUtils;


public class CreateNetwork {

   public static void main(String[] args) {
      String osm = "input/Geodaten/createNetwork/bayern.osm";
      Config config = ConfigUtils.createConfig();
      Scenario sc = ScenarioUtils.createScenario(config);
      Network net = sc.getNetwork();
      CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(
    		  TransformationFactory.WGS84, TransformationFactory.WGS84_UTM32N);
      OsmNetworkReader onr = new OsmNetworkReader(net,ct);
      onr.parse(osm); 
      new NetworkCleaner().run(net);
      new NetworkWriter(net).write("./output/network.xml");
   }
}


