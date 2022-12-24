/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.utils.livedata;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LiveDataAwareList<T> extends LiveData<List<T>> implements List<T> {

    private final List<T> list;

    private void onUpdate() {
        if (Looper.myLooper() == Looper.getMainLooper())
            setValue(this);
        else
            postValue(this  );
    }

    public LiveDataAwareList(List<T> list) {
        this.list = list;
        onUpdate();
    }

    public LiveDataAwareList() {
        this(new ArrayList<>());
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        try {
            return list.add(t);
        } finally {
            onUpdate();
        }
    }

    @Override
    public boolean remove(Object o) {
        try {
            return list.remove(o);
        } finally {
            onUpdate();
        }
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        try {
            return list.addAll(c);
        } finally {
            onUpdate();
        }
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        try {
            return list.addAll(c);
        } finally {
            onUpdate();
        }
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        try {
            return list.removeAll(c);
        } finally {
            onUpdate();
        }
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        try {
            return list.retainAll(c);
        } finally {
            onUpdate();
        }
    }

    @Override
    public void clear() {
        try {
            list.clear();
        } finally {
            onUpdate();
        }
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        try {
            return list.set(index, element);
        } finally {
            onUpdate();
        }
    }

    @Override
    public void add(int index, T element) {
        try {
            list.add(index, element);
        } finally {
            onUpdate();
        }
    }

    @Override
    public T remove(int index) {
        try {
            return list.remove(index);
        } finally {
            onUpdate();
        }
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(final int index) {
        return new ListIterator<T>() {
            final ListIterator<T> it = list.listIterator(index);

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return it.next();
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @Override
            public T previous() {
                return it.previous();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void remove() {
                it.remove();
                postValue(LiveDataAwareList.this);
            }

            @Override
            public void set(T t) {
                it.set(t);
                postValue(LiveDataAwareList.this);
            }

            @Override
            public void add(T t) {
                it.add(t);
                postValue(LiveDataAwareList.this);
            }
        };
    }

    @NonNull
    @Override
    public LiveDataAwareList<T> subList(int fromIndex, int toIndex) {
        return new LiveDataAwareList<>(list.subList(fromIndex, toIndex));
    }
}
