package com.applex.utsav;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.adapters.MultipleImageAdapter;
import com.applex.utsav.adapters.TagAdapter;
import com.applex.utsav.adapters.TagAdapter2;
import com.applex.utsav.adapters.ViewmoreSliderAdapter;
import com.applex.utsav.fragments.CommitteeFragment;
import com.applex.utsav.fragments.FeedsFragment;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.PujoTagModel;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.models.TagModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.dialogs.BottomTagsDialog;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.StoreTemp;
import com.applex.utsav.videoCompressor.VideoCompress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;

import static com.applex.utsav.utility.BasicUtility.tsLong;

public class NewPostHome extends AppCompatActivity implements BottomTagsDialog.BottomSheetListener {

    private ArrayList<TagModel> selected_tags;

    private TextView postusername;
    private Button post;
    private LinearLayout cam, gallery,  videopost, videocam;
    private RelativeLayout addToPost;
    private EditText postcontent, edtagtxt, head_content;
    private ImageView cross, user_image, video_cam_icon, video_gal_icon;
    private ImageView postimage;
    private LinearLayout customTag, icons;
    private PujoTagModel pujoTag;

    private ApplexLinkPreview LinkPreview;
    private IntroPref introPref;
    private RecyclerView recyclerView;

    private String textdata = "";
    private RecyclerView tags_selectedRecycler;
    private TagAdapter2 tagAdapter2;
    private ImageCompressor imageCompressor;

    private Uri filePath, finalUri, videoUri;
    private ArrayList<byte[]> imagelist = new ArrayList<>();
    private StorageReference storageReferenece;
    private ArrayList<String> generatedFilePath = new ArrayList<>();

    private Uri downloadUri;
    private String ts, USERNAME, PROFILEPIC, GENDER;

    private FirebaseAuth mAuth;
    private FirebaseUser fireuser;

    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;
    private static final int VIDEO_PICK_GALLERY_CODE = 3000;
    private static final int VIDEO_PICK_CAMERA_CODE = 4000;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int TAG_PUJO = 100;


    String[] cameraPermission;
    String[] storagePermission;
    private byte[] pic;
    private ProgressDialog progressDialog;

    private RelativeLayout container_image;
    private VideoView videoView;
    private int duration;

    private ReelsPostModel reelsPostModel;
    private HomePostModel homePostModel, editPostModel;
    private DocumentReference docRef;
    private byte[] frame;
    private FrameLayout videoframe;

    private TextView tagPujo;

    private LinearLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private ArrayList<String> tagList;

    Context context;
    Resources resources;

    @SuppressLint("WrongThread")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPref = new IntroPref(NewPostHome.this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_new_post);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        postusername = findViewById(R.id.post_username);
        user_image = findViewById(R.id.user_image99);
        cross = findViewById(R.id.cross99);
        cam= findViewById(R.id.camera);
        gallery = findViewById(R.id.Photos);
        postcontent = findViewById(R.id.post_content);
        postimage = findViewById(R.id.post_image);
        post = findViewById(R.id.post);
        recyclerView = findViewById(R.id.recyclerimages);
        icons = findViewById(R.id.icons);
        container_image = findViewById(R.id.image_container);
        LinkPreview = findViewById(R.id.LinkPreView);
        videoView = findViewById(R.id.videoview);
        videopost = findViewById(R.id.Video);
        videocam = findViewById(R.id.Recorder);
        customTag = findViewById(R.id.tag);
        addToPost = findViewById(R.id.add_to_post);
        videoframe = findViewById(R.id.videoframe);
        video_cam_icon = findViewById(R.id.video_cam_icon);
        video_gal_icon = findViewById(R.id.video_gal_icon);
        head_content = findViewById(R.id.post_headline);

        mAuth = FirebaseAuth.getInstance();
        fireuser = mAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReferenece = storage.getReference();

        tags_selectedRecycler = findViewById(R.id.tags_selectedList) ;
        selected_tags = new ArrayList<>();
        tagPujo = findViewById(R.id.pujo_tag);
        TextView newPostToolb= findViewById(R.id.new_post_toolb);

        tagList = new ArrayList<>();

        buildRecyclerView_selectedtags();

        ///////////////////LOADING CURRENT USER DP AND UNAME//////////////////////

        if(introPref.getType().matches("com")) { //committee
            tagPujo.setVisibility(View.GONE);
            videopost.setVisibility(View.VISIBLE);
        }
        else if(introPref.getType().matches("indi")) { //indi
            tagPujo.setVisibility(View.VISIBLE);
            head_content.setVisibility(View.GONE);
        }

        // get the bottom sheet view
        llBottomSheet = findViewById(R.id.new_post_bottomsheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //icons.setVisibility(View.GONE);


        addToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else{
                    bottomSheetBehavior.setState(STATE_COLLAPSED);
                }

            }
        });

        introPref = new IntroPref(NewPostHome.this);
        USERNAME = introPref.getFullName();
        postusername.setText(USERNAME);

        GENDER = introPref.getGender();
        PROFILEPIC = introPref.getUserdp();
        if(PROFILEPIC!= null){
            Picasso.get().load(PROFILEPIC).into(user_image);
        }
        else{
            if(GENDER!=null){
                if (GENDER.matches("Female") || GENDER.matches("মহিলা")){
                    user_image.setImageResource(R.drawable.ic_female);
                }
                else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")){
                    user_image.setImageResource(R.drawable.ic_male);
                }
                else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")){
                    user_image.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
            else{
                user_image.setImageResource(R.drawable.ic_account_circle_black_24dp);
            }
        }


        ///////////////////LOADING CURRENT USER DP AND UNAME//////////////////////

        editPostModel= new HomePostModel();
        reelsPostModel = new ReelsPostModel();
        homePostModel = new HomePostModel();

        ///////////////SHARED CONTENT///////////////
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(type == null && intent.getStringExtra("target")!=null){
            List<String> postingIn = new ArrayList<>();

            if(intent.getStringExtra("target").matches("1")){
                postingIn.add("Your Campus");
                postingIn.add("Global");
            }
            else if(intent.getStringExtra("target").matches("2")){
                tagPujo.setVisibility(View.VISIBLE);
                postingIn.add("Global");
                postingIn.add("Your Campus");
            }

            else if(intent.getStringExtra("target").matches("11")){ //Challenge
                postingIn.add("Global");
                //post_anon.setVisibility(View.GONE);
                //info.setVisibility(View.GONE);

            }

            else if(intent.getStringExtra("target").matches("4")){ //Community
                postingIn.add(intent.getStringExtra("comName"));
//                postingIn.add("Your Campus");
//                postingIn.add("Global");
                //post_anon.setVisibility(View.GONE);
               // info.setVisibility(View.GONE);

            }

            if(intent.getStringExtra("target").matches("100")){// EDIT POST

                if(intent.getStringExtra("headline")!=null && introPref.getType().matches("com"))
                {
                    head_content.setVisibility(View.VISIBLE);
                    head_content.setText(intent.getStringExtra("headline"));
                }
                if(intent.getStringExtra("txt")!=null)
                {
                    postcontent.setVisibility(View.VISIBLE);
                    postcontent.setText(intent.getStringExtra("txt"));
                }
                Bundle args = getIntent().getBundleExtra("BUNDLETAGS");
                if (args != null) {
                    if ((ArrayList<String>) args.getSerializable("ARRAYLISTTAGS") != null
                            && ((ArrayList<String>) args.getSerializable("ARRAYLISTTAGS")).size() > 0) {

                        tagList = (ArrayList<String>) args.getSerializable("ARRAYLISTTAGS");

                        if (tagList != null && tagList.size() > 0) {
                            tags_selectedRecycler.setHasFixedSize(true);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                            tags_selectedRecycler.setLayoutManager(linearLayoutManager);
                            tags_selectedRecycler.setItemAnimator(new DefaultItemAnimator());
                            //  selected_tags = new ArrayList<>();

                            tagAdapter2 = new TagAdapter2(tagList, getApplicationContext());
                            tags_selectedRecycler.setAdapter(tagAdapter2);

                            tagAdapter2.onClickListener((position, tag) -> {
                                Toast.makeText(getApplicationContext(), "Long Press to remove tag", Toast.LENGTH_SHORT).show();
                            });

                            tagAdapter2.onLongClickListener((position, tag_name) ->{
//            TagModel tagModel = new TagModel();
//            tagModel.setName_tag(tag_name);

                                tagList.remove(position);
                                tagAdapter2.notifyItemRemoved(position);

                                if(tagList.size()==0)
                                    tags_selectedRecycler.setVisibility(View.GONE);

                                //   tagAdapter.notifyDataSetChanged();
                            });

                        }
//                        else {
//                            sliderView.setVisibility(View.GONE);
//                        }

                    }
                }
                if(StoreTemp.getInstance().getPujoTagModel()!=null)
                {
                    tagPujo.setVisibility(View.VISIBLE);
                    pujoTag = StoreTemp.getInstance().getPujoTagModel();
                    tagPujo.setText(StoreTemp.getInstance().getPujoTagModel().getPujoName());
                }
                //post_anon.setVisibility(View.GONE);
//                if(intent.getStringExtra("usN")!=null){
//                    editPostModel.setUsN(intent.getStringExtra("usN"));
//                    postusername.setText(editPostModel.getUsN());
//
//                }
//
//                if(intent.getStringExtra("dp")!=null)
//                    editPostModel.setDp(intent.getStringExtra("dp"));
//
//                if(intent.getStringExtra("uid")!=null)
//                    editPostModel.setUid(intent.getStringExtra("uid"));
//
//                if(intent.getStringExtra("bool")!=null){
//                    if(intent.getStringExtra("bool").matches("0")||intent.getStringExtra("bool").matches("2")){
//                        postingIn.add("Global");
//                    }
//                    else if(intent.getStringExtra("bool").matches("3")){
//                        postingIn.add("Your Campus");
//                    }
//                }
//
//                if(intent.getStringExtra("challengeID")!=null){
//                    postingIn.add("Global");
//
//                    //post_anon.setVisibility(View.GONE);
//                    //info.setVisibility(View.GONE);
//                    editPostModel.setChallengeID(intent.getStringExtra("challengeID"));
//                }
//
//                if(intent.getSerializableExtra("reportL")!=null)
//                    editPostModel.setReportL((ArrayList<String>) intent.getSerializableExtra("reportL"));
//
//                if(intent.getStringExtra("docID")!=null)
//                    editPostModel.setDocID(intent.getStringExtra("docID"));
//
//                if(intent.getStringExtra("type")!=null)
//                    editPostModel.setType(intent.getStringExtra("type"));
//
//                if(intent.getStringExtra("likeCheck")!=null)
//                    editPostModel.setLikeCheck(Integer.parseInt(intent.getStringExtra("likeCheck")));
//
//                if(intent.getSerializableExtra("likeL")!=null)
//                    editPostModel.setLikeL((ArrayList<String>) intent.getSerializableExtra("likeL"));
//
//                if(intent.getStringExtra("cmtNo")!=null)
//                    editPostModel.setCmtNo(Long.parseLong(intent.getStringExtra("cmtNo")));
//
//                if((StoreTemp.getInstance().getTagTemp())!=null){
//                    editPostModel.setTagL(StoreTemp.getInstance().getTagTemp());
//                    tags_selectedRecycler.setVisibility(View.VISIBLE);
//                    selected_tags= editPostModel.getTagL();
//
//                    tagAdapter2.notifyDataSetChanged();
//                    buildRecyclerView_selectedtags();
//                }
//
//
//                if(intent.getStringExtra("txt")!=null){
//                    editPostModel.setTxt(intent.getStringExtra("txt"));
//                    postcontent.setText(editPostModel.getTxt());
//                }
//
//                if(intent.getStringExtra("comID")!=null){
//                    postingIn.add(intent.getStringExtra("comName"));
//                    //post_anon.setVisibility(View.GONE);
//                    //info.setVisibility(View.GONE);
//                    editPostModel.setComID(intent.getStringExtra("comID"));
//                }
//
//
//                if(intent.getStringExtra("comName")!=null)
//                    editPostModel.setComName(intent.getStringExtra("comName"));
//
//                if(intent.getStringExtra("ts")!=null)
//                    editPostModel.setTs(Long.parseLong(intent.getStringExtra("ts")));
//
//                if(intent.getStringExtra("newTs")!=null)
//                    editPostModel.setNewTs(Long.parseLong(intent.getStringExtra("newTs")));

            }


        }

        ///////////////////SHARED CONTENT////////////////////
        if(Intent.ACTION_SEND.equals(action) && type != null) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);

            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    postcontent.setText(sharedText);
                    if(postcontent.getUrls().length>0){
                        URLSpan urlSnapItem = postcontent.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if(url!= null && url.contains("http")){
                            LinkPreview.setLink(url ,new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) { }

                                @Override
                                public void onError(Exception e) {
                                }
                            });
                        }
                    }
                }
            }
            else if (type.startsWith("image/")) {
                filePath = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                finalUri = filePath;
                ExifInterface ei = null;
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), finalUri);
                    ei = new ExifInterface(Objects.requireNonNull(getContentResolver().openInputStream(finalUri)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int orientation = Objects.requireNonNull(ei).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap rotatedBitmap;
                switch(orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }

                new ImageCompressor(rotatedBitmap).execute();

            }
            else if (type.startsWith("video/")) {
                videoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                BasicUtility.saveVideo(videoUri, NewPostHome.this);

                final String[] filePath = {getExternalFilesDir(null) + "/Utsav/" + "VID-" + tsLong + ".mp4"};
                final ProgressDialog[] progressDialog = new ProgressDialog[1];

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(NewPostHome.this, videoUri);
                String mVideoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long mTimeInMilliseconds= Long.parseLong(Objects.requireNonNull(mVideoDuration));
                duration = (int)mTimeInMilliseconds/1000;
                Bitmap bitmap = retriever.getFrameAtTime(2000);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 80, out);
                frame = out.toByteArray();

                final long[] size = {new File(filePath[0]).length() / (1024 * 1024)};

                if(size[0] < 100) {
                    VideoCompress.compressVideoLow(filePath[0], filePath[0].replace(".mp4", "_compressed.mp4"), new VideoCompress.CompressListener() {
                        @Override
                        public void onStart() {
                            //Start Compress
                            progressDialog[0] = new ProgressDialog(NewPostHome.this);
                            progressDialog[0].setTitle("Preparing your video");
                            progressDialog[0].setMessage("Please wait...");
                            progressDialog[0].setCancelable(false);
                            progressDialog[0].show();
                        }

                        @Override
                        public void onSuccess() {
                            //Finish successfully
//                            filePath[0] = filePath[0].replace(".mp4", "_compressed.mp4");
//                            size[0] = new File(filePath[0]).length()/(1024*1024);
                            if(progressDialog[0] != null && progressDialog[0].isShowing()) {
                                progressDialog[0].dismiss();
                            }

                            videoframe.setVisibility(View.VISIBLE);
                            videoView.setVideoURI(Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4"))));
                            videoView.start();
                            videoUri = Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4")));

                            MediaController mediaController = new MediaController(NewPostHome.this);
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                        }

                        @Override
                        public void onFail() {
                            //Failed
                        }

                        @Override
                        public void onProgress(float percent) {
                            //Progress
                        }
                    });
                }
                else {
                    BasicUtility.showToast(getApplicationContext(), "Video size too large");
                }

            }
        }
        else if(Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);

            if (type.startsWith("image/")) {
                ArrayList<Uri> sharedImages2 = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if(sharedImages2 != null) {
                    for(Uri uri : sharedImages2){
                        ExifInterface ei = null;
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            ei = new ExifInterface(Objects.requireNonNull(getContentResolver().openInputStream(uri)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation = Objects.requireNonNull(ei).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                        Bitmap rotatedBitmap;
                        switch(orientation) {

                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotatedBitmap = rotateImage(bitmap, 90);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotatedBitmap = rotateImage(bitmap, 180);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotatedBitmap = rotateImage(bitmap, 270);
                                break;

                            case ExifInterface.ORIENTATION_NORMAL:
                            default:
                                rotatedBitmap = bitmap;
                        }

                        new ImageCompressor(rotatedBitmap).execute();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                }
            }
        }

            //////////////////SHARED CONTENT///////////////////

        ///////////////////////IMAGE HANDLING////////////////////////
        gallery.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            }
            else {
                pickGallery();
            }
        });

        cam.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            }
            else {
                pickCamera();
            }
        });

        videopost.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            }
            else {
                pickVideo();
            }
        });

        videocam.setOnClickListener(v -> {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            }
            else {
                pickVideoCam();
            }
        });

        ///////////////////////IMAGE HANDLING////////////////////////

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        ///////////////////////POST////////////////////////
        post.setOnClickListener(v -> {
            if(InternetConnection.checkConnection(getApplicationContext())){
                String text_content = postcontent.getText().toString();
                String headline = head_content.getText().toString();

                if(introPref.getType().matches("com") && (imagelist.size() == 0 && videoUri == null)
                        && (text_content.trim().isEmpty()) && (headline.trim().isEmpty())) {
                    BasicUtility.showToast(getApplicationContext(),"Post has got no picture or video or text...");
                }
                else if(introPref.getType().matches("com") && (videoUri != null) && (headline.trim().isEmpty())) {
                    BasicUtility.showToast(getApplicationContext(),"Please give a headline while posting a video...");
                }
                else if(introPref.getType().matches("com") && (!headline.trim().isEmpty())
                        && (imagelist.size()==0 && videoUri == null && text_content.trim().isEmpty())) {
                    BasicUtility.showToast(getApplicationContext(),"Post should contain picture or video along with headline...");
                }
                else if(introPref.getType().matches("com") && (!headline.trim().isEmpty())
                        && (imagelist.size()==0 && videoUri == null) && (!text_content.trim().isEmpty())) {
                    BasicUtility.showToast(getApplicationContext(),"Post should contain picture or video...");
                }
                else if(introPref.getType().matches("com") && (headline.trim().isEmpty())
                        && (imagelist.size()==0 && videoUri == null) && (!text_content.trim().isEmpty())) {
                    BasicUtility.showToast(getApplicationContext(),"Post should contain picture or video...");
                }
                else if(introPref.getType().matches("indi") && text_content.trim().isEmpty() && imagelist.size() == 0 && videoUri==null){
                    BasicUtility.showToast(getApplicationContext(),"Post has got nothing...");
                }
                else{
                    if(intent.getStringExtra("target")!=null && intent.getStringExtra("target").matches("100")){
                        progressDialog = new ProgressDialog(NewPostHome.this);
                        progressDialog.setTitle("Saving changes");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        if(introPref.getType().matches("indi")){
                            FeedsFragment.changed=1;
                            ActivityProfileUser.change=1;
                        }
                        else if(introPref.getType().matches("com")){
                            CommitteeFragment.changed=1;
                        }

                        if(intent.getStringExtra("typeofpost")!= null && intent.getStringExtra("typeofpost").matches("notreel"))
                        {
                            if(intent.getStringExtra("docID")!=null)
                            FirebaseFirestore.getInstance().collection("Feeds")
                                    .document(intent.getStringExtra("docID"))
                                    .update("tagList", tagList, "headline", headline, "txt", text_content, "pujoTag", pujoTag)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(getApplicationContext(), "Post edited", Toast.LENGTH_LONG).show();
                                                progressDialog.dismiss();
//                                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
//                                            finish();
                                                if (isTaskRoot()) {
                                                    startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    NewPostHome.super.onBackPressed();
                                                }
                                            }
                                            else {
                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Post edit failed", Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                        else if(intent.getStringExtra("typeofpost")!= null && intent.getStringExtra("typeofpost").matches("reel"))
                        {
                            if(intent.getStringExtra("docID")!=null)
                                FirebaseFirestore.getInstance().collection("Reels")
                                        .document(intent.getStringExtra("docID"))
                                        .update("tagList", tagList, "headline", headline, "description", text_content, "pujoTag", pujoTag)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(getApplicationContext(), "Post edited", Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
//                                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
//                                            finish();
                                                    if (isTaskRoot()) {
                                                        startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                        finish();
                                                    } else {
                                                        NewPostHome.super.onBackPressed();
                                                    }
                                                }
                                                else {
                                                    BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Post edit failed", Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }
                                });
                        }

//                        docRef = firebaseFirestore.collection("Feeds").document(editPostModel.getDocID());

//                        ts = Long.toString(editPostModel.getTs());

//                        if(selected_tags!= null && selected_tags.size()>0 ) {
//                            editPostModel.setTagL(selected_tags);
//                        }
//                        if(!text_content.isEmpty()) {
//                            editPostModel.setTxt(text_content.trim());
//                        }

//                        if (imagelist != null && imagelist.size() > 0) {
//                            for (int j = 0; j < imagelist.size(); j++) {
//                                Long tsLong = System.currentTimeMillis();
//                                ts = tsLong.toString();
//                                StorageReference reference = storageReferenece.child("Feeds/").child(fireuser.getUid()+"/").child(ts+j + "post_img");
//
//                                int finalJ = j;
//                                reference.putBytes(imagelist.get(finalJ))
//                                        .addOnSuccessListener(taskSnapshot ->
//                                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
//                                                    downloadUri = uri;
//                                                    generatedFilePath.add(downloadUri.toString());
//                                                    Log.i("count", generatedFilePath.size() + " " + imagelist.size());
//                                                    if (generatedFilePath.size() == imagelist.size()) {
//                                                        homePostModel.setImg(generatedFilePath);
//                                                        docRef.set(homePostModel).addOnCompleteListener(task -> {
//                                                            if(task.isSuccessful()){
//                                                                progressDialog.dismiss();
//                                                                if(isTaskRoot()){
//                                                                    startActivity(new Intent(NewPostHome.this, MainActivity.class));
//                                                                }
//                                                                else if(intent.getStringExtra("FromViewMoreHome")!=null){
//                                                                    Intent i= new Intent(NewPostHome.this, ViewMoreHome.class);
//                                                                    i.putExtra("username", editPostModel.getUsN());
//                                                                    i.putExtra("userdp", editPostModel.getDp());
//                                                                    i.putExtra("docID", editPostModel.getDocID());
//                                                                    StoreTemp.getInstance().setTagTemp(editPostModel.getTagL());
//
//                                                                    i.putExtra("likeL", editPostModel.getLikeL());
//                                                                    i.putExtra("postPic", editPostModel.getImg());
//                                                                    i.putExtra("postText", editPostModel.getTxt());
//                                                                    i.putExtra("bool", "3");
//                                                                    i.putExtra("commentNo", Long.toString(editPostModel.getCmtNo()));
//
//                                                                    i.putExtra("uid", editPostModel.getUid());
//                                                                    i.putExtra("timestamp", Long.toString(editPostModel.getTs()));
//                                                                    i.putExtra("newTs", Long.toString(editPostModel.getNewTs()));
//                                                                    intent.putExtra("gender",editPostModel.getGender());
//                                                                    startActivity(i);
//                                                                    finish();
//
//                                                                }
//                                                                else {
//                                                                    NewPostHome.super.onBackPressed();
//                                                                }
//                                                            } else {
//                                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
//                                                            }
//                                                        });
//                                                    }
//                                                }))
//                                        .addOnFailureListener(e -> {
//                                            BasicUtility.showToast(getApplicationContext(), "Something went wrong");
//                                            if (progressDialog != null)
//                                                progressDialog.dismiss();
//                                        });
//                            }
//                        }
//                        else {
//                            editPostModel.setImg(null);
//                            docRef.set(editPostModel).addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    progressDialog.dismiss();
//
//                                    if (isTaskRoot()) {
//                                        startActivity(new Intent(NewPostHome.this, MainActivity.class));
//                                    } else if (intent.getStringExtra("FromViewMoreHome") != null) {
//                                        Intent i = new Intent(NewPostHome.this, ViewMoreHome.class);
//                                        i.putExtra("username", editPostModel.getUsN());
//                                        i.putExtra("userdp", editPostModel.getDp());
//                                        i.putExtra("docID", editPostModel.getDocID());
//                                        StoreTemp.getInstance().setTagTemp(editPostModel.getTagL());
//
//                                        i.putExtra("likeL", editPostModel.getLikeL());
//                                        i.putExtra("postPic", editPostModel.getImg());
//                                        i.putExtra("postText", editPostModel.getTxt());
//                                        i.putExtra("bool", "3");
//                                        i.putExtra("commentNo", Long.toString(editPostModel.getCmtNo()));
//
//                                        i.putExtra("uid", editPostModel.getUid());
//                                        i.putExtra("timestamp", Long.toString(editPostModel.getTs()));
//                                        i.putExtra("newTs", Long.toString(editPostModel.getNewTs()));
//                                        i.putExtra("gender",editPostModel.getGender());
//                                        startActivity(i);
//                                        finish();
//
//                                    } else {
//                                        NewPostHome.super.onBackPressed();
//                                    }
//
//                                } else {
//                                    BasicUtility.showToast(getApplicationContext(), "Something went wrong.");
//
//                                }
//                            });
//                        }
                    }
                    else {
                        long timestampLong = System.currentTimeMillis();
//                        ts = tsLong.toString();
                        progressDialog = new ProgressDialog(NewPostHome.this);
                        progressDialog.setTitle("Uploading");
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        if(introPref.getType().matches("indi")){
                            FeedsFragment.changed=1;
                            ActivityProfileUser.change=1;
                        }
                        else if(introPref.getType().matches("com")){
                            CommitteeFragment.changed=1;
                        }

                        if(videoUri != null) {
                            docRef = firebaseFirestore.collection("Reels").document(String.valueOf(timestampLong));

                            reelsPostModel.setCommittee_name(introPref.getFullName());
                            reelsPostModel.setCommittee_dp(introPref.getUserdp());
                            reelsPostModel.setType(introPref.getType());
                            reelsPostModel.setTs(timestampLong);
                            reelsPostModel.setNewTs(timestampLong);
                            reelsPostModel.setDocID(String.valueOf(timestampLong));
                            reelsPostModel.setGender(GENDER);
                            reelsPostModel.setUid(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            reelsPostModel.setTagList(tagList);

                            if(selected_tags!= null && selected_tags.size()>0 ) {
                                reelsPostModel.setTagL(selected_tags);
                            }

                            if(duration % 60 < 10) {
                                reelsPostModel.setDuration(duration / 60 + ":0" + duration % 60);
                            } else {
                                reelsPostModel.setDuration(duration / 60 + ":" + duration % 60);
                            }

                            if (!text_content.isEmpty()) {
                                reelsPostModel.setDescription(text_content.trim());
                            }

                            if(!head_content.getText().toString().isEmpty()) {
                                reelsPostModel.setHeadline(head_content.getText().toString().trim());
                            }

                            Long tsLong = System.currentTimeMillis();
                            ts = tsLong.toString();
                            StorageReference referenceVideo = storageReferenece.child("Reels/").child("Videos").child(fireuser.getUid()).child(ts + "_post_vid");
                            StorageReference referenceImage = storageReferenece.child("Reels/").child("Images").child(fireuser.getUid()).child(ts + "_post_img");

                            referenceImage.putBytes(frame)
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful()) {
                                            referenceImage.getDownloadUrl().addOnCompleteListener(task1 -> {
                                                reelsPostModel.setFrame(Objects.requireNonNull(task1.getResult()).toString());
                                            });
                                        }
                                        else {
                                            BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                                        }
                                    });

                            referenceVideo.putFile(videoUri)
                                    .addOnSuccessListener(taskSnapshot ->
                                            referenceVideo.getDownloadUrl().addOnSuccessListener(uri -> {
                                                downloadUri = uri;
                                                generatedFilePath.add(downloadUri.toString());
                                                reelsPostModel.setVideo(generatedFilePath.get(0));
                                                docRef.set(reelsPostModel).addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Post Created", Toast.LENGTH_LONG).show();
                                                        progressDialog.dismiss();
                                                        if (isTaskRoot()) {
                                                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                            finish();
                                                        } else {
                                                            NewPostHome.super.onBackPressed();
                                                        }
                                                    } else {
                                                        BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                    }
                                                });
                                            }))
                                    .addOnFailureListener(e -> {
                                        BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                                        if (progressDialog != null)
                                            progressDialog.dismiss();
                                    });
                        }
                        else {
                            docRef = firebaseFirestore.collection("Feeds").document();

                            homePostModel.setUsN(introPref.getFullName());

                            homePostModel.setDp(introPref.getUserdp());
                            if(getIntent().getStringExtra("target")!= null){
                                if(getIntent().getStringExtra("target").matches("11")){
                                    homePostModel.setChallengeID(getIntent().getStringExtra("challengeID"));
                                }
                                if(getIntent().getStringExtra("target").matches("4")){
                                    homePostModel.setComID(getIntent().getStringExtra("comID"));
                                    homePostModel.setComName(getIntent().getStringExtra("comName"));
                                }
                            }

                            homePostModel.setTs(timestampLong);
                            homePostModel.setNewTs(timestampLong);
                            homePostModel.setType(introPref.getType());
                            homePostModel.setGender(GENDER);
                            homePostModel.setTagList(tagList);

                            homePostModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            if(selected_tags!= null && selected_tags.size()>0 ) {
                                homePostModel.setTagL(selected_tags);
                            }
                            if(!text_content.isEmpty()) {
                                homePostModel.setTxt(text_content.trim());
                            }
                            if(!head_content.getText().toString().isEmpty()) {
                                homePostModel.setHeadline(head_content.getText().toString().trim());
                            }

                            if (imagelist != null && imagelist.size() > 0) {
                                for (int j = 0; j < imagelist.size(); j++) {
                                    Long tsLong = System.currentTimeMillis();
                                    ts = tsLong.toString();
                                    StorageReference reference = storageReferenece.child("Feeds/").child(fireuser.getUid()+"/").child(ts+j + "post_img");

                                    int finalJ = j;
                                    reference.putBytes(imagelist.get(finalJ))
                                            .addOnSuccessListener(taskSnapshot ->
                                                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        downloadUri = uri;
                                                        generatedFilePath.add(downloadUri.toString());
                                                        Log.i("count", generatedFilePath.size() + " " + imagelist.size());
                                                        if (generatedFilePath.size() == imagelist.size()) {
                                                            homePostModel.setImg(generatedFilePath);
                                                            docRef.set(homePostModel).addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getApplicationContext(), "Post Created", Toast.LENGTH_LONG).show();
                                                                    progressDialog.dismiss();
                                                                    if (isTaskRoot()) {
                                                                        startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                                        finish();
                                                                    } else {
                                                                        NewPostHome.super.onBackPressed();
                                                                    }
                                                                } else {
                                                                    BasicUtility.showToast(getApplicationContext(), "Something went wrong...");
                                                                }
                                                            });
                                                        }
                                                    }))
                                            .addOnFailureListener(e -> {
                                                BasicUtility.showToast(getApplicationContext(), "Something went wrong");
                                                if (progressDialog != null)
                                                    progressDialog.dismiss();
                                            });
                                }
                            }
                            else {
                                docRef.set(homePostModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Post Created", Toast.LENGTH_LONG).show();
                                            progressDialog.dismiss();
//                                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
//                                            finish();
                                            if (isTaskRoot()) {
                                                startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                                finish();
                                            } else {
                                                NewPostHome.super.onBackPressed();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Post creation failed", Toast.LENGTH_LONG).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Post creation failed", Toast.LENGTH_LONG).show();
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }
                    }
                }
            }
            else {
                BasicUtility.showToast(getApplicationContext(), "Network unavailable...");
            }
        });

        customTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        cross.setOnClickListener(v -> {
            String text_content = postcontent.getText().toString();

            if(text_content.isEmpty() && pic==null){
                if(isTaskRoot()){
                    startActivity(new Intent(NewPostHome.this, MainActivity.class));
                    finish();
                }else {
                    super.onBackPressed();
                }
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewPostHome.this);
                builder.setTitle("Are you sure?")
                        .setMessage("Changes will be discarded...")
                        .setPositiveButton("Sure", (dialog, which) -> {
                            if(isTaskRoot()){
                                startActivity(new Intent(NewPostHome.this, MainActivity.class));
                                finish();
                            }else {
                                super.onBackPressed();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show();
            }

        });

        tagPujo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(NewPostHome.this, ActivityTagPujo.class);
                startActivityForResult(intent1, TAG_PUJO);
            }
        });

        postcontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(STATE_COLLAPSED);
            }
        });


    }

    ////////////TAGS////////////////
    private void openDialog() {
        AlertDialog.Builder dialog= new AlertDialog.Builder(NewPostHome.this);
        LayoutInflater inflater= LayoutInflater.from(NewPostHome.this);
        View view=inflater.inflate(R.layout.dialog_tag_spinner,null);
        edtagtxt =view.findViewById(R.id.addtag);
        dialog.setView(view)
                .setTitle("Add Tag")
                .setNegativeButton("Cancel", (dialog12, which) ->
                        dialog12.dismiss())
                .setPositiveButton("Done", (dialog1, which) -> {
                    textdata = edtagtxt.getText().toString().trim();
                    if(textdata.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Empty tag", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        edtagtxt.setText("");

//                        ArrayList<String> TagColorArray = new ArrayList<>();
//                        TagColorArray.add("#f4b4ff");
//                        TagColorArray.add("#aaf1ff");
//                        TagColorArray.add("#ffdfad");
//                        TagColorArray.add("#bcffa2");
//                        TagColorArray.add("#cecbff");
//                        TagColorArray.add("#cfffef");
//                        TagColorArray.add("#ffc0bd");
//                        TagColorArray.add("#faff9c");
//                        TagColorArray.add("#7efdff");
//                        TagColorArray.add("#ffe87b");
//
//                        int pos= (int) (Math.random()* 10);
//                        colorValue= TagColorArray.get(pos);
//
//                        TagModel mytag = new TagModel();
//                        mytag.setName_tag(textdata);
//                        mytag.setColor_hex(colorValue);
//                        selected_tags.add(mytag);

                        tagList.add(textdata);

                        tagAdapter2.notifyDataSetChanged();
                        tags_selectedRecycler.setVisibility(View.VISIBLE);
                    }
                })
                .show();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void buildRecyclerView_selectedtags(){
        tags_selectedRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        tags_selectedRecycler.setLayoutManager(linearLayoutManager);
        tags_selectedRecycler.setItemAnimator(new DefaultItemAnimator());
      //  selected_tags = new ArrayList<>();

        tagAdapter2 = new TagAdapter2(tagList, getApplicationContext());
        tags_selectedRecycler.setAdapter(tagAdapter2);

        tagAdapter2.onClickListener((position, tag) -> {
            Toast.makeText(getApplicationContext(), "Long Press to remove tag", Toast.LENGTH_SHORT).show();
        });

        tagAdapter2.onLongClickListener((position, tag_name) ->{
//            TagModel tagModel = new TagModel();
//            tagModel.setName_tag(tag_name);

            tagList.remove(position);
            tagAdapter2.notifyItemRemoved(position);

            if(tagList.size()==0)
                tags_selectedRecycler.setVisibility(View.GONE);

            //   tagAdapter.notifyDataSetChanged();
        });


    }


    @Override
    public void onTagClicked(TagModel tagModel) {
//        tags_selectedRecycler.setVisibility(View.VISIBLE);
//        selected_tags.add(tagModel);
//        tagAdapter2.notifyDataSetChanged();
    }
    ////////////TAGS////////////////

    ///////////////////////HANDLE CAMERA AND GALLERY//////////////////////////
    private void pickGallery(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),IMAGE_PICK_GALLERY_CODE);
    }

    private void pickVideo(){
        Intent intent= new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"), VIDEO_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
        }
    }

    private void pickVideoCam() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == VIDEO_PICK_GALLERY_CODE) {
                videoUri = data.getData();
                bottomSheetBehavior.setState(STATE_COLLAPSED);
                BasicUtility.saveVideo(videoUri, NewPostHome.this);

                final String[] filePath = {getExternalFilesDir(null) + "/Utsav/" + "VID-" + tsLong + ".mp4"};
                final ProgressDialog[] progressDialog = new ProgressDialog[1];

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(NewPostHome.this, videoUri);
                String mVideoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long mTimeInMilliseconds= Long.parseLong(Objects.requireNonNull(mVideoDuration));
                duration = (int)mTimeInMilliseconds/1000;
                Bitmap bitmap = retriever.getFrameAtTime(2000);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 80, out);
                frame = out.toByteArray();

                final long[] size = {new File(filePath[0]).length() / (1024 * 1024)};

                if(size[0] < 100) {
                    VideoCompress.compressVideoLow(filePath[0], filePath[0].replace(".mp4", "_compressed.mp4"), new VideoCompress.CompressListener() {
                        @Override
                        public void onStart() {
                            //Start Compress
                            progressDialog[0] = new ProgressDialog(NewPostHome.this);
                            progressDialog[0].setTitle("Preparing your video");
                            progressDialog[0].setMessage("Please wait...");
                            progressDialog[0].setCancelable(false);
                            progressDialog[0].show();
                        }

                        @Override
                        public void onSuccess() {
                            //Finish successfully
//                            filePath[0] = filePath[0].replace(".mp4", "_compressed.mp4");
//                            size[0] = new File(filePath[0]).length()/(1024*1024);
                            if(progressDialog[0] != null && progressDialog[0].isShowing()) {
                                progressDialog[0].dismiss();
                            }

                            videoframe.setVisibility(View.VISIBLE);
                            videoView.setVideoURI(Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4"))));
                            videoView.start();
                            videoUri = Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4")));

                            MediaController mediaController = new MediaController(NewPostHome.this);
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                        }

                        @Override
                        public void onFail() {
                            //Failed
                        }

                        @Override
                        public void onProgress(float percent) {
                            //Progress
                        }
                    });
                }
                else {
                    BasicUtility.showToast(getApplicationContext(), "Video size too large");
                }
            }
            else if(requestCode == IMAGE_PICK_GALLERY_CODE) {
                if(data.getClipData()!= null) {
                    bottomSheetBehavior.setState(STATE_COLLAPSED);
                    int count = data.getClipData().getItemCount();
                    for (int i =0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        ExifInterface ei = null;
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            ei = new ExifInterface(Objects.requireNonNull(getContentResolver().openInputStream(imageUri)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation = Objects.requireNonNull(ei).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                        Bitmap rotatedBitmap;
                        switch(orientation) {

                            case ExifInterface.ORIENTATION_ROTATE_90:
                                rotatedBitmap = rotateImage(bitmap, 90);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_180:
                                rotatedBitmap = rotateImage(bitmap, 180);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_270:
                                rotatedBitmap = rotateImage(bitmap, 270);
                                break;

                            case ExifInterface.ORIENTATION_NORMAL:
                            default:
                                rotatedBitmap = bitmap;
                        }
                        new ImageCompressor(rotatedBitmap).execute();
                    }
                } else if (data.getData() != null) {
                    Bitmap bitmap = null;
                    ExifInterface ei = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        ei = new ExifInterface(Objects.requireNonNull(getContentResolver().openInputStream(data.getData())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int orientation = Objects.requireNonNull(ei).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotatedBitmap;
                    switch(orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(bitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(bitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(bitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = bitmap;
                    }
                    new ImageCompressor(rotatedBitmap).execute();
                }
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) Objects.requireNonNull(extras).get("data");
                bottomSheetBehavior.setState(STATE_COLLAPSED);
                new ImageCompressor(bitmap).execute();
            }
            else if(requestCode == VIDEO_PICK_CAMERA_CODE) {
                videoUri = data.getData();
                bottomSheetBehavior.setState(STATE_COLLAPSED);
                BasicUtility.saveVideo(videoUri, NewPostHome.this);

                final String[] filePath = {getExternalFilesDir(null) + "/Utsav/" + "VID-" + tsLong + ".mp4"};
                final ProgressDialog[] progressDialog = new ProgressDialog[1];

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(NewPostHome.this, videoUri);
                String mVideoDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long mTimeInMilliseconds= Long.parseLong(Objects.requireNonNull(mVideoDuration));
                duration = (int)mTimeInMilliseconds/1000;
                Bitmap bitmap = retriever.getFrameAtTime(2000);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 80, out);
                frame = out.toByteArray();

                final long[] size = {new File(filePath[0]).length() / (1024 * 1024)};

                if(size[0] < 100) {
                    VideoCompress.compressVideoLow(filePath[0], filePath[0].replace(".mp4", "_compressed.mp4"), new VideoCompress.CompressListener() {
                        @Override
                        public void onStart() {
                            //Start Compress
                            progressDialog[0] = new ProgressDialog(NewPostHome.this);
                            progressDialog[0].setTitle("Preparing your video");
                            progressDialog[0].setMessage("Please wait...");
                            progressDialog[0].setCancelable(false);
                            progressDialog[0].show();
                        }

                        @Override
                        public void onSuccess() {
                            if(progressDialog[0] != null && progressDialog[0].isShowing()) {
                                progressDialog[0].dismiss();
                            }

                            videoframe.setVisibility(View.VISIBLE);
                            videoView.setVideoURI(Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4"))));
                            videoView.start();
                            videoUri = Uri.fromFile(new File(filePath[0].replace(".mp4", "_compressed.mp4")));

                            MediaController mediaController = new MediaController(NewPostHome.this);
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                        }

                        @Override
                        public void onFail() {
                            //Failed
                        }

                        @Override
                        public void onProgress(float percent) {
                            //Progress
                        }
                    });
                }
                else {
                    BasicUtility.showToast(getApplicationContext(), "Video size too large");
                }
            }
            else if(requestCode == TAG_PUJO) {
                pujoTag = new PujoTagModel();
                pujoTag.setPujoName(data.getStringExtra("name"));
                pujoTag.setPujoUid(data.getStringExtra("uid"));
                pujoTag.setDp(data.getStringExtra("dp"));

                homePostModel.setPujoTag(pujoTag);
                editPostModel.setPujoTag(pujoTag);
                reelsPostModel.setPujoTag(pujoTag);

                tagPujo.setText(data.getStringExtra("name"));
            }
            ////////////////////////CROP//////////////////////
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                finalUri = result.getUri();
                bottomSheetBehavior.setState(STATE_COLLAPSED);

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), finalUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos =new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                pic = baos.toByteArray();
                new ImageCompressor(bitmap).execute();
            }
            else {//CROP ERROR
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            }
            ////////////////////////CROP//////////////////////
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }
    ///////////////////////HANDLE CAMERA AND GALLERY///////////////////////////

    //////////////////////PREMISSIONS//////////////////////////
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(NewPostHome.this, storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )== (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(NewPostHome.this, cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE )== (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted) {
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this,"permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted) {
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this,"permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //////////////////////PREMISSIONS//////////////////////////

    @SuppressLint("StaticFieldLeak")
    class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private Bitmap bitmap, compressedBitmap;
        private ProgressDialog progressDialog;

        public ImageCompressor(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(NewPostHome.this);
            progressDialog.setTitle("Preparing your images");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public byte[] doInBackground(Void... strings) {
            try {
                compressedBitmap = BasicUtility.decodeSampledBitmapFromFile(bitmap, 612, 816);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteArray = stream.toByteArray();
            compressedBitmap.recycle();
            imagelist.add(byteArray);
            return null;
        }

        @Override
        protected void onPostExecute(byte[] picCompressed) {
            if(progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if(imagelist != null && imagelist.size() > 0) {
                container_image.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setHasFixedSize(false);
                final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setItemViewCacheSize(10);
                recyclerView.setNestedScrollingEnabled(true);

                MultipleImageAdapter multipleImageAdapter = new MultipleImageAdapter(imagelist, getApplicationContext());
                recyclerView.setAdapter(multipleImageAdapter);
                multipleImageAdapter.onClickListener(position -> {
                    imagelist.remove(position);
                    multipleImageAdapter.notifyDataSetChanged();
                });
            } else {
                container_image.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        String text_content = postcontent.getText().toString();

        if(text_content.isEmpty() && pic==null){
            if(isTaskRoot()){
                startActivity(new Intent(NewPostHome.this, MainActivity.class));
                finish();
            }else {
                super.onBackPressed();
            }
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostHome.this);
            builder.setTitle("Are you sure?")
                    .setMessage("Changes will be discarded...")
                    .setPositiveButton("Sure", (dialog, which) -> {
                        if(isTaskRoot()){
                            startActivity(new Intent(NewPostHome.this, MainActivity.class));
                            finish();
                        }else {
                            super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(imageCompressor != null) {
            imageCompressor.cancel(true);
        }
    }
}