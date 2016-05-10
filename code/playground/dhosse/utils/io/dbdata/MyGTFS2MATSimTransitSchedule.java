package playground.dhosse.utils.io.dbdata;


public class MyGTFS2MATSimTransitSchedule{}/* extends GTFS2MATSimTransitSchedule {

	public MyGTFS2MATSimTransitSchedule(File[] roots, String[] modes,
			Network network, String[] serviceIds, String outCoordinateSystem) {
		super(roots, modes, network, serviceIds, outCoordinateSystem);
	}
	
	@Override
	public void processCalendarDate(String[] parts, int[] indices, int publicSystemNumber) {
		
		Service actual = super.services[publicSystemNumber].get(parts[indices[0]]);
		if(actual != null){
			if(parts[indices[2]].equals("2"))
				actual.addException(parts[indices[1]]);
			else
				actual.addAddition(parts[indices[1]]);
		} else {
			
			boolean[] days = new boolean[7];
			
			int weekDay = getWeekDay(parts[1]);
			
			for(int i = 0; i < days.length; i++){

				if(i == weekDay){
					days[i] = true;
				} else{
					days[i] = false;
				}
				
			}
			
			services[publicSystemNumber].put(parts[indices[0]], new Service(days, parts[indices[1]], parts[indices[1]]));
			
		}
		
	}
	
	private int getWeekDay(String date){
		
		int y = Integer.parseInt(date.substring(0, 3));
		int year = Integer.parseInt(date.substring(2, 3));
		int month = Integer.parseInt(date.substring(4, 5));
		int day = Integer.parseInt(date.substring(6, 7));
		
		if(month <= 2){
			year--;
		}
		
		int century = y / 100;
		
		return (int) ((day + (2.6 * month - 0.2) + year + year/4 + century/4 - 2*century)%7 - 1);
		
	}

}
*/