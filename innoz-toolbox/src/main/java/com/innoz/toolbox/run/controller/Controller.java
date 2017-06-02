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
import com.innoz.toolbox.run.controller.task.ControllerTask;
import com.innoz.toolbox.run.controller.task.CreateOutputDirectoryTask;
import com.innoz.toolbox.run.controller.task.ReadGeodataTask;

/**
 * 
 * @author dhosse
 *
 */
public final class Controller {

	// The task queue
	private static final BlockingQueue<ControllerTask> queue = new ArrayBlockingQueue<>(15, true);
	
	private static final BlockingQueue<ControllerTask> queueBuffer = new ArrayBlockingQueue<>(15, true);
	
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
	 * Adds a new task at the end of the task queue.
	 * 
	 * @param r The {@link ControllerTask} to be submitted.
	 * @return
	 */
	public static boolean submit(ControllerTask r) {
		
		return queueBuffer.add(r);
		
	}

	/**
	 * 
	 * @param configuration The scenario generation configuration
	 */
	public static void run() {
		
		log.info("Starting scenario generation...");
	
		// Add the tasks that have to be executed regardless of the actual goal of the call
		addMandatoryTasks();
		queue.addAll(queueBuffer);
		
		// Create a new thread that runs until every ControllerTask has been executed
		Thread t = new Thread(() -> {
			
			while(!queue.isEmpty()) {

				try {
					
					// Take the next task from the queue and execute it
					ControllerTask next = queue.take();
					
					log.info("Executing task " + next.toString());
					
					next.run();
				
				} catch (InterruptedException e) {

					e.printStackTrace();
					
				}
				
			}
			
		});
		
		// Start the thread
		t.start();
		
	}
	
	private static void addMandatoryTasks() {
		
		queue.add(new CreateOutputDirectoryTask.Builder(Controller.configuration.misc().getOutputDirectory()).build());
		queue.add(new ReadGeodataTask.Builder().build());
		
	}
	
	public static final Configuration configuration() {
		
		return Controller.configuration;
		
	}
	
	public static final Scenario scenario() {
		
		return Controller.scenario;
		
	}
	
}