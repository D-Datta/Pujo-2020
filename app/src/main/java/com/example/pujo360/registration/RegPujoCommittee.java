package com.example.pujo360.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.pujo360.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegPujoCommittee extends AppCompatActivity {

    private EditText etcommitteename, etdescription, etaddressline, etcity, ettype;
    private String scommitteename, sdescription, saddress, scity, stype;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_pujo_committee);

        etaddressline = findViewById(R.id.committee_addressline);
        etcity = findViewById(R.id.committee_city);
        etcommitteename = findViewById(R.id.committee_name);
        etdescription = findViewById(R.id.committee_description);
        ettype = findViewById(R.id.committee_type);
        register = findViewById(R.id.register);



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saddress = etaddressline.getText().toString().trim();
                scity = etcity.getText().toString().trim();
                scommitteename = etcommitteename.getText().toString().trim();
                sdescription = etdescription.getText().toString().trim();
                stype = ettype.getText().toString().trim();

                //                    if(rschool.isEmpty())
//                    {
//                        etschool.setError("School name is missing");
//                        etschool.requestFocus();
//                    }



            }
        });
//        submit_student.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {   rguardiansname = etguardiansname.getText().toString().trim();
//
//                if(rguardiansname.isEmpty() ||rschool.isEmpty())
//                {
//
//
//                    if(rguardiansname.isEmpty())
//                    {
//                        etguardiansname.setError("Guardian's Name Missing");
//                        etguardiansname.requestFocus();
//                    }
//                    if(rschool.isEmpty())
//                    {
//                        etschool.setError("School name is missing");
//                        etschool.requestFocus();
//                    }
//                }
//
//                else{
//                    progressDialog = new ProgressDialog(RegStudents.this);
//                    progressDialog.setTitle("Creating Profile");
//                    progressDialog.setMessage("Hang on...");
//                    progressDialog.setCanceledOnTouchOutside(false);
//                    progressDialog.show();
//
//                    docref = FirebaseFirestore.getInstance().collection("Users/"+UsedID+"/"+type+"/")
//                            .document(UsedID);
//
//                    studentModel = new StudentModel();
//                    studentModel.setGuardianContact(rgcontact);
//                    studentModel.setGuardian(rguardiansname);
//                    studentModel.setSchool(rschool);
//                    studentModel.setGrade(rgrade);
//                    studentModel.setDp(dp);
//                    studentModel.setFirstname(firstname);
//                    studentModel.setLastname(lastname);
//                    studentModel.setContact(contact);
//                    studentModel.setUid(uid);
//
//                    docref.set(studentModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                            if(task.isSuccessful()){
//
//                                Utility.showToast(RegStudents.this,"Profile Created");
//                                progressDialog.dismiss();
//                                Intent i = new Intent(RegStudents.this, MainActivity.class);
//                                i.putExtra("type",type);
//                                startActivity(i);
//                                finish();
//
//                            }
//                            else{
//                                Utility.showToast(RegStudents.this,"Something went wrong");
//                                progressDialog.dismiss();
//                            }
//                        }
//                    })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//
//                                    Utility.showToast(RegStudents.this,"Something went wrong");
//                                    progressDialog.dismiss();
//                                }
//                            });
//                }
//            }
//        });





    }
}