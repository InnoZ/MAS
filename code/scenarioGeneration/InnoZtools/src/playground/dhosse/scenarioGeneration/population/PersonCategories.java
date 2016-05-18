package playground.dhosse.scenarioGeneration.population;

import java.util.HashSet;
import java.util.Set;

public class PersonCategories {
	
	public static PersonClassification createSimpleCategories(){
		return PersonClassification.SIMPLE_CLASSES;
	}
	
	public static PersonClassification createDetailedCategories(){
		return PersonClassification.DETAILED_CLASSES;
	}

	enum PersonClassification{
		
		SIMPLE_CLASSES(new PersonClass[]{
				new PersonClass(1, 18, 65, true, true, true),//Berufstätige mit Pkw und Führerschein
				new PersonClass(1, 18, 65, true, true, false),//Berufstätige mit Pkw ohne Führerschein
				new PersonClass(2, 18, 65, true, false, true),//Berufstätige ohne Pkw mit Führerschein
				new PersonClass(2, 18, 65, true, false, false),//Berufstätige ohne Pkw ohne Führerschein
				new PersonClass(3, 18, 65, false, true, true),//Nicht-Berufstätige mit Pkw mit Führerschein
				new PersonClass(3, 18, 65, false, true, false),//Nicht-Berufstätige mit Pkw ohne Führerschein
				new PersonClass(4, 18, 65, false, false, true),//Nicht-Berufstätige ohne Pkw mit Führerschein
				new PersonClass(4, 18, 65, false, false, false),//Nicht-Berufstätige ohne Pkw ohne Führerschein
				new PersonClass(5, 18, 65, false, false, false),//Studenten ohne Pkw ohne Führerschein
				new PersonClass(6, 18, 65, false, false, false),//Azubis ohne Pkw ohne Führerschein
				new PersonClass(7, 0, 6, false, false, false), //Kinder < 6
				new PersonClass(8, 7, 10, false, false, false),//Schüler 7 - 10
				new PersonClass(9, 11, 18, false, false, false) //Schüler > 11
		}),
		
		DETAILED_CLASSES(new PersonClass[]{
				
				/*
				 * TODO jeweils mit / ohne Auto & Führerschein
				 * Berufstätiger Vollzeit (18-65)
				 * Berufstätiger Teilzeit (18-65)
				 * Azubi (16-?)
				 * Schüler (7-10)
				 * Schüler (11-18)
				 * Student (18-?)
				 * Kind (0-6)
				 * Hausfrau / -mann (18-65)
				 * Rentner (65+)
				 */
				
				new PersonClass(1, 18, 65, true, true, true),//Berufstätige mit Pkw und Führerschein
				new PersonClass(1, 18, 65, true, true, false),//Berufstätige mit Pkw ohne Führerschein
				new PersonClass(2, 18, 65, true, false, true),//Berufstätige ohne Pkw mit Führerschein
				new PersonClass(2, 18, 65, true, false, false),//Berufstätige ohne Pkw ohne Führerschein
				new PersonClass(3, 18, 65, false, true, true),//Nicht-Berufstätige mit Pkw mit Führerschein
				new PersonClass(3, 18, 65, false, true, false),//Nicht-Berufstätige mit Pkw ohne Führerschein
				new PersonClass(4, 18, 65, false, false, true),//Nicht-Berufstätige ohne Pkw mit Führerschein
				new PersonClass(4, 18, 65, false, false, false),//Nicht-Berufstätige ohne Pkw ohne Führerschein
				new PersonClass(5, 18, 65, false, true, false),//Studenten mit Pkw
				new PersonClass(6, 18, 65, false, false, false),//Studenten ohne Pkw
				new PersonClass(7, 18, 65, false, true, false),//Azubis mit Pkw
				new PersonClass(8, 18, 65, false, true, false),//Azubis ohne Pkw
				new PersonClass(9, 0, 6, false, false, false), //Kinder < 6
				new PersonClass(10, 7, 10, false, false, false),//Schüler 7 - 10
				new PersonClass(11, 11, 18, false, false, false), //Schüler > 11 mit Pkw
				new PersonClass(12, 11, 18, false, false, false) //Schüler > 11 ohne Pkw
		});
		
		private Set<PersonClass> personClasses;
		
		private PersonClassification(PersonClass[] classes) {
			
			this.personClasses = new HashSet<>();
			for(PersonClass clazz : classes){
				this.personClasses.add(clazz);
			}
			
		}
		
		public Set<PersonClass> getPersonClasses(){
			
			return this.personClasses;
			
		}
		
		static class PersonClass{
			
			public static final int INDEX_EMPLOYED_W_CAR = 1;
			public static final int INDEX_EMPLOYED_WO_CAR = 2;
			
			private int id;
			private int fromAge;
			private int toAge;
			private boolean employed;
			private boolean carAvail;
			private boolean license;
			
			PersonClass(int id, int fromAge, int toAge, boolean employed, boolean carAvail, boolean license){
				
				this.id = id;
				this.fromAge = fromAge;
				this.toAge = toAge;
				this.employed = employed;
				this.carAvail = carAvail;
				
			}

			public int getId() {
				return id;
			}

			public int getFromAge() {
				return fromAge;
			}

			public int getToAge() {
				return toAge;
			}

			public boolean isEmployed() {
				return employed;
			}

			public boolean isCarAvail() {
				return carAvail;
			}
			
			public boolean hasDrivingLicense(){
				return license;
			}
			
		}
		
	}
	
}
