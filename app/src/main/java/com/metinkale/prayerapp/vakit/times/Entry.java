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

package com.metinkale.prayerapp.vakit.times;

import android.support.annotation.Nullable;

/**
 * Created by metin on 10.02.2017.
 */

public class Entry {
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
        return normalized;
    }

    public void setNormalized(String normalized) {
        this.normalized = normalized;
    }
}