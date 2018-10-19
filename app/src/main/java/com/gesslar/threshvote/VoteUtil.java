package com.gesslar.threshvote;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VoteUtil {
    protected static Map<String, Date> mIPList = new HashMap<>();
    protected static boolean mLoaded;

    // remove all expired IPs. All IPs that have not been voted on in the last
    // 12 hours should be removed.
    public static boolean canVoteFromIP(Context context, String ip) {
        if(ThreshVoteIntentService.mBlackListedSSID) return false;
        if(ip.isEmpty()) return false;

        if(!mLoaded) { mIPList = readIPList(context); mLoaded = true; }
        if(mIPList.containsKey(ip))
        {
            Date now = new Date(System.currentTimeMillis());
            Date timer = mIPList.get(ip);
            return now.after(timer);
        }
        return true ;
    }

    public static Long timeRemainingFromIP(Context context, String ip)
    {
        if(ThreshVoteIntentService.mBlackListedSSID) return -1L;
        if(ip.isEmpty()) return -1L;

        if(!mLoaded) { mIPList = readIPList(context); mLoaded = true; }
        if(mIPList.containsKey(ip))
        {
            Date now = new Date(System.currentTimeMillis());
            Date timer = mIPList.get(ip);
            if(timer.after(now)) return timer.getTime() - now.getTime();
            else return 0L;
        }
        return 0L ;
    }

    public static Long timerFromIP(Context context, String ip)
    {
        if(!mLoaded) { mIPList = readIPList(context); mLoaded = true; }
        if (mIPList.containsKey(ip)) return mIPList.get(ip).getTime();
        else return 0L;
    }

    public static Map <String,Date> readIPList(Context context) {
        Resources res = context.getResources();
        String filename = res.getString(R.string.file_ip_addresses);
        Date now = new Date(System.currentTimeMillis());
        Map<String, Date> ip_list = new HashMap<>();

        // check if file exists
        String path = context.getFilesDir().getAbsolutePath();
        File file = new File (path, filename);
        if(!file.exists()) return ip_list;

        // load the IPs from file
        try {
            FileInputStream is = context.openFileInput(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String left, right, line;
            Date time;
            Integer pos;

            while ((line = r.readLine()) != null)
            {
                pos = line.indexOf("|");
                left = line.substring(0, pos);
                right = line.substring(pos + 1, line.length());
                time = new Date(Long.parseLong(right));
                if(time.after(now)) ip_list.put(left, time);
            }
            r.close();
            is.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ip_list;
    }

    public static void saveVotedIP(Context context, String ip, Long duration) {
        Resources res = context.getResources();
        String filename = res.getString(R.string.file_ip_addresses);
        String path = context.getFilesDir().getAbsolutePath();

        Map<String, Date> ip_list = readIPList(context);
        if(ip_list.containsKey(ip)) {
            ip_list.remove(ip);
            ip_list.put(ip, new Date(System.currentTimeMillis() + duration));
        } else ip_list.put(ip, new Date(System.currentTimeMillis() + duration));

        File file = new File (path, filename);
        if(file.exists()) {
            if(!file.delete()) {
                Log.e(context.getPackageName(), "saveVotedIP: UNABLE TO DELETE IP FILE");
                return ;
            }
        }

        try {
            FileOutputStream os = context.openFileOutput(filename, Context.MODE_PRIVATE);
            Log.d(context.getPackageName(), "FILENAME: " + os.getFD().toString());
            for(Map.Entry<String,Date> entry : ip_list.entrySet())
            {
                String line;

                line = entry.getKey() + "|" + Long.toString(entry.getValue().getTime()) + "\n";
                os.write(line.getBytes());
            }

            os.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mIPList = ip_list;
        Log.d("VoteUtil", mIPList.toString());
    }

    public static void clearVotes(Context context)
    {
        mIPList.clear();

        Resources res = context.getResources();
        String filename = res.getString(R.string.file_ip_addresses);
        File dir = context.getFilesDir();
        File file = new File(dir, filename);
        if(!file.delete()) {
            Log.e(context.getPackageName(), "clearVotes: UNABLE TO DELETE IP FILE.");
        }

        ThreshVoteIntentService.startActionDetermineCanVote(context);
    }

    public static void removeIP(String ipaddress)
    {
        mIPList.remove(ipaddress);
    }
}
