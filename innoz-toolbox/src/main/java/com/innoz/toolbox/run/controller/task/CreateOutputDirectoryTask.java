package com.innoz.toolbox.run.controller.task;

import java.io.File;
import java.io.IOException;

import org.matsim.core.controler.OutputDirectoryLogging;

public class CreateOutputDirectoryTask implements ControllerTask {

	private String outputDirectory;
	
	private CreateOutputDirectoryTask(Builder builder){
		
		this.outputDirectory = builder.outputDirectory;
		
	};
	
	@Override
	public void run() {

		// Dump scenario generation settings on the console and create the output directory
		new File(this.outputDirectory).mkdirs();
		
		try {
		
			OutputDirectoryLogging.initLoggingWithOutputDirectory(this.outputDirectory);
		
		} catch (IOException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
	public static class Builder {
		
		String outputDirectory;
		
		public Builder(String outputDirectory) {
			
			this.outputDirectory = outputDirectory;
			
		}
		
		public CreateOutputDirectoryTask build() {
			
			return new CreateOutputDirectoryTask(this);
			
		}
		
	}

}