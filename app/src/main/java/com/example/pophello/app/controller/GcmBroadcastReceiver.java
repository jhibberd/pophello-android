package com.example.pophello.app.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Handle Google Cloud Messaging notification.
 *
 * The message is passed to an IntentService for processing.
 * This class ensures that the device doesn't enter a sleep state during the processing of the
 * message.
 *
 * This implementation is based on:
 * http://developer.android.com/google/gcm/client.html
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(
                context.getPackageName(), GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}