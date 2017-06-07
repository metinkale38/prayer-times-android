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

package com.metinkale.prayerapp.hadis;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.NumberDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends BaseActivity implements OnClickListener, OnQueryTextListener {

    private static final int STATE_SHUFFLED = 0;
    private static final int STATE_ORDER = 1;
    private static final int STATE_FAVORITE = 2;

    private int mState;
    private ViewPager mPager;
    private MyAdapter mAdapter;
    private TextView mNumber;
    private ImageView mLeft;
    private ImageView mRight;
    private SharedPreferences mPrefs;
    private MenuItem mSwitch;
    private MenuItem mFav;
    @NonNull
    private Set<Integer> mFavs = new HashSet<>();
    private List<Integer> mList = new ArrayList<>();
    private int mRemFav = -1;
    private ShareActionProvider mShareActionProvider;
    private SearchTask mTask;
    @Nullable
    private String mQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hadis_main);

        mPrefs = getSharedPreferences("hadis", 0);
        mNumber = (TextView) findViewById(R.id.number);
        mLeft = (ImageView) findViewById(R.id.left);
        mRight = (ImageView) findViewById(R.id.right);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mLeft.setOnClickListener(this);
        mRight.setOnClickListener(this);
        mNumber.setOnClickListener(this);

        loadFavs();
        try {
            setState(STATE_SHUFFLED);
        } catch (RuntimeException e) {
            Crashlytics.logException(e);
            finish();
            String lang = Prefs.getLanguage("en","de","tr");
            new File(App.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), lang + "/hadis.db").delete();
            startActivity(new Intent(this, com.metinkale.prayerapp.vakit.Main.class));
        }
    }

    private boolean setState(int state) {
        mList.clear();

        mQuery = null;

        switch (state) {
            case STATE_ORDER:
                for (int i = 1; i <= Shuffled.getList().size(); i++) {
                    mList.add(i);
                }
                break;
            case STATE_SHUFFLED:
                mList.addAll(Shuffled.getList());
                break;
            case STATE_FAVORITE:
                mList.addAll(mFavs);
                break;
            default:
                mList.addAll(SqliteHelper.get().get(SqliteHelper.get().getCategories().get(state - 3)));
                break;
        }
        if (mList.isEmpty()) {
            setState(mState);
            return false;

        }

        mState = state;
        runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.notifyDataSetChanged();
                mPager.setCurrentItem(9999);
                mPager.setAdapter(mAdapter);
                mPager.setCurrentItem(mPrefs.getInt(last(), 0));
            }
        });

        return true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mPager.setCurrentItem(mPrefs.getInt(last(), 0));
        loadFavs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPrefs.edit().putInt(last(), mPager.getCurrentItem()).apply();
        storeFavs();
    }

    @NonNull
    private String last() {
        if ((mState == STATE_FAVORITE) || (mState == STATE_SHUFFLED) || (mState == STATE_ORDER)) {
            return "last_nr" + (mState == STATE_FAVORITE) + (mState == STATE_SHUFFLED);
        } else {
            return "last_nr" + mState;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mLeft) {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        } else if (v == mRight) {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        } else if (v == mNumber) {
            NumberDialog nd = NumberDialog.create(1, mAdapter.getCount() + 1, mPager.getCurrentItem() + 1);
            nd.setOnNumberChangeListener(new NumberDialog.OnNumberChangeListener() {
                @Override
                public void onNumberChange(int nr) {
                    mPager.setCurrentItem(nr - 1, false);
                }
            });
            nd.show(getSupportFragmentManager(), null);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == mFav.getItemId()) {
            int i = (int) mAdapter.getItemId(mPager.getCurrentItem());
            if (mState == STATE_FAVORITE) {
                if (mRemFav == -1) {
                    mFav.setIcon(MaterialDrawableBuilder.with(this)
                            .setIcon(MaterialDrawableBuilder.IconValue.STAR_OUTLINE)
                            .setColor(Color.WHITE)
                            .setToActionbarSize()
                            .build());
                    mRemFav = i;
                    mNumber.setText(mPager.getCurrentItem() + 1 + "/" + (mAdapter.getCount() - 1));
                } else {
                    mFav.setIcon(MaterialDrawableBuilder.with(this)
                            .setIcon(MaterialDrawableBuilder.IconValue.STAR)
                            .setColor(Color.WHITE)
                            .setToActionbarSize()
                            .build());
                    mRemFav = -1;
                    mNumber.setText(mPager.getCurrentItem() + 1 + "/" + mAdapter.getCount());

                }
            } else {
                if (mFavs.contains(i)) {
                    mFavs.remove(Integer.valueOf(i));
                } else {
                    mFavs.add(i);
                }
                mAdapter.notifyDataSetChanged();
                setCurrentPage(mPager.getCurrentItem());
            }

        } else if (item.getItemId() == mSwitch.getItemId()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            List<String> cats = SqliteHelper.get().getCategories();
            List<String> items = new ArrayList<>();
            items.add(getString(R.string.mixed));
            items.add(getString(R.string.sorted));
            items.add(getString(R.string.favorite));
            for (String cat : cats) {
                items.add(Html.fromHtml(cat).toString());
            }
            builder.setTitle(items.get(mState)).setItems(items.toArray(new CharSequence[items.size()]),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!setState(which)) {
                                Toast.makeText(Main.this, R.string.noFavs, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            builder.show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        MaterialMenuInflater.with(this)
                .setDefaultColor(0xFFFFFFFF)
                .inflate(R.menu.hadis, menu);

        mSwitch = menu.findItem(R.id.favswitch);
        mFav = menu.findItem(R.id.fav);
        setCurrentPage(mPager.getCurrentItem());

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        item = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onQueryTextSubmit("");
    }

    void setCurrentPage(int i) {
        if (i >= mAdapter.getCount()) {
            i = mAdapter.getCount() - 1;
        }


        mNumber.setText(i + 1 + "/" + mAdapter.getCount());
        if (mFav == null) {
            return;
        }
        if (mAdapter.getCount() == 0) {
            return;
        }

        if (mFavs.contains((int) mAdapter.getItemId(mPager.getCurrentItem()))) {
            mFav.setIcon(MaterialDrawableBuilder.with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.STAR)
                    .setColor(Color.WHITE)
                    .setToActionbarSize()
                    .build());
        } else {
            mFav.setIcon(MaterialDrawableBuilder.with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.STAR_OUTLINE)
                    .setColor(Color.WHITE)
                    .setToActionbarSize()
                    .build());
        }

        if ((mRemFav != -1) && mFavs.contains(mRemFav)) {
            mFavs.remove(mRemFav);

            mAdapter.notifyDataSetChanged();
            setCurrentPage(mPager.getCurrentItem());
            mRemFav = -1;
        }
    }

    void storeFavs() {
        SharedPreferences.Editor edit = getSharedPreferences("hadis", Context.MODE_PRIVATE).edit();
        edit.clear();
        edit.putString("favs", new Gson().toJson(mFavs));
        edit.apply();
    }

    void loadFavs() {
        SharedPreferences prefs = getSharedPreferences("hadis", Context.MODE_PRIVATE);
        int count = prefs.getInt("Count", 0);

        if (count != 0) {
            for (int i = 0; i < count; i++) {
                mFavs.add(prefs.getInt("fav_" + i, i) + 1);
            }
            storeFavs();
            prefs.edit().clear().apply();
        }

        mFavs.addAll((HashSet<Integer>) new Gson().fromJson(prefs.getString("favs", "[]"), new TypeToken<HashSet<Integer>>() {
        }.getType()));
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        if ((mTask != null) && (mTask.getStatus() == Status.RUNNING)) {
            return false;
        }

        mQuery = query;


        mTask = new SearchTask(this);
        mTask.execute(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public long getItemId(int pos) {
            return mList.get(pos);
        }

        @NonNull
        @Override
        public Fragment getItem(int pos) {
            return Frag.create((int) getItemId(pos));
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            setCurrentPage(position);

            if (object instanceof Frag) {
                ((Frag) object).setQuery(mQuery);
                String hadis = ((Fragment) object).getArguments().getString("hadis", "");
                String kaynak = ((Fragment) object).getArguments().getString("kaynak", "");
                setShareText(hadis + (kaynak.length() <= 3 ? "" : "\n\n" + kaynak));
            }
        }

        private void setShareText(String txt) {
            txt = txt.replace("\n", "|");
            if (mShareActionProvider != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(txt).toString().replace("|", "\n"));
                shareIntent.setType("text/plain");
                mShareActionProvider.setShareIntent(shareIntent);
            }
        }
    }

    private class SearchTask extends AsyncTask<String, String, Boolean> {
        private ProgressDialog dialog;

        SearchTask(Context c) {
            dialog = new ProgressDialog(c);
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View v = getCurrentFocus();

            if (v != null) {
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            v.clearFocus();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (!isCancelled()) {

                mAdapter.notifyDataSetChanged();
                mPager.setCurrentItem(9999);
                mPager.setAdapter(mAdapter);
                mPager.setCurrentItem(0);
            }

            int s = mList.size();
            if (!mQuery.equals(""))
                Toast.makeText(Main.this, getString(R.string.foundXHadis,
                        (s == SqliteHelper.get().getCount()) ? 0 + "" : s + ""), Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onProgressUpdate(String... arg) {
            dialog.setMessage(arg[0]);
        }

        @NonNull
        @Override
        protected Boolean doInBackground(String... args) {
            if ("".equals(args[0])) {
                return false;
            }
            List<Integer> q;
            q = SqliteHelper.get().search(args[0]);
            if (!q.isEmpty()) {
                mList = q;
            }

            return !q.isEmpty();
        }

    }

}
