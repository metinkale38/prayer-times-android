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
package com.metinkale.prayerapp.vakit

import android.appwidget.AppWidgetManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.metinkale.prayer.receiver.InternalBroadcastReceiver
import com.metinkale.prayer.times.times.Times
import com.metinkale.prayer.widgets.R

open class WidgetConfigure : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var onlyCity = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            onlyCity = extras.getBoolean("onlyCity", false)
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        cityDialog()
    }

    open fun cityDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.cities)
        val array = arrayOfNulls<String>(Times.current.size)
        for (i in array.indices) {
            val t: Times? = Times.getTimesByIndex(i).current
            if (t == null) {
                array[i] = "DELETED"
            } else {
                array[i] = t.name + " (" + t.source.name + ")"
            }
        }
        builder.setItems(array) { dialog: DialogInterface?, which: Int ->
            getSharedPreferences("widgets", 0).edit()
                .putInt("" + appWidgetId, Times.getTimesByIndex(which).current?.id ?: 0).apply()
            if (onlyCity) result() else themeDialog()
        }
        builder.show()
    }

    open fun themeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.widgetDesign)
        builder.setItems(
            arrayOf(
                getString(R.string.whiteWidget),
                getString(R.string.blackWidget),
                getString(R.string.transWhiteWidget),
                getString(R.string.transWidget)
            )
        ) { dialogInterface: DialogInterface?, which: Int ->
            getSharedPreferences("widgets", 0).edit()
                .putInt(appWidgetId.toString() + "_theme", which).apply()
            result()
        }
        builder.show()
    }

    fun result() {
        InternalBroadcastReceiver.sender(this).sendTimeTick()
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
        WidgetUtils.Companion.updateWidgets(this)
    }

    companion object {
        const val ONLYCITY = "onlyCity"
    }
}