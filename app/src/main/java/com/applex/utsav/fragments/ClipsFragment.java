package com.applex.utsav.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.applex.utsav.HashtagClipsViewAll;
import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.MainActivity;
import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.adapters.CommitteeTopAdapter;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.StoreTemp;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

public class ClipsFragment extends Fragment {

    public static int changed = 0;
    public static int delete = 0;
    private IntroPref introPref;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerFrameLayout;

    private ProgressBar progressMore, contentProgress;
    private ProgressDialog progressDialog;
    private Dialog postMenuDialog;
    public static RecyclerView mRecyclerView;
    private String USERDP, USERNAME, link, GENDER;
    private FloatingActionButton floatingActionButton;

    public ClipsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        introPref = new IntroPref(getActivity());
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        Objects.requireNonNull(getActivity()).getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        View view = inflater.inflate(R.layout.fragment_clips, container, false);
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout= view.findViewById(R.id.swiperefresh);
        contentProgress = view.findViewById(R.id.content_progress);
        progressMore = view.findViewById(R.id.progress_more);
        floatingActionButton = view.findViewById(R.id.to_the_top_clips);

        //////////////RECYCLER VIEW////////////////////
        mRecyclerView = view.findViewById(R.id.recyclerClips) ;
        contentProgress.setVisibility(View.VISIBLE);

        /////////////SETUP//////////////
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        /////////////SETUP//////////////

        contentProgress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        buildRecyclerView();
        //////////////RECYCLER VIEW////////////////////

        introPref = new IntroPref(getActivity());
        USERDP = introPref.getUserdp();
        USERNAME = introPref.getFullName();
        GENDER = introPref.getGender();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.darkpurple),
                        getResources().getColor(R.color.darkpurple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            contentProgress.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            buildRecyclerView();
        });
    }

    private void buildRecyclerView() {
        Query query = FirebaseFirestore.getInstance()
                .collection("Reels")
                .orderBy("newTs", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(1)
                .setPageSize(1)
                .setPrefetchDistance(2)
                .setEnablePlaceholders(true)
                .build();

        FirestorePagingOptions<ReelsPostModel> options = new FirestorePagingOptions.Builder<ReelsPostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    ReelsPostModel newPostModel = new ReelsPostModel();
                    if(snapshot.exists()) {
                        newPostModel = snapshot.toObject(ReelsPostModel.class);
                        Objects.requireNonNull(newPostModel).setDocID(snapshot.getId());
                    }
                    return newPostModel;
                })
                .build();

        FirestorePagingAdapter adapter = new FirestorePagingAdapter<ReelsPostModel, ProgrammingViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder programmingViewHolder, int position, @NonNull ReelsPostModel currentItem) {

                if (programmingViewHolder.getItemViewType() == 0) {

                    programmingViewHolder.posting_item.setVisibility(View.VISIBLE);
                    programmingViewHolder.committee_item.setVisibility(View.GONE);

                    programmingViewHolder.type_something.setVisibility(View.VISIBLE);

                    programmingViewHolder.type_something.setOnClickListener(view -> {
                        if (InternetConnection.checkConnection(requireActivity())) {
                            Intent i = new Intent(getContext(), NewPostHome.class);
                            if(introPref.getType().matches("com")){
                                i.putExtra("target", "1");
                            }
                            else
                                i.putExtra("target", "2");

                            startActivity(i);
                        } else
                            BasicUtility.showToast(getContext(), "Network Unavailable...");
                    });

                    programmingViewHolder.newPostIconsLL.setOnClickListener(view -> {
                        if (InternetConnection.checkConnection(requireActivity())) {
                            Intent i = new Intent(getContext(), NewPostHome.class);

                            if(introPref.getType().matches("com")){
                                i.putExtra("target", "1");
                            }
                            else
                                i.putExtra("target", "2");

                            startActivity(i);
                        } else
                            BasicUtility.showToast(getContext(), "Network Unavailable...");
                    });

                    programmingViewHolder.type_dp.setOnClickListener(view -> {
                        Intent intent = new Intent(getContext(), ActivityProfile.class);
                        intent.putExtra("uid", FirebaseAuth.getInstance().getUid());
                        startActivity(intent);
                    });

                    if (USERDP != null) {
                        Picasso.get().load(USERDP).fit().centerCrop()
                                .placeholder(R.drawable.ic_account_circle_black_24dp)
                                .into(programmingViewHolder.type_dp);
                    }
                    else{
                        if(GENDER!=null){
                            if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                                programmingViewHolder.type_dp.setImageResource(R.drawable.ic_female);
                            }
                            else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                                programmingViewHolder.type_dp.setImageResource(R.drawable.ic_male);
                            }
                            else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                                programmingViewHolder.type_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }
                        else{
                            programmingViewHolder.type_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                }
                else if (programmingViewHolder.getItemViewType() == 4 || programmingViewHolder.getItemViewType() == 2) {
                    programmingViewHolder.committee_item.setVisibility(View.VISIBLE);
                    programmingViewHolder.posting_item.setVisibility(View.GONE);

                    programmingViewHolder.view_all.setOnClickListener(v ->
                            MainActivity.viewPager.setCurrentItem(3, true)
                    );

                    if (programmingViewHolder.getItemViewType() == 4) {
                        programmingViewHolder.comm_heading.setText(getResources().getText(R.string.recently_visited_pujos));
                    } else {
                        programmingViewHolder.comm_heading.setText(getResources().getText(R.string.upvoted_pujos));
                    }

                    buildCommunityRecyclerView(programmingViewHolder.cRecyclerView, programmingViewHolder.getItemViewType());
                }
                else {
                    programmingViewHolder.posting_item.setVisibility(View.GONE);
                    programmingViewHolder.committee_item.setVisibility(View.GONE);
                }

                DocumentReference likeStore;
                String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                programmingViewHolder.minsago.setText(timeAgo);
                if (timeAgo != null) {
                    if (timeAgo.matches("just now")) {
                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        programmingViewHolder.minsago.setTextColor(getResources().getColor(R.color.grey_868686));
                    }
                }

                likeStore = FirebaseFirestore.getInstance().document("Reels/" + currentItem.getDocID() + "/");

                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                if (USERDP != null) {
                    Picasso.get().load(USERDP).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(programmingViewHolder.profileimage);
                }
                else{
                    if(GENDER!=null){
                        if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_female);
                        }
                        else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_male);
                        }
                        else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                    else{
                        programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                }
                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                ///////////TAGLIST///////////////

                ///////////TAG RECYCLER SETUP////////////////
//                programmingViewHolder.tagList.setHasFixedSize(false);
//                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                programmingViewHolder.tagList.setNestedScrollingEnabled(true);
//                programmingViewHolder.tagList.setLayoutManager(linearLayoutManager);
//                ///////////TAG RECYCLER SETUP////////////////
//
//                if (currentItem.getTagL() != null && currentItem.getTagL().size() > 0) {
//                    programmingViewHolder.tagList.setVisibility(View.VISIBLE);
//                    TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL(), getActivity());
//                    programmingViewHolder.tagList.setAdapter(tagAdapter);
//                } else {
//                    programmingViewHolder.tagList.setAdapter(null);
//                    programmingViewHolder.tagList.setVisibility(View.GONE);
//                }
                /////////TAGLIST///////////////

                //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
                programmingViewHolder.username.setOnClickListener(v -> {
//                    if(currentItem.getType().matches("com")) {
//                        Intent intent = new Intent(requireActivity(), ActivityProfileCommittee.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
//                    else {
//                        Intent intent = new Intent(requireActivity(), ActivityProfileUser.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
                    Intent intent = new Intent(requireActivity(), ActivityProfile.class);
                    intent.putExtra("uid", currentItem.getUid());
                    startActivity(intent);
                });

                programmingViewHolder.userimage.setOnClickListener(v -> {
//                    if(currentItem.getType().matches("com")) {
//                        Intent intent = new Intent(requireActivity(), ActivityProfileCommittee.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
//                    else {
//                        Intent intent = new Intent(requireActivity(), ActivityProfileUser.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
                    Intent intent = new Intent(requireActivity(), ActivityProfile.class);
                    intent.putExtra("uid", currentItem.getUid());
                    startActivity(intent);
                });
                //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

                if(currentItem.getType().matches("com")) {
                    programmingViewHolder.dp_layout.setBackgroundResource(R.drawable.dp_outline);
                } else {
                    programmingViewHolder.dp_layout.setBackground(null);
                }

                //////////////LOADING USERNAME AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
                if (currentItem.getCommittee_dp() != null && !currentItem.getCommittee_dp().isEmpty()) {
                    Picasso.get().load(currentItem.getCommittee_dp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(programmingViewHolder.userimage, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    if(currentItem.getGender()!=null){
                                        if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_female);
                                        }
                                        else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_male);
                                        }
                                        else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    }
                                    else {
                                        programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                }
                            });
                }
                else{
                    if(currentItem.getGender()!=null){
                        if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_female);
                        }
                        else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_male);
                        }
                        else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                    else {
                        programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                }

                programmingViewHolder.username.setText(currentItem.getCommittee_name());
                //////////////LOADING USERNAME AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

                if (currentItem.getPujoTag() != null) {
                    programmingViewHolder.pujoTagHolder.setVisibility(View.VISIBLE);
                    programmingViewHolder.pujoTagHolder.setText(currentItem.getPujoTag().getPujoName());

//                    programmingViewHolder.pujoTagHolder.setOnClickListener(v -> {
//                        //To be changed
//                        Intent intent = new Intent(getActivity(), ActivityProfileCommittee.class);
//                        intent.putExtra("uid", currentItem.getPujoTag().getPujoUid());
//                        startActivity(intent);
//                    });
                    programmingViewHolder.pujoTagHolder.setOnClickListener(v -> {
                        //To be changed
                        Intent intent = new Intent(getActivity(), ActivityProfile.class);
                        intent.putExtra("uid", currentItem.getPujoTag().getPujoUid());
                        startActivity(intent);
                    });
                }
                else {
                    programmingViewHolder.pujoTagHolder.setVisibility(View.GONE);
                    programmingViewHolder.pujoTagHolder.setText(null);
                }

                //////////////////////////TEXT & VIDEO FOR POST//////////////////////
                programmingViewHolder.reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
                programmingViewHolder.reels_video.start();

                programmingViewHolder.reels_video.setOnInfoListener((mediaPlayer, i, i1) -> {
                    if(i == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        programmingViewHolder.progress_bar.setVisibility(View.VISIBLE);
                        return true;
                    } else if(i == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        programmingViewHolder.progress_bar.setVisibility(View.GONE);
                        return true;
                    }
                    return false;
                });

                Picasso.get().load(currentItem.getFrame()).into(programmingViewHolder.reels_image);

                programmingViewHolder.reels_video.setOnPreparedListener(mp -> {
                    programmingViewHolder.progress_bar.setVisibility(View.GONE);
                    requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    new Handler().postDelayed(() -> {
                        programmingViewHolder.reels_image.setVisibility(View.GONE);
                        programmingViewHolder.sound.setVisibility(View.VISIBLE);
                    }, 500);

                    if(position == 1) {
                        programmingViewHolder.reels_video.seekTo(1);
                        programmingViewHolder.reels_video.pause();
                    }

                    mp.setLooping(true);

                    if(MainActivity.viewPager.getCurrentItem() == 1 || MainActivity.viewPager.getCurrentItem() == 3) {
                        mp.setVolume(0f, 0f);
                    }
                    else {
                        if(introPref.isVolumeOn()) {
                            mp.setVolume(1f, 1f);
                            programmingViewHolder.sound.setImageResource(R.drawable.ic_baseline_volume_on_24);
                        } else {
                            mp.setVolume(0f, 0f);
                            programmingViewHolder.sound.setImageResource(R.drawable.ic_baseline_volume_off_24);
                        }
                    }

                    programmingViewHolder.sound.setOnClickListener(v -> {
                        if(introPref.isVolumeOn()) {
                            mp.setVolume(0f, 0f);
                            introPref.setIsVolumeOn(false);
                            programmingViewHolder.sound.setImageResource(R.drawable.ic_baseline_volume_off_24);
                        } else {
                            mp.setVolume(1f, 1f);
                            introPref.setIsVolumeOn(true);
                            programmingViewHolder.sound.setImageResource(R.drawable.ic_baseline_volume_on_24);
                        }
                    });
                });

                programmingViewHolder.reels_video.setOnCompletionListener(MediaPlayer::reset);

                if(programmingViewHolder.reels_video.getVisibility() == View.VISIBLE) {
                    programmingViewHolder.reels_video.setOnClickListener(v -> {
                        Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                        intent.putExtra("bool", "3");
                        intent.putExtra("docID", currentItem.getDocID());
                        requireActivity().startActivity(intent);
                    });
                }
                else if(programmingViewHolder.reels_image.getVisibility() == View.VISIBLE) {
                    programmingViewHolder.reels_image.setOnClickListener(v -> {
                        Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                        intent.putExtra("bool", "3");
                        intent.putExtra("docID", currentItem.getDocID());
                        requireActivity().startActivity(intent);
                    });
                }

                if (currentItem.getHeadline() == null || currentItem.getHeadline().isEmpty()) {
                    programmingViewHolder.head_content.setVisibility(View.GONE);
                    programmingViewHolder.head_content.setText(null);
                } else {
                    if(currentItem.getHeadline().equals("Stay safe. Do not forget to wear a mask. And enjoy you puja sitting at home through Utsav")
                            || currentItem.getHeadline().equals("সাবধানে থাকুন । বাইরে বেরোলে অবশ্যই মাস্ক ব্যবহার করুন । ঘরে বসেই পুজোর আনন্দ উপভোগ করুন")) {
                        programmingViewHolder.head_content.setVisibility(View.GONE);
                        programmingViewHolder.head_content.setText(null);
                    } else {
                        programmingViewHolder.head_content.setVisibility(View.VISIBLE);
                        programmingViewHolder.head_content.setText(currentItem.getHeadline());
                    }
                }

                if (currentItem.getDescription() == null || currentItem.getDescription().isEmpty()) {
                    programmingViewHolder.text_content.setVisibility(View.GONE);
                    programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                    programmingViewHolder.text_content.setText(null);
                } else {
                    programmingViewHolder.text_content.setVisibility(View.VISIBLE);
                    programmingViewHolder.text_content.setText(currentItem.getDescription());

                    //TAGS COLOURED DISPLAY
                    Pattern p = Pattern.compile("[#][a-zA-Z0-9-_]+");
                    Matcher m = p.matcher(programmingViewHolder.text_content.getText().toString());

                    SpannableString ss = new SpannableString(programmingViewHolder.text_content.getText().toString());

                    while(m.find()) // loops through all the words in the text which matches the pattern
                    {
                        final int s = m.start(); // add 1 to omit the "@" tag
                        final int e = m.end();

                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(@NonNull View textView) {
                                Intent i = new Intent(getContext(), HashtagClipsViewAll.class);
                                i.putExtra("hashtag", programmingViewHolder.text_content.getText().toString().substring(s+1, e));
                                startActivity(i);
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
                    programmingViewHolder.text_content.setText(ss);
                    programmingViewHolder.text_content.setMovementMethod(LinkMovementMethod.getInstance());
                    programmingViewHolder.text_content.setHighlightColor(Color.TRANSPARENT);
                    //TAGS COLOURED DISPLAY

                    if (programmingViewHolder.text_content.getUrls().length > 0 ) {
                        URLSpan urlSnapItem = programmingViewHolder.text_content.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if (url.contains("http")) {
                            programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                            programmingViewHolder.LinkPreview.setLink(url, new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) { }

                                @Override
                                public void onError(Exception e) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        //do stuff like remove view etc
                                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                                    });
                                }
                            });
                        }
                    }
                    else if (programmingViewHolder.head_content.getUrls().length > 0 ) {
                        URLSpan urlSnapItem = programmingViewHolder.head_content.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if (url.contains("http")) {
                            programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                            programmingViewHolder.LinkPreview.setLink(url, new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) { }

                                @Override
                                public void onError(Exception e) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        //do stuff like remove view etc
                                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                                    });
                                }
                            });
                        }
                    }
                    else {
                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                    }
                }

                programmingViewHolder.rlLayout.setVisibility(View.VISIBLE);

                programmingViewHolder.text_content.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ReelsActivity.class);
                    intent.putExtra("bool", "3");
                    intent.putExtra("docID", currentItem.getDocID());
                    startActivity(intent);
                });

                programmingViewHolder.head_content.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ReelsActivity.class);
                    intent.putExtra("bool", "3");
                    intent.putExtra("docID", currentItem.getDocID());
                    startActivity(intent);
                });
                //////////////////////////TEXT & VIDEO FOR POST//////////////////////

                programmingViewHolder.like_layout.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Reels", currentItem.getDocID());
                    bottomSheetDialog.show(requireActivity().getSupportFragmentManager(), "FlamedBySheet");
                });

                ///////////////////FLAMES AND COMMENTS///////////////////////

                //INITIAL SETUP//
                if (currentItem.getLikeL() != null) {
                    if (currentItem.getLikeL().size() == 0) {
                        programmingViewHolder.like_layout.setVisibility(View.GONE);
                    } else {
                        programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size()));
                    }

                    for (int j = 0; j < currentItem.getLikeL().size(); j++) {
                        if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                            programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
                            currentItem.setLikeCheck(j);
                        }
                    }
                } else {
                    programmingViewHolder.like_layout.setVisibility(View.GONE);
                }
                //INITIAL SETUP//

                PushDownAnim.setPushDownAnimTo(programmingViewHolder.like)
                        .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                        .setOnClickListener(v -> {
                            if (currentItem.getLikeCheck() >= 0) {
                                programmingViewHolder.like.setImageResource(R.drawable.ic_btmnav_notifications);//was already liked by current user
                                if (currentItem.getLikeL().size() - 1 == 0) {
                                    programmingViewHolder.like_layout.setVisibility(View.GONE);
                                } else {
                                    programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                                    programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() - 1));
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

                                batch.commit().addOnSuccessListener(task -> {
                                });
                                ///////////////////BATCH WRITE///////////////////
                            } else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                                BasicUtility.vibrate(requireActivity());
                                programmingViewHolder.dhak_anim.setVisibility(View.VISIBLE);
                                programmingViewHolder.dhak_anim.playAnimation();
                                try {
                                    AssetFileDescriptor afd = requireActivity().getAssets().openFd("dhak.mp3");
                                    MediaPlayer player = new MediaPlayer();
                                    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                    player.prepare();
                                    AudioManager audioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
                                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                        player.start();
                                    }
                                    new Handler().postDelayed(() -> {
                                        programmingViewHolder.dhak_anim.cancelAnimation();
                                        programmingViewHolder.dhak_anim.setVisibility(View.GONE);
                                    }, player.getDuration());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
                                programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                                if (currentItem.getLikeL() != null) {
                                    programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() + 1));
                                } else {
                                    programmingViewHolder.likesCount.setText("1");
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
                                flamedModel.setUserdp(USERDP);
                                flamedModel.setUsername(USERNAME);
                                flamedModel.setPostUid(currentItem.getUid());

                                DocumentReference flamedDoc = likeStore.collection("flameL")
                                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
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

                programmingViewHolder.commentimg.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 1, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                });

                programmingViewHolder.writecomment.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 1, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                });

                programmingViewHolder.share.setOnClickListener(view -> {
                    if(currentItem.getHeadline() != null && currentItem.getDescription() == null) {
                        String link = "\n\nCheck out this short video!\n\nPost Link - https://www.applex.in/utsav-app/clips/" + "3/" + currentItem.getDocID();
                        String playstore = getResources().getString(R.string.download_utsav);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getHeadline()+link+playstore);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent,"Share Using"));
                    }
                    else if(currentItem.getHeadline() == null && currentItem.getDescription() != null) {
                        String link = "\n\nCheck out this short video!\n\nPost Link - https://www.applex.in/utsav-app/clips/" + "3/" + currentItem.getDocID();
                        String playstore = getResources().getString(R.string.download_utsav);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getDescription()+link+playstore);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent,"Share Using"));
                    }
                    else if(currentItem.getHeadline() != null && currentItem.getHeadline() != null) {
                        String link = "\n\nCheck out this short video!\n\nPost Link - https://www.applex.in/utsav-app/clips/" + "3/" + currentItem.getDocID();
                        String playstore = getResources().getString(R.string.download_utsav);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,currentItem.getHeadline() + "\n\n" + currentItem.getDescription()+link+playstore);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent,"Share Using"));
                    }
                    else {
                        String link = "Check out this short video!\n\nPost Link - https://www.applex.in/utsav-app/clips/" + "3/" + currentItem.getDocID();
                        String playstore = getResources().getString(R.string.download_utsav);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,link+playstore);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent,"Share Using"));
                    }
                });

                if (currentItem.getCmtNo() > 0) {
                    ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                    ProgrammingViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

                    if (currentItem.getCom1() != null && !currentItem.getCom1().isEmpty()) {

                        programmingViewHolder.commentLayout1.setVisibility(View.VISIBLE);

                        programmingViewHolder.name_cmnt1.setText(currentItem.getCom1_usn());

                        if(currentItem.getCom1_dp()!=null && !currentItem.getCom1_dp().isEmpty()){
                            Picasso.get().load(currentItem.getCom1_dp())
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(programmingViewHolder.dp_cmnt1);
                        }
                        else{
                            if(currentItem.getCom1_gender()!=null){
                                if (currentItem.getCom1_gender().matches("Female") || currentItem.getCom1_gender().matches("মহিলা")){
                                    programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_female);
                                }
                                else if (currentItem.getCom1_gender().matches("Male") || currentItem.getCom1_gender().matches("পুরুষ")){
                                    programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_male);
                                }
                                else if (currentItem.getCom1_gender().matches("Others") || currentItem.getCom1_gender().matches("অন্যান্য")){
                                    programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            }
                            else {
                                programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }

                        programmingViewHolder.cmnt1.setText(currentItem.getCom1());
                        if (programmingViewHolder.cmnt1.getUrls().length > 0) {
                            URLSpan urlSnapItem = programmingViewHolder.cmnt1.getUrls()[0];
                            String url = urlSnapItem.getURL();
                            if (url.contains("http")) {
                                programmingViewHolder.link_preview1.setVisibility(View.VISIBLE);
                                programmingViewHolder.link_preview1.setLink(url, new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean status) { }

                                    @Override
                                    public void onError(Exception e) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            //do stuff like remove view etc
                                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
                                        });
                                    }
                                });
                            }
                        } else {
                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
                        }

                        programmingViewHolder.cmnt1_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom1_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom1_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom1_ts())).matches("just now")) {
                                programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                programmingViewHolder.cmnt1_minsago.setTextColor(getResources().getColor(R.color.grey_868686));
                            }
                        }
                    } else {
                        programmingViewHolder.commentLayout1.setVisibility(View.GONE);
                    }

                    if (currentItem.getCom2() != null && !currentItem.getCom2().isEmpty()) {

                        programmingViewHolder.commentLayout2.setVisibility(View.VISIBLE);
                        programmingViewHolder.name_cmnt2.setText(currentItem.getCom2_usn());

                        if(currentItem.getCom2_dp()!=null && !currentItem.getCom2_dp().isEmpty()){
                            Picasso.get().load(currentItem.getCom2_dp())
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(programmingViewHolder.dp_cmnt2);
                        }
                        else{
                            if(currentItem.getCom2_gender()!=null){
                                if (currentItem.getCom2_gender().matches("Female") || currentItem.getCom2_gender().matches("মহিলা")){
                                    programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_female);
                                }
                                else if (currentItem.getCom2_gender().matches("Male") || currentItem.getCom2_gender().matches("পুরুষ")){
                                    programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_male);
                                }
                                else if (currentItem.getCom2_gender().matches("Others") || currentItem.getCom2_gender().matches("অন্যান্য")){
                                    programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            }
                            else {
                                programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }

                        programmingViewHolder.cmnt2.setText(currentItem.getCom2());
                        if (programmingViewHolder.cmnt2.getUrls().length > 0) {
                            URLSpan urlSnapItem = programmingViewHolder.cmnt2.getUrls()[0];
                            String url = urlSnapItem.getURL();
                            if (url.contains("http")) {
                                programmingViewHolder.link_preview1.setVisibility(View.VISIBLE);
                                programmingViewHolder.link_preview1.setLink(url, new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean status) {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            //do stuff like remove view etc
                                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
                                        });
                                    }
                                });
                            }
                        } else {
                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
                        }

                        programmingViewHolder.cmnt2_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom2_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom2_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom2_ts())).matches("just now")) {
                                programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                programmingViewHolder.cmnt2_minsago.setTextColor(getResources().getColor(R.color.grey_868686));
                            }
                        }
                    } else {
                        programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                    }

                    ProgrammingViewHolder.comment_layout.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 2, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout1.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 2, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout2.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 2, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });
                }
                else {
                    ProgrammingViewHolder.comment_layout.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout1.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                }
                ///////////////////FLAMES AND COMMENTS///////////////////////

                ////////POST MENU///////
                programmingViewHolder.menuPost.setOnClickListener(v -> {
                    if (currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);
                        postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                            Intent i = new Intent(getActivity(), NewPostHome.class);
                            i.putExtra("target", "100"); //target value for edit post
                            i.putExtra("bool", "2");
                            i.putExtra("typeofpost", "reel");
                            i.putExtra("txt", currentItem.getDescription());
                            i.putExtra("headline", currentItem.getHeadline());
                            if(currentItem.getTagList() != null && currentItem.getTagList().size()>0) {
                                Bundle args = new Bundle();
                                args.putSerializable("ARRAYLISTTAGS", currentItem.getTagList());
                                i.putExtra("BUNDLETAGS", args);
                            }
                            i.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setPujoTagModel(currentItem.getPujoTag());
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
                                                .collection("Reels").document(currentItem.getDocID()).delete()
                                                .addOnSuccessListener(aVoid -> {
//                                                    ActivityProfileCommittee.delete = 1;
                                                    ActivityProfile.delete = 1;
                                                    programmingViewHolder.itemHome.setVisibility(View.GONE);
                                                    notifyDataSetChanged();
                                                    progressDialog.dismiss();
                                                });
                                        postMenuDialog.dismiss();
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Reels").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();
                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    } else {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Reels").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Post has been reported."));
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
            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_clips, viewGroup, false);
                return new ProgrammingViewHolder(v);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull ProgrammingViewHolder holder) {
                super.onViewDetachedFromWindow(holder);
                holder.reels_video.pause();
                holder.reels_image.setVisibility(View.VISIBLE);
                holder.sound.setVisibility(View.GONE);
                holder.progress_bar.setVisibility(View.VISIBLE);
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public int getItemCount() {
                return super.getItemCount();
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR:
                        BasicUtility.showToast(getActivity(), "Something went wrong...");
                        break;
                    case LOADING_MORE:
                        progressMore.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        new Handler().postDelayed(() -> {
                            mRecyclerView.setVisibility(View.VISIBLE);
                            progressMore.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                        }, 500);
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED:
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        contentProgress.setVisibility(View.GONE);
                        progressMore.setVisibility(View.GONE);
                        break;
                }
            }
        };

        contentProgress.setVisibility(View.GONE);
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
                            final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                            ProgrammingViewHolder cvh = (ProgrammingViewHolder) holder;

                            int[] location = new int[2];
                            Objects.requireNonNull(cvh).reels_video.getLocationOnScreen(location);

                            Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.reels_video.getWidth(), location[1] + cvh.reels_video.getHeight());

                            float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                            float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                            float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                            float overlapArea = x_overlap * y_overlap;
                            float percent = (overlapArea / rect_parent_area) * 100.0f;

                            if (percent >= 90) {
                                cvh.reels_video.start();
                                cvh.reels_video.setOnInfoListener((mediaPlayer, i2, i1) -> {
                                    if(i2 == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                                        cvh.progress_bar.setVisibility(View.VISIBLE);
                                        return true;
                                    } else if(i2 == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                                        cvh.progress_bar.setVisibility(View.GONE);
                                        return true;
                                    }
                                    return false;
                                });
                                cvh.reels_video.setOnPreparedListener(mp -> {
                                    requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    new Handler().postDelayed(() -> {
                                        cvh.progress_bar.setVisibility(View.GONE);
                                        cvh.reels_image.setVisibility(View.GONE);
                                        cvh.sound.setVisibility(View.VISIBLE);
                                    }, 500);
                                    mp.setLooping(true);
                                    if(introPref.isVolumeOn()) {
                                        mp.setVolume(1f, 1f);
                                        cvh.sound.setImageResource(R.drawable.ic_baseline_volume_on_24);
                                    } else {
                                        mp.setVolume(0f, 0f);
                                        cvh.sound.setImageResource(R.drawable.ic_baseline_volume_off_24);
                                    }

                                    cvh.sound.setOnClickListener(v -> {
                                        if(introPref.isVolumeOn()) {
                                            mp.setVolume(0f, 0f);
                                            introPref.setIsVolumeOn(false);
                                            cvh.sound.setImageResource(R.drawable.ic_baseline_volume_off_24);
                                        } else {
                                            mp.setVolume(1f, 1f);
                                            introPref.setIsVolumeOn(true);
                                            cvh.sound.setImageResource(R.drawable.ic_baseline_volume_on_24);
                                        }
                                    });
                                });
                            } else {
                                cvh.progress_bar.setVisibility(View.GONE);
                                cvh.sound.setVisibility(View.GONE);
                                cvh.reels_video.seekTo(1);
                                cvh.reels_video.pause();
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

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        @SuppressLint("StaticFieldLeak")
        public static TextView commentCount;
        @SuppressLint("StaticFieldLeak")
        public static LinearLayout comment_layout;

        public VideoView reels_video;
        TextView username, text_content, head_content, likesCount, minsago, writecomment, name_cmnt1, cmnt1, cmnt1_minsago, name_cmnt2, cmnt2, cmnt2_minsago, type_something, comm_heading, pujoTagHolder;
        ImageView userimage, like, commentimg, profileimage, menuPost, share, like_image, comment_image, dp_cmnt1, dp_cmnt2, type_dp, reels_image, sound;
        ApplexLinkPreview LinkPreview;
        LinearLayout itemHome, commentLayout1, commentLayout2, like_layout,new_post_layout, newPostIconsLL;
        RecyclerView tagList;
        com.applex.utsav.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;
        LottieAnimationView dhak_anim;
        ProgressBar progress_bar;
        RelativeLayout normal_item, rlLayout, dp_layout;
        LinearLayout posting_item, committee_item;
        TextView view_all;
        RecyclerView cRecyclerView;

        public ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);

            tagList = itemView.findViewById(R.id.tagsList);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage = itemView.findViewById(R.id.user_image);
            minsago = itemView.findViewById(R.id.mins_ago);
            like = itemView.findViewById(R.id.like);
            commentimg = itemView.findViewById(R.id.comment);
            profileimage = itemView.findViewById(R.id.profile_image);
            menuPost = itemView.findViewById(R.id.delete_post);
            writecomment = itemView.findViewById(R.id.write_comment);
            itemHome = itemView.findViewById(R.id.item_home);
            share = itemView.findViewById(R.id.share);
            LinkPreview = itemView.findViewById(R.id.LinkPreView);

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

            type_dp = itemView.findViewById(R.id.Pdp);
            type_something = itemView.findViewById(R.id.type_smthng);
            new_post_layout = itemView.findViewById(R.id.type_something);
            newPostIconsLL= itemView.findViewById(R.id.post_icons_ll);

            posting_item = itemView.findViewById(R.id.posting_item);
            normal_item = itemView.findViewById(R.id.normal_item);
            head_content = itemView.findViewById(R.id.head_content);
            dhak_anim = itemView.findViewById(R.id.dhak_anim);
            rlLayout = itemView.findViewById(R.id.rlLayout);

            committee_item = itemView.findViewById(R.id.committee_item);
            view_all = itemView.findViewById(R.id.community_view_all);
            cRecyclerView = itemView.findViewById(R.id.communityRecycler);
            comm_heading = itemView.findViewById(R.id.com_heading);
            pujoTagHolder = itemView.findViewById(R.id.tag_pujo);

            reels_image = itemView.findViewById(R.id.reels_image);
            reels_video = itemView.findViewById(R.id.reels_video);
            sound = itemView.findViewById(R.id.sound);
            dp_layout = itemView.findViewById(R.id.dp_layout);
            progress_bar = itemView.findViewById(R.id.progress_bar_clips);
        }
    }

    private void buildCommunityRecyclerView(RecyclerView cRecyclerView, int position) {
        cRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, RecyclerView.HORIZONTAL, false);
        cRecyclerView.setLayoutManager(gridLayoutManager);
        cRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ArrayList<BaseUserModel> committees = new ArrayList<>();
        Query query;

        if(position == 4) {
            query = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("type", "com")
                    .orderBy("lastVisitTime", Query.Direction.DESCENDING)
                    .limit(20);
        } else {
            query = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("type", "com")
                    .orderBy("upvotes", Query.Direction.DESCENDING)
                    .limit(20);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document: queryDocumentSnapshots) {
                if(document.exists()) {
                    BaseUserModel communityModel1 = document.toObject(BaseUserModel.class);
                    committees.add(communityModel1);
                }
            }
            if(committees.size()>0) {
                CommitteeTopAdapter communityAdapter= new CommitteeTopAdapter(committees, getActivity(), position);
                cRecyclerView.setAdapter(communityAdapter);
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error Community", Toast.LENGTH_LONG).show());
    }

    @Override
    public void onResume() {
        if((changed > 0 || delete > 0)) {
            buildRecyclerView();
            changed = 0;
            delete = 0;
        }
        super.onResume();

        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
        int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

        if (firstVisiblePosition >= 0) {
            Rect rect_parent = new Rect();
            mRecyclerView.getGlobalVisibleRect(rect_parent);

            for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                ProgrammingViewHolder cvh = (ProgrammingViewHolder) holder;

                int[] location = new int[2];
                Objects.requireNonNull(cvh).reels_video.getLocationOnScreen(location);

                Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.reels_video.getWidth(), location[1] + cvh.reels_video.getHeight());

                float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                float overlapArea = x_overlap * y_overlap;
                float percent = (overlapArea / rect_parent_area) * 100.0f;

               if (percent >= 90) {
                   cvh.reels_video.start();
                   cvh.reels_video.setOnInfoListener((mediaPlayer, i2, i1) -> {
                       if(i2 == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                           cvh.progress_bar.setVisibility(View.VISIBLE);
                           return true;
                       } else if(i2 == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                           cvh.progress_bar.setVisibility(View.GONE);
                           return true;
                       }
                       return false;
                   });
                   cvh.reels_video.setOnPreparedListener(mp -> {
                       cvh.progress_bar.setVisibility(View.GONE);
                       requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        new Handler().postDelayed(() -> {
                            cvh.reels_image.setVisibility(View.GONE);
                            cvh.sound.setVisibility(View.VISIBLE);
                        }, 500);

                        mp.setLooping(true);

                        if(MainActivity.viewPager.getCurrentItem() == 1 || MainActivity.viewPager.getCurrentItem() == 3) {
                            mp.setVolume(0f, 0f);
                        }
                        else {
                            if(introPref.isVolumeOn()) {
                                mp.setVolume(1f, 1f);
                                cvh.sound.setImageResource(R.drawable.ic_baseline_volume_on_24);
                            } else {
                                mp.setVolume(0f, 0f);
                                cvh.sound.setImageResource(R.drawable.ic_baseline_volume_off_24);
                            }
                        }

                        cvh.sound.setOnClickListener(v -> {
                            if(introPref.isVolumeOn()) {
                                mp.setVolume(0f, 0f);
                                introPref.setIsVolumeOn(false);
                                cvh.sound.setImageResource(R.drawable.ic_baseline_volume_off_24);
                            } else {
                                mp.setVolume(1f, 1f);
                                introPref.setIsVolumeOn(true);
                                cvh.sound.setImageResource(R.drawable.ic_baseline_volume_on_24);
                            }
                        });
                    });
                } else {
                    cvh.progress_bar.setVisibility(View.GONE);
                    cvh.sound.setVisibility(View.GONE);
                    cvh.reels_video.seekTo(1);
                    cvh.reels_video.pause();
                }
            }
        }
    }
}