package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pujo360.adapters.HomeTabAdapter;
import com.example.pujo360.fragments.CommitteeFragment;
import com.example.pujo360.fragments.FeedsFragment;
import com.example.pujo360.preferences.IntroPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Button ImageView360;
    @SuppressLint("StaticFieldLeak")
    static MainActivity instance;
    private DrawerLayout drawer;

    private IntroPref introPref;
    private ImageView toolbarImage;
    private String USERNAME;
    private String PROFILEPIC;


    NavigationView navigationView;
    ///NAV DRAWER VARIABLES/////
    ImageView displaypic;
    TextView username;
    TextView name;
    ///NAV DRAWER VARIABLES/////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        introPref = new IntroPref(MainActivity.this);

        ///////////////NOTIFICATIONS///////////////////
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel=new NotificationChannel("MyNotifications","MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            Objects.requireNonNull(manager).createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("users").addOnCompleteListener(task -> { });
        ///////////////NOTIFICATIONS///////////////////

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View hView;
        hView = navigationView.getHeaderView(0);
        name = hView.findViewById(R.id.nav_Name);
        displaypic = hView.findViewById(R.id.displaypic);

        hView.setOnClickListener(v -> {
            drawer.closeDrawers();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    Intent i = new Intent(MainActivity.this, ProfileActivity.class);
//                    i.putExtra("uid", FirebaseAuth.getInstance().getUid());
//                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            },200);
        });

        toolbarImage= findViewById(R.id.toolbarimg1);
        toolbarImage.setOnClickListener(v -> {
            drawer.openDrawer(GravityCompat.START);
        });

        TabLayout tabs = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.view_pager);
        setupViewPager(viewPager);
        tabs.setupWithViewPager(viewPager);

        tabs.getTabAt(0);
        tabs.getTabAt(1);

//        ImageView360 = findViewById(R.id.Viewer);
//        ImageView360.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this, ImageViewer360.class);
//                startActivity(i);
//            }
//        });

    }

    private void setupViewPager(ViewPager viewPager) {
        HomeTabAdapter tabAdapter = new HomeTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new CommitteeFragment(), "Committees");
        tabAdapter.addFragment(new FeedsFragment(),"Feeds");
        viewPager.setAdapter(tabAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                CommitteeFragment.swipe = 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    public static MainActivity getInstance() { return instance; }

    @Override
    protected void onResume() {
        super.onResume();
        introPref = new IntroPref(MainActivity.this);
        USERNAME = introPref.getFullName();
        PROFILEPIC = introPref.getUserdp();

        navigationView = findViewById(R.id.nav_view);
        View hView;
        hView = navigationView.getHeaderView(0);
        name = hView.findViewById(R.id.nav_Name);
        displaypic = hView.findViewById(R.id.displaypic);

        toolbarImage= findViewById(R.id.toolbarimg1);

        name.setText(USERNAME);

        if(PROFILEPIC!=null){
//            if(PROFILEPIC.matches("0")){
//                displaypic.get().setImageResource(R.drawable.default_dp_1);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_1);
//            }
//            else if(PROFILEPIC.matches("1")){
//                displaypic.get().setImageResource(R.drawable.default_dp_2);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_2);
//            }
//            else if(PROFILEPIC.matches("2")){
//                displaypic.get().setImageResource(R.drawable.default_dp_3);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_3);
//            }
//            else if(PROFILEPIC.matches("3")){
//                displaypic.get().setImageResource(R.drawable.default_dp_4);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_4);
//            }
//            else if(PROFILEPIC.matches("4")){
//                displaypic.get().setImageResource(R.drawable.default_dp_5);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_5);
//            }
//            else if(PROFILEPIC.matches("5")){
//                displaypic.get().setImageResource(R.drawable.default_dp_6);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_6);
//            }
//            else if(PROFILEPIC.matches("6")){
//                displaypic.get().setImageResource(R.drawable.default_dp_7);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_7);
//            }
//            else if(PROFILEPIC.matches("7")){
//                displaypic.get().setImageResource(R.drawable.default_dp_8);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_8);
//            }
//            else if(PROFILEPIC.matches("8")){
//                displaypic.get().setImageResource(R.drawable.default_dp_9);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_9);
//            }
//            else if(PROFILEPIC.matches("9")){
//                displaypic.get().setImageResource(R.drawable.default_dp_10);
//                Toolbar_img1.get().setImageResource(R.drawable.default_dp_10);
//            }
//            else{
                Picasso.Builder builder = new Picasso.Builder(getApplicationContext());
                builder.downloader(new OkHttp3Downloader(getApplicationContext(), Integer.MAX_VALUE));
                Picasso built = builder.build();
                built.setIndicatorsEnabled(true);
                built.setLoggingEnabled(true);
                Picasso.get()
                        .load(PROFILEPIC)
                        .error(R.drawable.image_background_grey)
                        .placeholder(R.drawable.image_background_grey)
                        .networkPolicy(NetworkPolicy.OFFLINE).into(displaypic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(PROFILEPIC).into(displaypic);
                    }
                });
                Picasso.get()
                        .load(PROFILEPIC)
                        .error(R.drawable.image_background_grey)
                        .placeholder(R.drawable.image_background_grey)
                        .networkPolicy(NetworkPolicy.OFFLINE).into(toolbarImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(PROFILEPIC).into(toolbarImage);
                    }
                });
            }

//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}