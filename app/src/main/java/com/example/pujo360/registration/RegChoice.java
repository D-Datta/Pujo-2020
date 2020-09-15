package com.example.pujo360.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

        introPref = new IntroPref(RegChoice.this);

        cardCommittee = findViewById(R.id.card_committee);
        cardIndividual = findViewById(R.id.card_individual);

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
                introPref.setType("Committee");
                intent.putExtra("email",email);
                startActivity(intent);
                finish();

            }
        });

        cardIndividual.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegChoice.this , RegIndividual.class);
                intent.putExtra("password",password);
                introPref.setType("Individual");
                intent.putExtra("email",email);
                startActivity(intent);
                finish();
            }
        });


    }
}