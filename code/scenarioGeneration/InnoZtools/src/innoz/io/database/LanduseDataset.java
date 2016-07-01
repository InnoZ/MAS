package innoz.io.database;

import com.vividsolutions.jts.geom.Geometry;

public class LanduseDataset {

	private Geometry geometry;
	private String landuse;
	private String amenity;
	private String leisure;
	private String shop;
	
	LanduseDataset(Geometry geometry, String landuse, String amenity, String leisure, String shop){
		
		this.geometry = geometry;
		this.landuse = landuse;
		this.amenity = amenity;
		this.leisure = leisure;
		this.shop = shop;
		
	}

	Geometry getGeometry() {
		return geometry;
	}

	void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	String getLanduse() {
		return landuse;
	}

	void setLanduse(String landuse) {
		this.landuse = landuse;
	}

	String getAmenity() {
		return amenity;
	}

	void setAmenity(String amenity) {
		this.amenity = amenity;
	}

	String getLeisure() {
		return leisure;
	}

	void setLeisure(String leisure) {
		this.leisure = leisure;
	}

	String getShop() {
		return shop;
	}

	void setShop(String shop) {
		this.shop = shop;
	}
	
}
