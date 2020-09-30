package com.applex.utsav.models;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class ReelsPostModel {

    private String committee_name;
    private String description;
    private String duration;
    private String video;
    private String committee_dp;
    private String docID;
    private String headline;
    private String frame;
    private String type;

    private long videoViews;

    private String uid;

    private long ts;
    private long cmtNo;

    private ArrayList<String> likeL;

    @Exclude
    private int likeCheck;

    private PujoTagModel pujoTag;


    public ReelsPostModel() {
        likeCheck = -1;
    }

    public long getCmtNo() {
        return cmtNo;
    }

    public void setCmtNo(long cmtNo) {
        this.cmtNo = cmtNo;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFrame() { return frame; }

    public void setFrame(String frame) { this.frame = frame; }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getHeadline() {
        return headline;
    }

    public ArrayList<String> getReportL() {
        return reportL;
    }

    public void setReportL(ArrayList<String> reportL) {
        this.reportL = reportL;
    }

    private ArrayList<String> reportL;

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

    public String getCommittee_dp() {
        return committee_dp;
    }

    public void setCommittee_dp(String committee_dp) {
        this.committee_dp = committee_dp;
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

    public String getCommittee_name() {
        return committee_name;
    }

    public void setCommittee_name(String committee_name) {
        this.committee_name = committee_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public long getVideoViews() {
        return videoViews;
    }

    public void setVideoViews(long videoViews) {
        this.videoViews = videoViews;
    }


    public PujoTagModel getPujoTag() {
        return pujoTag;
    }

    public void setPujoTag(PujoTagModel pujoTag) {
        this.pujoTag = pujoTag;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}