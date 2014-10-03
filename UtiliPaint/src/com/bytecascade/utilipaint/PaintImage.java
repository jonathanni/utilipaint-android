package com.bytecascade.utilipaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class PaintImage {
	private final Context activityContext;

	private final FloatBuffer imageTextureCoordinates;
	private int textureUniformHandle;
	private int textureCoordinateHandle;
	private final int textureCoordinateDataSize = 2;
	private int textureDataHandle;

	private final String vertexShaderCode =
	// Test
	"attribute vec2 a_TexCoordinate;" + "varying vec2 v_TexCoordinate;"
			+
			// End Test
			"uniform mat4 uMVPMatrix;" + "attribute vec4 vPosition;"
			+ "void main() {" + "  gl_Position = vPosition * uMVPMatrix;" +
			// Test
			"v_TexCoordinate = a_TexCoordinate;" +
			// End Test
			"}";

	private final String fragmentShaderCode = "precision mediump float;"
			+ "uniform vec4 vColor;"
			+
			// Test
			"uniform sampler2D u_Texture;" + "varying vec2 v_TexCoordinate;"
			+
			// End Test
			"void main() {"
			+
			// "gl_FragColor = vColor;" +
			"gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));"
			+ "}";

	private final int shaderProgram;
	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;
	private int positionHandle;
	private int colorHandle;
	private int MVPMatrixHandle;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 2;
	static float imageCoords[] = { -0.5f, 0.5f, // top left
			-0.5f, -0.5f, // bottom left
			0.5f, -0.5f, // bottom right
			0.5f, 0.5f }; // top right

	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // Order to draw vertices
	private final int vertexStride = COORDS_PER_VERTEX * 4; // Bytes per vertex

	private Bitmap image;

	public PaintImage(final Context activityContext, Bitmap image) {
		this.activityContext = activityContext;

		ByteBuffer bb = ByteBuffer.allocateDirect(imageCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());

		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(imageCoords).position(0);

		final float[] imageTextureCoordinateData = { -0.5f, 0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, 0.5f, 0.5f };

		imageTextureCoordinates = ByteBuffer
				.allocateDirect(imageTextureCoordinateData.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		imageTextureCoordinates.put(imageTextureCoordinateData).position(0);

		ByteBuffer dlb = ByteBuffer.allocateDirect(imageCoords.length * 2)
				.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer().put(drawOrder);
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

		// Load the texture
		if (image != null)
			setImage(image);
	}

	public void draw(float[] MVPMatrix) {

	}

	public static int loadTexture(final Context context, Bitmap image) {
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
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		textureDataHandle = loadTexture(activityContext, image);
	}
}
