package com.innoz.scenarios.osnabrueck;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.pt.routes.ExperimentalTransitRoute;

public class Ride {
	
	ExperimentalTransitRoute route;
	Person p;
	Activity actFrom; 
	Activity actTo;
	
	Ride (ExperimentalTransitRoute route, Person p, Activity actFrom, Activity actTo){
		this.route = route;
		this.p = p;
		this.actFrom = actFrom;
		this.actTo = actTo;
	}
	
	public ExperimentalTransitRoute getRoute(){
		return this.route;
	}
	public Person getPerson(){
		return this.p;
	}
	public Activity getFromActivity(){
		return this.actFrom;
	}
	public Activity getToActivity(){
		return this.actTo;
	}

}
