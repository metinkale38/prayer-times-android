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

package com.metinkale.prayer;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.os.LocaleListCompat;

import com.metinkale.prayer.CrashReporter;
import com.metinkale.prayer.receiver.InternalBroadcastReceiver;
import com.metinkale.prayer.utils.LocaleUtils;

import java.util.Locale;
import java.util.Map;

public class AliasManager extends InternalBroadcastReceiver implements InternalBroadcastReceiver.OnPrefsChangedListener {
    @Override
    public void onPrefsChanged(@NonNull String key) {
        if (key.equals(Preferences.LANGUAGE.getKey())) {
            PackageManager pm = getContext().getPackageManager();
            
            PackageInfo info;
            try {
                info = pm.getPackageInfo(getContext().getApplicationContext().getPackageName(),
                        PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
            } catch (PackageManager.NameNotFoundException e) {
                CrashReporter.recordException(e);
                throw new RuntimeException(e);
            }
            String prefix = "com.metinkale.prayer.alias";
            ArrayMap<String, String> aliases = new ArrayMap<>();
            for (ActivityInfo ai : info.activities) {
                if (ai.name.startsWith(prefix)) {
                    aliases.put(new Locale(ai.name.substring(prefix.length())).getLanguage(), ai.name);
                }
            }
            
            String bestAlias = "Default";
            LocaleListCompat locales = LocaleUtils.getLocalesCompat();
            for (int i = 0; i < locales.size(); i++) {
                String lang = locales.get(i).getLanguage();
                if (aliases.containsKey(lang)) {
                    bestAlias = lang;
                    break;
                }
            }
            
            for (Map.Entry<String, String> entry : aliases.entrySet()) {
                if (bestAlias.equals(entry.getKey())) {
                    //enable
                    pm.setComponentEnabledSetting(new ComponentName(getContext(), entry.getValue()), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                } else if (!bestAlias.equals(entry.getKey())) {
                    //disable
                    pm.setComponentEnabledSetting(new ComponentName(getContext(), entry.getValue()), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }
            }
            
            
        }
    }
}
