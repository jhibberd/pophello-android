package com.example.pophello.app.controller;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.pophello.app.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Handle a Google Cloud Messaging notification indicating that one of the user's tags has been
 * discovered by another user.
 *
 * The user is informed with a notification.
 *
 * This implementation is based on:
 * http://developer.android.com/google/gcm/client.html
 */
public class GcmIntentService extends IntentService {

    private final static String TAG = "GcmIntentService";

    // TODO: why is this public?
    // TODO: don't use this ID because if conflicts with the other notification + we probably want
    //       to stack these types of notifications
    public static final int NOTIFICATION_ID = 2;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        assert extras != null;
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String message = extras.toString();
                Log.i(TAG, "Received GCM message: " + message);
                sendNotification(message);

            } else {
                // GCM is likely to introduce new message types in the future so ignore all
                // messages that aren't recognised
                Log.i(TAG, "Received unknown GCM message: " + messageType);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // TODO: cleanup
    /**
     * Notify the user that another user has discovered one of their tags.
     */
    private void sendNotification(String msg) {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}