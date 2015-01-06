package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

public enum PaintTool
{
	PAN_ZOOM("Pan/Zoom Tool", R.drawable.pan_zoom), SELECTION("Selection Tool",
			R.drawable.selection), MOVE_PIXELS("Move Pixels Tool",
			R.drawable.move_pixels), MAGIC_WAND("Magic Wand Tool",
			R.drawable.magic_wand), PAINT_BUCKET("Paint Bucket Tool",
			R.drawable.paint_bucket), BRUSH("Brush Tool", R.drawable.brush), ERASER(
			"Eraser Tool", R.drawable.eraser), EYEDROPPER(
			"Color Selection Tool", R.drawable.color_selection), TEXT(
			"Text Tool", R.drawable.text), SHAPE("Shape Tool", R.drawable.shape);

	private final String name;
	private final int resourceID;

	private PaintTool(String name, int rID)
	{
		this.name = name;
		this.resourceID = rID;
	}

	public String getName()
	{
		return name;
	}

	public int getResourceID()
	{
		return resourceID;
	}

	@Override
	public String toString()
	{
		return name + ": " + resourceID;
	}
}
