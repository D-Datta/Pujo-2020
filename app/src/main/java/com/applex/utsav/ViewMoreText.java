package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applex.utsav.adapters.CommentAdapter;
import com.applex.utsav.adapters.TagAdapter;
import com.applex.utsav.adapters.ViewmoreSliderAdapter;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.fragments.FeedsFragment;
import com.applex.utsav.models.CommentModel;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.NotifCount;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.StoreTemp;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;

public class ViewMoreText extends AppCompatActivity {

    private ImageView send;
    //    private EditText newComment;
    private ImageView commentimg, userimage, flameimg, back, likeimage, img;
//    private SliderView sliderView;

    private LinearLayout like_layout;

    private TextView username, minsago,  flamedBy, comName;
    private ReadMoreTextView textContent;
    private int LikeCheck = -1;
    private int change = 0;
    public static int changed = 0;
    public static int commentChanged = 0;
//    private ApplexLinkPreview linkPreview;

    private ProgressDialog progressDialog;

    public static LinearLayout comment_layout;
    public static ImageView  commentimage;
    public static TextView noofcmnts;

    private ProgressBar progressComment;
    private ProgressBar progressBar;
    private DocumentSnapshot lastVisible;
    private int checkGetMore = -1;

    //    private RecyclerView mRecyclerView;
    private RecyclerView tagRecycler;
    private ArrayList<CommentModel> CommentList;
    private CommentAdapter adapter;

    private IntroPref introPref;
    private String PROFILEPIC;
    private String USERNAME, UID, TYPE;

    private ArrayList<String> likeList;
    private ArrayList<String> images;

    //    private CollectionReference commentRef;
    private DocumentReference docRef;
    private CollectionReference flamedRef;
    DocumentReference docref3;

    private BottomSheetDialog commentMenuDialog;
    private BottomSheetDialog postMenuDialog;

    private ImageView more,share;
//    private NotificationFragment instance;

    private int commentCount = 0;
    String bool;
    public final HomePostModel[] homeTextModel = {new HomePostModel()};

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
        setContentView(R.layout.activity_view_more_text);

        share = findViewById(R.id.share44);
        img = findViewById(R.id.post_image45);
        commentimg = findViewById(R.id.comment44);
        username = findViewById(R.id.username44);
        userimage = findViewById(R.id.user_image44);
        minsago = findViewById(R.id.mins_ago44);
        textContent = findViewById(R.id.text_content44);
        flamedBy = findViewById(R.id.flamed_by44);
        noofcmnts = findViewById(R.id.no_of_comments44);
        tagRecycler = findViewById(R.id.tagsList_recycler44);
        flameimg = findViewById(R.id.flame44);
        back = findViewById(R.id.back);
//        linkPreview = findViewById(R.id.LinkPreView);
        more = findViewById(R.id.delete_post);
        progressBar = findViewById(R.id.progress_more1);
        progressComment = findViewById(R.id.commentProgress);
        likeimage = findViewById(R.id.like_image);
        commentimage = findViewById(R.id.comment_image);
        like_layout = findViewById(R.id.like_layout);
        comment_layout = findViewById(R.id.comment_layout);

        UID = FirebaseAuth.getInstance().getUid();
        PROFILEPIC = introPref.getUserdp();
        USERNAME = introPref.getFullName();
        TYPE = introPref.getType();

        likeList = new ArrayList<>();

//
//        Display display1 = getWindowManager().getDefaultDisplay();
//        int displayWidth1 = display1.getWidth();
//        BitmapFactory.Options options1 = new BitmapFactory.Options();
//        options1.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_flame, options1);
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_conch_shell, options);
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_blossom, options);
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_favorite_24, options);
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_comment_viewmore, options);


//        int width1 = options1.outWidth;
//        if (width1 > displayWidth1) {
//            options1.inSampleSize = Math.round((float) width1 / (float) displayWidth1);
//        }
//        options1.inJustDecodeBounds = false;
//
//        Bitmap scaledBitmap1 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_flame, options1);
//        flameimg.setImageBitmap(scaledBitmap1);
//
//        Display display2 = getWindowManager().getDefaultDisplay();
//        int displayWidth2 = display2.getWidth();
//        BitmapFactory.Options options2 = new BitmapFactory.Options();
//        options2.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_conch_shell, options2);
//
//        int width2 = options2.outWidth;
//        if (width2 > displayWidth2) {
//            options2.inSampleSize = Math.round((float) width2 / (float) displayWidth2);
//        }
//        options2.inJustDecodeBounds = false;
//
//        Bitmap scaledBitmap2 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_conch_shell, options2);
//        commentimg.setImageBitmap(scaledBitmap2);
//
//
//        Display display3 = getWindowManager().getDefaultDisplay();
//        int displayWidth3 = display3.getWidth();
//        BitmapFactory.Options options3 = new BitmapFactory.Options();
//        options3.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_blossom, options3);
//
//        int width3 = options3.outWidth;
//        if (width3 > displayWidth3) {
//            options3.inSampleSize = Math.round((float) width3 / (float) displayWidth3);
//        }
//        options3.inJustDecodeBounds = false;
//
//        Bitmap scaledBitmap3 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_blossom, options3);
//        share.setImageBitmap(scaledBitmap3);
//
//
//        Display display4 = getWindowManager().getDefaultDisplay();
//        int displayWidth4 = display4.getWidth();
//        BitmapFactory.Options options4 = new BitmapFactory.Options();
//        options4.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_favorite_24, options3);
//
//        int width4 = options4.outWidth;
//        if (width4 > displayWidth4) {
//            options4.inSampleSize = Math.round((float) width4 / (float) displayWidth4);
//        }
//        options3.inJustDecodeBounds = false;
//
//        Bitmap scaledBitmap4 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_favorite_24, options3);
//        likeimage.setImageBitmap(scaledBitmap4);
//
//
//        Display display5 = getWindowManager().getDefaultDisplay();
//        int displayWidth5 = display5.getWidth();
//        BitmapFactory.Options options5 = new BitmapFactory.Options();
//        options5.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeResource(getResources(), R.drawable.ic_comment_viewmore, options3);
//
//        int width5 = options3.outWidth;
//        if (width5 > displayWidth5) {
//            options3.inSampleSize = Math.round((float) width5 / (float) displayWidth5);
//        }
//        options3.inJustDecodeBounds = false;
//
//        Bitmap scaledBitmap5 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_comment_viewmore, options3);
//        share.setImageBitmap(scaledBitmap5);

        Intent i = getIntent();

        if (getIntent().getExtras().getString("from") == null) {
            homeTextModel[0].setUid(i.getStringExtra("uid"));
            homeTextModel[0].setTs(Long.parseLong(i.getStringExtra("timestamp")));
            if (i.getStringExtra("newTs") != null) {
                homeTextModel[0].setNewTs(Long.parseLong(i.getStringExtra("newTs")));
            }

            minsago.setText(BasicUtility.getTimeAgo(homeTextModel[0].getTs()));
            homeTextModel[0].setDocID(i.getStringExtra("docID"));

            docRef = FirebaseFirestore.getInstance().document("Feeds/" + homeTextModel[0].getDocID() + "/");
            flamedRef = FirebaseFirestore.getInstance().collection("Feeds/" + homeTextModel[0].getDocID() + "/flameL/");

            /////////////USERNAME & USER IMAGE FOR POST//////////////
            homeTextModel[0].setUsN(i.getStringExtra("username"));
            username.setText(homeTextModel[0].getUsN());

            homeTextModel[0].setDp(i.getStringExtra("userdp"));
            homeTextModel[0].setGender(i.getStringExtra("gender"));
            if (homeTextModel[0].getDp() != null && !homeTextModel[0].getDp().isEmpty()) {
                Picasso.get().load(homeTextModel[0].getDp()).placeholder(R.drawable.ic_account_circle_black_24dp).into(userimage);
            }
            else {
                if(homeTextModel[0].getGender()!=null){
                    if (homeTextModel[0].getGender().matches("Female") || homeTextModel[0].getGender().matches("মহিলা")){
                        userimage.setImageResource(R.drawable.ic_female);
                    }
                    else if (homeTextModel[0].getGender().matches("Male") || homeTextModel[0].getGender().matches("পুরুষ")){
                        userimage.setImageResource(R.drawable.ic_male);
                    }
                    else if (homeTextModel[0].getGender().matches("Others") || homeTextModel[0].getGender().matches("অন্যান্য")){
                        userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                }
                else {
                    userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
//                userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
            }

            homeTextModel[0].setType(i.getStringExtra("type"));
            if (homeTextModel[0].getType().matches("com")) {
                username.setOnClickListener(v -> {
                    Intent i12 = new Intent(getApplicationContext(), ActivityProfileCommittee.class);
                    i12.putExtra("uid", homeTextModel[0].getUid());
                    startActivity(i12);
                });

                userimage.setOnClickListener(v -> {
                    Intent i1 = new Intent(getApplicationContext(), ActivityProfileCommittee.class);
                    i1.putExtra("uid", homeTextModel[0].getUid());
                    startActivity(i1);
                });
            } else if (homeTextModel[0].getType().matches("indi")) {
                username.setOnClickListener(v -> {
                    Intent i12 = new Intent(getApplicationContext(), ActivityProfileUser.class);
                    i12.putExtra("uid", homeTextModel[0].getUid());
                    startActivity(i12);
                });

                userimage.setOnClickListener(v -> {
                    Intent i1 = new Intent(getApplicationContext(), ActivityProfileUser.class);
                    i1.putExtra("uid", homeTextModel[0].getUid());
                    startActivity(i1);
                });
            }


            /////////////USERNAME & USER IMAGE FORE POST//////////////


            /////////////////TAGS/////////////////
            if (StoreTemp.getInstance().getTagTemp() != null) {
                homeTextModel[0].setTagL(StoreTemp.getInstance().getTagTemp());
                if (homeTextModel[0].getTagL().size() > 0) {
                    tagRecycler.setHasFixedSize(true);
                    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    tagRecycler.setLayoutManager(linearLayoutManager);
                    TagAdapter tagAdapter = new TagAdapter(homeTextModel[0].getTagL(), getApplicationContext());
                    tagRecycler.setAdapter(tagAdapter);
                } else {
                    tagRecycler.setVisibility(View.GONE);
                }
            } else {
                tagRecycler.setVisibility(View.GONE);
            }
            /////////////////TAGS/////////////////


            ////////////COMMUNITY//////////
//            if(getIntent().getStringExtra("comName")!=null && getIntent().getStringExtra("comID") !=null){
//                comName.setVisibility(View.VISIBLE);
//                homePostModel[0].setComID(getIntent().getStringExtra("comID"));
//                homePostModel[0].setComName(getIntent().getStringExtra("comName"));
//
//                comName.setText(getIntent().getStringExtra("comName"));
//                comName.setBackground(getResources().getDrawable(R.drawable.custom_com_backgnd));
//                comName.setTextColor(getResources().getColor(android.R.color.white));
//                comName.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
////                        Intent intent= new Intent(ViewMoreHome.this, CommunityActivity.class);
////                        intent.putExtra("comID", homePostModel[0].getComID());
////                        startActivity(intent);
//                    }
//                });
//            }
            ////////////COMMUNITY//////////


            ///////////////LIKE SETUP//////////////
            if (i.getSerializableExtra("likeL") != null) {
                likeList = (ArrayList<String>) i.getSerializableExtra("likeL");
                /////////////////UPDATNG FLAMED BY NO.//////////////////////
                if (likeList.size() == 0) {
                    like_layout.setVisibility(View.GONE);
                } else {
                    like_layout.setVisibility(View.VISIBLE);
                    flamedBy.setText(Integer.toString(likeList.size()));

                    like_layout.setOnClickListener(v -> {
                        BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", homeTextModel[0].getDocID());
                        bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                    });

                }

                for(int j = 0; j < likeList.size(); j++){
                    if(likeList.get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){

//                        Display display6 = getWindowManager().getDefaultDisplay();
//                        int displayWidth6 = display6.getWidth();
//                        BitmapFactory.Options options6 = new BitmapFactory.Options();
//                        options6.inJustDecodeBounds = true;
//                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options6);
//                        int width6 = options6.outWidth;
//                        if (width6 > displayWidth6) {
//                            options6.inSampleSize = Math.round((float) width6 / (float) displayWidth6);
//                        }
//                        options6.inJustDecodeBounds = false;
//                        Bitmap scaledBitmap6=  BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options6);
//                        flameimg.setImageBitmap(scaledBitmap6);
//                        flameimg.setImageTintList(null);
//                        LikeCheck = j;

                        flameimg.setImageResource(R.drawable.ic_dhak_view_more);
                        flameimg.setImageTintList(null);
                        LikeCheck = j;

                    }
                    else {
//                        Display display6 = getWindowManager().getDefaultDisplay();
//                        int displayWidth6 = display6.getWidth();
//                        BitmapFactory.Options options6 = new BitmapFactory.Options();
//                        options6.inJustDecodeBounds = true;
//                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_flame, options6);
//                        int width6 = options6.outWidth;
//                        if (width6 > displayWidth6) {
//                            options6.inSampleSize = Math.round((float) width6 / (float) displayWidth6);
//                        }
//                        options6.inJustDecodeBounds = false;
//                        Bitmap scaledBitmap11 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_flame, options6);
//                        flameimg.setImageBitmap(scaledBitmap11);

                        flameimg.setImageResource(R.drawable.ic_normal_flame);

                    }
                }

            } else {
                like_layout.setVisibility(View.GONE);
            }

            ///////////When viewing likelist from fragment global/campus////////////////
            if (i.getStringExtra("likeLOpen") != null && i.getStringExtra("likeLOpen").matches("likeLOpen")) {
                if (likeList != null && likeList.size() > 0) {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", homeTextModel[0].getDocID());
                    bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                } else
                    Toast.makeText(ViewMoreText.this, "No flames", Toast.LENGTH_SHORT).show();
            }
            ///////////When viewing likelist from fragment global/campus////////////////

            like_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (likeList != null && likeList.size() > 0) {
                        BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", homeTextModel[0].getDocID());
                        bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                    } else
                        Toast.makeText(ViewMoreText.this, "No flames", Toast.LENGTH_SHORT).show();
                }
            });
            ///////////////LIKE SETUP//////////////

            ////////////////POST PIC///////////////
//            Bundle args = getIntent().getBundleExtra("BUNDLE");
//            if (args != null) {
//                if ((ArrayList<String>) args.getSerializable("ARRAYLIST") != null
//                        && ((ArrayList<String>) args.getSerializable("ARRAYLIST")).size() > 0) {
//
//                    images = (ArrayList<String>) args.getSerializable("ARRAYLIST");
//
//                    if (images != null && images.size() > 0) {
//                        sliderView.setVisibility(View.VISIBLE);
//
//                        sliderView.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
//                        sliderView.setIndicatorRadius(5);
//                        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
//                        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
//                        sliderView.setIndicatorSelectedColor(R.color.colorPrimary);
//                        sliderView.setIndicatorUnselectedColor(R.color.white);
//                        sliderView.setAutoCycle(false);
//
//                        ViewmoreSliderAdapter viewmoreSliderAdapter = new ViewmoreSliderAdapter(ViewMoreText.this, images);
//
//                        sliderView.setSliderAdapter(viewmoreSliderAdapter);
//                    } else {
//                        sliderView.setVisibility(View.GONE);
//                    }
//
//                }
//            }
            ////////////////POST PIC///////////////


            ////////////////POST TEXT///////////////
            if (i.getStringExtra("postText") != null && !i.getStringExtra("postText").isEmpty()) {
                homeTextModel[0].setTxt(i.getStringExtra("postText"));
                textContent.setVisibility(View.VISIBLE);
                textContent.setText(homeTextModel[0].getTxt());


                //TAGS COLOURED DISPLAY
                Pattern p = Pattern.compile("[#][a-zA-Z0-9-_]+");
                Matcher m = p.matcher(textContent.getText().toString());

                SpannableString ss = new SpannableString(textContent.getText().toString());

                while(m.find()) // loops through all the words in the text which matches the pattern
                {
                    final int s = m.start(); // add 1 to omit the "@" tag
                    final int e = m.end();

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View textView)
                        {
                            Intent i = new Intent(ViewMoreText.this, HashtagPostViewAll.class);
                            i.putExtra("hashtag", textContent.getText().toString().substring(s+1, e));
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
                textContent.setText(ss);
                textContent.setMovementMethod(LinkMovementMethod.getInstance());
                textContent.setHighlightColor(Color.TRANSPARENT);
                //TAGS COLOURED DISPLAY

//                if(textContent.getUrls().length>0){
//                    URLSpan urlSnapItem = textContent.getUrls()[0];
//                    String url = urlSnapItem.getURL();
//                    if(url.contains("http")){
//                        linkPreview.setVisibility(View.VISIBLE);
//                        linkPreview.setLink(url ,new ViewListener() {
//                            @Override
//                            public void onSuccess(boolean status) {
//
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//                                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        //do stuff like remove view etc
//                                        linkPreview.setVisibility(View.GONE);
//                                    }
//                                });
//                            }
//                        });
//                    }
//
//                }
            } else {
                textContent.setVisibility(View.GONE);
            }
            ////////////////POST TEXT///////////////


            //////////////COMMENT SETUP from cmtNo////////////

            if (i.getStringExtra("commentNo") != null) {
                homeTextModel[0].setCmtNo(Long.parseLong(i.getStringExtra("commentNo")));
                if (homeTextModel[0].getCmtNo() > 0) {
                    comment_layout.setVisibility(View.VISIBLE);
                    noofcmnts.setText(Long.toString(homeTextModel[0].getCmtNo()));

                    comment_layout.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", homeTextModel[0].getDocID(), homeTextModel[0].getUid(), 2,"ViewMoreText", null,homeTextModel[0].getCmtNo(), null, null);
                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
                    });

                } else {
                    comment_layout.setVisibility(View.GONE);
                    checkGetMore = -1;
                }
                commentCount = Integer.parseInt(i.getStringExtra("commentNo"));
            } else {
//                mRecyclerView.setVisibility(View.GONE);
//                no_comment.setVisibility(View.VISIBLE);
                comment_layout.setVisibility(View.GONE);
                commentCount = 0;
                checkGetMore = -1;
            }
        }
        else {// from fcm notification or notiff tab or external link
            docref3 = FirebaseFirestore.getInstance()
                    .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/notifCount/")
                    .document("notifCount");
            final NotifCount[] notifCount = {new NotifCount()};
            docref3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            notifCount[0] = documentSnapshot.toObject(NotifCount.class);
                            if (notifCount[0].getNotifCount() > 0) {
                                docref3.update("notifCount", FieldValue.increment(-1));
                            }
                        }
                    }
                }
            });
            String postID;
            String type;
            String ts;
            String pCom_ts;

//            commentRef = FirebaseFirestore.getInstance().collection("Feeds/"+ homePostModel[0].getDocID()+"/commentL");
            postID = getIntent().getExtras().getString("postID");
            type = getIntent().getExtras().getString("type");
            ts = getIntent().getExtras().getString("ts");
            pCom_ts = getIntent().getExtras().getString("pCom_ts");

            docRef = FirebaseFirestore.getInstance().document("Feeds/" + postID + "/");
            flamedRef = FirebaseFirestore.getInstance().collection("Feeds/" + postID + "/flameL");

            FirebaseFirestore.getInstance().document("Feeds/" + postID + "/").get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult().exists()) {
                                homeTextModel[0] = task.getResult().toObject(HomePostModel.class);
                                homeTextModel[0].setDocID(task.getResult().getId());

                                if(type != null) {
                                    if(type.matches("flame")) {
                                        BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", homeTextModel[0].getDocID());
                                        bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                                    }
                                    else if (type.matches("comment")) {
                                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", homeTextModel[0].getDocID(), homeTextModel[0].getUid(), 2, "ViewMoreText", null, homeTextModel[0].getCmtNo(), null, null);
                                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
                                    }
                                    else if(type.matches("comment_flame")) {
                                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", homeTextModel[0].getDocID(), homeTextModel[0].getUid(), 2, "ViewMoreText", type, homeTextModel[0].getCmtNo(), ts, null);
                                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
                                    }
                                    else if(type.matches("comment_reply")) {
                                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", homeTextModel[0].getDocID(), homeTextModel[0].getUid(), 2, "ViewMoreText", type, homeTextModel[0].getCmtNo(), ts, null);
                                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
                                    }
                                    else if(type.matches("comment_reply_flame")) {
                                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", homeTextModel[0].getDocID(), homeTextModel[0].getUid(), 2, "ViewMoreText", type, homeTextModel[0].getCmtNo(), ts, pCom_ts);
                                        bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
                                    }
                                }

                                //SETTING DATABASE REF WRT BOOL VALUE//
                                docRef = FirebaseFirestore.getInstance().document("Feeds/" + homeTextModel[0].getDocID() + "/");
                                flamedRef = FirebaseFirestore.getInstance().collection("Feeds/" + homeTextModel[0].getDocID() + "/flameL");
                                //SETTING DATABASE REF WRT BOOL VALUE//

                                minsago.setText(BasicUtility.getTimeAgo(homeTextModel[0].getTs()));


                                /////////////USERNAME & USER IMAGE FORE POST//////////////
                                username.setText(homeTextModel[0].getUsN());

                                if (homeTextModel[0].getDp() != null && !homeTextModel[0].getDp().isEmpty()) {
                                    Picasso.get().load(homeTextModel[0].getDp()).placeholder(R.drawable.ic_account_circle_black_24dp).into(userimage);
                                }
                                else {
                                    if(homeTextModel[0].getGender()!=null){
                                        if (homeTextModel[0].getGender().matches("Female") || homeTextModel[0].getGender().matches("মহিলা")){
                                            userimage.setImageResource(R.drawable.ic_female);
                                        }
                                        else if (homeTextModel[0].getGender().matches("Male") || homeTextModel[0].getGender().matches("পুরুষ")){
                                            userimage.setImageResource(R.drawable.ic_male);
                                        }
                                        else if (homeTextModel[0].getGender().matches("Others") || homeTextModel[0].getGender().matches("অন্যান্য")){
                                            userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    }
                                    else {
                                        userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
//                                    userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }

                                if (homeTextModel[0].getType().matches("com")) {
                                    username.setOnClickListener(v -> {
                                        Intent i12 = new Intent(getApplicationContext(), ActivityProfileCommittee.class);
                                        i12.putExtra("uid", homeTextModel[0].getUid());
                                        startActivity(i12);
                                    });

                                    userimage.setOnClickListener(v -> {
                                        Intent i1 = new Intent(getApplicationContext(), ActivityProfileCommittee.class);
                                        i1.putExtra("uid", homeTextModel[0].getUid());
                                        startActivity(i1);
                                    });
                                } else if (homeTextModel[0].getType().matches("indi")) {
                                    username.setOnClickListener(v -> {
                                        Intent i12 = new Intent(getApplicationContext(), ActivityProfileUser.class);
                                        i12.putExtra("uid", homeTextModel[0].getUid());
                                        startActivity(i12);
                                    });

                                    userimage.setOnClickListener(v -> {
                                        Intent i1 = new Intent(getApplicationContext(), ActivityProfileUser.class);
                                        i1.putExtra("uid", homeTextModel[0].getUid());
                                        startActivity(i1);
                                    });
                                }

                                /////////////USERNAME & USER IMAGE FORE POST//////////////

                                /////////////////TAGS/////////////////
                                if (homeTextModel[0].getTagL() != null) {
                                    if (homeTextModel[0].getTagL().size() > 0) {
                                        tagRecycler.setHasFixedSize(true);
                                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                                        tagRecycler.setLayoutManager(linearLayoutManager);
                                        TagAdapter tagAdapter = new TagAdapter(homeTextModel[0].getTagL(), getApplicationContext());
                                        tagRecycler.setAdapter(tagAdapter);
                                    } else {
                                        tagRecycler.setVisibility(View.GONE);
                                    }
                                } else {
                                    tagRecycler.setVisibility(View.GONE);
                                }
                                /////////////////TAGS/////////////////


                                ///////////////LIKE SETUP//////////////
                                if (homeTextModel[0].getLikeL() != null) {
                                    likeList = homeTextModel[0].getLikeL();
                                    /////////////////UPDATNG FLAMED BY NO.//////////////////////
                                    if (likeList.size() == 0) {
                                        like_layout.setVisibility(View.GONE);
                                    } else {
                                        like_layout.setVisibility(View.VISIBLE);
                                        flamedBy.setText(Integer.toString(likeList.size()));

                                        like_layout.setOnClickListener(v -> {
                                            BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", homeTextModel[0].getDocID());
                                            bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                                        });

                                    }
                                    for(int j = 0; j < likeList.size(); j++){
                                        if(likeList.get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){

                                            flameimg.setImageResource(R.drawable.ic_flame_red);
                                            flameimg.setImageTintList(null);
                                            LikeCheck = j;

//                                            Display display7 = getWindowManager().getDefaultDisplay();
//                                            int displayWidth7 = display7.getWidth();
//                                            BitmapFactory.Options options7 = new BitmapFactory.Options();
//                                            options7.inJustDecodeBounds = true;
//                                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options7);
//                                            int width7 = options7.outWidth;
//                                            if (width7 > displayWidth7) {
//                                                options7.inSampleSize = Math.round((float) width7 / (float) displayWidth7);
//                                            }
//                                            options7.inJustDecodeBounds = false;
//                                            Bitmap scaledBitmap7 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options7);
//                                            flameimg.setImageBitmap(scaledBitmap7);
//                                            flameimg.setImageTintList(null);
//                                            LikeCheck = j;
//                                            if((likeList.size()-1) == 1)
//                                                flamedBy.setText("Flamed by you & "+ (likeList.size()-1) +" other");
//                                            else if((likeList.size()-1) == 0){
//                                                flamedBy.setText("Flamed by you");
//                                            }
//                                            else
//                                                flamedBy.setText("Flamed by you & "+ (likeList.size()-1) +" others");
                                            //Position in likeList where the current USer UId is found stored in likeCheck
                                        }
                                        else {
//                                            Display display7 = getWindowManager().getDefaultDisplay();
//                                            int displayWidth7 = display7.getWidth();
//                                            BitmapFactory.Options options7 = new BitmapFactory.Options();
//                                            options7.inJustDecodeBounds = true;
//                                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_flame, options7);
//                                            int width7 = options7.outWidth;
//                                            if (width7 > displayWidth7) {
//                                                options7.inSampleSize = Math.round((float) width7 / (float) displayWidth7);
//                                            }
//                                            options7.inJustDecodeBounds = false;
//                                            Bitmap scaledBitmap11 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_flame, options7);
//                                            flameimg.setImageBitmap(scaledBitmap11);

                                            flameimg.setImageResource(R.drawable.ic_dhak_view_more);
                                        }
                                    }

                                } else {
                                    like_layout.setVisibility(View.GONE);
                                }

                                ///////////When viewing likelist from fragment global/campus////////////////
//                                if(i.getStringExtra("likeLOpen")!=null && i.getStringExtra("likeLOpen").matches("likeLOpen"))
//                                {
//                                    if(likeList!=null && likeList.size() > 0){
//                                        BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", homePostModel[0].getDocID());
//                                        bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
//                                    }
//                                    else
//                                        Toast.makeText(ViewMoreHome.this, "No flames", Toast.LENGTH_SHORT).show();
//                                }
                                ///////////When viewing likelist from fragment global/campus////////////////

                                like_layout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (likeList != null && likeList.size() > 0) {
                                            BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", homeTextModel[0].getDocID());
                                            bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                                        } else
                                            Toast.makeText(ViewMoreText.this, "No flames", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                ///////////////LIKE SETUP//////////////


                                ////////////////POST PIC///////////////
//                                images = homePostModel[0].getImg();
//
//                                if (images != null && images.size() > 0) {
//                                    sliderView.setVisibility(View.VISIBLE);
//
//                                    sliderView.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
//                                    sliderView.setIndicatorRadius(5);
//                                    sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
//                                    sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
//                                    sliderView.setIndicatorSelectedColor(R.color.colorPrimary);
//                                    sliderView.setIndicatorUnselectedColor(R.color.white);
//                                    sliderView.setAutoCycle(false);
//
//                                    ViewmoreSliderAdapter viewmoreSliderAdapter = new ViewmoreSliderAdapter(ViewMoreText.this, images);
//
//                                    sliderView.setSliderAdapter(viewmoreSliderAdapter);
//                                } else {
//                                    sliderView.setVisibility(View.GONE);
//                                }
                                ////////////////POST PIC///////////////


                                ////////////////POST TEXT///////////////
                                if (homeTextModel[0].getTxt() != null && !homeTextModel[0].getTxt().isEmpty()) {
                                    textContent.setVisibility(View.VISIBLE);
                                    textContent.setText(homeTextModel[0].getTxt());
//                                    if(textContent.getUrls().length>0){
//                                        URLSpan urlSnapItem = textContent.getUrls()[0];
//                                        String url = urlSnapItem.getURL();
//                                        if(url.contains("http")){
//                                            linkPreview.setVisibility(View.VISIBLE);
//                                            linkPreview.setLink(url ,new ViewListener() {
//                                                @Override
//                                                public void onSuccess(boolean status) {
//
//                                                }
//
//                                                @Override
//                                                public void onError(Exception e) {
//                                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            //do stuff like remove view etc
//                                                            linkPreview.setVisibility(View.GONE);
//                                                        }
//                                                    });
//                                                }
//                                            });
//                                        }
//
//                                    }
                                } else {
                                    textContent.setVisibility(View.GONE);
                                }
                                ////////////////POST TEXT///////////////

                                //////////////COMMENT SETUP from cmtNo////////////
                                if (homeTextModel[0].getCmtNo() > -1) {

                                    if (homeTextModel[0].getCmtNo() > 0) {
                                        comment_layout.setVisibility(View.VISIBLE);
                                        noofcmnts.setText(Long.toString(homeTextModel[0].getCmtNo()));

                                        comment_layout.setOnClickListener(v -> {
                                            BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", homeTextModel[0].getDocID(), homeTextModel[0].getUid(), 2,"ViewMoreText", null,homeTextModel[0].getCmtNo(), null, null);
                                            bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
                                        });

                                    } else {
                                        comment_layout.setVisibility(View.GONE);
                                        checkGetMore = -1;
                                    }
                                    commentCount = (int) homeTextModel[0].getCmtNo();
                                } else {
//                                        mRecyclerView.setVisibility(View.GONE);
                                    ////                no_comment.setVisibility(View.VISIBLE);
                                    comment_layout.setVisibility(View.GONE);
                                    commentCount = 0;
                                    checkGetMore = -1;
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Post has been removed", Toast.LENGTH_SHORT).show();
                                if (getIntent().getStringExtra("position") != null) {
//                                    NotificationFragment.removeNotif = Integer.parseInt(getIntent().getStringExtra("position"));
                                }
                                if (isTaskRoot()) {
                                    startActivity(new Intent(ViewMoreText.this, MainActivity.class));
                                } else {
                                    ViewMoreText.super.onBackPressed();
                                }
                            }

                        }
                    });

        }
        PushDownAnim.setPushDownAnimTo(flameimg)
                .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        change = 1;
                        if (LikeCheck >= 0) {//was already liked by current user

//                            Display display7 = getWindowManager().getDefaultDisplay();
//                            int displayWidth7 = display7.getWidth();
//                            BitmapFactory.Options options7 = new BitmapFactory.Options();
//                            options7.inJustDecodeBounds = true;
//                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options7);
//                            int width7 = options7.outWidth;
//                            if (width7 > displayWidth7) {
//                                options7.inSampleSize = Math.round((float) width7 / (float) displayWidth7);
//                            }
//                            options7.inJustDecodeBounds = false;
//                            Bitmap scaledBitmap11 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options7);
//                            flameimg.setImageBitmap(scaledBitmap11);

                            flameimg.setImageResource(R.drawable.ic_dhak_view_more);//was already liked by current user

                            if (likeList.size() - 1 == 0) {
                                like_layout.setVisibility(View.GONE);
                            } else {
                                BasicUtility.vibrate(ViewMoreText.this);
                                like_layout.setVisibility(View.VISIBLE);
                                flamedBy.setText(Integer.toString(likeList.size() - 1));
                            }

                            likeList.remove(FirebaseAuth.getInstance().getUid());
                            LikeCheck = -1;

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();

                            DocumentReference flamedDoc = flamedRef.document(FirebaseAuth.getInstance().getUid());
                            batch.update(docRef, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                            batch.delete(flamedDoc);
                            batch.commit().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    change = 1;
//                                    FeedsFragment.changed=1;
                                } else {
                                    Toast.makeText(ViewMoreText.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }

                            });
                            ///////////////////BATCH WRITE///////////////////

                        } else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                            BasicUtility.vibrate(getApplicationContext());
                            try {
                                AssetFileDescriptor afd = ViewMoreText.this.getAssets().openFd("dhak.mp3");
                                MediaPlayer player = new MediaPlayer();
                                player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                                player.prepare();
                                AudioManager audioManager = (AudioManager) ViewMoreText.this.getSystemService(Context.AUDIO_SERVICE);
                                if(audioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL)
                                    player.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            Display display8 = getWindowManager().getDefaultDisplay();
                            int displayWidth8 = display8.getWidth();
                            BitmapFactory.Options options8 = new BitmapFactory.Options();
                            options8.inJustDecodeBounds = true;
                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options8);
                            int width8 = options8.outWidth;
                            if (width8 > displayWidth8) {
                                options8.inSampleSize = Math.round((float) width8 / (float) displayWidth8);
                            }
                            options8.inJustDecodeBounds = false;
                            Bitmap scaledBitmap11 =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_flame_red, options8);
                            flameimg.setImageBitmap(scaledBitmap11);

                            like_layout.setVisibility(View.VISIBLE);
                            if (likeList != null)
                                flamedBy.setText(Integer.toString(likeList.size() + 1));
                            else
                                flamedBy.setText("1");

                            likeList.add(FirebaseAuth.getInstance().getUid());
                            LikeCheck = likeList.size() - 1;

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            FlamedModel flamedModel = new FlamedModel();
                            long tsLong = System.currentTimeMillis();

                            flamedModel.setPostID(homeTextModel[0].getDocID());
                            flamedModel.setTs(tsLong);
                            flamedModel.setUid(UID);
                            flamedModel.setType(introPref.getType());
                            flamedModel.setUserdp(PROFILEPIC);
                            flamedModel.setUsername(USERNAME);
                            flamedModel.setPostUid(homeTextModel[0].getUid());
                            flamedModel.setGender(introPref.getGender());

                            DocumentReference flamedDoc = flamedRef.document(FirebaseAuth.getInstance().getUid());
                            batch.update(docRef, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if (likeList.size() % 5 == 0) {
                                batch.update(docRef, "newTs", tsLong);
                            }
                            batch.commit().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    change = 1;
//                                    FeedsFragment.changed=1;
                                } else {
                                    Toast.makeText(ViewMoreText.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }

                            });
                            ///////////////////BATCH WRITE///////////////////
                        }
                    }
                });
        commentimg.setOnClickListener(v -> {
            BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", homeTextModel[0].getDocID(), homeTextModel[0].getUid(), 1,"ViewMoreText", null,homeTextModel[0].getCmtNo(), null, null);
            bottomCommentsDialog.show(getSupportFragmentManager(), "CommentsSheet");
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (homeTextModel[0].getUid().matches(FirebaseAuth.getInstance().getUid())) {
                    postMenuDialog = new BottomSheetDialog(ViewMoreText.this);
                    postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                    //postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);

                    postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                                Intent i = new Intent(ViewMoreText.this, NewPostHome.class);
                                i.putExtra("target", "100"); //target value for edit post
                                i.putExtra("bool", "2");
                                i.putExtra("typeofpost", "notreel");
                                i.putExtra("txt", homeTextModel[0].getTxt());
                                i.putExtra("headline", homeTextModel[0].getHeadline());
                                if(homeTextModel[0].getTagList() != null && homeTextModel[0].getTagList().size()>0) {
                                    Bundle args = new Bundle();
                                    args.putSerializable("ARRAYLISTTAGS", homeTextModel[0].getTagList());
                                    i.putExtra("BUNDLETAGS", args);
                                }
                                i.putExtra("docID", homeTextModel[0].getDocID());
                                StoreTemp.getInstance().setPujoTagModel(homeTextModel[0].getPujoTag());
////                            i.putExtra("target", "100"); //target value for edit post
////                            i.putExtra("bool", "3");
////                            i.putExtra("usN", currentItem.getUsN());
////                            i.putExtra("dp", currentItem.getDp());
////                            i.putExtra("uid", currentItem.getUid());
////                            i.putExtra("type", currentItem.getType());
////                            if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
////                                Bundle args = new Bundle();
////                                args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
////                                i.putExtra("BUNDLE", args);
////                            }
////                            i.putExtra("txt", currentItem.getTxt());
////                            i.putExtra("comID", currentItem.getComID());
////                            i.putExtra("comName", currentItem.getComName());
////                            i.putExtra("ts", Long.toString(currentItem.getTs()));
////                            i.putExtra("newTs", Long.toString(currentItem.getNewTs()));
////                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
////                            i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));
////                            i.putExtra("likeL", currentItem.getLikeL());
////                            i.putExtra("likeCheck", currentItem.getLikeCheck());
////                            i.putExtra("docID", currentItem.getDocID());
////                            i.putExtra("reportL", currentItem.getReportL());
////                            i.putExtra("challengeID", currentItem.getChallengeID());
                                startActivity(i);
                                postMenuDialog.dismiss();

                            });

                    postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ViewMoreText.this);
                            builder.setTitle("Are you sure?")
                                    .setMessage("Post will be deleted permanently")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        docRef.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        postMenuDialog.dismiss();
                                                        change = 1;
//                                                        ProfileActivity.delete = 1;
                                                        if (getIntent().getStringExtra("from") != null && getIntent().getStringExtra("from").matches("link")) {
                                                            startActivity(new Intent(ViewMoreText.this, MainActivity.class));
                                                            finish();
                                                        } else if (isTaskRoot()) {
                                                            startActivity(new Intent(ViewMoreText.this, MainActivity.class));
                                                            finish();
                                                        } else {
                                                            ViewMoreText.super.onBackPressed();
                                                        }
                                                    }
                                                });
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();

                        }
                    });

                    postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);

                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            docRef.update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            BasicUtility.showToast(getApplicationContext(), "Post has been reported.");
                                        }
                                    });
                            postMenuDialog.dismiss();
                        }
                    });
                    Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    postMenuDialog.show();

                } else {
                    postMenuDialog = new BottomSheetDialog(ViewMoreText.this);

                    postMenuDialog.setContentView(R.layout.dialog_post_menu);
                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                    postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);

                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            docRef.update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            BasicUtility.showToast(getApplicationContext(), "Post has been reported.");
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

        back.setOnClickListener(v -> {
            if (getIntent().getStringExtra("from") != null && getIntent().getStringExtra("from").matches("link")) {
                startActivity(new Intent(ViewMoreText.this, MainActivity.class));
                finish();
            }
            if (isTaskRoot()) {
                startActivity(new Intent(ViewMoreText.this, MainActivity.class));
                finish();
            } else {
                if (change == 1)
                    Toast.makeText(ViewMoreText.this, "Swipe to refresh", Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            }

        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String id = postCampus.replaceAll(" ","_");
                String link = "https://www.applex.in/utsav-app/feeds/" + "0/" + homeTextModel[0].getDocID();
                Intent i = new Intent();
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, link);
                i.setType("text/plain");
                startActivity(Intent.createChooser(i, "Share with"));


            }
        });
    }
    @Override
    public void onBackPressed() {
        if(isTaskRoot()){
            startActivity(new Intent(ViewMoreText.this, MainActivity.class));
            finish();
        }
        else {
            if(change == 1)
                Toast.makeText(ViewMoreText.this, "Swipe to refresh", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        if(changed > 0 || commentChanged > 0) {
//            buildCommentRecyclerView();
            changed = 0;
            commentChanged = 0;
        }
        super.onResume();
    }
}