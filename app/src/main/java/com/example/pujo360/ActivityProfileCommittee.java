package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.example.pujo360.LinkPreview.ApplexLinkPreview;
import com.example.pujo360.adapters.ProfileAdapter;
import com.example.pujo360.fragments.Fragment_Posts;
import com.example.pujo360.fragments.Fragment_Reels;
import com.example.pujo360.models.PujoCommitteeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

public class ActivityProfileCommittee extends AppCompatActivity {

    public static int delete = 0;
    private TextView PName,PUsername,PDescription,PInstitute,Pcourse,totalcount,flamecount,commentcount;
    private TextView verify;
    private ImageView PDp,infobadge, starondp, noPost,Pcoverpic;
    private ReadMoreTextView PDetaileddesc;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String name, type, coverpic, dp;
    public static String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_committee);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        PDp = findViewById(R.id.Pdp);
        PName = findViewById(R.id.Profilename);
        PUsername =findViewById(R.id.Pusername);
        Pcoverpic = findViewById(R.id.coverpic);

        tabLayout = findViewById(R.id.tabBar);
        viewPager = findViewById(R.id.viewPager);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0);
        tabLayout.getTabAt(1);

        name = getIntent().getStringExtra("name");
        coverpic = getIntent().getStringExtra("coverpic");
        dp = getIntent().getStringExtra("dp");
        uid = getIntent().getStringExtra("comID");

        if(uid!=null)
        {
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
                                                       PName.setText(name);
                                                       PUsername.setText(model.getType());
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

                                                   }

                                               }
                                           });
        }

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
}