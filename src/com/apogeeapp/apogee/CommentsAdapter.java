package com.apogeeapp.apogee;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class CommentsAdapter extends ParseQueryAdapter<Comment> {
	private static final String TAG = "CommentsAdapter";

	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public CommentsAdapter(Context context, final String postId) {
		super(context, new ParseQueryAdapter.QueryFactory<Comment>() {

			@Override
			public ParseQuery<Comment> create() {
				ParseQuery<Comment> query = new ParseQuery<Comment>("Comment");
				query.whereEqualTo("post", postId);
				query.orderByDescending("epochTime");
				query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
				return query;
			}

		});

		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_launcher)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.cacheInMemory(true).build();
	}

	@Override
	public View getItemView(final Comment comment, View view, ViewGroup parent) {
		super.getItemView(comment, view, parent);
		if(view == null) {
			view = View.inflate(getContext(), R.layout.posts_row_view, null);
		}
		
		super.getItemView(comment, view, parent);

		TextView castTextView = (TextView) view.findViewById(R.id.postText);
		Log.d(TAG, "top Post Text: " + comment.getCommentText());
		castTextView.setText(comment.getCommentText());

		TextView castDisplayName = (TextView) view
				.findViewById(R.id.postDisplayName);
		castDisplayName.setText(comment.getDisplayName());

		TextView castEpochTime = (TextView) view.findViewById(R.id.postTime);
		Long time = comment.getEpochTime();
		CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(time,
				System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
		castEpochTime.setText(relativeTime);

		ImageView castImage = (ImageView) view.findViewById(R.id.postImage);
		if (comment.getImageURL().equals("Anonymous")) {
			castImage.setImageResource(R.drawable.ic_launcher);
		} else {
			imageLoader.displayImage(comment.getImageURL(), castImage, options);
		}

		return view;
	}

}
