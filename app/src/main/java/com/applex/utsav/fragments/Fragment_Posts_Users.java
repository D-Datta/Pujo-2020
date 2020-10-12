package com.applex.utsav.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
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

import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.adapters.SliderAdapter;
import com.applex.utsav.adapters.TagAdapter;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.StoreTemp;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class Fragment_Posts_Users extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar contentprogressposts,progressmoreposts;
    private LinearLayout noneImage;
    private FirestorePagingAdapter adapter1;
    private String my_uid, link;
    private ProgressDialog progressDialog;
    private Dialog postMenuDialog;

    public static int delete = 0;
    public static int change = 0;
    private RecyclerView mRecyclerView;
    private IntroPref introPref;
    private String USERNAME, Userprofilepic, GENDER;

    public Fragment_Posts_Users() {
        // Required empty public constructor
    }

    public static Fragment_Posts_Users newInstance(String id) {
        Bundle args = new Bundle();
        args.putString("id", id);
        Fragment_Posts_Users f = new Fragment_Posts_Users();
        f.setArguments(args);
        return f;
    }

//    public Fragment_Posts_Users(String uid){
//        this.my_uid = uid;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mRecyclerView = view.findViewById(R.id.recycler_posts);
        contentprogressposts = view.findViewById(R.id.content_progress_posts);
        progressmoreposts = view.findViewById(R.id.progress_more_posts);
        noneImage = view.findViewById(R.id.none_image);

        introPref = new IntroPref(requireActivity());
        Userprofilepic =  introPref.getUserdp();
        USERNAME = introPref.getFullName();
        GENDER = introPref.getGender();
        my_uid = Objects.requireNonNull(getArguments()).getString("id");

        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(10);

        buildRecycler();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources()
                        .getColor(R.color.purple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildRecycler();
        });
    }

    private void buildRecycler(){
        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds")
                .whereEqualTo("uid", my_uid)
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
                        HomePostModel newPostModel = new HomePostModel();
                        if(snapshot.exists()) {
                            newPostModel = snapshot.toObject(HomePostModel.class);
                            newPostModel.setDocID(snapshot.getId());
                        }
                        return newPostModel;
                    }
                })
                .build();

        adapter1 = new FirestorePagingAdapter<HomePostModel, RecyclerView.ViewHolder>(options) {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_profile_info, viewGroup, false);
                return new ProgrammingViewHolder(v);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull HomePostModel currentItem) {

                ProgrammingViewHolder programmingViewHolder = (ProgrammingViewHolder) holder;

                DocumentReference likeStore;
                String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                programmingViewHolder.minsago.setText(timeAgo);
                if (timeAgo != null) {
                    if (timeAgo.matches("just now")) {
                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#aa212121"));
                    }
                }

                ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////
                likeStore = FirebaseFirestore.getInstance()
                        .document("Feeds/"+currentItem.getDocID()+"/");

                programmingViewHolder.menuPost.setVisibility(View.VISIBLE);
                ///////////SET DOCUMENT REFERENCE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////


                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                if (Userprofilepic != null) {
                    Picasso.get().load(Userprofilepic).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(programmingViewHolder.profileimage);
                }
                else {
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
//                    programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                ///////////TAGLIST///////////////
                ///////////TAG RECYCLER SETUP////////////////
                programmingViewHolder.tagList.setHasFixedSize(false);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                programmingViewHolder.tagList.setNestedScrollingEnabled(true);
                programmingViewHolder.tagList.setLayoutManager(linearLayoutManager);
                ///////////TAG RECYCLER SETUP////////////////
                if(currentItem.getTagL()!=null && currentItem.getTagL().size()>0 ) {
                    programmingViewHolder.tagList.setVisibility(View.VISIBLE);
                    TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL() , requireActivity());
                    programmingViewHolder.tagList.setAdapter(tagAdapter);
                }
                else {
                    programmingViewHolder.tagList.setAdapter(null);
                    programmingViewHolder.tagList.setVisibility(View.GONE);
                }
                /////////TAGLIST///////////////


                //////////////LOADING USERNAME AND USERDP FROM USERNODE FOR CURRENT POST USER///////////////
                ////////////NORMAL POST///////////////
                if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
                    Picasso.get().load(currentItem.getDp()).fit().centerCrop()
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
//                                    programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            });
                }
                else {
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
//                    programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                programmingViewHolder.username.setText(currentItem.getUsN());

                if(currentItem.getTxt()==null || currentItem.getTxt().isEmpty()){
                    programmingViewHolder.text_content.setVisibility(View.GONE);
                    programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                    programmingViewHolder.text_content.setText(null);
                }
                else{
                    programmingViewHolder.text_content.setVisibility(View.VISIBLE);
                    programmingViewHolder.text_content.setText(currentItem.getTxt());
                    if(programmingViewHolder.text_content.getUrls().length>0){
                        URLSpan urlSnapItem = programmingViewHolder.text_content.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if(url.contains("http")){
                            programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                            programmingViewHolder.LinkPreview.setLink(url ,new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) {
                                }

                                @Override
                                public void onError(Exception e) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //do stuff like remove view etc
                                            programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });
                        }

                    } else {
                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                    }

                }
//
//                        String postimage_url = currentItem.getImg();
//                        if(postimage_url!=null){
//                            programmingViewHolder.postimage.setVisibility(View.VISIBLE);
//                            Picasso.get().load(postimage_url)
//                                    .placeholder(R.drawable.image_background_grey)
//                                    .memoryPolicy(MemoryPolicy.NO_STORE)
//                                    .into(programmingViewHolder.postimage);
//
//                            programmingViewHolder.postimage.setOnLongClickListener(v -> {
//
//                                Picasso.get().load(postimage_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(new Target() {
//                                    @Override
//                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                        save_Dialog(bitmap);
//                                    }
//                                    @Override
//                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
//                                    }
//                                    @Override
//                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                                    }
//
//                                });
//                                return true;
//                            });
//                        }
//                        else
//                            programmingViewHolder.postimage.setVisibility(View.GONE);
                if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                    programmingViewHolder.rlLayout.setVisibility(View.VISIBLE);
                    programmingViewHolder.sliderView.setVisibility(View.VISIBLE);
                    programmingViewHolder.sliderView.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    programmingViewHolder.sliderView.setIndicatorRadius(5);
                    programmingViewHolder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    programmingViewHolder.sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                    programmingViewHolder.sliderView.setIndicatorSelectedColor(Color.WHITE);
                    programmingViewHolder.sliderView.setIndicatorUnselectedColor(R.color.colorAccent);
                    programmingViewHolder.sliderView.setAutoCycle(false);

                    SliderAdapter sliderAdapter = new SliderAdapter(requireActivity(), currentItem.getImg(),currentItem);
                    programmingViewHolder.sliderView.setSliderAdapter(sliderAdapter);

                    programmingViewHolder.text_content.setOnClickListener(v -> {
                        Intent intent = new Intent(requireActivity(), ViewMoreHome.class);
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
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(),
                                programmingViewHolder.sliderView, Objects.requireNonNull(ViewCompat.getTransitionName(programmingViewHolder.sliderView)));
                        startActivity(intent, optionsCompat.toBundle());
                    });
                }
                else {
                    programmingViewHolder.rlLayout.setVisibility(View.GONE);
                    programmingViewHolder.sliderView.setVisibility(View.GONE);
                    programmingViewHolder.text_content.setOnClickListener(v -> {
                        Intent intent = new Intent(requireActivity(), ViewMoreText.class);
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
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(),
                                programmingViewHolder.sliderView, Objects.requireNonNull(ViewCompat.getTransitionName(programmingViewHolder.sliderView)));
                        startActivity(intent, optionsCompat.toBundle());
//                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2);
//                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
                    });
                }

                //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                programmingViewHolder.like_layout.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", currentItem.getDocID());
                    bottomSheetDialog.show(getFragmentManager(), "FlamedBySheet");
                });

                ///////////////////FLAMES AND COMMENTS///////////////////////

                //INITIAL SETUP//
                if(currentItem.getLikeL() != null){
                    /////////////////UPDATNG FLAMED BY NO.//////////////////////
                    if (currentItem.getLikeL().size() == 0) {
                        programmingViewHolder.like_layout.setVisibility(View.GONE);
                    }
                    else {
                        programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size()));
                    }

                    for (int j = 0; j < currentItem.getLikeL().size(); j++) {
                        if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                            programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
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
                }
                else{
                    programmingViewHolder.like_layout.setVisibility(View.GONE);
                }
                //INITIAL SETUP//


                PushDownAnim.setPushDownAnimTo(programmingViewHolder.like)
                        .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(currentItem.getLikeCheck() >= 0){//was already liked by current user
                                    programmingViewHolder.like.setImageResource(R.drawable.ic_btmnav_notifications);
                                    if (currentItem.getLikeL().size() - 1 == 0) {
                                        programmingViewHolder.like_layout.setVisibility(View.GONE);
                                    } else{
                                        programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() - 1));
                                    }
                                    ///////////REMOVE CURRENT USER LIKE/////////////
                                    currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                                    currentItem.setLikeCheck(-1);

                                    //                likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

                                    ///////////////////BATCH WRITE///////////////////
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                    DocumentReference flamedDoc = likeStore.collection("flameL")
                                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                                    batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                    batch.delete(flamedDoc);

                                    batch.commit().addOnSuccessListener(task -> {

                                    });
                                    ///////////////////BATCH WRITE///////////////////
                                }
                                else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                                    BasicUtility.vibrate(requireActivity());
                                    programmingViewHolder.dhak_anim.setVisibility(View.VISIBLE);
                                    programmingViewHolder.dhak_anim.playAnimation();
                                    try {
                                        AssetFileDescriptor afd = requireActivity().getAssets().openFd("dhak.mp3");
                                        MediaPlayer player = new MediaPlayer();
                                        player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                                        player.prepare();
                                        AudioManager audioManager = (AudioManager)requireActivity().getSystemService(Context.AUDIO_SERVICE);
                                        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                            player.start();
                                            if(!player.isPlaying()) {
                                                programmingViewHolder.dhak_anim.cancelAnimation();
                                                programmingViewHolder.dhak_anim.setVisibility(View.GONE);
                                            }
                                            player.setOnCompletionListener(mediaPlayer -> {
                                                programmingViewHolder.dhak_anim.cancelAnimation();
                                                programmingViewHolder.dhak_anim.setVisibility(View.GONE);
                                            });
                                        } else {
                                            new Handler().postDelayed(() -> {
                                                programmingViewHolder.dhak_anim.cancelAnimation();
                                                programmingViewHolder.dhak_anim.setVisibility(View.GONE);
                                            }, 2000);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
                                    programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                                    if (currentItem.getLikeL() != null){
                                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() + 1));
                                    }
                                    else{
                                        programmingViewHolder.likesCount.setText("1");
                                    }


                                    //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                    currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                    currentItem.setLikeCheck(currentItem.getLikeL().size()-1);
                                    //For local changes current item like added to remote list end

                                    ///////////////////BATCH WRITE///////////////////
                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                    FlamedModel flamedModel = new FlamedModel();
                                    long tsLong = System.currentTimeMillis();

                                    flamedModel.setPostID(currentItem.getDocID());
                                    flamedModel.setTs(tsLong);
                                    flamedModel.setType(introPref.getType());
                                    flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                    flamedModel.setUserdp(Userprofilepic);
                                    flamedModel.setUsername(USERNAME);
                                    flamedModel.setPostUid(currentItem.getUid());
                                    flamedModel.setGender(introPref.getGender());

                                    DocumentReference flamedDoc = likeStore.collection("flameL")
                                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                                    batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                    batch.set(flamedDoc, flamedModel);
                                    if(currentItem.getLikeL().size() % 5 == 0){
                                        batch.update(likeStore,"newTs", tsLong);
                                    }
                                    batch.commit().addOnSuccessListener(task -> {

                                    });
                                    ///////////////////BATCH WRITE///////////////////
                                }
                            }
                        });

                programmingViewHolder.commentimg.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 1, "ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(getFragmentManager(), "CommentsSheet");
                    try {
                        AssetFileDescriptor afd =requireActivity().getAssets().openFd("sonkho.mp3");
                        MediaPlayer player = new MediaPlayer();
                        player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                        player.prepare();
                        AudioManager audioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
                        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            player.start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                programmingViewHolder.writecomment.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 1,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(getFragmentManager(), "CommentsSheet");
                });

                programmingViewHolder.share.setOnClickListener(new View.OnClickListener() {
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
                    ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                    ProgrammingViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

                    if(currentItem.getCom1() != null && !currentItem.getCom1().isEmpty()) {

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

                        programmingViewHolder.cmnt1_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom1_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom1_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom1_ts())).matches("just now")) {
                                programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                    } else {
                        programmingViewHolder.commentLayout1.setVisibility(View.GONE);
                    }

                    if(currentItem.getCom2() != null && !currentItem.getCom2().isEmpty()) {

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

                        programmingViewHolder.cmnt2_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom2_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom2_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom2_ts())).matches("just now")) {
                                programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                    } else {
                        programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                    }

                    ProgrammingViewHolder.comment_layout.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(getFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout1.setOnClickListener(v-> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(getFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout2.setOnClickListener(v-> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(getFragmentManager(), "CommentsSheet");
                    });
                }
                else {
                    programmingViewHolder.comment_layout.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout1.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                }

                ////////POST MENU///////
                programmingViewHolder.menuPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                            postMenuDialog = new BottomSheetDialog(requireActivity());
                            postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                            postMenuDialog.setCanceledOnTouchOutside(TRUE);

                            postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);
//                            postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);
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
//

                            postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                                builder.setTitle("Are you sure?")
                                        .setMessage("Post will be deleted permanently")
                                        .setPositiveButton("Delete", (dialog, which) -> {
                                            progressDialog =new ProgressDialog(requireActivity()) ;
                                            progressDialog.setTitle("Deleting Post");
                                            progressDialog.setMessage("Please wait...");
                                            progressDialog.setCancelable(false);
                                            progressDialog.show();
                                            FirebaseFirestore.getInstance()
                                                    .collection("Feeds/").document(currentItem
                                                    .getDocID()).delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            CommitteeFragment.changed=1;
                                                            programmingViewHolder.itemHome.setVisibility(View.GONE);
                                                            notifyDataSetChanged();
                                                            FirebaseFirestore.getInstance()
                                                                    .collection("Feeds/")
                                                                    .whereEqualTo("uid", my_uid)
                                                                    .orderBy("ts", Query.Direction.DESCENDING)
                                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if(task.isSuccessful()) {
                                                                        if(task.getResult().size() == 0) {
                                                                            noneImage.setVisibility(View.VISIBLE);
                                                                        }
                                                                        else {
                                                                            noneImage.setVisibility(View.GONE);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                            postMenuDialog.dismiss();

                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                        .setCancelable(true)
                                        .show();

                            });

                            postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FirebaseFirestore.getInstance()
                                            .collection("Feeds/").document(currentItem.getDocID())
                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    BasicUtility.showToast(requireActivity(),"Post has been reported.");
                                                }
                                            });
                                    postMenuDialog.dismiss();

                                }
                            });
                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.show();

                        }
                        else {
                            postMenuDialog = new BottomSheetDialog(requireActivity());
                            postMenuDialog.setContentView(R.layout.dialog_post_menu);
                            postMenuDialog.setCanceledOnTouchOutside(TRUE);

                            postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);

                            postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FirebaseFirestore.getInstance()
                                            .collection("Feeds/").document(currentItem.getDocID())
                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    BasicUtility.showToast(requireActivity(),"Post has been reported.");
                                                }
                                            });
                                    postMenuDialog.dismiss();

                                }
                            });
                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.show();

                        }
                    }
                });
                ////////POST MENU///////

            }

            @Override
            public int getItemViewType(int position) {
                // Just as an example, return 0 or 2 depending on position
                // Note that unlike in ListView adapters, types don't have to be contiguous
                return position;
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR: BasicUtility.showToast(requireActivity(), "Something went wrong..."); break;
                    case LOADING_MORE: progressmoreposts.setVisibility(View.VISIBLE); break;
                    case LOADED: progressmoreposts.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED: contentprogressposts.setVisibility(View.GONE);
                        progressmoreposts.setVisibility(View.GONE);
                        if(adapter1.getItemCount() == 0) {
                            noneImage.setVisibility(View.VISIBLE);
                        }
                        else {
                            noneImage.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        };

        contentprogressposts.setVisibility(View.GONE);
        progressmoreposts.setVisibility(View.GONE);

        mRecyclerView.setAdapter(adapter1);



    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        public static TextView commentCount;
        public static LinearLayout comment_layout;

        private TextView  username, likesCount, text_content, minsago, writecomment;
        private ImageView userimage, like, commentimg,profileimage, menuPost, share;
        private ApplexLinkPreview LinkPreview;
        private RecyclerView tagList;
        private LinearLayout itemHome, like_layout, commentLayout1, commentLayout2, postHolder, profile_header;
        private SliderView sliderView;
        private ImageView dp_cmnt1, dp_cmnt2;
        private TextView cmnt1, cmnt2, cmnt1_minsago, cmnt2_minsago, name_cmnt1, name_cmnt2;

        private View view1, view2;
        com.applex.utsav.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;
        LottieAnimationView dhak_anim;
        RelativeLayout rlLayout;


        public ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);

            tagList = itemView.findViewById(R.id.tagsList);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage =itemView.findViewById(R.id.user_image);
            sliderView = itemView.findViewById(R.id.post_image);
            minsago=itemView.findViewById(R.id.mins_ago);
            like = itemView.findViewById(R.id.like);
//            comName = itemView.findViewById(R.id.comName);
            commentimg = itemView.findViewById(R.id.comment);
            profileimage = itemView.findViewById(R.id.profile_image);
            menuPost = itemView.findViewById(R.id.delete_post);
            writecomment =itemView.findViewById(R.id.write_comment);
            itemHome =itemView.findViewById(R.id.item_home);
            share =itemView.findViewById(R.id.share);
            LinkPreview =itemView.findViewById(R.id.LinkPreView);

//            view1 = itemView.findViewById(R.id.view1);
//            view2 = itemView.findViewById(R.id.view2);
            postHolder = itemView.findViewById(R.id.post);
            profile_header = itemView.findViewById(R.id.profile_header);
            dhak_anim = itemView.findViewById(R.id.dhak_anim);
            rlLayout = itemView.findViewById(R.id.rlLayout);

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

        }
    }

    @Override
    public void onResume() {
        if(change > 0 || delete > 0) {
            buildRecycler();
            change = 0;
            delete = 0;
        }
        super.onResume();
    }
}