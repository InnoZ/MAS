package playground.dhosse.scenarios.generic.population.io.mid;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import playground.dhosse.scenarios.generic.population.io.mid.MiDParser.Subtour;

public class MiDPlan {

	private LinkedList<MiDPlanElement> planElements;
	private int dayOfTheWeek;
	private double weight = 0d;
	
	private String mainActType = null;
	private int mainActId = -1;
	private int mainActIndex = -1;
	private int homeIndex = -1;
	boolean setHomeIndex = false;
	
	boolean firstActEqualsLastAct = false;
	
	private List<Subtour> subtours;
	
	private double longestLeg = 0.;
	
	public MiDPlan(){
		
		this.planElements = new LinkedList<>();
		this.subtours = new ArrayList<Subtour>();
		
	}

	public LinkedList<MiDPlanElement> getPlanElements() {
		
		return this.planElements;
		
	}

	public int getDayOfTheWeek() {
		
		return this.dayOfTheWeek;
		
	}

	public void setDayOfTheWeek(int dayOfTheWeek) {
		
		this.dayOfTheWeek = dayOfTheWeek;
		
	}
	
	public double getWeigt(){
		
		return this.weight;
		
	}
	
	public void incrementWeight(double v){
		
		this.weight = v;
		
	}
	
	public String getMainActType(){
		return this.mainActType;
	}
	
	public void setMainActType(String type){
		this.mainActType = type;
	}
	
	public double getLongestLeg(){
		return this.longestLeg;
	}
	
	public void setLongestLeg(double d){
		this.longestLeg = d;
	}

	public int getMainActId(){
		return this.mainActId;
	}
	
	public void setMainActId(int i){
		this.mainActId = i;
	}
	
	public List<Subtour> getSubtours(){
		return this.subtours;
	}
	
	public void setSubtours(List<Subtour> subtours){
		this.subtours = subtours;
	}
	
	public int getHomeIndex(){
		return this.homeIndex;
	}
	
	public void overrideHomeIndex(int i){
		this.homeIndex = i;
	}
	
	public void setHomeIndex(int i){
		if(!setHomeIndex){
			this.homeIndex = i;
			setHomeIndex = true;
		}
	}
	
	public void setFirstActEqualsLastAct(boolean b){
		this.firstActEqualsLastAct = b;
		((MiDActivity)this.planElements.get(this.planElements.size()-1)).setId(this.planElements.get(0).getId());
	}
	
	public boolean firstActEqualsLastAct(){
		return this.firstActEqualsLastAct;
	}
	
	public int getMainActIndex(){
		return this.mainActIndex;
	}
	
	public void setMainActIndex(int i){
		this.mainActIndex = i;
	}
	
}