package com.metinkale.prayerapp.custom;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.HashMap;

public class WeakValueHashMap<K, V> {
    private AbstractMap<K, WeakReference<V>> mDatabase = new HashMap<>();

    public V get(K key) {
        WeakReference<V> weakRef = mDatabase.get(key);
        if (weakRef == null)
            return null;
        V result = weakRef.get();
        if (result == null) {
            mDatabase.remove(key);
        }
        return result;
    }

    public void put(K key, V value) {
        mDatabase.put(key, new WeakReference<>(value));
    }
}