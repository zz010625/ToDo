package com.example.todo.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import com.example.todo.task.service.NotificationService;


public class SendNotificationUtil {
    private static PendingIntent pendingIntent;
    public  static void setAlarmToService(long time, Context context,String taskName,String taskEndTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        /**
         * 执行任务唤醒的时候PendingIntent，在8.0版本之后获取方式不同
         * 启动服务不在是startService() 而是startForegroundService()
         */
        Intent intent=new Intent(context,NotificationService.class);
        intent.putExtra("taskName",taskName);
        intent.putExtra("taskEndTime",taskEndTime);
        pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long triggerAtTime = SystemClock.elapsedRealtime() + time; //获取手机开机到现在的时间 加上延时的时间(ms)

        //不同版本使用AlarmManager的唤醒方法不同
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        }
    }
}
