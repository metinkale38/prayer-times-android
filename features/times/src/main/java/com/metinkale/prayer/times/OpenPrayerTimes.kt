package com.metinkale.prayer.times

import android.content.res.Resources
import android.os.Build
import androidx.lifecycle.LiveData
import dev.metinkale.prayertimes.core.Configuration
import dev.metinkale.prayertimes.core.Entry
import dev.metinkale.prayertimes.core.router.Method
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val coreRouter = dev.metinkale.prayertimes.core.router.coreRouter.also {
    Configuration.GOOGLE_API_KEY = "YOUR_GOOGLE_API_KEY"
    Configuration.IGMG_API_KEY = "YOUR_IGMG_API_KEY"
    Configuration.LONDON_PRAYER_TIMES_API_KEY = "YOUR_LONDON_PRAYER_TIMES_API_KEY"
    Configuration.HOT_ENTRIES = true
}


open class OpenPrayerTimesApi<T> : CoroutineScope by MainScope(), LiveData<T>() {
    private val remote = false
    private val remoteUrl = "http://"
    private val headers = mapOf<String, String>()

    protected suspend fun get(path: String, params: Map<String, String>): String {
        return if (remote) {
            ""
        } else {
            coreRouter.invoke(Method.GET, path, params, headers).body ?: "[]"
        }
    }
}

class OpenPrayerTimesSearchEndpoint : OpenPrayerTimesApi<List<Entry>>() {
    fun search(q: String): Job = launch(Dispatchers.IO) {
        get("/search", mapOf("q" to q))
            .let { Json.decodeFromString(ListSerializer(Entry.serializer()), it) }
            .let { postValue(it) }
    }


    fun search(lat: Double, lng: Double): Job = launch(Dispatchers.IO) {
        get("/search", mapOf("lat" to "$lat", "lng" to "$lng"))
            .let { Json.decodeFromString(ListSerializer(Entry.serializer()), it) }
            .let { postValue(it) }
    }

    fun list(id: String): Job = launch(Dispatchers.IO) { value = emptyList() }

}


val Source.drawableId: Int?
    get() = when (this) {
        Source.Diyanet -> R.drawable.ic_ditib
        Source.IGMG -> R.drawable.ic_igmg
        Source.Semerkand -> R.drawable.ic_semerkand
        Source.NVC -> R.drawable.ic_namazvakticom
        else -> null
    }


private val languages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Resources.getSystem().configuration.locales.let { locales ->
        (0 until locales.size()).map { locales[it].language }
    }
} else {
    listOf(Resources.getSystem().configuration.locale.language)
}

fun Entry.localizedNames(): List<String> = names(*languages.toTypedArray())
fun Entry.localizedName(): String = localizedNames().first()
fun Entry.localizedRegion(): String = localizedNames().drop(1).joinToString(" - ")