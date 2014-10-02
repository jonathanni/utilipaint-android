package com.bytecascade.utilipaint;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class PaintGLSurfaceView extends GLSurfaceView {

	public PaintGLSurfaceView(Context context) {
		super(context);
		setEGLContextClientVersion(2);
		setRenderer(new PaintRenderer());
	}
	
	public PaintGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setEGLContextClientVersion(2);
		setRenderer(new PaintRenderer());
	}

}
