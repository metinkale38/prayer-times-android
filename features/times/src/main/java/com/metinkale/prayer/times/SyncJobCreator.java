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

package com.metinkale.prayer.times;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.metinkale.prayer.CrashReporter;
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
            CrashReporter.recordException(e);
        }
        return null;
    }
}
