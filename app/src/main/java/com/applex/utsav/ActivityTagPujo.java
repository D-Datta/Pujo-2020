package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.applex.utsav.adapters.PujoTagAdapter;
import com.applex.utsav.models.PujoTagModel;
import com.applex.utsav.models.PujoTagsArrayModel;
import com.applex.utsav.models.TagModel;
import com.applex.utsav.preferences.IntroPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

public class ActivityTagPujo extends AppCompatActivity {

    private RecyclerView tagsRecycler;
    private IntroPref introPref;

    private PujoTagAdapter adapter;

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
        setContentView(R.layout.activity_tag_pujo);

        tagsRecycler = findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ActivityTagPujo.this);
        tagsRecycler.setLayoutManager(layoutManager);

        buildRecylerView();
    }

    private void buildRecylerView() {

        FirebaseFirestore.getInstance().collection("Tags")
                .document("Pujos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        PujoTagsArrayModel pujoTags = task.getResult().toObject(PujoTagsArrayModel.class);

                        adapter = new PujoTagAdapter(pujoTags.getTags(), ActivityTagPujo.this);

                        adapter.onClickListener(new PujoTagAdapter.OnClickListener() {
                            @Override
                            public void onClickListener(int position, String pujo, String uid) {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("name", pujo);
                                returnIntent.putExtra("uid", uid);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }
                        });
                        tagsRecycler.setAdapter(adapter);
                    }
                });

    }


}