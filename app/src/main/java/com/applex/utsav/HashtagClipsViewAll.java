package com.applex.utsav;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
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
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

public class HashtagClipsViewAll extends AppCompatActivity {
    IntroPref introPref;
    FirestorePagingAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerview;
    ShimmerFrameLayout shimmerFrameLayout;
    ProgressBar contentprogressposts, progressmoreposts;
    ImageView noneImage;
    BottomSheetDialog postMenuDialog;
    ProgressDialog progressDialog;
    FloatingActionButton floatingActionButton;
    String link;
    String tagName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPref = new IntroPref(this);
        String lang = introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        /////////////////DAY OR NIGHT MODE///////////////////
        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Mode/night_mode")
                    .get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(task.getResult().getBoolean("night_mode")) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        } else if(introPref.getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(introPref.getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_hashtag_clips_view_all);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("#"+ getIntent().getStringExtra("hashtag"));

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        recyclerview = findViewById(R.id.recycler_posts);
        contentprogressposts = findViewById(R.id.content_progress);
        progressmoreposts = findViewById(R.id.progress_more_posts);
        noneImage = findViewById(R.id.none_image);
        floatingActionButton = findViewById(R.id.to_the_top);

        recyclerview.setHasFixedSize(false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setItemViewCacheSize(20);

        shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        contentprogressposts.setVisibility(View.GONE);
        recyclerview.setVisibility(View.GONE);

        tagName = getIntent().getStringExtra("hashtag");
        buildRecyclerView();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.darkpurple),getResources()
                        .getColor(R.color.darkpurple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
            recyclerview.setVisibility(View.GONE);
            contentprogressposts.setVisibility(View.GONE);
            buildRecyclerView();
        });

        ///////////////Set Image Bitmap/////////////////////
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {

            Display display = getWindowManager().getDefaultDisplay();
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
            noneImage.setImageBitmap(scaledBitmap);
        } else if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {

            Display display = getWindowManager().getDefaultDisplay();
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
            noneImage.setImageBitmap(scaledBitmap);
        }
        ///////////////Set Image Bitmap/////////////////////

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(HashtagClipsViewAll.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("listener") != null && value.getBoolean("listener")) {
                                FirebaseFirestore.getInstance().document("Mode/night_mode")
                                        .get().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().getBoolean("night_mode")) {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                        } else {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                        }
                                    } else {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    }
                                    new Handler().postDelayed(() -> {
                                        MainActivity.mode_changed = 1;
                                        FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid()).update("listener", false);
                                        startActivity(new Intent(HashtagClipsViewAll.this, HashtagClipsViewAll.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }
    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Reels")
               .whereArrayContains("tagList", tagName);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
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

        adapter = new FirestorePagingAdapter<ReelsPostModel, ProgrammingViewHolder>(options) {
            @NonNull
            @Override
            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View v = layoutInflater.inflate(R.layout.item_clips, parent, false);
                return  new ProgrammingViewHolder(v);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder programmingViewHolder, int position, @NonNull ReelsPostModel currentItem) {

                programmingViewHolder.posting_item.setVisibility(View.GONE);
                programmingViewHolder.committee_item.setVisibility(View.GONE);

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
                //current user dp
                if (introPref.getUserdp() != null) {
                    Picasso.get().load(introPref.getUserdp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(programmingViewHolder.profileimage);
                }
                else{
                    if(introPref.getGender()!=null){
                        if (introPref.getGender().matches("Female") || introPref.getGender().matches("মহিলা")){
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_female);
                        }
                        else if (introPref.getGender().matches("Male") || introPref.getGender().matches("পুরুষ")){
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_male);
                        }
                        else if (introPref.getGender().matches("Others") || introPref.getGender().matches("অন্যান্য")){
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
//                        Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfileCommittee.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
//                    else {
//                        Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfileUser.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
                    Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfile.class);
                    intent.putExtra("uid", currentItem.getUid());
                    startActivity(intent);
                });

                programmingViewHolder.userimage.setOnClickListener(v -> {
//                    if(currentItem.getType().matches("com")) {
//                        Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfileCommittee.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
//                    else {
//                        Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfileUser.class);
//                        intent.putExtra("uid", currentItem.getUid());
//                        startActivity(intent);
//                    }
                    Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfile.class);
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
//                        Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfileCommittee.class);
//                        intent.putExtra("uid", currentItem.getPujoTag().getPujoUid());
//                        startActivity(intent);
//                    });
                    programmingViewHolder.pujoTagHolder.setOnClickListener(v -> {
                        //To be changed
                        Intent intent = new Intent(HashtagClipsViewAll.this, ActivityProfile.class);
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

                Picasso.get().load(currentItem.getFrame()).into(programmingViewHolder.reels_image);

                programmingViewHolder.reels_video.setOnPreparedListener(mp -> {
                    HashtagClipsViewAll.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                        Intent intent = new Intent(HashtagClipsViewAll.this, ReelsActivity.class);
                        intent.putExtra("bool", "3");
                        intent.putExtra("docID", currentItem.getDocID());
                        HashtagClipsViewAll.this.startActivity(intent);
                    });
                }
                else if(programmingViewHolder.reels_image.getVisibility() == View.VISIBLE) {
                    programmingViewHolder.reels_image.setOnClickListener(v -> {
                        Intent intent = new Intent(HashtagClipsViewAll.this, ReelsActivity.class);
                        intent.putExtra("bool", "3");
                        intent.putExtra("docID", currentItem.getDocID());
                        HashtagClipsViewAll.this.startActivity(intent);
                    });
                }

                if (currentItem.getHeadline() == null || currentItem.getHeadline().isEmpty()) {
                    programmingViewHolder.head_content.setVisibility(View.GONE);
                    programmingViewHolder.head_content.setText(null);
                }
                else {
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
                }
                else {
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
                            public void onClick(View textView)
                            {
                                Intent i = new Intent(HashtagClipsViewAll.this, HashtagPostViewAll.class);
                                i.putExtra("hashtag", programmingViewHolder.text_content.getText().toString().substring(s+1, e));
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
                    Intent intent = new Intent(HashtagClipsViewAll.this, ReelsActivity.class);
                    intent.putExtra("bool", "3");
                    intent.putExtra("docID", currentItem.getDocID());
                    startActivity(intent);
                });

                programmingViewHolder.head_content.setOnClickListener(v -> {
                    Intent intent = new Intent(HashtagClipsViewAll.this, ReelsActivity.class);
                    intent.putExtra("bool", "3");
                    intent.putExtra("docID", currentItem.getDocID());
                    startActivity(intent);
                });
                //////////////////////////TEXT & VIDEO FOR POST//////////////////////

                programmingViewHolder.like_layout.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Reels", currentItem.getDocID());
                    bottomSheetDialog.show(HashtagClipsViewAll.this.getSupportFragmentManager(), "FlamedBySheet");
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
                                BasicUtility.vibrate(HashtagClipsViewAll.this);
                                programmingViewHolder.dhak_anim.setVisibility(View.VISIBLE);
                                programmingViewHolder.dhak_anim.playAnimation();
                                try {
                                    AssetFileDescriptor afd = HashtagClipsViewAll.this.getAssets().openFd("dhak.mp3");
                                    MediaPlayer player = new MediaPlayer();
                                    player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                    player.prepare();
                                    AudioManager audioManager = (AudioManager) HashtagClipsViewAll.this.getSystemService(Context.AUDIO_SERVICE);
                                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                        player.start();
                                        if (!player.isPlaying()) {
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
                                flamedModel.setUserdp(introPref.getUserdp());
                                flamedModel.setUsername(introPref.getFullName());
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
                    bottomCommentsDialog.show(HashtagClipsViewAll.this.getSupportFragmentManager(), "CommentsSheet");
                    try {
                        AssetFileDescriptor afd = HashtagClipsViewAll.this.getAssets().openFd("sonkho.mp3");
                        MediaPlayer player = new MediaPlayer();
                        player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                        player.prepare();
                        AudioManager audioManager = (AudioManager) HashtagClipsViewAll.this.getSystemService(Context.AUDIO_SERVICE);
                        if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            player.start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                programmingViewHolder.writecomment.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 1, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(HashtagClipsViewAll.this.getSupportFragmentManager(), "CommentsSheet");
                });

                programmingViewHolder.share.setOnClickListener(view -> {
                    link = "Post Link - https://www.applex.in/utsav-app/clips/" + "3/" + currentItem.getDocID();
                    String playstore ="\nCheck out the short video."+getResources().getString(R.string.download_utsav);
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_SEND);
//                    i.putExtra(Intent.EXTRA_TEXT, link+ getResources().getString(R.string.link_suffix));
                    i.putExtra(Intent.EXTRA_TEXT, link+playstore);
                    i.setType("text/plain");
                    startActivity(Intent.createChooser(i, "Share Using"));
                });

                if (currentItem.getCmtNo() > 0) {
                    programmingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                    programmingViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

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

                    programmingViewHolder.comment_layout.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 2, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(HashtagClipsViewAll.this.getSupportFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout1.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 2, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(HashtagClipsViewAll.this.getSupportFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout2.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Reels", currentItem.getDocID(), currentItem.getUid(), 2, "ClipsFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(HashtagClipsViewAll.this.getSupportFragmentManager(), "CommentsSheet");
                    });
                }
                else {
                    programmingViewHolder.comment_layout.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout1.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                }
                ///////////////////FLAMES AND COMMENTS///////////////////////

                ////////POST MENU///////
                programmingViewHolder.menuPost.setOnClickListener(v -> {
                    if (currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                        postMenuDialog = new BottomSheetDialog(HashtagClipsViewAll.this);
                        postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);
                        postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                            Intent i = new Intent(HashtagClipsViewAll.this, NewPostHome.class);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(HashtagClipsViewAll.this);
                            builder.setTitle("Are you sure?")
                                    .setMessage("Post will be deleted permanently")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        progressDialog = new ProgressDialog(HashtagClipsViewAll.this);
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
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(HashtagClipsViewAll.this, "Post has been reported."));
                            postMenuDialog.dismiss();
                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    } else {
                        postMenuDialog = new BottomSheetDialog(HashtagClipsViewAll.this);
                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Reels").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(HashtagClipsViewAll.this, "Post has been reported."));
                            postMenuDialog.dismiss();
                        });
                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    }
                });
                ////////POST MENU///////
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }


            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR: BasicUtility.showToast(getApplicationContext(),"Something went wrong..."); break;
                    case LOADING_MORE: progressmoreposts.setVisibility(View.VISIBLE); break;
                    case LOADED:
                        new Handler().postDelayed(() -> {
                            recyclerview.setVisibility(View.VISIBLE);
                            progressmoreposts.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                        }, 1000);
                        progressmoreposts.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED:
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        contentprogressposts.setVisibility(View.GONE);
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

        final int[] scrollY = {0};
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
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
        TextView commentCount;
        @SuppressLint("StaticFieldLeak")
        LinearLayout comment_layout;

        public VideoView reels_video;
        TextView username, text_content, head_content, likesCount, minsago, writecomment, name_cmnt1, cmnt1, cmnt1_minsago, name_cmnt2, cmnt2, cmnt2_minsago, type_something, comm_heading, pujoTagHolder;
        ImageView userimage, like, commentimg, profileimage, menuPost, share, like_image, comment_image, dp_cmnt1, dp_cmnt2, type_dp, reels_image, sound;
        ApplexLinkPreview LinkPreview;
        LinearLayout itemHome, commentLayout1, commentLayout2, like_layout,new_post_layout, newPostIconsLL;
        RecyclerView tagList;
        com.applex.utsav.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;
        LottieAnimationView dhak_anim;
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
        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}