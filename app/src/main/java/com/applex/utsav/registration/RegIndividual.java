package com.applex.utsav.registration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applex.utsav.MainActivity;
import com.applex.utsav.R;
import com.applex.utsav.SearchCityState;
import com.applex.utsav.models.AccessToken;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.IndividualModel;
import com.applex.utsav.models.NotifCount;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.util.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class RegIndividual extends AppCompatActivity
{
    private ImageView reg_dhak,cover_ind,dp_ind,edit_cover_ind,edit_dp_ind;
    private EditText fname_ind,lname_ind,username_ind,bio_ind;
    public static EditText city_ind,state_ind;
    private TextView email_ind;
    private Spinner gender_ind;
    private Button submit_ind;

    private String FNAME,LNAME,USERNAME,ADDRESS,CITY,STATE,EMAIL,GENDER,BIO,COVERPIC,PASSWORD;

    private String tokenStr;
    private BaseUserModel baseUserModel;
    private IndividualModel individualModel;
    private DocumentReference docref, docref2, docref3,docref4;
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_individual);

        reg_dhak = findViewById(R.id.reg_dhak);
        cover_ind = findViewById(R.id.reg_coverpic_ind);
        dp_ind = findViewById(R.id.reg_dp_ind);
        edit_cover_ind = findViewById(R.id.reg_edit_coverpic_icon_ind);
        edit_dp_ind = findViewById(R.id.reg_edit_dp_ind);
        fname_ind = findViewById(R.id.first_name_ind);
        lname_ind = findViewById(R.id.last_name_ind);
        username_ind = findViewById(R.id.user_name_ind);
        bio_ind = findViewById(R.id.bio_line_ind);
        city_ind = findViewById(R.id.city_ind);
        state_ind = findViewById(R.id.state_ind);
        email_ind = findViewById(R.id.email_ind);
        gender_ind = findViewById(R.id.gender_ind);
        submit_ind = findViewById(R.id.btn_submit_ind);

        mAuth=FirebaseAuth.getInstance();
        fireuser= mAuth.getCurrentUser();

        storage= FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        introPref= new IntroPref(RegIndividual.this);

        if(getIntent().getStringExtra("email")!=null){
            EMAIL = getIntent().getStringExtra("email");
            email_ind.setText(EMAIL);
            email_ind.setFreezesText(true);
        }
        if(getIntent().getStringExtra("password")!=null){
            PASSWORD = getIntent().getStringExtra("password");
        }
//        EMAIL = mAuth.getCurrentUser().getEmail();
//        email_ind.setText(EMAIL);
//        email_ind.setFreezesText(true);

        accessToken= new AccessToken();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "getInstanceId failed", task.getException());
                        Utility.showToast(getApplicationContext(),"Error creating token");
                        return;
                    }
                    // Get new Instance ID token
                    tokenStr = task.getResult().getToken();
                    accessToken.setRegToken(tokenStr);
                    // Log and toast
                    // String msg = getString(R.string.msg_token_fmt, token);
                    Log.d("TAG", tokenStr);
                    // Toast.makeText(RegFormPost2.this, token, Toast.LENGTH_LONG).show();
                });

        cover_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=1;
            }
        });
        edit_cover_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=1;
            }
        });
        dp_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=0;
            }
        });
        edit_dp_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=0;
            }
        });

        city_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegIndividual.this, SearchCityState.class);
                i.putExtra("from","city_ind");
                startActivity(i);
            }
        });
        state_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegIndividual.this, SearchCityState.class);
                i.putExtra("from","state_ind");
                startActivity(i);
            }
        });


        submit_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FNAME = fname_ind.getText().toString().trim();
                LNAME = lname_ind.getText().toString().trim();
                USERNAME = username_ind.getText().toString().trim();
                EMAIL = email_ind.getText().toString().trim();
                BIO = bio_ind.getText().toString().trim();
                CITY = city_ind.getText().toString().trim();
                STATE = state_ind.getText().toString().trim();
                GENDER = gender_ind.getSelectedItem().toString().trim();


                if (FNAME.isEmpty() || LNAME.isEmpty() || USERNAME.isEmpty() || GENDER.isEmpty()) {

                    if (FNAME.isEmpty()) {
                        fname_ind.setError("First Name Missing");
                        fname_ind.requestFocus();
                    }
                    if (LNAME.isEmpty()) {
                        lname_ind.setError("Last Name Missing");
                        lname_ind.requestFocus();
                    }
                    if(USERNAME.isEmpty()){
                        username_ind.setError("Username Missing");
                        username_ind.requestFocus();
                    }
                    if (GENDER.isEmpty()) {
                        Utility.showToast(RegIndividual.this,"Gender Miising");
                    }

                }
                else {
                    progressDialog = new ProgressDialog(RegIndividual.this);
                    progressDialog.setTitle("Creating your profile");
                    progressDialog.setMessage("Hang on...");
                    progressDialog.show();

                    UsedID = fireuser.getUid();

                    docref = FirebaseFirestore.getInstance().collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    docref2= FirebaseFirestore.getInstance()
                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/AccessToken/")
                            .document("Token");
                    docref3= FirebaseFirestore.getInstance()
                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
                            .document("notifCount");

                    docref4 = FirebaseFirestore.getInstance().collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("indi")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    baseUserModel = new BaseUserModel();
                    baseUserModel.setName(USERNAME);
                    baseUserModel.setEmail(EMAIL);
                    baseUserModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    baseUserModel.setAddressline(ADDRESS);
                    baseUserModel.setCity(CITY);
                    baseUserModel.setState(STATE);
                    baseUserModel.setType(introPref.getType());

                    individualModel = new IndividualModel();
                    individualModel.setFirstname(FNAME);
                    individualModel.setLastname(LNAME);
                    individualModel.setGender(GENDER);
                    individualModel.setBio(BIO);

                    if (pic != null || coverpicbyte != null) {

                        NotifCount notifCount= new NotifCount();
                        notifCount.setNotifCount(0);

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

                                                                docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {

                                                                            docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    docref3.set(notifCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                progressDialog.dismiss();
                                                                                                                Utility.showToast(getApplicationContext(), "Profile Created");
                                                                                                                Intent intent = new Intent(RegIndividual.this, MainActivity.class);
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
                                                docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    docref3.set(notifCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                progressDialog.dismiss();
                                                                                                Utility.showToast(getApplicationContext(), "Profile Created");
                                                                                                Intent intent = new Intent(RegIndividual.this, MainActivity.class);
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

                        else if (coverpicbyte != null) {
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

                                            if(pic!=null){
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

                                                                docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {

                                                                            docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    docref3.set(notifCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                progressDialog.dismiss();
                                                                                                                Utility.showToast(getApplicationContext(), "Profile Created");
                                                                                                                Intent intent = new Intent(RegIndividual.this, MainActivity.class);
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

                                                docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    docref3.set(notifCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                progressDialog.dismiss();
                                                                                                Utility.showToast(getApplicationContext(), "Profile Created");
                                                                                                Intent intent = new Intent(RegIndividual.this, MainActivity.class);
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
                    else {

                        NotifCount notifCount= new NotifCount();
                        notifCount.setNotifCount(0);

                        docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            docref3.set(notifCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        progressDialog.dismiss();
                                                                        Utility.showToast(getApplicationContext(), "Profile Created");
                                                                        Intent intent = new Intent(RegIndividual.this, MainActivity.class);
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
                                    .start(RegIndividual.this);
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
                                    .start(RegIndividual.this);
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
                    compressedBitmap.recycle();
                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(pic, 0 , pic.length);
                    dp_ind.setImageBitmap(bitmap1);

                }
                else if(pictype==1){
                    coverpicbyte = baos.toByteArray();
                    compressedBitmap.recycle();
                    Bitmap bitmap2 = BitmapFactory.decodeByteArray(coverpicbyte, 0 , coverpicbyte.length);
                    cover_ind.setImageBitmap(bitmap2);

                }

               // new ImageCompressor().execute();

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
                    dp_ind.setImageBitmap(bitmap);
                }
                else if(pictype==1){
                    coverpicbyte = picCompressed;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(picCompressed, 0 , picCompressed.length);
                    cover_ind.setImageBitmap(bitmap);
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
    protected void finalize() throws Throwable {
        Log.d("FINALIZE","called IN REG FORM 2!!!!!!!!!!!!!");
        super.finalize();
    }


}