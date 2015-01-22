package com.bytecascade.utilipaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
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
	private float oX, oY, tX, tY; // , ptX, ptY;
	private float ptotX, ptotY, totX, totY, tchX, tchY, optotX, optotY;

	private boolean dragged = true, down;

	public PaintGLSurfaceView(Context context)
	{
		super(context);
		this.context = context;
		this.setEGLContextClientVersion(2);
		this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public PaintGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context = context;
		this.setEGLContextClientVersion(2);
		this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
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

		final PaintActivity pAct = (PaintActivity) context;

		tchX = -optotX + (ev.getX(0) - renderer.getWidth() / 2) * SCALE
				+ renderer.getImageWidth() / 2;
		tchY = -optotY + (ev.getY(0) - renderer.getHeight() / 2) * SCALE
				+ renderer.getImageHeight() / 2;

		int rtchX = (int) Math.round(tchX), rtchY = (int) Math.round(tchY);
		boolean inBounds = rtchX >= 0 && rtchX < renderer.getImageWidth()
				&& rtchY >= 0 && rtchY < renderer.getImageHeight();

		if (inBounds)
		{
			if (pAct.getCurrentTool() == PaintTool.BRUSH && rtchX >= 0)
				pAct.getPaintEvents().add(
						new PaintAction(PaintAction.PaintActionType.PAINT, Math
								.max(rtchX - pAct.getBrushRadius() + 1, 0),
								Math.max(rtchY - pAct.getBrushRadius() + 1, 0),
								Math.min(rtchX + pAct.getBrushRadius(),
										renderer.getImageWidth()), Math.min(
										rtchY + pAct.getBrushRadius(),
										renderer.getImageHeight()),
								new Object[] { new int[] { rtchX, rtchY },
										pAct.getPrimaryColor() }));

			if (pAct.getCurrentTool() == PaintTool.ERASER)
				pAct.getPaintEvents()
						.add(new PaintAction(PaintAction.PaintActionType.PAINT,
								Math.max(rtchX - pAct.getBrushRadius() + 1, 0),
								Math.max(rtchY - pAct.getBrushRadius() + 1, 0),
								Math.min(rtchX + pAct.getBrushRadius(),
										renderer.getImageWidth()), Math.min(
										rtchY + pAct.getBrushRadius(),
										renderer.getImageHeight()),
								new Object[] { new int[] { rtchX, rtchY }, 0 }));
		}

		if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
		{
			down = true;
			if (pAct.getCurrentTool() == PaintTool.SELECTION && inBounds)
				((PaintActivity) this.context).setRectSelectionPoints(rtchX,
						rtchY, rtchX, rtchY);
		} else if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP)
			down = false;

		if (pAct.getCurrentTool() == PaintTool.SELECTION && inBounds)
		{
			Point p = ((PaintActivity) this.context).getRectSelectionPoints()[0];
			((PaintActivity) this.context).setRectSelectionPoints(p.x, p.y,
					rtchX, rtchY);
		}

		if (pAct.getCurrentTool() != PaintTool.PAN_ZOOM
				&& pAct.getCurrentTool() != PaintTool.MOVE_PIXELS)
			return true;

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

			if (pAct.getCurrentTool() == PaintTool.PAN_ZOOM)
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
			if (pAct.getCurrentTool() == PaintTool.MOVE_PIXELS)
				break;

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
			if (pAct.getCurrentTool() == PaintTool.MOVE_PIXELS)
				break;

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

		if (pAct.getCurrentTool() != PaintTool.MOVE_PIXELS)
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
		return new float[] { tchX, tchY, -totX, totY, scaleFactor, -optotX,
				optotY };
	}

	public boolean isDown()
	{
		return down;
	}

	public void storeCoords()
	{
		optotX = ptotX;
		optotY = ptotY;

		ptotX = 0;
		ptotY = 0;

		totX = 0;
		totY = 0;
	}

	public void restoreCoords()
	{
		ptotX = optotX;
		ptotY = optotY;

		totX = ptotX;
		totY = ptotY;
	}
}
