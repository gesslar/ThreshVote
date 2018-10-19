package com.gesslar.threshvote;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationUtil
{
    static public void cancelNotifications(Context context)
    {
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("cancelNotification", "NOTIFICATIONS CANCELED '" + ThreshVoteIntentService.mIPAddress + "' - " + resultdate.toString());
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(ns);
        notificationManager.cancelAll();
    }
}
