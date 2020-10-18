package com.subra.workmanager.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.subra.workmanager.ConstantKey;
import com.subra.workmanager.MainActivity;
import com.subra.workmanager.R;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NotificationHandler extends Worker {

    public NotificationHandler(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void scheduleReminder(long duration, Data data, String tag) {
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationHandler.class)
                .setInitialDelay(duration, TimeUnit.MILLISECONDS).addTag(tag)
                .setInputData(data).build();
        WorkManager.getInstance().enqueue(notificationWork);
    }

    public static void cancelReminder(String tag) {
        WorkManager instance = WorkManager.getInstance();
        instance.cancelAllWorkByTag(tag);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        String title = getInputData().getString(ConstantKey.EXTRA_TITLE);
        String text = getInputData().getString(ConstantKey.EXTRA_TEXT);
        int id = (int) getInputData().getLong(ConstantKey.EXTRA_ID, 0);
        sendNotification(title, text, id);
        return Result.success();
    }

    private void sendNotification(String title, String text, int id) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(ConstantKey.EXTRA_ID, id);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        Objects.requireNonNull(notificationManager).notify(id, notification.build());
    }

}
