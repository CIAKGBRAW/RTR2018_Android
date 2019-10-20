package com.rtr.rotatingArm;

public class Stack {

    private float[][] _stack;
    private int sp;

    public Stack() {
        _stack = new float[16][10];
        sp = -1;
    }

    public void resetStack() {
        sp = -1;
    }

    public void push(float[] data) {
        if (sp == 10 - 1)
        {
            System.out.println("STACK OVERFLOW\n");
        }
        else
        {
            sp++;
            _stack[sp] = data;
        }
    }

    public float[] pop() {
        float[] data = new float[16];
        if (sp > -1)
        {
            data = _stack[sp];
            sp--;
        }
        else
        {
            System.out.println("STACK UNDERFLOW\n");
        }
        return data;
    }

    float[] peek() {
        if (sp > -1)
        {
            return _stack[sp];
        }
        return new float[16];
    }


}