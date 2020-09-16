package com.example.pujo360;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.example.pujo360.LinkPreview.ApplexLinkPreview;
import com.example.pujo360.LinkPreview.ViewListener;
import com.example.pujo360.adapters.CommentAdapter;
import com.example.pujo360.adapters.TagAdapter;
import com.example.pujo360.models.CommentModel;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.models.HomePostModel;
import com.example.pujo360.models.NotifCount;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.BottomFlamedByDialog;
import com.example.pujo360.util.InternetConnection;
import com.example.pujo360.util.StoreTemp;
import com.example.pujo360.util.Utility;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class ViewMoreHome extends AppCompatActivity {

    private ImageView send;
    private EditText newComment;
    private ImageView displaypic, commentimg, userimage, userimage_comment, no_comment , flameimg, back;

    private TextView username, minsago,  flamedBy, noofcmnts, comName;
    private ReadMoreTextView textContent;
    private int LikeCheck = -1;
    private int change = 0;
    public static int changed = 0;
    public static int commentChanged = 0;
    private ApplexLinkPreview linkPreview;

    private ProgressDialog progressDialog;

    private ProgressBar progressComment;
    private ProgressBar progressBar;
    private DocumentSnapshot lastVisible;
    private int checkGetMore = -1;

    private RecyclerView mRecyclerView;
    private RecyclerView tagRecycler;
    private ArrayList<CommentModel> CommentList;
    private CommentAdapter adapter;

    private IntroPref introPref;
    private String PROFILEPIC;
    private String USERNAME, UID;

    private ArrayList<String> likeList;

    private CollectionReference commentRef;
    private DocumentReference docRef;
    private CollectionReference flamedRef;
    DocumentReference docref3;

    private BottomSheetDialog commentMenuDialog;
    private BottomSheetDialog postMenuDialog;

    private ImageView more,share;
//    private NotificationFragment instance;

    private int commentCount = 0;
    String bool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_more_home);

        introPref = new IntroPref(this);

        mRecyclerView = findViewById(R.id.comments_recycler);
        send = findViewById(R.id.send_comment);
        share = findViewById(R.id.share44);
        newComment = findViewById(R.id.new_comment);
        displaypic = findViewById(R.id.post_image44);
        commentimg = findViewById(R.id.comment44);
        username = findViewById(R.id.username44);
        userimage = findViewById(R.id.user_image44);
        minsago = findViewById(R.id.mins_ago44);
        textContent = findViewById(R.id.text_content44);
        flamedBy = findViewById(R.id.flamed_by44);
        noofcmnts = findViewById(R.id.no_of_comments44);
        tagRecycler = findViewById(R.id.tagsList_recycler44);
        userimage_comment= findViewById(R.id.user_image_comment);
        no_comment = findViewById(R.id.no_comment44);
        flameimg = findViewById(R.id.flame44);
        back = findViewById(R.id.back);
        linkPreview = findViewById(R.id.LinkPreView);
        more = findViewById(R.id.delete_post);
        progressBar = findViewById(R.id.progress_more1);
        progressComment= findViewById(R.id.commentProgress);
        comName = findViewById(R.id.comName);

        NestedScrollView nestedScrollView = findViewById(R.id.scrollView1);
        nestedScrollView.setNestedScrollingEnabled(true);

        UID = FirebaseAuth.getInstance().getUid();
        PROFILEPIC = introPref.getUserdp();
        USERNAME = introPref.getFullName();

        likeList = new ArrayList<>();

        //////////////////CURRENT USER DETAILS///////////////////
        if(PROFILEPIC != null){
//            if(PROFILEPIC.matches("0")){
//                userimage_comment.setImageResource(R.drawable.default_dp_1);
//            }
//            else if(PROFILEPIC.matches("1")){
//                userimage_comment.setImageResource(R.drawable.default_dp_2);
//            }
//            else if(PROFILEPIC.matches("2")){
//                userimage_comment.setImageResource(R.drawable.default_dp_3);
//            }
//            else if(PROFILEPIC.matches("3")){
//                userimage_comment.setImageResource(R.drawable.default_dp_4);
//            }
//            else if(PROFILEPIC.matches("4")){
//                userimage_comment.setImageResource(R.drawable.default_dp_5);
//            }
//            else if(PROFILEPIC.matches("5")){
//                userimage_comment.setImageResource(R.drawable.default_dp_6);
//            }
//            else if(PROFILEPIC.matches("6")){
//                userimage_comment.setImageResource(R.drawable.default_dp_7);
//            }
//            else if(PROFILEPIC.matches("7")){
//                userimage_comment.setImageResource(R.drawable.default_dp_8);
//            }
//            else if(PROFILEPIC.matches("8")){
//                userimage_comment.setImageResource(R.drawable.default_dp_9);
//            }
//            else if(PROFILEPIC.matches("9")){
//                userimage_comment.setImageResource(R.drawable.default_dp_10);
//            }
//            else {
                Picasso.get().load(PROFILEPIC).into(userimage_comment, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                });
//            }

        }

        //////////////////CURRENT USER DETAILS///////////////////

        Intent i= getIntent();

        final HomePostModel[] homePostModel = {new HomePostModel()};

        if(getIntent().getExtras().getString("campus") == null){
            homePostModel[0].setUid(i.getStringExtra("uid"));
            homePostModel[0].setTs(Long.parseLong(i.getStringExtra("timestamp")));
          //  homePostModel[0].setNewTs(Long.parseLong(i.getStringExtra("newTs")));
            if(i.getStringExtra("newTs")!=null){
                homePostModel[0].setNewTs(Long.parseLong(i.getStringExtra("newTs")));
            }

            minsago.setText(Utility.getTimeAgo(homePostModel[0].getTs()));
            homePostModel[0].setDocID(i.getStringExtra("docID"));

            //SETTING DATABASE REF WRT BOOL VALUE//
//            bool = i.getStringExtra("bool");
//            if(bool.matches("2")|| bool.matches("0")|| bool.matches("1")){
//                //Global
//                postCampus = "Global";
//                if(i.getStringExtra("institute") != null) {
//                    comName.setVisibility(View.VISIBLE);
//                    comName.setBackground(null);
//                    comName.setTextColor(getResources().getColor(android.R.color.black));
//                    comName.setText(i.getStringExtra("institute"));
//                    comName.setEllipsize(TextUtils.TruncateAt.END);
//                }
//                commentRef = FirebaseFirestore.getInstance().collection("Home/Global/Feeds/"+ homePostModel[0].getDocID()+"/commentL/");
//                docRef = FirebaseFirestore.getInstance().document("Home/Global/Feeds/"+ homePostModel[0].getDocID()+"/");
//                flamedRef = FirebaseFirestore.getInstance().collection("Home/Global/Feeds/"+homePostModel[0].getDocID()+"/flameL/");
//            }
//            else if(bool.matches("3")|| bool.matches("4")){
//                //from campus
//                commentRef = FirebaseFirestore.getInstance().collection("Home/"+CAMPUSNAME+"/Feeds/"+ homePostModel[0].getDocID()+"/commentL");
//                docRef = FirebaseFirestore.getInstance().document("Home/"+CAMPUSNAME+"/Feeds/"+ homePostModel[0].getDocID()+"/");
//                flamedRef = FirebaseFirestore.getInstance().collection("Home/"+CAMPUSNAME+"/Feeds/"+homePostModel[0].getDocID()+"/flameL/");
//
//            }
            //SETTING DATABASE REF WRT BOOL VALUE//

            commentRef = FirebaseFirestore.getInstance().collection("Feeds/"+ homePostModel[0].getDocID()+"/commentL");
            docRef = FirebaseFirestore.getInstance().document("Feeds/"+ homePostModel[0].getDocID()+"/");
            flamedRef = FirebaseFirestore.getInstance().collection("Feeds/"+homePostModel[0].getDocID()+"/flameL/");

            /////////////USERNAME & USER IMAGE FOR POST//////////////
            homePostModel[0].setUsN(i.getStringExtra("username"));
            username.setText(homePostModel[0].getUsN());
//            if(homePostModel[0].getUsN().matches("Anonymous")){
//                userimage.setImageResource(R.drawable.ic_anonymous_icon);
//            }
//            else {
                ///////////////////NOT ANONYMOUS/////////////////
                homePostModel[0].setDp(i.getStringExtra("userdp"));
                if(homePostModel[0].getDp()!= null && !homePostModel[0].getDp().isEmpty()){
//                    if(homePostModel[0].getDp().matches("0")){
//                        userimage.setImageResource(R.drawable.default_dp_1);
//                    }
//                    else if(homePostModel[0].getDp().matches("1")){
//                        userimage.setImageResource(R.drawable.default_dp_2);
//                    }
//                    else if(homePostModel[0].getDp().matches("2")){
//                        userimage.setImageResource(R.drawable.default_dp_3);
//                    }
//                    else if(homePostModel[0].getDp().matches("3")){
//                        userimage.setImageResource(R.drawable.default_dp_4);
//                    }
//                    else if(homePostModel[0].getDp().matches("4")){
//                        userimage.setImageResource(R.drawable.default_dp_5);
//                    }
//                    else if(homePostModel[0].getDp().matches("5")){
//                        userimage.setImageResource(R.drawable.default_dp_6);
//                    }
//                    else if(homePostModel[0].getDp().matches("6")){
//                        userimage.setImageResource(R.drawable.default_dp_7);
//                    }
//                    else if(homePostModel[0].getDp().matches("7")){
//                        userimage.setImageResource(R.drawable.default_dp_8);
//                    }
//                    else if(homePostModel[0].getDp().matches("8")){
//                        userimage.setImageResource(R.drawable.default_dp_9);
//                    }
//                    else if(homePostModel[0].getDp().matches("9")){
//                        userimage.setImageResource(R.drawable.default_dp_10);
//                    }
//                    else {
                        Picasso.get().load(homePostModel[0].getDp()).placeholder(R.drawable.ic_account_circle_black_24dp).into(userimage);
//                    }
                }
                else {
                    userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                username.setOnClickListener(v -> {
                    Intent i12 = new Intent(getApplicationContext(), ProfileActivity.class);
                    i12.putExtra("uid", homePostModel[0].getUid());
                    startActivity(i12);
                });

                userimage.setOnClickListener(v -> {
                    Intent i1 = new Intent(getApplicationContext(), ProfileActivity.class);
                    i1.putExtra("uid", homePostModel[0].getUid());
                    startActivity(i1);
                });
//            }
            /////////////USERNAME & USER IMAGE FORE POST//////////////


            /////////////////TAGS/////////////////
            if(StoreTemp.getInstance().getTagTemp() != null){
                homePostModel[0].setTagL(StoreTemp.getInstance().getTagTemp());
                if(homePostModel[0].getTagL().size()>0){
                    tagRecycler.setHasFixedSize(true);
                    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    tagRecycler.setLayoutManager(linearLayoutManager);
                    TagAdapter tagAdapter = new TagAdapter(homePostModel[0].getTagL(), getApplicationContext());
                    tagRecycler.setAdapter(tagAdapter);
                }
                else {
                    tagRecycler.setVisibility(View.GONE);
                }
            }
            else {
                tagRecycler.setVisibility(View.GONE);
            }
            /////////////////TAGS/////////////////


            ////////////COMMUNITY//////////
            if(getIntent().getStringExtra("comName")!=null && getIntent().getStringExtra("comID") !=null){
                comName.setVisibility(View.VISIBLE);
                homePostModel[0].setComID(getIntent().getStringExtra("comID"));
                homePostModel[0].setComName(getIntent().getStringExtra("comName"));

                comName.setText(getIntent().getStringExtra("comName"));
                comName.setBackground(getResources().getDrawable(R.drawable.custom_com_backgnd));
                comName.setTextColor(getResources().getColor(android.R.color.white));
                comName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(ViewMoreHome.this, CommunityActivity.class);
                        intent.putExtra("comID", homePostModel[0].getComID());

                        startActivity(intent);
                    }
                });
            }
            ////////////COMMUNITY//////////



            ///////////////LIKE SETUP//////////////
            if(i.getSerializableExtra("likeL") != null) {
                likeList = (ArrayList<String>) i.getSerializableExtra("likeL");
                /////////////////UPDATNG FLAMED BY NO.//////////////////////
                if(likeList.size() == 0){
                    flamedBy.setText("Not flamed yet");
                }
                else if(likeList.size() == 1)
                    flamedBy.setText("Flamed by 1");
                else {
                    flamedBy.setText("Flamed by "+likeList.size()+" people");
                }

                for(int j = 0; j < likeList.size(); j++){
                    if(likeList.get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){
                        flameimg.setImageResource(R.drawable.ic_flame_red);
                        LikeCheck = j;
                        if((likeList.size()-1) == 1)
                            flamedBy.setText("Flamed by you & "+ (likeList.size()-1) +" other");
                        else if((likeList.size()-1) == 0){
                            flamedBy.setText("Flamed by you");
                        }
                        else
                            flamedBy.setText("Flamed by you & "+ (likeList.size()-1) +" others");
                        //Position in likeList where the current USer UId is found stored in likeCheck
                    }
                }
            }
            else
                flamedBy.setText("Not flamed yet");

            ///////////When viewing likelist from fragment global/campus////////////////
            if(i.getStringExtra("likeLOpen")!=null && i.getStringExtra("likeLOpen").matches("likeLOpen")) {
                if(likeList!=null && likeList.size() > 0){
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Home", homePostModel[0].getDocID());
                    bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                }
                else
                    Toast.makeText(ViewMoreHome.this, "No flames", Toast.LENGTH_SHORT).show();
            }
            ///////////When viewing likelist from fragment global/campus////////////////

            flamedBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(likeList!=null && likeList.size() > 0){
                        BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Home", homePostModel[0].getDocID());
                        bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                    }
                    else
                        Toast.makeText(ViewMoreHome.this, "No flames", Toast.LENGTH_SHORT).show();
                }
            });
            ///////////////LIKE SETUP//////////////

            ////////////////POST PIC///////////////
            if(i.getStringExtra("postPic")!=null && !i.getStringExtra("postPic").isEmpty()){
                homePostModel[0].setImg(i.getStringExtra("postPic"));
                displaypic.setVisibility(View.VISIBLE);
                Picasso.get().load(homePostModel[0].getImg()).placeholder(R.drawable.image_background_grey).into(displaypic);
                displaypic.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Picasso.get().load(homePostModel[0].getImg()).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                save_Dialog(bitmap);
                            }
                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }

                        });
                        return true;
                    }
                });

            }
            else {
                displaypic.setVisibility(View.GONE);
            }
            ////////////////POST PIC///////////////


            ////////////////POST TEXT///////////////
            if(i.getStringExtra("postText")!=null && !i.getStringExtra("postText").isEmpty()){
                homePostModel[0].setTxt(i.getStringExtra("postText"));
                textContent.setVisibility(View.VISIBLE);
                textContent.setText(homePostModel[0].getTxt());

                if(textContent.getUrls().length>0){
                    URLSpan urlSnapItem = textContent.getUrls()[0];
                    String url = urlSnapItem.getURL();
                    if(url.contains("http")){
                        linkPreview.setVisibility(View.VISIBLE);
                        linkPreview.setLink(url ,new ViewListener() {
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
            }
            else {
                textContent.setVisibility(View.GONE);
            }
            ////////////////POST TEXT///////////////


            //////////////COMMENT SETUP from cmtNo////////////
            LinearLayoutManager layoutManager = new LinearLayoutManager(ViewMoreHome.this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setNestedScrollingEnabled(true);

            if(i.getStringExtra("commentNo")!=null){
                homePostModel[0].setCmtNo(Long.parseLong(i.getStringExtra("commentNo")));
                if(homePostModel[0].getCmtNo()>0){
                    commentimg.setImageResource(R.drawable.comment_yellow);
                    if(homePostModel[0].getCmtNo()>1){
                        noofcmnts.setText(i.getStringExtra("commentNo")+" comments");
                    }
                    else {
                        noofcmnts.setText(i.getStringExtra("commentNo")+" comment");
                    }
                    no_comment.setVisibility(View.GONE);
                }
                else {
//                    mRecyclerView.setVisibility(View.GONE);
                    no_comment.setVisibility(View.VISIBLE);
                    noofcmnts.setText("No comments yet");
                    checkGetMore = -1;
                }
                commentCount = Integer.parseInt(i.getStringExtra("commentNo"));
            }
            else {
//                mRecyclerView.setVisibility(View.GONE);
                no_comment.setVisibility(View.VISIBLE);
                noofcmnts.setText("No comments yet");
                commentCount = 0;
                checkGetMore = -1;
            }

            CommentList = new ArrayList<>();
            adapter = new CommentAdapter(ViewMoreHome.this, CommentList, 1);
            adapter.onClickListener(new CommentAdapter.OnClickListener() {
                @Override
                public void onClickListener(int position) {

                    if( CommentList.get(position).getUid().matches(FirebaseAuth.getInstance().getUid())
                            || homePostModel[0].getUid().matches(FirebaseAuth.getInstance().getUid())){
                        commentMenuDialog= new BottomSheetDialog(ViewMoreHome.this);
                        commentMenuDialog.setContentView(R.layout.dialog_comment_menu2);
                        if( CommentList.get(position).getUid().matches(FirebaseAuth.getInstance().getUid())) {
                            commentMenuDialog.findViewById(R.id.edit_comment).setVisibility(View.VISIBLE);
                            commentMenuDialog.findViewById(R.id.edit_comment).setOnClickListener(v ->
                                    {
                                        Intent intent = new Intent(ViewMoreHome.this, CommentEdit.class);
                                        intent.putExtra("comment_home",CommentList.get(position).getComment());
                                        intent.putExtra("com_img_home",CommentList.get(position).getUserdp());
                                        intent.putExtra("com_postID_home",CommentList.get(position).getPostID());
                                        intent.putExtra("com_docID_home",CommentList.get(position).getDocID());
                                        intent.putExtra("com_bool",i.getStringExtra("bool"));
                                        intent.putExtra("from", "no");

                                        startActivity(intent);

//                                    final Intent intent1 = getIntent();
//                                    String updated = intent1.getStringExtra("update");
//                                    Toast.makeText(getApplicationContext(),updated,Toast.LENGTH_LONG).show();

//                                    commentRef.document(CommentList.get(position).getDocID())
//                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
//                                            .addOnSuccessListener(aVoid -> {
//                                                Utility.showToast(ViewMoreHome.this, "Comment has been reported.");
//                                            });
                                        commentMenuDialog.dismiss();

                                    }
                            );
                        }
                        commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                        commentMenuDialog.findViewById(R.id.delete_post).setVisibility(View.VISIBLE);
                        commentMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v -> {
                            progressDialog = new ProgressDialog(ViewMoreHome.this);
                            progressDialog.setTitle("Deleting Comment");
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            int total = CommentList.get(position).getrCmtNo() + 1;

                            DocumentReference cmtDoc = commentRef.document(CommentList.get(position).getDocID());
                            batch.delete(cmtDoc);
                            batch.update(docRef, "cmtNo", FieldValue.increment(-(total)));

                            batch.commit().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    change = 1;
                                    CommentList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    if(adapter.getItemCount() == 0){
                                        no_comment.setVisibility(View.VISIBLE);
                                    }

                                    commentCount--;
                                    if(commentCount == 0)
                                        noofcmnts.setText("No comments yet");
                                    else if(commentCount==1)
                                        noofcmnts.setText(commentCount+ " comment");
                                    else if(CommentList.size()>1)
                                        noofcmnts.setText(commentCount+ " comments");

                                    progressDialog.dismiss();
                                }
                                else {
                                    progressDialog.dismiss();
                                    Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }

                            });
                            ///////////////////BATCH WRITE///////////////////

                            if(CommentList.size() == 0){
                                commentimg.setImageResource(R.drawable.ic_comment);
                                no_comment.setVisibility(View.VISIBLE);
                            }
                            commentMenuDialog.dismiss();
                        });

                        commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v ->
                                {
                                    commentRef.document(CommentList.get(position).getDocID())
                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                            .addOnSuccessListener(aVoid -> {
                                                Utility.showToast(ViewMoreHome.this, "Comment has been reported.");
                                            });
                                    commentMenuDialog.dismiss();

                                }
                        );


                    }
                    else {
                        commentMenuDialog= new BottomSheetDialog(ViewMoreHome.this);
                        commentMenuDialog.setContentView(R.layout.dialog_comment_menu);
                        commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                        commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v ->
                                {
                                    commentRef.document(CommentList.get(position).getDocID())
                                            .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                            .addOnSuccessListener(aVoid -> {
                                                Utility.showToast(ViewMoreHome.this, "Comment has been reported.");
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

            buildCommentRecyclerView();


        }

        else {// from fcm notification or notiff tab or external link
            docref3= FirebaseFirestore.getInstance()
                    .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
                    .document("notifCount");
            final NotifCount[] notifCount = {new NotifCount()};
            docref3.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()) {
                            notifCount[0] = documentSnapshot.toObject(NotifCount.class);
                            if(notifCount[0].getNotifCount() > 0) {
                                docref3.update("notifCount", FieldValue.increment(-1));
                            }
                        }
                    }
                }
            });
            String postID;
            String bool;

            commentRef = FirebaseFirestore.getInstance().collection("Feeds/"+ homePostModel[0].getDocID()+"/commentL");
            docRef = FirebaseFirestore.getInstance().document("Feeds/"+ homePostModel[0].getDocID()+"/");
            flamedRef = FirebaseFirestore.getInstance().collection("Feeds/"+ homePostModel[0].getDocID()+"/flameL");
            postID = getIntent().getExtras().getString("postID");


//            campus = getIntent().getExtras().getString("campus");
//            if(campus.matches("Global")){
//                bool = "2";
//                if(homePostModel[0].getInstitute() != null) {
//                    comName.setVisibility(View.VISIBLE);
//                    comName.setText(homePostModel[0].getInstitute());
//                    comName.setBackground(getResources().getDrawable(R.drawable.custom_inst_backgnd));
//                    comName.setEllipsize(TextUtils.TruncateAt.END);
//                }
//            }
//            else {
//                bool = "3";
//            }


            FirebaseFirestore.getInstance().document("Feeds/"+postID+"/").get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.getResult().exists()){
                                homePostModel[0] = task.getResult().toObject(HomePostModel.class);
                                homePostModel[0].setDocID(task.getResult().getId());

                                //SETTING DATABASE REF WRT BOOL VALUE//
                                commentRef = FirebaseFirestore.getInstance().collection("Feeds/"+ homePostModel[0].getDocID()+"/commentL");
                                docRef = FirebaseFirestore.getInstance().document("Feeds/"+ homePostModel[0].getDocID()+"/");
                                flamedRef = FirebaseFirestore.getInstance().collection("Feeds/"+ homePostModel[0].getDocID()+"/flameL");
                                //SETTING DATABASE REF WRT BOOL VALUE//

                                minsago.setText(Utility.getTimeAgo(homePostModel[0].getTs()));

                                ////////////COMMUNITY/////////
                                if(homePostModel[0].getComName()!=null){
                                    comName.setVisibility(View.VISIBLE);
                                    comName.setBackground(getResources().getDrawable(R.drawable.custom_com_backgnd));
                                    comName.setText(homePostModel[0].getComName());
                                    comName.setTextColor(getResources().getColor(R.color.white));
                                    comName.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent intent= new Intent(ViewMoreHome.this, CommunityActivity.class);
                                            intent.putExtra("comID", homePostModel[0].getComID());
                                            startActivity(intent);

                                        }
                                    });
                                }
                                ////////////COMMUNITY//////////


                                /////////////USERNAME & USER IMAGE FORE POST//////////////
                                username.setText(homePostModel[0].getUsN());
    //                                    if(homePostModel[0].getUsN().matches("Anonymous")){
    //                                        userimage.setImageResource(R.drawable.ic_anonymous_icon);
    //                                    }
    //                                    else {
                                    ///////////////////NOT ANONYMOUS/////////////////
                                    if(homePostModel[0].getDp()!= null && !homePostModel[0].getDp().isEmpty()){
    //                                            if(homePostModel[0].getDp().matches("0")){
    //                                                userimage.setImageResource(R.drawable.default_dp_1);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("1")){
    //                                                userimage.setImageResource(R.drawable.default_dp_2);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("2")){
    //                                                userimage.setImageResource(R.drawable.default_dp_3);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("3")){
    //                                                userimage.setImageResource(R.drawable.default_dp_4);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("4")){
    //                                                userimage.setImageResource(R.drawable.default_dp_5);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("5")){
    //                                                userimage.setImageResource(R.drawable.default_dp_6);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("6")){
    //                                                userimage.setImageResource(R.drawable.default_dp_7);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("7")){
    //                                                userimage.setImageResource(R.drawable.default_dp_8);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("8")){
    //                                                userimage.setImageResource(R.drawable.default_dp_9);
    //                                            }
    //                                            else if(homePostModel[0].getDp().matches("9")){
    //                                                userimage.setImageResource(R.drawable.default_dp_10);
    //                                            }
    //                                            else {
                                            Picasso.get().load(homePostModel[0].getDp()).placeholder(R.drawable.ic_account_circle_black_24dp).into(userimage);
    //                                            }
                                    }
                                    else {
                                        userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }

                                    username.setOnClickListener(v -> {
                                        Intent i12 = new Intent(getApplicationContext(), ProfileActivity.class);
                                        i12.putExtra("uid", homePostModel[0].getUid());
                                        startActivity(i12);
    //                                        finish();
                                    });

                                    userimage.setOnClickListener(v -> {
                                        Intent i1 = new Intent(getApplicationContext(), ProfileActivity.class);
                                        i1.putExtra("uid", homePostModel[0].getUid());
                                        startActivity(i1);
    //                                        finish();

                                    });
    //                                    }
                                /////////////USERNAME & USER IMAGE FORE POST//////////////

                                /////////////////TAGS/////////////////
                                if(homePostModel[0].getTagL() != null){
                                    if(homePostModel[0].getTagL().size()>0){
                                        tagRecycler.setHasFixedSize(true);
                                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                                        tagRecycler.setLayoutManager(linearLayoutManager);
                                        TagAdapter tagAdapter = new TagAdapter(homePostModel[0].getTagL(), getApplicationContext());
                                        tagRecycler.setAdapter(tagAdapter);
                                    }
                                    else {
                                        tagRecycler.setVisibility(View.GONE);
                                    }
                                }
                                else {
                                    tagRecycler.setVisibility(View.GONE);
                                }
                                /////////////////TAGS/////////////////


                                ///////////////LIKE SETUP//////////////
                                if(homePostModel[0].getLikeL() != null) {
                                    likeList = homePostModel[0].getLikeL();
                                    /////////////////UPDATNG FLAMED BY NO.//////////////////////
                                    if(likeList.size() == 0){
                                        flamedBy.setText("Not flamed yet");
                                    }
                                    else if(likeList.size() == 1)
                                        flamedBy.setText("Flamed by 1");
                                    else {
                                        flamedBy.setText("Flamed by "+likeList.size()+" people");
                                    }

                                    for(int j = 0; j < likeList.size(); j++){
                                        if(likeList.get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){
                                            flameimg.setImageResource(R.drawable.ic_flame_red);
                                            LikeCheck = j;
                                            if((likeList.size()-1) == 1)
                                                flamedBy.setText("Flamed by you & "+ (likeList.size()-1) +" other");
                                            else if((likeList.size()-1) == 0){
                                                flamedBy.setText("Flamed by you");
                                            }
                                            else
                                                flamedBy.setText("Flamed by you & "+ (likeList.size()-1) +" others");
                                            //Position in likeList where the current USer UId is found stored in likeCheck
                                        }
                                    }
                                }
                                else
                                    flamedBy.setText("Not flamed yet");

                                ///////////When viewing likelist from fragment global/campus////////////////
                                if(i.getStringExtra("likeLOpen")!=null && i.getStringExtra("likeLOpen").matches("likeLOpen"))
                                {
                                    if(likeList!=null && likeList.size() > 0){
                                        BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Home", homePostModel[0].getDocID());
                                        bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                                    }
                                    else
                                        Toast.makeText(ViewMoreHome.this, "No flames", Toast.LENGTH_SHORT).show();
                                }
                                ///////////When viewing likelist from fragment global/campus////////////////

                                flamedBy.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(likeList!=null && likeList.size() > 0){
                                            BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Home", homePostModel[0].getDocID());
                                            bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                                        }
                                        else
                                            Toast.makeText(ViewMoreHome.this, "No flames", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                ///////////////LIKE SETUP//////////////


                                ////////////////POST PIC///////////////
                                if(homePostModel[0].getImg() != null && !homePostModel[0].getImg().isEmpty()){
                                    displaypic.setVisibility(View.VISIBLE);
                                    Picasso.get().load(homePostModel[0].getImg()).placeholder(R.drawable.image_background_grey).into(displaypic);
                                    displaypic.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            Picasso.get().load(homePostModel[0].getImg()).into(new Target() {
                                                @Override
                                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                    save_Dialog(bitmap);
                                                }
                                                @Override
                                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                                    Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                                }
                                                @Override
                                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                                }

                                            });
                                            return true;
                                        }
                                    });

                                }
                                else {
                                    displaypic.setVisibility(View.GONE);
                                }
                                ////////////////POST PIC///////////////



                                ////////////////POST TEXT///////////////
                                if(homePostModel[0].getTxt() != null && !homePostModel[0].getTxt().isEmpty()){
                                    textContent.setVisibility(View.VISIBLE);
                                    textContent.setText(homePostModel[0].getTxt());
                                    if(textContent.getUrls().length>0){
                                        URLSpan urlSnapItem = textContent.getUrls()[0];
                                        String url = urlSnapItem.getURL();
                                        if(url.contains("http")){
                                            linkPreview.setVisibility(View.VISIBLE);
                                            linkPreview.setLink(url ,new ViewListener() {
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
                                }
                                else {
                                    textContent.setVisibility(View.GONE);
                                }
                                ////////////////POST TEXT///////////////

                                //////////////COMMENT SETUP from cmtNo////////////
                                if(homePostModel[0].getCmtNo() > -1){

                                    if(homePostModel[0].getCmtNo()>0){
                                        commentimg.setImageResource(R.drawable.comment_yellow);
                                        if(homePostModel[0].getCmtNo()>1){
                                            noofcmnts.setText(homePostModel[0].getCmtNo()+" comments");
                                        }
                                        else {
                                            noofcmnts.setText(homePostModel[0].getCmtNo()+" comment");
                                        }
                                        no_comment.setVisibility(View.GONE);

                                    }
                                    else {
    //                                            mRecyclerView.setVisibility(View.GONE);
                                        no_comment.setVisibility(View.VISIBLE);
                                        noofcmnts.setText("No comments yet");
                                        checkGetMore = -1;
                                    }
                                    commentCount = (int)homePostModel[0].getCmtNo();
                                }
                                else {
    //                                        mRecyclerView.setVisibility(View.GONE);
                                    no_comment.setVisibility(View.VISIBLE);
                                    noofcmnts.setText("No comments yet");
                                    commentCount = 0;
                                    checkGetMore = -1;
                                }
                                //////////////COMMENT SETUP from cmtNo////////////

                                final LinearLayoutManager layoutManager = new LinearLayoutManager(ViewMoreHome.this);
                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                mRecyclerView.setLayoutManager(layoutManager);
                                mRecyclerView.setNestedScrollingEnabled(false);

                                CommentList = new ArrayList<>();
                                adapter = new CommentAdapter(ViewMoreHome.this, CommentList, 1);
                                adapter.onClickListener(new CommentAdapter.OnClickListener() {
                                    @Override
                                    public void onClickListener(int position) {

                                        if( CommentList.get(position).getUid().matches(FirebaseAuth.getInstance().getUid())
                                                || homePostModel[0].getUid().matches(FirebaseAuth.getInstance().getUid())){
                                            commentMenuDialog= new BottomSheetDialog(ViewMoreHome.this);
                                            commentMenuDialog.setContentView(R.layout.dialog_comment_menu2);
                                            if( CommentList.get(position).getUid().matches(FirebaseAuth.getInstance().getUid())) {
                                                commentMenuDialog.findViewById(R.id.edit_comment).setVisibility(View.VISIBLE);
                                                commentMenuDialog.findViewById(R.id.edit_comment).setOnClickListener(v ->
                                                        {
                                                            Intent intent = new Intent(ViewMoreHome.this, CommentEdit.class);
                                                            intent.putExtra("comment_home",CommentList.get(position).getComment());
                                                            intent.putExtra("com_img_home",CommentList.get(position).getUserdp());
                                                            intent.putExtra("com_postID_home",CommentList.get(position).getPostID());
                                                            intent.putExtra("com_docID_home",CommentList.get(position).getDocID());
//                                                            intent.putExtra("com_bool",bool);
                                                            intent.putExtra("from", "no");

                                                            startActivity(intent);

    //                                                                final Intent intent1 = getIntent();
    //                                                                String updated = intent1.getStringExtra("update");
    //                                                                Toast.makeText(getApplicationContext(),updated,Toast.LENGTH_LONG).show();
    //
    //                                                                commentRef.document(CommentList.get(position).getDocID())
    //                                                                        .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
    //                                                                        .addOnSuccessListener(aVoid -> {
    //                                                                            Utility.showToast(ViewMoreHome.this, "Comment has been reported.");
    //                                                                        });
                                                            commentMenuDialog.dismiss();

                                                        }
                                                );
                                            }
                                            commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                                            commentMenuDialog.findViewById(R.id.delete_post).setVisibility(View.VISIBLE);
                                            commentMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v -> {
                                                progressDialog = new ProgressDialog(ViewMoreHome.this);
                                                progressDialog.setTitle("Deleting Comment");
                                                progressDialog.setMessage("Please wait...");
                                                progressDialog.setCancelable(false);
                                                progressDialog.show();
                                                ///////////////////BATCH WRITE///////////////////
                                                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                                int total = CommentList.get(position).getrCmtNo() + 1;

                                                DocumentReference cmtDoc = commentRef.document(CommentList.get(position).getDocID());
                                                batch.delete(cmtDoc);
                                                batch.update(docRef, "cmtNo", FieldValue.increment(-(total)));

                                                batch.commit().addOnCompleteListener(task -> {
                                                    if(task.isSuccessful()){
                                                        change = 1;
                                                        CommentList.remove(position);
                                                        adapter.notifyItemRemoved(position);
                                                        if(adapter.getItemCount() == 0){
                                                            no_comment.setVisibility(View.VISIBLE);
                                                        }

                                                        commentCount--;
                                                        if(commentCount == 0)
                                                            noofcmnts.setText("No comments yet");
                                                        else if(commentCount==1)
                                                            noofcmnts.setText(commentCount+ " comment");
                                                        else if(CommentList.size()>1)
                                                            noofcmnts.setText(commentCount+ " comments");

                                                        progressDialog.dismiss();
                                                    }
                                                    else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                                    }

                                                });
                                                ///////////////////BATCH WRITE//////////////////
                                                if(CommentList.size() == 0){
                                                    commentimg.setImageResource(R.drawable.ic_comment);
                                                    no_comment.setVisibility(View.VISIBLE);
                                                }
                                                commentMenuDialog.dismiss();
                                            });

                                            commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v ->
                                                    {
                                                        commentRef.document(CommentList.get(position).getDocID())
                                                                .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Utility.showToast(ViewMoreHome.this, "Comment has been reported.");
                                                                });
                                                        commentMenuDialog.dismiss();

                                                    }
                                            );

                                        }
                                        else {
                                            commentMenuDialog= new BottomSheetDialog(ViewMoreHome.this);
                                            commentMenuDialog.setContentView(R.layout.dialog_comment_menu);
                                            commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                                            commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v ->
                                                    {
                                                        commentRef.document(CommentList.get(position).getDocID())
                                                                .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Utility.showToast(ViewMoreHome.this, "Comment has been reported.");
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

                                buildCommentRecyclerView();
                            }

                            else {
                                Toast.makeText(getApplicationContext(), "Post has been removed", Toast.LENGTH_SHORT).show();
                                if(getIntent().getStringExtra("position") != null){
//                                    NotificationFragment.removeNotif = Integer.parseInt(getIntent().getStringExtra("position"));
                                }
                                if(isTaskRoot()){
                                    startActivity(new Intent(ViewMoreHome.this, MainActivity.class));
                                }
                                else {
                                    ViewMoreHome.super.onBackPressed();
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
                        if(LikeCheck >= 0){//was already liked by current user
                            flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                            if(likeList.size()-1 == 0){
                                flamedBy.setText("Not flamed yet");
                            }
                            else if(likeList.size()-1 == 1){
                                flamedBy.setText("Flamed by "+ (likeList.size()-1));
                            }
                            else
                                flamedBy.setText("Flamed by "+ (likeList.size()-1) +" people");

                            likeList.remove(FirebaseAuth.getInstance().getUid());
                            LikeCheck = -1;

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();

                            DocumentReference flamedDoc = flamedRef.document(FirebaseAuth.getInstance().getUid());
                            batch.update(docRef, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                            batch.delete(flamedDoc);
                            batch.commit().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    change = 1;
                                }
                                else {
                                    Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }

                            });
                            ///////////////////BATCH WRITE///////////////////

                        }
                        else if(LikeCheck < 0 && likeList!=null){
                            Utility.vibrate(getApplicationContext());
                            flameimg.setImageResource(R.drawable.ic_flame_red);
                            if(likeList.size() == 0)
                                flamedBy.setText("Flamed by you");
                            else if(likeList.size() == 1)
                                flamedBy.setText("Flamed by you & "+likeList.size()+" other");
                            else
                                flamedBy.setText("Flamed by you & "+ likeList.size() +" others");

                            likeList.add(FirebaseAuth.getInstance().getUid());
                            LikeCheck = likeList.size()-1;
//                docRef.update("likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            FlamedModel flamedModel = new FlamedModel();
                            long tsLong = System.currentTimeMillis();

                            flamedModel.setPostID(homePostModel[0].getDocID());
                            flamedModel.setTs(tsLong);
                            flamedModel.setUid(UID);
                            flamedModel.setUserdp(PROFILEPIC);
                            flamedModel.setUsername(USERNAME);
                            flamedModel.setPostUid(homePostModel[0].getUid());

                            DocumentReference flamedDoc = flamedRef.document(FirebaseAuth.getInstance().getUid());
                            batch.update(docRef, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if(likeList.size() % 5 == 0){
                                batch.update(docRef,"newTs", tsLong);
                            }
                            batch.commit().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    change = 1;
                                }
                                else {
                                    Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }

                            });
                            ///////////////////BATCH WRITE///////////////////

                        }
                        else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                            Utility.vibrate(getApplicationContext());
                            flameimg.setImageResource(R.drawable.ic_flame_red);
                            if(likeList!=null)
                                flamedBy.setText("Flamed by you & "+ (likeList.size() + 1) +" people");
                            else
                                flamedBy.setText("Flamed by you");

                            likeList.add(FirebaseAuth.getInstance().getUid());
                            LikeCheck = likeList.size()-1;

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            FlamedModel flamedModel = new FlamedModel();
                            long tsLong = System.currentTimeMillis();

                            flamedModel.setPostID(homePostModel[0].getDocID());
                            flamedModel.setTs(tsLong);
                            flamedModel.setUid(UID);
                            flamedModel.setUserdp(PROFILEPIC);
                            flamedModel.setUsername(USERNAME);
                            flamedModel.setPostUid(homePostModel[0].getUid());

                            DocumentReference flamedDoc = flamedRef.document(FirebaseAuth.getInstance().getUid());
                            batch.update(docRef, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if(likeList.size() % 5 == 0){
                                batch.update(docRef,"newTs", tsLong);
                            }
                            batch.commit().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    change = 1;
                                }
                                else {
                                    Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }

                            });
                            ///////////////////BATCH WRITE///////////////////
                        }
                    }
                });


        commentimg.setOnClickListener(v -> {
            newComment.requestFocus();
            newComment.setFocusableInTouchMode(true);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(newComment, InputMethodManager.SHOW_IMPLICIT);
            ///////////ENABLE KEYBOARD//////////
        });


        send.setOnClickListener(v -> {
            if(InternetConnection.checkConnection(getApplicationContext())){
                if(newComment.getText().toString().isEmpty()){
                    Utility.showToast(getApplicationContext(), "Thoughts need to be typed...");
                }
                else {
                    send.setVisibility(View.GONE);
                    progressComment.setVisibility(View.VISIBLE);
                    String comment = newComment.getText().toString().trim();
                    long tsLong = System.currentTimeMillis();
//                    String ts = Long.toString(tsLong);
                    CommentModel commentModel = new CommentModel();

                    commentModel.setComment(comment);
                    commentModel.setUid(UID);
                    commentModel.setPostUid(homePostModel[0].getUid());
                    commentModel.setUserdp(PROFILEPIC);
                    commentModel.setUsername(USERNAME);
                    commentModel.setTs(0L); ///Pending state
                    commentModel.setPostID(homePostModel[0].getDocID());;

                    newComment.setText("");
                    commentimg.setImageResource(R.drawable.comment_yellow);
                    CommentList.add(0,commentModel);
                    adapter.notifyItemInserted(0);
                    no_comment.setVisibility(View.GONE);

                    ///////////////////BATCH WRITE///////////////////
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                    DocumentReference cmtDoc = commentRef.document(Long.toString(tsLong));
                    commentModel.setTs(tsLong);
                    commentModel.setDocID(Long.toString(tsLong));

                    batch.set(cmtDoc, commentModel);
                    batch.update(docRef, "cmtNo", FieldValue.increment(1));

                    if(!UID.matches(homePostModel[0].getUid())) {
                        batch.update(docRef, "newTs", tsLong);
                    }

                    batch.commit().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            change = 1;
                            commentCount++;
                            if(commentCount==1)
                                noofcmnts.setText(commentCount+ " comment");
                            else if(CommentList.size()>1)
                                noofcmnts.setText(commentCount+ " comments");

                            send.setVisibility(View.VISIBLE);
                            progressComment.setVisibility(View.GONE);
                            mRecyclerView.setAdapter(adapter);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else {
                            commentModel.setTs(0L); ///Pending state
                            CommentList.remove(commentModel);
                            commentModel.setTs(-1L);
                            CommentList.add(0, commentModel);
                            adapter.notifyDataSetChanged();
                            send.setVisibility(View.VISIBLE);
                            progressComment.setVisibility(View.GONE);
                            Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                        }

                    });
                    ///////////////////BATCH WRITE///////////////////
                }
            }
            else {
                Utility.showToast(getApplicationContext(), "Network unavailable...");
            }
        });


        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(homePostModel[0].getUid().matches(FirebaseAuth.getInstance().getUid())){
                    postMenuDialog= new BottomSheetDialog(ViewMoreHome.this);
                    postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                    postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i= new Intent(getApplicationContext(),NewPostHome.class);

                            i.putExtra("target","100"); //target value for edit post
                            i.putExtra("FromViewMoreHome","True");
                            i.putExtra("bool", bool);
                            i.putExtra("usN", homePostModel[0].getUsN());
                            i.putExtra("dp", homePostModel[0].getDp());
                            i.putExtra("uid", homePostModel[0].getUid());

                            i.putExtra("img", homePostModel[0].getImg());
                            i.putExtra("txt", homePostModel[0].getTxt());
                            i.putExtra("comID",homePostModel[0].getComID());
                            i.putExtra("comName", homePostModel[0].getComName());

                            i.putExtra("ts", Long.toString(homePostModel[0].getTs()));
                            i.putExtra("newTs", Long.toString(homePostModel[0].getNewTs()));

                            i.putExtra("cmtNo", Long.toString(homePostModel[0].getCmtNo()));
                            StoreTemp.getInstance().setTagTemp(homePostModel[0].getTagL());

                            i.putExtra("likeL", homePostModel[0].getLikeL());
                            i.putExtra("likeCheck", homePostModel[0].getLikeCheck());
                            i.putExtra("docID", homePostModel[0].getDocID());
                            i.putExtra("reportL", homePostModel[0].getReportL());
                            i.putExtra("challengeID", homePostModel[0].getChallengeID());
                            startActivity(i);
                            finish();

                            postMenuDialog.dismiss();
                        }
                    });
                    
                    postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ViewMoreHome.this);
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
//                                                        FragmentGlobal.delete = 1;
//                                                        FragmentCampus.changed = 1;
//                                                        CommunityActivity.delete = 1;
                                                        if(getIntent().getStringExtra("from")!=null && getIntent().getStringExtra("from").matches("link")){
                                                            startActivity(new Intent(ViewMoreHome.this, MainActivity.class));
                                                            finish();
                                                        }
                                                        else if(isTaskRoot()){
                                                            startActivity(new Intent(ViewMoreHome.this, MainActivity.class));
                                                            finish();
                                                        }
                                                        else {
                                                            ViewMoreHome.super.onBackPressed();
                                                        }
                                                    }
                                                });
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();

                        }
                    });

                    postMenuDialog.findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String link = "https://www.utsavapp.in/android/feeds/"+homePostModel[0].getDocID();
                            Intent i=new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i,"Share with"));
                            postMenuDialog.dismiss();

                        }
                    });

                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            docRef.update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Utility.showToast(getApplicationContext(),"Post has been reported.");
                                        }
                                    });
                            postMenuDialog.dismiss();
                        }
                    });
                    Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    postMenuDialog.show();

                }
                else {
                    postMenuDialog= new BottomSheetDialog(ViewMoreHome.this);

                    postMenuDialog.setContentView(R.layout.dialog_post_menu);
                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                    postMenuDialog.findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String link = "https://www.utsavapp.in/android/feeds/"+homePostModel[0].getDocID();
                            Intent i=new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i,"Share with"));
                            postMenuDialog.dismiss();

                        }
                    });

                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            docRef.update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Utility.showToast(getApplicationContext(),"Post has been reported.");
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
            if(getIntent().getStringExtra("from")!=null && getIntent().getStringExtra("from").matches("link")){
                startActivity(new Intent(ViewMoreHome.this, MainActivity.class));
                finish();
            }
            if(isTaskRoot()){
                startActivity(new Intent(ViewMoreHome.this, MainActivity.class));
                finish();
            }
            else {
                if(change == 1)
                    Toast.makeText(ViewMoreHome.this, "Swipe to refresh", Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            }

        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String id = postCampus.replaceAll(" ","_");
//                String link = "https://www.campus24.in/Home/"+id+"/"+homePostModel[0].getDocID();
//                Intent i=new Intent();
//                i.setAction(Intent.ACTION_SEND);
//                i.putExtra(Intent.EXTRA_TEXT, link);
//                i.setType("text/plain");
//                startActivity(Intent.createChooser(i,"Share with"));
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

        if(CommentList.size()>0 || CommentList!=null){
            CommentList.clear();
        }

        commentRef.orderBy("ts", Query.Direction.DESCENDING).limit(10).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document: task.getResult()){
                            CommentModel commentModel = document.toObject(CommentModel.class);
                            commentModel.setDocID(document.getId());
                            CommentList.add(commentModel);
                        }
                        if(CommentList.size()>0){
                            mRecyclerView.setAdapter(adapter);
                            mRecyclerView.setVisibility(View.VISIBLE);
//                            adapter.notifyDataSetChanged();
                            no_comment.setVisibility(View.GONE);

                            commentimg.setImageResource(R.drawable.comment_yellow);

                            if(task.getResult().size()>0)
                                lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                            if(CommentList.size()<10){
//                                if(CommentList.size()==1)
//                                    noofcmnts.setText(getIntent().getStringExtra("commentNo")+ " comment");
//                                else if(CommentList.size()>1)
//                                    noofcmnts.setText(getIntent().getStringExtra("commentNo")+ " comments");

                                checkGetMore = -1;
                            }
                            else {
                                checkGetMore = 0;
                            }

                        }
                        else{
                            checkGetMore = -1;
                            noofcmnts.setText("No comments yet");
                            no_comment.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
//                            mRecyclerView.setVisibility(View.GONE);
                            commentimg.setImageResource(R.drawable.ic_comment);
                        }
                    }
                    else {
                        Utility.showToast(getApplicationContext(),"Something went wrong...");
                    }

                    progressBar.setVisibility(View.GONE);
                });
    }

    private void fetchMore(){
        progressBar.setVisibility(View.VISIBLE);
        Query nextQuery = commentRef.orderBy("ts", Query.Direction.DESCENDING).startAfter(lastVisible).limit(10);

        nextQuery.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                ArrayList<CommentModel> commentModels = new ArrayList<>();
                for (DocumentSnapshot d : t.getResult()) {
                    CommentModel commentModel = d.toObject(CommentModel.class);
                    assert commentModel != null;
                    commentModel.setDocID(d.getId());
                    commentModels.add(commentModel);

                }

                if(commentModels.size()>0){
                    int lastSize = CommentList.size();
                    CommentList.addAll(commentModels);
                    adapter.notifyItemRangeInserted(lastSize, commentModels.size());
                    lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);

//                    if(CommentList.size()==1)
//                        noofcmnts.setText(getIntent().getStringExtra("commentNo")+ " comment");
//                    else if(CommentList.size()>1)
//                        noofcmnts.setText(getIntent().getStringExtra("commentNo")+ " comments");

                    commentimg.setImageResource(R.drawable.comment_yellow);
                }

                progressBar.setVisibility(View.GONE);

                if(commentModels.size()<10){
                    checkGetMore = -1;
                }
            }
        });
    }

    private void save_Dialog(Bitmap bitmap) {
        Dialog myDialogue;
        myDialogue = new Dialog(ViewMoreHome.this);
        myDialogue.setContentView(R.layout.dialog_image_options);
        myDialogue.setCanceledOnTouchOutside(TRUE);
        myDialogue.findViewById(R.id.saveToInternal).setOnClickListener(v -> {
            if(!Utility.checkStoragePermission(ViewMoreHome.this)){
                Utility.requestStoragePermission(ViewMoreHome.this);
            }
            else {
                    Boolean bool = Utility.saveImage(bitmap, ViewMoreHome.this);

                    if(bool){
                        Toast.makeText(ViewMoreHome.this, "Saved to device", Toast.LENGTH_SHORT).show();
                        myDialogue.dismiss();
                    }
                    else{
                        Toast.makeText(ViewMoreHome.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                        myDialogue.dismiss();
                    }

            }
        });
        myDialogue.show();
        Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    @Override
    public void onBackPressed() {
        if(isTaskRoot()){
            startActivity(new Intent(ViewMoreHome.this, MainActivity.class));
            finish();
        }
        else {
            if(change == 1)
                Toast.makeText(ViewMoreHome.this, "Swipe to refresh", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        if(changed > 0 || commentChanged > 0) {
            buildCommentRecyclerView();
            changed = 0;
            commentChanged = 0;
        }
        super.onResume();
    }

}
