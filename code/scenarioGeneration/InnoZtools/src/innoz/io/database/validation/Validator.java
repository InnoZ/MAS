package innoz.io.database.validation;

import innoz.io.database.handler.Logbook;

public interface Validator {
	
	boolean validate(Logbook logbook);

}
