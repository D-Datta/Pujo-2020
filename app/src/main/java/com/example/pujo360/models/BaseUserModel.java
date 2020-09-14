package com.example.pujo360.models;

import java.util.ArrayList;

public class BaseUserModel {
    private String name;
    private String addressline;
    private String city;
    private String state;
    private String dp;
    private String coverpic;

    private String uid;
    private String email;
    private String usertype;
    private ArrayList<String> interestL;

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

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public ArrayList<String> getInterestL() {
        return interestL;
    }

    public void setInterestL(ArrayList<String> interestL) {
        this.interestL = interestL;
    }
}
