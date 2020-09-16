package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.style.URLSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.example.pujo360.LinkPreview.ApplexLinkPreview;
import com.example.pujo360.LinkPreview.ViewListener;
import com.example.pujo360.adapters.TagAdapter;
import com.example.pujo360.fragments.CommitteeFragment;
import com.example.pujo360.models.BaseUserModel;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.models.HomePostModel;
import com.example.pujo360.models.IndividualModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.StoreTemp;
import com.example.pujo360.util.Utility;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.lang.ref.WeakReference;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class ActivityProfileUser extends AppCompatActivity {

    private ImageView editprofile2;
    private FirebaseUser fireuser;
    private FloatingActionButton floatingActionButton;
    private ProgressDialog progressDialog;
    ///////////////POSTS////////////////
    private SwipeRefreshLayout swipeRefreshLayout;
    private Dialog postMenuDialog;
    private FirestorePagingAdapter adapter1;

    ///////////////POSTS////////////////
    private ProgressBar contentProgress;
    public static int delete = 0;
    public static int change = 0;
    private RecyclerView mRecyclerView;
    private ProgressBar progressMore;
    private DocumentReference documentReference;
    private BaseUserModel userModel;
    ///////////////POSTS////////////////

    //////////////NO POSTS///////////////
    private TextView PName,PUsername;
    private ImageView PDp, nopost1,PCoverpic;
    private LinearLayout instituteBG, noProfilePost, LL;

    //////////////NO POSTS///////////////
    private TextView profile;
    private String my_uid;
    int bool;

    private IntroPref introPref;

    ///Current user details from intropref
    private String USERNAME, PROFILEPIC, COVERPIC, FirstName, LastName, UserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        introPref = new IntroPref(this);
        contentProgress = findViewById(R.id.content_progress);
        progressMore = findViewById(R.id.progress_more);
        editprofile2 =findViewById(R.id.edit_profile2);

        fireuser = FirebaseAuth.getInstance().getCurrentUser();
        nopost1 = findViewById(R.id.no_recent_com_post1);
        floatingActionButton = findViewById(R.id.to_the_top_profile);
        LL = findViewById(R.id.LL);

        //////////////POSTS SETUP////////////////////
        swipeRefreshLayout= findViewById(R.id.swiperefresh1);
        mRecyclerView = findViewById(R.id.your_posts_recycler);
        //////////////POSTS SETUP////////////////////

        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////
        if(getIntent()!=null && getIntent().getStringExtra("uid")!=null){
            my_uid = getIntent().getStringExtra("uid");
            if(!my_uid.matches(fireuser.getUid())){
                bool =1;//ANOTHER USER ACCOUNT
            }
        }
        else{
            my_uid = fireuser.getUid();
            bool = 0;//CURRENT USER ACCOUNT
        }
        ///////////////CHECK UID TO SET VISIBILITY FOR THE EDIT PROFILE ACTIVITY///////////////

        //////////////RECYCLER VIEW////////////////////
        /////////////SETUP//////////////
        contentProgress.setVisibility(View.VISIBLE);
        mRecyclerView.setHasFixedSize(false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(ActivityProfileUser.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(20);

        buildRecycler();

        /////////////SETUP//////////////
        PROFILEPIC =  introPref.getUserdp();
        USERNAME = introPref.getFullName();
        ///////////////RECYCLER VIEW////////////////////


        /////////////WHEN THERE IS NO POST//////////////
        PDp = findViewById(R.id.Pdp_no_post);
        PName = findViewById(R.id.Profilename_noPost);
        PUsername = findViewById(R.id.Pusername_noPost);
        noProfilePost = findViewById(R.id.noProfilePost);
        PCoverpic=findViewById(R.id.coverpic);

        /////////////WHEN THERE IS NO POST//////////////


        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.toolbarStart),
                getResources().getColor(R.color.md_blue_500));
        swipeRefreshLayout.setOnRefreshListener(this::buildRecycler);

        final int[] scrollY = {0};
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollY[0] = scrollY[0] + dy;
                if (scrollY[0] <= 2000 && dy < 0) {
                    floatingActionButton.setVisibility(View.GONE);
                }
                else {
                    if(dy < 0){
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("ObjectAnimatorBinding")
                            @Override
                            public void onClick(View v) {
                                recyclerView.scrollToPosition(0);
//                            recyclerView.smoothScrollToPosition();
                                recyclerView.postDelayed(new Runnable() {
                                    public void run() {
                                        recyclerView.scrollToPosition(0);
                                    }
                                },300);
                                // ObjectAnimator.ofInt(recyclerView, "dy", 0).setDuration(1000).start();
                            }
                        });
                    } else {
                        floatingActionButton.setVisibility(View.GONE);
                    }
                }
            }
        });


    }


    private void buildRecycler(){
        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds/")
                .whereEqualTo("uid", my_uid)
                .orderBy("ts", Query.Direction.DESCENDING);

        FirebaseFirestore.getInstance()
                .collection("Feeds/")
                .whereEqualTo("uid", my_uid)
                .orderBy("ts", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        noPostView();
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    else {
                        LL.setVisibility(View.VISIBLE);
                        noProfilePost.setVisibility(View.GONE);
                        nopost1.setVisibility(View.GONE);
                    }
                }
            }
        });

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, new SnapshotParser<HomePostModel>() {
                    @NonNull
                    @Override
                    public HomePostModel parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        HomePostModel newPostModel = new HomePostModel();
                        if(snapshot.exists()) {
                            newPostModel = snapshot.toObject(HomePostModel.class);
                            newPostModel.setDocID(snapshot.getId());
                        }
                        if(bool != 0){
                            if(!newPostModel.getUsN().matches("Anonymous"))
                                return newPostModel;
                            else {
                                return null;
                            }
                        }
                        else {
                            return newPostModel;
                        }
                    }
                })
                .build();

        adapter1 = new FirestorePagingAdapter<HomePostModel, RecyclerView.ViewHolder>(options) {

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                if(viewType == 0){
                    LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                    View v = layoutInflater.inflate(R.layout.item_profile_info, viewGroup, false);
                    return new ProgrammingViewHolder1(v);
                }
                else {
                    LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                    View v = layoutInflater.inflate(R.layout.item_newpost_home, viewGroup, false);
                    return new ProgrammingViewHolder2(v);
                }

            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @Nullable HomePostModel currentItem) {
                if(holder.getItemViewType() == 0) {
                    ///////////////////////LOAD PROFILE DETAILS///////////////////////
                    ProgrammingViewHolder1 holder1 = (ProgrammingViewHolder1) holder;

                    holder1.PDp.setOnClickListener(v -> {
                        if(userModel != null) {
                            if (userModel.getDp() != null && userModel.getDp().length()>2) {
                                Intent intent = new Intent(ActivityProfileUser.this, ProfilePictureActivity.class);
                                intent.putExtra("Bitmap", userModel.getDp());
                                intent.putExtra("from", "profile");
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(ActivityProfileUser.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    holder1.Pcoverpic.setOnClickListener(v -> {
                        if(userModel != null) {
                            if (userModel.getCoverpic() != null) {
                                Intent intent = new Intent(ActivityProfileUser.this, ProfilePictureActivity.class);
                                intent.putExtra("from", "profile");
                                intent.putExtra("Bitmap", userModel.getCoverpic());
                                startActivity(intent);
                            }
                        }
                        else {
                            Toast.makeText(ActivityProfileUser.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                        }
                    });

                    documentReference= FirebaseFirestore.getInstance().collection("Users")
                            .document(my_uid);

                    documentReference.get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        if(task.getResult().exists()){
                                            userModel = task.getResult().toObject(BaseUserModel.class);
                                            FirebaseFirestore.getInstance().collection("Users")
                                                    .document(my_uid).collection("Individual").document(my_uid).get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            IndividualModel individualModel= task.getResult().toObject(IndividualModel.class);
                                                            FirstName= individualModel.getFirstname();
                                                            LastName = individualModel.getLastname();
                                                        }
                                                    });

                                            UserName= userModel.getName();

                                            holder1.PName.setText(FirstName+" "+LastName);
                                            holder1.PUsername.setText('@'+UserName);

                                            if(userModel.getDp()!=null){

                                                String DP = userModel.getDp();
                                                if(DP!=null){
                                                    Picasso.get().load(DP).placeholder(R.drawable.image_background_grey).into(holder1.PDp);
                                                }
                                                else{
                                                    Picasso.get().load(DP).into(holder1.PDp);
                                                }

                                            }
                                            else {
                                                holder1.PDp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                            }

                                            if(userModel.getCoverpic()!=null){
                                                COVERPIC = userModel.getCoverpic();
                                                Picasso.get().load(COVERPIC).placeholder(R.drawable.image_background_grey).into(holder1.Pcoverpic);
                                            }
                                            else {
//                                                Picasso.get().load(R.drawable.coverpicpng).into(holder1.Pcoverpic);
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
                                                Bitmap scaledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dhaki_png, options);
//                                                coverpic.setImageBitmap(scaledBitmap);
                                                holder1.Pcoverpic.setImageBitmap(scaledBitmap);
                                            }

                                            if(my_uid.matches(FirebaseAuth.getInstance().getUid())){
                                                editprofile2.setVisibility(View.VISIBLE);
                                                editprofile2.setOnClickListener(v -> {
                                                    Intent i1 = new Intent(ActivityProfileUser.this, EditProfileActivity.class);
                                                    i1.putExtra("firstname", FirstName);            i1.putExtra("lastname", LastName);
                                                    i1.putExtra("username", USERNAME);             i1.putExtra("profilepic",PROFILEPIC );

                                                    startActivity(i1);
                                                    finish();
                                                });
                                            }
                                        }
                                        else{
                                            //profile doesn't exist in database
                                            ActivityProfileUser.super.onBackPressed();
                                            Utility.showToast(ActivityProfileUser.this,"Profile is temporarily unavailable");
                                        }


                                    }
                                }

                            });
                    ///////////////////////LOAD PROFILE DETAILS///////////////////////


                     ////////////////FOR THE FIRST POST////////////////
                    DocumentReference likeStore;
                    if(currentItem != null) {
                        String timeAgo = Utility.getTimeAgo(currentItem.getTs());
                        holder1.minsago.setText(timeAgo);
                        if(timeAgo != null) {
                            if(timeAgo.matches("just now")) {
                                holder1.minsago.setTextColor(Color.parseColor("#00C853"));
                            }
                            else {
                                holder1.minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                        if(currentItem.getChallengeID()!=null){
                            holder1.itemHome.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary) ));
                            holder1.menuPost.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary) ));
                        }
                        else {
                            holder1.itemHome.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white) ));
                            holder1.menuPost.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white) ));
                        }

                        ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////

                        likeStore = FirebaseFirestore.getInstance()
                                .document("Home/Global/Feeds/"+currentItem.getDocID()+"/");

                        holder1.comName.setVisibility(View.GONE);


                        holder1.menuPost.setVisibility(View.VISIBLE);
                        ///////////SET DOCUMENT REFERENCE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////


                        ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                        if(PROFILEPIC!=null){
                            Picasso.get().load(PROFILEPIC).fit().centerCrop()
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .memoryPolicy(MemoryPolicy.NO_STORE)
                                    .into(holder1.profileimage);

                        }
                        else {
                            holder1.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }

                        ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                        ///////////TAGLIST///////////////
                        ///////////TAG RECYCLER SETUP////////////////
                        holder1.tagList.setHasFixedSize(false);
                        WeakReference<LinearLayoutManager> linearLayoutManager = new WeakReference<>(new LinearLayoutManager(getApplicationContext()));
                        linearLayoutManager.get().setOrientation(LinearLayoutManager.HORIZONTAL);
                        holder1.tagList.setNestedScrollingEnabled(true);
                        holder1.tagList.setLayoutManager(linearLayoutManager.get());
                        ///////////TAG RECYCLER SETUP////////////////
                        if(currentItem.getTagL()!=null && currentItem.getTagL().size()>0 ) {
                            holder1.tagList.setVisibility(View.VISIBLE);
                            TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL() , getApplicationContext());
                            holder1.tagList.setAdapter(tagAdapter);
                        }
                        else {
                            holder1.tagList.setAdapter(null);
                            holder1.tagList.setVisibility(View.GONE);
                        }
                        /////////TAGLIST///////////////


                        //////////////LOADING USERNAME AND USERDP FROM USERNODE FOR CURRENT POST USER///////////////

                            ////////////NORMAL POST///////////////
                            if(currentItem.getDp()!= null && !currentItem.getDp().isEmpty()){

                                    Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                                            .memoryPolicy(MemoryPolicy.NO_STORE)
                                            .into(holder1.userimage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    holder1.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                                }
                                            });

                            }
                            else {
                                holder1.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                            holder1.username.setText(currentItem.getUsN());



                        ///////////////OPEN VIEW MORE//////////////
                        holder1.itemHome.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());

                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("bool", Integer.toString(bool));
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });

                        //change
                        holder1.postimage.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());
                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());
                            //            intent.putExtra("tagL", currentItem.getTagL());
                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("bool", Integer.toString(bool));;

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });

                        holder1.flamedBy.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());
                            //            intent.putExtra("tagL", currentItem.getTagL());
                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("bool", Integer.toString(bool));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));

                            intent.putExtra("likeLOpen", "likeLOpen");
                            startActivity(intent);

                        });
                        ///////////////OPEN VIEW MORE//////////////

                        //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                        if(currentItem.getTxt()==null || currentItem.getTxt().isEmpty()){
                            holder1.text_content.setVisibility(View.GONE);
                            holder1.LinkPreview.setVisibility(View.GONE);
                            holder1.text_content.setText(null);
                        }
                        else{
                            holder1.text_content.setVisibility(View.VISIBLE);
                            holder1.text_content.setText(currentItem.getTxt());
                            if(holder1.text_content.getUrls().length>0){
                                URLSpan urlSnapItem = holder1.text_content.getUrls()[0];
                                String url = urlSnapItem.getURL();
                                if(url.contains("http")){
                                    holder1.LinkPreview.setVisibility(View.VISIBLE);
                                    holder1.LinkPreview.setLink(url ,new ViewListener() {
                                        @Override
                                        public void onSuccess(boolean status) {
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //do stuff like remove view etc
                                                    holder1.LinkPreview.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    });
                                }

                            } else {
                                holder1.LinkPreview.setVisibility(View.GONE);
                            }

                        }

                        String postimage_url = currentItem.getImg();
                        if(postimage_url!=null){
                            holder1.postimage.setVisibility(View.VISIBLE);
                            Picasso.get().load(postimage_url)
                                    .placeholder(R.drawable.image_background_grey)
                                    .memoryPolicy(MemoryPolicy.NO_STORE)
                                    .into(holder1.postimage);

                            holder1.postimage.setOnLongClickListener(v -> {

                                Picasso.get().load(postimage_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        save_Dialog(bitmap);
                                    }
                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }

                                });
                                return true;
                            });
                        }
                        else
                            holder1.postimage.setVisibility(View.GONE);

                        //////////////////////////TEXT & IMAGE FOR POST//////////////////////



                        ///////////////////FLAMES///////////////////////

                        //INITIAL SETUP//
                        if(currentItem.getLikeL() != null){
                            /////////////////UPDATNG FLAMED BY NO.//////////////////////
                            if(currentItem.getLikeL().size() == 0){
                                holder1.flamedBy.setText("Not flamed yet");
                            }
                            else if(currentItem.getLikeL().size() == 1)
                                holder1.flamedBy.setText("Flamed by 1");
                            else {
                                holder1.flamedBy.setText("Flamed by "+currentItem.getLikeL().size()+" people");
                            }

                            for(int j = 0; j < currentItem.getLikeL().size(); j++){
                                if(currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){
                                    holder1.flameimg.setImageResource(R.drawable.ic_flame_red);
                                    currentItem.setLikeCheck(j);
                                    if((currentItem.getLikeL().size()-1) == 1)
                                        holder1.flamedBy.setText("Flamed by you & "+ (currentItem.getLikeL().size()-1) +" other");
                                    else if((currentItem.getLikeL().size()-1) == 0){
                                        holder1.flamedBy.setText("Flamed by you");
                                    }
                                    else
                                        holder1.flamedBy.setText("Flamed by you & "+ (currentItem.getLikeL().size()-1) +" others");
                                    //Position in likeList where the current USer UId is found stored in likeCheck
                                }
                            }
                        }
                        else{
                            holder1.flamedBy.setText("Not flamed yet");
                            holder1.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                        }
                        //INITIAL SETUP//


                        PushDownAnim.setPushDownAnimTo(holder1.flameimg)
                                .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(currentItem.getLikeCheck() >= 0){//was already liked by current user
                                            holder1.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                                            if(currentItem.getLikeL().size()-1 == 0){
                                                holder1.flamedBy.setText("Not flamed yet");
                                            }
                                            else
                                                holder1.flamedBy.setText("Flamed by "+ (currentItem.getLikeL().size()-1) +" people");
                                            ///////////REMOVE CURRENT USER LIKE/////////////
                                            currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                                            currentItem.setLikeCheck(-1);

                                            //                likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

                                            ///////////////////BATCH WRITE///////////////////
                                            WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                            DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                            batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                            batch.delete(flamedDoc);

                                            batch.commit().addOnSuccessListener(task -> {

                                            });
                                            ///////////////////BATCH WRITE///////////////////
                                        }

                                        else if(currentItem.getLikeCheck() < 0 && currentItem.getLikeL()!=null){
                                            Utility.vibrate(ActivityProfileUser.this);
                                            holder1.flameimg.setImageResource(R.drawable.ic_flame_red);
                                            if(currentItem.getLikeL().size() == 0)
                                                holder1.flamedBy.setText("Flamed by you");
                                            else if(currentItem.getLikeL().size() == 1)
                                                holder1.flamedBy.setText("Flamed by you & "+currentItem.getLikeL().size()+" other");
                                            else
                                                holder1.flamedBy.setText("Flamed by you & "+ currentItem.getLikeL().size() +" others");

                                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                            currentItem.setLikeCheck(currentItem.getLikeL().size()-1);//For local changes

                                            ///////////////////BATCH WRITE///////////////////
                                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                            FlamedModel flamedModel = new FlamedModel();
                                            long tsLong = System.currentTimeMillis();

                                            flamedModel.setPostID(currentItem.getDocID());
                                            flamedModel.setTs(tsLong);
                                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                            flamedModel.setUserdp(PROFILEPIC);
                                            flamedModel.setUsername(USERNAME);
                                            flamedModel.setPostUid(currentItem.getUid());

                                            DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                            batch.set(flamedDoc, flamedModel);
                                            if(currentItem.getLikeL().size() % 5 == 0){
                                                batch.update(likeStore,"newTs", tsLong);
                                            }
                                            batch.commit().addOnSuccessListener(task -> {

                                            });
                                            ///////////////////BATCH WRITE///////////////////
                                        }

                                        else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                                            Utility.vibrate(getApplicationContext());
                                            holder1.flameimg.setImageResource(R.drawable.ic_flame_red);
                                            if(currentItem.getLikeL()!=null)
                                                holder1.flamedBy.setText("Flamed by you & "+ (currentItem.getLikeL().size() + 1) +" people");
                                            else
                                                holder1.flamedBy.setText("Flamed by you");

                                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                            currentItem.setLikeCheck(currentItem.getLikeL().size()-1);
                                            //For local changes current item like added to remote list end

                                            ///////////////////BATCH WRITE///////////////////
                                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                            FlamedModel flamedModel = new FlamedModel();
                                            long tsLong = System.currentTimeMillis();

                                            flamedModel.setPostID(currentItem.getDocID());
                                            flamedModel.setTs(tsLong);
                                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                            flamedModel.setUserdp(PROFILEPIC);
                                            flamedModel.setUsername(USERNAME);
                                            flamedModel.setPostUid(currentItem.getUid());

                                            DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                            batch.set(flamedDoc, flamedModel);
                                            if(currentItem.getLikeL().size() % 5 == 0){
                                                batch.update(likeStore,"newTs", tsLong);
                                            }
                                            batch.commit().addOnSuccessListener(task -> {

                                            });
                                            ///////////////////BATCH WRITE///////////////////
                                        }
                                    }
                                });


                        if(currentItem.getCmtNo()>0){
                            holder1.commentimg.setImageResource(R.drawable.comment_yellow);
                            if(currentItem.getCmtNo()==1)
                                holder1.commentCount.setText(currentItem.getCmtNo()+" comment");
                            else if(currentItem.getCmtNo()>1)
                                holder1.commentCount.setText(currentItem.getCmtNo()+" comments");

                        }
                        else {
                            holder1.commentimg.setImageResource(R.drawable.ic_comment);
                            holder1.commentCount.setText("No comments");
                        }


                        ////////POST MENU///////
                        holder1.menuPost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if( currentItem.getUid().matches(FirebaseAuth.getInstance().getUid())) {
                                    postMenuDialog = new BottomSheetDialog(ActivityProfileUser.this);

                                    postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                                    postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                                        Intent i= new Intent(getApplicationContext(),NewPostHome.class);
                                        i.putExtra("target","100"); //target value for edit post
                                        i.putExtra("bool", Integer.toString(bool));
                                        i.putExtra("usN", currentItem.getUsN());
                                        i.putExtra("dp", currentItem.getDp());
                                        i.putExtra("uid", currentItem.getUid());

                                        i.putExtra("img", currentItem.getImg());
                                        i.putExtra("txt", currentItem.getTxt());
                                        i.putExtra("comID", currentItem.getComID());
                                        i.putExtra("comName", currentItem.getComName());

                                        i.putExtra("ts", Long.toString(currentItem.getTs()));
                                        i.putExtra("newTs", Long.toString(currentItem.getNewTs()));

                                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                                        i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));

                                        i.putExtra("likeL", currentItem.getLikeL());
                                        i.putExtra("likeCheck", currentItem.getLikeCheck());
                                        i.putExtra("docID", currentItem.getDocID());
                                        i.putExtra("reportL", currentItem.getReportL());
                                        i.putExtra("challengeID", currentItem.getChallengeID());
                                        startActivity(i);

                                        postMenuDialog.dismiss();

                                    });

                                    postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileUser.this);
                                        builder.setTitle("Are you sure?")
                                                .setMessage("Post will be deleted permanently")
                                                .setPositiveButton("Delete", (dialog, which) -> {
                                                    progressDialog =new ProgressDialog(ActivityProfileUser.this) ;
                                                    progressDialog.setTitle("Deleting Post");
                                                    progressDialog.setMessage("Please wait...");
                                                    progressDialog.setCancelable(false);
                                                    progressDialog.show();
                                                    FirebaseFirestore.getInstance()
                                                            .collection("Home/Global/Feeds/").document(currentItem
                                                            .getDocID()).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    CommitteeFragment.changed = 1;
                                                                    holder1.first_post.setVisibility(View.GONE);
                                                                    progressDialog.dismiss();
                                                                    FirebaseFirestore.getInstance()
                                                                            .collection("Home/Global/Feeds/")
                                                                            .whereEqualTo("uid", my_uid)
                                                                            .orderBy("ts", Query.Direction.DESCENDING)
                                                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if(task.isSuccessful()) {
                                                                                if(task.getResult().size() == 0) {
                                                                                    holder1.noPost.setVisibility(View.VISIBLE);
                                                                                }
                                                                                else {
                                                                                    holder1.noPost.setVisibility(View.GONE);
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                    postMenuDialog.dismiss();

                                                })
                                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                                .setCancelable(true)
                                                .show();

                                    });

                                    postMenuDialog.findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //                                        String id = gFeedsList.get(position).get.replaceAll(" ","_");
                                            String link = "https://ll.campus24.in/android/Home/Global/"+currentItem.getDocID();
                                            Intent i=new Intent();
                                            i.setAction(Intent.ACTION_SEND);
                                            i.putExtra(Intent.EXTRA_TEXT, link);
                                            i.setType("text/plain");
                                            startActivity(Intent.createChooser(i,"Share with"));
                                            postMenuDialog.dismiss();

                                        }
                                    });

                                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("Home/Global/Feeds/").document(currentItem.getDocID())
                                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Utility.showToast(getApplicationContext(),"Post has been reported.");
                                                        }
                                                    });
                                            postMenuDialog.dismiss();

                                        }
                                    });

                                    Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    postMenuDialog.show();

                                }
                                else {
                                    postMenuDialog = new BottomSheetDialog(ActivityProfileUser.this);

                                    postMenuDialog.setContentView(R.layout.dialog_post_menu);
                                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                                    postMenuDialog.findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //                                        String id = gFeedsList.get(position).get.replaceAll(" ","_");
                                            String link = "https://ll.campus24.in/android/Home/Global/"+currentItem.getDocID();
                                            Intent i=new Intent();
                                            i.setAction(Intent.ACTION_SEND);
                                            i.putExtra(Intent.EXTRA_TEXT, link);
                                            i.setType("text/plain");
                                            startActivity(Intent.createChooser(i,"Share with"));
                                            postMenuDialog.dismiss();

                                        }
                                    });

                                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("Home/Global/Feeds/").document(currentItem.getDocID())
                                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Utility.showToast(getApplicationContext(),"Post has been reported.");
                                                        }
                                                    });
                                            postMenuDialog.dismiss();

                                        }
                                    });
                                    Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    postMenuDialog.show();

                                }
                            }
                        });
                        ////////POST MENU///////
                    }
                    else{
                        ((ProgrammingViewHolder1) holder).itemHome.setVisibility(View.GONE);
                        ((ProgrammingViewHolder1) holder).view1.setVisibility(View.GONE);
                        ((ProgrammingViewHolder1) holder).view2.setVisibility(View.GONE);
                    }

                    ////////////////FOR THE FIRST POST////////////////
                }

                else{
                    DocumentReference likeStore;
                    if(currentItem != null) {
                        ProgrammingViewHolder2 programmingViewHolder = (ProgrammingViewHolder2) holder;
                        String timeAgo = Utility.getTimeAgo(currentItem.getTs());
                        programmingViewHolder.minsago.setText(timeAgo);
                        if(timeAgo != null) {
                            if(timeAgo.matches("just now")) {
                                programmingViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                            }
                            else {
                                programmingViewHolder.minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                        if(currentItem.getChallengeID()!=null){
                            programmingViewHolder.itemHome.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary) ));
                            programmingViewHolder.menuPost.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary) ));
                        }
                        else {
                            programmingViewHolder.itemHome.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white) ));
                            programmingViewHolder.menuPost.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white) ));
                        }

                        ///////////SET DOCUMENT REFERENCEE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////
                        likeStore = FirebaseFirestore.getInstance()
                                .document("Home/Global/Feeds/"+currentItem.getDocID()+"/");
                        programmingViewHolder.comName.setVisibility(View.GONE);


                        programmingViewHolder.menuPost.setVisibility(View.VISIBLE);
                        ///////////SET DOCUMENT REFERENCE FOR LIKES. & OTHER BOOLEAN VALUE CHANGES/////////


                        ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                        if(PROFILEPIC!=null){
                                Picasso.get().load(PROFILEPIC).fit().centerCrop()
                                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                                        .memoryPolicy(MemoryPolicy.NO_STORE)
                                        .into(programmingViewHolder.profileimage);

                        }
                        else {
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }

                        ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                        ///////////TAGLIST///////////////
                        ///////////TAG RECYCLER SETUP////////////////
                        programmingViewHolder.tagList.setHasFixedSize(false);
                        WeakReference<LinearLayoutManager> linearLayoutManager = new WeakReference<>(new LinearLayoutManager(getApplicationContext()));
                        linearLayoutManager.get().setOrientation(LinearLayoutManager.HORIZONTAL);
                        programmingViewHolder.tagList.setNestedScrollingEnabled(true);
                        programmingViewHolder.tagList.setLayoutManager(linearLayoutManager.get());
                        ///////////TAG RECYCLER SETUP////////////////
                        if(currentItem.getTagL()!=null && currentItem.getTagL().size()>0 ) {
                            programmingViewHolder.tagList.setVisibility(View.VISIBLE);
                            TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL() , getApplicationContext());
                            programmingViewHolder.tagList.setAdapter(tagAdapter);
                        }
                        else {
                            programmingViewHolder.tagList.setAdapter(null);
                            programmingViewHolder.tagList.setVisibility(View.GONE);
                        }
                        /////////TAGLIST///////////////


                        //////////////LOADING USERNAME AND USERDP FROM USERNODE FOR CURRENT POST USER///////////////
                            ////////////NORMAL POST///////////////
                            if(currentItem.getDp()!= null && !currentItem.getDp().isEmpty()){

                                    Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                                            .memoryPolicy(MemoryPolicy.NO_STORE)
                                            .into(programmingViewHolder.userimage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                                }
                                            });

                            }
                            else {
                                programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                            programmingViewHolder.username.setText(currentItem.getUsN());


                        ///////////////OPEN VIEW MORE//////////////
                        programmingViewHolder.itemHome.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());

                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());

                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("bool", Integer.toString(bool));
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });
                        programmingViewHolder.text_content.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());

                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());

                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("bool", Integer.toString(bool));
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });

                        programmingViewHolder.postimage.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());
                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());
                            //            intent.putExtra("tagL", currentItem.getTagL());
                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("bool", Integer.toString(bool));;

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                            startActivity(intent);
                        });

                        programmingViewHolder.flamedBy.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), ViewMoreHome.class);
                            intent.putExtra("username", currentItem.getUsN());
                            intent.putExtra("userdp", currentItem.getDp());
                            intent.putExtra("docID", currentItem.getDocID());
                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                            //            StoreTemp.getInstance().setLikeList(currentItem.getLikeL());
                            intent.putExtra("comName", currentItem.getComName());
                            intent.putExtra("comID", currentItem.getComID());
                            //            intent.putExtra("tagL", currentItem.getTagL());
                            intent.putExtra("likeL", currentItem.getLikeL());
                            intent.putExtra("postPic", currentItem.getImg());
                            intent.putExtra("postText", currentItem.getTxt());
                            intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                            intent.putExtra("bool", Integer.toString(bool));

                            intent.putExtra("uid", currentItem.getUid());
                            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));

                            intent.putExtra("likeLOpen", "likeLOpen");
                            startActivity(intent);

                        });
                        ///////////////OPEN VIEW MORE//////////////

                        //////////////////////////TEXT & IMAGE FOR POST//////////////////////
                        if(currentItem.getTxt()==null || currentItem.getTxt().isEmpty()){
                            programmingViewHolder.text_content.setVisibility(View.GONE);
                            programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                            programmingViewHolder.text_content.setText(null);
                        }
                        else{
                            programmingViewHolder.text_content.setVisibility(View.VISIBLE);
                            programmingViewHolder.text_content.setText(currentItem.getTxt());
                            if(programmingViewHolder.text_content.getUrls().length>0){
                                URLSpan urlSnapItem = programmingViewHolder.text_content.getUrls()[0];
                                String url = urlSnapItem.getURL();
                                if(url.contains("http")){
                                    programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                                    programmingViewHolder.LinkPreview.setLink(url ,new ViewListener() {
                                        @Override
                                        public void onSuccess(boolean status) {
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //do stuff like remove view etc
                                                    programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    });
                                }

                            } else {
                                programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                            }

                        }

                        String postimage_url = currentItem.getImg();
                        if(postimage_url!=null){
                            programmingViewHolder.postimage.setVisibility(View.VISIBLE);
                            Picasso.get().load(postimage_url)
                                    .placeholder(R.drawable.image_background_grey)
                                    .memoryPolicy(MemoryPolicy.NO_STORE)
                                    .into(programmingViewHolder.postimage);

                            programmingViewHolder.postimage.setOnLongClickListener(v -> {

                                Picasso.get().load(postimage_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        save_Dialog(bitmap);
                                    }
                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }

                                });
                                return true;
                            });
                        }
                        else
                            programmingViewHolder.postimage.setVisibility(View.GONE);

                        //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                        ///////////////////FLAMES///////////////////////

                        //INITIAL SETUP//
                        if(currentItem.getLikeL() != null){
                            /////////////////UPDATNG FLAMED BY NO.//////////////////////
                            if(currentItem.getLikeL().size() == 0){
                                programmingViewHolder.flamedBy.setText("Not flamed yet");
                            }
                            else if(currentItem.getLikeL().size() == 1)
                                programmingViewHolder.flamedBy.setText("Flamed by 1");
                            else {
                                programmingViewHolder.flamedBy.setText("Flamed by "+currentItem.getLikeL().size()+" people");
                            }

                            for(int j = 0; j < currentItem.getLikeL().size(); j++){
                                if(currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){
                                    programmingViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);
                                    currentItem.setLikeCheck(j);
                                    if((currentItem.getLikeL().size()-1) == 1)
                                        programmingViewHolder.flamedBy.setText("Flamed by you & "+ (currentItem.getLikeL().size()-1) +" other");
                                    else if((currentItem.getLikeL().size()-1) == 0){
                                        programmingViewHolder.flamedBy.setText("Flamed by you");
                                    }
                                    else
                                        programmingViewHolder.flamedBy.setText("Flamed by you & "+ (currentItem.getLikeL().size()-1) +" others");
                                    //Position in likeList where the current USer UId is found stored in likeCheck
                                }
                            }
                        }
                        else{
                            programmingViewHolder.flamedBy.setText("Not flamed yet");
                            programmingViewHolder.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                        }
                        //INITIAL SETUP//


                        PushDownAnim.setPushDownAnimTo(programmingViewHolder.flameimg)
                                .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(currentItem.getLikeCheck() >= 0){//was already liked by current user
                                            programmingViewHolder.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                                            if(currentItem.getLikeL().size()-1 == 0){
                                                programmingViewHolder.flamedBy.setText("Not flamed yet");
                                            }
                                            else
                                                programmingViewHolder.flamedBy.setText("Flamed by "+ (currentItem.getLikeL().size()-1) +" people");
                                            ///////////REMOVE CURRENT USER LIKE/////////////
                                            currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                                            currentItem.setLikeCheck(-1);

                                            //                likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

                                            ///////////////////BATCH WRITE///////////////////
                                            WriteBatch batch = FirebaseFirestore.getInstance().batch();

                                            DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                            batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                                            batch.delete(flamedDoc);

                                            batch.commit().addOnSuccessListener(task -> {

                                            });
                                            ///////////////////BATCH WRITE///////////////////
                                        }

                                        else if(currentItem.getLikeCheck() < 0 && currentItem.getLikeL()!=null){
                                            Utility.vibrate(ActivityProfileUser.this);
                                            programmingViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);
                                            if(currentItem.getLikeL().size() == 0)
                                                programmingViewHolder.flamedBy.setText("Flamed by you");
                                            else if(currentItem.getLikeL().size() == 1)
                                                programmingViewHolder.flamedBy.setText("Flamed by you & "+currentItem.getLikeL().size()+" other");
                                            else
                                                programmingViewHolder.flamedBy.setText("Flamed by you & "+ currentItem.getLikeL().size() +" others");

                                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                            currentItem.setLikeCheck(currentItem.getLikeL().size()-1);//For local changes

                                            ///////////////////BATCH WRITE///////////////////
                                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                            FlamedModel flamedModel = new FlamedModel();
                                            long tsLong = System.currentTimeMillis();

                                            flamedModel.setPostID(currentItem.getDocID());
                                            flamedModel.setTs(tsLong);
                                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                            flamedModel.setUserdp(PROFILEPIC);
                                            flamedModel.setUsername(USERNAME);
                                            flamedModel.setPostUid(currentItem.getUid());

                                            DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                            batch.set(flamedDoc, flamedModel);
                                            if(currentItem.getLikeL().size() % 5 == 0){
                                                batch.update(likeStore,"newTs", tsLong);
                                            }
                                            batch.commit().addOnSuccessListener(task -> {

                                            });
                                            ///////////////////BATCH WRITE///////////////////
                                        }

                                        else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                                            Utility.vibrate(getApplicationContext());
                                            programmingViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);
                                            if(currentItem.getLikeL()!=null)
                                                programmingViewHolder.flamedBy.setText("Flamed by you & "+ (currentItem.getLikeL().size() + 1) +" people");
                                            else
                                                programmingViewHolder.flamedBy.setText("Flamed by you");

                                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                                            currentItem.setLikeCheck(currentItem.getLikeL().size()-1);
                                            //For local changes current item like added to remote list end

                                            ///////////////////BATCH WRITE///////////////////
                                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                            FlamedModel flamedModel = new FlamedModel();
                                            long tsLong = System.currentTimeMillis();

                                            flamedModel.setPostID(currentItem.getDocID());
                                            flamedModel.setTs(tsLong);
                                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                                            flamedModel.setUserdp(PROFILEPIC);
                                            flamedModel.setUsername(USERNAME);
                                            flamedModel.setPostUid(currentItem.getUid());

                                            DocumentReference flamedDoc = likeStore.collection("flameL").document(FirebaseAuth.getInstance().getUid());
                                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                                            batch.set(flamedDoc, flamedModel);
                                            if(currentItem.getLikeL().size() % 5 == 0){
                                                batch.update(likeStore,"newTs", tsLong);
                                            }
                                            batch.commit().addOnSuccessListener(task -> {

                                            });
                                            ///////////////////BATCH WRITE///////////////////
                                        }
                                    }
                                });


                        if(currentItem.getCmtNo()>0){
                            programmingViewHolder.commentimg.setImageResource(R.drawable.comment_yellow);
                            if(currentItem.getCmtNo()==1)
                                programmingViewHolder.commentCount.setText(currentItem.getCmtNo()+" comment");
                            else if(currentItem.getCmtNo()>1)
                                programmingViewHolder.commentCount.setText(currentItem.getCmtNo()+" comments");

                        }
                        else {
                            programmingViewHolder.commentimg.setImageResource(R.drawable.ic_comment);
                            programmingViewHolder.commentCount.setText("No comments");
                        }


                        ////////POST MENU///////
                        programmingViewHolder.menuPost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if( currentItem.getUid().matches(FirebaseAuth.getInstance().getUid())) {
                                    postMenuDialog = new BottomSheetDialog(ActivityProfileUser.this);


                                    postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                                    postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                                        CommitteeFragment.changed=1;
                                        Intent i= new Intent(getApplicationContext(),NewPostHome.class);
                                        i.putExtra("target","100"); //target value for edit post
                                        i.putExtra("bool", Integer.toString(bool));
                                        i.putExtra("usN", currentItem.getUsN());
                                        i.putExtra("dp", currentItem.getDp());
                                        i.putExtra("uid", currentItem.getUid());

                                        i.putExtra("img", currentItem.getImg());
                                        i.putExtra("txt", currentItem.getTxt());
                                        i.putExtra("comID", currentItem.getComID());
                                        i.putExtra("comName", currentItem.getComName());

                                        i.putExtra("ts", Long.toString(currentItem.getTs()));
                                        i.putExtra("newTs", Long.toString(currentItem.getNewTs()));

                                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());

                                        i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));

                                        i.putExtra("likeL", currentItem.getLikeL());
                                        i.putExtra("likeCheck", currentItem.getLikeCheck());
                                        i.putExtra("docID", currentItem.getDocID());
                                        i.putExtra("reportL", currentItem.getReportL());
                                        i.putExtra("challengeID", currentItem.getChallengeID());
                                        startActivity(i);

                                        postMenuDialog.dismiss();

                                    });

                                    postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfileUser.this);
                                        builder.setTitle("Are you sure?")
                                                .setMessage("Post will be deleted permanently")
                                                .setPositiveButton("Delete", (dialog, which) -> {
                                                    progressDialog =new ProgressDialog(ActivityProfileUser.this) ;
                                                    progressDialog.setTitle("Deleting Post");
                                                    progressDialog.setMessage("Please wait...");
                                                    progressDialog.setCancelable(false);
                                                    progressDialog.show();
                                                    FirebaseFirestore.getInstance()
                                                            .collection("Home/Global/Feeds/").document(currentItem
                                                            .getDocID()).delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    CommitteeFragment.changed=1;
                                                                    programmingViewHolder.itemHome.setVisibility(View.GONE);
                                                                    programmingViewHolder.view1.setVisibility(View.GONE);
                                                                    programmingViewHolder.view2.setVisibility(View.GONE);
                                                                    notifyDataSetChanged();
                                                                    FirebaseFirestore.getInstance()
                                                                            .collection("Home/Global/Feeds/")
                                                                            .whereEqualTo("uid", my_uid)
                                                                            .orderBy("ts", Query.Direction.DESCENDING)
                                                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if(task.isSuccessful()) {
                                                                                if(task.getResult().size() == 0) {
                                                                                    programmingViewHolder.noPost.setVisibility(View.VISIBLE);
                                                                                }
                                                                                else {
                                                                                    programmingViewHolder.noPost.setVisibility(View.GONE);
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                    progressDialog.dismiss();
                                                                }
                                                            });
                                                    postMenuDialog.dismiss();

                                                })
                                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                                .setCancelable(true)
                                                .show();

                                    });

                                    postMenuDialog.findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //                                        String id = gFeedsList.get(position).get.replaceAll(" ","_");
                                            String link = "https://ll.campus24.in/android/Home/Global/"+currentItem.getDocID();
                                            Intent i=new Intent();
                                            i.setAction(Intent.ACTION_SEND);
                                            i.putExtra(Intent.EXTRA_TEXT, link);
                                            i.setType("text/plain");
                                            startActivity(Intent.createChooser(i,"Share with"));
                                            postMenuDialog.dismiss();

                                        }
                                    });

                                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("Home/Global/Feeds/").document(currentItem.getDocID())
                                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Utility.showToast(getApplicationContext(),"Post has been reported.");
                                                        }
                                                    });
                                            postMenuDialog.dismiss();

                                        }
                                    });

                                    Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    postMenuDialog.show();

                                }
                                else {
                                    postMenuDialog = new BottomSheetDialog(ActivityProfileUser.this);

                                    postMenuDialog.setContentView(R.layout.dialog_post_menu);
                                    postMenuDialog.setCanceledOnTouchOutside(TRUE);

                                    postMenuDialog.findViewById(R.id.share_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //                                        String id = gFeedsList.get(position).get.replaceAll(" ","_");
                                            String link = "https://ll.campus24.in/android/Home/Global/"+currentItem.getDocID();
                                            Intent i=new Intent();
                                            i.setAction(Intent.ACTION_SEND);
                                            i.putExtra(Intent.EXTRA_TEXT, link);
                                            i.setType("text/plain");
                                            startActivity(Intent.createChooser(i,"Share with"));
                                            postMenuDialog.dismiss();

                                        }
                                    });

                                    postMenuDialog.findViewById(R.id.report_post).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            FirebaseFirestore.getInstance()
                                                    .collection("Home/Global/Feeds/").document(currentItem.getDocID())
                                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Utility.showToast(getApplicationContext(),"Post has been reported.");
                                                        }
                                                    });
                                            postMenuDialog.dismiss();

                                        }
                                    });
                                    Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    postMenuDialog.show();

                                }
                            }
                        });
                        ////////POST MENU///////
                    }
                    else{
                        ((ProgrammingViewHolder2) holder).itemHome.setVisibility(View.GONE);
                        ((ProgrammingViewHolder2) holder).view1.setVisibility(View.GONE);
                        ((ProgrammingViewHolder2) holder).view2.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public int getItemViewType(int position) {
                // Just as an example, return 0 or 2 depending on position
                // Note that unlike in ListView adapters, types don't have to be contiguous
                return position;
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR: Utility.showToast(getApplicationContext(), "Something went wrong..."); break;
                    case LOADING_MORE: progressMore.setVisibility(View.VISIBLE); break;
                    case LOADED: progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED: contentProgress.setVisibility(View.GONE);
                        progressMore.setVisibility(View.GONE);
                        break;
                }
            }
        };

        contentProgress.setVisibility(View.GONE);
        progressMore.setVisibility(View.GONE);

        mRecyclerView.setAdapter(adapter1);



    }


    private static class ProgrammingViewHolder1 extends RecyclerView.ViewHolder{

        private TextView PName,PUsername,PDescription,PInstitute,Pcourse,totalcount,flamecount,commentcount;
        private TextView verify;
        private ImageView PDp,infobadge, starondp, noPost,Pcoverpic;
        private ReadMoreTextView PDetaileddesc;
        private LinearLayout instituteBG;
        private ImageButton startier, currenttier, chat;
        private CardView dpcard;
        private ProgressBar progressBar;

        private TextView  username,commentCount, comName, text_content, flamedBy, minsago, writecomment;
        private ImageView  userimage, postimage, flameimg, commentimg,profileimage, menuPost, share;
        private ApplexLinkPreview LinkPreview;
        private RecyclerView tagList, interests;
        private LinearLayout itemHome, display;
        private RelativeLayout first_post;

        private View view1, view2;

        public ProgrammingViewHolder1(@NonNull View itemView) {
            super(itemView);
            PDp = itemView.findViewById(R.id.Pdp);
            PName = itemView.findViewById(R.id.Profilename);
            PUsername =itemView.findViewById(R.id.Pusername);
            Pcoverpic = itemView.findViewById(R.id.coverpic);

            noPost = itemView.findViewById(R.id.no_recent_post);
            tagList = itemView.findViewById(R.id.tagsList66);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage =itemView.findViewById(R.id.user_image);
            postimage = itemView.findViewById(R.id.post_image);
            flamedBy = itemView.findViewById(R.id.flamed_by);
            minsago=itemView.findViewById(R.id.mins_ago);
            flameimg = itemView.findViewById(R.id.flame);
            comName = itemView.findViewById(R.id.comName);
            commentimg = itemView.findViewById(R.id.comment);
            commentCount = itemView.findViewById(R.id.no_of_comments);
            profileimage = itemView.findViewById(R.id.profile_image);
            menuPost = itemView.findViewById(R.id.delete_post);
            writecomment =itemView.findViewById(R.id.write_comment);
            itemHome =itemView.findViewById(R.id.item_home);
            share =itemView.findViewById(R.id.share);
            LinkPreview =itemView.findViewById(R.id.LinkPreView);
            first_post = itemView.findViewById(R.id.first_post);

            view1 = itemView.findViewById(R.id.view1);
            view2 = itemView.findViewById(R.id.view2);

        }
    }

    private static class ProgrammingViewHolder2 extends RecyclerView.ViewHolder{

        TextView  username,commentCount, comName, text_content, flamedBy, minsago, writecomment;
        ImageView  userimage, postimage, flameimg, commentimg,profileimage, menuPost, share, noPost;
        ApplexLinkPreview LinkPreview;
        RecyclerView tagList;
        LinearLayout itemHome;
        View view, view1, view2;

        @SuppressLint("CutPasteId")
        public ProgrammingViewHolder2(@NonNull View itemView) {
            super(itemView);

            tagList = itemView.findViewById(R.id.tagsList66);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage =itemView.findViewById(R.id.user_image);
            postimage = itemView.findViewById(R.id.post_image);
            flamedBy = itemView.findViewById(R.id.flamed_by);
            minsago=itemView.findViewById(R.id.mins_ago);
            flameimg = itemView.findViewById(R.id.flame);
            comName = itemView.findViewById(R.id.comName);
            commentimg = itemView.findViewById(R.id.comment);
            commentCount = itemView.findViewById(R.id.no_of_comments);
            profileimage = itemView.findViewById(R.id.profile_image);
            menuPost = itemView.findViewById(R.id.delete_post);
            writecomment =itemView.findViewById(R.id.write_comment);
            itemHome =itemView.findViewById(R.id.item_home);
            share =itemView.findViewById(R.id.share);
            LinkPreview =itemView.findViewById(R.id.LinkPreView);

            view= itemView.findViewById(R.id.view);
            view1= itemView.findViewById(R.id.view1);
            view2= itemView.findViewById(R.id.view2);

            noPost= itemView.findViewById(R.id.no_recent_post1);
        }
    }

    private void save_Dialog(Bitmap bitmap) {
        Dialog myDialogue = new Dialog(ActivityProfileUser.this);
        myDialogue.setContentView(R.layout.dialog_image_options);
        myDialogue.setCanceledOnTouchOutside(TRUE);
        myDialogue.findViewById(R.id.saveToInternal).setOnClickListener(v -> {
            if(!Utility.checkStoragePermission(ActivityProfileUser.this)){
                Utility.requestStoragePermission(ActivityProfileUser.this);
            }
            else {
                boolean bool = Utility.saveImage(bitmap, ActivityProfileUser.this);
                if(bool){
                    Toast.makeText(ActivityProfileUser.this, "Saved to device", Toast.LENGTH_SHORT).show();
                    myDialogue.dismiss();
                }
                else{
                    Toast.makeText(ActivityProfileUser.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    myDialogue.dismiss();
                }
            }
        });
        myDialogue.show();
        Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bitmap.recycle();
    }

    private void noPostView() {
        LL.setVisibility(View.GONE);
        noProfilePost.setVisibility(View.VISIBLE);
        nopost1.setVisibility(View.VISIBLE);
        ///////////////////////LOAD PROFILE DETAILS///////////////////////

        documentReference= FirebaseFirestore.getInstance().collection("Users")
                .document(my_uid);

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                userModel = task.getResult().toObject(BaseUserModel.class);
                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(my_uid).collection("Individual").document(my_uid)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(task.getResult().exists()){
                                                IndividualModel individualModel = task.getResult().toObject(IndividualModel.class);
                                                FirstName = individualModel.getFirstname();
                                                LastName = individualModel.getLastname();
                                            }

                                        }
                                    }
                                });

                                UserName = userModel.getName();
                                PName.setText(FirstName+" "+LastName);
                                PUsername.setText('@'+UserName);

                                if(userModel.getDp()!=null){
                                    PROFILEPIC = userModel.getDp();
                                    if(PROFILEPIC!=null){
                                        Picasso.get().load(PROFILEPIC).placeholder(R.drawable.image_background_grey).into(PDp);

                                    }
                                    else
                                        Picasso.get().load(PROFILEPIC).placeholder(R.drawable.image_background_grey).into(PDp);
                                }

                                if(userModel.getCoverpic()!=null){
                                    COVERPIC = userModel.getCoverpic();
                                    Picasso.get().load(COVERPIC).placeholder(R.drawable.image_background_grey).into(PCoverpic);
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
//                                                coverpic.setImageBitmap(scaledBitmap);
//                                    holder1.Pcoverpic.setImageBitmap(scaledBitmap);
                                    PCoverpic.setImageBitmap(scaledBitmap);
                                }

                                if(my_uid.matches(FirebaseAuth.getInstance().getUid())){
                                    editprofile2.setVisibility(View.VISIBLE);
                                    editprofile2.setOnClickListener(v -> {
                                        Intent i1 = new Intent(ActivityProfileUser.this, EditProfileActivity.class);
                                        i1.putExtra("firstname", FirstName);
                                        i1.putExtra("lastname", LastName);
                                        i1.putExtra("username", USERNAME);
                                        i1.putExtra("profilepic",PROFILEPIC );
                                        i1.putExtra("coverpic",COVERPIC);

                                        startActivity(i1);
                                        finish();
                                    });
                                }


                            }

                            else{
                                ActivityProfileUser.super.onBackPressed();
                                Utility.showToast(ActivityProfileUser.this, "Profile is temporarily unavailable");
                            }

                        }
                    }

                });
        ///////////////////////LOAD PROFILE DETAILS///////////////////////


        PDp.setOnClickListener(v -> {
            if(userModel != null) {
                if (userModel.getDp() != null && userModel.getDp().length()>2) {
                    Intent intent = new Intent(ActivityProfileUser.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", userModel.getDp());
                    startActivity(intent);
                }
            }
            else {
                Toast.makeText(ActivityProfileUser.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
            }
        });

        PCoverpic.setOnClickListener(v -> {
            if(userModel != null) {
                if (userModel.getCoverpic() != null) {
                    Intent intent = new Intent(ActivityProfileUser.this, ProfilePictureActivity.class);
                    intent.putExtra("from", "profile");
                    intent.putExtra("Bitmap", userModel.getCoverpic());
                    startActivity(intent);
                }
                else {
                    Toast.makeText(ActivityProfileUser.this, "Picture has not been set", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getAlphaNumericString (int n)
    {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i=0; i<n; i++)
        {
            int index = (int) (AlphaNumericString.length()*Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if(change > 0 || delete > 0) {
            buildRecycler();
            change = 0;
            delete = 0;
        }
        super.onResume();
    }

}