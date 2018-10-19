package com.gesslar.threshvote;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LeaderBoardActivity extends AppCompatActivity {
    private Bundle Boards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_leader_board);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new LeaderBoardFragmentAdapter(getSupportFragmentManager()));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        Boards = getIntent().getExtras();
        ActionBar bar = getActionBar();
        if(bar != null) bar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        ThreshVoteIntentService.mActive++;

        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("LeaderBoard", "SHOWN: " + ThreshVoteIntentService.mIPAddress + "' - " + resultdate.toString());
    }

    @Override
    public void onPause() {
        ThreshVoteIntentService.mActive--;
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
        Date resultdate = new Date(yourmilliseconds);
        Log.d("LeaderBoard", "HIDDEN: '" + ThreshVoteIntentService.mIPAddress + "' - " + resultdate.toString());
        super.onPause();
    }

}
