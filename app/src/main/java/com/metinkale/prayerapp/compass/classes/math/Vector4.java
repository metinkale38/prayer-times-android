/*
 * Copyright (c) 2013-2017 Metin Kale
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

package com.metinkale.prayerapp.compass.classes.math;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Derived from Vector3 from libgdx - encapsulates a 4 component vector for
 * representing Homogeneous Coordinates
 */
public final class Vector4 {

    public float x;
    public float y;
    public float z;
    private float w;

    /**
     * Constructs a vector at (0,0,0)
     */
    public Vector4() {
    }


    /**
     * Multiplies the vector by the given matrix.
     *
     * @param matrix The matrix
     * @return This vector for chaining
     */
    @NonNull
    public Vector4 mul(@NonNull Matrix4 matrix) {
        float[] l_mat = matrix.val;
        return set((x * l_mat[Matrix4.M00]) + (y * l_mat[Matrix4.M01]) + (z * l_mat[Matrix4.M02]) + (w * l_mat[Matrix4.M03]), x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + w * l_mat[Matrix4.M13], x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + w * l_mat[Matrix4.M23], x * l_mat[Matrix4.M30] + y * l_mat[Matrix4.M31] + z * l_mat[Matrix4.M32] + w * l_mat[Matrix4.M33]);
    }

    /**
     * Sets the vector to the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @return this vector for chaining
     */
    @NonNull
    public Vector4 set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Sets the components from the array. The array must have at least 3
     * elements
     *
     * @param values The array
     * @return this vector for chaining
     */
    @NonNull
    public Vector4 set(float[] values) {
        return set(values[0], values[1], values[2], values[3]);
    }


    /**
     * Adds the given vector to this vector
     *
     * @param vector The other vector
     * @return This vector for chaining
     */
    @NonNull
    public Vector4 add(@NonNull Vector4 vector) {
        return add(vector.x, vector.y, vector.z, vector.w);
    }

    /**
     * Adds the given vector to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining.
     */
    @NonNull
    private Vector4 add(float x, float y, float z, float w) {
        return set(this.x + x, this.y + y, this.z + z, this.w + w);
    }

    /**
     * Adds the given value to all three components of the vector.
     *
     * @param values The value
     * @return This vector for chaining
     */
    @NonNull
    public Vector4 add(float values) {
        return set(x + values, y + values, z + values, w + values);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = (prime * result) + Float.floatToIntBits(x);
        result = (prime * result) + Float.floatToIntBits(y);
        result = (prime * result) + Float.floatToIntBits(z);
        result = (prime * result) + Float.floatToIntBits(w);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vector4 other = (Vector4) obj;
        if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
            return false;
        }
        return Float.floatToIntBits(z) == Float.floatToIntBits(other.z) && Float.floatToIntBits(w) == Float.floatToIntBits(other.w);
    }
}