package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.applex.utsav.adapters.HomeTabAdapter;
import com.applex.utsav.drawerActivities.AboutUs;
import com.applex.utsav.fragments.CommitteeFragment;
import com.applex.utsav.fragments.FeedsFragment;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.util.DialogUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;

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
    private String TYPE;

    private String currentVersion;
    private String[] cameraPermission;


    NavigationView navigationView;
    ///NAV DRAWER VARIABLES/////
    ImageView displaypic;
    TextView username;
    TextView name;
    ///NAV DRAWER VARIABLES/////

    GoogleSignInClient mGooglesigninclient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        introPref = new IntroPref(MainActivity.this);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGooglesigninclient = GoogleSignIn.getClient(this, googleSignInOptions);

        ///////////////NOTIFICATIONS///////////////////
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel=new NotificationChannel("MyNotifications","MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            Objects.requireNonNull(manager).createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("users").addOnCompleteListener(task -> { });
        ///////////////NOTIFICATIONS///////////////////

        //////////////LATEST VERSION CHECK////////////////////
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new GetLatestVersion().execute();
        //////////////LATEST VERSION CHECK////////////////////

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
                    if(TYPE.matches("com")){

                        Intent i = new Intent(MainActivity.this, ActivityProfileCommittee.class);
                        i.putExtra("uid", FirebaseAuth.getInstance().getUid());
                        startActivity(i);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                    else if(TYPE.matches("indi")){
                        Intent i = new Intent(MainActivity.this, ActivityProfileUser.class);
                        i.putExtra("uid", FirebaseAuth.getInstance().getUid());
                        startActivity(i);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

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

    }

    private void setupViewPager(ViewPager viewPager) {
        HomeTabAdapter tabAdapter = new HomeTabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new CommitteeFragment(), "Pujo");
        tabAdapter.addFragment(new FeedsFragment(),"People");
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
        TYPE = introPref.getType();

        navigationView = findViewById(R.id.nav_view);
        View hView;
        hView = navigationView.getHeaderView(0);
        name = hView.findViewById(R.id.nav_Name);
        displaypic = hView.findViewById(R.id.displaypic);

        toolbarImage= findViewById(R.id.toolbarimg1);

        name.setText(USERNAME);

        if(PROFILEPIC!=null){

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
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Log out")
                            .setMessage("Do you want to continue?")
                            .setPositiveButton("Log out", (dialog, which) -> {
                                introPref.setFullName(null);
                                introPref.setUserdp(null);
                                introPref.setType(null);

                                FirebaseFirestore.getInstance()
                                        .collection("Users/" + FirebaseAuth.getInstance().getUid() + "/AccessToken/")
                                        .document("Token").delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseAuth.getInstance().signOut();
                                                mGooglesigninclient.signOut();
                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                finish();
                                            }
                                        });
//                                Intent broadcastIntent = new Intent();
//                                broadcastIntent.setAction("com.package.ACTION_LOGOUT");
//                                sendBroadcast(broadcastIntent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setCancelable(true)
                            .show();

                }
            }, 200);

        }

//        else if(id == R.id.nav_live){
//            drawer.closeDrawers();
//            Utility.showToast(MainActivity.this,"Coming Soon");
//        }

        else if(id == R.id.nav_tellafrnd){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawer.closeDrawers();
                    Intent i=new Intent();
                    i.setAction(Intent.ACTION_SEND);
                    String text="Hey! Join me at Utsav App: Durga Puja 2020 and experience the world of Celebration. Download now. https://play.google.com/store/apps/details?id=com.applex.utsav";
                    i.putExtra(Intent.EXTRA_TEXT,text);
                    i.setType("text/plain");
                    startActivity(Intent.createChooser(i,"Share with"));

                }
            },200);
        }

        else if(id == R.id.nav_contact){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, Webview.class);
                    intent.putExtra("text","https://applex.in/contact/");
                    intent.putExtra("bool","1");
                    startActivity(intent);
                }
            },200);
        }
        else if(id == R.id.nav_about){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, AboutUs.class);
                    intent.putExtra("text","https://applex.in/contact/");
                    intent.putExtra("bool","1");
                    startActivity(intent);
                }
            },200);
        }

        else if(id== R.id.nav_rate_us){
            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @SuppressLint("StaticFieldLeak")
    public class GetLatestVersion extends AsyncTask<Void, Void, Void> {

        private String latestVersion;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + getPackageName())
                        .timeout(8000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select(".hAyfc .htlgb")
                        .get(7)
                        .ownText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(currentVersion != null && latestVersion != null) {
                ////////////////////FOR THE DEVELOPERS/////////////////////
                if(Integer.parseInt(currentVersion.replace('.', '0')) > Integer.parseInt(latestVersion.replace('.', '0'))) {
                    Log.i("DICKEYDICK", "FUCK YOU");
                }
                ////////////////////FOR THE DEVELOPERS/////////////////////
                else if(!currentVersion.equalsIgnoreCase(latestVersion)) {

                    MaterialDialog dialog = DialogUtils.getInstance().createAnimationDialog4(MainActivity.this);
                    dialog.setCanceledOnTouchOutside(true);

                    TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
                    TextView update = (TextView) dialog.findViewById(R.id.update);

                    cancel.setOnClickListener(v -> dialog.dismiss());

                    update.setOnClickListener(v -> {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                        startActivity(intent);
                    });

                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
//                else if(currentVersion.equalsIgnoreCase(latestVersion) && introPref.isFirstTimeLaunchAfterUpdate()) {
//                    if (!checkCameraPermission()) {
//                        requestCameraPermission();
//                    }
//                    else {
//                        new MoveToFolders().execute();
//                    }
//                }
            }
        }
    }

}