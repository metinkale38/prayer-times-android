package com.metinkale.prayer.times

import android.content.res.Resources
import android.os.Build
import androidx.lifecycle.LiveData
import com.koushikdutta.ion.Ion
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter
import dev.metinkale.prayertimes.core.Configuration
import dev.metinkale.prayertimes.core.DayTimes
import dev.metinkale.prayertimes.core.Entry
import dev.metinkale.prayertimes.core.router.ListResponse
import dev.metinkale.prayertimes.core.router.Method
import dev.metinkale.prayertimes.core.sources.Source
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

private val coreRouter = dev.metinkale.prayertimes.core.router.coreRouter.also {
    // you will need your own api keys
    Configuration.GOOGLE_API_KEY = App.get().getString(R.string.GOOGLE_API_KEY)
    Configuration.IGMG_API_KEY = App.get().getString(R.string.IGMG_API_KEY)
    Configuration.LONDON_PRAYER_TIMES_API_KEY =
        App.get().getString(R.string.LONDON_PRAYER_TIMES_API_KEY)
    Configuration.HOT_ENTRIES = true
}


abstract class OpenPrayerTimesApi<T> : CoroutineScope by MainScope(), LiveData<T>() {
    private val headers = mapOf<String, String>()

    protected suspend fun get(path: String, params: Map<String, String> = emptyMap()): String =
        withContext(Dispatchers.IO) {
            val remoteUrl = Config.getConfig().api_url
            if (useRest()) {
                Ion.with(App.get()).load("GET", remoteUrl + path)
                    .addQueries(params.mapValues { listOf(it.value) }).asString().get()
            } else {
                coreRouter.invoke(Method.GET, path, params, headers).body ?: "[]"
            }
        }

    abstract suspend fun useRest(): Boolean
}

class OpenPrayerTimesListEndpoint : OpenPrayerTimesApi<ListResponse>() {
    fun list(path: List<String>): Job = launch(Dispatchers.IO) {
        tracker {
            try {
                get("/list/" + path.joinToString("/"))
                    .let { Json.decodeFromString(ListResponse.serializer(), it) }
                    .let { postValue(it) }
            } catch (e: Throwable) {
                CrashReporter.recordException(e)
            }
        }
    }


    override suspend fun useRest(): Boolean = Config.getConfig().use_search_rest
}


class OpenPrayerTimesSearchEndpoint : OpenPrayerTimesApi<List<Entry>>() {
    fun search(q: String): Job = launch(Dispatchers.IO) {
        tracker {
            try {
                get("/search", mapOf("q" to q))
                    .let { Json.decodeFromString(ListSerializer(Entry.serializer()), it) }
                    .let { postValue(it) }
            } catch (e: Throwable) {
                CrashReporter.recordException(e)
            }
        }
    }


    fun search(lat: Double, lng: Double): Job = launch(Dispatchers.IO) {
        tracker {
            try {
                get(
                    "/search",
                    mapOf(
                        "lat" to "${lat.roundLatLng()}",
                        "lng" to "${lng.roundLatLng()}"
                    )
                ).let { Json.decodeFromString(ListSerializer(Entry.serializer()), it) }
                    .let { postValue(it) }
            } catch (e: Throwable) {
                CrashReporter.recordException(e)
            }
        }
    }

    private fun Double.roundLatLng(): Double = (this * 100).roundToInt() / 100.0

    override suspend fun useRest(): Boolean = Config.getConfig().use_search_rest
}

class OpenPrayerTimesDayTimesEndpoint(val source: Source) : OpenPrayerTimesApi<Unit>() {
    suspend fun getDayTimes(id: String): List<DayTimes> {
        return try {
            get("/times/${source.name}/$id")
                .let { Json.decodeFromString(ListSerializer(DayTimes.serializer()), it) }
        } catch (e: Throwable) {
            CrashReporter.recordException(e)
            emptyList()
        }
    }


    override suspend fun useRest(): Boolean = when (source) {
        Source.Diyanet -> Config.getConfig().use_diyanet_rest
        Source.IGMG -> Config.getConfig().use_igmg_rest
        Source.NVC -> Config.getConfig().use_nvc_rest
        Source.London -> Config.getConfig().use_london_rest
        Source.Semerkand -> Config.getConfig().use_semerkand_rest
        else -> false
    }
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