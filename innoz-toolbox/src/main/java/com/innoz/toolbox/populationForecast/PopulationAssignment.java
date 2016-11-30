package com.innoz.toolbox.populationForecast;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.utils.data.Tree.Node;

public class PopulationAssignment {

	public void run(Configuration configuration, Geoinformation geoinformation) throws IOException, SQLException{

		for(Node<AdministrativeUnit> node : geoinformation.getAdminUnits()){

			if(node.getData().getId().length() < 6){
				
				HashMap<String, Integer> map = RunCalculationWithLessQueries.run(node.getData().getId().substring(1), configuration.scenario().getYear());
				node.getData().setPopulationMap(map);
				
			}
			
		}
		
	}

}
