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
 * Qibla times calculated by PrayTimes
 */
public class QiblaTime {
    String front;
    String left;
    String right;
    String back;

    QiblaTime() {
    }

    /**
     * if you turn yourself to sun at that time, you are on qibla qirection
     *
     * @return time in HH:MM or null, if it does not exists in your location
     */
    public String getFront() {
        return front;
    }

    /**
     * if you take the sun to your left at that time, you are on qibla direction
     *
     * @return time in HH:MM or null, if it does not exists in your location
     */
    public String getLeft() {
        return left;
    }

    /**
     * if you take the sun to your right at that time, you are on qibla direction
     *
     * @return time in HH:MM or null, if it does not exists in your location
     */
    public String getRight() {
        return right;
    }

    /**
     * if you turn yourself away from the sun at that time you are in qibla direction
     *
     * @return time in HH:MM or null, if it does not exists in your location
     */
    public String getBack() {
        return back;
    }

}
