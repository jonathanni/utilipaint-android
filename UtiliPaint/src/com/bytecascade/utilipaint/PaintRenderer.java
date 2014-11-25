package com.bytecascade.utilipaint;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

public class PaintRenderer implements Renderer {

	private final float[] MVPMatrix = new float[16];
	private final float[] projMatrix = new float[16];
	private final float[] vMatrix = new float[16];

	private PaintImage image;
	private Context context;
	private Bitmap rawImage;

	private int width, height;

	private PaintGLSurfaceView surfaceView;

	public PaintRenderer(Context context, Bitmap image,
			PaintGLSurfaceView glSurfaceView) {
		this.context = context;
		this.rawImage = image;
		this.surfaceView = glSurfaceView;
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		this.image = new PaintImage(context, rawImage, surfaceView);

		this.image.setWidth(rawImage.getWidth());
		this.image.setHeight(rawImage.getHeight());
	}

	public void onDrawFrame(GL10 unused) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		float[] transforms = surfaceView.getPSInfo();

		Matrix.setLookAtM(vMatrix,0,transforms[0],0,4,transforms[0],0,0,0,1,0);
		
		/*
		Matrix.setLookAtM(vMatrix, 0, width / 2 + transforms[0], height / 2
				+ transforms[1], 10, width / 2 + transforms[0], height / 2
				+ transforms[1], 0, 0, 1, 0);
				*/
		
		Matrix.multiplyMM(MVPMatrix, 0, projMatrix, 0, vMatrix, 0);

		image.draw(MVPMatrix);
	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		this.width = width;
		this.height = height;

		// This Projection Matrix is applied to object coordinates in the
		// onDrawFrame() method
		Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 0.1f, 5);
	}

	public static int loadShader(int type, String shaderCode) {
		// Create a Vertex Shader Type Or a Fragment Shader Type
		// (GLES20.GL_VERTEX_SHADER OR GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// Add The Source Code and Compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	public PaintImage getImage() {
		return image;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
