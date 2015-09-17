package simulation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;


/**
 *  A class that reads a csv-file including commuterData and keeps only the information about commuters 
 *  traveling from Garmisch to Bayern
 */
public class ReadAuspendler {
	
	/**
	 * create a reader-object and start the run-method
	 */
	public static void main(String[] args) {
		ReadAuspendler ra = new ReadAuspendler();
		System.out.println("AUSPENDLERDATEN: ");
		System.out.println();
		
		Map<String,Integer> gegebeneSchluessel = ra.run("given");
		System.out.println("Anzahl gegebener, verschiedener GemeindeSchluessel = " + gegebeneSchluessel.size());
		System.out.println("gegebene GemeindeSchluessel.entrySet =  " + gegebeneSchluessel.entrySet());
		System.out.println();
		Map<String,Integer> uebrigeSchluessel = ra.run("other");
		System.out.println("Anzahl gegebener Kreisschluessel ohne genaue Angabe zu Gemeinden = " + uebrigeSchluessel.size());
		System.out.println("Kreisschluessel-fuer-uebrige-Gemeinden.entrySet = " + uebrigeSchluessel.entrySet());

	}

	
	/**
	 * this method reads a csv-file with the aid of the pendlerParser-class.
	 * 
	 * @return a map containing special relations of departure- and arrival-locations in the form 
	 * "AbfahrtsGemeindeschluessel - AnkunftsGemeindeschluessel" as map-key and the corresponding
	 * number of commuters to that relation as map-value.
	 */
	public Map<String,Integer> run(String string) {

		ParseAuspendler pa = new ParseAuspendler();

		read( "input/Garmisch_Auspendler.csv",
				pa);
//	System.out.println("Anzahl aller Auspendler:  " + pa.allCommuters  + "  , Anzahl Auspendler aus uebrigen Gemeinden  " + pa.uebrigeGemeindenCommuters);

		if(string.equals("given")){
		return pa.relationsOfGivenMunicipalities;
		} else{
				return pa.relationsOfOtherMunicipalities;
			}
	}
	
	/**
	 * this method reads a tabular-file using the MatSim-TabularFileHandler. 
	 * @param fileName name of the file that shall be read. Should be in .txt format.
	 * @param handler
	 */
	private void read(String fileName, TabularFileHandler handler) {
		TabularFileParserConfig config = new TabularFileParserConfig();
		config.setDelimiterRegex(";");
		config.setFileName(fileName);
		config.setCommentRegex("#");
		new TabularFileParser().parse(config, handler);
	}
}


class ParseAuspendler implements TabularFileHandler {
	String currentFrom = "";
	String currentTo = "";
	String current = "";
	// Anzahl aller Auspendler
	int allCommuters = 0;
	// Anzahl der Auspendler aus "uebrigen Gemeinden des Kreises"
	int uebrigeGemeindenCommuters = 0;


	Map<String, Integer> relationsOfGivenMunicipalities;
	Map<String, Integer> relationsOfOtherMunicipalities;


	// Default-Konstruktor
	ParseAuspendler() {
		this.relationsOfGivenMunicipalities = new TreeMap<String, Integer>();
		this.relationsOfOtherMunicipalities = new TreeMap<String, Integer>();
	}

	/*
	 * Zeilenparser (non-Javadoc)
	 * @see org.matsim.core.utils.io.tabularFileParser.TabularFileHandler#startRow(java.lang.String[])
	 */
	@Override
	public void startRow(String[] row) {

		/*
		 * beachte nur Gemeindeschl��ssel, die mindestens 5 Ziffern enthalten
		 */
		if (row[0].length() >= 5 || row[2].length() >= 5) {

		  current = row[2];
			
		  /*
		   * entscheide, ob es sich um den Gemeindeschl��ssel des aktuellen Abfahrtsortes 
		   * oder eines Ankunftsortes handelt
		   */
			if (current.isEmpty()) {
				currentFrom = row[0];
			} else {
				  if(!current.equals("09180")){
					  currentTo = current;
				}
				
				/* 
				 * Auspendler von Garmisch nach Bayern
				 */
				
				if(getBundesland(currentTo).equals("09") 
					/*
					 * falls nur Auspendler nach Bayern(ohne Garmisch) ber��cksichtigt werden sollen:
					 */ 
					  && (!getKreis(currentTo).equals("09180"))
					  /*
					   * St��rzeilen:
					   */
					  	&& (!currentTo.substring(currentTo.length()-3).equals("000"))
					  	/*
					  	 * PROBLEM: Landkreise, die als 5-stelliger "Gemeindeschl��ssel" in der Pendlerliste auftauchen,
					  	 * m��ssen randomisiert auf "ihre" Gemeinden aufgeteilt werden! im Moment werden sie einfach 
					  	 * ignoriert...
					  	 */
					//  	&& (currentTo.length() > 5)
									&& (!row[3].contains("Gemeinden")) ){

					String key = currentFrom + " - " + currentTo;
					String anzahl = row[4];
					
					// eliminiere die tausender-trennzeichen
					for (int i = 0; i < anzahl.length(); i++) {
						// char 46 = "."
						if(anzahl.charAt(i)==46){
							anzahl = anzahl.substring(0, i) + anzahl.substring(i+1);
						}
					}
					Integer anzahlPendler = Integer.parseInt(anzahl);
					allCommuters += anzahlPendler;
					if(currentTo.length() == 5){
						uebrigeGemeindenCommuters += anzahlPendler;
						this.relationsOfOtherMunicipalities.put(key, anzahlPendler);
					} else{
							this.relationsOfGivenMunicipalities.put(key, anzahlPendler);
					  }
				}
			}
		}
	
}

		private String getKreis(String gemeinde) {
			return gemeinde.substring(0, 5);
			// Kreisschlueesel sind fuenfstellig
		}

		private String getBundesland(String gemeinde) {
			return gemeinde.substring(0, 2);
			// Bundesschlueesel sind dreistellig
		}

}