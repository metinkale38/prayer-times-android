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
 *
 */

package com.metinkale.prayer;


import android.app.Activity;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.metinkale.prayerapp.zikr.Main;
import com.metinkale.prayerapp.utils.Utils;

import org.junit.Rule;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ZikrTest {

    @Rule
    public ActivityTestRule<Main> mActivityTestRule = new ActivityTestRule<>(Main.class);

    @org.junit.Test
    public void takeScreenshots() throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.WRITE_EXTERNAL_STORAGE");
        }

        Activity act = mActivityTestRule.getActivity();
        String[] langs = act.getResources().getStringArray(R.array.language_val);
        act.finish();

        for (final String lang : langs) {
            Utils.changeLanguage(lang);
            Utils.init(act);


            act = mActivityTestRule.launchActivity(null);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TestUtils.takeScreenshot("zikr" + lang.toUpperCase(), act);
            act.finish();



        }

    }


}
