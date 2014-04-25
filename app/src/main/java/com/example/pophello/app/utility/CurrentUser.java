package com.example.pophello.app.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class CurrentUser {

    private static final String TAG = "CurrentUser";
    private final Bundle mBundle;

    public CurrentUser(Context context) {
        mBundle = getBundle(context);
        if (mBundle == null) {
            Log.e(TAG, "failed to get application bundle");
        }
    }

    public String getUserId() {
        return mBundle.getString("com.example.pophello.UserName");
    }

    public String getUserImageUrl() {
        return mBundle.getString("com.example.pophello.UserImageUrl");
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
