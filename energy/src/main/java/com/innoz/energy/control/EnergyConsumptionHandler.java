package com.innoz.energy.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.utils.misc.Time;

import com.innoz.energy.config.EnergyConsumptionConfigGroup;
import com.innoz.energy.io.EnergyConsumptionStatsWriter;

public class EnergyConsumptionHandler implements ActivityStartEventHandler, ActivityEndEventHandler, AfterMobsimListener {

	MatsimServices controler;
	EnergyConsumptionConfigGroup config;
	
	Map<Id<Person>, List<EnergyLog>> consumptionLog = new HashMap<>();
	double[] totalConsumptionPerHour = new double[24];
	EnergyConsumptionStatsWriter writer = new EnergyConsumptionStatsWriter();
	
	@Inject
	public EnergyConsumptionHandler(MatsimServices controler) {
	
		this.controler = controler;
		this.config = (EnergyConsumptionConfigGroup)
				controler.getConfig().getModule(EnergyConsumptionConfigGroup.GROUP_NAME);
		
		if(this.config == null)
			throw new RuntimeException("No config group of type " + 
					EnergyConsumptionConfigGroup.GROUP_NAME + " found! Aborting!");
		
	}
	
	@Override
	public void reset(int iteration) {
		
		this.totalConsumptionPerHour = new double[24];
		this.consumptionLog = new HashMap<>();
		
		for(Person person : this.controler.getScenario().getPopulation().getPersons().values()){
			
			this.consumptionLog.put(person.getId(), new ArrayList<>());
			
		}
		
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		
		Id<Person> personId = event.getPersonId();
		
		if(this.consumptionLog.containsKey(personId)){
			
			if(this.consumptionLog.get(personId).size() > 0){
				
				this.consumptionLog.get(personId).get(this.consumptionLog.get(personId).size()-1).endTime = event.getTime();
				
			} else if(this.config.getEnergyConsumptionParams(event.getActType()) != null){
				
				EnergyLog log = new EnergyLog(personId, 0, event.getActType());
				log.endTime = event.getTime();
				this.consumptionLog.get(personId).add(log);
				
			}
			
		}
		
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		
		Id<Person> personId = event.getPersonId();
		
		if(this.consumptionLog.containsKey(personId)){
			
			String actType = event.getActType();
			
			if(this.config.getEnergyConsumptionParams(actType) != null){

				this.consumptionLog.get(personId).add(new EnergyLog(
						personId, event.getTime(), event.getActType()));
				
			}

		}
		
	}
	
	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		
		Map<Id<Person>, Double> personId2EnergyConsumption = new HashMap<>();
		
		if(this.config.getWriterFrequency() != 0 && event.getIteration() % this.config.getWriterFrequency() == 0){
		
			for(Id<Person> personId : this.consumptionLog.keySet()){
	
				double consumption = 0.0;
				
				for(EnergyLog log : this.consumptionLog.get(personId)){
					
					if(log.endTime == 0) log.endTime = 30 * 3600;
					
					double c = calcEnergyConsumption(log.startTime, log.endTime, log.actType);
					consumption += c;
					
					int start = (int)(log.startTime/3600);
					int end = (int)(log.endTime/3600);
					int i = start;

					while(i <= end && i < this.totalConsumptionPerHour.length){
						
						int dur = end - start > 0 ? end - start : 1;
						this.totalConsumptionPerHour[i] += (c / dur);
						i++;
						
					}
						
				}
				
				personId2EnergyConsumption.put(personId, consumption);
				
			}
			
			this.writer.writePersonBasedStats(event.getServices().getControlerIO().getIterationFilename(event.getIteration(),
					"personEnergyConsumption.txt"), personId2EnergyConsumption);
			this.writer.writeAggregatedStatsPerHour(event.getServices().getControlerIO().getIterationFilename(event.getIteration(),
					"aggregatedConsumption.txt"), this.totalConsumptionPerHour);
		
		}
		
	}
	
	private double calcEnergyConsumption(double start, double end, String actType){
		
		double param = this.config.getEnergyConsumptionParams(actType).getEnergyConsumptionInKiloWattHours();
		
		return (end - start) * param / 3600;
		
	}

	static class EnergyLog implements Comparable<EnergyLog>{
		
		Id<Person> personId;
		double startTime;
		double endTime;
		String actType;
		
		EnergyLog(Id<Person> personId, double startTime, String actType){
			this.personId = personId;
			this.startTime = startTime;
			this.actType = actType;
		}
		
		@Override
		public int compareTo(EnergyLog o) {
			
			return Double.compare(this.startTime, o.startTime);
			
		}
		
		@Override
		public String toString(){
			
			return ("[from: " + Time.writeTime(startTime) + ", to: " + Time.writeTime(endTime) + ", type: " + actType + "]");
			
		}
		
	}

}