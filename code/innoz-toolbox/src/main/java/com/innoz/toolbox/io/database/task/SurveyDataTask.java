package com.innoz.toolbox.io.database.task;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;

public interface SurveyDataTask extends Task {

	public void run(SurveyDataContainer container);
	
}
