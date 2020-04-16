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

import androidx.annotation.NonNull;

import com.koushikdutta.ion.Ion;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Source;
import com.metinkale.prayer.times.times.Vakit;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by metin on 12.02.2017.
 */
public class LondonTimes extends WebTimes {
    @SuppressWarnings({"unused", "WeakerAccess"})
    public LondonTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public LondonTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.London;
    }


    protected boolean sync() throws ExecutionException, InterruptedException {
        try {
            final Trust trust = new Trust();
            final TrustManager[] trustmanagers = new TrustManager[]{trust};
            SSLContext sslContext;


            Ion ion = Ion.getDefault(App.get());
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustmanagers, new SecureRandom());

            ion.configure().createSSLContext("TLS");

            ion.getHttpClient().getSSLSocketMiddleware().setSSLContext(sslContext);
            ion.getHttpClient().getSSLSocketMiddleware().setTrustManagers(trustmanagers);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        Result r = Ion.with(App.get())
                .load("https://www.londonprayertimes.com/api/times/?formatDate=json&key=1e6f7b94-542d-4ff7-94cc-e9c8e0bd2e64&year=" + LocalDate.now().getYear())
                .setTimeout(3000)
                .userAgent(App.getUserAgent())
                .as(Result.class)
                .get();
        int i = 0;

        DateTimeFormatter pattern = DateTimeFormat.forPattern("yyyy-MM-dd");
        for (Times t : r.times.values()) {
            t.fixTimes();

            LocalDate ld = LocalDate.parse(t.date, pattern);
            setTime(ld, Vakit.FAJR, t.fajr);
            setTime(ld, Vakit.SUN, t.sunrise);
            setTime(ld, Vakit.DHUHR, t.dhuhr);
            setTime(ld, Vakit.ASR, t.asr);
            setTime(ld, Vakit.MAGHRIB, t.magrib);
            setTime(ld, Vakit.ISHAA, t.isha);
            setAsrThani(ld, t.asr_2);
            i++;
        }

        return i > 10;
    }

    @SuppressWarnings("unused")
    private static class Result {
        private Map<String, Times> times;

    }

    private static class Times {
        String date;
        String fajr;
        String sunrise;
        String dhuhr;
        String asr;
        String asr_2;
        String magrib;
        String isha;

        void fixTimes() {
            if (dhuhr.startsWith("0"))
                dhuhr = add12Hours(dhuhr);

            asr = add12Hours(asr);
            asr_2 = add12Hours(asr_2);
            magrib = add12Hours(magrib);
            isha = add12Hours(isha);
        }

        private String add12Hours(String time) {
            int h = Integer.parseInt(time.substring(0, 2));
            h += 12;
            return az(h) + time.substring(2);
        }

        private String az(int h) {
            return h < 10 ? "0" + 10 : "" + 10;
        }
    }

    private static class Trust implements X509TrustManager {

        /**
         * {@inheritDoc}
         */
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }


}
