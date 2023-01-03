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

import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.times.alarm.Alarm
import com.metinkale.prayer.times.utils.MD5
import kotlinx.serialization.Serializable
import java.io.File
import java.io.IOException

@Serializable
data class AppSound(
    override var name: String,
    override val size: Int,
    val md5: String?,
    private val url: String,
    override var shortName: String? = null,
) : Sound {
    init {
        Sounds.addSound(this)
    }

    var checkedMD5 = false

    override val id: Int get() = url.hashCode()
    override val appSounds: Set<AppSound> get() = setOf(this)

    val file: File
        get() {
            val path = url.substring(url.indexOf("/sounds/") + 8)
            val file = File(
                App.get().getExternalFilesDir(
                    Environment.DIRECTORY_MUSIC
                ), path
            )
            file.parentFile?.mkdirs()
            return file
        }
    val uri: Uri
        get() = Uri.fromFile(file)

    private fun checkMD5() {
        val file = file
        if (file.exists()) {
            if (size.toLong() != file.length() || md5 != null && !MD5.checkMD5(
                    md5, file
                )
            ) {
                file.delete()
            }
        }
    }


    override fun createMediaPlayer(alarm: Alarm?): MediaPlayer? {
        if (!isDownloaded) return null
        val mp = MediaPlayer()
        try {
            mp.setDataSource(App.get(), uri)
        } catch (e: IOException) {
            recordException(e)
            return null
        }
        return mp
    }

    override val isDownloaded: Boolean
        get() {
            if (!checkedMD5) {
                checkMD5()
            }
            checkedMD5 = file.exists()
            return checkedMD5
        }

    fun getUrl(): String {
        return App.API_URL + url
    }
}