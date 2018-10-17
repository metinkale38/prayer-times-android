package com.metinkale.prayer.times;

import android.content.Context;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.metinkale.prayer.App;
import com.metinkale.prayer.InternalBroadcast;
import com.metinkale.prayer.times.times.Times;

import static com.metinkale.prayer.InternalBroadcast.ACTION_ON_START;
import static com.metinkale.prayer.InternalBroadcast.ACTION_PREFSCHANGED;


public class InternalBroadcastReceiver extends InternalBroadcast.Receiver {

    public InternalBroadcastReceiver() {
        super(ACTION_ON_START, ACTION_PREFSCHANGED);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action;
        if (intent == null || (action = intent.getAction()) == null) return;
        switch (action) {
            case ACTION_ON_START:
                try {
                    Times.getTimes();
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }

                WidgetService.start(App.get());
                LocationService.start(App.get());
                Times.setAlarms();

                JobManager.create(App.get()).addJobCreator(new SyncJobCreator());
                break;
            case ACTION_PREFSCHANGED:
                String key = intent.getStringExtra("key");
                if (key != null) {
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
                            WidgetService.start(App.get());
                            break;
                    }
                }
        }
    }

}
