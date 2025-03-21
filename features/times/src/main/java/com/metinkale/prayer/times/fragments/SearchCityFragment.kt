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
package com.metinkale.prayer.times.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.metinkale.prayer.App
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.times.*
import com.metinkale.prayer.times.calc.PrayTimesConfigurationFragment
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.utils.PermissionUtils
import com.metinkale.prayer.utils.UUID
import dev.metinkale.prayertimes.providers.Entry
import dev.metinkale.prayertimes.providers.SearchEntry
import dev.metinkale.prayertimes.providers.sources.Source
import kotlinx.coroutines.launch
import java.util.*

class SearchCityFragment : BaseActivity.MainFragment(), OnItemClickListener,
    SearchView.OnQueryTextListener, LocationListener, View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, Observer<List<Entry>> {
    private lateinit var adapter: MyAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var searchItem: MenuItem
    private lateinit var autoLocation: SwitchCompat
    private var location: Location? = null
    private val entries = MutableLiveData<List<Entry>>()

    // prevent duplicate permission request
    private var askedForPermission = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.vakit_searchcity, container, false)
        autoLocation = v.findViewById(R.id.autoLocation)
        autoLocation.setOnCheckedChangeListener(this)
        var trackStates = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()), intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.white),
                ContextCompat.getColor(requireContext(), R.color.backgroundSecondary)
            )
        )
        autoLocation.thumbTintList = trackStates
        autoLocation.thumbTintMode = PorterDuff.Mode.MULTIPLY
        trackStates = ColorStateList(
            arrayOf(intArrayOf()), intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            )
        )
        autoLocation.trackTintList = trackStates
        autoLocation.trackTintMode = PorterDuff.Mode.MULTIPLY
        fab = v.findViewById(R.id.search)
        fab.setOnClickListener(this)
        val listView = v.findViewById<ListView>(R.id.listView)
        listView.isFastScrollEnabled = true
        listView.onItemClickListener = this
        val csv = View.inflate(activity, R.layout.vakit_searchcity_addcsv, null)
        csv.setOnClickListener { addFromCSV() }
        listView.addFooterView(csv)
        adapter = MyAdapter(requireContext())
        listView.adapter = adapter


        v.findViewById<View>(R.id.chooseByList)
            .setOnClickListener { moveToFrag(ListCityFragment.create()) }

        entries.observe(viewLifecycleOwner, this)
        return v
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    override fun onPause() {
        if (PermissionUtils.get(requireActivity()).pLocation) {
            val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.removeUpdates(this)
        }
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.get(requireActivity()).pLocation) {
            checkLocation()
        }
    }

    private fun checkLocation() {

        if (PermissionUtils.get(requireActivity()).pLocation) {
            val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var loc: Location? = null
            val providers = lm.getProviders(true)
            for (provider in providers) {
                val last = lm.getLastKnownLocation(provider!!)
                // one hour==1meter in accuracy
                if (last != null && (loc == null || last.accuracy - last.time / (1000 * 60 * 60).toFloat() < loc.accuracy - loc.time / (1000 * 60 * 60).toFloat())) {
                    loc = last
                }
            }
            loc?.let { onLocationChanged(it) }
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE
            criteria.isAltitudeRequired = false
            criteria.isBearingRequired = false
            criteria.isCostAllowed = false
            criteria.isSpeedRequired = true
            val provider = lm.getBestProvider(criteria, true)
            if (provider != null) {
                lm.requestSingleUpdate(provider, this, null)
            }
        } else if (!askedForPermission) {
            askedForPermission = true
            PermissionUtils.get(requireActivity()).needLocation(requireActivity())
        }
    }

    override fun onClick(view: View) {
        if (view === fab) {
            MenuItemCompat.collapseActionView(searchItem)
            MenuItemCompat.expandActionView(searchItem)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search, menu)
        searchItem = menu.findItem(R.id.menu_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.performClick()
        searchView.setOnQueryTextListener(this)
    }

    override fun onItemClick(arg0: AdapterView<*>?, arg1: View, pos: Int, index: Long) {
        adapter.getItem(pos)?.let {
            if (it.source != Source.Calc) {
                Times.add(
                    Times(
                        id = UUID.asInt(),
                        source = it.source,
                        name = it.localizedName,
                        lat = it.lat ?: 0.0,
                        lng = it.lng ?: 0.0,
                        key = it.id,
                        sortId = (Times.current.maxOfOrNull { it.sortId } ?: 0) + 1,
                        autoLocation = autoLocation.isChecked
                    )
                )
                back()
            } else {
                moveToFrag(
                    PrayTimesConfigurationFragment.from(
                        Times(
                            id = UUID.asInt(),
                            source = it.source,
                            name = it.localizedName,
                            lat = it.lat ?: 0.0,
                            lng = it.lng ?: 0.0,
                            key = it.id,
                            sortId = (Times.current.maxOfOrNull { it.sortId } ?: 0) + 1,
                            autoLocation = autoLocation.isChecked
                        )
                    )
                )
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        autoLocation.isChecked = false

        lifecycleScope.launch {
            val result = tracker {
                query?.let { SearchEntry.search(query) } ?: emptyList()
            }
            entries.postValue(result)
        }

        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }

    override fun onLocationChanged(loc: Location) {
        location = loc
        if (adapter.count <= 1) {
            autoLocation()
        }

        if (PermissionUtils.get(requireContext()).pLocation) {
            autoLocation.isChecked = true
        }
    }

    private fun autoLocation() {
        adapter.clear()
        if (location == null) {
            adapter.notifyDataSetChanged()
            return
        }

        location?.let { location ->
            lifecycleScope.launch {
                val result = tracker {
                    SearchEntry.search(
                        location.latitude,
                        location.longitude
                    )
                }
                entries.postValue(result)
            }
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private val csvResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result?.data?.data?.also { uri ->
                    val contentResolver = App.get().contentResolver

                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)

                    Times.add(
                        Times(
                            source = Source.CSV,
                            name = "CSV",
                            key = uri.toString()
                        )
                    )
                    back()
                }
            }
        }

    private fun addFromCSV() {
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        builder.setTitle(R.string.addFromCSV)
            .setItems(R.array.addFromCSV) { _: DialogInterface?, which: Int ->
                if (which == 0) {

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/csv"
                    }


                    csvResult.launch(intent)


                } else {
                    val alert = AlertDialog.Builder(
                        requireActivity()
                    )
                    val editText = EditText(activity)
                    editText.hint = "http(s)://example.com/prayertimes.csv"
                    alert.setView(editText)
                    alert.setTitle(R.string.csvFromURL)
                    alert.setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                        val url = editText.text.toString()
                        var name = url.substring(url.lastIndexOf("/") + 1)
                        if (name.contains(".")) name = name.substring(0, name.lastIndexOf("."))
                        Times.add(Times(source = Source.CSV, name = name, key = url))
                        back()
                    }
                    alert.setNegativeButton(R.string.cancel, null)
                    alert.show()
                }
            }
        builder.show()
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        if (PermissionUtils.get(requireContext()).pLocation) {
            if (b) autoLocation()
            adapter.notifyDataSetChanged()
        } else {
            autoLocation.isChecked = false
            PermissionUtils.get(requireContext()).needLocation(requireActivity())
        }
    }


    override fun onChanged(entries: List<Entry>) {
        adapter.clear()
        adapter.addAll(entries)
        adapter.notifyDataSetChanged()
    }

    private inner class MyAdapter(context: Context) :
        ArrayAdapter<Entry>(context, 0, 0) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val vh: ViewHolder
            if (convertView == null) {
                convertView = View.inflate(parent.context, R.layout.vakit_searchcity_row, null)
                vh = ViewHolder(convertView)
                convertView.tag = vh
            } else {
                vh = convertView.tag as ViewHolder
            }
            val i = getItem(position)!!
            vh.city.text = i.localizedName
            vh.country.text = Locale("", i.country).displayCountry
            vh.sourcetxt.text = i.source.name
            if (i.source.drawableId == 0) {
                vh.source.visibility = View.INVISIBLE
            } else {
                vh.source.setImageResource(i.source.drawableId ?: 0)
                vh.source.visibility = View.VISIBLE
            }
            if (autoLocation.isChecked) vh.gpsIcon.visibility =
                View.VISIBLE else vh.gpsIcon.visibility = View.GONE
            return convertView!!
        }
    }

    data class ViewHolder(
        val country: TextView,
        val city: TextView,
        val sourcetxt: TextView,
        val source: ImageView,
        val gpsIcon: ImageView
    ) {
        constructor(v: View) : this(
            v.findViewById(R.id.country),
            v.findViewById(R.id.city),
            v.findViewById(R.id.sourcetext),
            v.findViewById(R.id.source),
            v.findViewById(R.id.gps)
        )
    }
}