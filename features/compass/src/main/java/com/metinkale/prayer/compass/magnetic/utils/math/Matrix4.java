/*
 * Copyright (c) 2013-2023 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayer.compass.magnetic.utils.math;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Encapsulates a <a href="http://en.wikipedia.org/wiki/Row-major_order">column
 * major</a> 4 by 4 matrix. Like the {@link Vector3} class it allows the
 * chaining of methods by returning a reference to itself. For example:
 * <p/>
 * <pre>
 * Matrix4 mat = new Matrix4().trn(position).mul(camera.combined);
 * </pre>
 *
 * @author badlogicgames@gmail.com
 */
public class Matrix4 implements Serializable {
    static final int M00 = 0;// 0;
    static final int M01 = 4;// 1;
    static final int M02 = 8;// 2;
    static final int M03 = 12;// 3;
    static final int M10 = 1;// 4;
    static final int M11 = 5;// 5;
    static final int M12 = 9;// 6;
    static final int M13 = 13;// 7;
    static final int M20 = 2;// 8;
    static final int M21 = 6;// 9;
    static final int M22 = 10;// 10;
    static final int M23 = 14;// 11;
    static final int M30 = 3;// 12;
    static final int M31 = 7;// 13;
    static final int M32 = 11;// 14;
    static final int M33 = 15;// 15;
    private static final long serialVersionUID = -2717655254359579617L;
    final float[] val = new float[16];
    private final float[] tmp = new float[16];

    public Matrix4() {
        val[M00] = 1f;
        val[M11] = 1f;
        val[M22] = 1f;
        val[M33] = 1f;
    }


    /**
     * Sets the matrix to the given matrix as a float array. The float array
     * must have at least 16 elements; the first 16 will be copied.
     *
     * @param values The matrix, in float form, that is to be copied. Remember that
     *               this matrix is in <a
     *               href="http://en.wikipedia.org/wiki/Row-major_order">column
     *               major</a> order.
     * @return This matrix for the purpose of chaining methods together.
     */
    @NonNull
    public Matrix4 set(@NonNull float[] values) {
        System.arraycopy(values, 0, val, 0, val.length);
        return this;
    }

    @NonNull
    public float[] getValues() {
        return val;
    }

    /**
     * Multiplies this matrix with the given matrix, storing the result in this
     * matrix. For example:
     * <p/>
     * <pre>
     * A.mul(B) results in A := AB.
     * </pre>
     *
     * @param matrix The other matrix to multiply by.
     * @return This matrix for the purpose of chaining operations together.
     */
    @NonNull
    public Matrix4 mul(@NonNull Matrix4 matrix) {
        // mul(val, matrix.val);
        // return this;
        return mul_java(matrix);
    }

    /**
     * Multiplies this matrix with the given matrix, storing the result in this
     * matrix.
     *
     * @param matrix The other matrix
     * @return This matrix for chaining.
     */
    @NonNull
    private Matrix4 mul_java(@NonNull Matrix4 matrix) {
        tmp[M00] = (val[M00] * matrix.val[M00]) + (val[M01] * matrix.val[M10]) + (val[M02] * matrix.val[M20]) + (val[M03] * matrix.val[M30]);
        tmp[M01] = (val[M00] * matrix.val[M01]) + (val[M01] * matrix.val[M11]) + (val[M02] * matrix.val[M21]) + (val[M03] * matrix.val[M31]);
        tmp[M02] = (val[M00] * matrix.val[M02]) + (val[M01] * matrix.val[M12]) + (val[M02] * matrix.val[M22]) + (val[M03] * matrix.val[M32]);
        tmp[M03] = (val[M00] * matrix.val[M03]) + (val[M01] * matrix.val[M13]) + (val[M02] * matrix.val[M23]) + (val[M03] * matrix.val[M33]);
        tmp[M10] = (val[M10] * matrix.val[M00]) + (val[M11] * matrix.val[M10]) + (val[M12] * matrix.val[M20]) + (val[M13] * matrix.val[M30]);
        tmp[M11] = (val[M10] * matrix.val[M01]) + (val[M11] * matrix.val[M11]) + (val[M12] * matrix.val[M21]) + (val[M13] * matrix.val[M31]);
        tmp[M12] = (val[M10] * matrix.val[M02]) + (val[M11] * matrix.val[M12]) + (val[M12] * matrix.val[M22]) + (val[M13] * matrix.val[M32]);
        tmp[M13] = (val[M10] * matrix.val[M03]) + (val[M11] * matrix.val[M13]) + (val[M12] * matrix.val[M23]) + (val[M13] * matrix.val[M33]);
        tmp[M20] = (val[M20] * matrix.val[M00]) + (val[M21] * matrix.val[M10]) + (val[M22] * matrix.val[M20]) + (val[M23] * matrix.val[M30]);
        tmp[M21] = (val[M20] * matrix.val[M01]) + (val[M21] * matrix.val[M11]) + (val[M22] * matrix.val[M21]) + (val[M23] * matrix.val[M31]);
        tmp[M22] = (val[M20] * matrix.val[M02]) + (val[M21] * matrix.val[M12]) + (val[M22] * matrix.val[M22]) + (val[M23] * matrix.val[M32]);
        tmp[M23] = (val[M20] * matrix.val[M03]) + (val[M21] * matrix.val[M13]) + (val[M22] * matrix.val[M23]) + (val[M23] * matrix.val[M33]);
        tmp[M30] = (val[M30] * matrix.val[M00]) + (val[M31] * matrix.val[M10]) + (val[M32] * matrix.val[M20]) + (val[M33] * matrix.val[M30]);
        tmp[M31] = (val[M30] * matrix.val[M01]) + (val[M31] * matrix.val[M11]) + (val[M32] * matrix.val[M21]) + (val[M33] * matrix.val[M31]);
        tmp[M32] = (val[M30] * matrix.val[M02]) + (val[M31] * matrix.val[M12]) + (val[M32] * matrix.val[M22]) + (val[M33] * matrix.val[M32]);
        tmp[M33] = (val[M30] * matrix.val[M03]) + (val[M31] * matrix.val[M13]) + (val[M32] * matrix.val[M23]) + (val[M33] * matrix.val[M33]);
        return set(tmp);
    }

    /**
     * Sets the matrix to an identity matrix.
     *
     * @return This matrix for the purpose of chaining methods together.
     */
    @NonNull
    public Matrix4 idt() {
        val[M00] = 1;
        val[M01] = 0;
        val[M02] = 0;
        val[M03] = 0;
        val[M10] = 0;
        val[M11] = 1;
        val[M12] = 0;
        val[M13] = 0;
        val[M20] = 0;
        val[M21] = 0;
        val[M22] = 1;
        val[M23] = 0;
        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
        return this;
    }

    /**
     * Sets this matrix to an orthographic projection matrix with the origin at
     * (x,y) extending by width and height. The near plane is set to 0, the far
     * plane is set to 1.
     *
     * @param x      The x-coordinate of the origin
     * @param y      The y-coordinate of the origin
     * @param width  The width
     * @param height The height
     * @return This matrix for the purpose of chaining methods together.
     */
    @NonNull
    public Matrix4 setToOrtho2D(float x, float y, float width, float height) {
        setToOrtho(x, x + width, y, y + height, 0, 1);
        return this;
    }

    /**
     * Sets the matrix to an orthographic projection like glOrtho
     * (http://www.opengl.org/sdk/docs/man/xhtml/glOrtho.xml) following the
     * OpenGL equivalent
     *
     * @param left   The left clipping plane
     * @param right  The right clipping plane
     * @param bottom The bottom clipping plane
     * @param top    The top clipping plane
     * @param near   The near clipping plane
     * @param far    The far clipping plane
     * @return This matrix for the purpose of chaining methods together.
     */
    @NonNull
    private Matrix4 setToOrtho(float left, float right, float bottom, float top, float near, float far) {

        idt();
        float x_orth = 2 / (right - left);
        float y_orth = 2 / (top - bottom);
        float z_orth = -2 / (far - near);

        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        val[M00] = x_orth;
        val[M10] = 0;
        val[M20] = 0;
        val[M30] = 0;
        val[M01] = 0;
        val[M11] = y_orth;
        val[M21] = 0;
        val[M31] = 0;
        val[M02] = 0;
        val[M12] = 0;
        val[M22] = z_orth;
        val[M32] = 0;
        val[M03] = tx;
        val[M13] = ty;
        val[M23] = tz;
        val[M33] = 1;

        return this;
    }


    @NonNull
    @Override
    public String toString() {
        return "[" + val[M00] + "|" + val[M01] + "|" + val[M02] + "|" + val[M03] + "]\n" + "[" + val[M10] + "|" + val[M11] + "|" + val[M12] + "|" + val[M13] + "]\n" + "[" + val[M20] + "|" + val[M21] + "|" + val[M22] + "|" + val[M23] + "]\n" + "[" + val[M30] + "|" + val[M31] + "|" + val[M32] + "|" + val[M33] + "]\n";
    }


}