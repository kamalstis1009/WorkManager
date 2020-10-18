package com.subra.workmanager.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.subra.workmanager.ConstantKey;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordForegroundService extends Service {

    private static final String TAG = "ForegroundService";
    private boolean isStarted;
    private MediaRecorder mRecorder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null) {
            if (intent.getExtras().containsKey(ConstantKey.RECORDING)) {
                final String value = intent.getStringExtra(ConstantKey.RECORDING);
                if (value.equals("START")) {
                    startRecording();
                    Toast.makeText(this, "Start Recording", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        Toast.makeText(this, "Stop Recording", Toast.LENGTH_SHORT).show();
    }

    private void startRecording() {
        try {
            String path = getApplicationContext().getFilesDir().getPath();
            File file = new File(path, "records"); //child folder
            String mRecordName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            mRecorder = new MediaRecorder();
            /*mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mRecorder.setAudioChannels(1);
            mRecorder.setAudioSamplingRate(8000);
            mRecorder.setAudioEncodingBitRate(44100);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);*/

            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            if (!file.exists()){
                file.mkdirs();
            }
            String mRecordFilePath = file+"/" + "REC_" + mRecordName + ".3gp";
            mRecorder.setOutputFile(mRecordFilePath);

            mRecorder.prepare();
            mRecorder.start();
            isStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopForegroundService() {
        stopRecording();
        // Stop foreground service and remove the notification.
        stopForeground(true);
        // Stop the foreground service.
        stopSelf();
    }

    private void stopRecording() {
        if (isStarted && mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset(); // You can reuse the object by going back to setAudioSource() step
            mRecorder.release();
            mRecorder = null;
            isStarted = false;
            //publishResults();
        }
    }

    private void publishResults() {
        Intent intent = new Intent(ConstantKey.BROADCAST_RECEIVER);
        intent.putExtra("RECORD_RESULT", Activity.RESULT_OK);
        sendBroadcast(intent);
    }

    /*private void getInternalStorageFiles() {
        //String path = Environment.getExternalStorageDirectory().toString() + "/Testing"; //getExternalFilesDir(), getExternalCacheDir(), or getExternalMediaDir()
        //String path = this.getApplicationContext().getFilesDir() + "/system_sound"; //file.getAbsolutePath()
        //String[] listOfFiles = Environment.getExternalStoragePublicDirectory (Environment.DIRECTORY_DOWNLOADS).list();

        String path = getActivity().getFilesDir().getPath() + "/records/";
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                mRecordList.add(new FileModel(file.getName(), path));
            }
        }
        if (mRecordList != null && mRecordList.size() > 0) {
            initRecyclerView2(mRecordRecyclerView, mRecordList);
        }
    }*/


}