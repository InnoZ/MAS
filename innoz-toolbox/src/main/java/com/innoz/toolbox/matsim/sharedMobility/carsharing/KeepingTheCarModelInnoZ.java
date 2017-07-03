package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.carsharing.models.KeepingTheCarModel;
import org.matsim.core.gbl.MatsimRandom;

public class KeepingTheCarModelInnoZ implements KeepingTheCarModel {

	@Override
	public boolean keepTheCarDuringNextActivity(double d, Person person,
	        String type) {
		
		if(type.contains("twoway")) return true;
		
		else if(d < 7200d)
			return 	MatsimRandom.getRandom().nextDouble() > d / (2.0 * 3600.0);
		
		return false;
		
	}

}