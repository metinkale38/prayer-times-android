package com.metinkale.prayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.Locale;

public class InternalBroadcastReceiver extends InternalBroadcast.Receiver {
    
    public InternalBroadcastReceiver() {
        super(InternalBroadcast.ACTION_PREFSCHANGED);
    }
    
    
    @Override
    protected void onPrefsChanged(String key) {
        switch (key) {
            case "language":
                Context c = App.get();
                String language = Prefs.getLanguage();
                if (language.isEmpty())
                    language = "system";
                
                PackageManager pm = c.getPackageManager();
                String[] languages = App.get().getResources().getStringArray(com.metinkale.prayer.base.R.array.language_val);
                boolean hasEnabledActivity = false;
                for (String lang : languages) {
                    if (lang.equals("system"))
                        continue;
                    
                    if (lang.equals(language)) {
                        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.alias" + lang.toUpperCase(Locale.ENGLISH)),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        hasEnabledActivity = true;
                    } else {
                        pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.alias" + lang.toUpperCase(Locale.ENGLISH)),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    }
                }
                
                pm.setComponentEnabledSetting(new ComponentName(c, "com.metinkale.prayer.aliasDefault"),
                        hasEnabledActivity ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
        }
    }
}
