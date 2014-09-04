package com.apogeeapp.apogee;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlaceAutoAdapter extends ArrayAdapter<Place> {
	private ArrayList<Place> place;

	public PlaceAutoAdapter(Context context, ArrayList<Place> place) {
		super(context, 0);
		this.place = place;
	}
	

	@Override
	public Place getItem(int position) {
		return place.get(position);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.auto_list_item, null);
			
			Place place = getItem(position);
			if (place !=null) {
				TextView textView = (TextView)view.findViewById(R.id.autoTextView);
				textView.setText(place.getDescription());
			}
		}
		return view;
	}
	
	

}
