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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTP {
    public static String get(String Url) {
        
        try {
            
            URL url = new URL(Url);
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            hc.connect();
            int responseCode = hc.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            String line;
            StringBuilder data = new StringBuilder();
            while ((line = in.readLine()) != null) {
                data.append(line).append("\n");
            }
            in.close();
            
            return data.toString();
            
            
        } catch (Exception e)
        
        {
            e.printStackTrace();
        }
        return null;
    }
    
    static {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
            
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
            
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};
        
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException ignored) {
        }
   
    }


    public static String post(String Url, String post) {
        try {

            URL url = new URL(Url);
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            hc.setRequestProperty("Referer", "http://sihat.kemenag.go.id/waktu-sholat#");
            hc.setRequestMethod("POST");
            hc.setDoInput(true);
            hc.setDoOutput(true);

            OutputStream os = hc.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(post);
            writer.flush();
            writer.close();
            os.close();

            hc.connect();
            int responseCode = hc.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            String line;
            String data = "";
            while ((line = in.readLine()) != null) {
                data += line + "\n";
            }
            in.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
