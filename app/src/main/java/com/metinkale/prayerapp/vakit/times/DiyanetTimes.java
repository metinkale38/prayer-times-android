package com.metinkale.prayerapp.vakit.times;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiyanetTimes extends WebTimes {

    DiyanetTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Diyanet;
    }

    @Override
    protected boolean syncTimes() throws Exception {
        String path = getId();
        String a[] = path.split("_");
        int country = Integer.parseInt(a[1]);
        int state = Integer.parseInt(a[2]);
        int city = 0;
        if (a.length == 4)
            city = Integer.parseInt(a[3]);

        URL url = new URL("http://www.diyanet.gov.tr/PrayerTime/PrayerTimesList");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("Country", country);
        params.put("State", state);
        params.put("City", city);
        params.put("period", "Aylik");

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Size", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        OutputStream writer = conn.getOutputStream();
        writer.write(postDataBytes);

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        int i = 0;
        int d = 0, m = 0, y = 0;
        String[] times = new String[6];
        while ((line = reader.readLine()) != null) {
            if (line.contains("class=\"tCenter\"")) {
                line = extractLine(line);
                if (line.contains(".")) {
                    i = 0;
                    String s[] = line.split("\\.");
                    d = Integer.parseInt(s[0]);
                    m = Integer.parseInt(s[1]);
                    y = Integer.parseInt(s[2]);
                } else if (i <= 5) {
                    times[i] = line;

                    if (i == 5)
                        setTimes(d, m, y, times);
                    i++;
                }
            }
        }

        reader.close();
        writer.close();

        return true;
    }


}
