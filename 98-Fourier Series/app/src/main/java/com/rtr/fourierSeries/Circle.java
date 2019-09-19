package com.rtr.fourierSeries;

import java.lang.Math;

public class Circle {

    private float x,y;
    private float radius;
    private float theta;
    private float phase;

    public static Point[] EndPoints = new Point[200];
    public static int EndPointsIndex = 1;

    public Circle(float x, float y, float radius, float theta, float phase) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.theta = theta;
        this.phase = phase;

        EndPoints[0] = new Point(0.0f, 0.0f);
    }

    public Circle chainCircle(float r, float t, float p) {
        Point endPoint = this.calculate();
        return new Circle(endPoint.getX(), endPoint.getY(), r, t, p);
    }

    public Point calculate() {

        // calculate the center of new circle, 
        // which will be at the end of this circle
        float x = this.x + (this.radius * (float)Math.cos(this.theta + this.phase));
        float y = this.y + (this.radius * (float)Math.sin(this.theta + this.phase));

        Point point = new Point(x, y);
        EndPoints[EndPointsIndex] = point;
        EndPointsIndex++;

        return point;
    }

    public static void ResetEndPoints() {
        EndPoints[0] = new Point(0.0f, 0.0f);
        EndPointsIndex = 1;
    }


}