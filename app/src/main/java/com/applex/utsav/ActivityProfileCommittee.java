package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.models.SeenModel;
import com.applex.utsav.utility.BasicUtility;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.applex.utsav.adapters.ProfileAdapter;
import com.applex.utsav.fragments.Fragment_Posts;
import com.applex.utsav.fragments.Fragment_Reels;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.PujoCommitteeModel;
import com.applex.utsav.preferences.IntroPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ActivityProfileCommittee extends AppCompatActivity {

    public static int delete = 0;
    private TextView PName, PUsername, Paddress;

    private ImageView PDp, Pcoverpic, verified;
    private ReadMoreTextView PDetaileddesc;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String name, pujotype, coverpic, dp, address, city, state, pin, desc;

    public String uid;
    private FirebaseUser fireuser;
    int bool;
    private ConnectivityManager cm;
    private BaseUserModel baseUserModel;

    private TextView visits, likes, upvoters;
    boolean isUpvoted = false;
    boolean isLoadingFinished = false;

    private int imageCoverOrDp = 0; //dp = 0, cover = 1
    private ImageView editDp, editCover;


    private Button locate;
    private Button upvote, edit_profile_com;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        introPref = new IntroPref(ActivityProfileCommittee.this);
        String lang = introPref.getLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

//        /////////////////DAY OR NIGHT MODE///////////////////
//        FirebaseFirestore.getInstance().document("Mode/night_mode")
//                .addSnapshotListener(ActivityProfileCommittee.this, (value, error) -> {
//                    if(value != null) {
//                        if(value.getBoolean("night_mode")) {
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                        } else {
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                        }
//                        if(value.getBoolean("listener")) {
//                            FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
//                            startActivity(new Intent(MainActivity.this, MainActivity.class));
//                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                            finish();
//                        }
//                    } else {
//                        FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    }
//                });
//        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_profile_committee);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cm = (ConnectivityManager) ActivityProfileCommittee.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        upvote_anim = findViewById(R.id.upvote_anim);
        PDp = findViewById(R.id.Pdp);
        PName = findViewById(R.id.Profilename);
        PUsername = findViewById(R.id.Pusername);
        Pcoverpic = findViewById(R.id.coverpic);
        PDetaileddesc = findViewById(R.id.detaildesc);
        edit_profile_com = findViewById(R.id.edit_profile_com);
        locate = findViewById(R.id.locate);
        Paddress = findViewById(R.id.address_com);
        verified = findViewById(R.id.verified);

        visits = findViewById(R.id.visits);
        likes = findViewById(R.id.likes);
        upvoters = findViewById(R.id.followers);
        editDp = findViewById(R.id.edit_dp);
        editCover = findViewById(R.id.edit_cover);

        selfProfile = findViewById(R.id.selfProfile);
        elseProfile = findViewById(R.id.elseProfile);

        upvote = findViewById(R.id.follow);

        tabLayout = findViewById(R.id.tabBar);
        viewPager = findViewById(R.id.viewPager);

        fireuser = FirebaseAuth.getInstance().getCurrentUser();

        name = getIntent().getStringExtra("name");
        coverpic = getIntent().getStringExtra("coverpic");
        dp = getIntent().getStringExtra("dp");

        cm = (ConnectivityManager) ActivityProfileCommittee.this.getSystemService(Context.CONNECTIVITY_SERVICE);

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

        if (uid.matches(FirebaseAuth.getInstance().getUid())) {
            editCover.setVisibility(View.VISIBLE);
            editDp.setVisibility(View.VISIBLE);

            selfProfile.setVisibility(View.VISIBLE);
            elseProfile.setVisibility(View.GONE);

            edit_profile_com.setVisibility(View.VISIBLE);
            edit_profile_com.setOnClickListener(v -> {
                Intent i1 = new Intent(ActivityProfileCommittee.this, EditProfileCommitteeActivity.class);
                startActivity(i1);
                finish();
            });

            editDp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        imageCoverOrDp = 0; //dp
                        pickGallery();
                    }
                }
            });

            editCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        imageCoverOrDp = 1; //cover
                        pickGallery();
                    }
                }
            });
        }
        else {
            selfProfile.setVisibility(View.GONE);
            elseProfile.setVisibility(View.VISIBLE);

            edit_profile_com.setVisibility(View.GONE);
            editCover.setVisibility(View.GONE);
            editDp.setVisibility(View.GONE);

            //increment no of visitors
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(uid)
                    .update("pujoVisits", FieldValue.increment(1), "lastVisitTime", Timestamp.now());

//            set the last time profile was visited
//            FirebaseFirestore.getInstance()
//                    .collection("Users")
//                    .document(uid)
//                    .update("lastVisitTime", Timestamp.now());

            upvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isLoadingFinished) {
                        if (isUpvoted) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileCommittee.this);
                            builder.setTitle("Withdraw vote for " + baseUserModel.getName() + "?")
                                    .setMessage("Are you sure?")
                                    .setPositiveButton("Withdraw", (dialog, which) -> {

                                        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users").document(uid);

                                        DocumentReference followerRef = docRef.collection("Upvoters").document(fireuser.getUid());

                                        WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                        batch.update(docRef, "upvoteL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                        batch.delete(followerRef);

                                        batch.commit().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                FirebaseFirestore.getInstance()
                                                        .collection("Users")
                                                        .document(uid)
                                                        .update("upvotes", FieldValue.increment(-1));

                                                upvote.setText("Upvote");
                                                upvote.setBackgroundResource(R.drawable.custom_button);
                                                upvote.setTextColor(getResources().getColor(R.color.white));

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
                                                Toast.makeText(ActivityProfileCommittee.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();
                        } else {
                            long tsLong = System.currentTimeMillis();

                            SeenModel seenModel = new SeenModel();
                            seenModel.setUid(fireuser.getUid());
                            seenModel.setUserdp(introPref.getUserdp());
                            seenModel.setUsername(introPref.getFullName());
                            seenModel.setType(introPref.getType());
                            seenModel.setTs(tsLong);

                            upvote_anim.setVisibility(View.VISIBLE);
                            upvote_anim.playAnimation();

                            try {
                                AssetFileDescriptor afd = ActivityProfileCommittee.this.getAssets().openFd("fireworks.mp3");
                                MediaPlayer player = new MediaPlayer();
                                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                player.prepare();
                                AudioManager audioManager = (AudioManager) ActivityProfileCommittee.this.getSystemService(Context.AUDIO_SERVICE);
                                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                    player.start();
                                    if (!player.isPlaying()) {
                                        upvote_anim.cancelAnimation();
                                        upvote_anim.setVisibility(View.GONE);
                                    }
                                    player.setOnCompletionListener(mediaPlayer -> {
                                        upvote_anim.cancelAnimation();
                                        upvote_anim.setVisibility(View.GONE);
                                    });
                                } else {
                                    new Handler().postDelayed(() -> {
                                        upvote_anim.cancelAnimation();
                                        upvote_anim.setVisibility(View.GONE);
                                    }, 2000);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            DocumentReference docRef = FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(uid);

                            DocumentReference followerRef = docRef.collection("Upvoters").document(fireuser.getUid());

                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            batch.update(docRef, "upvoteL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(followerRef, seenModel);

                            batch.commit().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
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
                                    Toast.makeText(ActivityProfileCommittee.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                }
            });

        }

        PDp.setOnClickListener(v -> {
            if (baseUserModel != null) {
                if (baseUserModel.getDp() != null && baseUserModel.getDp().length() > 2) {
                    Intent intent = new Intent(ActivityProfileCommittee.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", baseUserModel.getDp());
                    startActivity(intent);
                }
            } else {
                Toast.makeText(ActivityProfileCommittee.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
            }
        });

        Pcoverpic.setOnClickListener(v -> {
            if (baseUserModel != null) {
                if (baseUserModel.getCoverpic() != null) {
                    Intent intent = new Intent(ActivityProfileCommittee.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", baseUserModel.getCoverpic());
                    startActivity(intent);
                } else {
                    Toast.makeText(ActivityProfileCommittee.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //setup profile
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(uid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                baseUserModel = task.getResult().toObject(BaseUserModel.class);
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
                                dp = baseUserModel.getDp();
                                address = baseUserModel.getAddressline();
                                city = baseUserModel.getCity();
                                state = baseUserModel.getState();
                                if (baseUserModel.getPin() != null && !baseUserModel.getPin().isEmpty()) {
                                    pin = baseUserModel.getPin();
                                }
                                String fulladd = address + "\n" + city + " , " + state + " - " + pin;
                                Paddress.setText(fulladd);
                                coverpic = baseUserModel.getCoverpic();
                                if (dp != null) {
//
                                    Picasso.get().load(dp).placeholder(R.drawable.image_background_grey).into(PDp);
                                } else {
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

                                if (coverpic != null) {
                                    Picasso.get().load(coverpic).placeholder(R.drawable.image_background_grey).into(Pcoverpic);
                                } else {
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

                                //metrics
                                if (baseUserModel.getPujoVisits() > 1) {
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

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(uid)
                                        .collection("com")
                                        .document(uid)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    PujoCommitteeModel model = task.getResult().toObject(PujoCommitteeModel.class);
                                                    pujotype = model.getType();
                                                    PUsername.setText(pujotype);
                                                    if (model.getDescription() != null && !model.getDescription().isEmpty()) {
                                                        desc = model.getDescription();
                                                        PDetaileddesc.setText(desc);
                                                    }
                                                } else {
                                                    BasicUtility.showToast(ActivityProfileCommittee.this, "Something went wrong...");
                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                BasicUtility.showToast(ActivityProfileCommittee.this, "Something went wrong...");
                                            }
                                        });

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
                                    upvote.setTextColor(getResources().getColor(R.color.white));

                                }

                                isLoadingFinished = true;

                            } else {
                                BasicUtility.showToast(ActivityProfileCommittee.this, "Something went wrong...");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            BasicUtility.showToast(ActivityProfileCommittee.this, "Something went wrong...");
                        }
                    });
        }


        PDp.setOnClickListener(v -> {
            if (baseUserModel != null) {
                if (baseUserModel.getDp() != null && baseUserModel.getDp().length() > 2) {
                    Intent intent = new Intent(ActivityProfileCommittee.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", baseUserModel.getDp());
                    startActivity(intent);
                }
            } else {
                Toast.makeText(ActivityProfileCommittee.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
            }
        });

        Pcoverpic.setOnClickListener(v -> {
            if (baseUserModel != null) {
                if (baseUserModel.getCoverpic() != null) {
                    Intent intent = new Intent(ActivityProfileCommittee.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", baseUserModel.getCoverpic());
                    startActivity(intent);
                } else {
                    Toast.makeText(ActivityProfileCommittee.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cm.getActiveNetworkInfo() != null) {
                    String location = name + "," + address + "," + city + "," + state + "-" + pin;
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(location) + "&mode=w");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(ActivityProfileCommittee.this, "Please check your internet connection and try again...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ProfileAdapter profileAdapter = new ProfileAdapter(getSupportFragmentManager());
        profileAdapter.addFragment(Fragment_Posts.newInstance(uid), "Posts");
        profileAdapter.addFragment(Fragment_Reels.newInstance(uid), "Clips");

        viewPager.setAdapter(profileAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
//                Fragment_Posts.swipe = 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ActivityProfileCommittee.this, storagePermission, STORAGE_REQUEST_CODE);
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
                                    .start(ActivityProfileCommittee.this);
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
                                    .start(ActivityProfileCommittee.this);
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

    @SuppressLint("StaticFieldLeak")
    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private Bitmap bitmap, compressedBitmap;

        public ImageCompressor(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ActivityProfileCommittee.this);
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