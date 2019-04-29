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

package com.metinkale.prayer.dhikr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.dhikr.VibrationModeView.PrefsFunctions;
import com.metinkale.prayer.dhikr.data.Dhikr;
import com.metinkale.prayer.dhikr.data.DhikrViewModel;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

@SuppressWarnings("deprecation")
public class DhikrFragment extends BaseActivity.MainFragment
        implements OnClickListener, OnLongClickListener, AdapterView.OnItemSelectedListener, Observer<List<Dhikr>> {
    
    private DhikrViewModel mViewModel;
    private SharedPreferences mPrefs;
    private DhikrView mDhikrView;
    private EditText mTitle;
    private Vibrator mVibrator;
    private ImageView mReset;
    private int mVibrate;
    private Spinner mSpinner;
    private List<Dhikr> mDhikrs;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dhikr_main, container, false);
        
        mPrefs = getActivity().getSharedPreferences("zikr", 0);
        mDhikrView = v.findViewById(R.id.zikr);
        mTitle = v.findViewById(R.id.title);
        mDhikrView.setOnClickListener(this);
        mDhikrView.setOnLongClickListener(this);
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        mReset = v.findViewById(R.id.reset);
        mReset.setOnClickListener(this);
        mVibrate = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("zikrvibrate2", 0);
        
        ((VibrationModeView) v.findViewById(R.id.vibration)).setPrefFunctions(new PrefsFunctions() {
            
            @Override
            public int getValue() {
                return mVibrate;
            }
            
            @Override
            public void setValue(int obj) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("zikrvibrate2", obj).apply();
                mVibrate = obj;
                
            }
            
        });
        
        OnClickListener colorlist = this::changeColor;
        v.findViewById(R.id.color1).setOnClickListener(colorlist);
        v.findViewById(R.id.color2).setOnClickListener(colorlist);
        v.findViewById(R.id.color3).setOnClickListener(colorlist);
        v.findViewById(R.id.color4).setOnClickListener(colorlist);
        v.findViewById(R.id.color5).setOnClickListener(colorlist);
        v.findViewById(R.id.color6).setOnClickListener(colorlist);
        v.findViewById(R.id.color7).setOnClickListener(colorlist);
        v.findViewById(R.id.color8).setOnClickListener(colorlist);
        
        mViewModel = ViewModelProviders.of(this).get(DhikrViewModel.class);
        mViewModel.getDhikrs().observe(this, this);
        return v;
    }
    
    
    private void migrateToRoom() {
        Map<String, Set<String>> map = (Map<String, Set<String>>) mPrefs.getAll();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            Set set = map.get(key);
            if (set == null) {
                continue;
            }
            try {
                List<String> list = new ArrayList<>(set);
                String title = list.get(0).substring(1);
                int color = Integer.parseInt(list.get(1).substring(1));
                int value = Integer.parseInt(list.get(2).substring(1));
                int reached = Integer.parseInt(list.get(3).substring(1));
                int max = Integer.parseInt(list.get(4).substring(1));
                Collections.sort(list);
                Dhikr dhikr = new Dhikr();
                dhikr.setTitle(title);
                dhikr.setColor(color);
                dhikr.setValue(value + reached * max);
                dhikr.setMax(max);
                mViewModel.addDhikr(dhikr);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        mPrefs.edit().clear().apply();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mDhikrs == null || mDhikrs.isEmpty())
            return;
        mViewModel.saveDhikr(mDhikrs.get(0));
    }
    
    
    public void changeColor(@NonNull View v) {
        int c = Color.parseColor((String) v.getTag());
        mDhikrs.get(0).setColor(c);
        mDhikrView.invalidate();
    }
    
    @Override
    public void onClick(View v) {
        if (v == mDhikrView) {
            Dhikr dhikr = mDhikrs.get(0);
            dhikr.setValue(dhikr.getValue() + 1);
            
            if (dhikr.getValue() % dhikr.getMax() == 0) {
                if (mVibrate != -1) {
                    mVibrator.vibrate(new long[]{0, 100, 100, 100, 100, 100}, -1);
                }
            } else if (mVibrate == 0) {
                mVibrator.vibrate(10);
            }
            mDhikrView.invalidate();
        } else if (v == mReset) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
            dialog.setTitle(R.string.dhikr);
            dialog.setMessage(getString(R.string.resetConfirmDhikr, mDhikrs.get(0).getTitle()));
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), (dialogInterface, i) -> {
                Dhikr dhikr = mDhikrs.get(0);
                dhikr.setValue(0);
                mDhikrView.invalidate();
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (dialogInterface, i) -> {
            });
            dialog.show();
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MaterialMenuInflater.with(getActivity(), inflater).setDefaultColorResource(R.color.background).inflate(R.menu.zikr, menu);
        
        MenuItem item = menu.findItem(R.id.menu_spinner);
        mSpinner = (Spinner) MenuItemCompat.getActionView(item);
        
        
        onChanged(mViewModel.getDhikrs().getValue());
    }
    
    @Override
    public boolean onlyPortrait() {
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        mDhikrs = mViewModel.getDhikrs().getValue();
        int i = item.getItemId();
        if (i == R.id.add) {
            addNewDhikr();
            return true;
        } else if (i == R.id.del) {
            deleteDhikr();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void deleteDhikr() {
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setTitle(R.string.delete);
        dialog.setMessage(getString(R.string.delConfirmDhikr, mDhikrs.get(0).getTitle()));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), (dialogInterface, i) -> mViewModel.deleteDhikr(mDhikrs.get(0)));
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (dialogInterface, i) -> {
        });
        dialog.show();
    }
    
    private void addNewDhikr() {
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dhikr);
        
        final EditText input = new EditText(getActivity());
        input.setText(getString(R.string.newDhikr));
        input.setSelection(input.getText().length());
        builder.setView(input);
        
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            final String title = input.getText().toString();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            builder1.setTitle(R.string.dhikrCount);

            final EditText input1 = new EditText(getActivity());
            input1.setInputType(InputType.TYPE_CLASS_NUMBER);
            input1.setText(String.valueOf(33));
            input1.setSelection(input1.getText().length());
            builder1.setView(input1);

            builder1.setPositiveButton(R.string.ok, (dialogInterface1, i12) -> {
                int count = Integer.parseInt(input1.getText().toString());
                mViewModel.saveDhikr(mDhikrs.get(0));
                Dhikr model = new Dhikr();
                model.setTitle(title);
                model.setMax(count);
                model.setPosition(-1);
                mViewModel.addDhikr(model);
            });

            builder1.setNegativeButton(R.string.cancel, (dialog, i1) -> dialog.cancel());

            builder1.show();
        });
        
        builder.setNegativeButton(R.string.cancel, (dialog, i) -> dialog.cancel());
        
        builder.show();
    }
    
    @Override
    public boolean onLongClick(View arg0) {
        if (mDhikrs.get(0).getId() == 0) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dhikrCount);
        
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(mDhikrs.get(0).getMax()));
        
        builder.setView(input);
        
        // Set up the buttons
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            try {
                mDhikrs.get(0).setMax(Integer.parseInt(input.getText().toString()));
                mDhikrView.invalidate();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        });
        
        builder.setNegativeButton(R.string.cancel, (dialog, i) -> dialog.cancel());
        
        builder.show();
        return false;
    }
    
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        if (pos == 0)
            return;
        mDhikrs = mViewModel.getDhikrs().getValue();
        for (int i = 0; i < mDhikrs.size(); i++) {
            Dhikr model = mDhikrs.get(i);
            if (i == pos) {
                model.setPosition(0);
            } else if (i < pos) {
                model.setPosition(i + 1);
            } else {
                model.setPosition(i);
            }
        }
        
        mViewModel.saveDhikr(mDhikrs.toArray(new Dhikr[0]));
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    
    }
    
    @Override
    public void onChanged(@Nullable List<Dhikr> dhikrs) {
        if (dhikrs == null)
            return;
        this.mDhikrs = dhikrs;
        migrateToRoom();
        
        
        if (mSpinner != null) {
            ArrayList<String> itemList = new ArrayList<>();
            for (Dhikr dhikr : dhikrs) {
                itemList.add(dhikr.getTitle());
            }
            
            if (dhikrs.isEmpty()) {
                Dhikr model = new Dhikr();
                model.setTitle(getString(R.string.tasbih));
                model.setMax(33);
                mViewModel.addDhikr(model);
            } else {
                Dhikr model = dhikrs.get(0);
                mTitle.setText(model.getTitle());
                mTitle.setSelection(mTitle.getText().length());
                mDhikrView.setDhikr(model);
                mDhikrView.invalidate();
            }
            Context c = new ContextThemeWrapper(getActivity(), R.style.ToolbarTheme);
            SpinnerAdapter adap = new ArrayAdapter<>(c, android.R.layout.simple_list_item_1, android.R.id.text1, itemList);
            
            mSpinner.setAdapter(adap);
            mSpinner.setOnItemSelectedListener(this);
        }
    }
    
    
}
