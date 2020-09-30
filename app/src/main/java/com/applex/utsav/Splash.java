package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.registration.LoginActivity;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.BasicUtility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Splash extends AppCompatActivity {

    private IntroPref introPref;
    private static final long Splash_time_out = 1500;
    public FirebaseUser fireuser;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        introPref = new IntroPref(this);

        progress  =findViewById(R.id.progressbar);
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
            if(InternetConnection.checkConnection(getApplicationContext())){
                if (fireuser != null) {
                    progress.setVisibility(View.VISIBLE);
//                    if (fireuser.isEmailVerified()) {
                        FirebaseFirestore.getInstance().document("Users/"+FirebaseAuth.getInstance().getUid()+"/")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if(documentSnapshot.exists()){
                                            new Handler().postDelayed(() -> {
                                                IntroPref introPref= new IntroPref(Splash.this);
                                                BaseUserModel userModel = documentSnapshot.toObject(BaseUserModel.class);

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
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        new Handler().postDelayed(() -> {
                                            Intent homeIntent = new Intent(Splash.this, LoginActivity.class);
                                            homeIntent.putExtra("value", "splash");
                                            homeIntent.putExtra("email", fireuser.getEmail());
                                            startActivity(homeIntent);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        }, Splash_time_out);
                                    }
                                });
//                    }
//                    else {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                //            progress.setVisibility(View.GONE);
//
//                                Intent intent = new Intent(Splash.this, LoginActivity.class);
//                                Splash.this.startActivity(intent);
//                                Splash.this.finish();
//                            }
//                        }, Splash_time_out);
//                    }
                }
                else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Splash.this, LoginActivity.class);
                            Splash.this.startActivity(intent);
                            Splash.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            Splash.this.finish();
                        }
                    }, Splash_time_out);
                }
            }
            else {
                BasicUtility.showToast(getApplicationContext(), "Network unavailable...");

            }
        }


    }
}