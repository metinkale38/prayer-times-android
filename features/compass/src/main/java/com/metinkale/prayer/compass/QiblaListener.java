package com.metinkale.prayer.compass;

public interface QiblaListener {
    void setUserLocation(double lat, double lng, double alt);
    
    void setQiblaAngle(double angle);
    
    void setQiblaDistance(double distance);
}
