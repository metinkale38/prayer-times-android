/*
 * Copyright (c) 2013-2017 Metin Kale
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static sun.net.NetProperties.get;

/**
 * ATTENTION: THIS CLASS IS VERY BAD WRITTEN
 */
@SuppressWarnings("Duplicates")
public class Cities {
    private static final String MAIL_ADRESS = "metinkale38@gmail.com";
    private static AtomicInteger id = new AtomicInteger();
    private static Map<String, Geocoder> mGeocodings = new ConcurrentHashMap<>();
    private static Map<Integer, Entry> mEntries = Collections.synchronizedNavigableMap(new TreeMap<Integer, Entry>());
    private static AtomicInteger mTaskCounter = new AtomicInteger();
    //only increase poolsize if you want to speedup network requests, but use 1 for final version to keep ids consistent
    private static Executor mWorker = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()) {
        @Override
        public void execute(Runnable command) {
            if (getMaximumPoolSize() == 1) {
                command.run();
                return;
            }
            mTaskCounter.incrementAndGet();
            super.execute(() -> {
                command.run();
                mTaskCounter.decrementAndGet();
            });
        }
    };


    private static class Entry {
        int id;
        int parent;
        double lat;
        double lng;
        String key;
        String name;
    }

    public static void main(String args[]) throws UnsupportedEncodingException {
        Locale.setDefault(Locale.ENGLISH);


        mWorker.execute(() -> {
            long time = System.currentTimeMillis();
            System.out.println("Start fetchIndonesia()");
            fetchIndonesia();
            System.out.println("fetchIndonesia() - finished in " + (System.currentTimeMillis() - time) + " ms");
        });


        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get("citiesfetcher/geocodings.geo"));
            mGeocodings = new Gson().fromJson(new String(encoded, "UTF-8"), new TypeToken<ConcurrentHashMap<String, Geocoder>>() {
            }.getType());
        } catch (IOException e) {
            // e.printStackTrace();
            mGeocodings = new ConcurrentHashMap<>();
        }


        while (mTaskCounter.get() > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long time = System.currentTimeMillis();
        System.out.println("Start geocode()");
        geocode();
        System.out.println("geocode() - finished in " + (System.currentTimeMillis() - time) + " ms");


        time = System.currentTimeMillis();
        System.out.println("Start export()");
        export();

        System.out.println("export() - finished in " + (System.currentTimeMillis() - time) + " ms");
        if (mTaskCounter.get() == 0) System.exit(0);
    }


    private static void export() throws UnsupportedEncodingException {

        new File("citiesfetcher/cities.tsv").delete();
        try (PrintWriter out = new PrintWriter("citiesfetcher/cities.tsv", "utf-8")) {
            for (Entry e : mEntries.values()) {
                out.print(e.id);
                out.print('\t');
                out.print(e.parent);
                out.print('\t');
                if (e.lat != 0) out.print(e.lat);
                out.print('\t');
                if (e.lng != 0) out.print(e.lng);
                out.print('\t');
                if (e.key != null) out.print(e.key);
                out.print('\t');
                out.print(e.name.trim().replace("  ", " ").replace("( ", "(").replace(" )", ")"));
                out.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try (PrintWriter out = new PrintWriter("geocodings.geo")) {
            out.write(new Gson().toJson(mGeocodings));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void fetchIndonesia() {
        String data = get("http://sihat.kemenag.go.id/waktu-sholat#");
        data = data.substring(data.indexOf("\"opt_lokasi_provinsi\""));
        data = data.substring(0, data.indexOf("</select"));

        Entry parent = new Entry();
        parent.id = Cities.id.incrementAndGet();
        parent.parent = 0;
        parent.name = "Kemenag.go.id";
        parent.key = null;
        parent.lat = 0;
        parent.lng = 0;
        mEntries.put(parent.id, parent);

        for (data = data.substring(data.indexOf("<option value='") + 1);
             data.contains("option value=");
             data = data.substring(i(data.indexOf("option ")))) {
            data = data.substring(1);
            String _id = data.substring(data.indexOf("'") + 1);
            _id = _id.substring(0, _id.indexOf("'"));
            Entry e = new Entry();
            e.id = Cities.id.incrementAndGet();
            e.parent = parent.id;
            e.name = _id;
            e.key = null;
            e.lat = 0;
            e.lng = 0;
            mEntries.put(e.id, e);

            fetchIndonesiaKota(_id, e.id);
        }


    }

    private static void fetchIndonesiaKota(String id, int parent) {

        String data = HTTP.post("http://sihat.kemenag.go.id/site/get_kota_lintang", "q=" + id.replace(" ", "+"));

        for (data = data.substring(data.indexOf("<option value='") + 1);
             data.contains("option value=");
             data = data.substring(i(data.indexOf("option ")))) {
            data = data.substring(1);
            String search = "'";
            if (!data.contains("'") && data.contains("\"")) {
                search = "\"";
            }
            String _id = data.substring(data.indexOf(search) + 1);
            _id = _id.substring(0, _id.indexOf(search));
            if (_id.isEmpty()) continue;
            String name = data.substring(data.indexOf(">") + 1);
            name = name.substring(0, name.indexOf("<"));
            Entry e = new Entry();
            e.id = Cities.id.incrementAndGet();
            e.parent = parent;
            e.name = name;
            e.key = _id;
            e.lat = 0;
            e.lng = 0;
            mEntries.put(e.id, e);
        }


    }


    private static void fetchHabous() {
        String data = get("http://www.habous.gov.ma/horaire%20de%20priere%20fr/horaire_hijri.php");

        data = data.substring(data.indexOf("<select name=\"ville\""));
        data = data.substring(0, data.indexOf("</select>"));


        Entry parent = new Entry();
        parent.id = Cities.id.incrementAndGet();
        parent.parent = 0;
        parent.name = "Habous.gov.ma";
        parent.key = null;
        parent.lat = 0;
        parent.lng = 0;
        mEntries.put(parent.id, parent);

        Map<Integer, Entry> habous = new HashMap<>();

        for (data = data.substring(data.indexOf("<option value=") + 1);
             data.contains("option value=");
             data = data.substring(i(data.indexOf("option ")))) {
            data = data.substring(1);
            String _id = data.substring(data.indexOf("?ville=") + 7);
            _id = _id.substring(0, _id.indexOf("&"));
            int id = Integer.parseInt(_id);
            String name = data.substring(data.indexOf(">") + 1);
            name = name.substring(0, name.indexOf("<"));
            Entry e = new Entry();
            e.id = Cities.id.incrementAndGet();
            e.parent = parent.id;
            e.name = name;
            e.key = "H_" + id;
            e.lat = 0;
            e.lng = 0;
            mEntries.put(e.id, e);
            habous.put(id, e);

        }


        //fetch_ar
        data = get("http://www.habous.gov.ma/horaire%20de%20priere/horaire_hijri.php");

        data = data.substring(data.indexOf("<select name=\"ville\""));
        data = data.substring(0, data.indexOf("</select>"));


        for (data = data.substring(data.indexOf("<option value=") + 1);
             data.contains("option value=");
             data = data.substring(i(data.indexOf("option ")))) {
            data = data.substring(1);
            String _id = data.substring(data.indexOf("?ville=") + 7);
            _id = _id.substring(0, _id.indexOf("&"));
            int id = Integer.parseInt(_id);
            String name = data.substring(data.indexOf(">") + 1);
            name = name.substring(0, name.indexOf("<"));

            habous.get(id).name += " (" + name + ")";
        }


    }

    private static void fetchMalaysia() {
        Entry my = new Entry();
        my.id = Cities.id.incrementAndGet();
        my.parent = 0;
        my.name = "e-solat.gov.my";
        my.key = null;
        my.lat = 0;
        my.lng = 0;
        mEntries.put(my.id, my);

        String data = get("http://www.e-solat.gov.my/web/waktusolat.php");
        data = data.substring(data.indexOf("name=\"negeri\""));
        data = data.substring(0, data.indexOf("</select>"));
        data = data.substring(data.indexOf("<option value=\"000\">") + 1);
        for (data = data.substring(data.indexOf("<option"));
             data.contains("<option");
             data = data.substring(i(data.indexOf("<option")))) {
            data = data.substring(1);
            String value = data.substring(data.indexOf("=\"") + 2);
            value = value.substring(0, value.indexOf("\""));
            String name = data.substring(data.indexOf(">") + 1);
            name = name.substring(0, name.indexOf("<"));


            Entry state = new Entry();
            state.id = Cities.id.incrementAndGet();
            state.parent = my.id;
            state.name = name;
            state.key = null;
            state.lat = 0;
            state.lng = 0;
            mEntries.put(state.id, state);

            fetchMyZones(state, value);

        }
    }

    private static void fetchMyZones(Entry state, String val) {
        String data = get("http://www.e-solat.gov.my/web/waktusolat.php?negeri=" +
                URLEncoder.encode(val) + "&negeri=" + URLEncoder.encode(val));
        data = data.substring(data.indexOf("name=\"zone\""));
        data = data.substring(0, data.indexOf("</select>"));
        data = data.substring(data.indexOf("<option value=\"000\">") + 1);
        for (data = data.substring(data.indexOf("<option"));
             data.contains("<option");
             data = data.substring(i(data.indexOf("<option")))) {
            data = data.substring(1);
            String value = data.substring(data.indexOf("=\"") + 2);
            value = value.substring(0, value.indexOf("\""));
            String name = data.substring(data.indexOf(">") + 1);
            name = name.substring(0, name.indexOf("<"));


            Entry zone = new Entry();
            zone.id = Cities.id.incrementAndGet();
            zone.parent = state.id;
            zone.name = name;
            zone.key = "M_" + val + "_" + value;
            zone.lat = 0;
            zone.lng = 0;
            mEntries.put(zone.id, zone);
        }
    }

    private static void fetchIGMG() {
        String data = get("https://www.igmg.org/gebetskalender/");

        String ger = data.substring(data.indexOf("id=\"alman_sehirleri\""));
        ger = ger.substring(0, ger.indexOf("</select>"));

        String wor = data.substring(data.indexOf("id=\"dunya_sehirleri\""));
        wor = wor.substring(0, wor.indexOf("</select>"));

        String all = ger + wor;
        all = all.replace("Hoogenzand", "Hoogezand");
        all = all.replace("Groinchem", "Gorinchem");


        class City {
            private String name;
            private int id;
        }
        HashMap<String, List<City>> map = new HashMap<>();
        for (all = all.substring(all.indexOf("<option value='"));
             all.contains("<option value='");
             all = all.substring(i(all.indexOf("<option value='")))) {
            all = all.substring(1);
            String _id = all.substring(all.indexOf("'") + 1);
            _id = _id.substring(0, _id.indexOf("'"));
            int id = Integer.parseInt(_id);
            String name = all.substring(all.indexOf(">") + 1);
            name = name.substring(0, name.indexOf("<"));
            String country = name.substring(name.lastIndexOf("(") + 1);
            country = country.substring(0, 2);
            name = name.substring(0, name.lastIndexOf(" "));

            if (!map.containsKey(country)) {
                map.put(country, new ArrayList<>());
            }
            City c = new City();
            c.id = id;
            c.name = name;
            map.get(country).add(c);
        }


        Entry igmg = new Entry();
        igmg.id = Cities.id.incrementAndGet();
        igmg.parent = 0;
        igmg.name = "IGMG.org";
        igmg.key = null;
        igmg.lat = 0;
        igmg.lng = 0;
        mEntries.put(igmg.id, igmg);


        for (String country : map.keySet()) {

            Entry parent = new Entry();
            parent.id = Cities.id.incrementAndGet();
            parent.parent = igmg.id;
            parent.name = country;
            parent.key = null;
            parent.lat = 0;
            parent.lng = 0;
            mEntries.put(parent.id, parent);

            switch (parent.name) {
                case "BA":
                    parent.name = "Bosnien";
                    break;
                case "NL":
                    parent.name = "Niederlande";
                    break;
                case "DE":
                    parent.name = "Deutschland";
                    break;
                case "AT":
                    parent.name = "\u00d6sterreich";
                    break;
                case "AU":
                    parent.name = "Australien";
                    break;
                case "BE":
                    parent.name = "Belgien";
                    break;
                case "CA":
                    parent.name = "Kanada";
                    break;
                case "CH":
                    parent.name = "Schweiz";
                    break;
                case "DK":
                    parent.name = "D\u00e4nemark";
                    break;
                case "FR":
                    parent.name = "Frankreich";
                    break;
                case "GB":
                    parent.name = "Vereinigtes K\u00f6nigreich";
                    break;
                case "IT":
                    parent.name = "Italien";
                    break;
                case "LI":
                    parent.name = "Liechtenstein";
                    break;
                case "MK":
                    parent.name = "Mazedonien";
                    break;
                case "NO":
                    parent.name = "Norwegen";
                    break;
                case "SE":
                    parent.name = "Schweden";
                    break;
                case "US":
                    parent.name = "Vereinigte Staaten von Amerika";
                    break;
            }


            List<City> cities = map.get(country);
            for (City c : cities) {
                Entry e = new Entry();
                e.id = Cities.id.incrementAndGet();
                e.parent = parent.id;
                e.name = c.name;
                e.key = "I_" + c.id;
                e.lat = 0;
                e.lng = 0;
                mEntries.put(e.id, e);

            }

        }
    }

    private static void fetchNVCCountries() {
        String data = get("http://namazvakti.com/CountryList.php");
        Entry nvc = new Entry();
        nvc.id = Cities.id.incrementAndGet();
        nvc.parent = 0;
        nvc.name = "NamazVakti.com";
        nvc.key = null;
        nvc.lat = 0;
        nvc.lng = 0;
        mEntries.put(nvc.id, nvc);


        for (data = data.substring(data.indexOf("StateList.php?"));
             data.contains("StateList.php?");
             data = data.substring(i(data.indexOf("StateList.php?")))) {
            data = data.substring(1);
            String _id = data.substring(data.indexOf("=") + 1);
            _id = _id.substring(0, _id.indexOf("\""));
            int id = Integer.parseInt(_id);
            String name = data.substring(data.indexOf("<b>") + 3);
            name = name.substring(0, name.indexOf("<"));
            name = name.replace("&nbsp;", " ").trim();

            Entry e = new Entry();
            e.id = Cities.id.incrementAndGet();
            e.parent = nvc.id;
            e.name = name;
            e.key = null;
            e.lat = 0;
            e.lng = 0;
            mEntries.put(e.id, e);

            fetchNVCStates(id, e);
        }

    }

    private static void fetchNVCStates(int parentId, Entry parent) {
        String data = get("http://namazvakti.com/StateList.php?countryID=" + parentId);
        for (data = data.substring(data.indexOf("CityList.php?"));
             data.contains("CityList.php?");
             data = data.substring(i(data.indexOf("CityList.php?")))) {
            data = data.substring(1);
            String _id = data.substring(data.indexOf("=") + 1);
            String state = _id.substring(_id.indexOf("state=") + 6);
            state = state.substring(0, state.indexOf("\""));
            _id = _id.substring(0, _id.indexOf("&"));
            int id = Integer.parseInt(_id);
            String name = data.substring(data.indexOf("<b>") + 3);
            name = name.substring(0, name.indexOf("<"));
            name = name.replace("&nbsp;", " ").trim();

            Entry e = new Entry();
            e.id = Cities.id.incrementAndGet();
            e.parent = parent.id;
            e.name = name;
            e.key = null;
            e.lat = 0;
            e.lng = 0;
            mEntries.put(e.id, e);

            fetchNVCCitites(id, state, e);
        }
    }

    private static void fetchNVCCitites(int country, String state, Entry parent) {
        String data = get("http://namazvakti.com/CityList.php?countryID=" + country + "&state=" + URLEncoder.encode(state));
        data = data.substring(data.indexOf("stateTitle"));
        for (data = data.substring(data.indexOf("Main.php?cityID"));
             data.contains("Main.php?cityID");
             data = data.substring(i(data.indexOf("Main.php?cityID")))) {
            data = data.substring(1);
            String _id = data.substring(data.indexOf("=") + 1);
            _id = _id.substring(0, _id.indexOf("\""));
            int id = Integer.parseInt(_id);
            String name = data.substring(data.indexOf("<b>") + 3);
            name = name.substring(0, name.indexOf("<"));
            name = name.replace("&nbsp;", " ").trim();

            Entry e = new Entry();
            e.id = Cities.id.incrementAndGet();
            e.parent = parent.id;
            e.name = name;
            e.key = "N_" + id;
            e.lat = 0;
            e.lng = 0;
            mEntries.put(e.id, e);
        }
    }

    private static void fetchSemerkand() {
        try {
            String Url = "http://semerkandtakvimi.semerkandmobile.com/locations";
            String data = get(Url);
            List<Semerkand> semerkand = new Gson().fromJson(data,
                    new TypeToken<List<Semerkand>>() {
                    }.getType());


            Entry sem = new Entry();
            sem.id = Cities.id.incrementAndGet();
            sem.parent = 0;
            sem.name = "Semerkandtakvimi.com";
            sem.key = null;
            sem.lat = 0;
            sem.lng = 0;
            mEntries.put(sem.id, sem);


            for (Semerkand country : semerkand) {
                Entry parent = new Entry();
                parent.id = Cities.id.incrementAndGet();
                parent.parent = sem.id;
                parent.name = country.Name;
                parent.key = null;
                parent.lat = 0;
                parent.lng = 0;
                mEntries.put(parent.id, parent);


                for (Semerkand city : country.Cities) {
                    if (city.Districts.length != 0) {
                        Entry parent2 = new Entry();
                        parent2.id = Cities.id.incrementAndGet();
                        parent2.parent = parent.id;
                        parent2.name = city.Name;
                        parent2.key = null;
                        parent2.lat = 0;
                        parent2.lng = 0;
                        mEntries.put(parent2.id, parent2);
                        for (Semerkand county : city.Districts) {
                            Entry e = new Entry();
                            e.id = Cities.id.incrementAndGet();
                            e.parent = parent2.id;
                            e.name = county.Name;
                            e.key = "S_" + 'd' + county.Id;
                            e.lat = 0;
                            e.lng = 0;
                            mEntries.put(e.id, e);
                        }
                    } else {
                        Entry e = new Entry();
                        e.id = Cities.id.incrementAndGet();
                        e.parent = parent.id;
                        e.name = city.Name;
                        e.key = "S_" + 'c' + city.Id;
                        e.lat = 0;
                        e.lng = 0;
                        mEntries.put(e.id, e);

                    }
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static class Semerkand {
        int Id;
        String Name;
        Semerkand[] Cities = new Semerkand[0];
        Semerkand[] Districts = new Semerkand[0];
    }


    private static void fetchFaziletCountries() {
        try {
            Calendar cal = Calendar.getInstance();
            String data = get("http://www.fazilettakvimi.com/tr/" + cal.get(Calendar.YEAR) +
                    "/" + (1 + cal.get(Calendar.MONTH)) + "/" + cal.get(Calendar.DATE) + ".html");
            BufferedReader in = new BufferedReader(
                    new StringReader(data));

            //noinspection StatementWithEmptyBody
            while (!in.readLine().contains("<option value=\"")) {
            }
            Entry faz = new Entry();
            faz.id = Cities.id.incrementAndGet();
            faz.parent = 0;
            faz.name = "Fazilettakvimi.com";
            faz.key = null;
            faz.lat = 0;
            faz.lng = 0;
            mEntries.put(faz.id, faz);

            String line;

            while ((line = in.readLine()).contains("<option value=\"")) {
                String id_ = line.substring(line.indexOf("value=") + 7);
                id_ = id_.substring(0, id_.indexOf("\""));
                int id = Integer.parseInt(id_);
                String name = line.substring(line.lastIndexOf("\">") + 2, line.lastIndexOf("</"));

                Entry e = new Entry();
                e.id = Cities.id.incrementAndGet();
                e.parent = faz.id;
                e.name = name;
                e.key = null;
                e.lat = 0;
                e.lng = 0;
                mEntries.put(e.id, e);


                fetchFaziletStates(id, e);

            }
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void fetchFaziletStates(int countryId, Entry parent) {
        try {
            String Url = "http://www.fazilettakvimi.com/api/ulke/sehir/liste/" + countryId;
            String data = get(Url);
            Sehirler sehirler = new Gson().fromJson(data, Sehirler.class);
            for (Sehir sehir : sehirler.sehirler) {

                Entry e = new Entry();
                e.id = Cities.id.incrementAndGet();
                e.parent = parent.id;
                e.name = sehir.adi;
                e.key = null;
                e.lat = 0;
                e.lng = 0;
                mEntries.put(e.id, e);
                fetchFaziletCities(countryId, sehir.id, e);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void fetchFaziletCities(int countryId, int stateId, Entry parent) {
        try {
            String Url = "http://www.fazilettakvimi.com/api/ulke/sehir/ilce/liste/" + stateId;
            String data = get(Url);

            Ilce ilceler = new Gson().fromJson(data, Ilce.class);
            if (ilceler.ilceler.size() == 1) {
                if (mEntries.containsKey(parent.id))
                    mEntries.get(parent.id).key = "F_" + countryId + "_" + stateId + "_" + ilceler.ilceler.get(0).id;
            } else
                for (Sehir state : ilceler.ilceler) {
                    state.adi = state.adi.replace("\n", "");

                    Entry e = new Entry();
                    e.id = Cities.id.incrementAndGet();
                    e.parent = parent.id;
                    e.name = state.adi;
                    e.key = "F_" + countryId + "_" + stateId + "_" + state.id;
                    e.lat = 0;
                    e.lng = 0;
                    mEntries.put(e.id, e);

                }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private static void fetchDitibCountries() {
        try {
            URL url = new URL("http://www.diyanet.gov.tr/tr/PrayerTime/WorldPrayerTimes");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String line;
            while (true) {
                if (in.readLine().contains("class=\"title\"")) break;
            }


            Entry diyanet = new Entry();
            diyanet.id = Cities.id.incrementAndGet();
            diyanet.parent = 0;
            diyanet.name = "Diyanet.gov.tr";
            diyanet.key = null;
            diyanet.lat = 0;
            diyanet.lng = 0;
            mEntries.put(diyanet.id, diyanet);


            while ((line = in.readLine()).contains("option")) {
                int id = Integer.parseInt(line.substring(line.indexOf("value=") + 7, line.lastIndexOf("\"")));
                String name = line.substring(line.lastIndexOf("\">") + 2, line.lastIndexOf("</"));

                Entry e = new Entry();
                e.id = Cities.id.incrementAndGet();
                e.parent = diyanet.id;
                e.name = name.replace("T&#220;RK\u0130YE", "T\u00dcRK\u0130YE");
                e.key = null;
                e.lat = 0;
                e.lng = 0;
                mEntries.put(e.id, e);


                fetchDitibStates(id, e);

            }
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void fetchDitibStates(int countryId, Entry parent) {
        try {
            String Url = "http://www.diyanet.gov.tr/PrayerTime/FillState?countryCode=" + countryId;
            String data = get(Url);
            List<State> states = new Gson().fromJson(data, new TypeToken<List<State>>() {
            }.getType());
            for (State state : states) {

                Entry e = new Entry();
                e.id = Cities.id.incrementAndGet();
                e.parent = parent.id;
                e.name = state.Text;
                e.key = null;
                e.lat = 0;
                e.lng = 0;
                mEntries.put(e.id, e);

                fetchDitibCities(countryId, state.Value, e);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void fetchDitibCities(int countryId, int stateId, Entry parent) {
        try {
            String Url = "http://www.diyanet.gov.tr/PrayerTime/FillCity?itemId=" + stateId;
            String data = get(Url);

            List<State> states = new Gson().fromJson(data, new TypeToken<List<State>>() {
            }.getType());
            if (states.isEmpty()) {
                if (mEntries.containsKey(parent.id))
                    mEntries.get(parent.id).key = "D_" + countryId + "_" + stateId;
            } else
                for (State state : states) {


                    Entry e = new Entry();
                    e.id = Cities.id.incrementAndGet();
                    e.parent = parent.id;
                    e.name = state.Text;
                    e.key = "D_" + countryId + "_" + stateId + "_" + state.Value;
                    e.lat = 0;
                    e.lng = 0;
                    mEntries.put(e.id, e);
                }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static class State {
        String Text;
        int Value;
    }


    private static void geocode(Entry e, boolean onlyCache) {
        String name = e.name + "+INDONESIA";
        Entry parent = e;

/*        while ((parent = mEntries.get(parent.parent)) != null && parent.parent != 0) {
            name = parent.name + ", " + name;
        }
*/
        if (mGeocodings.containsKey(name)) {
            Geocoder gc = mGeocodings.get(name);
            e.lat = gc.lat;
            e.lng = gc.lon;
            return;
        }

        String url = "http://nominatim.openstreetmap.org/search?q="
                + URLEncoder.encode(name.replace("+", " ")
                .replace("ABD,", "USA,"))
                + "&format=json&limit=1&email=" + MAIL_ADRESS + "&namedetails=1";
        String data =get(url);

        List<Geocoder> result = null;
        try {
            result = new Gson().fromJson(data, new TypeToken<List<Geocoder>>() {
            }.getType());
        } catch (Exception exception) {
        
        }
        if (result != null && result.size() != 0) {
            Geocoder gc = result.get(0);
            e.lat = gc.lat;
            e.lng = gc.lon;
            mGeocodings.put(name, gc);
        }
    }


    private static String extract(String data, String s) {
        data = data.substring(data.indexOf(s));
        data = data.substring(data.indexOf("=") + 2);
        data = data.substring(0, data.indexOf("\""));
        return data;
    }

    private static void geocodeNVC(Entry entry) {
        String name = entry.name;
        Entry parent = entry;
        while ((parent = mEntries.get(parent.parent)) != null && parent.parent != 0) {
            name = parent.name + ", " + name;
        }

        if (mGeocodings.containsKey(name)) {
            Geocoder gc = mGeocodings.get(name);
            entry.lat = gc.lat;
            entry.lng = gc.lon;
            return;
        }

        try {
            String data = HTTP.get("http://namazvakti.com/XML.php?cityID=" + entry.key.substring(2));
            String arzDer = extract(data, "arzDer");
            String arzDak = extract(data, "arzDak");
            String arzYon = extract(data, "arzYon");
            String tulDer = extract(data, "tulDer");
            String tulDak = extract(data, "tulDak");
            String tulYon = extract(data, "tulYon");

            double lat = (arzYon.endsWith("N") ? 1 : -1) * (Double.parseDouble(arzDer) + Double.parseDouble(arzDak) / 60.0);
            double lng = (tulYon.endsWith("E") ? 1 : -1) * (Double.parseDouble(tulDer) + Double.parseDouble(tulDak) / 60.0);
            entry.lat = lat;
            entry.lng = lng;

            Geocoder gc = new Geocoder();
            gc.lat = lat;
            gc.lon = lng;
            mGeocodings.put(name, gc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void geocode() {
        int size = mEntries.values().size();
        AtomicInteger count = new AtomicInteger();
        for (Entry e : mEntries.values()) {
            if ((e.lat != 0 && e.lng != 0) || e.key == null) {
                count.incrementAndGet();
                continue;
            }

            if (e.key.startsWith("N_")) {
                mWorker.execute(() -> {
                    geocode(e, true);
                    if (e.lat == 0 && e.lng == 0)
                        geocodeNVC(e);
                });
            } else {
                mWorker.execute(() -> {
                    geocode(e, false);
                    System.out.println(count.incrementAndGet() + "/" + size);
                });

            }
        }

        while (mTaskCounter.get() != 0) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private static class Ilce {
        List<Sehir> ilceler = new ArrayList<>();
    }

    private static class Sehirler {
        List<Sehir> sehirler = new ArrayList<>();
    }

    private static class Sehir {
        String adi;
        int id;
    }

    private static class Geocoder {
        double lat;
        double lon;
    }


    public static int i(int i) {
        return i < 0 ? 0 : i;
    }
}
