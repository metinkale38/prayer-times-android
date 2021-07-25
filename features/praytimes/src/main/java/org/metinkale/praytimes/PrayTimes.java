/*
PrayTimes-Java: Prayer Times Java Calculator (ver 0.9)

Copyright (C) 2007-2011 PrayTimes.org (JS Code ver 2.3)
Copyright (C) 2017 Metin Kale (Java Code)

Developer JS: Hamid Zarrabi-Zadeh
Developer Java: Metin Kale

License: GNU LGPL v3.0

TERMS OF USE:
	Permission is granted to use this code, with or
	without modification, in any website or application
	provided that credit is given to the original work
	with a link back to PrayTimes.org.

This program is distributed in the hope that it will
be useful, but WITHOUT ANY WARRANTY.

PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.

*/
package org.metinkale.praytimes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;


@SuppressWarnings({"WeakerAccess", "unused"})
public class PrayTimes implements Serializable {
    // constants are at the bottom


    private double lat, lng, elv;
    private HighLatsAdjustment highLats;

    private Midnight midnight;
    private TimeZone timeZone = TimeZone.getDefault();


    private int[] minutes = new int[Times.values().length];
    private double[] angles = new double[Times.values().length];

    private transient double jdate;
    private transient int year;
    private transient int month;
    private transient int day;
    private transient long timestamp;
    private transient double[] times;
    private transient String[] strTimes;

    public PrayTimes() {
        setMethod(Method.MWL);
    }

    /**
     * convert Gregorian date to Julian day
     * Ref: Astronomical Algorithms by Jean Meeus
     *
     * @param year  year
     * @param month month
     * @param day   day
     * @return julian day
     */
    private static double julian(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double a = Math.floor(year / 100.0);
        double b = (2 - a + Math.floor(a / 4.0));

        return (Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524.5);
    }

    private void clearTimes() {
        times = null;
        strTimes = null;
    }

    /**
     * set coordinates
     *
     * @param lat Latitude
     * @param lng Longitute
     * @param elv Elevation
     */
    public void setCoordinates(double lat, double lng, double elv) {
        this.lat = lat;
        this.lng = lng;
        this.elv = elv;
        clearTimes();
    }


    /**
     * set date
     *
     * @param year  Year (e.g. 2017)
     * @param month Month (1-12)
     * @param day   Date/Day of Month
     */
    public void setDate(int year, int month, int day) {
        if (this.year == year && this.month == month && this.day == day)
            return;
        this.year = year;
        this.month = month;
        this.day = day;

        clearTimes();
    }

    /**
     * return prayer time for a given date and time
     *
     * @param t time from Constants
     * @return array of Times
     */
    public String getTime(Times t) {
        calculate();
        return strTimes[t.ordinal()];
    }


    /**
     * calculate prayer times for a given date
     */
    private void calculate() {
        if (times != null && strTimes != null)
            return;

        Calendar cal = timeZone != null ? Calendar.getInstance(timeZone) : Calendar.getInstance();
        cal.set(year, month - 1, day);
        timestamp = cal.getTimeInMillis();
        jdate = julian(year, month, day) - lng / (15.0 * 24.0);


        // simple guess
        times = new double[]{5, 5, 6, 12, 12, 13, 13, 18, 18, 18, 0};

        // convert to day portions
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] / 24.0;
        }

        // first all angle based calculations are done

        //Imsak: if angle is 0, use sunrise, given angle otherwhise (if angle is 0, calculation is probably minute based)
        times[Times.Imsak.ordinal()] =
                sunAngleTime(angles[Times.Imsak.ordinal()] == 0 ? riseSetAngle() : angles[Times.Imsak.ordinal()], times[Times.Imsak.ordinal()], true);
        //Fajr: if angle is 0, use sunrise, angle otherwhise (if angle is 0, calculation is probably minute based)
        times[Times.Fajr.ordinal()] =
                sunAngleTime(angles[Times.Fajr.ordinal()] == 0 ? riseSetAngle() : angles[Times.Fajr.ordinal()], times[Times.Fajr.ordinal()], true);
        // Sunrise: fix calculation
        times[Times.Sunrise.ordinal()] = sunAngleTime(riseSetAngle(), times[Times.Sunrise.ordinal()], true);
        // Zawal: fix calculation
        times[Times.Zawal.ordinal()] = midDay(times[Times.Zawal.ordinal()]);
        // Dhuhr: fix calculation
        times[Times.Dhuhr.ordinal()] = midDay(times[Times.Dhuhr.ordinal()]);
        // Asr Shafi: fix calculation, shadow factor 1
        times[Times.AsrShafi.ordinal()] = asrTime(1, times[Times.AsrShafi.ordinal()]);
        // Asr Shafi: fix calculation, shadow factor 2
        times[Times.AsrHanafi.ordinal()] = asrTime(2, times[Times.AsrHanafi.ordinal()]);
        // Sunset: fix calculation
        times[Times.Sunset.ordinal()] = sunAngleTime(riseSetAngle(), times[Times.Sunset.ordinal()], false);
        // Maghrib: if angle is 0, use sunset, given angle otherwhise
        times[Times.Maghrib.ordinal()] =
                sunAngleTime(angles[Times.Maghrib.ordinal()] == 0 ? riseSetAngle() : angles[Times.Maghrib.ordinal()], times[Times.Maghrib.ordinal()],
                        false);
        // Ishaa: if angle is 0, use sunset, given angle otherwhise (if angle is 0, calculation is probably minute based)
        times[Times.Ishaa.ordinal()] =
                sunAngleTime(angles[Times.Ishaa.ordinal()] == 0 ? riseSetAngle() : angles[Times.Ishaa.ordinal()], times[Times.Ishaa.ordinal()],
                        false);
        // midnight will be calculated later
        // times[Times.Midnight.ordinal()] = 0;


        if (highLats != HighLatsAdjustment.None) {
            double nightTime = Utils.timeDiff(times[Times.Sunset.ordinal()], times[Times.Sunrise.ordinal()]);

            times[Times.Imsak.ordinal()] =
                    adjustHLTime(times[Times.Imsak.ordinal()], times[Times.Sunrise.ordinal()], angles[Times.Imsak.ordinal()], nightTime, true);
            times[Times.Fajr.ordinal()] =
                    adjustHLTime(times[Times.Fajr.ordinal()], times[Times.Sunrise.ordinal()], angles[Times.Fajr.ordinal()], nightTime, true);
            times[Times.Ishaa.ordinal()] =
                    adjustHLTime(times[Times.Ishaa.ordinal()], times[Times.Sunset.ordinal()], angles[Times.Ishaa.ordinal()], nightTime, false);
            times[Times.Maghrib.ordinal()] =
                    adjustHLTime(times[Times.Maghrib.ordinal()], times[Times.Sunset.ordinal()], angles[Times.Maghrib.ordinal()], nightTime, false);
        }


        // add midnight time
        if (midnight == Midnight.Standard) {
            times[Times.Midnight.ordinal()] =
                    times[Times.Sunset.ordinal()] + Utils.timeDiff(times[Times.Sunset.ordinal()], times[Times.Sunrise.ordinal()]) / 2.0;
        } else if (midnight == Midnight.Jafari) {
            times[Times.Midnight.ordinal()] =
                    times[Times.Sunset.ordinal()] + Utils.timeDiff(times[Times.Sunset.ordinal()], times[Times.Fajr.ordinal()]) / 2.0;
        }


        //  add minute offset and adjust timezone
        double offset = getTimeZoneOffset();
        for (int i = 0; i < times.length; i++) {
            times[i] += minutes[i] / 60.0;
            times[i] += offset - lng / 15.0;

            while (times[i] > 24) {
                times[i] -= 24;
            }
            while (times[i] < 0) {
                times[i] += 24;
            }
        }


        // convert to string
        strTimes = new String[times.length];
        for (int i = 0; i < times.length; i++) {
            strTimes[i] = Utils.toString(times[i]);
        }
    }

    /**
     * adjust a time for higher latitudes
     * <p>
     * how it works:
     * dependend on the calculation method there is a maximum night portion, that might be used from time to base
     * <p>
     * (e.g.) if NightMiddle is taken and the calculated fajr takes more than the half night, it will be fixed to midnight
     *
     * @param time  time
     * @param base  base
     * @param angle angle
     * @param night night time
     * @param ccw   true if clock-counter-wise, false otherwise
     * @return adjusted time
     */
    private double adjustHLTime(double time, double base, double angle, double night, boolean ccw) {
        double maxPortion = maxNightPortion(angle);
        double maxDuration = maxPortion * night;

        double timeDiff = (ccw) ? Utils.timeDiff(time, base) : Utils.timeDiff(base, time);
        if (Double.isNaN(time) || timeDiff > maxDuration)
            time = base + (ccw ? -maxDuration : maxDuration);
        return time;
    }

    /**
     * the maximum night portion used for adjusting times in higher latitudes
     *
     * @param angle angle
     * @return night portion
     */
    private double maxNightPortion(double angle) {
        double portion = 0;
        switch (highLats) {
            case None:
                portion = 1;
                break;
            case AngleBased:
                portion = 1.0 / 60.0 * angle;
                break;
            case OneSeventh:
                portion = 1.0 / 7.0;
                break;
            case NightMiddle:
                portion = 1.0 / 2.0;
                break;
        }
        return portion;
    }

    /**
     * Calculates the qibla time, if you turn yourself to the sun at that time, you are turned to qibla
     * Note: does not exists everywhere
     *
     * @return Qibla Time
     */
    public QiblaTime getQiblaTime() {
        calculate();
        Calendar cal = Calendar.getInstance(timeZone);
        //noinspection MagicConstant
        cal.set(year, month - 1, day, 12, 0, 0);
        long[] qibla = new long[4];
        qibla[0] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, 0);
        qibla[1] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, Math.PI / 2);
        qibla[2] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, -Math.PI / 2);
        qibla[3] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, Math.PI);
        double[] qiblaD = new double[4];
        String[] qiblaS = new String[4];
        double sunrise = times[Times.Sunrise.ordinal()];
        double sunset = times[Times.Sunset.ordinal()];

        for (int i = 0; i < 4; i++) {
            cal.setTimeInMillis(qibla[i]);
            qiblaD[i] = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60d + cal.get(Calendar.SECOND) / 3600d;
            if (qiblaD[i] < sunrise || qiblaD[i] > sunset) {
                qiblaD[i] = 0;
                qiblaS[i] = null;
            } else {
                qiblaS[i] = Utils.toString(qiblaD[i]);
            }


        }
        QiblaTime qt = new QiblaTime();
        qt.front = qiblaS[0];
        qt.right = qiblaS[1];
        qt.left = qiblaS[2];
        qt.back = qiblaS[3];

        return qt;
    }


    /**
     * compute asr time
     *
     * @param factor Shadow Factor
     * @param time   default  time
     * @return asr time
     */
    private double asrTime(int factor, double time) {
        double decl = this.sunPositionDeclination(jdate + time);
        double angle = -DMath.arccot(factor + DMath.tan(Math.abs(lat - decl)));
        return this.sunAngleTime(angle, time, false);
    }


    /**
     * compute the time at which sun reaches a specific angle below horizon
     *
     * @param angle angle
     * @param time  default time
     * @param ccw   true if counter-clock-wise, false otherwise
     * @return time
     */
    private double sunAngleTime(double angle, double time, boolean ccw) {
        double decl = this.sunPositionDeclination(jdate + time);
        double noon = this.midDay(time);
        double t = 1.0 / 15.0 * DMath.arccos((-DMath.sin(angle) - DMath.sin(decl) * DMath.sin(lat)) / (DMath.cos(decl) * DMath.cos(lat)));
        return noon + (ccw ? -t : t);
    }

    /**
     * compute mid-day time
     *
     * @param time default time
     * @return midday time
     */
    private double midDay(double time) {
        double eqt = this.equationOfTime(jdate + time);
        return DMath.fixHour(12 - eqt);
    }

    /**
     * compute equation of time
     * Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
     *
     * @param jd julian date
     * @return equation of time
     */
    private double equationOfTime(double jd) {
        double d = jd - 2451545.0;
        double g = DMath.fixAngle(357.529 + 0.98560028 * d);
        double q = DMath.fixAngle(280.459 + 0.98564736 * d);
        double l = DMath.fixAngle(q + 1.915 * DMath.sin(g) + 0.020 * DMath.sin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        double ra = DMath.arctan2(DMath.cos(e) * DMath.sin(l), DMath.cos(l)) / 15;
        return q / 15.0 - DMath.fixHour(ra);
    }

    /**
     * compute  declination angle of sun
     * Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
     *
     * @param jd julian date
     * @return declination angle of sun
     */
    private double sunPositionDeclination(double jd) {
        double d = jd - 2451545.0;
        double g = DMath.fixAngle(357.529 + 0.98560028 * d);
        double q = DMath.fixAngle(280.459 + 0.98564736 * d);
        double l = DMath.fixAngle(q + 1.915 * DMath.sin(g) + 0.020 * DMath.sin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        return DMath.arcsin(DMath.sin(e) * DMath.sin(l));
    }


    /**
     * compute sun angle for sunset/sunrise
     *
     * @return sun angle of sunset/sunrise
     */
    private double riseSetAngle() {
        //double earthRad = 6371009; // in meters
        //double angle = DMath.arccos(earthRad/(earthRad+ elv));
        double angle = 0.0347 * Math.sqrt(elv); // an approximation
        return 0.833 + angle;
    }


    /**
     * Sets the calculation method
     * Attention: overrides all other parameters, set this as first
     * Default: MWL
     *
     * @param method calculation method
     */
    public void setMethod(Method method) {
        Arrays.fill(angles, 0);
        Arrays.fill(minutes, 0);
        highLats = HighLatsAdjustment.None;
        midnight = Midnight.Standard;
        method.set(this);
        clearTimes();
    }

    /**
     * tunes Imsak value
     * if angle is 0, sunrise + minute is used, given angle + minute otherwhise
     *
     * @param angle  angle
     * @param minute minute
     */
    public void tuneImsak(double angle, int minute) {
        angles[Times.Imsak.ordinal()] = angle;
        minutes[Times.Imsak.ordinal()] = minute;
        clearTimes();
    }


    /**
     * tunes Fajr value
     * if angle is 0, sunrise + minute is used, given angle + minute otherwhise
     *
     * @param angle  angle
     * @param minute minute
     */
    public void tuneFajr(double angle, int minute) {
        angles[Times.Fajr.ordinal()] = angle;
        minutes[Times.Fajr.ordinal()] = minute;
        clearTimes();
    }

    /**
     * tunes Sunrise value
     * sunrise has a fix calculation, we can only shift minutes
     *
     * @param minute minute
     */
    public void tuneSunrise(int minute) {
        minutes[Times.Sunrise.ordinal()] = minute;
        angles[Times.Sunrise.ordinal()] = 0;
        clearTimes();
    }

    /**
     * tunes Dhuhr value
     * dhuhr has a fix calculation (see Zawal), we can only shift minutes
     *
     * @param minute minute
     */
    public void tuneDhuhr(int minute) {
        minutes[Times.Dhuhr.ordinal()] = minute;
        angles[Times.Dhuhr.ordinal()] = 0;
        clearTimes();
    }


    /**
     * tunes Asr value
     * Asr has a fix calculation, we can only shift minutes
     *
     * @param minute minute
     */
    public void tuneAsrShafi(int minute) {
        minutes[Times.AsrShafi.ordinal()] = minute;
        angles[Times.AsrShafi.ordinal()] = 0;
        clearTimes();
    }

    /**
     * tunes Asr value
     * Asr has a fix calculation, we can only shift minutes
     *
     * @param minute minute
     */
    public void tuneAsrHanafi(int minute) {
        minutes[Times.AsrHanafi.ordinal()] = minute;
        angles[Times.AsrHanafi.ordinal()] = 0;
        clearTimes();
    }

    /**
     * tunes sunset value
     * sunset has a fix calculation, we can only shift minutes
     *
     * @param minute minute
     */
    public void tuneSunset(int minute) {
        minutes[Times.Sunset.ordinal()] = minute;
        angles[Times.Sunset.ordinal()] = 0;
        clearTimes();
    }


    /**
     * tunes Maghrib value
     * if angle is 0, sunset + minute is used, given angle + minute otherwhise
     *
     * @param angle  angle
     * @param minute minute
     */
    public void tuneMaghrib(double angle, int minute) {
        angles[Times.Maghrib.ordinal()] = angle;
        minutes[Times.Maghrib.ordinal()] = minute;
        clearTimes();
    }


    /**
     * tunes Ishaa value
     * if angle is 0, sunset + minute is used, given angle + minute otherwhise
     *
     * @param angle  angle
     * @param minute minute
     */
    public void tuneIshaa(double angle, int minute) {
        angles[Times.Ishaa.ordinal()] = angle;
        minutes[Times.Ishaa.ordinal()] = minute;
        clearTimes();
    }


    public double getImsakAngle() {
        return angles[Times.Imsak.ordinal()];
    }


    public double getFajrAngle() {
        return angles[Times.Fajr.ordinal()];
    }


    public double getMaghribAngle() {
        return angles[Times.Maghrib.ordinal()];
    }

    public double getIshaaAngle() {
        return angles[Times.Ishaa.ordinal()];
    }


    public int getImsakMinuteAdjust() {
        return minutes[Times.Imsak.ordinal()];
    }

    public int getFajrMinuteAdjust() {
        return minutes[Times.Fajr.ordinal()];
    }

    public int getSunriseMinuteAdjust() {
        return minutes[Times.Sunrise.ordinal()];
    }

    public int getDhuhrMinuteAdjust() {
        return minutes[Times.Dhuhr.ordinal()];
    }

    public int getAsrHanafiMinuteAdjust() {
        return minutes[Times.AsrHanafi.ordinal()];
    }

    public int getAsrShafiMinuteAdjust() {
        return minutes[Times.AsrShafi.ordinal()];
    }

    public int getMaghribMinuteAdjust() {
        return minutes[Times.Maghrib.ordinal()];
    }

    public int getIshaaMinuteAdjust() {
        return minutes[Times.Ishaa.ordinal()];
    }


    /**
     * Midnight is generally calculated as the mean time from Sunset to Sunrise, i.e., Midnight = 1/2(Sunrise - Sunset).
     * In Shia point of view, the juridical midnight (the ending time for performing Isha prayer) is the mean time
     * from Sunset to Fajr, i.e., Midnight = 1/2(Fajr - Sunset).
     * <p>
     * {@link Midnight#Standard Standard} (Default)
     * {@link Midnight#Jafari Jafari}
     *
     * @param mode mode
     */
    public void setMidnightMode(Midnight mode) {
        midnight = mode;
        clearTimes();

    }

    /**
     * TimeZone for times
     * <p>
     * Default: {@link TimeZone#getDefault() TimeZone.getDefault()}
     *
     * @param tz Timezone
     */
    public void setTimezone(TimeZone tz) {
        timeZone = tz;
        clearTimes();
    }

    /**
     * get Timezone offset for specific date
     *
     * @return time zone offset
     */
    private double getTimeZoneOffset() {
        if (timeZone == null) return 0;
        return timeZone.getOffset(timestamp) / 1000.0 / 60 / 60;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public HighLatsAdjustment getHighLatsAdjustment() {
        return highLats;
    }

    /**
     * In locations at higher latitude, twilight may persist throughout the night during some months of the year.
     * In these abnormal periods, the determination of Fajr and Isha is not possible using the usual formulas mentioned
     * in the previous section. To overcome this problem, several solutions have been proposed,
     * three of which are described below.
     * <p>
     * {@link HighLatsAdjustment#None None} (Default, see notes)
     * {@link HighLatsAdjustment#NightMiddle NightMiddle}
     * {@link HighLatsAdjustment#OneSeventh OneSeventh}
     * {@link HighLatsAdjustment#AngleBased AngleBased}
     *
     * @param method method
     */
    public void setHighLatsAdjustment(HighLatsAdjustment method) {
        highLats = method;
        clearTimes();

    }

    public double getLatitude() {
        return lat;
    }


    public double getLongitude() {
        return lng;
    }


    public double getElevation() {
        return elv;
    }


}


