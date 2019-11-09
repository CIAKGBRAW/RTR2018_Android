package com.rtr.shadow;

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
import java.nio.ShortBuffer;  // ShortBuffer
import java.nio.ByteOrder;    // ByteOrder
import java.nio.FloatBuffer;  // FloatBuffer

import android.opengl.Matrix; // Matrix


public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener {

    private final Context context;
    private GestureDetector gestureDetector;

    private int gWidth;
    private int gHeight;

    private final float FRUSTRUM_DEPTH = 1500.0f;
    private final int DEPTH_TEXTURE_SIZE = 4096;

    private int[] vaoSphere = new int[1];
    private int[] vboSpherePosition = new int[1];
    private int[] vboSphereNormal = new int[1];
    private int[] vboSphereElement = new int[1];
    
    private int render_light_prog;
    private int render_scene_prog;

    private int[] vaoGround = new int[1];           // vertex array object
    private int[] vboGroundPos = new int[1];        // vertex buffer object
    private int[] vboGroundNor = new int[1];        // vertex buffer object

    private int mvpUniform;
    private int modelUniform;
    private int viewUniform;
    private int projectionUniform;
    private int shadowUniform;
    private int kaUniform;
    private int kdUniform;
    private int ksUniform;
    private int shineUniform;
    private int enableLightUniform;
    private int lightPositionUniform;
    private int depthTextureUniform;

    private boolean bLight = false;
    private boolean bAnimation = true;

    private int numVertices;
    private int numElements;

    private int[] depth_texture = new int[1];
    private int[] depth_fbo = new int[1];

    private float t;

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
        bAnimation = !bAnimation;
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
        bLight = !bLight;
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

        float vertexShaderObject;
        float fragmentShaderObject;

        float render_light_vertex_shader;
        float render_light_fragment_shader;

        ///////////////// light rendering shader ///////////////////////////////////////////////

        // create vertex shader object
        render_light_vertex_shader = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // vertex shader source code 
        final String *render_light_vertex_shader_source = String.format(
            "#version 330 core es"                                +                                  
            "\n"                                                  +                                  
            "in vec4 vPosition;"                                  +                                  
            "uniform mat4 u_mvp_matrix;"                          +                                  
            "void main(void)"                                     +                                  
            "{"                                                   +                                  
            "   gl_Position = u_mvp_matrix * vPosition;"          +                                  
            "}"
        );

        // attach source code to vertex shader
        GLES32.glShaderSource(render_light_vertex_shader, render_light_vertex_shader_source);

        // compile vertex shader source code
        GLES32.glCompileShader(render_light_vertex_shader);

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
            "#version 320 es"             +
            "\n"                          +
            "precision highp float;"      +
            "out vec4 FragColor;"         +
            "void main(void)"             +
            "{"                           +
            "   FragColor = vec4(1.0);"   +
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
                szInfo = GLES32.glGetProgramInfoLog(shaderProgramObject);
                System.out.println("RTR: Program Linking: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // get unifrom locations
        mvpUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_mvp_matrix");

        final float[] ground_vertices = new float[] {
            -500.0f, -50.0f, -500.0f, 1.0f,
            -500.0f, -50.0f,  500.0f, 1.0f,
             500.0f, -50.0f,  500.0f, 1.0f,
             500.0f, -50.0f, -500.0f, 1.0f,
        };

        final float[] ground_normals = new float[] {
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        };

        // create vao
        GLES32.glGenVertexArrays(1, vaoGround, 0);
        GLES32.glBindVertexArray(vaoGround[0]);

        // ground vertices
        GLES32.glGenBuffers(1, vboGroundPos, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboGroundPos[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(ground_vertices.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBuffer.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferGround = byteBuffer.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferGround.put(ground_vertices);

        // 5. set the array at 0th position of buffer
        positionBufferGround.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, ground_vertices.length * 4, positionBufferGround, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION); 

        // ground normals
        GLES32.glGenBuffers(1, vboGroundNor, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboGroundNor[0]);

        // 1. Allocate buffer directly from native memory
        byteBuffer = ByteBuffer.allocateDirect(ground_normals.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBuffer.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer normalGroundBuffer = byteBuffer.asFloatBuffer();

        // 4. put data in this COOKED buffer
        normalGroundBuffer.put(ground_normals);

        // 5. set the array at 0th position of buffer
        normalGroundBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, ground_normals.length * 4, normalGroundBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_NORMAL, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_NORMAL);

        ////////////////////////////////////////////////////////////////////////////////////////////////////

        /////// scene render shader ////////////////////////////////////////////////////////////////////////

        // vertex shader source code 
        final String *render_light_vertex_shader_source = String.format(
            "#version 330 core es"                                +                                  
            "\n"                                                  +                                  
            "in vec4 vPosition;"                                  +                                  
            "uniform mat4 u_mvp_matrix;"                          +                                  
            "void main(void)"                                     +                                  
            "{"                                                   +                                  
            "   gl_Position = u_mvp_matrix * vPosition;"          +                                  
            "}"
        );

        // attach source code to vertex shader
        glShaderSource(render_light_vertex_shader, render_light_vertex_shader_source);

        // compile vertex shader source code
        glCompileShader(render_light_vertex_shader);

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
            "#version 320 es"             +
            "\n"                          +
            "precision highp float;"      +
            "out vec4 FragColor;"         +
            "void main(void)"             +
            "{"                           +
            "   FragColor = vec4(1.0);"   +
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
                szInfo = GLES32.glGetProgramInfoLog(shaderProgramObject);
                System.out.println("RTR: Program Linking: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // get unifrom locations
        mvpUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_mvp_matrix");



        Sphere sphere = new Sphere();
        float[] sphereVertices = new float[1146];
        float[] sphereNormals = new float[1146];
        float[] sphereTextures = new float[764];
        short[] sphereElements = new short[2280];

        sphere.getSphereVertexData(sphereVertices, sphereNormals, sphereTextures, sphereElements);
        numVertices = sphere.getNumberOfSphereVertices();
        numElements = sphere.getNumberOfSphereElements();


        //// sphere /////////////////

        // create vao
        GLES32.glGenVertexArrays(1, vaoSphere, 0);
        GLES32.glBindVertexArray(vaoSphere[0]);

        // sphere vertices
        GLES32.glGenBuffers(1, vboSpherePosition, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboSpherePosition[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferSphere = ByteBuffer.allocateDirect(sphereVertices.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferSphere.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer positionBufferSphere = byteBufferSphere.asFloatBuffer();

        // 4. put data in this COOKED buffer
        positionBufferSphere.put(sphereVertices);

        // 5. set the array at 0th position of buffer
        positionBufferSphere.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphereVertices.length * 4, positionBufferSphere, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION); 

        // sphere normals
        GLES32.glGenBuffers(1, vboSphereNormal, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboSphereNormal[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferSphereNormal = ByteBuffer.allocateDirect(sphereNormals.length * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferSphereNormal.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer normalSphereBuffer = byteBufferSphereNormal.asFloatBuffer();

        // 4. put data in this COOKED buffer
        normalSphereBuffer.put(sphereNormals);

        // 5. set the array at 0th position of buffer
        normalSphereBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphereNormals.length * 4, normalSphereBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_NORMAL, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_NORMAL);

        // sphere elements 
        GLES32.glGenBuffers(1, vboSphereElement, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);

        byteBufferSphere = ByteBuffer.allocateDirect(sphereElements.length * 2);
        byteBufferSphere.order(ByteOrder.nativeOrder());
        ShortBuffer elementsBuffer = byteBufferSphere.asShortBuffer();
        elementsBuffer.put(sphereElements);
        elementsBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, sphereElements.length * 2, elementsBuffer, GLES32.GL_STATIC_DRAW);


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

        Matrix.setIdentityM(perspectiveProjectionMatrix, 0);
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
        float[] rotationMatrix = new float[16];
        float[] scaleMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        //// Cube //////////////////////////////////////

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            0.0f, 0.0f, -2.0f);

        Matrix.setRotateM(rotationMatrix, 0,
            angleSphere, 1.0f, 1.0f, 1.0f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            translationMatrix, 0);

        Matrix.multiplyMM(modelViewMatrix, 0,
            modelViewMatrix, 0,
            rotationMatrix, 0);

        // send necessary matrices to shader in respective uniforms
        GLES32.glUniformMatrix4fv(mvUniform, 1, false, modelViewMatrix, 0);
        GLES32.glUniformMatrix4fv(pUniform, 1, false, perspectiveProjectionMatrix, 0);

        GLES32.glUniform3f(ldUniform, 1.0f, 1.0f, 1.0f);
        GLES32.glUniform3f(kdUniform, 0.5f, 0.5f, 0.5f);
        GLES32.glUniform3f(lightPositionUniform, 0.0f, 0.0f, 2.0f);


        if (bLight)
            GLES32.glUniform1i(enableLightUniform, 1);
        else
            GLES32.glUniform1i(enableLightUniform, 0);

        // bind with vao (this will avoid many binding to vbo)
        GLES32.glBindVertexArray(vaoSphere[0]);  
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);

        // draw necessary scene
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        // unbind vao
        GLES32.glBindVertexArray(0);

        ////////////////////////////////////////////////////

        // unuse program
        GLES32.glUseProgram(0);
        
        requestRender();  // ~ swapBuffers

        if(bAnimation) update();
    }

    private void uninitialize() {

        if (vboSphereElement[0] != 0) {
            GLES32.glDeleteBuffers(1, vboSphereElement, 0);
            vboSphereElement[0] = 0;
        }

        if (vboSphereNormal[0] != 0) {
            GLES32.glDeleteBuffers(1, vboSphereNormal, 0);
            vboSphereNormal[0] = 0;
        }

        if (vboSpherePosition[0] != 0) {
            GLES32.glDeleteBuffers(1, vboSpherePosition, 0);
            vboSpherePosition[0] = 0;
        }

        if (vaoSphere[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vaoSphere, 0);
            vaoSphere[0] = 0;
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

    private void update() {
        if (angleSphere >= 360.0f)
        {
            angleSphere = 0.0f;
        }
        else
        {
            angleSphere += 2.0f;
        }
    }
}



