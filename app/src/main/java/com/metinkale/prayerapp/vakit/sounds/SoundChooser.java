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

package com.metinkale.prayerapp.vakit.sounds;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.utils.FileChooser;
import com.metinkale.prayerapp.utils.MD5;
import com.metinkale.prayerapp.utils.PermissionUtils;
import com.metinkale.prayerapp.vakit.AlarmReceiver;
import com.metinkale.prayerapp.vakit.sounds.Sounds.Sound;
import com.metinkale.prayerapp.vakit.times.other.Vakit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoundChooser extends DialogFragment implements OnItemClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    private ListView mList;
    private CheckBox mVolumeCB;
    private SeekBar mVolume;
    private MyAdapter mAdapter;
    private MediaPlayer mMp;
    private Callback mCb;
    private int mStartVolume;
    private AudioManager mAm;
    private Runnable onResume;

    public static String getRingtonePathFromContentUri(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor ringtoneCursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        ringtoneCursor.moveToFirst();

        String path = ringtoneCursor.getString(ringtoneCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

        ringtoneCursor.close();
        return path;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (onResume != null) {
            onResume.run();
            onResume = null;
        }
    }

    public void showExpanded(FragmentManager fm, Callback cb) {
        mCb = cb;
        show(fm, "tag");
    }

    @Override
    public void onPause() {
        super.onPause();
        mAm.setStreamVolume(AlarmReceiver.getStreamType(getActivity()), mStartVolume, 0);
        if (mMp != null) {
            mMp.stop();
            mMp.release();
            mMp = null;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAm = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mStartVolume = mAm.getStreamVolume(AlarmReceiver.getStreamType(getActivity()));
        if (Sounds.getSounds().isEmpty()) {
            dismiss();
            return new AlertDialog.Builder(getActivity()).create();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.sound_chooser, null);
        mList = (ListView) v.findViewById(R.id.lv);
        mVolume = (SeekBar) v.findViewById(R.id.volume);
        mVolumeCB = (CheckBox) v.findViewById(R.id.volumeCB);

        mVolume.setMax(mAm.getStreamMaxVolume(AlarmReceiver.getStreamType(getActivity())));

        mVolume.setOnSeekBarChangeListener(this);
        mVolumeCB.setOnCheckedChangeListener(this);


        mList.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        if (mCb == null)
            mAdapter = new MyAdapter(getActivity(), new ArrayList<Sound>());
        mAdapter = new MyAdapter(getActivity(), mCb.getSounds());
        mList.setAdapter(mAdapter);

        mList.setOnItemClickListener(this);
        builder.setView(v).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mMp != null) {
                    mMp.stop();
                    mMp.release();
                    mMp = null;
                }
                if (mList.getTag() != null) {
                    if (mVolumeCB.isChecked()) {
                        mCb.setCurrent(((Sound) mList.getTag()).uri + "$volume" + mVolume.getProgress());
                    } else {
                        mCb.setCurrent(((Sound) mList.getTag()).uri);
                    }
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mMp != null) {
                    mMp.stop();
                    mMp.release();
                    mMp = null;
                }
                getDialog().cancel();
            }
        });

        Sound s = new Sound();
        s.uri = mCb.getCurrent();
        mList.setTag(s);
        if ((s.uri != null) && s.uri.contains("$volume")) {
            mVolumeCB.setChecked(true);
            mVolume.setEnabled(true);
            int progress = Integer.parseInt(s.uri.substring(s.uri.indexOf("$volume") + 7));
            mVolume.setProgress(progress);
            mAm.setStreamVolume(AlarmReceiver.getStreamType(getActivity()), progress, 0);
            s.uri = s.uri.substring(0, s.uri.indexOf("$volume"));

        } else {
            mVolume.setEnabled(false);
            mVolume.setProgress(0);
            mVolumeCB.setChecked(false);

        }


        mList.setItemChecked(mAdapter.getPosition(s), true);

        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> v, View item, int pos, long arg3) {

        if (mMp != null) {
            mMp.stop();
            mMp.release();
            mMp = null;
        }

        final Sound s = mAdapter.getItem(pos);
        if ("picker".equals(s.uri)) {
            onResume = new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.other)
                            .setItems(R.array.soundPicker, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        FileChooser chooser = new FileChooser(getActivity());
                                        chooser.showDialog();
                                        chooser.setFileListener(new FileChooser.FileSelectedListener() {
                                            @Override
                                            public void fileSelected(File file) {
                                                onSelect(Uri.fromFile(file));
                                            }
                                        });
                                    } else {
                                        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                                        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                                        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
                                        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                                        Intent chooserIntent = Intent.createChooser(i, getActivity().getString(R.string.sound));
                                        startActivityForResult(chooserIntent, 0);
                                    }
                                }
                            });
                    builder.show();

                }
            };
            if (PermissionUtils.get(getActivity()).pStorage) {
                onResume.run();
                onResume = null;
            } else {
                PermissionUtils.get(getActivity()).needStorage(getActivity());
            }
        } else if (!"silent".equals(s.uri)) {
            if (Sounds.isDownloaded(s)) {
                try {
                    mMp = AlarmReceiver.play(App.getContext(), s.uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mList.setTag(s);
            } else if (!App.isOnline()) {
                Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.setTitle(R.string.sound);
                dialog.setMessage(getString(R.string.dlSound, s.name, s.size));
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int buttonId) {
                        final ProgressDialog dlg = new ProgressDialog(getActivity());
                        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        dlg.show();
                        File f = s.getFile();
                        f.getParentFile().mkdirs();
                        Ion.with(getActivity())
                                .load(s.url)
                                .progressDialog(dlg)
                                .write(f)
                                .setCallback(new FutureCallback<File>() {
                                    @Override
                                    public void onCompleted(Exception e, File result) {
                                        if (e != null || result == null || !result.exists()) {
                                            if (e != null) {
                                                e.printStackTrace();
                                                Crashlytics.logException(e);
                                            }
                                            dlg.cancel();
                                            Toast.makeText(App.getContext(), R.string.error, Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        Ion.with(App.getContext()).load(s.url + ".md5")
                                                .setTimeout(3000)
                                                .asString()
                                                .setCallback(new FutureCallback<String>() {
                                                    @Override
                                                    public void onCompleted(Exception e, String md5) {
                                                        dlg.cancel();
                                                        if (e != null) {
                                                            e.printStackTrace();
                                                            Crashlytics.logException(e);
                                                            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        if (!MD5.isValidMD5(md5)) return;
                                                        SharedPreferences preferences = App.getContext().getSharedPreferences("md5", 0);
                                                        preferences.edit().putString(s.name, md5).apply();
                                                        s.checkMD5();

                                                        try {
                                                            mMp = AlarmReceiver.play(App.getContext(), s.uri);
                                                        } catch (IOException ee) {
                                                            ee.printStackTrace();
                                                            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                                                        }
                                                        mList.setTag(s);
                                                    }
                                                });

                                    }
                                });


                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int buttonId) {
                        dialog.cancel();
                        Sound s = new Sound();
                        s.uri = mCb.getCurrent();

                        mList.setItemChecked(mAdapter.getPosition(s), true);

                    }
                });
                dialog.show();

            }
        } else {
            mList.setTag(s);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            try {
                uri = Uri.parse(getRingtonePathFromContentUri(App.getContext(), uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
            onSelect(uri);
        }
    }

    private void onSelect(Uri uri) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(getActivity(), uri);
            mp.prepare();
            mp.start();
            mp.reset();
        } catch (Exception e) {
            Toast.makeText(App.getContext(), R.string.corruptAudio, Toast.LENGTH_LONG).show();
            dismiss();
            new SoundChooser().showExpanded(getFragmentManager(), mCb);
            return;
        }
        if (uri != null) {
            mCb.setCurrent(uri.toString());
            dismiss();
            new SoundChooser().showExpanded(getFragmentManager(), mCb);

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mVolume.setEnabled(isChecked);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAm.setStreamVolume(AlarmReceiver.getStreamType(getActivity()), progress, 0);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface Callback {
        String getCurrent();

        void setCurrent(String current);

        List<Sound> getSounds();

        Vakit getVakit();
    }

    public class MyAdapter extends ArrayAdapter<Sound> {

        public MyAdapter(Context context, List<Sound> sounds) {
            super(context, 0, 0, sounds);
            Sound silent = new Sound();
            silent.name = context.getString(R.string.silent);
            silent.uri = "silent";
            sounds.add(0, silent);
            Sound s = new Sound();
            s.uri = mCb.getCurrent();

            if (!s.uri.startsWith("silent")) {
                try {
                    String sound = s.uri;
                    if (sound.contains("$")) {
                        sound = sound.substring(0, sound.indexOf("$"));
                    }
                    AlarmReceiver.play(App.getContext(), sound).reset();
                } catch (Exception e) {
                    mCb.setCurrent("silent");
                    s.uri = "silent";
                }
            }

            if ((s.uri != null) && s.uri.contains("$volume")) {
                s.uri = s.uri.substring(0, s.uri.indexOf("$volume"));
            }
            if (!sounds.contains(s)) {
                Ringtone r = RingtoneManager.getRingtone(getContext(), Uri.parse(s.uri));
                if (r != null) {
                    s.name = r.getTitle(App.getContext());
                } else {
                    s.name = "Unknown";
                }
                sounds.add(s);
            }

            Sound other = new Sound();
            other.name = context.getString(R.string.selectOther);
            other.uri = "picker";
            sounds.add(other);
        }

        @NonNull
        @Override
        public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getActivity(), android.R.layout.simple_list_item_single_choice, null);
            }

            Sound s = getItem(pos);
            ((TextView) convertView).setText(s.name);

            return convertView;
        }

    }


}
