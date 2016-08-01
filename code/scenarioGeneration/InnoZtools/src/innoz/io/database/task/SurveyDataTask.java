package innoz.io.database.task;

import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;

public interface SurveyDataTask extends Task {

	public void run(SurveyDataContainer container);
	
}
