package com.metinkale.prayerapp.dhikr.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Entity
public class Dhikr {
    @PrimaryKey(autoGenerate = true) @Setter(AccessLevel.PACKAGE)
    private int id;
    private String title;
    private int max = 33;
    private int value;
    private int color = 0xFF33B5E5;
    private int position = -1;

}
