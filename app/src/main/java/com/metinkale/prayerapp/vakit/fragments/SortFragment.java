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

package com.metinkale.prayerapp.vakit.fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.times.Times;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortFragment extends Fragment {

    private MyAdapter mAdapter;
    private VakitFragment act;
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

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback();
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
        switch (item.getItemId()) {
            case R.id.delete:
                mDeleteMode = !mDeleteMode;
                item.setIcon(MaterialDrawableBuilder.with(getActivity())
                        .setIcon(mDeleteMode ? MaterialDrawableBuilder.IconValue.DELETE_EMPTY : MaterialDrawableBuilder.IconValue.DELETE)
                        .setColor(Color.WHITE).setToActionbarSize().build());
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

        act = (VakitFragment) getParentFragment();
    }


    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mAdapter.ids, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mAdapter.ids, i, i - 1);
            }
        }
        mAdapter.notifyItemMoved(fromPosition, toPosition);

        Times.drop(fromPosition, toPosition);
    }

    public void onItemDismiss(final int position) {
        final Times times = Times.getTimesAt(position);

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setTitle(R.string.delete);
        dialog.setMessage(getString(R.string.delConfirm, times.getName()));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                times.delete();
                mAdapter.notifyItemRemoved(position);
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                mAdapter.notifyDataSetChanged();
            }
        });
        dialog.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView city;
        public TextView source;
        public View handler;
        public View delete;

        public ViewHolder(@NonNull View v) {
            super(v);
            city = v.findViewById(R.id.city);
            source = v.findViewById(R.id.source);
            handler = v.findViewById(R.id.handle);
            delete = v.findViewById(R.id.delete);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> implements Observer<List<Times>> {

        @NonNull
        private List<Long> ids = new ArrayList<>();

        private int hide = -1;

        public MyAdapter() {
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.vakit_sort_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder vh, final int position) {

            Times c = getItem(position);
            if (c == null) {
                return;
            }
            vh.city.setText(c.getName());
            vh.source.setText(c.getSource().name);

            if (c.isAutoLocation()) {
                Drawable icon = MaterialDrawableBuilder.with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.CROSSHAIRS_GPS)
                        .setSizeDp(20)
                        .setColor(0xAA666666)
                        .build();
                if (Utils.isRTL(getActivity())) {
                    vh.city.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                } else {
                    vh.city.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                }
                vh.city.setCompoundDrawablePadding((int) Utils.convertPixelsToDp(5, getActivity()));
            } else {
                vh.city.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            vh.delete.setVisibility(mDeleteMode ? View.VISIBLE : View.GONE);

            vh.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemDismiss(position);
                }
            });
            vh.handler.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        mItemTouchHelper.startDrag(vh);
                    }

                    return false;
                }
            });

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    act.onItemClick(vh.getAdapterPosition());
                }
            });
        }


        @Nullable
        public Times getItem(int pos) {
            return Times.getTimes(ids.get(pos));
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getID();
        }

        @Override
        public int getItemCount() {
            return ids.size();
        }


        @Override
        public void onChanged(@Nullable List<Times> times) {
            ids.clear();
            ids.addAll(Times.getIds());
            notifyDataSetChanged();
        }
    }

    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {


        @Override
        public int getMovementFlags(RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder
                target) {
            onItemMove(viewHolder.getAdapterPosition(),
                    target.getAdapterPosition());
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
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                      int actionState) {
            // We only want the active item
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof ViewHolder) {
                    ViewHolder itemViewHolder =
                            (ViewHolder) viewHolder;
                    itemViewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof ViewHolder) {
                ViewHolder itemViewHolder =
                        (ViewHolder) viewHolder;
                itemViewHolder.itemView.setBackgroundColor(Color.WHITE);
            }
        }
    }
}