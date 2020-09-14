package com.example.pujo360.models;

import android.util.Log;

public class NotifCount {
    int notifCount;
    public NotifCount(){}

    public int getNotifCount() {
        return notifCount;
    }

    public void setNotifCount(int notifCount) {
        this.notifCount = notifCount;
    }
    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called!!!!!!!!!!!!!");
        super.finalize();
    }
}
