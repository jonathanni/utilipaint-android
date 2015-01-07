package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IconAdapter extends ArrayAdapter<String>
{
	private Context context;

	private static String[] strings;
	private static int[] images;
	private static Bitmap[] bitmaps;
	private static final int ICON_SIZE = 48;

	static
	{
		strings = new String[PaintTool.values().length];
		images = new int[PaintTool.values().length];
		bitmaps = new Bitmap[PaintTool.values().length];

		int j = 0;
		for (PaintTool i : PaintTool.values())
		{
			strings[j] = i.getName();
			images[j++] = i.getResourceID();
		}
	}

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

		if (bitmaps[position] == null)
		{
			Bitmap b = BitmapFactory.decodeResource(
					((PaintActivity) context).getResources(), images[position]);
			bitmaps[position] = Bitmap.createScaledBitmap(b, ICON_SIZE,
					ICON_SIZE, false);
			b.recycle();
		}

		icon.setImageBitmap(bitmaps[position]);

		return row;
	}

	public static String[] getStrings()
	{
		return strings;
	}
}
