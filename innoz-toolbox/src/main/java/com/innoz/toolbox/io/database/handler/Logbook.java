package com.innoz.toolbox.io.database.handler;

import java.util.ArrayList;
import java.util.List;

public class Logbook {

	private List<SurveyStage> stages = new ArrayList<>();
	private boolean delete = false;
	
	public List<SurveyStage> getStages(){
		return this.stages;
	}
	
	public boolean isDelete(){
		return this.delete;
	}
	
	public void setDelete(boolean delete){
		this.delete = delete;
	}
	
}
