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
package com.metinkale.prayer.times.alarm.sounds

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.asLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.utils.Store

@SuppressLint("NotifyDataSetChanged")
class SoundsAdapter(
    private val recyclerView: RecyclerView,
    soundsStore: Store<List<Sound>>
) : RecyclerView.Adapter<SoundsAdapter.ItemVH>() {
    private val layoutManager: LinearLayoutManager = LinearLayoutManager(recyclerView.context)

    private var volume = -1
    private var player: MyPlayer? = null
    var onItemClickListener: (ItemVH.(sound: Sound) -> Unit)? = null
    var onDelete: ((Sound) -> Unit)? = null
    var sounds: List<Sound> = soundsStore.current


    init {
        recyclerView.layoutManager = layoutManager
        soundsStore.data.asLiveData().observe(recyclerView.findViewTreeLifecycleOwner()!!) {
            sounds = it
            notifyDataSetChanged()
        }
    }


    override fun getItemId(position: Int): Long {
        return sounds[position].id.toLong()
    }

    fun getItem(i: Int): Sound {
        return sounds[i]
    }


    override fun onBindViewHolder(vh: ItemVH, position: Int) {
        val sound = sounds[position]
        vh.resetAudio()
        vh.radio.visibility = View.GONE
        vh.expand.visibility = View.GONE
        vh.delete.visibility = View.VISIBLE
        vh.text.text = sound.name
        vh.delete.setOnClickListener { onDelete?.invoke(sound) }
        vh.action.setOnClickListener { playPause(vh, sound) }


        vh.view.setOnClickListener {
            onItemClickListener?.invoke(vh, sounds[position])
        }
    }

    override fun getItemCount(): Int {
        return sounds.size
    }


    private fun playPause(vh: ItemVH, item: Sound) {
        if (vh.player == null) {
            resetAudios()
            vh.action.setImageResource(R.drawable.ic_pause)
            vh.text.visibility = View.INVISIBLE
            vh.seekbar.visibility = View.VISIBLE
            try {
                player =
                    MyPlayer.from(item).seekbar(vh.seekbar).volume(volume)
                        .play().onComplete { vh.resetAudio() }
                vh.player = player
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            vh.resetAudio()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
        return ItemVH(parent)
    }

    fun resetAudios() {
        val childCount = recyclerView.childCount
        var i = 0
        while (i < childCount) {
            val view = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(view) as ItemVH
            holder.resetAudio()
            ++i
        }
    }

    fun setVolume(volume: Int) {
        this.volume = volume
        player?.volume(volume)
    }

    class ItemVH internal constructor(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.sound_chooser_item, parent, false)
    ) {
        val view: View = itemView
        val action: ImageView = view.findViewById(R.id.action)
        val expand: ImageView = view.findViewById(R.id.expand)
        val delete: ImageView = view.findViewById(R.id.delete)
        val seekbar: SeekBar = view.findViewById(R.id.seekbar)
        val radio: RadioButton = view.findViewById(R.id.radioButton)
        val text: TextView = view.findViewById(R.id.text)
        var player: MyPlayer? = null

        init {
            action.setImageResource(R.drawable.ic_play)
        }

        fun resetAudio() {
            if (seekbar.visibility == View.VISIBLE) {
                action.setImageResource(R.drawable.ic_play)
            }
            seekbar.visibility = View.GONE
            text.visibility = View.VISIBLE
            if (player != null) {
                try {
                    player!!.stop()
                } catch (ignore: Exception) {
                }
                player = null
            }
        }
    }
}