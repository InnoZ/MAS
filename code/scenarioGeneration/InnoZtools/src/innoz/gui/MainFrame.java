package innoz.gui;

import innoz.config.Configuration;
import innoz.config.ConfigurationParameterSetter;
import innoz.config.Configuration.AdminUnitEntry;
import innoz.config.SshConnector;
import innoz.io.database.DatabaseReader;
import innoz.io.database.DatabaseUpdater;
import innoz.scenarioGeneration.config.InitialConfigCreator;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.network.NetworkCreatorFromPsql;
import innoz.scenarioGeneration.population.PopulationCreator;
import innoz.scenarioGeneration.population.utils.PersonUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;
import org.matsim.vehicles.VehicleWriterV1;
import org.opengis.referencing.FactoryException;

import com.jcraft.jsch.JSchException;
import com.vividsolutions.jts.io.ParseException;

public final class MainFrame {

	private final JFrame frame;
	private final Configuration configuration;
	private final RunnerActionListener listener;
	JPanel mainPanel;
	
	//Components
	private JTextField surveyAreaIdsTextField;
	private JTextField vicinityIdsTextField;
	private JTextField nHouseholdsTextField;
	private JLabel outputDir;
	private JCheckBox overwrite;
	
	public static void main(String args[]) {

		new MainFrame();
		
	}
	
	private MainFrame() {

		this.configuration = new Configuration();
		this.listener = new RunnerActionListener();
		
		this.frame = new JFrame("InnoZ scenario generation toolbox");
		
		this.frame.setSize(new Dimension(800, 600));
		this.frame.setLayout(new BorderLayout());
		
		this.frame.add(this.createView(), BorderLayout.CENTER);
		this.mainPanel.setEnabled(false);
		
		JPanel footer = this.createFooter();
		this.frame.add(footer, BorderLayout.NORTH);
		
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
		
	}
	
	private JPanel createView(){

		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 10;
		
		JLabel l = new JLabel("Scenario generation parameters");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 0.1;
		mainPanel.add(l, c);
		
		JSeparator line = new JSeparator(JSeparator.HORIZONTAL);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		mainPanel.add(line, c);
		
		JLabel l1 = new JLabel("Survey area ids (comma-separated):");
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		mainPanel.add(l1, c);
		this.surveyAreaIdsTextField = new JTextField();
		this.surveyAreaIdsTextField.setEnabled(false);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		mainPanel.add(this.surveyAreaIdsTextField, c);
		
		JLabel l2 = new JLabel("Vicinity ids (comma-separated):");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		mainPanel.add(l2, c);
		vicinityIdsTextField = new JTextField();
		vicinityIdsTextField.setEnabled(false);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 2;
		mainPanel.add(vicinityIdsTextField, c);
		
		JLabel l3 = new JLabel("Number of households:");
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		mainPanel.add(l3, c);
		this.nHouseholdsTextField = new JTextField();
		this.nHouseholdsTextField.setEnabled(false);
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 2;
		mainPanel.add(this.nHouseholdsTextField, c);

		line = new JSeparator(JSeparator.HORIZONTAL);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 3;
		mainPanel.add(line, c);
		
		line = new JSeparator(JSeparator.HORIZONTAL);
		c.gridx = 0;
		c.gridy = 6;
		mainPanel.add(line, c);
		
		l = new JLabel("Output");
		c.gridx = 0;
		c.gridy = 7;
		mainPanel.add(l, c);
		
		line = new JSeparator(JSeparator.HORIZONTAL);
		c.gridx = 0;
		c.gridy = 8;
		c.gridwidth = 3;
		mainPanel.add(line, c);
		
		JLabel ll = new JLabel("Output directory");
		c.gridx = 0;
		c.gridy = 9;
		c.gridwidth = 1;
		mainPanel.add(ll, c);
		
		outputDir = new JLabel();
		c.gridx = 1;
		mainPanel.add(outputDir, c);
		
		JButton choose = new JButton("Choose");
		choose.setEnabled(false);
		choose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				int returnVal = chooser.showOpenDialog(choose);
				if(returnVal == JFileChooser.APPROVE_OPTION){
					outputDir.setText(chooser.getSelectedFile().getAbsolutePath() + "/");
				}
				
			}
		});
		c.gridx = 2;
		mainPanel.add(choose, c);
		
		JLabel lll = new JLabel("Overwrite existing files?");
		c.gridx = 0;
		c.gridy = 10;
		mainPanel.add(lll, c);
		overwrite = new JCheckBox();
		overwrite.setEnabled(false);
		c.gridx = 1;
		mainPanel.add(overwrite, c);
		
		JLabel l4 = new JLabel("Create network");
		c.gridx = 0;
		c.gridy = 11;
		mainPanel.add(l4, c);
		
		JRadioButton network = new JRadioButton("Yes");
		network.setEnabled(false);
		c.gridx = 1;
		c.gridy = 11;
		mainPanel.add(network, c);
		JRadioButton noNetwork = new JRadioButton("No");
		noNetwork.setEnabled(false);
		c.gridx = 2;
		c.gridy = 11;
		mainPanel.add(noNetwork, c);
		
		ButtonGroup networkGroup = new ButtonGroup();
		networkGroup.add(network);
		networkGroup.add(noNetwork);
		
		JLabel l5 = new JLabel("Create Households");
		c.gridx = 0;
		c.gridy = 12;
		mainPanel.add(l5, c);
		
		JRadioButton households = new JRadioButton("Yes");
		households.setEnabled(false);
		c.gridx = 1;
		c.gridy = 12;
		mainPanel.add(households, c);
		JRadioButton noHouseholds = new JRadioButton("No");
		noHouseholds.setEnabled(false);
		c.gridx = 2;
		c.gridy = 12;
		mainPanel.add(noHouseholds, c);
		
		ButtonGroup householdsGroup = new ButtonGroup();
		householdsGroup.add(households);
		householdsGroup.add(noHouseholds);
		
		JButton runButton = new JButton("Run");
		runButton.setEnabled(false);
		c.gridx = 1;
		c.gridy = 13;
		mainPanel.add(runButton, c);
		runButton.addActionListener(this.listener);
		
		return mainPanel;

	}
	
	private JPanel createFooter(){

		JLabel label = new JLabel("Status of MobilityDataHub connection:");
		JLabel connectionStatus = new JLabel("<html><font color='red'>Not connected</font></html>");
		JButton connection = new JButton("Connect");
		
		connection.addActionListener(new SshConnectionListener(connection, connectionStatus));
		
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(connectionStatus);
		panel.add(connection);
		
		return panel;
		
	}
	
	private void enableComponents(){
		
		for(Component component : mainPanel.getComponents()){
			component.setEnabled(true);
		}
		
	}
	
	private void disableComponents(){
		
		for(Component component : mainPanel.getComponents()){
			component.setEnabled(false);
		}
		
	}
	
	class SshConnectionListener implements ActionListener{

		final JButton button;
		final JLabel status;
		
		SshConnectionListener(JButton button, JLabel status){
			
			this.button = button;
			this.status = status;
			
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(this.status.getText().equals("<html><font color='red'>Not connected</font></html>")){
				
				try {
					
					SshConnector.connect(configuration);
				
				} catch (JSchException | IOException e1) {

					e1.printStackTrace();
				
				}
				
				this.status.setText("<html><font color='green'>Connected</font></html>");
				this.button.setText("Disconnect");
				enableComponents();
				
			} else {
				
				SshConnector.disconnect();
				this.status.setText("<html><font color='red'>Not connected</font></html>");
				this.button.setText("Connect");
				disableComponents();
				
			}
			
		}
		
	}
	
	class RunnerActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			ConfigurationParameterSetter.set(configuration, Configuration.SURVEY_AREA_IDS, surveyAreaIdsTextField.getText());

			for(String s : configuration.getSurveyAreaIds().split(",")){
				configuration.getAdminUnitEntries().add(new AdminUnitEntry(s, Integer.parseInt(nHouseholdsTextField.getText())));
			}
			
			String vicinity = surveyAreaIdsTextField.getText().length() > 0 ? surveyAreaIdsTextField.getText() : null;
			ConfigurationParameterSetter.set(configuration, Configuration.VICINITY_IDS, vicinity);
			ConfigurationParameterSetter.set(configuration, Configuration.OVERWRITE_FILES, overwrite.isSelected());
			
			configuration.dumpSettings();
			
			MatsimRandom.reset(configuration.getRandomSeed());
			
			// Create a MATSim scenario
			Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
			// Enable the usage of households
			scenario.getConfig().scenario().setUseHouseholds(true);
			((ScenarioImpl)scenario).createHouseholdsContainer();
			
			// If we want to explicitly model household's cars, enable it
			if(configuration.isUsingVehicles()){
				scenario.getConfig().scenario().setUseVehicles(true);
				((ScenarioImpl)scenario).createVehicleContainer();
			}
			
			// Container for geoinformation (admin borders, landuse)
			Geoinformation geoinformation = new Geoinformation();

			// A class that reads data from database tables into local containers
			DatabaseReader dbReader = new DatabaseReader(geoinformation);
			dbReader.readGeodataFromDatabase(configuration, configuration.getSurveyAreaIds(),
					configuration.getVicinityIds(), scenario);
			
			// Create a MATSim network from OpenStreetMap data
			NetworkCreatorFromPsql nc;
			try {
				nc = new NetworkCreatorFromPsql(scenario.getNetwork(),
						geoinformation,	configuration);
				nc.setSimplifyNetwork(true);
				nc.setCleanNetwork(true);
				nc.setScaleMaxSpeed(true);
				nc.create(dbReader);
			} catch (FactoryException | InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | ParseException e1) {
				e1.printStackTrace();
			}
			
			// Create a MATSim population
			try {
				new PopulationCreator(geoinformation).run(configuration, scenario);
			} catch (FactoryException e1) {
				e1.printStackTrace();
			}
			
			// Create an initial MATSim config file and write it into the output directory
			Config config = InitialConfigCreator.create(configuration);
			new ConfigWriter(config).write(configuration.getOutputDirectory() + "config.xml.gz");
			
			// Dump scenario elements into the output directory
			new NetworkWriter(scenario.getNetwork()).write(configuration
					.getOutputDirectory() + "network.xml.gz");
			new PopulationWriter(scenario.getPopulation()).write(configuration
					.getOutputDirectory() + "plans.xml.gz");
			new ObjectAttributesXmlWriter((ObjectAttributes) scenario.getScenarioElement(
					PersonUtils.PERSON_ATTRIBUTES)).writeFile(configuration.getOutputDirectory()
							+ "personAttributes.xml.gz");
			
			if(configuration.isUsingHouseholds()){
				
				new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration
						.getOutputDirectory() + "households.xml.gz");
				
			}
			
			if(configuration.isUsingVehicles()){

				new VehicleWriterV1(scenario.getVehicles()).writeFile(configuration
						.getOutputDirectory() + "vehicles.xml.gz");
				
			}
			
			if(configuration.isWritingDatabaseOutput()){
				
				new DatabaseUpdater().update(configuration, scenario,
						configuration.getDatabaseSchemaName(),
						configuration.isWritingIntoMobilityDatahub());
				
			}
			
		}
		
	}
	
	class ImagePanel extends JComponent {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7280737010865829760L;
		
		private Image image;
		
		public ImagePanel(Image image){
			this.image = image;
		}
		
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(image, 0, 0, this);
		}
		
	}
	
}
