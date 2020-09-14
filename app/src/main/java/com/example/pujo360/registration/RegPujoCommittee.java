package com.example.pujo360.registration;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.example.pujo360.R;

public class RegPujoCommittee extends AppCompatActivity {

    private EditText etcommitteename, etdescription, etaddressline, etcity;
    private String scommitteename, sdescription, saddress, scity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_pujo_committee);





    }
}