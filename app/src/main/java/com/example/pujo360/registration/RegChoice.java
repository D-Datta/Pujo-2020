package com.example.pujo360.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.example.pujo360.R;
import com.example.pujo360.preferences.IntroPref;

public class RegChoice extends AppCompatActivity {

    private CardView cardCommittee, cardIndividual;
    String email,password;
    private IntroPref introPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_choice);

        ///////////////Set Image Bitmap/////////////////////
        ImageView imageView = findViewById(R.id.ma_durga);

        Display display = getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.dhaki_png, options);
        int width = options.outWidth;
        if (width > displayWidth) {
            int widthRatio = Math.round((float) width / (float) displayWidth);
            options.inSampleSize = widthRatio;
        }
        options.inJustDecodeBounds = false;
        Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dhaki_png, options);
        imageView.setImageBitmap(scaledBitmap);
        ///////////////Set Image Bitmap/////////////////////

        introPref = new IntroPref(RegChoice.this);

        cardCommittee = findViewById(R.id.card_committee);
        cardIndividual = findViewById(R.id.card_individual);
        introPref = new IntroPref(RegChoice.this);

        Intent i = getIntent();
        if(getIntent().getStringExtra("value")!=null){
            if(getIntent().getStringExtra("value").matches("emailpass")){
                email = getIntent().getStringExtra("email");
                password = getIntent().getStringExtra("password");
            }
            else if(getIntent().getStringExtra("value").matches("google")){
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