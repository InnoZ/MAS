package playground.dhosse.scenarioGeneration.utils;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.matsim.core.gbl.MatsimRandom;

import playground.dhosse.utils.matsim.RecursiveStatsContainer;

public class Hydrograph {

	private final String activityType;
	private final TreeMap<Double, Double> sharePerHour;
	
	double totalDepartures = 0;
	
	RecursiveStatsContainer durationStats;
	
	public Hydrograph(String activityType){
		
		this.activityType = activityType;
		this.sharePerHour = new TreeMap<>();
		this.durationStats = new RecursiveStatsContainer();
		
	}
	
	public String getActivityType(){
		return this.activityType;
	}
	
	public void handleEntry(double hour, double nDepartures){
		
		if(!this.sharePerHour.containsKey(hour)){
			
			this.sharePerHour.put(hour, nDepartures);
			
		} else {
			
			double departures = nDepartures + this.sharePerHour.get(hour);
			this.sharePerHour.put(hour, departures);
			
		}
		
		this.totalDepartures += nDepartures;
		
	}
	
	public void handleDurationEntry(double duration){
		this.durationStats.handleNewEntry(duration);
	}
	
	public double getDepartureTime(){
		
		double random = MatsimRandom.getLocalInstance().nextDouble();
		double accumulatedWeight = 0;
		
		double departureTime = 0;
		
		for(Entry<Double, Double> entry : this.sharePerHour.entrySet()){
		
			accumulatedWeight += entry.getValue() / this.totalDepartures;
			
			if(random <= accumulatedWeight){
				
				departureTime = entry.getKey() * 3600 + MatsimRandom.getLocalInstance().nextInt(3600);
				break;
						
			}
			
		}
		
		return departureTime;
		
	}
	
	public double getDurationInSec(){
		
		return this.durationStats.getMin() + MatsimRandom.getLocalInstance().nextInt((int)(this.durationStats.getMax() - this.durationStats.getMin()));
		
	}
	
}
