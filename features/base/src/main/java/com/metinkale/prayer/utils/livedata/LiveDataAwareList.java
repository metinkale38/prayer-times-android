package com.metinkale.prayer.utils.livedata;

import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LiveDataAwareList<T> extends LiveData<List<T>> implements List<T> {

    private final List<T> list;

    public LiveDataAwareList(List<T> list) {
        this.list = list;
        postValue(list);
    }

    public LiveDataAwareList() {
        this(new ArrayList<T>());
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
            postValue(list);
        }
    }

    @Override
    public boolean remove(Object o) {
        try {
            return list.remove(o);
        } finally {
            postValue(list);
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
            postValue(list);
        }
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        try {
            return list.addAll(c);
        } finally {
            postValue(list);
        }
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        try {
            return list.removeAll(c);
        } finally {
            postValue(list);
        }
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        try {
            return list.retainAll(c);
        } finally {
            postValue(list);
        }
    }

    @Override
    public void clear() {
        try {
            list.clear();
        } finally {
            postValue(list);
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
            postValue(list);
        }
    }

    @Override
    public void add(int index, T element) {
        try {
            list.add(index, element);
        } finally {
            postValue(list);
        }
    }

    @Override
    public T remove(int index) {
        try {
            return list.remove(index);
        } finally {
            postValue(list);
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
            ListIterator<T> it = list.listIterator(index);

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
