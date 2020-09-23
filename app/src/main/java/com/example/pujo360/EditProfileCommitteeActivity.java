package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pujo360.models.AccessToken;
import com.example.pujo360.models.BaseUserModel;
import com.example.pujo360.models.IndividualModel;
import com.example.pujo360.models.NotifCount;
import com.example.pujo360.models.PujoCommitteeModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.registration.RegPujoCommittee;
import com.example.pujo360.util.StoreTemp;
import com.example.pujo360.util.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.TYPE;

public class EditProfileCommitteeActivity extends AppCompatActivity {

    private EditText com_name, com_desc, com_type, com_address, com_pin;
    public static EditText com_state, com_city;
    private Button submit;

    private long likeCount;
    private long commentcount;
    private long pujoVisits;
    private Timestamp lastVisitTs;

    private String COMNAME,DESCRIPTION,PUJOTYPE,EMAIL,ADDRESS,CITY,STATE,PIN,PROFILEPIC,COVERPIC,uid;
    private String tokenStr;
    private BaseUserModel baseUserModel;
    private PujoCommitteeModel pujoCommitteeModel;
    private DocumentReference docrefBase, docrefCommittee;
    private FirebaseAuth mAuth;
    private FirebaseUser fireuser;
    private ProgressDialog progressDialog;
    private IntroPref introPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            BaseUserModel baseUserModel = task.getResult().toObject(BaseUserModel.class);
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
                            if(pujoCommitteeModel.getDescription()!=null && !pujoCommitteeModel.getDescription().isEmpty())
                            {
                                com_desc.setText(pujoCommitteeModel.getDescription());
                            }
                            if(pujoCommitteeModel.getType()!=null && !pujoCommitteeModel.getType().isEmpty())
                            {
                                com_type.setText(pujoCommitteeModel.getType());
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


                if (COMNAME.isEmpty() || CITY.isEmpty() ||PUJOTYPE.isEmpty() || ADDRESS.isEmpty()
                        || STATE.isEmpty() || PIN.isEmpty() || DESCRIPTION.isEmpty() || PROFILEPIC==null || COVERPIC==null) {
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
                    if (PUJOTYPE.isEmpty()) {
                        com_type.setError("Type is missing");
                        com_type.requestFocus();
                    }
                    if(PROFILEPIC==null){
                        Utility.showToast(EditProfileCommitteeActivity.this,"Please set a Profile Photo");
                    }
                    if(COVERPIC==null){
                        Utility.showToast(EditProfileCommitteeActivity.this,"Please set a Cover Photo");
                    }

                }
                else {

                    progressDialog = new ProgressDialog(EditProfileCommitteeActivity.this);
                    progressDialog.setTitle("Editing Your Profile");
                    progressDialog.setMessage("Hang on...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    docrefBase = FirebaseFirestore.getInstance().collection("Users")
                            .document(uid);

//                    docref2= FirebaseFirestore.getInstance()
//                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/AccessToken/")
//                            .document("Token");
//                    docref3= FirebaseFirestore.getInstance()
//                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
//                            .document("notifCount");


                    docrefCommittee = FirebaseFirestore.getInstance().collection("Users/"+uid+"/"+"com/")
                            .document(uid);

                    baseUserModel = new BaseUserModel();
                    baseUserModel.setAddressline(ADDRESS);
                    baseUserModel.setCity(CITY);
                    baseUserModel.setEmail(EMAIL);
                    baseUserModel.setName(COMNAME);
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


                    pujoCommitteeModel = new PujoCommitteeModel();
                    pujoCommitteeModel.setDescription(DESCRIPTION);
                    pujoCommitteeModel.setType(PUJOTYPE);

                    docrefBase.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                docrefCommittee.set(pujoCommitteeModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Utility.showToast(getApplicationContext(), "Profile Edited");
                                            Intent intent = new Intent(EditProfileCommitteeActivity.this, ActivityProfileCommittee.class);
                                            intent.putExtra("uid", fireuser.getUid());
                                            startActivity(intent);
                                            finish();
                                        }
                                        else{
                                            progressDialog.dismiss();
                                            Utility.showToast(getApplicationContext(), "Something went wrong.");
                                        }
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Utility.showToast(getApplicationContext(), "Something went wrong.");
                                            }
                                        });

                            } else {
                                progressDialog.dismiss();
                                Utility.showToast(getApplicationContext(), "Something went wrong.");
                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Utility.showToast(getApplicationContext(), "Something went wrong.");
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
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}