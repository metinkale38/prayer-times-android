package org.metinkale.praytimes;

class Utils {

    /**
     * convert double time to HH:MM
     *
     * @param time time in double
     * @return HH:MM
     */
    static String toString(double time) {
        int t = (int) Math.round(time * 60);
        return az(t / 60) + ":" + az(t % 60);
    }

    /**
     * return a two digit String of number
     *
     * @param i number
     * @return two digit number
     */
    private static String az(int i) {
        return i >= 10 ? "" + i : "0" + i;
    }


    /**
     * compute the difference between two times
     *
     * @param time1 Time 1
     * @param time2 Time 2
     * @return timediff
     */
    static double timeDiff(double time1, double time2) {
        return DMath.fixHour(time2 - time1);
    }

}
