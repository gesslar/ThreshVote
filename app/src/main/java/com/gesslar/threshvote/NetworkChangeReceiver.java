package com.gesslar.threshvote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NetworkChangeReceiver", "Network state changed: " + NetworkUtil.getConnectivityStatusString(context));
        ThreshVoteIntentService.startActionNetworkStatusChanged(context, NetworkUtil.getConnectivityStatus(context));
    }
}
