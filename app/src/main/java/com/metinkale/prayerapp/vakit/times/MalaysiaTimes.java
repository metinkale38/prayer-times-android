package com.metinkale.prayerapp.vakit.times;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayerapp.App;

import org.joda.time.LocalDate;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by metin on 12.02.2017.
 */
public class MalaysiaTimes extends WebTimes {
    @SuppressWarnings("unused")
    MalaysiaTimes() {
        super();
    }

    MalaysiaTimes(long id) {
        super(id);
    }

    @Override
    public Source getSource() {
        return Source.Malaysia;
    }


    protected Builders.Any.F[] createIonBuilder() {
        LocalDate ldate = LocalDate.now();
        int rY = ldate.getYear();
        int Y = rY;
        int m = ldate.getMonthOfYear();
        String[] split = getId().split("_");
        if (split.length < 3) delete();
        split[1] = URLEncoder.encode(split[1]);
        split[2] = URLEncoder.encode(split[2]);

        final List<Builders.Any.B> queue = new ArrayList<>();
        for (int M = m; (M <= (m + 1)) && (rY == Y); M++) {
            if (M == 13) {
                M = 1;
                Y++;
            }
            queue.add(Ion.with(App.getContext())
                    .load("http://www.e-solat.gov.my/web/waktusolat.php?negeri=" + split[1] + "&state=" + split[1] + "&zone=" + split[2] + "&year="
                            + Y + "&jenis=year&bulan=" + M)
                    .setTimeout(3000)
            );
        }


        return queue.toArray(new Builders.Any.F[queue.size()]);
    }

    protected boolean parseResult(String result) {
        String date = result.substring(result.indexOf("muatturun"));
        date = date.substring(date.indexOf("year=") + 5);
        int year = Integer.parseInt(date.substring(0, 4));
        date = date.substring(date.indexOf("bulan") + 6);
        int month = Integer.parseInt(date.substring(0, date.indexOf("\"")));

        result = result.substring(result.indexOf("#7C7C7C"));
        result = result.substring(0, result.indexOf("<table"));
        int x = 0;
        FOR1:
        for (result = result.substring(result.indexOf("<tr", 1));
             result.contains("tr ");
             result = result.substring(result.indexOf("<tr", 1))) {
            try {
                result = result.substring(result.indexOf("<td") + 1);
                int day = Integer.parseInt(extract(result));
                result = result.substring(result.indexOf("<td") + 1);
                result = result.substring(result.indexOf("<td") + 1);
                String imsak = extract(result);
                result = result.substring(result.indexOf("<td") + 1);
                result = result.substring(result.indexOf("<td") + 1);
                String sun = extract(result);
                result = result.substring(result.indexOf("<td") + 1);
                String ogle = extract(result);
                result = result.substring(result.indexOf("<td") + 1);
                String ikindi = extract(result);
                result = result.substring(result.indexOf("<td") + 1);
                String aksam = extract(result);
                result = result.substring(result.indexOf("<td" + 1));
                String yatsi = extract(result);

                String[] array = new String[]{imsak, sun, ogle, ikindi, aksam, yatsi};
                for (int i = 0; i < array.length; i++) {
                    array[i] = array[i].replace(".", ":");
                    if (array[i].length() == 4) array[i] = "0" + array[i];
                    if (array[i].length() != 5) continue FOR1;
                }
                setTimes(new LocalDate(year, month, day), array);
                x++;
            } catch (Exception ignore) {
            }
        }

        return x > 25;
    }

    private String extract(String s) {
        return s.substring(s.indexOf(">"), s.indexOf("<")).trim();
    }
}
