package innoz.gui;

import innoz.config.Configuration;
import innoz.config.ConfigurationUtils;
import innoz.gui.actionListeners.RunnerActionListener;
import innoz.gui.actionListeners.SshConnectionListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.LogManager;

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
	private MainPanel mainPanel;
	
	//Components
	private Map<String, String> surveyArea;
	private Map<String, String> vicinity;
	
	public MainFrame() {

		this.configuration = ConfigurationUtils.createConfiguration();
		this.listener = new RunnerActionListener(this);
		
		this.surveyArea = new ConcurrentHashMap<String, String>();
		this.vicinity = new ConcurrentHashMap<String, String>();
		
		this.frame = new JFrame(GuiConstants.TITLE);

		BufferedImage icon = null;
		
		try {
			
			URL in = this.classLoader.getResource(GuiConstants.BACKGROUND_IMAGE);
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
	
	void reset(){
		
		this.mainPanel.getChooseOutputDirButton().setText(GuiConstants.CHOOSE_OUTPUT);
		this.surveyArea = new HashMap<String, String>();
		this.vicinity = new HashMap<String, String>();
		this.mainPanel.getNetwork().setSelected(false);
		this.mainPanel.getHouseholds().setSelected(false);
		this.mainPanel.getOverwrite().setSelected(false);
		this.frame.repaint();
		
	}
	
	private JPanel createFooter(){

		JLabel label = new JLabel("Status of MobilityDataHub connection:");
		JLabel connectionStatus = new JLabel(GuiConstants.STATUS_DISCONNECTED);
		connectionStatus.setPreferredSize(new Dimension(120,40));
		JButton connection = new JButton(GuiConstants.CONNECT);
		
		connection.addActionListener(new SshConnectionListener(this, connection, connectionStatus));
		
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(connectionStatus);
		panel.add(connection);
		
		return panel;
		
	}
	
	public void enableComponents(){
		
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
	
	public void disableComponents(){
		
		for(Component component : mainPanel.getComponents()){
			component.setEnabled(false);
		}
		
		this.frame.repaint();
		
	}
	
	public Configuration getConfiguration(){
		return this.configuration;
	}
	
	public MainPanel getMainPanel(){
		return this.mainPanel;
	}
	
	public JFrame getFrame(){
		return this.frame;
	}
	
	public Map<String,String> getSurveyAreaMap(){
		return this.surveyArea;
	}
	
	public Map<String,String> getVicinityMap(){
		return this.vicinity;
	}
	
	public RunnerActionListener getRunnerActionListener(){
		return this.listener;
	}
	
}