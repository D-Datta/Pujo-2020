package com.applex.utsav.utility;


import com.applex.utsav.models.PujoTagModel;
import com.applex.utsav.models.TagModel;

import java.util.ArrayList;

public class StoreTemp {

    private static StoreTemp instance;

    private ArrayList<TagModel> tagTemp;
    private byte[] pic;
    public PujoTagModel pujoTagModel;

    private long cmtNo;
    private byte[] coverpic;

    public ArrayList<TagModel> getTagTemp() {
        return tagTemp;
    }

    public void setTagTemp(ArrayList<TagModel> tagTemp) {
        this.tagTemp = tagTemp;
    }

    public static StoreTemp getInstance(){
        if(instance == null){
            instance = new StoreTemp();
        }
        return instance;
    }

    public byte[] getPic(){return pic;}

    public void setPic(byte[] dp){
        this.pic = dp;
    }

    public byte[] getCoverpic() {
        return coverpic;
    }

    public void setCoverpic(byte[] coverpic) {
        this.coverpic = coverpic;
    }

    public long getCmtNo() {
        return cmtNo;
    }

    public void setCmtNo(long cmtNo) {
        this.cmtNo = cmtNo;
    }

    private ArrayList<String> likeList;

    public ArrayList<String> getLikeList() {
        return likeList;
    }

    public void setLikeList(ArrayList<String> likeList) {
        this.likeList = likeList;
    }

    public PujoTagModel getPujoTagModel() {
        return pujoTagModel;
    }

    public void setPujoTagModel(PujoTagModel pujoTagModel) {
        this.pujoTagModel = pujoTagModel;
    }
}
