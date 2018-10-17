package com.metinkale.prayer.utils.livedata;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * extending from LiveData makes problems with GSON (De-)Serialization, so i created this TransientLiveData Class
 *
 * it is basically a wrapper for a transient MutableLiveData Member Object
 */
public class TransientLiveData<T> {

    private transient MutableLiveData<T> mLiveData = new MutableLiveData<>();

    protected void postValue(T value) {
        mLiveData.postValue(value);
    }

    public void setValue(T value) {
        mLiveData.setValue(value);
    }

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        mLiveData.observe(owner, observer);
    }

    @MainThread
    public void observeForever(@NonNull Observer<T> observer) {
        mLiveData.observeForever(observer);
    }

    @MainThread
    public void removeObserver(@NonNull Observer<T> observer) {
        mLiveData.removeObserver(observer);
    }

    @MainThread
    public void removeObservers(@NonNull LifecycleOwner owner) {
        mLiveData.removeObservers(owner);
    }

    @Nullable
    public T getValue() {
        return mLiveData.getValue();
    }

    public boolean hasObservers() {
        return mLiveData.hasObservers();
    }

    public boolean hasActiveObservers() {
        return mLiveData.hasActiveObservers();
    }



}
