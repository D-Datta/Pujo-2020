package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.adapters.ProfileAdapter;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.dialogs.BottomPayDialog;
import com.applex.utsav.fragments.Fragment_Posts;
import com.applex.utsav.fragments.Fragment_Posts_Users;
import com.applex.utsav.fragments.Fragment_Reels;
import com.applex.utsav.fragments.Fragment_Reels_Users;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.PujoCommitteeModel;
import com.applex.utsav.models.SeenModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ActivityProfile extends AppCompatActivity {

    public static int delete = 0;
    public static int change = 0;
    private TextView PName, PUsername, Paddress, Pcity, aboutheading;
    private ImageView PDp, Pcoverpic, verified;
    private ReadMoreTextView PDetaileddesc;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView editDp, editCover;

    private String name, pujotype, coverpic, dp, address, city, state, pin, desc, type;

    public String uid;
    private FirebaseUser fireuser;
    int bool;
    private ConnectivityManager cm;
    private BaseUserModel baseUserModel;

    private TextView visits, likes, upvoters;
    boolean isUpvoted = false;
    boolean isLoadingFinished = false;

    private int imageCoverOrDp = 0; //dp = 0, cover = 1

    private String UPIID;

    private Button locate, ePronami;
    private Button upvote, edit_profile;
    private LinearLayout upvoteHolder;
    private LinearLayout counts;
    private RelativeLayout dp_outline;

    private LinearLayout selfProfile, elseProfile;

    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri filePath;
    private ProgressDialog progressDialog;
    byte[] pic;

    private IntroPref introPref;
    private LottieAnimationView upvote_anim;
    public static int mode_changed = 0;

    private LinearLayout pronamiHolder;
    private ImageView pronamiInfo;
    
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPref = new IntroPref(ActivityProfile.this);
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

        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cm = (ConnectivityManager) ActivityProfile.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        upvote_anim = findViewById(R.id.upvote_anim);
        PDp = findViewById(R.id.Pdp);
        PName = findViewById(R.id.Profilename);
        PUsername = findViewById(R.id.Pusername);
        Pcoverpic = findViewById(R.id.coverpic);
        PDetaileddesc = findViewById(R.id.detaildesc);
        edit_profile = findViewById(R.id.edit_profile);
        locate = findViewById(R.id.locate);
        Paddress = findViewById(R.id.address_com);
        verified = findViewById(R.id.verified);
        Pcity = findViewById(R.id.Pcity);
        aboutheading = findViewById(R.id.about);

        visits = findViewById(R.id.visits);
        likes = findViewById(R.id.likes);
        upvoters = findViewById(R.id.followers);
        editDp = findViewById(R.id.edit_dp);
        editCover = findViewById(R.id.edit_cover);

        selfProfile = findViewById(R.id.selfProfile);
        elseProfile = findViewById(R.id.elseProfile);
        counts = findViewById(R.id.counts);
        dp_outline = findViewById(R.id.dp_outline);

        upvote = findViewById(R.id.follow);
        upvoteHolder = findViewById(R.id.upvote_holder);
        ePronami = findViewById(R.id.e_pronami);
        pronamiHolder = findViewById(R.id.pronami_holder);
        pronamiInfo = findViewById(R.id.pronami_info);

        tabLayout = findViewById(R.id.tabBar);
        viewPager = findViewById(R.id.viewPager);

        fireuser = FirebaseAuth.getInstance().getCurrentUser();

        name = getIntent().getStringExtra("name");
        coverpic = getIntent().getStringExtra("coverpic");
        dp = getIntent().getStringExtra("dp");

        cm = (ConnectivityManager) ActivityProfile.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////
        if (getIntent() != null && getIntent().getStringExtra("uid") != null) {
            uid = getIntent().getStringExtra("uid");
            if (!uid.matches(fireuser.getUid())) {
                bool = 1;//ANOTHER USER ACCOUNT
            }
        } else {
            uid = fireuser.getUid();
            bool = 0;//CURRENT USER ACCOUNT
        }
        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0);
        tabLayout.getTabAt(1);

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(ActivityProfile.this, (value, error) -> {
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
                                        startActivity(new Intent(ActivityProfile.this, ActivityProfile.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }

        //setup profile
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            baseUserModel = task.getResult().toObject(BaseUserModel.class);

                            type = baseUserModel.getType();

                            ///////////////for individual///////////////
                            if(type.matches("indi")){
                                dp_outline.setBackground(getResources().getDrawable(R.drawable.dp_outline));
                                dp_outline.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
//                                dp_outline.setCardBackgroundColor(getResources().getColor(R.color.white));
                                counts.setVisibility(View.GONE);
                                Paddress.setVisibility(View.GONE);
                                PUsername.setVisibility(View.GONE);
                                Pcity.setVisibility(View.VISIBLE);

                                name = baseUserModel.getName();
                                PName.setText(name);
                                PName.setTextColor(getResources().getColor(R.color.dark_black));

                                if(baseUserModel.getCity()!=null || baseUserModel.getState()!=null){

                                    if((baseUserModel.getCity()!=null && baseUserModel.getCity().isEmpty())
                                            && (baseUserModel.getState()!=null && !baseUserModel.getState().isEmpty())){
                                        Pcity.setText(baseUserModel.getState());
                                    }
                                    else if((baseUserModel.getState()!=null && baseUserModel.getState().isEmpty())
                                            && (baseUserModel.getCity()!=null && !baseUserModel.getCity().isEmpty())){
                                        Pcity.setText(baseUserModel.getCity());
                                    }
                                    else if((baseUserModel.getCity()!=null && !baseUserModel.getCity().isEmpty())
                                            && (baseUserModel.getState()!=null && !baseUserModel.getState().isEmpty())){
                                        Pcity.setText(baseUserModel.getCity()+", "+baseUserModel.getState());
                                    }
                                    else if((baseUserModel.getCity()!=null && baseUserModel.getCity().isEmpty())
                                            && (baseUserModel.getState()!=null && baseUserModel.getState().isEmpty())){
                                        Pcity.setVisibility(View.GONE);
                                    }

                                }
                                else if(baseUserModel.getCity()==null && baseUserModel.getState()==null){
                                    Pcity.setVisibility(View.GONE);
                                }

                                if(baseUserModel.getDp()!=null && !baseUserModel.getDp().isEmpty()){
                                    dp = baseUserModel.getDp();
                                    Picasso.get().load(dp).placeholder(R.drawable.ic_account_circle_black_24dp).into(PDp);
                                }
                                else{
                                    if(baseUserModel.getGender()!=null){
                                        if (baseUserModel.getGender().matches("Female") || baseUserModel.getGender().matches("মহিলা")){
                                            PDp.setImageResource(R.drawable.ic_female);
                                        }
                                        else if (baseUserModel.getGender().matches("Male") || baseUserModel.getGender().matches("পুরুষ")){
                                            PDp.setImageResource(R.drawable.ic_male);
                                        }
                                        else if (baseUserModel.getGender().matches("Others") || baseUserModel.getGender().matches("অন্যান্য")){
                                            PDp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    }
                                    else{
                                        PDp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                }

                                if(baseUserModel.getCoverpic()!=null && !baseUserModel.getCoverpic().isEmpty()){
                                    coverpic = baseUserModel.getCoverpic();
                                    Picasso.get().load(coverpic).placeholder(R.drawable.image_background_grey).into(Pcoverpic);
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
                                    Pcoverpic.setImageBitmap(scaledBitmap);
                                }

                                if(baseUserModel.getAbout()!=null && !baseUserModel.getAbout().isEmpty()){
                                    desc = baseUserModel.getAbout();
                                    PDetaileddesc.setText(desc);
                                }
                                else{
                                    aboutheading.setVisibility(View.GONE);
                                    PDetaileddesc.setVisibility(View.GONE);
                                }

                                isLoadingFinished = true;

                                PDp.setOnClickListener(v -> {
                                    if (baseUserModel.getDp()!=null) {
                                        if (task.getResult().getBoolean("isdpshared")!=null) {
                                            if(baseUserModel.isIsdpshared()){
                                                Intent intent = new Intent(ActivityProfile.this, ViewMoreHome.class);
                                                intent.putExtra("from", "dp");
                                                intent.putExtra("type", type);
                                                intent.putExtra("postID", baseUserModel.getDppostid());
                                                startActivity(intent);
                                            }
                                            else{
                                                Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                                intent.putExtra("from", "profile");
                                                intent.putExtra("Bitmap", baseUserModel.getDp());
                                                intent.putExtra("caption", baseUserModel.getDpcaption());
                                                startActivity(intent);
                                            }
                                        }
                                        else{
                                            Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                            intent.putExtra("from", "profile");
                                            intent.putExtra("Bitmap", baseUserModel.getDp());
                                            intent.putExtra("caption", baseUserModel.getDpcaption());
                                            startActivity(intent);
                                        }
                                    }
                                    else{
                                        Toast.makeText(ActivityProfile.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Pcoverpic.setOnClickListener(v -> {
                                    if (baseUserModel.getCoverpic()!=null) {
                                        if (task.getResult().getBoolean("iscovershared")!=null) {
                                            if(baseUserModel.isIscovershared()){
                                                Intent intent = new Intent(ActivityProfile.this, ViewMoreHome.class);
                                                intent.putExtra("from", "cover");
                                                intent.putExtra("type", type);
                                                intent.putExtra("postID", baseUserModel.getCoverpostid());
                                                startActivity(intent);
                                            }
                                            else{
                                                Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                                intent.putExtra("from", "profile");
                                                intent.putExtra("Bitmap", baseUserModel.getCoverpic());
                                                intent.putExtra("caption", baseUserModel.getCovercaption());
                                                startActivity(intent);
                                            }
                                        }
                                        else{
                                            Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                            intent.putExtra("from", "profile");
                                            intent.putExtra("Bitmap", baseUserModel.getCoverpic());
                                            intent.putExtra("caption", baseUserModel.getCovercaption());
                                            startActivity(intent);
                                        }
                                    }
                                    else{
                                        Toast.makeText(ActivityProfile.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                if (uid.matches(FirebaseAuth.getInstance().getUid()) ) {

                                    editCover.setVisibility(View.VISIBLE);
                                    editDp.setVisibility(View.VISIBLE);
                                    selfProfile.setVisibility(View.VISIBLE);
                                    edit_profile.setVisibility(View.VISIBLE);
                                    elseProfile.setVisibility(View.GONE);

                                    edit_profile.setOnClickListener(v -> {
                                        Intent i1 = new Intent(ActivityProfile.this, EditProfileIndividualActivity.class);
                                        startActivity(i1);
                                        finish();
                                    });

//                                    editDp.setOnClickListener(v -> {
//                                        if (!checkStoragePermission()) {
//                                            requestStoragePermission();
//                                        } else {
//                                            imageCoverOrDp = 0; //dp
//                                            pickGallery();
//                                        }
//                                    });

                                    editDp.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(ActivityProfile.this,UpdateProfilePictureActivity.class);
                                            i.putExtra("uid",uid);
                                            i.putExtra("type",baseUserModel.getType());
                                            i.putExtra("gender",baseUserModel.getGender());
                                            i.putExtra("dp",baseUserModel.getDp());
//                                            if(baseUserModel.getDpcation()!=null && !baseUserModel.getDpcation().isEmpty()){
//                                                i.putExtra("dpcaption",baseUserModel.getDpcation());
//                                            }
                                            startActivity(i);
                                            finish();
                                        }
                                    });

//                                    editCover.setOnClickListener(v -> {
//                                        if (!checkStoragePermission()) {
//                                            requestStoragePermission();
//                                        } else {
//                                            imageCoverOrDp = 1; //cover
//                                            pickGallery();
//                                        }
//                                    });

                                    editCover.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(ActivityProfile.this,UpdateCoverPictureActivity.class);
                                            i.putExtra("uid",uid);
                                            i.putExtra("type",baseUserModel.getType());
                                            i.putExtra("gender",baseUserModel.getGender());
                                            i.putExtra("cover",baseUserModel.getCoverpic());
//                                            if(baseUserModel.getDpcation()!=null && !baseUserModel.getDpcation().isEmpty()){
//                                                i.putExtra("dpcaption",baseUserModel.getDpcation());
//                                            }
                                            startActivity(i);
                                            finish();
                                        }
                                    });
                                }
                                else if(!uid.matches(FirebaseAuth.getInstance().getUid())) {
                                    selfProfile.setVisibility(View.GONE);
                                    edit_profile.setVisibility(View.GONE);
                                    editCover.setVisibility(View.GONE);
                                    editDp.setVisibility(View.GONE);
                                    elseProfile.setVisibility(View.GONE);
                                }
                            }
                            ///////////////for individual///////////////

                            ///////////////for committee///////////////
                            else if (type.matches("com")){
                                dp_outline.setBackground(getResources().getDrawable(R.drawable.dp_outline_profile));
                                counts.setVisibility(View.VISIBLE);
                                Paddress.setVisibility(View.VISIBLE);
                                PUsername.setVisibility(View.VISIBLE);
                                Pcity.setVisibility(View.GONE);

                                if(task.getResult().getBoolean("verified")!=null){
                                    if(baseUserModel.isVerified()){
                                        verified.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        verified.setVisibility(View.GONE);
                                    }
                                }
                                else{
                                    verified.setVisibility(View.GONE);
                                }

                                name = baseUserModel.getName();
                                PName.setText(name);

                                if(baseUserModel.getAddressline()!=null && !baseUserModel.getAddressline().isEmpty()){
                                    address = baseUserModel.getAddressline();
                                }
                                if(baseUserModel.getCity()!=null && !baseUserModel.getCity().isEmpty()){
                                    city = baseUserModel.getCity();
                                }
                                if(baseUserModel.getState()!=null && !baseUserModel.getState().isEmpty()){
                                    state = baseUserModel.getState();
                                }
                                if (baseUserModel.getPin() != null && !baseUserModel.getPin().isEmpty()) {
                                    pin = baseUserModel.getPin();
                                }
                                String fulladd = address + "\n" + city + " , " + state + " - " + pin;
                                Paddress.setText(fulladd);

                                if (baseUserModel.getDp() != null && !baseUserModel.getDp().isEmpty()) {
                                    dp = baseUserModel.getDp();
                                    Picasso.get().load(dp).placeholder(R.drawable.image_background_grey).transform(new CropCircleTransformation()).into(PDp);
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
                                    PDp.setImageBitmap(scaledBitmap);
                                }

                                if (baseUserModel.getCoverpic() != null && !baseUserModel.getCoverpic().isEmpty()) {
                                    coverpic = baseUserModel.getCoverpic();
                                    Picasso.get().load(coverpic).placeholder(R.drawable.image_background_grey).into(Pcoverpic);
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
                                    Pcoverpic.setImageBitmap(scaledBitmap);
                                }

                                if(baseUserModel.getPujotype()!=null && !baseUserModel.getPujotype().isEmpty()){
                                    pujotype = baseUserModel.getPujotype();
                                    PUsername.setText(pujotype);
                                }

                                if (baseUserModel.getAbout() != null && !baseUserModel.getAbout().isEmpty()) {
                                    desc = baseUserModel.getAbout();
                                    PDetaileddesc.setText(desc);
                                }

                                //metrics
                                if (baseUserModel.getPujoVisits()> 1) {
                                    if (baseUserModel.getPujoVisits() > 1000) {
                                        visits.setText(baseUserModel.getPujoVisits() / 1000 + "." + (baseUserModel.getPujoVisits() % 1000) / 100 + "K");
                                    } else {
                                        visits.setText(baseUserModel.getPujoVisits() + "");
                                    }
                                } else {
                                    visits.setText(baseUserModel.getPujoVisits() + "");
                                }

                                if (baseUserModel.getLikeCount() > 1) {
                                    if (baseUserModel.getLikeCount() > 1000) {
                                        likes.setText(baseUserModel.getLikeCount() / 1000 + "." + (baseUserModel.getLikeCount() % 1000) / 100 + "K");
                                    } else {
                                        likes.setText(baseUserModel.getLikeCount() + "");
                                    }
                                } else {
                                    likes.setText(baseUserModel.getLikeCount() + "");
                                }
                                //metrics
                                if (baseUserModel.getUpvoteL() != null) {
                                    if (baseUserModel.getUpvoteL().size() == 0) {
                                        upvoters.setText("0");
                                    } else if (baseUserModel.getUpvoteL().size() == 1) {
                                        upvoters.setText(baseUserModel.getUpvoteL().size() + "");
                                    } else {
                                        upvoters.setText(baseUserModel.getUpvoteL().size() + "");
                                    }
                                    for (String uid : baseUserModel.getUpvoteL()) {
                                        if (uid.matches(fireuser.getUid())) {
                                            isUpvoted = true;
                                            break;
                                        }
                                    }
                                } else {
                                    upvoters.setText("0");
                                }

                                if (isUpvoted) {
                                    upvote.setText("Upvoted");
                                    upvote.setBackgroundResource(R.drawable.custom_button_outline);
                                    upvote.setTextColor(getResources().getColor(R.color.purple));

                                } else {
                                    upvote.setText("Upvote");
                                    upvote.setBackgroundResource(R.drawable.custom_button);
                                    upvote.setTextColor(getResources().getColor(R.color.reels_white));

                                }

                                isLoadingFinished = true;


                                PDp.setOnClickListener(v -> {
                                    if (baseUserModel.getDp()!=null) {
                                        if (task.getResult().getBoolean("isdpshared")!=null) {
                                            if(baseUserModel.isIsdpshared()){
                                                Intent intent = new Intent(ActivityProfile.this, ViewMoreHome.class);
                                                intent.putExtra("from", "dp");
                                                intent.putExtra("type", type);
                                                intent.putExtra("postID", baseUserModel.getDppostid());
                                                startActivity(intent);
                                            }
                                            else{
                                                Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                                intent.putExtra("from", "profile");
                                                intent.putExtra("Bitmap", baseUserModel.getDp());
                                                intent.putExtra("caption", baseUserModel.getDpcaption());
                                                startActivity(intent);
                                            }
                                        }
                                        else{
                                            Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                            intent.putExtra("from", "profile");
                                            intent.putExtra("Bitmap", baseUserModel.getDp());
                                            intent.putExtra("caption", baseUserModel.getDpcaption());
                                            startActivity(intent);
                                        }
                                    }
                                    else{
                                        Toast.makeText(ActivityProfile.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Pcoverpic.setOnClickListener(v -> {
                                    if (baseUserModel.getCoverpic()!=null) {
                                        if (task.getResult().getBoolean("iscovershared")!=null) {
                                            if(baseUserModel.isIscovershared()){
                                                Intent intent = new Intent(ActivityProfile.this, ViewMoreHome.class);
                                                intent.putExtra("from", "cover");
                                                intent.putExtra("type", type);
                                                intent.putExtra("postID", baseUserModel.getCoverpostid());
                                                startActivity(intent);
                                            }
                                            else{
                                                Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                                intent.putExtra("from", "profile");
                                                intent.putExtra("Bitmap", baseUserModel.getCoverpic());
                                                intent.putExtra("caption", baseUserModel.getCovercaption());
                                                startActivity(intent);
                                            }
                                        }
                                        else{
                                            Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
                                            intent.putExtra("from", "profile");
                                            intent.putExtra("Bitmap", baseUserModel.getCoverpic());
                                            intent.putExtra("caption", baseUserModel.getCovercaption());
                                            startActivity(intent);
                                        }
                                    }
                                    else{
                                        Toast.makeText(ActivityProfile.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                if (uid.matches(FirebaseAuth.getInstance().getUid()) ) {

                                    editCover.setVisibility(View.VISIBLE);
                                    editDp.setVisibility(View.VISIBLE);
                                    selfProfile.setVisibility(View.VISIBLE);
                                    edit_profile.setVisibility(View.VISIBLE);
                                    elseProfile.setVisibility(View.GONE);

                                    edit_profile.setOnClickListener(v -> {
                                        Intent i1 = new Intent(ActivityProfile.this, EditProfileCommitteeActivity.class);
                                        startActivity(i1);
                                        finish();
                                    });

//                                    editDp.setOnClickListener(v -> {
//                                        if (!checkStoragePermission()) {
//                                            requestStoragePermission();
//                                        } else {
//                                            imageCoverOrDp = 0; //dp
//                                            pickGallery();
//                                        }
//                                    });

                                    editDp.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(ActivityProfile.this,UpdateProfilePictureActivity.class);
                                            i.putExtra("uid",uid);
                                            i.putExtra("type",baseUserModel.getType());
                                            i.putExtra("gender",baseUserModel.getGender());
                                            i.putExtra("dp",baseUserModel.getDp());
//                                            if(baseUserModel.getDpcation()!=null && !baseUserModel.getDpcation().isEmpty()){
//                                                i.putExtra("dpcaption",baseUserModel.getDpcation());
//                                            }
                                            startActivity(i);
                                            finish();
                                        }
                                    });

//                                    editCover.setOnClickListener(v -> {
//                                        if (!checkStoragePermission()) {
//                                            requestStoragePermission();
//                                        } else {
//                                            imageCoverOrDp = 1; //cover
//                                            pickGallery();
//                                        }
//                                    });

                                    editCover.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent i = new Intent(ActivityProfile.this,UpdateCoverPictureActivity.class);
                                            i.putExtra("uid",uid);
                                            i.putExtra("type",baseUserModel.getType());
                                            i.putExtra("gender",baseUserModel.getGender());
                                            i.putExtra("cover",baseUserModel.getCoverpic());
//                                            if(baseUserModel.getDpcation()!=null && !baseUserModel.getDpcation().isEmpty()){
//                                                i.putExtra("dpcaption",baseUserModel.getDpcation());
//                                            }
                                            startActivity(i);
                                            finish();
                                        }
                                    });
                                }
                                else if(!uid.matches(FirebaseAuth.getInstance().getUid())) {
                                    selfProfile.setVisibility(View.GONE);
                                    edit_profile.setVisibility(View.GONE);
                                    editCover.setVisibility(View.GONE);
                                    editDp.setVisibility(View.GONE);
                                    elseProfile.setVisibility(View.VISIBLE);

                                    //increment no of visitors
                                    FirebaseFirestore.getInstance()
                                            .collection("Users")
                                            .document(uid)
                                            .update("pujoVisits", FieldValue.increment(1), "lastVisitTime", Timestamp.now());
                                    //increment no of visitors

                                    upvote.setOnClickListener(v -> {
                                        if (isLoadingFinished) {
                                            if (isUpvoted) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfile.this);
                                                builder.setTitle("Withdraw vote for " + baseUserModel.getName() + "?")
                                                        .setMessage("Are you sure?")
                                                        .setPositiveButton("Withdraw", (dialog, which) -> {

                                                            DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users").document(uid);

                                                            DocumentReference followerRef = docRef.collection("Upvoters").document(fireuser.getUid());

                                                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                                            batch.update(docRef, "upvoteL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                                            batch.delete(followerRef);

                                                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {
                                                                        FirebaseFirestore.getInstance()
                                                                                .collection("Users")
                                                                                .document(uid)
                                                                                .update("upvotes", FieldValue.increment(-1));

                                                                        upvote.setText("Upvote");
                                                                        upvote.setBackgroundResource(R.drawable.custom_button);
                                                                        upvote.setTextColor(getResources().getColor(R.color.reels_white));

                                                                        if (baseUserModel.getUpvoteL() != null) {
                                                                            if (baseUserModel.getUpvoteL().size() - 1 == 0) {
                                                                                upvoters.setText("0");
                                                                            } else if (baseUserModel.getUpvoteL().size() - 1 == 1) {
                                                                                upvoters.setText((baseUserModel.getUpvoteL().size() - 1) + "");
                                                                            } else {
                                                                                upvoters.setText((baseUserModel.getUpvoteL().size() - 1) + "");
                                                                            }
                                                                        } else {
                                                                            upvoters.setText("0");
                                                                            upvoters.setVisibility(View.GONE);
                                                                        }

                                                                        isUpvoted = false;
                                                                        baseUserModel.getUpvoteL().remove(fireuser.getUid());
                                                                    } else {
                                                                        Toast.makeText(ActivityProfile.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });

                                                        })
                                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                                        .setCancelable(true)
                                                        .show();
                                            }
                                            else {
                                                long tsLong = System.currentTimeMillis();

                                                SeenModel seenModel = new SeenModel();
                                                seenModel.setUid(fireuser.getUid());
                                                seenModel.setUserdp(introPref.getUserdp());
                                                seenModel.setUsername(introPref.getFullName());
                                                seenModel.setType(introPref.getType());
                                                seenModel.setTs(tsLong);

                                                DocumentReference docRef = FirebaseFirestore.getInstance()
                                                        .collection("Users")
                                                        .document(uid);

                                                DocumentReference followerRef = docRef.collection("Upvoters").document(fireuser.getUid());

                                                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                                batch.update(docRef, "upvoteL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                                batch.set(followerRef, seenModel);

                                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            upvote_anim.setVisibility(View.VISIBLE);
                                                            upvote_anim.playAnimation();

                                                            try {
                                                                AssetFileDescriptor afd = ActivityProfile.this.getAssets().openFd("fireworks.mp3");
                                                                MediaPlayer player = new MediaPlayer();
                                                                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                                                player.prepare();
                                                                AudioManager audioManager = (AudioManager) ActivityProfile.this.getSystemService(Context.AUDIO_SERVICE);
                                                                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                                                    player.start();
                                                                }
                                                                new Handler().postDelayed(() -> {
                                                                    upvote_anim.cancelAnimation();
                                                                    upvote_anim.setVisibility(View.GONE);
                                                                }, player.getDuration());
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }

                                                            FirebaseFirestore.getInstance()
                                                                    .collection("Users")
                                                                    .document(uid)
                                                                    .update("upvotes", FieldValue.increment(1));

                                                            upvote.setText("Upvoted");
                                                            upvote.setBackgroundResource(R.drawable.custom_button_outline);
                                                            upvote.setTextColor(getResources().getColor(R.color.purple));

                                                            if (baseUserModel.getUpvoteL() != null) {
                                                                if (baseUserModel.getUpvoteL().size() + 1 == 0) {
                                                                    upvoters.setText("0");
                                                                } else if (baseUserModel.getUpvoteL().size() + 1 == 1) {
                                                                    upvoters.setText((baseUserModel.getUpvoteL().size() + 1) + "");
                                                                } else {
                                                                    upvoters.setText((baseUserModel.getUpvoteL().size() + 1) + "");
                                                                }
                                                            } else {
                                                                upvoters.setText("0");
                                                            }

                                                            baseUserModel.getUpvoteL().add(fireuser.getUid());
                                                            isUpvoted = true;
                                                        } else {
                                                            Toast.makeText(ActivityProfile.this, "Upvoting is closed for now", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });

                                    locate.setOnClickListener(view -> {
                                        if (cm.getActiveNetworkInfo() != null) {
                                            String location = name + "," + address + "," + city + "," + state + "-" + pin;
                                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(location) + "&mode=w");
                                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                            mapIntent.setPackage("com.google.android.apps.maps");
                                            startActivity(mapIntent);
                                        } else {
                                            Toast.makeText(ActivityProfile.this, "Please check your internet connection and try again...", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    if(Integer.parseInt(String.valueOf(upvoters.getText())) > 0) {
                                        upvoteHolder.setOnClickListener(view -> {
                                            BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Upvotes", uid);
                                            bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
                                        });
                                    }
                                }

                                }
                            ///////////////for committee///////////////

                            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                                int scrollRange = -1;
                                @Override
                                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                                    if (scrollRange == -1) {
                                        scrollRange = appBarLayout.getTotalScrollRange();
                                    }

                                    if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
                                        //  Collapsed
                                        collapsingToolbarLayout.setTitle(baseUserModel.getName());
                                    }
                                    else {
                                        //Expanded
                                        collapsingToolbarLayout.setTitle(" ");
                                    }
                                }
                            });
                        }
                        else {
                            BasicUtility.showToast(ActivityProfile.this, "Something went wrong...");
                        }
                    })
                    .addOnFailureListener(e -> BasicUtility.showToast(ActivityProfile.this, "Something went wrong..."));
        }

        if(getIntent().getStringExtra("to") != null && getIntent().getStringExtra("to").matches("profile")) {
            BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Upvotes", uid);
            bottomSheetDialog.show(getSupportFragmentManager(), "FlamedBySheet");
        }

//        PDp.setOnClickListener(v -> {
//            if (baseUserModel != null) {
//                if (baseUserModel.getDp() != null && baseUserModel.getDp().length() > 2) {
//                    Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
//                    intent.putExtra("from", "profile");
//                    intent.putExtra("Bitmap", baseUserModel.getDp());
//                    intent.putExtra("caption",baseUserModel.getDpcaption());
//                    startActivity(intent);
//                }
//            } else {
//                Toast.makeText(ActivityProfile.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Pcoverpic.setOnClickListener(v -> {
//            if (baseUserModel != null) {
//                if (baseUserModel.getCoverpic() != null) {
//                    Intent intent = new Intent(ActivityProfile.this, ProfilePictureActivity.class);
//                    intent.putExtra("from", "profile");
//                    intent.putExtra("Bitmap", baseUserModel.getCoverpic());
//                    intent.putExtra("caption",baseUserModel.getCovercaption());
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(ActivityProfile.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    private void setupViewPager(ViewPager viewPager) {
        if(uid!=null){
            FirebaseFirestore.getInstance().collection("Users")
                    .document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    BaseUserModel baseUserModel = task.getResult().toObject(BaseUserModel.class);
                    type=baseUserModel.getType();

                    ProfileAdapter profileAdapter = new ProfileAdapter(getSupportFragmentManager());
                    if(type!=null){
                        if(type.matches("com")){
                            profileAdapter.addFragment(Fragment_Posts.newInstance(uid), "Posts");
                            profileAdapter.addFragment(Fragment_Reels.newInstance(uid), "Clips");
                        }
                        else if(type.matches("indi")){
                            profileAdapter.addFragment(Fragment_Posts_Users.newInstance(uid), "Posts");
                            profileAdapter.addFragment(Fragment_Reels_Users.newInstance(uid),"Clips");
                        }
                    }

                    viewPager.setAdapter(profileAdapter);

                    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

                        @Override
                        public void onPageSelected(int position) {

                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            BasicUtility.showToast(ActivityProfile.this,"Something went wrong");
                        }
                    });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        else if(id == R.id.share_profile) {
            BottomSheetDialog dialog = new BottomSheetDialog(ActivityProfile.this);
            dialog.setContentView(R.layout.dialog_share_menu);
            dialog.setCanceledOnTouchOutside(true);
            String text = "";
            String link = "https://www.applex.in/utsav-app/profile/" + baseUserModel.getName().replace(" ", "_") + "/" + uid;
            if(bool == 0) {
                text = "Check out my profile on Utsav!\n\nProfile Link :\n";
            }
            else if(bool == 1) {
                text = "Check out " + baseUserModel.getName() + "\'s profile on Utsav!\n\nProfile Link :\n";
            }

            String finalText = text;
            dialog.findViewById(R.id.copy_link).setOnClickListener(v -> {
                BasicUtility.showToast(ActivityProfile.this, "Copied to clipboard");
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", link);
                clipboard.setPrimaryClip(clip);
                dialog.dismiss();
            });

            dialog.findViewById(R.id.share_profile).setOnClickListener(v -> {
                Picasso.get().load(baseUserModel.getDp()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        if(BasicUtility.checkStoragePermission(ActivityProfile.this)) {
                            String finalbitmap = MediaStore.Images.Media.insertImage(
                                    getContentResolver(), bitmap, String.valueOf(System.currentTimeMillis()), null);
                            Uri uri =  Uri.parse(finalbitmap);
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("*/*");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, finalText + link);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(shareIntent,"Share Using"));
                            dialog.dismiss();
                        }
                        else {
                            BasicUtility.requestStoragePermission(ActivityProfile.this);
                        }
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            });

            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ActivityProfile.this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }
    //////////////////////PREMISSIONS//////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                try {
                    filePath = data.getData();
                    if (filePath != null) {
                        if (imageCoverOrDp == 0) {
                            CropImage.activity(filePath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setAspectRatio(1, 1)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(ActivityProfile.this);
                        } else {
                            CropImage.activity(filePath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setAspectRatio(16, 9)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(ActivityProfile.this);
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
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
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

    private void pickGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onResume() {
        if(change > 0 || delete > 0) {
//            buildRecycler();
            change = 0;
            delete = 0;
        }

        super.onResume();
        if(mode_changed == 1) {
            mode_changed = 0;
            startActivity(new Intent(ActivityProfile.this, ActivityProfile.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private Bitmap bitmap, compressedBitmap;

        public ImageCompressor(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ActivityProfile.this);
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
        protected void onPostExecute(byte[] picCompressed) {
            if (picCompressed != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(picCompressed, 0, picCompressed.length);
                if (imageCoverOrDp == 0) {
                    PDp.setImageBitmap(bitmap);
                } else {
                    Pcoverpic.setImageBitmap(bitmap);
                }
                pic = picCompressed;
                FirebaseStorage storage;
                StorageReference storageReference;
                StorageReference reference;
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference();
                long ts = Calendar.getInstance().getTimeInMillis();

                if (imageCoverOrDp == 1) {
                    reference = storageReference.child("Users/")
                            .child("Coverpic/")
                            .child(FirebaseAuth.getInstance().getUid() +ts+ "_coverpic");
                } else {
                    reference = storageReference.child("Users/")
                            .child("DP/")
                            .child(FirebaseAuth.getInstance().getUid() +ts+ "_dp");
                }

                reference.putBytes(picCompressed)
                        .addOnSuccessListener(taskSnapshot ->
                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String generatedFilePath = uri.toString();
                                    DocumentReference docref = FirebaseFirestore.getInstance()
                                            .collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                                    if (imageCoverOrDp == 0) {
                                        docref.update("dp", generatedFilePath).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                baseUserModel.setDp(generatedFilePath);
                                                introPref.setUserdp(generatedFilePath);
                                                progressDialog.dismiss();
                                            } else {
                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                            }
                                        });
                                    } else {
                                        docref.update("coverpic", generatedFilePath).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                baseUserModel.setCoverpic(generatedFilePath);
                                                progressDialog.dismiss();
                                            } else {
                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
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