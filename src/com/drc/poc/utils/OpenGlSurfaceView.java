/*************************************************************************

  MySurfaceRenderer
  
  	OpenGL display for location tracking
  	
  	MH 3.27.2012
  	
*************************************************************************/

package com.drc.poc.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class OpenGlSurfaceView implements GLSurfaceView.Renderer {
	
	private FloatBuffer shapeBuffer;
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		
		initShapes();
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
	}

	private void initShapes() {	
		
		float[] shapeCoords =
			{
				-0.10f, 0.0f, 0.0f,		//pointer triangle
				 0.0f,  0.35f, 0.0f,
				 0.10f, 0.0f, 0.0f,
				-0.0f, -0.0f, 0.0f,		//home square
				 0.0f, -0.0f, 0.0f,
				 0.0f,  0.0f, 0.0f,
				-0.0f,  0.0f, 0.0f
			};
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(shapeCoords.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		shapeBuffer = vbb.asFloatBuffer();
		shapeBuffer.put(shapeCoords);
		shapeBuffer.position(0);
	}
	
	public void onDrawFrame(GL10 gl) {
		FPoint p = PositionHandler.getCurrentLocation();
		
		//clear the frame
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
        GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        
        //zoom out
        float scaleFactor = (float)Math.min(0.5, 1.0f / p.distance());
        gl.glScalef(scaleFactor, scaleFactor, scaleFactor);
        
        //draw home square
		// gl.glVertexPointer(3, GL10.GL_FLOAT, 0, shapeBuffer);
		// gl.glDrawArrays(GL10.GL_LINE_LOOP, 3, 4);
        
        //draw pointer
        gl.glPushMatrix();
        gl.glTranslatef(p.getX(), p.getY(), 0);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, shapeBuffer);
		gl.glRotatef(PositionHandler.getCurrentAzimuthDegrees()-90, 0, 0, 1);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
		gl.glPopMatrix();
		
		//draw trail
		gl.glPushMatrix();
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, PositionHandler.getCrumbBuffer());
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, PositionHandler.getCrumbBufferSize());
		gl.glPopMatrix();
	}
}
