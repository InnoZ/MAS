package com.innoz.toolbox.gui.actionListeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet.PopulationSource;
import com.innoz.toolbox.gui.GuiConstants;
import com.innoz.toolbox.gui.MainFrame;
import com.innoz.toolbox.run.controller.ScenarioGenerationController;

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
			this.mainFrame.getConfiguration().psql().setPsqlPort(5432);
		} else {
			this.mainFrame.getConfiguration().psql().setPsqlPort(3200);
		}
		
		this.mainFrame.getMainPanel().getRunButton().setEnabled(false);
		
		String outputDir = !this.mainFrame.getMainPanel().getChooseOutputDirButton().getText().contains(GuiConstants.CHOOSE) ?
				this.mainFrame.getMainPanel().getChooseOutputDirButton().getText() : ".";
		
				this.mainFrame.getConfiguration().misc().setOutputDirectory(outputDir);
				
		StringBuilder surveyAreaIds = new StringBuilder();
		
		for(Entry<String, String> entry : this.mainFrame.getSurveyAreaMap().entrySet()){
		
			surveyAreaIds.append(entry.getKey() + ",");
		
		}
		
		String surveyArea = surveyAreaIds.toString().length() > 0 ? surveyAreaIds.toString() : null;
		AreaSet areaSet = new AreaSet();
		areaSet.setIds(surveyArea);
		areaSet.setIsSurveyArea(true);
		areaSet.setNetworkLevel(6);
		areaSet.setPopulationSource(PopulationSource.SURVEY);
		this.mainFrame.getConfiguration().scenario().addAreaSet(areaSet);
		
		StringBuilder vicinityIds = new StringBuilder();
		
		for(Entry<String, String> entry : this.mainFrame.getVicinityMap().entrySet()){
			
			vicinityIds.append(entry.getKey());
			
		}
		
		String vicinity = vicinityIds.toString().length() > 0 ? vicinityIds.toString() : null;
		
		AreaSet areaSet2 = new AreaSet();
		areaSet2.setIds(vicinity);
		areaSet2.setIsSurveyArea(true);
		areaSet2.setNetworkLevel(6);
		areaSet2.setPopulationSource(PopulationSource.COMMUTER);
		this.mainFrame.getConfiguration().scenario().addAreaSet(areaSet2);
		
		new ScenarioGenerationController(this.mainFrame.getConfiguration()).run();
		
		this.mainFrame.getMainPanel().getRunButton().setEnabled(true);
		
		JOptionPane.showMessageDialog(this.mainFrame.getFrame(), new JLabel("Output successfully created!"));
		
		this.mainFrame.getConfiguration().psql().setPsqlPort(3200);
		
	}

}
