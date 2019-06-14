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

package com.metinkale.prayer.times.times.sources;

import android.util.Log;

import androidx.annotation.NonNull;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Source;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by metin on 12.02.2017.
 */
public class IndonesiaTimes extends WebTimes {
    @SuppressWarnings({"unused", "WeakerAccess"})
    public IndonesiaTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public IndonesiaTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Indonesia;
    }


    protected boolean sync() throws ExecutionException, InterruptedException {

        Response<String> resp = Ion.with(App.get()).load("http://sihat.kemenag.go.id").asString().withResponse().get();
        List<String> cookies = resp.getHeaders().getHeaders().getAll("Set-Cookie");
        String phpsessid = null;
        for (String cookie : cookies) {
            if (cookie.contains("PHPSESSID"))
                phpsessid = cookie;
        }

        resp = Ion.with(App.get()).load("https://bimasislam.kemenag.go.id/jadwalshalat").setHeader("Cookie", "PHPSESSID:" + phpsessid + ";").asString().withResponse().get();

        String[] lines = resp.getResult().split("\n");
        for (String line : lines) {
            if (line.contains("option")) {
                Log.e("line", line);
                break;
            }
        }


        return false;
    }


}
