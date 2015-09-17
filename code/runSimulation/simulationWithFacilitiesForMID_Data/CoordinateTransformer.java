package simulationWithFacilitiesForMID_Data;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class CoordinateTransformer {
	
	private CoordinateTransformation ct = TransformationFactory
			.getCoordinateTransformation("EPSG:32632", "EPSG:4326");
	
	public CoordinateTransformation getCT(){
		return this.ct;
	}
	
	public Coord getDecimalDegreeCoordFromDoubleXY(double x, double y){
		Coord wgs84 = new CoordImpl(x, y);
		Coord decimalDegree = this.ct.transform(wgs84);
		return decimalDegree;
	}
	public static void main(String[] args) {
		CoordinateTransformer coordtrans = new CoordinateTransformer();
		
		double x1 = 657376.3827177022;
		double y1 = 5285283.693343911;
		Coord dezimalgrad_1 = coordtrans.getDecimalDegreeCoordFromDoubleXY(x1, y1);
		
		double x2 = 658929.2490394979;
		double y2 = 5265055.018733777;
		Coord dezimalgrad_2 = coordtrans.getDecimalDegreeCoordFromDoubleXY(x2, y2);
		
		System.out.println("WGS84UTM32N: " + "(" + x1 + " , " + y1 + ")");
		System.out.println("Dezimalgrad: " + "(" + dezimalgrad_1.getY() + " , " + dezimalgrad_1.getX() + ")");
		
		System.out.println("WGS84UTM32N: " + "(" + x2 + " , " + y2 + ")");
		System.out.println("Dezimalgrad: " + "(" + dezimalgrad_2.getY() + " , " + dezimalgrad_2.getX() + ")");
	}

}
