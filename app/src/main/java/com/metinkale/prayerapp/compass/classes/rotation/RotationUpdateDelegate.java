package com.metinkale.prayerapp.compass.classes.rotation;

/**
 * Delegate to receive updates when rotation of device changes
 *
 * @author Adam
 */
public interface RotationUpdateDelegate {
    /**
     * @param newMatrix - 4x4 matrix
     */
    void onRotationUpdate(float newMatrix[]);
}
