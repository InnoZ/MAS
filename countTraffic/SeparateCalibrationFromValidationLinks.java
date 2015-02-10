package countTraffic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;

public class SeparateCalibrationFromValidationLinks {

	private Random random = new Random();

	/*
	 * map which contains linkIds for that traffic has been counted.
	 */
	private Map<Id, Integer> allLinks;
	private Map<Id, Integer> calibrationLinks;
	private Map<Id, Integer> validationLinks;
	private ArrayList<Integer> orderOfLinks;

	public Map<Id, Integer> getCalibrationLinks() {
		return this.calibrationLinks;
	}

	public Map<Id, Integer> getValidationLinks() {
		return validationLinks;
	}

	public void run() {
		ReadTrafficVolume rtv = new ReadTrafficVolume();
		rtv.readData();
		Map<Id, Integer> LinkIds = rtv.getVolume();
		ArrayList<Integer> order = rtv.getOrder();
		this.allLinks = new HashMap<Id, Integer>();
		this.allLinks.putAll(LinkIds);
		this.orderOfLinks = order;
		separateCalibrationAndValidationLinks();
	}

	private void separateCalibrationAndValidationLinks() {
		/*
		 * initialize linkId-Maps for calibration/validation
		 */
		this.calibrationLinks = new HashMap<Id, Integer>();
		this.calibrationLinks.putAll(this.allLinks);
		this.validationLinks = new HashMap<Id, Integer>();

		boolean calibration = true;
		int indexForValidation = 0;
		int mapValue = 0;
		/*
		 * iterate over LinkIds and split Ids into two maps -> calibrationLinks and
		 * ValidationLinks
		 */
		for (int i = 0; i < this.orderOfLinks.size(); i += 2) {
			calibration = this.random.nextBoolean();
			if (!calibration) {
				indexForValidation = i;
			} else {
				indexForValidation = i + 1;
			}
			Id id = new IdImpl(orderOfLinks.get(indexForValidation));
			mapValue = calibrationLinks.get(id);
			this.validationLinks.put(id, mapValue);
			calibrationLinks.remove(id);
		}

	}

	public static void main(String[] args) {
		SeparateCalibrationFromValidationLinks cv = new SeparateCalibrationFromValidationLinks();
		cv.run();

		System.out.println("AllLinks");
		System.out.println(cv.allLinks.keySet().toString() + "\n");
		System.out.println("CalibrationLinks");
		System.out.println(cv.calibrationLinks.keySet().toString() + "\n");
		System.out.println("ValidationLinks");
		System.out.println(cv.validationLinks.keySet().toString() + "\n");
	}

}
