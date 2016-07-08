package innoz.io.database.datasets;

import com.vividsolutions.jts.geom.Geometry;

public class OsmPolygonDataset implements OsmDataset {

	private final Geometry geometry;
	private final String landuseKey;
	private final String amenityKey;
	private final String shopKey;
	private final String leisureKey;
	private final String buildingKey;
	
	public OsmPolygonDataset(Geometry geometry, String landuse, String amenity, String shop, String leisure,
			String building){
		
		this.amenityKey = amenity;
		this.buildingKey = building;
		this.geometry = geometry;
		this.landuseKey = landuse;
		this.leisureKey = leisure;
		this.shopKey = shop;
		
	}
	
	public Geometry getGeometry() {
		return geometry;
	}

	public String getLanduseKey() {
		return landuseKey;
	}

	public String getAmenityKey() {
		return amenityKey;
	}

	public String getShopKey() {
		return shopKey;
	}

	public String getLeisureKey() {
		return leisureKey;
	}

	public String getBuildingKey() {
		return buildingKey;
	}
	
}
