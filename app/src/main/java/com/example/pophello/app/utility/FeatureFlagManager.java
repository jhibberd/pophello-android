package com.example.pophello.app.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Provides access to whether optional app features are enabled.
 */
public class FeatureFlagManager {

    private static final String TAG = "FeatureFlagManager";
    private static final String FLAG_LOCATION_MOCKING = "com.example.pophello.FeatureLocationMocking";
    private static final String FLAG_BUG_SENSE = "com.example.pophello.FeatureBugSense";

    private final Bundle mBundle;

    public FeatureFlagManager(Context context) {
        mBundle = getBundle(context);
        if (mBundle == null) {
            Log.e(TAG, "failed to get application bundle");
        }
    }

    public boolean isBugSenseEnabled() {
        return mBundle.getBoolean(FLAG_BUG_SENSE);
    }

    public boolean isLocationMockingEnabled() {
        return mBundle.getBoolean(FLAG_LOCATION_MOCKING);
    }

    private Bundle getBundle(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return null;
        }
        String packageName = context.getPackageName();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return applicationInfo.metaData;
    }
}
