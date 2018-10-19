package com.gesslar.threshvote;

/**
 * Created by gesslar on 2016-03-13.
 */
class BoardItem {
    private String name;
    private Integer votes;
    public BoardItem(String name, Integer votes) {
        this.name = name;
        this.votes = votes;
    }

    public String getName() { return name; }
    public Integer getVotes() { return votes; }
}

