package innoz.io.database;

import innoz.config.Configuration;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;

public interface DemandDatabaseParser {

	public void run(Configuration configuration, SurveyDataContainer container, Geoinformation geoinformation);
	
}
