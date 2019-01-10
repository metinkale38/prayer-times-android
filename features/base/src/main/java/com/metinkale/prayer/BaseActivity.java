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

package com.metinkale.prayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.metinkale.prayer.base.BuildConfig;
import com.metinkale.prayer.base.R;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.PermissionUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class BaseActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, AdapterView.OnItemClickListener {
    
    
    private final int mTitleRes;
    @Getter
    @Setter
    @Accessors(prefix = "m")
    private Fragment mDefaultFragment;
    private final int mIconRes;
    
    
    public BaseActivity(int titleRes, int iconRes, Fragment defaultFragment) {
        this.mTitleRes = titleRes;
        this.mDefaultFragment = defaultFragment;
        this.mIconRes = iconRes;
    }
    
    private int mNavPos;
    private ListView mNav;
    private DrawerLayout mDrawerLayout;
    //private long mStartTime;
    //private Fragment mFragment;
    private Toolbar mToolbar;


    /*@Override
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
    }*/
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleUtils.initLocale(this);
        //AppRatingDialog.increaseAppStarts();
        
        if (Preferences.SHOW_INTRO.get() || Preferences.CHANGELOG_VERSION.get() < BuildConfig.CHANGELOG_VERSION) {
            Module.INTRO.launch(this);
        }
        
        super.setContentView(R.layout.activity_base);
        
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setBackgroundResource(R.color.colorPrimary);
            mToolbar.setNavigationIcon(
                    MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.MENU).setColorResource(R.color.background)
                            .setToActionbarSize().build());
        }
        
        
        mDrawerLayout = findViewById(R.id.drawer);
        mNav = mDrawerLayout.findViewById(R.id.base_nav);
        ArrayAdapter<Module> list = buildNavAdapter(this);
        mNav.setAdapter(list);
        mNav.setOnItemClickListener(this);
        
        final int title = mTitleRes;
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mToolbar != null) {
                    mToolbar.setTitle(title);
                }
            }
        });
        
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        
        if (savedInstanceState != null)
            mNavPos = savedInstanceState.getInt("navPos", 0);
        
        String comp = getIntent().getComponent().getClassName();
        for (int i = 0; i < Module.values().length; i++) {
            if (comp.contains(Module.values()[i].getKey())) {
                mNavPos = i;
            }
        }
        
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, mIconRes);
            
            Intent intent = new Intent();
            Intent launchIntent = new Intent(this, BaseActivity.class);
            launchIntent.setComponent(getIntent().getComponent());
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchIntent.putExtra("duplicate", false);
            
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(mTitleRes));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            
            setResult(RESULT_OK, intent);
            finish();
        }
        
        moveToFrag(mDefaultFragment);
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("navPos", mNavPos);
    }
    
    
    public void moveToFrag(Fragment frag) {
        //mFragment = frag;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.basecontent, frag);
        if (frag != mDefaultFragment)
            transaction.addToBackStack(null);
        transaction.commit();
        if (frag instanceof MainFragment)
            setRequestedOrientation(
                    ((MainFragment) frag).onlyPortrait() ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
    
    
    public ArrayAdapter<Module> buildNavAdapter(final Context c) {
        return new ArrayAdapter<Module>(c, 0, Module.values()) {
            @NonNull
            @Override
            public View getView(int pos, View v, @NonNull ViewGroup p) {
                if (v == null) {
                    v = LayoutInflater.from(c).inflate(R.layout.drawer_list_item, p, false);
                }
                Module item = getItem(pos);
                if (item.getIconRes() == 0 && item.getTitleRes() == 0) {
                    v.setVisibility(View.GONE);
                    return v;
                }
                v.setVisibility(View.VISIBLE);
                ((TextView) v).setText(item.getTitleRes());
                if (pos == mNavPos) {
                    ((TextView) v).setTypeface(null, Typeface.BOLD);
                } else
                    ((TextView) v).setTypeface(null, Typeface.NORMAL);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                        c.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                    ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(0, 0, item.getIconRes(), 0);
                } else {
                    ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(item.getIconRes(), 0, 0, 0);
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
            mToolbar.setNavigationIcon(
                    MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.ARROW_LEFT).setColorResource(R.color.background)
                            .setToActionbarSize().build());
        } else {
            mToolbar.setNavigationIcon(
                    MaterialDrawableBuilder.with(this).setIcon(MaterialDrawableBuilder.IconValue.MENU).setColorResource(R.color.background)
                            .setToActionbarSize().build());
        }
    }
    
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (pos == mNavPos && mDrawerLayout.isDrawerOpen(mNav)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        Module.values()[pos].launch(this);
        mDrawerLayout.closeDrawers();
        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(Module.values()[mNavPos].getKey()));
        //AppRatingDialog.addToOpenedMenus(ACTS[pos]);
        
    }
    
    
    public static class MainFragment extends Fragment {
        public MainFragment() {
            super();
            setHasOptionsMenu(true);
        }
        
        public boolean onlyPortrait() {
            return false;
        }
        
        
        public boolean onBackPressed() {
            return false;
        }
        
        public BaseActivity getBaseActivity() {
            return (BaseActivity) getActivity();
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
