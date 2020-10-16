package com.applex.utsav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.applex.utsav.preferences.IntroPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LanguageChoice extends AppCompatActivity {

    private Button english, bangla;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntroPref introPref = new IntroPref(LanguageChoice.this);

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

        setContentView(R.layout.activity_language_choice);
        bangla = findViewById(R.id.button_bangla);
        english = findViewById(R.id.button_english);

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(LanguageChoice.this, (value, error) -> {
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
                                    startActivity(new Intent(LanguageChoice.this, LanguageChoice.class));
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                }, 200);
                            });
                        }
                    });
        }

        ///////////////Set Image Bitmap/////////////////////
        ImageView imageView = findViewById(R.id.dhaki_png);

        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma2, options);
        int width = options.outWidth;
        if (width > displayWidth) {
            int widthRatio = Math.round((float) width / (float) displayWidth);
            options.inSampleSize = widthRatio;
        }
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.decorative_item, options);
        imageView.setImageBitmap(scaledBitmap);
        ///////////////Set Image Bitmap/////////////////////

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introPref.setLanguage("en");
                Intent intent = new Intent(LanguageChoice.this, WalkthroughActivity.class);
                startActivity(intent);
                finish();
            }
        });

        bangla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introPref.setLanguage("bn");
                Intent intent = new Intent(LanguageChoice.this, WalkthroughActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}