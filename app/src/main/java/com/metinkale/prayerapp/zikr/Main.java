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
 *
 */

package com.metinkale.prayerapp.zikr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.vakit.PrefsView;
import com.metinkale.prayerapp.vakit.PrefsView.PrefsFunctions;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public class Main extends BaseActivity implements OnClickListener, OnNavigationListener, OnLongClickListener {

    private SharedPreferences mPrefs;
    private ZikrView mZikr;
    private EditText mTitle;
    private Vibrator mVibrator;
    @Nullable
    private Zikr mCurrent;
    @NonNull
    private List<Zikr> mZikrList = new ArrayList<>();
    private ImageView mReset;
    private int mVibrate;

    @Override
    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        setContentView(R.layout.zikr_main);
        mPrefs = getSharedPreferences("zikr", 0);
        mZikr = (ZikrView) findViewById(R.id.zikr);
        mTitle = (EditText) findViewById(R.id.title);
        mZikr.setOnClickListener(this);
        mZikr.setOnLongClickListener(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mReset = (ImageView) findViewById(R.id.reset);
        mReset.setOnClickListener(this);
        mVibrate = PreferenceManager.getDefaultSharedPreferences(this).getInt("zikrvibrate2", 0);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        ((PrefsView) findViewById(R.id.vibration)).setPrefFunctions(new PrefsFunctions() {

            @Override
            public Object getValue() {
                return mVibrate;
            }

            @Override
            public void setValue(Object obj) {
                PreferenceManager.getDefaultSharedPreferences(Main.this).edit().putInt("zikrvibrate2", (Integer) obj).apply();
                mVibrate = (Integer) obj;

            }

        });
    }

    private void saveCurrent() {
        if (mCurrent == null) {
            return;
        }
        mCurrent.title = mTitle.getText().toString();
        mCurrent.color = mZikr.getColor();
        mCurrent.count = mZikr.getCount();
        mCurrent.max = mZikr.getMax();
        mCurrent.count2 = mZikr.getCount2();

        mPrefs.edit().putStringSet(mCurrent.key, new HashSet<>(Arrays.asList(new String[]{"0" + mCurrent.title, "1" + mCurrent.color, "2" + mCurrent.count, "3" + mCurrent.count2, "4" + mCurrent.max}))).apply();
    }

    @Override
    public void onResume() {
        super.onResume();

        Map<String, Set<String>> map = (Map<String, Set<String>>) mPrefs.getAll();
        Set<String> keys = map.keySet();
        mZikrList = new ArrayList<>();
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            Set set = map.get(key);
            if (set == null) {
                continue;
            }
            try {
                List<String> list = new ArrayList<String>(set);
                Collections.sort(list);
                Zikr zikr = new Zikr();
                zikr.title = list.get(0).substring(1);
                zikr.color = Integer.parseInt(list.get(1).substring(1));
                zikr.count = Integer.parseInt(list.get(2).substring(1));
                zikr.count2 = Integer.parseInt(list.get(3).substring(1));
                zikr.max = Integer.parseInt(list.get(4).substring(1));
                zikr.key = key;
                mZikrList.add(zikr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        createDefault();

        load(mCurrent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrent();
    }

    private void createDefault() {
        if (mZikrList.isEmpty()) {
            mZikrList = new ArrayList<>();
            mCurrent = new Zikr();
            mCurrent.title = getString(R.string.tasbih);
            mCurrent.max = 33;
            mCurrent.key = "default";
            mZikrList.add(mCurrent);
            saveCurrent();
        }
    }

    private void load(@Nullable Zikr z) {
        if (!mZikrList.contains(z) && (z != null)) {
            mZikrList.add(z);
        }

        if (mCurrent != z) {
            saveCurrent();
            mCurrent = z;
        }
        if (z != null) {
            mTitle.setText(z.title);
            mZikr.setColor(z.color);
            mZikr.setCount(z.count);
            mZikr.setCount2(z.count2);
            mZikr.setMax(z.max);
        }

        if ((z != null) && "default".equals(z.key)) {
            mTitle.setEnabled(false);
            mTitle.setText(getString(R.string.tasbih));
        } else {
            mTitle.setEnabled(true);
        }
        ArrayList<String> itemList = new ArrayList<>();
        for (Zikr zi : mZikrList) {
            itemList.add(zi.title);
        }
        Context c = new ContextThemeWrapper(this, R.style.ToolbarTheme);
        SpinnerAdapter aAdpt = new ArrayAdapter<>(c, android.R.layout.simple_list_item_1, android.R.id.text1, itemList);
        getSupportActionBar().setListNavigationCallbacks(aAdpt, this);
        getSupportActionBar().setSelectedNavigationItem(mZikrList.indexOf(z));

    }

    public void changeColor(@NonNull View v) {
        int c = Color.parseColor((String) v.getTag());
        mZikr.setColor(c);
    }

    @Override
    public void onClick(View v) {
        if (v == mZikr) {
            mZikr.setCount(mZikr.getCount() + 1);

            if (mZikr.getCount() == mZikr.getMax()) {
                mZikr.counter();
                if (mVibrate != -1) {
                    mVibrator.vibrate(new long[]{0, 100, 100, 100, 100, 100}, -1);
                }
                mZikr.setCount(0);
            } else if (mVibrate == 0) {
                mVibrator.vibrate(10);
            }

        } else if (v == mReset) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle(R.string.dhikr);
            dialog.setMessage(getString(R.string.resetConfirmDhikr, mCurrent.title));
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mZikr.setCount(0);
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (mZikrList.indexOf(mCurrent) != itemPosition) {
            load(mZikrList.get(itemPosition));
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MaterialMenuInflater.with(this)
                .setDefaultColor(0xFFFFFFFF)
                .inflate(R.menu.zikr, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Zikr z = new Zikr();
                z.key = System.currentTimeMillis() + "";
                z.title = getString(R.string.newDhikr);
                mZikrList.add(z);
                load(z);
                onLongClick(null);
                return true;

            case R.id.del:
                if ("default".equals(mCurrent.key)) {
                    return false;
                }
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle(R.string.delete);
                dialog.setMessage(getString(R.string.delConfirmDhikr, mCurrent.title));
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mZikrList.remove(mCurrent);
                        mPrefs.edit().remove(mCurrent.key).apply();
                        mCurrent = null;
                        createDefault();
                        load(mZikrList.get(0));
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onLongClick(View arg0) {
        if (mZikrList.indexOf(mCurrent) == 0) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dhikrCount);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(mZikr.getMax() + "");

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    mZikr.setMax(Integer.parseInt(input.getText().toString()));
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });

        builder.show();
        return false;
    }


    private static class Zikr {
        String title;
        int max;
        int count;
        int color;
        int count2;
        @Nullable
        String key;
    }

}
