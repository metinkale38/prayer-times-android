/*
 * Copyright (c) 2013-2026 Metin Kale
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
package com.metinkale.prayer.dhikr.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Dhikr::class], version = 2, exportSchema = false)
abstract class DhikrDatabase : RoomDatabase() {
    abstract fun dhikrDao(): DhikrDao?

    companion object {
        private var INSTANCE: DhikrDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE dhikr ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE dhikr ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE dhikr ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE dhikr SET syncId = 'legacy-' || id WHERE syncId = ''")
            }
        }

        @JvmStatic
        fun getDatabase(context: Context): DhikrDatabase {
            return INSTANCE ?: run {
                if (!context.getDatabasePath("dhikr").exists()) {
                    context.getDatabasePath("prayertimes").takeIf { it.exists() }
                        ?.renameTo(context.getDatabasePath("dhikr"))
                    context.getDatabasePath("prayertimes-shn").takeIf { it.exists() }
                        ?.renameTo(context.getDatabasePath("dhikr-shm"))
                    context.getDatabasePath("prayertimes-wal").takeIf { it.exists() }
                        ?.renameTo(context.getDatabasePath("dhikr-wal"))
                }
                databaseBuilder(
                    context.applicationContext,
                    DhikrDatabase::class.java,
                    "dhikr"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }
    }
}