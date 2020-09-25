package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applex.utsav.utility.BasicUtility;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.applex.utsav.adapters.ProfileAdapter;
import com.applex.utsav.fragments.Fragment_Posts;
import com.applex.utsav.fragments.Fragment_Reels;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.PujoCommitteeModel;
import com.applex.utsav.preferences.IntroPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ActivityProfileCommittee extends AppCompatActivity {

    public static int delete = 0;
    private TextView PName,PUsername,Paddress;

    private ImageView PDp,Pcoverpic;
    private ReadMoreTextView PDetaileddesc;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String name, pujotype, coverpic, dp, address, city, state, pin, desc;

    public String uid;
    private com.google.android.material.floatingactionbutton.FloatingActionButton edit_profile_com;
    private FirebaseUser fireuser;
    int bool;
    private Button locate;
    private ConnectivityManager cm;

    private TextView visits, likes, followers;

    private int imageCoverOrDp = 0; //dp = 0, cover = 1
    private ImageView editDp, editCover;

    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri filePath;
    private ProgressDialog progressDialog;
    byte[] pic;

    IntroPref introPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_committee);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cm = (ConnectivityManager) ActivityProfileCommittee.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        introPref = new IntroPref(ActivityProfileCommittee.this);

        PDp = findViewById(R.id.Pdp);
        PName = findViewById(R.id.Profilename);
        PUsername =findViewById(R.id.Pusername);
        Pcoverpic = findViewById(R.id.coverpic);
        PDetaileddesc = findViewById(R.id.detaildesc);
        edit_profile_com = findViewById(R.id.edit_profile_com);
        locate = findViewById(R.id.locate);
        Paddress = findViewById(R.id.address_com);

        visits = findViewById(R.id.visits);
        likes = findViewById(R.id.likes);
        followers = findViewById(R.id.followers);
        editDp = findViewById(R.id.edit_dp);
        editCover = findViewById(R.id.edit_cover);

        tabLayout = findViewById(R.id.tabBar);
        viewPager = findViewById(R.id.viewPager);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0);
        tabLayout.getTabAt(1);

        fireuser = FirebaseAuth.getInstance().getCurrentUser();

        name = getIntent().getStringExtra("name");
        coverpic = getIntent().getStringExtra("coverpic");
        dp = getIntent().getStringExtra("dp");
        uid = getIntent().getStringExtra("uid");

        cm = (ConnectivityManager) ActivityProfileCommittee.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////
        if(getIntent()!=null && getIntent().getStringExtra("uid")!=null){
            uid = getIntent().getStringExtra("uid");
            if(!uid.matches(fireuser.getUid())){
                bool =1;//ANOTHER USER ACCOUNT
            }
        }
        else{
            uid = fireuser.getUid();
            bool = 0;//CURRENT USER ACCOUNT
        }
        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////

        if(uid.matches(FirebaseAuth.getInstance().getUid())) {
            editCover.setVisibility(View.VISIBLE);
            editDp.setVisibility(View.VISIBLE);

            edit_profile_com.setVisibility(View.VISIBLE);
            edit_profile_com.setOnClickListener(v -> {
                Intent i1 = new Intent(ActivityProfileCommittee.this, EditProfileCommitteeActivity.class);
                startActivity(i1);
                finish();
            });

            editDp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        imageCoverOrDp = 0; //dp
                        pickGallery();
                    }
                }
            });

            editCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        imageCoverOrDp = 1; //cover
                        pickGallery();
                    }
                }
            });
        }
        else {
            edit_profile_com.setVisibility(View.GONE);
            editCover.setVisibility(View.GONE);
            editDp.setVisibility(View.GONE);
            //increment no of visitors
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(FirebaseAuth.getInstance().getUid())
                    .update("pujoVisits", FieldValue.increment(1));
        }

        //setup profile
        if(uid!=null) {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(uid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                BaseUserModel baseUserModel = task.getResult().toObject(BaseUserModel.class);
                                name = baseUserModel.getName();
                                PName.setText(name);
                                dp = baseUserModel.getDp();
                                address = baseUserModel.getAddressline();
                                city = baseUserModel.getCity();
                                state = baseUserModel.getState();
                                if(baseUserModel.getPin()!=null && !baseUserModel.getPin().isEmpty()) {
                                    pin=baseUserModel.getPin();
                                }
                                String fulladd = address+"\n"+city+" , "+state+" - "+pin;
                                Paddress.setText(fulladd);
                                coverpic = baseUserModel.getCoverpic();
                                if(dp!=null){
//
                                    Picasso.get().load(dp).placeholder(R.drawable.image_background_grey).into(PDp);
                                }
                                else{
                                    Display display = getWindowManager().getDefaultDisplay();
                                    int displayWidth = display.getWidth();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;
                                    BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
                                    int width = options.outWidth;
                                    if (width > displayWidth) {
                                        int widthRatio = Math.round((float) width / (float) displayWidth);
                                        options.inSampleSize = widthRatio;
                                    }
                                    options.inJustDecodeBounds = false;
                                    Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.durga_ma, options);
                                    PDp.setImageBitmap(scaledBitmap);
                                }

                                if(coverpic!=null){
                                    Picasso.get().load(coverpic).placeholder(R.drawable.image_background_grey).into(Pcoverpic);
                                }
                                else{
                                    Display display = getWindowManager().getDefaultDisplay();
                                    int displayWidth = display.getWidth();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;
                                    BitmapFactory.decodeResource(getResources(), R.drawable.dhaki_png, options);
                                    int width = options.outWidth;
                                    if (width > displayWidth) {
                                        int widthRatio = Math.round((float) width / (float) displayWidth);
                                        options.inSampleSize = widthRatio;
                                    }
                                    options.inJustDecodeBounds = false;
                                    Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dhaki_png, options);
                                    Pcoverpic.setImageBitmap(scaledBitmap);
                                }

                                //metrics
                                visits.setText(baseUserModel.getPujoVisits()+"");
                                likes.setText(baseUserModel.getLikeCount()+"");
                                //metrics

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(uid)
                                        .collection("com")
                                        .document(uid)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful())
                                                {
                                                    PujoCommitteeModel model = task.getResult().toObject(PujoCommitteeModel.class);
                                                    pujotype=model.getType();
                                                    PUsername.setText(pujotype);
                                                    if(model.getDescription()!=null && !model.getDescription().isEmpty()){
                                                        desc=model.getDescription();
                                                        PDetaileddesc.setText(desc);
                                                    }
                                                }
                                                else{
                                                    BasicUtility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                                                }

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                BasicUtility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                                            }
                                        });

                            }
                            else{
                                BasicUtility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                BasicUtility.showToast(ActivityProfileCommittee.this,"Something went wrong...");
                            }
                        });
        }

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cm.getActiveNetworkInfo() != null) {
                    String location = name+","+address+","+city+","+state+"-"+pin;
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+Uri.encode(location)+"&mode=w");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
                else {
                    Toast.makeText(ActivityProfileCommittee.this, "Please check your internet connection and try again...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ProfileAdapter profileAdapter = new ProfileAdapter(getSupportFragmentManager());
        profileAdapter.addFragment(new Fragment_Posts(uid), "Posts");
        profileAdapter.addFragment(new Fragment_Reels(uid),"Clips");


        viewPager.setAdapter(profileAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
//                Fragment_Posts.swipe = 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ActivityProfileCommittee.this, storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE ) == (PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data!=null){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                try {
                    filePath = data.getData();
                    if(filePath!=null) {
                        if(imageCoverOrDp == 0){
                            CropImage.activity(filePath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setAspectRatio(1,1)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(ActivityProfileCommittee.this);
                        }
                        else {
                            CropImage.activity(filePath)
                                    .setActivityTitle("Crop Image")
                                    .setAllowRotation(TRUE)
                                    .setAllowCounterRotation(TRUE)
                                    .setAllowFlipping(TRUE)
                                    .setAutoZoomEnabled(TRUE)
                                    .setMultiTouchEnabled(FALSE)
                                    .setAspectRatio(16,9)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .start(ActivityProfileCommittee.this);
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
                ByteArrayOutputStream baos =new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                pic = baos.toByteArray();

                /////////////COMPRESS AND UPDATE//////////////
                new ImageCompressor().execute();
                /////////////COMPRESS AND UPDATE//////////////

            }
            else {//CROP ERROR
                Toast.makeText(this, "+error", Toast.LENGTH_SHORT).show();
            }
            ////////////////////////CROP//////////////////////

        }

    }

    //////////////////////PREMISSIONS//////////////////////////

    private void pickGallery(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),IMAGE_PICK_GALLERY_CODE);
    }

    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private final float maxHeight = 1080.0f;
        private final float maxWidth = 720.0f;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ActivityProfileCommittee.this);
            progressDialog.setTitle("Updating Profile");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        public byte[] doInBackground(Void... strings) {
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

            if(bmp!=null)
            {
                bmp.recycle();
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            byte[] by = out.toByteArray();
            return by;
        }

        @Override
        protected void onPostExecute(byte[] picCompressed) {
            if(picCompressed!= null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(picCompressed, 0 ,picCompressed.length);
                if(imageCoverOrDp == 0){
                    PDp.setImageBitmap(bitmap);
                }
                else {
                    Pcoverpic.setImageBitmap(bitmap);
                }
//                appBarImage.setImageBitmap(bitmap);
                pic = picCompressed;
                FirebaseStorage storage;
                StorageReference storageReference;
                StorageReference reference;
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference();

                if(imageCoverOrDp == 1){
                    reference = storageReference.child("Profile/")
                            .child(FirebaseAuth.getInstance().getUid()+"/")
                            .child( FirebaseAuth.getInstance().getUid()+"_cover");
                }
                else {
                    reference = storageReference.child("Profile/")
                            .child(FirebaseAuth.getInstance().getUid()+"/")
                            .child( FirebaseAuth.getInstance().getUid()+"_dp");
                }

                reference.putBytes(picCompressed)
                        .addOnSuccessListener(taskSnapshot ->
                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    Uri downloadUri = uri;
                                    String generatedFilePath = downloadUri.toString();
                                    DocumentReference docref = FirebaseFirestore.getInstance()
                                            .collection("Users").document(FirebaseAuth.getInstance().getUid());
                                    if(imageCoverOrDp == 0){
                                        docref.update("dp", generatedFilePath).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                introPref.setDefaultdp(generatedFilePath);
                                                progressDialog.dismiss();
                                            }else{
                                                BasicUtility.showToast(getApplicationContext(),"Something went wrong.");
                                            }
                                        });
                                    }
                                    else {
                                        docref.update("coverpic", generatedFilePath).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                            }else{
                                                BasicUtility.showToast(getApplicationContext(),"Something went wrong.");
                                            }
                                        });
                                    }

                                }))

                        .addOnFailureListener(e -> {
                            BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                            progressDialog.dismiss();

                        });

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
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }

            return inSampleSize;
        }

    }

}