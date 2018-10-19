package com.gesslar.threshvote;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by gesslar on 2016-03-13.
 */
public class LeaderBoardFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    public static Fragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        LeaderBoardFragment fragment = new LeaderBoardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Resources res = container.getResources();
        HashMap<String,Integer> board = new HashMap<>();
        switch(mPage) {
            case 0: board = ThreshVoteIntentService.mDaily; break;
            case 1: board = ThreshVoteIntentService.mMonthly; break;
            case 2: board = ThreshVoteIntentService.mYearly; break;
        }
        Log.d(getTag(), "THIS IS PAGE: " + mPage);
        List<BoardItem> theBoard = new ArrayList<>();
        Iterator it = board.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Integer num = (Integer) pair.getValue();
            String key = (String) pair.getKey();
            theBoard.add(new BoardItem(key, num));
        }

        Collections.sort(theBoard, new Comparator<BoardItem>() {
            public int compare(BoardItem one, BoardItem two) {
                Integer ret;

                ret = two.getVotes() - one.getVotes();
                if (ret == 0) return one.getName().compareTo(two.getName());
                return ret;
            }
        });

        StringBuilder mess = new StringBuilder();
        mess
                .append("<!DOCTYPE html>")
                .append("  <head>")
                .append("    <meta charset=\"UTF-8\">")
                .append("    <title>ThreshVote Rankings</title>")
                .append("    <style>")
                .append("      #voting {font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif; border-collapse: collapse; width: 100% }")
                .append("      #voting td { border: 1px solid #ddd; padding: 8px; width: 200; margin-left: 100px; }")
                .append("      #voting .num { text-align: center; }")
                .append("      #voting .name { text-align: left; }")
                .append("      #voting tr:nth-child(even) { background-color: #f2f2f2 }")
                .append("      #voting th { padding-top: 12px; padding-bottom: 12px; background-color: #dd2c00; color: white; }")
                .append("      #maindiv { margin: auto; }")
                .append("    </style>")
                .append("  </head>")
                .append("  <body>")
                .append("    <div id=\"maindiv\">")
                .append("      <div>")
                .append("        <table id=\"voting\">")
                .append("          <tr><th class=\"num\">&#35;</th><th class=\"name\">Character</th><th class=\"num\">Votes</th></tr>");


    Integer cnt = theBoard.size();
        for(Integer i = 0; i < cnt; i++) {
            //mess.append(i+1).append("\t").append(theBoard.get(i).getName()).append("\t").append(theBoard.get(i).getVotes()).append("\n");
            mess
                    .append("<tr>")
                    .append("<td class=\"num\">").append(i+1).append("</td>")
                    .append("<td class=\"name\">").append(theBoard.get(i).getName()).append("</td>")
                    .append("<td class=\"num\">").append(theBoard.get(i).getVotes()).append("</td>")
                    .append("</tr>");
        }

        mess
                .append("      </div>")
                .append("    </div>")
                .append("  </body>")
                .append("</html>");

        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        WebView v = (WebView) view;
        //textView.setText(mess.toString());
        v.loadData(mess.toString(), "text/html", "utf-8");


        return view;
    }

}
