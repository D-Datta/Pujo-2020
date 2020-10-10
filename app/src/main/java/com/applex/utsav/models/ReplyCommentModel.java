package com.applex.utsav.models;

import android.util.Log;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class ReplyCommentModel {

    private String userdp;
    private String uid;
    private String postUid;

    private String pComID, comUid;
    private String type;

    private String username, comment;
    private long ts;
    private String gender;

    @Exclude
    private String docID;

    private ArrayList<String> likeL;

    private ArrayList<String> reportL;

    private String postID;

    @Exclude
    private int likeCheck;

    public ReplyCommentModel() { likeCheck = -1; }


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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
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

    public String getpComID() {
        return pComID;
    }

    public void setpComID(String pComID) {
        this.pComID = pComID;
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

    public String getComUid() { return comUid; }

    public void setComUid(String comUid) { this.comUid = comUid; }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ArrayList<String> getReportL() {
        return reportL;
    }

    public void setReportL(ArrayList<String> reportL) {
        this.reportL = reportL;
    }


}
