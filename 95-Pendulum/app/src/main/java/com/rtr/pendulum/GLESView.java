package com.rtr.pendulum;

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

    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;

    private int[] vaoSphere = new int[1];
    private int[] vboSpherePosition = new int[1];
    private int[] vboSphereNormal = new int[1];
    private int[] vboSphereElement = new int[1];

    private int[] vaoLine = new int[1];
    private int[] vboLinePosition = new int[1];
    private int[] vboLineNormal = new int[1];

    private FloatBuffer positionBuffer;

    private Light[] lights = new Light[3];

    private float[] materialAmbient = new float[] { 0.20f, 0.20f, 0.20f, 1.0f };
    private float[] materialDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] materialSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float materialShininess = 128.0f;

    private int mUniform;
    private int vUniform;
    private int pUniform;
    
    private int laUniform_red;
    private int ldUniform_red;
    private int lsUniform_red;
    private int lightPositionUniform_red;

    private int laUniform_green;
    private int ldUniform_green;
    private int lsUniform_green;
    private int lightPositionUniform_green;

    private int laUniform_blue;
    private int ldUniform_blue;
    private int lsUniform_blue;
    private int lightPositionUniform_blue;

    private int kaUniform;
    private int kdUniform;
    private int ksUniform;
    private int shininessUniform;

    private int enableLightUniform;

    private float[] perspectiveProjectionMatrix = new float[16];

    private boolean bLight = true;
    private boolean bAnimation = true;

    private int numVertices;
    private int numElements;

    private float t = 0.0f;

    private int state = 0;

    private float cX = 0.0f;
    private float cY = 6.0f;
    private float cZ = 0.0f;
    private float theta = 0.0f;

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
            "precision lowp int;" +
            "in vec4 vPosition;" +
            "in vec3 vNormal;" +
            "uniform mat4 u_m_matrix;" +
            "uniform mat4 u_v_matrix;" +
            "uniform mat4 u_p_matrix;" +
            "uniform vec4 u_light_position_red;" +
            "uniform vec4 u_light_position_green;" +
            "uniform vec4 u_light_position_blue;" +
            "uniform int u_enable_light;" +
            "out vec3 tnorm;" +
            "out vec3 viewer_vector;" +
            "out vec3 light_direction_red;" +
            "out vec3 light_direction_green;" +
            "out vec3 light_direction_blue;" +
            "void main(void)" +
            "{" +
            "   if (u_enable_light == 1) " +
            "   { " +
            "       vec4 eye_coordinates = u_v_matrix * u_m_matrix * vPosition;" +
            "       tnorm = mat3(u_v_matrix * u_m_matrix) * vNormal;" +
            "       viewer_vector = vec3(-eye_coordinates.xyz);" +
            "       light_direction_red   = vec3(u_light_position_red - eye_coordinates);" +
            "       light_direction_green = vec3(u_light_position_green - eye_coordinates);" +
            "       light_direction_blue  = vec3(u_light_position_blue - eye_coordinates);" +
            "   }" +
            "   gl_Position = u_p_matrix * u_v_matrix * u_m_matrix * vPosition;" +
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
            "precision lowp int;" +
            "precision highp float;" +
            "in vec3 tnorm;" +
            "in vec3 light_direction_red;" +
            "in vec3 light_direction_green;" +
            "in vec3 light_direction_blue;" +
            "in vec3 viewer_vector;" +
            "uniform vec3 u_la_red;" +
            "uniform vec3 u_ld_red;" +
            "uniform vec3 u_ls_red;" +
            "uniform vec3 u_la_green ;" +
            "uniform vec3 u_ld_green ;" +
            "uniform vec3 u_ls_green ;" +
            "uniform vec3 u_la_blue;" +
            "uniform vec3 u_ld_blue;" +
            "uniform vec3 u_ls_blue;" +
            "uniform vec3 u_ka;" +
            "uniform vec3 u_kd;" +
            "uniform vec3 u_ks;" +
            "uniform float u_shininess;" +
            "uniform int u_enable_light;" +
            "out vec4 FragColor;" +
            "void main(void)" +
            "{" +
            "   if (u_enable_light == 1) " +
            "   { " +
            "       vec3 ntnorm = normalize(tnorm);" +
            "       vec3 nviewer_vector = normalize(viewer_vector);" +
            "                                                                                                                            " +
            "       vec3 nlight_direction_red = normalize(light_direction_red);" +
            "       vec3 reflection_vector_red = reflect(-nlight_direction_red, ntnorm);" +
            "       float tn_dot_ldir_red = max(dot(ntnorm, nlight_direction_red), 0.0);" +
            "       vec3 ambient_red  = u_la_red * u_ka;" +
            "       vec3 diffuse_red  = u_ld_red * u_kd * tn_dot_ldir_red;" +
            "       vec3 specular_red = u_ls_red * u_ks * pow(max(dot(reflection_vector_red, nviewer_vector), 0.0), u_shininess);" +
            "                                                                                                                            " +
            "       vec3 nlight_direction_green = normalize(light_direction_green);" +
            "       vec3 reflection_vector_green = reflect(-nlight_direction_green, ntnorm);" +
            "       float tn_dot_ldir_green = max(dot(ntnorm, nlight_direction_green), 0.0);" +
            "       vec3 ambient_green  = u_la_green * u_ka;" +
            "       vec3 diffuse_green  = u_ld_green * u_kd * tn_dot_ldir_green;" +
            "       vec3 specular_green = u_ls_green * u_ks * pow(max(dot(reflection_vector_green, nviewer_vector), 0.0), u_shininess);" +
            "                                                                                                                            " +
            "       vec3 nlight_direction_blue = normalize(light_direction_blue);" +
            "       vec3 reflection_vector_blue = reflect(-nlight_direction_blue, ntnorm);" +
            "       float tn_dot_ldir_blue = max(dot(ntnorm, nlight_direction_blue), 0.0);" +
            "       vec3 ambient_blue  = u_la_blue * u_ka;" +
            "       vec3 diffuse_blue  = u_ld_blue * u_kd * tn_dot_ldir_blue;" +
            "       vec3 specular_blue = u_ls_blue * u_ks * pow(max(dot(reflection_vector_blue, nviewer_vector), 0.0), u_shininess);" +
            "                                                                                                                            " +
            "       vec3 phong_ads_light = ambient_red + diffuse_red + specular_red + ambient_green + diffuse_green + specular_green + ambient_blue + diffuse_blue + specular_blue;" +
            "       FragColor = vec4(phong_ads_light, 1.0);" +
            "   }" +
            "   else" +
            "   {" +
            "       FragColor = vec4(1.0);" +
            "   }" +
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
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.AMC_ATTRIBUTE_NORMAL, "vNormal");

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
        mUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_m_matrix");
        vUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_v_matrix");
        pUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_p_matrix");

        laUniform_red = GLES32.glGetUniformLocation(shaderProgramObject, "u_la_red");
        ldUniform_red = GLES32.glGetUniformLocation(shaderProgramObject, "u_ld_red");
        lsUniform_red = GLES32.glGetUniformLocation(shaderProgramObject, "u_ls_red");
        lightPositionUniform_red = GLES32.glGetUniformLocation(shaderProgramObject, "u_light_position_red");

        laUniform_green = GLES32.glGetUniformLocation(shaderProgramObject, "u_la_green");
        ldUniform_green = GLES32.glGetUniformLocation(shaderProgramObject, "u_ld_green");
        lsUniform_green = GLES32.glGetUniformLocation(shaderProgramObject, "u_ls_green");
        lightPositionUniform_green = GLES32.glGetUniformLocation(shaderProgramObject, "u_light_position_green");

        laUniform_blue = GLES32.glGetUniformLocation(shaderProgramObject, "u_la_blue");
        ldUniform_blue = GLES32.glGetUniformLocation(shaderProgramObject, "u_ld_blue");
        lsUniform_blue = GLES32.glGetUniformLocation(shaderProgramObject, "u_ls_blue");
        lightPositionUniform_blue = GLES32.glGetUniformLocation(shaderProgramObject, "u_light_position_blue");

        kaUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_ka");
        kdUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_kd");
        ksUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_ks");
        shininessUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_shininess");

        enableLightUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_enable_light");


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

        ////// LINE ///////////////////////////////////////////////////////////////////////////////////////////

        float[] normals = new float[]{
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
        };

        // create vao
        GLES32.glGenVertexArrays(1, vaoLine, 0);
        GLES32.glBindVertexArray(vaoLine[0]);

        ////// create vbo for position
        GLES32.glGenBuffers(1, vboLinePosition, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboLinePosition[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2 * 3 * 4);

        // 2. Arrange the buffer in native byte order
        byteBuffer.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        positionBuffer = byteBuffer.asFloatBuffer();

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 2 * 3 * 4, null, GLES32.GL_DYNAMIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        ///// create vbo for color
        GLES32.glGenBuffers(1, vboLineNormal, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboLineNormal[0]);

        // 1. Allocate buffer directly from native memory
        byteBuffer = ByteBuffer.allocateDirect(2 * 3 * 4);

        // 2. Arrange the buffer in native byte order
        byteBuffer.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer normalBuffer = byteBuffer.asFloatBuffer();

        // 4. put data in this COOKED buffer
        normalBuffer.put(normals);

        // 5. set the array at 0th position of buffer
        normalBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 2 * 3 * 4, normalBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_NORMAL, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_NORMAL);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        //////////////////////////////////////////////////////////////////////

        // light configurations

        lights[0] = new Light();
        lights[1] = new Light();
        lights[2] = new Light();

        // RED light
        lights[0].lightAmbient[0] = 1.0f;
        lights[0].lightAmbient[1] = 0.0f;
        lights[0].lightAmbient[2] = 0.0f;
        lights[0].lightAmbient[3] = 1.0f;

        lights[0].lightDiffuse[0] = 1.0f;
        lights[0].lightDiffuse[1] = 0.0f;
        lights[0].lightDiffuse[2] = 0.0f;
        lights[0].lightDiffuse[3] = 1.0f;

        lights[0].lightSpecular[0] = 1.0f;
        lights[0].lightSpecular[1] = 0.0f;
        lights[0].lightSpecular[2] = 0.0f;
        lights[0].lightSpecular[3] = 1.0f;

        lights[0].lightPosition[0] = 0.0f;
        lights[0].lightPosition[1] = 0.0f;
        lights[0].lightPosition[2] = 0.0f;
        lights[0].lightPosition[3] = 1.0f;

        lights[0].angle = 0.0f;

        // GREEN light
        lights[1].lightAmbient[0] = 0.0f;
        lights[1].lightAmbient[1] = 1.0f;
        lights[1].lightAmbient[2] = 0.0f;
        lights[1].lightAmbient[3] = 1.0f;

        lights[1].lightDiffuse[0] = 0.0f;
        lights[1].lightDiffuse[1] = 1.0f;
        lights[1].lightDiffuse[2] = 0.0f;
        lights[1].lightDiffuse[3] = 1.0f;

        lights[1].lightSpecular[0] = 0.0f;
        lights[1].lightSpecular[1] = 1.0f;
        lights[1].lightSpecular[2] = 0.0f;
        lights[1].lightSpecular[3] = 1.0f;

        lights[1].lightPosition[0] = 0.0f;
        lights[1].lightPosition[1] = 0.0f;
        lights[1].lightPosition[2] = 0.0f;
        lights[1].lightPosition[3] = 1.0f;

        lights[1].angle = 0.0f;

        // BLUE light
        lights[2].lightAmbient[0] = 0.0f;
        lights[2].lightAmbient[1] = 0.0f;
        lights[2].lightAmbient[2] = 1.0f;
        lights[2].lightAmbient[3] = 1.0f;

        lights[2].lightDiffuse[0] = 0.0f;
        lights[2].lightDiffuse[1] = 0.0f;
        lights[2].lightDiffuse[2] = 1.0f;
        lights[2].lightDiffuse[3] = 1.0f;

        lights[2].lightSpecular[0] = 0.0f;
        lights[2].lightSpecular[1] = 0.0f;
        lights[2].lightSpecular[2] = 1.0f;
        lights[2].lightSpecular[3] = 1.0f;

        lights[2].lightPosition[0] = 0.0f;
        lights[2].lightPosition[1] = 0.0f;
        lights[2].lightPosition[2] = 0.0f;
        lights[2].lightPosition[3] = 1.0f;

        lights[2].angle = 0.0f;


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
        float theta0 = (float)Math.PI / 3.0f;
        float G = 9.8f;
        float l = 10.0f;

        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        // use shader program
        GLES32.glUseProgram(shaderProgramObject);

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelMatrix = new float[16];
        float[] viewMatrix = new float[16];

        if (state == 0) {

            cX = 0.0f;
            cY -= 0.01f;

            System.out.println("RTR: state 0 | cY: " + cY);
            if (cY <= 4.0f+((l-2.4f)*(float)Math.sin(theta - ((float)Math.PI / 2.0f)))) {
                state = 1;
                System.out.println("RTR: state 0 -> state 1 | cY: " + cY);
            }

        } else if (state == 1) {
            System.out.println("RTR: state 1");
            theta += 0.0075f;

            if (theta >= theta0) {
                state = 2;
                System.out.println("RTR: state 1 -> state 2");
            }

        } else if (state == 2) {
            System.out.println("RTR: state 2");

        }
        

        for (int i = 0; i < 12; i++) {
            l -= 0.2f;

            // intialize above matrices to identity
            Matrix.setIdentityM(translationMatrix, 0);
            Matrix.setIdentityM(viewMatrix, 0);
            Matrix.setIdentityM(modelMatrix, 0);

            if (state == 1) {

                cX = l*(float)Math.cos(theta - ((float)Math.PI / 2.0f));
                cY = 4.0f+(l*(float)Math.sin(theta - ((float)Math.PI / 2.0f)));
                System.out.println("RTR: state 1 inside loop cY: " + cY);

            } else if (state == 2) {

                theta = theta0 * (float)Math.cos(Math.sqrt(G / l) * t);

                cX = l*(float)Math.cos(theta - ((float)Math.PI / 2.0f));
                cY = 4.0f+(l*(float)Math.sin(theta - ((float)Math.PI / 2.0f)));

            }

            cZ = -20.0f+(i*0.8f);

            // perform necessary transformations
            Matrix.translateM(translationMatrix, 0,
                cX, cY, cZ);

            // do necessary matrix multiplication
            Matrix.multiplyMM(modelMatrix, 0,
                modelMatrix, 0,
                translationMatrix, 0);

            // send necessary matrices to shader in respective uniforms
            GLES32.glUniformMatrix4fv(mUniform, 1, false, modelMatrix, 0);
            GLES32.glUniformMatrix4fv(vUniform, 1, false, viewMatrix, 0);
            GLES32.glUniformMatrix4fv(pUniform, 1, false, perspectiveProjectionMatrix, 0);

            GLES32.glUniform3fv(laUniform_red, 1, lights[0].lightAmbient, 0);
            GLES32.glUniform3fv(ldUniform_red, 1, lights[0].lightDiffuse, 0);
            GLES32.glUniform3fv(lsUniform_red, 1, lights[0].lightSpecular, 0);
            GLES32.glUniform4f(lightPositionUniform_red, 0.0f, 100.0f*(float)Math.cos(lights[0].angle), 100.0f*(float)Math.sin(lights[0].angle), 1.0f);

            GLES32.glUniform3fv(laUniform_green, 1, lights[1].lightAmbient, 0);
            GLES32.glUniform3fv(ldUniform_green, 1, lights[1].lightDiffuse, 0);
            GLES32.glUniform3fv(lsUniform_green, 1, lights[1].lightSpecular, 0);
            GLES32.glUniform4f(lightPositionUniform_green, 100.0f*(float)Math.cos(lights[0].angle), 0.0f, 100.0f*(float)Math.sin(lights[0].angle), 1.0f);

            GLES32.glUniform3fv(laUniform_blue, 1, lights[2].lightAmbient, 0);
            GLES32.glUniform3fv(ldUniform_blue, 1, lights[2].lightDiffuse, 0);
            GLES32.glUniform3fv(lsUniform_blue, 1, lights[2].lightSpecular, 0);
            GLES32.glUniform4f(lightPositionUniform_blue, 100.0f*(float)Math.cos(lights[0].angle), 100.0f*(float)Math.sin(lights[0].angle), 0.0f, 1.0f);

            GLES32.glUniform3fv(kaUniform, 1, materialAmbient, 0);
            GLES32.glUniform3fv(kdUniform, 1, materialDiffuse, 0);
            GLES32.glUniform3fv(ksUniform, 1, materialSpecular, 0);
            GLES32.glUniform1f(shininessUniform, materialShininess);


            if (bLight)
                GLES32.glUniform1i(enableLightUniform, 1);
            else
                GLES32.glUniform1i(enableLightUniform, 0);

            // bind with vao (this will avoid many binding to vbo)
            GLES32.glBindVertexArray(vaoSphere[0]);  
            GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);

            // draw necessary scene
            GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

            float[] points = new float[] {
                0.0f, 0.0f, 0.0f,
                -l*(float)Math.sin(theta), l*(float)Math.cos(theta), 0.0f
            };

            GLES32.glBindVertexArray(vaoLine[0]);

            // fill vertex buffer
            positionBuffer.put(points);

            // set the array at 0th position of buffer
            positionBuffer.position(0);

            //
            GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboLinePosition[0]);
            GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 2 * 3 * 4, positionBuffer, GLES32.GL_DYNAMIC_DRAW);
            GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

            GLES32.glLineWidth(4.0f);
            GLES32.glDrawArrays(GLES32.GL_LINES, 0, 2);

            // unbind vao
            GLES32.glBindVertexArray(0);
        }

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
        if (lights[0].angle < 360.0f)
        {
            lights[0].angle += 0.02f;
        }
        else
        {
            lights[0].angle = 0.0f;
        }

        if (lights[1].angle < 360.0f)
        {
            lights[1].angle += 0.02f;
        }
        else
        {
            lights[1].angle = 0.0f;
        }

        if (lights[2].angle < 360.0f)
        {
            lights[2].angle += 0.02f;
        }
        else
        {
            lights[2].angle = 0.0f;
        }

        if (state == 2) 
            t += 0.025f;
    }
}



