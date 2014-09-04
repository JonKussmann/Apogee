package com.apogeeapp.apogee;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "PushBroadcastReceiver";
	private Context context;
	private double lat, lon;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		Log.d(TAG, "onReceived called" + intent);

		if (intent.getAction().equals("com.apogee.REFRESH") && MainActivity.getMainActivityVisible()) {
			try {
				JSONObject json = new JSONObject(intent.getExtras().getString(
						"com.parse.Data"));
				lat = json.getDouble("lat");
				lon = json.getDouble("lon");
				ParseGeoPoint point = new ParseGeoPoint(lat, lon);
				if (point.distanceInKilometersTo(geoPointFromLatLng(MainActivity.getLatLng())) < Application.getRadius()) {
					MainActivity.doListQuery();
					MainActivity.doMapQuery();
					Log.d(TAG, "onRECEIVE WORKED");
					
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}

	}
	
	private static ParseGeoPoint geoPointFromLatLng(LatLng loc) {
		return new ParseGeoPoint(loc.latitude, loc.longitude);
	}

}
