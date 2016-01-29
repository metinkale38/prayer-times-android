package com.metinkale.prayerapp;

import com.metinkale.prayerapp.settings.Prefs;

import java.util.*;

public class Date
{
    private static final Calendar CAL = new GregorianCalendar();

    private static final String[] ASSETS = new String[]{"/dinigunler/hicriyil.html", "/dinigunler/asure.html", "/dinigunler/mevlid.html", "/dinigunler/3aylar.html", "/dinigunler/regaib.html", "/dinigunler/mirac.html", "/dinigunler/berat.html", "/dinigunler/ramazan.html", "/dinigunler/kadir.html", "/dinigunler/arefe.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/ramazanbay.html", "/dinigunler/arefe.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html", "/dinigunler/kurban.html"};

    private int mHD, mHM, mHY, mGD, mGM, mGY, mWD;

    public Date()
    {
        this(Calendar.getInstance());
    }

    public Date(Calendar cal)
    {
        init(cal.get(Calendar.DATE), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), false);

    }

    private Date(int day, int month, int year, boolean hicri)
    {
        init(day, month, year, hicri);
    }

    public static String isHolyday()
    {
        Date today = new Date();
        Map<Date, String> map = getHolydays(today.getGregYear(), false);
        if(map.containsKey(today))
        {
            return map.get(today);
        } else
        {
            return null;
        }
    }

    public static Map<Date, String> getHolydays(int year, boolean hicri)
    {
        return hicri ? getHolydaysForHicriYear(year) : getHolydaysForGregYear(year);
    }

    private static LinkedHashMap<Date, String> getHolydaysForGregYear(int year)
    {
        LinkedHashMap<Date, String> map = new LinkedHashMap<>();
        List<DiyanetTakvimi.DATE> diy = DiyanetTakvimi.get().getHolydays(year);

        if(!diy.isEmpty())
        {
            for(DiyanetTakvimi.DATE d : diy)
            {
                map.put(new Date(d.grg[0], d.grg[1], d.grg[2], false), Utils.getHolyday(d.day - 1));
            }

            return map;
        }

        int y = new Date(1, 1, year, false).getHicriYear();
        HashMap<Date, String> y1 = getHolydaysForHicriYear(y);
        HashMap<Date, String> y2 = getHolydaysForHicriYear(y + 1);

        for(Date h : y1.keySet())
        {
            if(h.getGregYear() == year)
            {
                map.put(h, y1.get(h));
            }
        }

        for(Date h : y2.keySet())
        {
            if(h.getGregYear() == year)
            {
                map.put(h, y2.get(h));
            }
        }
        return map;

    }

    private static LinkedHashMap<Date, String> getHolydaysForHicriYear(int y)
    {
        LinkedHashMap<Date, String> map = new LinkedHashMap<>();

        map.put(new Date(1, 1, y, true), Utils.getHolyday(0));
        map.put(new Date(10, 1, y, true), Utils.getHolyday(1));
        map.put(new Date(11, 3, y, true), Utils.getHolyday(2));
        map.put(new Date(1, 7, y, true), Utils.getHolyday(3));

        Date h = new Date(1, 7, y, true);
        if(h.getWeekDay() <= 3)
        {
            h.setHicriDay(h.getHicriDay() + 3 - h.getWeekDay());
        } else
        {
            h.setHicriDay(h.getHicriDay() + 10 - h.getWeekDay());
        }
        map.put(h, Utils.getHolyday(4));

        map.put(new Date(26, 7, y, true), Utils.getHolyday(5));
        map.put(new Date(14, 8, y, true), Utils.getHolyday(6));
        map.put(new Date(1, 9, y, true), Utils.getHolyday(7));
        map.put(new Date(26, 9, y, true), Utils.getHolyday(8));
        map.put(new Date(30, 9, y, true), Utils.getHolyday(9));
        map.put(new Date(1, 10, y, true), Utils.getHolyday(10));
        map.put(new Date(2, 10, y, true), Utils.getHolyday(11));
        map.put(new Date(3, 10, y, true), Utils.getHolyday(12));
        map.put(new Date(9, 12, y, true), Utils.getHolyday(13));
        map.put(new Date(10, 12, y, true), Utils.getHolyday(14));
        map.put(new Date(11, 12, y, true), Utils.getHolyday(15));
        map.put(new Date(12, 12, y, true), Utils.getHolyday(16));
        map.put(new Date(13, 12, y, true), Utils.getHolyday(17));

        return map;

    }

    private static String formatInt(int Int, int num)
    {
        String ret = Int + "";
        if(ret.length() < num)
        {
            for(int i = ret.length(); i < num; i++)
            {
                ret = "0" + ret;
            }
        } else if(ret.length() > num)
        {
            ret = ret.substring(ret.length() - num, ret.length());
        }

        return ret;
    }

    public static String getAssetForHolyday(int year, int pos, boolean hicri)
    {
        if(hicri)
        {
            return Prefs.getLanguage() + ASSETS[pos];
        } else
        {
            AbstractMap<Date, String> map = Date.getHolydaysForGregYear(year);

            AbstractList<String> set = new ArrayList<>(map.values());

            return Prefs.getLanguage() + ASSETS[Arrays.asList(Utils.getAllHolydays()).indexOf(set.get(pos))];

        }
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    private void init(int day, int month, int year, boolean hicri)
    {
        if(hicri)
        {
            mHD = day;
            mHM = month;
            mHY = year;
            calcGreg();
        } else
        {
            mGD = day;
            mGM = month;
            mGY = year;

            int[] res = DiyanetTakvimi.get().toHicri(day, month, year);
            if(res == null)
            {
                calcHicri();
            } else
            {
                mHD = res[0];
                mHM = res[1];
                mHY = res[2];
            }
        }

        CAL.set(mGY, mGM - 1, mGD);
        mWD = CAL.get(Calendar.DAY_OF_WEEK);

    }

    int getWeekDay()
    {
        return mWD;
    }

    int getHicriDay()
    {
        return mHD;
    }

    void setHicriDay(int d)
    {
        init(d, mHM, mHY, true);
    }

    public int getHicriMonth()
    {
        return mHM;
    }

    public void setHicriMonth(int m)
    {
        init(mHD, m, mHY, true);
    }

    int getHicriYear()
    {
        return mHY;
    }

    public void setHicriYear(int y)
    {
        init(mHD, mHM, y, true);
    }

    public int getGregDay()
    {
        return mGD;
    }

    public void setGregDay(int d)
    {
        init(d, mGM, mGY, false);
    }

    public int getGregMonth()
    {
        return mGM;
    }

    public void setGregMonth(int m)
    {
        init(mGD, m, mGY, false);
    }

    public int getGregYear()
    {
        return mGY;
    }

    public void setGregYear(int y)
    {
        init(mGD, mGM, y, false);
    }

    private double intPart(double floatNum)
    {
        if(floatNum < -0.0000001)
        {
            return Math.ceil(floatNum - 0.0000001);
        }
        return Math.floor(floatNum + 0.0000001);
    }

    private void calcHicri()
    {
        double d = mGD;
        double m = mGM;
        double y = mGY;
        double jd;
        if(y > 1582 || y == 1582 && m > 10 || y == 1582 && m == 10 && d > 14)
        {
            jd = intPart(1461 * (y + 4800 + intPart((m - 14) / 12)) / 4) + intPart(367 * (m - 2 - 12 * intPart((m - 14) / 12)) / 12) - intPart(3 * intPart((y + 4900 + intPart((m - 14) / 12)) / 100) / 4) + d - 32075;
        } else
        {
            jd = 367 * y - intPart(7 * (y + 5001 + intPart((m - 9) / 7)) / 4) + intPart(275 * m / 9) + d + 1729777;
        }

        double l = jd - 1948440 + 10632;
        double n = intPart((l - 1) / 10631);
        l = l - 10631 * n + 354;
        double j = intPart((10985 - l) / 5316) * intPart(50 * l / 17719) + intPart(l / 5670) * intPart(43 * l / 15238);
        l = l - intPart((30 - j) / 15) * intPart(17719 * j / 50) - intPart(j / 16) * intPart(15238 * j / 43) + 29;
        m = intPart(24 * l / 709);
        d = l - intPart(709 * m / 24);
        y = 30 * n + j - 30;

        mHD = (int) d;
        mHM = (int) m;
        mHY = (int) y;
    }

    private void calcGreg()
    {
        double d = mHD;
        double m = mHM;
        double y = mHY;

        double jd = intPart((11 * y + 3) / 30) + 354 * y + 30 * m - intPart((m - 1) / 2) + d + 1948440 - 385;
        double l, n, i, j, k;
        if(jd > 2299160)
        {
            l = jd + 68569;
            n = intPart(4 * l / 146097);
            l = l - intPart((146097 * n + 3) / 4);
            i = intPart(4000 * (l + 1) / 1461001);
            l = l - intPart(1461 * i / 4) + 31;
            j = intPart(80 * l / 2447);
            d = l - intPart(2447 * j / 80);
            l = intPart(j / 11);
            m = j + 2 - 12 * l;
            y = 100 * (n - 49) + i + l;
        } else
        {
            j = jd + 1402;
            k = intPart((j - 1) / 1461);
            l = j - 1461 * k;
            n = intPart((l - 1) / 365) - intPart(l / 1461);
            i = l - 365 * n + 30;
            j = intPart(80 * i / 2447);
            d = i - intPart(2447 * j / 80);
            i = intPart(j / 11);
            m = j + 2 - 12 * i;
            y = 4 * k + n + i - 4716;
        }

        mGD = (int) d;
        mGM = (int) m;
        mGY = (int) y;
    }

    @Override
    public String toString()
    {
        return "Gregorian:" + mGD + "/" + mGM + "/" + mGY + "|Hicri:" + mHD + "/" + mHM + "/" + mHY + "|" + mWD;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof Date)
        {
            Date h = (Date) o;
            if(h.mGD == mGD && h.mGM == mGM && h.mGY == mGY)
            {
                return true;
            }
        }
        return false;
    }

    public String format(boolean hicri)
    {
        return format(Utils.getDateFormat(hicri), hicri);
    }

    public String format(String format, boolean hicri)
    {
        int d, m, y;

        if(hicri)
        {
            d = mHD;
            m = mHM;
            y = mHY;
        } else
        {
            d = mGD;
            m = mGM;
            y = mGY;
        }
        String date = format;
        date = date.replace("EE", Utils.getWeekday(mWD - 1));
        date = date.replace("E", Utils.getShortWeekday(mWD - 1));
        date = date.replace("DD", formatInt(d, 2));

        if(m == 0 && !hicri)
        {
            calcGreg();
            m = mGM;
        } else if(m == 0)
        {
            calcHicri();
            m = mHM;
        }

        try
        {
            if(hicri)
            {
                date = date.replace("MMM", Utils.getHijriMonth(m - 1));
            } else
            {
                date = date.replace("MMM", Utils.getGregMonth(m - 1));
            }
        } catch(ArrayIndexOutOfBoundsException ex)
        {
            if(hicri)
            {
                calcHicri();
            } else
            {
                calcGreg();
            }

            return format(format, hicri);
        }
        date = date.replace("MM", formatInt(m, 2));
        date = date.replace("YYYY", formatInt(y, 4));
        date = date.replace("YY", formatInt(y, 2));

        return date;

    }
}
