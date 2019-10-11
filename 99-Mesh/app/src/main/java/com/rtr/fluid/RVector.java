package com.rtr.fluid;

// vmath like implementation for float vectors

public class RVector {

    private float x, y, z;

    public RVector() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
    }

    public RVector(float f) {
        x = f;
        y = f;
        z = f;
    }

    public RVector(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public RVector invert() {
        return new RVector(-this.x, -this.y, -this.z);
    }

    public RVector add(RVector v) {
        return new RVector(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public RVector sub(RVector v) {
        return this.add(v.invert());
    }




}