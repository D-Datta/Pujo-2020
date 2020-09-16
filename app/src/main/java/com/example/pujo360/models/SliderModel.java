package com.example.pujo360.models;

import android.util.Log;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class SliderModel {
    private ArrayList<TagModel> tagL;
    private String uid;
    private String eventImage, eventName, eventDetails, themecolor;

    private ArrayList<String> likeL;
    private long cmtNo;

    @Exclude
    private String docID;
    @Exclude
    private int likeCheck;

    private boolean participate;


    public SliderModel() {
        likeCheck = -1;
    }

    public boolean isParticipate() {
        return participate;
    }

    public void setParticipate(boolean participate) {
        this.participate = participate;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getThemecolor() {
        return themecolor;
    }

    public void setThemecolor(String themecolor) {
        this.themecolor = themecolor;
    }

    public ArrayList<TagModel> getTagL() {
        return tagL;
    }

    public void setTagL(ArrayList<TagModel> tagL) {
        this.tagL = tagL;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public ArrayList<String> getLikeL() {
        return likeL;
    }

    public void setLikeL(ArrayList<String> likeL) {
        this.likeL = likeL;
    }

    public long getCmtNo() {
        return cmtNo;
    }

    public void setCmtNo(long cmtNo) {
        this.cmtNo = cmtNo;
    }

    public int getLikeCheck() {
        return likeCheck;
    }

    public void setLikeCheck(int likeCheck) {
        this.likeCheck = likeCheck;
    }

    public void removeFromLikeList(String Uid){
        this.likeL.remove(Uid);
    }

    public void addToLikeList(String uid) {
        if(this.likeL == null){
            this.likeL = new ArrayList<>();
        }
        this.likeL.add(uid);
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called!!!!!!!!!!!!!");
        super.finalize();
    }
}


