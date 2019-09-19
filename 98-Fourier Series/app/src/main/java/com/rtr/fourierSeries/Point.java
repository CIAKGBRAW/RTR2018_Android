package com.rtr.fourierSeries;

public class Point {
    
    private float x, y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String toString() {
        return "X: " + x + " Y: " + y;
    }
}