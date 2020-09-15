package com.example.pujo360.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pujo360.R;

public class RegChoice extends AppCompatActivity {

    private CardView cardCommittee, cardIndividual;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_choice);

        cardCommittee = findViewById(R.id.card_committee);
        cardIndividual = findViewById(R.id.card_individual);

        Intent i = getIntent();

        cardCommittee.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegChoice.this , RegPujoCommittee.class);
                intent.putExtra(i.getStringExtra("password") , "password");
                startActivity(intent);

            }
        });

        cardIndividual.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegChoice.this , RegIndividual.class);
                intent.putExtra(i.getStringExtra("password") , "password");
                startActivity(intent);
            }
        });


    }
}