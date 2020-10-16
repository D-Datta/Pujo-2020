package com.applex.utsav.drawerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.applex.utsav.ActivityNotification;
import com.applex.utsav.MainActivity;
import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.preferences.IntroPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Objects;

public class AboutUs extends AppCompatActivity {
    IntroPref introPref;

    private TextView tsnaplingo, tcampus24, tjee, tinnovacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPref = new IntroPref(AboutUs.this);
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

        setContentView(R.layout.activity_about_us);
//
//        Toolbar toolbar= findViewById(R.id.toolb);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle("Edit Comment");
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        tsnaplingo = findViewById(R.id.link_snaplingo);
        tcampus24 = findViewById(R.id.link_campus24);
        tjee = findViewById(R.id.link_jeeab360);
        tinnovacion = findViewById(R.id.link_innovacion);

        tsnaplingo.setMovementMethod(LinkMovementMethod.getInstance());
        tcampus24.setMovementMethod(LinkMovementMethod.getInstance());
        tjee.setMovementMethod(LinkMovementMethod.getInstance());
        tinnovacion.setMovementMethod(LinkMovementMethod.getInstance());

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(AboutUs.this, (value, error) -> {
                        if(value.getBoolean("listener")) {
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
                                    FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid()).update("listener", false);
                                    startActivity(new Intent(AboutUs.this, AboutUs.class));
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                }, 200);
                            });
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}