package com.example.pujo360.models;

import android.util.Log;

public class CommentCount {
    long commentCount;
    public CommentCount(){}

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called!!!!!!!!!!!!!");
        super.finalize();
    }
}
