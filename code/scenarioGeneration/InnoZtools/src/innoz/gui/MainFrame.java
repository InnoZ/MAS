package innoz.gui;

import innoz.config.Configuration;
import innoz.config.Configuration.AdminUnitEntry;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;
import innoz.run.ScenarioGenerationController;

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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.LogManager;

import com.jcraft.jsch.JSchException;

/**
 * 
 * Main class for the GUI version of the scenario generation code.
 * 
 * @author dhosse
 *
 */
public final class MainFrame {

	final JFrame frame;
	private final Configuration configuration;
	final RunnerActionListener listener;
	private final ClassLoader classLoader = this.getClass().getClassLoader();
	MainPanel mainPanel;
	
	//Components
	Map<String, String> surveyArea;
	Map<String, String> vicinity;
	
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
		
		this.mainPanel = new MainPanel(this);
		this.frame.add(this.mainPanel, BorderLayout.CENTER);
		
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
			int option = JOptionPane.showOptionDialog(frame, message, "Add a new administrative unit", JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
			
			if(option == 0){

				surveyArea.remove(oldKey);
				
				surveyArea.put(id.getText(), n.getText());
				
				this.button.setText(id.getText() + ", " + n. getText());
				
				this.parent.revalidate();
				frame.repaint();
				
			}

			
		}
		
	}
	
	void reset(){
		
		this.mainPanel.chooseOutputDirButton.setText("Choose output directory");
		this.surveyArea = new HashMap<String, String>();
		this.vicinity = new HashMap<String, String>();
		this.mainPanel.network.setSelected(false);
		this.mainPanel.households.setSelected(false);
		this.mainPanel.overwrite.setSelected(false);
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
			
			mainPanel.runButton.setEnabled(false);
			
			String outputDir = !mainPanel.chooseOutputDirButton.getText().contains("Choose") ? mainPanel.chooseOutputDirButton.getText() : ".";
			
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
			ConfigurationUtils.set(configuration, Configuration.OVERWRITE_FILES, mainPanel.overwrite.isSelected());
			ConfigurationUtils.set(configuration, Configuration.OUTPUT_DIR, outputDir);

			new ScenarioGenerationController(configuration).run();
			
			mainPanel.runButton.setEnabled(true);
			
			JOptionPane.showMessageDialog(frame, new JLabel("Output successfully created!"));

		}
		
	}
	
}