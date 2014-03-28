package com.example.pophello.app.controller;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.example.pophello.app.model.Tag;
import com.example.pophello.app.utility.FeatureFlagManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the following location services:
 *
 * - Monitor for significant device location updates
 * - Monitor for precise device location updates
 * - Monitor for enter/exit tag geofence transitions
 * - Create/destroy tag geofences
 */
public class LocationService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public interface ConnectionCallbacks {
        public void onLocationServiceConnected();
    }

    public interface OnPreciseLocationUpdateListener {
        public void onDeviceUpdatedPreciseLocation(Location location);
    }

    public interface OnEstablishedGeofencesListener {
        public void onEstablishedGeofences();
    }

    private enum LocationUpdateMode {
        NONE, SIGNIFICANT, PRECISE
    }

    private static final String TAG = "LocationService";
    private static final float TAG_GEOFENCE_RADIUS = 100; // meters
    private static final long TAG_GEOFENCE_EXPIRY = 86400000; // 1 day in milliseconds

    private Context mContext;
    private LocationClient mLocationClient;
    private ConnectionCallbacks mConnectionCallbacks;
    private OnPreciseLocationUpdateListener mOnPreciseLocationUpdateListener;
    private OnEstablishedGeofencesListener mOnEstablishedGeofencesListener;
    private LocationUpdateMode mLocationUpdateMode;
    private PendingIntent mSignificantLocationUpdatePendingIntent;

    public LocationService(
            Context context,
            ConnectionCallbacks connectionCallbacks,
            OnPreciseLocationUpdateListener preciseLocationUpdateListener,
            OnEstablishedGeofencesListener establishedGeofencesListener) {
        mContext = context;
        mLocationClient = new LocationClient(context, this, this);
        mConnectionCallbacks = connectionCallbacks;
        mOnPreciseLocationUpdateListener = preciseLocationUpdateListener;
        mOnEstablishedGeofencesListener = establishedGeofencesListener;
        mLocationUpdateMode = LocationUpdateMode.NONE;
    }

    public void connect() {
        mLocationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "connected to Google Play services");
        mConnectionCallbacks.onLocationServiceConnected();
    }

    public void disconnect() {
        mLocationClient.disconnect();
    }

    public void startMonitoringSignificantLocationChanges() {

        Log.i(TAG, "started monitoring significant location changes");
        switch (mLocationUpdateMode) {
            case NONE:
                break;
            case PRECISE:
                stopMonitoringPreciseLocationChanges();
                break;
            case SIGNIFICANT:
                return;
        }

        // when mocking locations the location update interval should be smaller
        LocationRequest locationRequest;
        if (new FeatureFlagManager(mContext).isLocationMockingEnabled()) {
            mLocationClient.setMockMode(true);
            locationRequest = LocationRequest.create().
                    setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).
                    setFastestInterval(5000).
                    setInterval(5000).
                    setSmallestDisplacement(0); // meters

        } else {
            // the update interval should mirror the iOS counterpart
            locationRequest = LocationRequest.create().
                    setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).
                    setFastestInterval(300000). // 5 minutes in milliseconds
                    setInterval(600000). // 10 minutes in milliseconds
                    setSmallestDisplacement(1000); // meters
        }

        // location updates are handled by the `SignificantLocationUpdateHandlerService` service,
        // which is responsible for rebuilding the zone
        Intent locationIntent = new Intent(
                mContext, SignificantLocationUpdateHandlerService.class);
        mSignificantLocationUpdatePendingIntent = PendingIntent.getService(
                mContext, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mLocationClient.requestLocationUpdates(
                locationRequest, mSignificantLocationUpdatePendingIntent);

        mLocationUpdateMode = LocationUpdateMode.SIGNIFICANT;
    }

    public void startMonitoringPreciseLocationChanges() {

        Log.i(TAG, "started monitoring precise location changes");
        switch (mLocationUpdateMode) {
            case NONE:
                break;
            case PRECISE:
                return;
            case SIGNIFICANT:
                stopMonitoringSignificantLocationChanges();
                break;
        }

        LocationRequest locationRequest = LocationRequest.create().
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).
                setFastestInterval(0). // milliseconds
                setInterval(500); // milliseconds
        mLocationClient.requestLocationUpdates(locationRequest, this);
        mLocationUpdateMode = LocationUpdateMode.PRECISE;
    }

    public void stopMonitoringLocationChanges() {
        Log.i(TAG, "stopped monitoring location changes");
        switch (mLocationUpdateMode) {
            case NONE:
                return;
            case PRECISE:
                stopMonitoringPreciseLocationChanges();
                break;
            case SIGNIFICANT:
                stopMonitoringSignificantLocationChanges();
                break;
        }
        mLocationUpdateMode = LocationUpdateMode.NONE;
    }

    private void stopMonitoringPreciseLocationChanges() {
        mLocationClient.removeLocationUpdates(this);
    }

    private void stopMonitoringSignificantLocationChanges() {
        mLocationClient.removeLocationUpdates(mSignificantLocationUpdatePendingIntent);
    }

    /**
     * A new precise location update for the device has been received.
     *
     * It is assumed that the most recent update is the more relevant.
     */
    @Override
    public void onLocationChanged(Location location) {
        mOnPreciseLocationUpdateListener.onDeviceUpdatedPreciseLocation(location);
    }

    public void establishGeofences(Tag[] tagsOld, Tag[] tagsNew) {
        destroyGeofences(tagsOld, tagsNew);
    }

    private void destroyGeofences(final Tag[] tagsOld, final Tag[] tagsNew) {

        Tag[] tagsExpired = getTagSetDifference(tagsOld, tagsNew);
        if (tagsExpired.length == 0) {
            createGeofences(tagsOld, tagsNew); // no geofences to remove
            return;
        }

        List<String> geofenceRequestIds = new ArrayList<String>();
        for (Tag tag : tagsExpired) {
            geofenceRequestIds.add(tag.id);
        }

        mLocationClient.removeGeofences(
                geofenceRequestIds, new LocationClient.OnRemoveGeofencesResultListener() {

            @Override
            public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {
                if (i == LocationStatusCodes.SUCCESS) {
                    Log.i(TAG, "removed geofences");
                    createGeofences(tagsOld, tagsNew);
                } else {
                    Log.e(TAG, "failed to remove geofence");
                    mOnEstablishedGeofencesListener.onEstablishedGeofences();
                }
            }

            // not used
            @Override
            public void onRemoveGeofencesByPendingIntentResult(
                    int i, PendingIntent pendingIntent) {}
        });
    }

    /**
     * Create geofences for any tags that currently don't have one.
     *
     * Android doesn't provide a way to list all currently active geofences. The app attempts to
     * maintain a list in local storage but this may become out of sync during error conditions.
     * To avoid having geofences that persist forever add an expiry of 1 day to each geofence. If
     * the user doesn't leave a zone for a day I think it's reasonable to stop monitoring
     * geofences until they do move.
     *
     * When any geofence experiences an enter or exit transition an intent is dispatched which is
     * handled by the `GeofenceTransitionsService`.
     */
    private void createGeofences(Tag[] tagsOld, Tag[] tagsNew) {

        Tag[] tagsRevealed = getTagSetDifference(tagsNew, tagsOld);
        if (tagsRevealed.length == 0) {
            mOnEstablishedGeofencesListener.onEstablishedGeofences(); // no geofences to create
            return;
        }

        List<Geofence> fences = new ArrayList<Geofence>();
        for (Tag tag : tagsRevealed) {
            Geofence fence = new Geofence.Builder().
                    setRequestId(tag.id).
                    setTransitionTypes(
                            Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT).
                    setCircularRegion(tag.latitude, tag.longitude, TAG_GEOFENCE_RADIUS).
                    setExpirationDuration(TAG_GEOFENCE_EXPIRY).
                    build();
            fences.add(fence);
        }

        // any geofence enter/exit transition is handler by the `GeofenceTransitionService`
        Intent intent = new Intent(mContext, GeofenceTransitionsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mLocationClient.addGeofences(
                fences, pendingIntent, new LocationClient.OnAddGeofencesResultListener() {

            @Override
            public void onAddGeofencesResult(int i, String[] strings) {
                if (i == LocationStatusCodes.SUCCESS) {
                    Log.i(TAG, "added geofences");
                } else {
                    Log.e(TAG, "failed to add geofence");
                }
                mOnEstablishedGeofencesListener.onEstablishedGeofences();
            }
        });
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "disconnected from Google Play services");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: should provide a callback so that services can end, etc.
        Log.e(TAG, "failed to connect to Google Play services");
    }

    /**
     * Return all tags that are in set `a` but not set `b`.
     */
    private Tag[] getTagSetDifference(Tag[] a, Tag[] b) {
        List<Tag> result = new ArrayList<Tag>();
        for (Tag tA : a) {
            boolean inB = false;
            for (Tag tB : b) {
                if (tB.id.equals(tA.id)) {
                    inB = true;
                    break;
                }
            }
            if (!inB) {
                result.add(tA);
            }
        }
        return result.toArray(new Tag[result.size()]);
    }
}
