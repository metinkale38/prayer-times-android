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

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.metinkale.prayer.App
import com.metinkale.prayer.times.alarm.Alarm
import java.io.IOException

class MyPlayer private constructor() {
    //internal variabke
    private var mediaPlayers: List<MediaPlayer> = listOf()
    private var volume = -1
    private var sound: List<Sound> = emptyList()
    private var onComplete: (() -> Unit)? = null
    private var seekbar: SeekBar? = null
    private var alarm: Alarm? = null


    fun volume(volume: Int): MyPlayer {
        if (isPlaying) {
            if (this.volume >= 0 && volume >= 0) {
                val am = App.get().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            } else {
                stop()
            }
        }
        this.volume = volume
        return this
    }

    private val streamType: Int
        get() = when (volume) {
            Alarm.VOLUME_MODE_ALARM -> AudioManager.STREAM_ALARM
            Alarm.VOLUME_MODE_RINGTONE -> AudioManager.STREAM_RING
            Alarm.VOLUME_MODE_NOTIFICATION -> AudioManager.STREAM_NOTIFICATION
            Alarm.VOLUME_MODE_MEDIA -> AudioManager.STREAM_MUSIC
            else -> AudioManager.STREAM_MUSIC
        }

    fun play(): MyPlayer {
        if (sound.isEmpty()) return this
        mediaPlayers = sound.indices.mapNotNull { i ->
            val s = sound[i]
            s.createMediaPlayer(alarm)?.also {
                it.setAudioStreamType(streamType)
                try {
                    it.prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
        for (i in sound.size - 2 downTo 0) {
            mediaPlayers[i].setNextMediaPlayer(mediaPlayers[i + 1])
        }
        val am = App.get().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        var streamType = 0
        val oldvol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (volume >= 0) {
            streamType = AudioManager.STREAM_MUSIC
            am.setStreamVolume(streamType, volume, 0)
        }
        mediaPlayers[0].start()
        setupSeekbar()
        mediaPlayers[mediaPlayers.size - 1].setOnCompletionListener {
            if (volume > 0) {
                am.setStreamVolume(streamType, oldvol, 0)
            }
            onComplete?.invoke()
        }
        return this
    }

    private fun setupSeekbar() {
        val mediaPlayer = mediaPlayer ?: return
        if (seekbar != null) {
            seekbar!!.max = mediaPlayer.duration
            seekbar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            seekbar!!.postDelayed(object : Runnable {
                override fun run() {
                    if (mediaPlayer.isPlaying) {
                        seekbar!!.setProgress(mediaPlayer.currentPosition, true)
                        seekbar!!.postDelayed(this, 100)
                    }
                }
            }, 100)
        }
    }

    fun stop() {
        for (mp in mediaPlayers) {
            mp.reset()
        }
        mediaPlayers = emptyList()
        onComplete?.also { onComplete = null }?.invoke()
    }

    private val mediaPlayer: MediaPlayer?
        get() = mediaPlayers.firstOrNull { it.isPlaying }

    val isPlaying: Boolean
        get() = mediaPlayer != null

    fun seekbar(seekbar: SeekBar): MyPlayer {
        this.seekbar = seekbar
        return this
    }

    fun alarm(alarm: Alarm): MyPlayer {
        this.alarm = alarm
        return this
    }

    fun onComplete(onComplete: () -> Unit): MyPlayer {
        this.onComplete = onComplete
        return this
    }

    companion object {
        fun from(alarm: Alarm): MyPlayer? {
            if (alarm.sounds.isEmpty()) return null
            val myPlayer = MyPlayer()
            myPlayer.sound = alarm.sounds
            myPlayer.volume(alarm.volume)
            return myPlayer
        }

        fun from(sound: Sound): MyPlayer {
            val myPlayer = MyPlayer()
            myPlayer.sound = listOf(sound)
            return myPlayer
        }
    }
}