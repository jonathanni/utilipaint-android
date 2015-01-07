package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

public enum PaintTool
{
	PAN_ZOOM("Pan/Zoom Tool", R.drawable.pan_zoom, R.layout.pan_zoom), SELECTION(
			"Selection Tool", R.drawable.selection, R.layout.selection), MOVE_PIXELS(
			"Move Pixels Tool", R.drawable.move_pixels, R.layout.move_pixels), MAGIC_WAND(
			"Magic Wand Tool", R.drawable.magic_wand, R.layout.magic_wand), PAINT_BUCKET(
			"Paint Bucket Tool", R.drawable.paint_bucket, R.layout.paint_bucket), BRUSH(
			"Brush Tool", R.drawable.brush, R.layout.brush), ERASER(
			"Eraser Tool", R.drawable.eraser, R.layout.eraser), EYEDROPPER(
			"Eyedropper Tool", R.drawable.color_selection,
			R.layout.color_selection), TEXT("Text Tool", R.drawable.text,
			R.layout.text), SHAPE("Shape Tool", R.drawable.shape,
			R.layout.shape);

	private final String name;
	private final int resourceID, layoutID;

	private PaintTool(String name, int rID, int lID)
	{
		this.name = name;
		this.resourceID = rID;
		this.layoutID = lID;
	}

	public String getName()
	{
		return name;
	}

	public int getResourceID()
	{
		return resourceID;
	}

	public int getLayoutID()
	{
		return layoutID;
	}

	@Override
	public String toString()
	{
		return name + ":" + resourceID + ":" + layoutID;
	}
}
