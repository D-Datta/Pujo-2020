package com.applex.utsav.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.ActivityProfileCommittee;
import com.applex.utsav.ActivityProfileUser;
import com.applex.utsav.CommitteeViewAll;
import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.adapters.SliderAdapter;
import com.applex.utsav.adapters.CommitteeTopAdapter;
import com.applex.utsav.adapters.TagAdapter;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.StoreTemp;
import com.applex.utsav.utility.BasicUtility;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import static java.lang.Boolean.TRUE;

public class FeedsFragment extends Fragment {

    private ProgressDialog progressDialog;
    private BottomSheetDialog postMenuDialog;
    private FloatingActionButton create_post;
    private SwipeRefreshLayout swipeRefreshLayout, swiperefreshNoPost;
    private ProgressBar progressMore, contentProgress, contentProgCom;

    private TextView view_all_NoPost;
    private RecyclerView comRecyclerView;
    private LinearLayout viewNoPost, viewPostExist;

    public static int changed = 0;
    public static int comDelete = 0;

    private RecyclerView mRecyclerView;

    private FirestorePagingAdapter adapter;
    private IntroPref introPref;

    private String DP, USERNAME, link;

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
        swiperefreshNoPost= view.findViewById(R.id.swiperefresh_no_post);

        contentProgress = view.findViewById(R.id.content_progress);
        progressMore = view.findViewById(R.id.progress_more);
//        floatingActionButton = view.findViewById(R.id.to_the_top_campus);
        create_post = view.findViewById(R.id.create_post_ind);
//        noPostYet1= view.findViewById(R.id.no_recent_com_post1);

        if(introPref.getType().matches("com")) {
            create_post.setVisibility(View.GONE);
        }
        create_post.setVisibility(View.GONE);

        //////////////RECYCLER VIEW////////////////////
        mRecyclerView = view.findViewById(R.id.recyclerCampusPost);
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(20);
        //////////////RECYCLER VIEW////////////////////

        //////////WHEN THERE ARE NO POSTS IN CAMPUS/////////
        contentProgCom = view.findViewById(R.id.content_progress_community);
        view_all_NoPost = view.findViewById(R.id.community_view_all);
        comRecyclerView = view.findViewById(R.id.communityRecyclerNoPost);
        viewNoPost = view.findViewById(R.id.view_no_post);
        //////////WHEN THERE ARE NO POSTS IN CAMPUS/////////

        viewPostExist = view.findViewById(R.id.view_post_exist);
        buildRecyclerView();

        //SWIPE REFRESH//
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources()
                        .getColor(R.color.purple));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            contentProgCom.setVisibility(View.GONE);
            buildRecyclerView();
        });

        swiperefreshNoPost.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources()
                .getColor(R.color.purple));

        swiperefreshNoPost.setOnRefreshListener(() -> {
            swiperefreshNoPost.setRefreshing(true);
            contentProgCom.setVisibility(View.GONE);
            buildRecyclerView();
        });
        //SWIPE REFRESH//

//        final int[] scrollY = {0};
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                scrollY[0] = scrollY[0] + dy;
//                if (scrollY[0] <= 2000 && dy < 0) {
//                    floatingActionButton.setVisibility(View.GONE);
//                }
//                else {
//                    if(dy < 0){
//                        floatingActionButton.setVisibility(View.VISIBLE);
//                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//                            @SuppressLint("ObjectAnimatorBinding")
//                            @Override
//                            public void onClick(View v) {
//                                recyclerView.scrollToPosition(0);
//                                recyclerView.postDelayed(new Runnable() {
//                                    public void run() {
//                                        recyclerView.scrollToPosition(0);
//                                    }
//                                },300);
//                            }
//                        });
//                    } else {
//                        floatingActionButton.setVisibility(View.GONE);
//                    }
//                }
//            }
//        });

    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds")
                .whereEqualTo("type", "indi")
                .orderBy("newTs", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setPrefetchDistance(4)
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
            @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull HomePostModel currentItem) {

//                floatingActionButton.setVisibility(View.GONE);

                FeedViewHolder feedViewHolder = (FeedViewHolder)holder;

                if(position == 0){
                    feedViewHolder.committeeHolder.setVisibility(View.VISIBLE);

                    feedViewHolder.view_all.setOnClickListener(v ->
                            startActivity(new Intent(getActivity(), CommitteeViewAll.class))
                    );

                    if(introPref.getType().matches("indi")){
                        feedViewHolder.new_post_layout.setVisibility(View.VISIBLE);

                        feedViewHolder.type_dp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(), ActivityProfileUser.class);
                                intent.putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                startActivity(intent);
                            }
                        });
                        feedViewHolder.type_something.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(InternetConnection.checkConnection(requireActivity())){
                                    Intent i= new Intent(getContext(), NewPostHome.class);
                                    i.putExtra("target", "2");
                                    startActivity(i);
                                }
                                else
                                    BasicUtility.showToast(getContext(), "Network Unavailable...");
                            }
                        });

                        feedViewHolder.newPostIconsLL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(InternetConnection.checkConnection(requireActivity())){
                                    Intent i= new Intent(getContext(), NewPostHome.class);
                                    i.putExtra("target", "2");
                                    startActivity(i);
                                }
                                else
                                    BasicUtility.showToast(getContext(), "Network Unavailable...");
                            }
                        });

                        if (DP != null) {
                            Picasso.get().load(DP).fit().centerCrop()
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(feedViewHolder.type_dp);
                        } else {
                            feedViewHolder.type_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                    else {
                        feedViewHolder.new_post_layout.setVisibility(View.GONE);
                    }


                    buildCommunityRecyclerView(feedViewHolder.cRecyclerView);
                }
                else {
                    feedViewHolder.postHolder.setVisibility(View.VISIBLE);
                    feedViewHolder.committeeHolder.setVisibility(View.GONE);
                }

                DocumentReference likeStore;
                String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                feedViewHolder.minsago.setText(timeAgo);
                if (timeAgo != null) {
                    if (timeAgo.matches("just now")) {
                        feedViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        feedViewHolder.minsago.setTextColor(Color.parseColor("#aa212121"));
                    }
                }


                if (currentItem.getPujoTag() != null) {
                    feedViewHolder.pujoTagHolder.setVisibility(View.VISIBLE);
                    feedViewHolder.pujoTagHolder.setText(currentItem.getPujoTag().getPujoName());

                    feedViewHolder.pujoTagHolder.setOnClickListener(v -> {
                        //To be changed
                        Intent intent = new Intent(getActivity(), ActivityProfileCommittee.class);
                        intent.putExtra("uid", currentItem.getPujoTag().getPujoUid());
                        startActivity(intent);
                    });
                }
                else {
                    feedViewHolder.pujoTagHolder.setVisibility(View.GONE);
                    feedViewHolder.pujoTagHolder.setText(null);
                }

                ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////

                likeStore = FirebaseFirestore.getInstance().document("Feeds/" + currentItem.getDocID() + "/");

                feedViewHolder.menuPost.setVisibility(View.VISIBLE);

                ///////////SET DOCUMENT REFERENCE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////

                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                if (DP != null) {
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
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                feedViewHolder.tagList.setNestedScrollingEnabled(true);
                feedViewHolder.tagList.setLayoutManager(linearLayoutManager);
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

                //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
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

                //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

                //////////////LOADING USERNAME AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
                if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
                    Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(feedViewHolder.userimage, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            });
                } else {
                    feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                feedViewHolder.username.setText(currentItem.getUsN());
                //////////////LOADING USERNAME AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

                //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                if (currentItem.getTxt() == null || currentItem.getTxt().isEmpty()) {
                    feedViewHolder.text_content.setVisibility(View.GONE);
                    feedViewHolder.LinkPreview.setVisibility(View.GONE);
                    feedViewHolder.text_content.setText(null);
                }
                else {
                    feedViewHolder.text_content.setVisibility(View.VISIBLE);
                    feedViewHolder.text_content.setText(currentItem.getTxt());
                    if (feedViewHolder.text_content.getUrls().length > 0) {
                        URLSpan urlSnapItem = feedViewHolder.text_content.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if (url.contains("http")) {
                            feedViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                            feedViewHolder.LinkPreview.setLink(url, new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) { }

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
                    feedViewHolder.rlLayout.setVisibility(View.VISIBLE);
                    feedViewHolder.sliderView.setVisibility(View.VISIBLE);
                    feedViewHolder.sliderView.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    feedViewHolder.sliderView.setIndicatorRadius(5);
                    feedViewHolder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    feedViewHolder.sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                    feedViewHolder.sliderView.setIndicatorSelectedColor(Color.WHITE);
                    feedViewHolder.sliderView.setIndicatorUnselectedColor(R.color.colorAccent);
                    feedViewHolder.sliderView.setAutoCycle(false);

                    SliderAdapter sliderAdapter = new SliderAdapter(getActivity(), currentItem.getImg(),currentItem);
                    feedViewHolder.sliderView.setSliderAdapter(sliderAdapter);

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
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("type", currentItem.getType());
                        startActivity(intent);
                    });
                }
                else {
                    feedViewHolder.rlLayout.setVisibility(View.GONE);
                    feedViewHolder.sliderView.setVisibility(View.GONE);
                    feedViewHolder.text_content.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreText.class);
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
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("type", currentItem.getType());
                        startActivity(intent);
//                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2);
//                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });
                }

                //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                feedViewHolder.like_layout.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", currentItem.getDocID());
                    bottomSheetDialog.show(requireActivity().getSupportFragmentManager(), "FlamedBySheet");
                });

                ///////////////////FLAMES AND COMMENTS///////////////////////

                //INITIAL SETUP//
                if (currentItem.getLikeL() != null) {
                    if (currentItem.getLikeL().size() == 0) {
                        feedViewHolder.like_layout.setVisibility(View.GONE);
                    }
                    else {
                        feedViewHolder.like_layout.setVisibility(View.VISIBLE);
                        feedViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size()));
                    }

                    for (int j = 0; j < currentItem.getLikeL().size(); j++) {
                        if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                            feedViewHolder.like.setImageResource(R.drawable.ic_flame_red);
                            currentItem.setLikeCheck(j);
//                            if ((currentItem.getLikeL().size() - 1) == 1)
//                                feedViewHolder.flamedBy.setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " other");
//                            else if ((currentItem.getLikeL().size() - 1) == 0) {
//                                feedViewHolder.flamedBy.setText("Flamed by you");
//                            } else
//                                feedViewHolder.flamedBy.setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " others");
                            //Position in likeList where the current USer UId is found stored in likeCheck
                        }
                    }

                } else {
                    feedViewHolder.like_layout.setVisibility(View.GONE);
                }
                //INITIAL SETUP//

                PushDownAnim.setPushDownAnimTo(feedViewHolder.like)
                        .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                        .setOnClickListener(v -> {
                            if (currentItem.getLikeCheck() >= 0) {
                                feedViewHolder.like.setImageResource(R.drawable.ic_btmnav_notifications);//was already liked by current user
                                if (currentItem.getLikeL().size() - 1 == 0) {
                                    feedViewHolder.like_layout.setVisibility(View.GONE);
                                } else{
                                    feedViewHolder.like_layout.setVisibility(View.VISIBLE);
                                    feedViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() - 1));
                                }
                                ///////////REMOVE CURRENT USER LIKE/////////////
                                currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                                currentItem.setLikeCheck(-1);

                                ///////////////////BATCH WRITE///////////////////
                                WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                DocumentReference flamedDoc = likeStore.collection("flameL")
                                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                                batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                batch.delete(flamedDoc);

                                batch.commit().addOnSuccessListener(task -> { });
                                ///////////////////BATCH WRITE///////////////////
                            }
                            else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                                BasicUtility.vibrate(requireActivity());
                                feedViewHolder.dhak_anim.setVisibility(View.VISIBLE);
                                feedViewHolder.dhak_anim.playAnimation();
                                try {
                                    AssetFileDescriptor afd =requireActivity().getAssets().openFd("dhak.mp3");
                                    MediaPlayer player = new MediaPlayer();
                                    player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                                    player.prepare();
                                    AudioManager audioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
                                    if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                        player.start();
                                        if(!player.isPlaying()) {
                                            feedViewHolder.dhak_anim.cancelAnimation();
                                            feedViewHolder.dhak_anim.setVisibility(View.GONE);
                                        }
                                        player.setOnCompletionListener(mediaPlayer -> {
                                            feedViewHolder.dhak_anim.cancelAnimation();
                                            feedViewHolder.dhak_anim.setVisibility(View.GONE);
                                        });
                                    } else {
                                        new Handler().postDelayed(() -> {
                                            feedViewHolder.dhak_anim.cancelAnimation();
                                            feedViewHolder.dhak_anim.setVisibility(View.GONE);
                                        }, 2000);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                feedViewHolder.like.setImageResource(R.drawable.ic_flame_red);
                                feedViewHolder.like_layout.setVisibility(View.VISIBLE);
                                if (currentItem.getLikeL() != null){
                                    feedViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() + 1));
                                }
                                else{
                                    feedViewHolder.likesCount.setText("1");
                                }

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

                                DocumentReference flamedDoc = likeStore.collection("flameL")
                                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                                batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                batch.set(flamedDoc, flamedModel);
                                if (currentItem.getLikeL().size() % 5 == 0) {
                                    batch.update(likeStore, "newTs", tsLong);
                                }
                                batch.commit().addOnSuccessListener(task -> { });
                                ///////////////////BATCH WRITE///////////////////
                            }
                        });

                feedViewHolder.commentimg.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 1,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                });

                feedViewHolder.writecomment.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 1,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                });

                feedViewHolder.share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(currentItem.getImg() != null && currentItem.getImg().size()>0)
                            link = "https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
                        else
                            link = "https://www.applex.in/utsav-app/feeds/" + "0/" + currentItem.getDocID();
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_SEND);
                        i.putExtra(Intent.EXTRA_TEXT, link);
                        i.setType("text/plain");
                        startActivity(Intent.createChooser(i, "Share with"));
                    }
                });

                if (currentItem.getCmtNo() > 0) {
                    FeedViewHolder.comment_layout.setVisibility(View.VISIBLE);
                    FeedViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

                    if(currentItem.getCom1() != null && !currentItem.getCom1().isEmpty()) {
                        feedViewHolder.commentLayout1.setVisibility(View.VISIBLE);
                        feedViewHolder.name_cmnt1.setText(currentItem.getCom1_usn());
                        Picasso.get().load(currentItem.getCom1_dp())
                                .placeholder(R.drawable.ic_account_circle_black_24dp)
                                .into(feedViewHolder.dp_cmnt1);

                        feedViewHolder.cmnt1.setText(currentItem.getCom1());
                        if (feedViewHolder.cmnt1.getUrls().length > 0) {
                            URLSpan urlSnapItem = feedViewHolder.cmnt1.getUrls()[0];
                            String url = urlSnapItem.getURL();
                            if (url.contains("http")) {
                                feedViewHolder.link_preview1.setVisibility(View.VISIBLE);
                                feedViewHolder.link_preview1.setLink(url, new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean status) {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            //do stuff like remove view etc
                                            feedViewHolder.link_preview1.setVisibility(View.GONE);
                                        });
                                    }
                                });
                            }
                        } else {
                            feedViewHolder.link_preview1.setVisibility(View.GONE);
                        }

                        feedViewHolder.cmnt1_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom1_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom1_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom1_ts())).matches("just now")) {
                                feedViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                feedViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                    } else {
                        feedViewHolder.commentLayout1.setVisibility(View.GONE);
                    }

                    if(currentItem.getCom2() != null && !currentItem.getCom2().isEmpty()) {
                        feedViewHolder.commentLayout2.setVisibility(View.VISIBLE);
                        feedViewHolder.name_cmnt2.setText(currentItem.getCom2_usn());
                        Picasso.get().load(currentItem.getCom2_dp())
                                .placeholder(R.drawable.ic_account_circle_black_24dp)
                                .into(feedViewHolder.dp_cmnt2);

                        feedViewHolder.cmnt2.setText(currentItem.getCom2());
                        if (feedViewHolder.cmnt2.getUrls().length > 0) {
                            URLSpan urlSnapItem = feedViewHolder.cmnt2.getUrls()[0];
                            String url = urlSnapItem.getURL();
                            if (url.contains("http")) {
                                feedViewHolder.link_preview1.setVisibility(View.VISIBLE);
                                feedViewHolder.link_preview1.setLink(url, new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean status) { }

                                    @Override
                                    public void onError(Exception e) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            //do stuff like remove view etc
                                            feedViewHolder.link_preview1.setVisibility(View.GONE);
                                        });
                                    }
                                });
                            }
                        } else {
                            feedViewHolder.link_preview1.setVisibility(View.GONE);
                        }

                        feedViewHolder.cmnt2_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom2_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom2_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom2_ts())).matches("just now")) {
                                feedViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                feedViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                    } else {
                        feedViewHolder.commentLayout2.setVisibility(View.GONE);
                    }

                    FeedViewHolder.comment_layout.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    feedViewHolder.commentLayout1.setOnClickListener(v-> {
                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    feedViewHolder.commentLayout2.setOnClickListener(v-> {
                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });
                }
                else {
                    FeedViewHolder.comment_layout.setVisibility(View.GONE);
                    feedViewHolder.commentLayout1.setVisibility(View.GONE);
                    feedViewHolder.commentLayout2.setVisibility(View.GONE);
                }
                ///////////////////FLAMES AND COMMENTS///////////////////////

                ////////POST MENU///////
                feedViewHolder.menuPost.setOnClickListener(v -> {
                    if (currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);
                        postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);
//                        postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
//                            Intent i = new Intent(getActivity(), NewPostHome.class);
//                            i.putExtra("target", "100"); //target value for edit post
//                            i.putExtra("bool", "3");
//                            i.putExtra("usN", currentItem.getUsN());
//                            i.putExtra("dp", currentItem.getDp());
//                            i.putExtra("uid", currentItem.getUid());
//                            i.putExtra("type", currentItem.getType());
//                            if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
//                                Bundle args = new Bundle();
//                                args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
//                                i.putExtra("BUNDLE", args);
//                            }
//                            i.putExtra("txt", currentItem.getTxt());
//                            i.putExtra("comID", currentItem.getComID());
//                            i.putExtra("comName", currentItem.getComName());
//                            i.putExtra("ts", Long.toString(currentItem.getTs()));
//                            i.putExtra("newTs", Long.toString(currentItem.getNewTs()));
//                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
//                            i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));
//                            i.putExtra("likeL", currentItem.getLikeL());
//                            i.putExtra("likeCheck", currentItem.getLikeCheck());
//                            i.putExtra("docID", currentItem.getDocID());
//                            i.putExtra("reportL", currentItem.getReportL());
//                            i.putExtra("challengeID", currentItem.getChallengeID());
//                            startActivity(i);
//                            postMenuDialog.dismiss();
//
//                        });

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

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Feeds/").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();
                        });
                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    }
                    else {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Feeds/").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();

                        });
                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    }
                });
                ////////POST MENU//////
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
                    case ERROR: BasicUtility.showToast(getActivity(), "Something went wrong...");
                        break;
                    case LOADING_MORE: progressMore.setVisibility(View.VISIBLE);
                        break;
                    case LOADED: progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        viewNoPost.setVisibility(View.GONE);
                        viewPostExist.setVisibility(View.VISIBLE);
                        break;
                    case FINISHED: contentProgress.setVisibility(View.GONE);
                        progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(swiperefreshNoPost.isRefreshing()) {
                            swiperefreshNoPost.setRefreshing(false);
                        }
                        if(adapter.getItemCount() == 0){
                            noPostView();
                        }
                        else {
                            viewNoPost.setVisibility(View.GONE);
                            viewPostExist.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }
        };

        contentProgress.setVisibility(View.GONE);
        progressMore.setVisibility(View.GONE);
        mRecyclerView.setAdapter(adapter);
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder{

        TextView view_all;
        ImageView noPost;
        RecyclerView cRecyclerView;

        TextView pujoTagHolder;

        @SuppressLint("StaticFieldLeak")
        public static TextView commentCount;
        public static LinearLayout comment_layout;

        TextView username, likesCount, text_content, minsago, writecomment;
        ImageView userimage, like, commentimg,profileimage, menuPost, share, like_image, comment_image;
        ImageView dp_cmnt1, dp_cmnt2, type_dp;
        TextView cmnt1, cmnt2, cmnt1_minsago, cmnt2_minsago, name_cmnt1, name_cmnt2, type_something;
        SliderView sliderView;
        ApplexLinkPreview LinkPreview;
        LinearLayout itemHome, new_post_layout, newPostIconsLL;
        RelativeLayout first_post,rlLayout;
        RecyclerView tagList;
        com.applex.utsav.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;

        LinearLayout postHolder, like_layout, commentLayout1, commentLayout2;
        LinearLayout committeeHolder;
        LottieAnimationView dhak_anim;


        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            view_all = itemView.findViewById(R.id.community_view_all);
            cRecyclerView = itemView.findViewById(R.id.communityRecycler);

            tagList = itemView.findViewById(R.id.tagsList);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage = itemView.findViewById(R.id.user_image);
            sliderView = itemView.findViewById(R.id.post_image);
            minsago = itemView.findViewById(R.id.mins_ago);
            like = itemView.findViewById(R.id.like);
            commentimg = itemView.findViewById(R.id.comment);
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
            type_dp = itemView.findViewById(R.id.Pdp);
            type_something = itemView.findViewById(R.id.type_smthng);
            new_post_layout = itemView.findViewById(R.id.type_something);
            newPostIconsLL = itemView.findViewById(R.id.post_icons_ll);

            like_image = itemView.findViewById(R.id.like_image);
            comment_image = itemView.findViewById(R.id.comment_image);
            likesCount = itemView.findViewById(R.id.no_of_likes);
            commentCount = itemView.findViewById(R.id.no_of_comments);
            like_layout = itemView.findViewById(R.id.like_layout);
            comment_layout = itemView.findViewById(R.id.comment_layout);

            commentLayout1 = itemView.findViewById(R.id.comment_layout1);
            name_cmnt1 = itemView.findViewById(R.id.comment_username1);
            cmnt1 = itemView.findViewById(R.id.comment1);
            cmnt1_minsago = itemView.findViewById(R.id.comment_mins_ago1);
            dp_cmnt1 = itemView.findViewById(R.id.comment_user_dp1);
            link_preview1 = itemView.findViewById(R.id.LinkPreViewComment1);

            commentLayout2 = itemView.findViewById(R.id.comment_layout2);
            name_cmnt2 = itemView.findViewById(R.id.comment_username2);
            cmnt2 = itemView.findViewById(R.id.comment2);
            cmnt2_minsago = itemView.findViewById(R.id.comment_mins_ago2);
            dp_cmnt2 = itemView.findViewById(R.id.comment_user_dp2);
            link_preview2 = itemView.findViewById(R.id.LinkPreViewComment2);

            pujoTagHolder = itemView.findViewById(R.id.tag_pujo);
            dhak_anim = itemView.findViewById(R.id.dhak_anim);
            rlLayout = itemView.findViewById(R.id.rlLayout);

        }
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
                .orderBy("pujoVisits", Query.Direction.DESCENDING)
                .limit(15);

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
                CommitteeTopAdapter communityAdapter= new CommitteeTopAdapter(committees, getActivity(), 0);
                cRecyclerView.setAdapter(communityAdapter);
            }

        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error Community", Toast.LENGTH_LONG).show());
    }


    private void noPostView() {
        viewNoPost.setVisibility(View.VISIBLE);
        viewPostExist.setVisibility(View.GONE);
        contentProgCom.setVisibility(View.GONE);

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

    @Override
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called IN FRG CMPUS!!!!!!!!!!!!!");
        System.gc();
        super.finalize();
    }
}