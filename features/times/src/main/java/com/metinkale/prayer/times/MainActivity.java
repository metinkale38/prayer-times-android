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

import android.os.Bundle;

import com.metinkale.prayer.BaseActivity;
import com.metinkale.prayer.times.fragments.SearchCityFragment;
import com.metinkale.prayer.times.fragments.TimesFragment;

public class MainActivity extends BaseActivity {

    public MainActivity() {
        super(R.string.appName, R.mipmap.ic_launcher, new TimesFragment());
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationReceiver.triggerUpdate(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("openCitySearch", false)) {
            moveToFrag(new SearchCityFragment());
        }
    }
}
