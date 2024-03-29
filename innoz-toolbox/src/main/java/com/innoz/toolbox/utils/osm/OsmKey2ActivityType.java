package com.innoz.toolbox.utils.osm;

import java.util.Set;

import org.matsim.core.utils.collections.CollectionUtils;


public final class OsmKey2ActivityType {

	private OsmKey2ActivityType(){};
	
	public static final Set<String> landuseKeys = CollectionUtils.stringToSet(
			"residential, commercial, industrial, leisure, retail");
	
	//SHOP
	//grocery
	public static final Set<String> groceryShops = CollectionUtils.stringToSet(
			"alcohol,bakery,beverages,butcher,cheese,chocolate,coffee,confectionery," +
			"convenience,deli,dairy,farm,greengrocer,pasta,pastry,seafood,tea,wine," +
			"supermarket");

	//service
	public static final Set<String> serviceShops = CollectionUtils.stringToSet(
			"hearing_aids,optician,hairdresser");
	
	//misc
	public static final Set<String> miscShops = CollectionUtils.stringToSet(
		"department_store,general,kiosk,mall,baby_goods,bag,boutique,clothes,"+
		"fabric,fashion,jewelry,leather,shoes,tailor,watches,charity,second_hand,"+
		"variety_store,beauty,chemist,cosmetics,erotic,herbalist,"+
		"medical_supply,nutrition_supplements,perfumery,bathroom_furnishing,doityourself,"+
		"electrical,energy,florist,garden_centre,garden_furniture,gas,glaziery,hardware,"+
		"locksmith,paint,trade,antiques,bed,candles,carpet,curtain,furniture,"+
		"interior_design,kitchen,lamps,window_blind,computer,electronics,hifi,mobile_phone,"+
		"radiotechnics,vacuum_cleaner,bicycle,car,car_repair,car_parts,fishing,free_flying,"+
		"hunting,motorcycle,outdoor,scuba_diving,sports,tyres,swimming_pool,art,craft,"+
		"frame,games,model,music,musical_instrument,photo,trophy,video,video_games,"+
		"anime,books,gift,lottery,newsagent,stationery,ticket,copyshop,dry_cleaning,"+
		"e-cigarette,funeral_directors,laundry,money_lender,pawnbroker,pet,pyrotechnics,"+
		"religion,tobacco,toys,travel_agency,weapons");
	
	//EDUCATION
	public static final Set<String> education = CollectionUtils.stringToSet(
		"college,school,university");
	
	public static final Set<String> higherEducation = CollectionUtils.stringToSet(
			"college,university");
	
	//LEISURE
	public static final Set<String> leisure = CollectionUtils.stringToSet(
			"brothel,casino,community_centre,gambling,nightclub,planetarium,social_centre,"
			+ "adult_gaming_centre,amusement_arcade,beach_resort,bandstand,bird_hide,dance,dog_park,firepit,"
			+ "fishing,garden,hackerspace,marina,miniature_golf,nature_reserve,park,"
			+ "playground,summer_camp,swimming_area,track,"
			+ "water_park,wildlife_hide,ice_cream,pub,bar,bbq,biergarten");
	
	//DISTINCT ACTIVITIES
	public static final String allotment = "allotment";
	
	//eat and drink
	public static final Set<String> eating = CollectionUtils.stringToSet(
			"cafe,fast_food,food_court,restaurant");
	
	//culture
	public static final Set<String> culture = CollectionUtils.stringToSet(
			"arts_centre,cinema,theatre");
	
	//sports
	public static final Set<String> sports = CollectionUtils.stringToSet(
			"golf_course,ice_rink,sports_centre,swimming_pool,dojo");
	
	public static final Set<String> furtherEducation = CollectionUtils.stringToSet(
			"music_school,driving_school,language_school,library");
	
	//events
	public static final Set<String> events = CollectionUtils.stringToSet(
			"pitch,stadium,market");
	
	//OTHER
	public static final Set<String> otherPlaces = CollectionUtils.stringToSet(
		"massage,tattoo,public_bookcase,pharmacy,veterinary");
	
	//health
	public static final Set<String> healthcare = CollectionUtils.stringToSet(
			"clinic,dentist,doctors,hospital");
	
	public static final Set<String> errand = CollectionUtils.stringToSet(
			"atm,bank,bureau_de_change,credit_institution,courthouse,police,post_box,post_office,townhall");
	
}
