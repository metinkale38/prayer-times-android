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
package com.metinkale.prayer.times

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.times.times.DayTimesWebProvider
import com.metinkale.prayer.times.times.Times

internal class SyncJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        try {
            if (tag.startsWith(DayTimesWebProvider.SYNC_JOB_TAG)) {
                (Times.getTimesById(
                    tag.substring(DayTimesWebProvider.SYNC_JOB_TAG.length).toInt()
                ).value?.dayTimes as? DayTimesWebProvider)?.syncJob()
            }
        } catch (e: Exception) {
            recordException(e)
        }
        return null
    }
}