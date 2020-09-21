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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.pujo360.ActivityProfileCommittee;
import com.example.pujo360.R;
import com.example.pujo360.ViewMoreHome;
import com.example.pujo360.models.HomePostModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.StoreTemp;
import com.example.pujo360.util.Utility;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Fragment_Posts extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerview;
    private ProgressBar contentprogressposts,progressmoreposts;
    private ImageView noneImage;
    private FirestorePagingAdapter adapter;


    public Fragment_Posts() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        recyclerview = view.findViewById(R.id.recycler_posts);
        contentprogressposts = view.findViewById(R.id.content_progress_posts);
        progressmoreposts = view.findViewById(R.id.progress_more_posts);
        noneImage = view.findViewById(R.id.none_image);

        recyclerview.setHasFixedSize(false);
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
                .setQuery(query, config, snapshot -> {
                    HomePostModel homePostModel = snapshot.toObject(HomePostModel.class);
                    Objects.requireNonNull(homePostModel).setDocID(snapshot.getId());
                    return homePostModel;
                })
                .build();

        adapter = new FirestorePagingAdapter<HomePostModel, ProgrammingViewHolder>(options) {
            @NonNull
            @Override
            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View v = layoutInflater.inflate(R.layout.item_profile_com_post, parent, false);
                return  new ProgrammingViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position, @NonNull HomePostModel model) {

                if(model.getImg() != null) {
                    Picasso.get().load(model.getImg().get(0)).into(holder.post_image);
                }
//                else
//                {
//                    holder.post_image.setVisibility(View.GONE);
//                    ViewGroup.LayoutParams params = holder.view.getLayoutParams();
//                    params.height = 0;
//                    holder.view.setLayoutParams(params);
//                }
                if(model.getImg()!=null) {
                    holder.post_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), ViewMoreHome.class);
                            intent.putExtra("username", model.getUsN());
                            intent.putExtra("userdp", model.getDp());
                            intent.putExtra("docID", model.getDocID());
                            StoreTemp.getInstance().setTagTemp(model.getTagL());
                            intent.putExtra("comName", model.getComName());
                            intent.putExtra("comID", model.getComID());
                            intent.putExtra("likeL", model.getLikeL());
                            if (model.getImg() != null && model.getImg().size() > 0) {
                                Bundle args = new Bundle();
                                args.putSerializable("ARRAYLIST", (Serializable) model.getImg());
                                intent.putExtra("BUNDLE", args);
                            }
                            intent.putExtra("postText", model.getTxt());
                            intent.putExtra("bool", "3");
                            intent.putExtra("commentNo", Long.toString(model.getCmtNo()));
                            intent.putExtra("newTs", Long.toString(model.getNewTs()));
                            intent.putExtra("uid", model.getUid());
                            intent.putExtra("timestamp", Long.toString(model.getTs()));
                            intent.putExtra("type", model.getType());
                            startActivity(intent);
                        }
                    });

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
                    case LOADING_MORE: progressmoreposts.setVisibility(View.VISIBLE); break;
                    case LOADED: progressmoreposts.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED: contentprogressposts.setVisibility(View.GONE);
                        progressmoreposts.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(adapter!=null && adapter.getItemCount() == 0)
                            noneImage.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };

        contentprogressposts.setVisibility(View.GONE);
        noneImage.setVisibility(View.GONE);
        recyclerview.setAdapter(adapter);
    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        ImageView post_image;
        RelativeLayout rela;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.post_img);
            rela = itemView.findViewById(R.id.rela);
//            Name = itemView.findViewById(R.id.coursename);
//            Duration = itemView.findViewById(R.id.courseduration);
//            Course = itemView.findViewById(R.id.view_course);
//            add = itemView.findViewById(R.id.add_to_batch);
//            lessons = itemView.findViewById(R.id.lessons_short);
        }
    }
}