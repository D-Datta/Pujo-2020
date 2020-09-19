package com.example.pujo360;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
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
import com.google.android.material.tabs.TabLayout;
import com.smarteist.autoimageslider.SliderView;

public class ActivityProfileCommittee extends AppCompatActivity {

    public static int delete = 0;
    private TextView PName,PUsername,PDescription,PInstitute,Pcourse,totalcount,flamecount,commentcount;
    private TextView verify;
    private ImageView PDp,infobadge, starondp, noPost,Pcoverpic;
    private ReadMoreTextView PDetaileddesc;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_committee);

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