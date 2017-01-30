package com.innoz.toolbox.config;

import java.util.HashMap;
import java.util.Map;

import com.innoz.toolbox.config.groups.ConfigurationGroup;
import com.innoz.toolbox.config.groups.MiscConfigurationGroup;
import com.innoz.toolbox.config.groups.NetworkConfigurationGroup;
import com.innoz.toolbox.config.groups.PsqlConfigurationGroup;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup;
import com.innoz.toolbox.config.groups.TracksConfigurationGroup;

/**
 * 
 * Class that holds all relevant parameters for the semi-automatic generation of a MATSim pre-base scenario.
 * 
 * @author dhosse
 *
 */
public final class Configuration {
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private MiscConfigurationGroup misc;
	private NetworkConfigurationGroup network;
	private PsqlConfigurationGroup psql;
	private ScenarioConfigurationGroup scenario;
	private SurveyPopulationConfigurationGroup surveyPopulation;
	private TracksConfigurationGroup tracks;
	private Map<String,ConfigurationGroup> groups = new HashMap<>();
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * 
	 * Creates a new configuration and sets its parameters according to what's defined in the given file.
	 * 
	 * @param file Text file containing the configuration parameters.
	 */
	Configuration(String file) {
		
		this();
		this.load(file);
		
	}
	
	/**
	 * 
	 * Creates an empty (i.e. only default values) configuration.
	 * 
	 */
	Configuration() {
		
		this.misc = new MiscConfigurationGroup();
		this.groups.put("misc", misc);
		this.network = new NetworkConfigurationGroup();
		this.groups.put("network", network);
		this.psql = new PsqlConfigurationGroup();
		this.groups.put("psql", psql);
		this.scenario = new ScenarioConfigurationGroup();
		this.groups.put("scenario", scenario);
		this.surveyPopulation = new SurveyPopulationConfigurationGroup();
		this.groups.put("surveyPopulation", surveyPopulation);
		this.tracks = new TracksConfigurationGroup();
		this.groups.put("tracksPopulation", tracks);
	
	}
	
	/**
	 * 
	 * Loads the configuration object by reading in data from an existing configuration file (*.xml).
	 * 
	 * @param file The configuration file to load.
	 */
	void load(String file) {
		
		new ConfigurationReaderXml(this).read(file);
		
	}
	
	public final MiscConfigurationGroup misc() {
		
		return this.misc;
		
	}
	
	public final NetworkConfigurationGroup network() {

		return this.network;
		
	}
	
	public final PsqlConfigurationGroup psql() {
		
		return this.psql;
		
	}
	
	public final ScenarioConfigurationGroup scenario() {
		
		return this.scenario;
		
	}
	
	public final SurveyPopulationConfigurationGroup surveyPopulation() {
		
		return this.surveyPopulation;
		
	}
	
	public final TracksConfigurationGroup tracks() {
		
		return this.tracks;
		
	}
	
	public final ConfigurationGroup getModule(String name) {
		
		return this.groups.get(name);
		
	}
	
}