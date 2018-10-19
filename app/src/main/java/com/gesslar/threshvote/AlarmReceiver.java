package com.gesslar.threshvote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    private static final int MY_NOTIFICATION_ID = 1234;
    @Override
    public void onReceive(Context context, Intent intent) {
        //NotificationUtil.cancelNotifications(context);
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("AlarmReceiver", "ALARM HAS GONE OFF FOR '" + ThreshVoteIntentService.mIPAddress + "' ON "+resultdate.toString());
        ThreshVoteIntentService.startActionDetermineCanVote(context);
    }
}
