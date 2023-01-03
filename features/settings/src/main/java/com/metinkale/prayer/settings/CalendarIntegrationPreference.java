/*
 * Copyright (c) 2013-2023 Metin Kale
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

package com.metinkale.prayer.settings;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;

import androidx.preference.SwitchPreference;

import com.metinkale.prayer.utils.PermissionUtils;

public class CalendarIntegrationPreference extends SwitchPreference {


    public CalendarIntegrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Activity getActivity() {
        Context c = getContext();
        while ((c instanceof ContextWrapper) && !(c instanceof Activity)) {
            c = ((ContextWrapper) c).getBaseContext();
        }
        if (c instanceof Activity) {
            return (Activity) c;
        }
        return null;
    }

    @Override
    protected void onClick() {
        if (PermissionUtils.get(getActivity()).pCalendar) {
            super.onClick();
        } else {
            PermissionUtils.get(getActivity()).needCalendar(getActivity(), true);
        }
    }

    // backwards compability

    @Override
    protected boolean persistBoolean(boolean value) {
        return persistString(value ? "1" : "-1");
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (PermissionUtils.get(getActivity()).pCalendar)
            return "1".equals(getPersistedString(defaultReturnValue ? "1" : "-1"));
        else
            return false;
    }
}