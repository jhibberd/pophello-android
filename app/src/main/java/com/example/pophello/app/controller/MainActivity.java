package com.example.pophello.app.controller;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Window;

import com.bugsense.trace.BugSenseHandler;
import com.example.pophello.app.R;
import com.example.pophello.app.model.MainView;
import com.example.pophello.app.model.Tag;
import com.example.pophello.app.model.TagNotification;
import com.example.pophello.app.model.ZoneManager;
import com.example.pophello.app.model.data.TagsStore;
import com.example.pophello.app.utility.FeatureFlagManager;
import com.example.pophello.app.view.TagCreateFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends ActionBarActivity implements
        ZoneManager.ConnectionCallbacks,
        ZoneManager.OnEstablishedZoneListener,
        TagCreateFragment.OnTagCreateListener {

    private static final String TAG = "MainActivity";

    private enum StartupMode {
        TAG, CREATE
    }

    private ZoneManager mZoneManager;
    private TagsStore mTagsStore;
    private MainView mMainView;
    private StartupMode mStartupMode;
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
        mTagsStore = new TagsStore(this);
        mMainView = new MainView(getFragmentManager());
    }

    /** Handle the activity coming back into view.
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
        new TagNotification(this).dismissAll();
        Tag tagActive = mZoneManager.getActiveTag();

        if (tagActive == null) {
            mMainView.presentTagCreate();
            mStartupMode = StartupMode.CREATE;

        } else {
            mMainView.presentTag(tagActive);
            mStartupMode = StartupMode.TAG;
        }

        // TODO: part of service availability monitor
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play services is unavailable");
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
    @Override
    protected void onPause() {
        mIsAppVisible = false;
        mZoneManager.startMonitoringSignificantLocationChanges();
        mTagsStore.close();
        mMainView.presentNothing();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mZoneManager.disconnect(); // TODO: maybe do this in `onPause`?
        super.onDestroy();
    }

    @Override
    public void onEstablishedZone() {
        Log.i(TAG, "established zone (which may be empty)");
    }

    /**
     * Handle the user successfully creating a tag.
     *
     * If the app is in the background when this message is received ignore it because we don't
     * want to stop monitoring for significant location updates and there is no point in updating
     * the UI.
     */
    @Override
    public void onTagCreateSucceed() {
        if (!mIsAppVisible) {
            return;
        }
        mZoneManager.stopMonitoringLocationChanges();
        mMainView.presentTagCreateSuccess();
    }

    /**
     * Handle an error occurring when the user tried to create a tag.
     *
     * If the app is in the background when this message is received ignore it because we don't
     * want to stop monitoring for significant location updates and there is no point in updating
     * the UI.
     */
    @Override
    public void onTagCreateFailure() {
        if (!mIsAppVisible) {
            return;
        }
        mZoneManager.stopMonitoringLocationChanges();
        mMainView.presentTagCreateFailure();
    }
}
