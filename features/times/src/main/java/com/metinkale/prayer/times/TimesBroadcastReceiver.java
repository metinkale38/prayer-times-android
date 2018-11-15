package com.metinkale.prayer.times;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.metinkale.prayer.App;
import com.metinkale.prayer.InternalBroadcastReceiver;
import com.metinkale.prayer.times.times.Times;

import androidx.annotation.NonNull;


public class TimesBroadcastReceiver extends InternalBroadcastReceiver
        implements InternalBroadcastReceiver.OnStartListener, InternalBroadcastReceiver.OnPrefsChangedListener {
    
    
    @Override
    public void onPrefsChanged(@NonNull String key) {
        switch (key) {
            case "useAlarm":
                Times.setAlarms();
                break;
            case "use12h":
            case "ongoingIcon":
            case "ongoingNumber":
            case "showAltWidgetHightlight":
            case "widget_countdown":
            case "alternativeOngoing":
            case "showLegacyWidgets":
                TimeTickReceiver.start(App.get());
                break;
        }
    }
    
    @Override
    public void onStart() {
        JobManager.create(App.get()).addJobCreator(new SyncJobCreator());
        
        try {
            Times.getTimes();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        
        TimeTickReceiver.start(App.get());
        LocationReceiver.start(App.get());
        Times.setAlarms();
    }
}
