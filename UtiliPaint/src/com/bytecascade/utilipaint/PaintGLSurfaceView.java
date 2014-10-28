package com.bytecascade.utilipaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class PaintGLSurfaceView extends GLSurfaceView {

	private Context context;
	private PaintRenderer renderer;

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

}
