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

package com.metinkale.prayer.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;

import java.util.List;


public class Geocoder {

    public static class Result {
        private String city;
        private String county;
        private String state;
        private String country;
        private double lat;
        private double lon;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public String getName() {
            if (city != null) return city;
            if (county != null) return county;
            if (state != null) return state;
            return null;
        }
    }

    public interface GeocoderCallback {
        void onResult(@Nullable Result entry);
    }


    public static void reverse(final double lat, final double lng, @NonNull final GeocoderCallback callback) {
        Ion.with(App.get())
                .load("http://nominatim.openstreetmap.org/reverse?format=json&email=metinkale38@gmail.com&lat=" + lat +
                        "&lon=" + lng + "&accept-language=" + LocaleUtils.getLocale().getLanguage())
                .as(OSMReverse.class).withResponse().setCallback((e, response) -> {
                    if (e != null) e.printStackTrace();
                    OSMReverse result = response == null ? null : response.getResult();

                    Result Result = null;
                    if (result != null && result.address != null) {
                        Result = new Result();
                        Result.setCountry(result.address.country);
                        Result.setCity(result.address.city);
                        Result.setCounty(result.address.county);
                        Result.setState(result.address.state);
                        Result.setLat(result.lat);
                        Result.setLon(result.lon);
                    }
                    callback.onResult(Result);
                });

    }


    public static void search(String query, @NonNull final GeocoderCallback callback) {
        Ion.with(App.get())
                .load("http://nominatim.openstreetmap.org/search?format=jsonv2&email=metinkale38@gmail.com&q=" + query
                        + "&accept-language=" + LocaleUtils.getLocale().getLanguage())
                .as(new TypeToken<List<OSMPlace>>() {
                }).withResponse().setCallback((e, response) -> {
                    List<OSMPlace> result;
                    if (response == null || (result = response.getResult()) == null || result.isEmpty()) {
                        callback.onResult(null);
                        return;
                    }

                    if (e != null) e.printStackTrace();

                    Result entry = null;
                    if (result.size() >= 1) {
                        OSMPlace res = result.get(0);
                        String[] parts = res.display_name.split(", ");
                        entry = new Result();
                        for (int i = 0; i < parts.length - 1; i++) {
                            if (i == 0) entry.setCity(parts[i]);
                            else if (i == 1) entry.setCounty(parts[i]);
                            else if (i == 2) entry.setState(parts[i]);
                        }
                        entry.setLat(res.lat);
                        entry.setLon(res.lon);
                        entry.setCountry(parts[parts.length - 1]);
                    }
                    callback.onResult(entry);

                });


    }


    private static class OSMReverse {
        double lat;
        double lon;
        Result address;
    }


    private static class OSMPlace {
        double lat;
        double lon;
        String display_name;
    }


}