package com.innoz.toolbox.run.controller.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.matsim.core.controler.OutputDirectoryLogging;

public class CreateOutputDirectoryTask implements ControllerTask {

	private String outputDirectory;
	
	private CreateOutputDirectoryTask(Builder builder){
		
		this.outputDirectory = builder.outputDirectory;
		
	};
	
	@Override
	public void run() {

		try {
			
			// Dump scenario generation settings on the console and create the output directory		
			Files.createDirectories(Paths.get(outputDirectory));
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