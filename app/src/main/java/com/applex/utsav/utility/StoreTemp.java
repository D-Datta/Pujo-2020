package com.applex.utsav.utility;


import com.applex.utsav.models.TagModel;

import java.util.ArrayList;

public class StoreTemp {

    private static StoreTemp instance;

    private ArrayList<TagModel> tagTemp;
    private byte[] pic;


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


    private ArrayList<String> likeList;

    public ArrayList<String> getLikeList() {
        return likeList;
    }

    public void setLikeList(ArrayList<String> likeList) {
        this.likeList = likeList;
    }
}
