package com.applex.utsav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.util.Log;
import com.applex.utsav.adapters.ReelsAdapter;
import com.applex.utsav.fragments.CommitteeFragment;
import com.applex.utsav.models.ReelsPostModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Objects;

public class ReelsActivity extends AppCompatActivity {

    private ViewPager2 reelsList;
    private String uid;
    private ArrayList<ReelsPostModel> models;
    private ReelsAdapter adapter;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        reelsList = findViewById(R.id.recyclerReelsViewAll);

        DocumentSnapshot lastVisible = CommitteeFragment.lastVisible;
        String bool = Objects.requireNonNull(getIntent().getStringExtra("bool"));
        if(getIntent().getStringExtra("uid") != null) {
            uid = getIntent().getStringExtra("uid");
        }

        models = new ArrayList<>();

        if(bool.matches("1")) {
            if(lastVisible != null) {
                Log.i("BAM", bool);

                query = FirebaseFirestore.getInstance()
                        .collection("Reels")
                        .orderBy("ts", Query.Direction.DESCENDING)
                        .startAfter(lastVisible);
            }
            else {

            }
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .orderBy("ts", Query.Direction.DESCENDING);
        }
        else if(bool.matches("2")) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .whereEqualTo("uid", uid)
                    .orderBy("ts", Query.Direction.DESCENDING);
        }



        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    ReelsPostModel commentModel = document.toObject(ReelsPostModel.class);
                    Objects.requireNonNull(commentModel).setDocID(document.getId());
                    models.add(commentModel);
                }
                adapter = new ReelsAdapter(models, ReelsActivity.this, reelsList);
                reelsList.setAdapter(adapter);
                reelsList.setCurrentItem(Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("position"))));
            }
        });
    }
}