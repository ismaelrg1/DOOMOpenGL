package com.example.textura;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class FaceHud {
	private FloatBuffer vertexBuffer;  // Buffer for vertex-array
	private ByteBuffer indexBuffer;    // Buffer for index-array
	private FloatBuffer textureBuffer;  // Buffer for texture-coords-array

	private float[] vertices = {  // Vertices of the wall
			-1.0f,  1.0f, 0.0f, // 0. top-left
			-1.0f, -1.0f, 0.0f, // 1. bottom-left
			1.0f, -1.0f, 0.0f,  // 2. bottom-right
			1.0f, 1.0f, 0.0f    // 3. top-right
	};

	private byte[] indices = { 0, 1, 2, 0, 2, 3 }; // Indices to above vertices (in CCW)

	private float[] textureCoords = {
			0.0f, 0.0f, // 0. top-left
			0.0f, 1.0f, // 1. bottom-left
			0.33f, 1.0f, // 2. bottom-right
			0.33f, 0.0f  // 3. top-right
	};

	int[] textureIDs = new int[1];

	// Constructor - Setup the data-array buffers
	public FaceHud() {
		// Setup vertex-array buffer. Vertices in float. A float has 4 bytes.
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder()); // Use native byte order
		vertexBuffer = vbb.asFloatBuffer(); // Convert byte buffer to float
		vertexBuffer.put(vertices);         // Copy data into buffer
		vertexBuffer.position(0);           // Rewind

		// Setup index-array buffer. Indices in byte.
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);

		// Setup texture-coords array buffer
		ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		textureBuffer = tbb.asFloatBuffer();
		textureBuffer.put(textureCoords);
		textureBuffer.position(0);
	}

	int countTicks=0;
	boolean moveLeft=false;
	// Render this shape
	public void draw(GL10 gl) {


		if(countTicks==45){

			if (moveLeft){
				textureCoords[0] = (textureCoords[0]<0.0) ? 0 : (float) (textureCoords[0] - 0.33);
				textureCoords[2] = (textureCoords[2]<0.0) ? 0: (float) (textureCoords[2] - 0.33);
				textureCoords[4] = (textureCoords[4]<0.33) ? 0.33f : (float) (textureCoords[4] - 0.33);
				textureCoords[6] = (textureCoords[6]<0.33) ? 0.33f : (float) (textureCoords[6] - 0.33);
				if (textureCoords[0]<=0) moveLeft=false;
			}else{
				textureCoords[0] = (textureCoords[0]>0.66) ? 0.66f: (float) (textureCoords[0] + 0.33);
				textureCoords[2] = (textureCoords[2]>0.66) ? 0.66f: (float) (textureCoords[2] + 0.33);
				textureCoords[4] = (textureCoords[4]>0.99) ?  0.99f: (float) (textureCoords[4] + 0.33);
				textureCoords[6] = (textureCoords[6]>0.99) ? 0.99f: (float) (textureCoords[6] + 0.33);
				if (textureCoords[0]>=0.66) moveLeft=true;

			}
			Log.d("Face","la cara va ha "+moveLeft);
			ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoords.length * 4);
			tbb.order(ByteOrder.nativeOrder());
			textureBuffer = tbb.asFloatBuffer();
			textureBuffer.put(textureCoords);
			textureBuffer.position(0);

			countTicks=0;
		}
		gl.glColor4f(1,1,1,1);

		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glCullFace(GL10.GL_BACK);
		gl.glEnable(GL10.GL_TEXTURE_2D);


		// Enable vertex-array, texture-coords-array, and define the buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		// Bind the texture
		//gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[0]);

		// Draw the primitives via index-array
		gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);

		// Disable vertex-array and texture-coords-array
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_TEXTURE_2D);

		countTicks++;
	}

	public void loadTexture(GL10 gl, Context context) {
		gl.glGenTextures(1, textureIDs, 0); // Generate texture-ID array

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs[0]);   // Bind to texture ID
		// Set up texture filters
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Construct an input stream to texture image "res\drawable\nehe.png"
		InputStream istream = context.getResources().openRawResource(R.raw.face);

		Bitmap bitmap;
		try {
			// Read and decode input as bitmap
			bitmap = BitmapFactory.decodeStream(istream);
		} finally {
			try {
				istream.close();
			} catch(IOException e) { }
		}

		// Build Texture from loaded bitmap for the currently-bound texture ID
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}
}