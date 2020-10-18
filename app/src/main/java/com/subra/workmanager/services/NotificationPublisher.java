package com.subra.workmanager.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class NotificationPublisher extends BroadcastReceiver {

    private static final String TAG = "MyAlarmManager";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper mHelper = new NotificationHelper(context);
        NotificationCompat.Builder mBuilder = mHelper.getChannelNotification();
        mHelper.getManager().notify(1, mBuilder.build());
        Notification mNotification = mBuilder.build();
        mNotification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotification.defaults |= Notification.DEFAULT_SOUND;
    }

}
