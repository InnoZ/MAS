package com.innoz.toolbox.gui;

import com.innoz.toolbox.gui.actionListeners.ButtonChangeActionListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

public class MainPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2199029831448438809L;
	
	private final MainFrame mainFrame;
	private JPanel surveyAreaPanel;
	private JPanel vicinityPanel;
	private JButton chooseOutputDirButton;
	private JCheckBox overwrite;
	private JCheckBox households;
	private JCheckBox network;
	private JButton runButton;
	
	protected MainPanel(final MainFrame parent){
		
		this.mainFrame = parent;
		
		this.setLayout(new GridLayout(15,1));
		this.setBackground(new Color(1,1,1,0.5f));
		this.setPreferredSize(new Dimension(1024,600));
		this.setEnabled(false);
		
		JLabel l = new JLabel("<html><font size='10'><strong>Scenario generation parameters</strong></font></html>");
		l.setMinimumSize(new Dimension(1024, 200));
		l.setPreferredSize(new Dimension(1024, 200));
		this.add(l);
		

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
				int option = JOptionPane.showOptionDialog(parent.getFrame(), message, "Add a new administrative unit", JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
				
				if(option == 0){
				
					parent.getSurveyAreaMap().put(id.getText(), n.getText());
					
					surveyAreaPanel.removeAll();
					
					surveyAreaPanel.add(l1, BorderLayout.LINE_START);

					JPanel buttonPanel = new JPanel();
					buttonPanel.setBackground(new Color(0,0,0,0));
					
					for(Entry<String, String> t : parent.getSurveyAreaMap().entrySet()){
						
						JButton newButton = new JButton(t.getKey() + ", " + t.getValue());
						newButton.addActionListener(new ButtonChangeActionListener(MainPanel.this.mainFrame, newButton, surveyAreaPanel));
						
						buttonPanel.add(newButton);
					
					}
					
					surveyAreaPanel.add(buttonPanel, BorderLayout.CENTER);
					
					surveyAreaPanel.add(addButton, BorderLayout.LINE_END);
					
					surveyAreaPanel.revalidate();
					parent.getFrame().repaint();
					
				}

			}
			
		});
		this.add(surveyAreaPanel);
		
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
				int option = JOptionPane.showOptionDialog(parent.getFrame(), message, "Add a new administrative unit", JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
				
				if(option == 0){
				
					parent.getVicinityMap().put(id.getText(), n.getText());
					
					vicinityPanel.removeAll();
					
					vicinityPanel.add(l2, BorderLayout.LINE_START);

					JPanel buttonPanel = new JPanel();
					buttonPanel.setBackground(new Color(0,0,0,0));
					
					for(Entry<String, String> t : parent.getVicinityMap().entrySet()){
						
						JButton newButton = new JButton(t.getKey() + ", " + t.getValue());
						newButton.addActionListener(new ButtonChangeActionListener(MainPanel.this.mainFrame, newButton, vicinityPanel));
						
						buttonPanel.add(newButton);
					
					}
					
					vicinityPanel.add(buttonPanel, BorderLayout.CENTER);
					
					vicinityPanel.add(addButton2, BorderLayout.LINE_END);
					
					vicinityPanel.revalidate();
					parent.getFrame().repaint();
					
				}

			}
			
		});
		this.add(vicinityPanel);
		
		JSeparator line = new JSeparator(JSeparator.HORIZONTAL);
		this.add(line);
		
		l = new JLabel("<html><font size='10'><strong>Output</strong></font></html>");
		this.add(l);
		
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
				
				parent.getFrame().repaint();
				
			}
		});
		this.add(chooseOutputDirButton);
		
		
		overwrite = new JCheckBox("Overwrite existing files?");
		overwrite.setEnabled(false);
		this.add(overwrite);
		
		network = new JCheckBox("Create network");
		network.setEnabled(false);
		this.add(network);
		
		households = new JCheckBox("Create Households");
		households.setEnabled(false);
		this.add(households);
		
		line = new JSeparator(JSeparator.HORIZONTAL);
		this.add(line);
		
		l = new JLabel("<html><font size='10'><strong>Execution</strong></font></html>");
		this.add(l);
		
		runButton = new JButton("Run");
		runButton.setEnabled(false);
		this.add(runButton);
		runButton.addActionListener(this.mainFrame.getRunnerActionListener());
		
		JButton reset = new JButton("Reset");
		reset.setEnabled(false);
		this.add(reset);
		reset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.reset();
				parent.getFrame().repaint();
			}
		});
		
	}

	public JPanel getSurveyAreaPanel() {
		return surveyAreaPanel;
	}

	public JPanel getVicinityPanel() {
		return vicinityPanel;
	}

	public JButton getChooseOutputDirButton() {
		return chooseOutputDirButton;
	}

	public JCheckBox getOverwrite() {
		return overwrite;
	}

	public JCheckBox getHouseholds() {
		return households;
	}

	public JCheckBox getNetwork() {
		return network;
	}

	public JButton getRunButton() {
		return runButton;
	}
	
}
