package com.apogeeapp.apogee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

public class Settings extends PreferenceFragment {
	private static final String TAG = "PreferenceFragment";
	public static final String PROFILE_PICTURE = "select_image";
	public static final int RESULT_LOAD_IMAGE = 1;
	private static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";
	private ImageView profileImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference imagePreference = findPreference("select_image");
		Log.d(TAG, "asfasdfa" + imagePreference.toString());
		imagePreference
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent();
						intent.setType("image/");
						intent.putExtra("crop", true);
						intent.putExtra("aspectX", 200);
						intent.putExtra("aspectY", 200);
						intent.putExtra("outputX", 200);
						intent.putExtra("outputY", 200);
						intent.putExtra("scale", true);
						intent.putExtra("scaleUpIfNeeded", true);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
						intent.putExtra("outputFormar",
								Bitmap.CompressFormat.JPEG.toString());
						intent.setAction(Intent.ACTION_PICK);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								RESULT_LOAD_IMAGE);
						
						Log.d(TAG, "startActivityForResult");

						return true;
					}
				});
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		getView().setBackgroundColor(Color.WHITE);
	}

	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}

	private File getTempFile() {
		if (isSDCARDMounted()) {

			File f = new File(Environment.getExternalStorageDirectory(),
					TEMP_PHOTO_FILE);
			try {
				f.createNewFile();
			} catch (IOException e) {

			}
			return f;
		} else {
			return null;
		}
	}

	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult called");

		if (requestCode == RESULT_LOAD_IMAGE
				&& resultCode == Activity.RESULT_OK && null != data) {
			Log.d(TAG, "start of if statement on activity result" + requestCode + resultCode + data);
			
			String filePath = Environment.getExternalStorageDirectory() + "/temporary_holder.jpg";
			Log.d(TAG, "path" + filePath);
			
			// String picturePath contains the path of selected Image
			Bitmap bm = BitmapFactory.decodeFile(filePath);
			Bitmap bmScaled = Bitmap.createScaledBitmap(bm, 150,
					150 * bm.getHeight() / bm.getWidth(), false);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bmScaled.compress(Bitmap.CompressFormat.JPEG, 75, baos);
			byte[] b = baos.toByteArray();
			

			profileImage = (ImageView) getView().findViewById(R.id.markerImage);
			profileImage.setImageBitmap(bmScaled);

			SharedPreferences imagePreference = getPreferenceManager()
					.getSharedPreferences();
			imagePreference.edit().putString(PROFILE_PICTURE, filePath)
					.commit();

			final ParseFile parseFile = new ParseFile("image.jpeg", b);
			parseFile.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
					ParseUser user = ParseUser.getCurrentUser();
					user.put("imageFile", parseFile);
					user.saveInBackground();
					Log.d(TAG, "picture has been saved to parse!!! well, hopefully..." + user.getObjectId());
					} else {
						e.printStackTrace();
					}
					

				
				}

			});
			Log.d(TAG, "end of if statement on activity result");
		}
		Log.d(TAG, "end of on activity result");
	}


}
