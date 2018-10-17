package com.metinkale.prayer.dhikr.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class DhikrViewModel extends AndroidViewModel {
    private final LiveData<List<Dhikr>> dhikrs;
    private DhikrDatabase appDatabase;

    public DhikrViewModel(Application application) {
        super(application);

        appDatabase = DhikrDatabase.getDatabase(this.getApplication());

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

        private DhikrDatabase db;

        DeleteAsyncTask(DhikrDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Dhikr... params) {
            db.dhikrDao().deleteDhikr(params);
            return null;
        }

    }


    private static class AddAsyncTask extends AsyncTask<Dhikr, Void, Void> {

        private DhikrDatabase db;

        AddAsyncTask(DhikrDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Dhikr... params) {
            db.dhikrDao().addDhikr(params);
            return null;
        }

    }

    private static class SaveAsyncTask extends AsyncTask<Dhikr, Void, Void> {

        private DhikrDatabase db;

        SaveAsyncTask(DhikrDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Dhikr... params) {
            db.dhikrDao().saveDhikr(params);
            return null;
        }

    }

}
