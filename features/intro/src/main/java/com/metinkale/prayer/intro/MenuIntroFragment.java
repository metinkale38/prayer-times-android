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

package com.metinkale.prayer.intro;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.Prefs;
import com.metinkale.prayer.times.fragments.TimesFragment;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import static com.metinkale.prayer.BaseActivity.MENU_ITEMS;

/**
 * Created by metin on 17.07.2017.
 */

public class MenuIntroFragment extends IntroFragment {
    private Runnable mOpen = new Runnable() {
        @Override
        public void run() {
            mDrawerLayout.openDrawer(GravityCompat.START);
            mDrawerLayout.postDelayed(mClose, 3000);
        }
    };
    
    private Runnable mClose = new Runnable() {
        @Override
        public void run() {
            mDrawerLayout.closeDrawers();
            mDrawerLayout.postDelayed(mOpen, 2000);
        }
    };
    private DrawerLayout mDrawerLayout;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_menu, container, false);
        
        
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.appName);
        toolbar.setNavigationIcon(
                MaterialDrawableBuilder.with(getActivity()).setIcon(MaterialDrawableBuilder.IconValue.MENU).setColorResource(R.color.background)
                        .setToActionbarSize().build());
        
        mDrawerLayout = v.findViewById(R.id.drawer);
        mDrawerLayout.setBackgroundResource(R.color.background);
        
        ListView lv = v.findViewById(R.id.base_nav);
        lv.setAdapter(buildNavAdapter(getActivity()));
        
        return v;
    }
    
    public ArrayAdapter<BaseActivity.Item> buildNavAdapter(final Context c) {
        return new ArrayAdapter<BaseActivity.Item>(c, R.layout.drawer_list_item, MENU_ITEMS) {
            @NonNull
            @Override
            public View getView(int pos, View v, @NonNull ViewGroup p) {
                if (v == null) {
                    v = LayoutInflater.from(c).inflate(com.metinkale.prayer.base.R.layout.drawer_list_item, p, false);
                }
                BaseActivity.Item item = getItem(pos);
                ((TextView) v).setText(item.getTitleRes());
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fragment frag = new TimesFragment();
        getChildFragmentManager().beginTransaction().replace(R.id.basecontent, frag).commit();
    }
    
    @Override
    protected void onSelect() {
        if (mDrawerLayout != null)
            mDrawerLayout.postDelayed(mOpen, 500);
    }
    
    @Override
    protected void onEnter() {
    }
    
    @Override
    protected void onExit() {
        if (mDrawerLayout != null) {
            mDrawerLayout.removeCallbacks(mOpen);
            mDrawerLayout.removeCallbacks(mClose);
        }
    }
    
    @Override
    protected boolean shouldShow() {
        return Prefs.showIntro();
    }
}
