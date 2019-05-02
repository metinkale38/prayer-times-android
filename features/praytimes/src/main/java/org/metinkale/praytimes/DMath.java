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

/**
 * Degree-Based Math Class
 */
@SuppressWarnings("WeakerAccess")
class DMath {
    public static double dtr(double d) {
        return (d * Math.PI) / 180.0;
    }

    public static double rtd(double r) {
        return (r * 180.0) / Math.PI;
    }

    public static double sin(double d) {
        return Math.sin(dtr(d));
    }

    public static double cos(double d) {
        return Math.cos(dtr(d));
    }

    public static double tan(double d) {
        return Math.tan(dtr(d));
    }

    public static double arcsin(double d) {
        return rtd(Math.asin(d));
    }

    public static double arccos(double d) {
        return rtd(Math.acos(d));
    }

    public static double arctan(double d) {
        return rtd(Math.atan(d));
    }

    public static double arccot(double x) {
        return rtd(Math.atan(1 / x));
    }

    public static double arctan2(double y, double x) {
        return rtd(Math.atan2(y, x));
    }

    public static double fixAngle(double a) {
        return fix(a, 360);
    }

    public static double fixHour(double a) {
        return fix(a, 24);
    }

    public static double fix(double a, double b) {
        a = a - b * (Math.floor(a / b));
        return (a < 0) ? a + b : a;
    }
}
