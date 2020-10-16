package com.applex.utsav;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.registration.LoginActivity;
import com.applex.utsav.utility.StoreTemp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GateWayActivity extends AppCompatActivity {
    IntroPref introPref;

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
                    .addSnapshotListener(GateWayActivity.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("night_mode")) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                            if(value.getBoolean("listener")) {
                                MainActivity.mode_changed = 1;
                                FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                                startActivity(new Intent(GateWayActivity.this, GateWayActivity.class));
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        } else {
                            MainActivity.mode_changed = 1;
                            FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            startActivity(new Intent(GateWayActivity.this, GateWayActivity.class));
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

        setContentView(R.layout.activity_gate_way_activty);
        String campus;
        String postID;

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Uri uri = getIntent().getData();
            if(uri!=null) {
                List<String> params = uri.getPathSegments();
                if(params.size()>=3){
                    postID = params.get(3);
                    if(params.get(1).matches("feeds"))
                    {
                        if(params.get(2).matches("0"))
                        {
                            Intent i= new Intent(GateWayActivity.this, ViewMoreText.class);
                            i.putExtra("campus", "Text");
                            i.putExtra("postID", postID);
                            i.putExtra("from", "link");
                            startActivity(i);
                            finish();

                        }
                        else if(params.get(2).matches("1"))
                        {
                            Intent i= new Intent(GateWayActivity.this, ViewMoreHome.class);
                            i.putExtra("campus", "Image");
                            i.putExtra("postID", postID);
                            i.putExtra("from", "link");
                            startActivity(i);
                            finish();
                        }
                        else {
                            startActivity(new Intent(GateWayActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                    else if(params.get(1).matches("clips")){
                        if(params.get(2).matches("1")){
                            Intent i= new Intent(GateWayActivity.this, ReelsActivity.class);
//                            i.putExtra("campus", "Text");
                            i.putExtra("bool", "1");
                            i.putExtra("docID", postID);
//                            i.putExtra("from", "link");
                            startActivity(i);
                            finish();
                        }
                        else if(params.get(2).matches("2")){
                            Intent i= new Intent(GateWayActivity.this, ReelsActivity.class);
//                            i.putExtra("campus", "Text");
                            i.putExtra("bool", "2");
                            i.putExtra("docID", postID);
//                            i.putExtra("from", "link");
                            startActivity(i);
                            finish();
                        }
                        else if(params.get(2).matches("3")){
                            Intent i= new Intent(GateWayActivity.this, ReelsActivity.class);
//                            i.putExtra("campus", "Text");
                            i.putExtra("bool", "3");
                            i.putExtra("docID", postID);
//                            i.putExtra("from", "link");
                            startActivity(i);
                            finish();
                        }
                    }
                }
                else {
                    startActivity(new Intent(GateWayActivity.this, MainActivity.class));
                    finish();
                }

            }
            else {
                startActivity(new Intent(GateWayActivity.this, MainActivity.class));
                finish();
            }

        }
        else {
            startActivity(new Intent(GateWayActivity.this, LoginActivity.class));
            finish();
        }


    }

}