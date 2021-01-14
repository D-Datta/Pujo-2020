package com.applex.utsav.ViewAllGridMVVM;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applex.utsav.MainActivity;
import com.applex.utsav.R;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.preferences.IntroPref;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Objects;

public class ViewAllGridActivity extends AppCompatActivity {

    private RecyclerView recyclerview;
    IntroPref introPref;
    ShimmerFrameLayout shimmerFrameLayout;
    ProgressBar contentprogressposts, progressmoreposts;
    public static ImageView noneImage;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        setContentView(R.layout.activity_view_all_grid);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getIntent().getStringExtra("username"));

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        recyclerview = findViewById(R.id.recycler_posts);
        contentprogressposts = findViewById(R.id.content_progress);
        progressmoreposts = findViewById(R.id.progress_more_posts);
        noneImage = findViewById(R.id.none_image);
        floatingActionButton = findViewById(R.id.to_the_top);

        shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        contentprogressposts.setVisibility(View.GONE);
        recyclerview.setVisibility(View.GONE);

        recyclerview.setHasFixedSize(false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setItemViewCacheSize(20);

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(ViewAllGridActivity.this, (value, error) -> {
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
                                        startActivity(new Intent(ViewAllGridActivity.this, ViewAllGridActivity.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }

        ///////////////Set Image Bitmap/////////////////////
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {

            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                options.inSampleSize = Math.round((float) width / (float) displayWidth);
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            noneImage.setImageBitmap(scaledBitmap);
        }
        else if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {

            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                options.inSampleSize = Math.round((float) width / (float) displayWidth);
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            noneImage.setImageBitmap(scaledBitmap);
        }
        ///////////////Set Image Bitmap/////////////////////

        ViewAllGridViewModel viewAllGridViewModel = new ViewAllGridViewModel(
                getIntent().getStringExtra("timestamp"), getIntent().getStringExtra("uid"));
        ViewAllGridPagedAdapter viewAllGridPagedAdapter = new ViewAllGridPagedAdapter(ViewAllGridActivity.this);
        viewAllGridViewModel.getPostLiveData().observe(ViewAllGridActivity.this, homePostModels -> new Handler().postDelayed(() -> {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);
            viewAllGridPagedAdapter.submitList(homePostModels);
        }, 1000));
        recyclerview.setAdapter(viewAllGridPagedAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
