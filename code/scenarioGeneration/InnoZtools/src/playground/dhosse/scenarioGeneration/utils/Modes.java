package playground.dhosse.scenarioGeneration.utils;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;

/**
 * 
 * Collection of transport modes that might be used in scenarios.
 * Just for (internal) standardization reasons.
 * 
 * @author dhosse
 *
 */
public class Modes {
	
	//transit
	public static final String BUS = "bus";
	public static final String FERRY = "ferry";
	public static final String SUBWAY = "subway";
	public static final String TAXI = "taxi";
	public static final String TRAIN = "train";
	public static final String TRAM = "tram";

	//shared mobility
	public static final String OW = "onewaycarsharing";
	public static final String TW = "twowaycarsharing";
	public static final String FF = "freefloating";
	
	//misc
	public static final String MOTORCYCLE = "motorcycle";
	public static final String PEDELEC = "pedelec";
	public static final String SCOOTER = "scooter";
	
	public static final double v_walk = 4/3.6;
	public static final double v_car = 25/3.6;
	public static final double v_bike = 11/3.6;
	public static final double v_pt = 20/3.6;
	
	public static double getDistanceTravelledForModeAndTravelTime(String mode, double time){
		
		if(mode.equals(TransportMode.car)){
			
			return time * (v_car/3.6);
			
		} else if(mode.equals(TransportMode.pt)){
			
			return time * (v_pt / 3.6);
			
		} else if(mode.equals(TransportMode.bike)){
			
			return time * (v_bike / 3.6);
			
		} else if(mode.equals(TransportMode.walk)){
			
			return time * (v_walk / 3.6);
			
		} else {
			
			return time * (v_car / 3.6);
			
		}
		
	}
	
	public static double getSpeedForMode(String mode){
		
		if(TransportMode.walk.equals(mode)){
			return v_walk;
		} else if(TransportMode.bike.equals(mode)){
			return v_bike;
		} else if(TransportMode.pt.equals(mode)){
			return v_pt;
		} else {
			return v_car;
		}
		
	}
	
}
