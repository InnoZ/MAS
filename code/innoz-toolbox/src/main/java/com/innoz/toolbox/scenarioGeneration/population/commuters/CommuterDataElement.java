package com.innoz.toolbox.scenarioGeneration.population.commuters;

/**
 * @author dhosse
 *
 */
public class CommuterDataElement {
	private String fromId;
	private String toId;
	private String fromName;
	private String toName;
	private int commuters;
	private int trainees;
	private int adminLevel;
	private double shareOfMaleCommuters;
	
	private int malePersonsCreated = 0;
	private int femalePersonsCreated = 0;
	
	public CommuterDataElement(String fromId, String fromName,  String toId, String toName,  int commuters){
		this.fromId = fromId;
		this.toId = toId;
		this.commuters = commuters;
		this.fromName = fromName;
		this.toName = toName;
		this.adminLevel = fromId.length();
	}
	
	public CommuterDataElement(String fromId, String fromName,  String toId, String toName,  int commuters,
			double shareOfMaleCommuters){
		this.fromId = fromId;
		this.toId = toId;
		this.commuters = commuters;
		this.fromName = fromName;
		this.toName = toName;
		this.adminLevel = fromId.length();
		this.shareOfMaleCommuters = shareOfMaleCommuters;
	}
	
	public CommuterDataElement(String fromId, String fromName,  String toId, String toName,  int commuters,
			double shareOfMaleCommuters, int nTrainees){
		this.fromId = fromId;
		this.toId = toId;
		this.commuters = commuters;
		this.fromName = fromName;
		this.toName = toName;
		this.adminLevel = fromId.length();
		this.shareOfMaleCommuters = shareOfMaleCommuters;
		this.trainees = nTrainees;
	}
	
	

	public String getFromName() {
		return fromName;
	}



	public void setFromName(String fromName) {
		this.fromName = fromName;
	}



	public String getToName() {
		return toName;
	}



	public void setToName(String toName) {
		this.toName = toName;
	}



	public String getFromId() {
		return fromId;
	}

	public String getToId() {
		return toId;
	}

	public int getNumberOfCommuters() {
		return commuters;
	}
	
	public int getNumberOfTrainees(){
		return this.trainees;
	}
	
	public String toString(){
		return ("F: "+fromId+" T: "+toId+" C: "+commuters);
	}
	
	public int getAdminLevel(){
		return this.adminLevel;
	}
	
	public double getShareOfMaleCommuters(){
		return this.shareOfMaleCommuters;
	}
	
	public int getMalePersonsCreated(){
		
		return this.malePersonsCreated;
		
	}
	
	public void incMalePersonsCreated(){
		this.malePersonsCreated++;
	}
	
	public int getFemalePersonsCreated(){
		
		return this.femalePersonsCreated;
		
	}
	
	public void incFemalePersonsCreated(){
		this.femalePersonsCreated++;
	}

}