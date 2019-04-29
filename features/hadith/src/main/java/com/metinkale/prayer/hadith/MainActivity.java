/*
 * Copyright (c) 2013-2019 Metin Kale
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

package com.metinkale.prayer.hadith;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.utils.LocaleUtils;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class MainActivity extends BaseActivity {
    
    public MainActivity() {
        super(R.string.hadith, R.mipmap.ic_hadith, new Fragment());
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String lang = LocaleUtils.getLanguage("en", "de", "tr");
        final String file = lang + "/hadis.db";
        final String url = App.API_URL + "/files/hadis." + lang + ".db";
        File f = new File(App.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file);
        
        
        if (f.exists()) {
            try {
                if (SqliteHelper.get().getCount() == 0) {
                    SqliteHelper.get().close();
                    ((Object) null).toString();
                }
            } catch (Exception e) {
                if (f.exists() && !f.delete()) {
                    Log.e("BaseActivity", "could not delete " + f.getAbsolutePath());
                }
                finish();
            }
            setDefaultFragment(new HadithFragment());
            moveToFrag(getDefaultFragment());
        } else if (!App.isOnline()) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle(R.string.hadith);
            dialog.setMessage(getString(R.string.dlHadith));
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), (dialogInterface, i) -> {

                File f1 = new File(App.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file);


                if (!f1.getParentFile().mkdirs()) {
                    Log.e("BaseActivity", "could not mkdirs " + f1.getParent());
                }
                final ProgressDialog dlg = new ProgressDialog(MainActivity.this);
                dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dlg.setCancelable(false);
                dlg.setCanceledOnTouchOutside(false);
                dlg.show();
                Ion.with(MainActivity.this).load(url).progressDialog(dlg).write(f1).setCallback((e, result) -> {

                    dlg.dismiss();
                    if (e != null) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                        finish();
                    } else if (result.exists()) {
                        openHadithFrag();
                    }
                });
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel());
            dialog.show();
        }
        
    }
    
    private void openHadithFrag() {
        moveToFrag(new HadithFragment());
    }
}
