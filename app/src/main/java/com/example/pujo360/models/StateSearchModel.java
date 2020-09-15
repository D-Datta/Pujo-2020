package com.example.pujo360.models;

import android.util.Log;

public class StateSearchModel {



    private String state;

    public StateSearchModel() {

    }

    public StateSearchModel(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called!!!!!!!!!!!!!");
        super.finalize();
    }
}
