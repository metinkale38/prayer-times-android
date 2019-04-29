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

package com.metinkale.prayer.times.alarm.sounds;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.R;
import com.metinkale.prayer.times.times.Vakit;
import com.metinkale.prayer.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Sounds {
    private static final List<Sound> sSounds = new ArrayList<>();

    static {
        loadSounds();
    }


    public static List<Sound> getSounds() {
        return sSounds;
    }

    static void addSound(Sound sound) {
        sSounds.add(sound);
    }

    public static List<Sound> getRootSounds() {
        ArrayList<Sound> sounds = new ArrayList<>();
        for (Sound sound : sSounds) {
            if (sound instanceof BundledSound || sound instanceof UserSound) {
                sounds.add(sound);
            }
        }
        return sounds;
    }


    public static Sound getSound(int id) {
        for (Sound sound : sSounds) {
            if (sound.getId() == id) {
                return sound;
            }
        }
        return null;
    }

    public static Sound getSound(String uri) {
        Sound sound = getSound(uri.hashCode());
        if (sound == null) {
            return UserSound.create(Uri.parse(uri));
        }
        return sound;
    }

    protected static void downloadData(Context c, final Runnable onFinish) {
        final ProgressDialog dlg = new ProgressDialog(c);
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        dlg.show();

        final Runnable onFinish2 = () -> {
            if (onFinish != null)
                onFinish.run();
            dlg.dismiss();
        };
        Ion.with(App.get()).load(App.API_URL + "/sounds/sounds.json").progressDialog(dlg)
                .asString(Charset.forName("UTF-8")).setCallback((exp, result) -> {
                    if (exp != null) {
                        Crashlytics.logException(exp);
                    } else {
                        FileOutputStream outputStream = null;
                        try {
                            if (!new JSONObject(result).getString("name").equals("sounds")) return;

                            outputStream = App.get().openFileOutput("sounds.json", Context.MODE_PRIVATE);
                            outputStream.write(result.getBytes());

                            loadSounds();
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                        } finally {
                            Utils.close(outputStream);
                        }
                    }
                    onFinish2.run();
                });
    }

    private static synchronized void loadSounds() {
        if (!sSounds.isEmpty()) return;
        String json = null;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(App.get().openFileInput("sounds.json")));
            StringBuilder text = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            json = text.toString();
        } catch (IOException e) {
            Crashlytics.logException(e);
        } finally {
            Utils.close(br);
        }
        if (json == null) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jfiles = jsonObject.getJSONArray("files");
            sSounds.clear();

            for (int i = 0; i < jfiles.length(); i++) {
                JSONObject jobj = jfiles.getJSONObject(i);
                if (!jobj.has("files")) continue;
                String folderName = jobj.getString("name");
                BundledSound bundledSound = new BundledSound(folderName);


                JSONArray jsounds = jobj.getJSONArray("files");
                for (int j = 0; j < jsounds.length(); j++) {
                    JSONObject jsound = jsounds.getJSONObject(j);
                    String name = jsound.getString("name");

                    String url = "/sounds/" + URLEncoder.encode(folderName).replace("+", "%20") + "/" + URLEncoder.encode(name).replace("+", "%20");

                    if (name.contains("."))
                        name = name.substring(0, name.lastIndexOf("."));
                    AppSound sound = new AppSound(name, jsound.getInt("size"), jsound.getString("md5"), url);

                    if (!name.contains(" - ")) {
                        bundledSound.addSound(BundledSound.SoundType.Default, sound);
                        sound.setShortName(name);
                        continue;
                    }

                    String params = name.substring(name.indexOf(" - ") + 3).trim();
                    if (params.contains("Short") && params.contains("Fajr")) {
                        bundledSound.addSound(BundledSound.SoundType.FajrShort, sound);
                        sound.setShortName(App.get().getString(R.string.fajrShort));
                    } else if (params.contains("Short")) {
                        bundledSound.addSound(BundledSound.SoundType.Takbir, sound);
                        sound.setShortName(App.get().getString(R.string.takbir));
                    } else if (params.contains("Fajr")) {
                        bundledSound.addSound(BundledSound.SoundType.Fajr, sound);
                        sound.setShortName(Vakit.FAJR.getString());
                    } else if (params.contains("Zuhr")) {
                        bundledSound.addSound(BundledSound.SoundType.Zuhr, sound);
                        sound.setShortName(Vakit.DHUHR.getString());
                    } else if (params.contains("Asr")) {
                        bundledSound.addSound(BundledSound.SoundType.Asr, sound);
                        sound.setShortName(Vakit.ASR.getString());
                    } else if (params.contains("Magrib")) {
                        bundledSound.addSound(BundledSound.SoundType.Magrib, sound);
                        sound.setShortName(Vakit.MAGHRIB.getString());
                    } else if (params.contains("Isha")) {
                        bundledSound.addSound(BundledSound.SoundType.Ishaa, sound);
                        sound.setShortName(Vakit.ISHAA.getString());
                    } else if (params.contains("Dua")) {
                        bundledSound.addSound(BundledSound.SoundType.Dua, sound);
                        sound.setShortName(App.get().getString(R.string.adhanDua));
                    } else if (params.contains("Sela")) {
                        bundledSound.addSound(BundledSound.SoundType.Sela, sound);
                        sound.setShortName(App.get().getString(R.string.salavat));
                    } else {
                        bundledSound.addSound(BundledSound.SoundType.Default, sound);
                        sound.setShortName(params);
                    }

                    sound.setName(folderName + " - " + sound.getShortName());

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}
