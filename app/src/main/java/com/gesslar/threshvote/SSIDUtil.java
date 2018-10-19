package com.gesslar.threshvote;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gesslar on 2016-03-11.
 */
public class SSIDUtil {
    private static List<String> BlackList = new ArrayList<>();
    private static Boolean mLoaded = false;

    public static boolean isSSIDBlackListed(Context context, String ssid) {
        if(ssid == null || ssid.isEmpty()) return false;

        if(!mLoaded) { BlackList = readBlackList(context); mLoaded = true; }
        Log.d("BLACKLIST", "BLACKLIST:" + BlackList.toString());
        return BlackList.contains(ssid);
    }

    public static List<String> readBlackList(Context context) {
        Resources res = context.getResources();
        String filename = res.getString(R.string.file_blacklist);
        List<String> blacklist = new ArrayList<>();
        Log.d("SSSID", "READBLACKLIST");
        // check if file exists
        String path = context.getFilesDir().getAbsolutePath()+"/"+filename;
        File file = new File (path);

        if(!file.exists()) return blacklist;

        // load the IPs from file
        try {
            FileInputStream is = context.openFileInput(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = r.readLine()) != null)
            {
                blacklist.add(line);
                Log.d("READING", "LINE: " + line);
            }
            r.close();
            is.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return blacklist;
    }

    public static void blacklistSSID(Context context, String ssid, Boolean blacklisted) {
        Log.d("SSID", "MOO");
        Log.d("SSID", "MOO2: " + ssid);
        if (blacklisted) {
            if (BlackList.contains(ssid)) {
                Log.d("SSID", "ONE");
                return;
            } else {
                Log.d("SSID", "TWO");
                BlackList.add(ssid);
            }
        } else {
            if (BlackList.contains(ssid)) {
                Log.d("SSID", "THREE");
                BlackList.remove(ssid);
            } else {
                Log.d("SSID", "FOUR");
                return;
            }
        }

        Log.d("SSID", "MOO3");

        Resources res = context.getResources();
        String path = context.getFilesDir().getAbsolutePath();
        String filename = res.getString(R.string.file_blacklist);
        File file = new File (path, filename);

        if (BlackList.isEmpty()) {
            file.delete();
        } else {
            try {
                FileOutputStream os = context.openFileOutput(filename, Context.MODE_PRIVATE);
                Iterator<String> it = BlackList.iterator();
                String line;
                while (it.hasNext()) {
                    line = it.next() + "\n";
                    os.write(line.getBytes());
                    Log.d("SSID", line);
                }
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ThreshVoteIntentService.startActionDetermineBlackisted(context);
    }
}
