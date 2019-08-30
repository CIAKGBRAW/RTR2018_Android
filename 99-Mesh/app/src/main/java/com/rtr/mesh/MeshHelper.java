package com.rtr.mesh;

import java.nio.ByteBuffer;   // ByteBuffer
import java.nio.ByteOrder;    // ByteOrder
import java.nio.FloatBuffer;  // FloatBuffer

import android.opengl.Matrix; // Matrix


public class MeshHelper {

    public static float[] generateMeshCoords(float base, float delta) {
        // 100x100 points
        float[] coords = new float[100*100*2*3];

        float xbase = base;
        float zbase = base;
        int n = 0;

        for(int i = 0; i < 100; i++) {

            for(int j = 0; j < 100; j++) {
                coords[n + 0] = xbase;
                coords[n + 1] = 0.0f;
                coords[n + 2] = zbase + (delta*j);
                System.out.println("RTR: " + n + " " + coords[n] + " " + coords[n+1] + " " + coords[n+2]);
                n += 3;

                coords[n + 0] = xbase + delta;
                coords[n + 1] = 0.0f;
                coords[n + 2] = zbase + (delta*j);
                System.out.println("RTR: " + n + " " + coords[n] + " " + coords[n+1] + " " + coords[n+2]);
                n += 3;
            }
            xbase += delta;
        }
        return coords;
    }

}
