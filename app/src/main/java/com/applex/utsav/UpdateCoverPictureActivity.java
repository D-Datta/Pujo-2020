package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class UpdateCoverPictureActivity extends AppCompatActivity {

    private ImageView cover,cover_cross,cover_edit;
    private Button cover_save;
    private EditText cover_content;
    private CheckBox cover_checkbox;

    private IntroPref introPref;
    private BaseUserModel baseUserModel;
    private HomePostModel homePostModel;
    private String uid,COVER,type,COVER_CONTENT,gender,tspost;

    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private String[] cameraPermission;
    private String[] storagePermission;

    private DocumentReference docref, docref2;
    private Uri filepath;
    private Uri downloaduri, postdownloaduri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference reference;
    private String generatedfilepath;
    private ArrayList<String> postgeneratedfilepath = new ArrayList<>();
    private ArrayList<String> tagList;
    private byte[] coverpicbyte;
    private ProgressDialog progressDialog;
    private int pictype;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        introPref = new IntroPref(UpdateCoverPictureActivity.this);
        String lang = introPref.getLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
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

        setContentView(R.layout.activity_update_cover_picture);

        cover_cross = findViewById(R.id.cover_cross);
        cover = findViewById(R.id.cover);
        cover_edit = findViewById(R.id.cover_edit);
        cover_save = findViewById(R.id.cover_save);
        cover_content = findViewById(R.id.cover_content);
        cover_checkbox = findViewById(R.id.cover_chechbox);

        storage= FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        tagList = new ArrayList<>();
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(getIntent().getStringExtra("uid")!=null && !getIntent().getStringExtra("uid").isEmpty()){
            uid = getIntent().getStringExtra("uid");
        }
        if(getIntent().getStringExtra("type")!=null && !getIntent().getStringExtra("type").isEmpty()){
            type = getIntent().getStringExtra("type");
        }
        if(getIntent().getStringExtra("gender")!=null && !getIntent().getStringExtra("gender").isEmpty()){
            gender = getIntent().getStringExtra("gender");
        }
        if(getIntent().getStringExtra("cover")!=null && !getIntent().getStringExtra("cover").isEmpty()){
            COVER = getIntent().getStringExtra("cover");
        }
//        if(getIntent().getStringExtra("dpcaption")!=null && !getIntent().getStringExtra("dpcaption").isEmpty()){
//            DP_CONTENT = getIntent().getStringExtra("dpcaption");
//            dp_content.setText(DP_CONTENT);
//            BasicUtility.showToast(UpdateProfilePictureActivity.this,DP_CONTENT);
//        }
        if (COVER != null && !COVER.isEmpty()) {
            Picasso.get().load(COVER).placeholder(R.drawable.image_background_grey).into(cover);
        }
        else {
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
            Bitmap scaledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cover_kaash, options);
            cover.setImageBitmap(scaledBitmap);
        }

        cover_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pictype = 1; //cover
                    chooseImage();
                }
            }
        });

        cover_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                COVER_CONTENT = cover_content.getText().toString().trim();
                if(COVER_CONTENT.isEmpty() && coverpicbyte==null && cover_checkbox.isChecked()){
                    if(isTaskRoot()){
                        startActivity(new Intent(UpdateCoverPictureActivity.this, MainActivity.class));
                        finish();
                    }
                    else{
                        UpdateCoverPictureActivity.super.onBackPressed();
                    }
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdateCoverPictureActivity.this);
                    builder.setTitle("Are you sure?")
                            .setMessage("Changes will be discarded...")
                            .setPositiveButton("Sure", (dialog, which) -> {
                                if(isTaskRoot()){
                                    startActivity(new Intent(UpdateCoverPictureActivity.this, MainActivity.class));
                                    finish();
                                }
                                else {
                                    UpdateCoverPictureActivity.super.onBackPressed();
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setCancelable(true)
                            .show();
                }
            }
        });

        cover_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                COVER_CONTENT = cover_content.getText().toString().trim();

                docref = FirebaseFirestore.getInstance().collection("Users").document(uid);

                if(!cover_checkbox.isChecked()){
                    if((COVER_CONTENT==null || COVER_CONTENT.isEmpty()) && coverpicbyte==null){
                        BasicUtility.showToast(UpdateCoverPictureActivity.this,"No changes were made");
                    }
                    else if(COVER_CONTENT!=null && !COVER_CONTENT.isEmpty() && coverpicbyte==null){
                        progressDialog = new ProgressDialog(UpdateCoverPictureActivity.this);
                        progressDialog.setTitle("Updating caption");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        docref.update("covercaption",COVER_CONTENT,"coverpostid",null,"iscovershared",false)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            BasicUtility.showToast(getApplicationContext(), "Caption updated successfully");
                                            Intent intent = new Intent(UpdateCoverPictureActivity.this, ActivityProfile.class);
                                            intent.putExtra("uid", uid);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            BasicUtility.showToast(UpdateCoverPictureActivity.this,"Something went wrong...");
                                        }
                                    }
                                });
                    }
                    else if((COVER_CONTENT==null || COVER_CONTENT.isEmpty()) && coverpicbyte!=null){
                        progressDialog = new ProgressDialog(UpdateCoverPictureActivity.this);
                        progressDialog.setTitle("Updating cover picture");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("Coverpic/").child(uid + ts + "_coverpic");
                        reference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref.update("coverpic",generatedfilepath,"covercaption",null,"coverpostid",null,"iscovershared",false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(getApplicationContext(), "Cover picture updated successfully");
                                                    Intent intent = new Intent(UpdateCoverPictureActivity.this, ActivityProfile.class);
                                                    intent.putExtra("uid", uid);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateCoverPictureActivity.this,"Something went wrong...");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                    else if(COVER_CONTENT!=null && !COVER_CONTENT.isEmpty() && coverpicbyte!=null){
                        progressDialog = new ProgressDialog(UpdateCoverPictureActivity.this);
                        progressDialog.setTitle("Updating cover picture and caption");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("Coverpic/").child(uid + ts + "_coverpic");
                        reference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref.update("coverpic",generatedfilepath,"covercaption",COVER_CONTENT,"coverpostid",null,"iscovershared",false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(getApplicationContext(), "Cover picture and caption updated successfully");
                                                    Intent intent = new Intent(UpdateCoverPictureActivity.this, ActivityProfile.class);
                                                    intent.putExtra("uid", uid);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateCoverPictureActivity.this,"Something went wrong...");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
                else if(cover_checkbox.isChecked()){
                    if(coverpicbyte==null){
                        BasicUtility.showToast(UpdateCoverPictureActivity.this,"Please select a new cover picture to add to feed");
                    }
                    else if(coverpicbyte!=null && (COVER_CONTENT==null || COVER_CONTENT.isEmpty())){
                        progressDialog = new ProgressDialog(UpdateCoverPictureActivity.this);
                        progressDialog.setTitle("Updating cover picture");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("Coverpic/").child(uid + ts + "_coverpic");
                        reference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref2 = FirebaseFirestore.getInstance().collection("Feeds").document();
                                        docref.update("coverpic",generatedfilepath,"covercaption",null,"coverpostid",docref2.getId(),"iscovershared",true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

//                                                    docref2 = FirebaseFirestore.getInstance().collection("Feeds").document();
                                                    long timestampLong = System.currentTimeMillis();

                                                    homePostModel = new HomePostModel();
                                                    homePostModel.setUsN(introPref.getFullName());
                                                    homePostModel.setType(introPref.getType());
                                                    homePostModel.setGender(introPref.getGender());
                                                    homePostModel.setDp(introPref.getUserdp());
                                                    homePostModel.setUid(uid);
                                                    homePostModel.setTs(timestampLong);
                                                    homePostModel.setNewTs(timestampLong);
                                                    homePostModel.setChallengeID("CoverUpdate");

                                                    if(type.matches("indi")){
                                                        if (gender.matches("Female") || gender.matches("মহিলা")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated her cover picture");
                                                        } else if (gender.matches("Male") || gender.matches("পুরুষ")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated his cover picture");
                                                        } else if (gender.matches("Others") || gender.matches("অন্যান্য")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated their cover picture");
                                                        }
                                                    }
                                                    if(type.matches("com")){
                                                        homePostModel.setHeadline(introPref.getFullName()+" updated their cover picture");
                                                    }

                                                    Long tsLong = System.currentTimeMillis();
                                                    tspost = tsLong.toString();
                                                    StorageReference postreference = storageReference.child("Feeds/").child(uid+"/").child(tspost+"post_img");
                                                    postreference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            postreference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    postdownloaduri = uri;
                                                                    postgeneratedfilepath.add(postdownloaduri.toString());
                                                                    homePostModel.setImg(postgeneratedfilepath);
                                                                    docref2.set(homePostModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                progressDialog.dismiss();
                                                                                BasicUtility.showToast(getApplicationContext(), "Cover picture updated successfully");
                                                                                if (isTaskRoot()) {
                                                                                    Intent intent = new Intent(UpdateCoverPictureActivity.this, ActivityProfile.class);
                                                                                    intent.putExtra("uid", uid);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                                else {
                                                                                    UpdateCoverPictureActivity.super.onBackPressed();
                                                                                }
                                                                            }
                                                                            else {
                                                                                progressDialog.dismiss();
                                                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                                            }
                                                                        }
                                                                    })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    progressDialog.dismiss();
                                                                                    BasicUtility.showToast(UpdateCoverPictureActivity.this,"Something went wrong...");
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                        }
                                                    });

                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateCoverPictureActivity.this,"Something went wrong...");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                    else if(coverpicbyte!=null && COVER_CONTENT!=null && !COVER_CONTENT.isEmpty()){
                        progressDialog = new ProgressDialog(UpdateCoverPictureActivity.this);
                        progressDialog.setTitle("Updating cover picture and caption");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("Coverpic/").child(uid + ts + "_coverpic");
                        reference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref2 = FirebaseFirestore.getInstance().collection("Feeds").document();
                                        docref.update("coverpic",generatedfilepath,"covercaption",COVER_CONTENT,"coverpostid",docref2.getId(),"iscovershared",true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

//                                                    docref2 = FirebaseFirestore.getInstance().collection("Feeds").document();
                                                    long timestampLong = System.currentTimeMillis();

                                                    homePostModel = new HomePostModel();
                                                    homePostModel.setUsN(introPref.getFullName());
                                                    homePostModel.setType(introPref.getType());
                                                    homePostModel.setGender(introPref.getGender());
                                                    homePostModel.setDp(introPref.getUserdp());
                                                    homePostModel.setUid(uid);
                                                    homePostModel.setTs(timestampLong);
                                                    homePostModel.setNewTs(timestampLong);
                                                    homePostModel.setChallengeID("CoverUpdate");

                                                    if(type.matches("indi")){
                                                        if (gender.matches("Female") || gender.matches("মহিলা")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated her cover picture");
                                                            homePostModel.setTxt(COVER_CONTENT);
                                                        } else if (gender.matches("Male") || gender.matches("পুরুষ")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated his cover picture");
                                                            homePostModel.setTxt(COVER_CONTENT);
                                                        } else if (gender.matches("Others") || gender.matches("অন্যান্য")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated their cover picture");
                                                            homePostModel.setTxt(COVER_CONTENT);
                                                        }
                                                    }
                                                    if(type.matches("com")){
                                                        homePostModel.setHeadline(introPref.getFullName()+" updated their cover picture");
                                                        homePostModel.setTxt(COVER_CONTENT);
                                                    }

                                                    //TAGS
                                                    generateTagList(COVER_CONTENT);
                                                    homePostModel.setTagList(tagList);

                                                    Long tsLong = System.currentTimeMillis();
                                                    tspost = tsLong.toString();
                                                    StorageReference postreference = storageReference.child("Feeds/").child(uid+"/").child(tspost+"post_img");
                                                    postreference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            postreference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    postdownloaduri = uri;
                                                                    postgeneratedfilepath.add(postdownloaduri.toString());
                                                                    homePostModel.setImg(postgeneratedfilepath);
                                                                    docref2.set(homePostModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                progressDialog.dismiss();
                                                                                BasicUtility.showToast(getApplicationContext(), "Cover picture and caption updated successfully");
                                                                                if (isTaskRoot()) {
                                                                                    Intent intent = new Intent(UpdateCoverPictureActivity.this, ActivityProfile.class);
                                                                                    intent.putExtra("uid", uid);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                                else {
                                                                                    UpdateCoverPictureActivity.super.onBackPressed();
                                                                                }
                                                                            }
                                                                            else {
                                                                                progressDialog.dismiss();
                                                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                                            }
                                                                        }
                                                                    })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    progressDialog.dismiss();
                                                                                    BasicUtility.showToast(UpdateCoverPictureActivity.this,"Something went wrong...");
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateCoverPictureActivity.this,"Something went wrong...");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });

                    }
                }
            }
        });

        //CREATING HASHTAG COLOR EFFECT
        cover_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String text = s.toString();
//
//                if(text.isEmpty()) {
//                    postcontent.setAdapter(null);
//                } else {
//                    Pattern p1 = Pattern.compile("[@][a-zA-Z0-9]+");
//                    Matcher m1 = p1.matcher(text);
//                    int cursorPosition1 = postcontent.getSelectionStart();
//                    while(m1.find()) {
//                        if (cursorPosition1 >= m1.start() && cursorPosition1 <= m1.end()) {
//                            final int a = m1.start(); // add 1 to ommit the "@" tag
//                            final int b = m1.end();
//
//                            postcontent.getText().setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple2)),a, b, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            String newText = text.substring(a + 1, b);
//                            Query query = FirebaseFirestore.getInstance()
//                                    .collection("Users")
//                                    .whereGreaterThanOrEqualTo("small_name", newText.trim().toLowerCase())
//                                    .limit(5);
//
//                            query.get().addOnCompleteListener(task -> {
//                                if(task.isSuccessful()) {
//                                    ArrayList<UserTagModel> userTagModels = new ArrayList<>();
//                                    ArrayList<String> users = new ArrayList<>();
//                                    for(DocumentSnapshot documentSnapshot: Objects.requireNonNull(task.getResult())) {
//                                        UserTagModel userTagModel = new UserTagModel();
//                                        if(documentSnapshot.get("gender") != null) {
//                                            userTagModel.setGender(Objects.requireNonNull(documentSnapshot.get("gender")).toString());
//                                        }
//                                        if(documentSnapshot.get("dp") != null) {
//                                            userTagModel.setDp(Objects.requireNonNull(documentSnapshot.get("dp")).toString());
//                                        }
//                                        userTagModel.setName(Objects.requireNonNull(documentSnapshot.get("name")).toString());
//                                        users.add(Objects.requireNonNull(documentSnapshot.get("name")).toString());
//                                        userTagModel.setType(Objects.requireNonNull(documentSnapshot.get("type")).toString());
//                                        userTagModel.setUid(Objects.requireNonNull(documentSnapshot.get("uid")).toString());
//                                        userTagModels.add(userTagModel);
//                                    }
////                                    ArrayAdapter<UserTagModel> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.item_tag_user, userTagModels);
//                                    UserTagAdapter userTagAdapter = new UserTagAdapter(getApplicationContext(), R.layout.item_tag_user, userTagModels);
//                                    postcontent.setThreshold(1);
//                                    postcontent.setTokenizer(new SpaceTokenizer());
//                                    postcontent.setAdapter(userTagAdapter);
//                                }
//                            });
//                            break;
//                        }
//                    }
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                Pattern p = Pattern.compile("[#][a-zA-Z0-9-_]+");
                Matcher m = p.matcher(text);
                int cursorPosition = cover_content.getSelectionStart();
                while(m.find()) {
                    if (cursorPosition >= m.start() && cursorPosition <= m.end()) {
                        final int a = m.start(); // add 1 to ommit the "@" tag
                        final int b = m.end();
                        cover_content.getText().setSpan(new ForegroundColorSpan(getResources().getColor(R.color.md_blue_500)), a, b, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    }
                }
            }
        });
        //CREATING HASHTAG COLOR EFFECT


    }

    //TAGS NEW
    private void generateTagList(String postContent){
        Pattern p = Pattern.compile("[#][a-zA-Z0-9-_]+");
        Matcher m = p.matcher(postContent);

        //CHECK ALL HASHTAGS WHILE POSTING
        while(m.find())
        {
            final int a = m.start(); // add 1 to ommit the "@" tag
            final int b = m.end();
            Log.d("here", postContent.substring(a+1, b));
            tagList.add(postContent.substring(a+1, b));
        }

    }
    //TAGS NEW

    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(UpdateCoverPictureActivity.this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }
    //////////////////////PREMISSIONS//////////////////////////

    private void chooseImage() {

        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== RESULT_OK){
            if(requestCode == 1 && data!=null && data.getData()!=null){
                filepath=data.getData();
                try {
                    if(filepath!=null) {
                        if(pictype==0){
                            CropImage.activity(filepath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAspectRatio(1,1)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(UpdateCoverPictureActivity.this);
                        }
                        else if(pictype==1){
                            CropImage.activity(filepath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAspectRatio(16,9)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(UpdateCoverPictureActivity.this);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            ////////////////////////CROP//////////////////////
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri imageUri = result.getUri();
                Bitmap bitmap = null;
                Bitmap compressedBitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    compressedBitmap = BasicUtility.decodeSampledBitmapFromFile(bitmap, 612, 816);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);


//                if(pictype==0){
//                    pic = baos.toByteArray();
//                    compressedBitmap.recycle();
//                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(pic, 0 , pic.length);
//                    dp.setImageBitmap(bitmap1);
//
//                }
                if(pictype==1){
                    coverpicbyte = baos.toByteArray();
                    compressedBitmap.recycle();
                    Bitmap bitmap2 = BitmapFactory.decodeByteArray(coverpicbyte, 0 , coverpicbyte.length);
                    cover.setImageBitmap(bitmap2);

                }

                // new ImageCompressor().execute();

            }
            else {//CROP ERROR
                Toast.makeText(this, "+error", Toast.LENGTH_SHORT).show();
            }
            ////////////////////////CROP//////////////////////
        }
    }

//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            super.onBackPressed();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(UpdateCoverPictureActivity.this, ActivityProfile.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
        finish();
    }
}