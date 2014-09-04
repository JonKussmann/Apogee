package com.apogeeapp.apogee;




import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImagePicker extends Preference {
	
	public ImageView imageView, profileImage;
	private static final String TAG = "ImagePicker";
	
	public ImagePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImagePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.setWidgetLayoutResource(R.layout.single_image);
		
		
	
	}


	@Override
	protected View onCreateView(ViewGroup parent) {
		return super.onCreateView(parent);
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		
		SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getContext());
		String picturePath = preference.getString(Settings.PROFILE_PICTURE, null);
		profileImage = (ImageView) view.findViewById(R.id.markerImage);
		
		if (picturePath == null) {
			profileImage.setImageResource(R.drawable.ic_launcher);
		} else {
			Bitmap bm = BitmapFactory.decodeFile(picturePath);
			Bitmap bmScaled = Bitmap.createScaledBitmap(bm, 150, 150
					* bm.getHeight() / bm.getWidth(), false);
			profileImage.setImageBitmap(bmScaled);
			Log.d(TAG, "image should be set as chosen picture");
		}
	}

}
