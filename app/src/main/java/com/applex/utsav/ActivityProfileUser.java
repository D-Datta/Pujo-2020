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
                    .addSnapshotListener(ActivityProfileUser.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("night_mode")) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                            if(value.getBoolean("listener")) {
                                MainActivity.mode_changed = 1;
                                FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                                startActivity(new Intent(ActivityProfileUser.this, ActivityProfileUser.class));
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        } else {
                            MainActivity.mode_changed = 1;
                            FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            startActivity(new Intent(ActivityProfileUser.this, ActivityProfileUser.class));
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }
                    });
        } else if(introPref.getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(introPref.getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
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
        if(mode_changed == 1) {
            mode_changed = 0;
            startActivity(new Intent(ActivityProfileUser.this, ActivityProfileUser.class));
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