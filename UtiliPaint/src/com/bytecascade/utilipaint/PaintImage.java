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

public class PaintImage {
	private final Context activityContext;

	private final FloatBuffer imageTextureCoordinates;
	private int textureUniformHandle;
	private int textureCoordinateHandle;
	private final int textureCoordinateDataSize = 2;
	private int textureDataHandle;

	private final String vertexShaderCode =

	"attribute vec2 a_TexCoordinate;" + "varying vec2 v_TexCoordinate;"
			+ "uniform mat4 uMVPMatrix;" + "attribute vec4 vPosition;"
			+ "void main() {" + "  gl_Position = vPosition * uMVPMatrix;"
			+ "v_TexCoordinate = a_TexCoordinate;" + "}";

	private final String fragmentShaderCode = "precision mediump float;"
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
	static float imageCoords[] = { -1, 1, // top left
			-1, -1, // bottom left
			1, -1, // bottom right
			1, 1 }; // top right

	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // Order to draw vertices
	private final int vertexStride = COORDS_PER_VERTEX * 4; // Bytes per vertex

	private float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

	private Bitmap image;
	
	private float translateX, translateY;

	public PaintImage(final Context activityContext, Bitmap image) {
		this.activityContext = activityContext;

		ByteBuffer bb = ByteBuffer.allocateDirect(imageCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());

		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(imageCoords);
		vertexBuffer.position(0);

		final float[] imageTextureCoordinateData = { 0f, 0f, 0f, 1f,
				1f, 1f, 1f, 0f };

		imageTextureCoordinates = ByteBuffer
				.allocateDirect(imageTextureCoordinateData.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		imageTextureCoordinates.put(imageTextureCoordinateData).position(0);

		ByteBuffer dlb = ByteBuffer.allocateDirect(imageCoords.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);

		int vertexShader = PaintRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = PaintRenderer.loadShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		shaderProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(shaderProgram, vertexShader);
		GLES20.glAttachShader(shaderProgram, fragmentShader);

		// Texture Code
		GLES20.glBindAttribLocation(shaderProgram, 0, "a_TexCoordinate");

		GLES20.glLinkProgram(shaderProgram);

		textureDataHandle = loadTexture(activityContext, image);
	}

	public void draw(float[] MVPMatrix) {
		// Update texture scaling
		
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

		imageTextureCoordinates.position(0);
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

	public static int loadTexture(final Context context, Bitmap image) {
		Log.i("Load Texture ",
				"Bitmap w: " + image.getWidth() + " h: " + image.getHeight());

		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
			
			// Recycle the bitmap, since its data has been loaded into OpenGL.
			image.recycle();
		}

		if (textureHandle[0] == 0) {
			Log.e("GL ERROR: ", "ERR " + GLES20.glGetError());

			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	public Bitmap getImage() {
		return image;
	}

	public float getTranslateX() {
		return translateX;
	}

	public void setTranslateX(float translateX) {
		this.translateX = translateX;
	}

	public float getTranslateY() {
		return translateY;
	}

	public void setTranslateY(float translateY) {
		this.translateY = translateY;
	}
}
