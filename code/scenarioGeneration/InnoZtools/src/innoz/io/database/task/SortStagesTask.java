package innoz.io.database.task;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import innoz.io.database.handler.Logbook;
import innoz.io.database.handler.SurveyStage;

public class SortStagesTask extends LogbookTask {

	private Comparator<String> intComparator = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			
			int i1 = Integer.parseInt(o1);
			int i2 = Integer.parseInt(o2);
			
			return i1 - i2;
			
		}
	};
	
	@Override
	public void apply(Logbook logbook) {

		SortedMap<String, SurveyStage> stages = new TreeMap<>(intComparator);
		
		for(SurveyStage stage : logbook.getStages()){
			stages.put(stage.getIndex(), stage);
		}
		
		for(SurveyStage stage : stages.values()){
			logbook.getStages().remove(stage);
		}
		
		for(SurveyStage stage : stages.values()){
			logbook.getStages().add(stage);
		}
		
	}

}