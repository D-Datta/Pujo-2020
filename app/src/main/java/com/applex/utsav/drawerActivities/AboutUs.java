package com.applex.utsav.drawerActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.preferences.IntroPref;

import java.util.Locale;
import java.util.Objects;

public class AboutUs extends AppCompatActivity {
    IntroPref introPref;

    private TextView tsnaplingo, tcampus24, tjee, tinnovacion;

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

        tsnaplingo = findViewById(R.id.link_snaplingo);
        tcampus24 = findViewById(R.id.link_campus24);
        tjee = findViewById(R.id.link_jeeab360);
        tinnovacion = findViewById(R.id.link_innovacion);

        tsnaplingo.setMovementMethod(LinkMovementMethod.getInstance());
        tcampus24.setMovementMethod(LinkMovementMethod.getInstance());
        tjee.setMovementMethod(LinkMovementMethod.getInstance());
        tinnovacion.setMovementMethod(LinkMovementMethod.getInstance());
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