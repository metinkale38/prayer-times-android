/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.times.alarm.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.SeekBar;

import com.metinkale.prayer.App;
import com.metinkale.prayer.times.alarm.Alarm;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class MyPlayer {

    //internal variabke
    private MediaPlayer[] mediaPlayers;
    private int volume = -1;
    private List<Sound> sound;

    private SeekBar seekbar;
    private Alarm alarm;
    private OnCompletionListener onComplete;

    public interface OnCompletionListener {
        void onComplete();
    }

    private MyPlayer() {
    }

    public static MyPlayer from(Alarm alarm) {
        if (alarm.getSounds() == null || alarm.getSounds().isEmpty()) return null;
        MyPlayer myPlayer = new MyPlayer();
        if (alarm.isRandomSound())
        {
            myPlayer.sound = alarm.getRandomSound();
        } else
        {
            myPlayer.sound = alarm.getSounds();
        }
        myPlayer.volume(alarm.getVolume());
        return myPlayer;
    }

    public static MyPlayer from(Sound sound) {
        MyPlayer myPlayer = new MyPlayer();
        myPlayer.sound = Collections.singletonList(sound);
        return myPlayer;
    }

    public MyPlayer volume(int volume) {
        if (isPlaying()) {
            if (this.volume >= 0 && volume >= 0) {
                AudioManager am = (AudioManager) App.get().getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            } else {
                stop();
            }
        }

        this.volume = volume;
        return this;
    }

    private int getStreamType() {
        switch (volume) {
            case Alarm.VOLUME_MODE_ALARM:
                return AudioManager.STREAM_ALARM;
            case Alarm.VOLUME_MODE_RINGTONE:
                return AudioManager.STREAM_RING;
            case Alarm.VOLUME_MODE_NOTIFICATION:
                return AudioManager.STREAM_NOTIFICATION;
            case Alarm.VOLUME_MODE_MEDIA:
            default:
                return AudioManager.STREAM_MUSIC;
        }
    }


    public MyPlayer play() {
        if (sound.size() == 0) return this;

        mediaPlayers = new MediaPlayer[sound.size()];
        for (int i = 0; i < sound.size(); i++) {
            Sound s = sound.get(i);
            mediaPlayers[i] = s.createMediaPlayer(alarm);

            mediaPlayers[i].setAudioStreamType(getStreamType());
            try {
                mediaPlayers[i].prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        for (int i = sound.size() - 2; i >= 0; i--) {
            mediaPlayers[i].setNextMediaPlayer(mediaPlayers[i + 1]);
        }


        final AudioManager am = (AudioManager) App.get().getSystemService(Context.AUDIO_SERVICE);

        final int streamType;
        final int oldvol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volume < 0) {
            streamType = getStreamType();
        } else {
            streamType = AudioManager.STREAM_MUSIC;
            am.setStreamVolume(streamType, volume, 0);
        }

        mediaPlayers[0].start();
        setupSeekbar();

        mediaPlayers[mediaPlayers.length - 1].setOnCompletionListener(mp -> {
            if (volume > 0) {
                am.setStreamVolume(streamType, oldvol, 0);
            }

            if (onComplete != null) {
                onComplete.onComplete();
            }

        });
        return this;
    }

    private void setupSeekbar() {
        MediaPlayer mediaPlayer = getMediaPlayer();
        if (mediaPlayer == null) return;
        if (seekbar != null) {
            seekbar.setMax(mediaPlayer.getDuration());

            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            seekbar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getMediaPlayer() != null && getMediaPlayer().isPlaying()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            seekbar.setProgress(getMediaPlayer().getCurrentPosition(), true);
                        } else {
                            seekbar.setProgress(getMediaPlayer().getCurrentPosition());
                        }
                        seekbar.postDelayed(this, 100);
                    }
                }
            }, 100);
        }
    }


    public void stop() {
        for (MediaPlayer mp : mediaPlayers) {
            mp.reset();
        }
        mediaPlayers = null;
        if (onComplete != null) {
            onComplete.onComplete();
        }
    }

    private MediaPlayer getMediaPlayer() {
        if (mediaPlayers == null) return null;
        for (MediaPlayer mp : mediaPlayers) {
            if (mp.isPlaying()) return mp;
        }
        return null;
    }

    public boolean isPlaying() {
        return getMediaPlayer() != null;
    }

    public MyPlayer seekbar(SeekBar seekbar) {
        this.seekbar = seekbar;
        return this;
    }

    public MyPlayer alarm(Alarm alarm) {
        this.alarm = alarm;
        return this;
    }

    public MyPlayer onComplete(OnCompletionListener onComplete) {
        this.onComplete = onComplete;
        return this;
    }
}
