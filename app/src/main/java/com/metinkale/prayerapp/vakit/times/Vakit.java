package com.metinkale.prayerapp.vakit.times;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;

public enum Vakit
{
    IMSAK(R.string.imsak, 0, "الإمساك"), SABAH(R.string.sabah, 0, "فجر"), GUNES(R.string.gunes, 1, "شروق"), OGLE(R.string.ogle, 2, "ظهر"), IKINDI(R.string.ikindi, 3, "عصر"), AKSAM(R.string.aksam, 4, "مغرب"), YATSI(R.string.yatsi, 5, "عشاء");

    public int index;

    private String arabic;
    private String name;
    private int resId;

    Vakit(int id, int index, String arabic)
    {
        resId = id;
        this.index = index;
        this.arabic = arabic;
    }

    public static Vakit getByIndex(int index)
    {
        switch(index)
        {
            case 0:
                if(Prefs.useArabic()) return SABAH;
                return IMSAK;
            case 1:
                return GUNES;
            case 2:
                return OGLE;
            case 3:
                return IKINDI;
            case 4:
                return AKSAM;
            case 5:
                return YATSI;
        }
        return IMSAK;
    }

    public String getString()
    {
        if(Prefs.useArabic()) return arabic;
        if(name == null) name = App.getContext().getString(resId);

        return name;
    }
}