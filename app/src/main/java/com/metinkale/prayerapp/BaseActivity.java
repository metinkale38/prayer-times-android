/*
 * Copyright (c) 2016 Metin Kale
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

package com.metinkale.prayerapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.crashlytics.android.Crashlytics;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.BuildConfig;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.hadis.SqliteHelper;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Changelog;
import com.metinkale.prayerapp.utils.PermissionUtils;

import java.io.File;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String[] mActs = {"vakit", "compass", "names", "calendar", "tesbihat", "hadis", "kaza", "zikr", "settings", "about"};
    private static final int[] ICONS = {R.drawable.ic_menu_times, R.drawable.ic_menu_compass, R.drawable.ic_menu_names,
            R.drawable.ic_menu_calendar, R.drawable.ic_menu_tesbihat, R.drawable.ic_menu_hadith, R.drawable.ic_menu_missed,
            R.drawable.ic_menu_dhikr, R.drawable.ic_menu_settings, R.drawable.ic_menu_about};
    private int mNavPos;
    private ListView mNav;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    public BaseActivity() {
        String clz = getClass().toString();
        for (int i = 0; i < mActs.length; i++) {
            if (clz.contains("prayerapp." + mActs[i])) {
                mNavPos = i;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(Prefs.isNightMode() ?
                AppCompatDelegate.MODE_NIGHT_YES :
                AppCompatDelegate.MODE_NIGHT_NO);
        if (App.getContext() == null) {
            App.setContext(this);
        }


        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            int id = R.mipmap.ic_launcher;
            switch (mNavPos) {
                case 1:
                    id = R.mipmap.ic_compass;
                    break;
                case 2:
                    id = R.mipmap.ic_names;
                    break;
                case 3:
                    id = R.mipmap.ic_calendar;
                    break;
                case 4:
                    id = R.mipmap.ic_tesbihat;
                    break;
                case 7:
                    id = R.mipmap.ic_zikr;
                    break;
            }

            Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, id);


            Intent intent = new Intent();
            Intent launchIntent = new Intent(this, getClass());
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchIntent.putExtra("duplicate", false);

            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getStringArray(R.array.dropdown)[mNavPos]);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

            setResult(RESULT_OK, intent);
            finish();
        }


        if (!Utils.askLang(this)) {
            Utils.init();
            Changelog.start(this);
        }

    }

    @Override
    public void setContentView(int res) {
        super.setContentView(R.layout.activity_base);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        //toolbar.setNavigationIcon(R.drawable.ic_abicon);
        toolbar.setBackgroundResource(R.color.colorPrimary);

        ViewGroup content = (ViewGroup) findViewById(R.id.content);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        View v = LayoutInflater.from(this).inflate(res, content, false);
        content.addView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mNav = (ListView) mDrawerLayout.findViewById(R.id.base_nav);
        ArrayAdapter<String> list = new ArrayAdapter<String>(this, R.layout.drawer_list_item, getResources().getStringArray(R.array.dropdown)) {
            @Override
            public View getView(int pos, View v, ViewGroup p) {
                v = super.getView(pos, v, p);
                if (pos == mNavPos) {
                    ((TextView) v).setTypeface(null, Typeface.BOLD);
                }
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ICONS[pos], 0, 0, 0);


                return v;
            }

            @Override
            public int getCount() {
                if (BuildConfig.DEBUG)
                    return super.getCount();
                return super.getCount() - 1;
            }
        };
        mNav.setAdapter(list);
        mNav.setOnItemClickListener(new MyClickListener());

        final String title = list.getItem(mNavPos);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.appName, R.string.appName) {

            @Override
            public void onDrawerClosed(View view) {
                toolbar.setTitle(title);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                toolbar.setTitle(title);
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(title);
            }
        });

        // Set the activity_base toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (getIntent().getBooleanExtra("anim", false)) {

            mDrawerLayout.openDrawer(GravityCompat.START);
            mDrawerLayout.post(new Runnable() {

                @Override
                public void run() {
                    mDrawerLayout.closeDrawers();

                }
            });

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mNav.setSelection(mNavPos);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtils.get(this).onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    private class MyClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            if (pos == mNavPos) {
                mDrawerLayout.closeDrawers();
                return;
            }

            Intent i = null;
            switch (pos) {
                case 0:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.vakit.Main.class);
                    break;
                case 1:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.compass.Main.class);
                    break;
                case 2:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.names.Main.class);
                    break;
                case 3:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.calendar.Main.class);
                    break;
                case 4:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.tesbihat.Tesbihat.class);
                    break;
                case 5:

                    String lang = Prefs.getLanguage();

                    if (lang.equals("ar")) lang = "en";
                    String file = lang + "/hadis.db";
                    File f = new File(App.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file);


                    if (f.exists()) {
                        try {
                            if (SqliteHelper.get().getCount() == 0) {
                                SqliteHelper.get().close();
                                ((Object) null).toString();
                            }
                        } catch (Exception e) {
                            f.delete();
                            onItemClick(null, null, 5, 0);
                        }
                        i = new Intent(BaseActivity.this, com.metinkale.prayerapp.hadis.Main.class);
                    } else {
                        f.delete();
                        AlertDialog dialog = new AlertDialog.Builder(BaseActivity.this).create();
                        dialog.setTitle(R.string.hadith);
                        dialog.setMessage(getString(R.string.dlHadith));
                        dialog.setCancelable(false);
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int buttonId) {
                                if (Prefs.getLanguage() == null) {
                                    return;
                                }
                                String lang = Prefs.getLanguage();
                                if (lang.equals("ar")) lang = "en";

                                String file = lang + "/hadis.db";
                                File f = new File(App.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file);

                                String url = App.API_URL + "/hadis." + lang + ".db";

                                f.getParentFile().mkdirs();
                                final ProgressDialog dlg = new ProgressDialog(BaseActivity.this);
                                dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                dlg.show();
                                Ion.with(BaseActivity.this)
                                        .load(url)
                                        .progressDialog(dlg)
                                        .write(f)
                                        .setCallback(new FutureCallback<File>() {
                                            @Override
                                            public void onCompleted(Exception e, File result) {
                                                dlg.cancel();
                                                if (e != null) {
                                                    e.printStackTrace();
                                                    Crashlytics.logException(e);
                                                    Toast.makeText(BaseActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                                } else if (result.exists()) {
                                                    onItemClick(null, null, 5, 0);
                                                }
                                            }
                                        });

                            }
                        });
                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int buttonId) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                        return;
                    }

                    break;
                case 6:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.kaza.Main.class);
                    break;
                case 7:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.zikr.Main.class);
                    break;
                case 8:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.settings.Settings.class);
                    break;
                case 9:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.about.AboutAct.class);
                    break;
                default:
                    i = new Intent(BaseActivity.this, com.metinkale.prayerapp.vakit.Main.class);
            }

            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("anim", true);
            startActivity(i);

            overridePendingTransition(0, 0);

            mDrawerLayout.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mDrawerLayout.closeDrawers();

                }
            }, 500);

        }
    }
}
