package com.innoz.toolbox.io;

public final class SurveyConstants {

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
		WSID("wsid", "wnr"),
		WWEIGHT("w_gew", "gewicht_w"),
		WDEPD("st_dat", ""),
		WDEP("st_time", "e_beginn"),
		WDEPH("st_std", "v_beginn_stunde"),
		WDEPM("st_min", "v_beginn_minute"),
		WARR("en_time", "e_ankunft"),
		WARRD("en_dat", ""),
		WARRH("en_std", "v_ankunft_stunde"),
		WARRM("en_min", "v_ankunft_minute"),
		WTD("wegkm_k", "v_laenge"),
		WTT("wegmin_k", "e_dauer"),
		WPUR("w04", "v_zweck"),
		WPURD("w04_dzw", null),
		WMODE("hvm", "e_hvm"),
		WSOURCE("w01", "v_start_lage"),
		WSINK("w13", "v_ziel_lage"),
		
		MOBILE("mobil", ""),
		
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

	private final static String PSEX_MALE = "1";
	
	public static String dayOfTheWeek(String namespace){
		
		return SurveyConstant.WTAG.getName(namespace);
		
	}
	
	public static String startDate(String namespace){
		
		return SurveyConstant.DATEST.getName(namespace);
		
	}
	
	public static String endDate(String namespace){
		
		return SurveyConstant.DATEEN.getName(namespace);
		
	}
	
	public static String bundesland(String namespace){
		
		return SurveyConstant.BLAND.getName(namespace);
		
	}
	
	public static String regionType(String namespace){
		
		return SurveyConstant.RTYP.getName(namespace);
		
	}
	
	public static String householdId(String namespace){
		
		return SurveyConstant.HHID.getName(namespace);
		
	}
	
	public static String householdWeight(String namespace){
		
		return SurveyConstant.HHWEIGHT.getName(namespace);
		
	}
	
	public static String householdSize(String namespace){
		
		return SurveyConstant.HHSIZE.getName(namespace);
		
	}
	
	public static String numberOfHouseholdVehicles(String namespace){
		
		return SurveyConstant.HHVEHICLES.getName(namespace);
		
	}
	
	public static String householdIncomePerMonth(String namespace){
		
		return SurveyConstant.HHINCOME.getName(namespace);
		
	}
	
	public static String personId(String namespace){
		
		return SurveyConstant.PID.getName(namespace);
		
	}
	
	public static String personWeight(String namespace){
		
		return SurveyConstant.PWEIGHT.getName(namespace);
		
	}
	
	public static String personSex(String namespace){
		
		return SurveyConstant.PSEX.getName(namespace);
		
	}
	
	public static String personAge(String namespace){
		
		return SurveyConstant.PAGE.getName(namespace);
		
	}
	
	public static String personCarAvailability(String namespace){
		
		return SurveyConstant.PCARAVAIL.getName(namespace);
		
	}
	
	public static String personDrivingLicense(String namespace){
		
		return SurveyConstant.PLICENSE.getName(namespace);
		
	}
	
	public static String personEmployment(String namespace){
		
		return SurveyConstant.PEMPLOYED.getName(namespace);
		
	}
	
	public static String personIsCarsharingUser(String namespace){
		
		return SurveyConstant.PCSUSER.getName(namespace);
		
	}

	public static String sortedWayId(String namespace){
		
		return SurveyConstant.WSID.getName(namespace);
		
	}
	
	public static String wayId(String namespace){
		
		return SurveyConstant.WID.getName(namespace);
		
	}
	
	public static String wayWeight(String namespace){
		
		return SurveyConstant.WWEIGHT.getName(namespace);
		
	}
	
	public static String wayDeparture(String namespace){
		
		return SurveyConstant.WDEP.getName(namespace);
		
	}
	
	public static String wayDepartureDay(String namespace) {
		
		return SurveyConstant.WDEPD.getName(namespace);
		
	}
	
	public static String wayDepartureHour(String namespace){
		
		return SurveyConstant.WDEPH.getName(namespace);
		
	}
	
	public static String wayDepartureMinute(String namespace){
		
		return SurveyConstant.WDEPM.getName(namespace);
		
	}
	
	public static String wayArrival(String namespace){
		
		return SurveyConstant.WARR.getName(namespace);
		
	}
	
	public static String wayArrivalDay(String namespace) {
		
		return SurveyConstant.WARRD.getName(namespace);
		
	}
	
	public static String wayArrivalHour(String namespace){
		
		return SurveyConstant.WARRH.getName(namespace);
		
	}
	
	public static String wayArrivalMinute(String namespace){
		
		return SurveyConstant.WARRM.getName(namespace);
		
	}
	
	public static String wayTravelDistance(String namespace){
		
		return SurveyConstant.WTD.getName(namespace);
		
	}
	
	public static String wayTravelTime(String namespace){
		
		return SurveyConstant.WTT.getName(namespace);
		
	}
	
	public static String wayMode(String namespace){
	
		return SurveyConstant.WMODE.getName(namespace);
		
	}
	
	public static String wayPurpose(String namespace){
		
		return SurveyConstant.WPUR.getName(namespace);
		
	}
	
	public static String waySource(String namespace){
		
		return SurveyConstant.WSOURCE.getName(namespace);
		
	}
	
	public static String waySink(String namespace){
		
		return SurveyConstant.WSINK.getName(namespace);
		
	}
	
	public static String personGroup(String namespace){
		
		return SurveyConstant.PG12.getName(namespace);
		
	}

	public static String personLifephase(String namespace){
		
		return SurveyConstant.PLPHASE.getName(namespace);
		
	}
	
	public static String wayDetailedPurpose(String namespace){
		
		return SurveyConstant.WPURD.getName(namespace);
		
	}
	
	public static String vehicleId(String namespace){
		
		return SurveyConstant.VID.getName(namespace);
		
	}
	
	public static String vehicleFuelType(String namespace){
		
		return SurveyConstant.VFUEL.getName(namespace);
		
	}
	
	public static String vehicleSegmentKBA(String namespace){
		
		return SurveyConstant.VSEG.getName(namespace);
		
	}
	
	public static String getSexMale(){
		
		return PSEX_MALE;
		
	}
	
	public static String mobile(String namespace){
		
		return SurveyConstant.MOBILE.getName(namespace);
		
	}
	
}
