package com.bytecascade.utilipaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class PaintGLSurfaceView extends GLSurfaceView {

	PaintRenderer renderer;

	public PaintGLSurfaceView(Context context) {
		super(context);
		setEGLContextClientVersion(2);
		setRenderer(renderer = new PaintRenderer(context, null));
	}

	public PaintGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setEGLContextClientVersion(2);
		setRenderer(renderer = new PaintRenderer(context, null));
	}

	public void setImage(Bitmap image) {
		renderer.setImage(image);
	}

}
