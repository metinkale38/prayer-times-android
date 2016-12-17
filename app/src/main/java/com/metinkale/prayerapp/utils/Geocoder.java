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
 */

package com.metinkale.prayerapp.utils;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;

import java.util.ArrayList;
import java.util.List;

public class Geocoder {

    public interface ReverseCallback {
        void onResult(String city, String state, String country, double lat, double lng);
    }

    public static void reverse(double lat, double lng, final ReverseCallback callback) {
        if (Prefs.useOSM()) {
            Ion.with(App.getContext())
                    .load("http://nominatim.openstreetmap.org/reverse?format=json&email=metinkale38@gmail.com&lat=" + lat +
                            "&lon=" + lng + "&accept-language=" + Prefs.getLanguage())
                    .as(OSMReverse.class).withResponse().setCallback(new FutureCallback<Response<OSMReverse>>() {
                @Override
                public void onCompleted(Exception e, Response<OSMReverse> response) {
                    OSMReverse result = response.getResult();
                    if (e != null) e.printStackTrace();
                    if (result == null) return;
                    callback.onResult(result.address.county, result.address.state, result.address.country, result.lat, result.lon);
                }
            });
        } else {
            Ion.with(App.getContext())
                    .load("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat +
                            "," + lng + "&language=" + Prefs.getLanguage())
                    .as(GResponse.class).setCallback(new FutureCallback<GResponse>() {
                @Override
                public void onCompleted(Exception e, GResponse result) {
                    if (e != null) e.printStackTrace();
                    if (result == null || !result.status.equals("OK") || result.results.size() == 0)
                        return;
                    GResult res = result.results.get(0);
                    String country = null;
                    String state = null;
                    String city = null;
                    double lat = res.geometry.location.lat;
                    double lng = res.geometry.location.lng;
                    for (GAddress ad : res.address_components) {
                        if (ad.types.contains("country"))
                            country = ad.long_name;
                        else if (ad.types.contains("administrative_area_level_1"))
                            state = ad.long_name;
                        else if (ad.types.contains("political")) {
                            city = ad.long_name;
                            break;
                        }
                    }

                    callback.onResult(city, state, country, lat, lng);
                }
            });
        }
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

    public static void search(String query, final SearchCallback callback) {
        if (Prefs.useOSM()) {
            Ion.with(App.getContext())
                    .load("http://nominatim.openstreetmap.org/search?format=jsonv2&email=metinkale38@gmail.com&q=" + query
                            + "&accept-language=" + Prefs.getLanguage())
                    .as(new TypeToken<List<OSMPlace>>() {
                    }).withResponse().setCallback(new FutureCallback<Response<List<OSMPlace>>>() {
                @Override
                public void onCompleted(Exception e, Response<List<OSMPlace>> response) {
                    List<OSMPlace> result = response.getResult();
                    if (e != null) e.printStackTrace();
                    if (result == null || result.size() == 0)
                        return;
                    List<Result> allresults = new ArrayList<>();
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

        } else {
            Ion.with(App.getContext())
                    .load("https://maps.googleapis.com/maps/api/geocode/json?address=" + query
                            + "&language=" + Prefs.getLanguage())
                    .as(GResponse.class).setCallback(new FutureCallback<GResponse>() {
                @Override
                public void onCompleted(Exception e, GResponse result) {
                    if (e != null) e.printStackTrace();
                    if (result == null || !result.status.equals("OK") || result.results.size() == 0)
                        return;
                    List<Result> allresults = new ArrayList<>();
                    for (GResult res : result.results) {
                        Result r = new Result();
                        r.lat = res.geometry.location.lat;
                        r.lng = res.geometry.location.lng;
                        r.formatted_address = res.formatted_address;
                        for (GAddress ad : res.address_components) {
                            if (ad.types.contains("country"))
                                r.country = ad.long_name;
                            else if (ad.types.contains("administrative_area_level_1"))
                                r.state = ad.long_name;
                            else if (ad.types.contains("political")) {
                                r.city = ad.long_name;
                                break;
                            }
                        }
                        allresults.add(r);
                    }


                    callback.onResult(allresults);
                }
            });
        }
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

    private static class GResponse {
        public String status;
        public List<GResult> results;
    }


    private static class GResult {
        List<GAddress> address_components;
        String formatted_address;
        GGeometry geometry;

        @Override
        public String toString() {
            return formatted_address;
        }
    }


    private static class GAddress {
        public String long_name;
        public List<String> types;
    }


    private static class GGeometry {
        public GLatLng location;
    }

    private static class GLatLng {
        public double lat;
        public double lng;
    }

}