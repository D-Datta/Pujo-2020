package com.example.pujo360.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.pujo360.ActivityProfileCommittee;
import com.example.pujo360.LinkPreview.ApplexLinkPreview;
import com.example.pujo360.LinkPreview.ViewListener;
import com.example.pujo360.NewPostHome;
import com.example.pujo360.R;
import com.example.pujo360.ViewMoreHome;
import com.example.pujo360.adapters.HomeSliderAdapter;
import com.example.pujo360.adapters.TagAdapter;
import com.example.pujo360.models.CommentModel;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.models.HomePostModel;
import com.example.pujo360.models.ReelsPostModel;
import com.example.pujo360.models.SliderModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.InternetConnection;
import com.example.pujo360.util.StoreTemp;
import com.example.pujo360.util.Utility;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import co.gofynd.gravityview.GravityView;

import static java.lang.Boolean.TRUE;

@SuppressWarnings("rawtypes")
public class CommitteeFragment extends Fragment {

    public static int changed = 0;
    public static int delete = 0;
    public static int swipe = 0;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressMore, contentProgress;
    private ProgressDialog progressDialog;
    private Dialog  postMenuDialog;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton create_post;
    private RecyclerView mRecyclerView;
    private String COMMITEE_LOGO, COMMITTEE_NAME;
    private FirestorePagingAdapter adapter, reelsAdapter;
    private DocumentSnapshot lastVisible;
    private IntroPref introPref;
    private GravityView gravityView;
    private boolean isSupported = false;

    public CommitteeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_committee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        swipeRefreshLayout= view.findViewById(R.id.swiperefresh);
        contentProgress = view.findViewById(R.id.content_progress);
        progressMore = view.findViewById(R.id.progress_more);
        gravityView = GravityView.getInstance(requireActivity());
        isSupported = gravityView.deviceSupported();

        //////////////RECYCLER VIEW////////////////////
        mRecyclerView = view.findViewById(R.id.recyclerCommitteePost) ;
        contentProgress.setVisibility(View.VISIBLE);
        floatingActionButton = view.findViewById(R.id.to_the_top_committee);
        create_post = view.findViewById(R.id.create_post_com);

        /////////////SETUP//////////////
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(10);
        mRecyclerView.setDrawingCacheEnabled(true);
        /////////////SETUP//////////////

        buildRecyclerView();
        //////////////RECYCLER VIEW////////////////////

        introPref = new IntroPref(getActivity());
        COMMITEE_LOGO = introPref.getUserdp();
        COMMITTEE_NAME = introPref.getFullName();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.toolbarStart),getResources()
                        .getColor(R.color.md_blue_500));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildRecyclerView();
        });

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
                        floatingActionButton.setOnClickListener(v -> {
                            recyclerView.scrollToPosition(0);
                            recyclerView.postDelayed(() -> recyclerView.scrollToPosition(0),300);
                        });
                    } else {
                        floatingActionButton.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Feeds")
                .whereEqualTo("type", "com")
                .orderBy("newTs", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setEnablePlaceholders(true)
                .build();

        FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    HomePostModel newPostModel = new HomePostModel();
                    if(snapshot.exists()) {
                        newPostModel = snapshot.toObject(HomePostModel.class);
                        Objects.requireNonNull(newPostModel).setDocID(snapshot.getId());
                    }
                    return newPostModel;
                })
                .build();

        adapter = new FirestorePagingAdapter<HomePostModel, RecyclerView.ViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull HomePostModel currentItem) {
                ProgrammingViewHolder programmingViewHolder = (ProgrammingViewHolder) holder;

                if (holder.getItemViewType() == 0) {
                    programmingViewHolder.slider_item.setVisibility(View.VISIBLE);
                    programmingViewHolder.reels_item.setVisibility(View.GONE);

                    programmingViewHolder.sliderView.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    programmingViewHolder.sliderView.setIndicatorRadius(8);
                    programmingViewHolder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    programmingViewHolder.sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                    programmingViewHolder.sliderView.setIndicatorSelectedColor(Color.WHITE);
                    programmingViewHolder.sliderView.setIndicatorUnselectedColor(R.color.colorAccent);
                    programmingViewHolder.sliderView.setAutoCycle(false);

                    ArrayList<SliderModel> itemGroups = new ArrayList<>();

                    FirebaseFirestore.getInstance().collection("Sliders/Slides")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                if (document.exists()) {
                                    SliderModel itemGroup = document.toObject(SliderModel.class);
                                    Objects.requireNonNull(itemGroup).setDocID(document.getId());
                                    itemGroups.add(itemGroup);
                                }
                            }
                            HomeSliderAdapter adapter1 = new HomeSliderAdapter(getContext(), itemGroups, 2);
                            programmingViewHolder.sliderView.setSliderAdapter(adapter1);
                        })
                        .addOnFailureListener(e -> Utility.showToast(getContext(), "No Internet Connection"));
                }
                else if((holder.getItemViewType() == 2 || holder.getItemViewType() == getItemCount() % 8
                        && getItemCount() % 8 == 0) && holder.getItemViewType() != 0
                        && holder.getItemViewType() < getItemCount()) {
                    programmingViewHolder.slider_item.setVisibility(View.GONE);
                    programmingViewHolder.reels_item.setVisibility(View.VISIBLE);

                    buildReelsRecyclerView(programmingViewHolder.reelsList, programmingViewHolder.reelsLayout);
                }
                else {
                    programmingViewHolder.slider_item.setVisibility(View.GONE);
                    programmingViewHolder.reels_item.setVisibility(View.GONE);
                }

                DocumentReference likeStore;
                String timeAgo = Utility.getTimeAgo(currentItem.getTs());
                programmingViewHolder.minsago.setText(timeAgo);
                if (timeAgo != null) {
                    if (timeAgo.matches("just now")) {
                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#aa212121"));
                    }
                }

                programmingViewHolder.itemHome.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                programmingViewHolder.menuPost.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

                likeStore = FirebaseFirestore.getInstance().document("Feeds/" + currentItem.getDocID() + "/");

                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                if (COMMITEE_LOGO != null) {
                    Picasso.get().load(COMMITEE_LOGO).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(programmingViewHolder.profileimage);
                } else {
                    programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////

                ///////////TAGLIST///////////////

                ///////////TAG RECYCLER SETUP////////////////
                programmingViewHolder.tagList.setHasFixedSize(false);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                programmingViewHolder.tagList.setNestedScrollingEnabled(true);
                programmingViewHolder.tagList.setLayoutManager(linearLayoutManager);
                ///////////TAG RECYCLER SETUP////////////////

                if (currentItem.getTagL() != null && currentItem.getTagL().size() > 0) {
                    programmingViewHolder.tagList.setVisibility(View.VISIBLE);
                    TagAdapter tagAdapter = new TagAdapter(currentItem.getTagL(), getActivity());
                    programmingViewHolder.tagList.setAdapter(tagAdapter);
                } else {
                    programmingViewHolder.tagList.setAdapter(null);
                    programmingViewHolder.tagList.setVisibility(View.GONE);
                }
                /////////TAGLIST///////////////

                //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
                programmingViewHolder.userimage.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), ActivityProfileCommittee.class);
                    intent.putExtra("uid", currentItem.getUid());
                    startActivity(intent);
                });

                programmingViewHolder.username.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), ActivityProfileCommittee.class);
                    intent.putExtra("uid", currentItem.getUid());
                    startActivity(intent);
                });
                //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

                //////////////LOADING USERNAME AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
                if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
                    Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(programmingViewHolder.userimage, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            });

                } else {
                    programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                programmingViewHolder.username.setText(currentItem.getUsN());
                //////////////LOADING USERNAME AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

                ///////////////OPEN VIEW MORE//////////////
                programmingViewHolder.itemHome.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                    intent.putExtra("username", currentItem.getUsN());
                    intent.putExtra("userdp", currentItem.getDp());
                    intent.putExtra("docID", currentItem.getDocID());
                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                    intent.putExtra("comName", currentItem.getComName());
                    intent.putExtra("comID", currentItem.getComID());
                    intent.putExtra("likeL", currentItem.getLikeL());
                    intent.putExtra("postPic", currentItem.getSingle_img());
                    intent.putExtra("postText", currentItem.getTxt());
                    intent.putExtra("bool", "2");
                    intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                    intent.putExtra("uid", currentItem.getUid());
                    intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                    intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                    startActivity(intent);
                });

                programmingViewHolder.text_content.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                    intent.putExtra("username", currentItem.getUsN());
                    intent.putExtra("userdp", currentItem.getDp());
                    intent.putExtra("docID", currentItem.getDocID());
                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                    intent.putExtra("comName", currentItem.getComName());
                    intent.putExtra("comID", currentItem.getComID());
                    intent.putExtra("likeL", currentItem.getLikeL());
                    intent.putExtra("postPic", currentItem.getSingle_img());
                    intent.putExtra("postText", currentItem.getTxt());
                    intent.putExtra("bool", "3");
                    intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                    intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                    intent.putExtra("uid", currentItem.getUid());
                    intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                    startActivity(intent);
                });

                programmingViewHolder.postimage.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                    intent.putExtra("username", currentItem.getUsN());
                    intent.putExtra("userdp", currentItem.getDp());
                    intent.putExtra("docID", currentItem.getDocID());
                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                    intent.putExtra("comName", currentItem.getComName());
                    intent.putExtra("comID", currentItem.getComID());
                    intent.putExtra("likeL", currentItem.getLikeL());
                    intent.putExtra("postPic", currentItem.getSingle_img());
                    intent.putExtra("postText", currentItem.getTxt());
                    intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                    intent.putExtra("bool", "2");
                    intent.putExtra("uid", currentItem.getUid());
                    intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                    intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                    startActivity(intent);
                });

                programmingViewHolder.likesCount.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                    intent.putExtra("username", currentItem.getUsN());
                    intent.putExtra("userdp", currentItem.getDp());
                    intent.putExtra("docID", currentItem.getDocID());
                    StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                    intent.putExtra("comName", currentItem.getComName());
                    intent.putExtra("comID", currentItem.getComID());
                    intent.putExtra("likeL", currentItem.getLikeL());
                    intent.putExtra("postPic", currentItem.getSingle_img());
                    intent.putExtra("postText", currentItem.getTxt());
                    intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                    intent.putExtra("bool", "2");
                    intent.putExtra("uid", currentItem.getUid());
                    intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                    intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                    intent.putExtra("likeLOpen", "likeLOpen");
                    startActivity(intent);
                });
                ///////////////OPEN VIEW MORE//////////////

                //////////////////////////TEXT & IMAGE FOR POST//////////////////////
                if (currentItem.getTxt() == null || currentItem.getTxt().isEmpty()) {
                    programmingViewHolder.text_content.setVisibility(View.GONE);
                    programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                    programmingViewHolder.text_content.setText(null);
                }
                else {
                    programmingViewHolder.text_content.setVisibility(View.VISIBLE);
                    programmingViewHolder.text_content.setText(currentItem.getTxt());
                    if (programmingViewHolder.text_content.getUrls().length > 0) {
                        URLSpan urlSnapItem = programmingViewHolder.text_content.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if (url.contains("http")) {
                            programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                            programmingViewHolder.LinkPreview.setLink(url, new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) { }

                                @Override
                                public void onError(Exception e) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        //do stuff like remove view etc
                                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                                    });
                                }
                            });
                        }

                    } else {
                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                    }
                }

                String postimage_url = currentItem.getSingle_img();
                if (postimage_url != null) {
                    programmingViewHolder.postimage.setVisibility(View.VISIBLE);
                    if(isSupported) {
                        int id = getResources().getIdentifier(postimage_url,"drawable", requireActivity().getPackageName());
                        gravityView.setImage(programmingViewHolder.postimage, id);
                    }
                    else {
                        Picasso.get().load(postimage_url)
                                .memoryPolicy(MemoryPolicy.NO_STORE)
                                .placeholder(R.drawable.image_background_grey)
                                .into(programmingViewHolder.postimage);
                    }

                    programmingViewHolder.postimage.setOnLongClickListener(v -> {
                        Picasso.get().load(postimage_url).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                save_Dialog(bitmap);
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }

                        });
                        return true;
                    });
                } else
                    programmingViewHolder.postimage.setVisibility(View.GONE);
                //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                ///////////////////FLAMES AND COMMENTS///////////////////////

                //INITIAL SETUP//
                if (currentItem.getLikeL() != null) {
                    if (currentItem.getLikeL().size() == 0) {
                        programmingViewHolder.like_image.setVisibility(View.GONE);
                        programmingViewHolder.likesCount.setVisibility(View.GONE);
                    }
                    else {
                        programmingViewHolder.like_image.setVisibility(View.VISIBLE);
                        programmingViewHolder.likesCount.setVisibility(View.VISIBLE);
                        programmingViewHolder.likesCount.setText(currentItem.getLikeL().size());
                    }
                } else {
                    programmingViewHolder.like_image.setVisibility(View.GONE);
                    programmingViewHolder.likesCount.setVisibility(View.GONE);
                }
                //INITIAL SETUP//

                PushDownAnim.setPushDownAnimTo(programmingViewHolder.like)
                    .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                    .setOnClickListener(v -> {
                        if (currentItem.getLikeCheck() >= 0) {//was already liked by current user
                            if (currentItem.getLikeL().size() - 1 == 0) {
                                programmingViewHolder.likesCount.setVisibility(View.GONE);
                                programmingViewHolder.like_image.setVisibility(View.GONE);
                            } else{
                                programmingViewHolder.likesCount.setVisibility(View.VISIBLE);
                                programmingViewHolder.like_image.setVisibility(View.VISIBLE);
                                programmingViewHolder.likesCount.setText(currentItem.getLikeL().size());
                            }
                            ///////////REMOVE CURRENT USER LIKE/////////////
                            currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                            currentItem.setLikeCheck(-1);

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();

                            DocumentReference flamedDoc = likeStore.collection("flameL")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                            batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                            batch.delete(flamedDoc);

                            batch.commit().addOnSuccessListener(task -> { });
                            ///////////////////BATCH WRITE///////////////////
                        }
                        else if (currentItem.getLikeCheck() < 0 && currentItem.getLikeL() != null) {
                            Utility.vibrate(requireActivity());
                            if (currentItem.getLikeL().size() == 0){
                                programmingViewHolder.like_image.setVisibility(View.GONE);
                                programmingViewHolder.likesCount.setVisibility(View.GONE);
                            }
                            else{
                                programmingViewHolder.like_image.setVisibility(View.VISIBLE);
                                programmingViewHolder.likesCount.setVisibility(View.VISIBLE);
                                programmingViewHolder.likesCount.setText( currentItem.getLikeL().size());
                            }

                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                            currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);//For local changes

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            FlamedModel flamedModel = new FlamedModel();
                            long tsLong = System.currentTimeMillis();

                            flamedModel.setPostID(currentItem.getDocID());
                            flamedModel.setTs(tsLong);
                            flamedModel.setType(introPref.getType());
                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                            flamedModel.setUserdp(COMMITEE_LOGO);
                            flamedModel.setUsername(COMMITTEE_NAME);
                            flamedModel.setPostUid(currentItem.getUid());

                            DocumentReference flamedDoc = likeStore.collection("flameL")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if (currentItem.getLikeL().size() % 5 == 0) {
                                batch.update(likeStore, "newTs", tsLong);
                            }
                            batch.commit().addOnSuccessListener(task -> { });
                            ///////////////////BATCH WRITE///////////////////
                        }
                        else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                            Utility.vibrate(requireActivity());
                            programmingViewHolder.likesCount.setVisibility(View.VISIBLE);
                            programmingViewHolder.like_image.setVisibility(View.VISIBLE);
                            if (currentItem.getLikeL() != null){
                                programmingViewHolder.likesCount.setText(currentItem.getLikeL().size() + 1);
                            }
                            else{
                                programmingViewHolder.likesCount.setText("1");
                            }

                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                            currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);
                            //For local changes current item like added to remote list end

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            FlamedModel flamedModel = new FlamedModel();
                            long tsLong = System.currentTimeMillis();

                            flamedModel.setPostID(currentItem.getDocID());
                            flamedModel.setTs(tsLong);
                            flamedModel.setType(introPref.getType());
                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                            flamedModel.setUserdp(COMMITEE_LOGO);
                            flamedModel.setUsername(COMMITTEE_NAME);
                            flamedModel.setPostUid(currentItem.getUid());

                            DocumentReference flamedDoc = likeStore.collection("flameL")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if (currentItem.getLikeL().size() % 5 == 0) {
                                batch.update(likeStore, "newTs", tsLong);
                            }
                            batch.commit().addOnSuccessListener(task -> { });
                            ///////////////////BATCH WRITE///////////////////
                        }
                    });

                if (currentItem.getCmtNo() > 0) {
                    programmingViewHolder.commentimg.setVisibility(View.VISIBLE);
                    programmingViewHolder.commentCount.setVisibility(View.VISIBLE);
                    programmingViewHolder.commentLayout1.setVisibility(View.VISIBLE);

                    programmingViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

                    if(currentItem.getCmtNo() == 1) {
                        programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                        FirebaseFirestore.getInstance().collection("Feeds/" + currentItem.getDocID() + "/commentL")
                                .get().addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null) {
                                    CommentModel commentModel = querySnapshot.getDocuments().get(0).toObject(CommentModel.class);
                                    Picasso.get().load(Objects.requireNonNull(commentModel).getUserdp())
                                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                                            .into(programmingViewHolder.dp_cmnt1);
                                    programmingViewHolder.name_cmnt1.setText(commentModel.getUsername());

                                    programmingViewHolder.cmnt1.setText(commentModel.getComment());
                                    if (programmingViewHolder.cmnt1.getUrls().length > 0) {
                                        URLSpan urlSnapItem = programmingViewHolder.cmnt1.getUrls()[0];
                                        String url = urlSnapItem.getURL();
                                        if (url.contains("http")) {
                                            programmingViewHolder.link_preview1.setVisibility(View.VISIBLE);
                                            programmingViewHolder.link_preview1.setLink(url, new ViewListener() {
                                                @Override
                                                public void onSuccess(boolean status) { }

                                                @Override
                                                public void onError(Exception e) {
                                                    new Handler(Looper.getMainLooper()).post(() -> {
                                                        //do stuff like remove view etc
                                                        programmingViewHolder.link_preview1.setVisibility(View.GONE);
                                                    });
                                                }
                                            });
                                        }
                                    } else {
                                        programmingViewHolder.link_preview1.setVisibility(View.GONE);
                                    }

                                    programmingViewHolder.cmnt1_minsago.setText(Utility.getTimeAgo(commentModel.getTs()));
                                    if (Utility.getTimeAgo(commentModel.getTs()) != null) {
                                        if (Objects.requireNonNull(Utility.getTimeAgo(commentModel.getTs())).matches("just now")) {
                                            programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#00C853"));
                                        } else {
                                            programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#aa212121"));
                                        }
                                    }
                                }
                            }
                        });
                    }
                    else {
                        programmingViewHolder.commentLayout2.setVisibility(View.VISIBLE);
                        FirebaseFirestore.getInstance()
                                .collection("Feeds/" + currentItem.getDocID() + "/commentL")
                                .get().addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null) {
                                    querySnapshot.getQuery().orderBy("ts", Query.Direction.DESCENDING)
                                            .get().addOnCompleteListener(task1 -> {
                                        QuerySnapshot querySnapshot1 = task1.getResult();
                                        if (querySnapshot1 != null) {
                                            CommentModel commentModel1 = querySnapshot1.getDocuments().get(0).toObject(CommentModel.class);
                                            Picasso.get().load(Objects.requireNonNull(commentModel1).getUserdp())
                                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                                    .into(programmingViewHolder.dp_cmnt1);
                                            programmingViewHolder.name_cmnt1.setText(commentModel1.getUsername());

                                            programmingViewHolder.cmnt1.setText(commentModel1.getComment());
                                            if (programmingViewHolder.cmnt1.getUrls().length > 0) {
                                                URLSpan urlSnapItem = programmingViewHolder.cmnt1.getUrls()[0];
                                                String url = urlSnapItem.getURL();
                                                if (url.contains("http")) {
                                                    programmingViewHolder.link_preview1.setVisibility(View.VISIBLE);
                                                    programmingViewHolder.link_preview1.setLink(url, new ViewListener() {
                                                        @Override
                                                        public void onSuccess(boolean status) { }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                                //do stuff like remove view etc
                                                                programmingViewHolder.link_preview1.setVisibility(View.GONE);
                                                            });
                                                        }
                                                    });
                                                }
                                            } else {
                                                programmingViewHolder.link_preview1.setVisibility(View.GONE);
                                            }

                                            programmingViewHolder.cmnt1_minsago.setText(Utility.getTimeAgo(commentModel1.getTs()));
                                            if (Utility.getTimeAgo(commentModel1.getTs()) != null) {
                                                if (Objects.requireNonNull(Utility.getTimeAgo(commentModel1.getTs())).matches("just now")) {
                                                    programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#00C853"));
                                                } else {
                                                    programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#aa212121"));
                                                }
                                            }

                                            CommentModel commentModel2 = querySnapshot1.getDocuments().get(1).toObject(CommentModel.class);
                                            Picasso.get().load(Objects.requireNonNull(commentModel2).getUserdp())
                                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                                    .into(programmingViewHolder.dp_cmnt2);
                                            programmingViewHolder.name_cmnt2.setText(commentModel2.getUsername());

                                            programmingViewHolder.cmnt2.setText(commentModel2.getComment());
                                            if (programmingViewHolder.cmnt2.getUrls().length > 0) {
                                                URLSpan urlSnapItem = programmingViewHolder.cmnt2.getUrls()[0];
                                                String url = urlSnapItem.getURL();
                                                if (url.contains("http")) {
                                                    programmingViewHolder.link_preview2.setVisibility(View.VISIBLE);
                                                    programmingViewHolder.link_preview2.setLink(url, new ViewListener() {
                                                        @Override
                                                        public void onSuccess(boolean status) { }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                                //do stuff like remove view etc
                                                                programmingViewHolder.link_preview2.setVisibility(View.GONE);
                                                            });
                                                        }
                                                    });
                                                }
                                            } else {
                                                programmingViewHolder.link_preview2.setVisibility(View.GONE);
                                            }

                                            programmingViewHolder.cmnt2_minsago.setText(Utility.getTimeAgo(commentModel2.getTs()));
                                            if (Utility.getTimeAgo(commentModel2.getTs()) != null) {
                                                if (Objects.requireNonNull(Utility.getTimeAgo(commentModel2.getTs())).matches("just now")) {
                                                    programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#00C853"));
                                                } else {
                                                    programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#aa212121"));
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }

                    programmingViewHolder.commentLayout1.setOnClickListener(v-> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        intent.putExtra("postPic", currentItem.getSingle_img());
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("bool", "2");
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        startActivity(intent);
                    });

                    programmingViewHolder.commentLayout2.setOnClickListener(v-> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        intent.putExtra("postPic", currentItem.getSingle_img());
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("bool", "2");
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        startActivity(intent);
                    });
                }
                else {
                    programmingViewHolder.commentimg.setVisibility(View.GONE);
                    programmingViewHolder.commentCount.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout1.setVisibility(View.GONE);
                    programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                }
                ///////////////////FLAMES AND COMMENTS///////////////////////

                ////////POST MENU///////
                programmingViewHolder.menuPost.setOnClickListener(v -> {
                    if (currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
                            Intent i = new Intent(getContext(), NewPostHome.class);
                            i.putExtra("target", "100"); //target value for edit post
                            i.putExtra("bool", "2");
                            i.putExtra("usN", currentItem.getUsN());
                            i.putExtra("dp", currentItem.getDp());
                            i.putExtra("uid", currentItem.getUid());
                            i.putExtra("img", currentItem.getSingle_img());
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Are you sure?")
                                .setMessage("Post will be deleted permanently")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setTitle("Deleting Post");
                                    progressDialog.setMessage("Please wait...");
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();
                                    FirebaseFirestore.getInstance()
                                        .collection("Feeds").document(currentItem
                                        .getDocID()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            ActivityProfileCommittee.delete = 1;
                                            programmingViewHolder.itemHome.setVisibility(View.GONE);
                                            programmingViewHolder.view1.setVisibility(View.GONE);
                                            programmingViewHolder.view2.setVisibility(View.GONE);
                                            notifyDataSetChanged();
                                            progressDialog.dismiss();
                                        });
                                    postMenuDialog.dismiss();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .setCancelable(true)
                                .show();
                        });

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v13 -> {
                            String link = "https://www.utsavapp.in/android/feeds/" + currentItem.getDocID();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Feeds").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();
                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    }
                    else {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v1 -> {
                            String link = "https://www.utsavapp.in/android/feeds/" + currentItem.getDocID();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                            FirebaseFirestore.getInstance()
                                .collection("Feeds").document(currentItem.getDocID())
                                .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();
                        });
                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    }
                });
                ////////POST MENU///////
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_committee_post, viewGroup, false);
                return new ProgrammingViewHolder(v);
            }

            @Override
            public int getItemViewType(int position) { return position; }

            @Override
            public int getItemCount() { return super.getItemCount(); }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR:
                        Utility.showToast(getActivity(), "Something went wrong...");
                        break;
                    case LOADING_MORE:
                        progressMore.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        progressMore.setVisibility(View.GONE);
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED:
                        contentProgress.setVisibility(View.GONE);
                        progressMore.setVisibility(View.GONE);
                        if(adapter.getItemCount() == 0) { }
                        break;
                }
            }
        };

        contentProgress.setVisibility(View.GONE);
        progressMore.setVisibility(View.GONE);
        mRecyclerView.setAdapter(adapter);
    }

    private static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        SliderView sliderView;
        TextView username,commentCount, text_content, likesCount, minsago, writecomment, name_cmnt1, cmnt1, cmnt1_minsago, name_cmnt2, cmnt2, cmnt2_minsago, view_all_reels;
        ImageView userimage, postimage, like, commentimg,profileimage, menuPost, share, like_image, comment_image,dp_cmnt1,dp_cmnt2;
        ApplexLinkPreview LinkPreview;
        LinearLayout itemHome, commentLayout1, commentLayout2, reelsLayout;
        RecyclerView tagList, reelsList;
        View view, view1, view2;
        com.example.pujo360.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;
        RelativeLayout slider_item, reels_item, normal_item;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);

            sliderView = itemView.findViewById(R.id.imageSlider);
            tagList = itemView.findViewById(R.id.tagsList66);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage = itemView.findViewById(R.id.user_image);
            postimage = itemView.findViewById(R.id.post_image);
            minsago = itemView.findViewById(R.id.mins_ago);
            like = itemView.findViewById(R.id.like);
            commentimg = itemView.findViewById(R.id.comment);
            commentCount = itemView.findViewById(R.id.no_of_comments);
            profileimage = itemView.findViewById(R.id.profile_image);
            menuPost = itemView.findViewById(R.id.delete_post);
            writecomment = itemView.findViewById(R.id.write_comment);
            itemHome = itemView.findViewById(R.id.item_home);
            share = itemView.findViewById(R.id.share);
            LinkPreview = itemView.findViewById(R.id.LinkPreView);

            like_image = itemView.findViewById(R.id.like_image);
            comment_image = itemView.findViewById(R.id.comment_image);
            likesCount = itemView.findViewById(R.id.no_of_likes);

            view = itemView.findViewById(R.id.view);
            view1 = itemView.findViewById(R.id.view1);
            view2 = itemView.findViewById(R.id.view2);

            commentLayout1 = itemView.findViewById(R.id.comment_layout1);
            name_cmnt1 = itemView.findViewById(R.id.comment_username1);
            cmnt1 = itemView.findViewById(R.id.comment1);
            cmnt1_minsago = itemView.findViewById(R.id.comment_mins_ago1);
            dp_cmnt1 = itemView.findViewById(R.id.comment_user_dp1);
            link_preview1 = itemView.findViewById(R.id.LinkPreViewComment1);

            commentLayout2 = itemView.findViewById(R.id.comment_layout2);
            name_cmnt2 = itemView.findViewById(R.id.comment_username2);
            cmnt2 = itemView.findViewById(R.id.comment2);
            cmnt2_minsago = itemView.findViewById(R.id.comment_mins_ago2);
            dp_cmnt2 = itemView.findViewById(R.id.comment_user_dp2);
            link_preview2 = itemView.findViewById(R.id.LinkPreViewComment2);

            view_all_reels = itemView.findViewById(R.id.view_all_reels);
            reelsList = itemView.findViewById(R.id.reelsRecycler);
            reelsLayout = itemView.findViewById(R.id.reels_layout);

            slider_item = itemView.findViewById(R.id.slider_item);
            reels_item = itemView.findViewById(R.id.reels_item);
            normal_item = itemView.findViewById(R.id.normal_item);
        }
    }


    private void buildReelsRecyclerView(RecyclerView reelsList, LinearLayout reelsLayout) {
        reelsList.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        reelsList.setLayoutManager(layoutManager);
        reelsList.setNestedScrollingEnabled(true);
        reelsList.setItemViewCacheSize(10);
        reelsList.setDrawingCacheEnabled(true);

        Query query;

        if(lastVisible != null) {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .orderBy("ts", Query.Direction.DESCENDING)
                    .limit(10)
                    .startAfter(lastVisible);
        }
        else {
            query = FirebaseFirestore.getInstance()
                    .collection("Reels")
                    .orderBy("ts", Query.Direction.DESCENDING)
                    .limit(10);
        }

        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                lastVisible = Objects.requireNonNull(task.getResult()).getDocuments().get(task.getResult().size() - 1);
            }
        });

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setEnablePlaceholders(true)
                .build();

        FirestorePagingOptions<ReelsPostModel> options = new FirestorePagingOptions.Builder<ReelsPostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    ReelsPostModel reelsPostModel = new ReelsPostModel();
                    if(snapshot.exists()) {
                        reelsPostModel = snapshot.toObject(ReelsPostModel.class);
                        Objects.requireNonNull(reelsPostModel).setDocID(snapshot.getId());
                    }
                    return reelsPostModel;
                })
                .build();

        reelsAdapter = new FirestorePagingAdapter<ReelsPostModel, ReelsItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReelsItemViewHolder holder, int position, @NonNull ReelsPostModel currentItem) {
                holder.item_reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
                holder.video_time.setText(currentItem.getDuration());
                holder.pujo_com_name.setText(currentItem.getCommittee_name());

                if (currentItem.getCommittee_dp() != null && !currentItem.getCommittee_dp().isEmpty()) {
                    Picasso.get().load(currentItem.getCommittee_dp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(holder.pujo_com_dp, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            });
                } else {
                    holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                holder.reels_more.setOnClickListener(v -> {
                    if (currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);
                        postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);

                        postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Are you sure?")
                                    .setMessage("Reel will be deleted permanently")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        progressDialog = new ProgressDialog(requireActivity());
                                        progressDialog.setTitle("Deleting Reel");
                                        progressDialog.setMessage("Please wait...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        FirebaseFirestore.getInstance()
                                            .collection("Reels").document(currentItem.getDocID()).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                ActivityProfileCommittee.delete = 1;
                                                holder.itemView.setVisibility(View.GONE);
                                                progressDialog.dismiss();
                                            });
                                        postMenuDialog.dismiss();
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();
                        });

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v12 -> {
                            String link = "https://www.utsavapp.in/android/reels/" + currentItem.getDocID();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v1 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Reels").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Reel has been reported."));
                            postMenuDialog.dismiss();
                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();

                    } else {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v13 -> {
                            String link = "https://www.utsavapp.in/android/reels/" + currentItem.getDocID();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_TEXT, link);
                            i.setType("text/plain");
                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Reels").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> Utility.showToast(getActivity(), "Reel has been reported."));
                            postMenuDialog.dismiss();
                        });
                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    }
                });
            }

            @NonNull
            @Override
            public ReelsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_reels, viewGroup, false);
                return new ReelsItemViewHolder(v);
            }

            @Override
            public int getItemViewType(int position) { return position; }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                super.onLoadingStateChanged(state);
                if (state == LoadingState.FINISHED) {
                    if (reelsAdapter.getItemCount() == 0) {
                        reelsLayout.setVisibility(View.GONE);
                    }
                    else {
                        reelsLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
        reelsList.setAdapter(reelsAdapter);
    }


    private static class ReelsItemViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout item_reels;
        VideoView item_reels_video;
        TextView video_time;
        ImageView pujo_com_dp, reels_more;
        TextView pujo_com_name;

        ReelsItemViewHolder(View itemView) {
            super(itemView);

            item_reels = itemView.findViewById(R.id.item_reels);
            item_reels_video = itemView.findViewById(R.id.item_reels_video);
            video_time = itemView.findViewById(R.id.video_time);
            pujo_com_dp = itemView.findViewById(R.id.pujo_com_dp);
            pujo_com_name = itemView.findViewById(R.id.pujo_com_name);
            reels_more =  itemView.findViewById(R.id.reels_more);
        }
    }

    private void save_Dialog(Bitmap bitmap) {
        Dialog myDialogue = new Dialog(requireActivity());
        myDialogue.setContentView(R.layout.dialog_image_options);
        myDialogue.setCanceledOnTouchOutside(TRUE);
        myDialogue.findViewById(R.id.saveToInternal).setOnClickListener(v -> {
            if(!Utility.checkStoragePermission(requireActivity())) {
                Utility.requestStoragePermission(requireActivity());
            }
            else {
                boolean bool = Utility.saveImage(bitmap, requireActivity());
                if(bool) {
                    Toast.makeText(getContext(), "Saved to device", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                }
                myDialogue.dismiss();
            }
        });
        myDialogue.show();
        Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            onResume();
            if(new IntroPref(requireActivity()).getType().matches("Committee")) {
                create_post.setVisibility(View.VISIBLE);
                create_post.setOnClickListener(v -> {
                    if(InternetConnection.checkConnection(requireActivity())){
                        Intent intent =  new Intent(requireActivity(), NewPostHome.class);
                        intent.putExtra("target", "2");
                        startActivity(intent);
                    }
                    else
                        Utility.showToast(requireActivity(), "Network Unavailable...");
                });
            }
            else {
                create_post.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        if((changed > 0 || delete > 0) && swipe == 0) {
            buildRecyclerView();
            changed = 0;
            delete = 0;
        }
        else {
            swipe = 1;
        }
        super.onResume();
        gravityView.registerListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        gravityView.unRegisterListener();
    }

    @Override
    protected void finalize() throws Throwable {
        System.gc();
        super.finalize();
    }
}