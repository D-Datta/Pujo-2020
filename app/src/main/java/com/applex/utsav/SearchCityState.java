package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.applex.utsav.adapters.CitySearchAdapter;
import com.applex.utsav.adapters.StateSearchAdapter;
import com.applex.utsav.models.CitySearchModel;
import com.applex.utsav.models.StateSearchModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.registration.RegIndividual;
import com.applex.utsav.registration.RegPujoCommittee;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class SearchCityState extends AppCompatActivity {

    private ImageButton back1;
    //    private ImageButton searchinstbutton;
    private EditText searchcitystate;
    private Intent i;

    private RecyclerView mRecyclerView;
    private CitySearchAdapter cityadapter;
    private StateSearchAdapter stateadapter;
    private ArrayList<CitySearchModel> citySearchModels= new ArrayList<>();
    private ArrayList<StateSearchModel> stateSearchModels= new ArrayList<>();
    private ProgressBar progress;
    IntroPref introPref;
    private com.google.android.material.floatingactionbutton.FloatingActionButton addcitystate;

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

//        /////////////////DAY OR NIGHT MODE///////////////////
        FirebaseFirestore.getInstance().document("Mode/night_mode")
                .addSnapshotListener(SearchCityState.this, (value, error) -> {
                    if(value != null) {
                        if(value.getBoolean("night_mode")) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        if(value.getBoolean("listener")) {
                            FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                            startActivity(new Intent(SearchCityState.this, SearchCityState.class));
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }
                    } else {
                        FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        startActivity(new Intent(SearchCityState.this, SearchCityState.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                });
//        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_search_city_state);

        i = getIntent();
        back1 = findViewById(R.id.back1);
//        searchinstbutton = findViewById(R.id.searchinstButton);
        searchcitystate = findViewById(R.id.search_citystate);
        mRecyclerView = findViewById(R.id.search_citystate_recycler);
        progress = findViewById(R.id.progress);
        addcitystate = findViewById(R.id.addcitystate);
//        addcitystate.setVisibility(View.GONE);

        back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SearchCityState.super.onBackPressed();
            }
        });

        progress.setVisibility(View.VISIBLE);
        searchcitystate.requestFocus();
        searchcitystate.setFocusableInTouchMode(true);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchcitystate, InputMethodManager.SHOW_FORCED);


        mRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("state_pujo")
                ||i.getStringExtra("from").matches("state_ind")||i.getStringExtra("from").matches("state_pujo_edit")
                ||i.getStringExtra("from").matches("state_ind_edit"))){

            try {
                buildRecyclerViewState();
            } catch (IOException e) {
                e.printStackTrace();
            }

            searchcitystate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(searchcitystate != null){
                        ArrayList<StateSearchModel> searchstate = new ArrayList<>();

                        for(StateSearchModel stateSearchModel:stateSearchModels){
                            if(stateSearchModel.getState().toLowerCase().contains(s.toString().toLowerCase())){
                                searchstate.add(stateSearchModel);
                            }

                        }
//                        if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("state_pujo")
//                                ||i.getStringExtra("from").matches("state_ind")||i.getStringExtra("from").matches("state_pujo_edit")
//                                ||i.getStringExtra("from").matches("state_ind_edit"))){
//                            stateSearchModels.add(new StateSearchModel(getResources().getString(R.string.statehead)));
//                        }

                        stateadapter = new StateSearchAdapter(SearchCityState.this, searchstate);
                        stateadapter.onClickListener(new StateSearchAdapter.OnClickListener() {
                            @Override
                            public void onClickListener(String name) {
                                if(name.contains(getResources().getString(R.string.statehead))){
                                    Dialog myDialogue = new Dialog(SearchCityState.this);
                                    myDialogue.setContentView(R.layout.dialog_add_state);
                                    myDialogue.setCanceledOnTouchOutside(TRUE);
                                    EditText et = myDialogue.findViewById(R.id.addState);

                                    myDialogue.findViewById(R.id.submit).setOnClickListener(v -> {
                                        String state = et.getText().toString();
                                        if(InternetConnection.checkConnection(getApplicationContext())){
                                            if(state.isEmpty()){
                                                BasicUtility.showToast(SearchCityState.this,"Please add your state name");
                                            }
                                            else {
//                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NewState");
//                                                reference.child("uid").setValue(FirebaseAuth.getInstance().getUid())
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                Toast.makeText(getApplicationContext(), "Thank you for the input.", Toast.LENGTH_LONG).show();
////                                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                                            }
//                                                        });
                                                myDialogue.dismiss();
                                                if(i.getStringExtra("from").matches("state_ind")){
                                                    RegIndividual.state_ind.setText(state);
                                                    SearchCityState.super.onBackPressed();
                                                }
                                                else if(i.getStringExtra("from").matches("state_pujo")){
                                                    RegPujoCommittee.etstate.setText(state);
                                                    SearchCityState.super.onBackPressed();
                                                }
                                                else if(i.getStringExtra("from").matches("state_pujo_edit")){
                                                    EditProfileCommitteeActivity.com_state.setText(state);
                                                    SearchCityState.super.onBackPressed();
                                                }
                                                else if(i.getStringExtra("from").matches("state_ind_edit")){
                                                    EditProfileIndividualActivity.state_ind.setText(state);
                                                    SearchCityState.super.onBackPressed();
                                                }
//                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                SearchInstituteActivity.super.onBackPressed();
                                            }

                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(), "Network unavailable...",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    myDialogue.show();
                                    Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                }
                                else{

                                    if(i.getStringExtra("from").matches("state_ind")){
                                        RegIndividual.state_ind.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("state_pujo")){
                                        RegPujoCommittee.etstate.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("state_pujo_edit")){
                                        EditProfileCommitteeActivity.com_state.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("state_ind_edit")){
                                        EditProfileIndividualActivity.state_ind.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                }

                            }
                        });
                        mRecyclerView.setAdapter(stateadapter);
                    }

                }
            });

        }
        else if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("city_ind")
                ||i.getStringExtra("from").matches("city_pujo")||i.getStringExtra("from").matches("city_pujo_edit")
                ||i.getStringExtra("from").matches("city_ind_edit"))){

            try {
                buildRecyclerViewCity();
            } catch (IOException e) {
                e.printStackTrace();
            }

            searchcitystate.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(searchcitystate != null){
                        ArrayList<CitySearchModel> searchcity = new ArrayList<>();

                        for(CitySearchModel citySearchModel:citySearchModels){
                            if(citySearchModel.getCity().toLowerCase().contains(s.toString().toLowerCase())){
                                searchcity.add(citySearchModel);
                            }
                        }
//                        if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("city_ind")
//                                ||i.getStringExtra("from").matches("city_pujo")||i.getStringExtra("from").matches("city_pujo_edit")
//                                ||i.getStringExtra("from").matches("city_ind_edit"))){
//                            citySearchModels.add(new CitySearchModel(getResources().getString(R.string.cityhead)));
//                        }

                        cityadapter = new CitySearchAdapter(SearchCityState.this, searchcity);
                        cityadapter.onClickListener(new CitySearchAdapter.OnClickListener() {
                            @Override
                            public void onClickListener(String name) {
                                if(name.contains(getResources().getString(R.string.cityhead))){
                                    Dialog myDialogue = new Dialog(SearchCityState.this);
                                    myDialogue.setContentView(R.layout.dialog_add_city);
                                    myDialogue.setCanceledOnTouchOutside(TRUE);
                                    EditText et = myDialogue.findViewById(R.id.addCity);

                                    myDialogue.findViewById(R.id.submit).setOnClickListener(v -> {
                                        String city = et.getText().toString();
                                        if(InternetConnection.checkConnection(getApplicationContext())){
                                            if(city.isEmpty()){
                                                BasicUtility.showToast(SearchCityState.this,"Please add your city/district name");
                                            }
                                            else {
//                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NewCity");
//                                                reference.child("uid").setValue(FirebaseAuth.getInstance().getUid())
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                Toast.makeText(getApplicationContext(), "Thank you for the input.", Toast.LENGTH_LONG).show();
////                                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                                            }
//                                                        });
                                                myDialogue.dismiss();
                                                if(i.getStringExtra("from").matches("city_ind")){
                                                    RegIndividual.city_ind.setText(city);
                                                    SearchCityState.super.onBackPressed();
                                                }
                                                else if(i.getStringExtra("from").matches("city_pujo")){
                                                    RegPujoCommittee.etcity.setText(city);
                                                    SearchCityState.super.onBackPressed();
                                                }
                                                else if(i.getStringExtra("from").matches("city_pujo_edit")){
                                                    EditProfileCommitteeActivity.com_city.setText(city);
                                                    SearchCityState.super.onBackPressed();
                                                }
                                                else if(i.getStringExtra("from").matches("city_ind_edit")){
                                                    EditProfileIndividualActivity.city_ind.setText(city);
                                                    SearchCityState.super.onBackPressed();
                                                }
//                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                SearchInstituteActivity.super.onBackPressed();
                                            }

                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(), "Network unavailable...",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    myDialogue.show();
                                    Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                }
                                else{

                                    if(i.getStringExtra("from").matches("city_ind")){
                                        RegIndividual.city_ind.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("city_pujo")){
                                        RegPujoCommittee.etcity.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("city_pujo_edit")){
                                        EditProfileCommitteeActivity.com_city.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("city_ind_edit")){
                                        EditProfileIndividualActivity.city_ind.setText(name);
                                        SearchCityState.super.onBackPressed();
                                    }
                                }

                            }
                        });

                        mRecyclerView.setAdapter(cityadapter);
                    }

                }
            });
        }

        if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("state_pujo")
                ||i.getStringExtra("from").matches("state_ind")||i.getStringExtra("from").matches("state_pujo_edit")
                ||i.getStringExtra("from").matches("state_ind_edit"))){
            addcitystate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog myDialogue = new Dialog(SearchCityState.this);
                    myDialogue.setContentView(R.layout.dialog_add_state);
                    myDialogue.setCanceledOnTouchOutside(TRUE);
                    EditText et = myDialogue.findViewById(R.id.addState);

                    myDialogue.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String state = et.getText().toString();
                            if(InternetConnection.checkConnection(getApplicationContext())){
                                if(state.isEmpty()){
                                    BasicUtility.showToast(SearchCityState.this, "Please enter your state name");

                                }
                                else {
//                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NewState");
//                                    reference.child("uid").setValue(FirebaseAuth.getInstance().getUid())
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    Toast.makeText(getApplicationContext(), "Thank you for the input.", Toast.LENGTH_LONG).show();
////                                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                                }
//                                            });
                                    myDialogue.dismiss();
                                    if(i.getStringExtra("from").matches("state_ind")){
                                        RegIndividual.state_ind.setText(state);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("state_pujo")){
                                        RegPujoCommittee.etstate.setText(state);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("state_pujo_edit")){
                                        EditProfileCommitteeActivity.com_state.setText(state);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("state_ind_edit")){
                                        EditProfileIndividualActivity.state_ind.setText(state);
                                        SearchCityState.super.onBackPressed();
                                    }
//                                    RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                    SearchInstituteActivity.super.onBackPressed();
                                }

                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Network unavailable...",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    myDialogue.show();
                    Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            });
        }
        else if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("city_ind")
                ||i.getStringExtra("from").matches("city_pujo")||i.getStringExtra("from").matches("city_pujo_edit")
                ||i.getStringExtra("from").matches("city_ind_edit"))){
            addcitystate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog myDialogue = new Dialog(SearchCityState.this);
                    myDialogue.setContentView(R.layout.dialog_add_city);
                    myDialogue.setCanceledOnTouchOutside(TRUE);
                    EditText et = myDialogue.findViewById(R.id.addCity);

                    myDialogue.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String city = et.getText().toString();
                            if(InternetConnection.checkConnection(getApplicationContext())){
                                if(city.isEmpty()){
                                    BasicUtility.showToast(SearchCityState.this, "Please enter your city/district name");

                                }
                                else {
//                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NewCity");
//                                    reference.child("uid").setValue(FirebaseAuth.getInstance().getUid())
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    Toast.makeText(getApplicationContext(), "Thank you for the input.", Toast.LENGTH_LONG).show();
////                                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                                }
//                                            });
                                    myDialogue.dismiss();
                                    if(i.getStringExtra("from").matches("city_ind")){
                                        RegIndividual.city_ind.setText(city);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("city_pujo")){
                                        RegPujoCommittee.etcity.setText(city);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("city_pujo_edit")){
                                        EditProfileCommitteeActivity.com_city.setText(city);
                                        SearchCityState.super.onBackPressed();
                                    }
                                    else if(i.getStringExtra("from").matches("city_ind_edit")){
                                        EditProfileIndividualActivity.city_ind.setText(city);
                                        SearchCityState.super.onBackPressed();
                                    }
//                                    RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                    SearchInstituteActivity.super.onBackPressed();
                                }

                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Network unavailable...",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    myDialogue.show();
                    Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            });
        }


    }

    private void buildRecyclerViewCity() throws IOException {

        // InstitutesArray institutesArray= new InstitutesArray();
        //  ArrayList<InstituteSearchModel> inst= new ArrayList<>();
        InputStreamReader is = new InputStreamReader(getAssets()
                .open("cityList.csv"));

        BufferedReader reader = new BufferedReader(is);
        reader.readLine();
        String line;
        while ((line = reader.readLine()) != null) {
            CitySearchModel citySearchModel= new CitySearchModel();
            citySearchModel.setCity(line);
            citySearchModels.add(citySearchModel);
        }

//        if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("city_ind")
//                ||i.getStringExtra("from").matches("city_pujo")||i.getStringExtra("from").matches("city_pujo_edit")
//                ||i.getStringExtra("from").matches("city_ind_edit"))){
//            citySearchModels.add(new CitySearchModel(getResources().getString(R.string.cityhead)));
//        }

        cityadapter = new CitySearchAdapter(SearchCityState.this, citySearchModels);
        cityadapter.onClickListener(new CitySearchAdapter.OnClickListener() {
            @Override
            public void onClickListener(String name) {

                if(name.contains(getResources().getString(R.string.cityhead))){
                    Dialog myDialogue = new Dialog(SearchCityState.this);
                    myDialogue.setContentView(R.layout.dialog_add_city);
                    myDialogue.setCanceledOnTouchOutside(TRUE);
                    EditText et = myDialogue.findViewById(R.id.addCity);

                    myDialogue.findViewById(R.id.submit).setOnClickListener(v -> {
                        String city = et.getText().toString();
                        if(InternetConnection.checkConnection(getApplicationContext())){
                            if(city.isEmpty()){
                                BasicUtility.showToast(SearchCityState.this,"Please add your city/district name");
                            }
                            else {
//                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NewCity");
//                                reference.child("uid").setValue(FirebaseAuth.getInstance().getUid())
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                Toast.makeText(getApplicationContext(), "Thank you for the input.", Toast.LENGTH_LONG).show();
////                                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                            }
//                                        });
                                myDialogue.dismiss();
                                if(i.getStringExtra("from").matches("city_ind")){
                                    RegIndividual.city_ind.setText(city);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("city_pujo")){
                                    RegPujoCommittee.etcity.setText(city);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("city_pujo_edit")){
                                    EditProfileCommitteeActivity.com_city.setText(city);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("city_ind_edit")){
                                    EditProfileIndividualActivity.city_ind.setText(city);
                                    SearchCityState.super.onBackPressed();
                                }
//                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                SearchInstituteActivity.super.onBackPressed();
                            }

                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Network unavailable...",Toast.LENGTH_LONG).show();
                        }
                    });
                    myDialogue.show();
                    Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                else{

                    if(i.getStringExtra("from").matches("city_ind")){
                        RegIndividual.city_ind.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                    else if(i.getStringExtra("from").matches("city_pujo")){
                        RegPujoCommittee.etcity.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                    else if(i.getStringExtra("from").matches("city_pujo_edit")){
                        EditProfileCommitteeActivity.com_city.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                    else if(i.getStringExtra("from").matches("city_ind_edit")){
                        EditProfileIndividualActivity.city_ind.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                }
            }
        });

        mRecyclerView.setAdapter(cityadapter);
        progress.setVisibility(View.GONE);

    }

    private void buildRecyclerViewState() throws IOException {

        // InstitutesArray institutesArray= new InstitutesArray();
        //  ArrayList<InstituteSearchModel> inst= new ArrayList<>();
        InputStreamReader is = new InputStreamReader(getAssets()
                .open("stateList.csv"));

        BufferedReader reader = new BufferedReader(is);
        reader.readLine();
        String line;
        while ((line = reader.readLine()) != null) {
            StateSearchModel stateSearchModel= new StateSearchModel();
            stateSearchModel.setState(line);
            stateSearchModels.add(stateSearchModel);
        }
//        if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("state_pujo")
//                ||i.getStringExtra("from").matches("state_ind")||i.getStringExtra("from").matches("state_pujo_edit")
//                ||i.getStringExtra("from").matches("state_ind_edit"))){
//            stateSearchModels.add(new StateSearchModel(getResources().getString(R.string.statehead)));
//        }

        stateadapter = new StateSearchAdapter(SearchCityState.this, stateSearchModels);
        stateadapter.onClickListener(new StateSearchAdapter.OnClickListener() {
            @Override
            public void onClickListener(String name) {

                if(name.contains(getResources().getString(R.string.statehead))){
                    Dialog myDialogue = new Dialog(SearchCityState.this);
                    myDialogue.setContentView(R.layout.dialog_add_state);
                    myDialogue.setCanceledOnTouchOutside(TRUE);
                    EditText et = myDialogue.findViewById(R.id.addState);

                    myDialogue.findViewById(R.id.submit).setOnClickListener(v -> {
                        String state = et.getText().toString();
                        if(InternetConnection.checkConnection(getApplicationContext())){
                            if(state.isEmpty()){
                                BasicUtility.showToast(SearchCityState.this,"Please add your state name");
                            }
                            else {
//                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("NewState");
//                                reference.child("uid").setValue(FirebaseAuth.getInstance().getUid())
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                Toast.makeText(getApplicationContext(), "Thank you for the input.", Toast.LENGTH_LONG).show();
////                                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                            }
//                                        });
                                myDialogue.dismiss();
                                if(i.getStringExtra("from").matches("state_ind")){
                                    RegIndividual.state_ind.setText(state);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("state_pujo")){
                                    RegPujoCommittee.etstate.setText(state);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("state_pujo_edit")){
                                    EditProfileCommitteeActivity.com_state.setText(state);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("state_ind_edit")){
                                    EditProfileIndividualActivity.state_ind.setText(state);
                                    SearchCityState.super.onBackPressed();
                                }
//                                RegistrationFormPost.etinstitute.setText("NEW : {"+college+"}");
//                                SearchInstituteActivity.super.onBackPressed();
                            }

                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Network unavailable...",Toast.LENGTH_LONG).show();
                        }
                    });
                    myDialogue.show();
                    Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
                else{

                    if(i.getStringExtra("from").matches("state_ind")){
                        RegIndividual.state_ind.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                    else if(i.getStringExtra("from").matches("state_pujo")){
                        RegPujoCommittee.etstate.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                    else if(i.getStringExtra("from").matches("state_pujo_edit")){
                        EditProfileCommitteeActivity.com_state.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                    else if(i.getStringExtra("from").matches("state_ind_edit")){
                        EditProfileIndividualActivity.state_ind.setText(name);
                        SearchCityState.super.onBackPressed();
                    }
                }

            }
        });

        mRecyclerView.setAdapter(stateadapter);
        progress.setVisibility(View.GONE);

    }

}