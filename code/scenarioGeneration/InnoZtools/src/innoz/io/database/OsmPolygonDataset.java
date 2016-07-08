package innoz.io.database;

import com.vividsolutions.jts.geom.Geometry;

public class OsmPolygonDataset {

	private final Geometry geometry;
	private final String landuseKey;
	private final String amenityKey;
	private final String shopKey;
	private final String leisureKey;
	private final String buildingKey;
	
	protected OsmPolygonDataset(Geometry geometry, String landuse, String amenity, String shop, String leisure,
			String building){
		
		this.amenityKey = amenity;
		this.buildingKey = building;
		this.geometry = geometry;
		this.landuseKey = landuse;
		this.leisureKey = leisure;
		this.shopKey = shop;
		
	}
	
	Geometry getGeometry() {
		return geometry;
	}

	String getLanduseKey() {
		return landuseKey;
	}

	String getAmenityKey() {
		return amenityKey;
	}

	String getShopKey() {
		return shopKey;
	}

	String getLeisureKey() {
		return leisureKey;
	}

	String getBuildingKey() {
		return buildingKey;
	}
	
}
