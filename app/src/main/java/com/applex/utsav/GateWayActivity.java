package com.applex.utsav;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.applex.utsav.registration.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class GateWayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_way_activty);
        String campus;
        String postID;

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Uri uri = getIntent().getData();
            if(uri!=null) {
                List<String> params = uri.getPathSegments();
                if(params.size()>3){
                    campus = params.get(2).replaceAll("_", " ");
                    postID = params.get(3);
                    if(params.get(1).matches("Home")) {
                        Intent i= new Intent(GateWayActivity.this, ViewMoreHome.class);
                        i.putExtra("campus", campus);
                        i.putExtra("postID", postID);
                        i.putExtra("from", "link");
                        startActivity(i);
                        finish();
                    }
                    else {
                        startActivity(new Intent(GateWayActivity.this, MainActivity.class));
                        finish();
                    }
                }
                else {
                    startActivity(new Intent(GateWayActivity.this, MainActivity.class));
                    finish();
                }

            }
            else {
                startActivity(new Intent(GateWayActivity.this, MainActivity.class));
                finish();
            }

        }
        else {
            startActivity(new Intent(GateWayActivity.this, LoginActivity.class));
            finish();
        }


    }
}