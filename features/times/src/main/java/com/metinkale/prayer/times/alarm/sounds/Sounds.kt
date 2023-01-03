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

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import com.koushikdutta.ion.Ion
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.sounds.UserSound.Companion.create
import com.metinkale.prayer.times.times.Vakit
import com.metinkale.prayer.utils.Utils
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Sounds {
    private val sSounds: MutableList<Sound> = ArrayList()

    init {
        loadSounds()
    }

    @JvmStatic
    val sounds: List<Sound>
        get() = sSounds

    fun addSound(sound: Sound) {
        sSounds.add(sound)
    }

    @JvmStatic
    val rootSounds: List<Sound>
        get() {
            val sounds = ArrayList<Sound>()
            for (sound in sSounds) {
                if (sound is BundledSound || sound is UserSound) {
                    sounds.add(sound)
                }
            }
            return sounds
        }

    fun getSound(id: Int): Sound? {
        for (sound in sSounds) {
            if (sound.id == id) {
                return sound
            }
        }
        return null
    }

    @JvmStatic
    fun getSound(uri: String): Sound {
        return getSound(uri.hashCode())
            ?: return create(Uri.parse(uri))
    }

    @JvmStatic
    fun downloadData(c: Context?, onFinish: Runnable?) {
        val dlg = ProgressDialog(c)
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dlg.setCancelable(false)
        dlg.setCanceledOnTouchOutside(false)
        dlg.show()
        val onFinish2 = Runnable {
            onFinish?.run()
            dlg.dismiss()
        }
        Ion.with(App.get()).load(App.API_URL + "/sounds/sounds.json").progressDialog(dlg)
            .asString(StandardCharsets.UTF_8).setCallback { exp: Exception?, result: String ->
                if (exp != null) {
                    recordException(exp)
                } else {
                    var outputStream: FileOutputStream? = null
                    try {
                        if (JSONObject(result).getString("name") != "sounds") return@setCallback
                        outputStream = App.get().openFileOutput("sounds.json", Context.MODE_PRIVATE)
                        outputStream.write(result.toByteArray())
                        loadSounds()
                    } catch (e: Exception) {
                        //CrashReporter.recordException(e);
                    } finally {
                        Utils.close(outputStream)
                    }
                }
                onFinish2.run()
            }
    }

    @Synchronized
    private fun loadSounds() {
        if (sSounds.isNotEmpty()) return
        var json: String? = null
        var br: BufferedReader? = null
        try {
            br = BufferedReader(InputStreamReader(App.get().openFileInput("sounds.json")))
            val text = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            json = text.toString()
        } catch (e: IOException) {
            //CrashReporter.recordException(e);
        } finally {
            Utils.close(br)
        }
        if (json == null) {
            return
        }
        try {
            val jsonObject = JSONObject(json)
            val jfiles = jsonObject.getJSONArray("files")
            sSounds.clear()
            for (i in 0 until jfiles.length()) {
                val jobj = jfiles.getJSONObject(i)
                if (!jobj.has("files")) continue
                val folderName = jobj.getString("name")
                val bundledSound = BundledSound(folderName)
                val jsounds = jobj.getJSONArray("files")
                for (j in 0 until jsounds.length()) {
                    val jsound = jsounds.getJSONObject(j)
                    var name = jsound.getString("name")
                    val url = "/sounds/" + URLEncoder.encode(folderName)
                        .replace("+", "%20") + "/" + URLEncoder.encode(name).replace("+", "%20")
                    if (name.contains(".")) name = name.substring(0, name.lastIndexOf("."))
                    val sound = AppSound(name, jsound.getInt("size"), jsound.getString("md5"), url)
                    if (!name.contains(" - ")) {
                        bundledSound.addSound(BundledSound.SoundType.Default, sound)
                        sound.shortName = name
                        continue
                    }
                    val params = name.substring(name.indexOf(" - ") + 3).trim { it <= ' ' }
                    if (params.contains("Short") && params.contains("Fajr")) {
                        bundledSound.addSound(BundledSound.SoundType.FajrShort, sound)
                        sound.shortName = App.get().getString(R.string.fajrShort)
                    } else if (params.contains("Short")) {
                        bundledSound.addSound(BundledSound.SoundType.Takbir, sound)
                        sound.shortName = App.get().getString(R.string.takbir)
                    } else if (params.contains("Fajr")) {
                        bundledSound.addSound(BundledSound.SoundType.Fajr, sound)
                        sound.shortName = Vakit.FAJR.string
                    } else if (params.contains("Zuhr")) {
                        bundledSound.addSound(BundledSound.SoundType.Zuhr, sound)
                        sound.shortName = Vakit.DHUHR.string
                    } else if (params.contains("Asr")) {
                        bundledSound.addSound(BundledSound.SoundType.Asr, sound)
                        sound.shortName = Vakit.ASR.string
                    } else if (params.contains("Magrib")) {
                        bundledSound.addSound(BundledSound.SoundType.Magrib, sound)
                        sound.shortName = Vakit.MAGHRIB.string
                    } else if (params.contains("Isha")) {
                        bundledSound.addSound(BundledSound.SoundType.Ishaa, sound)
                        sound.shortName = Vakit.ISHAA.string
                    } else if (params.contains("Dua")) {
                        bundledSound.addSound(BundledSound.SoundType.Dua, sound)
                        sound.shortName = App.get().getString(R.string.adhanDua)
                    } else if (params.contains("Sela")) {
                        bundledSound.addSound(BundledSound.SoundType.Sela, sound)
                        sound.shortName = App.get().getString(R.string.salavat)
                    } else {
                        bundledSound.addSound(BundledSound.SoundType.Default, sound)
                        sound.shortName = params
                    }
                    sound.name = folderName + " - " + sound.shortName
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}