package com.example.pophello.app.utility;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;

import com.example.pophello.app.model.ZoneManager;

/**
 * Send a sequence of mock locations to the location service.
 *
 * Used as a development tool.
 */
public class MockLocationSequence {

    private static final String TAG = "MockLocationSequence";
    private static final String LOCATION_PROVIDER = "flp"; // required provider for mock locations
    private static final long DELAY_START = 15000; // ms
    private static final long DELAY_UPDATE = 7000; // ms
    private static final float ACCURACY = 3;

    private static final double[][] mCoordinates = new double[][] {
        {1, 1},
        {3, 3},
        {3, 3},
        {1, 1},
    };

    private final ZoneManager mZoneManager;

    public MockLocationSequence(ZoneManager zoneManager) {
        mZoneManager = zoneManager;
    }

    public void run() {
        new Thread(new Runnable() {
            public void run() {
                SystemClock.sleep(DELAY_START);
                Log.i(TAG, "beginning mock location sequence");
                for (double[] coordinate : mCoordinates) {
                    double latitude = coordinate[0];
                    double longitude = coordinate[1];
                    Location location = makeMockLocation(latitude, longitude);
                    mZoneManager.setMockLocation(location);
                    SystemClock.sleep(DELAY_UPDATE);
                }
                Log.i(TAG, "completed mock location sequence");
            }
        }).start();
    }

    private Location makeMockLocation(double latitude, double longitude) {
        Location location = new Location(LOCATION_PROVIDER);
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        location.setTime(System.currentTimeMillis());
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(ACCURACY);
        return location;
    }
}
