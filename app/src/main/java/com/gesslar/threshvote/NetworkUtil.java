package com.gesslar.threshvote;

/**
 * Created by gesslar on 2016-02-13.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public class NetworkUtil {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    public static int getConnectivityStatus(Context context) {
        String key = context.getResources().getString(R.string.pref_key_mode);
        String defaultMode = context.getResources().getString(R.string.pref_mode_option_default);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String mode = sharedPreferences.getString(key, defaultMode);
        Boolean opportunistic = mode.equals(context.getResources().getString(R.string.pref_mode_option_opportune));

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = NetworkUtil.getConnectivityStatus(context);
        String status = null;
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    public static String getConnectionType(Context context) {
        int type;

        type = getConnectivityStatus(context);
        if(type == TYPE_WIFI) return "WiFi";
        else if (type == TYPE_MOBILE) return "Data";
        else return "None";
    }


}

