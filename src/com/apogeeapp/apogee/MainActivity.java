package com.apogeeapp.apogee;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String TAG = "MainActivity";

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_DETAILS = "/details";
	private static final String OUT_JSON = "/json";

	private static final String API_KEY = "GOOGLE_MAP_API_KEY";

	private String imageUrl;
	private static GoogleMap map;

	private final static Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
	private static Map<Marker, Post> haspMap = new HashMap<Marker, Post>();

	// Initial offset for calculating the map bounds
	private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

	// Accuracy for calculating the map bounds
	private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

	private static LatLng myLatLng;
	private static LatLng travellingLatLng = null;
	private LocationRequest locationRequest;
	private LocationClient locationClient;

	private static Location lastLocation = null;
	private static Location currentLocation = null;
	private Circle mapCircle;
	private static float radius;

	private boolean hasSetUpInitialLocation = false;

	private static final int METERS_PER_KILOMETER = 1000;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private static final int MILLISECONDS_PER_SECOND = 1000;
	private static final int FASTEST_INTERVAL_IN_SECONDS = 30;
	private static final int REGULAR_INTERVAL_IN_SECONDS = 120;
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;
	private static final long REGULAR_INTERVAL = MILLISECONDS_PER_SECOND
			* REGULAR_INTERVAL_IN_SECONDS;

	private static PostsAdapter posts;
	private EditText postEditText;
	private static boolean mainActivityVisibility;
	private static ImageLoader imageLoader;
	private static DisplayImageOptions options;
	private static View view;
	private static IconGenerator iconGenerator;
	private boolean inSettings = false;
	private static ListView postsListView;
	private AutoCompleteTextView autoCompleteTextView;
	private static boolean isTravelling = false;

	private PlacesAutoCompleteAdapter adapter;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (servicesConnected()) {
			map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			map.setMyLocationEnabled(true);

			map.setOnMapLoadedCallback(new OnMapLoadedCallback() {

				@Override
				public void onMapLoaded() {
					Log.d(TAG, "MAP HAS BEEN LOADED");
					if (lastLocation != null) {
						setNewLatLng(lastLocation.getLatitude(),
								lastLocation.getLongitude());
						// if (lastRadius != radius) {
						updateZoom(getLatLng());
						// }

						updateCircle(getLatLng());
					}

				}
			});
			map.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker marker) {
					Post post = haspMap.get(marker);
					Log.d(TAG, "post: " + post + " marker: " + marker);
					Intent bundle = new Intent(MainActivity.this,
							SinglePostView.class);

					bundle.putExtra("postObjectId", post.getObjectId());
					bundle.putExtra("postText", post.getPostText());
					bundle.putExtra("postAuthor", post.getAuthor());
					bundle.putExtra("postDisplayName", post.getDisplayName());
					bundle.putExtra("postImage", post.getImageURL());
					bundle.putExtra("postLat", post.getLocation().getLatitude());
					bundle.putExtra("postLon", post.getLocation()
							.getLongitude());

					startActivityForResult(bundle, 1);

					return true;

				}
			});
			map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {

				@Override
				public boolean onMyLocationButtonClick() {
					if (isTravelling) {
						isTravelling = false;

						if (getLatLng() != null) {
							updateZoom(getLatLng());
							updateCircle(getLatLng());

							doListQuery();
							doMapQuery();
							Log.d(TAG, "doMapQuery1");

							// Update the installation subscribed channels
							ParseInstallation installation = ParseInstallation
									.getCurrentInstallation();
							installation.put("location",
									geoPointFromLatLng(getLatLng()));
							installation.saveInBackground();
						} else {
							Toast.makeText(MainActivity.this,
									R.string.location_not_found,
									Toast.LENGTH_SHORT).show();
						}
					}
					return true;

				}
			});
		}

		adapter = new PlacesAutoCompleteAdapter(MainActivity.this,
				R.layout.auto_list_item);

		autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
		autoCompleteTextView.setAdapter(adapter);
		autoCompleteTextView.setThreshold(2);
		autoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				Log.d(TAG, "on Item Click called");
				Place place = (Place) adapterView.getItemAtPosition(position);
				Log.d(TAG, place.getDescription() + " : " + place.getPlace_id());
				PlaceDetailsQuery placeDetails = new PlaceDetailsQuery();
				placeDetails.execute(place.getPlace_id());

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						autoCompleteTextView.getWindowToken(), 0);

			}
		});

		autoCompleteTextView
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							autoCompleteTextView.getText().clear();
						}

					}
				});

		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_launcher)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.cacheInMemory(true).build();

		postEditText = (EditText) findViewById(R.id.postEditText);

		ImageButton postSendButton = (ImageButton) findViewById(R.id.postSendButton);
		postSendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				newPost();

			}
		});

		posts = new PostsAdapter(this);
		posts.setAutoload(false);
		posts.setPaginationEnabled(false);

		postsListView = (ListView) findViewById(R.id.postListView);
		postsListView.setAdapter(posts);

		postsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Post post = (Post) parent.getItemAtPosition(position);
				Intent bundle = new Intent(MainActivity.this,
						SinglePostView.class);

				bundle.putExtra("postObjectId", post.getObjectId());
				bundle.putExtra("postText", post.getPostText());
				bundle.putExtra("postAuthor", post.getAuthor());
				bundle.putExtra("postDisplayName", post.getDisplayName());
				bundle.putExtra("postImage", post.getImageURL());
				bundle.putExtra("postLat", post.getLocation().getLatitude());
				bundle.putExtra("postLon", post.getLocation().getLongitude());

				startActivityForResult(bundle, 1);

			}
		});

		try {
			ParseFile imageFile = (ParseFile) ParseUser.getCurrentUser().get(
					"imageFile");
			imageUrl = imageFile.getUrl();
		} catch (Exception e) {
			imageUrl = "Anonymous";
			e.printStackTrace();
		}

		// Create a new global location parameters object
		locationRequest = LocationRequest.create();
		locationRequest
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		locationRequest.setInterval(REGULAR_INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);

		// Create a new location client, using the enclosing class to handle
		// callbacks.
		locationClient = new LocationClient(this, this, this);

		view = this.getLayoutInflater().inflate(R.layout.marker, null);

		Log.d(TAG, "Context: " + this);
		iconGenerator = new IconGenerator(this);
		Log.d(TAG, "view: " + view);
		iconGenerator.setContentView(view);

	}

	protected void newPost() {
		if (getLatLng() == null) {
			Toast.makeText(
					this,
					"Please wait until your location has been found, or you've travelled to a location",
					Toast.LENGTH_SHORT).show();
			return;
		}
		ParseGeoPoint myPoint = geoPointFromLatLng(getLatLng());
		if (postEditText.getText().toString().length() > 0) {
			if (postEditText.getText().toString().length() <= 160) {
				String displayName = Application.getDisplayName();
				ParseUser user = ParseUser.getCurrentUser();

				try {
					ParseFile imageFile = (ParseFile) ParseUser
							.getCurrentUser().get("imageFile");
					imageUrl = imageFile.getUrl();
				} catch (Exception e) {
					imageUrl = "Anonymous";
					e.printStackTrace();
				}

				Post cast = new Post();
				cast.setAuthor(user);
				cast.setPostText(postEditText.getText().toString());
				cast.setDisplayName(displayName);
				cast.setEpochTime();
				cast.setImageURL(imageUrl);
				cast.setLocation(myPoint);
				cast.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							Toast.makeText(MainActivity.this, R.string.sent,
									Toast.LENGTH_SHORT).show();
							postEditText.getText().clear();
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									postEditText.getWindowToken(), 0);
						} else {
							Toast.makeText(MainActivity.this,
									"Error: " + e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}

					}
				});
				Log.d(TAG, "new shout sent of length:"
						+ postEditText.getText().toString().length()
						+ postEditText.getText().toString());
				postEditText.getText().clear();
			} else {
				Toast.makeText(
						this,
						"The length of your message is too long: "
								+ postEditText.getText().length() + "/160",
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(
					this,
					"You have not entered any text! "
							+ postEditText.getText().length() + "/160",
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume called " + Application.getRadius());
		setMainActivityVisible(true);

		radius = Application.getRadius();

		doListQuery();
		doMapQuery();
		Log.d(TAG, "doMapQuery2");
	}

	@Override
	protected void onPause() {
		super.onPause();
		setMainActivityVisible(false);
	}

	private void setMainActivityVisible(boolean b) {
		mainActivityVisibility = b;

	}

	public static boolean getMainActivityVisible() {
		return mainActivityVisibility;
	}

	public static void doListQuery() {
		if (getLatLng() != null) {
			Log.d(TAG, "doListQuery");
			// Refreshes the list view with new data based
			// usually on updated location data.
			posts.loadObjects();

		}
	}

	public static void doMapQuery() {
		Log.d(TAG, "doMapQuery called");
		if (getLatLng() == null) {
			cleanUpMarkers(new HashSet<String>());
			Log.d(TAG, "getMyLoc() is null in doMapQuery");
			return;
		}
		LatLngBounds bounds = calculateBoundsWithCenter(getLatLng());
		ParseGeoPoint northEast = new ParseGeoPoint(bounds.northeast.latitude,
				bounds.northeast.longitude);
		ParseGeoPoint southWest = new ParseGeoPoint(bounds.southwest.latitude,
				bounds.southwest.longitude);
		ParseQuery<Post> query = new ParseQuery<Post>("Post");
		query.orderByDescending("createdAt");
		query.setLimit(1);
		query.whereWithinGeoBox("location", southWest, northEast);
		query.findInBackground(new FindCallback<Post>() {

			@Override
			public void done(List<Post> objects, ParseException e) {
				// TODO Auto-generated method stub
				for (final Post post : objects) {

					// Posts to show on the map
					Set<String> toKeep = new HashSet<String>();
					// Add this post to the list of map pins to keep
					toKeep.add(post.getObjectId());
					Log.d(TAG, "toKeep: " + toKeep);
					mapMarkers.get(post.getObjectId());
					new MarkerOptions().position(new LatLng(post.getLocation()
							.getLatitude(), post.getLocation().getLongitude()));

					TextView displayName = (TextView) view
							.findViewById(R.id.markerDisplayName);
					displayName.setText(post.getDisplayName());
					Log.d(TAG, "postDisplayName: " + post.getDisplayName());
					TextView postText = (TextView) view
							.findViewById(R.id.markerText);
					Log.d(TAG, "postText" + post.getPostText().toString());
					if (post.getPostText().length() > 40) {
						postText.setText(post.getPostText().toString()
								.substring(0, 37)
								+ "...");
					} else {
						postText.setText(post.getPostText());
					}
					ImageView postImage = (ImageView) view
							.findViewById(R.id.markerImage);
					if (post.getImageURL().equals("Anonymous")) {
						postImage.setImageResource(R.drawable.ic_launcher);
						MarkerOptions markerOpts1 = new MarkerOptions()
								.position(new LatLng(post.getLocation()
										.getLatitude(), post.getLocation()
										.getLongitude()));
						// Display a green marker with the post information

						Bitmap iconBitmap = iconGenerator.makeIcon();
						markerOpts1 = markerOpts1.icon(BitmapDescriptorFactory
								.fromBitmap(iconBitmap));

						map.clear();

						// Add a new marker
						Marker marker = map.addMarker(markerOpts1);
						// mapMarkers.put(post.getObjectId(), marker);
						haspMap.put(marker, post);
						// marker.showInfoWindow();
						// Clean up old markers.

					} else {
						// imageLoader.displayImage(post.getImageURL(),
						// postImage, options);
						imageLoader.displayImage(post.getImageURL(), postImage,
								options, new ImageLoadingListener() {

									@Override
									public void onLoadingStarted(
											String imageUri, View view) {
										// TODO Auto-generated method stub

									}

									@Override
									public void onLoadingFailed(
											String imageUri, View view,
											FailReason failReason) {
										// TODO Auto-generated method stub

									}

									@Override
									public void onLoadingComplete(
											String imageUri, View view,
											Bitmap loadedImage) {
										Log.d(TAG, "loading complete image");
										MarkerOptions markerOpts = new MarkerOptions()
												.position(new LatLng(post
														.getLocation()
														.getLatitude(), post
														.getLocation()
														.getLongitude()));
										// Display a green marker with the post
										// information

										Bitmap iconBitmap = iconGenerator
												.makeIcon();
										markerOpts = markerOpts.icon(BitmapDescriptorFactory
												.fromBitmap(iconBitmap));

										map.clear();

										// Add a new marker
										Marker marker = map
												.addMarker(markerOpts);
										// mapMarkers.put(post.getObjectId(),
										// marker);
										haspMap.put(marker, post);
										// marker.showInfoWindow();

									}

									@Override
									public void onLoadingCancelled(
											String imageUri, View view) {
										// TODO Auto-generated method stub

									}
								});
					}
					// Clean up old markers.
					// Log.d(TAG, "cleanUpMarkers: " + toKeep);
					// cleanUpMarkers(toKeep);
				}
			}
		});
	};

	/*
	 * Helper method to clean up old markers
	 */
	private static void cleanUpMarkers(Set<String> markersToKeep) {
		Log.d(TAG, "clean up Markers called");
		for (String objId : new HashSet<String>(mapMarkers.keySet())) {
			Log.d(TAG, "mapMarkers KeySet: " + mapMarkers.keySet() + "toKeep: "
					+ markersToKeep);
			if (!markersToKeep.contains(objId)) {
				Log.d(TAG, objId + " : " + markersToKeep);
				Marker marker = mapMarkers.get(objId);
				Log.d(TAG, "marker: " + marker);
				marker.remove();
				Log.d(TAG, "marker.remove: " + objId + " : " + marker);
				mapMarkers.get(objId).remove();
				mapMarkers.remove(objId);
			}
		}
	}

	public void createNewMarker(String author, String text, String imageURL,
			String displayName, final double lat, final double lon) {

		TextView markerDisplayName = (TextView) view
				.findViewById(R.id.markerDisplayName);
		markerDisplayName.setText(displayName);
		TextView postText = (TextView) view.findViewById(R.id.markerText);
		if (text.length() > 80) {
			postText.setText(text.substring(0, 77) + "...");
		} else {
			postText.setText(text);
		}
		ImageView postImage = (ImageView) view.findViewById(R.id.markerImage);
		if (imageURL.equals("Anonymous")) {
			postImage.setImageResource(R.drawable.ic_launcher);
			MarkerOptions markerOpts1 = new MarkerOptions()
					.position(new LatLng(lat, lon));

			Bitmap iconBitmap = iconGenerator.makeIcon();
			markerOpts1 = markerOpts1.icon(BitmapDescriptorFactory
					.fromBitmap(iconBitmap));

			map.clear();

			// Add a new marker
			Marker marker = map.addMarker(markerOpts1);
			// mapMarkers.put(post.getObjectId(), marker);
			// marker.showInfoWindow();
			// Clean up old markers.

		} else {
			// imageLoader.displayImage(post.getImageURL(),
			// postImage, options);
			imageLoader.displayImage(imageURL, postImage, options,
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String imageUri, View view) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							Log.d(TAG, "loading complete image");
							MarkerOptions markerOpts = new MarkerOptions()
									.position(new LatLng(lat, lon));
							// Display a green marker with the post
							// information

							Bitmap iconBitmap = iconGenerator.makeIcon();
							markerOpts = markerOpts
									.icon(BitmapDescriptorFactory
											.fromBitmap(iconBitmap));

							map.clear();

							// Add a new marker
							Marker marker = map.addMarker(markerOpts);

						}

						@Override
						public void onLoadingCancelled(String imageUri,
								View view) {
							// TODO Auto-generated method stub

						}
					});
		}

	}

	private void updateZoom(LatLng myLatLng) {
		// Get the bounds to zoom to
		LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);
		// Zoom to the given bounds
		map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));

	}

	private void setNewLatLng(double latitude, double longitude) {
		myLatLng = new LatLng(latitude, longitude);

	}

	public static LatLng getLatLng() {
		if (isTravelling) {
			return travellingLatLng;
		} else {
			return myLatLng;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationClient.connect();
	}

	@Override
	protected void onStop() {
		if (locationClient.isConnected()) {
			stopPeriodicUpdates();
		}
		locationClient.disconnect();

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (inSettings) {
			backFromSettings();

			radius = Application.getRadius();

			if (lastLocation != null) {
				setNewLatLng(lastLocation.getLatitude(),
						lastLocation.getLongitude());
				// if (lastRadius != radius) {
				updateZoom(getLatLng());
				// }

				updateCircle(getLatLng());
			}

			doListQuery();
			doMapQuery();
			Log.d(TAG, "doMapQuery3");
		} else {

			super.onBackPressed();
		}
	}

	private void backFromSettings() {
		inSettings = false;
		getFragmentManager().popBackStack();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
			}
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		if (getLocation() != null) {
			setNewLatLng(getLocation().getLatitude(), getLocation()
					.getLongitude());
		}
		startPeriodicUpdates();

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		if (lastLocation != null
				&& geoPointFromLocation(location).distanceInKilometersTo(
						geoPointFromLocation(lastLocation)) < 0.250) {
			setNewLatLng(location.getLatitude(), location.getLongitude());
			// If the location hasn't changed by more than 250 meters, ignore
			// it.
			return;
		}

		lastLocation = location;
		if (!hasSetUpInitialLocation) {
			// Zoom to the current location.
			Log.d(TAG, "on Location changed myLastLng: " + myLatLng
					+ hasSetUpInitialLocation);
			setNewLatLng(location.getLatitude(), location.getLongitude());
			updateZoom(getLatLng());
			updateCircle(getLatLng());
			doListQuery();
			doMapQuery();

			// Update the installation subscribed channels
			ParseInstallation installation = ParseInstallation
					.getCurrentInstallation();
			installation.put("location", geoPointFromLocation(location));
			installation.saveInBackground();

			hasSetUpInitialLocation = true;
			return;
		}
		if (!isTravelling) {
			doListQuery();
			doMapQuery();
			Log.d(TAG, "doMapQuery4");

			// Update the installation subscribed channels
			ParseInstallation installation = ParseInstallation
					.getCurrentInstallation();
			installation.put("location", geoPointFromLocation(location));
			installation.saveInBackground();
		}

	}

	private static ParseGeoPoint geoPointFromLocation(Location loc) {
		return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
	}

	private static ParseGeoPoint geoPointFromLatLng(LatLng loc) {
		return new ParseGeoPoint(loc.latitude, loc.longitude);
	}

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
					this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getFragmentManager(), TAG);
			}
			return false;
		}
	}

	private void startPeriodicUpdates() {
		locationClient.requestLocationUpdates(locationRequest, this);
		Log.d(TAG, "location request: " + locationRequest.getPriority());
	}

	private void stopPeriodicUpdates() {
		locationClient.removeLocationUpdates(this);
	}

	private Location getLocation() {
		if (servicesConnected()) {
			return locationClient.getLastLocation();
		} else {
			return null;
		}
	}

	private void showErrorDialog(int errorCode) {

		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			errorFragment.show(getFragmentManager(), TAG);
		}
	}

	private void updateCircle(LatLng myLatLng) {
		if (mapCircle == null) {
			mapCircle = map.addCircle(new CircleOptions().center(myLatLng)
					.radius(radius * METERS_PER_KILOMETER));
			mapCircle.setStrokeWidth(0);
			// int baseColor = Color.DKGRAY;
			// mapCircle.setStrokeColor(baseColor);
			// mapCircle.setStrokeWidth(2);
			// mapCircle.setFillColor(Color.argb(50, Color.red(baseColor),
			// Color.green(baseColor), Color.blue(baseColor)));
		}
		mapCircle.setCenter(myLatLng);
		mapCircle.setRadius(radius * METERS_PER_KILOMETER); // Convert radius in
															// feet to meters.
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
	private static double calculateLatLngOffset(LatLng myLatLng,
			boolean bLatOffset) {
		// The return offset, initialized to the default difference
		double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
		// Set up the desired offset distance in meters
		float desiredOffsetInMeters = radius * METERS_PER_KILOMETER;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new Settings())
					.addToBackStack("mainActivity").commit();
			inSettings = true;

			return true;

		default:
			return false;
		}

	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		Log.d(TAG, "onLowMemory called");
	}

	private class PlaceDetailsQuery extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			HttpURLConnection conn = null;
			StringBuilder jsonResults = new StringBuilder();
			try {
				StringBuilder sb = new StringBuilder(PLACES_API_BASE
						+ TYPE_DETAILS + OUT_JSON);
				sb.append("?placeid=" + URLEncoder.encode(params[0], "utf8"));
				sb.append("&key=" + API_KEY);

				URL url = new URL(sb.toString());
				conn = (HttpURLConnection) url.openConnection();
				Log.d(TAG, "openConnectionCalled");
				InputStreamReader in = new InputStreamReader(
						conn.getInputStream());

				// Load the results into a StringBuilder
				int read;
				char[] buff = new char[1024];
				while ((read = in.read(buff)) != -1) {
					jsonResults.append(buff, 0, read);
				}
				return jsonResults.toString();
			} catch (MalformedURLException e) {
				Log.d(TAG, "Error processing Places API URL", e);
				return null;
			} catch (IOException e) {
				Log.d(TAG, "Error connecting to Places API", e);
				return null;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Locating...");
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(TAG, "OnPlaceDetailsQuery onPostExecute" + result);
			LatLng latLng = null;

			try {
				// Create a JSON object hierarchy from the results
				JSONObject jsonObj = new JSONObject(result.toString());
				Log.d(TAG, "123123Results: " + jsonObj.getJSONObject("result"));
				Log.d(TAG,
						"123123Geometry: "
								+ jsonObj.getJSONObject("result")
										.getJSONObject("geometry"));
				double lat = jsonObj.getJSONObject("result")
						.getJSONObject("geometry").getJSONObject("location")
						.getDouble("lat");

				double lon = jsonObj.getJSONObject("result")
						.getJSONObject("geometry").getJSONObject("location")
						.getDouble("lng");

				latLng = new LatLng(lat, lon);
			} catch (JSONException e) {
				Log.d(TAG, "Cannot process JSON results", e);
			}

			if (latLng != null) {
				isTravelling = true;
				travellingLatLng = latLng;
				updateCircle(getLatLng());
				updateZoom(getLatLng());
				doMapQuery();
				doListQuery();
				Log.d(TAG, "doMapQuery5");
				ParseInstallation installation = ParseInstallation
						.getCurrentInstallation();
				installation.put("location", geoPointFromLatLng(getLatLng()));
				installation.saveInBackground();

				autoCompleteTextView.clearFocus();
			} else {
				Toast.makeText(MainActivity.this,
						"Something went wrong with getting the location",
						Toast.LENGTH_SHORT).show();
			}
			progressDialog.dismiss();
		}

	}

}
