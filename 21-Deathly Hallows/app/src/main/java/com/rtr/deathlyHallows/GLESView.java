package com.rtr.deathlyHallows;

// added by me
import android.opengl.GLSurfaceView;              // SurfaceView with support of OpenGL
import android.opengl.GLES32;                     // OpenGL ES 3.2
import javax.microedition.khronos.opengles.GL10;  // OpenGL Extension for basic features of OpenGL ES
import javax.microedition.khronos.egl.EGLConfig;  // Embedded Graphics Library

import android.content.Context;                           // Context
import android.view.MotionEvent;                          // MotionEvent
import android.view.GestureDetector;                      // GestureDetector
import android.view.GestureDetector.OnGestureListener;    // OnGestureListener
import android.view.GestureDetector.OnDoubleTapListener;  // OnDoubleTapListener

import java.nio.ByteBuffer;   // ByteBuffer
import java.nio.ByteOrder;    // ByteOrder
import java.nio.FloatBuffer;  // FloatBuffer

import android.opengl.Matrix; // Matrix


public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener {

    private final Context context;
    private GestureDetector gestureDetector;

    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;

    private int[] vaoCloak = new int[1];
    private int[] vboCloak  = new int[1];

    private int[] vaoStone = new int[1];
    private int[] vboStone = new int[1];

    private int[] vaoWand = new int[1];
    private int[] vboWand = new int[1];
    
    private int mvpUniform;

    private float[] perspectiveProjectionMatrix = new float[16];

    private float xOffsetCloak = 3.50f;
    private float yOffsetCloak = -1.60f;

    private float xOffsetStone = -3.50f;
    private float yOffsetStone = -1.60f;

    private float yOffsetWand = 1.80f;

    private float fAngle = 0.0f;

    private boolean bState1 = true;
    private boolean bState2 = false;
    private boolean bState3 = false;

    private float fRadius = 1.0f;

	public GLESView(Context drawingContext) {
		super(drawingContext);
		context = drawingContext;

        setEGLContextClientVersion(3);   // highest supported 3.x
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); 

        gestureDetector = new GestureDetector(context, this, null, false);
        gestureDetector.setOnDoubleTapListener(this);
	}

    // handling 'onTouchEvent' is the most important
    // because it triggers all gesture and tap events
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // code
        int eventaction = event.getAction();  // not required for now
        if (!gestureDetector.onTouchEvent(event))
            super.onTouchEvent(event);
        return(true);
    }

    // abstract method from OnDoubleTapEventListener so must be implemented
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return(true);
    }

    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // do not write anything here, because already written 'onDoubleTap'
        return(true);
    }

    // abstract method from OnDoubleTapListener so must be implemented
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return(true);
    }

    // abstract method from OnGestureListener so must implement
    @Override
    public boolean onDown(MotionEvent e) {
        // do not write anything here, because already written in 'onSingleTapConfirmed'
        return(true);
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return(true);
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onLongPress(MotionEvent e) {
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        uninitialize();
        System.exit(0);
        return(true);
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public void onShowPress(MotionEvent e) {

    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return(true);
    }

    ///// implementation of GLSurfaceView.Renderer methods
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String version = gl.glGetString(GL10.GL_VERSION);
        System.out.println("RTR: OpenGL Version: " + version);

        // version = gl.glGetString(GL10.GL_SHADING_LANGUAGE_VERSION);
        // System.out.println("RTR: Shading Language Version: " + version);

        String vendor = gl.glGetString(GL10.GL_VENDOR);
        System.out.println("RTR: Vendor: " + vendor);

        String renderer = gl.glGetString(GL10.GL_RENDERER);
        System.out.println("RTR: Renderer: " + renderer);

        initialize();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        resize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        display();
    }

    /////////// Rendering Functions //////////////////////////////////////////////////// 

    private void initialize() {

        //// VERTEX SHADER ////////////////////////////////////////////////
        // create shader object
        vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // shader source code
        final String vertexShaderSourceCode = String.format(
            "#version 320 es" +
            "\n" +
            "in vec4 vPosition;" +
            "in vec4 vColor;" +
            "out vec4 out_Color;"  + 
            "uniform mat4 u_mvp_matrix;" +
            "void main(void)" +
            "{" +
            "   gl_Position = u_mvp_matrix * vPosition;" +
            "   out_Color = vColor;" +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(vertexShaderObject, vertexShaderSourceCode);

        // compile shader source code
        GLES32.glCompileShader(vertexShaderObject);

        // compilation errors
        int[] iShaderCompileStatus = new int[1];
        int[] iInfoLogLength = new int[1];
        String szInfo = null;

        GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(vertexShaderObject);
                System.out.println("RTR: Vertex Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }

        }

        //// FRAGMENT SHADER ////////////////////////////////////////////////
        // create shader object
        fragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

        // shader source code
        final String fragmentShaderSourceCode = String.format(
            "#version 320 es" +
            "\n" +
            "precision highp float;" +
            "in vec4 out_Color;" +
            "out vec4 FragColor;" +
            "void main(void)" +
            "{" +
            "   FragColor = out_Color;" +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(fragmentShaderObject, fragmentShaderSourceCode);

        // compile shader source code
        GLES32.glCompileShader(fragmentShaderObject);

        // compilation errors
        iShaderCompileStatus[0] = 0;
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(fragmentShaderObject);
                System.out.println("RTR: Fragment Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // create shader program object
        shaderProgramObject = GLES32.glCreateProgram();

        // attach vertex shader to shader program
        GLES32.glAttachShader(shaderProgramObject, vertexShaderObject);

        // attach fragment shader to shader program
        GLES32.glAttachShader(shaderProgramObject, fragmentShaderObject);

        // pre-linking binding to vertex attribute
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.AMC_ATTRIBUTE_POSITION, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.AMC_ATTRIBUTE_COLOR, "vColor");

        // link the shader program
        GLES32.glLinkProgram(shaderProgramObject);

        // linking errors
        int[] iProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_LINK_STATUS, iProgramLinkStatus, 0);
        if (iProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfo = GLES32.glGetShaderInfoLog(shaderProgramObject);
                System.out.println("RTR: Program Linking: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // get unifrom locations
        mvpUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_mvp_matrix");

        final float fLength = 0.75f;

        // vertex array
        final float cloakCoords[] = new float[] {
                0.0f,  fLength, 0.0f,
            -fLength, -fLength, 0.0f,
             fLength, -fLength, 0.0f,
        };

        final float stoneCoords[] = generateStoneCoords(fLength);

        final float wandCoords[] = new float[] {
            0.0f,  fLength, 0.0f,
            0.0f, -fLength, 0.0f,
        };

        ////////// Cloak Of Invisibility //////////////////////////////////////////////////////

        // create vao
        GLES32.glGenVertexArrays(1, vaoCloak, 0);
        GLES32.glBindVertexArray(vaoCloak[0]);

        // create vbo
        GLES32.glGenBuffers(1, vboCloak, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboCloak[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer bbVboCloak = ByteBuffer.allocateDirect(cloakCoords.length * 4);

        // 2. Arrange the buffer in native byte order
        bbVboCloak.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer fbVboCloak = bbVboCloak.asFloatBuffer();

        // 4. put data in this COOKED buffer
        fbVboCloak.put(cloakCoords);

        // 5. set the array at 0th position of buffer
        fbVboCloak.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, cloakCoords.length * 4, fbVboCloak, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION); 

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        ////////////////////////////////////////////////////////////////////////////////////////

        //// Resurrection Stone ////////////////////////////////////////////////////////////////

        // create vao
        GLES32.glGenVertexArrays(1, vaoStone, 0);
        GLES32.glBindVertexArray(vaoStone[0]);

        // axes vertices
        GLES32.glGenBuffers(1, vboStone, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboStone[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer bbVboStone = ByteBuffer.allocateDirect(stoneCoords.length * 4);

        // 2. Arrange the buffer in native byte order
        bbVboStone.order(ByteOrder.nativeOrder());

        // 3. Create  float type buffer and convert it to float buffer
        FloatBuffer fbVboStone = bbVboStone.asFloatBuffer();

        // 4. put data in this COOKED buffer
        fbVboStone.put(stoneCoords);

        // 5. set the array at 0th position of buffer
        fbVboStone.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, stoneCoords.length * 4, fbVboStone, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION); 

        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        //// Wand /////////////////////////////////////////////////////////////////////////////////////////////

        GLES32.glGenVertexArrays(1, vaoWand, 0);
        GLES32.glBindVertexArray(vaoWand[0]);

        // shapes vertices
        GLES32.glGenBuffers(1, vboWand, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboWand[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer bbVboWand = ByteBuffer.allocateDirect(wandCoords.length * 4);

        // 2. Arrange the buffer in native byte order
        bbVboWand.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer fbVboWand = bbVboWand.asFloatBuffer();

        // 4. put data in this COOKED buffer
        fbVboWand.put(wandCoords);

        // 5. set the array at 0th position of buffer
        fbVboWand.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, wandCoords.length * 4, fbVboWand, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        GLES32.glVertexAttrib3f(GLESMacros.AMC_ATTRIBUTE_COLOR, 1.0f, 1.0f, 1.0f);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        //////////////////////////////////////////////////////////////////////

        // clear the depth buffer
        GLES32.glClearDepthf(1.0f);

        // clear the screen by OpenGL
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // enable depth
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);
    }

    private void resize(int width, int height) {
        System.out.println("RTR: resize called with: " + width + " " + height);
        if (height == 0)
        {
            height = 1;
        }

        GLES32.glViewport(0, 0, width, height);

        Matrix.perspectiveM(perspectiveProjectionMatrix, 0,
            45.0f,
            (float)width / (float)height,
            0.1f, 100.0f);
    }

    private void display() {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        // use shader program
        GLES32.glUseProgram(shaderProgramObject);

        if(bState1) {
            bState2 = drawCloakOfInvisibility(fAngle);
        } else if (bState2 || bState3) {
            drawCloakOfInvisibility(0.0f);
        }

        if (bState2) {
            bState1 = false;
            bState3 = drawResurrectionStone(fAngle);
        } else if (bState3) {
            drawResurrectionStone(0.0f);
        }

        if (bState3) {
            bState1 = bState2 = false;
            drawElderWand(fAngle);
        }

        // unuse program
        GLES32.glUseProgram(0);
        requestRender();  // ~ swapBuffers

        fAngle += 1.0f;
        if (fAngle >= 360) {
            fAngle = 0.0f;
        }
    }

    private void uninitialize() {
        if (vaoCloak[0] != 0) {
            GLES32.glDeleteBuffers(1, vaoCloak, 0);
            vaoCloak[0] = 0;
        }

        if (vaoStone[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoStone, 0);
            vaoStone[0] = 0;
        }

        if (vaoWand[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoWand, 0);
            vaoWand[0] = 0;
        }

        if (vboCloak[0] != 0) {
            GLES32.glDeleteBuffers(1, vboCloak, 0);
            vboCloak[0] = 0;
        }

        if (vboStone[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vboStone, 0);
            vboStone[0] = 0;
        }

        if (vboWand[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vboWand, 0);
            vboWand[0] = 0;
        }

        if (shaderProgramObject != 0) {
            int[] shaderCount = new int[1];
            int shaderNumber;

            GLES32.glUseProgram(shaderProgramObject);
            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_ATTACHED_SHADERS, shaderCount, 0);

            int[] shaders = new int[shaderCount[0]];

            GLES32.glGetAttachedShaders(shaderProgramObject, shaderCount[0], shaderCount, 0, shaders, 0);
            
            for (shaderNumber = 0; shaderNumber < shaderCount[0]; shaderNumber++) {
                // detach shader
                GLES32.glDetachShader(shaderProgramObject, shaders[shaderNumber]);

                // delete shader
                GLES32.glDeleteShader(shaders[shaderNumber]);
                shaders[shaderNumber] = 0;
            }

            GLES32.glUseProgram(0);
            GLES32.glDeleteProgram(shaderProgramObject);
            shaderProgramObject = 0;
        }
    }

    private float[] generateStoneCoords(float fLength) {
        float[] coords = new float[629 * 3];
        float fAngle = 0.0f;
        float s, a, b, c;
        int idx = 0;

        /* Radius Of Incircle */
        a = (float) Math.sqrt(Math.pow((-fLength - 0.0), 2.0) + Math.pow(-fLength - fLength, 2.0));
        b = (float) Math.sqrt(Math.pow((fLength - (-fLength)), 2.0) + Math.pow(-fLength - (-fLength), 2.0));
        c = (float) Math.sqrt(Math.pow((fLength - 0.0), 2.0) + Math.pow(-fLength - fLength, 2.0));
        s = (a + b + c) / 2.0f;
        fRadius = (float) Math.sqrt(s * (s - a) * (s - b) * (s - c)) / s;

        for (fAngle = 0.0f; fAngle < 2.0f * Math.PI; fAngle += 0.01f)
        {
            coords[idx++] = (float) fRadius * (float) Math.cos(fAngle);
            coords[idx++] = ((float) fRadius * (float) Math.sin(fAngle)) - (fLength + fRadius);
            coords[idx++] = 0.0f;
        }

        return coords;
    }

    private boolean drawCloakOfInvisibility(float fAngle) {

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        Matrix.translateM(translationMatrix, 0,
            xOffsetCloak, yOffsetCloak, -4.5f);

        Matrix.setRotateM(rotationMatrix, 0,
            fAngle, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            rotationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        // send necessary matrices to shader in respective uniforms
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        // Clock
        GLES32.glBindVertexArray(vaoCloak[0]);  

        // draw necessary scene
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, 3);

        GLES32.glBindVertexArray(0);  

        if (xOffsetCloak <= 0.0)
            return true;
        else {
            xOffsetCloak -= (3.50f / (720.0f));
            yOffsetCloak += (1.60f / (720.0f));
        }
        return false;
    }

    private boolean drawResurrectionStone(float gAngle) {

        float fLength = 0.75f;

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        Matrix.translateM(translationMatrix, 0,
            xOffsetStone, yOffsetStone  + (2.0f * fRadius), -4.5f);

        Matrix.setRotateM(rotationMatrix, 0,
            gAngle, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            rotationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        // send necessary matrices to shader in respective uniforms
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        // Clock
        GLES32.glBindVertexArray(vaoStone[0]);  

        // draw necessary scene
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, 629);

        GLES32.glBindVertexArray(0);

        if (xOffsetStone >= 0.0) {
            return true;
        } else {
            xOffsetStone += (3.50f / (720.0f));
            yOffsetStone += (1.60f / (720.0f));
        }
        return false;
    }

    private boolean drawElderWand(float fAngle) {
        
        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        Matrix.translateM(translationMatrix, 0,
            0.0f, yOffsetWand, -4.5f);

        Matrix.setRotateM(rotationMatrix, 0,
            fAngle, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            rotationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        // send necessary matrices to shader in respective uniforms
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        // Clock
        GLES32.glBindVertexArray(vaoWand[0]);

        // draw necessary scene
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, 2);

        GLES32.glBindVertexArray(0);

        if (yOffsetWand <= 0.0) {
            return true;
        }
        else {
            yOffsetWand -= (1.80f / (400.0f));
        }

        return false;
    }
}



