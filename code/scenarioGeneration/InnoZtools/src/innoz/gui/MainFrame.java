package innoz.gui;

import innoz.config.Configuration;
import innoz.config.Configuration.AdminUnitEntry;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;
import innoz.io.BbsrDataReader;
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
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.LogManager;
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

/**
 * 
 * Main class for the GUI version of the scenario generation code.
 * 
 * @author dhosse
 *
 */
public final class MainFrame {

	private final JFrame frame;
	private final Configuration configuration;
	private final RunnerActionListener listener;
	private final ClassLoader classLoader = this.getClass().getClassLoader();
	JPanel mainPanel;
	
	//Components
	private JButton chooseOutputDirButton;
	private JCheckBox overwrite;
	private JButton runButton;
	private JCheckBox network;
	private JCheckBox households;
	
	private Map<String, String> surveyArea;
	private Map<String, String> vicinity;
	
	private JPanel surveyAreaPanel;
	private JPanel vicinityPanel;
	
	public MainFrame() {

		this.configuration = ConfigurationUtils.createConfiguration();
		this.listener = new RunnerActionListener();
		
		this.surveyArea = new ConcurrentHashMap<String, String>();
		this.vicinity = new ConcurrentHashMap<String, String>();
		
		this.frame = new JFrame("InnoZ scenario generation toolbox");

		BufferedImage icon = null;
		
		try {
			
			URL in = this.classLoader.getResource("background.png");
			icon = ImageIO.read(in);
			
		} catch (IOException e) {

			
			e.printStackTrace();
		}

		this.frame.setIconImage(icon);
		
		this.frame.setSize(new Dimension(1024, 768));
		this.frame.setLayout(new BorderLayout());
		
		this.frame.setContentPane(new JLabel(new ImageIcon(icon)));
		
		this.frame.setLayout(new BorderLayout());
		
		this.frame.add(this.createMainPanel(), BorderLayout.CENTER);
		this.mainPanel.setBackground(new Color(1,1,1,0.5f));
		this.mainPanel.setPreferredSize(new Dimension(1024,600));
		this.mainPanel.setEnabled(false);
		
		JPanel footer = this.createFooter();
		this.frame.add(footer, BorderLayout.NORTH);
		footer.setPreferredSize(new Dimension(1024,100));
		footer.setBackground(new Color(0,0.59f,0.84f,0.5f));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea(10,5);
		textArea.setEditable(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		StatusMessageAppender appender = new StatusMessageAppender(textArea);
		LogManager.getRootLogger().addAppender(appender);
		panel.add(scrollPane, BorderLayout.CENTER);
		this.frame.add(panel, BorderLayout.SOUTH);
		
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
		
	}
	
	private JPanel createMainPanel(){
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(15,1));
		
		JLabel l = new JLabel("<html><font size='10'><strong>Scenario generation parameters</strong></font></html>");
		l.setMinimumSize(new Dimension(1024, 200));
		l.setPreferredSize(new Dimension(1024, 200));
		mainPanel.add(l);
		

		surveyAreaPanel = new JPanel();
		surveyAreaPanel.setBackground(new Color(0,0,0,0));
		surveyAreaPanel.setLayout(new BorderLayout());
		JLabel l1 = new JLabel("Survey area ids:");
		surveyAreaPanel.add(l1, BorderLayout.LINE_START);
		
		JButton addButton = new JButton("Add...");
		addButton.setEnabled(false);
		surveyAreaPanel.add(addButton, BorderLayout.LINE_END);
		
		addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JPanel message = new JPanel();
				message.setLayout(new GridLayout());
				message.add(new JLabel("Id"));
				JTextField id = new JTextField();
				message.add(id);
				message.add(new JLabel("Number of households"));
				JTextField n = new JTextField();
				message.add(n);
				
				String[] options = {"Ok", "Cancel"};
				int option = JOptionPane.showOptionDialog(MainFrame.this.frame, message, "Add a new administrative unit", JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
				
				if(option == 0){
				
					surveyArea.put(id.getText(), n.getText());
					
					surveyAreaPanel.removeAll();
					
					surveyAreaPanel.add(l1, BorderLayout.LINE_START);

					JPanel buttonPanel = new JPanel();
					buttonPanel.setBackground(new Color(0,0,0,0));
					
					for(Entry<String, String> t : surveyArea.entrySet()){
						
						JButton newButton = new JButton(t.getKey() + ", " + t.getValue());
						newButton.addActionListener(new ButtonChangeActionListener(newButton, surveyAreaPanel));
						
						buttonPanel.add(newButton);
					
					}
					
					surveyAreaPanel.add(buttonPanel, BorderLayout.CENTER);
					
					surveyAreaPanel.add(addButton, BorderLayout.LINE_END);
					
					surveyAreaPanel.revalidate();
					frame.repaint();
					
				}

			}
			
		});
		mainPanel.add(surveyAreaPanel);
		
		vicinityPanel = new JPanel();
		
		vicinityPanel.setLayout(new BorderLayout());
		
		vicinityPanel.setBackground(new Color(0,0,0,0));
		
		JLabel l2 = new JLabel("Vicinity ids:");
		vicinityPanel.add(l2, BorderLayout.LINE_START);
		JButton addButton2 = new JButton("Add...");
		addButton2.setEnabled(false);
		vicinityPanel.add(addButton2, BorderLayout.LINE_END);
		
		addButton2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JPanel message = new JPanel();
				message.setLayout(new GridLayout());
				message.add(new JLabel("Id"));
				JTextField id = new JTextField();
				message.add(id);
				message.add(new JLabel("Number of households"));
				JTextField n = new JTextField();
				message.add(n);
				
				String[] options = {"Ok", "Cancel"};
				int option = JOptionPane.showOptionDialog(MainFrame.this.frame, message, "Add a new administrative unit", JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
				
				if(option == 0){
				
					vicinity.put(id.getText(), n.getText());
					
					vicinityPanel.removeAll();
					
					vicinityPanel.add(l2, BorderLayout.LINE_START);

					JPanel buttonPanel = new JPanel();
					buttonPanel.setBackground(new Color(0,0,0,0));
					
					for(Entry<String, String> t : vicinity.entrySet()){
						
						JButton newButton = new JButton(t.getKey() + ", " + t.getValue());
						newButton.addActionListener(new ButtonChangeActionListener(newButton, vicinityPanel));
						
						buttonPanel.add(newButton);
					
					}
					
					vicinityPanel.add(buttonPanel, BorderLayout.CENTER);
					
					vicinityPanel.add(addButton2, BorderLayout.LINE_END);
					
					vicinityPanel.revalidate();
					frame.repaint();
					
				}

			}
			
		});
		mainPanel.add(vicinityPanel);
		
		JSeparator line = new JSeparator(JSeparator.HORIZONTAL);
		mainPanel.add(line);
		
		l = new JLabel("<html><font size='10'><strong>Output</strong></font></html>");
		mainPanel.add(l);
		
		chooseOutputDirButton = new JButton("Choose output directory");
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
		mainPanel.add(chooseOutputDirButton);
		
		
		overwrite = new JCheckBox("Overwrite existing files?");
		overwrite.setEnabled(false);
		mainPanel.add(overwrite);
		
		network = new JCheckBox("Create network");
		network.setEnabled(false);
		mainPanel.add(network);
		
		households = new JCheckBox("Create Households");
		households.setEnabled(false);
		mainPanel.add(households);
		
		runButton = new JButton("Run");
		runButton.setEnabled(false);
		mainPanel.add(runButton);
		runButton.addActionListener(this.listener);
		
		JButton reset = new JButton("Reset");
		reset.setEnabled(false);
		mainPanel.add(reset);
		reset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
				MainFrame.this.frame.repaint();
			}
		});
		
		return mainPanel;

	}
	
	class ButtonChangeActionListener implements ActionListener{

		private final JButton button;
		private final JPanel parent;
		
		public ButtonChangeActionListener(JButton button, JPanel parent) {
			this.button = button;
			this.parent = parent;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String oldKey = this.button.getText().split(", ")[0];
			
			JPanel message = new JPanel();
			message.setLayout(new GridLayout());
			message.add(new JLabel("Id"));
			JTextField id = new JTextField(this.button.getText().split(", ")[0]);
			message.add(id);
			message.add(new JLabel("Number of households"));
			JTextField n = new JTextField(this.button.getText().split(", ")[1]);
			message.add(n);
			
			String[] options = {"Ok", "Cancel"};
			int option = JOptionPane.showOptionDialog(MainFrame.this.frame, message, "Add a new administrative unit", JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
			
			if(option == 0){

				surveyArea.remove(oldKey);
				
				surveyArea.put(id.getText(), n.getText());
				
				this.button.setText(id.getText() + ", " + n. getText());
				
				this.parent.revalidate();
				frame.repaint();
				
			}

			
		}
		
	}
	
	private void reset(){
		
		this.chooseOutputDirButton.setText("Choose output directory");
		this.surveyArea = new HashMap<String, String>();
		this.vicinity = new HashMap<String, String>();
		this.network.setSelected(false);
		this.households.setSelected(false);
		this.overwrite.setSelected(false);
		this.frame.repaint();
		
	}
	
	private JPanel createFooter(){

		JLabel label = new JLabel("Status of MobilityDataHub connection:");
		JLabel connectionStatus = new JLabel("<html><font color='red'>Not connected</font></html>");
//		connectionStatus.setBackground(Color.WHITE);
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
			if(component instanceof JComponent){
				for(Component c : ((JComponent)component).getComponents()){
					c.setEnabled(true);
				}
			}
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
					
				} else {
					
					JPanel message = new JPanel();
					message.setPreferredSize(new Dimension(400,50));
					message.add(new JLabel("Could not connect to MobilityDataHub!"));
					message.add(new JLabel("Did you enter correct user names and passwords?"));
					
					JOptionPane.showConfirmDialog(MainFrame.this.frame, message, "Error!", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE);
					
				}
				
			} else {
		
				JPanel panel = new JPanel();
				panel.setPreferredSize(new Dimension(300,50));
				panel.add(new JLabel("Disconnect from MobilityDataHub?"));
				String[] options = {"Yes","No"};
					
				int option = JOptionPane.showOptionDialog(MainFrame.this.frame, panel, "",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				
				if(option == 0){

					SshConnector.disconnect();
					this.status.setText("<html><font color='red'>Not connected</font></html>");
					this.button.setText("Connect");
					disableComponents();
					
				}
				
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
			
			String outputDir = !chooseOutputDirButton.getText().contains("Choose") ? chooseOutputDirButton.getText() : ".";
			
			StringBuilder surveyAreaIds = new StringBuilder();
			
			for(Entry<String, String> entry : surveyArea.entrySet()){
				int nHouseholds = entry.getValue().equals("") ? 0 : Integer.parseInt(entry.getValue());
				configuration.getAdminUnitEntries().put(entry.getKey(), new AdminUnitEntry(entry.getKey(), nHouseholds, null));
				surveyAreaIds.append(entry.getKey() + ",");
			}
			
			ConfigurationUtils.set(configuration, Configuration.SURVEY_AREA_IDS, surveyAreaIds.toString());
			
			StringBuilder vicinityIds = new StringBuilder();
			
			for(Entry<String, String> entry : vicinity.entrySet()){
				int nHouseholds = entry.getValue().equals("") ? 0 : Integer.parseInt(entry.getValue());
				configuration.getAdminUnitEntries().put(entry.getKey(), new AdminUnitEntry(entry.getKey(), nHouseholds, null));
				vicinityIds.append(entry.getKey());
			}
			
			String vicinity = vicinityIds.toString().length() > 0 ? vicinityIds.toString() : null;
			ConfigurationUtils.set(configuration, Configuration.VICINITY_IDS, vicinity);
			ConfigurationUtils.set(configuration, Configuration.OVERWRITE_FILES, overwrite.isSelected());
			ConfigurationUtils.set(configuration, Configuration.OUTPUT_DIR, outputDir);
			
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
			dbReader.readGeodataFromDatabase(configuration, scenario);
			InputStream in = classLoader.getResourceAsStream("regionstypen.csv");
			new BbsrDataReader().read(geoinformation, new InputStreamReader(in));
			
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
			
			runButton.setEnabled(true);
			
			JOptionPane.showMessageDialog(frame, new JLabel("Output successfully created!"));

		}
		
	}
	
}