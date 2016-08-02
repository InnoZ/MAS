package innoz.io.database.task;

import innoz.io.database.handler.Logbook;

public interface LogbookTask extends Task {

	public void apply(Logbook logbook);
	
}
