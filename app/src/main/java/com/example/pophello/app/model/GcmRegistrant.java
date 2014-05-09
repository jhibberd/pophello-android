package com.example.pophello.app.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.example.pophello.app.controller.MainActivity;
import com.example.pophello.app.model.server.EndpointDevicesPOST;
import com.example.pophello.app.utility.CurrentUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


/**
 * Registers the device/application with the Google Cloud Messaging service, then updates the
 * server with the device ID.
 *
 * Base on the implementation:
 * http://developer.android.com/google/gcm/client.html
 *
 * Uses the Google Project:
 * https://console.developers.google.com/project/apps~tough-fact-574
 */
public class GcmRegistrant implements EndpointDevicesPOST.OnResponseListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private static final String SENDER_ID = "479655286828";

    private GoogleCloudMessaging mGcm;
    private final Activity mActivity;

    public GcmRegistrant(Activity activity) {
        mActivity = activity;
    }

    public void register() {

        // TODO: aren't we already checking that Google Play Services is installed as part of the
        //       service availability monitor?

        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(mActivity);
            String regId = getRegistrationId(mActivity);
            if (regId.isEmpty()) {
                registerInBackground();
            } else {
                Log.i(TAG, "Device already registered for GCM");
            }
        } else {
            Log.w(TAG, "No valid Google Play Services APK found");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a
     * dialog that allows users to download the APK from the Google Play Store or enable it in the
     * device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(
                        resultCode, mActivity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported");
                mActivity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Return the current GCM service registration ID for the device/application.
     *
     * Return empty string if the device isn't currently registered.
     *
     * Check if the app was updated; if so, the registration ID must be cleared since the existing
     * registration ID is not guaranteed to work with the new app version.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed");
            return "";
        }
        return registrationId;
    }

    /**
     * Return the application's shared preferences.
     */
    private SharedPreferences getGCMPreferences() {
        return mActivity.getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * Return the application's current version.
     */
    private int getAppVersion() {
        try {
            PackageManager packageManager = mActivity.getPackageManager();
            assert packageManager != null;
            PackageInfo packageInfo = packageManager.getPackageInfo(mActivity.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e); // shouldn't happen
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     *
     * Stores the registration ID and application version in the application's shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(mActivity);
                    }
                    String regId = mGcm.register(SENDER_ID);
                    sendRegistrationIdToBackend(regId);
                    storeRegistrationId(regId);

                } catch (IOException e) {
                    // TODO: if there is an error don't just keep trying to register, perform
                    //       exponential back-off
                    Log.e(TAG, "Failed to register with GCM: " + e.getMessage());
                }
                return null;
            }
        }.execute(null, null, null);
    }

    /**
     * Send the registration ID (device ID) to the server so that it can be used in addressing the
     * device for push notifications.
     */
    private void sendRegistrationIdToBackend(String regId) {
        CurrentUser currentUser = new CurrentUser(mActivity);
        new EndpointDevicesPOST(mActivity, currentUser.getUserId(), regId, this).call();
    }

    @Override
    public void onEndpointDevicesPOSTResponseSuccess() {
        Log.i(TAG, "Registered device for push notifications");
    }

    @Override
    public void onEndpointDevicesPOSTResponseFailed() {
        Log.e(TAG, "Failed to register device for push notifications");
    }

    /**
     * Stores the registration ID and current application version in the application's shared
     * preferences.
     */
    private void storeRegistrationId(String regId) {
        SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
