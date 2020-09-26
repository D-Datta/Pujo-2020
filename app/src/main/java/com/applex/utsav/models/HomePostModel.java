package com.applex.utsav.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class HomePostModel {

    private String usN, dp, uid, type;
    private String txt;

    private ArrayList<String> img;
    private String comID, comName;
    private long ts;
    private long newTs;
    private String headline;

    private ArrayList<TagModel> tagL;

    private long cmtNo;

    private ArrayList<String> likeL;

    @Exclude
    private int likeCheck;

    @Exclude
    private String docID;

    private ArrayList<String> reportL;

    private String challengeID;




    ////////////DEFAULT CONSTRUCTOR///////////
    public HomePostModel(){
        likeCheck = -1;
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public long getNewTs() {
        return newTs;
    }

    public void setNewTs(long newTs) {
        this.newTs = newTs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getComID() {
        return comID;
    }

    public void setComID(String comID) {
        this.comID = comID;
    }

    public String getUsN() {
        return usN;
    }
    public void setUsN(String usN) {
        this.usN = usN;
    }

    public String getDp() {
        return dp;
    }
    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getTxt() {
        return txt;
    }
    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getImg() {
        return img;
    }

    public void setImg(ArrayList<String> img) {
        this.img = img;
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

    public ArrayList<TagModel> getTagL() {
        return tagL;
    }
    public void setTagL(ArrayList<TagModel> tagL) {
        this.tagL = tagL;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
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


    public ArrayList<String> getReportL() {
        return reportL;
    }

    public void setReportL(ArrayList<String> reportL) {
        this.reportL = reportL;
    }


    public String getChallengeID() {
        return challengeID;
    }

    public void setChallengeID(String challengeID) {
        this.challengeID = challengeID;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getHeadline() {
        return headline;
    }
}
