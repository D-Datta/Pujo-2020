package com.example.pujo360;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pujo360.preferences.IntroPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class CommentEdit extends AppCompatActivity {

    private String PROFILEPIC;
    private EditText comment;
    private ImageView userimage_comment;
    private Button update;
    private CollectionReference commentRef;
    private ProgressDialog progressDialog;
    private IntroPref introPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_edit);

        introPref = new IntroPref(CommentEdit.this);

        Toolbar toolbar = findViewById(R.id.toolbar12);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Edit Comment");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        comment = findViewById(R.id.comment_edit);
        userimage_comment = findViewById(R.id.user_image_comment);
        update = findViewById(R.id.update_comment);

        final Intent intent = getIntent();
        Intent i= getIntent();
        String bool = i.getStringExtra("com_bool");

//        if(bool.matches("2")|| bool.matches("0")|| bool.matches("1")){
//            //Global
//            //postCampus = "Global";
//            commentRef = FirebaseFirestore.getInstance().collection("Home/Global/Feeds/"+i.getStringExtra("com_postID_home")+"/commentL/");
//
//           if(i.getStringExtra("com_parentID_home")!=null ){
//               commentRef = FirebaseFirestore.getInstance().collection("Home/"+i.getStringExtra("com_campus_home")+"/Feeds/"+i.getStringExtra("com_postID_home")+"/commentL/"+i.getStringExtra("com_parentID_home")+"/commentL");
//           }
//
//            comment.setText(intent.getStringExtra("comment_home"));
//            if(intent.getStringExtra("com_img_home")!=null){
//                PROFILEPIC = intent.getStringExtra("com_img_home");
//
////                if(PROFILEPIC.matches("0")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_1);
////                }
////                else if(PROFILEPIC.matches("1")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_2);
////                }
////                else if(PROFILEPIC.matches("2")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_3);
////                }
////                else if(PROFILEPIC.matches("3")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_4);
////                }
////                else if(PROFILEPIC.matches("4")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_5);
////                }
////                else if(PROFILEPIC.matches("5")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_6);
////                }
////                else if(PROFILEPIC.matches("6")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_7);
////                }
////                else if(PROFILEPIC.matches("7")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_8);
////                }
////                else if(PROFILEPIC.matches("8")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_9);
////                }
////                else if(PROFILEPIC.matches("9")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_10);
////                }
////                else {
//                    Picasso.get().load(PROFILEPIC).into(userimage_comment, new Callback() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//                        @Override
//                        public void onError(Exception e) {
//                            userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                        }
//                    });
////                }
//            }
//
//            update.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    progressDialog = new ProgressDialog(CommentEdit.this);
//                    progressDialog.setTitle("Saving changes");
//                    progressDialog.setMessage("Please wait...");
//                    progressDialog.show();
//                    //Toast.makeText(getApplicationContext(),i.getStringExtra("com_docID"),Toast.LENGTH_LONG).show();
//                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
//                    batch.update(commentRef.document(i.getStringExtra("com_docID_home")),"comment",comment.getText().toString());
//                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//                                progressDialog.dismiss();
//                                ViewMoreHome.changed = 1;
//                                CommentReplyActivity.change = 1;
//                                if(i.getStringExtra("com_parentID_home") == null && i.getStringExtra("from").matches("yes")) {
//
//                                    Intent intent = new Intent(CommentEdit.this, CommentReplyActivity.class);
//                                    intent.putExtra("comment", comment.getText().toString());
//                                    intent.putExtra("username", i.getStringExtra("com_username_home"));
//                                    intent.putExtra("userdp", i.getStringExtra("com_img_home"));
//                                    intent.putExtra("docID", i.getStringExtra("com_docID_home"));
//                                    intent.putExtra("postID", i.getStringExtra("com_postID_home"));
//                                    intent.putExtra("postUid", i.getStringExtra("com_postUid_home"));
//                                    intent.putExtra("likeL", i.getStringExtra("com_likeL_home"));
//                                    intent.putExtra("ReplyCommentNo", i.getStringExtra("com_rComNo_home"));
//                                    intent.putExtra("uid", i.getStringExtra("com_uid_home"));
//                                    intent.putExtra("bool", i.getStringExtra("com_bool_home"));
//                                    intent.putExtra("timestamp", i.getStringExtra("timestamp"));
//                                    intent.putExtra("pComUid", i.getStringExtra("pComUid"));
//
//                                    Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
//                                    startActivity(intent);
//                                    finish();
//                                } else {
//                                    Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
//                                    CommentEdit.super.onBackPressed();
//                                }
//                            }
//                            else{
//                                Toast.makeText(getApplicationContext(),"Comment not updated !", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });
//
//                }
//            });
//        }

//        else
//            if(bool.matches("3")|| bool.matches("4")){
            //from campus
            //postCampus = CAMPUSNAME;

            commentRef = FirebaseFirestore.getInstance().collection("Feeds/"+i.getStringExtra("com_postID_home")+"/commentL");

            if(i.getStringExtra("com_parentID_home")!=null ){
                commentRef = FirebaseFirestore.getInstance().collection("Feeds/"+i.getStringExtra("com_postID_home")+"/commentL/"+i.getStringExtra("com_parentID_home")+"/commentL");

            }


            comment.setText(intent.getStringExtra("comment_home"));
            if(intent.getStringExtra("com_img_home")!=null){

                PROFILEPIC = intent.getStringExtra("com_img_home").toString();

//                if(PROFILEPIC.matches("0")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_1);
//                }
//                else if(PROFILEPIC.matches("1")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_2);
//                }
//                else if(PROFILEPIC.matches("2")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_3);
//                }
//                else if(PROFILEPIC.matches("3")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_4);
//                }
//                else if(PROFILEPIC.matches("4")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_5);
//                }
//                else if(PROFILEPIC.matches("5")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_6);
//                }
//                else if(PROFILEPIC.matches("6")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_7);
//                }
//                else if(PROFILEPIC.matches("7")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_8);
//                }
//                else if(PROFILEPIC.matches("8")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_9);
//                }
//                else if(PROFILEPIC.matches("9")){
//                    userimage_comment.setImageResource(R.drawable.default_dp_10);
//                }
//                else {
                    Picasso.get().load(PROFILEPIC).into(userimage_comment, new Callback() {
                        @Override
                        public void onSuccess() {

                        }
                        @Override
                        public void onError(Exception e) {
                            userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    });
//                }
            }
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog = new ProgressDialog(CommentEdit.this);
                    progressDialog.setTitle("Saving changes");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();

                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                    batch.update(commentRef.document(i.getStringExtra("com_docID_home")),"comment",comment.getText().toString());
                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressDialog.dismiss();
                                ViewMoreHome.changed = 1;
                                CommentReplyActivity.change = 1;
                                if(i.getStringExtra("com_parentID_home") == null && i.getStringExtra("from").matches("yes")) {

                                    Intent intent = new Intent(CommentEdit.this, CommentReplyActivity.class);
                                    intent.putExtra("comment", comment.getText().toString());
                                    intent.putExtra("username", i.getStringExtra("com_username_home"));
                                    intent.putExtra("userdp", i.getStringExtra("com_img_home"));
                                    intent.putExtra("docID", i.getStringExtra("com_docID_home"));
                                    intent.putExtra("postID", i.getStringExtra("com_postID_home"));
                                    intent.putExtra("postUid", i.getStringExtra("com_postUid_home"));
                                    intent.putExtra("likeL", i.getStringExtra("com_likeL_home"));
                                    intent.putExtra("ReplyCommentNo", i.getStringExtra("com_rComNo_home"));
                                    intent.putExtra("uid", i.getStringExtra("com_uid_home"));
                                    intent.putExtra("bool", i.getStringExtra("com_bool_home"));
                                    intent.putExtra("timestamp", i.getStringExtra("timestamp"));
                                    intent.putExtra("pComUid", i.getStringExtra("pComUid"));

                                    Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
                                    CommentEdit.super.onBackPressed();
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Comment not updated !", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });



//        else if(bool.matches("6")) {
//
//            commentRef = FirebaseFirestore.getInstance().collection("Sliders/Global/Slides/"+ i.getStringExtra("com_postID_home") +"/commentSL/");
//
//            if(i.getStringExtra("com_parentID_home")!=null ){
//                commentRef = FirebaseFirestore.getInstance().collection("Sliders/Global/Slides/"+i.getStringExtra("com_postID_home")+"/commentSL/"+i.getStringExtra("com_parentID_home")+"/commentL");
//
//            }
//            comment.setText(intent.getStringExtra("comment_home"));
//                PROFILEPIC = introPref.getUserdp();
//
////                if(PROFILEPIC.matches("0")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_1);
////                }
////                else if(PROFILEPIC.matches("1")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_2);
////                }
////                else if(PROFILEPIC.matches("2")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_3);
////                }
////                else if(PROFILEPIC.matches("3")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_4);
////                }
////                else if(PROFILEPIC.matches("4")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_5);
////                }
////                else if(PROFILEPIC.matches("5")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_6);
////                }
////                else if(PROFILEPIC.matches("6")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_7);
////                }
////                else if(PROFILEPIC.matches("7")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_8);
////                }
////                else if(PROFILEPIC.matches("8")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_9);
////                }
////                else if(PROFILEPIC.matches("9")){
////                    userimage_comment.setImageResource(R.drawable.default_dp_10);
////                }
////                else {
//                    Picasso.get().load(PROFILEPIC).into(userimage_comment, new Callback() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//                        @Override
//                        public void onError(Exception e) {
//                            userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                        }
//                    });
////                }
//
//            update.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    progressDialog = new ProgressDialog(CommentEdit.this);
//                    progressDialog.setTitle("Saving changes");
//                    progressDialog.setMessage("Please wait...");
//                    progressDialog.show();
//                    //Toast.makeText(getApplicationContext(),i.getStringExtra("com_docID"),Toast.LENGTH_LONG).show();
//                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
//                    batch.update(commentRef.document(i.getStringExtra("com_docID_home")),"comment",comment.getText().toString());
//                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isSuccessful()){
//                                progressDialog.dismiss();
////                                ViewMoreSlider.changed = 1;
//                                CommentReplyActivity.change = 1;
//                                if(i.getStringExtra("com_parentID_home") == null && i.getStringExtra("from").matches("yes")) {
//
//                                    Intent intent = new Intent(CommentEdit.this, CommentReplyActivity.class);
//                                    intent.putExtra("comment", comment.getText().toString());
//                                    intent.putExtra("username", i.getStringExtra("com_username_home"));
//                                    intent.putExtra("userdp", i.getStringExtra("com_img_home"));
//                                    intent.putExtra("campus", i.getStringExtra("com_campus_home"));
//                                    intent.putExtra("docID", i.getStringExtra("com_docID_home"));
//                                    intent.putExtra("postID", i.getStringExtra("com_postID_home"));
//                                    intent.putExtra("postUid", i.getStringExtra("com_postUid_home"));
//                                    intent.putExtra("likeL", i.getStringExtra("com_likeL_home"));
//                                    intent.putExtra("ReplyCommentNo", i.getStringExtra("com_rComNo_home"));
//                                    intent.putExtra("uid", i.getStringExtra("com_uid_home"));
//                                    intent.putExtra("bool", i.getStringExtra("com_bool_home"));
//                                    intent.putExtra("timestamp", i.getStringExtra("timestamp"));
//                                    intent.putExtra("pComUid", i.getStringExtra("pComUid"));
//
//                                    Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
//                                    startActivity(intent);
//                                    finish();
//                                } else {
//                                    Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
//                                    CommentEdit.super.onBackPressed();
//                                }
//                            }
//                            else{
//                                Toast.makeText(getApplicationContext(),"Comment not updated !", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });
//
//                }
//            });
//        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}