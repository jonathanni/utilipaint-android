package com.bytecascade.utilipaint;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class PaintRenderer implements Renderer
{

	private final float[] MVPMatrix = new float[16];
	private final float[] projMatrix = new float[16];
	private final float[] vMatrix = new float[16];

	private PaintCheckerboard bg;
	private PaintSelectionRect selection;
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
		GLES20.glClearColor(0.811764f, 0.811764f, 0.811764f, 1.0f);
		this.image = new PaintImage(this.context, this.rawImage,
				this.surfaceView, this.iwidth, this.iheight);
		this.selection = new PaintSelectionRect();
		this.bg = new PaintCheckerboard(context, this.iwidth, this.iheight);
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

		{
			float dx = transforms[2], dy = transforms[3];

			if (((PaintActivity) context).getCurrentTool() != PaintTool.PAN_ZOOM)
			{
				dx = transforms[5];
				dy = transforms[6];
			}

			Matrix.setLookAtM(vMatrix, 0, iwidth / 2 + dx, iheight / 2 + dy, 1,
					iwidth / 2 + dx, iheight / 2 + dy, 0, 0, 1, 0);
		}

		Matrix.orthoM(projMatrix, 0, SCALE * -width / 2, SCALE * width / 2,
				SCALE * -height / 2, SCALE * height / 2, 0.1f, 2);
		Matrix.multiplyMM(MVPMatrix, 0, projMatrix, 0, vMatrix, 0);

		// Draw checkerboard
		this.bg.setScale(transforms[4]);

		this.bg.draw(MVPMatrix);

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		GLES20.glEnable(GLES20.GL_ALPHA_BITS);

		// Draw image
		this.image.draw(MVPMatrix);

		GLES20.glDisable(GLES20.GL_ALPHA_BITS);

		{
			int dx = 0, dy = 0;

			if (((PaintActivity) this.context).getCurrentTool() == PaintTool.MOVE_PIXELS)
			{
				dx = -(int) transforms[2];
				dy = -(int) transforms[3];
			}

			Point[] p = ((PaintActivity) this.context).getRectSelectionPoints();
			this.selection.setCoords(p[0].x + dx, this.iheight - p[0].y + dy,
					p[1].x + dx, this.iheight - p[1].y + dy);
		}

		this.selection.draw(MVPMatrix);
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

	public PaintSelectionRect getSelection()
	{
		return selection;
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
