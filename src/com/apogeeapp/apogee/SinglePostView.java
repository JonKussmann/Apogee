package com.apogeeapp.apogee;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SinglePostView extends Activity {
	private static final String TAG = "SinglePostView";
	private String postId, postText, postAuthor, postDisplayName, postImageUrl,
			postEpochTime;
	private ParseGeoPoint postLocation;
	private double postLat, postLon;

	private EditText commentEditText;
	private ImageButton sendButton;
	private String imageUrl;
	private CommentsAdapter adapter;
	private ListView commentsListView;

	private static MapFragment map;
	
	private static View view;
	private static IconGenerator iconGenerator;
	
	private static ImageLoader imageLoader;
	private static DisplayImageOptions options;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_post_view);

		try {
			ParseFile imageFile = (ParseFile) ParseUser.getCurrentUser().get(
					"imageFile");
			imageUrl = imageFile.getUrl();
		} catch (Exception e) {
			imageUrl = "Anonymous";
			e.printStackTrace();
		}

		Intent bundle = getIntent();
		postId = bundle.getStringExtra("postObjectId");
		postText = bundle.getStringExtra("postText");
		postAuthor = bundle.getStringExtra("postAuthor");
		postDisplayName = bundle.getStringExtra("postDisplayName");
		postImageUrl = bundle.getStringExtra("postImage");
		postLat = bundle.getDoubleExtra("postLat", 0);
		postLon = bundle.getDoubleExtra("postLon", 0);
		Log.d(TAG, "postLon:" + postLon);
		Log.d(TAG, "postLat: " + postLat);

		commentEditText = (EditText) findViewById(R.id.singlePostEditText);
		sendButton = (ImageButton) findViewById(R.id.singlePostSendButton);
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				newComment();

			}
		});
		
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_launcher)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.cacheInMemory(true).build();
		
		

		commentsListView = (ListView) findViewById(R.id.singlePostListView);
		adapter = new CommentsAdapter(this, postId);
		commentsListView.setAdapter(adapter);

		map = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.singlePostMap);
		
		
		view = this.getLayoutInflater().inflate(R.layout.marker, null);

		Log.d(TAG, "Context: " + this);
		iconGenerator = new IconGenerator(this);
		Log.d(TAG, "view: " + view);
		iconGenerator.setContentView(view);
		
		TextView displayName = (TextView) view
				.findViewById(R.id.markerDisplayName);
		displayName.setText(postDisplayName);
		TextView text = (TextView) view
				.findViewById(R.id.markerText);
		if (postText.length() > 40) {
			text.setText(postText
					.substring(0, 37)
					+ "...");
		} else {
			text.setText(postText.toString());
		}
		ImageView postImage = (ImageView) view
				.findViewById(R.id.markerImage);
		if (postImageUrl.equals("Anonymous")) {
			postImage.setImageResource(R.drawable.ic_launcher);
			MarkerOptions markerOpts1 = new MarkerOptions()
					.position(new LatLng(postLat, postLon));
			// Display a green marker with the post information

			Bitmap iconBitmap = iconGenerator.makeIcon();
			markerOpts1 = markerOpts1.icon(BitmapDescriptorFactory
					.fromBitmap(iconBitmap));

			map.getMap().clear();

			// Add a new marker
			Marker marker = map.getMap().addMarker(markerOpts1);
			// mapMarkers.put(post.getObjectId(), marker);
		} else {
			// imageLoader.displayImage(post.getImageURL(),
			// postImage, options);
			imageLoader.displayImage(postImageUrl, postImage,
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
									.position(new LatLng(postLat, postLon));
							// Display a green marker with the post
							// information

							Bitmap iconBitmap = iconGenerator
									.makeIcon();
							markerOpts = markerOpts.icon(BitmapDescriptorFactory
									.fromBitmap(iconBitmap));

							map.getMap().clear();

							// Add a new marker
							Marker marker = map.getMap()
									.addMarker(markerOpts);
							
						}

						@Override
						public void onLoadingCancelled(
								String imageUri, View view) {
							// TODO Auto-generated method stub

						}
					});
		}

	}

	protected void newComment() {
		if (commentEditText.getText().toString().length() > 0) {
			if (commentEditText.getText().toString().length() <= 160) {
				String displayName = Application.getDisplayName();
				ParseUser user = ParseUser.getCurrentUser();

				ParseGeoPoint myPoint = geoPointFromLatLng(MainActivity
						.getLatLng());

				Comment comment = new Comment();
				comment.setAuthor(user);
				comment.setPost(postId);
				comment.setCommentText(commentEditText.getText().toString());
				comment.setDisplayName(displayName);
				comment.setEpochTime();
				comment.setImageURL(imageUrl);
				comment.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							Toast.makeText(SinglePostView.this, R.string.sent,
									Toast.LENGTH_SHORT).show();
							commentEditText.getText().clear();
							adapter.clear();
							adapter.loadObjects();
							adapter.notifyDataSetChanged();
						} else {
							Toast.makeText(SinglePostView.this,
									"Error: " + e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}

					}
				});
				Log.d(TAG, "new shout sent of length:"
						+ commentEditText.getText().toString().length()
						+ commentEditText.getText().toString());
				commentEditText.getText().clear();
			} else {
				Toast.makeText(
						this,
						"The length of your comment is too long: "
								+ commentEditText.getText().length() + "/160",
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(
					this,
					"You have not entered any text! "
							+ commentEditText.getText().length() + "/160",
					Toast.LENGTH_LONG).show();
		}

	}

	private static ParseGeoPoint geoPointFromLatLng(LatLng loc) {
		return new ParseGeoPoint(loc.latitude, loc.longitude);
	}

	@Override
	protected void onResume() {
		super.onResume();

		map.getMap().setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition position) {
						CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(postLat, postLon))
						.zoom(15)
						.build();
						map.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
						map.getMap().setOnCameraChangeListener(null);
						
					}
				});

			}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new Settings()).addToBackStack("mainActivity")
            .commit();
			
			return true;
			
		case android.R.id.home:
			onBackPressed();
			return true;
			
		default: 
			return false;
		}
		
		
	}
}
