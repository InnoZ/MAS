package com.innoz.toolbox.run.parallelization;

import java.util.ArrayList;
import java.util.List;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.ActivityLocationsType;
import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.io.database.datasets.OsmDataset;
import com.innoz.toolbox.io.database.datasets.OsmPointDataset;
import com.innoz.toolbox.io.database.datasets.OsmPolygonDataset;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Building;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.utils.osm.OsmKey2ActivityType;

public final class DataProcessingAlgoThread extends AlgoThread {

	private List<OsmDataset> data = new ArrayList<>();
	private String type;
	private DatabaseReader reader;
	
	@Override
	public void init(Object... args) {
		
		this.reader = (DatabaseReader)args[0];
		this.type = (String)args[1];
		
	}
	
	@Override
	void addToThread(Object obj){
		
		if(obj instanceof OsmDataset){
			this.data.add(((OsmDataset)obj));
		}
		
	}
	
	public void run(){
		
		if(this.type.equals("landuse")){
			
			for(OsmDataset dataset : this.data){
				
				processLanduseDataset((OsmPolygonDataset)dataset);
				
			}
			
		} else if(this.type.equals("buildings")) {
			
			for(OsmDataset dataset : this.data){
				
				processBuildingDataset((OsmPolygonDataset)dataset);
				
			}
			
		} else {
			
			for(OsmDataset dataset : this.data){

				processAmenityDataset((OsmPointDataset)dataset);
				
			}
			
		}
		
	}
	
	void processLanduseDataset(OsmPolygonDataset dataset){
		
		String landuse = dataset.getLanduseKey();
		
		// Set the landuse type by checking the amenity, leisure and shop tags
		if(dataset.getAmenityKey() != null){
			
			landuse = dataset.getAmenityKey();
			
		} else if(dataset.getLeisureKey() != null){
			
			landuse = dataset.getLeisureKey();
			
		} else if(dataset.getShopKey() != null){
			
			landuse = dataset.getShopKey();
			
		}
		
		landuse = getLanduseType(landuse);
		
		if(landuse != null){
			
			// Add the landuse geometry to the geoinformation if we have a valid activity option for it
			
			if(!reader.getConfiguration().scenario().getActivityLocationsType().equals(ActivityLocationsType.LANDUSE)){
				
					for(Building b : this.reader.getBuildingList()){

						if(b.getActivityOptions().isEmpty()){

							if(dataset.getGeometry().contains(b.getGeometry())){
								
								b.addActivityOption(landuse);
								
								if(!landuse.startsWith(ActivityTypes.LEISURE) && !landuse.equals(ActivityTypes.HOME)){
								
									b.addActivityOption(ActivityTypes.WORK);
								
								}
								
							}
							
						}
						
					}

			} else {
				
				reader.addGeometry(landuse, new Building(dataset.getGeometry()));
				
			}
			
		}
		
	}
	
	void processBuildingDataset(OsmPolygonDataset dataset){
	
		synchronized (this.reader.getBuildingsQuadTree()) {
			
			if(dataset.getGeometry().isValid()){
		
				Building b = new Building(dataset.getGeometry());
				String buildingType = getTypeOfBuilding(dataset.getBuildingKey());
				
				if(buildingType != null){
					
					b.addActivityOption(buildingType);
					
				}
				
				if(dataset.getAmenityKey() != null){
					
					b.addActivityOption(getAmenityType(dataset.getAmenityKey()));
					
				}
				
				if(dataset.getLeisureKey()!= null){
					b.addActivityOption(getAmenityType(dataset.getLeisureKey()));
				}
				
				if(dataset.getShopKey()!= null){
					
					b.addActivityOption(getAmenityType(dataset.getShopKey()));
					
				}
				
				this.reader.getBuildingList().add(b);
				this.reader.getBuildingsQuadTree().put(dataset.getGeometry().getCentroid().getX(),
						dataset.getGeometry().getCentroid().getY(), b);
				
			}
		
		}
		
	}
	
	private static String getAmenityType(String tag){
		
		if(OsmKey2ActivityType.education.contains(tag)){
			
			if("university".equals(tag) || "college".equals(tag))
				return ActivityTypes.UNIVERSITY;
			
			return ActivityTypes.EDUCATION;
			
		} else if(OsmKey2ActivityType.groceryShops.contains(tag) || OsmKey2ActivityType.miscShops.contains(tag) ||
				OsmKey2ActivityType.serviceShops.contains(tag)){
			
			if(OsmKey2ActivityType.groceryShops.contains(tag)){
				
				return ActivityTypes.SUPPLY;
				
			} else if(OsmKey2ActivityType.serviceShops.contains(tag)){
				
				return ActivityTypes.SERVICE;
				
			} else {
				
				return ActivityTypes.SHOPPING;
				
			}
			
		} else if(OsmKey2ActivityType.leisure.contains(tag) || OsmKey2ActivityType.eating.contains(tag)
				|| OsmKey2ActivityType.culture.contains(tag) || OsmKey2ActivityType.sports.contains(tag)
				|| OsmKey2ActivityType.furtherEducation.contains(tag) || OsmKey2ActivityType.events.contains(tag)
				|| OsmKey2ActivityType.allotment.contains(tag)){
			
			if(OsmKey2ActivityType.eating.contains(tag)){
				
				return ActivityTypes.EATING;
				
			} else if(OsmKey2ActivityType.culture.contains(tag)){
				
				return ActivityTypes.CULTURE;
				
			} else if(OsmKey2ActivityType.sports.contains(tag)){
				
				return ActivityTypes.SPORTS;
				
			} else if(OsmKey2ActivityType.furtherEducation.contains(tag)){
				
				return ActivityTypes.FURTHER;
				
			} else if(OsmKey2ActivityType.events.contains(tag)){
				
				return ActivityTypes.EVENT;
				
			} else if(OsmKey2ActivityType.allotment.equals(tag)){
				
				return ActivityTypes.ALLOTMENT;
				
			} else {
				
				return ActivityTypes.LEISURE;
				
			}
			
		} else if(OsmKey2ActivityType.otherPlaces.contains(tag) || OsmKey2ActivityType.healthcare.contains(tag)
				|| OsmKey2ActivityType.errand.contains(tag)) {
		
			if(OsmKey2ActivityType.healthcare.contains(tag)){
				
				return ActivityTypes.HEALTH;
				
			} else if(OsmKey2ActivityType.errand.contains(tag)){
				
				return ActivityTypes.ERRAND;
						
			} else {
				
				return ActivityTypes.OTHER;
				
			}
			
		} else if(ActivityTypes.KINDERGARTEN.equals(tag)){
			
			return ActivityTypes.KINDERGARTEN;
			
		} else{
			
			return null;
			
		}
		
	}
	
	private String getLanduseType(String landuseTag){
		
		if(landuseTag.equals("college") || landuseTag.equals("school") || landuseTag.equals(
				"university")){
			
			return ActivityTypes.EDUCATION;
			
		} else if(landuseTag.equals("commercial") || landuseTag.equals("industrial")){
			
			return ActivityTypes.WORK;
			
		} else if(landuseTag.equals("hospital")){
			
			return ActivityTypes.OTHER;
			
		} else if(landuseTag.equals("recreation_ground") || landuseTag.equals("park")
				|| landuseTag.equals("village_green")){
			
			return ActivityTypes.LEISURE;
			
		} else if(landuseTag.equals("residential")){
			
			return ActivityTypes.HOME;
			
		} else if(landuseTag.equals("retail")){
			
			return ActivityTypes.SHOPPING;
			
		} else if(landuseTag.equals("allotments")){
			
			return ActivityTypes.ALLOTMENT;
			
		}
		
		return null;
		
	}
	
	private String getTypeOfBuilding(String buildingTag){
		
		if(buildingTag.equals("apartments") || buildingTag.equals("residential")){
			
			return ActivityTypes.HOME;
			
		} else if(buildingTag.equals("barn") || buildingTag.equals("brewery") || buildingTag.equals("factory")
				|| buildingTag.equals("office")	|| buildingTag.equals("warehouse")){
			
			return ActivityTypes.WORK;
			
		} else if(buildingTag.equals("castle") || buildingTag.equals("monument") || buildingTag.equals("palace")){
			
			//TODO tourism
			return "tourism";
			
		} else if(buildingTag.equals("church") || buildingTag.equals("city_hall") || buildingTag.equals("hall")){
			
			return ActivityTypes.OTHER;
			
		} else if(buildingTag.equals("stadium")){
			
			return ActivityTypes.LEISURE;
			
		} else if(buildingTag.equals("store")){
			
			return ActivityTypes.SHOPPING;
			
		}
		
		return null;
		
	}
	
	private void processAmenityDataset(OsmPointDataset dataset){
		
		String landuse = null;
		
		// Set the landuse type by checking the amenity, leisure and shop tags
		if(dataset.getAmenityKey() != null){
			
			landuse = dataset.getAmenityKey();
			
		} else if(dataset.getLeisureKey() != null){
			
			landuse = dataset.getLeisureKey();
			
		} else if(dataset.getShopKey() != null){
			
			landuse = dataset.getShopKey();
			
		}

		// Convert the OSM landuse tag into a MATSim activity type
		String actType = getAmenityType(landuse);
		
		if(actType != null){

			// Add the landuse geometry to the geoinformation if we have a valid activity option for it
//			this.reader.addGeometry(actType, new Building(dataset.getGeometry()));
			
			Building closest = this.reader.getBuildingsQuadTree().getClosest(dataset.getGeometry().getCentroid().getX(),
					dataset.getGeometry().getCentroid().getY());
			
			if(closest != null){
			
				closest.addActivityOption(actType);
				
			}
			
		}
		
	}

}
