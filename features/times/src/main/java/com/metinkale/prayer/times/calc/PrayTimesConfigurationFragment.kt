package com.metinkale.prayer.times.calc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.times.times.Times
import dev.metinkale.calctimes.CalcTimes
import dev.metinkale.openprayertimes.sources.Source
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
                    val model =
                        PrayTimesConfigurationViewModel(Source.Calc.deserializeCalcTimes(times.key ?: "")) {
                            Times.add(times.copy(key =Source.Calc.serializeCalcTimes(it), asrType = asrType.value))
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