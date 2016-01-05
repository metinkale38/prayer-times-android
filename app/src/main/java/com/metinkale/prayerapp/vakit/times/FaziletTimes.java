package com.metinkale.prayerapp.vakit.times;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

public class FaziletTimes extends WebTimes {

    FaziletTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Fazilet;
    }

    @Override
    protected boolean syncTimes() throws Exception {
        String path = getId().replace("nix", "-1");
        String a[] = path.split("_");
        int country = Integer.parseInt(a[1]);
        int state = Integer.parseInt(a[2]);

        List<String> ay = new ArrayList<String>();
        ay.add("Ocak");
        ay.add("Şubat");
        ay.add("Mart");
        ay.add("Nisan");
        ay.add("Mayıs");
        ay.add("Haziran");
        ay.add("Temmuz");
        ay.add("Ağustos");
        ay.add("Eylül");
        ay.add("Ekim");
        ay.add("Kasım");
        ay.add("Aralık");

        Calendar cal = Calendar.getInstance();
        int Y = cal.get(Calendar.YEAR);
        int M = cal.get(Calendar.MONTH) + 1;


        URL url = new URL("http://www.fazilettakvimi.com/tr/namaz_vakitleri.html");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ulke_id", country);
        params.put("sehir_id", state);
        params.put("baslangic_tarihi", Y + "-" + az(M) + "-01");
        params.put("bitis_tarihi", (Y + 5) + "-12-31");

        StringBuilder postData = new StringBuilder();
        for (
                Map.Entry<String, Object> param
                : params.entrySet())

        {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }

        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        OutputStream writer = conn.getOutputStream();
        writer.write(postDataBytes);

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        int i = 0;
        while ((line = reader.readLine()) != null)

        {
            if (line.contains("<tr class=\"acik\">") || line.contains("<tr class=\"koyu\">")) {
                String date = extractLine(reader.readLine());
                String[] dd = date.split(" ");
                int d = Integer.parseInt(dd[0]);
                int m = ay.indexOf(dd[1]) + 1;
                int y = Integer.parseInt(dd[2]);
                String times[] = new String[6];

                reader.readLine();//0
                reader.readLine();//1
                times[0] = extractLine(reader.readLine());//2
                reader.readLine();//3
                times[1] = extractLine(reader.readLine());//4
                reader.readLine();//5
                times[2] = extractLine(reader.readLine());//6
                times[3] = extractLine(reader.readLine());//7
                reader.readLine();//8
                times[4] = extractLine(reader.readLine());//9
                times[5] = extractLine(reader.readLine());//10
                setTimes(d, m, y, times);

            }

        }
        return true;

    }
}