package com.metinkale.prayerapp.vakit.times;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class NVCTimes extends WebTimes {

    NVCTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.NVC;
    }

    public static String getName(String id) {
        try {
            URL url = new URL("http://namazvakti.com/XML.php?cityID=" + id);
            URLConnection ucon = url.openConnection();

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);


            BufferedReader reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));

            String line;

            Calendar l = new GregorianCalendar();

            int y = l.get(Calendar.YEAR);
            while ((line = reader.readLine()) != null) if (line.contains("cityNameTR")) {
                line = line.substring(line.indexOf("cityNameTR"));
                line = line.substring(line.indexOf("\"") + 1);
                line = line.substring(0, line.indexOf("\""));
                return line;

            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @Override
    protected boolean syncTimes() throws Exception {

        URL url = new URL("http://namazvakti.com/XML.php?cityID=" + getId());
        URLConnection ucon = url.openConnection();

        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);


        BufferedReader reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));

        String line;

        Calendar l = new GregorianCalendar();

        int y = l.get(Calendar.YEAR);
        while ((line = reader.readLine()) != null) try {
            if (line.contains("<prayertimes")) {
                String doy = line.substring(line.indexOf("dayofyear=") + 11);
                String day = line.substring(line.indexOf("day=") + 5);
                String month = line.substring(line.indexOf("month=") + 7);
                doy = doy.substring(0, doy.indexOf("\""));
                day = day.substring(0, day.indexOf("\""));
                month = month.substring(0, month.indexOf("\""));
                if (day.length() == 1) day = "0" + day;
                if (month.length() == 1) month = "0" + month;
                String date;
                if ("0".equals(doy)) date = day + "." + month + "." + (y - 1);
                else if ("366".equals(doy) || ("367".equals(doy) && "01".equals(month)))
                    date = day + "." + month + "." + (y + 1);
                else date = day + "." + month + "." + y;
                String data = line.substring(line.indexOf(">") + 1, line.lastIndexOf("<"));
                data = data.replace("*", "").replace("\t", " ");
                List<String> d = new ArrayList<>(Arrays.asList(data.split(" ")));
                d.remove(15);
                d.remove(14);
                d.remove(13);
                d.remove(12);
                d.remove(10);
                d.remove(8);
                d.remove(7);
                d.remove(4);
                d.remove(3);
                d.remove(1);

                data = "";
                for (String s : d)
                    if (s.length() == 4) data += " 0" + s;
                    else data += " " + s;
                setTimes(Integer.parseInt(day), Integer.parseInt(month), y, data.substring(1).split(" "));

            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        return true;

    }


}
