package com.example.pophello.app.model;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.pophello.app.controller.LocationService;
import com.example.pophello.app.model.data.LocalStorageUnavailableException;
import com.example.pophello.app.model.data.TagActiveStore;
import com.example.pophello.app.model.data.TagsStore;

import java.util.Arrays;

/**
 * Manages the zone by coordinating between the location service, server, tags store and active tag
 * store.
 */
public class ZoneManager implements
        LocationService.ConnectionCallbacks,
        LocationService.OnPreciseLocationUpdateListener,
        LocationService.OnUpdatedGeofencesListener {

    public interface ConnectionCallbacks {
        public void onZoneManagerConnectedToLocationServices();
    }

    public interface OnUpdatedZoneListener {
        public void onUpdatedZone();
    }

    private enum LocationUpdateMode {
        NONE, SIGNIFICANT, PRECISE
    }

    private static final String TAG = "ZoneManager";

    private Context mContext;
    private LocationService mLocationService;
    private TagsStore mTagsStore;
    private TagActiveStore mTagActiveStore;
    private ConnectionCallbacks mConnectionCallbacks;
    private OnUpdatedZoneListener mOnUpdatedZoneListener;
    private Location mLastPreciseLocation;
    private LocationUpdateMode mLocationUpdateMode;

    public ZoneManager(
            Context context,
            ConnectionCallbacks connectionCallbacks,
            OnUpdatedZoneListener updatedZoneListener) {
        mContext = context;
        mLocationService = new LocationService(context, this, this, this);
        mTagsStore = new TagsStore(context);
        mTagActiveStore = new TagActiveStore(context);
        mConnectionCallbacks = connectionCallbacks;
        mOnUpdatedZoneListener = updatedZoneListener;
        mLocationUpdateMode = LocationUpdateMode.NONE;
    }

    public boolean isConnectedToLocationServices() {
        return mLocationService.isConnected();
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
                break;
        }
        mLocationService.stopMonitoringLocationChanges();
        mLocationUpdateMode = LocationUpdateMode.NONE;
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
            mTagActiveStore.clearIfActive(tag.id);
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
     * Remove a single tag from a zone, in response to a user acknowledging it.
     */
    public void removeTag(String tagId) {
        try {
            mTagActiveStore.clearIfActive(tagId);
            mTagsStore.remove(tagId);
        } catch (LocalStorageUnavailableException e) {
            Log.e(TAG, "local storage is unavailable");
            return;
        }
        mLocationService.removeGeofence(tagId);
    }

    /**
     * Rebuild the zone with a new list of tags.
     *
     * The list of tags may be empty (as is the case with clearing the zone) or will contain tags
     * retrieved from the server.
     */
    public void updateZone(Tag[] tagsNew) {

        Tag[] tagsOld;
        try {
            tagsOld = mTagsStore.fetchAll();
            mTagsStore.clear();
            mTagsStore.put(tagsNew);
            Tag tagActive = mTagActiveStore.fetch();
            boolean keepTagActive =
                    tagActive != null && Arrays.asList(tagsNew).contains(tagActive);
            if (!keepTagActive && tagActive != null) {
                mTagActiveStore.clear();
                new TagNotification(mContext).dismissAll();
            }

        } catch (LocalStorageUnavailableException e) {
            Log.e(TAG, "local storage is unavailable");
            mOnUpdatedZoneListener.onUpdatedZone();
            return;
        }
        mLocationService.establishGeofences(tagsOld, tagsNew);
    }

    @Override
    public void onUpdatedGeofences() {
        mOnUpdatedZoneListener.onUpdatedZone();
    }

    public void disconnect() {
        mLocationService.disconnect();
        mTagsStore.close();
        mTagActiveStore.close();
    }

    public void setMockLocation(Location location) {
        mLocationService.setMockLocation(location);
    }
}
