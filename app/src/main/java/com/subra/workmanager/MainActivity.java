package com.subra.workmanager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.subra.workmanager.services.MyJobService;
import com.subra.workmanager.services.NotificationHandler;
import com.subra.workmanager.services.NotificationPublisher;
import com.subra.workmanager.services.NotifyWorker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 101;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //----------------------------------------| Job Scheduler
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //jobScheduler();
        }

        //----------------------------------------| Time wise Notification
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 16);
            c.set(Calendar.MINUTE, 2);
            c.set(Calendar.SECOND, 0);
            startAlarm(c);
        }*/

        //----------------------------------------| Worker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //workRequest();
        }

        //----------------------------------------|
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Generate notification string tag
            String tag = generateKey();
            //Get time before alarm
            int minutesBeforeAlert = 1;
            long alertTime = getAlertTime(minutesBeforeAlert) - System.currentTimeMillis();
            long current =  System.currentTimeMillis();
            Log.d(TAG, "Alert time - " + alertTime + "Current time " + current);
            int random = (int )(Math.random() * 50 + 1);
            //Data
            Data data = createWorkInputData("TITLE", "TEXT", random);
            NotificationHandler.scheduleReminder(alertTime, data, tag);

            //OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationHandler.class).build();
            //WorkManager.getInstance().enqueue(notificationWork);
        }

        //----------------------------------------| Service
        //startMyService(ConstantKey.RECORDING, "START");
        //stopMyService();
    }

    private Data createWorkInputData(String title, String text, int id){
        return new Data.Builder()
                .putString(ConstantKey.EXTRA_TITLE, title)
                .putString(ConstantKey.EXTRA_TEXT, text)
                .putInt(ConstantKey.EXTRA_ID, id)
                .build();
    }
    private long getAlertTime(int userInput){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, userInput);
        return cal.getTimeInMillis();
    }
    private String generateKey(){
        return UUID.randomUUID().toString();
    }

    //========================================| Worker
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void workRequest() {
        //we set a tag to be able to cancel all work of this type if needed
        String workTag = "notificationWork";

        //store DBEventID to pass it to the PendingIntent and open the appropriate event page on notification click
        Data inputData = new Data.Builder().putInt("TAG"/*DBEventIDTag*/ , 1/*DBEventID*/).build();
        // we then retrieve it inside the NotifyWorker with:
        // final int DBEventID = getInputData().getInt(DBEventIDTag, ERROR_VALUE);

        // Time to show notification at
        LocalDateTime timeAt = LocalDate.now().atTime(16, 43);
        LocalDateTime timeNow = LocalDateTime.now();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                //.setInitialDelay(calculateDelay(event.getDate()), TimeUnit.MILLISECONDS)
                .setInitialDelay(Duration.between(timeNow, timeAt))
                .setInputData(inputData)
                .addTag(workTag)
                .build();

        WorkManager.getInstance(this).enqueue(notificationWork);

        //alternatively, we can use this form to determine what happens to the existing stack
        // WorkManager.getInstance(context).beginUniqueWork(workTag, ExistingWorkPolicy.REPLACE, notificationWork);

        //ExistingWorkPolicy.REPLACE - Cancel the existing sequence and replace it with the new one
        //ExistingWorkPolicy.KEEP - Keep the existing sequence and ignore your new request
        //ExistingWorkPolicy.APPEND - Append your new sequence to the existing one,
        //running the new sequence's first task after the existing sequence's last task finishes
    }

    //========================================| AlarmManager
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        //alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP, c.getTimeInMillis(), pendingIntent);
        //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    //========================================| JobService
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void jobScheduler() {
        final JobScheduler scheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(this, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // require unmetered network
                .setPersisted(true)
                .setMinimumLatency(10 * 1000) // wait at least
                .setOverrideDeadline(60 * 1000) // maximum delay
                //.setRequiresDeviceIdle(true); // device should be idle
                //.setRequiresCharging(false); // we don't care if the device is charging or not
                .build();
        if (scheduler != null) {
            // Checking if job is already running
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (scheduler.getPendingJob(JOB_ID) == jobInfo)
                    return;
            }
            scheduler.schedule(jobInfo);
        }
    }
}