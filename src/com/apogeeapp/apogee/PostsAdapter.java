package com.apogeeapp.apogee;

import android.content.Context;
import android.location.Location;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class PostsAdapter extends ParseQueryAdapter<Post> {
	private static final String TAG = "PostsAdapter";
	
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private static final int METERS_PER_KILOMETER = 1000;
	
	// Initial offset for calculating the map bounds
		private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

		// Accuracy for calculating the map bounds
		private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

	public PostsAdapter(Context context) {
		super(context, new ParseQueryAdapter.QueryFactory<Post>() {
			

			@Override
			public ParseQuery<Post> create() {
				LatLngBounds bounds = calculateBoundsWithCenter(MainActivity.getLatLng());
				ParseGeoPoint northEast = new ParseGeoPoint(bounds.northeast.latitude, bounds.northeast.longitude);
				ParseGeoPoint southWest = new ParseGeoPoint(bounds.southwest.latitude, bounds.southwest.longitude);
				
				
				ParseQuery<Post> query = new ParseQuery<Post>("Post");
				query.orderByDescending("createdAt");
				query.setLimit(100);
				query.whereWithinGeoBox("location", southWest, northEast);
				return query;
				
			}
		});
		
		imageLoader = ImageLoader.getInstance();
		  options = new DisplayImageOptions.Builder()
       .showImageOnLoading(R.drawable.ic_launcher)
       .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
       .cacheInMemory(true)
       .build();
		
	}

	@Override
	public View getItemView(Post post, View view, ViewGroup parent) {
		if(view == null) {
			view = View.inflate(getContext(), R.layout.posts_row_view, null);
		}
		
		super.getItemView(post, view, parent);
		
		TextView numberOfComments = (TextView)view.findViewById(R.id.commentCount);
		if (post.getComments() == 0) {
			numberOfComments.setText("");
		} else if (post.getComments() == 1){
			numberOfComments.setText("1 Comment");
		} else {
			numberOfComments.setText(post.getComments() + " Comments");
		}
		
		TextView castTextView = (TextView)view.findViewById(R.id.postText);
		Log.d(TAG, "top Post Text: " + post.getPostText());
		castTextView.setText(post.getPostText());
		
		TextView castDisplayName = (TextView)view.findViewById(R.id.postDisplayName);
		castDisplayName.setText(post.getDisplayName());
		
		TextView castEpochTime = (TextView)view.findViewById(R.id.postTime);
		Log.d(TAG, "post Epoch Time" + post.getEpochTime());
		Long time = post.getEpochTime();
		CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
		castEpochTime.setText(relativeTime);
		
		ImageView castImage = (ImageView)view.findViewById(R.id.postImage); 
		if (post.getImageURL().equals("Anonymous")) {
			castImage.setImageResource(R.drawable.ic_launcher);
		} else {
			imageLoader.displayImage(post.getImageURL(), castImage, options);
		}
		
		return view;
	}
	
	/*
	 * Helper method to calculate the bounds for map zooming
	 */
	public static LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
		// Create a bounds
		LatLngBounds.Builder builder = LatLngBounds.builder();

		// Calculate east/west points that should to be included
		// in the bounds
		double lngDifference = calculateLatLngOffset(myLatLng, false);
		LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude
				+ lngDifference);
		builder.include(east);
		LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude
				- lngDifference);
		builder.include(west);

		// Calculate north/south points that should to be included
		// in the bounds
		double latDifference = calculateLatLngOffset(myLatLng, true);
		LatLng north = new LatLng(myLatLng.latitude + latDifference,
				myLatLng.longitude);
		builder.include(north);
		LatLng south = new LatLng(myLatLng.latitude - latDifference,
				myLatLng.longitude);
		builder.include(south);

		return builder.build();
	}

	/*
	 * Helper method to calculate the offset for the bounds used in map zooming
	 */
	private static double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
		// The return offset, initialized to the default difference
		double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
		// Set up the desired offset distance in meters
		float desiredOffsetInMeters = Application.getRadius() * METERS_PER_KILOMETER;
		// Variables for the distance calculation
		float[] distance = new float[1];
		boolean foundMax = false;
		double foundMinDiff = 0;
		// Loop through and get the offset
		do {
			// Calculate the distance between the point of interest
			// and the current offset in the latitude or longitude direction
			if (bLatOffset) {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude + latLngOffset, myLatLng.longitude,
						distance);
			} else {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude, myLatLng.longitude + latLngOffset,
						distance);
			}
			// Compare the current difference with the desired one
			float distanceDiff = distance[0] - desiredOffsetInMeters;
			if (distanceDiff < 0) {
				// Need to catch up to the desired distance
				if (!foundMax) {
					foundMinDiff = latLngOffset;
					// Increase the calculated offset
					latLngOffset *= 2;
				} else {
					double tmp = latLngOffset;
					// Increase the calculated offset, at a slower pace
					latLngOffset += (latLngOffset - foundMinDiff) / 2;
					foundMinDiff = tmp;
				}
			} else {
				// Overshot the desired distance
				// Decrease the calculated offset
				latLngOffset -= (latLngOffset - foundMinDiff) / 2;
				foundMax = true;
			}
		} while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
		return latLngOffset;
	}
	
	

}
