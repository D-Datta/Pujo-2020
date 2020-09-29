package com.applex.utsav.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.preferences.IntroPref;

import java.util.Locale;

public class RegChoice extends AppCompatActivity {

    private CardView cardCommittee, cardIndividual;
    String email, password;
    private IntroPref introPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        introPref = new IntroPref(RegChoice.this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_reg_choice);

        ///////////////Set Image Bitmap/////////////////////
        ImageView imageView = findViewById(R.id.ma_durga);

        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.dui_dhaki, options);
        int width = options.outWidth;
        if (width > displayWidth) {
            int widthRatio = Math.round((float) width / (float) displayWidth);
            options.inSampleSize = widthRatio;
        }
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dui_dhaki, options);
        imageView.setImageBitmap(scaledBitmap);
        ///////////////Set Image Bitmap/////////////////////

        introPref = new IntroPref(RegChoice.this);

        cardCommittee = findViewById(R.id.card_committee);
        cardIndividual = findViewById(R.id.card_individual);
        introPref = new IntroPref(RegChoice.this);

        if(getIntent().getStringExtra("value")!=null){
            if(getIntent().getStringExtra("value").matches("emailpass")){
                email = getIntent().getStringExtra("email");
                password = getIntent().getStringExtra("password");
            }
            else if(getIntent().getStringExtra("value").matches("google")){
                email = getIntent().getStringExtra("email");
            }
            else if(getIntent().getStringExtra("value").matches("phone")){
                email = getIntent().getStringExtra("email");
            }
        }


        cardCommittee.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegChoice.this , RegPujoCommittee.class);
                intent.putExtra( "password",password);
                introPref.setType("com");
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });

        cardIndividual.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegChoice.this , RegIndividual.class);
                intent.putExtra("password",password);
                introPref.setType("indi");
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });


    }
}