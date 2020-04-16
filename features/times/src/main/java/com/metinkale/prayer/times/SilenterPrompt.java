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

package com.metinkale.prayer.times;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

import com.metinkale.prayer.times.alarm.SilenterReceiver;
import com.metinkale.prayer.utils.PermissionUtils;

public class SilenterPrompt extends AppCompatActivity {
    private SharedPreferences widgets;

    @Override
    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        widgets = getSharedPreferences("widgets", 0);

        setContentView(R.layout.vakit_silenterprompt);

        final NumberPicker np = findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(300);
        np.setValue(widgets.getInt("silenterWidget", 15));

        findViewById(R.id.cancel).setOnClickListener(view -> finish());

        findViewById(R.id.ok).setOnClickListener(v -> {
            widgets.edit().putInt("silenterWidget", np.getValue()).apply();
            if (PermissionUtils.get(this).pNotPolicy)
                SilenterReceiver.silent(v.getContext(), np.getValue());
            else
                PermissionUtils.get(this).needNotificationPolicy(this);
            finish();
        });

    }
}
