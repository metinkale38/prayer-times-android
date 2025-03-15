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
package com.metinkale.prayer.intro

import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.Module
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.base.BuildConfig
import com.metinkale.prayer.intro.R.string
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.utils.LocaleUtils
import dev.metinkale.prayertimes.calc.Method
import dev.metinkale.prayertimes.calc.PrayTimes
import dev.metinkale.prayertimes.providers.sources.Source
import java.util.*

/**
 * Created by metin on 17.07.2017.
 */
class MainActivity : AppCompatActivity(), OnPageChangeListener, View.OnClickListener {
    private lateinit var mPager: ViewPager
    private val colors by lazy {
        intArrayOf(
            ContextCompat.getColor(this, R.color.colorPrimary),
            ContextCompat.getColor(this, R.color.accent),
            ContextCompat.getColor(this, R.color.colorPrimaryDark),
            -0xc0ae4b,
            -0xff432c
        )
    }
    private val fragClz = listOf<Class<*>>(
        LanguageFragment::class.java,
        ChangelogFragment::class.java,
        MenuIntroFragment::class.java,
        PagerIntroFragment::class.java,
        ConfigIntroFragment::class.java
    )
    private lateinit var fragments: Array<IntroFragment?>
    private lateinit var adapter: MyAdapter
    private lateinit var main: View
    private lateinit var forward: Button
    private lateinit var back: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.init(this)
        createTemproraryTimes()
        setContentView(R.layout.intro_main)
        mPager = findViewById(R.id.pager)
        main = findViewById(R.id.main)
        back = findViewById(R.id.back)
        forward = findViewById(R.id.forward)
        mPager.offscreenPageLimit = 10
        back.setOnClickListener(this)
        forward.setOnClickListener(this)
        fragments = arrayOfNulls(fragClz.size)
        for (i in fragClz.indices) {
            try {
                fragments[i] = fragClz[i].newInstance() as IntroFragment
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            }
        }
        var doNotShow = 0
        for (i in fragments.indices) {
            if (fragments[i]?.shouldShow() == false) {
                fragments[i] = null
                doNotShow++
            }
        }
        val newArray = arrayOfNulls<IntroFragment>(fragments.size - doNotShow + 1)
        if (newArray.size == 1) startActivity(Intent(this, BaseActivity::class.java))
        var i = 0
        for (frag in fragments) {
            if (frag != null) {
                newArray[i++] = frag
            }
        }
        newArray[i] = LastFragment()
        fragments = newArray
        adapter = MyAdapter(supportFragmentManager)
        mPager.adapter = adapter
        mPager.addOnPageChangeListener(this)
    }

    private fun createTemproraryTimes() {
        if (Times.current.size < 2) {
            if (Locale.getDefault().language == Locale("tr").language) {
                buildTemporaryTimes(
                    "Mekke",
                    21.4260221,
                    39.8296538,
                    -1,
                    TimeZone.getTimeZone("Asia/Riyadh")
                )
                buildTemporaryTimes(
                    "Kayseri",
                    38.7333333333333,
                    35.4833333333333,
                    -2,
                    TimeZone.getTimeZone("Turkey")
                )
            } else if (Locale.getDefault().language == Locale("de").language) {
                buildTemporaryTimes(
                    "Mekka",
                    21.4260221,
                    39.8296538,
                    -1,
                    TimeZone.getTimeZone("Asia/Riyadh")
                )
                buildTemporaryTimes(
                    "Braunschweig",
                    52.2666666666667,
                    10.5166666666667,
                    -2,
                    TimeZone.getTimeZone("Europe/Berlin")
                )
            } else if (Locale.getDefault().language == Locale("fr").language) {
                buildTemporaryTimes(
                    "Mecque",
                    21.4260221,
                    39.8296538,
                    -1,
                    TimeZone.getTimeZone("Asia/Riyadh")
                )
                buildTemporaryTimes(
                    "Paris",
                    48.8566101,
                    2.3514992,
                    -2,
                    TimeZone.getTimeZone("Europe/Paris")
                )
            } else {
                buildTemporaryTimes(
                    "Mecca",
                    21.4260221,
                    39.8296538,
                    -1,
                    TimeZone.getTimeZone("Asia/Riyadh")
                )
                buildTemporaryTimes(
                    "London",
                    51.5073219,
                    -0.1276473,
                    -2,
                    TimeZone.getTimeZone("Europe/London")
                )
            }
        }
    }

    private fun buildTemporaryTimes(
        name: String,
        lat: Double,
        lng: Double,
        id: Int,
        timeZone: TimeZone?
    ) {
        Times.add(
            Times(
                id = id, name = name, source = Source.Companion.Calc, key = PrayTimes(
                    lat, lng, 0.0, kotlinx.datetime.TimeZone.of(timeZone?.id ?: "UTC"), Method.MWL
                ).serialize()
            )
        )
    }

    override fun recreate() {
        try {
            mPager.adapter = adapter //force reinflating of fragments
        } catch (e: Exception) {
            super.recreate()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val color1 = colors[position % colors.size]
        val color2 = colors[(position + 1) % colors.size]
        main.setBackgroundColor(blendColors(color1, color2, 1 - positionOffset))
        if (position > 0 && positionOffset == 0f) fragments[position]?.setPagerPosition(1f)
        fragments[position]?.setPagerPosition(positionOffset)
        if (fragments.size > position + 1) fragments[position + 1]?.setPagerPosition(1 - positionOffset)
    }

    override fun onPageSelected(position: Int) {
        back.visibility = if (position == 0) View.GONE else View.VISIBLE
        forward.setText(if (position == adapter.count - 1) string.finish else string.continue_)
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onClick(view: View) {
        val i1 = view.id
        if (i1 == R.id.back) {
            val pos = mPager.currentItem
            if (pos > 0) mPager.setCurrentItem(pos - 1, true)
        } else if (i1 == R.id.forward) {
            val pos = mPager.currentItem
            if (pos < adapter.count - 1) mPager.setCurrentItem(pos + 1, true) else {
                Times.clearTemporaryTimes()
                finish()
                Preferences.CHANGELOG_VERSION = BuildConfig.CHANGELOG_VERSION
                Preferences.SHOW_INTRO = false
                val bdl = Bundle()
                if (Times.current.isEmpty()) {
                    bdl.putBoolean("openCitySearch", true)
                }
                Module.TIMES.launch(this, bdl)
                var appName = "appName"
                val lang = Preferences.LANGUAGE
                if (!lang.isEmpty() && lang != "system") {
                    appName += lang[0].uppercaseChar().toString() + lang.substring(1)
                }
                val icon = ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
                val intent = Intent()
                val launchIntent = Intent(this, BaseActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                launchIntent.putExtra("duplicate", false)
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                intent.putExtra(
                    Intent.EXTRA_SHORTCUT_NAME,
                    getString(getStringResId(appName, string.appName))
                )
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
                intent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
                sendBroadcast(intent)
            }
        }
    }

    private fun getStringResId(resName: String, def: Int): Int {
        return try {
            val f = String::class.java.getDeclaredField(resName)
            f.getInt(null)
        } catch (e: IllegalAccessException) {
            def
        } catch (e: NoSuchFieldException) {
            def
        }
    }

    fun getBackgroundColor(clz: Class<*>): Int {
        return colors[fragClz.indexOf(clz)]
    }

    private inner class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]!!
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }

    companion object {
        @JvmStatic
        fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRation = 1f - ratio
            val r = Color.red(color1) * ratio + Color.red(color2) * inverseRation
            val g = Color.green(color1) * ratio + Color.green(color2) * inverseRation
            val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRation
            return Color.rgb(r.toInt(), g.toInt(), b.toInt())
        }
    }
}