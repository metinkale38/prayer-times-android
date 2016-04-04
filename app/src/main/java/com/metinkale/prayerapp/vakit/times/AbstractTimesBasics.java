package com.metinkale.prayerapp.vakit.times;

import android.preference.PreferenceManager;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.WidgetService;

/**
 * Created by Metin on 20.06.2015.
 */
public class AbstractTimesBasics extends AbstractTimesStorage {

    AbstractTimesBasics(long id) {
        super(id);
    }

    static Source getSource(long id) {
        return new AbstractTimesBasics(id).getSource();
    }

    public int getSortId() {
        return getInt("sortId", 99);
    }

    public void setSortId(int sortId) {
        set("sortId", sortId);
    }

    public Source getSource() {
        return Source.valueOf(getString("source"));
    }

    public void setSource(Source source) {
        set("source", source.name());
    }

    public double getLng() {
        return getDouble("lng");
    }

    public void setLng(double value) {
        set("lng", value);
    }

    public double getLat() {
        return getDouble("lat");
    }

    public void setLat(double value) {
        set("lat", value);
    }


    public int[] getMinuteAdj() {
        return getIntArray("minuteAdj", new int[6]);
    }

    public void setMinuteAdj(int[] adj) {
        if (adj.length != 6)
            throw new RuntimeException("setMinuteAdj(double[] adj) can only be called with adj of size 6");
        set("minuteAdj", adj);
    }

    public String getName() {
        return getString("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public double getTZFix() {
        double def = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("summertime_fixer", "0"));
        return getDouble("timezone", def);
    }

    public void setTZFix(double tz) {
        set("timezone", tz);
    }

    public int getCumaSilenterDuration() {
        return getInt("cuma_silenter", 0);
    }

    public void setCumaSilenterDuration(int value) {
        set("cuma_silenter", value);
    }

    public String getCumaSound() {
        return getString("cuma_sound", "silent");

    }

    public void setCumaSound(String value) {
        set("cuma_sound", value);
    }

    public int getCumaTime() {
        return getInt("cuma_time", 15);
    }

    public void setCumaTime(int value) {
        set("cuma_time", value);
    }

    public String getDua(Vakit v) {
        String dua = getString(v.name() + "_dua", "silent");
        if ("1".equals(dua))
            return PreferenceManager.getDefaultSharedPreferences(App.getContext()).getString("dua_source", "dua");

        return dua;
    }

    public int getEarlySilenterDuration(Vakit v) {
        return getInt("pre_" + v.name() + "_silenter", 0);
    }

    public String getEarlySound(Vakit v) {
        return getString("pre_" + v.name() + "_sound", "silent");
    }

    public int getEarlyTime(Vakit v) {
        return getInt("pre_" + v.name() + "_time", 15);
    }

    public int getSabahTime() {
        return getInt("sabah_time", 30);
    }

    public void setSabahTime(int time) {
        set("sabah_time", time);
    }

    public int getSilenterDuration(Vakit v) {
        return getInt(v.name() + "_silenter", 0);

    }

    public String getSound(Vakit v) {
        return getString(v.name() + "_sound", "silent");
    }

    public boolean hasCumaVibration() {
        return is("cuma_vibration");
    }

    public boolean hasEarlyVibration(Vakit v) {
        return is("pre_" + v.name() + "_vibration");
    }

    public boolean hasVibration(Vakit v) {
        return is(v.name() + "_vibration");
    }

    public boolean isAfterImsak() {
        return is("sabah_afterImsak");

    }

    public void setAfterImsak(boolean value) {
        set("sabah_afterImsak", value);
    }

    public boolean isCumaActive() {
        return is("cuma");
    }

    public void setCumaActive(boolean value) {
        set("cuma", value);
    }

    public boolean isEarlyNotificationActive(Vakit v) {
        return is("pre_" + v.name());

    }

    public boolean isNotificationActive(Vakit v) {
        return is(v.name());

    }

    public boolean isOngoingNotificationActive() {
        return is("ongoing");
    }

    public void setOngoingNotificationActive(boolean value) {
        set("ongoing", value);
        WidgetService.updateOngoing();
    }

    public void setCumaVibration(boolean value) {
        set("cuma_viration", value);
    }

    public void setDua(Vakit v, String value) {
        set(v.name() + "_dua", value);
    }

    public void setEarlyNotificationActive(Vakit v, boolean value) {
        set("pre_" + v.name(), value);
    }

    public void setEarlySilenterDuration(Vakit v, int value) {
        set("pre_" + v.name() + "_silenter", value);
    }

    public void setEarlySound(Vakit v, String value) {
        set("pre_" + v.name() + "_sound", value);
    }

    public void setEarlyTime(Vakit v, int value) {
        set("pre_" + v.name() + "_time", value);
    }

    public void setEarlyVibration(Vakit v, boolean value) {
        set("pre_" + v.name() + "_vibration", value);
    }

    public void setNotificationActive(Vakit v, boolean value) {
        set(v.name(), value);
    }

    public void setSilenterDuration(Vakit v, int value) {
        set(v.name() + "_silenter", value);
    }

    public void setSound(Vakit v, String value) {
        set(v.name() + "_sound", value);
    }

    public void setVibration(Vakit v, boolean value) {
        set(v.name() + "_vibration", value);
    }


}



