package com.gesslar.threshvote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class WifiReceiver extends BroadcastReceiver {
    public WifiReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String mess;


        Log.d(context.getPackageName(), "ACTION: " + intent.getAction());
        mess = "checking wifi state";
        //Log.d(context.getPackageName(), "checking wifi state...");
        SupplicantState supState;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        supState = wifiInfo.getSupplicantState();
        mess += "\tsupplicant state";
        //Log.d(context.getPackageName(), "supplicant state: " + supState);

        switch(supState) {
            case ASSOCIATING:
                mess += "\twifi associating";
                break;
            case ASSOCIATED:
                mess += "\twifi associated";
                break;
            case AUTHENTICATING:
                mess += "\twifi authenticating";
                break;
            case INACTIVE:
                mess += "\twifi inactive";
                break;
            case COMPLETED:
                mess += "\twifi enabled and connected";
                //Log.d(context.getPackageName(), "wifi enabled and connected");
                break;
            case SCANNING:
                mess += "\twifi scanning";
                //Log.d(context.getPackageName(), "wifi scanning");
                break;
            case DISCONNECTED:
                mess += "\twifi disconnected";
                //Log.d(context.getPackageName(), "wifi disconnected");
                break;
            default:
                mess += "\t"+supState.toString();
        }

        Log.d(context.getPackageName(), mess);

    }
}
