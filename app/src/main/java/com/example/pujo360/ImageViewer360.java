package com.example.pujo360;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import co.gofynd.gravityview.GravityView;

public class ImageViewer360 extends AppCompatActivity {
    private ImageView img;
    private GravityView gravityView;
    private boolean esSoportado= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer360);

        init();

        if(esSoportado){
            this.gravityView.setImage(img, R.drawable.panorama).center();
        }else{
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.panorama);
            img.setImageBitmap(bitmap);
        }
    }

    private void init(){
        this.img = findViewById(R.id.imageView);
        this.gravityView = GravityView.getInstance(getBaseContext());
        this.esSoportado =gravityView.deviceSupported();

    }

    @Override
    protected void onResume() {
        super.onResume();
        gravityView.registerListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gravityView.unRegisterListener();
    }

}