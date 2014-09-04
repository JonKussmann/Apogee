package com.apogeeapp.apogee;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {
	
	public Comment() {
		
	}
	
	public String getCommentText() {
		return getString("commentText");
	}
	
	public void setCommentText(String text) {
		put("commentText", text);
	}
	
	public String getAuthor() {
		return getString("author");
	}
	
	public void setAuthor(ParseUser user) {
		put("user", user.getObjectId());
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
	
	public String getImageURL() {
		return getString("imageURL");
	}
	
	public void setImageURL(String imageURL) {
		put("imageURL", imageURL);
	}
	
	public String getPost() {
		return getString ("post");
	}
	public void setPost(String post) {
		put("post", post);
	}
	


}
