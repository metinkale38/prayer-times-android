package com.metinkale.prayerapp.vakit.times;

import com.metinkale.prayerapp.App;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class NVCTimes extends WebTimes {

    NVCTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.NVC;
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
        while ((line = reader.readLine()) != null)
            try {
                if (line.contains("cityNameTR")) {
                    line = line.substring(line.indexOf("cityNameTr"));
                    line = line.substring(line.indexOf("\"") + 1);
                    line = line.substring(0, line.indexOf("\""));
                    setName(line);

                }
                if (line.contains("<prayertimes")) {
                    String doy = line.substring(line.indexOf("dayofyear=") + 11);
                    String day = line.substring(line.indexOf("day=") + 5);
                    String month = line.substring(line.indexOf("month=") + 7);
                    doy = doy.substring(0, doy.indexOf("\""));
                    day = day.substring(0, day.indexOf("\""));
                    month = month.substring(0, month.indexOf("\""));
                    if (day.length() == 1)
                        day = "0" + day;
                    if (month.length() == 1)
                        month = "0" + month;
                    String date;
                    if (doy.equals("0"))
                        date = day + "." + month + "." + (y - 1);
                    else if (doy.equals("366") || doy.equals("367") && month.equals("01"))
                        date = day + "." + month + "." + (y + 1);
                    else
                        date = day + "." + month + "." + y;
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
                        if (s.length() == 4)
                            data += " 0" + s;
                        else
                            data += " " + s;
                    setTimes(Integer.parseInt(day), Integer.parseInt(month), y, data.substring(1).split(" "));

                }
            } catch (Exception e) {
            }

        return true;

    }


}
