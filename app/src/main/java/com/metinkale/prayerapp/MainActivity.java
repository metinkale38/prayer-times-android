/*
 * Copyright (c) 2013-2017 Metin Kale
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.about.AboutFragment;
import com.metinkale.prayerapp.calendar.CalendarFragment;
import com.metinkale.prayerapp.compass.CompassFragment;
import com.metinkale.prayerapp.hadis.HadithFragment;
import com.metinkale.prayerapp.hadis.SqliteHelper;
import com.metinkale.prayerapp.intro.IntroActivity;
import com.metinkale.prayerapp.kaza.KazaFragment;
import com.metinkale.prayerapp.names.NamesFragment;
import com.metinkale.prayerapp.settings.SettingsFragment;
import com.metinkale.prayerapp.tesbihat.TesbihatFragment;
import com.metinkale.prayerapp.utils.AppRatingDialog;
import com.metinkale.prayerapp.utils.PermissionUtils;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.fragments.VakitFragment;
import com.metinkale.prayerapp.zikr.ZikrFragment;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.io.File;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, AdapterView.OnItemClickListener {
    private static final String[] ACTS = {"vakit", "compass", "names", "calendar", "tesbihat", "hadis", "kaza", "zikr", "settings", "about"};
    public static final int[] ICONS = {R.drawable.ic_menu_times, R.drawable.ic_menu_compass, R.drawable.ic_menu_names,
            R.drawable.ic_menu_calendar, R.drawable.ic_menu_tesbihat, R.drawable.ic_menu_hadith, R.drawable.ic_menu_missed,
            R.drawable.ic_menu_dhikr, R.drawable.ic_menu_settings, R.drawable.ic_menu_about};
    private int mNavPos;
    private ListView mNav;
    private DrawerLayout mDrawerLayout;
    private long mStartTime;
    private Fragment mFragment;
    private Toolbar mToolbar;


    @Override
    public void onBackPressed() {
        if (!(mFragment instanceof MainFragment) || !((MainFragment) mFragment).onBackPressed())
            if (getSupportFragmentManager().getBackStackEntryCount() > 0
                    || !AppRatingDialog.showDialog(this, System.currentTimeMillis() - mStartTime))
                super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppRatingDialog.increaseAppStarts();
        IntroActivity.startIfNecessary(this);
        super.setContentView(R.layout.activity_base);

        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setBackgroundResource(R.color.colorPrimary);
            mToolbar.setNavigationIcon(MaterialDrawableBuilder.with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.MENU)
                    .setColor(Color.WHITE)
                    .setToActionbarSize()
                    .build());
        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mNav = mDrawerLayout.findViewById(R.id.base_nav);
        ArrayAdapter<String> list = buildNavAdapter(this);
        mNav.setAdapter(list);
        mNav.setOnItemClickListener(this);

        final String title = list.getItem(mNavPos);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mToolbar != null) {
                    mToolbar.setTitle(title);
                }
            }
        });


        Utils.init(this);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        String comp = getIntent().getComponent().getClassName();
        for (int i = 0; i < ACTS.length; i++) {
            if (comp.contains(ACTS[i])) {
                mNavPos = i;
            }
        }

        onItemClick(mNav, null, mNavPos, mNavPos);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            int id = R.mipmap.ic_launcher;
            switch (mNavPos) {
                case 0:
                    id = R.mipmap.ic_launcher;
                    break;
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
            Intent launchIntent = new Intent(this, MainActivity.class);
            launchIntent.setComponent(getIntent().getComponent());
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchIntent.putExtra("duplicate", false);

            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getStringArray(R.array.dropdown)[mNavPos]);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();

        for (int i = fm.getBackStackEntryCount() - 1; i >= 0; i--) {
            fm.popBackStack();
        }
    }


    private void openFrag(Fragment frag) {
        clearBackStack();
        mFragment = frag;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.basecontent, frag)
                .commit();
    }

    public void moveToFrag(Fragment frag) {
        mFragment = frag;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.basecontent, frag)
                .addToBackStack(null)
                .commit();
    }


    public ArrayAdapter<String> buildNavAdapter(final Context c) {
        return new ArrayAdapter<String>(c, R.layout.drawer_list_item, c.getResources().getStringArray(R.array.dropdown)) {
            @NonNull
            @Override
            public View getView(int pos, View v, @NonNull ViewGroup p) {
                v = super.getView(pos, v, p);
                if (pos == mNavPos) {
                    ((TextView) v).setTypeface(null, Typeface.BOLD);
                } else ((TextView) v).setTypeface(null, Typeface.NORMAL);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        && c.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(0, 0, ICONS[pos], 0);
                } else {
                    ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ICONS[pos], 0, 0, 0);
                }


                return v;
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        mNav.setSelection(mNavPos);
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0)
                    onBackPressed();
                else
                    mDrawerLayout.openDrawer(isRTL() ? Gravity.RIGHT : Gravity.LEFT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean isRTL() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        }
        Configuration config = getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtils.get(this).onRequestPermissionResult(permissions, grantResults);
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            mToolbar.setNavigationIcon(MaterialDrawableBuilder.with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.ARROW_LEFT)
                    .setColor(Color.WHITE)
                    .setToActionbarSize()
                    .build());
        } else {
            mToolbar.setNavigationIcon(MaterialDrawableBuilder.with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.MENU)
                    .setColor(Color.WHITE)
                    .setToActionbarSize()
                    .build());
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (pos == mNavPos && mDrawerLayout.isDrawerOpen(mNav)) {
            mDrawerLayout.closeDrawers();
            return;
        }


        switch (pos) {
            case 0:
                if (getIntent().hasExtra("startCity")) {
                    Bundle bdl = new Bundle();
                    bdl.putInt("startCity", getIntent().getIntExtra("startCity", 0));
                    Fragment frag = new VakitFragment();
                    frag.setArguments(bdl);
                    openFrag(frag);
                } else {
                    openFrag(new VakitFragment());
                }
                break;
            case 1:
                openFrag(new CompassFragment());
                break;
            case 2:
                openFrag(new NamesFragment());
                break;
            case 3:
                openFrag(new CalendarFragment());
                break;
            case 4:
                openFrag(new TesbihatFragment());
                break;
            case 5:

                String lang = Utils.getLanguage("en", "de", "tr");
                final String file = lang + "/hadis.db";
                final String url = App.API_URL + "/hadis." + lang + ".db";
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
                        onItemClick(null, null, 5, 0);
                    }
                    openFrag(new HadithFragment());
                } else if (!App.isOnline()) {
                    Toast.makeText(MainActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                    dialog.setTitle(R.string.hadith);
                    dialog.setMessage(getString(R.string.dlHadith));
                    dialog.setCancelable(false);
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    File f1 = new File(App.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                            file);


                                    if (!f1.getParentFile().mkdirs()) {
                                        Log.e("BaseActivity", "could not mkdirs " + f1.getParent());
                                    }
                                    final ProgressDialog dlg = new ProgressDialog(MainActivity.this);
                                    dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    dlg.setCancelable(false);
                                    dlg.setCanceledOnTouchOutside(false);
                                    dlg.show();
                                    Ion.with(MainActivity.this)
                                            .load(url)
                                            .progressDialog(dlg)
                                            .write(f1)
                                            .setCallback(new FutureCallback<File>() {
                                                @Override
                                                public void onCompleted(Exception e, File result) {

                                                    dlg.dismiss();
                                                    if (e != null) {
                                                        e.printStackTrace();
                                                        Crashlytics.logException(e);
                                                        Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                                    } else if (result.exists()) {
                                                        onItemClick(null, null, 5, 0);
                                                    }
                                                }
                                            });
                                }
                            }
                    );
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }
                    );
                    dialog.show();
                    return;
                }

                break;
            case 6:
                openFrag(new KazaFragment());
                break;
            case 7:
                openFrag(new ZikrFragment());
                break;
            case 8:
                openFrag(new SettingsFragment());
                break;
            case 9:
                openFrag(new AboutFragment());
                break;
            default:
                openFrag(new VakitFragment());
        }


        mDrawerLayout.closeDrawers();

        mNavPos = pos;

        mToolbar.setTitle((String) mNav.getAdapter().getItem(mNavPos));
        ((ArrayAdapter) mNav.getAdapter()).notifyDataSetChanged();
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(ACTS[mNavPos]));

        AppRatingDialog.addToOpenedMenus(ACTS[pos]);

    }


    public static abstract class MainFragment extends android.support.v4.app.Fragment {
        public MainFragment() {
            super();
            setHasOptionsMenu(true);
        }

        public boolean onBackPressed() {
            return false;
        }

        public MainActivity getBaseActivity() {
            return (MainActivity) getActivity();
        }

        public void backToMain() {
            FragmentManager fm = getBaseActivity().getSupportFragmentManager();
            int c = fm.getBackStackEntryCount();
            for (int i = 0; i < c; i++) {
                fm.popBackStack();
            }
        }

        public boolean back() {
            FragmentManager fm = getBaseActivity().getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();

                return true;
            }
            return false;
        }

        public void moveToFrag(Fragment frag) {
            getBaseActivity().moveToFrag(frag);
        }
    }
}
