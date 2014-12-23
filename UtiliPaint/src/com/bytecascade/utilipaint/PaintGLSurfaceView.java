package com.bytecascade.utilipaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
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
	private float oX, oY, tX, tY; // , ptX, ptY;
	private float ptotX, ptotY, totX, totY;

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

	public void setImage(Bitmap image, int fullWidth, int fullHeight)
	{
		setRenderer(renderer = new PaintRenderer(context, image, this,
				fullWidth, fullHeight));
	}

	public PaintRenderer getRenderer()
	{
		return renderer;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (renderer == null)
			return false;

		final float SCALE = 1 / scaleFactor;

		switch (ev.getAction() & MotionEvent.ACTION_MASK)
		{
		// finger 1 down, finger 2 up
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;

			// oX = ev.getX() - ptX;
			// oY = ev.getY() - ptY;

			oX = ev.getX();
			oY = ev.getY();

			break;
		// finger moves across screen (sometimes when still)
		case MotionEvent.ACTION_MOVE:
			if (mode == ZOOM || ev.getPointerCount() != 1)
				break;

			tX = ev.getX() - oX;
			tY = ev.getY() - oY;

			totX = ptotX + tX * SCALE;
			totY = ptotY + tY * SCALE;

			{
				final float IWIDTH = renderer.getImageWidth(), IHEIGHT = renderer
						.getImageHeight();

				totX = Math.min(Math.max(-IWIDTH / 2, totX), IWIDTH / 2);
				totY = Math.min(Math.max(-IHEIGHT / 2, totY), IHEIGHT / 2);
			}

			// double distance = Math.sqrt(Math.pow(ev.getX() - (oX + ptX), 2)
			// + Math.pow(ev.getY() - (oY + ptY), 2));

			double distance = Math.sqrt(Math.pow(tX, 2) + Math.pow(tY, 2));

			if (distance > 0.01f)
				dragged = true;

			break;
		// finger 1 down, finger 2 down
		case MotionEvent.ACTION_POINTER_DOWN:
			if(((PaintActivity)context).getCurrentTool() != PaintTool.PAN_ZOOM)
				return false;
			
			if (ev.getPointerCount() != 2)
				break;

			mode = ZOOM;

			dragged = false;

			ptotX = totX;
			ptotY = totY;

			tX = 0;
			tY = 0;

			break;
		// finger 1 up, finger 2 up
		case MotionEvent.ACTION_UP:
			mode = NONE;

			dragged = false;

			ptotX = totX;
			ptotY = totY;

			tX = 0;
			tY = 0;

			break;
		// finger 1 down, finger 2 up
		case MotionEvent.ACTION_POINTER_UP:
			if(((PaintActivity)context).getCurrentTool() != PaintTool.PAN_ZOOM)
				return false;
			
			if (ev.getPointerCount() != 2)
				break;

			mode = NONE;

			dragged = true;

			ptotX = totX;
			ptotY = totY;

			tX = 0;
			tY = 0;

			oX = ev.getX(1 - ev.getActionIndex());
			oY = ev.getY(1 - ev.getActionIndex());

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
			final float SCALE = detector.getScaleFactor();
			scaleFactor *= SCALE;
			scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
			return true;
		}
	}

	public float[] getPSInfo()
	{
		return new float[] { -ptotX, ptotY, -totX, totY, scaleFactor };
	}
}
