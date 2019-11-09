package com.example.temp;

//Added by me


import android.opengl.GLSurfaceView;
import android.opengl.GLES32;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.content.Context;
import android.view.Gravity;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.opengl.Matrix;
import java.lang.Math;


public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer,OnGestureListener,OnDoubleTapListener{
    
    private static Context context;
    private GestureDetector gestureDetector;
    private int gVertexShaderObject;
    private int gFragmentShaderObject;
    private int shaderProgramObject;
    
    private int[] vao_line1=new int[1];
    private int[] vbo_line1_position=new int[1];
    private int[] vbo_line1_color=new int[1];
    
    private int[] vao_line2=new int[1];
    private int[] vbo_line2_position=new int[1];
    private int[] vbo_line2_color=new int[1];
    
    private int[] vao_line3=new int[1];
    private int[] vbo_line3_position=new int[1];
    private int[] vbo_line3_color=new int[1];

    private int[] vao_circle=new int[1];
    private int[] vbo_circle_position=new int[1];
    private int[] vbo_circle_color=new int[1];
    
    private int[] vao_line=new int[1];
    private int[] vbo_line_position=new int[1];
    private int[] vbo_line_color=new int[1];
    
    private int mvpUniform;
    private float[] perspectiveProjectionMatrix=new float[16];
    float angle_triangle=0.0f;
    float angle_rectangle=0.0f;
    FloatBuffer positionBuffer1;
    public GLESView(Context drawingContext){
        super(drawingContext);
        context=drawingContext;
        gestureDetector=new GestureDetector(drawingContext,this,null,false);
        gestureDetector.setOnDoubleTapListener(this);
        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int eventaction=event.getAction();
        if(!gestureDetector.onTouchEvent(event))
            super.onTouchEvent(event);
        return(true);
    }
    
    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        return(true);
    }
    
    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        return(true);
    }
    
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        return(true);
    }
    
    @Override
    public boolean onDown(MotionEvent e)
    {
        return(true);
    }
    @Override
    public boolean onFling(MotionEvent e1,MotionEvent e2,float velocityx,float velocityy)
    {
        
        return(true);
    }
    @Override
    public void onLongPress(MotionEvent e)
    {
    }
    @Override
    public boolean onScroll(MotionEvent e1,MotionEvent e2,float distancex,float distancey)
    {
        uninitialize();
        System.exit(0);
        return(true);
    }
    @Override
    public void onShowPress(MotionEvent e)
    {
        
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return(true);
    }
    @Override
    public void onSurfaceCreated(GL10 gl,EGLConfig config)
    {
        String version=gl.glGetString(GL10.GL_VERSION);
        System.out.println("RTR"+version);
        String version1=gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
        System.out.println("RTR"+version1);
        initialize();
    }
    @Override
    public void onSurfaceChanged(GL10 unused,int width,int height)
    {
        resize(width,height);
    }
    @Override
    public void onDrawFrame(GL10 unused)
    {
        
        display();
    }
    
    //Our custom methods
    
    private void initialize()
    {

        gVertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        final String vertexShaderSourceCode=
        String.format
        (
            "#version 320 es"+
            "\n"+
            "in vec4 vPosition;"+
            "in vec4 vColor;"+
            "uniform mat4 u_mvp_matrix;"+
            "out vec4 out_color;"+
            "void main(void)"+
            "{"+
            "gl_Position=u_mvp_matrix * vPosition;"+
            "out_color = vec4(1.0);"+
            "}"
        );

        GLES32.glShaderSource(gVertexShaderObject,vertexShaderSourceCode);
        GLES32.glCompileShader(gVertexShaderObject);
                


        //Error checking
        int[] iShaderCompileStatus=new int[1];
        int[] iInfoLogLength=new int[1];
        String szInfoLog=null;
        GLES32.glGetShaderiv(gVertexShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

        if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(gVertexShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
            if(iInfoLogLength[0]>0)
            {
                szInfoLog=GLES32.glGetShaderInfoLog(gVertexShaderObject);
                System.out.println("iRTR:VertexShaderObject"+szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }
        

        gFragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        final String fragmentShaderSourceCode=
        String.format
        (
            "#version 320 es"+
            "\n"+
            "precision highp float;"+
            "in vec4 out_color;"+
            "out vec4 FragColor;"+
            "void main(void)"+
            "{"+
            "FragColor=vec4(1.0,0.0,1.0,1.0);"+
            "}" 
        );

        GLES32.glShaderSource(gFragmentShaderObject,fragmentShaderSourceCode);
        GLES32.glCompileShader(gFragmentShaderObject);
                


        //Error checking
        iShaderCompileStatus[0]=0;
        iInfoLogLength[0]=0;
        szInfoLog=null;
        GLES32.glGetShaderiv(gFragmentShaderObject,GLES32.GL_COMPILE_STATUS,iShaderCompileStatus,0);

        if(iShaderCompileStatus[0]==GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(gFragmentShaderObject,GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);
            if(iInfoLogLength[0]>0)
            {
                szInfoLog=GLES32.glGetShaderInfoLog(gFragmentShaderObject);
                System.out.println("iRTR:FragmentShaderObject"+szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }
        
        shaderProgramObject=GLES32.glCreateProgram();
        GLES32.glAttachShader(shaderProgramObject,gVertexShaderObject);
        GLES32.glAttachShader(shaderProgramObject,gFragmentShaderObject);

        GLES32.glBindAttribLocation(shaderProgramObject,GLESMacros.AMC_ATTRIBUTE_POSITION,"vPosition"); 
        GLES32.glLinkProgram(shaderProgramObject);
        
        int[] iShaderLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfoLog = null;

        GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_LINK_STATUS,iShaderLinkStatus,0);

    if (iShaderLinkStatus[0] == GLES32.GL_FALSE) {
        GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_INFO_LOG_LENGTH,iInfoLogLength,0);

        if (iInfoLogLength[0] > 0) {
            szInfoLog=GLES32.glGetProgramInfoLog(shaderProgramObject);

            System.out.println("iRTR:shaderProgramObject"+szInfoLog);
            uninitialize();
            System.exit(0); 
            
        }
    }

        mvpUniform=GLES32.glGetUniformLocation(shaderProgramObject,"u_mvp_matrix");
        
        
        GLES32.glGenVertexArrays(1,vao_circle,0);
        GLES32.glBindVertexArray(vao_circle[0]);
        
        float[] circlePos=new float[6284*3];


        float angle = 0.0f; float radius = 1.0f;

        int index = 0;


        for (angle = 0.0f; angle < 2 * 3.14159265; angle = angle + 0.001f) {
        circlePos[index++] = (float)Math.cos(angle)*radius;
        circlePos[index++] = (float)Math.sin(angle)*radius;
        circlePos[index++] = 0.0f;
        //indexPos++;
        }

        System.out.println("RTR: circle points done..");
        
        GLES32.glGenBuffers(1,vbo_circle_position,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_circle_position[0]);
        ByteBuffer byteBuffer2=ByteBuffer.allocateDirect(6284*3*4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        FloatBuffer positionBuffer1=byteBuffer2.asFloatBuffer();
        positionBuffer1.put(circlePos);
        positionBuffer1.position(0);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,6284*3*4,positionBuffer1,GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);
        GLES32.glEnableVertexAttribArray(GLESMacros.AMC_ATTRIBUTE_POSITION);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
        
        
        GLES32.glBindVertexArray(0);

        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);
        Matrix.setIdentityM(perspectiveProjectionMatrix,0);

        GLES32.glClearColor(0.0f,0.0f,0.0f,1.0f);
    }
    private void resize(int width,int height)
    {
        if(height==0)
            height=1;
        GLES32.glViewport(0,0,width,height);
        Matrix.perspectiveM(perspectiveProjectionMatrix,0,45.0f, (float)width / (float)height, 0.1f, 100.0f);
    }
    private void display()
    {   
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT|GLES32.GL_DEPTH_BUFFER_BIT);
        GLES32.glUseProgram(shaderProgramObject);
        float[] modelViewProjectionMatrix=new float[16];
        float[] modelViewMatrix=new float[16];
        
        
        //triangle
        Matrix.setIdentityM(modelViewProjectionMatrix,0);
        Matrix.setIdentityM(modelViewMatrix,0);
        //Matrix.setIdentityM(rotationMatrix,0);
        Matrix.translateM(modelViewMatrix,0,-1.5f,0.0f,-6.0f);
        Matrix.rotateM(modelViewMatrix,0,angle_triangle,0.0f,1.0f,0.0f);
        Matrix.multiplyMM(modelViewProjectionMatrix,0,perspectiveProjectionMatrix,0,modelViewMatrix,0);
        GLES32.glUniformMatrix4fv(mvpUniform,1,false,modelViewProjectionMatrix,0);
        /*GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,vbo_circle_position[0]);
        ByteBuffer byteBuffer1=ByteBuffer.allocateDirect(18849);
        byteBuffer1.order (ByteOrder.nativeOrder());
        FloatBuffer positionBuffer1=byteBuffer1.asFloatBuffer();
        positionBuffer1.put(circlePos);
        positionBuffer1.position(0);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,18849,positionBuffer1,GLES32.GL_DYNAMIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.AMC_ATTRIBUTE_POSITION,3,GLES32.GL_FLOAT,false,0,0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);*/
        GLES32.glBindVertexArray(vao_circle[0]);
        GLES32.glDrawArrays(GLES32.GL_POINTS, 0, 6283);
        GLES32.glBindVertexArray(0);
        
        GLES32.glUseProgram(0);

        requestRender();
    }
    private void Update()
    {
        if(angle_triangle<=360.0f)
        {
            angle_triangle=angle_triangle+5.0f;
        }
        else
        {
            angle_triangle=0.0f;
        }
        if(angle_rectangle<=360.0f)
        {
            angle_rectangle=angle_rectangle+5.0f;
        }
        else
        {
            angle_rectangle=0.0f;
        }
    }
    private void uninitialize()
    {
        if (vbo_circle_position[0]!=0) {
            GLES32.glDeleteBuffers(1,vbo_circle_position,0);
            vbo_circle_position[0] = 0;
        }
        if (vao_circle[0]!=0) {
            GLES32.glDeleteVertexArrays(1,vao_circle,0);
            vao_circle[0] = 0;
        }
        if (shaderProgramObject != 0) {
            int[] shaderCount=new int[1];

            GLES32.glUseProgram(shaderProgramObject);
            
            GLES32.glGetProgramiv(shaderProgramObject,GLES32.GL_ATTACHED_SHADERS,shaderCount,0);    
    
            if (shaderCount[0]!=0) {
                
                int shaderNumber;
                int[] shaders = new int[shaderCount[0]];

                GLES32.glGetAttachedShaders(shaderProgramObject,shaderCount[0],shaderCount,0,shaders,0);
    
                for (shaderNumber = 0; shaderNumber < shaderCount[0]; shaderNumber++) {
                    GLES32.glDetachShader(shaderProgramObject, shaders[shaderNumber]);
                    GLES32.glDeleteShader(shaders[shaderNumber]);
                    shaders[shaderNumber] = 0;
                }
            }
            GLES32.glDeleteProgram(shaderProgramObject);
            shaderProgramObject = 0;
            GLES32.glUseProgram(0);
        }   
    }
}
