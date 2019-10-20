package com.rtr.rotatingArm;

public class Sphere {
    
    public static int GenerateSphereCoords(float r, int n, float[] pos, float[] norm, float[] tex) {
        int iNoOfCoords = 0;
        int i, j;
        double phi1, phi2, theta, s, t;
        float ex, ey, ez, px, py, pz;

        // *ppos = (float *)malloc(3 * sizeof(float) * n * (n + 1) * 2);
        // *pnorm = (float *)malloc(3 * sizeof(float) * n * (n + 1) * 2);
        // *ptex = (float *)malloc(2 * sizeof(float) * n * (n + 1) * 2);

        //iNoOfCoords = n * (n + 1);

        for (j = 0; j < n; j++) {
            phi1 = j * Math.PI * 2 / n;
            phi2 = (j + 1) * Math.PI * 2 / n;

            //fprintf(gpFile, "phi1 [%g]...\n", phi1);
            //fprintf(gpFile, "phi2 [%g]...\n", phi2);

            for (i = 0; i <= n; i++) {
                theta = i * Math.PI / n;

                ex = (float)(Math.sin(theta)) * (float)(Math.cos(phi2));
                ey = (float)(Math.sin(theta)) * (float)(Math.sin(phi2));
                ez = (float)(Math.cos(theta));
                px = r * ex;
                py = r * ey;
                pz = r * ez;

                //glNormal3f(ex, ey, ez);
                norm[(iNoOfCoords * 3) + 0] = ex;
                norm[(iNoOfCoords * 3) + 1] = ey;
                norm[(iNoOfCoords * 3) + 2] = ez;

                s = phi2 / (Math.PI * 2);   // column
                t = 1 - (theta / Math.PI);  // row
                //glTexCoord2f(s, t);
                tex[(iNoOfCoords * 2) + 0] = (float)s;
                tex[(iNoOfCoords * 2) + 1] = (float)t;

                //glVertex3f(px, py, pz);
                pos[(iNoOfCoords * 3) + 0] = px;
                pos[(iNoOfCoords * 3) + 1] = py;
                pos[(iNoOfCoords * 3) + 2] = pz;

                /*fprintf(gpFile, "pos[%d]...\n", (iNoOfCoords * 3) + 0);
                fprintf(gpFile, "pos[%d]...\n", (iNoOfCoords * 3) + 1);
                fprintf(gpFile, "pos[%d]...\n", (iNoOfCoords * 3) + 2);*/

                ex = (float)(Math.sin(theta)) * (float)(Math.cos(phi1));
                ey = (float)(Math.sin(theta)) * (float)(Math.sin(phi1));
                ez = (float)(Math.cos(theta));
                px = r * ex;
                py = r * ey;
                pz = r * ez;

                //glNormal3f(ex, ey, ez);
                norm[(iNoOfCoords * 3) + 3] = ex;
                norm[(iNoOfCoords * 3) + 4] = ey;
                norm[(iNoOfCoords * 3) + 5] = ez;

                s = phi1 / (Math.PI * 2);   // column
                t = 1 - (theta / Math.PI);  // row
                //glTexCoord2f(s, t);
                tex[(iNoOfCoords * 2) + 2] = (float)        s;
                tex[(iNoOfCoords * 2) + 3] = (float)        t;

                //glVertex3f(px, py, pz);
                pos[(iNoOfCoords * 3) + 3] = px;
                pos[(iNoOfCoords * 3) + 4] = py;
                pos[(iNoOfCoords * 3) + 5] = pz;

                /*fprintf(gpFile, "pos[%d]...\n", (iNoOfCoords * 3) + 3);
                fprintf(gpFile, "pos[%d]...\n", (iNoOfCoords * 3) + 4);
                fprintf(gpFile, "pos[%d]...\n", (iNoOfCoords * 3) + 5);*/

                iNoOfCoords += 2;
            }
        }
        return iNoOfCoords;
    }
}