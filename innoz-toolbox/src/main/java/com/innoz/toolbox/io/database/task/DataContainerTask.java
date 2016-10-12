package com.innoz.toolbox.io.database.task;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;

public interface DataContainerTask extends Task {

	public void apply(SurveyDataContainer container);
	
}
