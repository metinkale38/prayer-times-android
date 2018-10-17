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

package com.metinkale.prayer.compass.classes.math;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Encapsulates a 3D vector. Allows chaining operations by returning a reference
 * to itself in all modification methods.
 *
 * @author badlogicgames@gmail.com
 */
public class Vector3 implements Serializable {

    public static final Vector3 X = new Vector3(1, 0, 0);
    public static final Vector3 Y = new Vector3(0, 1, 0);
    private static final long serialVersionUID = 3840054589595372522L;

    public float x;
    public float y;
    public float z;

    public Vector3() {
    }

    /**
     * Creates a vector with the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     */
    private Vector3(float x, float y, float z) {
        set(x, y, z);
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
    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Sets the components of the given vector
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    @NonNull
    public Vector3 set(@NonNull Vector3 vector) {
        return set(vector.x, vector.y, vector.z);
    }


    @NonNull
    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }


    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = (prime * result) + NumberUtils.floatToIntBits(x);
        result = (prime * result) + NumberUtils.floatToIntBits(y);
        result = (prime * result) + NumberUtils.floatToIntBits(z);
        return result;
    }

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
        Vector3 other = (Vector3) obj;
        return NumberUtils.floatToIntBits(x) == NumberUtils.floatToIntBits(other.x) && NumberUtils.floatToIntBits(y) == NumberUtils.floatToIntBits(other.y) && NumberUtils.floatToIntBits(z) == NumberUtils.floatToIntBits(other.z);
    }


}