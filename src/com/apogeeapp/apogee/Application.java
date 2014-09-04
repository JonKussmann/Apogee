package com.apogeeapp.apogee;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Application extends android.app.Application {
	private static final String TAG = "Application";
	private static String DISPLAY_NAME = "display_name";
	private static SharedPreferences sharedPreferences;
	private static String SEARCH_RADIUS = "search_radius";

	@Override
	public void onCreate() {
		super.onCreate();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		ParseObject.registerSubclass(Post.class);
		ParseObject.registerSubclass(Comment.class);
		Parse.initialize(this, "PARSE KEY", "PARSE_KEY");
		
		
		
		
		ParseUser.enableAutomaticUser();
		ParseUser.getCurrentUser().put(DISPLAY_NAME, "Anonymous");
		ParseUser.getCurrentUser().saveInBackground();
		
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .threadPriority(Thread.NORM_PRIORITY)
        .threadPoolSize(7)
        .build();
		 ImageLoader.getInstance().init(configuration);
	}

	public static int getRadius() {
		// TODO Auto-generated method stub
		return Integer.parseInt(sharedPreferences.getString(SEARCH_RADIUS, "100"));
	}

	public static String getDisplayName() {
		// TODO Auto-generated method stub
		return sharedPreferences.getString(DISPLAY_NAME, "Anonymous");
	}

}
