package com.applex.utsav.models;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

public class ReelsPostModel {

    private String committee_name, description, duration, video, committee_dp, docID, headline, frame, type, uid;
    private long ts, newTs, cmtNo, videoViews;

    private ArrayList<String> likeL;
    private ArrayList<String> tagList;
    private ArrayList<TagModel> tagL;
    private ArrayList<String> reportL;

    private String com1_usn, com1_dp, com1, com1_uid, com1_gender;
    private String com2_usn, com2_dp, com2, com2_uid, com2_gender;
    private long com1_ts, com2_ts;

    private PujoTagModel pujoTag;

    @Exclude
    private int likeCheck;

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

    public long getNewTs() {
        return newTs;
    }

    public void setNewTs(long newTs) {
        this.newTs = newTs;
    }

    public ArrayList<String> getTagList() {
        return tagList;
    }

    public void setTagList(ArrayList<String> tagList) {
        this.tagList = tagList;
    }

    public ArrayList<TagModel> getTagL() {
        return tagL;
    }

    public void setTagL(ArrayList<TagModel> tagL) {
        this.tagL = tagL;
    }

    public String getCom1_usn() {
        return com1_usn;
    }

    public void setCom1_usn(String com1_usn) {
        this.com1_usn = com1_usn;
    }

    public String getCom1_dp() {
        return com1_dp;
    }

    public void setCom1_dp(String com1_dp) {
        this.com1_dp = com1_dp;
    }

    public String getCom1() {
        return com1;
    }

    public void setCom1(String com1) {
        this.com1 = com1;
    }

    public String getCom1_uid() {
        return com1_uid;
    }

    public void setCom1_uid(String com1_uid) {
        this.com1_uid = com1_uid;
    }

    public String getCom1_gender() {
        return com1_gender;
    }

    public void setCom1_gender(String com1_gender) {
        this.com1_gender = com1_gender;
    }

    public String getCom2_usn() {
        return com2_usn;
    }

    public void setCom2_usn(String com2_usn) {
        this.com2_usn = com2_usn;
    }

    public String getCom2_dp() {
        return com2_dp;
    }

    public void setCom2_dp(String com2_dp) {
        this.com2_dp = com2_dp;
    }

    public String getCom2() {
        return com2;
    }

    public void setCom2(String com2) {
        this.com2 = com2;
    }

    public String getCom2_uid() {
        return com2_uid;
    }

    public void setCom2_uid(String com2_uid) {
        this.com2_uid = com2_uid;
    }

    public String getCom2_gender() {
        return com2_gender;
    }

    public void setCom2_gender(String com2_gender) {
        this.com2_gender = com2_gender;
    }

    public long getCom1_ts() {
        return com1_ts;
    }

    public void setCom1_ts(long com1_ts) {
        this.com1_ts = com1_ts;
    }

    public long getCom2_ts() {
        return com2_ts;
    }

    public void setCom2_ts(long com2_ts) {
        this.com2_ts = com2_ts;
    }
}