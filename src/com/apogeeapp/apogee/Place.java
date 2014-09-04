package com.apogeeapp.apogee;

public class Place {
	private String description;
	private String place_id;
	
	public Place() {
		
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPlace_id() {
		return place_id;
	}
	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getDescription();
	}

}
