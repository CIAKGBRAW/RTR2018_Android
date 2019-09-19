package com.rtr.graphpaperShapes;

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

    private int[] vao = new int[1];
    private int[] vbo_vertex  = new int[1];
    private int[] vbo_colorSmallAxes = new int[1];

    private int[] vaoAxes = new int[1];
    private int[] vbo_vertexAxes = new int[1];
    private int[] vbo_colorAxes = new int[1];

    private int[] vaoShapes = new int[1];
    private int[] vbo_vertexShapes = new int[1];
    
    private int mvpUniform;

    private float[] perspectiveProjectionMatrix = new float[16];

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

        // vertex array
        float graphCoords[] = generateGraphCoordinates();

        // color array 
        final float axisCoords[] = new float[] {
            -1.0f,  0.0f, 0.0f,
             1.0f,  0.0f, 0.0f,
             0.0f, -1.0f, 0.0f,
             0.0f,  1.0f, 0.0f
        };

        final float axisColors[] = new float[] {
             1.0f,  0.0f, 0.0f,
             1.0f,  0.0f, 0.0f,
             0.0f,  1.0f, 0.0f,
             0.0f,  1.0f, 0.0f
        };

        float smallAxisColors[] = new float[graphCoords.length];
        for (int i = 0; i < smallAxisColors.length; i += 3) {
            smallAxisColors[i+0] = 0.0f;
            smallAxisColors[i+1] = 0.0f;
            smallAxisColors[i+2] = 1.0f;
        }


        float[] shapesCoords = new float[1300 * 3];
        int shapesCoordsCount = 0;
        float fX, fY;

        shapesCoordsCount = generateOuterCircleCoords(shapesCoords, shapesCoordsCount);

        fX = fY = (float)Math.cos(Math.PI / 4.0);
        shapesCoordsCount = generateSquareCoords(fX, fY, shapesCoords, shapesCoordsCount);
        shapesCoordsCount = generateTriangleAndIncircleCoords(fX, fY, shapesCoords, shapesCoordsCount);

        // create vao
        GLES32.glGenVertexArrays(1, vao, 0);
        GLES32.glBindVertexArray(vao[0]);

        // Triangle vertices
        GLES32.glGenBuffers(1, vbo_vertex, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_vertex[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(graphCoords.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBuffer.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBuffer = byteBuffer.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBuffer.put(graphCoords);

        // 5. set the array at 0th position of buffer
        positionBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, graphCoords.length * 4, positionBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        // small axes colors
        GLES32.glGenBuffers(1, vbo_colorSmallAxes, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_colorSmallAxes[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferSmallAxesColor = ByteBuffer.allocateDirect(smallAxisColors.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferSmallAxesColor.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer smallAxesColorBuffer = byteBufferSmallAxesColor.asFloatBuffer();

        // 4. put data in this COOKED buffer
        smallAxesColorBuffer.put(smallAxisColors);

        // 5. set the array at 0th position of buffer
        smallAxesColorBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, smallAxisColors.length * 4, smallAxesColorBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR); 

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        //// axes /////////////////

        // create vao
        GLES32.glGenVertexArrays(1, vaoAxes, 0);
        GLES32.glBindVertexArray(vaoAxes[0]);

        // axes vertices
        GLES32.glGenBuffers(1, vbo_vertexAxes, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_vertexAxes[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferCoords = ByteBuffer.allocateDirect(axisCoords.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferCoords.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferCoords = byteBufferCoords.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferCoords.put(axisCoords);

        // 5. set the array at 0th position of buffer
        positionBufferCoords.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, axisCoords.length * 4, positionBufferCoords, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION); 

        // axes colors
        GLES32.glGenBuffers(1, vbo_colorAxes, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_colorAxes[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferCoordsColor = ByteBuffer.allocateDirect(axisColors.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferCoordsColor.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer colorBufferCoords = byteBufferCoordsColor.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBufferCoords.put(axisColors);

        // 5. set the array at 0th position of buffer
        colorBufferCoords.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, axisColors.length * 4, colorBufferCoords, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR); 

        //// shapes /////////////////////////////////////////////////////////////////////////////////////////////

        GLES32.glGenVertexArrays(1, vaoShapes, 0);
        GLES32.glBindVertexArray(vaoShapes[0]);

        // shapes vertices
        GLES32.glGenBuffers(1, vbo_vertexShapes, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_vertexShapes[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferShapes = ByteBuffer.allocateDirect(shapesCoords.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferShapes.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferShape = byteBufferShapes.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferShape.put(shapesCoords);

        // 5. set the array at 0th position of buffer
        positionBufferShape.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, shapesCoords.length * 4, positionBufferShape, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        GLES32.glVertexAttrib3f(GLESMacros.AMC_ATTRIBUTE_COLOR, 1.0f, 1.0f, 0.0f);

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

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        Matrix.translateM(translationMatrix, 0,
            0.0f, 0.0f, -2.5f);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0,
            perspectiveProjectionMatrix, 0,
            modelViewMatrix, 0);

        // send necessary matrices to shader in respective uniforms
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);


        // bind with vao (this will avoid many binding to vbo)
        GLES32.glBindVertexArray(vao[0]);  

        // draw necessary scene
        GLES32.glLineWidth(1.0f);
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, 160);

        // small axis lines
        GLES32.glBindVertexArray(vaoAxes[0]);

        // draw necessary scene
        GLES32.glLineWidth(3.0f);
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, 4);

        // draw necessary scene
        GLES32.glLineWidth(1.0f);
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, 160);

        // shapes
        GLES32.glBindVertexArray(vaoShapes[0]);

        // draw necessary scene
        GLES32.glLineWidth(2.0f);
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, 629);
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 629, 4);
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 633, 3);
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 636, 629);

        // unbind vao
        GLES32.glBindVertexArray(0);

        // unuse program
        GLES32.glUseProgram(0);
        requestRender();  // ~ swapBuffers
    }

    private void uninitialize() {
        if (vao[0] != 0) {
            GLES32.glDeleteBuffers(1, vao, 0);
            vao[0] = 0;
        }

        if (vaoAxes[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoAxes, 0);
            vaoAxes[0] = 0;
        }

        if (vaoShapes[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoShapes, 0);
            vaoShapes[0] = 0;
        }

        if (vbo_vertex[0] != 0) {
            GLES32.glDeleteBuffers(1, vbo_vertex, 0);
            vbo_vertex[0] = 0;
        }

        if (vbo_vertexAxes[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vbo_vertexAxes, 0);
            vbo_vertexAxes[0] = 0;
        }

        if (vbo_colorAxes[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vbo_colorAxes, 0);
            vbo_colorAxes[0] = 0;
        }

        if (vbo_colorSmallAxes[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vbo_colorSmallAxes, 0);
            vbo_colorSmallAxes[0] = 0;
        }

        if (vbo_vertexShapes[0] != 0) {
            GLES32.glDeleteBuffers(1, vbo_vertexShapes, 0);
            vbo_vertexShapes[0] = 0;
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

    private float[] generateGraphCoordinates() {
        int iNoOfCoords = 0;

        float[] pos = new float[3 * 160];

        for (float fOffset = -1.0f; fOffset <= 0; fOffset += (1.0f / 20.0f))
        {
            pos[(iNoOfCoords * 3) + 0] = -1.0f;
            pos[(iNoOfCoords * 3) + 1] = fOffset;
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;

            pos[(iNoOfCoords * 3) + 0] = 1.0f;
            pos[(iNoOfCoords * 3) + 1] = fOffset;
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;

            pos[(iNoOfCoords * 3) + 0] = -1.0f;
            pos[(iNoOfCoords * 3) + 1] = fOffset + 1.0f + (1.0f / 20.0f);
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;

            pos[(iNoOfCoords * 3) + 0] = 1.0f;
            pos[(iNoOfCoords * 3) + 1] = fOffset + 1.0f + (1.0f / 20.0f);
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;
        }

        for (float fOffset = -1.0f; fOffset <= 0; fOffset += (1.0f / 20.0f))
        {
            pos[(iNoOfCoords * 3) + 0] = fOffset;
            pos[(iNoOfCoords * 3) + 1] = -1.0f;
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;

            pos[(iNoOfCoords * 3) + 0] = fOffset;
            pos[(iNoOfCoords * 3) + 1] = 1.0f;
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;

            pos[(iNoOfCoords * 3) + 0] = fOffset + 1.0f + (1.0f / 20.0f);
            pos[(iNoOfCoords * 3) + 1] = -1.0f;
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;

            pos[(iNoOfCoords * 3) + 0] = fOffset + 1.0f + (1.0f / 20.0f);
            pos[(iNoOfCoords * 3) + 1] = 1.0f;
            pos[(iNoOfCoords * 3) + 2] = 0.0f;
            iNoOfCoords++;      
        }

        return pos;
    }

    private int generateTriangleAndIncircleCoords(float fY, float fX, float[] coords, int idx) {
        // variables 
        float s, a, b, c;
        float fRadius = 1.0f;
        float fAngle = 0.0f;

        // float[] coords = new float[628 * 3];

        /* Triangle */
        coords[idx++] = 0.0f;
        coords[idx++] = fY;
        coords[idx++] = 0.0f;

        coords[idx++] = -fX;
        coords[idx++] = -fY;
        coords[idx++] = 0.0f;

        coords[idx++] = fX;
        coords[idx++] = -fY;
        coords[idx++] = 0.0f;

        /* Radius Of Incircle */
        a = (float)Math.sqrt(Math.pow((-fX - 0.0f), 2.0f) + Math.pow(-fY - fY, 2.0f));
        b = (float)Math.sqrt(Math.pow((fX - (-fX)), 2.0f) + Math.pow(-fY - (-fY), 2.0f));
        c = (float)Math.sqrt(Math.pow((fX - 0.0f), 2.0f) + Math.pow(-fY - fY, 2.0f));
        s = (a + b + c) / 2.0f;
        fRadius = (float)Math.sqrt(s * (s - a) * (s - b) * (s - c)) / s;

        /* Incircle */
        for (fAngle = 0.0f; fAngle < 2 * Math.PI; fAngle += 0.01f)
        {
            coords[idx++] = fRadius * (float)Math.cos(fAngle);
            coords[idx++] = (fRadius * (float)Math.sin(fAngle)) - fX + fRadius;
            coords[idx++] = 0.0f;
        }

        return idx;
    }

    private int generateSquareCoords(float fX, float fY, float[] coords, int idx) {
        
        coords[idx++] = fX;
        coords[idx++] = fY;
        coords[idx++] = 0.0f;

        coords[idx++] = -fX;
        coords[idx++] = fY;
        coords[idx++] = 0.0f;

        coords[idx++] = -fX;
        coords[idx++] = -fY;
        coords[idx++] = 0.0f;

        coords[idx++] = fX;
        coords[idx++] = -fY;
        coords[idx++] = 0.0f;

        return idx;
    }

    private int generateOuterCircleCoords(float[] coords, int idx) {
        float fRadius = 1.0f;

        for (float fAngle = 0.0f; fAngle < 2 * Math.PI; fAngle += 0.01f)
        {
            coords[idx++] = fRadius * (float)Math.cos(fAngle);
            coords[idx++] = fRadius * (float)Math.sin(fAngle);
            coords[idx++] = 0.0f;
        }

        return idx;
    }
}



