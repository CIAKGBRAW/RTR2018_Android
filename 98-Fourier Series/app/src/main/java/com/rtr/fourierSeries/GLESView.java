package com.rtr.fourierSeries;

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

import android.media.MediaPlayer;

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
    private int[] vboPos = new int[1];
    private int[] vboCol = new int[1];
    private int mvpUniform;

    private float[] orthographicProjectionMatrix = new float[16];

    private FloatBuffer positionBuffer;
    private float[] points = new float[300 * 3];
    private int index = 0;
    private boolean limit = false;

    private int tapCount = 0;
    private boolean bArrows = false;
    private boolean bFirst = false;
    private boolean bAnimate = false;

    private int counter = 0;
    private int loop = 0;

    private float t = 0.0f;
    private MediaPlayer mediaPlayer;

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
        bArrows = !bArrows;
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

        tapCount++;

        if (tapCount > 9) {
            tapCount = 0;
        }

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
        System.out.println("RTR: LONG PRESS####################");
        bAnimate = !bAnimate;
        counter = 0;
        tapCount = 0;
        loop = 0;
    }

    // abstract method from OnGestureListener so must be implemented
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // uninitialize();
        // System.exit(0);
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

        ////////////////////////////////////////////////////////////////////////

        mediaPlayer = MediaPlayer.create(context, R.raw.theme);
        mediaPlayer.start();

        //////////////////////////////////////////////////////////////////////

        //// VERTEX SHADER ////////////////////////////////////////////////
        // create shader object
        vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        // shader source code
        final String vertexShaderSourceCode = String.format(
            "#version 320 es" +
            "\n" +
            "in vec4 vPosition;" +
            "in vec4 vColor;" +
            "uniform mat4 u_mvp_matrix;" +
            "out vec4 out_Color;" +
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

        float[] colors = new float[300 * 4];
        for(int i = 0; i < 200; i++) {
            colors[(4*i) + 0] = 0.0f;
            colors[(4*i) + 1] = 1.0f;
            colors[(4*i) + 2] = 0.0f;
            if (i > 150) {
                colors[(4*i) + 3] = 1.0f;
            } else {
                colors[(4*i) + 3] = ((float)i / 150.0f);
            }
        }

        for(int i = 200; i < 300; i ++) {
            colors[(4*i) + 0] = 1.0f;
            colors[(4*i) + 1] = 1.0f;
            colors[(4*i) + 2] = 1.0f;
            colors[(4*i) + 3] = 1.0f;
        }

        // create vao
        GLES32.glGenVertexArrays(1, vao, 0);
        GLES32.glBindVertexArray(vao[0]);

        ////// create vbo for position
        GLES32.glGenBuffers(1, vboPos, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPos[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(300 * 3 * 4);

        // 2. Arrange the buffer in native byte order
        byteBuffer.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        positionBuffer = byteBuffer.asFloatBuffer();

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 300 * 3 * 4, null, GLES32.GL_DYNAMIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);

        ///// create vbo for color
        GLES32.glGenBuffers(1, vboCol, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboCol[0]);

        // 1. Allocate buffer directly from native memory
        ByteBuffer byteBufferCol = ByteBuffer.allocateDirect(300 * 4 * 4);

        // 2. Arrange the buffer in native byte order
        byteBufferCol.order(ByteOrder.nativeOrder());

        // 3. Create the float type buffer and convert it to float buffer
        FloatBuffer colorBuffer = byteBufferCol.asFloatBuffer();

        // 4. put data in this COOKED buffer
        colorBuffer.put(colors);

        // 5. set the array at 0th position of buffer
        colorBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 300 * 4 * 4, colorBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_COLOR, 4, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_COLOR);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        ////////////////////////////////////////////////////////////////////////

        mediaPlayer = MediaPlayer.create(context, R.raw.theme);
        mediaPlayer.start();

        //////////////////////////////////////////////////////////////////////

        // clear the depth buffer
        GLES32.glClearDepthf(1.0f);

        // clear the screen by OpenGL
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // enable depth
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);

        // enable blending
        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);

        Matrix.setIdentityM(orthographicProjectionMatrix, 0);
        System.out.println("RTR: " + orthographicProjectionMatrix.toString());

    }

    private void resize(int width, int height) {
        System.out.println("RTR: resize called with: " + width + " " + height);
        if (height == 0)
        {
            height = 1;
        }

        GLES32.glViewport(0, 0, width, height);

        if (width < height)
        {
            System.out.println("RTR: resize: width < height");
            Matrix.orthoM(orthographicProjectionMatrix, 0,
                -300.0f, 300.0f,
                -300.0f * ((float)height / (float)width), 300.0f * ((float)height / (float)width),
                -300.0f, 300.0f);
        }
        else 
        {
            Matrix.orthoM(orthographicProjectionMatrix, 0,
                -300.0f * ((float)width / (float)height), 300.0f * ((float)width / (float)height),
                -300.0f, 300.0f,
                -300.0f, 300.0f);
        }
    }

    private void display() {

        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        // use shader program
        GLES32.glUseProgram(shaderProgramObject);

        if (bAnimate) {
            counter++;
            System.out.println("RTR: counter" + counter);

            if (counter == 200) {

                if (bFirst == true) {
                    bFirst = false;
                }

                counter = 0;
                loop++;
                System.out.println("RTR: loop " + loop);

                if (loop == 3) {
                    bArrows = true;
                    System.out.println("RTR: bArrows " + bArrows);
                } else if (loop == 6) {
                    bArrows = false;
                    loop = 0;
                    tapCount++;
                    System.out.println("RTR: loop " + loop);
                    System.out.println("RTR: tapCount " + tapCount);

                    if (tapCount > 9) {
                        tapCount = 9;
                    }
                }
            }
        }

        //declaration of matrices
        float[] modelViewMatrix = new float[16];
        float[] modelViewProjectionMatrix = new float[16];

        // intialize above matrices to identity
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);

        final float[] amps = new float[] {
            112.70499594706054f,
            45.1062186548737f,
            40.245909894034504f,
            33.66414320911293f,
            31.795173522964575f,
            26.622471154415454f,
            15.517589116422805f,
            9.63400969044891f,
            9.426363176091753f,
            6.957447601577001f,
            6.218679694021884f,
            5.917691347989756f,
            4.739984602914571f,
            3.3764031917577437f,
            2.6581243623683593f,
            2.0593253809221563f,
            1.9728053522369562f,
            1.7827267040353396f,
            1.5755359292269644f,
            1.2388076433055748f,
            1.0758319387678772f,
            1.0405220308464147f,
            0.7645059344079934f,
            0.7427060290029845f,
            0.6339511819335483f,
            0.6007122270996832f,
            0.5768588652089517f,
            0.5638351526275915f,
            0.5136697032075562f,
            0.5126017429917268f,
            0.46273235675855723f,
            0.45982942467977844f,
            0.43762188825576476f,
            0.41388186046389625f,
            0.3845424573406701f,
            0.3702153332923732f,
            0.3395745123046622f,
            0.32021400834623986f,
            0.2842352084039312f,
            0.28410353091960755f,
            0.2754066007453611f,
            0.269154372225744f,
            0.22882447965599728f,
            0.22881353551801575f,
            0.2173900086291289f,
            0.2128111465886529f,
            0.2120796858462154f,
            0.2074518506651931f,
            0.2018698522798391f,
            0.19311609106693337f,
            0.1799027248111329f,
            0.17505379028788504f,
            0.16370888184462293f,
            0.15956098063207613f,
            0.13223857708684345f,
            0.12785243312062147f,
            0.11861930356184965f,
            0.11435320123090129f,
            0.11391060959721862f,
            0.11178258841934224f,
            0.10128511996830246f,
            0.10115235829868086f,
            0.09849286125692094f,
            0.09842802077054047f,
            0.0945966807143086f,
            0.09122469834949322f,
            0.08387971586673368f,
            0.07772803869637053f,
            0.07637165405384395f,
            0.07548224407581151f,
            0.07546730128106383f,
            0.0728765573692056f,
            0.07166911812787768f,
            0.07041507405675454f,
            0.06788840884428235f,
            0.06065426966888907f,
            0.06063656984816868f,
            0.06000860731833102f,
            0.05673045269829512f,
            0.05657467930833193f,
            0.05388625773570047f,
            0.05077436815706684f,
            0.03565166394257637f,
            0.023520171171979118f,
            0.021769244177421175f,
            0.021526504270217173f,
            0.020590604923703626f,
            0.014350576353742124f,
            0.01028945559343435f,
            0.007874723648818607f,
        };

        final float[] freq = new float[] {
             1.0f,
             -1.0f,
             3.0f,
             -2.0f,
             0.0f,
             2.0f,
             -3.0f,
             -4.0f,
             4.0f,
             -5.0f,
             5.0f,
             -6.0f,
             6.0f,
             7.0f,
             -8.0f,
             10.0f,
             8.0f,
             11.0f,
             13.0f,
             9.0f,
             -9.0f,
             -11.0f,
             21.0f,
             17.0f,
             14.0f,
             -16.0f,
             -14.0f,
             18.0f,
             16.0f,
             12.0f,
             -10.0f,
             -7.0f,
             -18.0f,
             15.0f,
             19.0f,
             -12.0f,
             -13.0f,
             25.0f,
             22.0f,
             23.0f,
             -19.0f,
             29.0f,
             26.0f,
             20.0f,
             -28.0f,
             31.0f,
             -20.0f,
             -21.0f,
             24.0f,
             -17.0f,
             -23.0f,
             -26.0f,
             28.0f,
             -31.0f,
             -24.0f,
             30.0f,
             32.0f,
             -36.0f,
             -30.0f,
             27.0f,
             -38.0f,
             -33.0f,
             35.0f,
             -27.0f,
             -29.0f,
             43.0f,
             33.0f,
             41.0f,
             -15.0f,
             39.0f,
             -41.0f,
             34.0f,
             -32.0f,
             -40.0f,
             36.0f,
             -39.0f,
             38.0f,
             -25.0f,
             -43.0f,
             -22.0f,
             -45.0f,
             40.0f,
             -42.0f,
             -34.0f,
             44.0f,
             -44.0f,
             42.0f,
             -35.0f,
             -37.0f,
             37.0f,
        };

        final float[] phase = new float[] { 
            0.2077607886449236f,
            1.8872028205385787f,
            -1.6370413670787356f,
            -1.0345512283060425f,
            -0.9358927907569734f,
            -1.9572851512145661f,
            -0.21368758522840225f,
            -2.384762473062791f,
            0.6570508980500064f,
            -2.93046936868902f,
            -1.7292344921091483f,
            -2.826531169057744f,
            -0.13280642604670628f,
            -2.143584214628542f,
            1.8535971848591155f,
            -0.46903694459984535f,
            1.9623929985940711f,
            -0.6450021567013753f,
            0.7002497708424327f,
            -0.7536283195321579f,
            1.1600306693847244f,
            -1.362293972659522f,
            2.237789848147991f,
            1.2300286220246943f,
            0.649964349306718f,
            0.10033021854637698f,
            1.2603108991831862f,
            0.4521821813719948f,
            -2.884679706241495f,
            2.1193318788503763f,
            1.7252768485417966f,
            0.968559462640539f,
            -0.9978935085899467f,
            0.26668214919466204f,
            1.1343169807696811f,
            1.6036502465235076f,
            2.912091899702024f,
            3.106261854635057f,
            1.7164116422598037f,
            -2.8720332355242353f,
            -2.893052050282665f,
            -2.494533818644382f,
            1.5770126256852819f,
            2.528796060994584f,
            2.051704732350875f,
            -0.8929169151451999f,
            -2.2491776482769796f,
            2.1854982229528677f,
            -2.354257361775848f,
            -0.8967487499705923f,
            1.0215869532297437f,
            -2.87273179245344f,
            -2.8551714498697947f,
            -0.4609087926750668f,
            -2.216406342882012f,
            3.1181739601401457f,
            -1.3012882303563513f,
            0.26629702654224563f,
            0.7101162367879483f,
            -2.1200522742607752f,
            -1.043891835675337f,
            -2.1040105999276038f,
            0.8091949453781984f,
            1.4117061486462872f,
            0.4333933006362994f,
            -3.1068679012238554f,
            -0.583245746144349f,
            2.241946532867679f,
            1.7775071541396192f,
            1.1891569264293465f,
            2.3665506441643225f,
            2.549879117696773f,
            0.542005061562853f,
            -1.765820125801071f,
            -1.925479557706551f,
            -2.1338549045401196f,
            -1.6361578966501116f,
            -1.2060633384389212f,
            1.1671585216063751f,
            -1.681173439088644f,
            0.08587396866696997f,
            0.1895388302417661f,
            3.0800459569138643f,
            0.44334312270010445f,
            -1.7218169432068842f,
            -1.1090432992090178f,
            2.891531199250868f,
            -1.4947575372167716f,
            -2.066732378326905f,
            -1.8167796902262794f,
        };

        final float amps1[] = new float[] {
            181.025434967296f,
            103.69405862972533f,
            68.35575150817438f,
            59.411438886963566f,
            53.89616588957561f,
            42.238249681145554f,
            34.720022974013844f,
            31.90099481475216f,
            29.896374108962927f,
            20.708432583838842f,
            18.362810483528644f,
            16.614221172158487f,
            14.193115088602513f,
            11.211802670704278f,
            8.159277174716529f,
            8.127907424306413f,
            7.8240787294955645f,
            7.299392810302043f,
            4.700123918501083f,
            4.5852594577297365f,
            4.553995653704598f,
            4.414807969027218f,
            4.383637427468741f,
            4.276804855526626f,
            3.4598444281242293f,
            3.37604657079527f,
            2.7234450743212633f,
            2.633441811531376f,
            2.339587081146975f,
            1.6944427455825781f,
            1.3839303255789404f,
            1.3474124749665992f,
            1.2528218218723308f,
            1.2041110887911426f,
            1.1676203608387987f,
            1.1266133948355908f,
            1.1038053024490309f,
            0.9202786648922316f,
            0.8301140945432596f,
            0.8281097713484735f,
            0.8060680537654755f,
            0.7741629897008973f,
            0.6857614459153579f,
            0.6855157228523242f,
            0.6381212936338458f,
            0.6369182496455413f,
            0.5733027158133565f,
            0.5578645103897164f,
            0.5437509136650374f,
            0.5289842671493036f,
            0.4959350756478461f,
            0.47771258077790973f,
            0.4758779740721983f,
            0.4249165266069964f,
            0.4245715332351801f,
            0.4214460898154198f,
            0.4193954867946844f,
            0.41740222778114316f,
            0.40228728074617764f,
            0.39934687521221274f,
            0.39011309624778867f,
            0.3612705292237162f,
            0.36019955267641396f,
            0.34613059801886453f,
            0.33423477244502875f,
            0.3309629948810954f,
            0.3100945245515357f,
            0.3033641704243967f,
            0.2433984561201099f,
            0.2407534110411121f,
            0.2374198753365175f,
            0.22254762646620174f,
            0.22185883138612078f,
            0.21856637969212023f,
            0.20271708664682328f,
            0.20147726673813343f,
            0.19989380350498853f,
            0.19935568554870428f,
            0.1791210237484605f,
            0.1303232538114093f,
            0.11533257827734636f,
            0.09287134051771588f,
            0.09224148245633519f,
            0.08942543305900412f,
            0.0869264516909838f,
            0.08386181664135485f,
            0.08249895455197336f,
            0.07603300070375556f,
            0.06363227596480142f,
            0.05677752126516614f,
        };

        final float[] freq1 = new float[] {
             1.0f,
             2.0f,
             -3.0f,
             -2.0f,
             4.0f,
             0.0f,
             -4.0f,
             -1.0f,
             3.0f,
             -6.0f,
             6.0f,
             -5.0f,
             9.0f,
             5.0f,
             8.0f,
             -9.0f,
             -7.0f,
             7.0f,
             -11.0f,
             -13.0f,
             12.0f,
             14.0f,
             -12.0f,
             -10.0f,
             10.0f,
             -14.0f,
             11.0f,
             -8.0f,
             19.0f,
             15.0f,
             -18.0f,
             13.0f,
             -20.0f,
             -19.0f,
             24.0f,
             17.0f,
             28.0f,
             -16.0f,
             16.0f,
             22.0f,
             -24.0f,
             23.0f,
             21.0f,
             -21.0f,
             25.0f,
             -17.0f,
             -15.0f,
             26.0f,
             -33.0f,
             29.0f,
             -35.0f,
             20.0f,
             -40.0f,
             -30.0f,
             41.0f,
             27.0f,
             33.0f,
             42.0f,
             -25.0f,
             -28.0f,
             37.0f,
             -37.0f,
             -29.0f,
             31.0f,
             36.0f,
             -27.0f,
             -36.0f,
             34.0f,
             40.0f,
             -43.0f,
             -32.0f,
             -31.0f,
             39.0f,
             43.0f,
             -26.0f,
             32.0f,
             -23.0f,
             -38.0f,
             35.0f,
             -22.0f,
             18.0f,
             44.0f,
             -42.0f,
             -39.0f,
             30.0f,
             -44.0f,
             -45.0f,
             -41.0f,
             -34.0f,
             38.0f,
            };

        final float[] phase1 = new float[] {
             2.205779633392398f,
             3.0609706610467486f,
             2.6158625702297704f,
             0.016290955602000556f,
             -1.6013406067098017f,
             1.4560930967447383f,
             2.9108814971452945f,
             -2.8373011960634926f,
             0.8545654671584848f,
             -2.4070073513107406f,
             -2.624656580891547f,
             0.5210770577351386f,
             -1.3529565403297859f,
             -1.3145374781423416f,
             -2.8365704853040006f,
             -2.547763324947151f,
             -2.294390586366907f,
             0.9669514940639051f,
             -1.7919392783817947f,
             -0.19132006173227145f,
             2.054730862088876f,
             -1.4001546401041538f,
             -2.589326023135132f,
             -0.43999705206033046f,
             -1.5506175732455576f,
             -1.752530431662722f,
             -2.711754123792571f,
             -1.0270011020683065f,
             -1.1680293235753254f,
             0.3563161626764666f,
             -0.1578398042929553f,
             -2.009319342837335f,
             2.8649611060524096f,
             -1.728196522277749f,
             -1.604103643503431f,
             2.471397195281675f,
             2.8672545157518003f,
             0.05624066552748769f,
             -3.093631289975648f,
             -1.9891774129760267f,
             -2.2510077955168972f,
             2.7012503586625773f,
             -1.7883408919559713f,
             1.4273321068520015f,
             -2.795984516595045f,
             3.0428575503262683f,
             -2.172501038841027f,
             -2.4677649115943026f,
             0.0057523906234071545f,
             -1.9582319870153244f,
             -2.6618753149429546f,
             2.045897325578448f,
             -3.05814938576691f,
             -1.6716942696485033f,
             -2.609188209202639f,
             -1.405041042948704f,
             3.0993140658932896f,
             -1.347821776218587f,
             -2.7452836784659587f,
             -0.1318779567067616f,
             -1.3774738115496463f,
             1.8369611973775184f,
             -1.9578540201579997f,
             -3.0474813158891085f,
             -2.5831490100051884f,
             -1.788949726476128f,
             1.5820422769671774f,
             -1.533762926921201f,
             2.768598679357589f,
             -2.093857888272537f,
             2.180669800286497f,
             1.3460519006403935f,
             -1.6417333222654549f,
             2.286378281539385f,
             2.254613741878935f,
             -0.6908751071361008f,
             -1.7291278679198088f,
             -1.0447697922180637f,
             0.9093321468702866f,
             -1.6050830862648282f,
             0.18788175290305542f,
             -3.097815795407868f,
             0.986106668848979f,
             -2.09125600746214f,
             -2.643807824338536f,
             -1.7049485340301618f,
             3.0483174274572966f,
             2.5146741429800543f,
             -0.7138576123345838f,
             -2.49641831543654f,
        };

        final float[] amps2 = new float[] {
            63.3222341515416f,
            47.20738096936088f,
            41.2721323800698f,
            39.091376064615346f,
            34.23267655675229f,
            30.036777283140523f,
            28.687967646680185f,
            18.134161968455256f,
            17.200533951396118f,
            17.06641866088537f,
            13.200085074276496f,
            12.258467692946915f,
            11.109330948181308f,
            9.227972895051233f,
            9.087011622832716f,
            8.719641571418766f,
            8.26587625815611f,
            7.635307924830268f,
            7.580838516015148f,
            7.571586605649376f,
            7.531416579242496f,
            7.36591315505693f,
            7.135056995091613f,
            7.005219979103429f,
            5.357430816847483f,
            5.205481702715689f,
            5.167285668982836f,
            5.047687387441944f,
            4.653771439629311f,
            4.617924818424361f,
            4.0534947671794175f,
            3.9190936425261307f,
            3.790485179454478f,
            3.7291310528664976f,
            3.542641628266334f,
            3.2678525265364033f,
            3.1855800009311275f,
            3.0700876466526164f,
            2.781006004969726f,
            2.7501075961329327f,
            2.661472347667962f,
            2.659649649676298f,
            2.6501273887095014f,
            2.6152271772537636f,
            2.5285975855023595f,
            2.4016760576077267f,
            2.3867679807055f,
            2.3386647973306838f,
            2.2978968947407146f,
            2.297751920704881f,
            2.2921158384441087f,
            2.1932953406785742f,
            2.18399818531834f,
            2.182039870355373f,
            2.1411416708411735f,
            2.124140273974298f,
            2.100063509490467f,
            2.0665012007622066f,
            2.064359097598247f,
            1.9206933054715984f,
            1.8359202769010239f,
            1.828779356251814f,
            1.8136415342882555f,
            1.614194490879706f,
            1.585417337509451f,
            1.5842802585541231f,
            1.527629354086448f,
            1.4893960696598258f,
            1.4818552015958997f,
            1.4714183328429262f,
            1.4257463318705221f,
            1.421244432155012f,
            1.3290992556006365f,
            1.3270579311544815f,
            1.2288221801155252f,
            1.1848716918753688f,
            1.135752885203584f,
            1.1104371162164766f,
            1.0899680547360815f,
            1.088728549616705f,
            1.0102501423842762f,
            0.8466108177514272f,
            0.8398104071808812f,
            0.8125848823552129f,
            0.7928833807772172f,
            0.7455725480465593f,
            0.7309118391287932f,
            0.5796619506398853f,
            0.3625740008489293f,
            0.12534980277372804f,
            };

    final float[] freq2 = new float[] {
         1.0f,
         -1.0f,
         3.0f,
         2.0f,
         0.0f,
         4.0f,
         -3.0f,
         -4.0f,
         5.0f,
         -2.0f,
         -10.0f,
         -7.0f,
         6.0f,
         7.0f,
         -5.0f,
         -13.0f,
         -6.0f,
         -11.0f,
         12.0f,
         11.0f,
         13.0f,
         16.0f,
         -16.0f,
         9.0f,
         -15.0f,
         -22.0f,
         14.0f,
         -17.0f,
         15.0f,
         8.0f,
         25.0f,
         20.0f,
         -14.0f,
         -8.0f,
         -21.0f,
         18.0f,
         10.0f,
         -23.0f,
         24.0f,
         -19.0f,
         -9.0f,
         27.0f,
         -20.0f,
         -25.0f,
         -33.0f,
         -12.0f,
         -34.0f,
         -24.0f,
         -28.0f,
         32.0f,
         30.0f,
         17.0f,
         33.0f,
         -45.0f,
         -31.0f,
         22.0f,
         42.0f,
         41.0f,
         21.0f,
         -18.0f,
         -43.0f,
         26.0f,
         -36.0f,
         -26.0f,
         29.0f,
         36.0f,
         44.0f,
         39.0f,
         -32.0f,
         -42.0f,
         -35.0f,
         28.0f,
         -29.0f,
         -44.0f,
         -37.0f,
         -30.0f,
         35.0f,
         40.0f,
         31.0f,
         -27.0f,
         37.0f,
         -40.0f,
         23.0f,
         43.0f,
         34.0f,
         -39.0f,
         38.0f,
         -41.0f,
         19.0f,
         -38.0f,
        };

    final float[] phase2 = new float[] {
        -2.100678819733969f,
        -1.1613927166581344f,
        0.3677109191174241f,
        2.8701258476475022f,
        -1.2300548573815908f,
        2.776545846632665f,
        2.38344446079473f,
        0.7736112688135003f,
        -1.7849108856611122f,
        0.45450771661130135f,
        -0.5597937712695347f,
        1.3719070612496922f,
        2.2577394648618103f,
        1.8654799772211779f,
        2.136750128198372f,
        -0.06229947612852633f,
        2.4765355869678998f,
        -1.770398571481798f,
        1.2578202705270805f,
        -2.8654191257345767f,
        2.16157674490601f,
        3.094514461949261f,
        0.3900586151122528f,
        2.6973326671560005f,
        2.235695787765136f,
        -0.8047135035902588f,
        -0.8276265092264579f,
        -1.513471990922243f,
        1.575690643065324f,
        -0.4699841212709458f,
        -3.073353316351475f,
        1.2730966814689497f,
        -1.5448119312250792f,
        -0.3039113007252909f,
        0.8426773747010384f,
        2.0355297742662275f,
        -2.614351950681727f,
        -3.1036005816645207f,
        1.8901481983312003f,
        -0.30203370232003296f,
        -0.530665917817503f,
        1.968936725328548f,
        -2.4981763915919717f,
        -0.933874490861974f,
        0.42669159315270433f,
        2.31241505089835f,
        -1.3776717362727564f,
        0.721291731897842f,
        -0.4805243801093464f,
        1.599666522812811f,
        2.917406960558015f,
        -1.0846010296014381f,
        -3.1338654794118885f,
        -0.29287013675226914f,
        -1.178832036574954f,
        -1.8336589074158145f,
        -2.7280419068751636f,
        1.8414559563954975f,
        2.8037391025972336f,
        2.7022917769983987f,
        -1.9904272509327228f,
        -0.6934109018826098f,
        0.021813880209827836f,
        -2.6860709916960994f,
        0.9634359425185623f,
        2.167566106007729f,
        1.9818647851852225f,
        3.106578467077562f,
        2.8080911705547003f,
        -0.37953540508032974f,
        2.5568071419511336f,
        -2.6429469406553765f,
        -1.5241439859614156f,
        1.78416827207759f,
        -1.3465453188127585f,
        0.34847208925005496f,
        0.9079840473867132f,
        -0.7124099646398931f,
        -1.3774454264754545f,
        1.0833233936357796f,
        -2.6959729302296256f,
        -1.6283298061896034f,
        0.22126390853931044f,
        -0.10091587409361867f,
        -2.3036939691041596f,
        -0.43532219899919655f,
        0.9862708122899063f,
        1.5569450929825115f,
        2.111905910876252f,
        0.02489504006540001f,
        };

    final float[] amps3 = new float[] {
        155.7342400883745f,
44.46629883479594f,
9.397341855815085f,
6.503178079924794f,
6.349901798625585f,
6.074995759175744f,
5.2299873497351435f,
2.655908777881252f,
2.33333766565002f,
2.2399936449359266f,
2.1263239791762527f,
2.001046296414906f,
1.9493994374025696f,
1.6422221871756117f,
1.4702207668092622f,
1.458818494940079f,
1.3409194249925547f,
1.3291638591805923f,
1.2661855001058484f,
1.1568190803148382f,
1.0792940118835477f,
1.0321780024756728f,
1.0259725860788713f,
0.9389450391575697f,
0.9251825380743544f,
0.728595891449711f,
0.694067012138461f,
0.6001916929992946f,
0.5950620873239584f,
0.5550607017316027f,
0.5392135200820172f,
0.5348215496687125f,
0.534035558878037f,
0.5335622024521085f,
0.5217635058519396f,
0.5166092195306994f,
0.50202202580406f,
0.4658714524021827f,
0.4479091377716394f,
0.43882428161697845f,
0.415583561446832f,
0.410193678398908f,
0.369539728111142f,
0.3654219338135365f,
0.3566444127652091f,
0.3517758997467228f,
0.33669787571139076f,
0.33323123798549986f,
0.3277442591004065f,
0.32707936561491424f,
0.314970612846329f,
0.30367445687114597f,
0.29939513171245874f,
0.2964639945246314f,
0.28541945226690046f,
0.27262884924539943f,
0.253981933197584f,
0.24891019972775882f,
0.24400256113140792f,
0.23672273789536383f,
0.19407385406723593f,
0.18122891494595378f,
0.17895228774795544f,
0.1782328214340759f,
0.16941727395770384f,
0.16357820915787302f,
0.16182845724219933f,
0.16181450715685833f,
0.16075659321089358f,
0.1590580493544436f,
0.15795272341086455f,
0.14149029139501101f,
0.1373536556305327f,
0.13505212207430012f,
0.12791050813757002f,
0.1277867239861853f,
0.12684246841582625f,
0.12122250325468263f,
0.09681568246759847f,
0.09257837214222872f,
0.09141385112824923f,
0.08421933195116178f,
0.07393549913383494f,
0.0697692841231188f,
0.06718813490806436f,
0.06221901217618973f,
0.05968122663594502f,
0.04655150621524589f,
0.03516948151057221f,
0.01867826704357455f,
        };

        final float[] freq3 = new float[] {
           1.0f,
0.0f,
2.0f,
5.0f,
-3.0f,
3.0f,
-2.0f,
8.0f,
-1.0f,
-6.0f,
4.0f,
7.0f,
-5.0f,
9.0f,
6.0f,
-7.0f,
-8.0f,
-4.0f,
11.0f,
-10.0f,
16.0f,
13.0f,
15.0f,
12.0f,
-9.0f,
31.0f,
17.0f,
-19.0f,
-12.0f,
10.0f,
30.0f,
-11.0f,
18.0f,
-23.0f,
-15.0f,
32.0f,
14.0f,
-17.0f,
-16.0f,
-20.0f,
-27.0f,
-13.0f,
-28.0f,
21.0f,
-18.0f,
-32.0f,
22.0f,
-14.0f,
29.0f,
-24.0f,
33.0f,
-21.0f,
26.0f,
34.0f,
19.0f,
-29.0f,
25.0f,
-35.0f,
24.0f,
36.0f,
20.0f,
-30.0f,
-26.0f,
-45.0f,
-31.0f,
-25.0f,
-22.0f,
-34.0f,
41.0f,
-33.0f,
38.0f,
-40.0f,
37.0f,
-36.0f,
-44.0f,
42.0f,
23.0f,
27.0f,
-37.0f,
-43.0f,
39.0f,
-41.0f,
28.0f,
35.0f,
-38.0f,
43.0f,
44.0f,
-42.0f,
-39.0f,
40.0f,
        };

        final float[] phase3 = new float[] {
          2.540412655318261f,
-0.1253082384135515f,
2.4297960717081795f,
1.6242880741723347f,
-2.1333553827866436f,
-2.6601176393617267f,
2.5778153366007035f,
0.2720032548646886f,
1.8734506572703207f,
0.4074098429844231f,
1.1624803244270878f,
1.473332306655293f,
-1.9567213671885015f,
0.28962490256604645f,
-2.0581752168628364f,
-0.021129377029414132f,
1.2934623247324621f,
1.177194214464715f,
0.9183887095588312f,
2.3890085195143826f,
2.3058506919113766f,
-0.6228234905673515f,
-2.0988963514354957f,
-0.8164859421399432f,
-0.397863181533376f,
1.0925059222917064f,
1.6811621687907183f,
2.1149777952271434f,
1.3697253210085205f,
2.864289427862932f,
2.105760364510961f,
0.4247427542504465f,
1.14612762385667f,
-2.5499175321316754f,
-0.5407305765821777f,
-0.1907454646257499f,
2.9551287606048873f,
0.3702173916276489f,
-2.0655029257538677f,
-2.248797812704653f,
0.10676398967147108f,
2.975122366646815f,
1.6785496074655473f,
2.985831489836134f,
0.25567088547618644f,
2.912592318959545f,
0.1463692829679692f,
-2.5841059467944003f,
2.630673474031067f,
-0.48979250462673385f,
-1.8078014364589305f,
1.96102621756951f,
-2.116853557712353f,
3.038837604522843f,
-1.7464022922117144f,
-1.2584231348236519f,
-1.8888032564688946f,
-1.1233316499667285f,
0.0725528119428763f,
-0.34400026806194656f,
0.044533968738690914f,
-0.3573326240803814f,
-0.014970125415765633f,
-0.831822498221992f,
0.7772708648944395f,
-2.361647878878986f,
1.548618975298313f,
-1.9227494007450663f,
-2.8757421759157857f,
-2.4828890142012745f,
-3.091109486362943f,
1.6851599085522577f,
-2.1883245179163833f,
0.19955727846095314f,
-1.380575083560141f,
0.744834849808684f,
0.1842199676195639f,
2.9008926959501533f,
0.6348652427460271f,
-2.840584154721319f,
3.0192046898338605f,
-2.0175915199050207f,
-0.6241534155461469f,
-0.4324370177588731f,
-0.8036896917602315f,
1.6354233470296815f,
-0.8453328275974056f,
-0.6264825228113441f,
2.4966076262319454f,
2.9396048457433435f,
            };
            
        if(bFirst != true) {

            // calculate the series
            Circle center = new Circle(0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

            switch (tapCount) {
                case 0:
                    center = new Circle(0.0f, 0.0f, 120.0f, (2.0f * (float)Math.PI * t), 0.0f);
                break;

                case 1:
                    center = new Circle(0.0f, 0.0f, 120.0f, (2.0f * (float)Math.PI * t), 0.0f)
                        .chainCircle(30.0f, (2.0f * (float)Math.PI * 5.0f * t), 0.0f);
                break;

                case 2:
                    center = new Circle(0.0f, 0.0f, 120.0f, (2.0f * (float)Math.PI * t), 0.0f)
                        .chainCircle(30.0f, (2.0f * (float)Math.PI * 4.0f * t), 0.0f);
                break;

                case 3:
                    center = new Circle(0.0f, 0.0f, 120.0f, (2.0f * (float)Math.PI * t), 0.0f)
                        .chainCircle(60.0f, (2.0f * (float)Math.PI * 4.0f * t), 0.0f)
                        .chainCircle(30.0f, -(2.0f * (float)Math.PI * 8.0f * t), 0.0f);
                break;

                case 4:
                    center = new Circle(0.0f, 0.0f, 120.0f, (2.0f * (float)Math.PI * t), 0.0f)
                        .chainCircle(60.0f, (-2.0f * (float)Math.PI * 3.0f * t), 0.0f);
                break;

                case 5:
                    center = new Circle(0.0f, 0.0f, 120.0f, (2.0f * (float)Math.PI * t), 0.0f)
                        .chainCircle(120.0f, (-2.0f * (float)Math.PI * t), 0.0f);
                break;

                case 6:
                    center = new Circle(0.0f, 0.0f, 120.0f, (2.0f * (float)Math.PI * t), 0.0f)
                        .chainCircle(120.0f, (-2.0f * (float)Math.PI * 3.0f * t), 0.0f);
                break;

                case 7:
                    center = new Circle(0.0f, 0.0f, amps[0], (2.0f * (float)Math.PI * (float)freq[0] * t), phase[0]);
                    for (int n = 1; n < 80; n++) {
                        center = center.chainCircle(amps[n], (2.0f * (float)Math.PI * (float)freq[n] * t), phase[n]);
                    }
                break;

                case 8:
                    center = new Circle(0.0f, 0.0f, amps1[0], (2.0f * (float)Math.PI * (float)freq1[0] * t), phase1[0]);
                    for (int n = 1; n < 80; n++) {
                        center = center.chainCircle(amps1[n], (2.0f * (float)Math.PI * (float)freq1[n] * t), phase1[n]);
                    }
                break;

                case 111:
                    center = new Circle(0.0f, 0.0f, amps2[0], (2.0f * (float)Math.PI * (float)freq2[0] * t), phase2[0]);
                    for (int n = 1; n < 80; n++) {
                        center = center.chainCircle(amps2[n], (2.0f * (float)Math.PI * (float)freq2[n] * t), phase2[n]);
                    }
                break;

                case 9:
                    center = new Circle(0.0f, 0.0f, amps3[0], (2.0f * (float)Math.PI * (float)freq3[0] * t), phase3[0]);
                    for (int n = 1; n < 80; n++) {
                        center = center.chainCircle(amps3[n], (2.0f * (float)Math.PI * (float)freq3[n] * t), phase3[n]);
                    }
                break;
            }
            
            Point result = center.calculate();

            // float zoomX1 = result.getX() - 40.0f;
            // float zoomX2 = result.getX() + 40.0f;
            // float zoomY1 = result.getY() - 40.0f;
            // float zoomY2 = result.getY() + 40.0f;

            // Matrix.orthoM(orthographicProjectionMatrix, 0,
            //         zoomX1 * ((float)3040 / (float)1440), zoomX2 * ((float)3040 / (float)1440),
            //         zoomY1, zoomY2,
            //         -300.0f, 300.0f);

            // do necessary matrix multiplication
            Matrix.multiplyMM(modelViewProjectionMatrix, 0,
                orthographicProjectionMatrix, 0,
                modelViewMatrix, 0);

            // send necessary matrices to shader in respective uniforms
            GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

            // bind with vao (this will avoid many binding to vbo)
            GLES32.glBindVertexArray(vao[0]);  


            // System.out.println("RTR: ###########index: " + index);

            if (limit) {

                // System.out.println("RTR: inside limit ++++++");
                for (int i = 0; i < 199; i++) {
                    // System.out.println("RTR: replacing index " + i + " --------------------");
                    // System.out.println("RTR: replacing " + points[(3 * i) + 0] + " with " + points[(3 * (i+1)) + 0]);
                    points[(3 * i) + 0] = points[(3 * (i+1)) + 0];
                    // System.out.println("RTR: replacing " + points[(3 * i) + 1] + " with " + points[(3 * (i+1)) + 1]);
                    points[(3 * i) + 1] = points[(3 * (i+1)) + 1];
                    // System.out.println("RTR: replacing " + points[(3 * i) + 2] + " with " + points[(3 * (i+1)) + 2]);
                    points[(3 * i) + 2] = points[(3 * (i+1)) + 2];
                    // System.out.println("RTR: ----------------------------------------------");
                }

                points[(3 * 199) + 0] = result.getX();
                points[(3 * 199) + 1] = result.getY();
                points[(3 * 199) + 2] = 0.0f;

            } else {
                points[(3 * index) + 0] = result.getX();
                points[(3 * index) + 1] = result.getY();
                points[(3 * index) + 2] = 0.0f;
            }

            // calculate the arrows
            for (int i = 0; i < 99; i++) {
                if (Circle.EndPoints[i] != null) {
                    points[600 + (i * 3) + 0] = Circle.EndPoints[i].getX();
                    points[600 + (i * 3) + 1] = Circle.EndPoints[i].getY();
                    points[600 + (i * 3) + 2] = 0.0f;
                } else {
                    // System.out.println("RTR: Arrow function found invalid index: " + i);
                }

            }

            // System.out.println("RTR: Added point: " + result);
            
            index++;
            if (index >= 200) {
                index = 0;
                limit = true;
            }

            // fill vertex buffer
            positionBuffer.put(points);

            // set the array at 0th position of buffer
            positionBuffer.position(0);

            //
            GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPos[0]);
            GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 300 * 3 * 4, positionBuffer, GLES32.GL_DYNAMIC_DRAW);
            GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

            // draw circle arrows
            if (bArrows) {
                GLES32.glLineWidth(2.0f);
                GLES32.glDrawArrays(GLES32.GL_LINE_STRIP, 200, Circle.EndPointsIndex);
            }

            // draw output of circles
            GLES32.glLineWidth(4.0f);
            if (!limit) {
                GLES32.glDrawArrays(GLES32.GL_LINE_STRIP, 0, index);
                // System.out.println("RTR: Drawing from 0 to " + index);
            } else {
                GLES32.glDrawArrays(GLES32.GL_LINE_STRIP, 0, 199);
                // System.out.println("RTR: Drawing from 0 to " + 199);
            }
        }

        // unbind vao
        GLES32.glBindVertexArray(0);

        // unuse program
        GLES32.glUseProgram(0);
        requestRender();  // ~ swapBuffers

        t += 0.005f;
        Circle.ResetEndPoints();
    }

    private void uninitialize() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vboCol[0] != 0) {
            GLES32.glDeleteBuffers(1, vboCol, 0);
            vboCol[0] = 0;
        }

        if (vboPos[0] != 0) {
            GLES32.glDeleteBuffers(1, vboPos, 0);
            vboPos[0] = 0;
        }

        if (vao[0] != 0) {
            GLES32.glDeleteVertexArrays(1, vao, 0);
            vao[0] = 0;
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
}



