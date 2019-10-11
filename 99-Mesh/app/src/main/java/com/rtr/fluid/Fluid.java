package com.rtr.fluid;


import android.opengl.Matrix; // Matrix

// From book: Mathematics for 3D game programming and Computer Graphics (3rd Ed)
// Topic: 15.1 Fluid Simulation
public class Fluid {

    private int width;
    private int height;

    private float[][] buffer0;
    private float[][] buffer1;

    private int renderBuffer;

    private float[][] normal;
    private float[][] tangent;

    private float k1, k2, k3;

    public Fluid(int n, int m, float d, float t, float c, float mu) {
        width = n;
        height = m;
        int count = m*n;

        buffer0 = new float[count][3];
        buffer1 = new float[count][3];
        renderBuffer = 0;

        normal = new float[count][3];
        tangent = new float[count][3];

        // precompute constants for eqn 15.25
        float f1 = c*c*t*t/(d*d);
        float f2 = 1.0f/(mu*t+2);

        k1 = (4.0f-8.0f*f1)*f2;
        k2 = (mu*t-2.0f)*f2;
        k3 = 2.0f*f1*f2;

        // initialize buffers
        int a = 0;
        for(int j = 0; j < m; j++) {
            float y = d*j;
            for(int i = 0; i < n; i++) {
                buffer0[a][0] = d*i;
                buffer0[a][1] = y;
                buffer0[a][2] = 0.0f;

                buffer1[a][0] = d*i;
                buffer1[a][1] = y;
                buffer1[a][2] = 0.0f;

                normal[a][0] = 0.0f;
                normal[a][1] = 0.0f;
                normal[a][2] = 2.0f*d;

                tangent[a][0] = 2.0f*d;
                tangent[a][1] = 0.0f;
                tangent[a][2] = 0.0f;
                a++;
            }
        }
    }

    public void evaluate() {
        // System.out.println("RTR: Evaluting...");

        // apply eqn 15.25
        for(int j = 1; j < height - 1; j++) {
            System.out.println("RTR: j: " + j);

            int curIdx = j*width;
            System.out.println("RTR: curIdx: " + curIdx);
            
            if(renderBuffer == 0) {
                System.out.println("RTR: renderbuffer0");
                for(int i = 0; i < width - 1; i++) {
                    buffer1[curIdx+i][2] = k1*buffer0[curIdx+i][2] + k2*buffer1[curIdx+i][2] + 
                        k3*(buffer0[curIdx+i+1][2] + buffer0[curIdx+i-1][2] + buffer0[curIdx+i+width][2] + buffer0[curIdx+i-width][2]);
                        System.out.println("`````````````````````` evaluate0 " + buffer0[curIdx+i][2]);
                }

            } else {
                System.out.println("RTR: renderbuffer1");
                for(int i = 0; i < width - 1; i++) {
                    buffer0[curIdx+i][2] = k1*buffer1[curIdx+i][2] + k2*buffer0[curIdx+i][2] + 
                        k3*(buffer1[curIdx+i+1][2] + buffer1[curIdx+i-1][2] + buffer1[curIdx+i+width][2] + buffer1[curIdx+i-width][2]);
                       System.out.println("`````````````````````` evaluate1 " + buffer1[curIdx][2]);
                }
            }
        }

        // swap buffers
        renderBuffer = 1 - renderBuffer;

        // calculate normals and tangents

    }

    public float[] getResult() {
        float[] coords = new float[width*height*3];
        if(renderBuffer == 0) {
            for(int i = 0; i < width*height; i++) {
                coords[(i*3)+0] = buffer0[i][0];
                coords[(i*3)+1] = buffer0[i][1];
                coords[(i*3)+2] = buffer0[i][2];
            }
        } else {
            for(int i = 0; i < width*height; i++) {
                coords[(i*3)+0] = buffer1[i][0];
                coords[(i*3)+1] = buffer1[i][1];
                coords[(i*3)+2] = buffer1[i][2];
            }
        }
        // System.out.println("RTR: Returning buffer: " + renderBuffer);
        return coords;
    }

}