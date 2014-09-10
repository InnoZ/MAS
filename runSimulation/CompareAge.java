package simulation;

import java.util.Comparator;

import readTrafficParticipants.TrafficParticipants;

public class CompareAge implements Comparator<TrafficParticipants>{

	@Override
	public int compare(TrafficParticipants tp1, TrafficParticipants tp2){
		 if(tp1.getAge() < tp2.getAge()) {
       return -1;
    } else if (tp1.getAge() == tp2.getAge()) {
       return 0;
    } else {
       return 1;
    }
  }			
}
