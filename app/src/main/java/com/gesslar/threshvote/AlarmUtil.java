package com.gesslar.threshvote;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmUtil
{

    static public void setAlarm(Context context, Long ms)
    {
        if(ms < 0) {
            cancelAlarm(context);
            return ;
        }

        Long nextAlarm;
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("IP", ThreshVoteIntentService.mIPAddress);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nextAlarm = System.currentTimeMillis() + ms;

        long yourmilliseconds = nextAlarm;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("setAlarm", "ALARM ON '" + ThreshVoteIntentService.mIPAddress+"' SET FOR " + resultdate.toString());

        am.set(AlarmManager.RTC_WAKEUP, nextAlarm, pi);
    }

    static public void cancelAlarm(Context context)
    {
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("cancelAlarm", "ALARM CANCELED: '" + ThreshVoteIntentService.mIPAddress+"' - " + resultdate.toString());

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public static void snoozeAlarm(Context context, String ipaddress)
    {
        NotificationUtil.cancelNotifications(context);
        //cancelAlarm(context);

        VoteUtil.removeIP(ipaddress);

        Number num = context.getResources().getInteger(R.integer.long_snooze);
        Long duration = num.longValue();
        VoteUtil.saveVotedIP(
                context,
                ipaddress,
                duration
                );

        duration = VoteUtil.timeRemainingFromIP(context, ipaddress);
        setAlarm(context, duration);

    }
}
