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

package com.metinkale.prayerapp.vakit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.NumberPicker;
import com.metinkale.prayer.R;

public class SilenterPrompt extends Activity {
    private SharedPreferences widgets;

    @Override
    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        widgets = getSharedPreferences("widgets", 0);

        setContentView(R.layout.vakit_silenterprompt);

        final NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(300);
        np.setValue(widgets.getInt("silenterWidget", 15));

        findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                widgets.edit().putInt("silenterWidget", np.getValue()).apply();
                AlarmReceiver.silenter(v.getContext(), np.getValue());
                finish();
            }
        });

    }
}
