package com.bytecascade.utilipaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class PaintSelectionRect {

	private final String vertexShaderCode =
	// This matrix member variable provides a hook to manipulate
	// the coordinates of the objects that use this vertex shader
	"uniform mat4 uMVPMatrix;" +

	"attribute vec4 vPosition;" + "void main() {" +
	// the matrix must be included as a modifier of gl_Position
			"  gl_Position = uMVPMatrix * vPosition;" + "}";

	private final String fragmentShaderCode = "precision mediump float;"
			+ "uniform vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 2;
	static float LineCoords[] = { 0.0f, 0.0f, 1.0f, 0.0f };

	protected int glProgram;
	protected int positionHandle;
	protected int colorHandle;
	protected int MVPMatrixHandle;

	private final int vertexCount = LineCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per
															// vertex

	// Set color with red, green, blue and alpha (opacity) values
	float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

	private float vertices[];

	private FloatBuffer vertexBuffer;
	private ShortBuffer indexBuffer;

	public PaintSelectionRect() {
		int vertexShader = PaintRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = PaintRenderer.loadShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		glProgram = GLES20.glCreateProgram(); // create empty OpenGL ES Program
		GLES20.glAttachShader(glProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(glProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(glProgram); // creates OpenGL ES program
											// executables

	}

	public void draw(float[] MVPMatrix) {
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
		// (number of coordinate values * 4 bytes per float)
				LineCoords.length * 4);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());

		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(LineCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);
		
		   // Add program to OpenGL ES environment
	    GLES20.glUseProgram(glProgram);

	    // get handle to vertex shader's vPosition member
	    positionHandle = GLES20.glGetAttribLocation(glProgram, "vPosition");

	    // Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(positionHandle);

	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
	                                 GLES20.GL_FLOAT, false,
	                                 vertexStride, vertexBuffer);

	    // get handle to fragment shader's vColor member
	    colorHandle = GLES20.glGetUniformLocation(glProgram, "vColor");

	    // Set color for drawing the triangle
	    GLES20.glUniform4fv(colorHandle, 1, color, 0);

	    // get handle to shape's transformation matrix
	    MVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uMVPMatrix");

	    // Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);


	    // Draw the triangle
	    GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(positionHandle);
	}

}