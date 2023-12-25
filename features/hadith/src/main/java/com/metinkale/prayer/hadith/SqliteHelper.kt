/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.hadith

import android.annotation.SuppressLint
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment
import com.metinkale.prayer.App
import com.metinkale.prayer.utils.LocaleUtils.getLanguage
import java.io.File
import java.text.Normalizer
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("Range")
class SqliteHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, "hadis.db", null, 1) {
    @Volatile
    private var db: SQLiteDatabase? = null
    private val openCounter = AtomicInteger()
    private val file: File

    init {
        file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: File("")
        mInstance = this
    }

    val count by lazy {
        withDatabase {
            val c = query("HADIS", null, null, null, null, null, null)
            val ret = c.count
            c.close()
            ret
        }
    }

    val categories: List<String> by lazy {
        withDatabase {
            buildList {
                val c = query("HADIS", arrayOf("KONU"), null, null, "KONU", null, "ID")
                c.moveToFirst()
                do {
                    val cat = c.getString(c.getColumnIndex("KONU"))
                    if (!contains(cat)) {
                        add(cat)
                    }
                } while (c.moveToNext())
                // Collections.sort(categories);
                c.close()
            }
        }
    }


    fun search(query: CharSequence): List<Int> = withDatabase {
        val list: MutableList<Int> = ArrayList()
        val c = query("HADIS", null, null, null, null, null, null)
        c.moveToFirst()
        if (c.isAfterLast) {
            return@withDatabase list
        }
        do {
            var txt = c.getString(c.getColumnIndex("KONU"))
            txt += c.getString(c.getColumnIndex("DETAY"))
            txt += c.getString(c.getColumnIndex("HADIS"))
            txt += c.getString(c.getColumnIndex("KAYNAK"))
            if (normalize(txt).contains(normalize(query))) {
                list.add(c.getInt(c.getColumnIndex("ID")))
            }
        } while (c.moveToNext())
        c.close()
        list
    }

    operator fun get(cat: String): Collection<Int> = withDatabase {
        val list: MutableCollection<Int> = ArrayList()
        val c = query("HADIS", arrayOf("ID"), "KONU = \"$cat\"", null, null, null, null)
        c.moveToFirst()
        if (c.isAfterLast) {
            return@withDatabase list
        }
        do {
            list.add(c.getInt(c.getColumnIndex("ID")))
        } while (c.moveToNext())
        c.close()
        list
    }

    operator fun get(id: Int): Hadis? {
        return withDatabase {
            val c = query("HADIS", null, "ID=$id", null, null, null, null, "1")
            c.moveToFirst()
            if (c.isAfterLast) {
                return@withDatabase null
            }
            val h = Hadis()
            h.id = id
            h.konu = c.getString(c.getColumnIndex("KONU"))
            h.detay = c.getString(c.getColumnIndex("DETAY"))
            h.hadis = c.getString(c.getColumnIndex("HADIS"))
            h.kaynak = c.getString(c.getColumnIndex("KAYNAK"))
            c.close()
            h
        }
    }


    private fun <T> withDatabase(init: SQLiteDatabase.() -> T): T = try {
        openDatabase()
        init.invoke(db!!)
    } finally {
        closeDatabase()
    }


    @Throws(SQLException::class)
    private fun openDatabase() {
        openCounter.incrementAndGet()
        if (db == null) {
            synchronized(this) {
                if (db == null) {
                    val lang = getLanguage("en", "de", "tr")
                    db = SQLiteDatabase.openDatabase(
                        file.absolutePath + "/" + lang + "/hadis.db",
                        null,
                        SQLiteDatabase.OPEN_READWRITE
                    )
                }
            }
        }
    }

    @Throws(SQLException::class)
    private fun closeDatabase() {
        if (openCounter.decrementAndGet() == 0) {
            if (db != null) {
                synchronized(this) {
                    if (db != null) {
                        db!!.close()
                        db = null
                    }
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        file.delete()
    }

    class Hadis {
        var id = 0

        @JvmField
        var konu: String? = null

        @JvmField
        var detay: String? = null

        @JvmField
        var hadis: String? = null

        @JvmField
        var kaynak: String? = null
    }

    companion object {
        private lateinit var mInstance: SqliteHelper
        private fun normalize(str: CharSequence): String {
            var string = Normalizer.normalize(str, Normalizer.Form.NFD)
            string = string.replace("[^\\p{ASCII}]".toRegex(), "_")
            return string.lowercase()
        }

        @JvmStatic
        fun get(): SqliteHelper {
            if (!this::mInstance.isInitialized) {
                SqliteHelper(App.get())
            }
            return mInstance
        }
    }
}