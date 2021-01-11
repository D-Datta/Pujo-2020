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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.applex.utsav.models.AccessToken;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.registration.RegIndividual;
import com.applex.utsav.utility.BasicUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class UpdateProfilePictureActivity extends AppCompatActivity {

    private ImageView dp,dp_cross,dp_edit;
    private Button dp_save;
    private EditText dp_content;
    private CheckBox dp_checkbox;
    private CardView dp_outline;

    private IntroPref introPref;
    private BaseUserModel baseUserModel;
    private HomePostModel homePostModel;
    private String uid,DP,type,DP_CONTENT,gender,tspost;

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
    private byte[] pic;
    private ProgressDialog progressDialog;
    private int pictype;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPref = new IntroPref(UpdateProfilePictureActivity.this);
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

        setContentView(R.layout.activity_update_profile_picture);

        dp_cross = findViewById(R.id.dp_cross);
        dp = findViewById(R.id.dp);
        dp_edit = findViewById(R.id.dp_edit);
        dp_save = findViewById(R.id.dp_save);
        dp_content = findViewById(R.id.dp_content);
        dp_checkbox = findViewById(R.id.dp_chechbox);
        dp_outline = findViewById(R.id.dp_outline);

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
        if(getIntent().getStringExtra("dp")!=null && !getIntent().getStringExtra("dp").isEmpty()){
            DP = getIntent().getStringExtra("dp");
        }
//        if(getIntent().getStringExtra("dpcaption")!=null && !getIntent().getStringExtra("dpcaption").isEmpty()){
//            DP_CONTENT = getIntent().getStringExtra("dpcaption");
//            dp_content.setText(DP_CONTENT);
//            BasicUtility.showToast(UpdateProfilePictureActivity.this,DP_CONTENT);
//        }

        if(type.matches("indi")) {
            dp_outline.setCardBackgroundColor(getResources().getColor(R.color.reels_white));
            if (DP!=null && !DP.isEmpty()) {
                Picasso.get().load(DP).placeholder(R.drawable.ic_account_circle_black_24dp).into(dp);
            }
            else {
                if (gender != null) {
                    if (gender.matches("Female") || gender.matches("মহিলা")) {
                        dp.setImageResource(R.drawable.ic_female);
                    } else if (gender.matches("Male") || gender.matches("পুরুষ")) {
                        dp.setImageResource(R.drawable.ic_male);
                    } else if (gender.matches("Others") || gender.matches("অন্যান্য")) {
                        dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                } else {
                    dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
        }
        else if(type.matches("com")) {
            dp_outline.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            if (DP!=null && !DP.isEmpty()) {
                Picasso.get().load(DP).placeholder(R.drawable.image_background_grey).into(dp);
            }
            else {
                Display display = getWindowManager().getDefaultDisplay();
                int displayWidth = display.getWidth();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
                int width = options.outWidth;
                if (width > displayWidth) {
                    int widthRatio = Math.round((float) width / (float) displayWidth);
                    options.inSampleSize = widthRatio;
                }
                options.inJustDecodeBounds = false;
                Bitmap scaledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
                dp.setImageBitmap(scaledBitmap);
            }
        }


//            FirebaseFirestore.getInstance().collection("Users").document(uid)
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()){
//                    baseUserModel = task.getResult().toObject(BaseUserModel.class);
//                    type = baseUserModel.getType();
//
//                    if(type.matches("indi")){
//                        if(baseUserModel.getDp()!=null && !baseUserModel.getDp().isEmpty()){
//                            DP = baseUserModel.getDp();
//                            Picasso.get().load(DP).placeholder(R.drawable.ic_account_circle_black_24dp).into(dp);
//                        }
//                        else{
//                            if(baseUserModel.getGender()!=null){
//                                if (baseUserModel.getGender().matches("Female") || baseUserModel.getGender().matches("মহিলা")){
//                                    dp.setImageResource(R.drawable.ic_female);
//                                }
//                                else if (baseUserModel.getGender().matches("Male") || baseUserModel.getGender().matches("পুরুষ")){
//                                    dp.setImageResource(R.drawable.ic_male);
//                                }
//                                else if (baseUserModel.getGender().matches("Others") || baseUserModel.getGender().matches("অন্যান্য")){
//                                    dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                                }
//                            }
//                            else{
//                                dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
//                            }
//                        }
//
//                        dp_edit.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                if (!checkStoragePermission()) {
//                                    requestStoragePermission();
//                                } else {
//                                    pictype = 0; //dp
//                                    chooseImage();
//                                }
//                            }
//                        });
//
//                    }
//                    else if(type.matches("com")){
//                        if (baseUserModel.getDp() != null && !baseUserModel.getDp().isEmpty()) {
//                            DP = baseUserModel.getDp();
//                            Picasso.get().load(DP).placeholder(R.drawable.image_background_grey).transform(new CropCircleTransformation()).into(dp);
//                        }
//                        else {
//                            Display display = getWindowManager().getDefaultDisplay();
//                            int displayWidth = display.getWidth();
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inJustDecodeBounds = true;
//                            BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
//                            int width = options.outWidth;
//                            if (width > displayWidth) {
//                                int widthRatio = Math.round((float) width / (float) displayWidth);
//                                options.inSampleSize = widthRatio;
//                            }
//                            options.inJustDecodeBounds = false;
//                            Bitmap scaledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
//                            dp.setImageBitmap(scaledBitmap);
//                        }
//
//                        dp_edit.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                if (!checkStoragePermission()) {
//                                    requestStoragePermission();
//                                } else {
//                                    pictype = 0; //dp
//                                    chooseImage();
//                                }
//                            }
//                        });
//
//                    }
//
//                }
//            }
//        })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
//                    }
//                });

        dp_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pictype = 0; //dp
                    chooseImage();
                }
            }
        });

        dp_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DP_CONTENT = dp_content.getText().toString().trim();
                if(DP_CONTENT.isEmpty() && pic==null && dp_checkbox.isChecked()){
                    if(isTaskRoot()){
                        startActivity(new Intent(UpdateProfilePictureActivity.this, MainActivity.class));
                        finish();
                    }
                    else{
                        UpdateProfilePictureActivity.super.onBackPressed();
                    }
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdateProfilePictureActivity.this);
                    builder.setTitle("Are you sure?")
                            .setMessage("Changes will be discarded...")
                            .setPositiveButton("Sure", (dialog, which) -> {
                                if(isTaskRoot()){
                                    startActivity(new Intent(UpdateProfilePictureActivity.this, MainActivity.class));
                                    finish();
                                }
                                else {
                                    UpdateProfilePictureActivity.super.onBackPressed();
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setCancelable(true)
                            .show();
                }
            }
        });

        dp_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DP_CONTENT = dp_content.getText().toString().trim();

                docref = FirebaseFirestore.getInstance().collection("Users").document(uid);

                if(!dp_checkbox.isChecked()){
                    if((DP_CONTENT==null || DP_CONTENT.isEmpty()) && pic==null){
                        BasicUtility.showToast(UpdateProfilePictureActivity.this,"No changes were made");
                    }
                    else if(DP_CONTENT!=null && !DP_CONTENT.isEmpty() && pic==null){
                        progressDialog = new ProgressDialog(UpdateProfilePictureActivity.this);
                        progressDialog.setTitle("Updating caption");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        docref.update("dpcaption",DP_CONTENT,"dppostid",null,"isdpshared",false)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            BasicUtility.showToast(getApplicationContext(), "Caption updated successfully");
                                            Intent intent = new Intent(UpdateProfilePictureActivity.this, ActivityProfile.class);
                                            intent.putExtra("uid", uid);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
                                        }
                                    }
                                });
                    }
                    else if((DP_CONTENT==null || DP_CONTENT.isEmpty()) && pic!=null){
                        progressDialog = new ProgressDialog(UpdateProfilePictureActivity.this);
                        progressDialog.setTitle("Updating profile picture");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("DP/").child(uid + ts + "_dp");
                        reference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref.update("dp",generatedfilepath,"dpcaption",null,"dppostid",null,"isdpshared",false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    introPref.setUserdp(generatedfilepath);
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(getApplicationContext(), "Profile picture updated successfully");
                                                    Intent intent = new Intent(UpdateProfilePictureActivity.this, ActivityProfile.class);
                                                    intent.putExtra("uid", uid);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                    else if(DP_CONTENT!=null && !DP_CONTENT.isEmpty() && pic!=null){
                        progressDialog = new ProgressDialog(UpdateProfilePictureActivity.this);
                        progressDialog.setTitle("Updating profile picture and caption");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("DP/").child(uid + ts + "_dp");
                        reference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref.update("dp",generatedfilepath,"dpcaption",DP_CONTENT,"dppostid",null,"isdpshared",false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    introPref.setUserdp(generatedfilepath);
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(getApplicationContext(), "Profile picture and caption updated successfully");
                                                    Intent intent = new Intent(UpdateProfilePictureActivity.this, ActivityProfile.class);
                                                    intent.putExtra("uid", uid);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
                else if(dp_checkbox.isChecked()){
                    if(pic==null){
                        BasicUtility.showToast(UpdateProfilePictureActivity.this,"Please select a new profile picture to add to feed");
                    }
                    else if(pic!=null && (DP_CONTENT==null || DP_CONTENT.isEmpty())){
                        progressDialog = new ProgressDialog(UpdateProfilePictureActivity.this);
                        progressDialog.setTitle("Updating profile picture");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("DP/").child(uid + ts + "_dp");
                        reference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref2 = FirebaseFirestore.getInstance().collection("Feeds").document();
                                        docref.update("dp",generatedfilepath,"dpcaption",null,"dppostid",docref2.getId(),"isdpshared",true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    introPref.setUserdp(generatedfilepath);

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
                                                    homePostModel.setChallengeID("PictureUpdate");

                                                    if(type.matches("indi")){
                                                        if (gender.matches("Female") || gender.matches("মহিলা")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated her profile picture");
                                                        } else if (gender.matches("Male") || gender.matches("পুরুষ")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated his profile picture");
                                                        } else if (gender.matches("Others") || gender.matches("অন্যান্য")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated their profile picture");
                                                        }
                                                    }
                                                    if(type.matches("com")){
                                                        homePostModel.setHeadline(introPref.getFullName()+" updated their profile picture");
                                                    }

                                                    Long tsLong = System.currentTimeMillis();
                                                    tspost = tsLong.toString();
                                                    StorageReference postreference = storageReference.child("Feeds/").child(uid+"/").child(tspost+"post_img");
                                                    postreference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                                                                BasicUtility.showToast(getApplicationContext(), "Profile picture updated successfully");
                                                                                if (isTaskRoot()) {
                                                                                    Intent intent = new Intent(UpdateProfilePictureActivity.this, ActivityProfile.class);
                                                                                    intent.putExtra("uid", uid);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                                else {
                                                                                    UpdateProfilePictureActivity.super.onBackPressed();
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
                                                                                    BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                        }
                                                    });

                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                    else if(pic!=null && DP_CONTENT!=null && !DP_CONTENT.isEmpty()){
                        progressDialog = new ProgressDialog(UpdateProfilePictureActivity.this);
                        progressDialog.setTitle("Updating profile picture and caption");
                        progressDialog.setMessage("Hang on...");
                        progressDialog.show();
                        long ts = Calendar.getInstance().getTimeInMillis();
                        reference = storageReference.child("Users/").child("DP/").child(uid + ts + "_dp");
                        reference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloaduri = uri;
                                        generatedfilepath = downloaduri.toString();
                                        docref2 = FirebaseFirestore.getInstance().collection("Feeds").document();
                                        docref.update("dp",generatedfilepath,"dpcaption",DP_CONTENT,"dppostid",docref2.getId(),"isdpshared",true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    introPref.setUserdp(generatedfilepath);

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
                                                    homePostModel.setChallengeID("PictureUpdate");

                                                    if(type.matches("indi")){
                                                        if (gender.matches("Female") || gender.matches("মহিলা")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated her profile picture");
                                                            homePostModel.setTxt(DP_CONTENT);
                                                        } else if (gender.matches("Male") || gender.matches("পুরুষ")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated his profile picture");
                                                            homePostModel.setTxt(DP_CONTENT);
                                                        } else if (gender.matches("Others") || gender.matches("অন্যান্য")) {
                                                            homePostModel.setHeadline(introPref.getFullName()+" updated their profile picture");
                                                            homePostModel.setTxt(DP_CONTENT);
                                                        }
                                                    }
                                                    if(type.matches("com")){
                                                        homePostModel.setHeadline(introPref.getFullName()+" updated their profile picture");
                                                        homePostModel.setTxt(DP_CONTENT);
                                                    }

                                                    //TAGS
                                                    generateTagList(DP_CONTENT);
                                                    homePostModel.setTagList(tagList);

                                                    Long tsLong = System.currentTimeMillis();
                                                    tspost = tsLong.toString();
                                                    StorageReference postreference = storageReference.child("Feeds/").child(uid+"/").child(tspost+"post_img");
                                                    postreference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                                                                BasicUtility.showToast(getApplicationContext(), "Profile picture and caption updated successfully");
                                                                                if (isTaskRoot()) {
                                                                                    Intent intent = new Intent(UpdateProfilePictureActivity.this, ActivityProfile.class);
                                                                                    intent.putExtra("uid", uid);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                                else {
                                                                                    UpdateProfilePictureActivity.super.onBackPressed();
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
                                                                                    BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    BasicUtility.showToast(UpdateProfilePictureActivity.this,"Something went wrong...");
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
        dp_content.addTextChangedListener(new TextWatcher() {
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
                int cursorPosition = dp_content.getSelectionStart();
                while(m.find()) {
                    if (cursorPosition >= m.start() && cursorPosition <= m.end()) {
                        final int a = m.start(); // add 1 to ommit the "@" tag
                        final int b = m.end();
                        dp_content.getText().setSpan(new ForegroundColorSpan(getResources().getColor(R.color.md_blue_500)), a, b, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        ActivityCompat.requestPermissions(UpdateProfilePictureActivity.this, storagePermission, STORAGE_REQUEST_CODE);
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
                                    .start(UpdateProfilePictureActivity.this);
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
                                    .start(UpdateProfilePictureActivity.this);
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


                if(pictype==0){
                    pic = baos.toByteArray();
                    compressedBitmap.recycle();
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(pic, 0 , pic.length);
                    dp.setImageBitmap(bitmap1);

                }
//                else if(pictype==1){
//                    coverpicbyte = baos.toByteArray();
//                    compressedBitmap.recycle();
//                    Bitmap bitmap2 = BitmapFactory.decodeByteArray(coverpicbyte, 0 , coverpicbyte.length);
//                    cover_ind.setImageBitmap(bitmap2);
//
//                }

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
        Intent intent = new Intent(UpdateProfilePictureActivity.this, ActivityProfile.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
        finish();
    }
}