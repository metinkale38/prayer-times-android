package com.metinkale.prayer.dhikr.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.REPLACE;

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
