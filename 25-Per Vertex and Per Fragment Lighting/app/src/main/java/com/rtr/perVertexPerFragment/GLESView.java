package com.rtr.perVertexPerFragment;

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

    private int vertexShaderObject_pv;
    private int fragmentShaderObject_pv;
    private int shaderProgramObject_pv;

    private int vertexShaderObject_pf;
    private int fragmentShaderObject_pf;
    private int shaderProgramObject_pf;

    private int[] vaoSphere = new int[1];
    private int[] vboSpherePosition = new int[1];
    private int[] vboSphereNormal = new int[1];
    private int[] vboSphereElement = new int[1];

    private float[] lightAmbient  = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
    private float[] lightDiffuse  = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] lightPosition = new float[] { 100.0f, 100.0f, 100.0f, 1.0f };

    private float[] materialAmbient  = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
    private float[] materialDiffuse  = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float[] materialSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float materialShininess = 128.0f;

    // per vertex shader //////////////////////////////////////////////////////

    private int mUniform_pv;
    private int vUniform_pv;
    private int pUniform_pv;
    
    private int laUniform_pv;
    private int ldUniform_pv;
    private int lsUniform_pv;
    private int lightPositionUniform_pv;

    private int kaUniform_pv;
    private int kdUniform_pv;
    private int ksUniform_pv;
    private int shininessUniform_pv;

    private int enableLightUniform_pv;

    //////////////////////////////////////////////////////////////////////////

    // per fragment shader //////////////////////////////////////////////////////

    private int mUniform_pf;
    private int vUniform_pf;
    private int pUniform_pf;
    
    private int laUniform_pf;
    private int ldUniform_pf;
    private int lsUniform_pf;
    private int lightPositionUniform_pf;

    private int kaUniform_pf;
    private int kdUniform_pf;
    private int ksUniform_pf;
    private int shininessUniform_pf;

    private int enableLightUniform_pf;

    //////////////////////////////////////////////////////////////////////////

    private float[] perspectiveProjectionMatrix = new float[16];

    private boolean bLight = false;
    private boolean bAnimation = true;
    private boolean bFragment = false;

    private int numVertices;
    private int numElements;

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
        bFragment = !bFragment;
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

        //////////// P E R   V E R T E X   S H A D E R ////////////////////////////////////
        //// VERTEX SHADER ////////////////////////////////////////////////
        // create shader object
        vertexShaderObject_pv = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // shader source code
        final String vertexShaderSourceCode_pv = String.format(
            "#version 320 es" +
            "\n" +
            "precision lowp int;" +
            "in vec4 vPosition;                                                                                                                 " +
            "in vec3 vNormal;                                                                                                                   " +
            "uniform mat4 u_m_matrix;                                                                                                           " +
            "uniform mat4 u_v_matrix;                                                                                                           " +
            "uniform mat4 u_p_matrix;                                                                                                           " +
            "uniform vec3 u_la;                                                                                                             " +
            "uniform vec3 u_ld;                                                                                                             " +
            "uniform vec3 u_ls;                                                                                                             " +
            "uniform vec4 u_light_position;                                                                                                 " +
            "uniform vec3 u_ka;                                                                                                                 " +
            "uniform vec3 u_kd;                                                                                                                 " +
            "uniform vec3 u_ks;                                                                                                                 " +
            "uniform float u_shininess;                                                                                                         " +
            "uniform int u_enable_light;                                                                                                        " +
            "out vec3 phong_ads_light;                                                                                                          " +
            "void main(void)                                                                                                                    " +
            "{                                                                                                                                  " +
            "   if (u_enable_light == 1)                                                                                                        " +
            "   {                                                                                                                               " +
            "       vec4 eye_coordinates = u_v_matrix * u_m_matrix * vPosition;                                                                 " +
            "       vec3 tnorm = normalize(mat3(u_v_matrix * u_m_matrix) * vNormal);                                                            " +
            "       vec3 viewer_vector = normalize(vec3(-eye_coordinates.xyz));                                                                 " +
            "                                                                                                                                   " +
            "       vec3 light_direction = normalize(vec3(u_light_position - eye_coordinates));                                         " +
            "       float tn_dot_ldir = max(dot(light_direction, tnorm), 0.0);                                                          " +
            "       vec3 reflection_vector = reflect(-light_direction, tnorm);                                                          " +
            "       vec3 ambient  = u_la * u_ka;                                                                                        " +
            "       vec3 diffuse  = u_ld * u_kd * tn_dot_ldir;                                                                      " +
            "       vec3 specular = u_ls * u_ks * pow(max(dot(reflection_vector, viewer_vector), 0.0), u_shininess);                " +
            "                                                                                                                                   " +
            "       phong_ads_light = ambient + diffuse + specular;                                                                 " +
            "   }                                                                                                                               " +
            "   else                                                                                                                            " +
            "   {                                                                                                                               " +
            "       phong_ads_light = vec3(1.0, 1.0, 1.0);                                                                                      " +
            "   }                                                                                                                               " +
            "   gl_Position = u_p_matrix * u_v_matrix * u_m_matrix * vPosition;                                                                 " +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(vertexShaderObject_pv, vertexShaderSourceCode_pv);

        // compile shader source code
        GLES32.glCompileShader(vertexShaderObject_pv);

        // compilation errors
        int[] iShaderCompileStatus = new int[1];
        int[] iInfoLogLength = new int[1];
        String szInfo = null;

        GLES32.glGetShaderiv(vertexShaderObject_pv, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(vertexShaderObject_pv, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(vertexShaderObject_pv);
                System.out.println("RTR: Vertex Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }

        }

        //// FRAGMENT SHADER ////////////////////////////////////////////////
        // create shader object
        fragmentShaderObject_pv = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

        // shader source code
        final String fragmentShaderSourceCode_pv = String.format(
            "#version 320 es" +
            "\n" +
            "precision lowp int;" +
            "precision highp float;" +
            "in vec3 phong_ads_light;                           " +
            "out vec4 FragColor;                                " +
            "void main(void)                                    " +
            "{                                                  " +
            "   FragColor = vec4(phong_ads_light, 1.0);         " +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(fragmentShaderObject_pv, fragmentShaderSourceCode_pv);

        // compile shader source code
        GLES32.glCompileShader(fragmentShaderObject_pv);

        // compilation errors
        iShaderCompileStatus[0] = 0;
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetShaderiv(fragmentShaderObject_pv, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(fragmentShaderObject_pv, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(fragmentShaderObject_pv);
                System.out.println("RTR: Fragment Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // create shader program object
        shaderProgramObject_pv = GLES32.glCreateProgram();

        // attach vertex shader to shader program
        GLES32.glAttachShader(shaderProgramObject_pv, vertexShaderObject_pv);

        // attach fragment shader to shader program
        GLES32.glAttachShader(shaderProgramObject_pv, fragmentShaderObject_pv);

        // pre-linking binding to vertex attribute
        GLES32.glBindAttribLocation(shaderProgramObject_pv, GLESMacros.AMC_ATTRIBUTE_POSITION, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObject_pv, GLESMacros.AMC_ATTRIBUTE_NORMAL, "vNormal");

        // link the shader program
        GLES32.glLinkProgram(shaderProgramObject_pv);

        // linking errors
        int[] iProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetProgramiv(shaderProgramObject_pv, GLES32.GL_LINK_STATUS, iProgramLinkStatus, 0);
        if (iProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObject_pv, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfo = GLES32.glGetProgramInfoLog(shaderProgramObject_pv);
                System.out.println("RTR: Program Linking: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // get unifrom locations
        mUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_m_matrix");
        vUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_v_matrix");
        pUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_p_matrix");

        laUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_la");
        ldUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_ld");
        lsUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_ls");
        lightPositionUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_light_position");

        kaUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_ka");
        kdUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_kd");
        ksUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_ks");
        shininessUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_shininess");

        enableLightUniform_pv = GLES32.glGetUniformLocation(shaderProgramObject_pv, "u_enable_light");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //////////// P E R   F R A G M E N T   S H A D E R ////////////////////////////////////
        //// VERTEX SHADER ////////////////////////////////////////////////
        // create shader object
        vertexShaderObject_pf = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // shader source code
        final String vertexShaderSourceCode_pf = String.format(
            "#version 320 es" +
            "\n" +
            "precision lowp int;" +
            "in vec4 vPosition;" +
            "in vec3 vNormal;" +
            "uniform mat4 u_m_matrix;" +
            "uniform mat4 u_v_matrix;" +
            "uniform mat4 u_p_matrix;" +
            "uniform vec4 u_light_position;" +
            "uniform int u_enable_light;" +
            "out vec3 tnorm;" +
            "out vec3 viewer_vector;" +
            "out vec3 light_direction;" +
            "void main(void)" +
            "{" +
            "   if (u_enable_light == 1) " +
            "   { " +
            "       vec4 eye_coordinates = u_v_matrix * u_m_matrix * vPosition;" +
            "       tnorm = mat3(u_v_matrix * u_m_matrix) * vNormal;" +
            "       viewer_vector = vec3(-eye_coordinates.xyz);" +
            "       light_direction   = vec3(u_light_position - eye_coordinates);" +
            "   }" +
            "   gl_Position = u_p_matrix * u_v_matrix * u_m_matrix * vPosition;" +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(vertexShaderObject_pf, vertexShaderSourceCode_pf);

        // compile shader source code
        GLES32.glCompileShader(vertexShaderObject_pf);

        // compilation errors
        iShaderCompileStatus = new int[1];
        iInfoLogLength = new int[1];
        szInfo = null;

        GLES32.glGetShaderiv(vertexShaderObject_pf, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(vertexShaderObject_pf, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(vertexShaderObject_pf);
                System.out.println("RTR: Vertex Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }

        }

        //// FRAGMENT SHADER ////////////////////////////////////////////////
        // create shader object
        fragmentShaderObject_pf = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

        // shader source code
        final String fragmentShaderSourceCode_pf = String.format(
            "#version 320 es" +
            "\n" +
            "precision lowp int;" +
            "precision highp float;" +
            "in vec3 tnorm;" +
            "in vec3 light_direction;" +
            "in vec3 viewer_vector;" +
            "uniform vec3 u_la;" +
            "uniform vec3 u_ld;" +
            "uniform vec3 u_ls;" +
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
            "       vec3 nlight_direction = normalize(light_direction);" +
            "       vec3 reflection_vector = reflect(-nlight_direction, ntnorm);" +
            "       float tn_dot_ldir = max(dot(ntnorm, nlight_direction), 0.0);" +
            "       vec3 ambient  = u_la * u_ka;" +
            "       vec3 diffuse  = u_ld * u_kd * tn_dot_ldir;" +
            "       vec3 specular = u_ls * u_ks * pow(max(dot(reflection_vector, nviewer_vector), 0.0), u_shininess);" +
            "                                                                                                                            " +
            "       vec3 phong_ads_light = ambient + diffuse + specular;" +
            "       FragColor = vec4(phong_ads_light, 1.0);" +
            "   }" +
            "   else" +
            "   {" +
            "       FragColor = vec4(1.0);" +
            "   }" +
            "}"
        );

        // attach shader source code to shader object
        GLES32.glShaderSource(fragmentShaderObject_pf, fragmentShaderSourceCode_pf);

        // compile shader source code
        GLES32.glCompileShader(fragmentShaderObject_pf);

        // compilation errors
        iShaderCompileStatus[0] = 0;
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetShaderiv(fragmentShaderObject_pf, GLES32.GL_COMPILE_STATUS, iShaderCompileStatus, 0);

        if (iShaderCompileStatus[0] == GLES32.GL_FALSE) {
            GLES32.glGetShaderiv(fragmentShaderObject_pf, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0) {
                szInfo = GLES32.glGetShaderInfoLog(fragmentShaderObject_pf);
                System.out.println("RTR: Fragment Shader: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // create shader program object
        shaderProgramObject_pf = GLES32.glCreateProgram();

        // attach vertex shader to shader program
        GLES32.glAttachShader(shaderProgramObject_pf, vertexShaderObject_pf);

        // attach fragment shader to shader program
        GLES32.glAttachShader(shaderProgramObject_pf, fragmentShaderObject_pf);

        // pre-linking binding to vertex attribute
        GLES32.glBindAttribLocation(shaderProgramObject_pf, GLESMacros.AMC_ATTRIBUTE_POSITION, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObject_pf, GLESMacros.AMC_ATTRIBUTE_NORMAL, "vNormal");

        // link the shader program
        GLES32.glLinkProgram(shaderProgramObject_pf);

        // linking errors
        iProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfo = null;

        GLES32.glGetProgramiv(shaderProgramObject_pf, GLES32.GL_LINK_STATUS, iProgramLinkStatus, 0);
        if (iProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObject_pf, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfo = GLES32.glGetProgramInfoLog(shaderProgramObject_pf);
                System.out.println("RTR: Program Linking: " + szInfo);
                uninitialize();
                System.exit(0);
            }
        }

        // get unifrom locations
        mUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_m_matrix");
        vUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_v_matrix");
        pUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_p_matrix");

        laUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_la");
        ldUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_ld");
        lsUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_ls");
        lightPositionUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_light_position");

        kaUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_ka");
        kdUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_kd");
        ksUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_ks");
        shininessUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_shininess");

        enableLightUniform_pf = GLES32.glGetUniformLocation(shaderProgramObject_pf, "u_enable_light");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        if (bFragment)
            GLES32.glUseProgram(shaderProgramObject_pf);
        else
            GLES32.glUseProgram(shaderProgramObject_pv);

        //declaration of matrices
        float[] translationMatrix = new float[16];
        float[] modelMatrix = new float[16];
        float[] viewMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(translationMatrix, 0);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setIdentityM(modelMatrix, 0);

        // perform necessary transformations
        Matrix.translateM(translationMatrix, 0,
            0.0f, 0.0f, -3.0f);

        // do necessary matrix multiplication
        Matrix.multiplyMM(modelMatrix, 0,
            modelMatrix, 0,
            translationMatrix, 0);

        if (bFragment) {
            // send necessary matrices to shader in respective uniforms
            GLES32.glUniformMatrix4fv(mUniform_pf, 1, false, modelMatrix, 0);
            GLES32.glUniformMatrix4fv(vUniform_pf, 1, false, viewMatrix, 0);
            GLES32.glUniformMatrix4fv(pUniform_pf, 1, false, perspectiveProjectionMatrix, 0);

            GLES32.glUniform3fv(laUniform_pf, 1, lightAmbient, 0);
            GLES32.glUniform3fv(ldUniform_pf, 1, lightDiffuse, 0);
            GLES32.glUniform3fv(lsUniform_pf, 1, lightSpecular, 0);
            GLES32.glUniform4fv(lightPositionUniform_pf, 1, lightPosition, 0);

            GLES32.glUniform3fv(kaUniform_pf, 1, materialAmbient, 0);
            GLES32.glUniform3fv(kdUniform_pf, 1, materialDiffuse, 0);
            GLES32.glUniform3fv(ksUniform_pf, 1, materialSpecular, 0);
            GLES32.glUniform1f(shininessUniform_pf, materialShininess);

            if (bLight)
                GLES32.glUniform1i(enableLightUniform_pf, 1);
            else
                GLES32.glUniform1i(enableLightUniform_pf, 0);
        } 
        else {
            // send necessary matrices to shader in respective uniforms
            GLES32.glUniformMatrix4fv(mUniform_pv, 1, false, modelMatrix, 0);
            GLES32.glUniformMatrix4fv(vUniform_pv, 1, false, viewMatrix, 0);
            GLES32.glUniformMatrix4fv(pUniform_pv, 1, false, perspectiveProjectionMatrix, 0);

            GLES32.glUniform3fv(laUniform_pv, 1, lightAmbient, 0);
            GLES32.glUniform3fv(ldUniform_pv, 1, lightDiffuse, 0);
            GLES32.glUniform3fv(lsUniform_pv, 1, lightSpecular, 0);
            GLES32.glUniform4fv(lightPositionUniform_pv, 1, lightPosition, 0);

            GLES32.glUniform3fv(kaUniform_pv, 1, materialAmbient, 0);
            GLES32.glUniform3fv(kdUniform_pv, 1, materialDiffuse, 0);
            GLES32.glUniform3fv(ksUniform_pv, 1, materialSpecular, 0);
            GLES32.glUniform1f(shininessUniform_pv, materialShininess);

            if (bLight)
                GLES32.glUniform1i(enableLightUniform_pv, 1);
            else
                GLES32.glUniform1i(enableLightUniform_pv, 0);
        }

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

        if (shaderProgramObject_pf != 0) {
            int[] shaderCount = new int[1];
            int shaderNumber;

            GLES32.glUseProgram(shaderProgramObject_pf);
            GLES32.glGetProgramiv(shaderProgramObject_pf, GLES32.GL_ATTACHED_SHADERS, shaderCount, 0);

            int[] shaders = new int[shaderCount[0]];

            GLES32.glGetAttachedShaders(shaderProgramObject_pf, shaderCount[0], shaderCount, 0, shaders, 0);
            
            for (shaderNumber = 0; shaderNumber < shaderCount[0]; shaderNumber++) {
                // detach shader
                GLES32.glDetachShader(shaderProgramObject_pf, shaders[shaderNumber]);

                // delete shader
                GLES32.glDeleteShader(shaders[shaderNumber]);
                shaders[shaderNumber] = 0;
            }

            GLES32.glUseProgram(0);
            GLES32.glDeleteProgram(shaderProgramObject_pf);
            shaderProgramObject_pf = 0;
        }
    }

    private void update() {
        
    }
}



