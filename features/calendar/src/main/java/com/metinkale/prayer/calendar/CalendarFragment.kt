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
package com.metinkale.prayer.calendar

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.date.HijriDate.Companion.MAX_GREG_YEAR
import com.metinkale.prayer.date.HijriDate.Companion.MAX_HIJRI_YEAR
import com.metinkale.prayer.date.HijriDate.Companion.fromLocalDate
import com.metinkale.prayer.date.HijriDate.Companion.now
import com.metinkale.prayer.date.HijriDay
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.LocaleUtils.getLanguage
import com.metinkale.prayer.utils.LocaleUtils.locale
import java.time.LocalDate
import java.util.*

class CalendarFragment : BaseActivity.MainFragment(), OnItemClickListener {
    private lateinit var adapter: PagerAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.calendar_main, container, false)
        adapter = HijriPagerAdapter(childFragmentManager)
        viewPager = v.findViewById(R.id.pager)
        viewPager.adapter = adapter
        viewPager.currentItem = now().year - MIN_HIJRI
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.calendar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.switchHijriGreg) {
            if (adapter is HijriPagerAdapter) {
                adapter = GregorianPagerAdapter(childFragmentManager)
                viewPager.adapter = adapter
                viewPager.currentItem = LocalDate.now().year - MIN_GREG
            } else {
                adapter = HijriPagerAdapter(childFragmentManager)
                viewPager.adapter = adapter
                viewPager.currentItem = now().year - MIN_HIJRI
            }
            adapter.notifyDataSetChanged()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(arg0: AdapterView<*>?, v: View, pos: Int, arg3: Long) {
        val hijriday = v.tag as? HijriDay
        hijriday?.assetPath?.let { path ->
            val asset = getLanguage("en", "de", "tr") + path
            if (Locale("de").language == locale.language || Locale("tr").language == locale.language) {
                val bdl = Bundle()
                bdl.putString("asset", asset)
                val frag: Fragment = WebViewFragment()
                frag.arguments = bdl
                moveToFrag(frag)
            }
        }
    }

    class YearFragment : Fragment() {
        private var year = 0
        private var isHijri = false
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.calendar_frag, container, false)
            val listView = view.findViewById<ListView>(android.R.id.list)
            year = requireArguments().getInt(YEAR)
            isHijri = requireArguments().getBoolean(IS_HIJRI)
            listView.adapter = Adapter(requireActivity(), year, isHijri)
            listView.onItemClickListener = parentFragment as? OnItemClickListener
            return view
        }

        companion object {
            const val YEAR = "year"
            const val IS_HIJRI = "isHijri"
        }
    }

    class GregorianPagerAdapter internal constructor(fm: FragmentManager?) : FragmentPagerAdapter(
        fm!!
    ) {
        override fun getItem(position: Int): Fragment {
            var pos = position
            if (locale.language == Locale("ar").language) pos = count - pos - 1
            val fragment: Fragment = YearFragment()
            val args = Bundle()
            args.putInt(YearFragment.YEAR, pos + MIN_GREG)
            args.putBoolean(YearFragment.IS_HIJRI, false)
            fragment.arguments = args
            return fragment
        }

        override fun getCount(): Int {
            return MAX_GREG_YEAR - MIN_GREG + 1
        }

        override fun getPageTitle(position: Int): CharSequence {
            var pos = position
            if (locale.language == Locale("ar").language) pos = count - pos - 1
            return LocaleUtils.formatNumber(pos + MIN_GREG)
        }

        override fun getItemId(position: Int): Long {
            return (position + MIN_GREG).toLong()
        }
    }

    class HijriPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            var pos = position
            if (locale.language == Locale("ar").language) pos = count - pos - 1
            val fragment: Fragment = YearFragment()
            val args = Bundle()
            args.putInt(YearFragment.YEAR, pos + MIN_HIJRI)
            args.putBoolean(YearFragment.IS_HIJRI, true)
            fragment.arguments = args
            return fragment
        }

        override fun getCount(): Int {
            return MAX_HIJRI_YEAR - MIN_HIJRI + 1
        }

        override fun getPageTitle(position: Int): CharSequence {
            var pos = position
            if (locale.language == Locale("ar").language) pos = count - pos - 1
            return LocaleUtils.formatNumber(pos + MIN_HIJRI)
        }

        override fun getItemId(position: Int): Long {
            return (position + MIN_HIJRI).toLong()
        }
    }

    companion object {
        private const val MIN_GREG = 1900
        private val MIN_HIJRI = fromLocalDate(LocalDate.of(MIN_GREG, 1, 1)).year
    }
}