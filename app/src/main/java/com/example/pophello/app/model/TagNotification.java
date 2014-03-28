package com.example.pophello.app.model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.pophello.app.R;
import com.example.pophello.app.controller.MainActivity;

public class TagNotification {

    private static final String TAG = "TagNotification";
    private static final int NOTIFICATION_ID = 1;
    private final Context mContext;
    private final NotificationManager mNotificationManager;

    public TagNotification(Context context) {
        mContext = context;
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Present a tag local notification.
     *
     * Used when the device touches a tag region and the app is running in the background. Only one
     * tag local notification should be visible at once (representing the most recently touched
     * tag). Since the old local notifications that this app presents are tag notifications it's
     * sufficient to dismiss all existing local notifications before presenting a new notification.
     * This is more robust that maintaining a memory reference to the visible local notification
     * object which would be lost if the app was terminated.
     *
     * Clicking the local notification will simply launch the app. When the app launches it checks
     * local storage to determine whether the device is currently in a tag geofence or not so that
     * notification doesn't need to encapsulate this data.
     */
    public void present(Tag tag) {

        Log.i(TAG, "dispatching notification");
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(mContext).
                setSmallIcon(R.drawable.ic_launcher).
                setContentTitle(tag.text).
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).
                setContentText("PopHello").
                setAutoCancel(true).
                setContentIntent(pendingIntent).
                setTicker(tag.text).
                build();

        mNotificationManager.cancelAll();
        mNotificationManager.notify(tag.id, NOTIFICATION_ID, notification);
    }

    /**
     * Dismiss a tag local notification if it's currently being presented.
     *
     * Called when the device leaves a tag region and accommodates for an exit event being received
     * after an enter event. Dismissing a notification that isn't (or has never been) visible is
     * harmless.
     */
    public void dismissIfPresenting(Tag tag) {
        mNotificationManager.cancel(tag.id, NOTIFICATION_ID);
    }

    /**
     * Dismiss all (any) tag local notifications.
     *
     * Used when the main activity is launched by the user (and not the OS, ie. in response to a
     * background event). Notifications are a means to attract the user to the app. Once the app
     * has been launched they have served their purpose.
     */
    public void dismissAll() {
        mNotificationManager.cancelAll();
    }

}
