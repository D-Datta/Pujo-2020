package com.example.pujo360.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.pujo360.ActivityProfileCommittee;
import com.example.pujo360.R;
import com.example.pujo360.models.HomePostModel;
import com.example.pujo360.util.Utility;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class Fragment_Reels extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerview;
    private ProgressBar contentprogressreels,progressmorereels;
    private ImageView noneImage;
    //    private List<CourseModel> courseList;
    private FirestorePagingAdapter adapter;

    public Fragment_Reels() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__reels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
//        introPref= new IntroPref(getContext());
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        recyclerview = view.findViewById(R.id.recycler_reels);
        contentprogressreels = view.findViewById(R.id.content_progress_reels);
        progressmorereels = view.findViewById(R.id.progress_more_reels);
        noneImage = view.findViewById(R.id.none_image);

//        recyclerview.setHasFixedSize(false);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setItemViewCacheSize(10);

        buildRecyclerView();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.toolbarStart),getResources()
                        .getColor(R.color.md_blue_500));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildRecyclerView();
        });


        final int[] scrollY = {0};
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                scrollY[0] = scrollY[0] + dy;
                if (scrollY[0] <= 2000 && dy < 0) {
                    //floatingActionButton.setVisibility(View.GONE);
                }
                else
                {
                    if(dy < 0)
                    {
                        //floatingActionButton.setVisibility(View.VISIBLE);
//                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//                            @SuppressLint("ObjectAnimatorBinding")
//                            @Override
//                            public void onClick(View v) {
//                                recyclerView.scrollToPosition(0);
////                            recyclerView.smoothScrollToPosition();
//                                recyclerView.postDelayed(new Runnable() {
//                                    public void run() {
//                                        recyclerView.scrollToPosition(0);
//                                    }
//                                },300);
//                                // ObjectAnimator.ofInt(recyclerView, "dy", 0).setDuration(1000).start();
//                            }
//                        });
                    } else {
                        //floatingActionButton.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds")
                .whereEqualTo("uid", ActivityProfileCommittee.uid)
                .orderBy("ts", Query.Direction.DESCENDING);


        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build();
        FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, new SnapshotParser<HomePostModel>() {
                    @NonNull
                    @Override
                    public HomePostModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        HomePostModel homePostModel = snapshot.toObject(HomePostModel.class);
                        homePostModel.setDocID(snapshot.getId());
//                        courseList.add(courseModel);
                        return homePostModel;
                    }
                })
                .build();

        adapter = new FirestorePagingAdapter<HomePostModel, Fragment_Posts.ProgrammingViewHolder>(options) {

            @NonNull
            @Override
            public Fragment_Posts.ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View v = layoutInflater.inflate(R.layout.item_profile_com_post, parent, false);
                return  new Fragment_Posts.ProgrammingViewHolder(v);
            }


            @Override
            protected void onBindViewHolder(@NonNull Fragment_Posts.ProgrammingViewHolder holder, int position, @NonNull HomePostModel model) {

                if(model.getImg()!=null)
                {
                    Picasso.get().load(model.getImg().get(0)).into(holder.post_image);
                }

            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR: Utility.showToast(getContext(), "Something went wrong..."); break;
                    case LOADING_MORE: progressmorereels.setVisibility(View.VISIBLE); break;
                    case LOADED: progressmorereels.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED: contentprogressreels.setVisibility(View.GONE);
                        progressmorereels.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(adapter!=null && adapter.getItemCount() == 0)
                            noneImage.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };

        contentprogressreels.setVisibility(View.GONE);
        noneImage.setVisibility(View.GONE);
        recyclerview.setAdapter(adapter);
    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        //        TextView Name, Duration, lessons;
//        CardView Course;
        ImageView post_image;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.post_img);
//            Name = itemView.findViewById(R.id.coursename);
//            Duration = itemView.findViewById(R.id.courseduration);
//            Course = itemView.findViewById(R.id.view_course);
//            add = itemView.findViewById(R.id.add_to_batch);
//            lessons = itemView.findViewById(R.id.lessons_short);
        }
    }
}