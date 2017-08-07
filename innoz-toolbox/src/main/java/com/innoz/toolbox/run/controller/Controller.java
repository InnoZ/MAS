package com.innoz.toolbox.run.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.ConfigurationUtils;
import com.innoz.toolbox.run.controller.task.ConfigCreatorTask;
import com.innoz.toolbox.run.controller.task.ControllerTask;
import com.innoz.toolbox.run.controller.task.DemandGenerationTask;
import com.innoz.toolbox.run.controller.task.NetworkGenerationTask;
import com.innoz.toolbox.run.controller.task.ReadGeodataTask;
import com.innoz.toolbox.run.controller.task.WriteOutputTask;

/**
 * 
 * A class that connects the front-end of the MAS software with the back-end. The basic use is that of a static access class, meaning
 * there can only be one controller at a time resulting in one JVM running per scenario generation task.<br>
 * The controller contains an InnoZ configuration with specific information about the scenario generation task and MATSim data
 * structures that store the information generated during this task.<br>
 * Basically, the controller works task-oriented. Each work step of the process is represented by a <code>ControllerTask</code> (e.g.
 * read geodata, read network data, generate demand etc.). These processes are stored inside a queue and subsequently executed (in a
 * specific predefined order). The controller and thus the JVM runs as long as there is another task to be executed.
 * 
 * @author dhosse
 *
 */
public final class Controller {

	// The task queue
	private static final BlockingQueue<ControllerTask> queue = new ArrayBlockingQueue<>(15, true);
	
	static final Logger log = Logger.getLogger(Controller.class);
	
	// InnoZ
	private static Configuration configuration = ConfigurationUtils.createConfiguration();
	
	// MATSim
	private static Config config = ConfigUtils.createConfig();
	private static Scenario scenario = ScenarioUtils.createScenario(Controller.config);
	
	// Non-instantiable
	private Controller(){};
	
	/**
	 * 
	 * Executes the tasks stored inside the controller task queue subsequently.
	 * 
	 * @param configuration The scenario generation configuration holding all the information about the process.
	 */
	public static void run(String scenarioName, String railsEnvironment) {
		
		log.info("Starting scenario generation...");
	
		// Add the tasks that have to be executed regardless of the actual goal of the call
		addMandatoryTasks(scenarioName, railsEnvironment);
		
		// Create a new thread that runs until every ControllerTask has been executed
		Thread t = new Thread(() -> {
			
			while(!queue.isEmpty()) {

				try {
					
					// Take the next task from the queue and execute it
					ControllerTask next = queue.take();
					
					log.info("Executing task " + next.toString());
					
					next.run();
				
				} catch (InterruptedException e) {

					log.error(e.getMessage());
					
				}
				
			}
			
		});
		
		// Start the thread
		t.start();
		
	}
	
	/**
	 * 
	 * Adds tasks that need to be executed regardless of the execution context.<br>
	 * Maybe discard this method or put ALL the tasks in here because there isn't any user interaction on this level. //dhosse 08/17
	 * 
	 */
	private static void addMandatoryTasks(String scenarioName, String railsEnvironment) {
		
		queue.add(new ReadGeodataTask.Builder().build());
		queue.add(new NetworkGenerationTask.Builder(configuration, scenario).build());
		queue.add(new DemandGenerationTask.Builder(Controller.configuration(), Controller.scenario()).build());
		queue.add(new ConfigCreatorTask.Builder(scenario).build());
		queue.add(new WriteOutputTask.Builder(scenarioName, railsEnvironment).build());
		
	}
	
	/**
	 * 
	 * Getter method for the scenario generation configuration (InnoZ).
	 * 
	 * @return The configuration class with all the information for the scenario generation process.
	 */
	public static final Configuration configuration() {
		
		return Controller.configuration;
		
	}
	
	/**
	 * 
	 * Getter method for the MATSim scenario object.
	 * 
	 * @return The MATSim scenario (to be) generated during the controller execution.
	 */
	public static final Scenario scenario() {
		
		return Controller.scenario;
		
	}
	
}