package com.applex.utsav.ViewAllGridMVVM;

import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.applex.utsav.models.HomePostModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class ViewAllGridDataFactory extends DataSource.Factory<Query, DocumentSnapshot> {

    private final MutableLiveData<ViewAllGridDataSource> mutableLiveData;
    private final String ts, uid;

    public ViewAllGridDataFactory(String ts, String uid) {
        this.ts = ts;
        this.uid = uid;
        this.mutableLiveData = new MutableLiveData<>();
    }

    @Override
    public DataSource<Query, DocumentSnapshot> create() {
        ViewAllGridDataSource viewAllGridDataSource = new ViewAllGridDataSource(ts, uid);
        mutableLiveData.postValue(viewAllGridDataSource);
        return viewAllGridDataSource;
    }


    public MutableLiveData<ViewAllGridDataSource> getMutableLiveData() {
        return mutableLiveData;
    }
}
