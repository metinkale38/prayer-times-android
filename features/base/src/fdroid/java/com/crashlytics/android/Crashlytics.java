package com.crashlytics.android;

/**
 * Created by metin on 08.08.16.
 */
public class Crashlytics {
    public static void logException(Throwable e) {
        e.printStackTrace();
    }

    public static void setDouble(String key, double value) {
    }

    public static void setString(String key, String value) {
    }

    public static void setBool(String key, boolean value) {
    }

    public static void setUserIdentifier(String uuid) {

    }
}
