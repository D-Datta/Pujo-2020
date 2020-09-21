package com.example.pujo360;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.pujo360.adapters.CitySearchAdapter;
import com.example.pujo360.adapters.StateSearchAdapter;
import com.example.pujo360.models.CitySearchModel;
import com.example.pujo360.models.StateSearchModel;
import com.example.pujo360.registration.RegIndividual;
import com.example.pujo360.registration.RegPujoCommittee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city_state);

        i = getIntent();


        back1 = findViewById(R.id.back1);
//        searchinstbutton = findViewById(R.id.searchinstButton);
        searchcitystate = findViewById(R.id.search_citystate);
        mRecyclerView = findViewById(R.id.search_citystate_recycler);
        progress = findViewById(R.id.progress);

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
                        stateadapter = new StateSearchAdapter(SearchCityState.this, searchstate);
                        stateadapter.onClickListener(new StateSearchAdapter.OnClickListener() {
                            @Override
                            public void onClickListener(String name) {
                                if(i.getStringExtra("from").matches("state_ind")){
                                    RegIndividual.state_ind.setText(name);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("state_pujo")){
                                    RegPujoCommittee.etstate.setText(name);
                                    SearchCityState.super.onBackPressed();
                                }
//                                else if(i.getStringExtra("from").matches("state_pujo_edit")){
//                                    EditProfileIndividualActivity.state_ind.setText(name);
//                                    SearchCityState.super.onBackPressed();
//                                }
                                else if(i.getStringExtra("from").matches("state_ind_edit")){
                                    EditProfileIndividualActivity.state_ind.setText(name);
                                    SearchCityState.super.onBackPressed();
                                }

                            }
                        });
                        mRecyclerView.setAdapter(stateadapter);
                    }

                }
            });

        }
        else if(i.getStringExtra("from")!=null && (i.getStringExtra("from").matches("city_ind")
                ||i.getStringExtra("from").matches("city_pujo"))){

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
                        cityadapter = new CitySearchAdapter(SearchCityState.this, searchcity);
                        cityadapter.onClickListener(new CitySearchAdapter.OnClickListener() {
                            @Override
                            public void onClickListener(String name) {
                                if(i.getStringExtra("from").matches("city_ind")){
                                    RegIndividual.city_ind.setText(name);
                                    SearchCityState.super.onBackPressed();
                                }
                                else if(i.getStringExtra("from").matches("city_pujo")){
                                    RegPujoCommittee.etcity.setText(name);
                                    SearchCityState.super.onBackPressed();
                                }
//                                else if(i.getStringExtra("from").matches("city_pujo_edit")){
//                                    EditProfileIndividualActivity.city_ind.setText(name);
//                                    SearchCityState.super.onBackPressed();
//                                }
                                else if(i.getStringExtra("from").matches("city_ind_edit")){
                                    EditProfileIndividualActivity.city_ind.setText(name);
                                    SearchCityState.super.onBackPressed();
                                }

                            }
                        });

                        mRecyclerView.setAdapter(cityadapter);
                    }

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

        cityadapter = new CitySearchAdapter(SearchCityState.this, citySearchModels);
        cityadapter.onClickListener(new CitySearchAdapter.OnClickListener() {
            @Override
            public void onClickListener(String name) {
                if(i.getStringExtra("from").matches("city_ind")){
                    RegIndividual.city_ind.setText(name);
                    SearchCityState.super.onBackPressed();
                }
                else if(i.getStringExtra("from").matches("city_pujo")){
                    RegPujoCommittee.etcity.setText(name);
                    SearchCityState.super.onBackPressed();
                }
//                else if(i.getStringExtra("from").matches("city_pujo_edit")){
//                    EditProfileIndividualActivity.city_ind.setText(name);
//                    SearchCityState.super.onBackPressed();
//                }
                else if(i.getStringExtra("from").matches("city_ind_edit")){
                    EditProfileIndividualActivity.city_ind.setText(name);
                    SearchCityState.super.onBackPressed();
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

        stateadapter = new StateSearchAdapter(SearchCityState.this, stateSearchModels);
        stateadapter.onClickListener(new StateSearchAdapter.OnClickListener() {
            @Override
            public void onClickListener(String name) {
                if(i.getStringExtra("from").matches("state_ind")){
                    RegIndividual.state_ind.setText(name);
                    SearchCityState.super.onBackPressed();
                }
                else if(i.getStringExtra("from").matches("state_pujo")){
                    RegPujoCommittee.etstate.setText(name);
                    SearchCityState.super.onBackPressed();
                }
//                else if(i.getStringExtra("from").matches("state_pujo_edit")){
//                    EditProfileIndividualActivity.state_ind.setText(name);
//                    SearchCityState.super.onBackPressed();
//                }
                else if(i.getStringExtra("from").matches("state_ind_edit")){
                    EditProfileIndividualActivity.state_ind.setText(name);
                    SearchCityState.super.onBackPressed();
                }
            }
        });

        mRecyclerView.setAdapter(stateadapter);
        progress.setVisibility(View.GONE);

    }

}