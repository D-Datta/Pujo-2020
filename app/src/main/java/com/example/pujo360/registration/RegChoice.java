package com.example.pujo360.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pujo360.R;

public class RegChoice extends AppCompatActivity {

    private CardView cardCommittee, cardIndividual;
    String email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_choice);

        cardCommittee = findViewById(R.id.card_committee);
        cardIndividual = findViewById(R.id.card_individual);

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
                startActivity(new Intent(RegChoice.this , RegPujoCommittee.class)
                .putExtra("email",email));
                finish();

            }
        });

        cardIndividual.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegChoice.this , RegIndividual.class)
                .putExtra("email",email));
                finish();
            }
        });


    }
}