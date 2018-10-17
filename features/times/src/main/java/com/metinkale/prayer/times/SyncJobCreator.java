package com.metinkale.prayer.times;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.sources.WebTimes;

class SyncJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        try {
            if (tag.startsWith(WebTimes.SyncJob.TAG)) {
                Times t = Times.getTimes(Long.parseLong(tag.substring(WebTimes.SyncJob.TAG.length())));
                if (t instanceof WebTimes)
                    return ((WebTimes) t).new SyncJob();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return null;
    }
}
