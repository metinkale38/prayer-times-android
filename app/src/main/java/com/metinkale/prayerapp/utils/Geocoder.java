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

package com.metinkale.prayerapp.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.vakit.times.Entry;

import java.util.ArrayList;
import java.util.List;

public class Geocoder {

    public interface ReverseCallback {
        void onResult(Entry e);
    }

    public static void reverse(double lat, double lng, @NonNull final ReverseCallback callback) {
        Ion.with(App.get())
                .load("http://nominatim.openstreetmap.org/reverse?format=json&email=metinkale38@gmail.com&lat=" + lat +
                        "&lon=" + lng + "&accept-language=" + Prefs.getLanguage())
                .as(OSMReverse.class).withResponse().setCallback(new FutureCallback<Response<OSMReverse>>() {
            @Override
            public void onCompleted(@Nullable Exception e, @NonNull Response<OSMReverse> response) {
                OSMReverse result = response.getResult();
                if (e != null) e.printStackTrace();
                if (result == null) {
                    callback.onResult(null);
                    return;
                }
                Entry entry = new Entry();
                entry.setCountry(result.address.country);
                entry.setName(result.address.county);
                entry.setLat(result.lat);
                entry.setLng(result.lon);
                callback.onResult(entry);
            }
        });

    }

    public static class Result {
        String formatted_address;
        public String city;
        public String state;
        public String country;
        public double lat;
        public double lng;

        @Override
        public String toString() {
            return formatted_address;
        }
    }

    public interface SearchCallback {
        void onResult(List<Result> results);
    }

    public static void search(String query, @NonNull final SearchCallback callback) {
        Ion.with(App.get())
                .load("http://nominatim.openstreetmap.org/search?format=jsonv2&email=metinkale38@gmail.com&q=" + query
                        + "&accept-language=" + Prefs.getLanguage())
                .as(new TypeToken<List<OSMPlace>>() {
                }).withResponse().setCallback(new FutureCallback<Response<List<OSMPlace>>>() {
            @Override
            public void onCompleted(@Nullable Exception e, @NonNull Response<List<OSMPlace>> response) {
                List<OSMPlace> result = response.getResult();
                List<Result> allresults = new ArrayList<>();
                if (e != null) e.printStackTrace();
                if (result == null || result.size() == 0) {
                    callback.onResult(allresults);
                    return;
                }

                for (OSMPlace res : result) {
                    Result r = new Result();
                    r.lat = res.lat;
                    r.lng = res.lon;
                    r.formatted_address = res.display_name;
                    String[] parts = res.display_name.split(", ");
                    r.city = parts[0];
                    r.state = parts[(parts.length - 1) / 2];
                    r.country = parts[parts.length - 1];
                    allresults.add(r);

                }


                callback.onResult(allresults);

            }
        });


    }


    private static class OSMReverse {
        double lat;
        double lon;
        String display_name;
        OSMReverseAdress address;
    }

    private static class OSMReverseAdress {
        String city;
        String county;
        String state;
        String country;
    }

    private static class OSMPlace {
        double lat;
        double lon;
        String display_name;
    }


}