package org.metinkale.praytimes;

//===============================================>
public enum Times {
    /**
     * The time to stop eating Sahur (for fasting), slightly before Fajr.
     */
    Imsak,
    /**
     * When the sky begins to lighten (dawn).
     */
    Fajr,
    /**
     * The time at which the first part of the Sun appears above the horizon.
     */
    Sunrise,
    /**
     * Zawal (Solar Noon): When the Sun reaches its highest point in the sky.
     */
    Zawal,
    /**
     * When the Sun begins to decline after reaching its highest point in the sky, slightly after solar noon
     */
    Dhuhr,
    /**
     * The time when the length of any object's shadow reaches a factor 1 of the length of the object itself plus the length of that object's shadow at noon.
     */
    AsrShafi,
    /**
     * The time when the length of any object's shadow reaches a factor 2 of the length of the object itself plus the length of that object's shadow at noon.
     */
    AsrHanafi,
    /**
     * The time at which the Sun disappears below the horizon.
     */
    Sunset,
    /**
     * Soon after sunset.
     */
    Maghrib,
    /**
     * The time at which darkness falls and there is no scattered light in the sky.
     */
    Ishaa,
    /**
     * The mean time from sunset to sunrise .
     */
    Midnight
}
