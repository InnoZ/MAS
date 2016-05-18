package playground.dhosse.scenarioGeneration.population.surveys;

public class SurveyPlanActivity implements SurveyPlanElement {

	private int id = 0;
	
	private double startTime;
	private double endTime;
	private String actType;
	
	private boolean inHomeCell = false;
	
	private int priority;
	
	public SurveyPlanActivity(String actType){
		this.actType = actType;
	}
	
	@Override
	public int getId() {
		return this.id;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public String getActType() {
		return actType;
	}

	public void setActType(String actType) {
		this.actType = actType;
	}
	
	public int getPriority(){
		return this.priority;
	}
	
	public void setPriority(int i){
		this.priority = i;
	}
	
	public boolean isInHomeCell(){
		return this.inHomeCell;
	}
	
	public void setInHomeCell(boolean b){
		this.inHomeCell = b;
	}
	
	public void setId(int i){
		this.id = i;
	}

}
