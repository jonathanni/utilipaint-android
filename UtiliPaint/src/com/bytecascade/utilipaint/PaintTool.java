package com.bytecascade.utilipaint;

import com.example.utilipaint.R;

public enum PaintTool
{
	PAN_ZOOM("Pan/Zoom Tool", R.drawable.pan_zoom), SELECTION(), MOVE_PIXELS(), MAGIC_WAND(), PAINT_BUCKET(), BRUSH(), ERASER(), EYEDROPPER(), TEXT(), SHAPE();

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
}
