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

	public PaintRenderer(Context context, Bitmap image) {
		this.context = context;
		this.rawImage = image;
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		this.image = new PaintImage(context, rawImage);
	}

	public void onDrawFrame(GL10 unused) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		Matrix.setLookAtM(vMatrix, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
		Matrix.multiplyMM(MVPMatrix, 0, projMatrix, 0, vMatrix, 0);

		image.draw(MVPMatrix);
	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		// This Projection Matrix is applied to object coordinates in the
		// onDrawFrame() method
		Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
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
}
