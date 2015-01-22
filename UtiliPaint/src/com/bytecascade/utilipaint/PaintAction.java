package com.bytecascade.utilipaint;

import android.graphics.Point;

public class PaintAction
{
	private PaintActionType actionType;
	private int x1, y1, x2, y2;
	private Object data;

	public PaintAction(PaintActionType type)
	{
		this(type, 0, 0, 0, 0);
	}

	public PaintAction(PaintActionType type, int x1, int y1, int x2, int y2)
	{
		this(type, x1, y1, x2, y2, null);
	}

	public PaintAction(PaintActionType type, int x1, int y1, int x2, int y2,
			Object data)
	{
		setActionType(type);
		setBounds(x1, y1, x2, y2);
		setData(data);
	}

	public void setBounds(int x1, int y1, int x2, int y2)
	{
		this.x1 = x1;
		this.y1 = y1;

		this.x2 = x2;
		this.y2 = y2;
	}

	public Point[] getBounds()
	{
		return new Point[] { new Point(this.x1, this.y1),
				new Point(this.x2, this.y2) };
	}

	public PaintActionType getActionType()
	{
		return actionType;
	}

	public void setActionType(PaintActionType actionType)
	{
		this.actionType = actionType;
	}

	public Object getData()
	{
		return data;
	}

	public void setData(Object data)
	{
		this.data = data;
	}

	// PAINT
	// x1, y1 = tlbound
	// x2, y2 = brbound
	// data = {int[2], int}

	// REPLACE_PIXELS
	// x1, y1 = tlbound
	// x2, y2 = brbound
	// data = {int[4], int[abs(x1-x2)*abs(y1-y2)]}

	enum PaintActionType
	{
		PAINT, REPLACE_PIXELS;
	}
}
