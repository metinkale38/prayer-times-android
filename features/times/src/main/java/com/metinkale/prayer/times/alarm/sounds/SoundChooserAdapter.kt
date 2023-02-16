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

import android.app.ProgressDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.metinkale.prayer.App
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.alarm.sounds.SoundChooserAdapter.ItemVH
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.Utils
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class SoundChooserAdapter(
    private val recyclerView: RecyclerView,
    rootSounds: List<Sound>
) : RecyclerView.Adapter<ItemVH>() {
    private val layoutManager: LinearLayoutManager = LinearLayoutManager(recyclerView.context)
    private val sounds: MutableList<Sound> = ArrayList()
    private val rootSounds: MutableList<Sound>
    private var expanded: BundledSound? = null
    var selected: Sound? = null
        private set
    private var volume = -1
    private var player: MyPlayer? = null
    var onItemClickListener: (ItemVH.(sound: Sound) -> Unit)? = null

    init {
        recyclerView.layoutManager = layoutManager
        this.rootSounds = rootSounds.toMutableList()
        update()
    }

    fun update() {
        sounds.clear()
        rootSounds.sortWith { o1: Sound, o2: Sound ->
            val name1 = o1.name
            val name2 = o2.name
            name1!!.compareTo(name2!!)
        }
        sounds.addAll(rootSounds)
        val iter = sounds.listIterator()
        while (iter.hasNext()) {
            val o = iter.next()
            if (o === expanded) {
                val sounds: Collection<Sound> = o.subSounds.values
                for (sound in sounds) {
                    iter.add(sound)
                }
            }
        }
        sounds.removeAll(setOf<Any?>(null))
        notifyDataSetChanged()
    }

    fun getItem(i: Int): Sound {
        return sounds[i]
    }

    override fun getItemId(position: Int): Long {
        return sounds[position].id.toLong()
    }

    override fun onBindViewHolder(vh: ItemVH, position: Int) {
        val sound = sounds[position]
        vh.resetAudio()
        vh.radio.visibility = View.VISIBLE

        vh.delete.visibility = View.GONE

        if (sound is BundledSound) {
            vh.expand.visibility = View.VISIBLE
            vh.expand.setOnClickListener {
                expanded = if (expanded === sound) {
                    null
                } else {
                    sound
                }
                update()
                checkAllVisibilities()
            }
        } else {
            vh.expand.visibility = View.GONE
        }
        vh.text.text = sound.name
        if (rootSounds.contains(sound)) {
            vh.view.setPadding(0, 0, 0, 0)
        } else {
            val padding = Utils.convertDpToPixel(App.get(), 32f).toInt()
            vh.view.setPadding(padding, 0, padding, 0)
        }
        checkVisibility(sound, vh)
        checkCheckbox(sound, vh)
        checkAction(sound, vh)
        vh.action.setOnClickListener { v: View ->
            if (sound.isDownloaded) {
                playPause(vh, sound)
            } else {
                downloadSound(v, sound.name, sound.appSounds)
            }
        }
        vh.radio.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                selected = sound
                checkAllCheckboxes()
            }
        }
        vh.view.setOnClickListener {
            onItemClickListener?.invoke(vh, sounds[position])
        }
    }

    override fun getItemCount(): Int {
        return sounds.size
    }

    private fun checkAllVisibilities() {
        val childCount = recyclerView.childCount
        var i = 0
        while (i < childCount) {
            val view = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(view) as ItemVH
            if (holder.bindingAdapterPosition == -1) {
                ++i
                continue
            }
            checkVisibility(sounds[holder.bindingAdapterPosition], holder)
            ++i
        }
    }

    private fun checkAllCheckboxes() {
        val childCount = recyclerView.childCount
        var i = 0
        while (i < childCount) {
            val view = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(view) as ItemVH
            if (holder.bindingAdapterPosition == -1) {
                ++i
                continue
            }
            checkCheckbox(sounds[holder.bindingAdapterPosition], holder)
            ++i
        }
    }

    private fun checkAllActionButtons() {
        val childCount = recyclerView.childCount
        var i = 0
        while (i < childCount) {
            val view = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(view) as ItemVH
            if (holder.bindingAdapterPosition == -1) {
                ++i
                continue
            }
            checkAction(sounds[holder.bindingAdapterPosition], holder)
            ++i
        }
    }

    private fun checkVisibility(item: Sound, vh: ItemVH) {
        if (rootSounds.contains(item)) {
            vh.view.visibility = View.VISIBLE
            vh.expand.rotation = if (expanded === item) 180f else 0.toFloat()
        } else {
            vh.view.visibility =
                if (expanded != null && expanded!!.subSounds.containsValue(item)) View.VISIBLE else View.GONE
        }
    }

    private fun checkCheckbox(item: Sound, vh: ItemVH) {
        if (item === selected) {
            vh.radio.isChecked = true
            vh.staticRadio.visibility = View.GONE
        } else {
            vh.radio.isChecked = false
            if (selected is BundledSound) {
                if ((selected as BundledSound).subSounds.containsValue(item)) {
                    vh.staticRadio.visibility =
                        if ((selected as BundledSound).subSounds.containsValue(item)) View.VISIBLE else View.GONE
                }
            } else if (item is BundledSound) {
                vh.staticRadio.visibility =
                    if (item.subSounds.containsValue(selected)) View.VISIBLE else View.GONE
            }
        }
    }

    private fun checkAction(item: Sound, vh: ItemVH) {
        val dled = item.isDownloaded
        vh.radio.isEnabled = dled
        vh.action.setImageResource(if (dled) R.drawable.ic_play else R.drawable.ic_download)
    }

    private fun downloadSound(v: View, name: String?, items: Set<AppSound?>) {
        var size = 0
        for (item in items) {
            size += item!!.size
        }
        val ctx = v.context
        val dialog = AlertDialog.Builder(ctx).create()
        dialog.setTitle(R.string.sound)
        dialog.setMessage(ctx.getString(R.string.dlSound, name, LocaleUtils.readableSize(size)))
        dialog.setCancelable(false)
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            ctx.getString(R.string.yes)
        ) { _, _ ->
            val dlg = ProgressDialog(ctx)
            dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dlg.max = items.size * 100
            dlg.show()
            val iterator = items.iterator()
            val count = AtomicInteger(0)

            //bit tricky and hacky recursion using iterators :)
            val callback: FutureCallback<File?> = object : FutureCallback<File?> {
                var lastItem: Sound? = null
                override fun onCompleted(e: Exception?, result: File?) {
                    if (lastItem != null) {
                        count.incrementAndGet()
                        if (e != null || result == null || !result.exists()) {
                            if (e != null) {
                                e.printStackTrace()
                                recordException(e)
                            }
                            dlg.cancel()
                            Toast.makeText(App.get(), R.string.error, Toast.LENGTH_LONG).show()
                        }
                        checkAllActionButtons()
                    }
                    if (iterator.hasNext()) {
                        val item = iterator.next()
                        val f = item!!.file
                        f.parentFile?.mkdirs()
                        if (!item.isDownloaded) {
                            Ion.with(ctx)
                                .load(item.getUrl())
                                .progress { downloaded: Long, total: Long ->
                                    dlg.progress =
                                        (count.get() * 100 + downloaded * 100 / total).toInt()
                                }
                                .write(f)
                                .setCallback(this)
                        } else {
                            onCompleted(null, f)
                        }
                        lastItem = item
                    } else {
                        if (dlg.isShowing) dlg.dismiss()
                        checkAllActionButtons()
                    }
                }
            }
            callback.onCompleted(null, null)
        }
        dialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            ctx.getString(R.string.no)
        ) { dialog1: DialogInterface, _: Int -> dialog1.cancel() }
        dialog.show()
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


    class ItemVH internal constructor(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.sound_chooser_item, parent, false)
    ) {
        val view: View = itemView
        val action: ImageView = view.findViewById(R.id.action)
        val expand: ImageView = view.findViewById(R.id.expand)
        val delete: ImageView = view.findViewById(R.id.delete)
        val seekbar: SeekBar = view.findViewById(R.id.seekbar)
        val radio: RadioButton = view.findViewById(R.id.radioButton)
        val staticRadio: RadioButton = view.findViewById(R.id.staticRadioDisabled)
        val text: TextView = view.findViewById(R.id.text)
        var player: MyPlayer? = null

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