package com.metinkale.prayerapp.custom;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.settings.Prefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Geocoder {

    public static List<Address> from(String address, int limit) {
        List<Address> geo = new ArrayList<>();
        Locale locale = new Locale(Prefs.getLanguage());
        android.location.Geocoder geocoder = new android.location.Geocoder(App.getContext(), locale);
        try {
            List<android.location.Address> resp;
            if (address.contains(";")) {
                try {
                    double lat = Double.parseDouble(address.substring(0, address.indexOf(";")));
                    double lng = Double.parseDouble(address.substring(address.indexOf(";") + 1));
                    resp = geocoder.getFromLocation(lat, lng, limit);
                } catch (NumberFormatException ignore) {
                    resp = geocoder.getFromLocationName(address, limit);
                }
            } else
                resp = geocoder.getFromLocationName(address, limit);
            for (android.location.Address a : resp) {
                Address g = new Address();
                g.lat = a.getLatitude();
                g.lng = a.getLongitude();
                g.country = a.getCountryName();
                g.state = a.getAdminArea();
                g.city = a.getLocality();
                if (g.city == null)
                    g.city = a.getSubAdminArea();
                if (g.city == null)
                    g.city = a.getFeatureName();
                geo.add(g);
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
        return geo;
    }


    public static class Address {
        public String city, state, country;
        public double lat, lng;
    }

}