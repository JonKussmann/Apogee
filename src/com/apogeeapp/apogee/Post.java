package com.apogeeapp.apogee;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

	public Post() {

	}

	public String getPostText() {
		return getString("postText");
	}

	public void setPostText(String postText) {
		put("postText", postText);
	}

	public String getAuthor() {
		return getString("author");
	}

	public void setAuthor(ParseUser user) {
		put("author", user.getObjectId());
	}

	public Long getEpochTime() {
		return getLong("epochTime");
	}

	public void setEpochTime() {
		put("epochTime", System.currentTimeMillis());
	}
	
	public String getDisplayName() {
		return getString("displayName");
	}
	
	public void setDisplayName(String displayName) {
		put("displayName", displayName);
	}

	public int getComments() {
		return getInt("comments");
	}

	public void setComments() {
		put("comments", 0);
	}
	
	public String getImageURL() {
		return getString("imageURL");
	}
	
	public void setImageURL(String imageURL) {
		put("imageURL", imageURL);
	}
	
	public ParseGeoPoint getLocation() {
		return getParseGeoPoint("location");
	}
	
	public void setLocation(ParseGeoPoint location){
		put("location", location);
	}

}
