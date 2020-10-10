package com.applex.utsav.models;


import com.google.firebase.firestore.Exclude;

public class FlamedModel {

    private String userdp;
    private String uid;

    private String type;
    private String username;
    private String pComID, comment;
    private String gender;

    private long ts;

    @Exclude
    private String docID;

    private String  postID, postUid;


    public FlamedModel() {
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

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getpComID() { return pComID; }

    public void setpComID(String pComID) { this.pComID = pComID; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
