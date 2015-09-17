package countTraffic;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;

public class CreateCalibrationLinks {

	private Map<Id, Integer> calibrationLinks = new HashMap<Id, Integer>();

	public void run(){
		createLinkMap();
	}
	private void createLinkMap() {

		Id id1 = new IdImpl(27701);
		this.calibrationLinks.put(id1, 4500);
		Id id2 = new IdImpl(27697);
		this.calibrationLinks.put(id2, 11800);
		Id id3 = new IdImpl(36019);
		this.calibrationLinks.put(id3, 6500);
		Id id4 = new IdImpl(20481);
		this.calibrationLinks.put(id4, 850);
		Id id5 = new IdImpl(44873);
		this.calibrationLinks.put(id5, 8250);
		Id id6 = new IdImpl(15244);
		this.calibrationLinks.put(id6, 3100);
		Id id7 = new IdImpl(20842);
		this.calibrationLinks.put(id7, 11000);
		Id id8 = new IdImpl(45977);
		this.calibrationLinks.put(id8, 4000);
		Id id9 = new IdImpl(30679);
		this.calibrationLinks.put(id9, 11950);
	}

	public Map<Id, Integer> getCalibrationLinks() {
		return this.calibrationLinks;
	}

	public static void main(String[] args) {
		CreateCalibrationLinks cc = new CreateCalibrationLinks();
		cc.run();
		System.out.println("Relevant Links For Calibration: \n" + cc.getCalibrationLinks().keySet().toString());
	}

}
