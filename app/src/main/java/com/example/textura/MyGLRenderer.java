package com.example.textura;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import android.view.MotionEvent;

import java.io.Console;

/**
 * OpenGL Custom renderer used with GLSurfaceView
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {


    private float width, height;


    Context context;   // Application's context
    Triangle triangle;

    Wall[] walls = new Wall[4];
    float[] angular = new float[3];


    TextureCube textureCube;
    float angle = 0;
    public float zCam=0;

    public float xCamPos=0;
    public float yCamPos=0;
    public float zCamPos=3;
    public float xCamFront=0;
    public float yCamFront=0;
    public float zCamFront=-1; // Ejemplo de posición inicial de la cámara

    private float cameraSpeed = 0.5f;

    public float pitch = 0.0f;
    public float yaw = -90.0f;

    long lastTime = 0, currentTime;


    // Constructor with global application context
    public MyGLRenderer(Context context) {
        this.context = context;
        this.triangle = new Triangle();
        this.textureCube = new TextureCube();
        for (int i = 0; i<4; i++){
            this.walls[i] = new Wall();
        }
    }

    // Call back when the surface is first created or re-created
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Set color's clear-value to black
        gl.glClearDepthf(1.0f);            // Set depth's clear-value to farthest
        gl.glEnable(GL10.GL_DEPTH_TEST);   // Enables depth-buffer for hidden surface removal
        gl.glDepthFunc(GL10.GL_LEQUAL);    // The type of depth testing to do
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
        gl.glShadeModel(GL10.GL_SMOOTH);   // Enable smooth shading of color
        gl.glDisable(GL10.GL_DITHER);      // Disable dithering for better performance

        // You OpenGL|ES initialization code here
        // ......
        textureCube.loadTexture(gl, context);    // Load image into Texture (NEW)
        walls[0].loadTexture(gl, context);
        walls[1].loadTexture(gl, context);
        walls[2].loadTexture(gl, context);

    }

    // Call back after onSurfaceCreated() or whenever the window's size changes
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) height = 1;   // To prevent divide by zero
        float aspect = (float) width / height;

        this.width = width;
        this.height = height;

        // Set the viewport (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL10.GL_PROJECTION); // Select projection matrix
        gl.glLoadIdentity();                 // Reset projection matrix
        // Use perspective projection
        GLU.gluPerspective(gl, 60, aspect, 0.1f, 100.f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
        gl.glLoadIdentity();                 // Reset
    }

    // Call back to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d("CameraPosition", "X: " + xCamPos + ", Y: " + yCamPos + ", Z: " + zCamPos);
        // Clear color and depth buffers using clear-value set earlie

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();


        GLU.gluLookAt(gl, xCamPos, yCamPos, zCamPos, xCamPos+xCamFront, yCamPos+yCamFront, zCamPos+zCamFront, 0f, 1f, 0f);


        gl.glPushMatrix();
        gl.glTranslatef(0.0f, 0.0f, 10.0f);
        gl.glRotatef(180, 0, 1,0);
        gl.glTranslatef(0.0f, 0.0f, 0.0f);
        walls[0].draw(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-1.0f, 0.0f, 9.0f);
        gl.glRotatef(90, 0, 1,0);
        gl.glTranslatef(0.0f, 0.0f, 0.0f);
        walls[1].draw(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(1.0f, 0.0f, 9.0f);
        gl.glRotatef(-90, 0, 1,0);
        gl.glTranslatef(0.0f, 0.0f, 0.0f);
        walls[3].draw(gl);
        gl.glPopMatrix();



       /* currentTime = System.nanoTime();
        System.out.println((currentTime - lastTime) / 1000000);
        lastTime = currentTime;*/

    }

    public float getHeight() {
        return this.height;
    }

    public float getWidth() {
        return this.width;
    }

    public float getzCam() {
        return 0;
    }

    public void setzCam(float zCam) {
        this.zCam = zCam;
    }


    public void movePOVForward() {
        xCamPos += cameraSpeed * xCamFront;
        zCamPos += cameraSpeed * zCamFront;
    }

    public void movePOVBackward() {
        xCamPos -= cameraSpeed * xCamFront;
        zCamPos -= cameraSpeed * zCamFront;
    }

    public void movePOVLeft() {
        float strafeX = (float)Math.cos(Math.toRadians(yaw - 90.0f));
        float strafeZ = (float)Math.sin(Math.toRadians(yaw - 90.0f));

        xCamPos -= strafeX * cameraSpeed;
        zCamPos -= strafeZ * cameraSpeed;
    }

    public void movePOVRight() {// Calcula la dirección perpendicular a la vista de la cámara
        float strafeX = (float)Math.cos(Math.toRadians(yaw - 90.0f));
        float strafeZ = (float)Math.sin(Math.toRadians(yaw - 90.0f));

        xCamPos += strafeX * cameraSpeed;
        zCamPos += strafeZ * cameraSpeed;
    }

    public void moveCameraUp() {
        pitch += 1.0f;
        if (pitch > 89.0f) pitch = 89.0f;
        updateCameraVectors();
    }

    public void moveCameraDown() {
        pitch -= 1.0f;
        if (pitch < -89.0f) pitch = -89.0f;
        updateCameraVectors();
    }

    public void moveCameraLeft() {
        yaw -= 1.0f;
        updateCameraVectors();
    }

    public void moveCameraRight() {
        yaw += 1.0f;
        updateCameraVectors();
    }

    private void updateCameraVectors() {
        float newFrontx = (float) (cos(Math.toRadians(yaw)) * cos(Math.toRadians(pitch)));
        float newFronty = (float) sin(Math.toRadians(pitch));
        float newFrontz = (float) (sin(Math.toRadians(yaw)) * cos(Math.toRadians(pitch)));

        float length = (float) sqrt(newFrontx * newFrontx + newFronty * newFronty + newFrontz * newFrontz);

        // Normalizar manualmente dividiendo cada componente por la longitud
        xCamFront = newFrontx / length;
        yCamFront = newFronty / length;
        zCamFront = newFrontz / length;
    }


}