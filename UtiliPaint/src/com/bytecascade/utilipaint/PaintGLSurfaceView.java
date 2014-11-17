package com.bytecascade.utilipaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class PaintGLSurfaceView extends GLSurfaceView
{

	private Context context;
	private PaintRenderer renderer;
	private int mode;
	private float scaleFactor = 1f;
	private ScaleGestureDetector scaleDetector;

	// 10%, 2400%
	private static float MIN_ZOOM = 0.1f, MAX_ZOOM = 24f;
	private static int NONE = 0, DRAG = 1, ZOOM = 2;

	// Original (x,y), Translate (Dx,Dy), Previous Translate (Dx,Dy)
	private float oX, oY, tX, tY, ptX, ptY;

	private boolean dragged = true;

	public PaintGLSurfaceView(Context context)
	{
		super(context);
		this.context = context;
		setEGLContextClientVersion(2);
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public PaintGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context = context;
		setEGLContextClientVersion(2);
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public void setImage(Bitmap image)
	{
		setRenderer(renderer = new PaintRenderer(context, image));
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (renderer == null)
			return true;

		switch (ev.getAction() & MotionEvent.ACTION_MASK)
		{
		// finger 1 down, finger 2 up
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;

			oX = ev.getX() - ptX;
			oY = ev.getY() - ptY;

			break;
		// finger moves across screen (sometimes when still)
		case MotionEvent.ACTION_MOVE:
			tX = ev.getX() - oX;
			tY = ev.getY() - oY;

			double distance = Math.sqrt(Math.pow(ev.getX() - (oX + ptX), 2)
					+ Math.pow(ev.getY() - (oY + ptY), 2));

			if (distance > 0)
				dragged = true;

			break;
		// finger 1 down, finger 2 down
		case MotionEvent.ACTION_POINTER_DOWN:
			mode = ZOOM;
			break;
		// finger 1 up, finger 2 up
		case MotionEvent.ACTION_UP:
			mode = NONE;

			dragged = false;

			ptX = tX;
			ptY = tY;
			break;
		// finger 1 down, finger 2 up
		case MotionEvent.ACTION_POINTER_UP:
			mode = ZOOM;

			ptX = tX;
			ptY = tY;
			break;
		}

		scaleDetector.onTouchEvent(ev);

		if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM)
			requestRender();

		return true;
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener
	{
		@Override
		public boolean onScale(ScaleGestureDetector detector)
		{
			scaleFactor *= detector.getScaleFactor();
			scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
			return true;
		}
	}

	public float[] getPSInfo()
	{
		return new float[] { tX, tY, scaleFactor };
	}

}
