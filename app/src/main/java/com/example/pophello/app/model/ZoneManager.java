package com.example.pophello.app.model;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.pophello.app.controller.LocationService;
import com.example.pophello.app.model.data.LocalStorageUnavailableException;
import com.example.pophello.app.model.data.TagActiveStore;
import com.example.pophello.app.model.data.TagsStore;

/**
 * Manages the zone by coordinating between the location service, server, tags store and active tag
 * store.
 */
public class ZoneManager implements
        LocationService.ConnectionCallbacks,
        LocationService.OnPreciseLocationUpdateListener,
        LocationService.OnEstablishedGeofencesListener {

    public interface ConnectionCallbacks {
        public void onZoneManagerConnectedToLocationServices();
    }

    public interface OnEstablishedZoneListener {
        public void onEstablishedZone();
    }

    private enum LocationUpdateMode {
        NONE, SIGNIFICANT, PRECISE
    }

    private static final String TAG = "ZoneManager";

    private LocationService mLocationService;
    private TagsStore mTagsStore;
    private TagActiveStore mTagActiveStore;
    private ConnectionCallbacks mConnectionCallbacks;
    private OnEstablishedZoneListener mOnEstablishedZoneListener;
    private Location mLastPreciseLocation;
    private LocationUpdateMode mLocationUpdateMode;

    public ZoneManager(
            Context context,
            ConnectionCallbacks connectionCallbacks,
            OnEstablishedZoneListener establishedZoneListener) {
        mLocationService = new LocationService(context, this, this, this);
        mTagsStore = new TagsStore(context);
        mTagActiveStore = new TagActiveStore(context);
        mConnectionCallbacks = connectionCallbacks;
        mOnEstablishedZoneListener = establishedZoneListener;
        mLocationUpdateMode = LocationUpdateMode.NONE;
    }

    public void connectToLocationServices() {
        mLocationService.connect();
    }

    @Override
    public void onLocationServiceConnected() {
        mConnectionCallbacks.onZoneManagerConnectedToLocationServices();
    }

    public void startMonitoringSignificantLocationChanges() {
        switch (mLocationUpdateMode) {
            case NONE:
                break;
            case PRECISE:
                stopMonitoringPreciseLocationChanges();
                break;
            case SIGNIFICANT:
                return;
        }
        mLocationService.startMonitoringSignificantLocationChanges();
        mLocationUpdateMode = LocationUpdateMode.SIGNIFICANT;
    }

    public void startMonitoringPreciseLocationChanges() {
        switch (mLocationUpdateMode) {
            case NONE:
                break;
            case PRECISE:
                return;
            case SIGNIFICANT:
                stopMonitoringSignificantLocationChanges();
                break;
        }
        mLocationService.startMonitoringPreciseLocationChanges();
        mLocationUpdateMode = LocationUpdateMode.PRECISE;
    }

    public void stopMonitoringLocationChanges() {
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
        mLocationService.stopMonitoringLocationChanges();
        mLocationUpdateMode = LocationUpdateMode.NONE;
    }

    private void stopMonitoringSignificantLocationChanges() {
        clearZone();
    }

    private void stopMonitoringPreciseLocationChanges() {
        mLastPreciseLocation = null;
    }

    @Override
    public void onDeviceUpdatedPreciseLocation(Location location) {
        Log.i(TAG, "device updated precise location: " + location);
        mLastPreciseLocation = location;
    }

    public Location getLastPreciseLocation() {
        return mLastPreciseLocation;
    }

    public void onEnterTagRegion(Tag tag) {
        try {
            mTagActiveStore.put(tag);
        } catch (LocalStorageUnavailableException e) {
            Log.e(TAG, "local storage is unavailable");
        }
    }

    public void onExitTagRegion(Tag tag) {
        try {
            mTagActiveStore.clearIfActive(tag);
        } catch (LocalStorageUnavailableException e) {
            Log.e(TAG, "local storage is unavailable");
        }
    }

    public Tag getTag(String tagId) {
        try {
            return mTagsStore.fetch(tagId);
        } catch (LocalStorageUnavailableException e) {
            Log.e(TAG, "local storage is unavailable");
            return null;
        }
    }

    public Tag getActiveTag() {
        try {
            return mTagActiveStore.fetch();
        } catch (LocalStorageUnavailableException e) {
            Log.e(TAG, "local storage is unavailable");
            return null;
        }
    }

    /**
     * Clear the existing zone.
     *
     * In response to significant location monitoring being stopped. Clearing a zone is logically
     * equivalent to rebuilding a zone with no tags.
     */
    private void clearZone() {
        rebuildZone(new Tag[0]);
    }

    /**
     * Rebuild the zone with a new list of tags.
     *
     * The list of tags may be empty (as is the case with clearing the zone) or will contain tags
     * retrieved from the server.
     */
    public void rebuildZone(Tag[] tagsNew) {

        Tag[] tagsOld;
        try {
            mTagActiveStore.clear();
            tagsOld = mTagsStore.fetchAll();
            mTagsStore.clear();
            mTagsStore.put(tagsNew);
        } catch (LocalStorageUnavailableException e) {
            Log.e(TAG, "local storage is unavailable");
            mOnEstablishedZoneListener.onEstablishedZone();
            return;
        }
        mLocationService.establishGeofences(tagsOld, tagsNew);
    }

    @Override
    public void onEstablishedGeofences() {
        mOnEstablishedZoneListener.onEstablishedZone();
    }

    public void disconnect() {
        mLocationService.disconnect();
        mTagsStore.close();
        mTagActiveStore.close();
    }
}
