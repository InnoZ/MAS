package innoz.io.database.task;

import innoz.io.database.handler.Logbook;

public abstract class LogbookTask {

	public abstract void apply(Logbook logbook);
	
}
