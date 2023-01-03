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

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.fragments.AlarmsAdapter.MyViewHolder

internal class AlarmsAdapter : RecyclerView.Adapter<MyViewHolder?>() {
    private val items: MutableList<Any> = ArrayList()
    fun add(o: Any) {
        items.add(o)
        notifyDataSetChanged()
    }

    fun getItem(i: Int): Any {
        return items[i]
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is SwitchItem) {
            return TYPE_SWITCH
        }
        if (o is Button) {
            return TYPE_BUTTON
        }
        return if (o is String) {
            TYPE_TITLE
        } else -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (viewType == TYPE_SWITCH || viewType == TYPE_BUTTON) {
            return SwitchVH(parent)
        }
        return if (viewType == TYPE_TITLE) {
            TitleVH(parent)
        } else MyViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_SWITCH -> (holder as SwitchVH).setSwitchItem(
                items[position] as SwitchItem
            )
            TYPE_BUTTON -> (holder as SwitchVH).setSwitchItem(items[position] as Button)
            TYPE_TITLE -> (holder as TitleVH).setTitle(items[position] as String)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun clearAllItems() {
        items.clear()
        notifyDataSetChanged()
    }

    abstract class Button {
        abstract val name: String?
        abstract fun onClick()
        open fun onLongClick(): Boolean {
            return false
        }
    }

    abstract class SwitchItem {
        abstract val name: String?
        abstract fun onChange(checked: Boolean)
        abstract val checked: Boolean
        open fun onClick() {
            onChange(!checked)
        }

        open fun onLongClick(): Boolean {
            return false
        }
    }

    internal class SwitchVH(parent: ViewGroup) : MyViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.vakit_notprefs_switch, parent, false)
    ) {
        private val title: TextView
        private val switch: SwitchCompat = view.findViewById(R.id.switchView)

        init {
            title = view.findViewById(R.id.titleView)
        }

        fun setSwitchItem(item: SwitchItem) {
            title.text = item.name
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = item.checked
            switch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                item.onChange(
                    isChecked
                )
            }
            title.setOnClickListener { v: View? -> item.onClick() }
            title.setOnLongClickListener { v: View? -> item.onLongClick() }
            switch.visibility = View.VISIBLE
            title.gravity = Gravity.START or Gravity.CENTER
        }

        fun setSwitchItem(item: Button) {
            title.text = item.name
            title.setOnClickListener { v: View? -> item.onClick() }
            title.setOnLongClickListener { v: View? -> item.onLongClick() }
            title.gravity = Gravity.CENTER
            switch.visibility = View.GONE
        }
    }

    internal class TitleVH(parent: ViewGroup) : MyViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.vakit_alarm_config_dialog, parent, false)
    ) {
        var text: TextView = view as TextView

        fun setTitle(title: String?) {
            text.text = title
        }
    }

    internal open class MyViewHolder(val view: View) : RecyclerView.ViewHolder(
        view
    )

    companion object {
        private const val TYPE_SWITCH = 0
        private const val TYPE_TITLE = 1
        private const val TYPE_BUTTON = 2
    }
}