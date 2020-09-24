package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.example.pujo360.LinkPreview.ApplexLinkPreview;
import com.example.pujo360.adapters.ProfileAdapter;
import com.example.pujo360.fragments.Fragment_Posts;
import com.example.pujo360.fragments.Fragment_Reels;
import com.example.pujo360.models.BaseUserModel;
import com.example.pujo360.models.PujoCommitteeModel;
import com.example.pujo360.util.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

public class ActivityProfileCommittee extends AppCompatActivity {

    public static int delete = 0;
    private TextView PName,PUsername,Paddress,PDescription,PInstitute,Pcourse,totalcount,flamecount,commentcount;
    private TextView verify;
    private ImageView PDp,infobadge, starondp, noPost,Pcoverpic;
    private ReadMoreTextView PDetaileddesc;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String name, pujotype, coverpic, dp, address, city, state, pin, desc;
    public static String uid;
    private com.google.android.material.floatingactionbutton.FloatingActionButton edit_profile_com;
    private FirebaseUser fireuser;
    int bool;
    private Button locate;
    private ConnectivityManager cm;

    private TextView visits, likes, followers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_committee);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        cm = (ConnectivityManager) ActivityProfileCommittee.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        PDp = findViewById(R.id.Pdp);
        PName = findViewById(R.id.Profilename);
        PUsername =findViewById(R.id.Pusername);
        Pcoverpic = findViewById(R.id.coverpic);
        PDetaileddesc = findViewById(R.id.detaildesc);
        edit_profile_com = findViewById(R.id.edit_profile_com);
        locate = findViewById(R.id.locate);
        Paddress = findViewById(R.id.address_com);

        visits = findViewById(R.id.visits);
        likes = findViewById(R.id.likes);
        followers = findViewById(R.id.followers);

        tabLayout = findViewById(R.id.tabBar);
        viewPager = findViewById(R.id.viewPager);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0);
        tabLayout.getTabAt(1);

        fireuser = FirebaseAuth.getInstance().getCurrentUser();

        name = getIntent().getStringExtra("name");
        coverpic = getIntent().getStringExtra("coverpic");
        dp = getIntent().getStringExtra("dp");
        uid = getIntent().getStringExtra("uid");

        cm = (ConnectivityManager) ActivityProfileCommittee.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////
        if(getIntent()!=null && getIntent().getStringExtra("uid")!=null){
            uid = getIntent().getStringExtra("uid");
            if(!uid.matches(fireuser.getUid())){
                bool =1;//ANOTHER USER ACCOUNT
            }
        }
        else{
            uid = fireuser.getUid();
            bool = 0;//CURRENT USER ACCOUNT
        }
        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////

        if(uid.matches(FirebaseAuth.getInstance().getUid())) {
            edit_profile_com.setVisibility(View.VISIBLE);
            edit_profile_com.setOnClickListener(v -> {
                Intent i1 = new Intent(ActivityProfileCommittee.this, EditProfileCommitteeActivity.class);
                startActivity(i1);
                finish();
            });
        }
        else {
            //increment no of visitors
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(FirebaseAuth.getInstance().getUid())
                    .update("pujoVisits", FieldValue.increment(1));

        }


        //setup profile
        if(uid!=null)
        {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(uid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                BaseUserModel baseUserModel = task.getResult().toObject(BaseUserModel.class);
                                name = baseUserModel.getName();
                                PName.setText(name);
                                dp = baseUserModel.getDp();
                                address = baseUserModel.getAddressline();
                                city = baseUserModel.getCity();
                                state = baseUserModel.getState();
                                if(baseUserModel.getPin()!=null && !baseUserModel.getPin().isEmpty()) {
                                    pin=baseUserModel.getPin();
                                }
                                String fulladd = address+"\n"+city+" , "+state+" - "+pin;
                                Paddress.setText(fulladd);
                                coverpic = baseUserModel.getCoverpic();
                                if(dp!=null){
//
                                    Picasso.get().load(dp).placeholder(R.drawable.image_background_grey).into(PDp);
                                }
                                else{
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
                                    Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
                                    PDp.setImageBitmap(scaledBitmap);
                                }

                                if(coverpic!=null){
                                    Picasso.get().load(coverpic).placeholder(R.drawable.image_background_grey).into(Pcoverpic);
                                }
                                else{
                                    Display display = getWindowManager().getDefaultDisplay();
                                    int displayWidth = display.getWidth();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;
                                    BitmapFactory.decodeResource(getResources(), R.drawable.dhaki_png, options);
                                    int width = options.outWidth;
                                    if (width > displayWidth) {
                                        int widthRatio = Math.round((float) width / (float) displayWidth);
                                        options.inSampleSize = widthRatio;
                                    }
                                    options.inJustDecodeBounds = false;
                                    Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dhaki_png, options);
                                    Pcoverpic.setImageBitmap(scaledBitmap);
                                }

                                //metrics
                                visits.setText(baseUserModel.getPujoVisits()+"");
                                likes.setText(baseUserModel.getLikeCount()+"");
                                //metrics


                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(uid)
                                        .collection("com")
                                        .document(uid)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful())
                                                {
                                                    PujoCommitteeModel model = task.getResult().toObject(PujoCommitteeModel.class);
                                                    pujotype=model.getType();
                                                    PUsername.setText(pujotype);
                                                    if(model.getDescription()!=null && !model.getDescription().isEmpty()){
                                                        desc=model.getDescription();
                                                        PDetaileddesc.setText(desc);
                                                    }
                                                }
                                                else{
                                                    Utility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Utility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                                            }
                                        });

                            }
                            else{
                                Utility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                            }
                        });

        }

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cm.getActiveNetworkInfo() != null) {
                    String location = address+","+city+","+state+"-"+pin;
                    if (location.length() != 0) {
                        Uri gmmIntentUri = Uri.parse("geo:0,0?z=15&q=" + Uri.encode(location));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(ActivityProfileCommittee.this, "Field Empty", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(ActivityProfileCommittee.this, "Please check your internet connection and try again...", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }

    private void setupViewPager(ViewPager viewPager)
    {
        ProfileAdapter profileAdapter = new ProfileAdapter(getSupportFragmentManager());
        profileAdapter.addFragment(new Fragment_Posts(), "Posts");
        profileAdapter.addFragment(new Fragment_Reels(),"Reels");


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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}