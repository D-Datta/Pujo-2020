package com.applex.utsav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.applex.utsav.preferences.IntroPref;

public class LanguageChoice extends AppCompatActivity {

    private Button english, bangla;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_choice);
        bangla = findViewById(R.id.button_bangla);
        english = findViewById(R.id.button_english);

        IntroPref introPref = new IntroPref(LanguageChoice.this);

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introPref.setLanguage("en");
                Intent intent = new Intent(LanguageChoice.this, WalkthroughActivity.class);
                startActivity(intent);
            }
        });

        bangla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                introPref.setLanguage("bn");
                Intent intent = new Intent(LanguageChoice.this, WalkthroughActivity.class);
                startActivity(intent);
            }
        });


    }
}