package org.metinkale.praytimes;

public enum HighLatsAdjustment {
    
    /**
     * Adjust Methods for Higher Latitudes
     * Method: no adjustment
     */
    None,
    /**
     * Adjust Methods for Higher Latitudes
     * Method: angle/60th of night
     * <p>
     * This is an intermediate solution, used by some recent prayer time calculators. Let α be the twilight angle for Isha, and let t = α/60. The period between sunset and sunrise is divided into t parts. Isha begins after the first part. For example, if the twilight angle for Isha is 15, then Isha begins at the end of the first quarter (15/60) of the night. Time for Fajr is calculated similarly.
     */
    AngleBased,
    /**
     * Adjust Methods for Higher Latitudes
     * Method: 1/7th of night
     * <p>
     * In this method, the period between sunset and sunrise is divided into seven parts. Isha begins after the first one-seventh part, and Fajr is at the beginning of the seventh part.
     */
    OneSeventh,
    /**
     * Adjust Methods for Higher Latitudes
     * Method: middle of night
     * <p>
     * In this method, the period from sunset to sunrise is divided into two halves. The first half is considered to be the "night" and the other half as "day break". Fajr and Isha in this method are assumed to be at mid-night during the abnormal periods.
     */
    NightMiddle
}
