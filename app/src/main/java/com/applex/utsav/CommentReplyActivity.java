package com.applex.utsav;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.applex.utsav.LinkPreview.ApplexLinkPreviewShort;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.adapters.CommentReplyAdapter;
import com.applex.utsav.fragments.CommitteeFragment;
import com.applex.utsav.models.CommentModel;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.ReplyCommentModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.dialogs.BottomFlamedByDialog2;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class CommentReplyActivity extends AppCompatActivity {

    private ImageView send, flameComment, replyComment, more;
    private EditText newComment;
    private ImageView userimage, userimage_comment;

    private TextView username, minsago, comment, flamedByComment, repliedByNo;
    private int LikeCheck = -1;
    private ApplexLinkPreviewShort linkPreview;

    private ProgressDialog progressDialog;
    private ProgressBar progressComment;
    private ProgressBar progressBar;
    private DocumentSnapshot lastVisible;
    private int checkGetMore = -1;

    private RecyclerView mRecyclerView;
    private ArrayList<ReplyCommentModel> CommentList;
    private CommentReplyAdapter adapter;

    private IntroPref introPref;
    private String PROFILEPIC, user_image, GENDER;
    private String USERNAME, UID, notifType, pCom_ts;

    private ArrayList<String> likeList;

    private CollectionReference commentColRef, flameColRef, orgCommentColRef;
    private DocumentReference commentDocRef, docRef;
    private String postID, docID;

    private BottomSheetDialog commentMenuDialog;
    private int commentCount = 0;
    private String bool, bool_comment;

    public static int change = 0;
    public static int mode_changed = 0;

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

        setContentView(R.layout.activity_reply_comments);

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(CommentReplyActivity.this, (value, error) -> {
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
                                        startActivity(new Intent(CommentReplyActivity.this, CommentReplyActivity.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }

        Toolbar toolbar = findViewById(R.id.toolbar12);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Replies");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        mRecyclerView = findViewById(R.id.reply_comments_recycler);
        send = findViewById(R.id.send_comment);
        more = findViewById(R.id.comment_more1);
        newComment = findViewById(R.id.new_comment);
        username = findViewById(R.id.username);
        userimage = findViewById(R.id.user_image);
        minsago = findViewById(R.id.mins_ago);
        comment = findViewById(R.id.comment);
        userimage_comment= findViewById(R.id.user_image_comment);
        linkPreview = findViewById(R.id.LinkPreViewComment);
        progressBar = findViewById(R.id.progress_more1);
        progressComment = findViewById(R.id.commentProgress);
        flameComment = findViewById(R.id.flame_comment);
        flamedByComment = findViewById(R.id.flamed_by_comment);
        replyComment = findViewById(R.id.comment_reply);
        repliedByNo = findViewById(R.id.replied_by);

        NestedScrollView nestedScrollView = findViewById(R.id.scrollView);
        nestedScrollView.setNestedScrollingEnabled(true);

        UID = FirebaseAuth.getInstance().getUid();
        PROFILEPIC = introPref.getUserdp();
        USERNAME = introPref.getFullName();
        GENDER = introPref.getGender();

        likeList = new ArrayList<>();

        //////////////////CURRENT USER DETAILS///////////////////
        if(PROFILEPIC != null){
            Picasso.get().load(PROFILEPIC).into(userimage_comment, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError(Exception e) {
                        if(GENDER!=null){
                            if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                                userimage_comment.setImageResource(R.drawable.ic_female);
                            }
                            else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                                userimage_comment.setImageResource(R.drawable.ic_male);
                            }
                            else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                                userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }
                        else{
                            userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
//                    userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            });
        }
        else{
            if(GENDER!=null){
                if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                    userimage_comment.setImageResource(R.drawable.ic_female);
                }
                else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                    userimage_comment.setImageResource(R.drawable.ic_male);
                }
                else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                    userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
            else{
                userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
            }
        }

        //////////////////CURRENT USER DETAILS///////////////////

        Intent i= getIntent();
        bool = i.getStringExtra("bool");
        notifType = i.getStringExtra("notiType");
        pCom_ts = i.getStringExtra("pCom_ts");

        final CommentModel[] commentModel = {new CommentModel()};

        ////////////SETTING DETAILS OF THE MAIN COMMENT////////////
        user_image = i.getStringExtra("userdp");
        minsago.setText(BasicUtility.getTimeAgo(Long.parseLong(i.getStringExtra("timestamp"))));
        username.setText(i.getStringExtra("username"));

        comment.setText(i.getStringExtra("comment"));
        if(comment.getUrls().length>0) {
            URLSpan urlSnapItem = comment.getUrls()[0];
            String url = urlSnapItem.getURL();
            if (url.contains("http")) {
                linkPreview.setVisibility(View.VISIBLE);
                linkPreview.setLink(url, new ViewListener() {
                    @Override
                    public void onSuccess(boolean status) {

                    }

                    @Override
                    public void onError(Exception e) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //do stuff like remove view etc
                                linkPreview.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        }

        if(user_image!=null){
            Picasso.get().load(user_image).into(userimage, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError(Exception e) {
                    if(i.getStringExtra("gender")!=null){
                        if(i.getStringExtra("gender").matches("Female") || i.getStringExtra("gender").matches("Female")){
                            userimage.setImageResource(R.drawable.ic_female);
                        }
                        else if(i.getStringExtra("gender").matches("Female") || i.getStringExtra("gender").matches("Female")){
                            userimage.setImageResource(R.drawable.ic_male);
                        }
                        else if(i.getStringExtra("gender").matches("Female") || i.getStringExtra("gender").matches("Female")){
                            userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                    else{
                        userimage.setImageResource(R.drawable.ic_male);
                    }
//                    userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            });
        }
        else{
            if(i.getStringExtra("gender")!=null){
                if(i.getStringExtra("gender").matches("Female") || i.getStringExtra("gender").matches("Female")){
                    userimage.setImageResource(R.drawable.ic_female);
                }
                else if(i.getStringExtra("gender").matches("Female") || i.getStringExtra("gender").matches("Female")){
                    userimage.setImageResource(R.drawable.ic_male);
                }
                else if(i.getStringExtra("gender").matches("Female") || i.getStringExtra("gender").matches("Female")){
                    userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
            else{
                userimage.setImageResource(R.drawable.ic_male);
            }
        }


        ////////////SETTING DETAILS OF THE MAIN COMMENT////////////

        ////////////SETTING DATABASE REF WRT BOOL VALUE////////////
        postID = i.getStringExtra("postID");
        docID = i.getStringExtra("docID");

        ////////////SETTING DATABASE REF WRT BOOL VALUE////////////
        if(bool.matches("1")) {
            flameColRef = FirebaseFirestore.getInstance().collection("Feeds/"+postID+"/commentL/"+docID+"/flameL/");
            commentColRef = FirebaseFirestore.getInstance().collection("Feeds/"+postID+"/commentL/"+docID+"/commentL/");
            orgCommentColRef = FirebaseFirestore.getInstance().collection("Feeds/"+postID+"/commentL/");
            commentDocRef = FirebaseFirestore.getInstance().document("Feeds/"+postID+"/commentL/"+docID+"/");
            docRef = FirebaseFirestore.getInstance().document("Feeds/"+postID+"/");
        }
        else if(bool.matches("2")) {
            flameColRef = FirebaseFirestore.getInstance().collection("Reels/"+postID+"/commentL/"+docID+"/flameL/");
            commentColRef = FirebaseFirestore.getInstance().collection("Reels/"+postID+"/commentL/"+docID+"/commentL/");
            orgCommentColRef = FirebaseFirestore.getInstance().collection("Reels/"+postID+"/commentL/");
            commentDocRef = FirebaseFirestore.getInstance().document("Reels/"+postID+"/commentL/"+docID+"/");
            docRef = FirebaseFirestore.getInstance().document("Reels/"+postID+"/");
            ReelsActivity.postID = postID;
        }

        username.setOnClickListener(v -> {
//            if(i.getStringExtra("type").matches("com")) {
//                Intent intent = new Intent(CommentReplyActivity.this, ActivityProfileCommittee.class);
//                intent.putExtra("uid", i.getStringExtra("uid"));
//                startActivity(intent);
//            }
//            else {
//                Intent intent = new Intent(CommentReplyActivity.this, ActivityProfileUser.class);
//                intent.putExtra("uid", i.getStringExtra("uid"));
//                startActivity(intent);
//            }
            Intent intent = new Intent(CommentReplyActivity.this, ActivityProfile.class);
            intent.putExtra("uid", i.getStringExtra("uid"));
            startActivity(intent);
        });

        userimage.setOnClickListener(v -> {
//            if(i.getStringExtra("type").matches("com")) {
//                Intent intent = new Intent(CommentReplyActivity.this, ActivityProfileCommittee.class);
//                intent.putExtra("uid", i.getStringExtra("uid"));
//                startActivity(intent);
//            }
//            else {
//                Intent intent = new Intent(CommentReplyActivity.this, ActivityProfileUser.class);
//                intent.putExtra("uid", i.getStringExtra("uid"));
//                startActivity(intent);
//            }
            Intent intent = new Intent(CommentReplyActivity.this, ActivityProfile.class);
            intent.putExtra("uid", i.getStringExtra("uid"));
            startActivity(intent);
        });

        //////////////FLAME SETUP///////////////////
        if(i.getSerializableExtra("likeL") != null) {
            likeList = (ArrayList<String>) i.getSerializableExtra("likeL");
            /////////////////UPDATNG FLAMED BY NO.//////////////////////
            flamedByComment.setText(String.valueOf(likeList.size()));

            for(int j = 0; j < likeList.size(); j++){
                if(likeList.get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){
                    flameComment.setImageResource(R.drawable.ic_flame_red);
                    LikeCheck = j;
                    flamedByComment.setText(String.valueOf(likeList.size()));
                    //Position in likeList where the current USer UId is found stored in likeCheck
                }
            }
        } else {
            flamedByComment.setText("0");
        }

        PushDownAnim.setPushDownAnimTo(flameComment)
                .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                .setOnClickListener(v -> {
                    if(LikeCheck >= 0){//was already liked by current user
                        flameComment.setImageResource(R.drawable.ic_btmnav_notifications);
                        flamedByComment.setText(String.valueOf(likeList.size()-1));
                        ///////////REMOVE CURRENT USER LIKE/////////////
                        likeList.remove(FirebaseAuth.getInstance().getUid());
                        LikeCheck = -1;

                        //likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

                        ///////////////////BATCH WRITE///////////////////
                        WriteBatch batch = FirebaseFirestore.getInstance().batch();

                        DocumentReference flamedDoc = flameColRef.document(FirebaseAuth.getInstance().getUid());
                        batch.update(commentDocRef, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                        batch.delete(flamedDoc);

                        batch.commit().addOnSuccessListener(task -> {
                            ViewMoreHome.commentChanged = 1;
//                            ViewMoreSlider.commentChanged = 1;
                        })
                        .addOnFailureListener(task -> {
                            Toast.makeText(CommentReplyActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                        });
                        ///////////////////BATCH WRITE///////////////////
                    }
                    else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                        BasicUtility.vibrate(getApplicationContext());
                        try {
                            AssetFileDescriptor afd = getAssets().openFd("dhak.mp3");
                            MediaPlayer player = new MediaPlayer();
                            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                            player.prepare();
                            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            if(audioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL)
                                player.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        flameComment.setImageResource(R.drawable.ic_flame_red);

                        //////////////ADD CURRENT USER TO LIKELIST//////////////////
                        likeList.add(FirebaseAuth.getInstance().getUid());
                        LikeCheck = likeList.size()-1;
                        //For local changes current item like added to remote list end

                        flamedByComment.setText(String.valueOf(likeList.size()));

                        ///////////////////BATCH WRITE///////////////////
                        WriteBatch batch = FirebaseFirestore.getInstance().batch();
                        FlamedModel flamedModel = new FlamedModel();
                        long tsLong = System.currentTimeMillis();

                        flamedModel.setPostID(postID);
                        flamedModel.setDocID(docID);
                        flamedModel.setTs(tsLong);
                        flamedModel.setType(introPref.getType());
                        flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                        flamedModel.setUserdp(PROFILEPIC);
                        flamedModel.setUsername(USERNAME);
                        flamedModel.setPostUid(i.getStringExtra("uid"));
                        flamedModel.setGender(introPref.getGender());

                        DocumentReference flamedDoc = flameColRef.document(FirebaseAuth.getInstance().getUid());
                        batch.update(commentDocRef, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                        batch.set(flamedDoc, flamedModel);

                        batch.commit().addOnSuccessListener(task -> {
                            ViewMoreHome.commentChanged = 1;
//                            ViewMoreSlider.commentChanged = 1;
                        })
                        .addOnFailureListener(task -> {
                            Toast.makeText(CommentReplyActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                        });
                        ///////////////////BATCH WRITE///////////////////
                    }
                });
        //////////////////FLAME SETUP////////////////////////

        flamedByComment.setOnClickListener(v -> {
            if(likeList !=null && likeList.size() > 0) {
                BottomFlamedByDialog2 bottomSheetDialog = null;
                if(bool.matches("1")) {
                    bottomSheetDialog = new BottomFlamedByDialog2("Feeds", postID, docID);
                } else if(bool.matches("2")) {
                    bottomSheetDialog = new BottomFlamedByDialog2("Reels", postID, docID);
                }
                bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
            }
            else
                Toast.makeText(getApplicationContext(), "No flames", Toast.LENGTH_SHORT).show();
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(i.getStringExtra("uid").matches(FirebaseAuth.getInstance().getUid())) {
                    commentMenuDialog= new BottomSheetDialog(CommentReplyActivity.this);
                    commentMenuDialog.setContentView(R.layout.dialog_comment_menu2);
                    commentMenuDialog.findViewById(R.id.edit_comment).setVisibility(View.VISIBLE);
                    commentMenuDialog.findViewById(R.id.edit_comment).setOnClickListener(v2 -> {
                                Intent intent = new Intent(CommentReplyActivity.this, CommentEdit.class);
                                intent.putExtra("comment_home", i.getStringExtra("comment"));
                                intent.putExtra("com_img_home", i.getStringExtra("userdp"));
                                intent.putExtra("com_username_home", i.getStringExtra("username"));
                                intent.putExtra("com_postUid_home", i.getStringExtra("postUid"));
                                intent.putExtra("com_likeL_home", i.getStringExtra("likeL"));
                                intent.putExtra("com_rComNo_home", i.getStringExtra("ReplyCommentNo"));
                                intent.putExtra("com_uid_home", i.getStringExtra("uid"));
                                intent.putExtra("timestamp", i.getStringExtra("timestamp"));
                                intent.putExtra("pComUid", i.getStringExtra("pComUid"));
                                intent.putExtra("com_postID_home", postID);
                                intent.putExtra("com_docID_home", docID);
                                intent.putExtra("com_bool_home", bool);
                                intent.putExtra("com_bool", bool_comment);
                                intent.putExtra("from", "yes");
                                intent.putExtra("type", i.getStringExtra("type"));
                                intent.putExtra("gender",i.getStringExtra("gender"));
                                startActivity(intent);
                                commentMenuDialog.dismiss();
                                finish();
                            });

                    commentMenuDialog.findViewById(R.id.delete_post).setVisibility(View.VISIBLE);
                    commentMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v3 -> {
                        progressDialog = new ProgressDialog(CommentReplyActivity.this);
                        progressDialog.setTitle("Deleting Comment");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        ///////////////////BATCH WRITE///////////////////
                        WriteBatch batch = FirebaseFirestore.getInstance().batch();

                        DocumentReference cmtDoc = orgCommentColRef.document(docID);
                        int total = CommentList.size() + 1;
                        batch.delete(cmtDoc);
                        batch.update(docRef, "cmtNo", FieldValue.increment(-(total)));

                        batch.commit().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                ViewMoreHome.commentChanged = 1;
//                                ViewMoreSlider.commentChanged = 1;
                                CommentReplyActivity.super.onBackPressed();
                                progressDialog.dismiss();
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(CommentReplyActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                            }

                        });
                        ///////////////////BATCH WRITE///////////////////
                        commentMenuDialog.dismiss();
                    });

                    commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v4 ->
                            {
                                orgCommentColRef.document(docID)
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> {
                                            BasicUtility.showToast(CommentReplyActivity.this, "Comment has been reported.");
                                        });
                                commentMenuDialog.dismiss();

                            }
                    );


                }
                else {
                    commentMenuDialog= new BottomSheetDialog(CommentReplyActivity.this);
                    commentMenuDialog.setContentView(R.layout.dialog_comment_menu);
                    commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                    commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v5 ->
                            {
                                orgCommentColRef.document(docID)
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> {
                                            BasicUtility.showToast(CommentReplyActivity.this, "Comment has been reported.");
                                        });
                                commentMenuDialog.dismiss();
                            }
                    );

                }

                Objects.requireNonNull(commentMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                commentMenuDialog.show();
            }
        });

        //////////////COMMENT SETUP from cmtNo////////////
        LinearLayoutManager layoutManager = new LinearLayoutManager(CommentReplyActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        if(i.getStringExtra("ReplyCommentNo")!=null){
            commentModel[0].setrCmtNo(Integer.parseInt(i.getStringExtra("ReplyCommentNo")));
            if(commentModel[0].getrCmtNo()>0){
                repliedByNo.setText(String.valueOf(commentModel[0].getrCmtNo()));
            }
            else {
                repliedByNo.setText("0");
                checkGetMore = -1;
            }
            commentCount = Integer.parseInt(i.getStringExtra("ReplyCommentNo"));
        }
        else {
            repliedByNo.setText("0");
            commentCount = 0;
            checkGetMore = -1;
        }

        CommentList = new ArrayList<>();
        adapter = new CommentReplyAdapter(CommentReplyActivity.this, CommentList, Integer.parseInt(bool), notifType, pCom_ts);
        adapter.onClickListener(new CommentReplyAdapter.OnClickListener() {
            @Override
            public void onClickListener(int position) {

                if( CommentList.get(position).getUid().matches(FirebaseAuth.getInstance().getUid())
                        || i.getStringExtra("uid").matches(FirebaseAuth.getInstance().getUid())){
                    commentMenuDialog= new BottomSheetDialog(CommentReplyActivity.this);
                    commentMenuDialog.setContentView(R.layout.dialog_comment_menu2);
                    commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                    if( CommentList.get(position).getUid().matches(FirebaseAuth.getInstance().getUid())) {
                        commentMenuDialog.findViewById(R.id.edit_comment).setVisibility(View.VISIBLE);
                        commentMenuDialog.findViewById(R.id.edit_comment).setOnClickListener(v ->
                                {
                                    Intent intent = new Intent(CommentReplyActivity.this, CommentEdit.class);
                                    intent.putExtra("comment_home",CommentList.get(position).getComment());
                                    intent.putExtra("com_img_home",CommentList.get(position).getUserdp());
                                    intent.putExtra("com_postID_home",CommentList.get(position).getPostID());
                                    intent.putExtra("com_parentID_home", docID);
                                    intent.putExtra("com_docID_home",CommentList.get(position).getDocID());
                                    intent.putExtra("com_bool", bool_comment);
                                    intent.putExtra("from", "no");
                                    intent.putExtra("type", CommentList.get(position).getType());
                                    intent.putExtra("gender", CommentList.get(position).getGender());
                                    startActivity(intent);
                                 //   Toast.makeText(getApplicationContext(), postCampus+ postID + docID+CommentList.get(position).getDocID() , Toast.LENGTH_LONG).show();

//                                    final Intent intent1 = getIntent();
//                                    String updated = intent1.getStringExtra("update");
//                                    Toast.makeText(getApplicationContext(),updated,Toast.LENGTH_LONG).show();

//                                    commentRef.document(CommentList.get(position).getDocID())
//                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
//                                            .addOnSuccessListener(aVoid -> {
//                                                BasicUtility.showToast(ViewMoreHome.this, "Comment has been reported.");
//                                            });
                                    commentMenuDialog.dismiss();

                                }
                        );
                    }
                    commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                    commentMenuDialog.findViewById(R.id.delete_post).setVisibility(View.VISIBLE);
                    commentMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v -> {
                        progressDialog = new ProgressDialog(CommentReplyActivity.this);
                        progressDialog.setTitle("Deleting Comment");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        ///////////////////BATCH WRITE///////////////////
                        WriteBatch batch = FirebaseFirestore.getInstance().batch();

                        DocumentReference cmtDoc = commentColRef.document(CommentList.get(position).getDocID());
                        batch.delete(cmtDoc);
                        batch.update(commentDocRef, "rCmtNo", FieldValue.increment(-1));
                        batch.update(docRef, "cmtNo", FieldValue.increment(-1));

                        batch.commit().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                ViewMoreHome.commentChanged = 1;
//                                ViewMoreSlider.commentChanged = 1;
                                CommentList.remove(position);
                                adapter.notifyItemRemoved(position);

                                commentCount--;
                                repliedByNo.setText(String.valueOf(commentCount));
                                if(commentCount == 0) {
                                    replyComment.setImageResource(R.drawable.ic_conch_shell);
                                }

                                progressDialog.dismiss();
                            }
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(CommentReplyActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                            }

                        });
                        ///////////////////BATCH WRITE///////////////////

                        if(CommentList.size() == 0){
                            replyComment.setImageResource(R.drawable.ic_conch_shell);
                            repliedByNo.setText("0");
                        }
                        commentMenuDialog.dismiss();
                    });

                    commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v ->
                            {
                                commentColRef.document(CommentList.get(position).getDocID())
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> {
                                            BasicUtility.showToast(CommentReplyActivity.this, "Comment has been reported.");
                                        });
                                commentMenuDialog.dismiss();

                            }
                    );


                }
                else {
                    commentMenuDialog= new BottomSheetDialog(CommentReplyActivity.this);
                    commentMenuDialog.setContentView(R.layout.dialog_comment_menu);
                    commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                    commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v ->
                            {
                                commentColRef.document(CommentList.get(position).getDocID())
                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                        .addOnSuccessListener(aVoid -> {
                                            BasicUtility.showToast(CommentReplyActivity.this, "Comment has been reported.");
                                        });
                                commentMenuDialog.dismiss();
                            }
                    );

                }

                Objects.requireNonNull(commentMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                commentMenuDialog.show();
            }
        });
        mRecyclerView.setAdapter(adapter);

        if(Integer.parseInt(i.getStringExtra("ReplyCommentNo")) > 0L) {
            buildCommentRecyclerView();
        }


        replyComment.setOnClickListener(v -> {
            newComment.requestFocus();
            newComment.setFocusableInTouchMode(true);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(newComment, InputMethodManager.SHOW_IMPLICIT);
            ///////////ENABLE KEYBOARD//////////
        });

        send.setOnClickListener(v -> {
            if(InternetConnection.checkConnection(getApplicationContext())){
                if(newComment.getText().toString().isEmpty()){
                    BasicUtility.showToast(getApplicationContext(), "Thoughts need to be typed...");
                }
                else {
                    send.setVisibility(View.GONE);
                    progressComment.setVisibility(View.VISIBLE);
                    String comment = newComment.getText().toString().trim();
                    long tsLong = System.currentTimeMillis();
//                    String ts = Long.toString(tsLong);
                    ReplyCommentModel commentModel1 = new ReplyCommentModel();

                    commentModel1.setComment(comment);
                    commentModel1.setUid(UID);
                    commentModel1.setPostUid(i.getStringExtra("postUid"));
                    commentModel1.setUserdp(PROFILEPIC);
                    commentModel1.setUsername(USERNAME);
                    commentModel1.setTs(0L); ///Pending state
                    commentModel1.setPostID(postID);
                    commentModel1.setpComID(docID);
                    commentModel1.setType(introPref.getType());
                    commentModel1.setComUid(i.getStringExtra("pComUid"));
                    commentModel1.setGender(introPref.getGender());

                    newComment.setText("");
                    CommentList.add(0,commentModel1);
                    adapter.notifyItemInserted(0);

                    ///////////////////BATCH WRITE///////////////////
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                    DocumentReference cmtDoc = commentColRef.document(Long.toString(tsLong));
                    commentModel1.setTs(tsLong);
                    commentModel1.setDocID(Long.toString(tsLong));

                    batch.set(cmtDoc, commentModel1);
                    batch.update(commentDocRef, "rCmtNo", FieldValue.increment(1));
                    batch.update(docRef, "cmtNo", FieldValue.increment(1));

                    if(!bool.matches("3")) {
                        if(!UID.matches(i.getStringExtra("postUid"))) {
                            batch.update(docRef, "newTs", tsLong);
                        }
                    }

                    batch.commit().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            ViewMoreHome.commentChanged = 1;
//                            ViewMoreSlider.commentChanged = 1;
                            commentCount++;
                            repliedByNo.setText(String.valueOf(commentCount));
                            send.setVisibility(View.VISIBLE);
                            progressComment.setVisibility(View.GONE);
                        }
                        else {
                            commentModel1.setTs(0L); ///Pending state
                            CommentList.remove(commentModel1);
                            commentModel1.setTs(-1L);
                            CommentList.add(0, commentModel1);
                            adapter.notifyDataSetChanged();
                            send.setVisibility(View.VISIBLE);
                            progressComment.setVisibility(View.GONE);
                            Toast.makeText(CommentReplyActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                        }

                    });
                    ///////////////////BATCH WRITE///////////////////
                }
            }
            else {
                BasicUtility.showToast(getApplicationContext(), "Network unavailable...");
            }
        });

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(v, scrollX, scrollY, oldScrollX, oldScrollY) ->{
            if(v.getChildAt(v.getChildCount() - 1) != null){
                if((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY){
                    if(checkGetMore != -1){
                        if(progressBar.getVisibility() == View.GONE){
                            progressBar.setVisibility(View.VISIBLE);
                            fetchMore();//Load more data
                        }
                    }
                }
            }
        });
    }

    private void buildCommentRecyclerView(){

        CommentList.clear();

        commentColRef.orderBy("ts", Query.Direction.DESCENDING).limit(10).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document: task.getResult()){
                            ReplyCommentModel replyCommentModel = document.toObject(ReplyCommentModel.class);
                            replyCommentModel.setDocID(document.getId());
                            CommentList.add(replyCommentModel);
                        }
                        if(CommentList.size()>0) {
                            adapter.notifyDataSetChanged();

                            if(task.getResult().size()>0)
                                lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                            if(CommentList.size()<10){
                                repliedByNo.setText(String.valueOf(CommentList.size()));
                                checkGetMore = -1;
                            }
                            else {
                                checkGetMore = 0;
                            }

                        }
                        else{
                            checkGetMore = -1;
                            repliedByNo.setText("0");
                            progressBar.setVisibility(View.GONE);
                            replyComment.setImageResource(R.drawable.ic_conch_shell);
                        }
                    }
                    else {
                        BasicUtility.showToast(getApplicationContext(),"Something went wrong...");
                    }

                    progressBar.setVisibility(View.GONE);
                });
    }

    private void fetchMore(){
        progressBar.setVisibility(View.VISIBLE);
        Query nextQuery = commentColRef.orderBy("ts", Query.Direction.DESCENDING).startAfter(lastVisible).limit(10);

        nextQuery.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                ArrayList<ReplyCommentModel> commentModels = new ArrayList<>();
                for (DocumentSnapshot d : t.getResult()) {
                    ReplyCommentModel commentModel = d.toObject(ReplyCommentModel.class);
                    assert commentModel != null;
                    commentModel.setDocID(d.getId());
                    commentModels.add(commentModel);

                }

                if(commentModels.size()>0){
                    int lastSize = CommentList.size();
                    CommentList.addAll(commentModels);
                    adapter.notifyItemRangeInserted(lastSize, commentModels.size());
                    lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                }

                progressBar.setVisibility(View.GONE);

                if(commentModels.size()<10){
                    checkGetMore = -1;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        if(change > 0 && getIntent().getStringExtra("edited") != null) {
            comment.setText(getIntent().getStringExtra("edited"));
            if(comment.getUrls().length>0) {
                URLSpan urlSnapItem = comment.getUrls()[0];
                String url = urlSnapItem.getURL();
                if (url.contains("http")) {
                    linkPreview.setLink(url, new ViewListener() {
                        @Override
                        public void onSuccess(boolean status) {

                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
                }
            }
            buildCommentRecyclerView();
            change = 0;
        }
        else if(change > 0) {
            buildCommentRecyclerView();
            change = 0;
        }
        super.onResume();

        if(mode_changed == 1) {
            mode_changed = 0;
            startActivity(new Intent(CommentReplyActivity.this, CommentReplyActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }
}