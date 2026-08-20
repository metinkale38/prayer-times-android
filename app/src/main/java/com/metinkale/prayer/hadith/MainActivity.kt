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
package com.metinkale.prayer.hadith

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.metinkale.prayer.App
import com.metinkale.prayer.App.Companion.isOnline
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.CrashReporter
import com.metinkale.prayer.R
import com.metinkale.prayer.utils.LocaleUtils.getLanguage
import java.io.File

class MainActivity : BaseActivity(R.string.hadith, R.mipmap.ic_hadith, Fragment()) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lang = getLanguage("en", "de", "tr")
        val file = lang + "/hadis.db"
        val url = App.API_URL + "/files/hadis." + lang + ".db"
        val f = File(App.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file)


        if (f.exists()) {
            try {
                if (SqliteHelper.get().count == 0) {
                    SqliteHelper.get().close()
                }
            } catch (e: Exception) {
                CrashReporter.recordException(e)
                if (f.exists() && !f.delete()) {
                    Log.e("BaseActivity", "could not delete " + f.absolutePath)
                }
                finish()
            }
            defaultFragment = HadithFragment()
            moveToFrag(defaultFragment)
        } else if (!isOnline()) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
        } else {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setTitle(R.string.hadith)
            dialog.setMessage(getString(R.string.dlHadith))
            dialog.setCancelable(false)
            dialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                getString(R.string.yes)
            ) { _: DialogInterface?, i: Int ->
                val f1 =
                    File(App.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file)
                if (!f1.getParentFile().mkdirs()) {
                    Log.e("BaseActivity", "could not mkdirs " + f1.getParent())
                }
                val dlg = ProgressDialog(this@MainActivity)
                dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                dlg.setCancelable(false)
                dlg.setCanceledOnTouchOutside(false)
                dlg.show()
                Ion.with(this@MainActivity).load(url).progressDialog(dlg).write(f1)
                    .setCallback(FutureCallback { e: Exception?, result: File? ->
                        dlg.dismiss()
                        if (e != null) {
                            CrashReporter.recordException(e)
                            Toast.makeText(this@MainActivity, R.string.error, Toast.LENGTH_LONG)
                                .show()
                            finish()
                        } else if (result!!.exists()) {
                            openHadithFrag()
                        }
                    })
            }
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.no)
            ) { dialogInterface: DialogInterface?, i: Int -> dialogInterface!!.cancel() }
            dialog.show()
        }
    }

    private fun openHadithFrag() {
        moveToFrag(HadithFragment())
    }
}
