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

package com.metinkale.prayer.times.alarm.sounds;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.alarm.Alarm;
import com.metinkale.prayer.utils.LocaleUtils;
import com.metinkale.prayer.utils.Utils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SoundChooserAdapter extends RecyclerView.Adapter<SoundChooserAdapter.ItemVH> {
    private final RecyclerView mRecyclerView;
    private final LinearLayoutManager mLayoutManager;
    private final Alarm mAlarm;
    private final boolean mSortSounds;
    private List<Sound> mSounds = new ArrayList<>();
    private List<Sound> mRootSounds;
    private BundledSound mExpanded;
    private Sound mSelected;
    private int volume = -1;
    private MyPlayer mPlayer;
    private OnClickListener mOnItemClickListener;
    private boolean mShowRadioButton = true;

    public interface OnClickListener {
        void onItemClick(ItemVH vh, Sound sound);
    }

    public Sound getItem(int i) {
        return mSounds.get(i);
    }

    public SoundChooserAdapter(RecyclerView recyclerView, List<Sound> rootSounds, Alarm alarm, boolean sortSounds) {
        super();
        mSortSounds = sortSounds;
        mAlarm = alarm;
        mRecyclerView = recyclerView;
        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        mRootSounds = rootSounds;
        update();
    }

    public void update() {

        mSounds.clear();
        if (mSortSounds)
            Collections.sort(mRootSounds, (o1, o2) -> {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.compareTo(name2);
            });

        mSounds.addAll(mRootSounds);
        for (ListIterator<Sound> iter = mSounds.listIterator(); iter.hasNext(); ) {
            Sound o = iter.next();
            if (o == mExpanded && o != null) {
                Collection<Sound> sounds = ((BundledSound) o).getSubSounds();
                for (Sound sound : sounds) {
                    iter.add(sound);
                }
            }
        }

        mSounds.removeAll(Collections.singleton(null));
        notifyDataSetChanged();

    }

    @Override
    public long getItemId(int position) {
        return mSounds.get(position).getId();
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemVH vh, final int position) {
        final Sound sound = mSounds.get(position);
        vh.resetAudio();
        vh.getRadio().setVisibility(mShowRadioButton ? View.VISIBLE : View.GONE);

        if (sound instanceof BundledSound) {
            vh.getExpand().setVisibility(View.VISIBLE);
            vh.getExpand().setOnClickListener(v -> {
                if (mExpanded == sound) {
                    mExpanded = null;
                } else {
                    mExpanded = (BundledSound) sound;
                }
                update();
                checkAllVisibilities();
            });
        } else {
            vh.getExpand().setVisibility(View.GONE);
        }

        vh.getText().setText(sound.getName());
        if (mRootSounds.contains(sound)) {
            vh.getView().setPadding(0, 0, 0, 0);
        } else {
            int padding = (int) Utils.convertDpToPixel(App.get(), 32);
            vh.getView().setPadding(padding, 0, padding, 0);
        }

        checkVisibility(sound, vh);
        checkCheckbox(sound, vh);
        checkAction(sound, vh);


        vh.getAction().setOnClickListener(v -> {
            if (sound.isDownloaded()) {
                playPause(v, vh, sound);
            } else {
                downloadSound(v, sound.getName(), sound.getAppSounds());
            }
        });


        vh.getRadio().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mSelected = sound;
                checkAllCheckboxes();
            }
        });


        if (mOnItemClickListener != null) {
            vh.getView().setOnClickListener(v -> mOnItemClickListener.onItemClick(vh, mSounds.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return mSounds.size();
    }


    private void checkAllVisibilities() {
        for (int childCount = mRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            View view = mRecyclerView.getChildAt(i);
            ItemVH holder = (ItemVH) mRecyclerView.getChildViewHolder(view);
            if (holder.getAdapterPosition() == -1) continue;
            checkVisibility(mSounds.get(holder.getAdapterPosition()), holder);
        }
    }

    private void checkAllCheckboxes() {
        for (int childCount = mRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            View view = mRecyclerView.getChildAt(i);
            ItemVH holder = (ItemVH) mRecyclerView.getChildViewHolder(view);
            if (holder.getAdapterPosition() == -1) continue;
            checkCheckbox(mSounds.get(holder.getAdapterPosition()), holder);
        }
    }

    private void checkAllActionButtons() {
        for (int childCount = mRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            View view = mRecyclerView.getChildAt(i);
            ItemVH holder = (ItemVH) mRecyclerView.getChildViewHolder(view);
            if (holder.getAdapterPosition() == -1) continue;
            checkAction(mSounds.get(holder.getAdapterPosition()), holder);
        }
    }

    private void checkVisibility(Sound item, ItemVH vh) {
        if (mRootSounds.contains(item)) {
            vh.getView().setVisibility(View.VISIBLE);
            vh.getExpand().setRotation(mExpanded == item ? 180 : 0);
        } else {
            vh.getView().setVisibility(mExpanded != null && mExpanded.getSubSounds().contains(item) ? View.VISIBLE : View.GONE);
        }
    }

    private void checkCheckbox(Sound item, ItemVH vh) {
        if (item == mSelected) {
            vh.getRadio().setChecked(true);
            vh.getStaticRadio().setVisibility(View.GONE);
        } else {
            vh.getRadio().setChecked(false);
            if (mSelected instanceof BundledSound) {
                if (((BundledSound) mSelected).getSubSounds().contains(item)) {
                    vh.getStaticRadio().setVisibility(((BundledSound) mSelected).getSubSounds().contains(item) ? View.VISIBLE : View.GONE);
                }
            } else if (item instanceof BundledSound) {
                vh.getStaticRadio().setVisibility(((BundledSound) item).getSubSounds().contains(mSelected) ? View.VISIBLE : View.GONE);
            }
        }
    }


    private void checkAction(Sound item, ItemVH vh) {
        boolean dled = item.isDownloaded();
        vh.getRadio().setEnabled(dled);
        vh.getAction().setIcon(dled ? MaterialDrawableBuilder.IconValue.PLAY : MaterialDrawableBuilder.IconValue.DOWNLOAD);
    }


    private void downloadSound(View v, String name, final Set<AppSound> items) {
        int size = 0;
        for (Sound item : items) {
            size += item.getSize();
        }
        final Context ctx = v.getContext();
        AlertDialog dialog = new AlertDialog.Builder(ctx).create();
        dialog.setTitle(R.string.sound);
        dialog.setMessage(ctx.getString(R.string.dlSound, name, LocaleUtils.readableSize(size)));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final ProgressDialog dlg = new ProgressDialog(ctx);
                dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dlg.setMax(items.size() * 100);
                dlg.show();
                final Iterator<AppSound> iterator = items.iterator();
                final AtomicInteger count = new AtomicInteger(0);

                //bit tricky and hacky recursion using iterators :)
                FutureCallback<File> callback = new FutureCallback<File>() {
                    Sound lastItem;

                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (lastItem != null) {
                            count.incrementAndGet();
                            if (e != null || result == null || !result.exists()) {
                                if (e != null) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                }
                                dlg.cancel();
                                Toast.makeText(App.get(), R.string.error, Toast.LENGTH_LONG).show();
                            }
                            checkAllActionButtons();
                        }

                        if (iterator.hasNext()) {
                            AppSound item = iterator.next();
                            File f = item.getFile();
                            f.getParentFile().mkdirs();

                            if (!item.isDownloaded()) {
                                Ion.with(ctx)
                                        .load(item.getUrl())
                                        .progress((downloaded, total) -> dlg.setProgress((int) (count.get() * 100 + (downloaded * 100 / total))))
                                        .write(f)
                                        .setCallback(this);
                            } else {
                                onCompleted(null, f);
                            }
                            lastItem = item;
                        } else {
                            if (dlg.isShowing())
                                dlg.dismiss();
                            checkAllActionButtons();
                        }
                    }
                };
                callback.onCompleted(null, null);


            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, ctx.getString(R.string.no), (dialog1, i) -> dialog1.cancel());
        dialog.show();
    }

    private void playPause(final View v, final ItemVH vh, final Sound item) {
        if (vh.getPlayer() == null) {
            resetAudios();
            vh.getAction().setIcon(MaterialDrawableBuilder.IconValue.PAUSE);
            vh.getText().setVisibility(View.INVISIBLE);
            vh.getSeekbar().setVisibility(View.VISIBLE);


            try {
                mPlayer = MyPlayer.from(item).alarm(mAlarm).seekbar(vh.getSeekbar()).volume(volume).play().onComplete(vh::resetAudio);
                vh.setPlayer(mPlayer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            vh.resetAudio();
        }
    }


    @NonNull
    @Override
    public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemVH(parent);
    }

    public void resetAudios() {
        for (int childCount = mRecyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            View view = mRecyclerView.getChildAt(i);
            ItemVH holder = (ItemVH) mRecyclerView.getChildViewHolder(view);
            holder.resetAudio();
        }
    }

    public Sound getSelected() {
        return mSelected;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        if (mPlayer != null)
            mPlayer.volume(volume);
    }

    public void setOnItemClickListener(OnClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setShowRadioButton(boolean mShowRadioButton) {
        this.mShowRadioButton = mShowRadioButton;
    }

    public static class ItemVH extends RecyclerView.ViewHolder {
        private View view;
        private MaterialIconView action;
        private MaterialIconView expand;
        private SeekBar seekbar;
        private RadioButton radio;
        private RadioButton staticRadio;
        private TextView text;
        private MyPlayer player;

        ItemVH(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_chooser_item, parent, false));

            view = itemView;

            action = view.findViewById(R.id.action);
            expand = view.findViewById(R.id.expand);
            radio = view.findViewById(R.id.radioButton);
            staticRadio = view.findViewById(R.id.staticRadioDisabled);
            text = view.findViewById(R.id.text);
            seekbar = view.findViewById(R.id.seekbar);
        }

        void resetAudio() {
            if (getSeekbar().getVisibility() == View.VISIBLE) {
                getAction().setIcon(MaterialDrawableBuilder.IconValue.PLAY);
            }
            getSeekbar().setVisibility(View.GONE);
            getText().setVisibility(View.VISIBLE);
            if (getPlayer() != null) {
                try {
                    getPlayer().stop();
                } catch (Exception ignore) {
                }
                setPlayer(null);
            }
        }

        public MyPlayer getPlayer() {
            return player;
        }

        public void setPlayer(MyPlayer player) {
            this.player = player;
        }

        public View getView() {
            return view;
        }

        public MaterialIconView getAction() {
            return action;
        }

        public MaterialIconView getExpand() {
            return expand;
        }

        public SeekBar getSeekbar() {
            return seekbar;
        }

        public RadioButton getRadio() {
            return radio;
        }

        public RadioButton getStaticRadio() {
            return staticRadio;
        }

        public TextView getText() {
            return text;
        }
    }
}
