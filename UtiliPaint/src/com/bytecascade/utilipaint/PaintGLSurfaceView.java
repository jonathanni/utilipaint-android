package com.bytecascade.utilipaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class PaintGLSurfaceView extends GLSurfaceView {

	private Context context;
	private PaintRenderer renderer;
	private int activePointerID;
	private float scaleFactor = 1f;
	private ScaleGestureDetector scaleDetector;

	public PaintGLSurfaceView(Context context) {
		super(context);
		this.context = context;
		setEGLContextClientVersion(2);
	}

	public PaintGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setEGLContextClientVersion(2);
	}

	public void setImage(Bitmap image) {
		setRenderer(renderer = new PaintRenderer(context, image));
	}

	private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// Scrolling uses math based on the viewport (as opposed to math
			// using pixels).

			// Pixel offset is the offset in screen pixels, while viewport
			// offset is the
			// offset within the current viewport.

			renderer.getImage().setTranslateX(
					renderer.getImage().getTranslateX() + distanceX);
			renderer.getImage().setTranslateY(
					renderer.getImage().getTranslateY() + distanceY);

			// TODO change how the graphics are drawn (in PaintImage)
			// TODO implement scaling
			return true;
		}
	};

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));

			invalidate();
			return true;
		}
	}

}
