package com.innoz.toolbox.gui;

import javax.swing.JTextArea;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class StatusMessageAppender extends AppenderSkeleton {

	private final JTextArea textArea;
	
	public StatusMessageAppender(final JTextArea textArea){
		this.textArea = textArea;
	}
	
	@Override
	public void close() {
		
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent arg0) {

		this.textArea.append(arg0.getMessage().toString() + "\n");
		int len = this.textArea.getDocument().getLength();
		this.textArea.setCaretPosition(len);
		this.textArea.requestFocusInWindow();
		
	}

}
