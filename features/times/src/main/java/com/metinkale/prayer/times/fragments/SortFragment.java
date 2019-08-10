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

package com.metinkale.prayer.times.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.fragments.calctime.CalcTimeConfDialogFragment;
import com.metinkale.prayer.times.times.Times;
import com.metinkale.prayer.times.times.sources.CalcTimes;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SortFragment extends Fragment {

    private MyAdapter mAdapter;
    private TimesFragment act;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mDeleteMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bdl) {
        View v = inflater.inflate(R.layout.vakit_sort_main, container, false);
        RecyclerView recyclerMan = v.findViewById(R.id.list);
        mAdapter = new MyAdapter();
        recyclerMan.setAdapter(mAdapter);
        LinearLayoutManager linLayMan = new LinearLayoutManager(getContext());
        recyclerMan.setLayoutManager(linLayMan);
        recyclerMan.setHasFixedSize(true);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback();
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerMan);
        setHasOptionsMenu(true);

        Times.getTimes().observe(this, mAdapter);
        mAdapter.onChanged(Times.getTimes());
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MaterialMenuInflater.with(getActivity(), inflater).inflate(R.menu.sort_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.delete) {
            mDeleteMode = !mDeleteMode;
            item.setIcon(MaterialDrawableBuilder.with(getActivity())
                    .setIcon(mDeleteMode ? MaterialDrawableBuilder.IconValue.DELETE_EMPTY : MaterialDrawableBuilder.IconValue.DELETE)
                    .setToActionbarSize().build());
            mAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDeleteMode = false;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        act = (TimesFragment) getParentFragment();
    }


    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mAdapter.times, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mAdapter.times, i, i - 1);
            }
        }

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            mAdapter.times.get(i).setSortId(i);
        }

        mAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    public void onItemDismiss(final int position) {
        final Times times = Times.getTimesAt(position);

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setTitle(R.string.delete);
        dialog.setMessage(getString(R.string.delCityConfirm, times.getName()));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), (dialogInterface, i) -> {
            times.delete();
            mAdapter.notifyItemRemoved(position);
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), (dialogInterface, i) -> {
            dialogInterface.cancel();
            mAdapter.notifyDataSetChanged();
        });
        dialog.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView city;
        public TextView source;
        public View handler;
        public View delete;
        public View gps;

        ViewHolder(@NonNull View v) {
            super(v);
            city = v.findViewById(R.id.city);
            source = v.findViewById(R.id.source);
            handler = v.findViewById(R.id.handle);
            delete = v.findViewById(R.id.delete);
            gps = v.findViewById(R.id.gps);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> implements Observer<List<Times>> {

        @NonNull
        private ArrayList<Times> times = new ArrayList<>();

        public MyAdapter() {
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.vakit_sort_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder vh, int position) {

            final Times c = getItem(position);
            if (c == null) {
                return;
            }
            vh.city.setText(c.getName());
            vh.source.setText(c.getSource().name);
            vh.gps.setVisibility(c.isAutoLocation() ? View.VISIBLE : View.GONE);
            vh.delete.setVisibility(mDeleteMode ? View.VISIBLE : View.GONE);

            vh.delete.setOnClickListener(view -> onItemDismiss(vh.getAdapterPosition()));
            vh.handler.setOnTouchListener((view, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mItemTouchHelper.startDrag(vh);
                }

                return false;
            });

            vh.itemView.setOnClickListener(view -> act.onItemClick(vh.getAdapterPosition()));
            vh.itemView.setOnLongClickListener(c instanceof CalcTimes ? (View.OnLongClickListener) v -> {
                CalcTimeConfDialogFragment.forCity((CalcTimes) c).show(getChildFragmentManager(), "calcconfig");
                return true;
            } : null);
        }


        @Nullable
        public Times getItem(int pos) {
            return times.get(pos);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getID();
        }

        @Override
        public int getItemCount() {
            return times.size();
        }


        @Override
        public void onChanged(@Nullable List<Times> times) {
            this.times.clear();
            this.times.addAll(Times.getTimes());

            notifyDataSetChanged();
        }
    }

    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {


        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            onItemDismiss(viewHolder.getAdapterPosition());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            // We only want the active item
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof ViewHolder) {
                    ViewHolder itemViewHolder = (ViewHolder) viewHolder;
                    itemViewHolder.itemView.setBackgroundColor(mixColor(getActivity().getResources().getColor(R.color.background), getActivity().getResources().getColor(R.color.backgroundSecondary)));
                }
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof ViewHolder) {
                ViewHolder itemViewHolder = (ViewHolder) viewHolder;
                itemViewHolder.itemView.setBackgroundResource(R.color.background);
                Times.sort();
            }
        }


    }

    private static int mixColor(int color1, int color2) {
        final float inverseRation = 1f - (float) 0.5;
        float r = (Color.red(color1) * (float) 0.5) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * (float) 0.5) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * (float) 0.5) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }
}