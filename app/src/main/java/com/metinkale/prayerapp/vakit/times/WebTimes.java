/*
 * Copyright (c) 2016 Metin Kale
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

package com.metinkale.prayerapp.vakit.times;


import android.support.annotation.NonNull;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;

import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class WebTimes extends Times {


    private Runnable mNotify = new Runnable() {
        @Override
        public void run() {
            notifyOnUpdated();
            App.getHandler().removeCallbacks(this);
        }
    };
    protected Map<String, String> times = new HashMap<>();
    private String id;
    private int jobId = -1;


    WebTimes(long id) {
        super(id);
    }

    WebTimes() {
        super();
    }


    public static void add(Source source, String city, String id, double lat, double lng) {
        if (source == null || source == Source.Calc) return;
        long _id = System.currentTimeMillis();
        WebTimes t = null;

        switch (source) {
            case Diyanet:
                t = new DiyanetTimes(_id);
                break;
            case IGMG:
                t = new IGMGTimes(_id);
                break;
            case Fazilet:
                t = new FaziletTimes(_id);
                break;
            case NVC:
                t = new NVCTimes(_id);
                break;
            case Semerkand:
                t = new SemerkandTimes(_id);
                break;
            case Morocco:
                t = new MoroccoTimes(_id);
                break;
        }
        if (t == null) return;
        t.setSource(source);
        t.setName(city);
        t.setLat(lat);
        t.setLng(lng);
        t.setId(id);
        t.setSortId(99);
        t.scheduleJob();

    }

    @Override
    public void delete() {
        super.delete();
        if (jobId != -1)
            JobManager.instance().cancel(jobId);
    }

    @Override
    public synchronized String getTime(LocalDate date, int time) {

        return super.getTime(date, time);
    }


    String extractLine(String str) {
        str = str.substring(str.indexOf(">") + 1);
        str = str.substring(0, str.indexOf("</"));
        return str;
    }


    @Override
    protected synchronized String _getTime(LocalDate date, int time) {
        String str = times.get(date.toString("yyyy-MM-dd") + "-" + time);
        if (str == null || str.contains("00:00")) {
            return "00:00";
        }
        return str.replace("*", "");
    }

    private synchronized void setTime(LocalDate date, int time, String value) {
        if (deleted() || value.contains("00:00")) return;
        times.put(date.toString("yyyy-MM-dd") + "-" + time, value.replace("*", ""));
        save();

        App.getHandler().post(mNotify);
    }

    void setTimes(LocalDate date, String[] value) {
        if (deleted()) return;
        for (int i = 0; i < value.length; i++) {
            setTime(date, i, value[i]);
        }
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
        save();
    }

    public void syncAsync() {
        if (!App.isOnline()) {
            Toast.makeText(App.getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            return;
        }
        Builders.Any.F[] builders = createIonBuilder();
        for (Builders.Any.F builder : builders) {
            builder.asString().withResponse().setCallback(new FutureCallback<Response<String>>() {
                @Override
                public void onCompleted(Exception e, Response<String> result) {
                    if (e != null) {
                        Crashlytics.setString("WebTimesSource", getSource().toString());
                        Crashlytics.setString("WebTimesName", getName());
                        Crashlytics.setString("WebTimesId", getId());
                        Crashlytics.logException(e);
                        return;
                    }

                    try {
                        parseResult(result.getResult());
                    } catch (Exception ee) {
                        Crashlytics.setString("WebTimesSource", getSource().toString());
                        Crashlytics.setString("WebTimesName", getName());
                        Crashlytics.setString("WebTimesId", getId());
                        Crashlytics.logException(ee);
                    }
                }
            });
        }
    }


    protected Builders.Any.F[] createIonBuilder() {
        return new Builders.Any.F[0];
    }

    protected boolean parseResult(String result) {
        return true;
    }

    private int getSyncedDays() {
        LocalDate date = LocalDate.now();
        int i = 0;
        while (i < 45) {
            String prefix = date.toString("yyyy-MM-dd") + "-";
            String times[] = {
                    this.times.get(prefix + 0),
                    this.times.get(prefix + 1),
                    this.times.get(prefix + 2),
                    this.times.get(prefix + 3),
                    this.times.get(prefix + 4),
                    this.times.get(prefix + 5)
            };
            for (String time : times) {
                if (time == null || time.contains("00:00")) return i;
            }
            i++;
            date = date.plusDays(1);
        }
        return i;

    }

    public LocalDate getFirstSyncedDay() {
        LocalDate date = LocalDate.now();
        int i = 0;
        while (true) {
            String prefix = date.toString("yyyy-MM-dd") + "-";
            String times[] = {
                    this.times.get(prefix + 0),
                    this.times.get(prefix + 1),
                    this.times.get(prefix + 2),
                    this.times.get(prefix + 3),
                    this.times.get(prefix + 4),
                    this.times.get(prefix + 5)
            };
            for (String time : times) {
                if (time == null || time.contains("00:00") || i > this.times.size())
                    return date.plusDays(1);
            }
            i++;
            date = date.minusDays(1);
        }
    }

    public LocalDate getLastSyncedDay() {
        LocalDate date = LocalDate.now();
        int i = 0;
        while (true) {
            String prefix = date.toString("yyyy-MM-dd") + "-";
            String times[] = {
                    this.times.get(prefix + 0),
                    this.times.get(prefix + 1),
                    this.times.get(prefix + 2),
                    this.times.get(prefix + 3),
                    this.times.get(prefix + 4),
                    this.times.get(prefix + 5)
            };
            for (String time : times) {
                if (time == null || time.contains("00:00") || i > this.times.size())
                    return date.minusDays(1);
            }
            i++;
            date = date.plusDays(1);
        }
    }

    private void scheduleJob() {
        int syncedDays = getSyncedDays();


        if (syncedDays == 0) {
            if (App.isOnline()) syncAsync();
            else
                jobId = new JobRequest.Builder(SyncJob.TAG + getID())
                        .setExecutionWindow(1, TimeUnit.MINUTES.toMillis(3))
                        .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                        .setBackoffCriteria(TimeUnit.MINUTES.toMillis(3), JobRequest.BackoffPolicy.EXPONENTIAL)
                        .setUpdateCurrent(true)
                        .build()
                        .schedule();

        } else if (syncedDays < 3)
            jobId = new JobRequest.Builder(SyncJob.TAG + getID())
                    .setExecutionWindow(1, TimeUnit.HOURS.toMillis(3))
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setUpdateCurrent(true)
                    .setBackoffCriteria(TimeUnit.HOURS.toMillis(1), JobRequest.BackoffPolicy.EXPONENTIAL)
                    .build()
                    .schedule();
        else if (syncedDays < 10)
            jobId = new JobRequest.Builder(SyncJob.TAG + getID())
                    .setExecutionWindow(1, TimeUnit.DAYS.toMillis(3))
                    .setBackoffCriteria(TimeUnit.DAYS.toMillis(1), JobRequest.BackoffPolicy.LINEAR)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequiresCharging(true)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule();
        else if (syncedDays < 20)
            jobId = new JobRequest.Builder(SyncJob.TAG + getID())
                    .setExecutionWindow(1, TimeUnit.DAYS.toMillis(10))
                    .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                    .setBackoffCriteria(TimeUnit.DAYS.toMillis(3), JobRequest.BackoffPolicy.LINEAR)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule();

    }


    public class SyncJob extends Job {
        public static final String TAG = "WebTimesSyncJob";

        @NonNull
        @Override
        protected Result onRunJob(Params params) {
            if (!App.isOnline()) return Result.RESCHEDULE;
            boolean success = false;
            Builders.Any.F[] builders = createIonBuilder();
            for (Builders.Any.F builder : builders) {
                try {
                    String str = builder.asString().get();
                    if (parseResult(str)) success = true;
                } catch (Exception e) {
                    Crashlytics.setString("WebTimesSource", getSource().toString());
                    Crashlytics.setString("WebTimesName", getName());
                    Crashlytics.setString("WebTimesId", getId());
                    Crashlytics.logException(e);
                }
            }
            return success ? Result.SUCCESS : Result.RESCHEDULE;
        }

        @Override
        protected void onReschedule(int newJobId) {
            jobId = newJobId;
        }


    }


}
