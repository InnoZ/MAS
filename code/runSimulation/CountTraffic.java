package simulation;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
 
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
 
 
public class CountTraffic implements LinkEnterEventHandler {
 
/**
* @param args
*/
Map<Id,Integer> counts = new HashMap<Id,Integer>();
public static void main(String[] args) {
Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
new MatsimNetworkReader(sc).readFile("input/networks/network_bayern.xml");
CountTraffic ct = new CountTraffic();
EventsManager manager = EventsUtils.createEventsManager();
manager.addHandler(ct);
new MatsimEventsReader(manager).readFile("output/ITERS/it.20/20.events.xml.gz");
ct.writeFlow("output/flow.csv");
}
private void writeFlow(String outfileName) {
try{
Writer writer = IOUtils.getBufferedWriter(outfileName);
for (Entry<Id,Integer> countEntry : this.counts.entrySet() ){
writer.write(countEntry.getKey()+"\t"+countEntry.getValue()+"\n");
}
writer.flush();
writer.close();
} catch (IOException e){
e.printStackTrace();
 
}
}
@Override
public void reset(int iteration) {
// TODO Auto-generated method stub
}
@Override
public void handleEvent(LinkEnterEvent event) {
Id linkId = event.getLinkId();
int count = 0;
if (counts.containsKey(linkId)) {
count = counts.get(linkId);
}
count ++;
this.counts.put(linkId, count);
}
 
}