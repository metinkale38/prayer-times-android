package com.metinkale.prayer.times.calc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.times.times.Times
import dev.metinkale.prayertimes.calc.PrayTimes
import kotlinx.serialization.json.Json

class PrayTimesConfigurationFragment : BaseActivity.MainFragment() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val times =
            requireArguments().getString("times")
                ?.let { Json.decodeFromString(Times.serializer(), it) }

        return if (times == null) {
            backToMain()
            View(requireContext())
        } else {
            ComposeView(requireContext()).apply {
                setContent {
                    val model = PrayTimesConfigurationViewModel( PrayTimes.deserialize(times.id ?: "")){
                        times.copy(id = it.serialize()).save()
                        backToMain()
                    }

                    PrayTimesConfigurationView(model)
                }
            }
        }
    }

    companion object {
        fun from(time: Times): PrayTimesConfigurationFragment {
            val bdl = Bundle()
            bdl.putString("times", Json.encodeToString(Times.serializer(), time))
            val frag = PrayTimesConfigurationFragment()
            frag.arguments = bdl
            return frag
        }
    }
}