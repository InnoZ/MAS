package innoz.run;

import innoz.gui.MainFrame;

import java.awt.EventQueue;

public class Gui {

	public static void main(String args[]) {

		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				new MainFrame();
				
			}
			
		});
		
	}
	
}
