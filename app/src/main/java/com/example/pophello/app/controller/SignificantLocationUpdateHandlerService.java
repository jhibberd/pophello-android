package com.example.pophello.app.controller;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.example.pophello.app.model.Tag;
import com.example.pophello.app.model.TagNotification;
import com.example.pophello.app.model.ZoneManager;
import com.example.pophello.app.model.server.EndpointContentGET;
import com.example.pophello.app.utility.FeatureFlagManager;
import com.google.android.gms.location.LocationClient;

public class SignificantLocationUpdateHandlerService extends Service implements
        ZoneManager.ConnectionCallbacks,
        EndpointContentGET.OnResponseListener,
        ZoneManager.OnEstablishedZoneListener {

    private static final String TAG = "SignificantLocationUpdateHandlerService";

    private boolean mIsProcessing;
    private ZoneManager mZoneManager;
    private Location mLocation;

    @Override
    public void onCreate() {

        Log.i(TAG, "created");
        super.onCreate();
        mZoneManager = new ZoneManager(this, this, this);

        // TODO: not sure if this is necessary here but the app is crashing and no report is being
        // sent from the device to BugSense
        if (new FeatureFlagManager(this).isBugSenseEnabled()) {
            BugSenseHandler.initAndStartSession(
                    SignificantLocationUpdateHandlerService.this, "33d32cc5");
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroyed");
        mZoneManager.disconnect();
        super.onDestroy();
    }

    /**
     * Handle a new significant location update.
     *
     * Location updates shouldn't happen frequently so the chance of a location update being
     * received while a prior update is still being processed is extremely unlikely. In this
     * scenario just ignore the incoming location update.
     *
     * Although services can be reused this causes complications because of the asynchronous
     * nature of this service. For example, one location update could be processed, call `stopSelf`
     * and then begin processing a second location update when the service gets killed by the OS.
     * A simpler model is just to allow a service to process a single location before it's killed.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "started");

        if (mIsProcessing) {
            Log.w(TAG, "update ignored as already processing");
            return Service.START_NOT_STICKY;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(TAG, "location update intent contains no extras");
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        Location location = (Location) extras.get(LocationClient.KEY_LOCATION_CHANGED);
        if (location == null) {
            Log.e(TAG, "location update intent doesn't contain a location");
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        mIsProcessing = true;
        mLocation = location;
        mZoneManager.connectToLocationServices();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onZoneManagerConnectedToLocationServices() {
        Log.i(TAG, "processing location update " + mLocation.toString());
        new EndpointContentGET(
                this, mLocation.getLongitude(), mLocation.getLatitude(), this).call();
    }

    /**
     * Handle a new set of tags for the new zone.
     *
     * Remove geofences for the expired tags that are currently stored locally. If a tag appears
     * in the old and new set then don't delete it's geofence to prevent an undesired geofence
     * enter event from potentially being triggered.
     *
     * TODO: We may need to revise dismissing the notification here. What if the device stays in
     * the same geofence as we transition between two zones? We don't want to notification being
     * dispatched twice.
     */
    @Override
    public void onEndpointContentGETResponseSuccess(Tag[] tags) {
        new TagNotification(this).dismissAll();
        mZoneManager.rebuildZone(tags);
    }

    @Override
    public void onEstablishedZone() {
        stopSelf();
    }

    @Override
    public void onEndpointContentGETResponseFailed() {
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // no communication with activity
    }
}
