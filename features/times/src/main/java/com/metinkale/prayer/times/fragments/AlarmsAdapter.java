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

package com.metinkale.prayer.times.fragments;

import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.metinkale.prayer.times.R;

import java.util.ArrayList;
import java.util.List;

class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.MyViewHolder> {
    private static final int TYPE_SWITCH = 0;
    private static final int TYPE_TITLE = 1;

    private List<Object> mItems = new ArrayList<>();

    public void add(Object o) {
        mItems.add(o);
        notifyDataSetChanged();
    }

    public Object getItem(int i) {
        return mItems.get(i);
    }

    public AlarmsAdapter() {
    }

    @Override
    public int getItemViewType(int position) {
        Object o = mItems.get(position);
        if (o instanceof SwitchItem) {
            return TYPE_SWITCH;
        }
        if (o instanceof String) {
            return TYPE_TITLE;
        }

        return -1;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SWITCH) {
            return new SwitchVH(parent);
        }
        if (viewType == TYPE_TITLE) {
            return new TitleVH(parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_SWITCH:
                ((SwitchVH) holder).setSwitchItem((SwitchItem) mItems.get(position));
                break;
            case TYPE_TITLE:
                ((TitleVH) holder).setTitle((String) mItems.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void clearAllItems() {
        mItems.clear();
        notifyDataSetChanged();
    }


    public static abstract class SwitchItem {
        public abstract String getName();

        public abstract void onChange(boolean checked);

        public abstract boolean getChecked();

        public void onClick() {
            onChange(!getChecked());
        }

        public boolean onLongClick() {
            return false;
        }
    }

    static class SwitchVH extends MyViewHolder {
        private final TextView mTitle;
        private final SwitchCompat mSwitch;

        SwitchVH(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.vakit_notprefs_switch, parent, false));
            mSwitch = getView().findViewById(R.id.switchView);
            mTitle = getView().findViewById(R.id.titleView);
        }

        void setSwitchItem(final SwitchItem item) {
            mTitle.setText(item.getName());
            mSwitch.setChecked(item.getChecked());
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.onChange(isChecked);
                }
            });


            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.onClick();
                }
            });

            mTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return item.onLongClick();
                }
            });


        }


    }

    static class TitleVH extends MyViewHolder {
        TextView mText;

        TitleVH(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.vakit_alarm_config_dialog, parent, false));
            mText = (TextView) getView();
        }

        void setTitle(String title) {
            mText.setText(title);
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final View mView;

        MyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        View getView() {
            return mView;
        }
    }
}
