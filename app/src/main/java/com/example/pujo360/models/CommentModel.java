package com.example.pujo360.models;

import android.util.Log;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class CommentModel {

    private String userdp, uid, postUid;
    private String username, comment;
    private long ts;
    private int rCmtNo;

    @Exclude
    private String docID;

    private ArrayList<String> likeL;

    private ArrayList<String> reportL;

    private String campus, postID;

    @Exclude
    private int likeCheck;

    public CommentModel() { likeCheck = -1; }

    public CommentModel(String userdp, String uid, String username, String comment) {
        this.userdp = userdp;
        this.uid = uid;
        this.username = username;
        this.comment = comment;
    }

    public String getUserdp() { return userdp; }

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public int getrCmtNo() { return rCmtNo; }

    public void setrCmtNo(int rCmtNo) {
        this.rCmtNo = rCmtNo;
    }

    public ArrayList<String> getLikeL() {
        return likeL;
    }

    public void setLikeL(ArrayList<String> likeL) {
        this.likeL = likeL;
    }

    public int getLikeCheck() {
        return likeCheck;
    }

    public void setLikeCheck(int likeCheck) {
        this.likeCheck = likeCheck;
    }

    public void addToLikeList(String uid) {
        if(this.likeL == null){
            this.likeL = new ArrayList<>();
        }
        this.likeL.add(uid);
    }

    public void removeFromLikeList(String Uid){
        this.likeL.remove(Uid);
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called!!!!!!!!!!!!!");
        super.finalize();
    }
}

