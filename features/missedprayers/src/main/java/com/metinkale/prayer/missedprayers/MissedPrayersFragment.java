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

package com.metinkale.prayer.missedprayers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.metinkale.prayer.BaseActivity;

public class MissedPrayersFragment extends BaseActivity.MainFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.kaza_main, container, false);
        ViewGroup vg = view.findViewById(R.id.main);

        int[] ids = {R.string.morningPrayer, R.string.zuhr, R.string.asr, R.string.maghrib, R.string.ishaa, R.string.witr, R.string.fasting};
        for (int i = 0; i < 7; i++) {
            View v = vg.getChildAt(i);
            TextView name = v.findViewById(R.id.text);
            final EditText nr = v.findViewById(R.id.nr);
            ImageView plus = v.findViewById(R.id.plus);
            ImageView minus = v.findViewById(R.id.minus);

            name.setText(ids[i]);

            plus.setOnClickListener(view12 -> {
                String txt = nr.getText().toString();
                if (txt.isEmpty()) txt = "0";
                int i12 = 0;
                try {
                    i12 = Integer.parseInt(txt);
                } catch (Exception ignore) {
                } finally {
                    i12++;
                }

                nr.setText(i12 + "");

            });

            minus.setOnClickListener(view1 -> {
                String txt = nr.getText().toString();
                if (txt.isEmpty()) txt = "0";
                int i1 = 0;
                try {
                    i1 = Integer.parseInt(txt);
                } finally {
                    i1--;
                }

                nr.setText(i1 + "");
            });

        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewGroup vg = getView().findViewById(R.id.main);
        SharedPreferences prefs = getActivity().getSharedPreferences("kaza", 0);
        for (int i = 0; i < 7; i++) {
            View v = vg.getChildAt(i);
            EditText nr = v.findViewById(R.id.nr);
            nr.setText(prefs.getString("count" + i, "0"));

        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ViewGroup vg = getActivity().findViewById(R.id.main);
        SharedPreferences.Editor edit = getActivity().getSharedPreferences("kaza", 0).edit();
        for (int i = 0; i < 7; i++) {
            View v = vg.getChildAt(i);
            EditText nr = v.findViewById(R.id.nr);
            edit.putString("count" + i, nr.getText().toString());

        }

        edit.apply();
    }


}
