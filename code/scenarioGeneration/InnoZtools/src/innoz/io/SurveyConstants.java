package innoz.io;

public class SurveyConstants {

	private enum SurveyConstant{
		
		WTAG("stichtag", "stichtag_wtag"),
		DATEST("st_dat", null),
		DATEEN("en_dat", null),
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
		PG12("pergrup1", null),
		PLPHASE("lebensph", null),
		
		WID("wid", "wnr"),
		WWEIGHT("w_gew", "gewicht_w"),
		WDEP("st_time", "e_beginn"),
		WDEPH("st_std", "v_beginn_stunde"),
		WDEPM("st_min", "v_beginn_minute"),
		WARR("en_time", "e_ankunft"),
		WARRH("en_std", "v_ankunft_stunde"),
		WARRM("en_min", "v_ankunft_minute"),
		WTD("wegkm_k", "v_laenge"),
		WTT("wegmin_k", "e_dauer"),
		WPUR("w04", "v_zweck"),
		WPURD("w04_dzw", null),
		WMODE("hvm", "e_hvm"),
		WSOURCE("w01", "v_start_lage"),
		WSINK("w13", "v_ziel_lage"),
		
		VID("vmid", null),
		VFUEL("h048", null),
		VSEG("seg_kba", null);
		
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

	private final String PSEX_MALE = "1";

	final String namespace;
	
	public SurveyConstants(String namespace) {
		
		this.namespace = namespace;
		
	}
	
	public String getNamespace(){
		
		return this.namespace;
		
	}
	
	public String dayOfTheWeek(){
		
		return SurveyConstant.WTAG.getName(this.namespace);
		
	}
	
	public String startDate(){
		
		return SurveyConstant.DATEST.getName(this.namespace);
		
	}
	
	public String endDate(){
		
		return SurveyConstant.DATEEN.getName(this.namespace);
		
	}
	
	public String bundesland(){
		
		return SurveyConstant.BLAND.getName(this.namespace);
		
	}
	
	public String regionType(){
		
		return SurveyConstant.RTYP.getName(this.namespace);
		
	}
	
	public String householdId(){
		
		return SurveyConstant.HHID.getName(this.namespace);
		
	}
	
	public String householdWeight(){
		
		return SurveyConstant.HHWEIGHT.getName(this.namespace);
		
	}
	
	public String householdSize(){
		
		return SurveyConstant.HHSIZE.getName(this.namespace);
		
	}
	
	public String numberOfHouseholdVehicles(){
		
		return SurveyConstant.HHVEHICLES.getName(this.namespace);
		
	}
	
	public String householdIncomePerMonth(){
		
		return SurveyConstant.HHINCOME.getName(this.namespace);
		
	}
	
	public String personId(){
		
		return SurveyConstant.PID.getName(this.namespace);
		
	}
	
	public String personWeight(){
		
		return SurveyConstant.PWEIGHT.getName(this.namespace);
		
	}
	
	public String personSex(){
		
		return SurveyConstant.PSEX.getName(this.namespace);
		
	}
	
	public String personAge(){
		
		return SurveyConstant.PAGE.getName(this.namespace);
		
	}
	
	public String personCarAvailability(){
		
		return SurveyConstant.PCARAVAIL.getName(this.namespace);
		
	}
	
	public String personDrivingLicense(){
		
		return SurveyConstant.PLICENSE.getName(this.namespace);
		
	}
	
	public String personEmployment(){
		
		return SurveyConstant.PEMPLOYED.getName(this.namespace);
		
	}
	
	public String personIsCarsharingUser(){
		
		return SurveyConstant.PCSUSER.getName(this.namespace);
		
	}
	
	public String wayId(){
		
		return SurveyConstant.WID.getName(this.namespace);
		
	}
	
	public String wayWeight(){
		
		return SurveyConstant.WWEIGHT.getName(this.namespace);
		
	}
	
	public String wayDeparture(){
		
		return SurveyConstant.WDEP.getName(this.namespace);
		
	}
	
	public String wayDepartureHour(){
		
		return SurveyConstant.WDEPH.getName(this.namespace);
		
	}
	
	public String wayDepartureMinute(){
		
		return SurveyConstant.WDEPM.getName(this.namespace);
		
	}
	
	public String wayArrival(){
		
		return SurveyConstant.WARR.getName(this.namespace);
		
	}
	
	public String wayArrivalHour(){
		
		return SurveyConstant.WARRH.getName(this.namespace);
		
	}
	
	public String wayArrivalMinute(){
		
		return SurveyConstant.WARRM.getName(this.namespace);
		
	}
	
	public String wayTravelDistance(){
		
		return SurveyConstant.WTD.getName(this.namespace);
		
	}
	
	public String wayTravelTime(){
		
		return SurveyConstant.WTT.getName(this.namespace);
		
	}
	
	public String wayMode(){
	
		return SurveyConstant.WMODE.getName(this.namespace);
		
	}
	
	public String wayPurpose(){
		
		return SurveyConstant.WPUR.getName(this.namespace);
		
	}
	
	public String waySource(){
		
		return SurveyConstant.WSOURCE.getName(this.namespace);
		
	}
	
	public String waySink(){
		
		return SurveyConstant.WSINK.getName(this.namespace);
		
	}
	
	public String personGroup(){
		
		return SurveyConstant.PG12.getName(this.namespace);
		
	}

	public String personLifephase(){
		
		return SurveyConstant.PLPHASE.getName(this.namespace);
		
	}
	
	public String wayDetailedPurpose(){
		
		return SurveyConstant.WPURD.getName(this.namespace);
		
	}
	
	public String vehicleId(){
		
		return SurveyConstant.VID.getName(this.namespace);
		
	}
	
	public String vehicleFuelType(){
		
		return SurveyConstant.VFUEL.getName(this.namespace);
		
	}
	
	public String vehicleSegmentKBA(){
		
		return SurveyConstant.VSEG.getName(this.namespace);
		
	}
	
	public String getSexMale(){
		
		return this.PSEX_MALE;
		
	}
	
}
