/*
 * Copyright (c) 2013-2023 Metin Kale
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

package com.metinkale.prayer.dhikr.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DhikrViewModel extends AndroidViewModel {
    private final LiveData<List<Dhikr>> dhikrs;
    private final DhikrDatabase appDatabase;

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

        private final DhikrDatabase db;

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

        private final DhikrDatabase db;

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

        private final DhikrDatabase db;

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
