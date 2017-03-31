package com.innoz.toolbox.run;

import com.innoz.toolbox.gui.MainFrame;

import java.awt.EventQueue;

/**
 * 
 * Entry point for the graphical user interface of the scenario generation tool.
 * 
 * @author dhosse
 *
 */
public final class Gui {
	
	private Gui(){};

	public static void main(String args[]) {

		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				MainFrame.getInstance();
				
			}
			
		});
		
	}
	
}
