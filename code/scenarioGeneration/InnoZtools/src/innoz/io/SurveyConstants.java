package innoz.io;

public class SurveyConstants {

	private enum SurveyConstant{
		
		HHID("hhid","HHNR"),
		HHWEIGHT("hh_gew", "GEWICHT_HH"),
		HHSIZE("h02", "V_ANZ_PERS"),
		HHVEHICLES("h04_3", "V_ANZ_PKW_PRIV"),
		HHINCOME("hheink", "V_EINK"),
		
		PID("pid", "PNR"),
		PWEIGHT("p_gew", ""),
		PSEX("hp_sex", ""),
		PAGE("hp_alter", ""),
		PCARAVAIL("p01_1",""),
		PLICENSE("hp_pkwfs",""),
		PEMPLOYED("hp_beruf",""),
		PCSUSER(null,""),
		
		WID("wid",""),
		WWEIGHT("w_gew",""),
		WDEPH("st_std",""),
		WDEPM("st_min",""),
		WARRH("en_std",""),
		WARRM("en_min",""),
		WTD("wegkm_k",""),
		WPUR("w04",""),
		WMODE("hvm","");
		
		
		private String midName;
		private String srvName;
		
		SurveyConstant(String midName, String srvName){
			
			this.midName = midName;
			this.srvName = srvName;
			
		}
		
		private String getName(String namespace){
			
			if("mid".equalsIgnoreCase(namespace)){
				
				return this.midName;
				
			} else {
				
				return this.srvName;
				
			}
			
		}
		
	}
	
	final String namespace;
	
	public SurveyConstants(String namespace) {
		
		this.namespace = namespace;
		
	}
	
	public String getHouseholdId(){
		
		return SurveyConstant.HHID.getName(this.namespace);
		
	}
	
	public String getHouseholdWeight(){
		
		return SurveyConstant.HHWEIGHT.getName(this.namespace);
		
	}
	
	public String getHouseholdSize(){
		
		return SurveyConstant.HHSIZE.getName(this.namespace);
		
	}
	
	public String getNumberOfHouseholdVehicles(){
		
		return SurveyConstant.HHVEHICLES.getName(this.namespace);
		
	}
	
	public String getHouseholdIncomePerMonth(){
		
		return SurveyConstant.HHINCOME.getName(this.namespace);
		
	}
	
	public String getPersonId(){
		
		return SurveyConstant.PID.getName(this.namespace);
		
	}
	
	public String getPersonWeight(){
		
		return SurveyConstant.PWEIGHT.getName(this.namespace);
		
	}
	
	public String getPersonSex(){
		
		return SurveyConstant.PSEX.getName(this.namespace);
		
	}
	
	public String getPersonAge(){
		
		return SurveyConstant.PAGE.getName(this.namespace);
		
	}
	
	public String getPersonCarAvailability(){
		
		return SurveyConstant.PCARAVAIL.getName(this.namespace);
		
	}
	
	public String getPersonDrivingLicense(){
		
		return SurveyConstant.PLICENSE.getName(this.namespace);
		
	}
	
	public String getPersonEmplyment(){
		
		return SurveyConstant.PEMPLOYED.getName(this.namespace);
		
	}
	
	public String getPersonIsCarsharingUser(){
		
		return SurveyConstant.PCSUSER.getName(this.namespace);
		
	}
	
	public String getWayId(){
		
		return SurveyConstant.WID.getName(this.namespace);
		
	}
	
	public String getWayWeight(){
		
		return SurveyConstant.WWEIGHT.getName(this.namespace);
		
	}
	
	public String getWayDepartureHour(){
		
		return SurveyConstant.WDEPH.getName(this.namespace);
		
	}
	
	public String getWayDepartureMinute(){
		
		return SurveyConstant.WDEPM.getName(this.namespace);
		
	}
	
	public String getWayArrivalHour(){
		
		return SurveyConstant.WARRH.getName(this.namespace);
		
	}
	
	public String getWayArrivalMinute(){
		
		return SurveyConstant.WARRM.getName(this.namespace);
		
	}
	
	public String getWayTravelDistance(){
		
		return SurveyConstant.WTD.getName(this.namespace);
		
	}
	
	public String getWayMode(){
	
		return SurveyConstant.WMODE.getName(this.namespace);
		
	}
	
	public String getWayPurpose(){
		
		return SurveyConstant.WPUR.getName(this.namespace);
		
	}
	
}
