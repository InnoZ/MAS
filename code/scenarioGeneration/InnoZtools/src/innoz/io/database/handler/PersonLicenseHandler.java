package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonLicenseHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String license = attributes.get("hp_pkwfs");
		
		if(license.equals("1")){
			person.setHasLicense(true);
		} else {
			person.setHasLicense(false);
		}
		
	}

}
