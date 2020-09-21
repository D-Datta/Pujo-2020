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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pujo360.models.AccessToken;
import com.example.pujo360.models.BaseUserModel;
import com.example.pujo360.models.IndividualModel;
import com.example.pujo360.models.NotifCount;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.registration.RegIndividual;
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

public class EditProfileIndividualActivity extends AppCompatActivity {

    private ImageView reg_dhak,cover_ind,dp_ind,edit_cover_ind,edit_dp_ind;
    private EditText fname_ind,lname_ind,username_ind,addressline_ind;
    public static EditText city_ind,state_ind;
    private long likeCount;
    private long commentcount;
    private long pujoVisits;
    private Timestamp lastVisitTs;
//    private TextView email_ind;
//    private Spinner gender_ind;
    private Button submit_ind;

    private String FNAME,LNAME,USERNAME,ADDRESS,CITY,STATE,EMAIL,GENDER,PROFILEPIC,COVERPIC,PASSWORD,uid;

    private String tokenStr;
    private BaseUserModel baseUserModel;
    private IndividualModel individualModel;
    private DocumentReference docref,docref4;
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
        setContentView(R.layout.activity_edit_profile_individual);

        Toolbar toolbar = findViewById(R.id.toolbar_edit_indi);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        reg_dhak = findViewById(R.id.reg_dhak);
        cover_ind = findViewById(R.id.reg_coverpic_ind);
        dp_ind = findViewById(R.id.reg_dp_ind);
        edit_cover_ind = findViewById(R.id.reg_edit_coverpic_icon_ind);
        edit_dp_ind = findViewById(R.id.reg_edit_dp_ind);
        fname_ind = findViewById(R.id.first_name_ind);
        lname_ind = findViewById(R.id.last_name_ind);
        username_ind = findViewById(R.id.user_name_ind);
        addressline_ind = findViewById(R.id.address_line_ind);
        city_ind = findViewById(R.id.city_ind);
        state_ind = findViewById(R.id.state_ind);
//        email_ind = findViewById(R.id.email_ind);
//        gender_ind = findViewById(R.id.gender_ind);
        submit_ind = findViewById(R.id.btn_submit_ind);

        mAuth=FirebaseAuth.getInstance();
        fireuser= mAuth.getCurrentUser();

        storage= FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        introPref= new IntroPref(EditProfileIndividualActivity.this);
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
                                addressline_ind.setText(baseUserModel.getAddressline());
                            }
                            if(baseUserModel.getCity()!=null && !baseUserModel.getCity().isEmpty())
                            {
                                city_ind.setText(baseUserModel.getCity());
                            }
                            if(baseUserModel.getCoverpic()!=null && !baseUserModel.getCoverpic().isEmpty())
                            {
                                if(StoreTemp.getInstance().getCoverpic()!=null && StoreTemp.getInstance().getCoverpic().length>0){
                                    cover_ind.setImageBitmap(BitmapFactory.decodeByteArray(StoreTemp.getInstance().getCoverpic(),0, StoreTemp.getInstance().getCoverpic().length));
                                }
                                else
                                {
                                    Picasso.get().load(baseUserModel.getCoverpic()).into(cover_ind);
                                }
                            }
                            if(baseUserModel.getDp()!=null && !baseUserModel.getDp().isEmpty())
                            {
                                if(StoreTemp.getInstance().getPic()!=null && StoreTemp.getInstance().getPic().length>0){
                                    dp_ind.setImageBitmap(BitmapFactory.decodeByteArray(StoreTemp.getInstance().getPic(),0, StoreTemp.getInstance().getPic().length));
                                }
                                else
                                {
                                    Picasso.get().load(baseUserModel.getDp()).into(dp_ind);
                                }
                            }
                            if(baseUserModel.getName()!=null && !baseUserModel.getName().isEmpty())
                            {
                                username_ind.setText(baseUserModel.getName());
                            }
                            if(baseUserModel.getState()!=null && !baseUserModel.getState().isEmpty())
                            {
                                state_ind.setText(baseUserModel.getState());
                            }

                        }

                    }
                });

        FirebaseFirestore.getInstance().collection("Users").document(uid)
                .collection("indi")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            IndividualModel individualModel = task.getResult().toObject(IndividualModel.class);
                            GENDER = individualModel.getGender();
                            if(individualModel.getFirstname()!=null && !individualModel.getFirstname().isEmpty())
                            {
                                fname_ind.setText(individualModel.getFirstname());
                            }
                            if(individualModel.getLastname()!=null && !individualModel.getLastname().isEmpty())
                            {
                                lname_ind.setText(individualModel.getLastname());
                            }
                        }
                    }
                });

        edit_dp_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=0;
            }
        });
        edit_cover_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
                pictype=1;
            }
        });

        city_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditProfileIndividualActivity.this, SearchCityState.class);
                i.putExtra("from","city_ind_edit");
                startActivity(i);
            }
        });
        state_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditProfileIndividualActivity.this, SearchCityState.class);
                i.putExtra("from","state_ind_edit");
                startActivity(i);
            }
        });




        submit_ind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FNAME = fname_ind.getText().toString().trim();
                LNAME = lname_ind.getText().toString().trim();
                USERNAME = username_ind.getText().toString().trim();
//                EMAIL = email_ind.getText().toString().trim();
                ADDRESS = addressline_ind.getText().toString().trim();
                CITY = city_ind.getText().toString().trim();
                STATE = state_ind.getText().toString().trim();
//                GENDER = gender_ind.getSelectedItem().toString().trim();


                if (FNAME.isEmpty() || LNAME.isEmpty() || USERNAME.isEmpty()) {

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
//                    if (GENDER.isEmpty()) {
//                        Utility.showToast(RegIndividual.this,"Gender Miising");
//                    }

                }
                else {
                    progressDialog = new ProgressDialog(EditProfileIndividualActivity.this);
                    progressDialog.setTitle("Editing your profile");
                    progressDialog.setMessage("Hang on...");
                    progressDialog.show();

                    UsedID = fireuser.getUid();

                    docref = FirebaseFirestore.getInstance().collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

//                    docref2= FirebaseFirestore.getInstance()
//                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/AccessToken/")
//                            .document("Token");
//                    docref3= FirebaseFirestore.getInstance()
//                            .collection("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/notifCount/")
//                            .document("notifCount");

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
                    baseUserModel.setCommentcount(commentcount);
                    baseUserModel.setLastVisitTs(lastVisitTs);
                    baseUserModel.setLikeCount(likeCount);
                    baseUserModel.setPujoVisits(pujoVisits);
                    baseUserModel.setCoverpic(COVERPIC);
                    baseUserModel.setDp(PROFILEPIC);

                    individualModel = new IndividualModel();
                    individualModel.setFirstname(FNAME);
                    individualModel.setLastname(LNAME);
                    individualModel.setGender(GENDER);

                    if (pic != null || coverpicbyte != null) {

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

                                                                docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {

                                                                            docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        progressDialog.dismiss();
                                                                                        Utility.showToast(getApplicationContext(), "Profile Edited");
                                                                                        Intent intent = new Intent(EditProfileIndividualActivity.this, ActivityProfileUser.class);
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
                                                docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        progressDialog.dismiss();
                                                                        Utility.showToast(getApplicationContext(), "Profile Edited");
                                                                        Intent intent = new Intent(EditProfileIndividualActivity.this, ActivityProfileUser.class);
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
                                                                                        progressDialog.dismiss();
                                                                                        Utility.showToast(getApplicationContext(), "Profile Edited");
                                                                                        Intent intent = new Intent(EditProfileIndividualActivity.this, ActivityProfileUser.class);
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

                                                docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        progressDialog.dismiss();
                                                                        Utility.showToast(getApplicationContext(), "Profile Edited");
                                                                        Intent intent = new Intent(EditProfileIndividualActivity.this, ActivityProfileUser.class);
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
                    else {

//                        NotifCount notifCount= new NotifCount();
//                        notifCount.setNotifCount(0);

                        docref.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    docref4.set(individualModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                                Utility.showToast(getApplicationContext(), "Profile Edited");
                                                Intent intent = new Intent(EditProfileIndividualActivity.this, ActivityProfileUser.class);
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
                                    .start(EditProfileIndividualActivity.this);
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
                                    .start(EditProfileIndividualActivity.this);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            ////////////////////////CROP//////////////////////
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                if(pictype==0){
                    pic = baos.toByteArray();
                }
                else if(pictype==1){
                    coverpicbyte = baos.toByteArray();
                }

                new ImageCompressor().execute();

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
    public void onBackPressed() {
        Intent i = new Intent(EditProfileIndividualActivity.this, ActivityProfileUser.class);
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