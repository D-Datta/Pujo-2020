package com.applex.utsav.ViewAllGridMVVM;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.applex.utsav.models.HomePostModel;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ViewAllGridViewModel extends ViewModel {

    private LiveData<PagedList<DocumentSnapshot>> postLiveData;
    private final String ts, uid;

    public ViewAllGridViewModel(String ts, String uid) {
        this.uid = uid;
        this.ts = ts;
        init();
    }

    private void init() {
        Executor executor = Executors.newFixedThreadPool(5);

        ViewAllGridDataFactory feedDataFactory = new ViewAllGridDataFactory(ts, uid);

        PagedList.Config pagedListConfig = (new PagedList.Config.Builder())
                        .setEnablePlaceholders(true)
                        .setInitialLoadSizeHint(5)
                        .setPageSize(10)
                        .setPrefetchDistance(5)
                        .build();

        postLiveData = (new LivePagedListBuilder(feedDataFactory, pagedListConfig))
                .setFetchExecutor(executor)
                .build();
    }

    public LiveData<PagedList<DocumentSnapshot>> getPostLiveData() {
        return postLiveData;
    }

}
