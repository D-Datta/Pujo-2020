package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import com.example.pujo360.adapters.ReelsAdapter;
import com.example.pujo360.fragments.CommitteeFragment;
import com.example.pujo360.models.ReelsPostModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class ReelsActivity extends AppCompatActivity {

    private ViewPager2 reelsList;
    private DocumentSnapshot lastVisible, lastVisibleReel;
    private String uid;
    private String bool;
    private ArrayList<ReelsPostModel> models;
    private ReelsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        reelsList = findViewById(R.id.recyclerReelsViewAll);

//        reelsList.setHasFixedSize(false);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(ReelsActivity.this);
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        reelsList.setLayoutManager(layoutManager);
//        reelsList.setNestedScrollingEnabled(true);
//        reelsList.setItemViewCacheSize(10);
//        reelsList.setDrawingCacheEnabled(true);

        lastVisible = CommitteeFragment.lastVisible;
        bool = Objects.requireNonNull(getIntent().getStringExtra("bool"));
        if(getIntent().getStringExtra("uid") != null) {
            uid = getIntent().getStringExtra("uid");
        }

        buildReelsRecyclerView();

//        SnapHelperOneByOne snapHelperOneByOne = new SnapHelperOneByOne();
//        snapHelperOneByOne.attachToRecyclerView(reelsList);
        //////////////RECYCLER VIEW////////////////////
    }

//    private static class SnapHelperOneByOne extends LinearSnapHelper {
//        @Override
//        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
//
//            if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
//                return RecyclerView.NO_POSITION;
//            }
//
//            final View currentView = findSnapView(layoutManager);
//
//            if (currentView == null) {
//                return RecyclerView.NO_POSITION;
//            }
//
//            LinearLayoutManager myLayoutManager = (LinearLayoutManager) layoutManager;
//
//            int position1 = myLayoutManager.findFirstVisibleItemPosition();
//            int position2 = myLayoutManager.findLastVisibleItemPosition();
//
//            int currentPosition = layoutManager.getPosition(currentView);
//
//            if (velocityY > 800) {
//                currentPosition = position2;
//            } else if (velocityY < 800) {
//                currentPosition = position1;
//            }
//
//            return currentPosition;
//        }
//    }

    private void buildReelsRecyclerView() {
        models = new ArrayList<>();

        Query query;
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
        }
        else if(bool.matches("2")) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .whereEqualTo("uid", uid)
                    .orderBy("ts", Query.Direction.DESCENDING);
        }

        query = FirebaseFirestore.getInstance()
                .collection("Reels")
                .orderBy("ts", Query.Direction.DESCENDING);

//        PagedList.Config config = new PagedList.Config.Builder()
//                .setInitialLoadSizeHint(10)
//                .setPageSize(3)
//                .setEnablePlaceholders(true)
//                .build();

//        FirestorePagingOptions<ReelsPostModel> options = new FirestorePagingOptions.Builder<ReelsPostModel>()
//                .setLifecycleOwner(this)
//                .setQuery(query, config, snapshot -> {
//                    ReelsPostModel reelsPostModel = new ReelsPostModel();
//                    if(snapshot.exists()) {
//                        reelsPostModel = snapshot.toObject(ReelsPostModel.class);
//                        Objects.requireNonNull(reelsPostModel).setDocID(snapshot.getId());
//                    }
//                    return reelsPostModel;
//                })
//                .build();

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
            }
        });

//        reelsAdapter = new FirestorePagingAdapter<ReelsPostModel, ReelsItemViewHolder>(options) {
//            @SuppressLint("SetTextI18n")
//            @Override
//            protected void onBindViewHolder(@NonNull ReelsItemViewHolder holder, int position, @NonNull ReelsPostModel currentItem) {
//
//            }
//
//            @NonNull
//            @Override
//            public ReelsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
//                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
//                View v = layoutInflater.inflate(R.layout.item_view_all_reels, viewGroup, false);
//                return new ReelsItemViewHolder(v);
//            }
//
//            @Override
//            public int getItemViewType(int position) { return position; }
//
//        };
//
//        RecyclerView.LayoutManager manager = reelsList.getLayoutManager();
//        reelsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//                if (newState == 0) {
//                    int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
//                    int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
//
//                    if (firstVisiblePosition >= 0) {
//                        Rect rect_parent = new Rect();
//                        reelsList.getGlobalVisibleRect(rect_parent);
//
//                        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
//                            final RecyclerView.ViewHolder holder = reelsList.findViewHolderForAdapterPosition(i);
//                            ReelsItemViewHolder cvh = (ReelsItemViewHolder) holder;
//
//                            int[] location = new int[2];
//                            Objects.requireNonNull(cvh).reels_video.getLocationOnScreen(location);
//
//                            Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.reels_video.getWidth(), location[1] + cvh.reels_video.getHeight());
//
//                            float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
//                            float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
//                            float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
//                            float overlapArea = x_overlap * y_overlap;
//                            float percent = (overlapArea / rect_parent_area) * 100.0f;
//
//                            if (percent >= 50) {
//                                if (!cvh.reels_video.isPlaying()) {
//                                    cvh.reels_video.start();
//                                }
//                            } else {
//                                cvh.reels_video.pause();
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });
    }




}