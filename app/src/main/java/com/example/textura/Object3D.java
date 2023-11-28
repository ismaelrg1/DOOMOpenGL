package com.example.textura;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Object3D {

	// Color enabled or not
	boolean colorEnabled = false;

	// Texture enabled or not
	boolean textureEnabled = false;

	// Our vertex buffer.
	private FloatBuffer vertexBuffer;

	// Our normal buffer.
	private FloatBuffer normalBuffer;

	// Our index buffer.
	private IntBuffer indexBuffer;

	// Our texture buffer.
	private FloatBuffer texcoordBuffer;

	int[] textures = new int[1];
	int numFaceIndexs = 0;

	public Object3D(Context ctx, int filenameId) {

		try {
			String line;
			String[] tmp,ftmp;

			ArrayList<Float> vlist = new ArrayList<Float>();
			ArrayList<Float> tlist = new ArrayList<Float>();
			ArrayList<Float> nlist = new ArrayList<Float>();
			ArrayList<Integer> vindex = new ArrayList<Integer>();
			ArrayList<Integer> tindex = new ArrayList<Integer>();
			ArrayList<Integer> nindex = new ArrayList<Integer>();

			InputStream is = ctx.getResources().openRawResource(filenameId);
			BufferedReader inb = new BufferedReader(new InputStreamReader(is), 1024);
			while ((line = inb.readLine()) != null) {
				tmp = line.split(" ");
				if (tmp[0].equalsIgnoreCase("v")) {

					for (int i = 1; i < 4; i++) {
						vlist.add( Float.parseFloat(tmp[i]) );
					}

				}
				if (tmp[0].equalsIgnoreCase("vn")) {

					for (int i = 1; i < 4; i++) {
						nlist.add( Float.parseFloat(tmp[i]) );
					}

				}
				if (tmp[0].equalsIgnoreCase("vt")) {
					for (int i = 1; i < 3; i++) {
						tlist.add( Float.parseFloat(tmp[i]) );
					}

				}
				if (tmp[0].equalsIgnoreCase("f")) {
					for (int i = 1; i < 4; i++) {
						ftmp = tmp[i].split("/");

						vindex.add(Integer.parseInt(ftmp[0]) - 1);
						if (tlist.size()>0)
							tindex.add(Integer.parseInt(ftmp[1]) - 1);
						if (nlist.size()>0)
							nindex.add(Integer.parseInt(ftmp[2]) - 1);

						numFaceIndexs++;
					}
				}
			}

			ByteBuffer vbb = ByteBuffer.allocateDirect(vindex.size() * 4 * 3);
			vbb.order(ByteOrder.nativeOrder());
			vertexBuffer = vbb.asFloatBuffer();

			for (int j = 0; j < vindex.size(); j++) {
				vertexBuffer.put(vlist.get( vindex.get(j)*3 ));
				vertexBuffer.put(vlist.get( vindex.get(j)*3+1 ));
				vertexBuffer.put(vlist.get( vindex.get(j)*3+2 ));
			}
			vertexBuffer.position(0);


			if (tindex.size()>0)  {
				ByteBuffer vtbb = ByteBuffer.allocateDirect(tindex.size() * 4 * 2);
				vtbb.order(ByteOrder.nativeOrder());
				texcoordBuffer = vtbb.asFloatBuffer();

				for (int j = 0; j < tindex.size(); j++) {
					texcoordBuffer.put(tlist.get( tindex.get(j)*2 ));
					texcoordBuffer.put(tlist.get( tindex.get(j)*2+1 ));
				}
				texcoordBuffer.position(0);
			}

			if(nindex.size()>0) {
				ByteBuffer nbb = ByteBuffer.allocateDirect(nindex.size() * 4 * 3);
				nbb.order(ByteOrder.nativeOrder());
				normalBuffer = nbb.asFloatBuffer();

				for (int j = 0; j < nindex.size(); j++) {
					normalBuffer.put(nlist.get( nindex.get(j)*3 ));
					normalBuffer.put(nlist.get( nindex.get(j)*3+1 ));
					normalBuffer.put(nlist.get( nindex.get(j)*3+2 ));
				}
				normalBuffer.position(0);
			}

			ByteBuffer ibb = ByteBuffer.allocateDirect(numFaceIndexs * 4);
			ibb.order(ByteOrder.nativeOrder());
			indexBuffer = ibb.asIntBuffer();

			for (int j = 0; j < numFaceIndexs; j++) {
				indexBuffer.put(j);
			}
			indexBuffer.position(0);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void draw(GL10 gl) {
		// Enabled the vertices buffer for writing and to be used during
		// rendering.
		gl.glColor4f(1,1,1,1);
		gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
		gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
		gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		if(textureEnabled) {
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glEnable(GL10.GL_TEXTURE_2D);  // Enable texture
		}

		//////////////////////// NEW ////////////////////////////////
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		//////////////////////// NEW ////////////////////////////////

		// Specifies the location and data format of an array of vertex
		// coordinates to use when rendering.
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		//////////////////////// NEW ////////////////////////////////
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		//////////////////////// NEW ////////////////////////////////

		if(textureEnabled) {
			gl.glTexCoordPointer(2, GL10.GL_FLOAT,0,texcoordBuffer);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		}

		gl.glDrawElements(GL10.GL_TRIANGLES, numFaceIndexs, GLES20.GL_UNSIGNED_INT, indexBuffer);

		// Disable the vertices buffer.
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		if(textureEnabled){
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}

		//////////////////////// NEW ////////////////////////////////
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		//////////////////////// NEW ////////////////////////////////
	}

	// Load an image into GL texture
	public void loadTexture(GL10 gl, Context context, int textureResourceId) {
		gl.glGenTextures(1, textures, 0); // Generate texture-ID array

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);   // Bind to texture ID
		// Set up texture filters
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Construct an input stream to texture image "res\drawable\nehe.png"
		InputStream istream = context.getResources().openRawResource(textureResourceId);

		Bitmap bitmap;
		try {
			// Read and decode input as bitmap
			bitmap = BitmapFactory.decodeStream(istream);
		} finally {
			try {
				istream.close();
			} catch(IOException e) { }
		}

		// Build Texture from loaded bitmap for the currently-bind texture ID
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
		textureEnabled=true;
	}
}
