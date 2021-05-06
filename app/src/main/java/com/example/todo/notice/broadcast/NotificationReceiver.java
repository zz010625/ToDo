package com.example.todo.notice.broadcast;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;


import com.example.todo.HomeActivity;
import com.example.todo.R;

import java.util.ArrayList;


import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationReceiver extends BroadcastReceiver {
    private String notificationId = "0";
    private String notificationName = "taskNotification";
    private Context mContext;
    private ArrayList data=new ArrayList();
    private boolean send=true;
    public NotificationReceiver(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName=intent.getStringExtra("taskName");
        String taskEndTime=intent.getStringExtra("taskEndTime");
        data.add(taskName+taskEndTime);
        /**
         * 若用户只返回退出APP 没用彻底kill APP 则之前的service还会保留 所以此时用户再次打开APP时 就会存在两个service对象 则会推送两次重复的通知
         * 所有在这里加个验证筛选 保证重复的任务不会推送通知两次
         */
        for (int i = 0; i <data.size()-1; i++) {
            if (data.get(i).equals(data.get(data.size()-1))){
                send=false;
                break;
            }
        }
        //在广播中发送通知
        if (send){
            if (intent.getAction().equals("SEND_NOTIFICATION")) {
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                        new Intent(mContext, HomeActivity.class), 0);

                //创建Notification
                Notification notify;
                NotificationManager manager = (NotificationManager) mContext
                        .getSystemService(NOTIFICATION_SERVICE);
                //不同版本 通知创建不同
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notify = new Notification.Builder(mContext, notificationId)
                            .setSmallIcon(R.mipmap.ic_task)
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_notification_task))
                            .setContentTitle("任务: "+taskName+" 正在进行")
                            .setContentText("结束时间:"+taskEndTime)
                            .setContentIntent(pendingIntent)
                            .setNumber(1)
                            .setAutoCancel(true)
                            .build();

                    NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
                    channel.setLockscreenVisibility(1);
                    channel.enableVibration(true);
                    channel.setVibrationPattern(new long[]{200,800,200,800,200,200});
                    manager.createNotificationChannel(channel);
                }else {
                    notify = new Notification.Builder(mContext)
                            .setSmallIcon(R.mipmap.ic_task)
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_notification_task))
                            .setContentTitle("任务: "+taskName+" 正在进行")
                            .setContentText("结束时间:"+taskEndTime)
                            .setContentIntent(pendingIntent)
                            .setNumber(1)
                            .setAutoCancel(true)
                            .setVibrate(new long[]{200,800,200,800,200,200})
                            .build();

                }
                manager.notify(Integer.valueOf(notificationId), notify);
            }
            notificationId=String.valueOf((Integer.valueOf(notificationId)+1));
        }

    }

}
