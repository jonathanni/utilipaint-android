package com.bytecascade.utilipaint;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class PaintRenderer implements Renderer
{

	private final float[] MVPMatrix = new float[16];
	private final float[] projMatrix = new float[16];
	private final float[] vMatrix = new float[16];

	private PaintImage image;
	private Context context;
	private Bitmap rawImage;

	private int width, height, iwidth, iheight;

	private PaintGLSurfaceView surfaceView;

	private long startTime, endTime, dt;

	public PaintRenderer(Context context, Bitmap image,
			PaintGLSurfaceView glSurfaceView, int iwidth, int iheight)
	{
		this.context = context;
		this.rawImage = image;
		this.surfaceView = glSurfaceView;

		this.iwidth = iwidth;
		this.iheight = iheight;

		startTime = System.currentTimeMillis();
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config)
	{
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		this.image = new PaintImage(context, rawImage, surfaceView,
				this.iwidth, this.iheight);
	}

	public void onDrawFrame(GL10 unused)
	{
		endTime = System.currentTimeMillis();

		dt = endTime - startTime;
		if (dt < 33)
			try
			{
				Thread.sleep(33 - dt);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		startTime = System.currentTimeMillis();

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		float[] transforms = surfaceView.getPSInfo();

		final float SCALE = 1 / transforms[4];

		Matrix.setLookAtM(vMatrix, 0, iwidth / 2 + transforms[2], iheight / 2
				+ transforms[3], 1, iwidth / 2 + transforms[2], iheight / 2
				+ transforms[3], 0, 0, 1, 0);
		Matrix.orthoM(projMatrix, 0, SCALE * -width / 2, SCALE * width / 2,
				SCALE * -height / 2, SCALE * height / 2, 0.1f, 2);
		Matrix.multiplyMM(MVPMatrix, 0, projMatrix, 0, vMatrix, 0);

		// Draw image
		image.draw(MVPMatrix);
	}

	public void onSurfaceChanged(GL10 unused, int width, int height)
	{
		GLES20.glViewport(0, 0, width, height);

		this.width = width;
		this.height = height;

		Log.d("com.bytecascade.utilipaint", "Create Surface w: " + width
				+ " h: " + height);
	}

	public static int loadShader(int type, String shaderCode)
	{
		// Create a Vertex Shader Type Or a Fragment Shader Type
		// (GLES20.GL_VERTEX_SHADER OR GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// Add The Source Code and Compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	public PaintImage getImage()
	{
		return image;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public int getImageWidth()
	{
		return iwidth;
	}

	public int getImageHeight()
	{
		return iheight;
	}

	public long getFrameTime()
	{
		return dt;
	}
}
