package com.innoz.toolbox.run.callibration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

public class RememberModeStats implements IterationEndsListener{

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		
		if(event.getIteration() == event.getServices().getConfig().controler().getLastIteration()){
			
			Map<String, Integer> legModeCount = new HashMap<String, Integer>();
			event.getServices().getScenario().getPopulation().getPersons().values().stream().map(Person::getSelectedPlan).forEach(plan -> {
				Iterator<PlanElement> it = plan.getPlanElements().iterator();
				while (it.hasNext()){
					PlanElement next = it.next();
					
					if(next instanceof Leg){
						
						String mode = ((Leg) next).getMode();
						
						if (!mode.equalsIgnoreCase(TransportMode.access_walk) || !mode.equalsIgnoreCase(TransportMode.egress_walk)){
				
							if(!legModeCount.containsKey(mode)){
								
								legModeCount.put(mode, 1);
								
							} else {
								
								legModeCount.put(mode, legModeCount.get(mode)+1);
								
							}
							
						}
						
					}
				}
			});
			
			//calculate sum of all legs
			int sumLegs = 0;
			for (Entry<String, Integer> e: legModeCount.entrySet()){
				sumLegs += e.getValue();
			}
			
			//calculate modal shares
			Map<String, Double> modalSplit = new HashMap<String, Double>();
			for (Entry<String, Integer> e: legModeCount.entrySet()){
				modalSplit.put(e.getKey(), (double)(e.getValue())/(double)(sumLegs));
			}
			
			ASCModalSplitCallibration.updateModalSplit(modalSplit);
			
		}

	}

}
