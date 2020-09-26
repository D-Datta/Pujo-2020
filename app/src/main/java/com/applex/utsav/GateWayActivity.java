package com.applex.utsav;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.registration.LoginActivity;
import com.applex.utsav.utility.StoreTemp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
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
                if(params.get(1).matches("feeds"))
                {
                    if(params.get(2).matches("0"))
                    {
                        FirebaseFirestore.getInstance().collection("Feeds")
                                .document(params.get(3))
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            HomePostModel currentItem = task.getResult().toObject(HomePostModel.class);
                                            Intent intent = new Intent(GateWayActivity.this, ViewMoreText.class);
                                            intent.putExtra("username", currentItem.getUsN());
                                            intent.putExtra("userdp", currentItem.getDp());
                                            intent.putExtra("docID", currentItem.getDocID());
                                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                                            intent.putExtra("comName", currentItem.getComName());
                                            intent.putExtra("comID", currentItem.getComID());
                                            intent.putExtra("likeL", currentItem.getLikeL());
                                            if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                                                Bundle args = new Bundle();
                                                args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
                                                intent.putExtra("BUNDLE", args);
                                            }
                                            intent.putExtra("postText", currentItem.getTxt());
                                            intent.putExtra("bool", "3");
                                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                                            intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                                            intent.putExtra("uid", currentItem.getUid());
                                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                                            intent.putExtra("type", currentItem.getType());
                                            startActivity(intent);
                                        }

                                    }
                                });

                    }
                    else if(params.get(2).matches("1"))
                    {
                        FirebaseFirestore.getInstance().collection("Feeds")
                                .document(params.get(3))
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            HomePostModel currentItem = task.getResult().toObject(HomePostModel.class);
                                            Intent intent = new Intent(GateWayActivity.this, ViewMoreHome.class);
                                            intent.putExtra("username", currentItem.getUsN());
                                            intent.putExtra("userdp", currentItem.getDp());
                                            intent.putExtra("docID", currentItem.getDocID());
                                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                                            intent.putExtra("comName", currentItem.getComName());
                                            intent.putExtra("comID", currentItem.getComID());
                                            intent.putExtra("likeL", currentItem.getLikeL());
                                            if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
                                                Bundle args = new Bundle();
                                                args.putSerializable("ARRAYLIST", (Serializable)currentItem.getImg());
                                                intent.putExtra("BUNDLE", args);
                                            }
                                            intent.putExtra("postText", currentItem.getTxt());
                                            intent.putExtra("bool", "3");
                                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                                            intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                                            intent.putExtra("uid", currentItem.getUid());
                                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                                            intent.putExtra("type", currentItem.getType());
                                            startActivity(intent);
                                        }
                                    }
                                });
                    }
                }
//                if(params.size()>3){
//                    campus = params.get(2).replaceAll("_", " ");
//                    postID = params.get(3);
//                    if(params.get(1).matches("Home")){
//                        Intent i= new Intent(GateWayActivity.this, ViewMoreHome.class);
//                        i.putExtra("campus", campus);
//                        i.putExtra("postID", postID);
//                        i.putExtra("from", "link");
//                        startActivity(i);
//                        finish();
//                    }
//                    else {
//                        startActivity(new Intent(GateWayActivity.this, MainActivity.class));
//                        finish();
//                    }
//                }
//                else {
//                    startActivity(new Intent(GateWayActivity.this, MainActivity.class));
//                    finish();
//                }

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