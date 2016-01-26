package com.metinkale.prayerapp.vakit;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.AttributeSet;
import android.widget.Toast;
import com.metinkale.prayer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundPreference extends ListPreference implements OnActivityResultListener, OnPreferenceChangeListener {

    private Context mContext;

    public SoundPreference(SoundPreferenceContext act) {
        this((Context) act, null);

    }

    private SoundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(context instanceof SoundPreferenceContext) && context instanceof ContextWrapper) {
            mContext = ((ContextWrapper) context).getBaseContext();
        } else {
            mContext = context;
        }
        setEntries(new String[]{mContext.getString(R.string.silent), mContext.getString(R.string.dua), mContext.getString(R.string.selectother)});
        setEntryValues(new String[]{"silent", "dua", "picker"});

        setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            try {
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(getContext(), uri);
                mp.release();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.corruptAudio, Toast.LENGTH_LONG).show();
                return false;
            }
            if (uri != null) {
                persistString(uri.toString());
                setValue(uri.toString());
                return true;
            }
        }
        return false;
    }

    @Override
    public void setValue(String value) {

        List<CharSequence> vals = new ArrayList<CharSequence>(Arrays.asList(getEntryValues()));
        if (!vals.contains(value)) {
            try {
                List<CharSequence> ents = new ArrayList<CharSequence>(Arrays.asList(getEntries()));
                Ringtone r = RingtoneManager.getRingtone(getContext(), Uri.parse(value));
                String name = r.getTitle(getContext());
                vals.add(value);
                ents.add(name);

                this.setEntries(ents.toArray(new CharSequence[ents.size()]));
                this.setEntryValues(vals.toArray(new CharSequence[vals.size()]));
            } catch (Exception e) {

            }
        }
        super.setValue(value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("picker".equals(newValue)) {

            Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            Intent chooserIntent = Intent.createChooser(i, mContext.getString(R.string.sound));
            ((SoundPreferenceContext) mContext).startActivityForResult(chooserIntent, 0);
            ((SoundPreferenceContext) mContext).setActivityResultListener(this);
            return false;
        }

        return true;

    }

    public void setDefaultTitle(String title) {
        getEntries()[1] = title;

    }

    public interface SoundPreferenceContext {
        void setActivityResultListener(OnActivityResultListener list);

        void startActivityForResult(Intent chooserIntent, int i);
    }
}
