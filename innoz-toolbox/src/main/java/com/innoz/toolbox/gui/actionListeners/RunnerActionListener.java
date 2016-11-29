package com.innoz.toolbox.gui.actionListeners;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.Configuration.AdminUnitEntry;
import com.innoz.toolbox.config.ConfigurationUtils;
import com.innoz.toolbox.gui.GuiConstants;
import com.innoz.toolbox.gui.MainFrame;
import com.innoz.toolbox.run.controller.ScenarioGenerationController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class RunnerActionListener implements ActionListener, Runnable {
	
	private final MainFrame mainFrame;
	
	public RunnerActionListener(final MainFrame mainFrame){
		
		this.mainFrame = mainFrame;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Thread t = new Thread(this);
		t.start();
		
	}

	@Override
	public void run() {
		
		if(!this.mainFrame.isConnected()){
			ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.LOCAL_PORT, 5432);
		} else {
			ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.LOCAL_PORT, 3200);
		}
		
		this.mainFrame.getMainPanel().getRunButton().setEnabled(false);
		
		String outputDir = !this.mainFrame.getMainPanel().getChooseOutputDirButton().getText().contains(GuiConstants.CHOOSE) ?
				this.mainFrame.getMainPanel().getChooseOutputDirButton().getText() : ".";
		
		StringBuilder surveyAreaIds = new StringBuilder();
		
		for(Entry<String, String> entry : this.mainFrame.getSurveyAreaMap().entrySet()){
		
			int nHouseholds = entry.getValue().equals("") ? 0 : Integer.parseInt(entry.getValue());
			
			//TODO #Persons is hard-coded for the time being, needs to be put into a db table
			this.mainFrame.getConfiguration().getAdminUnitEntries().put(entry.getKey(), new AdminUnitEntry(entry.getKey(),
					nHouseholds, 0, null));
			
			surveyAreaIds.append(entry.getKey() + ",");
		
		}
		
		String surveyArea = surveyAreaIds.toString().length() > 0 ? surveyAreaIds.toString() : null;
		ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.SURVEY_AREA_IDS, surveyArea);
		
		StringBuilder vicinityIds = new StringBuilder();
		
		for(Entry<String, String> entry : this.mainFrame.getVicinityMap().entrySet()){
			
			int nHouseholds = entry.getValue().equals("") ? 0 : Integer.parseInt(entry.getValue());
			
			//TODO #Persons is hard-coded for the time being, needs to be put into a db table
			this.mainFrame.getConfiguration().getAdminUnitEntries().put(entry.getKey(), new AdminUnitEntry(entry.getKey(),
					nHouseholds, 0, null));
			
			vicinityIds.append(entry.getKey());
		}
		
		String vicinity = vicinityIds.toString().length() > 0 ? vicinityIds.toString() : null;
		ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.VICINITY_IDS, vicinity);
		ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.OVERWRITE_FILES, 
				this.mainFrame.getMainPanel().getOverwrite().isSelected());
		ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.OUTPUT_DIR, outputDir);

		new ScenarioGenerationController(this.mainFrame.getConfiguration()).run();
		
		this.mainFrame.getMainPanel().getRunButton().setEnabled(true);
		
		JOptionPane.showMessageDialog(this.mainFrame.getFrame(), new JLabel("Output successfully created!"));
		
		ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.LOCAL_PORT, 3200);
		
	}

}
