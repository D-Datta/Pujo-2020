package com.applex.utsav.models;

import android.util.Log;

public class NotifModel {

    private boolean seen;
    private String title;
    private String comTxt;
    private String DocID;
    private int bool;

    private String dp, uid;
    private String usN;

    private long ts;
    private String postID;

    private String type;

    public NotifModel(){}

    public String getPostID() { return postID; }

    public void setPostID(String postID) { this.postID = postID; }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getComTxt() {
        return comTxt;
    }

    public void setComTxt(String comTxt) {
        this.comTxt = comTxt;
    }

    public String getUsN() {
        return usN;
    }

    public void setUsN(String usN) {
        this.usN = usN;
    }

    public String getDocID() {
        return DocID;
    }

    public void setDocID(String docID) {
        DocID = docID;
    }

    public int getBool() { return bool; }

    public void setBool(int bool) { this.bool = bool; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
