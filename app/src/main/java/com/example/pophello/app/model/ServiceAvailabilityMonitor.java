package com.example.pophello.app.model;

import android.content.Context;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Monitors device settings, features and services to determine whether this app has everything it
 * needs to run.
 *
 * GPS is no longer a requirement has the app seems to function fine without it and GPS consumes
 * a large amount of power.
 */
public class ServiceAvailabilityMonitor {

    public enum State {
        AVAILABLE,
        GOOGLE_PLAY_SERVICES_MISSING,
        LOCATION_PROVIDER_NETWORK_DISABLED
    }

    private boolean mIsGooglePlayServicesAvailable;
    private boolean mIsLocationProviderNetworkEnabled;

    private final Context mContext;
    private final LocationManager mLocationManager;

    public ServiceAvailabilityMonitor(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void checkAvailability() {
        checkIsGooglePlayServicesAvailable();
        checkIsLocationProviderNetworkEnabled();
    }

    /**
     * Return whether the service is currently available, or if not the reason why it isn't.
     *
     * In the case of multiple reasons for unavailability, the reason that the user is most likely
     * to be able to do something about is returned.
     */
    public State getState() {
        if (!mIsGooglePlayServicesAvailable) {
            return State.GOOGLE_PLAY_SERVICES_MISSING;
        }
        if (!mIsLocationProviderNetworkEnabled) {
            return State.LOCATION_PROVIDER_NETWORK_DISABLED;
        }
        return State.AVAILABLE;
    }

    private void checkIsGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        mIsGooglePlayServicesAvailable = status == ConnectionResult.SUCCESS;
    }

    private void checkIsLocationProviderNetworkEnabled() {
        mIsLocationProviderNetworkEnabled =
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
