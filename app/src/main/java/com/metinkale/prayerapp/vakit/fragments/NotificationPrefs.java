package com.metinkale.prayerapp.vakit.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.metinkale.prayer.BuildConfig;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.MainIntentService;
import com.metinkale.prayerapp.vakit.AlarmReceiver;
import com.metinkale.prayerapp.vakit.PrefsView;
import com.metinkale.prayerapp.vakit.PrefsView.Pref;
import com.metinkale.prayerapp.vakit.PrefsView.PrefsFunctions;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;
import com.metinkale.prayerapp.vakit.times.Vakit;

public class NotificationPrefs extends Fragment {
    private Times mTimes;
    private PreferenceManager.OnActivityResultListener mListener;

    public static NotificationPrefs create(Times t) {
        Bundle bdl = new Bundle();
        bdl.putLong("city", t.getID());
        NotificationPrefs frag = new NotificationPrefs();
        frag.setArguments(bdl);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vakit_notprefs, container, false);

        mTimes = MainHelper.getTimes(getArguments().getLong("city", 0));
        Switch ongoing = (Switch) v.findViewById(R.id.ongoing);
        ExpandableListView list = (ExpandableListView) v.findViewById(R.id.expandableListView);
        ExpandableListAdapter adapter = new MyAdapter();
        list.setAdapter(adapter);


        ongoing.setChecked(mTimes.isOngoingNotificationActive());
        ongoing.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean val) {
                mTimes.setOngoingNotificationActive(val);
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        MainIntentService.setAlarms(getActivity());
    }

    public void onResume() {
        super.onResume();
    }


    public class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View v = View.inflate(getActivity(), R.layout.vakit_notprefs_item, null);
            final View container = v.findViewById(R.id.prefscontainer);
            final View contalt = v.findViewById(R.id.contalt);
            Switch s = (Switch) v.findViewById(R.id.active);
            TextView title = (TextView) v.findViewById(R.id.switchText);
            final PrefsView sound = (PrefsView) v.findViewById(R.id.sound);
            final PrefsView vibration = (PrefsView) v.findViewById(R.id.vibration);
            final PrefsView silenter = (PrefsView) v.findViewById(R.id.silenter);
            final PrefsView dua = (PrefsView) v.findViewById(R.id.dua);
            PrefsView time = (PrefsView) v.findViewById(R.id.time);


            title.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (!BuildConfig.DEBUG) return false;
                    Times.Alarm a = new Times.Alarm();
                    a.time = System.currentTimeMillis() + (5 * 1000);
                    a.sound = (String) sound.getValue();
                    a.dua = (String) dua.getValue();
                    a.silenter = (Integer) silenter.getValue();
                    a.vibrate = (boolean) vibration.getValue();
                    a.city = mTimes.getID();
                    a.pref = "TEST";
                    AlarmReceiver.setAlarm(getActivity(), a);
                    Toast.makeText(App.getContext(), "Will play within 5 seconds", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            if (childPosition == 0) {
                title.setText(R.string.ezanNotification);
                boolean isChecked = mTimes.isNotificationActive(Vakit.values()[groupPosition]);
                s.setChecked(isChecked);

                container.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

                contalt.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);

                s.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mTimes.setNotificationActive(Vakit.values()[groupPosition], isChecked);

                        container.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

                        contalt.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);

                    }
                });

                sound.setVakit((Vakit) getGroup(groupPosition));
                sound.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getSound(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setSound(Vakit.values()[groupPosition], (String) obj);
                    }
                });

                vibration.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.hasVibration(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setVibration(Vakit.values()[groupPosition], (Boolean) obj);
                    }
                });

                silenter.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getSilenterDuration(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setSilenterDuration(Vakit.values()[groupPosition], (Integer) obj);
                    }
                });

                dua.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getDua(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setDua(Vakit.values()[groupPosition], (String) obj);
                    }
                });

                if (groupPosition == 1) {
                    time.setTag("SabahTime");
                    time.setPrefFunctions(new PrefsFunctions() {

                        @Override
                        public Object getValue() {
                            return mTimes.getSabahTime() * (mTimes.isAfterImsak() ? -1 : 1);
                        }

                        @Override
                        public void setValue(Object obj) {
                            mTimes.setSabahTime(Math.abs((Integer) obj));
                            mTimes.setAfterImsak((Integer) obj < 0);
                        }
                    });
                } else {
                    time.setVisibility(View.GONE);
                }
            } else if (childPosition == 2) {
                title.setText(R.string.ecuma);
                boolean isChecked = mTimes.isCumaActive();
                s.setChecked(isChecked);

                container.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

                contalt.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);

                s.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mTimes.setCumaActive(isChecked);

                        container.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

                        contalt.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);

                    }
                });

                sound.setPrefType(Pref.Sela);
                sound.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getCumaSound();
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setCumaSound((String) obj);
                    }
                });

                vibration.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.hasCumaVibration();
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setCumaVibration((Boolean) obj);
                    }
                });

                silenter.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getCumaSilenterDuration();
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setCumaSilenterDuration((Integer) obj);
                    }
                });

                dua.setVisibility(View.GONE);

                time.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getCumaTime();
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setCumaTime((Integer) obj);
                    }
                });

            } else {
                title.setText(R.string.earlynotification);

                boolean isChecked = mTimes.isEarlyNotificationActive(Vakit.values()[groupPosition]);
                s.setChecked(isChecked);

                container.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

                contalt.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);

                s.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mTimes.setEarlyNotificationActive(Vakit.values()[groupPosition], isChecked);

                        container.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

                        contalt.setVisibility(isChecked ? View.INVISIBLE : View.VISIBLE);

                    }
                });

                sound.setVakit((Vakit) getGroup(groupPosition));
                sound.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getEarlySound(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setEarlySound(Vakit.values()[groupPosition], (String) obj);
                    }
                });

                vibration.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.hasEarlyVibration(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setEarlyVibration(Vakit.values()[groupPosition], (Boolean) obj);
                    }
                });

                silenter.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getEarlySilenterDuration(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setEarlySilenterDuration(Vakit.values()[groupPosition], (Integer) obj);
                    }
                });

                dua.setVisibility(View.GONE);

                time.setPrefFunctions(new PrefsFunctions() {

                    @Override
                    public Object getValue() {
                        return mTimes.getEarlyTime(Vakit.values()[groupPosition]);
                    }

                    @Override
                    public void setValue(Object obj) {
                        mTimes.setEarlyTime(Vakit.values()[groupPosition], (Integer) obj);
                    }
                });
            }
            return v;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case 1:
                    return 1;
                case 3:
                    return 3;
                default:
                    return 2;
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            return Vakit.values()[groupPosition];
        }

        @Override
        public int getGroupCount() {
            return Vakit.values().length;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return Vakit.values()[groupPosition].ordinal();
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null)

            {
                convertView = View.inflate(getActivity(), R.layout.vakit_notprefs_group, null);
            }

            Vakit v = (Vakit) getGroup(groupPosition);
            ((TextView) ((ViewGroup) convertView).getChildAt(0)).setText(v.getString());
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

}
