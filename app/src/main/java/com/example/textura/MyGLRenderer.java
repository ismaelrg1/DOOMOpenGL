package com.example.textura;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

/**
 * OpenGL Custom renderer used with GLSurfaceView
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {


    private float width, height;

    Context context;   // Application's context

    Hud hud;
    FaceHud face;
    boolean otherCamera=false;
    boolean hardmode=false;
    MediaPlayer doomMusic;
    float[] fogColor = {0.8f, 0.8f, 0.8f, 1f};


    private Light light;
    Object3D[] object3D;
    public float zCam=0;
    public float xCamPos=0;
    public float yCamPos=1;
    public float zCamPos=0;
    public float xCamFront=0;
    public float yCamFront=0;
    public float zCamFront=0; // Ejemplo de posición inicial de la cámara

    private float cameraSpeed = 0.1f;

    public float pitch = 0.0f;
    public float yaw = 0.0f;

    private static boolean isJumping = false;
    private static float jumpHeight = 1.0f;
    private static float jumpVelocity = 0.0f;
    private static float gravity = 0.005f;

    long lastTime = 0, currentTime;


    // Constructor with global application context
    public MyGLRenderer(Context context) {
        this.object3D = new Object3D[30];
        this.object3D[0] = new Object3D(context, R.raw.suelo);
        this.object3D[1] = new Object3D(context, R.raw.salaprincipal);
        this.object3D[2] = new Object3D(context, R.raw.salafinal);
        this.object3D[3] = new Object3D(context, R.raw.corazon);
        this.object3D[4] = new Object3D(context, R.raw.door0);
        this.object3D[5] = new Object3D(context, R.raw.door1);
        this.object3D[6] = new Object3D(context, R.raw.door2);
        this.object3D[7] = new Object3D(context, R.raw.door3);
        this.object3D[8] = new Object3D(context, R.raw.door4);
        this.object3D[9] = new Object3D(context, R.raw.door5);
        this.object3D[10] = new Object3D(context, R.raw.door6);
        this.object3D[11] = new Object3D(context, R.raw.bird);
        this.object3D[12] = new Object3D(context, R.raw.mountain);
        this.object3D[13] = new Object3D(context, R.raw.banana);
        this.object3D[14] = new Object3D(context, R.raw.pressn);
        this.object3D[15] = new Object3D(context, R.raw.sky);
        this.object3D[16] = new Object3D(context, R.raw.roof);
        doomMusic = MediaPlayer.create(context, R.raw.doomtheme);

        this.context = context;
    }

    // Call back when the surface is first created or re-created
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        light = new Light(gl, GL10.GL_LIGHT0);
        light.setPosition(new float[]{0.0f, 0f, 1, 0.0f});

        light.setAmbientColor(new float[]{0.1f, 0.1f, 0.1f});
        light.setDiffuseColor(new float[]{1, 1, 1});

        this.object3D[0].loadTexture(gl,context, R.raw.floortexture);
        this.object3D[1].loadTexture(gl,context, R.raw.bluewall);
        this.object3D[2].loadTexture(gl,context, R.raw.graywall);
        this.object3D[3].loadTexture(gl,context, R.raw.ventricalsbase);
        for(int i=4; i<11; i++) {
            this.object3D[i].loadTexture(gl, context, R.raw.doordoom);
        }
        this.object3D[11].loadTexture(gl,context, R.raw.texturebird);
        this.object3D[12].loadTexture(gl,context, R.raw.grass);
        this.object3D[13].loadTexture(gl,context, R.raw.bananatextu);
        this.object3D[14].loadTexture(gl,context, R.raw.texturepressn);
        this.object3D[15].loadTexture(gl,context, R.raw.skytexture);
        this.object3D[16].loadTexture(gl,context, R.raw.woodfloor);

        this.hud = new Hud();
        this.face = new FaceHud();

        doomMusic.setLooping(true);
        doomMusic.start();

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

    int ticks=0;
    float posDoor=0;
    boolean door_down=true;
    // Call back to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d("CameraPosition", "X: " + xCamPos + ", Y: " + yCamPos + ", Z: " + zCamPos);
        // Clear color and depth buffers using clear-value set earlie

        setPerspectiveProjection(gl);

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        if(otherCamera) {
            GLU.gluLookAt(gl, -8, 10, 34, -5, 20, 80, 0f, 1f, 0f);

        }else {

            updateCameraVectors();

            GLU.gluLookAt(gl, xCamPos, yCamPos, zCamPos, xCamPos + xCamFront, yCamPos + yCamFront, zCamPos + zCamFront, 0f, 1f, 0f);
        }

        if((hardmode || zCamPos < -7.5) && !otherCamera){
            gl.glEnable(GL10.GL_FOG);
            gl.glFogf(GL10.GL_FOG_MODE, GL10.GL_LINEAR);
            gl.glFogfv(GL10.GL_FOG_COLOR, fogColor, 0);
            gl.glFogf(GL10.GL_FOG_START, 2.0f);  // Ajusta la distancia inicial de la niebla
            gl.glFogf(GL10.GL_FOG_END, 10.0f);
        }else{
            gl.glDisable(GL10.GL_FOG);
        }


        for (int i = 0; i<4; i++){
            gl.glPushMatrix();// Reset model-view matrix ( NEW
            object3D[i].draw(gl);                // Draw triangle ( NEW )
            gl.glPopMatrix();
        }


        for(int i = 4; i<10; i++) {
            gl.glPushMatrix();
            if (ticks == 2) {
                if (door_down) {
                    posDoor += 0.01;
                } else {
                    posDoor -= 0.01;
                }
                if (posDoor < -2 || posDoor > 0) door_down = !door_down;
                ticks = 0;
            }
            gl.glTranslatef(0, posDoor, 0);
            object3D[i].draw(gl);                // Draw triangle ( NEW )
            gl.glPopMatrix();
        }
        ticks++;

        gl.glPushMatrix();
        object3D[10].draw(gl);
        gl.glPopMatrix();

        for(int i=11; i<17; i++){
            gl.glPushMatrix();
            object3D[i].draw(gl);
            gl.glPopMatrix();
        }

        gl.glDisable(GL10.GL_FOG);
///////////////////////////////////////////////////////////////////////
        //gHUD, Cara, Menu
        if(otherCamera) return;
        setOrthographicProjection(gl);
        hud.loadTexture(gl, context);

        gl.glPushMatrix();
        gl.glScalef(5.0f, 1,1);
        gl.glTranslatef(0.0f, -3.0f, 0.0f);
        hud.draw(gl);
        gl.glPopMatrix();

        face.loadTexture(gl, context);
        gl.glPushMatrix();
        gl.glScalef(0.53f,0.99f,1f);
        gl.glTranslatef(0.0f, -3.1f, 0.0f);
        face.draw(gl);
        gl.glPopMatrix();

        Log.d("CameraLock", "X: " + xCamFront + ", Y: " + yCamFront + ", Z: " + zCamFront);
        Log.d("HudPosition", "X: " + xCamPos+xCamFront + ", Y: " + yCamPos+yCamFront + ", Z: " + zCamPos+zCamFront);

       /* currentTime = System.nanoTime();
        System.out.println((currentTime - lastTime) / 1000000);
        lastTime = currentTime;*/

    }

    private void setPerspectiveProjection(GL10 gl) {

        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Set color's clear-value to black
        gl.glClearDepthf(1.0f);            // Set depth's clear-value to farthest
        gl.glEnable(GL10.GL_DEPTH_TEST);   // Enables depth-buffer for hidden surface removal
        gl.glDepthFunc(GL10.GL_LEQUAL);    // The type of depth testing to do
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
        gl.glShadeModel(GL10.GL_SMOOTH);   // Enable smooth shading of color
        gl.glDisable(GL10.GL_DITHER);      // Disable dithering for better performance
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
        gl.glShadeModel(GL10.GL_SMOOTH);   // Enable smooth shading of color
        gl.glDisable(GL10.GL_DITHER);      // Disable dithering for better performance

        gl.glEnable(GL10.GL_LIGHTING);

        // Enable Normalize
        gl.glEnable(GL10.GL_NORMALIZE);

        gl.glClearDepthf(1.0f);            // Set depth's clear-value to farthest
        gl.glEnable(GL10.GL_DEPTH_TEST);   // Enables depth-buffer for hidden surface removal
        gl.glDepthFunc(GL10.GL_LEQUAL);    // The type of depth testing to do
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
        gl.glShadeModel(GL10.GL_SMOOTH);   // Enable smooth shading of color
        gl.glDisable(GL10.GL_DITHER);      // Disable dithering for better performance
        gl.glDepthMask(true);  // disable writes to Z-Buffer

        gl.glMatrixMode(GL10.GL_PROJECTION); // Select projection matrix
        gl.glLoadIdentity();                 // Reset projection matrix

        // Use perspective projection
        GLU.gluPerspective(gl, 60, (float) width / height, 0.1f, 100.f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
        gl.glLoadIdentity();
    }

    private void setOrthographicProjection(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-5,5,-4,4,-5,5);
        gl.glDepthMask(false);  // disable writes to Z-Buffer
        gl.glDisable(GL10.GL_DEPTH_TEST);  // disable depth-testing

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
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
        if(otherCamera) return;
        xCamPos += cameraSpeed * xCamFront;
        zCamPos += cameraSpeed * zCamFront;
    }

    public void movePOVBackward() {
        if(otherCamera) return;
        xCamPos -= cameraSpeed * xCamFront;
        zCamPos -= cameraSpeed * zCamFront;
    }

    public void movePOVLeft() {
        if(otherCamera) return;
        float strafeX = (float)Math.cos(Math.toRadians(yaw - 90.0f));
        float strafeZ = (float)Math.sin(Math.toRadians(yaw - 90.0f));

        xCamPos += strafeX * cameraSpeed;
        zCamPos += strafeZ * cameraSpeed;
    }

    public void movePOVRight() {// Calcula la dirección perpendicular a la vista de la cámara
        if(otherCamera) return;
        float strafeX = (float)Math.cos(Math.toRadians(yaw - 90.0f));
        float strafeZ = (float)Math.sin(Math.toRadians(yaw - 90.0f));

        xCamPos -= strafeX * cameraSpeed;
        zCamPos -= strafeZ * cameraSpeed;
    }

    public void moveCameraUp() {
        if(otherCamera) return;
        pitch += 1.0f;
        if (pitch > 89.0f) pitch = 89.0f;
    }

    public void moveCameraDown() {
        if(otherCamera) return;
        pitch -= 1.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }

    public void moveCameraLeft() {
        if(otherCamera) return;
        yaw -= 1.0f;
    }

    public void moveCameraRight() {
        if(otherCamera) return;
        yaw += 1.0f;
    }

    public void jump() {
        if(otherCamera) return;
        if (!isJumping){
            isJumping = true;
            jumpVelocity = 0.1f;
        }
    }

    private void updateCameraVectors() {
        if(otherCamera) return;
        float newFrontx = (float) (cos(Math.toRadians(yaw)) * cos(Math.toRadians(pitch)));
        float newFronty = (float) sin(Math.toRadians(pitch));
        float newFrontz = (float) (sin(Math.toRadians(yaw)) * cos(Math.toRadians(pitch)));

        float length = (float) sqrt(newFrontx * newFrontx + newFronty * newFronty + newFrontz * newFrontz);

        // Normalizar manualmente dividiendo cada componente por la longitud
        xCamFront = newFrontx / length;
        yCamFront = newFronty / length;
        zCamFront = newFrontz / length;


        yCamPos += jumpVelocity;
        if (isJumping) {
            jumpVelocity -= gravity; // Aplicar gravedad durante el salto

            // Verificar si ha alcanzado el suelo
            if (yCamPos <= 1.0f) {
                yCamPos = 1.0f;
                isJumping = false;
                jumpVelocity = 0.0f;
            }
        }
    }

    public void camera() {
        if(otherCamera){
            otherCamera=false;
        }else{
            otherCamera=true;
        }

    }

    public void hardmode() {
        hardmode = !hardmode;
    }
}