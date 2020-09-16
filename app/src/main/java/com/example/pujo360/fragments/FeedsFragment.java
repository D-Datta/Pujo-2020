package com.example.pujo360.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pujo360.CommitteeViewAll;
import com.example.pujo360.LinkPreview.ApplexLinkPreview;
import com.example.pujo360.LinkPreview.ViewListener;
import com.example.pujo360.R;
import com.example.pujo360.ViewMoreHome;
import com.example.pujo360.adapters.CommunityAdapter;
import com.example.pujo360.adapters.TagAdapter;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.models.HomePostModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.StoreTemp;
import com.example.pujo360.util.Utility;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class FeedsFragment extends Fragment {

    private WeakReference<Dialog> dialog;
    private WeakReference<ImageView> noPostYet1;
    private WeakReference<ProgressDialog> progressDialog;
    private WeakReference<BottomSheetDialog> postMenuDialog;
    private WeakReference<FloatingActionButton> floatingActionButton;
    private WeakReference<SwipeRefreshLayout> swipeRefreshLayout;
    private WeakReference<ProgressBar> progressMore, contentProgress, contentProgCom;

    private WeakReference<TextView> view_all_NoPost;
    private WeakReference<ImageView> infoNoPost;
    private WeakReference<RecyclerView> comRecyclerView;
    private WeakReference<LinearLayout> campusLL, LL;
    //private SliderView sliderViewNoPost;

    public static int changed = 0;
    public static int comDelete = 0;

    private WeakReference<RecyclerView> mRecyclerView;

    private FirestorePagingAdapter adapter;
    private IntroPref introPref;

    private WeakReference<String> DP, USERNAME;



    public FeedsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feeds, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        introPref = new IntroPref(getActivity());
        DP = new WeakReference<>(introPref.getUserdp());
        USERNAME = new WeakReference<>(introPref.getFullName());

        changed = 0;

        swipeRefreshLayout= new WeakReference<>(view.findViewById(R.id.swiperefresh));
        contentProgress = new WeakReference<>(view.findViewById(R.id.content_progress));
        progressMore = new WeakReference<>(view.findViewById(R.id.progress_more));
        floatingActionButton = new WeakReference<>(view.findViewById(R.id.to_the_top_campus));
        noPostYet1= new WeakReference<>(view.findViewById(R.id.no_recent_com_post1));

        //////////////RECYCLER VIEW////////////////////
        mRecyclerView = new WeakReference<>(view.findViewById(R.id.recyclerCampusPost));
        mRecyclerView.get().setHasFixedSize(false);
        WeakReference<LinearLayoutManager> layoutManager = new WeakReference<>(new LinearLayoutManager(getActivity()));
        layoutManager.get().setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.get().setLayoutManager(layoutManager.get());
        mRecyclerView.get().setNestedScrollingEnabled(true);
        mRecyclerView.get().setItemViewCacheSize(10);


        //////////////RECYCLER VIEW////////////////////

        //////////WHEN THERE ARE NO POSTS IN CAMPUS/////////
        contentProgCom = new WeakReference<>(view.findViewById(R.id.content_progress_community));
        infoNoPost = new WeakReference<>(view.findViewById(R.id.info));
        view_all_NoPost = new WeakReference<>(view.findViewById(R.id.community_view_all));
        comRecyclerView = new WeakReference<>(view.findViewById(R.id.communityRecyclerNoPost));
        campusLL = new WeakReference<>(view.findViewById(R.id.campusLL));
        LL = new WeakReference<>(view.findViewById(R.id.LL));
        //////////WHEN THERE ARE NO POSTS IN CAMPUS/////////


        buildRecyclerView();

        swipeRefreshLayout.get()
                .setColorSchemeColors(getResources().getColor(R.color.toolbarStart),getResources()
                        .getColor(R.color.md_blue_500));
        swipeRefreshLayout.get().setOnRefreshListener(() -> {
            swipeRefreshLayout.get().setRefreshing(true);
            contentProgCom.get().setVisibility(View.GONE);
            buildRecyclerView();
        });

        final int[] scrollY = {0};
        mRecyclerView.get().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollY[0] = scrollY[0] + dy;
                if (scrollY[0] <= 2000 && dy < 0) {
                    floatingActionButton.get().setVisibility(View.GONE);
                }
                else {
                    if(dy < 0){
                        floatingActionButton.get().setVisibility(View.VISIBLE);
                        floatingActionButton.get().setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("ObjectAnimatorBinding")
                            @Override
                            public void onClick(View v) {
                                recyclerView.scrollToPosition(0);
                                recyclerView.postDelayed(new Runnable() {
                                    public void run() {
                                        recyclerView.scrollToPosition(0);
                                    }
                                },300);
                            }
                        });
                    } else {
                        floatingActionButton.get().setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds/")
                .whereEqualTo("type", "user")
                .orderBy("newTs", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    HomePostModel newPostModel = new HomePostModel();
                    if(snapshot.exists()) {
                        newPostModel = snapshot.toObject(HomePostModel.class);
                        newPostModel.setDocID(snapshot.getId());
                    }
                    return newPostModel;
                })
                .build();

        adapter = new FirestorePagingAdapter<HomePostModel, RecyclerView.ViewHolder>(options) {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull HomePostModel currentItem) {

                if(holder.getItemViewType() == 0) {

                    floatingActionButton.get().setVisibility(View.GONE);

                    CommunityViewHolder communityViewHolder = (CommunityViewHolder)holder;

                    communityViewHolder.view_all.get().setOnClickListener(v ->
                            startActivity(new Intent(getActivity(), CommitteeViewAll.class))
                    );

                    buildCommunityRecyclerView(communityViewHolder.cRecyclerView);
                    //buildSliderView(communityViewHolder.sliderView);

                    ///////////////////FOR THE FIRST POST/////////////////////
                    DocumentReference likeStore;
                    String timeAgo = Utility.getTimeAgo(currentItem.getTs());
                    communityViewHolder.minsago.get().setText(timeAgo);
                    if (timeAgo != null) {
                        if (timeAgo.matches("just now")) {
                            communityViewHolder.minsago.get().setTextColor(Color.parseColor("#00C853"));
                        } else {
                            communityViewHolder.minsago.get().setTextColor(Color.parseColor("#aa212121"));
                        }
                    }


                    if (currentItem.getComName() != null) {
                        communityViewHolder.comName.get().setVisibility(View.VISIBLE);
                        communityViewHolder.comName.get().setText(currentItem.getComName());
                        communityViewHolder.comName.get().setBackground(getResources().getDrawable(R.drawable.custom_com_backgnd));

                        communityViewHolder.comName.get().setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), CommunityActivity.class);
                            intent.putExtra("comID", currentItem.getComID());
                            startActivity(intent);
                        });
                    }
                    else {
                        communityViewHolder.comName.get().setVisibility(View.GONE);
                        communityViewHolder.comName.get().setText(null);
                    }

                    ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////

                    likeStore = FirebaseFirestore.getInstance().document("Feeds/" + currentItem.getDocID() + "/");

                    communityViewHolder.menuPost.get().setVisibility(View.VISIBLE);

                    ///////////SET DOCUMENT REFERENCE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////


                    ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                    if (DP != null) {

//                            if (DP.get().matches("0")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_1);
//                            } else if (DP.get().matches("1")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_2);
//                            } else if (DP.get().matches("2")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_3);
//                            } else if (DP.get().matches("3")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_4);
//                            } else if (DP.get().matches("4")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_5);
//                            } else if (DP.get().matches("5")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_6);
//                            } else if (DP.get().matches("6")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_7);
//                            } else if (DP.get().matches("7")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_8);
//                            } else if (DP.get().matches("8")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_9);
//                            } else if (DP.get().matches("9")) {
//                                communityViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_10);
//                            } else {
                            Picasso.get().load(DP.get()).fit().centerCrop()
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(communityViewHolder.profileimage.get());

                    } else {
                        communityViewHolder.profileimage.get().setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }

                    ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                    ///////////TAGLIST///////////////

                    ///////////TAG RECYCLER SETUP////////////////
                    communityViewHolder.tagList.get().setHasFixedSize(false);
                    WeakReference<LinearLayoutManager> linearLayoutManager = new WeakReference<>(new LinearLayoutManager(getContext()));
                    linearLayoutManager.get().setOrientation(LinearLayoutManager.HORIZONTAL);
                    communityViewHolder.tagList.get().setNestedScrollingEnabled(true);
                    communityViewHolder.tagList.get().setLayoutManager(linearLayoutManager.get());
                    ///////////TAG RECYCLER SETUP////////////////

                    if (currentItem.getTagL() != null && currentItem.getTagL().size() > 0) {
                        communityViewHolder.tagList.get().setVisibility(View.VISIBLE);
                        TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL(), getActivity());
                        communityViewHolder.tagList.get().setAdapter(tagAdapter);
                    } else {
                        communityViewHolder.tagList.get().setAdapter(null);
                        communityViewHolder.tagList.get().setVisibility(View.GONE);
                    }
                    /////////TAGLIST///////////////

                    //////////////LOADING USERNAME AND USERDP FROM USERNODE FOR CURRENT POST USER///////////////
                    ////////////ANONYMOUS POST///////////////
                    if (currentItem.getUsN() != null) {
//                            communityViewHolder.userimage.get().setImageResource(R.drawable.ic_anonymous_icon);
//                            communityViewHolder.username.get().setText(currentItem.getUsN());
//                        } else {
                        communityViewHolder.userimage.get().setOnClickListener(v -> {
                            Intent intent = new Intent(getContext(), ProfileActivity.class);
                            intent.putExtra("uid", currentItem.getUid());
                            startActivity(intent);
                        });

                        communityViewHolder.username.get().setOnClickListener(v -> {
                            Intent intent = new Intent(getContext(), ProfileActivity.class);
                            intent.putExtra("uid", currentItem.getUid());
                            startActivity(intent);
                        });

                        communityViewHolder.username.get().setText(currentItem.getUsN());

                        ////////////NORMAL POST///////////////
                        if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
//                                if (currentItem.getDp().matches("0")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_1);
//                                } else if (currentItem.getDp().matches("1")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_2);
//                                } else if (currentItem.getDp().matches("2")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_3);
//                                } else if (currentItem.getDp().matches("3")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_4);
//                                } else if (currentItem.getDp().matches("4")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_5);
//                                } else if (currentItem.getDp().matches("5")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_6);
//                                } else if (currentItem.getDp().matches("6")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_7);
//                                } else if (currentItem.getDp().matches("7")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_8);
//                                } else if (currentItem.getDp().matches("8")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_9);
//                                } else if (currentItem.getDp().matches("9")) {
//                                    communityViewHolder.userimage.get().setImageResource(R.drawable.default_dp_10);
//                                } else {
                                Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                                        .into(communityViewHolder.userimage.get(), new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                communityViewHolder.userimage.get().setImageResource(R.drawable.ic_account_circle_black_24dp);
                                            }
                                        });
//                                }
                        }
                        else {
                            communityViewHolder.userimage.get().setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }

                    }
                    ///////////////OPEN VIEW MORE//////////////
                    communityViewHolder.itemHome.get().setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());

                        intent.putExtra("likeL", currentItem.getLikeL());
                        intent.putExtra("postPic", currentItem.getImg());
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("bool", "3");
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));

                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        startActivity(intent);
                    });

                    communityViewHolder.text_content.get().setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());

                        intent.putExtra("likeL", currentItem.getLikeL());
                        intent.putExtra("postPic", currentItem.getImg());
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("bool", "3");
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));

                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        startActivity(intent);
                    });

                    communityViewHolder.postimage.get().setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        intent.putExtra("postPic", currentItem.getImg());
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("bool", "3");


                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        startActivity(intent);
                    });

                    communityViewHolder.flamedBy.get().setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        //            intent.putExtra("tagL", currentItem.getTagL());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        intent.putExtra("postPic", currentItem.getImg());
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("bool", "3");

                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("likeLOpen", "likeLOpen");
                        startActivity(intent);

                    });


                    ///////////////OPEN VIEW MORE//////////////

                    //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                    if (currentItem.getTxt() == null || currentItem.getTxt().isEmpty()) {
                        communityViewHolder.text_content.get().setVisibility(View.GONE);
                        communityViewHolder.LinkPreview.get().setVisibility(View.GONE);
                        communityViewHolder.text_content.get().setText(null);
                    } else {
                        communityViewHolder.text_content.get().setVisibility(View.VISIBLE);
                        communityViewHolder.text_content.get().setText(currentItem.getTxt());
                        if (communityViewHolder.text_content.get().getUrls().length > 0) {
                            URLSpan urlSnapItem = communityViewHolder.text_content.get().getUrls()[0];
                            String url = urlSnapItem.getURL();
                            if (url.contains("http")) {
                                communityViewHolder.LinkPreview.get().setVisibility(View.VISIBLE);
                                communityViewHolder.LinkPreview.get().setLink(url, new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean status) {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //do stuff like remove view etc
                                                communityViewHolder.LinkPreview.get().setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                });
                            }

                        } else {
                            communityViewHolder.LinkPreview.get().setVisibility(View.GONE);
                        }

                    }

                    String postimage_url = currentItem.getImg();
                    if (postimage_url != null) {
                        communityViewHolder.postimage.get().setVisibility(View.VISIBLE);
                        Picasso.get().load(postimage_url)
                                .placeholder(R.drawable.image_background_grey)
                                .into(communityViewHolder.postimage.get());

                        communityViewHolder.postimage.get().setOnLongClickListener(v -> {

                            Picasso.get().load(postimage_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    save_Dialog(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }

                            });
                            return true;
                        });
                    } else
                        communityViewHolder.postimage.get().setVisibility(View.GONE);

                    //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                    ///////////////////FLAMES///////////////////////

                    //INITIAL SETUP//
                    if (currentItem.getLikeL() != null) {
                        /////////////////UPDATNG FLAMED BY NO.//////////////////////
                        if (currentItem.getLikeL().size() == 0) {
                            communityViewHolder.flamedBy.get().setText("Not flamed yet");
                        } else if (currentItem.getLikeL().size() == 1)
                            communityViewHolder.flamedBy.get().setText("Flamed by 1");
                        else {
                            communityViewHolder.flamedBy.get().setText("Flamed by " + currentItem.getLikeL().size() + " people");
                        }

                        for (int j = 0; j < currentItem.getLikeL().size(); j++) {
                            if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                                communityViewHolder.flameimg.get().setImageResource(R.drawable.ic_flame_red);
                                currentItem.setLikeCheck(j);
                                if ((currentItem.getLikeL().size() - 1) == 1)
                                    communityViewHolder.flamedBy.get().setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " other");
                                else if ((currentItem.getLikeL().size() - 1) == 0) {
                                    communityViewHolder.flamedBy.get().setText("Flamed by you");
                                } else
                                    communityViewHolder.flamedBy.get().setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " others");
                                //Position in likeList where the current USer UId is found stored in likeCheck
                            }
                        }
                    } else {
                        communityViewHolder.flamedBy.get().setText("Not flamed yet");
                        communityViewHolder.flameimg.get().setImageResource(R.drawable.ic_btmnav_notifications);
                    }
                    //INITIAL SETUP//


                    PushDownAnim.setPushDownAnimTo(communityViewHolder.flameimg.get())
                            .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                            .setOnClickListener(v -> {
                                if (currentItem.getLikeCheck() >= 0) {//was already liked by current user
                                    communityViewHolder.flameimg.get().setImageResource(R.drawable.ic_btmnav_notifications);
                                    if (currentItem.getLikeL().size() - 1 == 0) {
                                        communityViewHolder.flamedBy.get().setText("Not flamed yet");
                                    } else
                                        communityViewHolder.flamedBy.get().setText("Flamed by " + (currentItem.getLikeL().size() - 1) + " people");
                                    ///////////REMOVE CURRENT USER LIKE/////////////
                                    currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                                    currentItem.setLikeCheck(-1);

                                    //                likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

                                    ///////////////////BATCH WRITE///////////////////
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                    DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                    batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                    batch.delete(flamedDoc);

                                    batch.commit().addOnSuccessListener(task -> {

                                    });
                                    ///////////////////BATCH WRITE///////////////////
                                } else if (currentItem.getLikeCheck() < 0 && currentItem.getLikeL() != null) {
                                    Utility.vibrate(getContext());
                                    communityViewHolder.flameimg.get().setImageResource(R.drawable.ic_flame_red);
                                    if (currentItem.getLikeL().size() == 0)
                                        communityViewHolder.flamedBy.get().setText("Flamed by you");
                                    else if (currentItem.getLikeL().size() == 1)
                                        communityViewHolder.flamedBy.get().setText("Flamed by you & " + currentItem.getLikeL().size() + " other");
                                    else
                                        communityViewHolder.flamedBy.get().setText("Flamed by you & " + currentItem.getLikeL().size() + " others");

                                    //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                    currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                    currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);//For local changes

                                    ///////////////////BATCH WRITE///////////////////
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                    FlamedModel flamedModel = new FlamedModel();
                                    long tsLong = System.currentTimeMillis();

                                    flamedModel.setPostID(currentItem.getDocID());
                                    flamedModel.setTs(tsLong);
                                    flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                    flamedModel.setUserdp(DP.get());
                                    flamedModel.setUsername(USERNAME.get());
                                    flamedModel.setPostUid(currentItem.getUid());

                                    DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                    batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                    batch.set(flamedDoc, flamedModel);
                                    if (currentItem.getLikeL().size() % 5 == 0) {
                                        batch.update(likeStore, "newTs", tsLong);
                                    }
                                    batch.commit().addOnSuccessListener(task -> {

                                    });
                                    ///////////////////BATCH WRITE///////////////////
                                } else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                                    Utility.vibrate(getActivity());
                                    communityViewHolder.flameimg.get().setImageResource(R.drawable.ic_flame_red);
                                    if (currentItem.getLikeL() != null)
                                        communityViewHolder.flamedBy.get().setText("Flamed by you & " + (currentItem.getLikeL().size() + 1) + " people");
                                    else
                                        communityViewHolder.flamedBy.get().setText("Flamed by you");

                                    //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                    currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                    currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);
                                    //For local changes current item like added to remote list end

                                    ///////////////////BATCH WRITE///////////////////
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                    FlamedModel flamedModel = new FlamedModel();
                                    long tsLong = System.currentTimeMillis();

                                    flamedModel.setPostID(currentItem.getDocID());
                                    flamedModel.setTs(tsLong);
                                    flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                    flamedModel.setUserdp(DP.get());
                                    flamedModel.setUsername(USERNAME.get());
                                    flamedModel.setPostUid(currentItem.getUid());

                                    DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                    batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                    batch.set(flamedDoc, flamedModel);
                                    if (currentItem.getLikeL().size() % 5 == 0) {
                                        batch.update(likeStore, "newTs", tsLong);
                                    }
                                    batch.commit().addOnSuccessListener(task -> {

                                    });
                                    ///////////////////BATCH WRITE///////////////////
                                }
                            });


                    if (currentItem.getCmtNo() > 0) {
                        communityViewHolder.commentimg.get().setImageResource(R.drawable.comment_yellow);
                        if (currentItem.getCmtNo() == 1)
                            communityViewHolder.commentCount.get().setText(currentItem.getCmtNo() + " comment");
                        else if (currentItem.getCmtNo() > 1)
                            communityViewHolder.commentCount.get().setText(currentItem.getCmtNo() + " comments");

                    } else {
                        communityViewHolder.commentimg.get().setImageResource(R.drawable.ic_comment);
                        communityViewHolder.commentCount.get().setText("No comments");
                    }


                    ////////POST MENU///////
                    communityViewHolder.menuPost.get().setOnClickListener(v -> {
                        if (currentItem.getUid().matches(FirebaseAuth.getInstance().getUid())) {
                            postMenuDialog = new WeakReference<>(new BottomSheetDialog(getActivity()));

                            postMenuDialog.get().setContentView(R.layout.dialog_post_menu_3);
                            postMenuDialog.get().setCanceledOnTouchOutside(TRUE);

                            postMenuDialog.get().findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                                Intent i = new Intent(getContext(), NewPostHome.class);
                                i.putExtra("target", "100"); //target value for edit post
                                i.putExtra("bool", "3");
                                i.putExtra("usN", currentItem.getUsN());
                                i.putExtra("dp", currentItem.getDp());
                                i.putExtra("uid", currentItem.getUid());

                                i.putExtra("img", currentItem.getImg());
                                i.putExtra("txt", currentItem.getTxt());
                                i.putExtra("comID", currentItem.getComID());
                                i.putExtra("comName", currentItem.getComName());

                                i.putExtra("ts", Long.toString(currentItem.getTs()));
                                i.putExtra("newTs", Long.toString(currentItem.getNewTs()));

                                StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                                i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));

                                i.putExtra("likeL", currentItem.getLikeL());
                                i.putExtra("likeCheck", currentItem.getLikeCheck());
                                i.putExtra("docID", currentItem.getDocID());
                                i.putExtra("reportL", currentItem.getReportL());
                                i.putExtra("challengeID", currentItem.getChallengeID());
                                startActivity(i);

                                postMenuDialog.get().dismiss();

                            });

                            postMenuDialog.get().findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Are you sure?")
                                        .setMessage("Post will be deleted permanently")
                                        .setPositiveButton("Delete", (dialog, which) -> {
                                            progressDialog = new WeakReference<>(new ProgressDialog(getActivity()));
                                            progressDialog.get().setTitle("Deleting Post");
                                            progressDialog.get().setMessage("Please wait...");
                                            progressDialog.get().setCancelable(false);
                                            progressDialog.get().show();
                                            FirebaseFirestore.getInstance()
                                                    .collection("Feeds/").document(currentItem
                                                    .getDocID()).delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        communityViewHolder.first_post.get().setVisibility(View.GONE);
                                                        progressDialog.get().dismiss();
                                                        FirebaseFirestore.getInstance()
                                                                .collection("Feeds/")
                                                                .orderBy("newTs", Query.Direction.DESCENDING)
                                                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if(task.isSuccessful()) {
                                                                    if(task.getResult().size() == 0) {
                                                                        communityViewHolder.noPost.get().setVisibility(View.VISIBLE);
                                                                    }
                                                                    else {
                                                                        communityViewHolder.noPost.get().setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    });
                                            postMenuDialog.get().dismiss();

                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                        .setCancelable(true)
                                        .show();
                            });

                            postMenuDialog.get().findViewById(R.id.share_post).setOnClickListener(v1 -> {
                                String link = "https://www.utsavapp.in/android/feeds/"+ currentItem.getDocID();
                                Intent i = new Intent();
                                i.setAction(Intent.ACTION_SEND);
                                i.putExtra(Intent.EXTRA_TEXT, link);
                                i.setType("text/plain");
                                startActivity(Intent.createChooser(i, "Share with"));
                                postMenuDialog.get().dismiss();

                            });

                            postMenuDialog.get().findViewById(R.id.report_post).setOnClickListener(v12 -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Feeds/").document(currentItem.getDocID())
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                                postMenuDialog.get().dismiss();

                            });

                            Objects.requireNonNull(postMenuDialog.get().getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.get().show();

                        }
                        else {
                            postMenuDialog = new WeakReference<>(new BottomSheetDialog(getActivity()));

                            postMenuDialog.get().setContentView(R.layout.dialog_post_menu);
                            postMenuDialog.get().setCanceledOnTouchOutside(TRUE);

                            postMenuDialog.get().findViewById(R.id.share_post).setOnClickListener(v13 -> {
                                String link = "https://www.utsavapp.in/android/feeds/"+ currentItem.getDocID();
                                Intent i = new Intent();
                                i.setAction(Intent.ACTION_SEND);
                                i.putExtra(Intent.EXTRA_TEXT, link);
                                i.setType("text/plain");
                                startActivity(Intent.createChooser(i, "Share with"));
                                postMenuDialog.get().dismiss();

                            });

                            postMenuDialog.get().findViewById(R.id.report_post).setOnClickListener(v14 -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Feeds/").document(currentItem.getDocID())
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                                postMenuDialog.get().dismiss();

                            });
                            Objects.requireNonNull(postMenuDialog.get().getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.get().show();

                        }
                    });
                    ////////POST MENU///////

                    ///////////////////FOR THE FIRST POST/////////////////////

                }
                else {

                    ProgrammingViewHolder programmingViewHolder = (ProgrammingViewHolder) holder;
                    DocumentReference likeStore;
                    if (currentItem != null) {
                        String timeAgo = Utility.getTimeAgo(currentItem.getTs());
                        programmingViewHolder.minsago.get().setText(timeAgo);
                        if (timeAgo != null) {
                            if (timeAgo.matches("just now")) {
                                programmingViewHolder.minsago.get().setTextColor(Color.parseColor("#00C853"));
                            } else {
                                programmingViewHolder.minsago.get().setTextColor(Color.parseColor("#aa212121"));
                            }
                        }


                        if (currentItem.getComName() != null) {
                            programmingViewHolder.comName.get().setVisibility(View.VISIBLE);
                            programmingViewHolder.comName.get().setText(currentItem.getComName());
                            programmingViewHolder.comName.get().setBackground(getResources().getDrawable(R.drawable.custom_com_backgnd));

                            programmingViewHolder.comName.get().setOnClickListener(v -> {
                                Intent intent = new Intent(getActivity(), CommunityActivity.class);
                                intent.putExtra("comID", currentItem.getComID());
                                startActivity(intent);
                            });
                        }
                        else {
                            programmingViewHolder.comName.get().setVisibility(View.GONE);
                            programmingViewHolder.comName.get().setText(null);
                        }

                        ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////

                        likeStore = FirebaseFirestore.getInstance().document("Feeds/" + currentItem.getDocID() + "/");

                        programmingViewHolder.menuPost.get().setVisibility(View.VISIBLE);

                        ///////////SET DOCUMENT REFERENCE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////


                        ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                        if (DP != null) {

//                            if (DP.get().matches("0")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_1);
//                            } else if (DP.get().matches("1")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_2);
//                            } else if (DP.get().matches("2")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_3);
//                            } else if (DP.get().matches("3")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_4);
//                            } else if (DP.get().matches("4")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_5);
//                            } else if (DP.get().matches("5")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_6);
//                            } else if (DP.get().matches("6")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_7);
//                            } else if (DP.get().matches("7")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_8);
//                            } else if (DP.get().matches("8")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_9);
//                            } else if (DP.get().matches("9")) {
//                                programmingViewHolder.profileimage.get().setImageResource(R.drawable.default_dp_10);
//                            } else {
                                Picasso.get().load(DP.get()).fit().centerCrop()
                                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                                        .into(programmingViewHolder.profileimage.get());
//                            }
                        } else {
                            programmingViewHolder.profileimage.get().setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }

                        ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                        ///////////TAGLIST///////////////

                        ///////////TAG RECYCLER SETUP////////////////
                        programmingViewHolder.tagList.get().setHasFixedSize(false);
                        WeakReference<LinearLayoutManager> linearLayoutManager = new WeakReference<>(new LinearLayoutManager(getContext()));
                        linearLayoutManager.get().setOrientation(LinearLayoutManager.HORIZONTAL);
                        programmingViewHolder.tagList.get().setNestedScrollingEnabled(true);
                        programmingViewHolder.tagList.get().setLayoutManager(linearLayoutManager.get());
                        ///////////TAG RECYCLER SETUP////////////////

                        if (currentItem.getTagL() != null && currentItem.getTagL().size() > 0) {
                            programmingViewHolder.tagList.get().setVisibility(View.VISIBLE);
                            TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL(), getActivity());
                            programmingViewHolder.tagList.get().setAdapter(tagAdapter);
                        } else {
                            programmingViewHolder.tagList.get().setAdapter(null);
                            programmingViewHolder.tagList.get().setVisibility(View.GONE);
                        }
                        /////////TAGLIST///////////////

                        //////////////LOADING USERNAME AND USERDP FROM USERNODE FOR CURRENT POST USER///////////////
                        ////////////ANONYMOUS POST///////////////
                        if (currentItem.getUsN() != null) {
//                            programmingViewHolder.userimage.get().setImageResource(R.drawable.ic_anonymous_icon);
//                            programmingViewHolder.username.get().setText(currentItem.getUsN());
//                        } else {
                            programmingViewHolder.userimage.get().setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), ProfileActivity.class);
                                intent.putExtra("uid", currentItem.getUid());
                                startActivity(intent);
                            });

                            programmingViewHolder.username.get().setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), ProfileActivity.class);
                                intent.putExtra("uid", currentItem.getUid());
                                startActivity(intent);
                            });

                            programmingViewHolder.username.get().setText(currentItem.getUsN());

                            ////////////NORMAL POST///////////////
                            if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
//                                if (currentItem.getDp().matches("0")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_1);
//                                } else if (currentItem.getDp().matches("1")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_2);
//                                } else if (currentItem.getDp().matches("2")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_3);
//                                } else if (currentItem.getDp().matches("3")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_4);
//                                } else if (currentItem.getDp().matches("4")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_5);
//                                } else if (currentItem.getDp().matches("5")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_6);
//                                } else if (currentItem.getDp().matches("6")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_7);
//                                } else if (currentItem.getDp().matches("7")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_8);
//                                } else if (currentItem.getDp().matches("8")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_9);
//                                } else if (currentItem.getDp().matches("9")) {
//                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.default_dp_10);
//                                } else {
                                    Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                                            .into(programmingViewHolder.userimage.get(), new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    programmingViewHolder.userimage.get().setImageResource(R.drawable.ic_account_circle_black_24dp);
                                                }
                                            });
                                }
//                            }
                            else {
                                programmingViewHolder.userimage.get().setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }

                        }



                        ///////////////OPEN VIEW MORE//////////////
                        programmingViewHolder.itemHome.get().setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());

                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("bool", "3");
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });

                        programmingViewHolder.postimage.get().setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());
                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("bool", "3");
                            intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });

                        programmingViewHolder.text_content.get().setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());

                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("bool", "3");
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });
                        programmingViewHolder.flamedBy.get().setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());
                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());
                            //            intent.putExtra("tagL", currentItem.getTagL());
                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("bool", "3");

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                            intent.putExtra("likeLOpen", "likeLOpen");
                            startActivity(intent);

                        });


                        ///////////////OPEN VIEW MORE//////////////

                        //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                        if (currentItem.getTxt() == null || currentItem.getTxt().isEmpty()) {
                            programmingViewHolder.text_content.get().setVisibility(View.GONE);
                            programmingViewHolder.LinkPreview.get().setVisibility(View.GONE);
                            programmingViewHolder.text_content.get().setText(null);
                        } else {
                            programmingViewHolder.text_content.get().setVisibility(View.VISIBLE);
                            programmingViewHolder.text_content.get().setText(currentItem.getTxt());
                            if (programmingViewHolder.text_content.get().getUrls().length > 0) {
                                URLSpan urlSnapItem = programmingViewHolder.text_content.get().getUrls()[0];
                                String url = urlSnapItem.getURL();
                                if (url.contains("http")) {
                                    programmingViewHolder.LinkPreview.get().setVisibility(View.VISIBLE);
                                    programmingViewHolder.LinkPreview.get().setLink(url, new ViewListener() {
                                        @Override
                                        public void onSuccess(boolean status) {
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //do stuff like remove view etc
                                                    programmingViewHolder.LinkPreview.get().setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    });
                                }

                            } else {
                                programmingViewHolder.LinkPreview.get().setVisibility(View.GONE);
                            }

                        }

                        String postimage_url = currentItem.getImg();
                        if (postimage_url != null) {
                            programmingViewHolder.postimage.get().setVisibility(View.VISIBLE);
                            Picasso.get().load(postimage_url)
                                    .placeholder(R.drawable.image_background_grey)
                                    .into(programmingViewHolder.postimage.get());

                            programmingViewHolder.postimage.get().setOnLongClickListener(v -> {

                                Picasso.get().load(postimage_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        save_Dialog(bitmap);
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }

                                });
                                return true;
                            });
                        } else
                            programmingViewHolder.postimage.get().setVisibility(View.GONE);

                        //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                        ///////////////////FLAMES///////////////////////

                        //INITIAL SETUP//
                        if (currentItem.getLikeL() != null) {
                            /////////////////UPDATNG FLAMED BY NO.//////////////////////
                            if (currentItem.getLikeL().size() == 0) {
                                programmingViewHolder.flamedBy.get().setText("Not flamed yet");
                            } else if (currentItem.getLikeL().size() == 1)
                                programmingViewHolder.flamedBy.get().setText("Flamed by 1");
                            else {
                                programmingViewHolder.flamedBy.get().setText("Flamed by " + currentItem.getLikeL().size() + " people");
                            }

                            for (int j = 0; j < currentItem.getLikeL().size(); j++) {
                                if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                                    programmingViewHolder.flameimg.get().setImageResource(R.drawable.ic_flame_red);
                                    currentItem.setLikeCheck(j);
                                    if ((currentItem.getLikeL().size() - 1) == 1)
                                        programmingViewHolder.flamedBy.get().setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " other");
                                    else if ((currentItem.getLikeL().size() - 1) == 0) {
                                        programmingViewHolder.flamedBy.get().setText("Flamed by you");
                                    } else
                                        programmingViewHolder.flamedBy.get().setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " others");
                                    //Position in likeList where the current USer UId is found stored in likeCheck
                                }
                            }
                        } else {
                            programmingViewHolder.flamedBy.get().setText("Not flamed yet");
                            programmingViewHolder.flameimg.get().setImageResource(R.drawable.ic_btmnav_notifications);
                        }
                        //INITIAL SETUP//


                        PushDownAnim.setPushDownAnimTo(programmingViewHolder.flameimg.get())
                                .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                                .setOnClickListener(v -> {
                                    if (currentItem.getLikeCheck() >= 0) {//was already liked by current user
                                        programmingViewHolder.flameimg.get().setImageResource(R.drawable.ic_btmnav_notifications);
                                        if (currentItem.getLikeL().size() - 1 == 0) {
                                            programmingViewHolder.flamedBy.get().setText("Not flamed yet");
                                        } else
                                            programmingViewHolder.flamedBy.get().setText("Flamed by " + (currentItem.getLikeL().size() - 1) + " people");
                                        ///////////REMOVE CURRENT USER LIKE/////////////
                                        currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                                        currentItem.setLikeCheck(-1);

                                        //                likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

                                        ///////////////////BATCH WRITE///////////////////
                                        WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                        DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                        batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                        batch.delete(flamedDoc);

                                        batch.commit().addOnSuccessListener(task -> {

                                        });
                                        ///////////////////BATCH WRITE///////////////////
                                    } else if (currentItem.getLikeCheck() < 0 && currentItem.getLikeL() != null) {
                                        Utility.vibrate(getContext());
                                        programmingViewHolder.flameimg.get().setImageResource(R.drawable.ic_flame_red);
                                        if (currentItem.getLikeL().size() == 0)
                                            programmingViewHolder.flamedBy.get().setText("Flamed by you");
                                        else if (currentItem.getLikeL().size() == 1)
                                            programmingViewHolder.flamedBy.get().setText("Flamed by you & " + currentItem.getLikeL().size() + " other");
                                        else
                                            programmingViewHolder.flamedBy.get().setText("Flamed by you & " + currentItem.getLikeL().size() + " others");

                                        //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                        currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                        currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);//For local changes

                                        ///////////////////BATCH WRITE///////////////////
                                        WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                        FlamedModel flamedModel = new FlamedModel();
                                        long tsLong = System.currentTimeMillis();

                                        flamedModel.setPostID(currentItem.getDocID());
                                        flamedModel.setTs(tsLong);
                                        flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                        flamedModel.setUserdp(DP.get());
                                        flamedModel.setUsername(USERNAME.get());
                                        flamedModel.setPostUid(currentItem.getUid());

                                        DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                        batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                        batch.set(flamedDoc, flamedModel);
                                        if (currentItem.getLikeL().size() % 5 == 0) {
                                            batch.update(likeStore, "newTs", tsLong);
                                        }
                                        batch.commit().addOnSuccessListener(task -> {

                                        });
                                        ///////////////////BATCH WRITE///////////////////
                                    } else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                                        Utility.vibrate(getContext());
                                        programmingViewHolder.flameimg.get().setImageResource(R.drawable.ic_flame_red);
                                        if (currentItem.getLikeL() != null)
                                            programmingViewHolder.flamedBy.get().setText("Flamed by you & " + (currentItem.getLikeL().size() + 1) + " people");
                                        else
                                            programmingViewHolder.flamedBy.get().setText("Flamed by you");

                                        //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                        currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                        currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);
                                        //For local changes current item like added to remote list end

                                        ///////////////////BATCH WRITE///////////////////
                                        WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                        FlamedModel flamedModel = new FlamedModel();
                                        long tsLong = System.currentTimeMillis();

                                        flamedModel.setPostID(currentItem.getDocID());
                                        flamedModel.setTs(tsLong);
                                        flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                        flamedModel.setUserdp(DP.get());
                                        flamedModel.setUsername(USERNAME.get());
                                        flamedModel.setPostUid(currentItem.getUid());

                                        DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                        batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                        batch.set(flamedDoc, flamedModel);
                                        if (currentItem.getLikeL().size() % 5 == 0) {
                                            batch.update(likeStore, "newTs", tsLong);
                                        }
                                        batch.commit().addOnSuccessListener(task -> {

                                        });
                                        ///////////////////BATCH WRITE///////////////////
                                    }
                                });


                        if (currentItem.getCmtNo() > 0) {
                            programmingViewHolder.commentimg.get().setImageResource(R.drawable.comment_yellow);
                            if (currentItem.getCmtNo() == 1)
                                programmingViewHolder.commentCount.get().setText(currentItem.getCmtNo() + " comment");
                            else if (currentItem.getCmtNo() > 1)
                                programmingViewHolder.commentCount.get().setText(currentItem.getCmtNo() + " comments");

                        } else {
                            programmingViewHolder.commentimg.get().setImageResource(R.drawable.ic_comment);
                            programmingViewHolder.commentCount.get().setText("No comments");
                        }

                        ////////POST MENU///////
                        programmingViewHolder.menuPost.get().setOnClickListener(v -> {
                            if (currentItem.getUid().matches(FirebaseAuth.getInstance().getUid())) {
//                                Utility.showToast(getContext(), String.valueOf(position));
                                postMenuDialog = new WeakReference<>(new BottomSheetDialog(getActivity()));

                                postMenuDialog.get().setContentView(R.layout.dialog_post_menu_3);
                                postMenuDialog.get().setCanceledOnTouchOutside(TRUE);

                                postMenuDialog.get().findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                                    Intent i = new Intent(getContext(), NewPostHome.class);
                                    i.putExtra("target", "100"); //target value for edit post
                                    i.putExtra("bool", "3");
                                    i.putExtra("usN", currentItem.getUsN());
                                    i.putExtra("dp", currentItem.getDp());
                                    i.putExtra("uid", currentItem.getUid());

                                    i.putExtra("img", currentItem.getImg());
                                    i.putExtra("txt", currentItem.getTxt());
                                    i.putExtra("comID", currentItem.getComID());
                                    i.putExtra("comName", currentItem.getComName());

                                    i.putExtra("ts", Long.toString(currentItem.getTs()));
                                    i.putExtra("newTs", Long.toString(currentItem.getNewTs()));

                                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                                    i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));

                                    i.putExtra("likeL", currentItem.getLikeL());
                                    i.putExtra("likeCheck", currentItem.getLikeCheck());
                                    i.putExtra("docID", currentItem.getDocID());
                                    i.putExtra("reportL", currentItem.getReportL());
                                    i.putExtra("challengeID", currentItem.getChallengeID());
                                    startActivity(i);

                                    postMenuDialog.get().dismiss();

                                });

                                postMenuDialog.get().findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Are you sure?")
                                            .setMessage("Post will be deleted permanently")
                                            .setPositiveButton("Delete", (dialog, which) -> {
                                                progressDialog = new WeakReference<>(new ProgressDialog(getActivity()));
                                                progressDialog.get().setTitle("Deleting Post");
                                                progressDialog.get().setMessage("Please wait...");
                                                progressDialog.get().setCancelable(false);
                                                progressDialog.get().show();
                                                FirebaseFirestore.getInstance()
                                                        .collection("Feeds/").document(currentItem.getDocID())
                                                        .delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            programmingViewHolder.itemHome.get().setVisibility(View.GONE);
                                                            programmingViewHolder.view1.get().setVisibility(View.GONE);
                                                            programmingViewHolder.view2.get().setVisibility(View.GONE);
                                                            notifyDataSetChanged();
                                                            FirebaseFirestore.getInstance()
                                                                    .collection("Feeds/")
                                                                    .orderBy("newTs", Query.Direction.DESCENDING)
                                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if(task.isSuccessful()) {
                                                                        if(task.getResult().size() == 0) {
                                                                            programmingViewHolder.noPost1.get().setVisibility(View.VISIBLE);
                                                                        }
                                                                        else {
                                                                            programmingViewHolder.noPost1.get().setVisibility(View.GONE);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                            progressDialog.get().dismiss();
                                                        });
                                                postMenuDialog.get().dismiss();

                                            })
                                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                            .setCancelable(true)
                                            .show();

                                });

                                postMenuDialog.get().findViewById(R.id.share_post).setOnClickListener(v1 -> {
                                    String link = "https://www.utsavapp.in/android/feeds/"+ currentItem.getDocID();
                                    Intent i = new Intent();
                                    i.setAction(Intent.ACTION_SEND);
                                    i.putExtra(Intent.EXTRA_TEXT, link);
                                    i.setType("text/plain");
                                    startActivity(Intent.createChooser(i, "Share with"));
                                    postMenuDialog.get().dismiss();

                                });

                                postMenuDialog.get().findViewById(R.id.report_post).setOnClickListener(v12 -> {
                                    FirebaseFirestore.getInstance()
                                            .collection("Feeds/").document(currentItem.getDocID())
                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                            .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                                    postMenuDialog.get().dismiss();

                                });

                                Objects.requireNonNull(postMenuDialog.get().getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                postMenuDialog.get().show();


                            } else {
                                postMenuDialog = new WeakReference<>(new BottomSheetDialog(getActivity()));

                                postMenuDialog.get().setContentView(R.layout.dialog_post_menu);
                                postMenuDialog.get().setCanceledOnTouchOutside(TRUE);

                                postMenuDialog.get().findViewById(R.id.share_post).setOnClickListener(v13 -> {
                                    String link = "https://www.utsavapp.in/android/feeds/" + currentItem.getDocID();
                                    Intent i = new Intent();
                                    i.setAction(Intent.ACTION_SEND);
                                    i.putExtra(Intent.EXTRA_TEXT, link);
                                    i.setType("text/plain");
                                    startActivity(Intent.createChooser(i, "Share with"));
                                    postMenuDialog.get().dismiss();

                                });

                                postMenuDialog.get().findViewById(R.id.report_post).setOnClickListener(v14 -> {
                                    FirebaseFirestore.getInstance()
                                            .collection("Feeds/").document(currentItem.getDocID())
                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                            .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                                    postMenuDialog.get().dismiss();

                                });
                                Objects.requireNonNull(postMenuDialog.get().getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                postMenuDialog.get().show();

                            }
                        });
                        ////////POST MENU///////
                    }
                }

            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

                if(viewType == 0) {
                    LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                    View v = layoutInflater.inflate(R.layout.item_campus_recycler, viewGroup, false);
                    return new CommunityViewHolder(v);
                }
                else {
                    LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                    View v = layoutInflater.inflate(R.layout.item_com_post, viewGroup, false);
                    return new ProgrammingViewHolder(v);
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
                    case ERROR: Utility.showToast(getActivity(), "Something went wrong..."); break;
                    case LOADING_MORE: progressMore.get().setVisibility(View.VISIBLE); break;
                    case LOADED: progressMore.get().setVisibility(View.GONE);
                        if(swipeRefreshLayout.get().isRefreshing()) {
                            swipeRefreshLayout.get().setRefreshing(false);
                        }
                        break;
                    case FINISHED: contentProgress.get().setVisibility(View.GONE);
                        progressMore.get().setVisibility(View.GONE);
                        if(swipeRefreshLayout.get().isRefreshing()) {
                            swipeRefreshLayout.get().setRefreshing(false);
                        }
                        if(adapter.getItemCount() == 0){
                            noPostView();
                        }
                        else {
                            LL.get().setVisibility(View.VISIBLE);
                            campusLL.get().setVisibility(View.GONE);
                            noPostYet1.get().setVisibility(View.GONE);
                        }
                        break;
                }
            }
        };

        contentProgress.get().setVisibility(View.GONE);
        progressMore.get().setVisibility(View.GONE);
        mRecyclerView.get().setAdapter(adapter);
    }

    private static class CommunityViewHolder extends RecyclerView.ViewHolder{

        WeakReference<TextView> view_all;
        WeakReference<ImageView> info, noPost;
        WeakReference<RecyclerView> cRecyclerView;

        //SliderView sliderView;

        WeakReference<TextView> username,commentCount, comName, text_content, flamedBy, minsago, writecomment;
        WeakReference<ImageView> userimage, postimage, flameimg, commentimg,profileimage, menuPost, share;
        WeakReference<ApplexLinkPreview> LinkPreview;
        WeakReference<LinearLayout> itemHome;
        WeakReference<RelativeLayout> first_post;
        WeakReference<RecyclerView> tagList;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            //campusName  = new WeakReference<>(itemView.findViewById(R.id.campus_name));
            info = new WeakReference<>(itemView.findViewById(R.id.info));
            view_all = new WeakReference<>(itemView.findViewById(R.id.community_view_all));
            cRecyclerView = new WeakReference<>(itemView.findViewById(R.id.communityRecycler));

            //sliderView = itemView.findViewById(R.id.imageSlider);

            tagList = new WeakReference<>(itemView.findViewById(R.id.tagsList66));
            username = new WeakReference<>(itemView.findViewById(R.id.username));
            text_content = new WeakReference<>(itemView.findViewById(R.id.text_content));
            userimage = new WeakReference<>(itemView.findViewById(R.id.user_image));
            postimage = new WeakReference<>(itemView.findViewById(R.id.post_image));
            flamedBy = new WeakReference<>(itemView.findViewById(R.id.flamed_by));
            minsago=new WeakReference<>(itemView.findViewById(R.id.mins_ago));
            flameimg = new WeakReference<>(itemView.findViewById(R.id.flame));
            comName = new WeakReference<>(itemView.findViewById(R.id.comName));
            commentimg = new WeakReference<>(itemView.findViewById(R.id.comment));
            commentCount = new WeakReference<>(itemView.findViewById(R.id.no_of_comments));
            profileimage = new WeakReference<>(itemView.findViewById(R.id.profile_image));
            menuPost = new WeakReference<>(itemView.findViewById(R.id.delete_post));
            writecomment = new WeakReference<>(itemView.findViewById(R.id.write_comment));
            itemHome = new WeakReference<>(itemView.findViewById(R.id.item_home));
            share = new WeakReference<>(itemView.findViewById(R.id.share));
            LinkPreview = new WeakReference<>(itemView.findViewById(R.id.LinkPreView));
            first_post = new WeakReference<>(itemView.findViewById(R.id.first_post));
            noPost = new WeakReference<>(itemView.findViewById(R.id.no_recent_post));
        }
    }

    private static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        WeakReference<TextView> username,commentCount, comName, text_content, flamedBy, minsago, writecomment;
        WeakReference<ImageView> userimage, postimage, flameimg, commentimg,profileimage, menuPost, share, noPost1;
        WeakReference<ApplexLinkPreview> LinkPreview;
        WeakReference<LinearLayout> itemHome;
        WeakReference<RecyclerView> tagList;
        WeakReference<View> view, view1, view2;

        ProgrammingViewHolder(@NonNull View itemView) {

            super(itemView);

            tagList = new WeakReference<>(itemView.findViewById(R.id.tagsList66));
            username = new WeakReference<>(itemView.findViewById(R.id.username));
            text_content = new WeakReference<>(itemView.findViewById(R.id.text_content));
            userimage = new WeakReference<>(itemView.findViewById(R.id.user_image));
            postimage = new WeakReference<>(itemView.findViewById(R.id.post_image));
            flamedBy = new WeakReference<>(itemView.findViewById(R.id.flamed_by));
            minsago=new WeakReference<>(itemView.findViewById(R.id.mins_ago));
            flameimg = new WeakReference<>(itemView.findViewById(R.id.flame));
            comName = new WeakReference<>(itemView.findViewById(R.id.comName));
            commentimg = new WeakReference<>(itemView.findViewById(R.id.comment));
            commentCount = new WeakReference<>(itemView.findViewById(R.id.no_of_comments));
            profileimage = new WeakReference<>(itemView.findViewById(R.id.profile_image));
            menuPost = new WeakReference<>(itemView.findViewById(R.id.delete_post));
            writecomment = new WeakReference<>(itemView.findViewById(R.id.write_comment));
            itemHome = new WeakReference<>(itemView.findViewById(R.id.item_home));
            share = new WeakReference<>(itemView.findViewById(R.id.share));
            LinkPreview = new WeakReference<>(itemView.findViewById(R.id.LinkPreView));
            view = new WeakReference<>(itemView.findViewById(R.id.view));
            view1 = new WeakReference<>(itemView.findViewById(R.id.view1));
            view2 = new WeakReference<>(itemView.findViewById(R.id.view2));

            noPost1 = new WeakReference<>(itemView.findViewById(R.id.no_recent_post1));
        }
    }

    private void save_Dialog(Bitmap bitmap) {
        Dialog myDialogue = new Dialog(getContext());
        myDialogue.setContentView(R.layout.dialog_image_options);
        myDialogue.setCanceledOnTouchOutside(TRUE);
        myDialogue.findViewById(R.id.saveToInternal).setOnClickListener(v -> {
            if(!Utility.checkStoragePermission(getContext())){
                Utility.requestStoragePermission(getContext());
            }
            else {
                boolean bool = Utility.saveImage(bitmap, getContext());
                if(bool){
                    Toast.makeText(getContext(), "Saved to device", Toast.LENGTH_SHORT).show();
                    myDialogue.dismiss();
                }
                else{
                    Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                    myDialogue.dismiss();
                }
            }
        });
        myDialogue.show();
        Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }



    private void buildCommunityRecyclerView(WeakReference<RecyclerView> cRecyclerView) {
        cRecyclerView.get().setHasFixedSize(true);
        WeakReference<LinearLayoutManager> layoutManagerCom = new WeakReference<>(new LinearLayoutManager(getActivity()));
        layoutManagerCom.get().setOrientation(LinearLayoutManager.HORIZONTAL);
        cRecyclerView.get().setLayoutManager(layoutManagerCom.get());
        cRecyclerView.get().setItemAnimator(new DefaultItemAnimator());

        ArrayList<CommunityModel> CommunityGrps = new ArrayList<>();

        CommunityModel communityModel= new CommunityModel();
        communityModel.setName("Add Community");
        communityModel.setDesc("type about your community");
        CommunityGrps.add(communityModel);

        Query query =  FirebaseFirestore.getInstance()
                .collection("Users/")
                .orderBy("random", Query.Direction.DESCENDING)
                .limit(10);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document: queryDocumentSnapshots) {
                if(document.exists()) {
                    CommunityModel communityModel1 = document.toObject(CommunityModel.class);
                    communityModel1.setDocID(document.getId());
                    CommunityGrps.add(communityModel1);
                    long pos = (long) (Math.random() * 1000000000);
                    FirebaseFirestore.getInstance().document("Home/Communities/" + document.getId())
                            .update("random", pos);
                }
            }
            if(CommunityGrps.size()>0) {
                CommunityAdapter communityAdapter= new CommunityAdapter(CommunityGrps, getActivity(), 10);
                cRecyclerView.get().setAdapter(communityAdapter);
            }

        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error Community", Toast.LENGTH_LONG).show());
    }


    private void noPostView() {
        campusLL.get().setVisibility(View.VISIBLE);
        LL.get().setVisibility(View.GONE);
        contentProgCom.get().setVisibility(View.GONE);
        noPostYet1.get().setVisibility(View.VISIBLE);

        campusNameNoPost.get().setText(CAMPUSNAME);
        view_all_NoPost.get().setOnClickListener(v -> startActivity(new Intent(getActivity(), CommitteeViewAll.class)));
        infoNoPost.get().setOnClickListener(v -> {
            dialog = new WeakReference<>(new Dialog(getActivity()));
            dialog.get().setContentView(R.layout.dialog_info_campus);
            dialog.get().show();
        });

        buildCommunityRecyclerView(comRecyclerView);
        //buildSliderView(sliderViewNoPost);

//        swipeRefreshCom.get()
//                .setColorSchemeColors(getResources().getColor(R.color.toolbarStart),getResources().getColor(R.color.md_blue_500));
//        swipeRefreshCom.get().setOnRefreshListener(() -> {
//            swipeRefreshCom.get().setRefreshing(true);
//            buildCommunityRecyclerView(comRecyclerView);
//            buildSliderView(sliderViewNoPost);
//        });

    }


//    private void newInstituteView() {
//        campusLL.get().setVisibility(View.GONE);
//        LL.get().setVisibility(View.GONE);
//        contentProgCom.get().setVisibility(View.GONE);
//        noPostYet1.get().setVisibility(View.GONE);
//
//        newInstitute.setVisibility(View.VISIBLE);
//        newInstituteName.setText("We provide the Your Campus feature to our Partner Colleges.\n\nYou have submitted your College name as ''"+ CAMPUSNAME +"''\n\nCurrently your college is not one of our Partner Colleges. For enquiry, contact us at\ncontact@campus24.in");
//
////        campusNameNoPost.get().setText(CAMPUSNAME);
////        view_all_NoPost.get().setOnClickListener(v -> startActivity(new Intent(getActivity(), CommunityViewAll.class)));
////        infoNoPost.get().setOnClickListener(v -> {
////            dialog = new WeakReference<>(new Dialog(getActivity()));
////            dialog.get().setContentView(R.layout.dialog_info_campus);
////            dialog.get().show();
////        });
////
////        buildCommunityRecyclerView(comRecyclerView);
////        buildSliderView(sliderViewNoPost);
//
//    }


    @Override
    public void onResume() {
        if(changed == 1 ) {
            buildRecyclerView();
            changed = 0;
        }
        else if(changed == 2 || comDelete == 2) {
            buildRecyclerView();
            changed = 0;
            CommitteeViewAll.changed = 0;
            comDelete = 0;
        }
        super.onResume();
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called IN FRG CMPUS!!!!!!!!!!!!!");
        System.gc();
        super.finalize();
    }


}