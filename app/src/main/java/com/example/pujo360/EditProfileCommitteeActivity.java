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

    private ImageView edit_coverpic, edit_dp, edit_coverpic_icon, edit_dp_icon;
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
    //    private File file;
    private Uri filepath;
    private Uri downloaduri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference reference;
    private FirebaseUser fireuser;
    private String generatedfilepath;
    byte[] pic, coverpicbyte;
    public static String UsedID;
    private ProgressDialog progressDialog;
    private IntroPref introPref;
    private AccessToken accessToken;
    private int pictype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_committee);

        Toolbar toolbar = findViewById(R.id.toolbar_edit_com);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        edit_coverpic = findViewById(R.id.edit_coverpic_pc);
        edit_coverpic_icon = findViewById(R.id.edit_coverpic_icon_pc);
        edit_dp = findViewById(R.id.edit_dp_pc);
        edit_dp_icon = findViewById(R.id.edit_dp_icon_pc);
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

        storage= FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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
                            if(baseUserModel.getCoverpic()!=null && !baseUserModel.getCoverpic().isEmpty())
                            {
                                if(StoreTemp.getInstance().getCoverpic()!=null && StoreTemp.getInstance().getCoverpic().length>0){
                                    edit_coverpic.setImageBitmap(BitmapFactory.decodeByteArray(StoreTemp.getInstance().getCoverpic(),0, StoreTemp.getInstance().getCoverpic().length));
                                }
                                else
                                {
                                    Picasso.get().load(baseUserModel.getCoverpic()).into(edit_coverpic);
                                }
                            }
                            if(baseUserModel.getDp()!=null && !baseUserModel.getDp().isEmpty())
                            {
                                if(StoreTemp.getInstance().getPic()!=null && StoreTemp.getInstance().getPic().length>0){
                                    edit_dp.setImageBitmap(BitmapFactory.decodeByteArray(StoreTemp.getInstance().getPic(),0, StoreTemp.getInstance().getPic().length));
                                }
                                else
                                {
                                    Picasso.get().load(baseUserModel.getDp()).into(edit_dp);
                                }
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

        edit_dp_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=0;
            }
        });

        edit_coverpic_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=1;
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
                        || STATE.isEmpty() || PIN.isEmpty() || PROFILEPIC==null || COVERPIC==null) {
                    if (COMNAME.isEmpty()) {
                        com_name.setError("Committee name is missing");
                        com_name.requestFocus();
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

                    if(pic!=null || coverpicbyte!=null){
//                        NotifCount notifCount= new NotifCount();
//                        notifCount.setNotifCount(0);

                        if (pic != null) {
                            reference = storageReference.child("Users/").child("DP/").child(fireuser.getUid() + "_dp");
                            reference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            downloaduri = uri;
                                            generatedfilepath = downloaduri.toString();
                                            introPref.setUserdp(generatedfilepath);
                                            baseUserModel.setDp(generatedfilepath);

                                            if (coverpicbyte != null) {
                                                reference = storageReference.child("Users/").child("Coverpic/").child(fireuser.getUid() + "_coverpic");
                                                reference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                downloaduri = uri;
                                                                generatedfilepath = downloaduri.toString();
                                                                baseUserModel.setCoverpic(generatedfilepath);

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
                                                        });
                                                    }
                                                });
                                            }
                                            else {
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
                            });
                        }
                        else if(coverpicbyte != null) {
                            reference = storageReference.child("Users/").child("Coverpic/").child(fireuser.getUid() + "_coverpic");
                            reference.putBytes(coverpicbyte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            downloaduri = uri;
                                            generatedfilepath = downloaduri.toString();
                                            baseUserModel.setCoverpic(generatedfilepath);

                                            if (pic != null) {
                                                reference = storageReference.child("Users/").child("DP/").child(fireuser.getUid() + "_dp");
                                                reference.putBytes(pic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                downloaduri = uri;
                                                                generatedfilepath = downloaduri.toString();
                                                                introPref.setUserdp(generatedfilepath);
                                                                baseUserModel.setDp(generatedfilepath);

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
                                                        });
                                                    }
                                                });
                                            }
                                            else {
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
                            });
                        }
                    }
                    else{
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

            }
        });

    }

    private void chooseImage() {

        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== RESULT_OK){
            if(requestCode == 1 && data!=null && data.getData()!=null){
                filepath=data.getData();
                try {
                    if(filepath!=null) {
                        if(pictype==0){
                            CropImage.activity(filepath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAspectRatio(1,1)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(EditProfileCommitteeActivity.this);
                        }
                        else if(pictype==1){
                            CropImage.activity(filepath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAspectRatio(16,9)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(EditProfileCommitteeActivity.this);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            ////////////////////////CROP//////////////////////
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri imageUri = result.getUri();
                Bitmap bitmap = null;
                Bitmap compressedBitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    compressedBitmap = Utility.decodeSampledBitmapFromFile(bitmap, 612, 816);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                if(pictype==0){
                    pic = baos.toByteArray();
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(pic, 0 , pic.length);
                    edit_dp.setImageBitmap(bitmap1);
                }
                else if(pictype==1){
                    coverpicbyte = baos.toByteArray();
                    Bitmap bitmap2 = BitmapFactory.decodeByteArray(coverpicbyte, 0 , coverpicbyte.length);
                    edit_coverpic.setImageBitmap(bitmap2);
                }

                //new ImageCompressor().execute();

            }
            else {//CROP ERROR
                Toast.makeText(this, "+error", Toast.LENGTH_SHORT).show();
            }
            ////////////////////////CROP//////////////////////
        }
    }

    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private final float maxHeight = 1080.0f;
        private final float maxWidth = 720.0f;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public byte[] doInBackground(Void... strings) {

            if(pictype==0) {
                Bitmap scaledBitmap = null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);

                int actualHeight = options.outHeight;
                int actualWidth = options.outWidth;

                float imgRatio = (float) actualWidth / (float) actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;

                    }
                }

                options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inTempStorage = new byte[16 * 1024];

                try {
                    bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                try {
                    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                float ratioX = actualWidth / (float) options.outWidth;
                float ratioY = actualHeight / (float) options.outHeight;
                float middleX = actualWidth / 4.0f;
                float middleY = actualHeight / 4.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 4, middleY - bmp.getHeight() / 4, new Paint(Paint.FILTER_BITMAP_FLAG));

                if (bmp != null) {
                    bmp.recycle();
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
                byte[] by = out.toByteArray();
                return by;
            }
            else if(pictype==1){
                Bitmap scaledBitmap = null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeByteArray(coverpicbyte, 0, coverpicbyte.length, options);

                int actualHeight = options.outHeight;
                int actualWidth = options.outWidth;

                float imgRatio = (float) actualWidth / (float) actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;

                    }
                }

                options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inTempStorage = new byte[16 * 1024];

                try {
                    bmp = BitmapFactory.decodeByteArray(coverpicbyte, 0, coverpicbyte.length, options);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                try {
                    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                float ratioX = actualWidth / (float) options.outWidth;
                float ratioY = actualHeight / (float) options.outHeight;
                float middleX = actualWidth / 4.0f;
                float middleY = actualHeight / 4.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 4, middleY - bmp.getHeight() / 4, new Paint(Paint.FILTER_BITMAP_FLAG));

                if (bmp != null) {
                    bmp.recycle();
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
                byte[] by = out.toByteArray();
                return by;
            }
            else{
                return new byte[0];
            }
        }

        @Override
        protected void onPostExecute(byte[] picCompressed) {
            if(picCompressed!= null) {

                if(pictype==0){
                    pic = picCompressed;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(picCompressed, 0 , picCompressed.length);
                    edit_dp.setImageBitmap(bitmap);
                }
                else if(pictype==1){
                    coverpicbyte = picCompressed;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(picCompressed, 0 , picCompressed.length);
                    edit_coverpic.setImageBitmap(bitmap);
                }
            }
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 4;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }

            return inSampleSize;
        }

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