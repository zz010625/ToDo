package com.example.todo.task.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;


import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationService extends Service {
    private LocalBroadcastManager localBroadcastManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //通过发送广播来发送通知
        sendNotification(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification(Intent intent) {
        Intent sendBroadcast = new Intent();
        sendBroadcast.setAction("SEND_NOTIFICATION");
        sendBroadcast.putExtra("taskName",intent.getStringExtra("taskName"));
        sendBroadcast.putExtra("taskEndTime",intent.getStringExtra("taskEndTime"));
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(sendBroadcast);
        //人工震动
        Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }



}
