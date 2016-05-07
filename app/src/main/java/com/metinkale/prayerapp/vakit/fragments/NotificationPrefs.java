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

package com.metinkale.prayerapp.vakit.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.metinkale.prayer.BuildConfig;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.vakit.AlarmReceiver;
import com.metinkale.prayerapp.vakit.PrefsView;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;

import static com.metinkale.prayer.R.id.vibration;


public class NotificationPrefs extends Fragment {
    private Times mTimes;
    private PreferenceManager.OnActivityResultListener mListener;
    private View mView;

    public static NotificationPrefs create(Times t) {
        Bundle bdl = new Bundle();
        bdl.putLong("city", t.getID());
        NotificationPrefs frag = new NotificationPrefs();
        frag.setArguments(bdl);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.vakit_notprefs, container, false);


        mTimes = Times.getTimes(getArguments().getLong("city", 0));
        SwitchCompat ongoing = (SwitchCompat) mView.findViewById(R.id.ongoing);
        ongoing.setChecked(mTimes.isOngoingNotificationActive());
        ongoing.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean val) {
                mTimes.setOngoingNotificationActive(val);
            }
        });


        initMain(R.id.imsak, R.id.imsakText, R.id.imsakExpanded, Vakit.IMSAK);
        initMain(R.id.gunes, R.id.gunesText, R.id.gunesExpanded, Vakit.GUNES);
        initMain(R.id.sabah, R.id.sabahText, R.id.sabahExpanded, Vakit.SABAH);
        initMain(R.id.ogle, R.id.ogleText, R.id.ogleExpanded, Vakit.OGLE);
        initMain(R.id.ikindi, R.id.ikindiText, R.id.ikindiExpanded, Vakit.IKINDI);
        initMain(R.id.aksam, R.id.aksamText, R.id.aksamExpanded, Vakit.AKSAM);
        initMain(R.id.yatsi, R.id.yatsiText, R.id.yatsiExpanded, Vakit.YATSI);

        initEarly(R.id.eimsak, R.id.eimsakText, R.id.eimsakExpanded, Vakit.IMSAK);
        initEarly(R.id.egunes, R.id.egunesText, R.id.egunesExpanded, Vakit.GUNES);
        initEarly(R.id.eogle, R.id.eogleText, R.id.eogleExpanded, Vakit.OGLE);
        initEarly(R.id.eikindi, R.id.eikindiText, R.id.eikindiExpanded, Vakit.IKINDI);
        initEarly(R.id.eaksam, R.id.eaksamText, R.id.eaksamExpanded, Vakit.AKSAM);
        initEarly(R.id.eyatsi, R.id.eyatsiText, R.id.eyatsiExpanded, Vakit.YATSI);

        initCuma(R.id.ecuma, R.id.ecuma, R.id.ecumaExpand, Vakit.OGLE);

        return mView;
    }

    private void initCuma(int switchId, int textId, int expandId, final Vakit vakit) {
        final SwitchCompat sw = (SwitchCompat) mView.findViewById(switchId);
        final RelativeLayout expand = (RelativeLayout) mView.findViewById(expandId);

        sw.setChecked(mTimes.isEarlyNotificationActive(vakit));
        sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mTimes.setCumaActive(b);
                if (!b && (expand.getVisibility() == View.VISIBLE)) expand.setVisibility(View.GONE);

            }
        });

        ((View) mView.findViewById(textId).getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw.isChecked())
                    expand.setVisibility((expand.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                else {
                    Toast.makeText(getActivity(), R.string.activateForMorePrefs, Toast.LENGTH_LONG).show();
                }
            }
        });

        PrefsView sound = (PrefsView) expand.findViewById(R.id.sound);
        sound.setPrefType(PrefsView.Pref.Sela);
        sound.setVakit(vakit);
        sound.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getCumaSound();
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setCumaSound((String) obj);
            }
        });

        PrefsView vibr = (PrefsView) expand.findViewById(vibration);
        vibr.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.hasCumaVibration();
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setCumaVibration((boolean) obj);
            }
        });

        PrefsView silenter = (PrefsView) expand.findViewById(R.id.silenter);
        silenter.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getCumaSilenterDuration();
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setCumaSilenterDuration((int) obj);
            }
        });

        PrefsView dua = (PrefsView) expand.findViewById(R.id.dua);
        dua.setVisibility(View.GONE);

        PrefsView time = (PrefsView) expand.findViewById(R.id.time);
        time.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getCumaTime();
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setCumaTime((int) obj);
            }
        });
    }

    private void initMain(int switchId, int textId, final int expandId, final Vakit vakit) {
        final SwitchCompat sw = (SwitchCompat) mView.findViewById(switchId);
        final RelativeLayout expand = (RelativeLayout) mView.findViewById(expandId);

        sw.setChecked(mTimes.isNotificationActive(vakit));
        sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mTimes.setNotificationActive(vakit, b);
                if (!b && (expand.getVisibility() == View.VISIBLE)) expand.setVisibility(View.GONE);

            }
        });

        View title = (View) mView.findViewById(textId).getParent();
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw.isChecked()) {
                    expand.setVisibility((expand.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), R.string.activateForMorePrefs, Toast.LENGTH_LONG).show();
                }
            }
        });

        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!BuildConfig.DEBUG) return false;
                Times.Alarm a = new Times.Alarm();
                a.time = System.currentTimeMillis() + 5 * 1000;
                a.city = mTimes.getID();
                a.vakit = vakit;
                AlarmReceiver.setAlarm(getActivity(), a);
                Toast.makeText(App.getContext(), "Will play within 5 seconds", Toast.LENGTH_LONG).show();
                return true;
            }
        });


        PrefsView sound = (PrefsView) expand.findViewById(R.id.sound);
        sound.setVakit(vakit);
        sound.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getSound(vakit);
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setSound(vakit, (String) obj);
            }
        });

        PrefsView vibr = (PrefsView) expand.findViewById(vibration);
        vibr.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                boolean ret = mTimes.hasVibration(vakit);
                return ret;
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setVibration(vakit, (boolean) obj);
            }
        });

        PrefsView silenter = (PrefsView) expand.findViewById(R.id.silenter);
        silenter.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getSilenterDuration(vakit);
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setSilenterDuration(vakit, (int) obj);
            }
        });

        PrefsView dua = (PrefsView) expand.findViewById(R.id.dua);
        dua.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getDua(vakit);
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setDua(vakit, (String) obj);
            }
        });

        PrefsView time = (PrefsView) expand.findViewById(R.id.time);
        if (vakit != Vakit.SABAH)
            time.setVisibility(View.GONE);
        else {
            time.setTag("SabahTime");
            time.setPrefFunctions(new PrefsView.PrefsFunctions() {
                @Override
                public Object getValue() {
                    return mTimes.getSabahTime() * (mTimes.isAfterImsak() ? -1 : 1);
                }

                @Override
                public void setValue(Object obj) {
                    int time = (int) obj;
                    mTimes.setSabahTime(Math.abs(time));
                    mTimes.setAfterImsak(time < 0);
                }
            });
        }
    }

    private void initEarly(int switchId, int textId, int expandId, final Vakit vakit) {
        final SwitchCompat sw = (SwitchCompat) mView.findViewById(switchId);
        final RelativeLayout expand = (RelativeLayout) mView.findViewById(expandId);

        sw.setChecked(mTimes.isEarlyNotificationActive(vakit));

        sw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mTimes.setEarlyNotificationActive(vakit, b);
                if (!b && (expand.getVisibility() == View.VISIBLE)) expand.setVisibility(View.GONE);

            }
        });

        ((View) mView.findViewById(textId).getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw.isChecked())
                    expand.setVisibility((expand.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
                else {
                    Toast.makeText(getActivity(), R.string.activateForMorePrefs, Toast.LENGTH_LONG).show();
                }
            }
        });

        PrefsView sound = (PrefsView) expand.findViewById(R.id.sound);
        sound.setVakit(vakit);
        sound.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getEarlySound(vakit);
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setEarlySound(vakit, (String) obj);
            }
        });

        PrefsView vibr = (PrefsView) expand.findViewById(vibration);
        vibr.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.hasEarlyVibration(vakit);
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setEarlyVibration(vakit, (boolean) obj);
            }
        });

        PrefsView silenter = (PrefsView) expand.findViewById(R.id.silenter);
        silenter.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getEarlySilenterDuration(vakit);
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setEarlySilenterDuration(vakit, (int) obj);
            }
        });

        PrefsView dua = (PrefsView) expand.findViewById(R.id.dua);
        dua.setVisibility(View.GONE);

        PrefsView time = (PrefsView) expand.findViewById(R.id.time);
        time.setPrefFunctions(new PrefsView.PrefsFunctions() {
            @Override
            public Object getValue() {
                return mTimes.getEarlyTime(vakit);
            }

            @Override
            public void setValue(Object obj) {
                mTimes.setEarlyTime(vakit, (int) obj);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        MainIntentService.setAlarms(getActivity());
        getActivity().setTitle(getString(R.string.vakit));
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(mTimes.getName());
    }


}
