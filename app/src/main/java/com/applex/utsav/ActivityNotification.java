package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.applex.utsav.fragments.Fragment_Posts;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.NotifModel;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class ActivityNotification extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView notifRecycler;
    private ProgressBar progressMore;

    private ArrayList<NotifModel> notifModels;
    private Dialog postMenuDialog;
    private ImageView noNotif;

    public static int removeNotif = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        progressMore = findViewById(R.id.progress_more);
        progressMore.setVisibility(View.GONE);
        noNotif = findViewById(R.id.no_recent_notiff);
        notifRecycler = findViewById(R.id.recyclerNotif);
        notifModels = new ArrayList<>();

        notifRecycler.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivityNotification.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notifRecycler.setLayoutManager(linearLayoutManager);
        notifRecycler.setItemAnimator(new DefaultItemAnimator());
        notifRecycler.setItemViewCacheSize(20);

        buildRecyclerView();

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources()
                .getColor(R.color.purple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildRecyclerView();
        });

    }

    public void buildRecyclerView() {
        notifModels.clear();
        Query query = FirebaseFirestore.getInstance()
                .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Notifs/")
                .orderBy("ts", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    HomePostModel homePostModel = snapshot.toObject(HomePostModel.class);
                    Objects.requireNonNull(homePostModel).setDocID(snapshot.getId());
                    return homePostModel;
                })
                .build();

//        adapter = new FirestorePagingAdapter<HomePostModel, Fragment_Posts.ProgrammingViewHolder>(options) {
//            @NonNull
//            @Override
//            public Fragment_Posts.ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//                View v = layoutInflater.inflate(R.layout.item_profile_com_post, parent, false);
//                return  new Fragment_Posts.ProgrammingViewHolder(v);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull Fragment_Posts.ProgrammingViewHolder holder, int position, @NonNull HomePostModel model) {
//
//                if(model.getImg() != null) {
//                    Picasso.get().load(model.getImg().get(0)).into(holder.post_image);
//                }
//
//                if(model.getImg()!=null) {
//                    holder.post_image.setOnClickListener((View.OnClickListener) view -> {
//                        Intent intent = new Intent(getContext(), ViewMoreHome.class);
//                        intent.putExtra("username", model.getUsN());
//                        intent.putExtra("userdp", model.getDp());
//                        intent.putExtra("docID", model.getDocID());
//                        StoreTemp.getInstance().setTagTemp(model.getTagL());
//                        intent.putExtra("comName", model.getComName());
//                        intent.putExtra("comID", model.getComID());
//                        intent.putExtra("likeL", model.getLikeL());
//                        if (model.getImg() != null && model.getImg().size() > 0) {
//                            Bundle args = new Bundle();
//                            args.putSerializable("ARRAYLIST", (Serializable) model.getImg());
//                            intent.putExtra("BUNDLE", args);
//                        }
//                        intent.putExtra("postText", model.getTxt());
//                        intent.putExtra("bool", "3");
//                        intent.putExtra("commentNo", Long.toString(model.getCmtNo()));
//                        intent.putExtra("newTs", Long.toString(model.getNewTs()));
//                        intent.putExtra("uid", model.getUid());
//                        intent.putExtra("timestamp", Long.toString(model.getTs()));
//                        intent.putExtra("type", model.getType());
//                        startActivity(intent);
//                    });
//
//                }
//
//            }
//
//            @Override
//            public int getItemViewType(int position) {
//                return position;
//            }
//
//            @Override
//            protected void onLoadingStateChanged(@NonNull LoadingState state) {
//
//                super.onLoadingStateChanged(state);
//                switch (state) {
//                    case ERROR: Utility.showToast(getContext(), "Something went wrong..."); break;
//                    case LOADING_MORE: progressmoreposts.setVisibility(View.VISIBLE); break;
//                    case LOADED: progressmoreposts.setVisibility(View.GONE);
//                        if(swipeRefreshLayout.isRefreshing()) {
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
//                        break;
//                    case FINISHED: contentprogressposts.setVisibility(View.GONE);
//                        progressmoreposts.setVisibility(View.GONE);
//                        if(swipeRefreshLayout.isRefreshing()) {
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
//                        if(adapter!=null && adapter.getItemCount() == 0)
//                            noneImage.setVisibility(View.VISIBLE);
//                        break;
//                }
//            }
//        };
//
//        contentprogressposts.setVisibility(View.GONE);
//        noneImage.setVisibility(View.GONE);
//        recyclerview.setAdapter(adapter);

    }

}