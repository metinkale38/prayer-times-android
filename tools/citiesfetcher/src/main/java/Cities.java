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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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


/**
 * ATTENTION: THIS CLASS IS VERY BAD WRITTEN
 */
@SuppressWarnings("Duplicates")
public class Cities {
    private static final String MAIL_ADRESS = "metinkale38@gmail.com";
    private static AtomicInteger id = new AtomicInteger();
    private static Map<String, Geocoder> mGeocodings = new ConcurrentHashMap<>();
    private static Map<Integer, Entry> mEntries = Collections.synchronizedNavigableMap(new TreeMap<>());
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

    public static void main(String[] args) throws UnsupportedEncodingException {
        Locale.setDefault(Locale.ENGLISH);

        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get("geocodings.geo"));
            mGeocodings = new Gson().fromJson(new String(encoded, StandardCharsets.UTF_8), new TypeToken<ConcurrentHashMap<String, Geocoder>>() {
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

        new File("out.tsv").delete();
        try (PrintWriter out = new PrintWriter("out.tsv", "utf-8")) {
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


    private static void fetchIGMG() {
        String data = HTTP.get("https://live.igmgapp.org:8091/api/Calendar/GetPrayerTimesCities?prefLocale=DE");

        class City {
            private String name;
            private int id;
            double lat;
            double lng;
        }


        String[] cities = data.replace("\"list\":", "").split("},");
        HashMap<String, List<City>> map = new HashMap<>();
        for (String city : cities) {
            String[] fields = city.split(",");
            City entity = new City();
            for (String field : fields) {
                String value = field.substring(field.indexOf(":") + 1).replace("\"", "").split("}")[0].trim();
                if (field.contains("\"id\""))
                    entity.id = Integer.parseInt(value);
                else if (field.contains("\"name\""))
                    entity.name = value;
                else if (field.contains("\"latitude\""))
                    entity.lat = Double.parseDouble(value);
                else if (field.contains("\"longitude\""))
                    entity.lng = Double.parseDouble(value);
            }

            String country = entity.name.substring(entity.name.lastIndexOf("(") + 1);
            country = country.substring(0, 2);
            entity.name = entity.name.substring(0, entity.name.lastIndexOf(" "));

            if (!map.containsKey(country)) {
                map.put(country, new ArrayList<>());
            }
            map.get(country).add(entity);

        }


        for (String country : map.keySet()) {

            Entry parent = new Entry();
            parent.id = Cities.id.incrementAndGet();
            parent.parent = 0;
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
                case "SG":
                    parent.name = "Singapur";
                case "TR":
                    parent.name = "Türkei";
                    break;
                case "JP":
                    parent.name = "Japan";
                    break;
                case "EN":
                    parent.name = "England";
                    break;
                case "MY":
                    parent.name = "Malaysia";
                    break;
                case "NZ":
                    parent.name = "Neuseeland";
                    break;
                default:
                    System.err.println("Unknown Country Code " + parent.name);
            }


            List<City> splits = map.get(country);
            for (City c : splits) {
                Entry e = new Entry();
                e.id = Cities.id.incrementAndGet();
                e.parent = parent.id;
                e.name = c.name;
                e.key = "" + c.id;
                e.lat = c.lat;
                e.lng = c.lng;
                mEntries.put(e.id, e);

            }

        }
    }

    private static void fetchNVCCountries() {
        String data = HTTP.get("http://namazvakti.com/CountryList.php");
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
        String data = HTTP.get("http://namazvakti.com/StateList.php?countryID=" + parentId);
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
        String data = HTTP.get("http://namazvakti.com/CityList.php?countryID=" + country + "&state=" + URLEncoder.encode(state));
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
            String data = HTTP.get(Url);
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

    private static void geocode(Entry e, boolean onlyCache) {
        String name = e.name;

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

        String url = "https://nominatim.openstreetmap.org/search?q="
                + URLEncoder.encode(name.replace("+", " ")
                .replace("ABD,", "USA,"))
                + "&format=json&limit=1&email=" + MAIL_ADRESS + "&namedetails=1";
        String data = HTTP.get(url);

        List<Geocoder> result = null;
        try {
            result = new Gson().fromJson(data, new TypeToken<List<Geocoder>>() {
            }.getType());
        } catch (Exception ignored) {
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
        return Math.max(i, 0);
    }
}
