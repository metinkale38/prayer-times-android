package com.metinkale.prayer.dhikr.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Dhikr.class}, version = 1, exportSchema = false)
public abstract class DhikrDatabase extends RoomDatabase {
    private static DhikrDatabase INSTANCE;

    public static DhikrDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), DhikrDatabase.class, "prayertimes").build();
        }
        return INSTANCE;
    }

    public DhikrDatabase() {
        super();
    }

    public abstract DhikrDao dhikrDao();
}