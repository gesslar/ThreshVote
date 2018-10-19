package com.gesslar.threshvote;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.StringBufferInputStream;

public class DeviceBootReceiver extends BroadcastReceiver {
    Boolean connected;
    String characterName, ipAddress;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d("DeviceBootReceiver", "Booted");
            ThreshVoteIntentService.startActionInitData(context);
        }
    }
}
