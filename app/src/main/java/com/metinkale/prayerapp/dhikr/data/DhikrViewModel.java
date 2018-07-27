package com.metinkale.prayerapp.dhikr.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.metinkale.prayerapp.persistence.AppDatabase;

import java.util.List;

public class DhikrViewModel extends AndroidViewModel {
    private final LiveData<List<Dhikr>> dhikrs;
    private AppDatabase appDatabase;

    public DhikrViewModel(Application application) {
        super(application);

        appDatabase = AppDatabase.getDatabase(this.getApplication());

        dhikrs = appDatabase.dhikrDao().getAllDhikrs();
    }


    public LiveData<List<Dhikr>> getDhikrs() {
        return dhikrs;
    }

    public void deleteDhikr(Dhikr...dhikr) {
        new DeleteAsyncTask(appDatabase).execute(dhikr);
    }

    public void addDhikr(Dhikr...dhikr) {
        new AddAsyncTask(appDatabase).execute(dhikr);
    }

    public void saveDhikr(Dhikr...dhikr) {
        new SaveAsyncTask(appDatabase).execute(dhikr);
    }

    private static class DeleteAsyncTask extends AsyncTask<Dhikr, Void, Void> {

        private AppDatabase db;

        DeleteAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Dhikr... params) {
            db.dhikrDao().deleteDhikr(params);
            return null;
        }

    }


    private static class AddAsyncTask extends AsyncTask<Dhikr, Void, Void> {

        private AppDatabase db;

        AddAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Dhikr... params) {
            db.dhikrDao().addDhikr(params);
            return null;
        }

    }

    private static class SaveAsyncTask extends AsyncTask<Dhikr, Void, Void> {

        private AppDatabase db;

        SaveAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Dhikr... params) {
            db.dhikrDao().saveDhikr(params);
            return null;
        }

    }

}
