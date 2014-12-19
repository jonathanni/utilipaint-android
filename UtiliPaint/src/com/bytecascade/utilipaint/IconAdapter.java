package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IconAdapter extends ArrayAdapter<String>
{
	private Context context;

	private static String[] strings = new String[] { "Pan/Zoom Tool" };
	private static int[] images = new int[] { R.drawable.panzoom };

	public IconAdapter(Context context, int resource, String[] objects)
	{
		super(context, resource, objects);

		this.context = context;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		return getCustomView(position, convertView, parent);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent)
	{

		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View row = inflater.inflate(R.layout.row, parent, false);
		TextView label = (TextView) row.findViewById(R.id.toolText);
		label.setText(strings[position]);

		ImageView icon = (ImageView) row.findViewById(R.id.image);
		icon.setImageResource(images[position]);

		return row;
	}

	public static String[] getStrings()
	{
		return strings;
	}
}
