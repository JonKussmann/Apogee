package com.apogeeapp.apogee;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<Place> implements Filterable {
    private ArrayList<Place> resultList;
    
    private static final String TAG = "PlacesAutoCompleteAdapter";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyDOmmuKFtCCQWz6WYc-KJYlv2EVdLmP7oY";
    
    
    HttpURLConnection conn = null;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
    	if (resultList!=null) {
        return resultList.size();
    	}
    	return 0;
    }

    @Override
    public Place getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
			@Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Place> resultListTemp = new ArrayList<Place>();
                Log.d(TAG, "RESULTLISTTEMP SHOULD BE EMPTY" + resultListTemp);

                
                if (constraint != null) {
                	if (conn !=null) {
                		conn.disconnect();
                	}
                	
                	if(constraint.length() > 0) {
                    // Retrieve the autocomplete results.
                    resultListTemp.addAll(autocomplete(constraint.toString()));        
                    // Assign the data to the FilterResults
                    filterResults.values = resultListTemp;
                    filterResults.count = resultListTemp.size();
                	}
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            	Log.d(TAG, "results: " + results + " Count: " + results.count);
            	resultList = (ArrayList<Place>) results.values;
            	
                if (results != null && results.count > 0) {
                	Log.d(TAG, "publishResults called");
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
    
    private ArrayList<Place> autocomplete(String input) {
    	Log.d(TAG, "autocomplete called");
        ArrayList<Place> resultList = new ArrayList<Place>();

        HttpURLConnection conn = null;
        InputStream stream = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&type=cities");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            Log.d(TAG, "URL URL URL: " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Connection", "close");
            conn.setReadTimeout(1000);
            conn.setUseCaches(false);
            Log.d(TAG, "before conn.getInputStream");
             stream = conn.getInputStream();
            Log.d(TAG, "after conn.getInputStream");
            
            
            InputStreamReader in = new InputStreamReader(stream);
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
             Log.d(TAG, "jsonResults" + jsonResults);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.d(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
                try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");            

            // Extract the Place descriptions from the results
            resultList = new ArrayList<Place>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
            	Place place = new Place();
            	place.setDescription(predsJsonArray.getJSONObject(i).getString("description"));
            	Log.d(TAG, "Description" + place.getDescription());
            	place.setPlace_id(predsJsonArray.getJSONObject(i).getString("place_id"));
            	Log.d(TAG, "Place_ID" + place.getPlace_id());
                resultList.add(place);
                
            }
        } catch (JSONException e) {
            Log.d(TAG, "Cannot process JSON results", e);
        }
        Log.d(TAG, "RESULSTLIST RESULT LIST" + resultList);
        return resultList;
    }

//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		View view = convertView;
//		if(view == null) {
//			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			view = inflater.inflate(R.layout.auto_list_item, null);
//			
//			Place place = getItem(position);
//			if (place !=null) {
//				TextView textView = (TextView)view.findViewById(R.id.autoTextView);
//				textView.setText(place.getDescription());
//			}
//		}
//		return view;
//	}
//	
	
    
}
