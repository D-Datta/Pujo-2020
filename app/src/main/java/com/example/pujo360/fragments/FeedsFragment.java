package com.example.pujo360.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pujo360.ActivityProfileCommittee;
import com.example.pujo360.ActivityProfileUser;
import com.example.pujo360.CommitteeViewAll;
import com.example.pujo360.LinkPreview.ApplexLinkPreview;
import com.example.pujo360.LinkPreview.ViewListener;
import com.example.pujo360.NewPostHome;
import com.example.pujo360.R;
import com.example.pujo360.ViewMoreHome;
import com.example.pujo360.adapters.SliderAdapter;
import com.example.pujo360.adapters.CommitteeTopAdapter;
import com.example.pujo360.adapters.TagAdapter;
import com.example.pujo360.models.BaseUserModel;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.models.HomePostModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.InternetConnection;
import com.example.pujo360.util.StoreTemp;
import com.example.pujo360.util.Utility;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
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
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class FeedsFragment extends Fragment {

    private Dialog dialog;
    private ImageView noPostYet1;
    private ProgressDialog progressDialog;
    private BottomSheetDialog postMenuDialog;
    private FloatingActionButton create_post, floatingActionButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressMore, contentProgress, contentProgCom;

    private TextView view_all_NoPost;
    private RecyclerView comRecyclerView;
    private LinearLayout campusLL, viewPostExist;

    public static int changed = 0;
    public static int comDelete = 0;

    private RecyclerView mRecyclerView;

    private FirestorePagingAdapter adapter;
    private IntroPref introPref;

    private String DP, USERNAME;



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
        DP = introPref.getUserdp();
        USERNAME = introPref.getFullName();

        changed = 0;

        swipeRefreshLayout= view.findViewById(R.id.swiperefresh);
        contentProgress = view.findViewById(R.id.content_progress);
        progressMore = view.findViewById(R.id.progress_more);
        floatingActionButton = view.findViewById(R.id.to_the_top_campus);
        create_post = view.findViewById(R.id.create_post_ind);
        noPostYet1= view.findViewById(R.id.no_recent_com_post1);

        if(introPref.getType().matches("com")) {
            create_post.setVisibility(View.GONE);
        }

        //////////////RECYCLER VIEW////////////////////
        mRecyclerView = view.findViewById(R.id.recyclerCampusPost);
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(10);
        //////////////RECYCLER VIEW////////////////////


        //////////WHEN THERE ARE NO POSTS IN CAMPUS/////////
        contentProgCom = view.findViewById(R.id.content_progress_community);
        view_all_NoPost = view.findViewById(R.id.community_view_all);
        comRecyclerView = view.findViewById(R.id.communityRecyclerNoPost);
        campusLL = view.findViewById(R.id.view_no_post);
        viewPostExist = view.findViewById(R.id.view_post_exist);
        //////////WHEN THERE ARE NO POSTS IN CAMPUS/////////

        create_post.setOnClickListener(v -> {
            if(InternetConnection.checkConnection(getActivity())){
                Intent i= new Intent(getContext(), NewPostHome.class);
                i.putExtra("target", "2");
                startActivity(i);
            }
            else
                Utility.showToast(getContext(), "Network Unavailable...");
        });


        buildRecyclerView();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.toolbarStart),getResources()
                        .getColor(R.color.md_blue_500));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            contentProgCom.setVisibility(View.GONE);
            buildRecyclerView();
        });

        final int[] scrollY = {0};
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollY[0] = scrollY[0] + dy;
                if (scrollY[0] <= 2000 && dy < 0) {
                    floatingActionButton.setVisibility(View.GONE);
                }
                else {
                    if(dy < 0){
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
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
                        floatingActionButton.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds")
                .whereEqualTo("type", "indi")
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

                floatingActionButton.setVisibility(View.GONE);

                FeedViewHolder feedViewHolder = (FeedViewHolder)holder;

                if(position == 0){
                    feedViewHolder.committeeHolder.setVisibility(View.VISIBLE);

                    feedViewHolder.view_all.setOnClickListener(v ->
                            startActivity(new Intent(getActivity(), CommitteeViewAll.class))
                    );

                    buildCommunityRecyclerView(feedViewHolder.cRecyclerView);
                }
                else {
                    feedViewHolder.postHolder.setVisibility(View.VISIBLE);
                    feedViewHolder.committeeHolder.setVisibility(View.GONE);
                }


                DocumentReference likeStore;
                String timeAgo = Utility.getTimeAgo(currentItem.getTs());
                feedViewHolder.minsago.setText(timeAgo);
                if (timeAgo != null) {
                    if (timeAgo.matches("just now")) {
                        feedViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        feedViewHolder.minsago.setTextColor(Color.parseColor("#aa212121"));
                    }
                }


                if (currentItem.getComName() != null) {
                    feedViewHolder.comName.setVisibility(View.VISIBLE);
                    feedViewHolder.comName.setText(currentItem.getComName());
                    feedViewHolder.comName.setBackground(getResources().getDrawable(R.drawable.custom_com_backgnd));

                    feedViewHolder.comName.setOnClickListener(v -> {
                        //To be changed
                        Intent intent = new Intent(getActivity(), ActivityProfileCommittee.class);
                        intent.putExtra("comID", currentItem.getComID());
                        startActivity(intent);
                    });
                }
                else {
                    feedViewHolder.comName.setVisibility(View.GONE);
                    feedViewHolder.comName.setText(null);
                }

                ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////

                likeStore = FirebaseFirestore.getInstance().document("Feeds/" + currentItem.getDocID() + "/");

                feedViewHolder.menuPost.setVisibility(View.VISIBLE);

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
                        Picasso.get().load(DP).fit().centerCrop()
                                .placeholder(R.drawable.ic_account_circle_black_24dp)
                                .into(feedViewHolder.profileimage);

                } else {
                    feedViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                ///////////TAGLIST///////////////

                ///////////TAG RECYCLER SETUP////////////////
                feedViewHolder.tagList.setHasFixedSize(false);
                WeakReference<LinearLayoutManager> linearLayoutManager = new WeakReference<>(new LinearLayoutManager(getContext()));
                linearLayoutManager.get().setOrientation(LinearLayoutManager.HORIZONTAL);
                feedViewHolder.tagList.setNestedScrollingEnabled(true);
                feedViewHolder.tagList.setLayoutManager(linearLayoutManager.get());
                ///////////TAG RECYCLER SETUP////////////////

                if (currentItem.getTagL() != null && currentItem.getTagL().size() > 0) {
                    feedViewHolder.tagList.setVisibility(View.VISIBLE);
                    TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL(), getActivity());
                    feedViewHolder.tagList.setAdapter(tagAdapter);
                } else {
                    feedViewHolder.tagList.setAdapter(null);
                    feedViewHolder.tagList.setVisibility(View.GONE);
                }
                /////////TAGLIST///////////////

                //////////////LOADING USERNAME AND USERDP FROM USERNODE FOR CURRENT POST USER///////////////
                if (currentItem.getUsN() != null) {

                    feedViewHolder.userimage.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfileUser.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

                    feedViewHolder.username.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfileUser.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

                    feedViewHolder.username.setText(currentItem.getUsN());

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
                                    .into(feedViewHolder.userimage, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    });
//                                }
                    }
                    else {
                        feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }

                }


                ///////////////OPEN VIEW MORE//////////////
                feedViewHolder.itemHome.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                    intent.putExtra("username", currentItem.getUsN());
                    intent.putExtra("userdp", currentItem.getDp());
                    intent.putExtra("docID", currentItem.getDocID());
                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                    intent.putExtra("comName", currentItem.getComName());
                    intent.putExtra("comID", currentItem.getComID());

                    intent.putExtra("likeL", currentItem.getLikeL());
                    if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                        Bundle args = new Bundle();
                        args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
                        intent.putExtra("BUNDLE", args);
                    }
                    intent.putExtra("postText", currentItem.getTxt());
                    intent.putExtra("bool", "3");
                    intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));

                    intent.putExtra("uid", currentItem.getUid());
                    intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                    intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                    startActivity(intent);
                });

                feedViewHolder.text_content.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                    intent.putExtra("username", currentItem.getUsN());
                    intent.putExtra("userdp", currentItem.getDp());
                    intent.putExtra("docID", currentItem.getDocID());
                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                    intent.putExtra("comName", currentItem.getComName());
                    intent.putExtra("comID", currentItem.getComID());

                    intent.putExtra("likeL", currentItem.getLikeL());
                    if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                        Bundle args = new Bundle();
                        args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
                        intent.putExtra("BUNDLE", args);
                    }
                    intent.putExtra("postText", currentItem.getTxt());
                    intent.putExtra("bool", "3");
                    intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));

                    intent.putExtra("uid", currentItem.getUid());
                    intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                    intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                    startActivity(intent);
                });

                feedViewHolder.sliderView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                    intent.putExtra("username", currentItem.getUsN());
                    intent.putExtra("userdp", currentItem.getDp());
                    intent.putExtra("docID", currentItem.getDocID());
                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                    //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());
                    intent.putExtra("comName", currentItem.getComName());
                    intent.putExtra("comID", currentItem.getComID());
                    intent.putExtra("likeL", currentItem.getLikeL());
                    if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                        Bundle args = new Bundle();
                        args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
                        intent.putExtra("BUNDLE", args);
                    }
                    intent.putExtra("postText", currentItem.getTxt());
                    intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                    intent.putExtra("bool", "3");


                    intent.putExtra("uid", currentItem.getUid());
                    intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                    intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                    startActivity(intent);
                });

                feedViewHolder.flamedBy.setOnClickListener(v -> {
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
                    if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                        Bundle args = new Bundle();
                        args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
                        intent.putExtra("BUNDLE", args);
                    }
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
                    feedViewHolder.text_content.setVisibility(View.GONE);
                    feedViewHolder.LinkPreview.setVisibility(View.GONE);
                    feedViewHolder.text_content.setText(null);
                } else {
                    feedViewHolder.text_content.setVisibility(View.VISIBLE);
                    feedViewHolder.text_content.setText(currentItem.getTxt());
                    if (feedViewHolder.text_content.getUrls().length > 0) {
                        URLSpan urlSnapItem = feedViewHolder.text_content.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if (url.contains("http")) {
                            feedViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                            feedViewHolder.LinkPreview.setLink(url, new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) {
                                }

                                @Override
                                public void onError(Exception e) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //do stuff like remove view etc
                                            feedViewHolder.LinkPreview.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });
                        }

                    } else {
                        feedViewHolder.LinkPreview.setVisibility(View.GONE);
                    }

                }

                if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                    feedViewHolder.sliderView.setVisibility(View.VISIBLE);
                    feedViewHolder.sliderView.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    feedViewHolder.sliderView.setIndicatorRadius(8);
                    feedViewHolder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    feedViewHolder.sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                    feedViewHolder.sliderView.setIndicatorSelectedColor(Color.WHITE);
                    feedViewHolder.sliderView.setIndicatorUnselectedColor(R.color.colorAccent);
                    feedViewHolder.sliderView.setAutoCycle(false);

                    SliderAdapter sliderAdapter = new SliderAdapter(getActivity(), currentItem.getImg());

                    feedViewHolder.sliderView.setSliderAdapter(sliderAdapter);
                }
                else {
                    feedViewHolder.sliderView.setVisibility(View.GONE);
                }

                //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                ///////////////////FLAMES///////////////////////

                //INITIAL SETUP//
                if (currentItem.getLikeL() != null) {
                    /////////////////UPDATNG FLAMED BY NO.//////////////////////
                    if (currentItem.getLikeL().size() == 0) {
                        feedViewHolder.flamedBy.setText("Not flamed yet");
                    } else if (currentItem.getLikeL().size() == 1)
                        feedViewHolder.flamedBy.setText("Flamed by 1");
                    else {
                        feedViewHolder.flamedBy.setText("Flamed by " + currentItem.getLikeL().size() + " people");
                    }

                    for (int j = 0; j < currentItem.getLikeL().size(); j++) {
                        if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                            feedViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);
                            currentItem.setLikeCheck(j);
                            if ((currentItem.getLikeL().size() - 1) == 1)
                                feedViewHolder.flamedBy.setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " other");
                            else if ((currentItem.getLikeL().size() - 1) == 0) {
                                feedViewHolder.flamedBy.setText("Flamed by you");
                            } else
                                feedViewHolder.flamedBy.setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " others");
                            //Position in likeList where the current USer UId is found stored in likeCheck
                        }
                    }
                } else {
                    feedViewHolder.flamedBy.setText("Not flamed yet");
                    feedViewHolder.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                }
                //INITIAL SETUP//


                PushDownAnim.setPushDownAnimTo(feedViewHolder.flameimg)
                        .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                        .setOnClickListener(v -> {
                            if (currentItem.getLikeCheck() >= 0) {//was already liked by current user
                                feedViewHolder.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                                if (currentItem.getLikeL().size() - 1 == 0) {
                                    feedViewHolder.flamedBy.setText("Not flamed yet");
                                } else
                                    feedViewHolder.flamedBy.setText("Flamed by " + (currentItem.getLikeL().size() - 1) + " people");
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
                                feedViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);
                                if (currentItem.getLikeL().size() == 0)
                                    feedViewHolder.flamedBy.setText("Flamed by you");
                                else if (currentItem.getLikeL().size() == 1)
                                    feedViewHolder.flamedBy.setText("Flamed by you & " + currentItem.getLikeL().size() + " other");
                                else
                                    feedViewHolder.flamedBy.setText("Flamed by you & " + currentItem.getLikeL().size() + " others");

                                //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);//For local changes

                                ///////////////////BATCH WRITE///////////////////
                                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                FlamedModel flamedModel = new FlamedModel();
                                long tsLong = System.currentTimeMillis();

                                flamedModel.setPostID(currentItem.getDocID());
                                flamedModel.setTs(tsLong);
                                flamedModel.setType(introPref.getType());
                                flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                flamedModel.setUserdp(DP);
                                flamedModel.setUsername(USERNAME);
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
                                feedViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);
                                if (currentItem.getLikeL() != null)
                                    feedViewHolder.flamedBy.setText("Flamed by you & " + (currentItem.getLikeL().size() + 1) + " people");
                                else
                                    feedViewHolder.flamedBy.setText("Flamed by you");

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
                                flamedModel.setType(introPref.getType());
                                flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                flamedModel.setUserdp(DP);
                                flamedModel.setUsername(USERNAME);
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
                    feedViewHolder.commentimg.setImageResource(R.drawable.comment_yellow);
                    if (currentItem.getCmtNo() == 1)
                        feedViewHolder.commentCount.setText(currentItem.getCmtNo() + " comment");
                    else if (currentItem.getCmtNo() > 1)
                        feedViewHolder.commentCount.setText(currentItem.getCmtNo() + " comments");

                } else {
                    feedViewHolder.commentimg.setImageResource(R.drawable.ic_comment);
                    feedViewHolder.commentCount.setText("No comments");
                }


                ////////POST MENU///////
                feedViewHolder.menuPost.setOnClickListener(v -> {
                    if (currentItem.getUid().matches(FirebaseAuth.getInstance().getUid())) {
                        postMenuDialog = new BottomSheetDialog(getActivity());

                        postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                            Intent i = new Intent(getActivity(), NewPostHome.class);
                            i.putExtra("target", "100"); //target value for edit post
                            i.putExtra("bool", "3");
                            i.putExtra("usN", currentItem.getUsN());
                            i.putExtra("dp", currentItem.getDp());
                            i.putExtra("uid", currentItem.getUid());
                            i.putExtra("type", currentItem.getType());

                            if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                                Bundle args = new Bundle();
                                args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
                                i.putExtra("BUNDLE", args);
                            }
                            i.putExtra("txt", currentItem.getTxt());
                            i.putExtra("comID", currentItem.getComID());
                            i.putExtra("comName", currentItem.getComName());
                            i.putExtra("type", currentItem.getType());

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

                            postMenuDialog.dismiss();

                        });

                        postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Are you sure?")
                                    .setMessage("Post will be deleted permanently")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        progressDialog = new ProgressDialog(getActivity());
                                        progressDialog.setTitle("Deleting Post");
                                        progressDialog.setMessage("Please wait...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        FirebaseFirestore.getInstance()
                                                .collection("Feeds/").document(currentItem
                                                .getDocID()).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    feedViewHolder.first_post.setVisibility(View.GONE);
                                                    progressDialog.dismiss();
                                                    FirebaseFirestore.getInstance()
                                                            .collection("Feeds/")
                                                            .orderBy("newTs", Query.Direction.DESCENDING)
                                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if(task.isSuccessful()) {
                                                                if(task.getResult().size() == 0) {
                                                                    feedViewHolder.noPost.setVisibility(View.VISIBLE);
                                                                }
                                                                else {
                                                                    feedViewHolder.noPost.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        }
                                                    });
                                                });
                                        postMenuDialog.dismiss();

                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();
                        });

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v1 -> {
                            String link = "https://www.utsavapp.in/android/feeds/"+ currentItem.getDocID();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();

                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Feeds/").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();

                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();

                    }
                    else {
                        postMenuDialog = new BottomSheetDialog(getActivity());

                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v13 -> {
                            String link = "https://www.utsavapp.in/android/feeds/"+ currentItem.getDocID();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();

                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Feeds/").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();

                        });
                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();

                    }
                });
                ////////POST MENU///////


            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_feeds_post, viewGroup, false);
                return new FeedViewHolder(v);
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR: Utility.showToast(getActivity(), "Something went wrong...");
                        break;
                    case LOADING_MORE: progressMore.setVisibility(View.VISIBLE);
                        break;
                    case LOADED: progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        break;
                    case FINISHED: contentProgress.setVisibility(View.GONE);
                        progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(adapter.getItemCount() == 0){
                            noPostView();
                        }
                        else {
                            viewPostExist.setVisibility(View.VISIBLE);
                            campusLL.setVisibility(View.GONE);
                            noPostYet1.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        };

        contentProgress.setVisibility(View.GONE);
        progressMore.setVisibility(View.GONE);
        mRecyclerView.setAdapter(adapter);
    }

    private static class FeedViewHolder extends RecyclerView.ViewHolder{

        TextView view_all;
        ImageView noPost;
        RecyclerView cRecyclerView;

        TextView username,commentCount, comName, text_content, flamedBy, minsago, writecomment;
        ImageView userimage, flameimg, commentimg,profileimage, menuPost, share;
        SliderView sliderView;
        ApplexLinkPreview LinkPreview;
        LinearLayout itemHome;
        RelativeLayout first_post;
        RecyclerView tagList;

        LinearLayout postHolder;
        LinearLayout committeeHolder;


        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            view_all = itemView.findViewById(R.id.community_view_all);
            cRecyclerView = itemView.findViewById(R.id.communityRecycler);

            //sliderView = itemView.findViewById(R.id.imageSlider);

            tagList = itemView.findViewById(R.id.tagsList66);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage = itemView.findViewById(R.id.user_image);
            sliderView = itemView.findViewById(R.id.post_image);
            flamedBy = itemView.findViewById(R.id.flamed_by);
            minsago = itemView.findViewById(R.id.mins_ago);
            flameimg = itemView.findViewById(R.id.flame);
            comName = itemView.findViewById(R.id.comName);
            commentimg = itemView.findViewById(R.id.comment);
            commentCount = itemView.findViewById(R.id.no_of_comments);
            profileimage = itemView.findViewById(R.id.profile_image);
            menuPost = itemView.findViewById(R.id.delete_post);
            writecomment = itemView.findViewById(R.id.write_comment);
            itemHome = itemView.findViewById(R.id.item_home);
            share = itemView.findViewById(R.id.share);
            LinkPreview = itemView.findViewById(R.id.LinkPreView);
            first_post = itemView.findViewById(R.id.first_post);
            noPost = itemView.findViewById(R.id.no_recent_post);


            postHolder = itemView.findViewById(R.id.post);
            committeeHolder = itemView.findViewById(R.id.header_committee);

        }
    }

    private void save_Dialog(Bitmap bitmap) {
        Dialog myDialogue = new Dialog(getActivity());
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



    private void buildCommunityRecyclerView(RecyclerView cRecyclerView) {
        cRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManagerCom = new LinearLayoutManager(getActivity());
        layoutManagerCom.setOrientation(LinearLayoutManager.HORIZONTAL);
        cRecyclerView.setLayoutManager(layoutManagerCom);
        cRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ArrayList<BaseUserModel> committees = new ArrayList<>();

        Query query =  FirebaseFirestore.getInstance()
                .collection("Users")
                .whereEqualTo("type", "com")
//                .orderBy("random", Query.Direction.DESCENDING)
                .limit(10);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document: queryDocumentSnapshots) {
                if(document.exists()) {
                    BaseUserModel communityModel1 = document.toObject(BaseUserModel.class);
                    committees.add(communityModel1);
//                    long pos = (long) (Math.random() * 1000000000);
//                    FirebaseFirestore.getInstance().document("Home/Communities/" + document.getId())
//                            .update("random", pos);
                }
            }
            if(committees.size()>0) {
                CommitteeTopAdapter communityAdapter= new CommitteeTopAdapter(committees, getActivity(), 10);
                cRecyclerView.setAdapter(communityAdapter);
            }

        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error Community", Toast.LENGTH_LONG).show());
    }


    private void noPostView() {
        campusLL.setVisibility(View.VISIBLE);
        viewPostExist.setVisibility(View.GONE);
        contentProgCom.setVisibility(View.GONE);
        noPostYet1.setVisibility(View.VISIBLE);

        view_all_NoPost.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CommitteeViewAll.class))
        );

        buildCommunityRecyclerView(comRecyclerView);

    }




    @Override
    public void onResume() {
        if(changed == 1 ) {
            buildRecyclerView();
            changed = 0;
        }
        else if(changed == 2 || comDelete == 2) {
            buildRecyclerView();
            changed = 0;
            comDelete = 0;
        }
        super.onResume();
    }


}