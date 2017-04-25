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
import android.graphics.Bitmap;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by metin on 23.04.2017.
 */

public class TestUtils {
    public static void takeScreenshot(final String name, final Activity activity) throws IOException {
        final AtomicBoolean finished = new AtomicBoolean();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                View scrView = activity.getWindow().getDecorView().getRootView();
                scrView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(scrView.getDrawingCache());
                scrView.setDrawingCacheEnabled(false);

                File folder = new File(activity.getExternalFilesDir(null), "screenshots");
                if (!folder.exists()) folder.mkdirs();
                String path = folder.getAbsolutePath() + "/" + name + "_" + bitmap.getWidth() + "x" + bitmap.getHeight() + ".png";


                OutputStream out = null;
                File imageFile = new File(path);

                try {
                    out = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    try {
                        if (out != null) {
                            out.close();
                        }

                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }

                }
                finished.getAndSet(true);
            }
        });

        while (!finished.get()) {
            try {
                Thread.sleep(100 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
