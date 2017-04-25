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
import android.os.Environment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.koushikdutta.ion.Ion;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.hadis.Main;
import com.metinkale.prayerapp.hadis.SqliteHelper;
import com.metinkale.prayerapp.settings.Prefs;
import com.metinkale.prayerapp.utils.Utils;

import org.junit.Rule;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HadithTest {

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

            String hlang = Prefs.getLanguage();

            if (!lang.equals("de") && !lang.equals("tr")) hlang = "en";
            final String file = lang + "/hadis.db";
            final String url = App.API_URL + "/hadis." + hlang + ".db";
            File f = new File(App.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file);

            f.delete();
            if (!f.getParentFile().mkdirs()) {
                Log.e("BaseActivity", "could not mkdirs " + f.getParent());
            }


            Ion.with(App.get())
                    .load(url)
                    .write(f)
                    .get();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SqliteHelper.get();
            if (SqliteHelper.get().getCount() == 0) continue;
            act = mActivityTestRule.launchActivity(null);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TestUtils.takeScreenshot("hadith" + lang.toUpperCase(), act);
            act.finish();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


}
