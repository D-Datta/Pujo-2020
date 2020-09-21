package com.example.pujo360;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class CommentEdit extends AppCompatActivity {

    private EditText comment;
    private ImageView userimage_comment;
    private CollectionReference commentRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_edit);

        Toolbar toolbar = findViewById(R.id.toolbar12);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Edit Comment");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        comment = findViewById(R.id.comment_edit);
        userimage_comment = findViewById(R.id.user_image_comment);
        Button update = findViewById(R.id.update_comment);

        final Intent intent = getIntent();
        Intent i= getIntent();
        String root = i.getStringExtra("root");

        commentRef = FirebaseFirestore.getInstance().collection(root + "/" + i.getStringExtra("com_postID_home") + "/commentL");

        if(i.getStringExtra("com_parentID_home") != null) {
            commentRef = FirebaseFirestore.getInstance().collection(root + "/" + i.getStringExtra("com_postID_home") + "/commentL/" + i.getStringExtra("com_parentID_home") + "/commentL");
        }

        comment.setText(intent.getStringExtra("comment_home"));
        if(intent.getStringExtra("com_img_home") != null) {
            String PROFILEPIC = intent.getStringExtra("com_img_home");
            Picasso.get().load(PROFILEPIC).into(userimage_comment, new Callback() {
                @Override
                public void onSuccess() { }

                @Override
                public void onError(Exception e) {
                    userimage_comment.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            });
        }

        update.setOnClickListener(v -> {
            progressDialog = new ProgressDialog(CommentEdit.this);
            progressDialog.setTitle("Saving changes");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            WriteBatch batch = FirebaseFirestore.getInstance().batch();

            batch.update(commentRef.document(Objects.requireNonNull(i.getStringExtra("com_docID_home"))),"comment",comment.getText().toString());
            batch.commit().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    progressDialog.dismiss();
                    ViewMoreHome.changed = 1;
                    CommentReplyActivity.change = 1;
                    if(i.getStringExtra("com_parentID_home") == null && Objects.requireNonNull(i.getStringExtra("from")).matches("yes")) {

                        Intent intent1 = new Intent(CommentEdit.this, CommentReplyActivity.class);
                        intent1.putExtra("comment", comment.getText().toString());
                        intent1.putExtra("username", i.getStringExtra("com_username_home"));
                        intent1.putExtra("userdp", i.getStringExtra("com_img_home"));
                        intent1.putExtra("docID", i.getStringExtra("com_docID_home"));
                        intent1.putExtra("postID", i.getStringExtra("com_postID_home"));
                        intent1.putExtra("postUid", i.getStringExtra("com_postUid_home"));
                        intent1.putExtra("likeL", i.getStringExtra("com_likeL_home"));
                        intent1.putExtra("ReplyCommentNo", i.getStringExtra("com_rComNo_home"));
                        intent1.putExtra("uid", i.getStringExtra("com_uid_home"));
                        intent1.putExtra("bool", i.getStringExtra("com_bool_home"));
                        intent1.putExtra("timestamp", i.getStringExtra("timestamp"));
                        intent1.putExtra("pComUid", i.getStringExtra("pComUid"));
                        intent1.putExtra("type", i.getStringExtra("type"));

                        Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
                        startActivity(intent1);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"Comment Updated", Toast.LENGTH_LONG).show();
                        CommentEdit.super.onBackPressed();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"Comment not updated !", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}