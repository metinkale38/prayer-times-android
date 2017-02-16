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
    private double lat;
    private double lng;
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
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
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