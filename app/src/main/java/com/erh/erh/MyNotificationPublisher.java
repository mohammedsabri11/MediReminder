package com.erh.erh;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;


public class MyNotificationPublisher extends BroadcastReceiver {
 
    private static final int NOTIFICATION_IDD = 10001;


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("SEI_RECEIVER_ALARM_ACTION")) {
            showNotification(context);
        }
    }




    void showNotification(Context context) {
        String title = "Warning Expire Date Detected";
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_no) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(" Expire Date Detected")// message for notification
                .setAutoCancel(true) // clear notification after click
                .setDefaults(Notification.DEFAULT_SOUND);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(NOTIFICATION_IDD, mBuilder.build());
    }
}

/*
 @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("SEI_RECEIVER_ALARM_ACTION")) {
            LocalDate date = strToDate(intent.getStringExtra("date"));
            if (date != null) {
                alarm(date, context);
                Toast.makeText(context, "SEI_RECEIVER_ALARM_ACTION", Toast.LENGTH_SHORT).show();
            }

        } else
            showNotification(context);
    }

 public LocalDate strToDate(String input) {
        Locale.setDefault(Locale.ENGLISH);
        DateTimeFormatter f = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("uuuu-MM-dd").toFormatter();
        try {
            LocalDate localDate = LocalDate.parse(input, f);

            if (localDate != null) {
                return localDate;

            }
            return null;

        } catch (DateTimeParseException e) {
            return null;
        }

    }

    public void alarm(LocalDate localDate, Context context) {

        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date curDate = new Date();
        Calendar current = Calendar.getInstance();
        current.setTime(curDate);
        int hours = current.get(Calendar.HOUR_OF_DAY);
        int min = current.get(Calendar.MINUTE);

        Calendar calendarToSchedule = Calendar.getInstance();
        calendarToSchedule.setTimeInMillis(System.currentTimeMillis());
        calendarToSchedule.clear();
        calendarToSchedule.setTime(date);


        calendarToSchedule.set(Calendar.HOUR_OF_DAY, hours);
        calendarToSchedule.set(Calendar.MINUTE, min + 1);
        calendarToSchedule.set(Calendar.SECOND, 00);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long reminderDateTimeInMilliseconds = 000;
        reminderDateTimeInMilliseconds = calendarToSchedule.getTimeInMillis();


        Intent intent = new Intent("ALARM_ACTION23");

        PendingIntent operation = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, reminderDateTimeInMilliseconds, operation);


    }*/
