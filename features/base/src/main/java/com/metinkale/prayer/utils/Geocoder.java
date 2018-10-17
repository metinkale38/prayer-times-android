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

package com.metinkale.prayer.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.metinkale.prayer.App;

import java.util.List;

import lombok.Data;

public class Geocoder {

    @Data
    public static class Result {
        private String city;
        private String county;
        private String state;
        private String country;
        private double lat;
        private double lon;

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
                        "&lon=" + lng + "&accept-language=" + Utils.getLocale().getLanguage())
                .as(OSMReverse.class).withResponse().setCallback(new FutureCallback<Response<OSMReverse>>() {
            @Override
            public void onCompleted(@Nullable Exception e, Response<OSMReverse> response) {
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
            }
        });

    }


    public static void search(String query, @NonNull final GeocoderCallback callback) {
        Ion.with(App.get())
                .load("http://nominatim.openstreetmap.org/search?format=jsonv2&email=metinkale38@gmail.com&q=" + query
                        + "&accept-language=" + Utils.getLocale().getLanguage())
                .as(new TypeToken<List<OSMPlace>>() {
                }).withResponse().setCallback(new FutureCallback<Response<List<OSMPlace>>>() {
            @Override
            public void onCompleted(@Nullable Exception e, @Nullable Response<List<OSMPlace>> response) {
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

            }
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