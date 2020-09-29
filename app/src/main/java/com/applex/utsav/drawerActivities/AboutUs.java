package com.applex.utsav.drawerActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.preferences.IntroPref;

import java.util.Locale;
import java.util.Objects;

public class AboutUs extends AppCompatActivity {
    IntroPref introPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPref = new IntroPref(AboutUs.this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_about_us);
//
//        Toolbar toolbar= findViewById(R.id.toolb);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle("Edit Comment");
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}