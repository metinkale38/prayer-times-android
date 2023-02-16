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

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.times.Times
import java.util.*

class SortFragment : Fragment() {
    private lateinit var adapter: MyAdapter
    private lateinit var act: TimesFragment
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var callback: SimpleItemTouchHelperCallback

    private var deleteMode = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        bdl: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.vakit_sort_main, container, false)
        val recyclerMan = v.findViewById<RecyclerView>(R.id.list)
        adapter = MyAdapter()
        recyclerMan.adapter = adapter
        val linLayMan = LinearLayoutManager(context)
        recyclerMan.layoutManager = linLayMan
        recyclerMan.setHasFixedSize(true)
        callback = SimpleItemTouchHelperCallback()
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerMan)

        setHasOptionsMenu(true)
        Times.asLiveData().observe(viewLifecycleOwner, adapter)
        adapter.onChanged(Times.current)
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sort_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.delete) {
            deleteMode = !deleteMode
            item.setIcon(R.drawable.ic_action_delete)
            adapter.notifyDataSetChanged()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        deleteMode = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = parentFragment as TimesFragment
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val ids = Times.current.map { it.id }.toMutableList()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(ids, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(ids, i, i - 1)
            }
        }

        ids.forEachIndexed { index, key ->
            Times.getTimesById(key).update { it?.copy(sortId = index) }
        }
        adapter.notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemDismiss(position: Int) {
        val times = Times.getTimesByIndex(position)
        val dialog = AlertDialog.Builder(requireActivity()).create()
        dialog.setTitle(R.string.delete)
        dialog.setMessage(getString(R.string.delCityConfirm, times.current?.name))
        dialog.setCancelable(false)
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.yes)
        ) { _: DialogInterface?, _: Int ->
            times.current?.delete()
            adapter.notifyItemRemoved(position)
        }
        dialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.no)
        ) { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.cancel()
            adapter.notifyDataSetChanged()
        }
        dialog.show()
    }

    internal class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var city: TextView
        var source: TextView
        var handler: View
        var delete: View
        var gps: View

        init {
            city = v.findViewById(R.id.city)
            source = v.findViewById(R.id.source)
            handler = v.findViewById(R.id.handle)
            delete = v.findViewById(R.id.delete)
            gps = v.findViewById(R.id.gps)
        }
    }

    private inner class MyAdapter : RecyclerView.Adapter<ViewHolder>(), Observer<List<Times>> {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(context).inflate(R.layout.vakit_sort_item, parent, false)
            return ViewHolder(v)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(vh: ViewHolder, position: Int) {
            val c = getItem(position) ?: return
            vh.city.text = c.name
            vh.source.text = c.source.name
            vh.gps.visibility = if (c.autoLocation) View.VISIBLE else View.GONE
            vh.delete.visibility = if (deleteMode) View.VISIBLE else View.GONE
            vh.delete.setOnClickListener { onItemDismiss(vh.bindingAdapterPosition) }
            vh.handler.setOnTouchListener { _: View?, event: MotionEvent? ->
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(vh)
                }
                false
            }
            vh.itemView.setOnClickListener { act.onItemClick(vh.bindingAdapterPosition) }
        }

        fun getItem(pos: Int): Times? {
            return Times.getTimesByIndex(pos).current
        }

        override fun getItemId(position: Int): Long {
            return getItem(position)!!.id.toLong()
        }

        override fun getItemCount(): Int {
            return Times.current.size
        }

        override fun onChanged(times: List<Times>) {
            if (callback.state == ItemTouchHelper.ACTION_STATE_IDLE) {
                notifyDataSetChanged()
            }
        }
    }

    inner class SimpleItemTouchHelperCallback : ItemTouchHelper.Callback() {

        var state: Int? = null

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.END
            return makeMovementFlags(dragFlags, swipeFlags)
        }


        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            onItemMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onItemDismiss(viewHolder.bindingAdapterPosition)
        }

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            state = actionState
            // We only want the active item
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is ViewHolder) {
                    viewHolder.itemView.setBackgroundColor(
                        mixColor(
                            ContextCompat.getColor(requireActivity(), R.color.background),
                            ContextCompat.getColor(requireActivity(), R.color.backgroundSecondary)
                        )
                    )
                }
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if (viewHolder is ViewHolder) {
                viewHolder.itemView.setBackgroundResource(R.color.background)
            }
        }
    }

    companion object {
        private fun mixColor(color1: Int, color2: Int): Int {
            val inverseRation = 1f - 0.5.toFloat()
            val r = Color.red(color1) * 0.5.toFloat() + Color.red(color2) * inverseRation
            val g = Color.green(color1) * 0.5.toFloat() + Color.green(color2) * inverseRation
            val b = Color.blue(color1) * 0.5.toFloat() + Color.blue(color2) * inverseRation
            return Color.rgb(r.toInt(), g.toInt(), b.toInt())
        }
    }
}