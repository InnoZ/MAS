package innoz.gui.actionListeners;

import innoz.config.Configuration;
import innoz.config.Configuration.AdminUnitEntry;
import innoz.config.ConfigurationUtils;
import innoz.gui.MainFrame;
import innoz.run.ScenarioGenerationController;

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
		
		this.mainFrame.getMainPanel().getRunButton().setEnabled(false);
		
		String outputDir = !this.mainFrame.getMainPanel().getChooseOutputDirButton().getText().contains("Choose") ?
				this.mainFrame.getMainPanel().getChooseOutputDirButton().getText() : ".";
		
		StringBuilder surveyAreaIds = new StringBuilder();
		
		for(Entry<String, String> entry : this.mainFrame.getSurveyAreaMap().entrySet()){
			int nHouseholds = entry.getValue().equals("") ? 0 : Integer.parseInt(entry.getValue());
			this.mainFrame.getConfiguration().getAdminUnitEntries().put(entry.getKey(), new AdminUnitEntry(entry.getKey(), nHouseholds, null));
			surveyAreaIds.append(entry.getKey() + ",");
		}
		
		ConfigurationUtils.set(this.mainFrame.getConfiguration(), Configuration.SURVEY_AREA_IDS, surveyAreaIds.toString());
		
		StringBuilder vicinityIds = new StringBuilder();
		
		for(Entry<String, String> entry : this.mainFrame.getVicinityMap().entrySet()){
			int nHouseholds = entry.getValue().equals("") ? 0 : Integer.parseInt(entry.getValue());
			this.mainFrame.getConfiguration().getAdminUnitEntries().put(entry.getKey(), new AdminUnitEntry(entry.getKey(), nHouseholds, null));
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
		
	}

}
