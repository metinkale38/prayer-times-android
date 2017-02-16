/*
 * Copyright (c) 2016 Metin Kale
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
 *
 */

package com.metinkale.prayerapp.vakit.sounds;

import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.utils.MD5;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.other.Vakit;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Sounds {
    @NonNull
    private static SimpleArrayMap<String, List<Sound>> sSounds = new SimpleArrayMap<>();

    public static boolean isDownloaded(@NonNull Sound sound) {
        return (sound.url == null) || sound.getFile().exists();

    }

    @NonNull
    public static SimpleArrayMap<String, List<Sound>> getSounds() {

        if (sSounds.isEmpty()) {
            List<Sound> sabah = new ArrayList<>();
            List<Sound> ogle = new ArrayList<>();
            List<Sound> ikindi = new ArrayList<>();
            List<Sound> aksam = new ArrayList<>();
            List<Sound> yatsi = new ArrayList<>();
            List<Sound> sela = new ArrayList<>();
            List<Sound> dua = new ArrayList<>();
            List<Sound> ezan = new ArrayList<>();


            aksam.add(new Sound("Abdulkadir Sehitoglu - Segah", App.API_URL + "/sounds/aksam/Abdulkadir%20Sehitoglu%20-%20Segah.mp3", "1126kB"));
            aksam.add(new Sound("Ahmet Sahin - Segah", App.API_URL + "/sounds/aksam/Ahmet%20Sahin%20-%20Segah.mp3", "1226kB"));
            aksam.add(new Sound("Bekir Buyukbas - Segah", App.API_URL + "/sounds/aksam/Bekir%20Buyukbas%20-%20Segah.mp3", "1460kB"));
            aksam.add(new Sound("Ebubekir Atalay - Segah", App.API_URL + "/sounds/aksam/Ebubekir%20Atalay%20-%20Segah.mp3", "896kB"));
            aksam.add(new Sound("Fatih Koca - Segah", App.API_URL + "/sounds/aksam/Fatih%20Koca%20-%20Segah.mp3", "1124kB"));
            aksam.add(new Sound("Ilhan Tok - Segah", App.API_URL + "/sounds/aksam/Ilhan%20Tok%20-%20Segah.mp3", "862kB"));
            aksam.add(new Sound("Ismail Cosar - Segah", App.API_URL + "/sounds/aksam/Ismail%20Cosar%20-%20Segah.mp3", "1292kB"));
            aksam.add(new Sound("Nurettin Okumus - Segah", App.API_URL + "/sounds/aksam/Nurettin%20Okumus%20-%20Segah.mp3", "1270kB"));
            dua.add(new Sound("Abdulkadir Sehitoglu - Ezan Duasi", App.API_URL + "/sounds/dua/Abdulkadir%20Sehitoglu%20-%20Ezan%20Duasi.mp3", "1098kB"));
            ezan.add(new Sound("Ghazi A-Sa'adoni", App.API_URL + "/sounds/ezan/Ghazi%20A-Sa-adoni.mp3", "1106kB"));
            ezan.add(new Sound("Hamad Deghreri", App.API_URL + "/sounds/ezan/Hamad%20Deghreri.mp3", "1206kB"));
            ezan.add(new Sound("Ibrahim Al-Arkani", App.API_URL + "/sounds/ezan/Ibrahim%20Al-Arkani.mp3", "1820kB"));
            ezan.add(new Sound("Majed Al-hamathani", App.API_URL + "/sounds/ezan/Majed%20Al-hamathani.mp3", "1706kB"));
            ezan.add(new Sound("Malek chebae - Takbir", App.API_URL + "/sounds/ezan/Malek%20chebae%20-%20Takbir.mp3", "210kB"));
            ezan.add(new Sound("Malek chebae", App.API_URL + "/sounds/ezan/Malek%20chebae.mp3", "1032kB"));
            ezan.add(new Sound("Mansoor Az-Zahrani", App.API_URL + "/sounds/ezan/Mansoor%20Az-Zahrani.mp3", "1440kB"));
            ezan.add(new Sound("Mishary Alafasi", App.API_URL + "/sounds/ezan/Mishary%20Alafasi.mp3", "1818kB"));
            ezan.add(new Sound("Nasser Alqatami", App.API_URL + "/sounds/ezan/Nasser%20Alqatami.mp3", "1896kB"));
            ikindi.add(new Sound("Abdulkadir Sehitoglu - Rast", App.API_URL + "/sounds/ikindi/Abdulkadir%20Sehitoglu%20-%20Rast.mp3", "1946kB"));
            ikindi.add(new Sound("Ahmet Sahin - Rast", App.API_URL + "/sounds/ikindi/Ahmet%20Sahin%20-%20Rast.mp3", "2022kB"));
            ikindi.add(new Sound("Bekir Buyukbas - Rast", App.API_URL + "/sounds/ikindi/Bekir%20Buyukbas%20-%20Rast.mp3", "1718kB"));
            ikindi.add(new Sound("Ebubekir Atalay - Rast", App.API_URL + "/sounds/ikindi/Ebubekir%20Atalay%20-%20Rast.mp3", "2084kB"));
            ikindi.add(new Sound("Fatih Koca - Rast", App.API_URL + "/sounds/ikindi/Fatih%20Koca%20-%20Rast.mp3", "2082kB"));
            ikindi.add(new Sound("Ilhan Tok - Rast", App.API_URL + "/sounds/ikindi/Ilhan%20Tok%20-%20Rast.mp3", "1526kB"));
            ikindi.add(new Sound("Ismail Cosar - Rast", App.API_URL + "/sounds/ikindi/Ismail%20Cosar%20-%20Rast.mp3", "2064kB"));
            ikindi.add(new Sound("Nurettin Okumus - Rast", App.API_URL + "/sounds/ikindi/Nurettin%20Okumus%20-%20Rast.mp3", "1824kB"));
            ogle.add(new Sound("Abdulkadir Sehitoglu - Ussak", App.API_URL + "/sounds/ogle/Abdulkadir%20Sehitoglu%20-%20Ussak.mp3", "2302kB"));
            ogle.add(new Sound("Ahmet Sahin - Ussak", App.API_URL + "/sounds/ogle/Ahmet%20Sahin%20-%20Ussak.mp3", "2310kB"));
            ogle.add(new Sound("Bekir Buyukbas - Ussak", App.API_URL + "/sounds/ogle/Bekir%20Buyukbas%20-%20Ussak.mp3", "2310kB"));
            ogle.add(new Sound("Ebubekir Atalay - Ussak", App.API_URL + "/sounds/ogle/Ebubekir%20Atalay%20-%20Ussak.mp3", "2346kB"));
            ogle.add(new Sound("Fatih Koca - Ussak", App.API_URL + "/sounds/ogle/Fatih%20Koca%20-%20Ussak.mp3", "1854kB"));
            ogle.add(new Sound("Ilhan Tok - Ussak", App.API_URL + "/sounds/ogle/Ilhan%20Tok%20-%20Ussak.mp3", "1526kB"));
            ogle.add(new Sound("Ismail Cosar - Ussak", App.API_URL + "/sounds/ogle/Ismail%20Cosar%20-%20Ussak.mp3", "1534kB"));
            ogle.add(new Sound("Nurettin Okumus - Ussak", App.API_URL + "/sounds/ogle/Nurettin%20Okumus%20-%20Ussak.mp3", "2186kB"));
            sabah.add(new Sound("Abdulkadir Sehitoglu - Saba", App.API_URL + "/sounds/sabah/Abdulkadir%20Sehitoglu%20-%20Saba.mp3", "2492kB"));
            sabah.add(new Sound("Ahmet Sahin - Saba", App.API_URL + "/sounds/sabah/Ahmet%20Sahin%20-%20Saba.mp3", "2478kB"));
            sabah.add(new Sound("Bekir Buyukbas - Saba", App.API_URL + "/sounds/sabah/Bekir%20Buyukbas%20-%20Saba.mp3", "2252kB"));
            sabah.add(new Sound("Fatih Koca - Saba", App.API_URL + "/sounds/sabah/Fatih%20Koca%20-%20Saba.mp3", "2376kB"));
            sabah.add(new Sound("Ilhan Tok - Saba", App.API_URL + "/sounds/sabah/Ilhan%20Tok%20-%20Saba.mp3", "2150kB"));
            sabah.add(new Sound("Ilhan Tok - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Ilhan%20Tok%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "274kB"));
            sabah.add(new Sound("Bekir Buyukbas - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Bekir%20Buyukbas%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "336kB"));
            sabah.add(new Sound("Nasser Alqatami - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Nasser%20Alqatami%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "436kB"));
            sabah.add(new Sound("Mansoor Az-Zahrani - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Mansoor%20Az-Zahrani%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "464kB"));
            sabah.add(new Sound("Fatih Koca - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Fatih%20Koca%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "492kB"));
            sabah.add(new Sound("Abdulkadir Sehitoglu - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Abdulkadir%20Sehitoglu%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "532kB"));
            sabah.add(new Sound("Ahmet Sahin - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Ahmet%20Sahin%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "584kB"));
            sabah.add(new Sound("Ismail Cosar - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Ismail%20Cosar%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "768kB"));
            sabah.add(new Sound("Ismail Cosar - Saba", App.API_URL + "/sounds/sabah/Ismail%20Cosar%20-%20Saba.mp3", "2464kB"));
            sabah.add(new Sound("Nurettin Okumus - Saba", App.API_URL + "/sounds/sabah/Nurettin%20Okumus%20-%20Saba.mp3", "2452kB"));
            sela.add(new Sound("Abdulkadir Sehitoglu - Sela", App.API_URL + "/sounds/sela/Abdulkadir%20Sehitoglu%20-%20Sela.mp3", "2078kB"));
            sela.add(new Sound("Ebubekir Atalay - Sala", App.API_URL + "/sounds/sela/Ebubekir%20Atalay%20-%20Sala.mp3", "3158kB"));
            sela.add(new Sound("Ilhan Tok - Sala", App.API_URL + "/sounds/sela/Ilhan%20Tok%20-%20Sala.mp3", "1978kB"));
            yatsi.add(new Sound("Abdulkadir Sehitoglu - Hicaz", App.API_URL + "/sounds/yatsi/Abdulkadir%20Sehitoglu%20-%20Hicaz.mp3", "2004kB"));
            yatsi.add(new Sound("Ahmet Sahin - Hicaz", App.API_URL + "/sounds/yatsi/Ahmet%20Sahin%20-%20Hicaz.mp3", "2124kB"));
            yatsi.add(new Sound("Bekir Buyukbas - Hicaz", App.API_URL + "/sounds/yatsi/Bekir%20Buyukbas%20-%20Hicaz.mp3", "1966kB"));
            yatsi.add(new Sound("Ebubekir Atalay - Hicaz", App.API_URL + "/sounds/yatsi/Ebubekir%20Atalay%20-%20Hicaz.mp3", "1928kB"));
            yatsi.add(new Sound("Fatih Koca - Hicaz", App.API_URL + "/sounds/yatsi/Fatih%20Koca%20-%20Hicaz.mp3", "2224kB"));
            yatsi.add(new Sound("Ilhan Tok - Hicaz", App.API_URL + "/sounds/yatsi/Ilhan%20Tok%20-%20Hicaz.mp3", "1800kB"));
            yatsi.add(new Sound("Ismail Cosar - Hicaz", App.API_URL + "/sounds/yatsi/Ismail%20Cosar%20-%20Hicaz.mp3", "2604kB"));
            yatsi.add(new Sound("Nurettin Okumus - Hicaz", App.API_URL + "/sounds/yatsi/Nurettin%20Okumus%20-%20Hicaz.mp3", "1980kB"));


            sSounds.put("sabah", sabah);
            sSounds.put("ogle", ogle);
            sSounds.put("ikindi", ikindi);
            sSounds.put("aksam", aksam);
            sSounds.put("yatsi", yatsi);
            sSounds.put("sela", sela);
            sSounds.put("dua", dua);
            sSounds.put("ezan", ezan);


        }

        return sSounds;
    }

    @NonNull
    public static List<Sound> getSounds(Vakit vakit) {
        if (vakit == Vakit.IMSAK) {
            vakit = Vakit.SABAH;
        }
        if (vakit == Vakit.GUNES) {
            vakit = Vakit.SABAH;
        }
        if (vakit == null) {
            return getSounds("extra");
        }
        return getSounds(vakit.name().toLowerCase(Locale.GERMAN), "ezan", "extra");
    }

    @NonNull
    public static List<Sound> getSounds(@NonNull String... categories) {
        List<Sound> sounds = new ArrayList<>();
        for (String cat : categories) {
            if (getSounds().containsKey(cat)) {
                sounds.addAll(getSounds().get(cat));
            }
        }


        return sounds;
    }

    @NonNull
    public static List<Sound> getAllSounds() {
        List<Sound> sounds = new ArrayList<>();
        SimpleArrayMap<String, List<Sound>> map = getSounds();
        for (int i = 0; i < map.size(); i++) {
            sounds.addAll(getSounds().get(map.valueAt(i)));
        }
        return sounds;
    }


    @Nullable
    private static String forAlarm(@NonNull Times.Alarm alarm) {
        Times t = Times.getTimes(alarm.time);
        String sound;
        if (alarm.cuma) {
            sound = t.getCumaSound();
        } else if (alarm.early) {
            sound = t.getEarlySound(alarm.vakit);
        } else {
            sound = t.getSound(alarm.vakit);
        }
        return sound;
    }


    public static class Sound implements Serializable {
        public String name;
        public String uri;
        public String url;
        public String size;

        Sound() {
        }

        Sound(String name, String url, String size) {
            this.name = name;
            this.url = url;
            this.size = size;
            uri = getFile().toURI().toString();
        }

        @Nullable
        public File getFile() {
            File folder = App.get().getExternalFilesDir(null);
            if (folder != null) {
                File old = new File(url.replace(App.API_URL + "/sounds/", folder.getAbsolutePath()));
                if (old.exists()) {
                    return old;
                }
            }

            File def = null;
            folder = App.get().getExternalFilesDir(null);
            if (folder != null) {
                def = new File(url.replace(App.API_URL + "/sounds", folder.getAbsolutePath()));
                if (def.exists()) {
                    return def;
                }
            }

            File nosd = null;
            folder = App.get().getFilesDir();
            if (folder != null) {
                nosd = new File(url.replace(App.API_URL + "/sounds", folder.getAbsolutePath()));
                if (nosd.exists()) {
                    return nosd;
                }
            }

            if (def != null && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return def;
            } else {
                return nosd;
            }
        }

        public void checkMD5() {
            File file = getFile();
            if (file.exists()) {
                SharedPreferences preferences = App.get().getSharedPreferences("md5", 0);
                String md5 = preferences.getString(name, null);

                if (md5 != null && !MD5.checkMD5(md5, file)) {
                    file.delete();
                }
            }
        }


        public boolean equals(Object o) {
            if (o instanceof Sound) {
                return uri.equals(((Sound) o).uri);
            } else {
                return uri.equals(o.toString());
            }
        }
    }


}
