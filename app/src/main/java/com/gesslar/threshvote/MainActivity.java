package com.gesslar.threshvote;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver receiverIp;
    private BroadcastReceiver receiverNoIp;
    private BroadcastReceiver receiverSetSnackMessage;
    private BroadcastReceiver receiverSetToastMessage;
    private BroadcastReceiver receiverTimerUpdate;
    private BroadcastReceiver receiverUpdateUi;

    final private Boolean UI_ENABLE = true;
    final private Boolean UI_DISABLE = false;
    final private Boolean UI_THINKING = true;
    final private Boolean UI_NOT_THINKING = false;

    private MenuItem miCharacterName, miSettings, miLeaderBoard, miSsid;
    private ImageButton ibVoteButton;

// ** @@EVENTS *************************************************************************************

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        if(!ThreshVoteIntentService.mInitialized) initializeData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ThreshVoteIntentService.mActive++;

        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("MainActivity", "SHOWN: " + ThreshVoteIntentService.mIPAddress + "' - " + resultdate.toString());

        final Resources resources = getResources();

        NotificationUtil.cancelNotifications(this);

        // SET UP ALL THE INTENT FILTERS FOR THE MAINACTIVITY TO RESPOND TO
        IntentFilter filterIPReceived = new IntentFilter(resources.getString(R.string.action_found_ip));
        receiverIp = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };

        this.registerReceiver(receiverIp, filterIPReceived);

        IntentFilter filterNoIPReceived = new IntentFilter(resources.getString(R.string.action_found_no_ip));
        receiverNoIp = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                promptForConnection();
            }
        };

        this.registerReceiver(receiverNoIp, filterNoIPReceived);

        IntentFilter filterSetSnackMessage = new IntentFilter(resources.getString(R.string.action_set_snack_message));
        receiverSetSnackMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String mess = intent.getStringExtra(resources.getString(R.string.extra_snack_message));
                Snackbar snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        mess,
                        Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        };

        this.registerReceiver(receiverSetSnackMessage, filterSetSnackMessage);

        IntentFilter filterSetToastMessage = new IntentFilter(resources.getString(R.string.action_set_toast_message));
        receiverSetToastMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String mess = intent.getStringExtra(resources.getString(R.string.extra_toast_message));
                Toast.makeText(context, mess, Toast.LENGTH_LONG).show();
            }
        };

        this.registerReceiver(receiverSetToastMessage, filterSetToastMessage);

        IntentFilter filterTimerUpdate = new IntentFilter(resources.getString(R.string.action_update_time_remaining));
        receiverTimerUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String mess = intent.getStringExtra(resources.getString(R.string.extra_time_remaining));
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbar.setSubtitle(mess);
            }
        };

        this.registerReceiver(receiverTimerUpdate, filterTimerUpdate);

        IntentFilter filterUpdateUI = new IntentFilter(resources.getString(R.string.action_update_ui));
        receiverUpdateUi = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
        };

        this.registerReceiver(receiverUpdateUi, filterUpdateUI);

        ibVoteButton = (ImageButton) findViewById(R.id.buttonVote);
        ibVoteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("About ThreshVote");
                final TextView mess = new TextView(MainActivity.this);
                mess.setGravity(Gravity.CENTER);
                boolean preLollipop = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
                if(preLollipop) mess.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Large);
                else mess.setTextAppearance(android.R.style.TextAppearance_Large);
                PackageInfo pi = null;
                try {
                    pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch(PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    return true;
                }
                if(pi == null) return true;
                String version = pi.versionName;
                Integer version_number = pi.versionCode;

                StringBuilder sb = new StringBuilder();
                sb.append("Version ").append(version).append(" (").append(version_number.toString()).append(")");
                mess.setText(sb.toString());
                builder.setView(mess);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                    }
                });
                builder.show();
                return true;
            }
        });

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // UNREGISTER ALL THE INTENT RECEIVERS FOR THE INTENT FILTERS
        this.unregisterReceiver(this.receiverIp);
        this.unregisterReceiver(this.receiverNoIp);
        this.unregisterReceiver(this.receiverSetSnackMessage);
        this.unregisterReceiver(this.receiverSetToastMessage);
        this.unregisterReceiver(this.receiverTimerUpdate);
        this.unregisterReceiver(this.receiverUpdateUi);

        // ALERT EVERYTHING THAT THE MAINACTIVITY IS HIDDEN
        ThreshVoteIntentService.mActive--;
        ThreshVoteIntentService.startActionDetermineCanVote(this, true);

        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("MainActivity", "HIDDEN: " + ThreshVoteIntentService.mIPAddress + "' - " + resultdate.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        miSettings = menu.findItem(R.id.menu_settings);
        miCharacterName = menu.findItem(R.id.menu_set_character_name);
        miLeaderBoard = menu.findItem(R.id.menu_show_leaderboard);
        miSsid = menu.findItem(R.id.menu_block_ssid);

        updateUI();

        return true;
    }

    // ACTION BAR ITEM CLICK EVENTS
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_show_leaderboard) {
            ThreshVoteIntentService.startActionShowLeaderboard(this);
        }

        if(id == R.id.menu_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.menu_set_character_name)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter your character's name");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            input.setText(ThreshVoteIntentService.mCharacterName);
            builder.setView(input);

            builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String characterName;
                    characterName = input.getText().toString();
                    ThreshVoteIntentService.startActionSetCharacterName(getApplicationContext(), characterName);
                }
            });
            builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

            return true;
        }

        if(id == R.id.menu_block_ssid) {
            String ssid = ThreshVoteIntentService.mSSID;
            Boolean enabled = SSIDUtil.isSSIDBlackListed(this, ssid);

            Log.d("MENU", "BLOCK THIS SSID: " + ssid);
            Log.d("MENU", "ALREADY BLOCKED: " + enabled.toString());

            SSIDUtil.blacklistSSID(this, ssid, !enabled);
            updateUI();
        }

        if(id == R.id.menu_about) {
            PackageInfo packageInfo = null;
            try {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            StringBuilder msg = new StringBuilder();

            msg.append("ThreshVote v.").append(packageInfo.versionName).append(" (").append(packageInfo.versionCode).append(")\n");
            msg.append("By Brian M. Workman (gesslar)").append("\n");
            msg.append("karahd@gmail.com");

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("About ThreshVote");
            alertDialog.setMessage(msg);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    public void onVotePressed(View view) {
        NotificationUtil.cancelNotifications(this);
        ThreshVoteIntentService.startActionStartVote(this);
    }

    public void enableUI(Boolean enabled, Boolean thinking) {
        ImageButton btnVote = (ImageButton) findViewById(R.id.buttonVote);
        ProgressBar loadingBar = (ProgressBar) findViewById(R.id.loadingBar);

        btnVote.setEnabled(enabled);
        boolean oldDrawable = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        Drawable icon;
        if (oldDrawable)
            icon = enabled ?
                    getResources().getDrawable(R.drawable.ic_vote_button_image) :
                    getResources().getDrawable(R.drawable.ic_vote_button_disabled);
        else
            icon = enabled ?
                    getResources().getDrawable(R.drawable.ic_vote_button_image, getTheme()) :
                    getResources().getDrawable(R.drawable.ic_vote_button_disabled, getTheme());

        btnVote.setImageDrawable(icon);
        /*
        loadingBar.setVisibility(thinking ? View.VISIBLE : View.GONE);
        */
        loadingBar.setVisibility(thinking ? View.VISIBLE : View.INVISIBLE);
    }

    // If we were unable to determine an IP address, for whatever reason,
    // show a retry alert box, when they hit retry, then start the process
    // over again, maybe the user needs to solve a connectivity issue before
    // retrying.
    public void promptForConnection() {
        RetryDialog();
    }

    // this is the dialog that informs the user that no IP was determined.
    // clicking Retry will start the process of finding the IP again.
    private void RetryDialog() {
        Resources res = getResources();

        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(res.getString(R.string.status_unable_to_determine_ip_address));
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Retry",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ThreshVoteIntentService.startActionDetermineIP(getApplicationContext());
                        updateUI();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateUI();
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    void initializeData()
    {
        ThreshVoteIntentService.startActionInitData(this);
    }

    void updateUI() {
        // OBJECTS ON THIS UI
        ImageButton buttonVote = (ImageButton) findViewById(R.id.buttonVote);
        TextView textStatusMessage = (TextView) findViewById(R.id.textStatusMessage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Menu theMenu = toolbar.getMenu();
        miCharacterName = theMenu.findItem(R.id.menu_set_character_name);
        miSettings = theMenu.findItem(R.id.menu_settings);
        miLeaderBoard = theMenu.findItem(R.id.menu_show_leaderboard);
        miSsid = theMenu.findItem(R.id.menu_block_ssid);

        // INFORMATION WE WILL NEED/STATUS
        Boolean disconnected = NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_NOT_CONNECTED;
        Boolean determiningIP = ThreshVoteIntentService.mDeterminingIP;
        String ipAddress = ThreshVoteIntentService.mIPAddress;
        String characterName = ThreshVoteIntentService.mCharacterName;
        String connection = NetworkUtil.getConnectionType(this);
        Boolean blacklisted = ThreshVoteIntentService.mBlackListedSSID;
        String ssid = ThreshVoteIntentService.mSSID;
        Boolean canVote = VoteUtil.canVoteFromIP(this, ipAddress);

        Log.d("updateUI", "BLACKLISTED: " + blacklisted.toString());
        // SET BASE INFORMATION ON UI ELEMENTS

        textStatusMessage.setText("");

        if(miSsid != null) {
            if(ssid == null) {
                miSsid.setVisible(false);
            } else {
                miSsid.setVisible(true);
                String mess = getString(R.string.title_block_ssid) + " " + ssid;
                miSsid.setTitle(mess);
                miSsid.setChecked(!blacklisted);
            }
        }

        // ARE WE DOING SOMETHING?
        if(determiningIP) {
            if(!characterName.isEmpty()) {
                toolbar.setTitle(characterName);
            }
        } else {
            if (characterName.isEmpty()) {
                if(miSettings != null) miSettings.setVisible(false);
                if(miCharacterName != null) miCharacterName.setVisible(true);
                if(miLeaderBoard != null) miLeaderBoard.setVisible(false);
                enableUI(UI_DISABLE, UI_NOT_THINKING);
                textStatusMessage.setVisibility(View.VISIBLE);
                textStatusMessage.setText(getResources().getString(R.string.character_default_name_message));
            } else {
                if(miSettings != null) miSettings.setVisible(true);
                if(miCharacterName != null) miCharacterName.setVisible(false);
                if(miLeaderBoard != null) miLeaderBoard.setVisible(true);
                toolbar.setTitle(characterName);
                if(disconnected || ipAddress.isEmpty()) {
                    enableUI(UI_DISABLE, UI_NOT_THINKING);
                    textStatusMessage.setVisibility(View.VISIBLE);
                    textStatusMessage.setText(getResources().getString(R.string.status_unable_to_determine_ip_address));
                } else {
                    if(blacklisted) {
                        enableUI(UI_DISABLE, UI_NOT_THINKING);
                        String status = getResources().getString(R.string.status_blacklisted_ssid) + "\n" + ssid;
                        textStatusMessage.setText(status);
                    } else {
                        if (canVote) {
                            enableUI(UI_ENABLE, UI_NOT_THINKING);
                        } else {
                            enableUI(UI_DISABLE, UI_NOT_THINKING);
                        }
                    }
                }
            }
        }
    }

    private ViewSwitcher.ViewFactory mFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(MainActivity.this);
            t.setGravity(Gravity.CENTER);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                t.setTextColor(getResources().getColor(R.color.colorText));
                t.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Large);
            } else {
                t.setTextColor(getResources().getColor(R.color.colorText, getTheme()));
                t.setTextAppearance(android.R.style.TextAppearance_Large);
            }

            return t;
        }
    };

}
