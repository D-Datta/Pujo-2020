package com.applex.utsav.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
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
import android.widget.VideoView;
import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.ActivityProfile;
import com.applex.utsav.ActivityProfileCommittee;
import com.applex.utsav.ActivityProfileUser;
import com.applex.utsav.HashtagPostViewAll;
import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.MainActivity;
import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.adapters.SliderAdapter;
import com.applex.utsav.adapters.CommitteeTopAdapter;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.StoreTemp;
import com.applex.utsav.utility.BasicUtility;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import com.squareup.picasso.Target;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

public class FeedsFragment extends Fragment {

    private ProgressDialog progressDialog;
    private BottomSheetDialog postMenuDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressMore;
    private ShimmerFrameLayout shimmerFrameLayout;

    public static int changed = 0;
    public static int comDelete = 0;

    private RecyclerView mRecyclerView;
    private FirestorePagingAdapter adapter;
    private FirestorePagingAdapter reelsAdapter;
    private IntroPref introPref;
    private Query reels_query;
    private ArrayList<Integer> positions;
    private DocumentSnapshot lastReelDocument;

    private String DP, USERNAME, link, GENDER;
    private FloatingActionButton floatingActionButton;

    public FeedsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feeds, container, false);
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        introPref = new IntroPref(getActivity());
        DP = introPref.getUserdp();
        USERNAME = introPref.getFullName();
        GENDER = introPref.getGender();

        changed = 0;

        swipeRefreshLayout= view.findViewById(R.id.swiperefresh);
        progressMore = view.findViewById(R.id.progress_more);
        floatingActionButton = view.findViewById(R.id.to_the_top_people);

        //////////////RECYCLER VIEW////////////////////
        mRecyclerView = view.findViewById(R.id.recyclerCampusPost);
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(20);
        //////////////RECYCLER VIEW////////////////////

        positions = new ArrayList<>();
        mRecyclerView.setVisibility(View.GONE);

        //SWIPE REFRESH//
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.darkpurple),
                getResources().getColor(R.color.darkpurple));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            mRecyclerView.setVisibility(View.GONE);
            positions = new ArrayList<>();
            adapter.refresh();
        });
        //SWIPE REFRESH//

        buildRecyclerView();
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
                .setEnablePlaceholders(true)
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
                FeedViewHolder feedViewHolder = (FeedViewHolder)holder;

                if(position == 0){
                    feedViewHolder.committeeHolder.setVisibility(View.VISIBLE);
                    feedViewHolder.reels_item.setVisibility(View.GONE);
                    feedViewHolder.view_all.setOnClickListener(v -> MainActivity.viewPager.setCurrentItem(3, true));

                    if(introPref.getType().matches("indi")){
                        feedViewHolder.new_post_layout.setVisibility(View.VISIBLE);

                        feedViewHolder.type_dp.setOnClickListener(view -> {
                            Intent intent = new Intent(getContext(), ActivityProfile.class);
                            intent.putExtra("uid", FirebaseAuth.getInstance().getUid());
                            startActivity(intent);
                        });

                        feedViewHolder.type_something.setOnClickListener(view -> {
                            if(InternetConnection.checkConnection(requireActivity())){
                                Intent i= new Intent(getContext(), NewPostHome.class);
                                i.putExtra("target", "2");
                                startActivity(i);
                            }
                            else
                                BasicUtility.showToast(getContext(), "Network Unavailable...");
                        });

                        feedViewHolder.newPostIconsLL.setOnClickListener(view -> {
                            if(InternetConnection.checkConnection(requireActivity())){
                                Intent i= new Intent(getContext(), NewPostHome.class);
                                i.putExtra("target", "2");
                                startActivity(i);
                            }
                            else
                                BasicUtility.showToast(getContext(), "Network Unavailable...");
                        });

                        if (DP != null) {
                            Picasso.get().load(DP).fit().centerCrop()
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(feedViewHolder.type_dp);
                        }
                        else{
                            if(GENDER!=null){
                                if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                                    feedViewHolder.type_dp.setImageResource(R.drawable.ic_female);
                                }
                                else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                                    feedViewHolder.type_dp.setImageResource(R.drawable.ic_male);
                                }
                                else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                                    feedViewHolder.type_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            }
                            else{
                                feedViewHolder.type_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }
                    }
                    else {
                        feedViewHolder.new_post_layout.setVisibility(View.GONE);
                    }
                    buildCommunityRecyclerView(feedViewHolder.cRecyclerView);
                }

                else if (feedViewHolder.getItemViewType() % 4  == 0) {
                    feedViewHolder.committeeHolder.setVisibility(View.GONE);
                    feedViewHolder.reels_item.setVisibility(View.VISIBLE);

                    if (feedViewHolder.getItemViewType() != 4 && lastReelDocument != null) {
                        reels_query = FirebaseFirestore.getInstance()
                                .collection("Reels")
                                .whereEqualTo("type", "indi")
                                .orderBy("ts", Query.Direction.DESCENDING)
                                .startAfter(lastReelDocument);

                        buildReelsRecyclerView(position, feedViewHolder);

                        feedViewHolder.viewallReels.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                            intent.putExtra("bool", "1");
                            intent.putExtra("from", "indi");
                            requireActivity().startActivity(intent);
                        });
                    } else {
                        reels_query = FirebaseFirestore.getInstance()
                                .collection("Reels")
                                .whereEqualTo("type", "indi")
                                .orderBy("ts", Query.Direction.DESCENDING);

                        buildReelsRecyclerView(position, feedViewHolder);

                        feedViewHolder.viewallReels.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                            intent.putExtra("bool", "1");
                            intent.putExtra("from", "indi");
                            requireActivity().startActivity(intent);
                        });
                    }
                }
                else {
                    feedViewHolder.reels_item.setVisibility(View.GONE);
                    feedViewHolder.committeeHolder.setVisibility(View.GONE);
                }

                DocumentReference likeStore;
                String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                feedViewHolder.minsago.setText(timeAgo);
                if (timeAgo != null) {
                    if (timeAgo.matches("just now")) {
                        feedViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        feedViewHolder.minsago.setTextColor(getResources().getColor(R.color.grey_868686));
                    }
                }

                if (currentItem.getPujoTag() != null) {
                    feedViewHolder.pujoTagHolder.setVisibility(View.VISIBLE);
                    feedViewHolder.pujoTagHolder.setText(currentItem.getPujoTag().getPujoName());

//                    feedViewHolder.pujoTagHolder.setOnClickListener(v -> {
//                        //To be changed
//                        Intent intent = new Intent(getActivity(), ActivityProfileCommittee.class);
//                        intent.putExtra("uid", currentItem.getPujoTag().getPujoUid());
//                        startActivity(intent);
//                    });
                    feedViewHolder.pujoTagHolder.setOnClickListener(v -> {
                        //To be changed
                        Intent intent = new Intent(getActivity(), ActivityProfile.class);
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
                }
                else{
                    if(GENDER!=null){
                        if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                            feedViewHolder.profileimage.setImageResource(R.drawable.ic_female);
                        }
                        else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                            feedViewHolder.profileimage.setImageResource(R.drawable.ic_male);
                        }
                        else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                            feedViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                    else{
                        feedViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                }

                feedViewHolder.userimage.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), ActivityProfile.class);
                    intent.putExtra("uid", currentItem.getUid());
                    startActivity(intent);
                });

                feedViewHolder.username.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), ActivityProfile.class);
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
                                if(currentItem.getGender() != null) {
                                    if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                                        feedViewHolder.userimage.setImageResource(R.drawable.ic_female);
                                    }
                                    else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                                        feedViewHolder.userimage.setImageResource(R.drawable.ic_male);
                                    }
                                    else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                                        feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                } else {
                                    feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            }
                        });
                } else {
                    if(currentItem.getGender() != null) {
                        if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                            feedViewHolder.userimage.setImageResource(R.drawable.ic_female);
                        }
                        else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                            feedViewHolder.userimage.setImageResource(R.drawable.ic_male);
                        }
                        else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                            feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    } else {
                        feedViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
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

                    //TAGS COLOURED DISPLAY
                    Pattern p = Pattern.compile("[#][a-zA-Z0-9-_]+");
                    Matcher m = p.matcher(feedViewHolder.text_content.getText().toString());

                    SpannableString ss = new SpannableString(feedViewHolder.text_content.getText().toString());

                    while(m.find()) // loops through all the words in the text which matches the pattern
                    {
                        final int s = m.start(); // add 1 to omit the "@" tag
                        final int e = m.end();

                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView)
                            {
                                Intent i = new Intent(getContext(), HashtagPostViewAll.class);
                                i.putExtra("hashtag", feedViewHolder.text_content.getText().toString().substring(s+1, e));
                                startActivity(i);
//                                Toast.makeText(getActivity(), programmingViewHolder.text_content.getText().toString().substring(s+1, e), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setColor(getResources().getColor(R.color.md_blue_500));
                                ds.setUnderlineText(false);
                            }
                        };

                        ss.setSpan(new ForegroundColorSpan(Color.BLUE), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ss.setSpan(clickableSpan, s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    feedViewHolder.text_content.setText(ss);
                    feedViewHolder.text_content.setMovementMethod(LinkMovementMethod.getInstance());
                    feedViewHolder.text_content.setHighlightColor(Color.TRANSPARENT);
                    //TAGS COLOURED DISPLAY

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

                    }
                    else {
                        feedViewHolder.LinkPreview.setVisibility(View.GONE);
                    }
                }

                if(currentItem.getImg() != null && currentItem.getImg().size()>0) {

                    feedViewHolder.rlLayout.setVisibility(View.VISIBLE);

                    if(currentItem.getChallengeID()!=null && !currentItem.getChallengeID().isEmpty() && currentItem.getChallengeID().matches("PictureUpdate")){
                        feedViewHolder.picupdate.setVisibility(View.VISIBLE);
                        feedViewHolder.picupdate.setCardBackgroundColor(getResources().getColor(R.color.reels_white));
                        feedViewHolder.sliderView.setVisibility(View.GONE);
                        Picasso.get().load(currentItem.getImg().get(0)).into(feedViewHolder.profilepicpost);

                        if(currentItem.getHeadline()!=null && !currentItem.getHeadline().isEmpty()){
                            feedViewHolder.head_content.setVisibility(View.VISIBLE);
                            feedViewHolder.head_content.setText(currentItem.getHeadline());
                        }

                        feedViewHolder.head_content.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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
                                intent.putExtra("gender",currentItem.getGender());
                                intent.putExtra("headline",currentItem.getHeadline());
                                intent.putExtra("challengeID",currentItem.getChallengeID());
                                startActivity(intent);
                            }
                        });

                        feedViewHolder.profilepicpost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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
                                intent.putExtra("gender",currentItem.getGender());
                                intent.putExtra("headline",currentItem.getHeadline());
                                intent.putExtra("challengeID",currentItem.getChallengeID());
                                startActivity(intent);
                            }
                        });
                    }
                    else{
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

                        if(currentItem.getChallengeID()!=null && !currentItem.getChallengeID().isEmpty() && currentItem.getChallengeID().matches("CoverUpdate")){

                            if(currentItem.getHeadline()!=null && !currentItem.getHeadline().isEmpty()){
                                feedViewHolder.head_content.setVisibility(View.VISIBLE);
                                feedViewHolder.head_content.setText(currentItem.getHeadline());
                            }

                            feedViewHolder.head_content.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
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
                                    intent.putExtra("gender",currentItem.getGender());
                                    intent.putExtra("headline",currentItem.getHeadline());
                                    intent.putExtra("challengeID",currentItem.getChallengeID());
                                    startActivity(intent);
                                }
                            });
                        }
                    }

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
                        intent.putExtra("gender",currentItem.getGender());
                        intent.putExtra("headline",currentItem.getHeadline());
                        intent.putExtra("challengeID",currentItem.getChallengeID());
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
                        intent.putExtra("gender",currentItem.getGender());
                        intent.putExtra("headline",currentItem.getHeadline());
                        intent.putExtra("challengeID",currentItem.getChallengeID());
                        startActivity(intent);
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
                                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                        player.start();
                                    }
                                    new Handler().postDelayed(() -> {
                                        feedViewHolder.dhak_anim.cancelAnimation();
                                        feedViewHolder.dhak_anim.setVisibility(View.GONE);
                                    }, player.getDuration());
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
                                flamedModel.setGender(introPref.getGender());

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
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 1,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                });

                feedViewHolder.writecomment.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 1,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                });

//                feedViewHolder.share.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if(currentItem.getImg() != null && currentItem.getImg().size()>0)
//                            link = "https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
//                        else
//                            link = "https://www.applex.in/utsav-app/feeds/" + "0/" + currentItem.getDocID();
//                        Intent i = new Intent();
//                        i.setAction(Intent.ACTION_SEND);
//                        i.putExtra(Intent.EXTRA_TEXT, link+ getResources().getString(R.string.link_suffix));
//                        i.setType("text/plain");
//                        startActivity(Intent.createChooser(i, "Share with"));
//                    }
//                });

                 ////////////////////////////////////////SHARE////////////////////////////////////////
                if(currentItem.getImg()==null){
                    feedViewHolder.share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(currentItem.getHeadline() != null && currentItem.getTxt() == null) {
                                String link = "\n\nPost Link - https://www.applex.in/utsav-app/feeds/" + "0/" + currentItem.getDocID();
                                String playstore = getResources().getString(R.string.download_utsav);
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getHeadline()+link+playstore);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(shareIntent,"Share Using"));
                            }
                            else if(currentItem.getHeadline() == null && currentItem.getTxt() != null) {
                                String link = "\n\nPost Link - https://www.applex.in/utsav-app/feeds/" + "0/" + currentItem.getDocID();
                                String playstore = getResources().getString(R.string.download_utsav);
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getTxt()+link+playstore);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(shareIntent,"Share Using"));
                            }
                            else if(currentItem.getHeadline() != null && currentItem.getHeadline() != null) {
                                String link = "\n\nPost Link - https://www.applex.in/utsav-app/feeds/" + "0/" + currentItem.getDocID();
                                String playstore = getResources().getString(R.string.download_utsav);
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getHeadline() + "\n\n" + currentItem.getTxt()+link+playstore);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(shareIntent,"Share Using"));
                            }
                        }
                    });
                }
                else if(currentItem.getImg()!=null && currentItem.getImg().size()>0){
                    feedViewHolder.share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String path = currentItem.getImg().get(0);
                            Picasso.get().load(path).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    if(BasicUtility.checkStoragePermission(requireActivity())) {
                                        if(currentItem.getTxt() == null && currentItem.getHeadline() == null) {
                                            String finalbitmap = MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(),
                                                    bitmap, String.valueOf(System.currentTimeMillis()), null);
                                            Uri uri =  Uri.parse(finalbitmap);
                                            String link = "Post Link - https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
                                            String playstore = getResources().getString(R.string.download_utsav);
                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                            shareIntent.setType("*/*");
                                            shareIntent.putExtra(Intent.EXTRA_TEXT,link+playstore);
                                            shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                                            startActivity(Intent.createChooser(shareIntent,"Share Using"));
                                        }
                                        else if(currentItem.getTxt() != null && currentItem.getHeadline() == null) {
                                            String finalbitmap = MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(),
                                                    bitmap, String.valueOf(System.currentTimeMillis()), null);
                                            Uri uri =  Uri.parse(finalbitmap);
                                            String link = "\n\nPost Link - https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
                                            String playstore = getResources().getString(R.string.download_utsav);
                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                            shareIntent.setType("*/*");
                                            shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getTxt()+link+playstore);
                                            shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                                            startActivity(Intent.createChooser(shareIntent,"Share Using"));
                                        }
                                        else if(currentItem.getTxt() == null && currentItem.getHeadline() != null) {
                                            String finalbitmap = MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(),
                                                    bitmap, String.valueOf(System.currentTimeMillis()), null);
                                            Uri uri =  Uri.parse(finalbitmap);
                                            String link = "\n\nPost Link - https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
                                            String playstore = getResources().getString(R.string.download_utsav);
                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                            shareIntent.setType("*/*");
                                            shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getHeadline()+link+playstore);
                                            shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                                            startActivity(Intent.createChooser(shareIntent,"Share Using"));
                                        }
                                        else if(currentItem.getTxt() != null && currentItem.getHeadline() != null) {
                                            String finalbitmap = MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(),
                                                    bitmap, String.valueOf(System.currentTimeMillis()), null);
                                            Uri uri =  Uri.parse(finalbitmap);
                                            String link = "\n\nPost Link - https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
                                            String playstore = getResources().getString(R.string.download_utsav);
                                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                            shareIntent.setType("*/*");
                                            shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getHeadline()+"\n\n"+currentItem.getTxt()+link+playstore);
                                            shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
                                            startActivity(Intent.createChooser(shareIntent,"Share Using"));
                                        }
                                    }
                                    else {
                                        BasicUtility.requestStoragePermission(requireActivity());
                                    }
                                }
                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                }
                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                        }
                    });
                }
                ////////////////////////////////////////SHARE////////////////////////////////////////

                if (currentItem.getCmtNo() > 0) {
                    FeedViewHolder.comment_layout.setVisibility(View.VISIBLE);
                    FeedViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

                    if(currentItem.getCom1() != null && !currentItem.getCom1().isEmpty()) {

                        feedViewHolder.commentLayout1.setVisibility(View.VISIBLE);

                        feedViewHolder.name_cmnt1.setText(currentItem.getCom1_usn());

                        if(currentItem.getCom1_dp()!=null && !currentItem.getCom1_dp().isEmpty()){
                            Picasso.get().load(currentItem.getCom1_dp())
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(feedViewHolder.dp_cmnt1);

                        }
                        else{
                            if(currentItem.getCom1_gender()!=null){
                                if (currentItem.getCom1_gender().matches("Female") || currentItem.getCom1_gender().matches("মহিলা")){
                                    feedViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_female);
                                }
                                else if (currentItem.getCom1_gender().matches("Male") || currentItem.getCom1_gender().matches("পুরুষ")){
                                    feedViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_male);
                                }
                                else if (currentItem.getCom1_gender().matches("Others") || currentItem.getCom1_gender().matches("অন্যান্য")){
                                    feedViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            }
                            else {
                                feedViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }

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
                                feedViewHolder.cmnt1_minsago.setTextColor(getResources().getColor(R.color.grey_868686));
                            }
                        }
                    } else {
                        feedViewHolder.commentLayout1.setVisibility(View.GONE);
                    }

                    if(currentItem.getCom2() != null && !currentItem.getCom2().isEmpty()) {

                        feedViewHolder.commentLayout2.setVisibility(View.VISIBLE);

                        feedViewHolder.name_cmnt2.setText(currentItem.getCom2_usn());

                        if(currentItem.getCom2_dp()!=null && !currentItem.getCom2_dp().isEmpty()){
                            Picasso.get().load(currentItem.getCom2_dp())
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(feedViewHolder.dp_cmnt2);
                        }
                        else{
                            if(currentItem.getCom2_gender()!=null){
                                if (currentItem.getCom2_gender().matches("Female") || currentItem.getCom2_gender().matches("মহিলা")){
                                    feedViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_female);
                                }
                                else if (currentItem.getCom2_gender().matches("Male") || currentItem.getCom2_gender().matches("পুরুষ")){
                                    feedViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_male);
                                }
                                else if (currentItem.getCom2_gender().matches("Others") || currentItem.getCom2_gender().matches("অন্যান্য")){
                                    feedViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            }
                            else {
                                feedViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }

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
                                feedViewHolder.cmnt2_minsago.setTextColor(getResources().getColor(R.color.grey_868686));
                            }
                        }
                    } else {
                        feedViewHolder.commentLayout2.setVisibility(View.GONE);
                    }

                    FeedViewHolder.comment_layout.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    feedViewHolder.commentLayout1.setOnClickListener(v-> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    feedViewHolder.commentLayout2.setOnClickListener(v-> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"FeedsFragment", null,currentItem.getCmtNo(), null, null);
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
//                        postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);
                        postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                            Intent i = new Intent(getActivity(), NewPostHome.class);
                            i.putExtra("target", "100"); //target value for edit post
                            i.putExtra("bool", "2");
                            i.putExtra("typeofpost", "notreel");
                            i.putExtra("txt", currentItem.getTxt());
                            i.putExtra("headline", currentItem.getHeadline());
                            if(currentItem.getTagList() != null && currentItem.getTagList().size()>0) {
                                Bundle args = new Bundle();
                                args.putSerializable("ARRAYLISTTAGS", currentItem.getTagList());
                                i.putExtra("BUNDLETAGS", args);
                            }
                            i.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setPujoTagModel(currentItem.getPujoTag());
                            i.putExtra("challengeID",currentItem.getChallengeID());
                            startActivity(i);
                            postMenuDialog.dismiss();

                        });

                        if(currentItem.getType()!=null && !currentItem.getType().isEmpty() && currentItem.getType().matches("com")
                                && currentItem.getChallengeID()!=null && !currentItem.getChallengeID().isEmpty()
                                && (currentItem.getChallengeID().matches("PictureUpdate") || currentItem.getChallengeID().matches("CoverUpdate"))){
                            postMenuDialog.findViewById(R.id.delete_post).setVisibility(View.GONE);
                        }


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

                                    if(currentItem.getChallengeID()!=null && !currentItem.getChallengeID().isEmpty()
                                            && currentItem.getChallengeID().matches("PictureUpdate")){
                                        FirebaseFirestore.getInstance().collection("Users")
                                                .document(currentItem.getUid())
                                                .update("dp",null,"dpcaption",null,"dppostid",null,"isdpshared",false)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
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
                                                    }
                                                });
                                    }
                                    else if(currentItem.getChallengeID()!=null && !currentItem.getChallengeID().isEmpty()
                                            && currentItem.getChallengeID().matches("CoverUpdate")){
                                        FirebaseFirestore.getInstance().collection("Users")
                                                .document(currentItem.getUid())
                                                .update("coverpic",null,"covercaption",null,"coverpostid",null,"iscovershared",false)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
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
                                                    }
                                                });
                                    }
                                    else{
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
                                    }
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
                    case LOADED:
                        progressMore.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        mRecyclerView.setVisibility(View.VISIBLE);
                        break;
                    case FINISHED:
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        mRecyclerView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };

        progressMore.setVisibility(View.GONE);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        final int[] scrollY = {0};

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
                    int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

                    if (firstVisiblePosition >= 0) {
                        Rect rect_parent = new Rect();
                        mRecyclerView.getGlobalVisibleRect(rect_parent);

                        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                            if(positions != null && positions.contains(i)) {

                                final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                                FeedViewHolder cvh = (FeedViewHolder) holder;

                                int[] location = new int[2];
                                Objects.requireNonNull(cvh).reels_item.getLocationOnScreen(location);
                                Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.reels_item.getWidth(), location[1] + cvh.reels_item.getHeight());

                                float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                                float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                                float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                                float overlapArea = x_overlap * y_overlap;
                                float percent = (overlapArea / rect_parent_area) * 100.0f;

                                if (percent >= 90) {
                                    RecyclerView.LayoutManager manager1 = Objects.requireNonNull(cvh).reelsList.getLayoutManager();

                                    int firstVisiblePosition1 = ((LinearLayoutManager) Objects.requireNonNull(manager1)).findFirstVisibleItemPosition();
                                    int lastVisiblePosition1 = ((LinearLayoutManager) manager1).findLastVisibleItemPosition();

                                    if (firstVisiblePosition1 >= 0) {
                                        Rect rect_parent1 = new Rect();
                                        cvh.reelsList.getGlobalVisibleRect(rect_parent1);

                                        for (int j = firstVisiblePosition1; j <= lastVisiblePosition1; j++) {
                                            final RecyclerView.ViewHolder holder2 = cvh.reelsList.findViewHolderForAdapterPosition(j);
                                            ReelsItemViewHolder cvh1 = (ReelsItemViewHolder) holder2;

                                            int[] location1 = new int[2];

                                            Objects.requireNonNull(cvh1).item_reels_video.getLocationOnScreen(location1);
                                            Rect rect_child1 = new Rect(location1[0], location1[1], location1[0] + cvh1.item_reels_video.getWidth(), location1[1] + cvh1.item_reels_video.getHeight());

                                            float rect_parent_area1 = (rect_child1.right - rect_child1.left) * (rect_child1.bottom - rect_child1.top);
                                            float x_overlap1 = Math.max(0, Math.min(rect_child1.right, rect_parent1.right) - Math.max(rect_child1.left, rect_parent1.left));
                                            float y_overlap1 = Math.max(0, Math.min(rect_child1.bottom, rect_parent1.bottom) - Math.max(rect_child1.top, rect_parent1.top));
                                            float overlapArea1 = x_overlap1 * y_overlap1;
                                            float percent1 = (overlapArea1 / rect_parent_area1) * 100.0f;

                                            if (percent1 >= 90) {
                                                cvh1.item_reels_video.start();
                                                cvh1.item_reels_video.setOnPreparedListener(mp -> {
                                                    requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    new Handler().postDelayed(() -> cvh1.item_reels_image.setVisibility(View.GONE), 500);
                                                    mp.setVolume(0f, 0f);
                                                    mp.setLooping(true);
                                                });
                                            } else {
                                                cvh1.item_reels_video.seekTo(1);
                                                cvh1.item_reels_video.pause();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                        floatingActionButton.setOnClickListener(v -> {
//                            recyclerView.scrollToPosition(0);
//                            recyclerView.postDelayed(() -> recyclerView.scrollToPosition(0),300);
                            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(requireActivity()) {
                                @Override
                                protected int getVerticalSnapPreference() {
                                    return LinearSmoothScroller.SNAP_TO_START;
                                }
                            };
                            smoothScroller.setTargetPosition(0);
                            Objects.requireNonNull(recyclerView.getLayoutManager()).startSmoothScroll(smoothScroller);
                        });
                    } else {
                        floatingActionButton.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder{

        TextView view_all;
        ImageView noPost;
        RecyclerView cRecyclerView;

        TextView pujoTagHolder;

        @SuppressLint("StaticFieldLeak")
        public static TextView commentCount;
        @SuppressLint("StaticFieldLeak")
        public static LinearLayout comment_layout;

        TextView username, likesCount, text_content, minsago, writecomment;
        ImageView userimage, like, commentimg,profileimage, menuPost, share, like_image, comment_image;
        ImageView dp_cmnt1, dp_cmnt2, type_dp;
        TextView cmnt1, cmnt2, cmnt1_minsago, cmnt2_minsago, name_cmnt1, name_cmnt2, type_something, viewallReels;
        SliderView sliderView;
        ApplexLinkPreview LinkPreview;
        LinearLayout itemHome, new_post_layout, newPostIconsLL, reels_item;
        RelativeLayout first_post,rlLayout;
        RecyclerView tagList;
        com.applex.utsav.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;

        LinearLayout postHolder, like_layout, commentLayout1, commentLayout2;
        LinearLayout committeeHolder;
        LottieAnimationView dhak_anim;
        RecyclerView reelsList;

        CardView picupdate;
        ImageView profilepicpost;
        TextView head_content;


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

            reelsList = itemView.findViewById(R.id.reelsRecycler);
            viewallReels = itemView.findViewById(R.id.view_all_reels);
            reels_item = itemView.findViewById(R.id.reels_item);

            picupdate = itemView.findViewById(R.id.picupdate);
            profilepicpost = itemView.findViewById(R.id.profilepicpost);
            head_content = itemView.findViewById(R.id.head_content);
        }
    }

    private static class ReelsItemViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout item_reels;
        VideoView item_reels_video;
        TextView video_time, reels_mins_ago;
        ImageView pujo_com_dp, reels_more, item_reels_image;
        TextView pujo_com_name;

        ReelsItemViewHolder(View itemView) {
            super(itemView);

            item_reels = itemView.findViewById(R.id.item_reels);
            item_reels_video = itemView.findViewById(R.id.item_reels_video);
            video_time = itemView.findViewById(R.id.video_time);
            pujo_com_dp = itemView.findViewById(R.id.pujo_com_dp);
            pujo_com_name = itemView.findViewById(R.id.pujo_com_name);
            reels_more =  itemView.findViewById(R.id.reels_more);
            item_reels_image = itemView.findViewById(R.id.item_reels_image);
            reels_mins_ago = itemView.findViewById(R.id.reels_mins_ago);
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
                .orderBy("lastVisitTime", Query.Direction.DESCENDING)
                .limit(20);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document: queryDocumentSnapshots) {
                if(document.exists()) {
                    BaseUserModel communityModel1 = document.toObject(BaseUserModel.class);
                    committees.add(communityModel1);
                }
            }
            if(committees.size()>0) {
                CommitteeTopAdapter communityAdapter= new CommitteeTopAdapter(committees, getActivity(), 0);
                cRecyclerView.setAdapter(communityAdapter);
            }

        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error Community", Toast.LENGTH_LONG).show());
    }

    private void buildReelsRecyclerView(int position, FeedViewHolder pvh) {
        if(pvh != null) {

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            pvh.reelsList.setHasFixedSize(true);
            pvh.reelsList.setLayoutManager(layoutManager);
            pvh.reelsList.setNestedScrollingEnabled(true);
            pvh.reelsList.setItemViewCacheSize(10);
            pvh.reelsList.setDrawingCacheEnabled(true);

            SnapHelper snapHelper = new PagerSnapHelper();
            pvh.reelsList.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(pvh.reelsList);

            PagedList.Config config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(10)
                    .setPageSize(1)
                    .setPrefetchDistance(0)
                    .setEnablePlaceholders(true)
                    .build();

            FirestorePagingOptions<ReelsPostModel> options = new FirestorePagingOptions.Builder<ReelsPostModel>()
                    .setLifecycleOwner(this)
                    .setQuery(reels_query, config, snapshot -> {
                        ReelsPostModel reelsPostModel = new ReelsPostModel();
                        if(snapshot.exists()) {
                            reelsPostModel = snapshot.toObject(ReelsPostModel.class);
                            Objects.requireNonNull(reelsPostModel).setDocID(snapshot.getId());
                            lastReelDocument = snapshot;
                        }
                        return reelsPostModel;
                    })
                    .build();

            reelsAdapter = new FirestorePagingAdapter<ReelsPostModel, ReelsItemViewHolder>(options) {
                @SuppressLint("SetTextI18n")
                @Override
                protected void onBindViewHolder(@NonNull ReelsItemViewHolder holder, int position, @NonNull ReelsPostModel currentItem) {
                    String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                    holder.reels_mins_ago.setText(timeAgo);
                    if (timeAgo != null) {
                        if (timeAgo.matches("just now")) {
                            holder.reels_mins_ago.setTextColor(Color.parseColor("#00C853"));
                        } else {
                            holder.reels_mins_ago.setTextColor(getResources().getColor(R.color.reels_white));
                        }
                    }

                    holder.item_reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
                    holder.item_reels_video.start();

                    Picasso.get().load(currentItem.getFrame()).fit().into(holder.item_reels_image);

                    holder.item_reels_video.setOnPreparedListener(mp -> {
                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        new Handler().postDelayed(() -> holder.item_reels_image.setVisibility(View.GONE), 500);
                        if(position == 1) {
                            holder.item_reels_video.seekTo(1);
                            holder.item_reels_video.pause();
                        }
                        mp.setVolume(0f, 0f);
                        mp.setLooping(true);
                    });

                    holder.video_time.setText(currentItem.getDuration());
                    if(currentItem.getCommittee_name().length() > 15) {
                        holder.pujo_com_name.setText(currentItem.getCommittee_name().substring(0, 15) + "...");
                    } else {
                        holder.pujo_com_name.setText(currentItem.getCommittee_name());
                    }

                    if(holder.item_reels_video.getVisibility() == View.VISIBLE) {
                        holder.item_reels_video.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                            intent.putExtra("bool", "1");
                            intent.putExtra("docID", currentItem.getDocID());
                            intent.putExtra("from", "indi");
                            requireActivity().startActivity(intent);
                        });
                    }
                    else if(holder.item_reels_image.getVisibility() == View.VISIBLE) {
                        holder.item_reels_image.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                            intent.putExtra("bool", "1");
                            intent.putExtra("from", "indi");
                            intent.putExtra("docID", currentItem.getDocID());
                            requireActivity().startActivity(intent);
                        });
                    }

                    if (currentItem.getCommittee_dp() != null && !currentItem.getCommittee_dp().isEmpty()) {
                        Picasso.get().load(currentItem.getCommittee_dp()).fit().centerCrop()
                                .placeholder(R.drawable.ic_account_circle_black_24dp)
                                .into(holder.pujo_com_dp, new Callback() {
                                    @Override
                                    public void onSuccess() { }

                                    @Override
                                    public void onError(Exception e) {
                                        if(currentItem.getGender()!=null){
                                            if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                                                holder.pujo_com_dp.setImageResource(R.drawable.ic_female);
                                            }
                                            else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                                                holder.pujo_com_dp.setImageResource(R.drawable.ic_male);
                                            }
                                            else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                                                holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                            }
                                        }
                                        else {
                                            holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    }
                                });
                    }
                    else {
                        if(currentItem.getGender()!=null){
                            if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                                holder.pujo_com_dp.setImageResource(R.drawable.ic_female);
                            }
                            else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                                holder.pujo_com_dp.setImageResource(R.drawable.ic_male);
                            }
                            else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                                holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }
                        else {
                            holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }

                    holder.pujo_com_dp.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfile.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

                    holder.pujo_com_name.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfile.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

                    holder.reels_more.setOnClickListener(v -> {
                        if (currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                            postMenuDialog = new BottomSheetDialog(requireActivity());
                            postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                            postMenuDialog.setCanceledOnTouchOutside(TRUE);
//                            postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);
                            postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                                Intent i = new Intent(getActivity(), NewPostHome.class);
                                i.putExtra("target", "100"); //target value for edit post
                                i.putExtra("bool", "2");
                                i.putExtra("typeofpost", "notreel");
                                i.putExtra("txt", currentItem.getDescription());
                                i.putExtra("headline", currentItem.getHeadline());
                                if(currentItem.getTagList() != null && currentItem.getTagList().size()>0) {
                                    Bundle args = new Bundle();
                                    args.putSerializable("ARRAYLISTTAGS", currentItem.getTagList());
                                    i.putExtra("BUNDLETAGS", args);
                                }
                                i.putExtra("docID", currentItem.getDocID());
                                StoreTemp.getInstance().setPujoTagModel(currentItem.getPujoTag());
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
                                startActivity(i);
                                postMenuDialog.dismiss();

                            });

                            postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Are you sure?")
                                        .setMessage("Reel will be deleted permanently")
                                        .setPositiveButton("Delete", (dialog, which) -> {
                                            progressDialog = new ProgressDialog(requireActivity());
                                            progressDialog.setTitle("Deleting Reel");
                                            progressDialog.setMessage("Please wait...");
                                            progressDialog.setCancelable(false);
                                            progressDialog.show();
                                            FirebaseFirestore.getInstance()
                                                    .collection("Reels").document(currentItem.getDocID()).delete()
                                                    .addOnSuccessListener(aVoid -> {
//                                                        ActivityProfileUser.delete = 1;
                                                        ActivityProfile.delete = 1;
                                                        holder.itemView.setVisibility(View.GONE);
                                                        progressDialog.dismiss();
                                                        if(getItemCount() == 0) {
                                                            pvh.reels_item.setVisibility(View.GONE);
                                                        }
                                                    });
                                            postMenuDialog.dismiss();
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                        .setCancelable(true)
                                        .show();
                            });

                            postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v12 -> {
                                link = "Post Link - https://www.applex.in/utsav-app/clips/" + "1/" + currentItem.getDocID();
                                String playstore = "\nCheck out the short video."+getResources().getString(R.string.download_utsav);
                                Intent i = new Intent();
                                i.setAction(Intent.ACTION_SEND);
                                i.putExtra(Intent.EXTRA_TEXT, link+playstore);
                                i.setType("text/plain");
                                startActivity(Intent.createChooser(i, "Share Using"));
                                postMenuDialog.dismiss();
                            });

                            postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v1 -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Reels").document(currentItem.getDocID())
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Reel has been reported."));
                                postMenuDialog.dismiss();
                            });

                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.show();

                        } else {
                            postMenuDialog = new BottomSheetDialog(requireActivity());
                            postMenuDialog.setContentView(R.layout.dialog_post_menu);
                            postMenuDialog.setCanceledOnTouchOutside(TRUE);

                            postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v13 -> {
                                link = "Post Link - https://www.applex.in/utsav-app/clips/" + "1/" + currentItem.getDocID();
                                String playstore = "\nCheck out the short video."+getResources().getString(R.string.download_utsav);
                                Intent i = new Intent();
                                i.setAction(Intent.ACTION_SEND);
                                i.putExtra(Intent.EXTRA_TEXT, link+playstore);
                                i.setType("text/plain");
                                startActivity(Intent.createChooser(i, "Share Using"));
                                postMenuDialog.dismiss();
                            });

                            postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                                FirebaseFirestore.getInstance()
                                        .collection("Reels").document(currentItem.getDocID())
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Reel has been reported."));
                                postMenuDialog.dismiss();
                            });

                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.show();
                        }
                    });
                }

                @Override
                public void onViewDetachedFromWindow(@NonNull ReelsItemViewHolder holder) {
                    super.onViewDetachedFromWindow(holder);
                    holder.item_reels_image.setVisibility(View.VISIBLE);
                }

                @NonNull
                @Override
                public ReelsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                    LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                    View v = layoutInflater.inflate(R.layout.item_reels, viewGroup, false);
                    return new ReelsItemViewHolder(v);
                }

                @Override
                public int getItemViewType(int position) { return position; }

                @Override
                protected void onLoadingStateChanged(@NonNull LoadingState state) {
                    super.onLoadingStateChanged(state);
                    switch (state) {
                        case ERROR:
                            BasicUtility.showToast(getActivity(), "Something went wrong...");
                            break;
                        case FINISHED:
                            if(reelsAdapter.getItemCount() == 0) {
                                pvh.reels_item.setVisibility(View.GONE);
                            } else {
                                pvh.reels_item.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            };

            pvh.reelsList.setAdapter(reelsAdapter);
            positions.add(position);

            RecyclerView.LayoutManager manager = pvh.reelsList.getLayoutManager();

            final int[] scrollY = {0};
            pvh.reelsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == 0) {
                        int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
                        int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

                        if (firstVisiblePosition >= 0) {
                            Rect rect_parent = new Rect();
                            pvh.reelsList.getGlobalVisibleRect(rect_parent);

                            for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                                final RecyclerView.ViewHolder holder = pvh.reelsList.findViewHolderForAdapterPosition(i);
                                ReelsItemViewHolder cvh = (ReelsItemViewHolder) holder;

                                int[] location = new int[2];
                                Objects.requireNonNull(cvh).item_reels_video.getLocationOnScreen(location);

                                Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.item_reels_video.getWidth(), location[1] + cvh.item_reels_video.getHeight());

                                float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                                float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                                float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                                float overlapArea = x_overlap * y_overlap;
                                float percent = (overlapArea / rect_parent_area) * 100.0f;

                                if (percent >= 90) {
                                    cvh.item_reels_video.start();
                                    cvh.item_reels_video.setOnPreparedListener(mp -> {
                                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        new Handler().postDelayed(() -> cvh.item_reels_image.setVisibility(View.GONE), 500);
                                        mp.setVolume(0f, 0f);
                                        mp.setLooping(true);
                                    });
                                } else {
                                    cvh.item_reels_video.seekTo(1);
                                    cvh.item_reels_video.pause();
                                }
                            }
                        }
                    }
                }
            });
        }
        else {
            BasicUtility.showToast(getActivity(), "Something went wrong...");
        }
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

        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
        int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

        if (firstVisiblePosition >= 0) {
            Rect rect_parent = new Rect();
            mRecyclerView.getGlobalVisibleRect(rect_parent);

            for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                if(positions != null && positions.contains(i)) {

                    final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                    FeedViewHolder cvh = (FeedViewHolder) holder;

                    int[] location = new int[2];
                    Objects.requireNonNull(cvh).reels_item.getLocationOnScreen(location);
                    Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.reels_item.getWidth(), location[1] + cvh.reels_item.getHeight());

                    float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                    float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                    float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                    float overlapArea = x_overlap * y_overlap;
                    float percent = (overlapArea / rect_parent_area) * 100.0f;

                    if (percent >= 90) {
                        RecyclerView.LayoutManager manager1 = Objects.requireNonNull(cvh).reelsList.getLayoutManager();

                        int firstVisiblePosition1 = ((LinearLayoutManager) Objects.requireNonNull(manager1)).findFirstVisibleItemPosition();
                        int lastVisiblePosition1 = ((LinearLayoutManager) manager1).findLastVisibleItemPosition();

                        if (firstVisiblePosition1 >= 0) {
                            Rect rect_parent1 = new Rect();
                            cvh.reelsList.getGlobalVisibleRect(rect_parent1);

                            for (int j = firstVisiblePosition1; j <= lastVisiblePosition1; j++) {
                                final RecyclerView.ViewHolder holder2 = cvh.reelsList.findViewHolderForAdapterPosition(j);
                                ReelsItemViewHolder cvh1 = (ReelsItemViewHolder) holder2;

                                int[] location1 = new int[2];

                                Objects.requireNonNull(cvh1).item_reels_video.getLocationOnScreen(location1);
                                cvh1.item_reels_image.setVisibility(View.VISIBLE);
                                Rect rect_child1 = new Rect(location1[0], location1[1], location1[0] + cvh1.item_reels_video.getWidth(), location1[1] + cvh1.item_reels_video.getHeight());

                                float rect_parent_area1 = (rect_child1.right - rect_child1.left) * (rect_child1.bottom - rect_child1.top);
                                float x_overlap1 = Math.max(0, Math.min(rect_child1.right, rect_parent1.right) - Math.max(rect_child1.left, rect_parent1.left));
                                float y_overlap1 = Math.max(0, Math.min(rect_child1.bottom, rect_parent1.bottom) - Math.max(rect_child1.top, rect_parent1.top));
                                float overlapArea1 = x_overlap1 * y_overlap1;
                                float percent1 = (overlapArea1 / rect_parent_area1) * 100.0f;

                                if (percent1 >= 90) {
                                    cvh1.item_reels_video.start();
                                    cvh1.item_reels_video.setOnPreparedListener(mp -> {
                                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        new Handler().postDelayed(() -> cvh1.item_reels_image.setVisibility(View.GONE), 500);
                                        mp.setVolume(0f, 0f);
                                        mp.setLooping(true);
                                    });
                                } else {
                                    cvh1.item_reels_video.seekTo(1);
                                    cvh1.item_reels_video.pause();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}