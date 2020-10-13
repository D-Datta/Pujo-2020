package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.style.URLSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.adapters.ProfileAdapter;
import com.applex.utsav.fragments.Fragment_Posts;
import com.applex.utsav.fragments.Fragment_Posts_Users;
import com.applex.utsav.fragments.Fragment_Reels;
import com.applex.utsav.fragments.Fragment_Reels_Users;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.adapters.SliderAdapter;
import com.applex.utsav.adapters.TagAdapter;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.fragments.CommitteeFragment;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.CommentModel;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.IndividualModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.StoreTemp;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ActivityProfileUser extends AppCompatActivity {

    private Button editProfile;
//    private LinearLayout emptyLayout;
//    private FloatingActionButton floatingActionButton;
    private ProgressDialog progressDialog;
    ///////////////POSTS////////////////
//    private SwipeRefreshLayout swipeRefreshLayout;
//    private Dialog postMenuDialog;
//    private FirestorePagingAdapter adapter1;

    private int imageCoverOrDp = 0; //dp = 0, cover = 1
    private int from = 0; //general = 0, item = 1
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri filePath;
    byte[] pic;
    private FirebaseUser fireuser;

    ///////////////POSTS////////////////
//    private ProgressBar contentProgress;
    public static int delete = 0;
    public static int change = 0;
//    private RecyclerView mRecyclerView;
//    private ProgressBar progressMore;
    private DocumentReference documentReference;
    private BaseUserModel userModel;
    ///////////////POSTS////////////////

    //////////////NO POSTS///////////////
    private TextView PName,Pcity, aboutheading;
    private ImageView PDp, nopost1,PCoverpic, edit_dp, edit_cover;
    private com.borjabravo.readmoretextview.ReadMoreTextView Pabout;

    //////////////NO POSTS///////////////
    private String my_uid, link;
    int bool;

    private IntroPref introPref;

    ///Current user details from intropref
    private String USERNAME, PROFILEPIC, COVERPIC, FirstName, LastName, UserName, ABOUT, Userprofilepic;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPref = new IntroPref(this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        /////////////////DAY OR NIGHT MODE///////////////////
        FirebaseFirestore.getInstance().document("Mode/night_mode").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        boolean night_mode = Boolean.getBoolean(
                                Objects.requireNonNull(Objects.requireNonNull(task.getResult()).get("night_mode")).toString());
                        if(night_mode) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                });
        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_profile_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

//        contentProgress = findViewById(R.id.content_progress);
//        progressMore = findViewById(R.id.progress_more);
        editProfile = findViewById(R.id.edit_profile);


        fireuser = FirebaseAuth.getInstance().getCurrentUser();
        tabLayout = findViewById(R.id.tabBar);
        viewPager = findViewById(R.id.viewPager);


        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////
        if(getIntent() != null && getIntent().getStringExtra("uid")!=null){
            my_uid = getIntent().getStringExtra("uid");
            if(!my_uid.matches(fireuser.getUid())){
                bool =1;//ANOTHER USER ACCOUNT
            }
        }
        else{
            my_uid = fireuser.getUid();
            bool = 0;//CURRENT USER ACCOUNT
        }
        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0);
        tabLayout.getTabAt(1);

        //////////////RECYCLER VIEW////////////////////
        /////////////SETUP//////////////
//        contentProgress.setVisibility(View.VISIBLE);
//        mRecyclerView.setHasFixedSize(false);
//        final LinearLayoutManager layoutManager = new LinearLayoutManager(ActivityProfileUser.this);
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(layoutManager);
//        mRecyclerView.setNestedScrollingEnabled(true);
//        mRecyclerView.setItemViewCacheSize(10);

//        buildRecycler();

        /////////////SETUP//////////////
        Userprofilepic =  introPref.getUserdp();
        USERNAME = introPref.getFullName();
        ///////////////RECYCLER VIEW////////////////////


        /////////////App BAR MENu//////////////

        PDp = findViewById(R.id.Pdp);
        PName = findViewById(R.id.Profilename);
        Pcity = findViewById(R.id.Pcity);
        PCoverpic = findViewById(R.id.coverpic);
        Pabout = findViewById(R.id.detaildesc);
        aboutheading = findViewById(R.id.about);
        edit_dp = findViewById(R.id.edit_dp_icon_ind);
        edit_cover = findViewById(R.id.edit_coverpic_icon_ind);
//        nopost1 = findViewById(R.id.none_image);
        loadUserDetails();

        /////////////App BAR MENu//////////////


//        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
//                getResources().getColor(R.color.purple));
//        swipeRefreshLayout.setOnRefreshListener(this::buildRecycler);
//
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
    private void setupViewPager(ViewPager viewPager) {
        ProfileAdapter profileAdapter = new ProfileAdapter(getSupportFragmentManager());
        profileAdapter.addFragment(Fragment_Posts_Users.newInstance(my_uid), "Posts");
        profileAdapter.addFragment(Fragment_Reels_Users.newInstance(my_uid),"Clips");

        viewPager.setAdapter(profileAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
//                Fragment_Posts.swipe = 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

//    private void buildRecycler(){
//        Query query = FirebaseFirestore.getInstance()
//                .collection("Feeds")
//                .whereEqualTo("uid", my_uid)
//                .orderBy("ts", Query.Direction.DESCENDING);
//
//        PagedList.Config config = new PagedList.Config.Builder()
//                .setInitialLoadSizeHint(10)
//                .setPageSize(10)
//                .build();
//
//        FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
//                .setLifecycleOwner(this)
//                .setQuery(query, config, new SnapshotParser<HomePostModel>() {
//                    @NonNull
//                    @Override
//                    public HomePostModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
//                        HomePostModel newPostModel = new HomePostModel();
//                        if(snapshot.exists()) {
//                            newPostModel = snapshot.toObject(HomePostModel.class);
//                            newPostModel.setDocID(snapshot.getId());
//                        }
//                        return newPostModel;
//                    }
//                })
//                .build();
//
//        adapter1 = new FirestorePagingAdapter<HomePostModel, RecyclerView.ViewHolder>(options) {
//            @NonNull
//            @Override
//            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
//                    LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
//                    View v = layoutInflater.inflate(R.layout.item_profile_info, viewGroup, false);
//                    return new ProgrammingViewHolder(v);
//            }
//
//            @SuppressLint("SetTextI18n")
//            @Override
//            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull HomePostModel currentItem) {
//
//                ProgrammingViewHolder programmingViewHolder = (ProgrammingViewHolder) holder;
//
//                DocumentReference likeStore;
//                String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
//                programmingViewHolder.minsago.setText(timeAgo);
//                if (timeAgo != null) {
//                    if (timeAgo.matches("just now")) {
//                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
//                    } else {
//                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#aa212121"));
//                    }
//                }
//
//                ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////
//                likeStore = FirebaseFirestore.getInstance()
//                        .document("Feeds/"+currentItem.getDocID()+"/");
//
//                programmingViewHolder.menuPost.setVisibility(View.VISIBLE);
//                ///////////SET DOCUMENT REFERENCE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////
//
//
//                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
//                if (Userprofilepic != null) {
//                    Picasso.get().load(Userprofilepic).fit().centerCrop()
//                            .placeholder(R.drawable.ic_account_circle_black_24dp)
//                            .into(programmingViewHolder.profileimage);
//                } else {
//                    programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                }
//
//                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
//
//                ///////////TAGLIST///////////////
//                ///////////TAG RECYCLER SETUP////////////////
//                programmingViewHolder.tagList.setHasFixedSize(false);
//                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                programmingViewHolder.tagList.setNestedScrollingEnabled(true);
//                programmingViewHolder.tagList.setLayoutManager(linearLayoutManager);
//                ///////////TAG RECYCLER SETUP////////////////
//                if(currentItem.getTagL()!=null && currentItem.getTagL().size()>0 ) {
//                    programmingViewHolder.tagList.setVisibility(View.VISIBLE);
//                    TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL() , ActivityProfileUser.this);
//                    programmingViewHolder.tagList.setAdapter(tagAdapter);
//                }
//                else {
//                    programmingViewHolder.tagList.setAdapter(null);
//                    programmingViewHolder.tagList.setVisibility(View.GONE);
//                }
//                /////////TAGLIST///////////////
//
//
//                //////////////LOADING USERNAME AND USERDP FROM USERNODE FOR CURRENT POST USER///////////////
//                    ////////////NORMAL POST///////////////
//                if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
//                    Picasso.get().load(currentItem.getDp()).fit().centerCrop()
//                            .placeholder(R.drawable.ic_account_circle_black_24dp)
//                            .into(programmingViewHolder.userimage, new Callback() {
//                                @Override
//                                public void onSuccess() { }
//
//                                @Override
//                                public void onError(Exception e) {
//                                    programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                                }
//                            });
//                } else {
//                    programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                }
//
//                programmingViewHolder.username.setText(currentItem.getUsN());
//
//                if(currentItem.getTxt()==null || currentItem.getTxt().isEmpty()){
//                    programmingViewHolder.text_content.setVisibility(View.GONE);
//                    programmingViewHolder.LinkPreview.setVisibility(View.GONE);
//                    programmingViewHolder.text_content.setText(null);
//                }
//                else{
//                    programmingViewHolder.text_content.setVisibility(View.VISIBLE);
//                    programmingViewHolder.text_content.setText(currentItem.getTxt());
//                    if(programmingViewHolder.text_content.getUrls().length>0){
//                        URLSpan urlSnapItem = programmingViewHolder.text_content.getUrls()[0];
//                        String url = urlSnapItem.getURL();
//                        if(url.contains("http")){
//                            programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
//                            programmingViewHolder.LinkPreview.setLink(url ,new ViewListener() {
//                                @Override
//                                public void onSuccess(boolean status) {
//                                }
//
//                                @Override
//                                public void onError(Exception e) {
//                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            //do stuff like remove view etc
//                                            programmingViewHolder.LinkPreview.setVisibility(View.GONE);
//                                        }
//                                    });
//                                }
//                            });
//                        }
//
//                    } else {
//                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
//                    }
//
//                }
////
////                        String postimage_url = currentItem.getImg();
////                        if(postimage_url!=null){
////                            programmingViewHolder.postimage.setVisibility(View.VISIBLE);
////                            Picasso.get().load(postimage_url)
////                                    .placeholder(R.drawable.image_background_grey)
////                                    .memoryPolicy(MemoryPolicy.NO_STORE)
////                                    .into(programmingViewHolder.postimage);
////
////                            programmingViewHolder.postimage.setOnLongClickListener(v -> {
////
////                                Picasso.get().load(postimage_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(new Target() {
////                                    @Override
////                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
////                                        save_Dialog(bitmap);
////                                    }
////                                    @Override
////                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
////                                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
////                                    }
////                                    @Override
////                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
////
////                                    }
////
////                                });
////                                return true;
////                            });
////                        }
////                        else
////                            programmingViewHolder.postimage.setVisibility(View.GONE);
//                if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
//                    programmingViewHolder.rlLayout.setVisibility(View.VISIBLE);
//                    programmingViewHolder.sliderView.setVisibility(View.VISIBLE);
//                    programmingViewHolder.sliderView.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
//                    programmingViewHolder.sliderView.setIndicatorRadius(5);
//                    programmingViewHolder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
//                    programmingViewHolder.sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
//                    programmingViewHolder.sliderView.setIndicatorSelectedColor(Color.WHITE);
//                    programmingViewHolder.sliderView.setIndicatorUnselectedColor(R.color.colorAccent);
//                    programmingViewHolder.sliderView.setAutoCycle(false);
//
//                    SliderAdapter sliderAdapter = new SliderAdapter(ActivityProfileUser.this, currentItem.getImg(),currentItem, programmingViewHolder.sliderView);
//                    programmingViewHolder.sliderView.setSliderAdapter(sliderAdapter);
//
//                    programmingViewHolder.text_content.setOnClickListener(v -> {
//                        Intent intent = new Intent(ActivityProfileUser.this, ViewMoreHome.class);
//                        intent.putExtra("username", currentItem.getUsN());
//                        intent.putExtra("userdp", currentItem.getDp());
//                        intent.putExtra("docID", currentItem.getDocID());
//                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
//                        intent.putExtra("comName", currentItem.getComName());
//                        intent.putExtra("comID", currentItem.getComID());
//                        intent.putExtra("likeL", currentItem.getLikeL());
//                        if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
//                            Bundle args = new Bundle();
//                            args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
//                            intent.putExtra("BUNDLE", args);
//                        }
//                        intent.putExtra("postText", currentItem.getTxt());
//                        intent.putExtra("bool", "3");
//                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
//                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
//                        intent.putExtra("uid", currentItem.getUid());
//                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
//                        intent.putExtra("type", currentItem.getType());
//                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ActivityProfileUser.this,
//                                programmingViewHolder.sliderView, Objects.requireNonNull(ViewCompat.getTransitionName(programmingViewHolder.sliderView)));
//                        startActivity(intent, optionsCompat.toBundle());
//                    });
//                }
//                else {
//                    programmingViewHolder.rlLayout.setVisibility(View.GONE);
//                    programmingViewHolder.sliderView.setVisibility(View.GONE);
//                    programmingViewHolder.text_content.setOnClickListener(v -> {
//                        Intent intent = new Intent(ActivityProfileUser.this, ViewMoreText.class);
//                        intent.putExtra("username", currentItem.getUsN());
//                        intent.putExtra("userdp", currentItem.getDp());
//                        intent.putExtra("docID", currentItem.getDocID());
//                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
//                        intent.putExtra("comName", currentItem.getComName());
//                        intent.putExtra("comID", currentItem.getComID());
//                        intent.putExtra("likeL", currentItem.getLikeL());
//                        if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
//                            Bundle args = new Bundle();
//                            args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
//                            intent.putExtra("BUNDLE", args);
//                        }
//                        intent.putExtra("postText", currentItem.getTxt());
//                        intent.putExtra("bool", "3");
//                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
//                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
//                        intent.putExtra("uid", currentItem.getUid());
//                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
//                        intent.putExtra("type", currentItem.getType());
//                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ActivityProfileUser.this,
//                                programmingViewHolder.sliderView, Objects.requireNonNull(ViewCompat.getTransitionName(programmingViewHolder.sliderView)));
//                        startActivity(intent, optionsCompat.toBundle());
////                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2);
////                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
//                    });
//                }
//
//                //////////////////////////TEXT & IMAGE FOR POST//////////////////////
//
//                programmingViewHolder.like_layout.setOnClickListener(v -> {
//                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", currentItem.getDocID());
//                    bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
//                });
//
//                ///////////////////FLAMES AND COMMENTS///////////////////////
//
//                //INITIAL SETUP//
//                if(currentItem.getLikeL() != null){
//                    /////////////////UPDATNG FLAMED BY NO.//////////////////////
//                    if (currentItem.getLikeL().size() == 0) {
//                        programmingViewHolder.like_layout.setVisibility(View.GONE);
//                    }
//                    else {
//                        programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
//                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size()));
//                    }
//
//                    for (int j = 0; j < currentItem.getLikeL().size(); j++) {
//                        if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
//                            programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
//                            currentItem.setLikeCheck(j);
////                            if ((currentItem.getLikeL().size() - 1) == 1)
////                                feedViewHolder.flamedBy.setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " other");
////                            else if ((currentItem.getLikeL().size() - 1) == 0) {
////                                feedViewHolder.flamedBy.setText("Flamed by you");
////                            } else
////                                feedViewHolder.flamedBy.setText("Flamed by you & " + (currentItem.getLikeL().size() - 1) + " others");
//                            //Position in likeList where the current USer UId is found stored in likeCheck
//                        }
//                    }
//                }
//                else{
//                    programmingViewHolder.like_layout.setVisibility(View.GONE);
//                }
//                //INITIAL SETUP//
//
//
//                PushDownAnim.setPushDownAnimTo(programmingViewHolder.like)
//                        .setScale(PushDownAnim.MODE_STATIC_DP, 6)
//                        .setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                if(currentItem.getLikeCheck() >= 0){//was already liked by current user
//                                    programmingViewHolder.like.setImageResource(R.drawable.ic_btmnav_notifications);
//                                    if (currentItem.getLikeL().size() - 1 == 0) {
//                                        programmingViewHolder.like_layout.setVisibility(View.GONE);
//                                    } else{
//                                        programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
//                                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() - 1));
//                                    }
//                                    ///////////REMOVE CURRENT USER LIKE/////////////
//                                    currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
//                                    currentItem.setLikeCheck(-1);
//
//                                    //                likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
//
//                                    ///////////////////BATCH WRITE///////////////////
//                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
//
//                                    DocumentReference flamedDoc = likeStore.collection("flameL")
//                                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
//                                    batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
//                                    batch.delete(flamedDoc);
//
//                                    batch.commit().addOnSuccessListener(task -> {
//
//                                    });
//                                    ///////////////////BATCH WRITE///////////////////
//                                }
//                                else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
//                                    BasicUtility.vibrate(getApplicationContext());
//                                    programmingViewHolder.dhak_anim.setVisibility(View.VISIBLE);
//                                    programmingViewHolder.dhak_anim.playAnimation();
//                                    try {
//                                        AssetFileDescriptor afd = getAssets().openFd("dhak.mp3");
//                                        MediaPlayer player = new MediaPlayer();
//                                        player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
//                                        player.prepare();
//                                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                                        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
//                                            player.start();
//                                            if(!player.isPlaying()) {
//                                                programmingViewHolder.dhak_anim.cancelAnimation();
//                                                programmingViewHolder.dhak_anim.setVisibility(View.GONE);
//                                            }
//                                            player.setOnCompletionListener(mediaPlayer -> {
//                                                programmingViewHolder.dhak_anim.cancelAnimation();
//                                                programmingViewHolder.dhak_anim.setVisibility(View.GONE);
//                                            });
//                                        } else {
//                                            new Handler().postDelayed(() -> {
//                                                programmingViewHolder.dhak_anim.cancelAnimation();
//                                                programmingViewHolder.dhak_anim.setVisibility(View.GONE);
//                                            }, 2000);
//                                        }
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
//                                    programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
//                                    if (currentItem.getLikeL() != null){
//                                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() + 1));
//                                    }
//                                    else{
//                                        programmingViewHolder.likesCount.setText("1");
//                                    }
//
//
//                                    //////////////ADD CURRENT USER TO LIKELIST//////////////////
//                                    currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
//                                    currentItem.setLikeCheck(currentItem.getLikeL().size()-1);
//                                    //For local changes current item like added to remote list end
//
//                                    ///////////////////BATCH WRITE///////////////////
//                                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
//                                    FlamedModel flamedModel = new FlamedModel();
//                                    long tsLong = System.currentTimeMillis();
//
//                                    flamedModel.setPostID(currentItem.getDocID());
//                                    flamedModel.setTs(tsLong);
//                                    flamedModel.setType(introPref.getType());
//                                    flamedModel.setUid(FirebaseAuth.getInstance().getUid());
//                                    flamedModel.setUserdp(Userprofilepic);
//                                    flamedModel.setUsername(USERNAME);
//                                    flamedModel.setPostUid(currentItem.getUid());
//
//                                    DocumentReference flamedDoc = likeStore.collection("flameL")
//                                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
//                                    batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
//                                    batch.set(flamedDoc, flamedModel);
//                                    if(currentItem.getLikeL().size() % 5 == 0){
//                                        batch.update(likeStore,"newTs", tsLong);
//                                    }
//                                    batch.commit().addOnSuccessListener(task -> {
//
//                                    });
//                                    ///////////////////BATCH WRITE///////////////////
//                                }
//                            }
//                        });
//
//                programmingViewHolder.commentimg.setOnClickListener(v -> {
//                    BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 1, "ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
//                    bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
//                });
//
//                programmingViewHolder.writecomment.setOnClickListener(v -> {
//                    BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 1,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
//                    bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
//                });
//
//                programmingViewHolder.share.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if(currentItem.getImg() != null && currentItem.getImg().size()>0)
//                            link = "https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
//                        else
//                            link = "https://www.applex.in/utsav-app/feeds/" + "0/" + currentItem.getDocID();
//                        Intent i = new Intent();
//                        i.setAction(Intent.ACTION_SEND);
//                        i.putExtra(Intent.EXTRA_TEXT, link);
//                        i.setType("text/plain");
//                        startActivity(Intent.createChooser(i, "Share with"));
//                    }
//                });
//
//                if (currentItem.getCmtNo() > 0) {
//                    ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
//                    ProgrammingViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));
//
//                    if(currentItem.getCom1() != null && !currentItem.getCom1().isEmpty()) {
//                        programmingViewHolder.commentLayout1.setVisibility(View.VISIBLE);
//                        programmingViewHolder.name_cmnt1.setText(currentItem.getCom1_usn());
//                        Picasso.get().load(currentItem.getCom1_dp())
//                                .placeholder(R.drawable.ic_account_circle_black_24dp)
//                                .into(programmingViewHolder.dp_cmnt1);
//
//                        programmingViewHolder.cmnt1.setText(currentItem.getCom1());
//                        if (programmingViewHolder.cmnt1.getUrls().length > 0) {
//                            URLSpan urlSnapItem = programmingViewHolder.cmnt1.getUrls()[0];
//                            String url = urlSnapItem.getURL();
//                            if (url.contains("http")) {
//                                programmingViewHolder.link_preview1.setVisibility(View.VISIBLE);
//                                programmingViewHolder.link_preview1.setLink(url, new ViewListener() {
//                                    @Override
//                                    public void onSuccess(boolean status) {
//                                    }
//
//                                    @Override
//                                    public void onError(Exception e) {
//                                        new Handler(Looper.getMainLooper()).post(() -> {
//                                            //do stuff like remove view etc
//                                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
//                                        });
//                                    }
//                                });
//                            }
//                        } else {
//                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
//                        }
//
//                        programmingViewHolder.cmnt1_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom1_ts()));
//                        if (BasicUtility.getTimeAgo(currentItem.getCom1_ts()) != null) {
//                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom1_ts())).matches("just now")) {
//                                programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#00C853"));
//                            } else {
//                                programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#aa212121"));
//                            }
//                        }
//                    } else {
//                        programmingViewHolder.commentLayout1.setVisibility(View.GONE);
//                    }
//
//                    if(currentItem.getCom2() != null && !currentItem.getCom2().isEmpty()) {
//                        programmingViewHolder.commentLayout2.setVisibility(View.VISIBLE);
//                        programmingViewHolder.name_cmnt2.setText(currentItem.getCom2_usn());
//                        Picasso.get().load(currentItem.getCom2_dp())
//                                .placeholder(R.drawable.ic_account_circle_black_24dp)
//                                .into(programmingViewHolder.dp_cmnt2);
//
//                        programmingViewHolder.cmnt2.setText(currentItem.getCom2());
//                        if (programmingViewHolder.cmnt2.getUrls().length > 0) {
//                            URLSpan urlSnapItem = programmingViewHolder.cmnt2.getUrls()[0];
//                            String url = urlSnapItem.getURL();
//                            if (url.contains("http")) {
//                                programmingViewHolder.link_preview1.setVisibility(View.VISIBLE);
//                                programmingViewHolder.link_preview1.setLink(url, new ViewListener() {
//                                    @Override
//                                    public void onSuccess(boolean status) { }
//
//                                    @Override
//                                    public void onError(Exception e) {
//                                        new Handler(Looper.getMainLooper()).post(() -> {
//                                            //do stuff like remove view etc
//                                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
//                                        });
//                                    }
//                                });
//                            }
//                        } else {
//                            programmingViewHolder.link_preview1.setVisibility(View.GONE);
//                        }
//
//                        programmingViewHolder.cmnt2_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom2_ts()));
//                        if (BasicUtility.getTimeAgo(currentItem.getCom2_ts()) != null) {
//                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom2_ts())).matches("just now")) {
//                                programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#00C853"));
//                            } else {
//                                programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#aa212121"));
//                            }
//                        }
//                    } else {
//                        programmingViewHolder.commentLayout2.setVisibility(View.GONE);
//                    }
//
//                    ProgrammingViewHolder.comment_layout.setOnClickListener(v -> {
//                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
//                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
//                    });
//
//                    programmingViewHolder.commentLayout1.setOnClickListener(v-> {
//                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
//                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
//                    });
//
//                    programmingViewHolder.commentLayout2.setOnClickListener(v-> {
//                        BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Feeds", currentItem.getDocID(), currentItem.getUid(), 2,"ActivityProfileUser", null,currentItem.getCmtNo(), null, null);
//                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
//                    });
//                }
//                else {
//                    ProgrammingViewHolder.comment_layout.setVisibility(View.GONE);
//                    programmingViewHolder.commentLayout1.setVisibility(View.GONE);
//                    programmingViewHolder.commentLayout2.setVisibility(View.GONE);
//                }
//
//                ////////POST MENU///////
//                programmingViewHolder.menuPost.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if( currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
//                            postMenuDialog = new BottomSheetDialog(ActivityProfileUser.this);
//                            postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
//                            postMenuDialog.setCanceledOnTouchOutside(TRUE);
//
//                            postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);
//                            postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);
////                            postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
////                                CommitteeFragment.changed=1;
////                                Intent i= new Intent(getApplicationContext(),NewPostHome.class);
////                                i.putExtra("target","100"); //target value for edit post
////                                i.putExtra("bool", Integer.toString(bool));
////                                i.putExtra("usN", currentItem.getUsN());
////                                i.putExtra("dp", currentItem.getDp());
////                                i.putExtra("uid", currentItem.getUid());
////                                i.putExtra("type", currentItem.getType());
////                                if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
////                                    Bundle args = new Bundle();
////                                    args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
////                                    i.putExtra("BUNDLE", args);
////                                }
////                                i.putExtra("txt", currentItem.getTxt());
////                                i.putExtra("comID", currentItem.getComID());
////                                i.putExtra("comName", currentItem.getComName());
////                                i.putExtra("ts", Long.toString(currentItem.getTs()));
////                                i.putExtra("newTs", Long.toString(currentItem.getNewTs()));
////                                StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
////                                i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));
////                                i.putExtra("likeL", currentItem.getLikeL());
////                                i.putExtra("likeCheck", currentItem.getLikeCheck());
////                                i.putExtra("docID", currentItem.getDocID());
////                                i.putExtra("reportL", currentItem.getReportL());
////                                i.putExtra("challengeID", currentItem.getChallengeID());
////                                startActivity(i);
////                                postMenuDialog.dismiss();
////
////                            });
//
//                            postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileUser.this);
//                                builder.setTitle("Are you sure?")
//                                        .setMessage("Post will be deleted permanently")
//                                        .setPositiveButton("Delete", (dialog, which) -> {
//                                            progressDialog =new ProgressDialog(ActivityProfileUser.this) ;
//                                            progressDialog.setTitle("Deleting Post");
//                                            progressDialog.setMessage("Please wait...");
//                                            progressDialog.setCancelable(false);
//                                            progressDialog.show();
//                                            FirebaseFirestore.getInstance()
//                                                    .collection("Feeds/").document(currentItem
//                                                    .getDocID()).delete()
//                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//                                                            CommitteeFragment.changed=1;
//                                                            programmingViewHolder.itemHome.setVisibility(View.GONE);
//                                                            notifyDataSetChanged();
//                                                            FirebaseFirestore.getInstance()
//                                                                    .collection("Feeds/")
//                                                                    .whereEqualTo("uid", my_uid)
//                                                                    .orderBy("ts", Query.Direction.DESCENDING)
//                                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                                @Override
//                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                                    if(task.isSuccessful()) {
//                                                                        if(task.getResult().size() == 0) {
//                                                                            nopost1.setVisibility(View.VISIBLE);
//                                                                        }
//                                                                        else {
//                                                                            nopost1.setVisibility(View.GONE);
//                                                                        }
//                                                                    }
//                                                                }
//                                                            });
//                                                            progressDialog.dismiss();
//                                                        }
//                                                    });
//                                            postMenuDialog.dismiss();
//
//                                        })
//                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                                        .setCancelable(true)
//                                        .show();
//
//                            });
//
//                            postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    FirebaseFirestore.getInstance()
//                                            .collection("Feeds/").document(currentItem.getDocID())
//                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    BasicUtility.showToast(getApplicationContext(),"Post has been reported.");
//                                                }
//                                            });
//                                    postMenuDialog.dismiss();
//
//                                }
//                            });
//                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                            postMenuDialog.show();
//
//                        }
//                        else {
//                            postMenuDialog = new BottomSheetDialog(ActivityProfileUser.this);
//                            postMenuDialog.setContentView(R.layout.dialog_post_menu);
//                            postMenuDialog.setCanceledOnTouchOutside(TRUE);
//
//                            postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);
//
//                            postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    FirebaseFirestore.getInstance()
//                                            .collection("Feeds/").document(currentItem.getDocID())
//                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    BasicUtility.showToast(getApplicationContext(),"Post has been reported.");
//                                                }
//                                            });
//                                    postMenuDialog.dismiss();
//
//                                }
//                            });
//                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                            postMenuDialog.show();
//
//                        }
//                    }
//                });
//                ////////POST MENU///////
//
//            }
//
//            @Override
//            public int getItemViewType(int position) {
//                // Just as an example, return 0 or 2 depending on position
//                // Note that unlike in ListView adapters, types don't have to be contiguous
//                return position;
//            }
//
//            @Override
//            protected void onLoadingStateChanged(@NonNull LoadingState state) {
//
//                super.onLoadingStateChanged(state);
//                switch (state) {
//                    case ERROR: BasicUtility.showToast(getApplicationContext(), "Something went wrong..."); break;
//                    case LOADING_MORE: progressMore.setVisibility(View.VISIBLE); break;
//                    case LOADED: progressMore.setVisibility(View.GONE);
//                        if(swipeRefreshLayout.isRefreshing()) {
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
//                        break;
//                    case FINISHED: contentProgress.setVisibility(View.GONE);
//                        progressMore.setVisibility(View.GONE);
//                        if(adapter1.getItemCount() == 0) {
//                            loadUserDetails();
//                            emptyLayout.setVisibility(View.VISIBLE);
//                        }
//                        else {
//                            emptyLayout.setVisibility(View.GONE);
//                        }
//                        break;
//                }
//            }
//        };
//
//        contentProgress.setVisibility(View.GONE);
//        progressMore.setVisibility(View.GONE);
//
//        mRecyclerView.setAdapter(adapter1);
//
//
//
//    }

//    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{
//
//        public static TextView commentCount;
//        public static LinearLayout comment_layout;
//
//        private TextView  username, likesCount, text_content, minsago, writecomment;
//        private ImageView  userimage, like, commentimg,profileimage, menuPost, share;
//        private ApplexLinkPreview LinkPreview;
//        private RecyclerView tagList;
//        private LinearLayout itemHome, like_layout, commentLayout1, commentLayout2, postHolder, profile_header;
//        private SliderView sliderView;
//        private ImageView dp_cmnt1, dp_cmnt2;
//        private TextView cmnt1, cmnt2, cmnt1_minsago, cmnt2_minsago, name_cmnt1, name_cmnt2;
//
//        private View view1, view2;
//        com.applex.utsav.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;
//        LottieAnimationView dhak_anim;
//        RelativeLayout rlLayout;
//
//
//        public ProgrammingViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            tagList = itemView.findViewById(R.id.tagsList);
//            username = itemView.findViewById(R.id.username);
//            text_content = itemView.findViewById(R.id.text_content);
//            userimage =itemView.findViewById(R.id.user_image);
//            sliderView = itemView.findViewById(R.id.post_image);
//            minsago=itemView.findViewById(R.id.mins_ago);
//            like = itemView.findViewById(R.id.like);
////            comName = itemView.findViewById(R.id.comName);
//            commentimg = itemView.findViewById(R.id.comment);
//            profileimage = itemView.findViewById(R.id.profile_image);
//            menuPost = itemView.findViewById(R.id.delete_post);
//            writecomment =itemView.findViewById(R.id.write_comment);
//            itemHome =itemView.findViewById(R.id.item_home);
//            share =itemView.findViewById(R.id.share);
//            LinkPreview =itemView.findViewById(R.id.LinkPreView);
//
////            view1 = itemView.findViewById(R.id.view1);
////            view2 = itemView.findViewById(R.id.view2);
//            postHolder = itemView.findViewById(R.id.post);
//            profile_header = itemView.findViewById(R.id.profile_header);
//            dhak_anim = itemView.findViewById(R.id.dhak_anim);
//            rlLayout = itemView.findViewById(R.id.rlLayout);
//
//            likesCount = itemView.findViewById(R.id.no_of_likes);
//            commentCount = itemView.findViewById(R.id.no_of_comments);
//            like_layout = itemView.findViewById(R.id.like_layout);
//            comment_layout = itemView.findViewById(R.id.comment_layout);
//
//            commentLayout1 = itemView.findViewById(R.id.comment_layout1);
//            name_cmnt1 = itemView.findViewById(R.id.comment_username1);
//            cmnt1 = itemView.findViewById(R.id.comment1);
//            cmnt1_minsago = itemView.findViewById(R.id.comment_mins_ago1);
//            dp_cmnt1 = itemView.findViewById(R.id.comment_user_dp1);
//            link_preview1 = itemView.findViewById(R.id.LinkPreViewComment1);
//
//            commentLayout2 = itemView.findViewById(R.id.comment_layout2);
//            name_cmnt2 = itemView.findViewById(R.id.comment_username2);
//            cmnt2 = itemView.findViewById(R.id.comment2);
//            cmnt2_minsago = itemView.findViewById(R.id.comment_mins_ago2);
//            dp_cmnt2 = itemView.findViewById(R.id.comment_user_dp2);
//            link_preview2 = itemView.findViewById(R.id.LinkPreViewComment2);
//
//        }
//    }

    private void loadUserDetails() {
//        nopost1.setVisibility(View.VISIBLE);
        ///////////////////////LOAD PROFILE DETAILS///////////////////////

        documentReference= FirebaseFirestore.getInstance().collection("Users")
                .document(my_uid);

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                userModel = task.getResult().toObject(BaseUserModel.class);

                                if(userModel.getCoverpic() != null){
                                    Picasso.get().load(userModel.getCoverpic())
                                            .error(R.drawable.image_background_grey)
                                            .placeholder(R.drawable.image_background_grey)
                                            .into(PCoverpic);
                                }

//                                if(userModel.getDp() != null){
//                                    Picasso.get().load(userModel.getDp())
//                                            .error(R.drawable.image_background_grey)
//                                            .placeholder(R.drawable.image_background_grey)
//                                            .into(PDp);
//                                }


                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(my_uid).collection("indi").document(my_uid)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(task.getResult().exists()){
                                                IndividualModel individualModel = task.getResult().toObject(IndividualModel.class);
                                                FirstName = individualModel.getFirstname();
                                                LastName = individualModel.getLastname();
                                                PName.setText(FirstName+" "+LastName);
                                                if(individualModel.getBio() != null && !individualModel.getBio().isEmpty()){
                                                    ABOUT = individualModel.getBio();
                                                    Pabout.setVisibility(View.VISIBLE);
                                                    aboutheading.setVisibility(View.VISIBLE);
                                                    Pabout.setText(ABOUT);
                                                }
                                                else{
                                                    Pabout.setVisibility(View.GONE);
                                                    aboutheading.setVisibility(View.GONE);
                                                }

                                            }

                                        }
                                    }
                                });

                                UserName = userModel.getName();
                                if(userModel.getCity()!=null || userModel.getState()!=null){

                                    if((userModel.getCity()!=null && userModel.getCity().isEmpty())
                                            && (userModel.getState()!=null && !userModel.getState().isEmpty())){
                                        Pcity.setText(userModel.getState());
                                    }
                                    else if((userModel.getState()!=null && userModel.getState().isEmpty())
                                            && (userModel.getCity()!=null && !userModel.getCity().isEmpty())){
                                        Pcity.setText(userModel.getCity());
                                    }
                                    else if((userModel.getCity()!=null && !userModel.getCity().isEmpty())
                                            && (userModel.getState()!=null && !userModel.getState().isEmpty())){
                                        Pcity.setText(userModel.getCity()+", "+userModel.getState());
                                    }
                                    else if((userModel.getCity()!=null && userModel.getCity().isEmpty())
                                            && (userModel.getState()!=null && userModel.getState().isEmpty())){
                                        Pcity.setVisibility(View.GONE);
                                    }

                                }
                                else if(userModel.getCity()==null && userModel.getState()==null){
                                    Pcity.setVisibility(View.GONE);
                                }

                                if(userModel.getDp()!=null){
                                    PROFILEPIC = userModel.getDp();
                                    if(PROFILEPIC!=null){
                                        Picasso.get().load(PROFILEPIC).placeholder(R.drawable.ic_account_circle_black_24dp).into(PDp);
                                    }
                                }
                                else{
                                    if(userModel.getGender()!=null){
                                        if (userModel.getGender().matches("Female") || userModel.getGender().matches("মহিলা")){
                                            PDp.setImageResource(R.drawable.ic_female);
                                        }
                                        else if (userModel.getGender().matches("Male") || userModel.getGender().matches("পুরুষ")){
                                            PDp.setImageResource(R.drawable.ic_male);
                                        }
                                        else if (userModel.getGender().matches("Others") || userModel.getGender().matches("অন্যান্য")){
                                            PDp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    }
                                    else{
                                        PDp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                }

//                                else{
//                                    Display display = getWindowManager().getDefaultDisplay();
//                                    int displayWidth = display.getWidth();
//                                    BitmapFactory.Options options = new BitmapFactory.Options();
//                                    options.inJustDecodeBounds = true;
//                                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_account_circle_black_24dp, options);
//                                    int width = options.outWidth;
//                                    if (width > displayWidth) {
//                                        int widthRatio = Math.round((float) width / (float) displayWidth);
//                                        options.inSampleSize = widthRatio;
//                                    }
//                                    options.inJustDecodeBounds = false;
//                                    Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_account_circle_black_24dp, options);
//                                    PDp.setImageBitmap(scaledBitmap);
//                                }

                                if(userModel.getCoverpic()!=null){
                                    COVERPIC = userModel.getCoverpic();
                                    Picasso.get().load(COVERPIC).placeholder(R.drawable.image_background_grey).into(PCoverpic);
                                }
                                else{
                                    Display display = getWindowManager().getDefaultDisplay();
                                    int displayWidth = display.getWidth();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;
                                    BitmapFactory.decodeResource(getResources(), R.drawable.cover_kaash, options);
                                    int width = options.outWidth;
                                    if (width > displayWidth) {
                                        int widthRatio = Math.round((float) width / (float) displayWidth);
                                        options.inSampleSize = widthRatio;
                                    }
                                    options.inJustDecodeBounds = false;
                                    Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.cover_kaash, options);
                                    PCoverpic.setImageBitmap(scaledBitmap);
                                }

                                if(my_uid.matches(FirebaseAuth.getInstance().getUid())){
                                    edit_cover.setVisibility(View.VISIBLE);
                                    edit_dp.setVisibility(View.VISIBLE);
                                    editProfile.setVisibility(View.VISIBLE);
                                    editProfile.setOnClickListener(v -> {
                                        Intent i1 = new Intent(ActivityProfileUser.this, EditProfileIndividualActivity.class);
                                        startActivity(i1);
                                        finish();
                                    });
                                    edit_cover.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!checkStoragePermission()) {
                                                requestStoragePermission();
                                            }
                                            else {
                                                imageCoverOrDp = 1; //cover
                                                from = 0; //general
                                                pickGallery();
                                            }
                                        }
                                    });
                                    edit_dp.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!checkStoragePermission()) {
                                                requestStoragePermission();
                                            }
                                            else {
                                                imageCoverOrDp = 0; //dp
                                                from = 0; //general
                                                pickGallery();
                                            }
                                        }
                                    });
                                }
                                else{
                                    edit_cover.setVisibility(View.GONE);
                                    edit_dp.setVisibility(View.GONE);
                                    editProfile.setVisibility(View.GONE);
                                }
                            }
                            else{
                                ActivityProfileUser.super.onBackPressed();
                                BasicUtility.showToast(ActivityProfileUser.this, "Profile is temporarily unavailable");
                            }

                        }
                    }

                });
        ///////////////////////LOAD PROFILE DETAILS///////////////////////

        PDp.setOnClickListener(v -> {
            if(userModel != null) {
                if (userModel.getDp() != null && userModel.getDp().length()>2) {
                    Intent intent = new Intent(ActivityProfileUser.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", userModel.getDp());
                    startActivity(intent);
                }
            }
            else {
                Toast.makeText(ActivityProfileUser.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
            }
        });

        PCoverpic.setOnClickListener(v -> {
            if(userModel != null) {
                if (userModel.getCoverpic() != null) {
                    Intent intent = new Intent(ActivityProfileUser.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", userModel.getCoverpic());
                    startActivity(intent);
                }
                else {
                    Toast.makeText(ActivityProfileUser.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ActivityProfileUser.this, storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE ) == (PackageManager.PERMISSION_GRANTED);
    }
    //////////////////////PREMISSIONS//////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data!=null){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                try {
                    filePath = data.getData();
                    if(filePath!=null) {
                        if(imageCoverOrDp == 0){
                            CropImage.activity(filePath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setAspectRatio(1,1)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(ActivityProfileUser.this);
                        }
                        else {
                            CropImage.activity(filePath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setAspectRatio(16,9)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(ActivityProfileUser.this);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new ImageCompressor(bitmap).execute();
            }
            else {//CROP ERROR
                Toast.makeText(this, "+error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickGallery(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),IMAGE_PICK_GALLERY_CODE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if(change > 0 || delete > 0) {
//            buildRecycler();
            change = 0;
            delete = 0;
        }
        super.onResume();
    }

    @SuppressLint("StaticFieldLeak")
    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private Bitmap bitmap, compressedBitmap;

        public ImageCompressor(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ActivityProfileUser.this);
            progressDialog.setTitle("Updating Profile");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        public byte[] doInBackground(Void... strings) {
            try {
                compressedBitmap = BasicUtility.decodeSampledBitmapFromFile(bitmap, 612, 816);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteArray = stream.toByteArray();
            compressedBitmap.recycle();
            return byteArray;
        }

        @Override
        protected void onPostExecute(byte[] pic) {
            if(pic!= null) {
                Bitmap bitmap1 = BitmapFactory.decodeByteArray(pic, 0 ,pic.length);
                if(imageCoverOrDp == 0 && from == 0){
                    PDp.setImageBitmap(bitmap1);
                }
                else if (imageCoverOrDp == 0 && from == 1){
                    PDp.setImageBitmap(bitmap1);
                }
                else if (imageCoverOrDp == 1 && from == 0){
                    PCoverpic.setImageBitmap(bitmap1);
                }
                else if (imageCoverOrDp == 1 && from == 1){
                    PCoverpic.setImageBitmap(bitmap1);
                }
                FirebaseStorage storage;
                StorageReference storageReference;
                StorageReference reference;
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference();

                long ts= Calendar.getInstance().getTimeInMillis();

                if(imageCoverOrDp == 1){
                    reference = storageReference.child("Users/")
                            .child("Coverpic/")
                            .child(FirebaseAuth.getInstance().getUid() + ts + "_coverpic");
                }
                else {
                    reference = storageReference.child("Users/")
                            .child("DP/")
                            .child( FirebaseAuth.getInstance().getUid() + ts + "_dp");
                }

                reference.putBytes(pic)
                        .addOnSuccessListener(taskSnapshot ->
                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String generatedFilePath = uri.toString();
                                    DocumentReference docref = FirebaseFirestore.getInstance()
                                            .collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                                    if(imageCoverOrDp == 0){
                                        docref.update("dp", generatedFilePath).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                introPref.setUserdp(generatedFilePath);
                                                progressDialog.dismiss();
                                            }else{
                                                BasicUtility.showToast(getApplicationContext(),"Something went wrong.");
                                            }
                                        });
                                    }
                                    else {
                                        docref.update("coverpic", generatedFilePath).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                            }else{
                                                BasicUtility.showToast(getApplicationContext(),"Something went wrong.");
                                            }
                                        });
                                    }

                                }))
                        .addOnFailureListener(e -> {
                            BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                            progressDialog.dismiss();

                        });
            }
        }
    }

}