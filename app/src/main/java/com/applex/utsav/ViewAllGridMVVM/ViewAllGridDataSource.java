package com.applex.utsav.ViewAllGridMVVM;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;
import com.applex.utsav.models.HomePostModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class ViewAllGridDataSource extends PageKeyedDataSource<Query, DocumentSnapshot> {

    private final String ts, uid;

    public ViewAllGridDataSource(String ts, String uid) {
        this.uid = uid;
        this.ts = ts;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Query> params, @NonNull LoadInitialCallback<Query, DocumentSnapshot> callback) {
        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds")
                .orderBy("ts", Query.Direction.DESCENDING)
                .whereEqualTo("uid", uid)
                .startAt(Long.parseLong(ts))
                .limit(5);

        query.get().addOnCompleteListener(task -> {
            ArrayList<DocumentSnapshot> homePostModelSnapshots = new ArrayList<>();
            DocumentSnapshot lastDocumentSnapshot = Objects.requireNonNull(task.getResult()).getDocuments().get(task.getResult().size() - 1);

            for(DocumentSnapshot documentSnapshot: task.getResult()) {
//                HomePostModel homePostModel = documentSnapshot.toObject(HomePostModel.class);
//                homePostModel.setDocID(documentSnapshot.getId());
                homePostModelSnapshots.add(documentSnapshot);
            }

            Query query_before = FirebaseFirestore.getInstance()
                    .collection("Feeds")
                    .orderBy("ts", Query.Direction.ASCENDING)
                    .whereEqualTo("uid", uid)
                    .startAfter(Long.parseLong(ts))
                    .limit(5);

            Query query_after = FirebaseFirestore.getInstance()
                    .collection("Feeds")
                    .orderBy("ts", Query.Direction.DESCENDING)
                    .whereEqualTo("uid", uid)
                    .startAfter(lastDocumentSnapshot)
                    .limit(5);

            callback.onResult(homePostModelSnapshots, query_before, query_after);
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Query> params, @NonNull LoadCallback<Query, DocumentSnapshot> callback) {
        params.key.get().addOnCompleteListener(task -> {
            ArrayList<DocumentSnapshot> homePostModelSnapshots = new ArrayList<>();
            if(task.getResult().size() > 0) {
                DocumentSnapshot lastDocumentSnapshot = Objects.requireNonNull(task.getResult()).getDocuments().get(task.getResult().size() - 1);

                for(DocumentSnapshot documentSnapshot: task.getResult()) {
//                    HomePostModel homePostModel = documentSnapshot.toObject(HomePostModel.class);
//                    homePostModel.setDocID(documentSnapshot.getId());
                    homePostModelSnapshots.add(documentSnapshot);
                }

                Collections.reverse(homePostModelSnapshots);

                Query query = FirebaseFirestore.getInstance()
                        .collection("Feeds")
                        .orderBy("ts", Query.Direction.ASCENDING)
                        .whereEqualTo("uid", uid)
                        .startAfter(lastDocumentSnapshot)
                        .limit(5);

                callback.onResult(homePostModelSnapshots, query);
            }
        });
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Query> params, @NonNull LoadCallback<Query, DocumentSnapshot> callback) {
        params.key.get().addOnCompleteListener(task -> {
            ArrayList<DocumentSnapshot> homePostModelSnapshots = new ArrayList<>();

            DocumentSnapshot lastDocumentSnapshot = Objects.requireNonNull(task.getResult()).getDocuments().get(task.getResult().size() - 1);

            for(DocumentSnapshot documentSnapshot: task.getResult()) {
//                HomePostModel homePostModel = documentSnapshot.toObject(HomePostModel.class);
//                homePostModel.setDocID(documentSnapshot.getId());
                homePostModelSnapshots.add(documentSnapshot);
            }

            Query query = FirebaseFirestore.getInstance()
                    .collection("Feeds")
                    .orderBy("ts", Query.Direction.DESCENDING)
                    .whereEqualTo("uid", uid)
                    .startAfter(lastDocumentSnapshot)
                    .limit(10);

            callback.onResult(homePostModelSnapshots, query);
        });
    }
}