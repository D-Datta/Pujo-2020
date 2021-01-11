package com.applex.utsav;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Locale;
import java.util.Objects;

public class ProfilePictureActivity extends AppCompatActivity {

    PhotoView img;
    private Bitmap mSelectedBitmap;
    private byte[] getByteArray;

    ImageView back, download;
    IntroPref introPref;
    ReadMoreTextView caption;

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
        setContentView(R.layout.activity_profile_picture);

        img = findViewById(R.id.imageView);
        back = findViewById(R.id.back);
        download = findViewById(R.id.download);
        caption = findViewById(R.id.caption);

        if(getIntent().getStringExtra("from") != null){

            if(getIntent().getStringExtra("caption")!=null && !getIntent().getStringExtra("caption").isEmpty()){
                caption.setVisibility(View.VISIBLE);
                String CAPTION = getIntent().getStringExtra("caption");
                caption.setText(CAPTION);
            }
            else{
                caption.setVisibility(View.GONE);
            }
            String pic = getIntent().getStringExtra("Bitmap");
            Picasso.get().load(pic).into(img);

            Picasso.get().load(pic).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            save_Dialog(bitmap);
                        }
                    });
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Toast.makeText(ProfilePictureActivity.this, "No Profile Picture", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });

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

            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save_Dialog(mSelectedBitmap);
                }
            });
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfilePictureActivity.super.onBackPressed();
            }
        });
    }
    private void save_Dialog(Bitmap bitmap) {

        if(!BasicUtility.checkStoragePermission(ProfilePictureActivity.this)){
            BasicUtility.requestStoragePermission(ProfilePictureActivity.this);
        }
        else {
            boolean bool = BasicUtility.saveImage(bitmap, ProfilePictureActivity.this);
            if(bool){
                Toast.makeText(ProfilePictureActivity.this, "Saved to device", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(ProfilePictureActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        }
    }


}