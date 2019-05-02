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
 * Calculated Methods
 */
@SuppressWarnings("unused")
public enum Method {
    /**
     * Muslim World League
     * Fajr: 18*, Maghrib: Sunset, Ishaa: 17*
     */
    MWL {
        @Override
        public void set(PrayTimes pt) {
            pt.tuneImsak(18, 0);
            pt.tuneFajr(18, 0);
            pt.tuneIshaa(17, 0);
        }
    },
    /**
     * Islamic Society of North America (ISNA)
     * Fajr: 15*, Maghrib: Sunset, Ishaa: 15*
     */
    ISNA {
        @Override
        public void set(PrayTimes pt) {
            pt.tuneImsak(18, 0);
            pt.tuneFajr(18, 0);
            pt.tuneIshaa(15, 0);
        }
    },
    /**
     * Egyptian General Authority of Survey
     * Fajr: 19.5*, Maghrib: Sunset, Ishaa: 17.5*
     */
    Egypt {
        @Override
        public void set(PrayTimes pt) {
            pt.tuneImsak(19.5, 0);
            pt.tuneFajr(19.5, 0);
            pt.tuneIshaa(17.5, 0);
        }
    },
    /**
     * Umm Al-Qura University, Makkah
     * Fajr: 18.5*, Maghrib: Sunset, Ishaa: 90min after sunset
     */
    Makkah {
        @Override
        public void set(PrayTimes pt) {
            pt.tuneImsak(18.5, 0);
            pt.tuneFajr(18.5, 0);
            pt.tuneIshaa(0, 90);
        }
    },
    /**
     * University of Islamic Sciences, Karachi
     * Fajr: 18*, Maghrib: Sunset, Ishaa: 15*
     */
    Karachi {
        @Override
        public void set(PrayTimes pt) {
            pt.tuneImsak(18, 0);
            pt.tuneFajr(18, 0);
            pt.tuneIshaa(15, 0);
        }
    },
    /**
     * /**
     * Institute of Geophysics, University of Tehran
     * Fajr: 17.7*, Maghrib: 4.5*, Ishaa: 14*
     */
    Tehran {
        @Override
        public void set(PrayTimes pt) {
            pt.tuneImsak(17.7, 0);
            pt.tuneFajr(17.7, 0);
            pt.tuneMaghrib(4.5, 0);
            pt.tuneIshaa(14, 0);
        }
    },
    /**
     * Shia Ithna-Ashari, Leva Institute, Qum
     * Fajr: 16*, Maghrib: 4*, Ishaa: 14*
     */
    Jafari {
        @Override
        public void set(PrayTimes pt) {
            pt.tuneImsak(16, 0);
            pt.tuneFajr(16, 0);
            pt.tuneMaghrib(4, 0);
            pt.tuneIshaa(14, 0);
        }
    };
    
    
    private int[] minutes = new int[Times.values().length];
    private double[] angles = new double[Times.values().length];
    
    
    public abstract void set(PrayTimes prayTimes);
}
