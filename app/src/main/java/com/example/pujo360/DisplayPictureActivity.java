package com.example.pujo360;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class DisplayPictureActivity extends AppCompatActivity {

    PhotoView img;
    private Bitmap mSelectedBitmap;
    private byte[] getByteArray;

    ImageView back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_picture);

        img = findViewById(R.id.imageView);
        back = findViewById(R.id.back);

        if(getIntent().getStringExtra("from") != null){
            String pic = getIntent().getStringExtra("Bitmap");
            Picasso.get().load(pic).into(img);

//            Picasso.get().load(pic).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    download.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            save_Dialog(bitmap);
//                        }
//                    });
//                }
//
//                @Override
//                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                    Toast.makeText(ProfilePictureActivity.this, "No Profile Picture", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            });

        }
        else {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2;
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];

            getByteArray = getIntent().getByteArrayExtra("Bitmap");
            mSelectedBitmap = BitmapFactory.decodeByteArray(getByteArray, 0, getByteArray.length, options);
            img.setImageBitmap(mSelectedBitmap);

//            download.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    save_Dialog(mSelectedBitmap);
//                }
//            });
        }



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayPictureActivity.super.onBackPressed();
            }
        });

    }



}
