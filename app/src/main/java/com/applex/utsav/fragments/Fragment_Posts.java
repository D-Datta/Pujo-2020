package com.applex.utsav.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.applex.utsav.R;
import com.applex.utsav.ViewAllGridMVVM.ViewAllGridActivity;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.StoreTemp;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import java.io.Serializable;
import java.util.Objects;

public class Fragment_Posts extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerview;
    private ProgressBar contentprogressposts,progressmoreposts;
    private LinearLayout noneImage;
    private ImageView noneimageview;
    private FirestorePagingAdapter adapter;
    public static DocumentSnapshot last_snapshot;
    private String uid;


    public Fragment_Posts() {
        // Required empty public constructor
    }

    public static Fragment_Posts newInstance(String id) {
        Bundle args = new Bundle();
        args.putString("id", id);
        Fragment_Posts f = new Fragment_Posts();
        f.setArguments(args);
        return f;
    }

//    public Fragment_Posts(String uid) {
//        this.uid = uid;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        recyclerview = view.findViewById(R.id.recycler_posts);
        contentprogressposts = view.findViewById(R.id.content_progress_posts);
        progressmoreposts = view.findViewById(R.id.progress_more_posts);
        noneImage = view.findViewById(R.id.none_image);
        noneimageview=view.findViewById(R.id.noneimageview);

        uid = Objects.requireNonNull(getArguments()).getString("id");

        recyclerview.setHasFixedSize(false);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setItemViewCacheSize(20);

        buildRecyclerView();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.darkpurple),getResources()
                        .getColor(R.color.darkpurple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildRecyclerView();
        });

        ///////////////Set Image Bitmap/////////////////////
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {

            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            noneimageview.setImageBitmap(scaledBitmap);
        } else if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {

            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            noneimageview.setImageBitmap(scaledBitmap);
        }
        ///////////////Set Image Bitmap/////////////////////

    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds")
                .whereEqualTo("uid", uid)
                .orderBy("ts", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setPrefetchDistance(2)
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
                    Picasso.get().load(model.getImg().get(0)).placeholder(R.drawable.image_background_grey).into(holder.post_image);
                }

                if(model.getImg()!=null) {
                    holder.post_image.setOnClickListener((View.OnClickListener) view -> {
                        Intent intent = new Intent(getContext(), ViewAllGridActivity.class);
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
                        intent.putExtra("gender",model.getGender());
                        intent.putExtra("headline",model.getHeadline());
                        intent.putExtra("challengeID",model.getChallengeID());
                        startActivity(intent);
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
                    case ERROR: BasicUtility.showToast(getContext(), "Something went wrong..."); break;
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
        }
    }
}