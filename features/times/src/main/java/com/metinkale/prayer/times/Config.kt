package com.metinkale.prayer.times

import com.koushikdutta.ion.Ion
import com.metinkale.prayer.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Config(
    val api_url: String = "https://open-prayer-times.lm.r.appspot.com/",
    val use_diyanet_rest: Boolean = false,
    val use_igmg_rest: Boolean = true,
    val use_london_rest: Boolean = false,
    val use_nvc_rest: Boolean = false,
    val use_semerkand_rest: Boolean = false,
    val use_search_rest: Boolean = false
) {

    companion object {
        private var config: Config? = null
        suspend fun getConfig(): Config = withContext(Dispatchers.IO) {
            if (config == null) {
                val prefs = App.get().getSharedPreferences("android-config", 0)
                val timestamp = System.currentTimeMillis() - prefs.getLong("timestamp", 0)

                if ((timestamp > 1000 * 60 && BuildConfig.DEBUG) || timestamp > 1000 * 60 * 60 * 24) {
                    runCatching {
                        val json = Ion.with(App.get()).load(
                            "GET",
                            "https://metinkale38.github.io/prayer-times-android/android-config.json"
                        ).asString().get()
                        prefs.edit().putLong("timestamp", System.currentTimeMillis())
                            .putString("json", json).commit()
                    }.getOrNull()
                }

                config =
                    prefs.getString("json", "")?.ifBlank { null }
                        ?.let { Json.decodeFromString(serializer(), it) }
                        ?: Config()
            }
            config!!
        }

    }

}