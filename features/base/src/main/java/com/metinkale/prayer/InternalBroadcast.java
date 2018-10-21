package com.metinkale.prayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class InternalBroadcast {
    public static final String ACTION_ON_START = "com.metikale.prayer.ON_START";
    public static final String ACTION_PREFSCHANGED = "com.metinkale.prayer.PREFS_CHANGED";
    public static final String ACTION_TIMETICK = "com.metinkale.prayer.TIMETICK";
    private final Context context;
    
    private InternalBroadcast(Context c) {
        context = c;
    }
    
    /**
     * easier setup of new InternalBroadcastReceivers without manifest modifications
     *
     * @param clz
     */
    public InternalBroadcast registerClass(String clz) {
        try {
            Receiver receiver = (Receiver) Class.forName(clz).newInstance();
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    
    public static InternalBroadcast with(Context c) {
        return new InternalBroadcast(c);
    }
    
    public InternalBroadcast sendOnPrefsChanged(String key) {
        Intent i = new Intent(ACTION_PREFSCHANGED);
        i.putExtra("key", key);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        return this;
    }
    
    
    public InternalBroadcast sendOnStart() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_ON_START));
        return this;
    }
    
    public InternalBroadcast sendTimeTick() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_TIMETICK));
        return this;
    }
    
    public static abstract class Receiver extends BroadcastReceiver {
        
        public Receiver(Context context, String... actions) {
            if (actions != null && actions.length > 0) {
                IntentFilter filter = new IntentFilter(actions[0]);
                for (int i = 1; i < actions.length; i++) {
                    filter.addAction(actions[i]);
                }
                context.registerReceiver(this, filter);
            }
        }
        
        public Receiver(String... actions) {
            this(App.get(), actions);
        }
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent == null || (action = intent.getAction()) == null)
                return;
            switch (action) {
                case InternalBroadcast.ACTION_TIMETICK:
                    onTimeTick();
                    break;
                case InternalBroadcast.ACTION_ON_START:
                    onStart();
                    break;
                case InternalBroadcast.ACTION_PREFSCHANGED:
                    onPrefsChanged(intent.getStringExtra("key"));
                    break;
            }
        }
        
        protected void onPrefsChanged(String key) {
        }
        
        protected void onStart() {
        }
        
        protected void onTimeTick() {
        }
    }
    
    
}
