package com.example.pujo360.models;


import android.util.Log;

import com.google.firebase.firestore.Exclude;

public class FlamedModel {

    private String userdp, uid;
    private String username;
    private String userCampus;
    private String pComID, comment;

    private long ts;

    @Exclude
    private String docID;

    private String campus, postID, postUid;


    public FlamedModel() {
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called!!!!!!!!!!!!!");
        super.finalize();
    }

    public FlamedModel(String userdp, String uid, String username) {
        this.userdp = userdp;
        this.uid = uid;
        this.username = username;
    }

    public String getUserdp() {
        return userdp;
    }

    public void setUserdp(String userdp) {
        this.userdp = userdp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostUid() { return postUid; }

    public void setPostUid(String postUid) { this.postUid = postUid; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getUserCampus() {
        return userCampus;
    }

    public void setUserCampus(String userCampus) {
        this.userCampus = userCampus;
    }

    public String getpComID() { return pComID; }

    public void setpComID(String pComID) { this.pComID = pComID; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }
}