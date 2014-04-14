package com.example.pophello.app.model;

import android.content.Context;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class ServiceAvailabilityMonitor {

    public enum State {
        AVAILABLE,
        GOOGLE_PLAY_SERVICES_MISSING,
        LOCATION_PROVIDER_GPS_DISABLED,
        LOCATION_PROVIDER_NETWORK_DISABLED
    }

    private boolean mIsGooglePlayServicesAvailable;
    private boolean mIsLocationProviderGPSEnabled;
    private boolean mIsLocationProviderNetworkEnabled;

    private final Context mContext;
    private final LocationManager mLocationManager;

    public ServiceAvailabilityMonitor(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void checkAvailability() {
        checkIsGooglePlayServicesAvailable();
        checkIsLocationProviderGPSEnabled();
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
        if (!mIsLocationProviderGPSEnabled) {
            return State.LOCATION_PROVIDER_GPS_DISABLED;
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

    private void checkIsLocationProviderGPSEnabled() {
        mIsLocationProviderGPSEnabled =
                mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void checkIsLocationProviderNetworkEnabled() {
        mIsLocationProviderNetworkEnabled =
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
