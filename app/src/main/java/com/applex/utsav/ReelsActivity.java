package com.applex.utsav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import com.applex.utsav.adapters.ReelsAdapter;
import com.applex.utsav.models.ReelsPostModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Objects;

public class ReelsActivity extends AppCompatActivity {

    private ViewPager2 reelsList;
    private String uid, bool;
    private Query query;
    private ReelsAdapter adapter;
    private ArrayList<ReelsPostModel> models;
    private String docID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        models = new ArrayList<>();
        reelsList = findViewById(R.id.recyclerReelsViewAll);

        bool = Objects.requireNonNull(getIntent().getStringExtra("bool"));

        if(getIntent().getStringExtra("uid") != null) {
            uid = getIntent().getStringExtra("uid");
        }

        if(getIntent().getStringExtra("docID") != null) {
            docID = getIntent().getStringExtra("docID");
        }

        query = FirebaseFirestore.getInstance().collection("Reels").whereEqualTo("docID", docID);
        buildRecyclerView();
    }

    private void buildRecyclerView() {

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                ReelsPostModel reelsPostModel = document.toObject(ReelsPostModel.class);
                Objects.requireNonNull(reelsPostModel).setDocID(document.getId());
                models.add(reelsPostModel);
                fetchAfter(bool, document);
                fetchBefore(bool, document);
                adapter = new ReelsAdapter(ReelsActivity.this, models, reelsList, bool, uid);
                reelsList.setAdapter(adapter);
            }
        });
    }

    private void fetchAfter(String bool, DocumentSnapshot reelslastVisible) {
        Query query = null;

        if(bool.matches("1")) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .orderBy("ts", Query.Direction.DESCENDING)
                    .limit(1)
                    .startAfter(reelslastVisible);
        }
        else if(bool.matches("2")) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .whereEqualTo("uid", uid)
                    .orderBy("ts", Query.Direction.DESCENDING)
                    .limit(1)
                    .startAfter(reelslastVisible);
        }

        Objects.requireNonNull(query).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && Objects.requireNonNull(task.getResult()).getDocuments().size() != 0) {
                ArrayList<ReelsPostModel> reelsPostModels = new ArrayList<>();
                DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                ReelsPostModel reelsPostModel = document.toObject(ReelsPostModel.class);
                Objects.requireNonNull(reelsPostModel).setDocID(document.getId());
                reelsPostModels.add(reelsPostModel);
                models.addAll(models.size(), reelsPostModels);
            }
        });
    }

    private void fetchBefore(String bool, DocumentSnapshot reelslastVisible) {
        Query query = null;

        if(bool.matches("1")) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .orderBy("ts", Query.Direction.ASCENDING)
                    .limit(1)
                    .startAfter(reelslastVisible);
        }
        else if(bool.matches("2")) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .whereEqualTo("uid", uid)
                    .orderBy("ts", Query.Direction.ASCENDING)
                    .limit(1)
                    .startAfter(reelslastVisible);
        }

        Objects.requireNonNull(query).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && Objects.requireNonNull(task.getResult()).getDocuments().size() != 0) {
                ArrayList<ReelsPostModel> reelsPostModels = new ArrayList<>();
                DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);
                ReelsPostModel reelsPostModel = document.toObject(ReelsPostModel.class);
                Objects.requireNonNull(reelsPostModel).setDocID(document.getId());
                reelsPostModels.add(reelsPostModel);
                models.addAll(0, reelsPostModels);
            }
        });
    }
}