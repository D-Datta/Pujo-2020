package com.example.pujo360.models;

import android.util.Log;

public class FlameCount {
    long flameCount;
    public FlameCount(){}

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called!!!!!!!!!!!!!");
        super.finalize();
    }

    public long getFlameCount() {
        return flameCount;
    }

    public void setFlameCount(long flameCount) {
        this.flameCount = flameCount;
    }
}
