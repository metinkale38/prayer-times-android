package com.metinkale.prayerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.custom.Changelog;
import com.metinkale.prayerapp.hadis.SqliteHelper;
import com.metinkale.prayerapp.settings.Prefs;

import java.io.File;

public abstract class BaseActivity extends AppCompatActivity {

    public static BaseActivity CurrectAct;
    private int mNavPos;
    private ListView mNav;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mNavBar;

    public BaseActivity() {
        String clz = getClass().toString();
        String[] pos = {"vakit", "compass", "names", "calendar", "tesbihat", "hadis", "kaza", "zikr", "settings"};
        for (int i = 0; i < pos.length; i++) {
            if (clz.contains("prayerapp." + pos[i])) {
                mNavPos = i;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.getContext() == null) App.setContext(this);


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
        super.setContentView(R.layout.drawer);

        View sb = findViewById(R.id.statusbar);
        sb.getLayoutParams().height = getTopMargin();
        if (getTopMargin() != 0) {
            sb.setBackgroundResource(R.color.colorPrimaryDark);
        }

        mNavBar = findViewById(R.id.navbar);
        setNavBarColor(0xff000000);
        if (setNavBar() && getBottomMargin() != 0) {
            mNavBar.setVisibility(View.VISIBLE);
            mNavBar.getLayoutParams().height = getBottomMargin();
        } else {
            mNavBar.setVisibility(View.GONE);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        //toolbar.setNavigationIcon(R.drawable.ic_abicon);
        toolbar.setBackgroundResource(R.color.colorPrimary);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        View v = LayoutInflater.from(this).inflate(res, mDrawerLayout, false);
        mDrawerLayout.addView(v, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mNav = (ListView) mDrawerLayout.getChildAt(1);
        ArrayAdapter<String> list = new ArrayAdapter<String>(this, R.layout.drawer_list_item, getResources().getStringArray(R.array.dropdown)) {
            @Override
            public View getView(int pos, View v, ViewGroup p) {
                v = super.getView(pos, v, p);
                if (pos == mNavPos && v instanceof TextView) {
                    ((TextView) v).setTypeface(null, Typeface.BOLD);
                }
                return v;
            }
        };
        mNav.setAdapter(list);
        mNav.setOnItemClickListener(new MyClickListener());

        String title = list.getItem(mNavPos);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.vakit, R.string.vakit) {

            @Override
            public void onDrawerClosed(View view) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (getIntent().getBooleanExtra("anim", false)) {

            mDrawerLayout.openDrawer(Gravity.LEFT);
            mDrawerLayout.post(new Runnable() {

                @Override
                public void run() {
                    mDrawerLayout.closeDrawers();

                }
            });

        }
    }

    protected void setNavBarResource(int res) {
        mNavBar.setBackgroundResource(res);
    }

    protected void setNavBarColor(int color) {
        mNavBar.setBackgroundColor(color);
    }

    protected abstract boolean setNavBar();

    @Override
    protected void onResume() {
        super.onResume();
        mNav.setSelection(mNavPos);
        CurrectAct = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        CurrectAct = null;
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
                    String file = Prefs.getLanguage() + "/hadis.db";
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
                        dialog.setTitle(R.string.hadis);
                        dialog.setMessage(getString(R.string.dlHadith));
                        dialog.setCancelable(false);
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int buttonId) {
                                MainIntentService.downloadHadis(App.getContext(), new Runnable() {
                                    @Override
                                    public void run() {
                                        onItemClick(null, null, 5, 0);
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

    int getTopMargin() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            return getStatusBarHeight();
        }
        return 0;
    }

    public int getBottomMargin() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            return getNavBarHeight();
        }
        return 0;
    }

    int getNavBarHeight() {
        return 0;
    }

    public int getSupportActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    int getStatusBarHeight() {

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtils.get(this).onRequestPermissionResult(requestCode, permissions, grantResults);
    }
}
