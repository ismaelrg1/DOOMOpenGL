package com.example.textura;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Arrays;


public class MainActivity extends Activity{

    private static final int PERMISSION_REQUEST_CODE = 1;

    private GLSurfaceView glView;   // Use GLSurfaceView
    private MyGLRenderer myGLRenderer;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 720;
    private float previousX;
    private float previousY;


    // Call back when the activity is started, to initialize the view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glView = new GLSurfaceView(this);           // Allocate a GLSurfaceView
        glView.setRenderer(myGLRenderer=new MyGLRenderer(this)); // Use a custom renderer
        this.setContentView(glView);                // This activity sets to GLSurfaceView

    }

    // Call back when the activity is going into the background
    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
    }

    // Call back after onPause()
    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                System.out.println("toca");

                float dx = x - previousX;
                float dy = y - previousY;

                // reverse direction of rotation above the mid-line
                if (y >  myGLRenderer.getHeight() / 2) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < myGLRenderer.getWidth() / 2) {
                    dy = dy * -1;
                }

                myGLRenderer.setzCam(myGLRenderer.getzCam()+(dx + dy) * TOUCH_SCALE_FACTOR);
        }

        previousX = x;
        previousY = y;
        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        switch (keyCode) {
            case KeyEvent.KEYCODE_W:
                // Acciones cuando se presiona la tecla de volumen hacia arriba
                Log.d("Tecla","Tecla delante");
                myGLRenderer.movePOVForward();
                return true;
            case KeyEvent.KEYCODE_S:
                // Acciones cuando se presiona la tecla de volumen hacia abajo
                myGLRenderer.movePOVBackward();
                return true;
            case KeyEvent.KEYCODE_A:
                // Acciones cuando se presiona la tecla de volumen hacia abajo

                myGLRenderer.movePOVLeft();
                return true;
            case KeyEvent.KEYCODE_D:
                // Acciones cuando se presiona la tecla de volumen hacia abajo
                myGLRenderer.movePOVRight();
                return true;
            // Puedes agregar más casos para otras teclas según sea necesario
            case KeyEvent.KEYCODE_DPAD_UP:
                myGLRenderer.moveCameraUp();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                myGLRenderer.moveCameraDown();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                myGLRenderer.moveCameraLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                myGLRenderer.moveCameraRight();
                return true;
            case KeyEvent.KEYCODE_SPACE:
                myGLRenderer.jump();
                return true;
            case KeyEvent.KEYCODE_N:
                myGLRenderer.camera();
                return true;
            case KeyEvent.KEYCODE_B:
                myGLRenderer.hardmode();
                return true;
        }

        return super.onKeyDown(keyCode, event);

    }
}