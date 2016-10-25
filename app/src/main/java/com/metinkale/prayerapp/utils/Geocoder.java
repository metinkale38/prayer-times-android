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

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;

import java.util.List;

public class Geocoder {

    public static ResponseFuture<Response> from(String address) {
        return Ion.with(App.getContext())
                .load("http://maps.google.com/maps/api/geocode/json?address=" + address + "&language=" + Prefs.getLanguage())
                .as(Response.class);
    }


    public static class Response {
        public String status;
        public List<Result> results;
    }


    public static class Result {
        public List<String> types;
        public List<Address> address_components;
        public String formatted_address;
        public String place_id;
        public Geometry geometry;

        @Override
        public String toString() {
            return formatted_address;
        }
    }


    public static class Address {
        public String long_name;
        public String short_name;
        public List<String> types;
    }


    public static class Geometry {
        public LatLng location;
        public Bounds bounds;
        public String location_type;
        public Bounds viewport;
    }

    public static class LatLng {
        public double lat;
        public double lng;
    }

    public static class Bounds {
        public LatLng northeast;
        public LatLng southwest;
    }
}