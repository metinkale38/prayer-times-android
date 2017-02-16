package com.metinkale.prayerapp.utils;


import android.support.annotation.Nullable;

import java.util.Arrays;

/**
 * This is a very simple int -> Generic Object map.
 * It is basically a resizable Array, an Index to Object mapping
 * <p>
 * It is probably the fastest (int) map implementation for all operations,
 * but it can be very memory inefficient if it is used for the wrong use case!
 * <p>
 * It only supports positive int-Keys, and should only be used, if the highest key is near to the
 * size of elements (it should have little to no gaps between the mapped keys)
 * It will create an array of atleast the size of the largest key value.
 * <p>
 * It does not use the Map interface, to avoid autoboxing overhead.
 * <p>
 * Whats the difference with an ArrayList?
 * An ArrayList fills the Array without gaps and the index ("key") will be in insertion order
 * SimpleIntArrayMap uses the key as the Array index, so we have a real key to object mapping.
 * Other than that, it works exactly the same as an ArrayList
 *
 * Why not use an Array?
 * It does not resize and you will get OutOfBoundsExceptions...
 *
 * @param <T> Object Type
 */
public class SimpleIntArrayMap<T> {
    private int mSize;
    private Object[] mArray;

    /**
     * Constructor with initial capacity
     *
     * @param initialCapacity initial capacity
     */
    public SimpleIntArrayMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " +
                    initialCapacity);
        mArray = new Object[initialCapacity];
    }

    /**
     * Default constructor with a initial array size of 10
     */
    public SimpleIntArrayMap() {
        this(10);
    }

    /**
     * Returns the actual number of elements in this map
     *
     * @return number of elements
     */
    public int numberOfElements() {
        return mSize;
    }

    /**
     * Returns the size of the backed array.
     * Unlike other map implementations, it is NOT the number of elements!
     * There might be gaps (null values) in the elements 0<=x<size();
     * <p>
     * For the number of elements use numberOfElements()
     *
     * @return size
     */
    public int size() {
        return mArray.length;
    }

    /**
     * Empty or not
     *
     * @return true for empty, false otherwise
     */
    public boolean isEmpty() {
        return mSize == 0;
    }

    /**
     * Checks whether it contains a value for key
     *
     * @param key Key to check
     * @return true or false
     */
    public boolean containsKey(int key) {
        return mArray[key] != null;
    }

    /**
     * Checks whether map contains a specific value
     *
     * @param object object
     * @return
     */
    public boolean containsValue(T object) {
        return indexOf(object) != -1;
    }

    /**
     * Gets the index of an object
     *
     * @param object Object
     * @return index or -1 if it does not exist
     */
    public int indexOf(T object) {
        if (object == null) return -1;
        for (int i = 0; i < mArray.length; i++) {
            Object o = mArray[i];
            if (o != null) {
                if (o == object) return i;
                else if (o.equals(object)) return i;
            }
        }
        return -1;
    }

    /**
     * Gets the value for a key
     *
     * @param key key
     * @return object or null if does not exists
     */
    @Nullable
    public T get(int key) {
        if (key >= mArray.length) return null;
        return (T) mArray[key];
    }

    /**
     * maps object to key
     *
     * @param key    index
     * @param object object
     */
    public void put(int key, T object) {
        ensureCapacity(key + 1);
        if (mArray[key] == null && object != null) mSize++;
        mArray[key] = object;
    }

    /**
     * removes object from map
     *
     * @param o object
     */
    public void remove(T o) {
        int i = indexOf(o);
        if (i >= 0 && mArray[i] != null) {
            mArray[i] = null;
            mSize--;
        }
    }

    /**
     * overrides the backed array with a blank array of size 10
     */
    public void clear() {
        mArray = new Object[10];
    }

    /**
     * can be used to trim the array to the actually needed size
     * <p>
     * can be used e.g. after the map wont be modified afterwards
     */
    public void trimToSize() {
        int i = mArray.length - 1;
        while (i >= 0 && mArray[i] == null) {
            i--;
        }
        i++;

        int oldCapacity = mArray.length;
        if (i < oldCapacity && i > 0) {
            mArray = Arrays.copyOf(mArray, i);
        }
    }


    /**
     * resizes the map if needed
     *
     * @param minCapacity minimum allowed capacity
     */
    public void ensureCapacity(int minCapacity) {
        int oldCapacity = mArray.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            // minCapacity is usually close to numberOfElements, so this is a win:
            mArray = Arrays.copyOf(mArray, newCapacity);
        }
    }


}
