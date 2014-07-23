package garmisch;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;


/**
 *  A class that reads a csv-file including commuterData and keeps only the information about commuters 
 *  traveling within Garmisch.
 */
public class ReadPendler {
	
	/**
	 * create a reader-object and start the run-method
	 */
	public static void main(String[] args) {
		ReadPendler rp = new ReadPendler();
		rp.run();
	}

	
	/**
	 * this method reads a csv-file with the aid of the pendlerParser-class.
	 * 
	 * @return a map containing special relations of departure- and arrival-locations and the corresponding
	 * number of commuters to that relation.
	 */
	public Map<String,Integer> run() {

		GarmischPendlerParser gp = new GarmischPendlerParser();

		read( "/Users/mini/Documents/MATSim/workspace/MyMatsimProject/inputGarmisch/Garmisch_Auspendler.csv",
				gp);
		
// 		print out relationsMap
//		for (Entry<String, Integer> e : gp.relations.entrySet()) {
//			System.out.println(e.getKey() + "\t" + e.getValue());
//		}
//		print out number of all commuters
//		System.out.println("allCommuters:  " + gp.allCommuters);
		
		return gp.relations;
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


class GarmischPendlerParser implements TabularFileHandler {
	String currentFrom = "";
	String currentTo = "";
	String current = "";
	int allCommuters = 0;

	Map<String, Integer> relations;

	// Default-Konstruktor
	GarmischPendlerParser() {
		this.relations = new TreeMap<String, Integer>();
	}

	/*
	 * Zeilenparser (non-Javadoc)
	 * @see org.matsim.core.utils.io.tabularFileParser.TabularFileHandler#startRow(java.lang.String[])
	 */
	@Override
	public void startRow(String[] row) {

		/*
		 * beachte nur Gemeindeschlüssel, die mindestens 5 Ziffern enthalten
		 */
		if (row[0].length() >= 5 || row[2].length() >= 5) {

		  current = row[2];
			
		  /*
		   * entscheide, ob es sich um den Gemeindeschlüssel des aktuellen Abfahrtsortes 
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
				
				// if(getBundesland(currentTo).equals("09") &&
				// (!row[3].contains("Gemeinden"))
				// && (!row[3].contains("Regierungsbezirke")) ){

				  /*
				   *  Aus-bzw. Einpendler von Garmisch nach Garmisch (hierfür nur eine Datei verwenden,
				   *  da innerhalb Garmischs die Ein- & Auspendler.csv die gleiche Aussage machen
				   */
				if (getKreis(currentTo).equals("09180")
						&& (!row[3].contains("Gemeinden"))
						&& (!row[3].contains("Regierungsbezirke"))) {

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
					
					this.relations.put(key, anzahlPendler);
					
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
