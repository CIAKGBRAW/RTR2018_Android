package com.rtr.rotatingArm;

public class Mat4 {

    private float[] data;

    public Mat4() {
        data = new float[16];
    }

    public Mat4(float[] _data) {
        data = new float[16];
        for(int i = 0; i < 16; i++)
            data[i] = _data[i];
    }

    public float[] get() {
        return data;
    }

    public void set(float[] _data) {
        for(int i = 0; i < 16; i++)
            data[i] = _data[i];
    }

}

// public class Stack {

//     private float[][] _stack;
//     private int sp;

//     public Stack() {
//         _stack = new float[10][16];
//         sp = -1;
//     }

//     public void resetStack() {
//         sp = -1;
//     }

//     public void push(float[] data) {
//         if (sp == 10 - 1)
//         {
//             System.out.println("RTR: STACK OVERFLOW\n");
//         }
//         else
//         {
//             sp++;
//             _stack[sp] = data;
//         }
//     }

//     public float[] pop() {
//         float[] data = new float[16];
//         if (sp > -1)
//         {
//             data = _stack[sp];
//             sp--;
//         }
//         else
//         {
//             System.out.println("RTR: STACK UNDERFLOW\n");
//         }
//         System.out.println("RTR: STACK POP: \n");

//         for(int i = 0; i < 16; i++) {
//             System.out.println(data[i]);
//         }

//         return data;
//     }

//     float[] peek() {
//         if (sp > -1) {
//             System.out.println("RTR: STACK PEEK: \n");
//             float[] data = _stack[sp];
//             for(int i = 0; i < 16; i++) {
//                 System.out.println(data[i]);
//             }

//             return _stack[sp];
//         }

//         return new float[16];
//     }


// }