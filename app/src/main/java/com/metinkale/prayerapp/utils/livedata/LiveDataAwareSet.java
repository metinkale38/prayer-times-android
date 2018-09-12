package com.metinkale.prayerapp.utils.livedata;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class LiveDataAwareSet<T> extends LiveData<Set<T>> implements Set<T> {

    private final Set<T> set;

    public LiveDataAwareSet(Set<T> set) {
        this.set = set;
        postValue(set);
    }

    public LiveDataAwareSet() {
        this(new ArraySet<T>());
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Iterator<T> it = set.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return it.next();
            }


            @Override
            public void remove() {
                it.remove();
                postValue(LiveDataAwareSet.this);
            }
        };
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(T t) {
        try {
            return set.add(t);
        } finally {
            postValue(set);
        }
    }

    @Override
    public boolean remove(Object o) {
        try {
            return set.remove(o);
        } finally {
            postValue(set);
        }
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        try {
            return set.addAll(c);
        } finally {
            postValue(set);
        }
    }


    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        try {
            return set.removeAll(c);
        } finally {
            postValue(set);
        }
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        try {
            return set.retainAll(c);
        } finally {
            postValue(set);
        }
    }

    @Override
    public void clear() {
        try {
            set.clear();
        } finally {
            postValue(set);
        }
    }

    public boolean remove(int index) {
        try {
            return set.remove(index);
        } finally {
            postValue(set);
        }
    }


}
