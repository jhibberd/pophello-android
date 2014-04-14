package com.example.pophello.app.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Window;

import com.bugsense.trace.BugSenseHandler;
import com.example.pophello.app.R;
import com.example.pophello.app.model.MainView;
import com.example.pophello.app.model.ServiceAvailabilityMonitor;
import com.example.pophello.app.model.Tag;
import com.example.pophello.app.model.TagNotification;
import com.example.pophello.app.model.ZoneManager;
import com.example.pophello.app.utility.FeatureFlagManager;
import com.example.pophello.app.view.TagCreateFragment;

public class MainActivity extends ActionBarActivity implements
        ZoneManager.ConnectionCallbacks,
        ZoneManager.OnUpdatedZoneListener,
        TagCreateFragment.OnTagCreateListener {

    private static final String TAG = "MainActivity";

    private enum StartupMode {
        TAG, CREATE
    }

    private ZoneManager mZoneManager;
    private MainView mMainView;
    private StartupMode mStartupMode;
    private ServiceAvailabilityMonitor mServiceAvailabilityMonitor;
    private boolean mIsAppVisible;

    /**
     * Initialise the main activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (new FeatureFlagManager(this).isBugSenseEnabled()) {
            BugSenseHandler.initAndStartSession(MainActivity.this, "33d32cc5");
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mZoneManager = new ZoneManager(this, this, this);
        mMainView = new MainView(getFragmentManager());
        mServiceAvailabilityMonitor = new ServiceAvailabilityMonitor(this);
    }

    /**
     * Handle the activity coming back into view.
     *
     * Due to complications with save instance state any fragment transactions should take place
     * here and not in `onResume` or async callbacks:
     * http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
     *
     * Local notifications exist to notify the user of a significant event occurring in the app. If
     * the app has just been launched by the user all local notifications can be cleared because
     * they've served their purpose.
     *
     * There can only ever be one local notification and it will always be consistent with the
     * active tag in local storage. When the app is launched or becomes active checking the active
     * tag in local storage is sufficient to handle both the app being launched directly by the
     * user or by the user clicking a local notification.
     */
    @Override
    protected void onPostResume() {

        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiverGeofenceEnter,
                new IntentFilter(GeofenceTransitionsService.ACTION_GEOFENCE_ENTER));
        new TagNotification(this).dismissAll();

        mServiceAvailabilityMonitor.checkAvailability();
        ServiceAvailabilityMonitor.State state = mServiceAvailabilityMonitor.getState();
        if (state == ServiceAvailabilityMonitor.State.AVAILABLE) {
            initUI();
        } else {
            mMainView.presentServiceUnavailable(state);
        }
    }

    private void initUI() {
        Tag tagActive = mZoneManager.getActiveTag();
        if (tagActive == null) {
            mMainView.presentTagCreate();
            mStartupMode = StartupMode.CREATE;

        } else {
            mMainView.presentTag(tagActive);
            mStartupMode = StartupMode.TAG;
        }
        mZoneManager.connectToLocationServices();
    }

    /**
     * Once connected to the zone manager continue with setup.
     */
    @Override
    public void onZoneManagerConnectedToLocationServices() {
        Log.i(TAG, "connected to the zone manager");
        switch (mStartupMode) {
            case CREATE:
                ((TagCreateFragment) mMainView.getVisibleFragment()).setZoneManager(mZoneManager);
                mZoneManager.startMonitoringPreciseLocationChanges();
                break;
            case TAG:
                mZoneManager.stopMonitoringLocationChanges();
                break;
        }
        mIsAppVisible = true;
    }

    // TODO: does it work if we connect in resume and disconnect in pause. probably not with
    // mock locations but maybe with real locations?

    /**
     * Handle the app moving into the background.
     *
     * If the service isn't available then don't attempt to monitor for significant location
     * changes.
     */
    @Override
    protected void onPause() {
        mIsAppVisible = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mBroadcastReceiverGeofenceEnter);
        if (mServiceAvailabilityMonitor.getState() == ServiceAvailabilityMonitor.State.AVAILABLE) {
            mZoneManager.startMonitoringSignificantLocationChanges();
        }
        mMainView.presentNothing();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mZoneManager.disconnect(); // TODO: maybe do this in `onPause`?
        super.onDestroy();
    }

    @Override
    public void onUpdatedZone() {
        Log.i(TAG, "updated zone");
    }

    /**
     * Handle the user submitting a request to the server to create a new tag.
     *
     * If the app is in the background when this message is received ignore it because we don't
     * want to stop monitoring for significant location updates and there is no point in updating
     * the UI. Although this is unlikely in this handler because the latency between the user
     * submitting a request and this delegate receiving the message doesn't leave much of an
     * opportunity for the app to be made inactive.
     */
    @Override
    public void onTagCreationSubmitted() {
        if (!mIsAppVisible) {
            return;
        }
        mMainView.presentPending();
    }

    /**
     * Handle the user successfully creating a tag.
     *
     * If the app is in the background when this message is received ignore it because we don't
     * want to stop monitoring for significant location updates and there is no point in updating
     * the UI.
     */
    @Override
    public void onTagCreationSucceed() {
        if (!mIsAppVisible) {
            return;
        }
        mZoneManager.stopMonitoringLocationChanges();
        mMainView.presentTagCreationSuccess();
    }

    /**
     * Handle an error occurring when the user tried to create a tag.
     *
     * If the app is in the background when this message is received ignore it because we don't
     * want to stop monitoring for significant location updates and there is no point in updating
     * the UI.
     */
    @Override
    public void onTagCreationFailure() {
        if (!mIsAppVisible) {
            return;
        }
        mZoneManager.stopMonitoringLocationChanges();
        mMainView.presentTagCreationFailure();
    }

    /**
     * Handle geofence enter events sent by the `GeofenceTransitionService`.
     *
     * No action is taken but receiving the event informs the service that the app is in the
     * foreground and prevents it from dispatching a notification.
     */
    private BroadcastReceiver mBroadcastReceiverGeofenceEnter = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {}
    };
}
