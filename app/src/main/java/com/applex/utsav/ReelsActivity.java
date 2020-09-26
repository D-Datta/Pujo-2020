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
    private String uid;
    private Query query;
    private int query_pos;
    private ReelsAdapter adapter;
    private ArrayList<ReelsPostModel> models;
    private int checkGetMore;
    private DocumentSnapshot reelslastVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        models = new ArrayList<>();
        reelsList = findViewById(R.id.recyclerReelsViewAll);

        String bool = Objects.requireNonNull(getIntent().getStringExtra("bool"));

        if(getIntent().getStringExtra("uid") != null) {
            uid = getIntent().getStringExtra("uid");
        }

        if(getIntent().getStringExtra("query_pos") != null) {
            query_pos = Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("query_pos")));
        }

        if(bool.matches("1")) {
            if(query_pos != 0) {
                query = FirebaseFirestore.getInstance()
                        .collection("Reels")
                        .orderBy("ts", Query.Direction.DESCENDING)
                        .limit(10)
                        .startAfter(query_pos);
            }
            else {
                query = FirebaseFirestore.getInstance()
                        .collection("Reels")
                        .orderBy("ts", Query.Direction.DESCENDING)
                        .limit(10);
            }
        }
        else if(bool.matches("2")) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .whereEqualTo("uid", uid)
                    .orderBy("ts", Query.Direction.DESCENDING)
                    .limit(10);
        }

        buildRecyclerView();
    }

    private void buildRecyclerView() {

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                    ReelsPostModel reelsPostModel = document.toObject(ReelsPostModel.class);
                    Objects.requireNonNull(reelsPostModel).setDocID(document.getId());
                    models.add(reelsPostModel);
                }
                adapter = new ReelsAdapter(ReelsActivity.this, models, reelsList);
                reelsList.setAdapter(adapter);
                reelsList.setCurrentItem(Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("position"))));
            }
        });
    }

    private void fetchMore() {

        query.limit(10).startAfter(reelslastVisible).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                ArrayList<ReelsPostModel> reelsPostModels = new ArrayList<>();
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                    ReelsPostModel reelsPostModel = document.toObject(ReelsPostModel.class);
                    Objects.requireNonNull(reelsPostModel).setDocID(document.getId());
                    reelsPostModels.add(reelsPostModel);
                }
                if(models.size() > 0) {
                    int lastSize = models.size();
                    models.addAll(reelsPostModels);
                    adapter.notifyItemRangeInserted(lastSize, reelsPostModels.size());
                    reelslastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }
            }

            if(models.size() < 10) {
                checkGetMore = -1;
            }
            else {
                checkGetMore = 0;
            }
        });
    }
}