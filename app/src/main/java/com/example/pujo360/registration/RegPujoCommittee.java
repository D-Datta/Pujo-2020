package com.example.pujo360.registration;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.example.pujo360.R;

public class RegPujoCommittee extends AppCompatActivity {

    private EditText etcommitteename, etdescription, etaddressline, etcity, ettype;
    private String scommitteename, sdescription, saddress, scity, stype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_pujo_committee);

        etaddressline = findViewById(R.id.committee_addressline);
        etcity = findViewById(R.id.committee_city);
        etcommitteename = findViewById(R.id.committee_name);
        etdescription = findViewById(R.id.committee_description);
        ettype = findViewById(R.id.committee_type);

        saddress = etaddressline.getText().toString().trim();
        scity = etcity.getText().toString().trim();
        scommitteename = etcommitteename.getText().toString().trim();
        sdescription = etdescription.getText().toString().trim();
        stype = ettype.getText().toString().trim();






    }
}