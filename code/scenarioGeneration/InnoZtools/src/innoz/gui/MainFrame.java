package innoz.gui;

import innoz.config.Configuration;
import innoz.config.Configuration.AdminUnitEntry;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;
import innoz.io.database.DatabaseReader;
import innoz.io.database.DatabaseUpdater;
import innoz.scenarioGeneration.config.InitialConfigCreator;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.network.NetworkCreatorFromPsql;
import innoz.scenarioGeneration.population.PopulationCreator;
import innoz.scenarioGeneration.population.utils.PersonUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
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
	private JButton chooseOutputDirButton;
	private JCheckBox overwrite;
	private JButton runButton;
	private JCheckBox network;
	private JCheckBox households;
	
	public static void main(String args[]) {

		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				new MainFrame();
				
			}
			
		});
		
	}
	
	private MainFrame() {

		this.configuration = ConfigurationUtils.createConfiguration();
		this.listener = new RunnerActionListener();
		
		this.frame = new JFrame("InnoZ scenario generation toolbox");

		URL url = this.getClass().getResource("background.png");
		
		try {
			this.frame.setIconImage(ImageIO.read(url));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.frame.setSize(new Dimension(1024, 768));
		this.frame.setLayout(new BorderLayout());
		
		try {
			this.frame.setContentPane(new JLabel(new ImageIcon(ImageIO.read(url))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.frame.setLayout(new BorderLayout());
		
		this.frame.add(this.createMainPanel(), BorderLayout.CENTER);
		this.mainPanel.setBackground(new Color(1,1,1,0.5f));
		this.mainPanel.setPreferredSize(new Dimension(1024,600));
		this.mainPanel.setEnabled(false);
		
		JPanel footer = this.createFooter();
		this.frame.add(footer, BorderLayout.NORTH);
		footer.setPreferredSize(new Dimension(1024,100));
		footer.setBackground(new Color(0,0.59f,0.84f,0.5f));

		JTextArea textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(1024,100));
		TextAreaOutputStreamContainer comp = new TextAreaOutputStreamContainer(textArea, new TextAreaOutputStream(textArea, ""));
		this.frame.add(comp, BorderLayout.SOUTH);
		
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.pack();
		this.frame.setVisible(true);
		
		System.out.println("initialized");
		
	}
	
	private JPanel createMainPanel(){
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 10;
//		c.ipadx = 100;
		
		JLabel l = new JLabel("Scenario generation parameters");
		l.setBackground(Color.WHITE);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weightx = 0.1;
		mainPanel.add(l, c);
		
		JSeparator line = new JSeparator(JSeparator.HORIZONTAL);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
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
		c.gridwidth = 1;
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
		c.gridwidth = 1;
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
		c.gridwidth = 1;
		mainPanel.add(this.nHouseholdsTextField, c);

		line = new JSeparator(JSeparator.HORIZONTAL);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 2;
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
		c.gridwidth = 2;
		mainPanel.add(line, c);
		
		JLabel ll = new JLabel("Output directory");
		c.gridx = 0;
		c.gridy = 9;
		c.gridwidth = 1;
		mainPanel.add(ll, c);
		
		chooseOutputDirButton = new JButton("Choose");
		chooseOutputDirButton.setEnabled(false);
		chooseOutputDirButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				int returnVal = chooser.showOpenDialog(chooseOutputDirButton);
				if(returnVal == JFileChooser.APPROVE_OPTION){
					chooseOutputDirButton.setText(chooser.getSelectedFile().getAbsolutePath() + "/");
				}
				
				frame.repaint();
				
			}
		});
		c.gridx = 1;
		mainPanel.add(chooseOutputDirButton, c);
		
//		outputDir = new JLabel();
//		c.gridx = 1;
//		mainPanel.add(outputDir, c);
		
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
		
		network = new JCheckBox();
		network.setEnabled(false);
		c.gridx = 1;
		c.gridy = 11;
		mainPanel.add(network, c);
		
		JLabel l5 = new JLabel("Create Households");
		c.gridx = 0;
		c.gridy = 12;
		mainPanel.add(l5, c);
		
		households = new JCheckBox();
		households.setEnabled(false);
		c.gridx = 1;
		c.gridy = 12;
		mainPanel.add(households, c);
		
		runButton = new JButton("Run");
		runButton.setEnabled(false);
		c.gridx = 1;
		c.gridy = 13;
		mainPanel.add(runButton, c);
		runButton.addActionListener(this.listener);
		
		JButton reset = new JButton("Reset");
		reset.setEnabled(false);
		c.gridx = 1;
		c.gridy = 14;
		mainPanel.add(reset, c);
		reset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		
		return mainPanel;

	}
	
	private void reset(){
		
		this.chooseOutputDirButton.setText("Choose");
		this.nHouseholdsTextField.setText("");
		this.surveyAreaIdsTextField.setText("");
		this.vicinityIdsTextField.setText("");
		
	}
	
	private JPanel createFooter(){

		JLabel label = new JLabel("Status of MobilityDataHub connection:");
		JLabel connectionStatus = new JLabel("<html><font color='red'>Not connected</font></html>");
		connectionStatus.setBackground(Color.WHITE);
		connectionStatus.setPreferredSize(new Dimension(120,40));
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
		
		this.frame.repaint();
		
	}
	
	private void disableComponents(){
		
		for(Component component : mainPanel.getComponents()){
			component.setEnabled(false);
		}
		
		this.frame.repaint();
		
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
				
				boolean established = false;
				
				try {
					
					established = SshConnector.connect(configuration);
				
				} catch (JSchException | IOException e1) {

					e1.printStackTrace();
				
				}
				
				if(established){
					this.status.setText("<html><font color='green'>Connected</font></html>");
					this.button.setText("Disconnect");
					enableComponents();
				}
				
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
			
			Thread t = new Thread(new ScenarioGeneration());
			t.start();
						
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
	
	class ScenarioGeneration implements Runnable{

		@Override
		public void run() {

			runButton.setEnabled(false);
			
			System.out.println("starting scenario generation...");
			
			ConfigurationUtils.set(configuration, Configuration.SURVEY_AREA_IDS, surveyAreaIdsTextField.getText());

			int nHouseholds = !nHouseholdsTextField.getText().equals("") ? Integer.parseInt(nHouseholdsTextField.getText()) : 0;
			
			for(String s : configuration.getSurveyAreaIds().split(",")){
				configuration.getAdminUnitEntries().add(new AdminUnitEntry(s, nHouseholds));
			}
			
			String vicinity = vicinityIdsTextField.getText().length() > 0 ? vicinityIdsTextField.getText() : null;
			ConfigurationUtils.set(configuration, Configuration.VICINITY_IDS, vicinity);
			ConfigurationUtils.set(configuration, Configuration.OVERWRITE_FILES, overwrite.isSelected());
			ConfigurationUtils.set(configuration, Configuration.OUTPUT_DIR, chooseOutputDirButton.getText());
			
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
			
			System.out.println("reading from database...");
			
			// Container for geoinformation (admin borders, landuse)
			Geoinformation geoinformation = new Geoinformation();

			// A class that reads data from database tables into local containers
			DatabaseReader dbReader = new DatabaseReader(geoinformation);
			dbReader.readGeodataFromDatabase(configuration, configuration.getSurveyAreaIds(),
					configuration.getVicinityIds(), scenario);
			
			System.out.println("creating network...");
			
			if(network.isSelected()){

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
				
				new NetworkWriter(scenario.getNetwork()).write(configuration
						.getOutputDirectory() + "network.xml.gz");
				
			}
			
			System.out.println("creating plans...");
			
			if(households.isSelected()){
				// Create a MATSim population
				try {
					new PopulationCreator(geoinformation).run(configuration, scenario);
				} catch (FactoryException e1) {
					e1.printStackTrace();
				}
				
				new PopulationWriter(scenario.getPopulation()).write(configuration
						.getOutputDirectory() + "plans.xml.gz");
				new ObjectAttributesXmlWriter((ObjectAttributes) scenario.getScenarioElement(
						PersonUtils.PERSON_ATTRIBUTES)).writeFile(configuration.getOutputDirectory()
								+ "personAttributes.xml.gz");
				new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration
						.getOutputDirectory() + "households.xml.gz");
			}
			
			// Create an initial MATSim config file and write it into the output directory
			Config config = InitialConfigCreator.create(configuration);
			new ConfigWriter(config).write(configuration.getOutputDirectory() + "config.xml.gz");
			
			if(configuration.isUsingVehicles()){

				new VehicleWriterV1(scenario.getVehicles()).writeFile(configuration
						.getOutputDirectory() + "vehicles.xml.gz");
				
			}
			
			if(configuration.isWritingDatabaseOutput()){
				
				new DatabaseUpdater().update(configuration, scenario,
						configuration.getDatabaseSchemaName(),
						configuration.isWritingIntoMobilityDatahub());
				
			}
			
			System.out.println("done.");

			runButton.setEnabled(true);
			
			
		}
		
	}
	
}