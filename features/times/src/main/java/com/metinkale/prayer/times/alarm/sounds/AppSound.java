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

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import com.metinkale.prayer.CrashReporter;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.times.utils.MD5;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;


public class AppSound extends Sound {



    private String name;
    private String shortName;
    private final String md5;
    private final int size;
    private final String url;

    private transient boolean checkedMD5;

    AppSound(String name, int size, String md5, String url) {
        this.name = name;
        this.size = size;
        this.md5 = md5;
        this.url = url;
    }

    public int getId() {
        return url.hashCode();
    }

    @Override
    public Set<AppSound> getAppSounds() {
        return Collections.singleton(this);
    }

    public File getFile() {
        String path = url.substring(url.indexOf("/sounds/") + 8);

        File file = new File(App.get().getExternalFilesDir(
                Environment.DIRECTORY_MUSIC), path);
        file.getParentFile().mkdirs();
        return file;
    }


    public Uri getUri() {
        return Uri.fromFile(getFile());
    }

    private void checkMD5() {
        File file = getFile();
        if (file.exists()) {
            if (size != file.length() || (md5 != null && !MD5.checkMD5(md5, file))) {
                file.delete();
            }
        }
    }

    @Override
    public MediaPlayer createMediaPlayer(Alarm alarm) {
        if (!isDownloaded()) return null;
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(App.get(), getUri());
        } catch (IOException e) {
            CrashReporter.recordException(e);
            return null;
        }
        return mp;
    }

    public boolean isDownloaded() {
        if (!checkedMD5) {
            checkMD5();
        }
        checkedMD5 = getFile().exists();
        return checkedMD5;
    }


    public String getUrl() {
        return App.API_URL + url;
    }


    void setName(String name) {
        this.name = name;
    }

    void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    public String getMd5() {
        return md5;
    }

    @Override
    public int getSize() {
        return size;
    }
}
