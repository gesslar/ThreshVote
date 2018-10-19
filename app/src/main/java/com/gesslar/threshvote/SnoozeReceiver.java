package com.gesslar.threshvote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SnoozeReceiver", "Snoozed for IP: " + ThreshVoteIntentService.mIPAddress);
        AlarmUtil.snoozeAlarm(context, intent.getStringExtra("ip_address"));
    }
}
