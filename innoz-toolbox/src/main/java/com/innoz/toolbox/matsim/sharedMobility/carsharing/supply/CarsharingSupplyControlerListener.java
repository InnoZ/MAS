package com.innoz.toolbox.matsim.sharedMobility.carsharing.supply;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.matsim.contrib.carsharing.config.CarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.OneWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.TwoWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;

import com.innoz.toolbox.scenarioGeneration.carsharing.CarsharingVehiclesWriter;

/**
 * 
 * A controller listener that adapts the carsharing supply between iterations.
 * The idea is to take the demand side events of the last <code>p</code> iterations where <code>p</code> is a 'smoothing' parameter
 * to avoid oscillations. From these, we find out which vehicles have not been rent and are thus to be removed. Also, new stations /
 * vehicles are created at places where no vehicle was available.<br>
 * 
 * This is a very basic first draft version! Elementary switches are not yet configurable from outside the code and test runs have
 * to be executed to evaluate whether the code is working (at all and the way we want) or not. Eventually, we will make this a MATSim 
 * config group. /dhosse 07/17
 * 
 * @author dhosse
 *
 */
public class CarsharingSupplyControlerListener implements StartupListener, IterationEndsListener, ShutdownListener {

	static final Logger log = Logger.getLogger(CarsharingSupplyControlerListener.class);
	
	@Inject CarsharingSupplyInterface container;
	CarsharingSupplyEventHandler handler;
	CarsharingConfigGroup csConfig;
	TwoWayCarsharingConfigGroup twConfig;
	OneWayCarsharingConfigGroup owConfig;
	
	final int averageOverIterations = 5;
	final int lastIteration = 20;
	final int threshold = 1;
	
	public CarsharingSupplyControlerListener(CarsharingSupplyEventHandler handler, CarsharingConfigGroup carsharing,
			TwoWayCarsharingConfigGroup twoway, OneWayCarsharingConfigGroup oneway) {
		
		this.handler = handler;
		this.csConfig = carsharing;
		this.twConfig = twoway;
		this.owConfig = oneway;
		
	}
	
	@Override
	public void notifyStartup(StartupEvent event) {
		
	}
	
	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		
		if(event.getIteration() > 0 && event.getIteration() % this.averageOverIterations == 0) {

			log.info("Removing idle vehicles...");
			
			// first, remove all vehicles that were idle during the last simulation period
			CarsharingSupplyAdaptation.removeIdleVehicles(this.container.getAllVehicles(), this.container.getAllVehicleLocations(),
					this.handler.bookedVehicles, this.threshold);
			
			if(event.getIteration() <= this.lastIteration) {
				
				log.info("Adding new vehicles to the fleet...");
				
				// second, add one vehicle to each location where persons got stuck because of absent vehicles
				CarsharingSupplyAdaptation.addNewVehicles(event.getServices().getScenario().getNetwork(), this.container.getAllVehicles(),
						this.container.getAllVehicleLocations(), this.handler.stuckEventsCausedByNoCsVeh, this.averageOverIterations);
			}

			// write output
			String filename = event.getServices().getControlerIO().getIterationFilename(event.getIteration(), event.getIteration() + 
					".csVCehicles.xml");
			new CarsharingVehiclesWriter().write(this.container.getAllVehicleLocations(), filename);
			this.csConfig.setvehiclelocations(filename);
			this.twConfig.setvehiclelocations(filename);
			
		}
		
	}

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		
	}
	
}