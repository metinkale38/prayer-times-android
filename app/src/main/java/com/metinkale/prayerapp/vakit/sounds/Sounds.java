package com.metinkale.prayerapp.vakit.sounds;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.custom.MD5;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Sounds {
    private static HashMap<String, List<Sound>> sSounds = new LinkedHashMap<>();

    public static boolean isDownloaded(Sound sound) {
        return (sound.url == null) || sound.getFile().exists();

    }

    public static AbstractMap<String, List<Sound>> getSounds() {

        if (sSounds.isEmpty()) {
            List<Sound> sabah = new ArrayList<Sound>();
            List<Sound> ogle = new ArrayList<Sound>();
            List<Sound> ikindi = new ArrayList<Sound>();
            List<Sound> aksam = new ArrayList<Sound>();
            List<Sound> yatsi = new ArrayList<Sound>();
            List<Sound> sela = new ArrayList<Sound>();
            List<Sound> dua = new ArrayList<Sound>();
            List<Sound> ezan = new ArrayList<Sound>();


            aksam.add(new Sound("Abdulkadir Sehitoglu - Segah", App.API_URL + "/sounds/aksam/Abdulkadir%20Sehitoglu%20-%20Segah.mp3", "1126kb"));
            aksam.add(new Sound("Ahmet Sahin - Segah", App.API_URL + "/sounds/aksam/Ahmet%20Sahin%20-%20Segah.mp3", "1226kb"));
            aksam.add(new Sound("Bekir Buyukbas - Segah", App.API_URL + "/sounds/aksam/Bekir%20Buyukbas%20-%20Segah.mp3", "1460kb"));
            aksam.add(new Sound("Ebubekir Atalay - Segah", App.API_URL + "/sounds/aksam/Ebubekir%20Atalay%20-%20Segah.mp3", "896kb"));
            aksam.add(new Sound("Fatih Koca - Segah", App.API_URL + "/sounds/aksam/Fatih%20Koca%20-%20Segah.mp3", "1124kb"));
            aksam.add(new Sound("Ilhan Tok - Segah", App.API_URL + "/sounds/aksam/Ilhan%20Tok%20-%20Segah.mp3", "862kb"));
            aksam.add(new Sound("Ismail Cosar - Segah", App.API_URL + "/sounds/aksam/Ismail%20Cosar%20-%20Segah.mp3", "1292kb"));
            aksam.add(new Sound("Nurettin Okumus - Segah", App.API_URL + "/sounds/aksam/Nurettin%20Okumus%20-%20Segah.mp3", "1270kb"));
            dua.add(new Sound("Abdulkadir Sehitoglu - Ezan Duasi", App.API_URL + "/sounds/dua/Abdulkadir%20Sehitoglu%20-%20Ezan%20Duasi.mp3", "1098kb"));
            ezan.add(new Sound("Ghazi A-Sa'adoni", App.API_URL + "/sounds/ezan/Ghazi%20A-Sa-adoni.mp3", "1106kb"));
            ezan.add(new Sound("Hamad Deghreri", App.API_URL + "/sounds/ezan/Hamad%20Deghreri.mp3", "1206kb"));
            ezan.add(new Sound("Ibrahim Al-Arkani", App.API_URL + "/sounds/ezan/Ibrahim%20Al-Arkani.mp3", "1820kb"));
            ezan.add(new Sound("Majed Al-hamathani", App.API_URL + "/sounds/ezan/Majed%20Al-hamathani.mp3", "1706kb"));
            ezan.add(new Sound("Malek chebae - Takbir", App.API_URL + "/sounds/ezan/Malek%20chebae%20-%20Takbir.mp3", "210kb"));
            ezan.add(new Sound("Malek chebae", App.API_URL + "/sounds/ezan/Malek%20chebae.mp3", "1032kb"));
            ezan.add(new Sound("Mansoor Az-Zahrani", App.API_URL + "/sounds/ezan/Mansoor%20Az-Zahrani.mp3", "1440kb"));
            ezan.add(new Sound("Mishary Alafasi", App.API_URL + "/sounds/ezan/Mishary%20Alafasi.mp3", "1818kb"));
            ezan.add(new Sound("Nasser Alqatami", App.API_URL + "/sounds/ezan/Nasser%20Alqatami.mp3", "1896kb"));
            ikindi.add(new Sound("Abdulkadir Sehitoglu - Rast", App.API_URL + "/sounds/ikindi/Abdulkadir%20Sehitoglu%20-%20Rast.mp3", "1946kb"));
            ikindi.add(new Sound("Ahmet Sahin - Rast", App.API_URL + "/sounds/ikindi/Ahmet%20Sahin%20-%20Rast.mp3", "2022kb"));
            ikindi.add(new Sound("Bekir Buyukbas - Rast", App.API_URL + "/sounds/ikindi/Bekir%20Buyukbas%20-%20Rast.mp3", "1718kb"));
            ikindi.add(new Sound("Ebubekir Atalay - Rast", App.API_URL + "/sounds/ikindi/Ebubekir%20Atalay%20-%20Rast.mp3", "2084kb"));
            ikindi.add(new Sound("Fatih Koca - Rast", App.API_URL + "/sounds/ikindi/Fatih%20Koca%20-%20Rast.mp3", "2082kb"));
            ikindi.add(new Sound("Ilhan Tok - Rast", App.API_URL + "/sounds/ikindi/Ilhan%20Tok%20-%20Rast.mp3", "1526kb"));
            ikindi.add(new Sound("Ismail Cosar - Rast", App.API_URL + "/sounds/ikindi/Ismail%20Cosar%20-%20Rast.mp3", "2064kb"));
            ikindi.add(new Sound("Nurettin Okumus - Rast", App.API_URL + "/sounds/ikindi/Nurettin%20Okumus%20-%20Rast.mp3", "1824kb"));
            ogle.add(new Sound("Abdulkadir Sehitoglu - Ussak", App.API_URL + "/sounds/ogle/Abdulkadir%20Sehitoglu%20-%20Ussak.mp3", "2302kb"));
            ogle.add(new Sound("Ahmet Sahin - Ussak", App.API_URL + "/sounds/ogle/Ahmet%20Sahin%20-%20Ussak.mp3", "2310kb"));
            ogle.add(new Sound("Bekir Buyukbas - Ussak", App.API_URL + "/sounds/ogle/Bekir%20Buyukbas%20-%20Ussak.mp3", "2310kb"));
            ogle.add(new Sound("Ebubekir Atalay - Ussak", App.API_URL + "/sounds/ogle/Ebubekir%20Atalay%20-%20Ussak.mp3", "2346kb"));
            ogle.add(new Sound("Fatih Koca - Ussak", App.API_URL + "/sounds/ogle/Fatih%20Koca%20-%20Ussak.mp3", "1854kb"));
            ogle.add(new Sound("Ilhan Tok - Ussak", App.API_URL + "/sounds/ogle/Ilhan%20Tok%20-%20Ussak.mp3", "1526kb"));
            ogle.add(new Sound("Ismail Cosar - Ussak", App.API_URL + "/sounds/ogle/Ismail%20Cosar%20-%20Ussak.mp3", "1534kb"));
            ogle.add(new Sound("Nurettin Okumus - Ussak", App.API_URL + "/sounds/ogle/Nurettin%20Okumus%20-%20Ussak.mp3", "2186kb"));
            sabah.add(new Sound("Abdulkadir Sehitoglu - Saba", App.API_URL + "/sounds/sabah/Abdulkadir%20Sehitoglu%20-%20Saba.mp3", "2492kb"));
            sabah.add(new Sound("Ahmet Sahin - Saba", App.API_URL + "/sounds/sabah/Ahmet%20Sahin%20-%20Saba.mp3", "2478kb"));
            sabah.add(new Sound("Bekir Buyukbas - Saba", App.API_URL + "/sounds/sabah/Bekir%20Buyukbas%20-%20Saba.mp3", "2252kb"));
            sabah.add(new Sound("Fatih Koca - Saba", App.API_URL + "/sounds/sabah/Fatih%20Koca%20-%20Saba.mp3", "2376kb"));
            sabah.add(new Sound("Ilhan Tok - Saba", App.API_URL + "/sounds/sabah/Ilhan%20Tok%20-%20Saba.mp3", "2150kb"));
            sabah.add(new Sound("Ilhan Tok - essalatu hayrun minen nevm", App.API_URL + "/sounds/sabah/Ilhan%20Tok%20-%20essalatu%20hayrun%20minen%20nevm.mp3", "274kb"));
            sabah.add(new Sound("Ismail Cosar - Saba", App.API_URL + "/sounds/sabah/Ismail%20Cosar%20-%20Saba.mp3", "2464kb"));
            sabah.add(new Sound("Nurettin Okumus - Saba", App.API_URL + "/sounds/sabah/Nurettin%20Okumus%20-%20Saba.mp3", "2452kb"));
            sela.add(new Sound("Abdulkadir Sehitoglu - Sela", App.API_URL + "/sounds/sela/Abdulkadir%20Sehitoglu%20-%20Sela.mp3", "2078kb"));
            sela.add(new Sound("Ebubekir Atalay - Sala", App.API_URL + "/sounds/sela/Ebubekir%20Atalay%20-%20Sala.mp3", "3158kb"));
            sela.add(new Sound("Ilhan Tok - Sala", App.API_URL + "/sounds/sela/Ilhan%20Tok%20-%20Sala.mp3", "1978kb"));
            yatsi.add(new Sound("Abdulkadir Sehitoglu - Hicaz", App.API_URL + "/sounds/yatsi/Abdulkadir%20Sehitoglu%20-%20Hicaz.mp3", "2004kb"));
            yatsi.add(new Sound("Ahmet Sahin - Hicaz", App.API_URL + "/sounds/yatsi/Ahmet%20Sahin%20-%20Hicaz.mp3", "2124kb"));
            yatsi.add(new Sound("Bekir Buyukbas - Hicaz", App.API_URL + "/sounds/yatsi/Bekir%20Buyukbas%20-%20Hicaz.mp3", "1966kb"));
            yatsi.add(new Sound("Ebubekir Atalay - Hicaz", App.API_URL + "/sounds/yatsi/Ebubekir%20Atalay%20-%20Hicaz.mp3", "1928kb"));
            yatsi.add(new Sound("Fatih Koca - Hicaz", App.API_URL + "/sounds/yatsi/Fatih%20Koca%20-%20Hicaz.mp3", "2224kb"));
            yatsi.add(new Sound("Ilhan Tok - Hicaz", App.API_URL + "/sounds/yatsi/Ilhan%20Tok%20-%20Hicaz.mp3", "1800kb"));
            yatsi.add(new Sound("Ismail Cosar - Hicaz", App.API_URL + "/sounds/yatsi/Ismail%20Cosar%20-%20Hicaz.mp3", "2604kb"));
            yatsi.add(new Sound("Nurettin Okumus - Hicaz", App.API_URL + "/sounds/yatsi/Nurettin%20Okumus%20-%20Hicaz.mp3", "1980kb"));


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

    public static List<Sound> getSounds(Vakit vakit) {
        if (vakit == Vakit.IMSAK) vakit = Vakit.SABAH;
        if (vakit == Vakit.GUNES) vakit = Vakit.SABAH;
        if (vakit == null) return (getSounds("extra"));
        return (getSounds(vakit.name().toLowerCase(Locale.GERMAN), "ezan", "extra"));
    }

    public static List<Sound> getSounds(String... categories) {
        List<Sound> sounds = new ArrayList<>();
        for (String cat : categories)
            if (getSounds().containsKey(cat)) sounds.addAll(getSounds().get(cat));


        return sounds;
    }

    public static List<Sound> getAllSounds() {
        List<Sound> sounds = new ArrayList<>();
        Set<String> set = getSounds().keySet();
        for (String cat : set)
            if (getSounds().containsKey(cat)) sounds.addAll(getSounds().get(cat));


        return sounds;
    }

    public static class Sound implements Serializable {
        public String name;
        public String uri;
        public String url;
        public String size;

        public Sound() {
        }

        public Sound(String name, String url, String size) {
            this.name = name;
            this.url = url;
            this.size = size;
            uri = getFile().toURI().toString();
        }

        public File getFile() {
            File old = new File(url.replace(App.API_URL + "/sounds/", App.getContext().getExternalFilesDir(null).getAbsolutePath()));
            if (old.exists()) return old;

            File def = new File(url.replace(App.API_URL + "/sounds", App.getContext().getExternalFilesDir(null).getAbsolutePath()));
            if (def.exists()) return def;

            File nosd = new File(url.replace(App.API_URL + "/sounds", App.getContext().getFilesDir().getAbsolutePath()));
            if (nosd.exists()) return nosd;

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return def;
            } else {
                return nosd;
            }
        }

        public boolean needsRedownload(Iterable<Times.Alarm> alarms) {
            if (getFile().exists()) {
                SharedPreferences preferences = App.getContext().getSharedPreferences("md5", 0);
                String md5 = preferences.getString(name, null);
                if (md5 == null) {
                    try {
                        URL url = new URL(this.url + ".md5");
                        URLConnection ucon = url.openConnection();

                        InputStream is = ucon.getInputStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));

                        md5 = in.readLine();
                        if ("failed".equals(md5) || !"done".equals(in.readLine())) {
                            return false;
                        }

                        preferences.edit().putString(name, md5).apply();


                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }

                return !MD5.checkMD5(md5, getFile());
            } else {
                for (Times.Alarm alarm : alarms) {
                    if (alarm.sound.startsWith(uri)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean equals(Object o) {
            if (o instanceof Sound) {
                return uri.equals(((Sound) o).uri);
            } else return uri.equals(o.toString());
        }


    }


    private static boolean checking;

    public static void checkIfNeeded() {
        if (checking) return;
        checking = true;
        if (!needsCheck()) return;
        List<Sound> sounds = getAllSounds();
        List<Times.Alarm> alarms = Times.getAllAlarms();
        for (Sound sound : sounds) {
            if (sound.needsRedownload(alarms)) {
                sound.getFile().delete();
                MainIntentService.downloadSound(App.getContext(), sound, null);
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        prefs.edit().putLong("lastMD5Check", System.currentTimeMillis()).apply();
        checking = false;
    }

    @SuppressWarnings("deprecation")
    public static boolean needsCheck() {
        if (checking) return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        long lastSync = prefs.getLong("lastMD5Check", 0);
        if (((System.currentTimeMillis() - lastSync) > (1000 * 60 * 60 * 24 * 7)) || (lastSync == 0)) {


            ConnectivityManager connManager = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (!wifi.isConnected()) return false;

            if (!((PowerManager) App.getContext().getSystemService(Context.POWER_SERVICE)).isScreenOn()) return false;


            return true;
        }

        return false;
    }
}
