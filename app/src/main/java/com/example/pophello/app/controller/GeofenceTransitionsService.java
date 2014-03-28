package com.example.pophello.app.controller;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.example.pophello.app.model.Tag;
import com.example.pophello.app.model.TagNotification;
import com.example.pophello.app.model.ZoneManager;
import com.example.pophello.app.utility.FeatureFlagManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.List;

public class GeofenceTransitionsService extends IntentService {

    private static final String TAG = "GeofenceTransitionsService";

    private ZoneManager mZoneManager;

    public GeofenceTransitionsService() {
        super("GeofenceTransitionsService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: not sure if this is necessary here but the app is crashing and no report is being
        // sent from the device to BugSense
        if (new FeatureFlagManager(this).isBugSenseEnabled()) {
            BugSenseHandler.initAndStartSession(GeofenceTransitionsService.this, "33d32cc5");
        }

        mZoneManager = new ZoneManager(this, null, null);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (LocationClient.hasError(intent)) {
            int errorCode = LocationClient.getErrorCode(intent);
            Log.e(TAG, "region transition error: " + Integer.toString(errorCode));
            return;
        }

        int transitionType = LocationClient.getGeofenceTransition(intent);
        List<Geofence> fences = LocationClient.getTriggeringGeofences(intent);

        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            for (Geofence fence : fences) {
                onRegionEnter(fence);
            }
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            for (Geofence fence : fences) {
                onRegionExit(fence);
            }
        }
    }

    /**
     * Handle the device entering a tag region.
     *
     * Maintain the current active tag in local storage so that it persists in the event of the app
     * being killed. When the app is launched it can check quickly in local storage to see which
     * tag (if any) should be shown to the user.
     *
     * The app only monitors geofences when running in the background so dispatch a notification to
     * inform the user.
     *
     * The geofence request ID is the tag ID.
     */
    private void onRegionEnter(Geofence fence) {

        Log.i(TAG, "device did enter region: " + fence.getRequestId());
        String tagId = fence.getRequestId();
        Tag tag = mZoneManager.getTag(tagId);
        if (tag == null) {
            // erroneous geofences eventually expire
            Log.e(TAG, "entered region for tag not found in local storage");
            return;
        }
        mZoneManager.onEnterTagRegion(tag);

        new TagNotification(this).present(tag);
    }

    /**
     * Handle the device exiting a tag region.
     *
     * The tag ID is the same as the geofence ID. Because tag regions can overlap each other only
     * take action if the region being exited represents the active tag, otherwise the most recent
     * call to `onRegionEnter` will have replaced the tag being exited.
     *
     * Cancel any visible notification.
     */
    private void onRegionExit(Geofence fence) {

        Log.i(TAG, "device did exit region: " + fence.getRequestId());
        String tagId = fence.getRequestId();
        Tag tag = mZoneManager.getTag(tagId);
        if (tag == null) {
            // erroneous geofences eventually expire
            Log.e(TAG, "exited region for tag not found in local storage");
            return;
        }
        mZoneManager.onExitTagRegion(tag);

        new TagNotification(this).dismissIfPresenting(tag);
    }

    /**
     * Destroy the service.
     *
     * Disconnecting the zone manager closes any open database connections.
     */
    @Override
    public void onDestroy() {
        mZoneManager.disconnect();
        super.onDestroy();
    }
}
