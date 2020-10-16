package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.PujoCommitteeModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.StoreTemp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class EditProfileCommitteeActivity extends AppCompatActivity {

    private EditText com_name, com_desc, com_type, com_address, com_pin, com_contact, com_upi;
    public static EditText com_state, com_city;
    private Button submit;

    private long likeCount;
    private long commentcount;
    private long pujoVisits;
    private Timestamp lastVisitTs;

    private String COMNAME,DESCRIPTION,PUJOTYPE,EMAIL,ADDRESS,CITY,STATE,PIN,PROFILEPIC,COVERPIC,uid, CONTACT, UPIID;
    private String tokenStr;
    private PujoCommitteeModel pujoCommitteeModel;
    private DocumentReference docrefBase, docrefCommittee;
    private FirebaseAuth mAuth;
    private FirebaseUser fireuser;
    private ProgressDialog progressDialog;
    private IntroPref introPref;

    private BaseUserModel baseUserModel;
    int verified;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

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

        /////////////////DAY OR NIGHT MODE///////////////////
        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Mode/night_mode")
                    .addSnapshotListener(EditProfileCommitteeActivity.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("night_mode")) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                            if(value.getBoolean("listener")) {
                                ActivityProfileCommittee.mode_changed = 1;
                                FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                                startActivity(new Intent(EditProfileCommitteeActivity.this, EditProfileCommitteeActivity.class));
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        } else {
                            ActivityProfileCommittee.mode_changed = 1;
                            FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            startActivity(new Intent(EditProfileCommitteeActivity.this, EditProfileCommitteeActivity.class));
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }
                    });
        } else if(introPref.getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(introPref.getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_edit_profile_committee);

        Toolbar toolbar = findViewById(R.id.toolbar_edit_com);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        com_name = findViewById(R.id.edit_committee_name);
        com_desc = findViewById(R.id.edit_committee_description);
        com_type = findViewById(R.id.edit_committee_type);
        com_address = findViewById(R.id.edit_committee_addressline);
        com_state = findViewById(R.id.edit_committee_state);
        com_city = findViewById(R.id.edit_committee_city);
        submit = findViewById(R.id.edit_com_profile);
        com_pin = findViewById(R.id.edit_committee_pin);
        com_contact = findViewById(R.id.edit_committee_contact_number);
        radioGroup = findViewById(R.id.radiogroup);
        com_upi = findViewById(R.id.edit_committee_upiid);

        mAuth=FirebaseAuth.getInstance();
        fireuser= mAuth.getCurrentUser();
        introPref= new IntroPref(EditProfileCommitteeActivity.this);
        uid = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            baseUserModel = task.getResult().toObject(BaseUserModel.class);
                            if(task.getResult().getBoolean("verified")!=null){
                                if(baseUserModel.isVerified()){
                                    verified = 1;
                                }
                                else{
                                    verified = 0;
                                }
                            }
                            else{
                                verified = 0;
                            }

                            PROFILEPIC = baseUserModel.getDp();
                            COVERPIC = baseUserModel.getCoverpic();
                            EMAIL = baseUserModel.getEmail();
                            commentcount = baseUserModel.getCommentcount();
                            likeCount = baseUserModel.getLikeCount();
                            pujoVisits = baseUserModel.getPujoVisits();
                            lastVisitTs = baseUserModel.getLastVisitTs();
                            if(baseUserModel.getAddressline()!=null && !baseUserModel.getAddressline().isEmpty())
                            {
                                com_address.setText(baseUserModel.getAddressline());
                            }
                            if(baseUserModel.getCity()!=null && !baseUserModel.getCity().isEmpty())
                            {
                                com_city.setText(baseUserModel.getCity());
                            }
                            if(baseUserModel.getPin()!=null && !baseUserModel.getPin().isEmpty())
                            {
                                com_pin.setText(baseUserModel.getPin());
                            }
                            if(baseUserModel.getName()!=null && !baseUserModel.getName().isEmpty())
                            {
                                com_name.setText(baseUserModel.getName());
                            }
                            if(baseUserModel.getState()!=null && !baseUserModel.getState().isEmpty())
                            {
                                com_state.setText(baseUserModel.getState());
                            }
                            if(baseUserModel.getContact()!=null && !baseUserModel.getContact().isEmpty())
                            {
                                com_contact.setText(baseUserModel.getContact());
                            }

                        }

                    }
                });

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .collection("com")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            PujoCommitteeModel pujoCommitteeModel = task.getResult().toObject(PujoCommitteeModel.class);
                            if(pujoCommitteeModel.getDescription()!=null && !pujoCommitteeModel.getDescription().isEmpty()) {
                                com_desc.setText(pujoCommitteeModel.getDescription());
                            }
                            if(pujoCommitteeModel.getType()!=null && !pujoCommitteeModel.getType().isEmpty()) {
//                                com_type.setText(pujoCommitteeModel.getType());
                                RadioButton radioButton1 = radioGroup.findViewById(R.id.sarbojonin);
                                RadioButton radioButton2 = radioGroup.findViewById(R.id.bonediBari);
                                RadioButton radioButton3 = radioGroup.findViewById(R.id.abashon);
                                RadioButton radioButton4 = radioGroup.findViewById(R.id.math);
                                RadioButton radioButton5 = radioGroup.findViewById(R.id.prabashi);
                                RadioButton radioButton6 = radioGroup.findViewById(R.id.others);

                                if(pujoCommitteeModel.getType().matches("Sarbojonin")
                                || pujoCommitteeModel.getType().matches("সর্বজনীন")){
                                    radioGroup.check(R.id.sarbojonin);
//                                    radioButton1.setSelected(true);
//                                    radioButton2.setSelected(false);
//                                    radioButton3.setSelected(false);
                                }
                                else if(pujoCommitteeModel.getType().matches("Bonedi Bari")
                                || pujoCommitteeModel.getType().matches("বোনেদি বাড়ি")){
                                    radioGroup.check(R.id.bonediBari);
//                                    radioButton1.setSelected(false);
//                                    radioButton2.setSelected(true);
//                                    radioButton3.setSelected(false);
                                }
                                else if(pujoCommitteeModel.getType().matches("Housing Complex")
                                || pujoCommitteeModel.getType().matches("আবাসন")){
                                    radioGroup.check(R.id.abashon);
//                                    radioButton1.setSelected(false);
//                                    radioButton2.setSelected(false);
//                                    radioButton3.setSelected(true);
                                }
                                else if(pujoCommitteeModel.getType().matches("Math/ Mission")
                                || pujoCommitteeModel.getType().matches("মঠ/ মিশন")){
                                    radioGroup.check(R.id.math);
                                }
                                else if(pujoCommitteeModel.getType().matches("Prabashi")
                                || pujoCommitteeModel.getType().matches("প্রবাসী")){
                                    radioGroup.check(R.id.prabashi);
                                }
                                else if(pujoCommitteeModel.getType().matches("Others")
                                || pujoCommitteeModel.getType().matches("অন্যান্য")){
                                    radioGroup.check(R.id.others);
                                }
                            }
                            if(pujoCommitteeModel.getUpiid()!=null && !pujoCommitteeModel.getUpiid().isEmpty()){
                                com_upi.setText(pujoCommitteeModel.getUpiid());
                            }
                        }
                    }
                });

        com_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditProfileCommitteeActivity.this, SearchCityState.class);
                i.putExtra("from","city_pujo_edit");
                startActivity(i);
            }
        });

        com_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditProfileCommitteeActivity.this, SearchCityState.class);
                i.putExtra("from","state_pujo_edit");
                startActivity(i);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                COMNAME = com_name.getText().toString().trim();
                DESCRIPTION = com_desc.getText().toString().trim();
                PUJOTYPE = com_type.getText().toString().trim();
                ADDRESS = com_address.getText().toString().trim();
                CITY = com_city.getText().toString().trim();
                STATE = com_state.getText().toString().trim();
                PIN =com_pin.getText().toString().trim();
                CONTACT = com_contact.getText().toString().trim();
                UPIID = com_upi.getText().toString().trim();

                int selectedType = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedType);
                PUJOTYPE = radioButton.getText().toString().trim();

                if (COMNAME.isEmpty() || CITY.isEmpty() ||PUJOTYPE.isEmpty() || ADDRESS.isEmpty()
                        || STATE.isEmpty() || PIN.isEmpty() || DESCRIPTION.isEmpty() || PROFILEPIC==null || COVERPIC==null || CONTACT.isEmpty()) {
                    if (COMNAME.isEmpty()) {
                        com_name.setError("Committee name is missing");
                        com_name.requestFocus();
                    }
                    if (DESCRIPTION.isEmpty()) {
                        com_desc.setError("Description is missing");
                        com_desc.requestFocus();
                    }
                    if (ADDRESS.isEmpty()) {
                        com_address.setError("Address line is missing");
                        com_address.requestFocus();
                    }
                    if (CITY.isEmpty()) {
                        com_city.setError("City is missing");
                        com_city.requestFocus();
                    }
                    if (STATE.isEmpty()) {
                        com_state.setError("State is missing");
                        com_state.requestFocus();
                    }
                    if (PIN.isEmpty()) {
                        com_pin.setError("Pincode is missing");
                        com_pin.requestFocus();
                    }
                    if (CONTACT.isEmpty()) {
                        com_contact.setError("Contact Number is missing");
                        com_contact.requestFocus();
                    }
//                    if (PUJOTYPE.isEmpty()) {
//                        com_type.setError("Type is missing");
//                        com_type.requestFocus();
//                    }
                    if (PUJOTYPE.isEmpty()) {
                        BasicUtility.showToast(getApplicationContext(),"Please provide type of pujo");
                    }
                    if(PROFILEPIC==null){
                        BasicUtility.showToast(EditProfileCommitteeActivity.this,"Please set a Profile Photo");
                    }
                    if(COVERPIC==null){
                        BasicUtility.showToast(EditProfileCommitteeActivity.this,"Please set a Cover Photo");
                    }

                }
                else {

                    long upvotes = baseUserModel.getUpvotes();
                    ArrayList<String> upvoteL = baseUserModel.getUpvoteL();
                    Timestamp lastVisit = baseUserModel.getLastVisitTime();

                    progressDialog = new ProgressDialog(EditProfileCommitteeActivity.this);
                    progressDialog.setTitle("Editing Your Profile");
                    progressDialog.setMessage("Hang on...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    docrefBase = FirebaseFirestore.getInstance().collection("Users")
                            .document(uid);

                    docrefCommittee = FirebaseFirestore.getInstance().collection("Users/"+uid+"/"+"com/")
                            .document(uid);

                    baseUserModel = new BaseUserModel();
                    baseUserModel.setAddressline(ADDRESS);
                    baseUserModel.setCity(CITY);
                    baseUserModel.setEmail(EMAIL);
                    baseUserModel.setName(COMNAME);
                    baseUserModel.setSmall_name(COMNAME.toLowerCase());
                    baseUserModel.setState(STATE);
                    baseUserModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    baseUserModel.setType(introPref.getType());
                    baseUserModel.setCommentcount(commentcount);
                    baseUserModel.setLastVisitTs(lastVisitTs);
                    baseUserModel.setLikeCount(likeCount);
                    baseUserModel.setPujoVisits(pujoVisits);
                    baseUserModel.setCoverpic(COVERPIC);
                    baseUserModel.setDp(PROFILEPIC);
                    baseUserModel.setPin(PIN);
                    baseUserModel.setContact(CONTACT);
                    baseUserModel.setUpvotes(upvotes);
                    baseUserModel.setUpvoteL(upvoteL);
                    baseUserModel.setLastVisitTime(lastVisit);
                    if(verified==1){
                        baseUserModel.setVerified(true);
                    }
                    else if (verified==0){
                        baseUserModel.setVerified(false);
                    }

                    pujoCommitteeModel = new PujoCommitteeModel();
                    pujoCommitteeModel.setDescription(DESCRIPTION);
                    pujoCommitteeModel.setType(PUJOTYPE);
                    pujoCommitteeModel.setUpiid(UPIID);

                    docrefBase.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                docrefCommittee.set(pujoCommitteeModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            introPref.setFullName(baseUserModel.getName());
                                            BasicUtility.showToast(getApplicationContext(), "Profile Edited");
                                            Intent intent = new Intent(EditProfileCommitteeActivity.this, ActivityProfileCommittee.class);
                                            intent.putExtra("uid", fireuser.getUid());
                                            startActivity(intent);
                                            finish();
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                        }
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                                            }
                                        });

                            } else {
                                progressDialog.dismiss();
                                BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(EditProfileCommitteeActivity.this, ActivityProfileCommittee.class);
        StoreTemp.getInstance().setPic(null);
        StoreTemp.getInstance().setCoverpic(null);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            Intent i = new Intent(EditProfileCommitteeActivity.this, ActivityProfileCommittee.class);
            StoreTemp.getInstance().setPic(null);
            StoreTemp.getInstance().setCoverpic(null);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}