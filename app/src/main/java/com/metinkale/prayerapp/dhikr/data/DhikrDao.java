package com.metinkale.prayerapp.dhikr.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface DhikrDao {
    @Query("select * from dhikr ORDER BY position")
    LiveData<List<Dhikr>> getAllDhikrs();

    @Update
    void saveDhikr(Dhikr...dhikr);

    @Insert(onConflict = REPLACE)
    void addDhikr(Dhikr...dhikr);

    @Delete
    void deleteDhikr(Dhikr...dhikr);
}
