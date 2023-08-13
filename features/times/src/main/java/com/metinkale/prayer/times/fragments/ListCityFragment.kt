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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.Observer
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.times.OpenPrayerTimesListEndpoint
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.localizedName
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.utils.UUID
import dev.metinkale.prayertimes.core.router.ListResponse
import dev.metinkale.prayertimes.core.sources.Source
import java.util.*


class ListCityFragment : BaseActivity.MainFragment(), Observer<ListResponse> {

    private lateinit var listView: ListView
    private val listApi = OpenPrayerTimesListEndpoint()
    private lateinit var path: List<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        path = arguments?.getStringArray("path")?.toList() ?: emptyList()

        val v = inflater.inflate(R.layout.vakit_listcity, container, false)
        listView = v.findViewById(R.id.listView)

        listApi.list(path)
        listApi.observe(viewLifecycleOwner, this)
        return v
    }

    override fun onChanged(resp: ListResponse?) {
        if (resp is ListResponse.Items) {
            var list = resp.items

            if (path.isEmpty()) {
                list = list.map { Source.valueOf(it)?.fullName ?: it }
            } else if (path.size == 1) {
                list = list.map { Locale("", if (it == "EN") "GB" else it).displayCountry }
            }

            listView.adapter = ArrayAdapter(
                requireActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1, list
            )

            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
                moveToFrag(create(path + resp.items[pos]))
            }
        }else if(resp is ListResponse.Result){
            val entry = resp.entry
            Times.add(
                Times(
                    id = UUID.asInt(),
                    source = entry.source,
                    name = entry.localizedName(),
                    lat = entry.lat ?: 0.0,
                    lng = entry.lng ?: 0.0,
                    key = entry.id,
                    sortId = (Times.current.maxOfOrNull { it.sortId } ?: 0) + 1,
                    autoLocation = false
                )
            )
            backToMain()
        }
    }


    companion object {
        @JvmStatic
        fun create(path: List<String> = emptyList()): ListCityFragment {
            val bdl = Bundle()
            bdl.putStringArray("path", path.toTypedArray())
            val frag = ListCityFragment()
            frag.arguments = bdl
            return frag
        }
    }
}