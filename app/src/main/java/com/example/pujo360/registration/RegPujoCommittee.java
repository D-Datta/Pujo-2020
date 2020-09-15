package com.example.pujo360.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pujo360.MainActivity;
import com.example.pujo360.R;
import com.example.pujo360.models.BaseUserModel;
import com.example.pujo360.models.PujoCommitteeModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.UploadTask;

public class RegPujoCommittee extends AppCompatActivity {

    private EditText etcommitteename, etdescription, etaddressline, etcity, ettype, etstate;
    private String scommitteename, sdescription, saddress, scity, stype, sstate;
    private Button register;
    private ProgressDialog progressDialog;
    private String userID, usertype;
    private String semail,spassword;


    private DocumentReference docrefBase, docrefCommittee;
    private BaseUserModel baseUserModel;
    private IntroPref introPref;
    private PujoCommitteeModel pujoCommitteeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_pujo_committee);

        etaddressline = findViewById(R.id.committee_addressline);
        etcity = findViewById(R.id.committee_city);
        etcommitteename = findViewById(R.id.committee_name);
        etdescription = findViewById(R.id.committee_description);
        ettype = findViewById(R.id.committee_type);
        etstate = findViewById(R.id.committee_state);
        register = findViewById(R.id.register);


        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        introPref = new IntroPref(RegPujoCommittee.this);
        usertype = introPref.getType();

        semail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        spassword = getIntent().getStringExtra("password");


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scommitteename = etcommitteename.getText().toString().trim();
                sdescription = etdescription.getText().toString().trim();
                stype = ettype.getText().toString().trim();
                saddress = etaddressline.getText().toString().trim();
                scity = etcity.getText().toString().trim();
                sstate = etstate.getText().toString().trim();


                if (scommitteename.isEmpty() || scity.isEmpty() ||stype.isEmpty() || saddress.isEmpty() || sstate.isEmpty()) {
                    if (scommitteename.isEmpty()) {
                        etcommitteename.setError("Committee name is missing");
                        etcommitteename.requestFocus();
                    }
                    if (saddress.isEmpty()) {
                        etaddressline.setError("Address line is missing");
                        etaddressline.requestFocus();
                    }
                    if (scity.isEmpty()) {
                        etcity.setError("City name is missing");
                        etcity.requestFocus();
                    }
                    if (sstate.isEmpty()) {
                        etstate.setError("Address line is missing");
                        etstate.requestFocus();
                    }
                    if (stype.isEmpty()) {
                        ettype.setError("Type is missing");
                        ettype.requestFocus();
                    }

                }
                else {

                    progressDialog = new ProgressDialog(RegPujoCommittee.this);
                    progressDialog.setTitle("Creating Profile");
                    progressDialog.setMessage("Hang on...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();


                  if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                  {
                      docrefBase = FirebaseFirestore.getInstance().collection("Users")
                              .document(userID);

                      docrefCommittee = FirebaseFirestore.getInstance().collection("Users/"+userID+"/"+usertype+"/")
                              .document(userID);

                      baseUserModel = new BaseUserModel();
                      baseUserModel.setAddressline(saddress);
                      baseUserModel.setCity(scity);
                      baseUserModel.setEmail(semail);
                      baseUserModel.setName(scommitteename);
                      baseUserModel.setState(sstate);
                      baseUserModel.setUid(userID);
                      baseUserModel.setUsertype(usertype);

                      pujoCommitteeModel = new PujoCommitteeModel();
                      pujoCommitteeModel.setDescription(sdescription);
                      pujoCommitteeModel.setType(stype);

                      docrefBase.set(baseUserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful())
                              {

                                  docrefCommittee.set(pujoCommitteeModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if(task.isSuccessful())
                                          {
                                              Utility.showToast(RegPujoCommittee.this,"Profile Created");
                                              progressDialog.dismiss();
                                              Intent i = new Intent(RegPujoCommittee.this, MainActivity.class);
                                              i.putExtra("usertype",usertype);
                                              startActivity(i);
                                              finish();
                                          }
                                          else{
                                              Utility.showToast(RegPujoCommittee.this,"Something went wrong");
                                              progressDialog.dismiss();
                                          }

                                      }
                                  });

                              }
                              else{
                                  Utility.showToast(RegPujoCommittee.this,"Something went wrong");
                                  progressDialog.dismiss();
                              }

                          }
                      });
                  }
//                    FirebaseAuth.getInstance().signInWithEmailAndPassword(remail, rpassword)
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (!task.isSuccessful()) {
//                                    } else {
//                                        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
//
//                                            docref = FirebaseFirestore.getInstance().collection("Users")
//                                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                                            docref2 = FirebaseFirestore.getInstance()
//                                                    .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/AccessToken/")
//                                                    .document("Token");
//
//                                            userModel = new BaseUserModel();
//                                            userModel.setFirstname(rfirstname);
//                                            userModel.setLastname(rlastname);
//                                            userModel.setContact(rcontact);
//                                            userModel.setAge(rage);
//                                            userModel.setAddress(raddress);
//                                            userModel.setHeadline(rheadline);
//                                            userModel.setCity(rcity);
//                                            userModel.setPincode(rpincode);
//                                            userModel.setState(rstate);
//                                            userModel.setGender(rgender);
//                                            userModel.setType(type);
//                                            userModel.setEmail(remail);
//                                            userModel.setUid(fireuser.getUid());
//                                            userModel.setFollowerL(followlist);
//
//
//                                            if (pic != null) {
//                                                reference = storageReference.child("Users/").child("DP/").child(fireuser.getUid() + "_dp");
//                                                reference.putBytes(pic)
//                                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                                            @Override
//                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                                    @Override
//                                                                    public void onSuccess(Uri uri) {
//                                                                        downloaduri = uri;
//                                                                        generatedfilepath = downloaduri.toString();
//
//                                                                        introPref.setUserdp(generatedfilepath);
//                                                                        userModel.setProfilepic(generatedfilepath);
//
//                                                                        docref.set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                            @Override
//                                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                                if (task.isSuccessful()) {
//                                                                                    docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                                        @Override
//                                                                                        public void onComplete(@NonNull Task<Void> task) {
//
//                                                                                            //                                                                                                Utility.showToast(getApplicationContext(), "Profile created");
//                                                                                            progressDialog.dismiss();
//                                                                                            if (type.matches("Teacher")) {
//                                                                                                Intent i = new Intent(Reg1.this, RegTeachers.class);
//                                                                                                i.putExtra("type", type);
//                                                                                                i.putExtra("firstname", rfirstname);
//                                                                                                i.putExtra("lastname", rlastname);
//                                                                                                i.putExtra("dp", generatedfilepath);
//                                                                                                i.putExtra("contact", rcontact);
//                                                                                                i.putExtra("uid", fireuser.getUid());
//                                                                                                startActivity(i);
//                                                                                                finish();
//                                                                                            } else if (type.matches("Student")) {
//                                                                                                Intent i = new Intent(Reg1.this, RegStudents.class);
//                                                                                                i.putExtra("type", type);
//                                                                                                i.putExtra("firstname", rfirstname);
//                                                                                                i.putExtra("lastname", rlastname);
//                                                                                                i.putExtra("dp", generatedfilepath);
//                                                                                                i.putExtra("contact", rcontact);
//                                                                                                i.putExtra("uid", fireuser.getUid());
//                                                                                                startActivity(i);
//                                                                                                finish();
//                                                                                            } else if (type.matches("Parent")) {
//                                                                                                Intent i = new Intent(Reg1.this, RegParents.class);
//                                                                                                i.putExtra("type", type);
//                                                                                                i.putExtra("firstname", rfirstname);
//                                                                                                i.putExtra("lastname", rlastname);
//                                                                                                i.putExtra("dp", generatedfilepath);
//                                                                                                i.putExtra("contact", rcontact);
//                                                                                                i.putExtra("uid", fireuser.getUid());
//                                                                                                startActivity(i);
//                                                                                                finish();
//                                                                                            }
//
//                                                                                        }
//                                                                                    });
//                                                                                } else {
//                                                                                    Utility.showToast(getApplicationContext(), "Something went wrong.");
//                                                                                    progressDialog.dismiss();
//                                                                                }
//                                                                            }
//                                                                        });
//                                                                    }
//                                                                });
//                                                            }
//                                                        })
//                                                        .addOnFailureListener(new OnFailureListener() {
//                                                            @Override
//                                                            public void onFailure(@NonNull Exception e) {
//                                                                Utility.showToast(getApplicationContext(), "Something went wrong");
//                                                                progressDialog.dismiss();
//
//                                                            }
//                                                        });
//                                            } else {
//                                                introPref.setUserdp(null);
//                                                userModel.setProfilepic(null);
//
//                                                docref.set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                        if (task.isSuccessful()) {
//                                                            docref2.set(accessToken).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                @Override
//                                                                public void onComplete(@NonNull Task<Void> task) {
//
//                                                                    //                                                                        Utility.showToast(getApplicationContext(), "Profile created");
//                                                                    progressDialog.dismiss();
//                                                                    if (type.matches("Teacher")) {
//                                                                        Intent i = new Intent(Reg1.this, RegTeachers.class);
//                                                                        i.putExtra("type", type);
//                                                                        i.putExtra("firstname", rfirstname);
//                                                                        i.putExtra("lastname", rlastname);
//                                                                        i.putExtra("dp", generatedfilepath);
//                                                                        i.putExtra("contact", rcontact);
//                                                                        i.putExtra("uid", fireuser.getUid());
//                                                                        startActivity(i);
//                                                                        finish();
//                                                                    } else if (type.matches("Student")) {
//                                                                        Intent i = new Intent(Reg1.this, RegStudents.class);
//                                                                        i.putExtra("type", type);
//                                                                        i.putExtra("firstname", rfirstname);
//                                                                        i.putExtra("lastname", rlastname);
//                                                                        i.putExtra("dp", generatedfilepath);
//                                                                        i.putExtra("contact", rcontact);
//                                                                        i.putExtra("uid", fireuser.getUid());
//                                                                        startActivity(i);
//                                                                        finish();
//                                                                    } else if (type.matches("Parent")) {
//                                                                        Intent i = new Intent(Reg1.this, RegParents.class);
//                                                                        i.putExtra("type", type);
//                                                                        i.putExtra("firstname", rfirstname);
//                                                                        i.putExtra("lastname", rlastname);
//                                                                        i.putExtra("dp", generatedfilepath);
//                                                                        i.putExtra("contact", rcontact);
//                                                                        i.putExtra("uid", fireuser.getUid());
//                                                                        startActivity(i);
//                                                                        finish();
//                                                                    }
//
//                                                                }
//                                                            });
//                                                        } else {
//                                                            Utility.showToast(getApplicationContext(), "Something went wrong.");
//                                                            progressDialog.dismiss();
//                                                        }
//                                                    }
//                                                });
//
//                                            }
//
//                                        }
//                                        else {
//                                            Toast.makeText(Reg1.this, "Please verify your email and register", Toast.LENGTH_SHORT).show();
//                                            progressDialog.dismiss();
//                                        }
//                                    }
//                                }
//                            });

                }

            }
        });






    }
}