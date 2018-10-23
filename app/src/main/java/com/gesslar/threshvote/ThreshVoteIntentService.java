package com.gesslar.threshvote;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ThreshVoteIntentService extends IntentService {
    // MY_NOTIFICATION_ID
    private static final int MY_NOTIFICATION_ID = 1234;
    private String SOUND_LIGHT, SOUND_MEDIUM, SOUND_DARK;
    private String ETHOS_GOOD, ETHOS_NEUTRAL, ETHOS_EVIL;

    // HANDLERS
    private Handler countDownHandler = new Handler();

    // DEFAULT - PRIVATE

    // DEFAULT - PUBLIC
    public static Integer mActive = 0;
    //public static Boolean mMainActivityShown = false;
    public static String mIPAddress = "";
    public static String mOldIPAddress = "";
    public static String mCharacterName = "";
    public static Boolean mDeterminingIP = false;
    public static Boolean mCanVote = false;
    public static Boolean mCanShowCountdown = false;
    public static Boolean mSoundEnabled = true;
    public static Boolean mInitialized = false;
    public static Boolean mBlackListedSSID = false;
    public static String mSSID = "";
    public static HashMap<String,Integer> mDaily;
    public static HashMap<String,Integer> mMonthly;
    public static HashMap<String,Integer> mYearly;

    // ACTION CONSTANTS
    private static final String ACTION_INIT_DATA              = "com.gesslar.threshvote.action.INIT_DATA";
    private static final String ACTION_DETERMINE_IP           = "com.gesslar.threshvote.action.DETERMINE_IP";
    private static final String ACTION_DETERMINE_CAN_VOTE     = "com.gesslar.threshvote.action.DETERMINE_CAN_VOTE";
    private static final String ACTION_NOTIFY_CAN_VOTE        = "com.gesslar.threshvote.action.NOTIFY_CAN_VOTE";
    private static final String ACTION_WIPE_TEMPORARY_DATA    = "com.gesslar.threshvote.action.WIPE_TEMPORARY_DATA";
    private static final String ACTION_START_VOTE             = "com.gesslar.threshvote.action.START_VOTE";
    private static final String ACTION_LOAD_CHARACTER_NAME    = "com.gesslar.threshvote.action.LOAD_CHARACTER_NAME";
    private static final String ACTION_SET_CHARACTER_NAME     = "com.gesslar.threshvote.action.SET_CHARACTER_NAME";
    private static final String ACTION_NETWORK_STATUS_CHANGED = "com.gesslar.threshvote.action.NETWORK_STATUS_CHANGED";
    private static final String ACTION_START_UPDATE_COUNTDOWN = "com.gesslar.threshvote.action.START_UPDATE_COUNTDOWN";
    private static final String ACTION_DETERMINE_SOUND_ENABLED = "com.gesslar.threshvote.action.ACTION_DETERMINE_SOUND_ENABLED";
    private static final String ACTION_REGISTER_VOTE          = "com.gesslar.threshvote.action.REGISTER_VOTE";
    private static final String ACTION_EMAIL_LOG              = "com.gesslar.threshvote.action.EMAIL_LOG";
    private static final String ACTION_DETERMINE_BLOCKED      = "com.gesslar.threshvote.action.DETERMINE_BLOCKED";
    private static final String ACTION_DETERMINE_SSID         = "com.gesslar.threshvote.action.DETERMINE_SSID";
    private static final String ACTION_FETCH_JSON             = "com.gesslar.threshvote.action.FETCH_JSON";
    private static final String ACTION_SHOW_LEADERBOARD       = "com.gesslar.threshvote.action.SHOW_LEADERBOARD";

    // CONSTRUCTOR, NOT REALLY USED SINCE WE USE THIS STATICALLY
    public ThreshVoteIntentService() { super("ThreshVoteIntentService"); }

    public static void startActionShowLeaderboard(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_SHOW_LEADERBOARD);
        context.startService(intent);
    }

    public static void startActionFetchJSON(Context context, String address) {
        Log.d(context.getPackageName(), "MEW");
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.putExtra(context.getString(R.string.extra_url), address);
        intent.setAction(ACTION_FETCH_JSON);
        context.startService(intent);
    }

    public static void startActionDetermineSSID(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_DETERMINE_SSID);
        context.startService(intent);
    }

    public static void startActionDetermineBlackisted(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_DETERMINE_BLOCKED);
        context.startService(intent);
    }

    public static void startActionEmailLog(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_EMAIL_LOG);
        context.startService(intent);
    }

    public static void startActionRegisterVote(Context context){
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_REGISTER_VOTE);
        context.startService(intent);
    }

    public static void startActionDetermineSoundEnabled(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_DETERMINE_SOUND_ENABLED);
        context.startService(intent);
    }

    public static void startActionInitData(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_INIT_DATA);
        context.startService(intent);
    }

    public static void startActionDetermineIP(Context context) {
        Resources res = context.getResources();
        if(NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            Intent intent = new Intent();
            intent.setAction(res.getString(R.string.action_found_no_ip));
            context.sendBroadcast(intent);
            return ;
        }
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_DETERMINE_IP);
        context.startService(intent);
    }

    public static void startActionNotifyCanVote(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_NOTIFY_CAN_VOTE);
        context.startService(intent);
    }

    public static void startActionNotifyCanVote(Context context, Boolean hiding) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_NOTIFY_CAN_VOTE);
        intent.putExtra(context.getResources().getString(R.string.extra_hiding_main_activity), hiding);
        context.startService(intent);
    }

    public static void startActionStartVote(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_START_VOTE);
        context.startService(intent);
    }

    public static void startActionSetCharacterName(Context context, String characterName) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_SET_CHARACTER_NAME);
        intent.putExtra(context.getResources().getString(R.string.extra_character_name), characterName);
        context.startService(intent);
    }

    public static void startActionLoadCharacterName(Context context) {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_LOAD_CHARACTER_NAME);
        context.startService(intent);
    }

    public static void startActionNetworkStatusChanged(Context context, int status)
    {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_NETWORK_STATUS_CHANGED);
        intent.putExtra(context.getResources().getString(R.string.extra_network_status), status);
        context.startService(intent);
    }

    public static void startActionUpdateCountdown(Context context)
    {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_START_UPDATE_COUNTDOWN);
        context.startService(intent);
    }

    public static void startActionDetermineCanVote(Context context)
    {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_DETERMINE_CAN_VOTE);
        context.startService(intent);
    }

    public static void startActionDetermineCanVote(Context context, Boolean hiding)
    {
        Intent intent = new Intent(context, ThreshVoteIntentService.class);
        intent.setAction(ACTION_DETERMINE_CAN_VOTE);
        intent.putExtra(context.getResources().getString(R.string.extra_hiding_main_activity), hiding);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Resources res = getResources();
        //String stringIPUrl = res.getString(R.string.url_ip);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DETERMINE_IP.equals(action)) {
                handleActionDetermineIP();
            } else if (ACTION_DETERMINE_CAN_VOTE.equals(action)) {
                Boolean hiding = intent.getBooleanExtra(getString(R.string.extra_hiding_main_activity), false);
                handleActionDetermineCanVote(hiding);
            } else if (ACTION_NOTIFY_CAN_VOTE.equals(action)) {
                Boolean hiding = intent.getBooleanExtra(getString(R.string.extra_hiding_main_activity), false);
                handleActionNotifyCanVote(hiding);
            } else if (ACTION_WIPE_TEMPORARY_DATA.equals(action)) {
                handleActionWipeData();
            } else if (ACTION_START_VOTE.equals(action)) {
                handleActionStartVote();
            } else if (ACTION_SET_CHARACTER_NAME.equals(action)) {
                final String characterName = intent.getStringExtra(getResources().getString(R.string.extra_character_name));
                handleActionSetCharacterName(characterName);
            } else if(ACTION_LOAD_CHARACTER_NAME.equals(action)) {
                handleActionLoadCharacterName();
            } else if (ACTION_NETWORK_STATUS_CHANGED.equals(action)) {
                final Integer status = intent.getIntExtra(getResources().getString(R.string.extra_network_status), 0);
                handleNetworkStatusChanged(status);
            } else if (ACTION_START_UPDATE_COUNTDOWN.equals(action)) {
                handleStartUpdateCountDown();
            } else if (ACTION_INIT_DATA.equals(action)) {
                handleInitData();
            } else if(ACTION_DETERMINE_SOUND_ENABLED.equals(action)) {
                handleActionDetermineSoundEnabled();
            } else if(ACTION_REGISTER_VOTE.equals(action)) {
                handleActionRegisterVote();
            } else if(ACTION_EMAIL_LOG.equals(action)) {
                handleActionEmailLog();
            } else if(ACTION_DETERMINE_BLOCKED.equals(action)) {
                handleActionDetermineBlacklisted();
            } else if(ACTION_DETERMINE_SSID.equals(action)) {
                handleActionDetermineSSID();
            } else if(ACTION_FETCH_JSON.equals(action)) {
                String url = intent.getStringExtra(getString(R.string.extra_url));
                handleActionFetchJSON(url);
            } else if(ACTION_SHOW_LEADERBOARD.equals(action)) {
                handleActionShowLeaderBoard();
            }
        }
    }

    private void handleActionDetermineIP() {
        Resources res = getResources();
        String stringIPUrl = res.getString(R.string.url_ip);
        String returnValue;
        URL ipURL = null;

        mDeterminingIP = true;
        mOldIPAddress = mIPAddress;
        mIPAddress = "";

        final Intent updateUI = new Intent();
        updateUI.setAction(res.getString(R.string.action_update_ui));
        sendBroadcast(updateUI);

        final Intent noIP = new Intent();
        noIP.setAction(res.getString(R.string.action_found_no_ip));

        try {
            URI uri = null;
            try {
                uri = new URI(stringIPUrl);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            String host = uri.getHost();

            InetAddress address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mDeterminingIP = false;
            sendBroadcast(updateUI);
            return ;
        }


        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        String receivedText = "";
        int intDataRead = -1;
        char charDataRead;
        Boolean flag = true;

        if (NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_NOT_CONNECTED) {
            mDeterminingIP = false;
            sendBroadcast(updateUI);
            return;
        }

        try {
            ipURL = new URL(stringIPUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(ipURL == null)
        {
            mDeterminingIP = false;
            sendBroadcast(updateUI);
            return ;
        }

        try {
            urlConnection = (HttpURLConnection) ipURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Could not establish a connection
        if (urlConnection == null) {
            mDeterminingIP = false;
            sendBroadcast(updateUI);
            return ;
        }

        try {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(inputStream == null)
        {
            mDeterminingIP = false;
            sendBroadcast(updateUI);
            return ;
        }

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        while(flag) {
            try {
                intDataRead = inputStreamReader.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (intDataRead == -1) {
                flag = false;
            } else {
                flag = true;
                charDataRead = (char) intDataRead;
                receivedText += Character.toString(charDataRead);
            }
        }

        urlConnection.disconnect();

        returnValue = receivedText;

        disableConnectionReuseIfNecessary();

        if(returnValue.isEmpty())
        {
            mDeterminingIP = false;
            sendBroadcast(updateUI);
        } else {
            mIPAddress = returnValue;
            mDeterminingIP = false;
            sendBroadcast(updateUI);

            Intent intentFoundIP = new Intent();
            intentFoundIP.setAction(res.getString(R.string.action_found_ip));
            sendBroadcast(intentFoundIP);

            if(!mOldIPAddress.equals(mIPAddress)) startActionDetermineCanVote(this);
        }
    }

    /*
    private static void handleActionDetermineIP() {
        try {
            Process process;
            process = Runtime.getRuntime().exec("dig +short whoami.akamai.net");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder mess = new StringBuilder();
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    mess.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("GETTING IP", mess.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

    /**
     * required in order to prevent issues in earlier Android version.
     */
    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private void handleActionDetermineCanVote(Boolean hiding) {
        Boolean canVote = VoteUtil.canVoteFromIP(this, mIPAddress);
        Boolean canShowCountdown = !canVote;
        Boolean disconnected = NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_NOT_CONNECTED;

        if(mCharacterName.isEmpty()) handleActionLoadCharacterName();

        if(canVote) {
            if(disconnected) canVote = false;
            if(mDeterminingIP) canVote = false;
            if(mCharacterName.isEmpty()) canVote = false;
            if(mIPAddress.isEmpty()) canVote = false;
        } else {
            canShowCountdown = true;
            if(mBlackListedSSID) canShowCountdown = false;
            if(disconnected) canShowCountdown = false;
            if(mDeterminingIP) canShowCountdown = false;
            if(mCharacterName.isEmpty()) canShowCountdown = false;
            if(mIPAddress.isEmpty()) canVote = false;
        }


        mCanVote = canVote;
        mCanShowCountdown = canShowCountdown;

        Log.d("CanVote", mBlackListedSSID.toString());
        Log.d("CanVote", mCanShowCountdown.toString());

        startActionUpdateCountdown(this);

        if(canVote) {
            startActionNotifyCanVote(this, hiding);
        } else {
            AlarmUtil.setAlarm(this, VoteUtil.timeRemainingFromIP(this, mIPAddress));
            NotificationUtil.cancelNotifications(this);
        }

    }

    private void handleActionNotifyCanVote(Boolean hiding) {
        // DO WE HAVE ANY NOTIFICATIONS? IF SO REMOVE THEM
        //NotificationUtil.cancelNotifications(this);
        /*
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.cancelAll();
        */
        // @@TODO: NOTIFICATIONS ARE STILL SHOWN EVEN WHEN A DIALOG BOX IS OPEN, SAY CHANGING TO/FROM OPPORTUNISTIC MODE
        //if(!mMainActivityShown) {

        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        String outDate = resultdate.toString();
        Log.d("NotifyCanVote", "STARTING NOTIFICATION FOR '" + ThreshVoteIntentService.mIPAddress + "' ON " + outDate);

        if(mActive < 1) {
            Log.d("NotifyCanVote", "mActive < 1: '" + ThreshVoteIntentService.mIPAddress + "' ON "+ outDate);
            Intent resultIntent = new Intent(this, MainActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // creating the button intents!
            // SNOOZE BUTTON
            Intent intentSnoozeButton = new Intent(this, SnoozeReceiver.class);
            intentSnoozeButton.setAction(getResources().getString(R.string.action_snooze_reminder));
            intentSnoozeButton.putExtra(getResources().getString(R.string.extra_ip_address), mIPAddress);
            PendingIntent resultIntentSnoozeButton = PendingIntent.getBroadcast(this, 0, intentSnoozeButton, PendingIntent.FLAG_UPDATE_CURRENT);
            // VOTE BUTTON
            Intent intentVoteButton = new Intent(this, MainActivity.class);
            PendingIntent resultIntentVoteButton = PendingIntent.getActivity(this, 0, intentVoteButton, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setContentIntent(resultPendingIntent)
                            .setOngoing(true)
                            .setAutoCancel(false)
                            .setSmallIcon(R.drawable.ic_notification_vote)
                            .setContentTitle(getString(R.string.notificaton_vote_title))
                            .setContentText(getString(R.string.notification_vote_message))
                            .addAction(R.drawable.ic_notify_snooze, getString(R.string.notification_snooze_button_title), resultIntentSnoozeButton)
                            .addAction(R.drawable.ic_notify_vote, getString(R.string.notification_vote_button_title), resultIntentVoteButton);

            Integer notifOptions = Notification.FLAG_ONLY_ALERT_ONCE | Notification.DEFAULT_LIGHTS;
            String notifSound = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_key_notification_sound),
                    getString(R.string.pref_sound_option_default));

            String[] types = getResources().getStringArray(R.array.pref_values_sound);
            if (!Arrays.asList(types).contains(notifSound)) notifSound =
                    getString(R.string.pref_sound_option_default);

            // GET THE SOUND NOTIFICATION TYPE, IF AT ALL
            if(mSoundEnabled && !hiding) {
                if (notifSound.equals(getString(R.string.pref_sound_option_light))) {
                        builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_light));
                } else if (notifSound.equals(getString(R.string.pref_sound_option_medium))) {
                        builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_medium));
                } else if (notifSound.equals(getString(R.string.pref_sound_option_dark))) {
                        builder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_dark));
                }
            }

            Boolean notifVibrate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_enable_notification_vibrate), true);
            if(notifVibrate && !hiding) {
                notifOptions |= Notification.DEFAULT_VIBRATE;
            }

            builder.setDefaults(notifOptions);

            // NOW FOR THE VOTE MESSAGES ON THE NOTIFICATION: WOOHOO!
            String bigtext;
            String[] notifs = new String[0];

            String ethos = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_key_ethos),
                    getResources().getString(R.string.pref_ethos_default));

            if(ethos.equals(getString(R.string.pref_ethos_option_good))) {
                    notifs = getResources().getStringArray(R.array.notify_message_good);
            } else if (ethos.equals(getString(R.string.pref_ethos_option_neutral))) {
                    notifs = getResources().getStringArray(R.array.notify_message_neutral);
            } else if(ethos.equals(getString(R.string.pref_ethos_option_evil))) {
                    notifs = getResources().getStringArray(R.array.notify_message_evil);
            }

            Integer sz = notifs.length;
            Random rn = new Random();
            Integer num = rn.nextInt(sz);
            bigtext = notifs[num];
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigtext));

            bigtext = notifs[num] + ": " + getString(R.string.notification_vote_message_big);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigtext));

            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(MY_NOTIFICATION_ID, builder.build());
        }
        else
        {
            Log.d("NotifyCanVote", "mActive > 0: '" + ThreshVoteIntentService.mIPAddress + "' ON "+ outDate);
            Intent intent = new Intent();
            intent.setAction(getResources().getString(R.string.action_set_snack_message));
            intent.putExtra(getResources().getString(R.string.extra_snack_message), getResources().getString(R.string.status_can_vote_now));
            sendBroadcast(intent);
        }
    }

    private void handleActionWipeData()
    {
        mCharacterName = "";
        mIPAddress = "";

        Resources res = getResources();
        String filename = res.getString(R.string.file_ip_addresses);
        File dir = getFilesDir();
        File file = new File(dir, filename);
        if(!file.delete())
        {
            Log.e(getPackageName(), "UNABLE TO DELETE "+file.toString());
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear()
              .apply();

        // DO WE HAVE ANY NOTIFICATIONS? IF SO REMOVE THEM
        //NotificationUtil.cancelNotifications(this);

        // CANCEL ALARMS
        //AlarmUtil.cancelAlarm(this);

        // UPDATE THE CHARACTER NAME ON THE UI
        startActionSetCharacterName(this, mCharacterName);
    }

    private void handleActionStartVote()
    {
        if(mIPAddress.isEmpty()) {
            String noVoteMess = getString(R.string.status_unable_to_determine_ip_address);
            Intent intentNoIP = new Intent();
            intentNoIP.setAction(getResources().getString(R.string.action_set_snack_message));
            intentNoIP.putExtra(getResources().getString(R.string.extra_snack_message), noVoteMess);
            sendBroadcast(intentNoIP);
            return ;
        }

        if(!VoteUtil.canVoteFromIP(this, mIPAddress))
        {
            Long remaining = VoteUtil.timeRemainingFromIP(this, mIPAddress);
            Integer diff, hr, min, sec;

            diff = remaining.intValue();
            diff = diff / 1000;
            hr = diff / (60 * 60);
            diff -= hr * (60 * 60);
            min = diff / 60;
            diff -= min * 60;
            sec = diff;

            final String noVoteMess = String.format(getString(R.string.status_message_no_vote_normal), hr, min, sec);

            Intent intent = new Intent();
            intent.setAction(getResources().getString(R.string.action_set_snack_message));
            intent.putExtra(getResources().getString(R.string.extra_snack_message), noVoteMess);

            sendBroadcast(intent);
            return ;
        }

        final String warnMess = getResources().getString(R.string.toast_vote_warning_normal);

        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.action_set_toast_message));
        intent.putExtra(getResources().getString(R.string.extra_toast_message), warnMess);
        sendBroadcast(intent);

        Integer intDuration = getResources().getInteger(R.integer.long_thirteen_hours);
        Long duration = intDuration.longValue();

        VoteUtil.saveVotedIP(
                this,
                mIPAddress,
                duration
        );

        AlarmUtil.setAlarm(this, VoteUtil.timeRemainingFromIP(this, mIPAddress));
        startActionRegisterVote(this);
        //handleRegisterVote();

        String voteURL = getResources().getString(R.string.url_vote);
        Uri webpage = Uri.parse(voteURL);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(webIntent);    }

    private void handleActionLoadCharacterName()
    {

        mCharacterName = PreferenceManager.
                getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_key_character_name), "");
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCharacterName = prefs.getString(getResources().getString(R.string.pref_character_name), "");
        */
        /*
        mCharacterName =
                PreferenceManager.getSharedPreferences().getString(getResources().getString(R.string.pref_character_name), "");
        */
        Intent updateUI = new Intent();
        updateUI.setAction(getString(R.string.action_update_ui));
        sendBroadcast(updateUI);
    }

    private void handleActionSetCharacterName(String characterName)
    {
        mCharacterName = characterName;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getResources().getString(R.string.pref_key_character_name), mCharacterName);
        editor.apply();

        // DONE UPDATING, NOTIFY THE UI
        Intent updateUI = new Intent();
        updateUI.setAction(getString(R.string.action_update_ui));
        sendBroadcast(updateUI);
    }

    private void handleNetworkStatusChanged(Integer status) {
        if(status == NetworkUtil.TYPE_NOT_CONNECTED) {
            mIPAddress = "";
            mSSID = "";
            mBlackListedSSID = false;
            Intent intent = new Intent();
            intent.setAction(getResources().getString(R.string.action_update_ui));
            sendBroadcast(intent);
        }
        else
        {
            startActionDetermineSSID(this);
            mBlackListedSSID = false;
            if(NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_WIFI) {
                startActionDetermineBlackisted(this);
            }

            if(!mBlackListedSSID) startActionDetermineIP(this);

            Intent updateUI = new Intent();
            updateUI.setAction(getString(R.string.action_update_ui));
            sendBroadcast(updateUI);
        }
    }

    private void handleStartUpdateCountDown()
    {
        if(mCanShowCountdown) countDownHandler.postDelayed(updateCountdownThread, 0);
        else countDownHandler.removeCallbacks(updateCountdownThread);
    }

    private Runnable updateCountdownThread = new Runnable()
    {
        public void run() {
            Long remaining = VoteUtil.timeRemainingFromIP(getApplicationContext(), mIPAddress);
            if(remaining > 1) {
                Intent intent = new Intent();
                intent.setAction(getResources().getString(R.string.action_update_time_remaining));
                Integer rem, hr, min, sec;
                remaining = remaining / 1000;
                rem = remaining.intValue();
                hr = rem / (60 * 60);
                rem -= hr * (60 * 60);
                min = rem / 60;
                rem -= min * 60;
                sec = rem;

                String mess = String.format(getResources().getString(R.string.status_countdown_remaining), hr, min, sec);
                intent.putExtra(getResources().getString(R.string.extra_time_remaining), mess);
                sendBroadcast(intent);
                countDownHandler.postDelayed(this, 1000);
            }
            else
            {
                Intent intent = new Intent();
                intent.setAction(getResources().getString(R.string.action_update_time_remaining));
                //intent.putExtra(getResources().getString(R.string.extra_time_remaining), getResources().getString(R.string.status_can_vote_now));
                intent.putExtra(getResources().getString(R.string.extra_time_remaining), "");
                sendBroadcast(intent);
                countDownHandler.removeCallbacks(updateCountdownThread);
            }
        }
    };

    private void handleActionRegisterVote() {
        Resources res = getResources();

        // START
        if (NetworkUtil.getConnectivityStatus(this) == 0) return;
        if(mCharacterName.isEmpty()) return ;

        String ethos =
                PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_key_ethos), getString(R.string.pref_ethos_default));

        final String mode = "normal";

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            int verCode = pInfo.versionCode;
            String version = pInfo.versionName;

            HashMap<String,String> params = new HashMap<>();
            params.put("app_version", version);
            params.put("app_version_code", ""+verCode);
            params.put("character_name", mCharacterName);
            params.put("ethos", ethos);
            params.put("mode", mode);

            String urlEncodedParams = getPostDataString(params);

            String stringRegisterURL = res.getString(R.string.url_register);
            URL registerURL = new URL(stringRegisterURL);
//String urlParameters = "character_name=" + characterName;php
            boolean PRE_KITKAT = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
            byte[] postData;
            if(PRE_KITKAT) postData = urlEncodedParams.getBytes(Charset.forName("UTF-8"));
            else postData = urlEncodedParams.getBytes(StandardCharsets.UTF_8);

            int postDataLength = postData.length;

            try {
                HttpURLConnection conn = (HttpURLConnection) registerURL.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                conn.setUseCaches(false);

                DataOutputStream wr = null;

                try {
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.write(postData);

                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if(wr != null)
                    try {
                        wr.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                InputStream in = new BufferedInputStream(conn.getInputStream());
                InputStreamReader isw = new InputStreamReader(in);
                char current;
                String receivedText = "";

                int data = isw.read();
                while (data != -1) {
                    current = (char) data;
                    receivedText += Character.toString(current);
                    data = isw.read();
                }

                Log.i(getPackageName(), receivedText);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        disableConnectionReuseIfNecessary();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private void handleInitData() {
        if(mInitialized) return ;

        mDaily = new HashMap<String,Integer>();
        mMonthly = new HashMap<String,Integer>();
        mYearly = new HashMap<String,Integer>();

        SOUND_LIGHT = getString(R.string.pref_sound_option_light);
        SOUND_MEDIUM = getString(R.string.pref_sound_option_medium);
        SOUND_DARK = getString(R.string.pref_sound_option_dark);

        ETHOS_GOOD = getString(R.string.pref_ethos_option_good);
        ETHOS_NEUTRAL = getString(R.string.pref_ethos_option_neutral);
        ETHOS_EVIL = getString(R.string.pref_ethos_option_evil);

        Log.d(getPackageName(), "** INITIALIZING DATA");
        if(mCharacterName.isEmpty()) startActionLoadCharacterName(this);
        startActionDetermineSSID(this);
        startActionDetermineBlackisted(this);
        startActionDetermineSoundEnabled(this);
        startActionDetermineCanVote(this);
        if(mIPAddress.isEmpty()) startActionDetermineIP(this);
        Log.d(getPackageName(), "** FINISHED INITIALIZING DATA");
        mInitialized = true;
    }

    private void handleActionFetchServerData() {
        Resources res = getResources();

        // START
        if (mCharacterName.isEmpty()) return;
        if (NetworkUtil.getConnectivityStatus(this) == 0) return;

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            int verCode = pInfo.versionCode;
            String version = pInfo.versionName;

            HashMap<String, String> params = new HashMap<>();
            params.put("character_name", mCharacterName);

            String urlEncodedParams = getPostDataString(params);

            String stringRegisterURL = res.getString(R.string.url_register);
            URL registerURL = new URL(stringRegisterURL);

            //String urlParameters = "character_name=" + characterName;php
            boolean PRE_KITKAT = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
            byte[] postData;
            if (PRE_KITKAT) postData = urlEncodedParams.getBytes(Charset.forName("UTF-8"));
            else postData = urlEncodedParams.getBytes(StandardCharsets.UTF_8);

            int postDataLength = postData.length;

            try {
                HttpURLConnection conn = (HttpURLConnection) registerURL.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                conn.setUseCaches(false);

                DataOutputStream wr = null;

                try {
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.write(postData);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (wr != null)
                        try {
                            wr.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                InputStream in = new BufferedInputStream(conn.getInputStream());
                InputStreamReader isw = new InputStreamReader(in);
                char current;
                String receivedText = "";

                int data = isw.read();
                while (data != -1) {
                    current = (char) data;
                    receivedText += Character.toString(current);
                    data = isw.read();
                }

                Log.i(getPackageName(), receivedText);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        disableConnectionReuseIfNecessary();
    }

    private void handleActionDetermineSoundEnabled() {
        String key = getResources().getString(R.string.pref_key_enable_notification_sound);
        Boolean defaultSoundOn = getResources().getBoolean(R.bool.default_enable_sound);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundEnabled = sharedPreferences.getBoolean(key, defaultSoundOn);
    }

    private void handleActionEmailLog() {
        Process process = null;

        try {
            process = Runtime.getRuntime().exec("logcat -d");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(process == null) return ;

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        StringBuilder log=new StringBuilder();
        String line = "";

        try {
            while ((line = bufferedReader.readLine()) != null) {
                //log.append(line + '\n');
                log.append(line+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String logData = log.toString();
        Intent mail = new Intent(Intent.ACTION_SEND);
        mail.setType("text/plain")
            .putExtra(Intent.EXTRA_EMAIL, new String[]{"karahd@gmail.com","gesslar@outlook.com","bworkman@frogdice.com"})
            .putExtra(Intent.EXTRA_SUBJECT, "[ThreshVote] LOG FROM " + mCharacterName)
            .putExtra(Intent.EXTRA_TEXT, logData);
        Intent sender = Intent.createChooser(mail, "Email:");
        sender.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sender);
    }

    private void handleActionDetermineBlacklisted() {
        mBlackListedSSID = SSIDUtil.isSSIDBlackListed(this, mSSID);
        Log.d("ThreshVoteIntent", "BLACKLISTED: " + mBlackListedSSID.toString());
        startActionDetermineCanVote(this);

        final Intent updateUI = new Intent();
        updateUI.setAction(getString(R.string.action_update_ui));
        sendBroadcast(updateUI);
    }

    private void handleActionDetermineSSID() {
        Boolean wifi = NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_WIFI;
        if(wifi) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            String ssid = wifiInfo.getSSID();

            if (ssid == null || ssid.isEmpty()) {
                mSSID = null;
            } else {
                mSSID = ssid.substring(1, ssid.length() - 1);
            }
        } else {
            mSSID = null;
        }
    }

    private void handleActionFetchJSON(String url) {
        JSONParser jp;
        JSONObject foo, bar;

        mDaily.clear();
        mMonthly.clear();
        mYearly.clear();

        //Log.d(getPackageName(), "DOING THE JSON THING");
        jp = new JSONParser();
        JSONArray ja = jp.makeHttpRequest(url, "GET", new HashMap<String, String>());

        if(ja == null) {
            //Log.d(getPackageName(), "JSON IS NULL");
            return ;
        } else {
            //Log.d(getPackageName(), "JSON IS NOT NULL");
        }

        Integer sz = ja.length();
        for(Integer i = 0; i < sz; i++){
            try {
                 foo = ja.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                return ;
            }

            try {
                String name = foo.names().getString(0);
                bar = foo.getJSONObject(name);
            } catch (JSONException e) {
                e.printStackTrace();
                return ;
            }

            Iterator x = bar.keys();
            while(x.hasNext()) {
                String key = (String) x.next();
                //Log.d("KEY", key);
                try {
                    switch (i) {
                        case 0:
                            mDaily.put(key,bar.getInt(key));
                            break;
                        case 1:
                            mMonthly.put(key,bar.getInt(key));
                            break;
                        case 2:
                            mYearly.put(key, bar.getInt(key));
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return ;
                }
            }

        }
        /*
        Log.d("DAILY", mDaily.toString());
        Log.d("MONTHLY", mMonthly.toString());
        Log.d("YEARLY", mYearly.toString());
        */
        Log.d(getPackageName(), "fetched JSON");
    }

    private void handleActionShowLeaderBoard() {
        mDaily.clear();
        mMonthly.clear();
        mYearly.clear();
        handleActionFetchJSON(getString(R.string.url_leaderboards));
        Log.d(getPackageName(), "mDaily size: " + mDaily.size() + "\tmMonthly size: " + mMonthly.size() + "\tmYearly size: " + mYearly.size());
        Intent intent = new Intent(this, LeaderBoardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra("daily", mDaily);
        intent.putExtra("monthly", mMonthly);
        intent.putExtra("yearly", mYearly);
        startActivity(intent);
    }
}
