package com.metinkale.prayer.dhikr.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public
class Dhikr {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private int max = 33;
    private int value;
    private int color = 0xFF33B5E5;
    private int position = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
