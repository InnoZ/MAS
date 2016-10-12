package com.innoz.energy.control;

import org.matsim.core.controler.AbstractModule;

public final class EnergyConsumptionModule extends AbstractModule {

	@Override
	public void install() {
		
		bind(EnergyConsumptionHandler.class).asEagerSingleton();
		addEventHandlerBinding().to(EnergyConsumptionHandler.class);
		addControlerListenerBinding().to(EnergyConsumptionHandler.class);

	}

}