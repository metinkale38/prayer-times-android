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

package com.metinkale.prayerapp.vakit.times;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by metin on 10.02.2017.
 */

public class Entry implements Cloneable {
    private int parent;
    private int id;
    private String name;
    private String normalized;
    @Nullable
    private String key;
    private float lat;
    private float lng;
    private String country;

    public Entry() {

    }

    public void setName(String name) {
        this.name = name;
        normalized = null;
    }

    public String getName() {
        return name;
    }

    protected int getParent() {
        return parent;
    }

    protected void setParent(int parent) {
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    public void setKey(@Nullable String key) {
        if (key == null || key.isEmpty())
            this.key = null;
        else
            this.key = key;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = (float) lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = (float) lng;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Nullable
    public Source getSource() {
        if (key == null || key.isEmpty()) return null;
        switch (key.charAt(0)) {
            case 'C':
                return Source.Calc;
            case 'I':
                return Source.IGMG;
            case 'D':
                return Source.Diyanet;
            case 'N':
                return Source.NVC;
            case 'H':
                return Source.Morocco;
            case 'M':
                return Source.Malaysia;
            case 'F':
                return Source.Fazilet;
            case 'S':
                return Source.Semerkand;
        }
        return null;
    }

    public String getNormalized() {
        if (normalized == null) {
            normalized = normalize(getName());
        }
        return normalized;
    }

    static String normalize(@NonNull String s) {
        StringBuilder builder = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 0x41 && c <= 0x5A) {//A-Z
                builder.append((char) (c + 0x20));
            } else if (c >= 0x61 && c <= 0x7A) {//a-z
                builder.append(c);
            } else {
                switch (c) {
                    case 'é':
                    case 'è':
                    case 'ê':
                    case 'ë':
                    case 'È':
                    case 'É':
                    case 'Ë':
                    case 'Ê':
                        builder.append("e");
                        break;
                    case 'Ç':
                    case 'ç':
                        builder.append("c");
                        break;
                    case 'Ğ':
                    case 'ğ':
                        builder.append("g");
                        break;
                    case 'ı':
                    case 'İ':
                    case 'ï':
                    case 'î':
                    case 'Ï':
                    case 'Î':
                        builder.append("i");
                        break;
                    case 'Ö':
                    case 'ö':
                    case 'Ô':
                        builder.append("o");
                        break;
                    case 'Ş':
                    case 'ş':
                        builder.append("s");
                        break;
                    case 'Ä':
                    case 'ä':
                    case 'à':
                    case 'â':
                    case 'À':
                    case 'Â':
                        builder.append("a");
                        break;
                    case 'ü':
                    case 'Ü':
                    case 'û':
                    case 'ù':
                    case 'Û':
                    case 'Ù':
                        builder.append("u");
                        break;
                    default:
                        builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    @Override
    protected Object clone() {
        Entry e = new Entry();
        e.id = id;
        e.normalized = normalized;
        e.name = name;
        e.lat = lat;
        e.lng = lng;
        e.country = country;
        e.key = key;
        e.parent = parent;
        return e;
    }
}