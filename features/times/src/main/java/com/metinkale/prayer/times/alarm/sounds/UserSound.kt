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
package com.metinkale.prayer.times.alarm.sounds

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.times.alarm.Alarm
import java.io.IOException

data class UserSound(val uri: Uri) : Sound {

    override val isDownloaded: Boolean
        get() = true
    override val size: Int
        get() = 0
    override val id: Int
        get() = uri.hashCode()
    override val appSounds: Set<AppSound?> = emptySet()

    override val shortName: String?
        get() = name

    override val name: String?
        get() {
            try {
                var result: String? = null
                if ("content" == uri.scheme) {
                    val cursor = App.get().contentResolver.query(uri, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                    cursor?.close()
                }
                if (result == null) {
                    result = uri.path
                    val cut: Int
                    if (result != null) {
                        cut = result.lastIndexOf('/')
                        if (cut != -1) {
                            result = result.substring(cut + 1)
                        }
                    }
                }
                return result
            } catch (e: Exception) {
                recordException(e)
            }
            return "Unknown"
        }


    override fun createMediaPlayer(alarm: Alarm?): MediaPlayer? {
        val mp = MediaPlayer()
        try {
            mp.setDataSource(App.get(), uri)
        } catch (e: IOException) {
            recordException(e)
            return null
        }
        return mp
    }


    companion object {
        @JvmStatic
        fun create(uri: Uri): UserSound {
            try {
                if (DocumentsContract.isDocumentUri(App.get(), uri)) {
                    App.get().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            } catch (ignore: Exception) {
            }
            var sound = Sounds.getSound(uri.hashCode()) as? UserSound
            if (sound == null) {
                sound = UserSound(uri)
            }
            return sound
        }
    }
}