package com.innoz.toolbox.io.database.datasets;

import com.vividsolutions.jts.geom.Geometry;

public class OsmPointDataset implements OsmDataset {

	private final Geometry geometry;
	private final String amenityKey;
	private final String shopKey;
	private final String leisureKey;

	public OsmPointDataset(Geometry geometry, String amenity, String shop, String leisure){
		
		this.amenityKey = amenity;
		this.geometry = geometry;
		this.leisureKey = leisure;
		this.shopKey = shop;
		
	}
	
	public Geometry getGeometry() {
		return geometry;
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
	
}
