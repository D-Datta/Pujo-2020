package com.applex.utsav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.registration.LoginActivity;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.BasicUtility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Splash extends AppCompatActivity {

    private IntroPref introPref;
    private static final long Splash_time_out = 1500;
    public FirebaseUser fireuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPref = new IntroPref(this);

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

        setContentView(R.layout.activity_splash);
        ProgressBar progress = findViewById(R.id.progressbar);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        fireuser = mAuth.getCurrentUser();

        if (introPref.isFirstTimeLaunch()) {
            new Handler().postDelayed(() -> {
                introPref.setIsFirstTimeLaunch(false);
                startActivity(new Intent(Splash.this, LanguageChoice.class));
                finish();
            },Splash_time_out);
        }
        else {
            if(InternetConnection.checkConnection(getApplicationContext())) {
                if (fireuser != null) {
                    progress.setVisibility(View.VISIBLE);
                    FirebaseFirestore.getInstance().document("Users/"+FirebaseAuth.getInstance().getUid()+"/")
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if(documentSnapshot.exists()){
                                    new Handler().postDelayed(() -> {
                                        IntroPref introPref= new IntroPref(Splash.this);
                                        BaseUserModel userModel = documentSnapshot.toObject(BaseUserModel.class);
                                        FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid()).update("listener", false);

                                        introPref.setUserdp(userModel.getDp());
                                        introPref.setType(userModel.getType());
                                        introPref.setFullName(userModel.getName());
                                        introPref.setGender(userModel.getGender());

                                        Intent homeIntent = new Intent(Splash.this, MainActivity.class);
                                        homeIntent.putExtra("value", "splash");
                                        homeIntent.putExtra("type",userModel.getType());
                                        homeIntent.putExtra("email", fireuser.getEmail());
                                        startActivity(homeIntent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, Splash_time_out);
                                }
                                else {
                                    new Handler().postDelayed(() -> {
                                        Intent homeIntent = new Intent(Splash.this, LoginActivity.class);
                                        homeIntent.putExtra("value", "splash");
                                        homeIntent.putExtra("email", fireuser.getEmail());
                                        startActivity(homeIntent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, Splash_time_out);
                                }
                            })
                            .addOnFailureListener(e -> new Handler().postDelayed(() -> {
                                Intent homeIntent = new Intent(Splash.this, LoginActivity.class);
                                homeIntent.putExtra("value", "splash");
                                homeIntent.putExtra("email", fireuser.getEmail());
                                startActivity(homeIntent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }, Splash_time_out));
                }
                else {
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(Splash.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }, Splash_time_out);
                }
            }
            else {
                BasicUtility.showToast(getApplicationContext(), "Network unavailable...");
            }
        }
    }
}