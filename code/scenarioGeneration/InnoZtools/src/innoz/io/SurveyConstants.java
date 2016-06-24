package innoz.io;

public class SurveyConstants {

	private enum SurveyConstant{
		
		WTAG("stichtag", "stichtag_wtag"),
		BLAND("bland", null),
		RTYP("rtypd7", null),
		
		HHID("hhid","hhnr"),
		HHWEIGHT("hh_gew", "gewicht_hh"),
		HHSIZE("h02", "v_anz_pers"),
		HHVEHICLES("h04_3", "v_anz_pkw_priv"),
		HHINCOME("hheink", "v_eink"),
		
		PID("pid", "pnr"),
		PWEIGHT("p_gew", "gewicht_p"),
		PSEX("hp_sex", "v_geschlecht"),
		PAGE("hp_alter", "v_alter"),
		PCARAVAIL("p01_1", "v_pkw_verfueg"),
		PLICENSE("hp_pkwfs", "v_fuehr_pkw"),
		PEMPLOYED("hp_beruf", "v_erw"),
		PCSUSER(null, "v_carshare"),
		
		WID("wid", "wnr"),
		WWEIGHT("w_gew", "gewicht_w"),
		WDEPH("st_std", "v_beginn_stunde"),
		WDEPM("st_min", "v_beginn_minute"),
		WARRH("en_std", "v_ankunft_stunde"),
		WARRM("en_min", "v_ankunft_minute"),
		WTD("wegkm_k", "v_laenge"),
		WPUR("w04", "v_zweck"),
		WMODE("hvm", "e_hvm");
		
		
		private String midName;
		private String srvName;
		
		SurveyConstant(String midName, String srvName){
			
			this.midName = midName;
			this.srvName = srvName;
			
		}
		
		private String Name(String namespace){
			
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
	
	public String dayOfTheWeek(){
		
		return SurveyConstant.WTAG.Name(this.namespace);
		
	}
	
	public String bundesland(){
		
		return SurveyConstant.BLAND.Name(this.namespace);
		
	}
	
	public String regionType(){
		
		return SurveyConstant.RTYP.Name(this.namespace);
		
	}
	
	public String householdId(){
		
		return SurveyConstant.HHID.Name(this.namespace);
		
	}
	
	public String householdWeight(){
		
		return SurveyConstant.HHWEIGHT.Name(this.namespace);
		
	}
	
	public String householdSize(){
		
		return SurveyConstant.HHSIZE.Name(this.namespace);
		
	}
	
	public String numberOfHouseholdVehicles(){
		
		return SurveyConstant.HHVEHICLES.Name(this.namespace);
		
	}
	
	public String householdIncomePerMonth(){
		
		return SurveyConstant.HHINCOME.Name(this.namespace);
		
	}
	
	public String personId(){
		
		return SurveyConstant.PID.Name(this.namespace);
		
	}
	
	public String personWeight(){
		
		return SurveyConstant.PWEIGHT.Name(this.namespace);
		
	}
	
	public String personSex(){
		
		return SurveyConstant.PSEX.Name(this.namespace);
		
	}
	
	public String personAge(){
		
		return SurveyConstant.PAGE.Name(this.namespace);
		
	}
	
	public String personCarAvailability(){
		
		return SurveyConstant.PCARAVAIL.Name(this.namespace);
		
	}
	
	public String personDrivingLicense(){
		
		return SurveyConstant.PLICENSE.Name(this.namespace);
		
	}
	
	public String personEmployment(){
		
		return SurveyConstant.PEMPLOYED.Name(this.namespace);
		
	}
	
	public String personIsCarsharingUser(){
		
		return SurveyConstant.PCSUSER.Name(this.namespace);
		
	}
	
	public String wayId(){
		
		return SurveyConstant.WID.Name(this.namespace);
		
	}
	
	public String wayWeight(){
		
		return SurveyConstant.WWEIGHT.Name(this.namespace);
		
	}
	
	public String wayDepartureHour(){
		
		return SurveyConstant.WDEPH.Name(this.namespace);
		
	}
	
	public String wayDepartureMinute(){
		
		return SurveyConstant.WDEPM.Name(this.namespace);
		
	}
	
	public String wayArrivalHour(){
		
		return SurveyConstant.WARRH.Name(this.namespace);
		
	}
	
	public String wayArrivalMinute(){
		
		return SurveyConstant.WARRM.Name(this.namespace);
		
	}
	
	public String wayTravelDistance(){
		
		return SurveyConstant.WTD.Name(this.namespace);
		
	}
	
	public String wayMode(){
	
		return SurveyConstant.WMODE.Name(this.namespace);
		
	}
	
	public String wayPurpose(){
		
		return SurveyConstant.WPUR.Name(this.namespace);
		
	}
	
}
