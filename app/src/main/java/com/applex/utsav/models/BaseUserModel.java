package com.applex.utsav.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.ArrayList;

@IgnoreExtraProperties
public class BaseUserModel {

    private String name;
    private String addressline;
    private String city;
    private String state;
    private String pin;
    private String dp;
    private String coverpic;
    private String gender;

    private String uid;
    private String email;

    private String type;

    //committee special
    private long likeCount;
    private long commentcount;
    private long pujoVisits;
    private long upvotes;
    private Timestamp lastVisitTs;
    private ArrayList<String> upvoteL;

    public BaseUserModel() {
        upvoteL = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressline() {
        return addressline;
    }

    public void setAddressline(String addressline) {
        this.addressline = addressline;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getCoverpic() {
        return coverpic;
    }

    public void setCoverpic(String coverpic) {
        this.coverpic = coverpic;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    //metrics for committee
    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentcount() {
        return commentcount;
    }

    public void setCommentcount(long commentcount) {
        this.commentcount = commentcount;
    }

    public long getPujoVisits() {
        return pujoVisits;
    }

    public void setPujoVisits(long pujoVisits) {
        this.pujoVisits = pujoVisits;
    }

    public Timestamp getLastVisitTs() {
        return lastVisitTs;
    }

    public void setLastVisitTs(Timestamp lastVisitTs) {
        this.lastVisitTs = lastVisitTs;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    //metrics


    public long getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(long upvotes) {
        this.upvotes = upvotes;
    }

    public ArrayList<String> getUpvoteL() {
        return upvoteL;
    }

    public void setUpvoteL(ArrayList<String> upvoteL) {
        this.upvoteL = upvoteL;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
