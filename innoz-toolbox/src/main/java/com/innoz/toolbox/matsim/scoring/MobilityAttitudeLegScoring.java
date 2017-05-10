package com.innoz.toolbox.matsim.scoring;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.scoring.functions.CharyparNagelScoringParameters;
import org.matsim.core.scoring.functions.ModeUtilityParameters;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.PtConstants;

import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParameterSet;

public class MobilityAttitudeLegScoring implements org.matsim.core.scoring.SumScoringFunction.LegScoring,
	org.matsim.core.scoring.SumScoringFunction.ArbitraryEventScoring {
	
	protected final CharyparNagelScoringParameters params;
	protected Network network;
	
	protected double score;
	private boolean nextEnterVehicleIsFirstOfTrip = true ;
	private boolean nextStartPtLegIsFirstOfTrip = true ;
	private boolean currentLegIsPtLeg = false;
	private double lastActivityEndTime = Time.UNDEFINED_TIME ;
	private double scaleFactor;
	
	private MobilityAttitudeModeParameterSet attitudeParams;
	
	private static int ccc=0 ;
	
	public MobilityAttitudeLegScoring(CharyparNagelScoringParameters params, MobilityAttitudeModeParameterSet attitudeParams,
	        Network network, double scaleFactor) {
		
		this.params = params;
		this.network = network;
		this.nextEnterVehicleIsFirstOfTrip = true ;
		this.nextStartPtLegIsFirstOfTrip = true ;
		this.currentLegIsPtLeg = false;
		this.attitudeParams = attitudeParams;
		this.scaleFactor = scaleFactor;
		
	}

	@Override
	public void finish() {
		
	}

	@Override
	public double getScore() {
		
		return this.score;
		
	}

	@Override
	public void handleEvent(Event event) {
		if ( event instanceof ActivityEndEvent ) {
			// When there is a "real" activity, flags are reset:
			if ( !PtConstants.TRANSIT_ACTIVITY_TYPE.equals( ((ActivityEndEvent)event).getActType()) ) {
				this.nextEnterVehicleIsFirstOfTrip  = true ;
				this.nextStartPtLegIsFirstOfTrip = true ;
			}
			this.lastActivityEndTime = event.getTime() ;
		}

		if ( event instanceof PersonEntersVehicleEvent && currentLegIsPtLeg ) {
			if ( !this.nextEnterVehicleIsFirstOfTrip ) {
				// all vehicle entering after the first triggers the disutility of line switch:
				this.score  += params.utilityOfLineSwitch ;
			}
			this.nextEnterVehicleIsFirstOfTrip = false ;
			// add score of waiting, _minus_ score of travelling (since it is added in the legscoring above):
			this.score += (event.getTime() - this.lastActivityEndTime) * (this.params.marginalUtilityOfWaitingPt_s - this.params.modeParams.get(TransportMode.pt).marginalUtilityOfTraveling_s) ;
		}

		if ( event instanceof PersonDepartureEvent ) {
			this.currentLegIsPtLeg = TransportMode.pt.equals( ((PersonDepartureEvent)event).getLegMode() );
			if ( currentLegIsPtLeg ) {
				if ( !this.nextStartPtLegIsFirstOfTrip ) {
					this.score -= params.modeParams.get(TransportMode.pt).constant ;
					// (yyyy deducting this again, since is it wrongly added above.  should be consolidated; this is so the code
					// modification is minimally invasive.  kai, dec'12)
				}
				this.nextStartPtLegIsFirstOfTrip = false ;
			}
		}
	}

	@Override
	public void handleLeg(Leg leg) {
		/* TODO At the moment, this function scores legs the 'default' way. What we have to do is to define our own scoring function in
		 * calcLegScore */
		double legScore = calcLegScore(leg.getDepartureTime(), leg.getDepartureTime() + leg.getTravelTime(), leg);
		this.score += legScore;
		
	}
	
	protected double calcLegScore(final double departureTime, final double arrivalTime, final Leg leg) {
		
		double tmpScore = 0.0;
		double travelTime = arrivalTime - departureTime; // travel time in seconds	
		ModeUtilityParameters modeParams = this.params.modeParams.get(leg.getMode());
		
		if (modeParams == null) {

			if (leg.getMode().equals(TransportMode.transit_walk)) {
			
				modeParams = this.params.modeParams.get(TransportMode.walk);
			
			} else {
			
				modeParams = this.params.modeParams.get(TransportMode.other);
		
			}
		
		}
		
		// replaced std. beta_tt with mobility attitude param
		tmpScore += travelTime * this.attitudeParams.getOffsetForMode(leg.getMode());//modeParams.marginalUtilityOfTraveling_s;
		
		if (modeParams.marginalUtilityOfDistance_m != 0.0
				|| modeParams.monetaryDistanceCostRate != 0.0) {
			
			Route route = leg.getRoute();
			double dist = route.getDistance(); // distance in meters
			
			if ( Double.isNaN(dist) ) {
				
				if ( ccc<10 ) {
					
					ccc++ ;
					Logger.getLogger(this.getClass()).warn("distance is NaN. Will make score of this plan NaN. Possible reason: Simulation does not report " +
							"a distance for this trip. Possible reason for that: mode is teleported and router does not " +
							"write distance into plan.  Needs to be fixed or these plans will die out.") ;
					
					if ( ccc==10 ) {
					
						Logger.getLogger(this.getClass()).warn(Gbl.FUTURE_SUPPRESSED) ;
				
					}
			
				}
			
			}
			
			tmpScore += modeParams.marginalUtilityOfDistance_m * dist;
			tmpScore += modeParams.monetaryDistanceCostRate * this.params.marginalUtilityOfMoney * dist;
			
		}
		
		tmpScore += modeParams.constant;

		// This is the only modification of the original code
//		double offset = this.attitudeParams.getOffsetForMode(leg.getMode()) * this.scaleFactor;
//		
//		tmpScore += offset;
		// end of modification
		
		return tmpScore;
		
	}

}