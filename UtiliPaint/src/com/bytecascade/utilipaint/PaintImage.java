package com.bytecascade.utilipaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class PaintImage
{
	private Context context;

	private final FloatBuffer imageTextureCoordinates;
	private int textureUniformHandle;
	private int textureCoordinateHandle;
	private final int textureCoordinateDataSize = 2;
	private int textureDataHandle;
	private static int[] textureHandle = new int[1];

	private static final String VERTEX_SHADER_CODE =

	"attribute vec2 a_TexCoordinate;" + "varying vec2 v_TexCoordinate;"
			+ "uniform mat4 uMVPMatrix;" + "attribute vec4 vPosition;"
			+ "void main() {" + "  gl_Position = uMVPMatrix * vPosition;"
			+ "v_TexCoordinate = a_TexCoordinate;" + "}";

	private static final String FRAGMENT_SHADER_CODE =

	"precision mediump float;"
			+ "uniform vec4 vColor;"
			+ "uniform sampler2D u_Texture;"
			+ "varying vec2 v_TexCoordinate;"
			+ "void main() {"
			+ "gl_FragColor = (vColor * texture2D(u_Texture, v_TexCoordinate));"
			+ "}";

	private final int shaderProgram;
	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;
	private int positionHandle;
	private int colorHandle;
	private int MVPMatrixHandle;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 2;
	static float imageCoords[] = { 0, 0, // top left
			0, 0, // bottom left
			0, 0, // bottom right
			0, 0 }; // top right

	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // Order to draw vertices
	private final int vertexStride = COORDS_PER_VERTEX * 4; // Bytes per vertex

	private float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

	private Bitmap image;

	private PaintGLSurfaceView surfaceView;

	private float width, height;
	private int fullWidth, fullHeight;

	public PaintImage(final Context activityContext, Bitmap image,
			PaintGLSurfaceView glSurfaceView, int fwidth, int fheight)
	{
		this.context = activityContext;

		ByteBuffer bb = ByteBuffer.allocateDirect(imageCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());

		vertexBuffer = bb.asFloatBuffer();

		imageTextureCoordinates = ByteBuffer
				.allocateDirect(imageCoords.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		ByteBuffer dlb = ByteBuffer.allocateDirect(imageCoords.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);

		int vertexShader = PaintRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				VERTEX_SHADER_CODE);
		int fragmentShader = PaintRenderer.loadShader(
				GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);

		shaderProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(shaderProgram, vertexShader);
		GLES20.glAttachShader(shaderProgram, fragmentShader);

		// Texture Code
		GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate");

		GLES20.glLinkProgram(shaderProgram);

		createTexture();
		textureDataHandle = loadTexture(activityContext, image);

		surfaceView = glSurfaceView;

		this.width = image.getWidth();
		this.height = image.getHeight();

		this.fullWidth = fwidth;
		this.fullHeight = fheight;
	}

	public void draw(float[] MVPMatrix)
	{
		// Positioning
		vertexBuffer.put(imageCoords);
		vertexBuffer.position(0);

		// Texturing
		final float[] imageTextureCoordinateData = { 0f, 0f, 0f, 1f, 1f, 1f,
				1f, 0f };

		imageTextureCoordinates.put(imageTextureCoordinateData).position(0);

		// Shader Program
		GLES20.glUseProgram(shaderProgram);

		positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

		colorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");

		GLES20.glUniform4fv(colorHandle, 1, color, 0);

		textureUniformHandle = GLES20.glGetAttribLocation(shaderProgram,
				"u_Texture");
		textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram,
				"a_TexCoordinate");

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);
		GLES20.glUniform1i(textureUniformHandle, 0);

		GLES20.glVertexAttribPointer(textureCoordinateHandle,
				textureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
				imageTextureCoordinates);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);

		MVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram,
				"uMVPMatrix");

		GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		GLES20.glDisableVertexAttribArray(positionHandle);
	}

	public static void createTexture()
	{
		GLES20.glGenTextures(1, textureHandle, 0);

		// Bind to the texture in OpenGL
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

		// Set filtering
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public static void deleteTexture()
	{
		GLES20.glDeleteTextures(1, textureHandle, 0);
	}

	public static int loadTexture(final Context context, Bitmap image)
	{
		if (textureHandle[0] != 0)
		{
			Log.i("Load Texture ", "Bitmap w: " + image.getWidth() + " h: "
					+ image.getHeight() + " " + textureHandle[0]);

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

			GLES20.glFlush();

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			image.recycle();
		}

		if (textureHandle[0] == 0)
		{
			Log.e("GL ERROR: ", "ERR " + GLES20.glGetError());

			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	public void updateCoords()
	{
		// Texture and position scaling

		final float[] psData = surfaceView.getPSInfo();

		// -0.5f, 0.5f // top left
		// -0.5f, -0.5f // bottom left
		// 0.5f, -0.5f // bottom right
		// 0.5f, 0.5f // top right

		float dx = psData[2], dy = psData[3];

		if (((PaintActivity) context).getCurrentTool() != PaintTool.PAN_ZOOM)
		{
			dx = psData[5];
			dy = psData[6];
		}

		final int VIEWPORT_HW = (int) ((1.0f / psData[4])
				* surfaceView.getWidth() / 2), VIEWPORT_HH = (int) ((1.0f / psData[4])
				* surfaceView.getHeight() / 2);

		imageCoords[0] = Math.max(0, (float) fullWidth / 2 + dx - VIEWPORT_HW);
		imageCoords[1] = Math.min(fullHeight, (float) fullHeight / 2 + dy
				+ VIEWPORT_HH);

		imageCoords[2] = Math.max(0, (float) fullWidth / 2 + dx - VIEWPORT_HW);
		imageCoords[3] = Math.max(0, (float) fullHeight / 2 + dy - VIEWPORT_HH);

		imageCoords[4] = Math.min(fullWidth, (float) fullWidth / 2 + dx
				+ VIEWPORT_HW);
		imageCoords[5] = Math.max(0, (float) fullHeight / 2 + dy - VIEWPORT_HH);

		imageCoords[6] = Math.min(fullWidth, (float) fullWidth / 2 + dx
				+ VIEWPORT_HW);
		imageCoords[7] = Math.min(fullHeight, (float) fullHeight / 2 + dy
				+ VIEWPORT_HH);
	}

	public Bitmap getImage()
	{
		return image;
	}

	public float getWidth()
	{
		return width;
	}

	public void setWidth(float width)
	{
		this.width = width;
	}

	public float getHeight()
	{
		return height;
	}

	public void setHeight(float height)
	{
		this.height = height;
	}

	public int getFullWidth()
	{
		return fullWidth;
	}

	public void setFullWidth(int fullWidth)
	{
		this.fullWidth = fullWidth;
	}

	public int getFullHeight()
	{
		return fullHeight;
	}

	public void setFullHeight(int fullHeight)
	{
		this.fullHeight = fullHeight;
	}
}
