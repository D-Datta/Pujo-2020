package com.example.pujo360;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import co.gofynd.gravityview.GravityView;

public class MainActivity extends AppCompatActivity {

    private Button ImageView360;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView360 = findViewById(R.id.Viewer);
        ImageView360.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ImageViewer360.class);
                startActivity(i);
            }
        });

    }


}