package innoz.io.database.task;

import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;

public interface DataContainerTask extends Task {

	public void apply(SurveyDataContainer container);
	
}
