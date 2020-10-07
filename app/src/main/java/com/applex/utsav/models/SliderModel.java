package com.applex.utsav.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class SliderModel {

    private String eventImage, eventLink, eventDetails;

    private ArrayList<String> clickL;

    @Exclude private String docID;

    private int option;


    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getEventLink() {
        return eventLink;
    }

    public void setEventLink(String eventLink) {
        this.eventLink = eventLink;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public ArrayList<String> getClickL() {
        return clickL;
    }

    public void setClickL(ArrayList<String> clickL) {
        this.clickL = clickL;
    }

    public void removeFromLikeList(String Uid){
        this.clickL.remove(Uid);
    }

    public void addToLikeList(String uid) {
        if(this.clickL == null){
            this.clickL = new ArrayList<>();
        }
        this.clickL.add(uid);
    }

}


