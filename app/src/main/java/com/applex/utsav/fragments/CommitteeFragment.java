package com.applex.utsav.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.ActivityProfileCommittee;
import com.applex.utsav.ActivityProfileUser;
import com.applex.utsav.CommitteeViewAll;
import com.applex.utsav.LinkPreview.ApplexLinkPreview;
import com.applex.utsav.LinkPreview.ViewListener;
import com.applex.utsav.MainActivity;
import com.applex.utsav.NewPostHome;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.adapters.CommitteeTopAdapter;
import com.applex.utsav.adapters.HomeSliderAdapter;
import com.applex.utsav.adapters.SliderAdapter;
import com.applex.utsav.adapters.TagAdapter;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.models.SliderModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.applex.utsav.utility.StoreTemp;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import static java.lang.Boolean.TRUE;

@SuppressWarnings("rawtypes")
public class CommitteeFragment extends Fragment {

    public static int changed = 0;
    public static int delete = 0;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressMore, contentProgress;
    private ProgressDialog progressDialog;
    private Dialog postMenuDialog;
    private RecyclerView mRecyclerView;
    private String COMMITEE_LOGO, COMMITTEE_NAME, link, GENDER;
    private FirestorePagingAdapter reelsAdapter, feedsAdapter;
    private IntroPref introPref;
    private Query reels_query, feeds_query;
    private ArrayList<Integer> positions;
    private DocumentSnapshot lastReelDocument, lastfeedDocument;
    private FloatingActionButton floatingActionButton;

    public CommitteeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_committee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        introPref = new IntroPref(getActivity());
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        Objects.requireNonNull(getActivity()).getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());

        swipeRefreshLayout= view.findViewById(R.id.swiperefresh);
        contentProgress = view.findViewById(R.id.content_progress);
        progressMore = view.findViewById(R.id.progress_more);
        floatingActionButton = view.findViewById(R.id.to_the_top_committee);

        //////////////RECYCLER VIEW////////////////////
        mRecyclerView = view.findViewById(R.id.recyclerCommitteePost) ;
        contentProgress.setVisibility(View.VISIBLE);

        /////////////SETUP//////////////
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        /////////////SETUP//////////////

        positions = new ArrayList<>();
        buildRecyclerView();
        //////////////RECYCLER VIEW////////////////////

        introPref = new IntroPref(getActivity());
        COMMITEE_LOGO = introPref.getUserdp();
        COMMITTEE_NAME = introPref.getFullName();
        GENDER = introPref.getGender();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                        getResources().getColor(R.color.purple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            positions = new ArrayList<>();
            buildRecyclerView();
        });
    }

    private void buildRecyclerView() {

        final Query[] query = {FirebaseFirestore.getInstance()
                .collection("Feeds")
                .whereEqualTo("type", "com")
                .orderBy("newTs", Query.Direction.DESCENDING)};

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setPrefetchDistance(4)
                .setEnablePlaceholders(true)
                .build();

        FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
                .setLifecycleOwner(this)
                .setQuery(query[0], config, snapshot -> {
                    HomePostModel newPostModel = new HomePostModel();
                    if(snapshot.exists()) {
                        newPostModel = snapshot.toObject(HomePostModel.class);
                        Objects.requireNonNull(newPostModel).setDocID(snapshot.getId());
                    }
                    return newPostModel;
                })
                .build();

        FirestorePagingAdapter adapter = new FirestorePagingAdapter<HomePostModel, ProgrammingViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder programmingViewHolder, int position, @NonNull HomePostModel currentItem) {

                if (programmingViewHolder.getItemViewType() == 0 || programmingViewHolder.getItemViewType() % 7 == 0) {
                    programmingViewHolder.slider_item.setVisibility(View.VISIBLE);
                    programmingViewHolder.reels_item.setVisibility(View.GONE);
                    programmingViewHolder.committee_item.setVisibility(View.GONE);
                    programmingViewHolder.feeds_item.setVisibility(View.GONE);

                    programmingViewHolder.sliderView.setIndicatorAnimation(IndicatorAnimations.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    programmingViewHolder.sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    programmingViewHolder.sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                    programmingViewHolder.sliderView.setIndicatorSelectedColor(Color.WHITE);
                    programmingViewHolder.sliderView.setSliderAnimationDuration(500);
                    programmingViewHolder.sliderView.setIndicatorUnselectedColor(R.color.colorAccent);
                    programmingViewHolder.sliderView.setAutoCycle(false);

                    ArrayList<SliderModel> itemGroups = new ArrayList<>();

                    FirebaseFirestore.getInstance().collection("Sliders")
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
                            .addOnFailureListener(e -> BasicUtility.showToast(getContext(), "No Internet Connection"));

                    if(programmingViewHolder.getItemViewType() == 0) {
                        programmingViewHolder.view.setVisibility(View.GONE);
                        programmingViewHolder.new_post_layout.setVisibility(View.VISIBLE);
                        programmingViewHolder.type_something.setOnClickListener(view -> {
                            if (InternetConnection.checkConnection(requireActivity())) {
                                Intent i = new Intent(getContext(), NewPostHome.class);
                                if (introPref.getType().matches("com")) {
                                    i.putExtra("target", "1");
                                } else
                                    i.putExtra("target", "2");

                                startActivity(i);
                            } else
                                BasicUtility.showToast(getContext(), "Network Unavailable...");
                        });

                        programmingViewHolder.newPostIconsLL.setOnClickListener(view -> {
                            if (InternetConnection.checkConnection(requireActivity())) {
                                Intent i = new Intent(getContext(), NewPostHome.class);

                                if (introPref.getType().matches("com")) {
                                    i.putExtra("target", "1");
                                } else
                                    i.putExtra("target", "2");

                                startActivity(i);
                            } else
                                BasicUtility.showToast(getContext(), "Network Unavailable...");
                        });

                        if (COMMITEE_LOGO != null) {
                            Picasso.get().load(COMMITEE_LOGO).fit().centerCrop()
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(programmingViewHolder.type_dp);
                        } else {
                            if (GENDER != null) {
                                if (GENDER.matches("Female") || GENDER.matches("মহিলা")) {
                                    programmingViewHolder.type_dp.setImageResource(R.drawable.ic_female);
                                } else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")) {
                                    programmingViewHolder.type_dp.setImageResource(R.drawable.ic_male);
                                } else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")) {
                                    programmingViewHolder.type_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            } else {
                                programmingViewHolder.type_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }
                    } else {
                        programmingViewHolder.view.setVisibility(View.VISIBLE);
                        programmingViewHolder.new_post_layout.setVisibility(View.GONE);
                    }
                }
                else if (programmingViewHolder.getItemViewType() == 4 || programmingViewHolder.getItemViewType() == 2) {
                    programmingViewHolder.committee_item.setVisibility(View.VISIBLE);
                    programmingViewHolder.feeds_item.setVisibility(View.GONE);
                    programmingViewHolder.reels_item.setVisibility(View.GONE);
                    programmingViewHolder.slider_item.setVisibility(View.GONE);

                    programmingViewHolder.view_all.setOnClickListener(v ->
                            startActivity(new Intent(getActivity(), CommitteeViewAll.class))
                    );

                    if (programmingViewHolder.getItemViewType() == 4) {
                        programmingViewHolder.comm_heading.setText(getResources().getText(R.string.recently_visited_pujos));
                    } else {
                        programmingViewHolder.comm_heading.setText(getResources().getText(R.string.upvoted_pujos));
                    }

                    buildCommunityRecyclerView(programmingViewHolder.cRecyclerView, programmingViewHolder.getItemViewType());
                }
                else if ((programmingViewHolder.getItemViewType() == 1 || programmingViewHolder.getItemViewType() % 8 == 0)) {
                    programmingViewHolder.feeds_item.setVisibility(View.GONE);
                    programmingViewHolder.slider_item.setVisibility(View.GONE);
                    programmingViewHolder.committee_item.setVisibility(View.GONE);
                    programmingViewHolder.reels_item.setVisibility(View.VISIBLE);

                    if (programmingViewHolder.getItemViewType() != 1 && lastReelDocument != null) {
                        reels_query = FirebaseFirestore.getInstance()
                                .collection("Reels")
                                .whereEqualTo("type", "com")
                                .orderBy("ts", Query.Direction.DESCENDING)
                                .startAfter(lastReelDocument);
                    } else {
                        reels_query = FirebaseFirestore.getInstance()
                                .collection("Reels")
                                .whereEqualTo("type", "com")
                                .orderBy("ts", Query.Direction.DESCENDING);
                    }

                    buildReelsRecyclerView(position, programmingViewHolder);

                    programmingViewHolder.view_all_reels.setOnClickListener(v -> {
                        Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                        intent.putExtra("bool", "1");
                        intent.putExtra("from", "com");
                        requireActivity().startActivity(intent);
                    });
                }
                else if(programmingViewHolder.getItemViewType() % 4 == 0) {
                    programmingViewHolder.feeds_item.setVisibility(View.VISIBLE);
                    programmingViewHolder.slider_item.setVisibility(View.GONE);
                    programmingViewHolder.reels_item.setVisibility(View.GONE);
                    programmingViewHolder.committee_item.setVisibility(View.GONE);

                    if (programmingViewHolder.getItemViewType() != 4 && lastfeedDocument != null) {
                        feeds_query = FirebaseFirestore.getInstance()
                                .collection("Feeds")
                                .whereEqualTo("type", "indi")
                                .orderBy("ts", Query.Direction.DESCENDING)
                                .startAfter(lastReelDocument);
                    } else {
                        feeds_query = FirebaseFirestore.getInstance()
                                .collection("Feeds")
                                .whereEqualTo("type", "indi")
                                .orderBy("ts", Query.Direction.DESCENDING);
                    }

                    buildFeedsRecyclerView(programmingViewHolder);
                    programmingViewHolder.view_all_feeds.setOnClickListener(v -> MainActivity.viewPager.setCurrentItem(1, true));
                }
                else {
                    programmingViewHolder.feeds_item.setVisibility(View.GONE);
                    programmingViewHolder.slider_item.setVisibility(View.GONE);
                    programmingViewHolder.reels_item.setVisibility(View.GONE);
                    programmingViewHolder.committee_item.setVisibility(View.GONE);
                }

                DocumentReference likeStore;
                String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                programmingViewHolder.minsago.setText(timeAgo);
                if (timeAgo != null) {
                    if (timeAgo.matches("just now")) {
                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                    } else {
                        programmingViewHolder.minsago.setTextColor(Color.parseColor("#aa212121"));
                    }
                }

                likeStore = FirebaseFirestore.getInstance().document("Feeds/" + currentItem.getDocID() + "/");

                ///////////////SETTING CURRENT USER BOTTOM PIC///////////////
                if (COMMITEE_LOGO != null) {
                    Picasso.get().load(COMMITEE_LOGO).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(programmingViewHolder.profileimage);
                } else {
                    if (GENDER != null) {
                        if (GENDER.matches("Female") || GENDER.matches("মহিলা")) {
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_female);
                        } else if (GENDER.matches("Male") || GENDER.matches("পুরুষ")) {
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_male);
                        } else if (GENDER.matches("Others") || GENDER.matches("অন্যান্য")) {
                            programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    } else {
                        programmingViewHolder.profileimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
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
                                    if (currentItem.getGender() != null) {
                                        if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")) {
                                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_female);
                                        } else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")) {
                                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_male);
                                        } else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")) {
                                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    } else {
                                        programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                }
                            });
                } else {
                    if (currentItem.getGender() != null) {
                        if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")) {
                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_female);
                        } else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")) {
                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_male);
                        } else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")) {
                            programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    } else {
                        programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                }

                programmingViewHolder.username.setText(currentItem.getUsN());
                //////////////LOADING USERNAME AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

                //////////////////////////TEXT & IMAGE FOR POST//////////////////////
                if (currentItem.getHeadline() == null || currentItem.getHeadline().isEmpty()) {
                    programmingViewHolder.head_content.setVisibility(View.GONE);
                    programmingViewHolder.head_content.setText(null);
                } else {
                    programmingViewHolder.head_content.setVisibility(View.VISIBLE);
                    programmingViewHolder.head_content.setText(currentItem.getHeadline());
                }

                if (currentItem.getTxt() == null || currentItem.getTxt().isEmpty()) {
                    programmingViewHolder.text_content.setVisibility(View.GONE);
                    programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                    programmingViewHolder.text_content.setText(null);
                } else {
                    programmingViewHolder.text_content.setVisibility(View.VISIBLE);
                    programmingViewHolder.text_content.setText(currentItem.getTxt());
                    if (programmingViewHolder.text_content.getUrls().length > 0) {
                        URLSpan urlSnapItem = programmingViewHolder.text_content.getUrls()[0];
                        String url = urlSnapItem.getURL();
                        if (url.contains("http")) {
                            programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                            programmingViewHolder.LinkPreview.setLink(url, new ViewListener() {
                                @Override
                                public void onSuccess(boolean status) {
                                }

                                @Override
                                public void onError(Exception e) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        //do stuff like remove view etc
                                        programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                                    });
                                }
                            });
                        }
                    } else if (programmingViewHolder.head_content.getUrls().length > 0) {
                        URLSpan urlSnapItem = programmingViewHolder.head_content.getUrls()[0];
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

                if (currentItem.getImg() != null && currentItem.getImg().size() > 0) {
                    programmingViewHolder.rlLayout.setVisibility(View.VISIBLE);
                    programmingViewHolder.sliderViewpost.setVisibility(View.VISIBLE);
                    programmingViewHolder.sliderViewpost.setIndicatorAnimation(IndicatorAnimations.SCALE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    programmingViewHolder.sliderViewpost.setIndicatorRadius(5);
                    programmingViewHolder.sliderViewpost.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    programmingViewHolder.sliderViewpost.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                    programmingViewHolder.sliderViewpost.setIndicatorSelectedColor(Color.WHITE);
                    programmingViewHolder.sliderViewpost.setIndicatorUnselectedColor(R.color.colorAccent);
                    programmingViewHolder.sliderViewpost.setAutoCycle(false);

                    SliderAdapter sliderAdapter = new SliderAdapter(getActivity(), currentItem.getImg(), currentItem);
                    programmingViewHolder.sliderViewpost.setSliderAdapter(sliderAdapter);

                    programmingViewHolder.text_content.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        if (currentItem.getImg() != null && currentItem.getImg().size() > 0) {
                            Bundle args = new Bundle();
                            args.putSerializable("ARRAYLIST", currentItem.getImg());
                            intent.putExtra("BUNDLE", args);
                        }
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("bool", "3");
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("type", currentItem.getType());
                        intent.putExtra("gender", currentItem.getGender());
                        startActivity(intent);
                    });

                    programmingViewHolder.head_content.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        if (currentItem.getImg() != null && currentItem.getImg().size() > 0) {
                            Bundle args = new Bundle();
                            args.putSerializable("ARRAYLIST", currentItem.getImg());
                            intent.putExtra("BUNDLE", args);
                        }
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("bool", "3");
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("type", currentItem.getType());
                        intent.putExtra("gender", currentItem.getGender());
                        startActivity(intent);
                    });
                } else {
                    programmingViewHolder.rlLayout.setVisibility(View.GONE);
                    programmingViewHolder.sliderViewpost.setVisibility(View.GONE);
                }
                //////////////////////////TEXT & IMAGE FOR POST//////////////////////

                ///////////////////FLAMES AND COMMENTS///////////////////////

                programmingViewHolder.like_layout.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Feeds", currentItem.getDocID());
                    bottomSheetDialog.show(requireActivity().getSupportFragmentManager(), "FlamedBySheet");
                });

                //INITIAL SETUP//
                if (currentItem.getLikeL() != null) {
                    if (currentItem.getLikeL().size() == 0) {
                        programmingViewHolder.like_layout.setVisibility(View.GONE);
                    } else {
                        programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                        programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size()));
                    }

                    for (int j = 0; j < currentItem.getLikeL().size(); j++) {
                        if (currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                            programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
                            currentItem.setLikeCheck(j);
                        }
                    }
                } else {
                    programmingViewHolder.like_layout.setVisibility(View.GONE);
                }
                //INITIAL SETUP//

                PushDownAnim.setPushDownAnimTo(programmingViewHolder.like)
                    .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                    .setOnClickListener(v -> {
                        if (currentItem.getLikeCheck() >= 0) {
                            programmingViewHolder.like.setImageResource(R.drawable.ic_btmnav_notifications); //was already liked by current user
                            if (currentItem.getLikeL().size() - 1 == 0) {
                                programmingViewHolder.like_layout.setVisibility(View.GONE);
                            } else {
                                programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                                programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() - 1));
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

                            batch.commit().addOnSuccessListener(task -> {
                            });
                            ///////////////////BATCH WRITE///////////////////
                        }
                        else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                            BasicUtility.vibrate(requireActivity());
                            programmingViewHolder.dhak_anim.setVisibility(View.VISIBLE);
                            programmingViewHolder.dhak_anim.playAnimation();
                            try {
                                AssetFileDescriptor afd = requireActivity().getAssets().openFd("dhak.mp3");
                                MediaPlayer player = new MediaPlayer();
                                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                player.prepare();
                                AudioManager audioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
                                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                    player.start();
                                    if (!player.isPlaying()) {
                                        programmingViewHolder.dhak_anim.cancelAnimation();
                                        programmingViewHolder.dhak_anim.setVisibility(View.GONE);
                                    }
                                    player.setOnCompletionListener(mediaPlayer -> {
                                        programmingViewHolder.dhak_anim.cancelAnimation();
                                        programmingViewHolder.dhak_anim.setVisibility(View.GONE);
                                    });
                                } else {
                                    new Handler().postDelayed(() -> {
                                        programmingViewHolder.dhak_anim.cancelAnimation();
                                        programmingViewHolder.dhak_anim.setVisibility(View.GONE);
                                    }, 2000);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            programmingViewHolder.like.setImageResource(R.drawable.ic_flame_red);
                            programmingViewHolder.like_layout.setVisibility(View.VISIBLE);
                            if (currentItem.getLikeL() != null) {
                                programmingViewHolder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() + 1));
                            } else {
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
                            flamedModel.setGender(introPref.getGender());

                            DocumentReference flamedDoc = likeStore.collection("flameL")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if (currentItem.getLikeL().size() % 5 == 0) {
                                batch.update(likeStore, "newTs", tsLong);
                            }
                            batch.commit().addOnSuccessListener(task -> {
                            });
                            ///////////////////BATCH WRITE///////////////////
                        }
                    });

                programmingViewHolder.commentimg.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 1, "CommitteeFragment", null, currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    try {
                        AssetFileDescriptor afd = requireActivity().getAssets().openFd("sonkho.mp3");
                        MediaPlayer player = new MediaPlayer();
                        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        player.prepare();
                        AudioManager audioManager = (AudioManager) requireActivity().getSystemService(Context.AUDIO_SERVICE);
                        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            player.start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                programmingViewHolder.writecomment.setOnClickListener(v -> {
                    BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 1, "CommitteeFragment", null, currentItem.getCmtNo(), null, null);
                    bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                });

                programmingViewHolder.share.setOnClickListener(view -> {
                    if (currentItem.getImg() != null && currentItem.getImg().size() > 0)
                        link = "https://www.applex.in/utsav-app/feeds/" + "1/" + currentItem.getDocID();
                    else
                        link = "https://www.applex.in/utsav-app/feeds/" + "0/" + currentItem.getDocID();
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra(Intent.EXTRA_TEXT, link);
                    i.setType("text/plain");
                    startActivity(Intent.createChooser(i, "Share with"));
                });

                if (currentItem.getCmtNo() > 0) {
                    ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                    ProgrammingViewHolder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

                    if (currentItem.getCom1() != null && !currentItem.getCom1().isEmpty()) {

                        programmingViewHolder.commentLayout1.setVisibility(View.VISIBLE);
                        programmingViewHolder.name_cmnt1.setText(currentItem.getCom1_usn());

                        if (currentItem.getCom1_dp() != null && !currentItem.getCom1_dp().isEmpty()) {
                            Picasso.get().load(currentItem.getCom1_dp())
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(programmingViewHolder.dp_cmnt1);

                        } else {
                            if (currentItem.getCom1_gender() != null) {
                                if (currentItem.getCom1_gender().matches("Female") || currentItem.getCom1_gender().matches("মহিলা")) {
                                    programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_female);
                                } else if (currentItem.getCom1_gender().matches("Male") || currentItem.getCom1_gender().matches("পুরুষ")) {
                                    programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_male);
                                } else if (currentItem.getCom1_gender().matches("Others") || currentItem.getCom1_gender().matches("অন্যান্য")) {
                                    programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            } else {
                                programmingViewHolder.dp_cmnt1.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }

                        programmingViewHolder.cmnt1.setText(currentItem.getCom1());
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

                        programmingViewHolder.cmnt1_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom1_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom1_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom1_ts())).matches("just now")) {
                                programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                programmingViewHolder.cmnt1_minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                    } else {
                        programmingViewHolder.commentLayout1.setVisibility(View.GONE);
                    }

                    if (currentItem.getCom2() != null && !currentItem.getCom2().isEmpty()) {

                        programmingViewHolder.commentLayout2.setVisibility(View.VISIBLE);
                        programmingViewHolder.name_cmnt2.setText(currentItem.getCom2_usn());

                        if (currentItem.getCom2_dp() != null && !currentItem.getCom2_dp().isEmpty()) {
                            Picasso.get().load(currentItem.getCom2_dp())
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(programmingViewHolder.dp_cmnt2);
                        } else {
                            if (currentItem.getCom2_gender() != null) {
                                if (currentItem.getCom2_gender().matches("Female") || currentItem.getCom2_gender().matches("মহিলা")) {
                                    programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_female);
                                } else if (currentItem.getCom2_gender().matches("Male") || currentItem.getCom2_gender().matches("পুরুষ")) {
                                    programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_male);
                                } else if (currentItem.getCom2_gender().matches("Others") || currentItem.getCom2_gender().matches("অন্যান্য")) {
                                    programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            } else {
                                programmingViewHolder.dp_cmnt2.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }

                        programmingViewHolder.cmnt2.setText(currentItem.getCom2());
                        if (programmingViewHolder.cmnt2.getUrls().length > 0) {
                            URLSpan urlSnapItem = programmingViewHolder.cmnt2.getUrls()[0];
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

                        programmingViewHolder.cmnt2_minsago.setText(BasicUtility.getTimeAgo(currentItem.getCom2_ts()));
                        if (BasicUtility.getTimeAgo(currentItem.getCom2_ts()) != null) {
                            if (Objects.requireNonNull(BasicUtility.getTimeAgo(currentItem.getCom2_ts())).matches("just now")) {
                                programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#00C853"));
                            } else {
                                programmingViewHolder.cmnt2_minsago.setTextColor(Color.parseColor("#aa212121"));
                            }
                        }
                    } else {
                        programmingViewHolder.commentLayout2.setVisibility(View.GONE);
                    }

                    ProgrammingViewHolder.comment_layout.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2, "CommitteeFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout1.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2, "CommitteeFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });

                    programmingViewHolder.commentLayout2.setOnClickListener(v -> {
                        BottomCommentsDialog bottomCommentsDialog = BottomCommentsDialog.newInstance("Feeds", currentItem.getDocID(), currentItem.getUid(), 2, "CommitteeFragment", null, currentItem.getCmtNo(), null, null);
                        bottomCommentsDialog.show(requireActivity().getSupportFragmentManager(), "CommentsSheet");
                    });
                } else {
                    ProgrammingViewHolder.comment_layout.setVisibility(View.GONE);
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

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);
                        postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);
//                        postMenuDialog.findViewById(R.id.edit_post).setOnClickListener(v2 -> {
//                            Intent i = new Intent(getContext(), NewPostHome.class);
//                            i.putExtra("target", "100"); //target value for edit post
//                            i.putExtra("bool", "2");
//                            i.putExtra("usN", currentItem.getUsN());
//                            i.putExtra("dp", currentItem.getDp());
//                            i.putExtra("uid", currentItem.getUid());
//                            i.putExtra("type", currentItem.getType());
//                            if(currentItem.getImg() != null && currentItem.getImg().size()>0) {
//                                Bundle args = new Bundle();
//                                args.putSerializable("ARRAYLIST", currentItem.getImg());
//                                i.putExtra("BUNDLE", args);
//                            }
//                            i.putExtra("txt", currentItem.getTxt());
//                            i.putExtra("comID", currentItem.getComID());
//                            i.putExtra("comName", currentItem.getComName());
//                            i.putExtra("ts", Long.toString(currentItem.getTs()));
//                            i.putExtra("newTs", Long.toString(currentItem.getNewTs()));
//                            StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
//                            i.putExtra("cmtNo", Long.toString(currentItem.getCmtNo()));
//                            i.putExtra("likeL", currentItem.getLikeL());
//                            i.putExtra("likeCheck", currentItem.getLikeCheck());
//                            i.putExtra("docID", currentItem.getDocID());
//                            i.putExtra("reportL", currentItem.getReportL());
//                            i.putExtra("challengeID", currentItem.getChallengeID());
//                            startActivity(i);
//                            postMenuDialog.dismiss();
//                        });

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
                                                    notifyDataSetChanged();
                                                    progressDialog.dismiss();
                                                });
                                        postMenuDialog.dismiss();
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Feeds").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Post has been reported."));
                            postMenuDialog.dismiss();
                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    } else {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setVisibility(View.GONE);

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Feeds").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Post has been reported."));
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
            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
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
                        BasicUtility.showToast(getActivity(), "Something went wrong...");
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
                        break;
                }
            }
        };

        contentProgress.setVisibility(View.GONE);
        progressMore.setVisibility(View.GONE);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        final int[] scrollY = {0};

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
                    int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

                    if (firstVisiblePosition >= 0) {
                        Rect rect_parent = new Rect();
                        mRecyclerView.getGlobalVisibleRect(rect_parent);

                        for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                            if(positions != null && positions.contains(i)) {

                                final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                                ProgrammingViewHolder cvh = (ProgrammingViewHolder) holder;

                                int[] location = new int[2];
                                Objects.requireNonNull(cvh).reels_item.getLocationOnScreen(location);
                                Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.reels_item.getWidth(), location[1] + cvh.reels_item.getHeight());

                                float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                                float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                                float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                                float overlapArea = x_overlap * y_overlap;
                                float percent = (overlapArea / rect_parent_area) * 100.0f;

                                if (percent >= 90) {
                                    RecyclerView.LayoutManager manager1 = Objects.requireNonNull(cvh).rRecyclerView.getLayoutManager();

                                    int firstVisiblePosition1 = ((LinearLayoutManager) Objects.requireNonNull(manager1)).findFirstVisibleItemPosition();
                                    int lastVisiblePosition1 = ((LinearLayoutManager) manager1).findLastVisibleItemPosition();

                                    if (firstVisiblePosition1 >= 0) {
                                        Rect rect_parent1 = new Rect();
                                        cvh.rRecyclerView.getGlobalVisibleRect(rect_parent1);

                                        for (int j = firstVisiblePosition1; j <= lastVisiblePosition1; j++) {
                                            final RecyclerView.ViewHolder holder2 = cvh.rRecyclerView.findViewHolderForAdapterPosition(j);
                                            ReelsItemViewHolder cvh1 = (ReelsItemViewHolder) holder2;

                                            int[] location1 = new int[2];

                                            Objects.requireNonNull(cvh1).item_reels_video.getLocationOnScreen(location1);
                                            Rect rect_child1 = new Rect(location1[0], location1[1], location1[0] + cvh1.item_reels_video.getWidth(), location1[1] + cvh1.item_reels_video.getHeight());

                                            float rect_parent_area1 = (rect_child1.right - rect_child1.left) * (rect_child1.bottom - rect_child1.top);
                                            float x_overlap1 = Math.max(0, Math.min(rect_child1.right, rect_parent1.right) - Math.max(rect_child1.left, rect_parent1.left));
                                            float y_overlap1 = Math.max(0, Math.min(rect_child1.bottom, rect_parent1.bottom) - Math.max(rect_child1.top, rect_parent1.top));
                                            float overlapArea1 = x_overlap1 * y_overlap1;
                                            float percent1 = (overlapArea1 / rect_parent_area1) * 100.0f;

                                            if (percent1 >= 90) {
                                                cvh1.item_reels_video.start();
                                                cvh1.item_reels_video.setOnPreparedListener(mp -> {
                                                    requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                    new Handler().postDelayed(() -> cvh1.item_reels_image.setVisibility(View.GONE), 500);
                                                    mp.setVolume(0f, 0f);
                                                    mp.setLooping(true);
                                                });
                                            } else {
                                                cvh1.item_reels_video.seekTo(1);
                                                cvh1.item_reels_video.pause();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder {

        @SuppressLint("StaticFieldLeak")
        public static TextView commentCount;
        @SuppressLint("StaticFieldLeak")
        public static LinearLayout comment_layout;
        SliderView sliderView, sliderViewpost;
        TextView username, text_content, head_content, likesCount, minsago, writecomment, name_cmnt1, cmnt1, cmnt1_minsago, name_cmnt2, cmnt2, cmnt2_minsago, view_all_reels, type_something, comm_heading, view_all, view_all_feeds;
        ImageView userimage, like, commentimg,profileimage, menuPost, share, like_image, comment_image,dp_cmnt1,dp_cmnt2,type_dp;
        ApplexLinkPreview LinkPreview;
        com.applex.utsav.LinkPreview.ApplexLinkPreviewShort link_preview1, link_preview2;
        LinearLayout itemHome, commentLayout1, commentLayout2, like_layout, new_post_layout, newPostIconsLL, reels_item, slider_item, committee_item, feeds_item;
        LottieAnimationView dhak_anim;
        View view;
        RelativeLayout normal_item, rlLayout;
        RecyclerView cRecyclerView, fRecyclerView, tagList, rRecyclerView;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);

            sliderView = itemView.findViewById(R.id.imageSlider);
            tagList = itemView.findViewById(R.id.tagsList);
            username = itemView.findViewById(R.id.username);
            text_content = itemView.findViewById(R.id.text_content);
            userimage = itemView.findViewById(R.id.user_image);
            sliderViewpost = itemView.findViewById(R.id.post_image);
            minsago = itemView.findViewById(R.id.mins_ago);
            like = itemView.findViewById(R.id.like);
            commentimg = itemView.findViewById(R.id.comment);
            profileimage = itemView.findViewById(R.id.profile_image);
            menuPost = itemView.findViewById(R.id.delete_post);
            writecomment = itemView.findViewById(R.id.write_comment);
            itemHome = itemView.findViewById(R.id.item_home);
            share = itemView.findViewById(R.id.share);
            LinkPreview = itemView.findViewById(R.id.LinkPreView);

            like_image = itemView.findViewById(R.id.like_image);
            comment_image = itemView.findViewById(R.id.comment_image);
            likesCount = itemView.findViewById(R.id.no_of_likes);
            commentCount = itemView.findViewById(R.id.no_of_comments);
            like_layout = itemView.findViewById(R.id.like_layout);
            comment_layout = itemView.findViewById(R.id.comment_layout);

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
            rRecyclerView = itemView.findViewById(R.id.reelsRecycler);
            type_dp = itemView.findViewById(R.id.Pdp);
            type_something = itemView.findViewById(R.id.type_smthng);
            new_post_layout = itemView.findViewById(R.id.type_something);
            newPostIconsLL= itemView.findViewById(R.id.post_icons_ll);

            slider_item = itemView.findViewById(R.id.slider_item);
            reels_item = itemView.findViewById(R.id.reels_item);
            normal_item = itemView.findViewById(R.id.normal_item);
            head_content = itemView.findViewById(R.id.head_content);
            dhak_anim = itemView.findViewById(R.id.dhak_anim);
            rlLayout = itemView.findViewById(R.id.rlLayout);

            committee_item = itemView.findViewById(R.id.committee_item);
            view_all = itemView.findViewById(R.id.community_view_all);
            cRecyclerView = itemView.findViewById(R.id.communityRecycler);
            comm_heading = itemView.findViewById(R.id.com_heading);

            fRecyclerView = itemView.findViewById(R.id.feedsRecycler);
            feeds_item = itemView.findViewById(R.id.feeds_item);
            view_all_feeds = itemView.findViewById(R.id.view_all_feeds);
            view = itemView.findViewById(R.id.view);
        }
    }

    private void buildReelsRecyclerView(int position, ProgrammingViewHolder pvh) {

        if(pvh != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            pvh.rRecyclerView.setHasFixedSize(true);
            pvh.rRecyclerView.setLayoutManager(layoutManager);
            pvh.rRecyclerView.setNestedScrollingEnabled(true);
            pvh.rRecyclerView.setItemViewCacheSize(10);
            pvh.rRecyclerView.setDrawingCacheEnabled(true);

            SnapHelper snapHelper = new PagerSnapHelper();
            pvh.rRecyclerView.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(pvh.rRecyclerView);

            PagedList.Config config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(10)
                    .setPageSize(1)
                    .setPrefetchDistance(0)
                    .setEnablePlaceholders(true)
                    .build();

            FirestorePagingOptions<ReelsPostModel> options = new FirestorePagingOptions.Builder<ReelsPostModel>()
                    .setLifecycleOwner(this)
                    .setQuery(reels_query, config, snapshot -> {
                        ReelsPostModel reelsPostModel = new ReelsPostModel();
                        if(snapshot.exists()) {
                            reelsPostModel = snapshot.toObject(ReelsPostModel.class);
                            Objects.requireNonNull(reelsPostModel).setDocID(snapshot.getId());
                            lastReelDocument = snapshot;
                        }
                        return reelsPostModel;
                    })
                    .build();

            reelsAdapter = new FirestorePagingAdapter<ReelsPostModel, ReelsItemViewHolder>(options) {
                @SuppressLint("SetTextI18n")
                @Override
                protected void onBindViewHolder(@NonNull ReelsItemViewHolder holder, int position, @NonNull ReelsPostModel currentItem) {
                    String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                    holder.reels_mins_ago.setText(timeAgo);
                    if (timeAgo != null) {
                        if (timeAgo.matches("just now")) {
                            holder.reels_mins_ago.setTextColor(Color.parseColor("#00C853"));
                        } else {
                            holder.reels_mins_ago.setTextColor(getResources().getColor(R.color.white));
                        }
                    }

                    holder.item_reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
                    holder.item_reels_video.start();

                    Picasso.get().load(currentItem.getFrame()).fit().into(holder.item_reels_image);

                    holder.item_reels_video.setOnPreparedListener(mp -> {
                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        new Handler().postDelayed(() -> holder.item_reels_image.setVisibility(View.GONE), 500);
                        if(position == 1) {
                            holder.item_reels_video.seekTo(1);
                            holder.item_reels_video.pause();
                        }
                        mp.setVolume(0f, 0f);
                        mp.setLooping(true);
                    });

                    holder.video_time.setText(currentItem.getDuration());
                    if(currentItem.getCommittee_name().length() > 15) {
                        holder.pujo_com_name.setText(currentItem.getCommittee_name().substring(0, 15) + "...");
                    } else {
                        holder.pujo_com_name.setText(currentItem.getCommittee_name());
                    }

                    if(holder.item_reels_video.getVisibility() == View.VISIBLE) {
                        holder.item_reels_video.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                            intent.putExtra("bool", "1");
                            intent.putExtra("from", "com");
                            intent.putExtra("docID", currentItem.getDocID());
                            requireActivity().startActivity(intent);
                        });
                    }
                    else if(holder.item_reels_image.getVisibility() == View.VISIBLE) {
                        holder.item_reels_image.setOnClickListener(v -> {
                            Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                            intent.putExtra("bool", "1");
                            intent.putExtra("from", "com");
                            intent.putExtra("docID", currentItem.getDocID());
                            requireActivity().startActivity(intent);
                        });
                    }

                    if (currentItem.getCommittee_dp() != null && !currentItem.getCommittee_dp().isEmpty()) {
                        Picasso.get().load(currentItem.getCommittee_dp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(holder.pujo_com_dp, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    if(currentItem.getGender()!=null){
                                        if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                                            holder.pujo_com_dp.setImageResource(R.drawable.ic_female);
                                        }
                                        else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                                            holder.pujo_com_dp.setImageResource(R.drawable.ic_male);
                                        }
                                        else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                                            holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    }
                                    else {
                                        holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                }
                            });
                    }
                    else {
                        if(currentItem.getGender() != null) {
                            if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")) {
                                holder.pujo_com_dp.setImageResource(R.drawable.ic_female);
                            }
                            else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")) {
                                holder.pujo_com_dp.setImageResource(R.drawable.ic_male);
                            }
                            else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")) {
                                holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        }
                        else {
                            holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }

                    holder.pujo_com_dp.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfileCommittee.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

                    holder.pujo_com_name.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfileCommittee.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

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
                                                        if(getItemCount() == 0) {
                                                            pvh.reels_item.setVisibility(View.GONE);
                                                        }
                                                    });
                                            postMenuDialog.dismiss();
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                        .setCancelable(true)
                                        .show();
                            });

                            postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v12 -> {
                                link = "https://www.applex.in/utsav-app/clips/" + "1/" + currentItem.getDocID();
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
                                        .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Reel has been reported."));
                                postMenuDialog.dismiss();
                            });

                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.show();

                        } else {
                            postMenuDialog = new BottomSheetDialog(requireActivity());
                            postMenuDialog.setContentView(R.layout.dialog_post_menu);
                            postMenuDialog.setCanceledOnTouchOutside(TRUE);

                            postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v13 -> {
                                link = "https://www.applex.in/utsav-app/clips/" + "1/" + currentItem.getDocID();
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
                                        .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Reel has been reported."));
                                postMenuDialog.dismiss();
                            });

                            Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            postMenuDialog.show();
                        }
                    });
                }

                @Override
                public void onViewDetachedFromWindow(@NonNull ReelsItemViewHolder holder) {
                    super.onViewDetachedFromWindow(holder);
                    holder.item_reels_image.setVisibility(View.VISIBLE);
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
                    switch (state) {
                        case ERROR:
                            BasicUtility.showToast(getActivity(), "Something went wrong...");
                            break;
                        case FINISHED:
                            if(reelsAdapter.getItemCount() == 0) {
                                pvh.reels_item.setVisibility(View.GONE);
                            } else {
                                pvh.reels_item.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            };

            pvh.rRecyclerView.setAdapter(reelsAdapter);
            positions.add(position);

            RecyclerView.LayoutManager manager = pvh.rRecyclerView.getLayoutManager();
            pvh.rRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == 0) {
                        int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
                        int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

                        if (firstVisiblePosition >= 0) {
                            Rect rect_parent = new Rect();
                            pvh.rRecyclerView.getGlobalVisibleRect(rect_parent);

                            for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                                final RecyclerView.ViewHolder holder = pvh.rRecyclerView.findViewHolderForAdapterPosition(i);
                                ReelsItemViewHolder cvh = (ReelsItemViewHolder) holder;

                                int[] location = new int[2];
                                Objects.requireNonNull(cvh).item_reels_video.getLocationOnScreen(location);

                                Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.item_reels_video.getWidth(), location[1] + cvh.item_reels_video.getHeight());

                                float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                                float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                                float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                                float overlapArea = x_overlap * y_overlap;
                                float percent = (overlapArea / rect_parent_area) * 100.0f;

                                if (percent >= 90) {
                                    cvh.item_reels_video.start();
                                    cvh.item_reels_video.setOnPreparedListener(mp -> {
                                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        new Handler().postDelayed(() -> cvh.item_reels_image.setVisibility(View.GONE), 500);
                                        mp.setVolume(0f, 0f);
                                        mp.setLooping(true);
                                    });
                                } else {
                                    cvh.item_reels_video.seekTo(1);
                                    cvh.item_reels_video.pause();
                                }
                            }
                        }
                    }
                }
            });
        }
        else {
            BasicUtility.showToast(getActivity(), "Something went wrong...");
        }
    }

    private static class ReelsItemViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout item_reels;
        VideoView item_reels_video;
        TextView video_time, reels_mins_ago;
        ImageView pujo_com_dp, reels_more, item_reels_image;
        TextView pujo_com_name;

        ReelsItemViewHolder(View itemView) {
            super(itemView);

            item_reels = itemView.findViewById(R.id.item_reels);
            item_reels_video = itemView.findViewById(R.id.item_reels_video);
            video_time = itemView.findViewById(R.id.video_time);
            pujo_com_dp = itemView.findViewById(R.id.pujo_com_dp);
            pujo_com_name = itemView.findViewById(R.id.pujo_com_name);
            reels_more =  itemView.findViewById(R.id.reels_more);
            item_reels_image = itemView.findViewById(R.id.item_reels_image);
            reels_mins_ago = itemView.findViewById(R.id.reels_mins_ago);
        }
    }

    private void buildCommunityRecyclerView(RecyclerView cRecyclerView, int position) {
        cRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, RecyclerView.HORIZONTAL, false);
        cRecyclerView.setLayoutManager(gridLayoutManager);
        cRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ArrayList<BaseUserModel> committees = new ArrayList<>();
        Query query;

        if(position == 4) {
            query = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .whereEqualTo("type", "com")
                        .orderBy("lastVisitTime", Query.Direction.DESCENDING)
                        .limit(15);
        } else {
            query = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .whereEqualTo("type", "com")
                        .orderBy("upvotes", Query.Direction.DESCENDING)
                        .limit(15);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document: queryDocumentSnapshots) {
                if(document.exists()) {
                    BaseUserModel communityModel1 = document.toObject(BaseUserModel.class);
                    committees.add(communityModel1);
                }
            }
            if(committees.size()>0) {
                CommitteeTopAdapter communityAdapter= new CommitteeTopAdapter(committees, getActivity(), position);
                cRecyclerView.setAdapter(communityAdapter);
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error Community", Toast.LENGTH_LONG).show());
    }

    private void buildFeedsRecyclerView(ProgrammingViewHolder pvh) {
        if(pvh != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            pvh.fRecyclerView.setHasFixedSize(true);
            pvh.fRecyclerView.setLayoutManager(layoutManager);
            pvh.fRecyclerView.setNestedScrollingEnabled(true);
            pvh.fRecyclerView.setItemViewCacheSize(10);
            pvh.fRecyclerView.setDrawingCacheEnabled(true);

            PagedList.Config config = new PagedList.Config.Builder()
                    .setInitialLoadSizeHint(5)
                    .setPageSize(1)
                    .setPrefetchDistance(0)
                    .setEnablePlaceholders(true)
                    .build();

            FirestorePagingOptions<HomePostModel> options = new FirestorePagingOptions.Builder<HomePostModel>()
                    .setLifecycleOwner(this)
                    .setQuery(feeds_query, config, snapshot -> {
                        HomePostModel homePostModel = new HomePostModel();
                        if (snapshot.exists()) {
                            homePostModel = snapshot.toObject(HomePostModel.class);
                            Objects.requireNonNull(homePostModel).setDocID(snapshot.getId());
                            lastfeedDocument = snapshot;
                        }
                        return homePostModel;
                    })
                    .build();

            feedsAdapter = new FirestorePagingAdapter<HomePostModel, FeedsItemViewHolder>(options) {
                @SuppressLint("SetTextI18n")
                @Override
                protected void onBindViewHolder(@NonNull FeedsItemViewHolder holder, int position, @NonNull HomePostModel currentItem) {
                    String timeAgo = BasicUtility.getTimeAgo(currentItem.getTs());
                    holder.feeds_mins_ago.setText(timeAgo);
                    if (timeAgo != null) {
                        if (timeAgo.matches("just now")) {
                            holder.feeds_mins_ago.setTextColor(Color.parseColor("#00C853"));
                        } else {
                            holder.feeds_mins_ago.setTextColor(Color.parseColor("#aa212121"));
                        }
                    }

                    holder.profile_name.setText(currentItem.getUsN());

                    if (currentItem.getDp() != null && !currentItem.getDp().isEmpty()) {
                        Picasso.get().load(currentItem.getDp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(holder.profile_pic, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    if(currentItem.getGender() != null) {
                                        if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                                            holder.profile_pic.setImageResource(R.drawable.ic_female);
                                        }
                                        else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                                            holder.profile_pic.setImageResource(R.drawable.ic_male);
                                        }
                                        else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                                            holder.profile_pic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                        }
                                    } else {
                                        holder.profile_pic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                    }
                                }
                            });
                    } else {
                        if(currentItem.getGender() != null) {
                            if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                                holder.profile_pic.setImageResource(R.drawable.ic_female);
                            }
                            else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                                holder.profile_pic.setImageResource(R.drawable.ic_male);
                            }
                            else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                                holder.profile_pic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                            }
                        } else {
                            holder.profile_pic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }

                    holder.profile_pic.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfileUser.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

                    holder.profile_name.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), ActivityProfileUser.class);
                        intent.putExtra("uid", currentItem.getUid());
                        startActivity(intent);
                    });

                    if(currentItem.getTxt() != null &&!currentItem.getTxt().isEmpty() && currentItem.getImg() == null) {
                        holder.text_without_image.setVisibility(View.VISIBLE);
                        holder.slider_image_without_text.setVisibility(View.GONE);
                        holder.layout_with_text_and_image.setVisibility(View.GONE);

                        holder.text_without_image.setText(currentItem.getTxt());
                    }
                    else if(currentItem.getImg() != null && (currentItem.getTxt() == null || currentItem.getTxt().isEmpty())) {
                        holder.text_without_image.setVisibility(View.GONE);
                        holder.slider_image_without_text.setVisibility(View.VISIBLE);
                        holder.layout_with_text_and_image.setVisibility(View.GONE);

                        holder.slider_image_without_text.setIndicatorAnimation(IndicatorAnimations.SCALE);
                        holder.slider_image_without_text.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                        holder.slider_image_without_text.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                        holder.slider_image_without_text.setIndicatorSelectedColor(Color.WHITE);
                        holder.slider_image_without_text.setIndicatorUnselectedColor(R.color.colorAccent);
                        holder.slider_image_without_text.setAutoCycle(false);

                        SliderAdapter sliderAdapter = new SliderAdapter(requireActivity(), currentItem.getImg(), currentItem);
                        holder.slider_image_without_text.setSliderAdapter(sliderAdapter);
                    }
                    else if(currentItem.getImg() != null && currentItem.getTxt() != null && !currentItem.getTxt().isEmpty()) {
                        holder.text_without_image.setVisibility(View.GONE);
                        holder.slider_image_without_text.setVisibility(View.GONE);
                        holder.layout_with_text_and_image.setVisibility(View.VISIBLE);

                        if(currentItem.getTxt().length() < 35) {
                            int width = (int)getResources().getDimension(R.dimen.image_width);
                            int height = (int)getResources().getDimension(R.dimen.image_height_large);
                            setDimensions(holder.slider_image_with_text, width, height);
                        }
                        else if(currentItem.getTxt().length() >= 35 && currentItem.getTxt().length() < 70) {
                            int width = (int)getResources().getDimension(R.dimen.image_width);
                            int height = (int)getResources().getDimension(R.dimen.image_height_medium);
                            setDimensions(holder.slider_image_with_text, width, height);
                        }
                        else if(currentItem.getTxt().length() >= 70) {
                            int width = (int)getResources().getDimension(R.dimen.image_width);
                            int height = (int)getResources().getDimension(R.dimen.image_height_small);
                            setDimensions(holder.slider_image_with_text, width, height);
                        }

                        holder.text_with_image.setText(currentItem.getTxt());

                        holder.slider_image_with_text.setIndicatorAnimation(IndicatorAnimations.SCALE);
                        holder.slider_image_with_text.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                        holder.slider_image_with_text.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                        holder.slider_image_with_text.setIndicatorSelectedColor(Color.WHITE);
                        holder.slider_image_with_text.setIndicatorUnselectedColor(R.color.colorAccent);
                        holder.slider_image_with_text.setAutoCycle(false);

                        SliderAdapter sliderAdapter = new SliderAdapter(requireActivity(), currentItem.getImg(), currentItem);
                        holder.slider_image_with_text.setSliderAdapter(sliderAdapter);
                    }

                    if(currentItem.getCmtNo() == 0 && currentItem.getLikeL() != null && currentItem.getLikeL().size() != 0) {
                        holder.comment_count.setVisibility(View.GONE);
                        holder.comment_image.setVisibility(View.GONE);
                        holder.view1.setVisibility(View.GONE);
                        holder.view2.setVisibility(View.GONE);
                        holder.likes_count.setVisibility(View.VISIBLE);
                        holder.like_image.setVisibility(View.VISIBLE);

                        holder.likes_count.setText(Integer.toString(currentItem.getLikeL().size()));
                    }
                    else if(currentItem.getCmtNo() != 0 && currentItem.getLikeL() != null && currentItem.getLikeL().size() == 0) {
                        holder.comment_count.setVisibility(View.VISIBLE);
                        holder.comment_image.setVisibility(View.VISIBLE);
                        holder.view1.setVisibility(View.GONE);
                        holder.view2.setVisibility(View.GONE);
                        holder.likes_count.setVisibility(View.GONE);
                        holder.like_image.setVisibility(View.GONE);

                        holder.comment_count.setText(Long.toString(currentItem.getCmtNo()));
                    }
                    else if(currentItem.getCmtNo() != 0 && currentItem.getLikeL() != null && currentItem.getLikeL().size() != 0) {
                        holder.comment_count.setVisibility(View.VISIBLE);
                        holder.comment_image.setVisibility(View.VISIBLE);
                        holder.view1.setVisibility(View.VISIBLE);
                        holder.view2.setVisibility(View.VISIBLE);
                        holder.likes_count.setVisibility(View.VISIBLE);
                        holder.like_image.setVisibility(View.VISIBLE);

                        holder.likes_count.setText(Integer.toString(currentItem.getLikeL().size()));
                        holder.comment_count.setText(Long.toString(currentItem.getCmtNo()));
                    }
                    else {
                        holder.comment_count.setVisibility(View.GONE);
                        holder.comment_image.setVisibility(View.GONE);
                        holder.view1.setVisibility(View.GONE);
                        holder.view2.setVisibility(View.GONE);
                        holder.likes_count.setVisibility(View.GONE);
                        holder.like_image.setVisibility(View.GONE);
                    }

                    holder.text_without_image.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreText.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        if (currentItem.getImg() != null && currentItem.getImg().size() > 0) {
                            Bundle args = new Bundle();
                            args.putSerializable("ARRAYLIST", currentItem.getImg());
                            intent.putExtra("BUNDLE", args);
                        }
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("bool", "3");
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("type", currentItem.getType());
                        intent.putExtra("gender", currentItem.getGender());
                        startActivity(intent);
                    });

                    holder.text_with_image.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        if (currentItem.getImg() != null && currentItem.getImg().size() > 0) {
                            Bundle args = new Bundle();
                            args.putSerializable("ARRAYLIST", currentItem.getImg());
                            intent.putExtra("BUNDLE", args);
                        }
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("bool", "3");
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("type", currentItem.getType());
                        intent.putExtra("gender", currentItem.getGender());
                        startActivity(intent);
                    });

                    holder.stats_layout.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ViewMoreHome.class);
                        intent.putExtra("username", currentItem.getUsN());
                        intent.putExtra("userdp", currentItem.getDp());
                        intent.putExtra("docID", currentItem.getDocID());
                        StoreTemp.getInstance().setTagTemp(currentItem.getTagL());
                        intent.putExtra("comName", currentItem.getComName());
                        intent.putExtra("comID", currentItem.getComID());
                        intent.putExtra("likeL", currentItem.getLikeL());
                        if (currentItem.getImg() != null && currentItem.getImg().size() > 0) {
                            Bundle args = new Bundle();
                            args.putSerializable("ARRAYLIST", currentItem.getImg());
                            intent.putExtra("BUNDLE", args);
                        }
                        intent.putExtra("postText", currentItem.getTxt());
                        intent.putExtra("bool", "3");
                        intent.putExtra("commentNo", Long.toString(currentItem.getCmtNo()));
                        intent.putExtra("newTs", Long.toString(currentItem.getNewTs()));
                        intent.putExtra("uid", currentItem.getUid());
                        intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                        intent.putExtra("type", currentItem.getType());
                        intent.putExtra("gender", currentItem.getGender());
                        startActivity(intent);
                    });
                }

                @NonNull
                @Override
                public FeedsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                    LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                    View v = layoutInflater.inflate(R.layout.item_home_feeds_post, viewGroup, false);
                    return new FeedsItemViewHolder(v);
                }

                @Override
                public int getItemViewType(int position) { return position; }

                @Override
                protected void onLoadingStateChanged(@NonNull LoadingState state) {
                    super.onLoadingStateChanged(state);
                    switch (state) {
                        case ERROR:
                            BasicUtility.showToast(getActivity(), "Something went wrong...");
                            break;
                        case FINISHED:
                            if(feedsAdapter.getItemCount() == 0) {
                                pvh.feeds_item.setVisibility(View.GONE);
                            } else {
                                pvh.feeds_item.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                }
            };
            pvh.fRecyclerView.setAdapter(feedsAdapter);
        }
        else {
            BasicUtility.showToast(requireActivity(), "Something went wrong...");
        }
    }

    private static class FeedsItemViewHolder extends RecyclerView.ViewHolder {

        ImageView profile_pic, like_image, comment_image;
        TextView profile_name, text_without_image, text_with_image, likes_count, comment_count, feeds_mins_ago;
        CardView feeds_card;
        LinearLayout stats_layout, layout_with_text_and_image;
        View view1, view2;
        SliderView slider_image_without_text, slider_image_with_text;

        FeedsItemViewHolder(View itemView) {
            super(itemView);

            feeds_card = itemView.findViewById(R.id.feeds_card);
            slider_image_with_text = itemView.findViewById(R.id.item_image);
            text_with_image = itemView.findViewById(R.id.item_text);
            slider_image_without_text = itemView.findViewById(R.id.item_image_without_text);
            text_without_image = itemView.findViewById(R.id.item_text_without_image);
            layout_with_text_and_image = itemView.findViewById(R.id.layout_with_text_and_image);
            stats_layout = itemView.findViewById(R.id.stats_layout);
            profile_pic = itemView.findViewById(R.id.profile_pic);
            profile_name = itemView.findViewById(R.id.profile_name);
            like_image = itemView.findViewById(R.id.like_image);
            comment_image = itemView.findViewById(R.id.comment_image);
            likes_count = itemView.findViewById(R.id.no_of_likes);
            comment_count = itemView.findViewById(R.id.no_of_comments);
            feeds_mins_ago = itemView.findViewById(R.id.feeds_mins_ago);
            view1 = itemView.findViewById(R.id.view1);
            view2 = itemView.findViewById(R.id.view2);
        }
    }

    private void setDimensions(View view, int width, int height){
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        if((changed > 0 || delete > 0)) {
            buildRecyclerView();
            changed = 0;
            delete = 0;
        }
        super.onResume();

        RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
        int firstVisiblePosition = ((LinearLayoutManager) Objects.requireNonNull(manager)).findFirstVisibleItemPosition();
        int lastVisiblePosition = ((LinearLayoutManager) manager).findLastVisibleItemPosition();

        if (firstVisiblePosition >= 0) {
            Rect rect_parent = new Rect();
            mRecyclerView.getGlobalVisibleRect(rect_parent);

            for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                if(positions != null && positions.contains(i)) {

                    final RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(i);
                    ProgrammingViewHolder cvh = (ProgrammingViewHolder) holder;

                    int[] location = new int[2];
                    Objects.requireNonNull(cvh).reels_item.getLocationOnScreen(location);
                    Rect rect_child = new Rect(location[0], location[1], location[0] + cvh.reels_item.getWidth(), location[1] + cvh.reels_item.getHeight());

                    float rect_parent_area = (rect_child.right - rect_child.left) * (rect_child.bottom - rect_child.top);
                    float x_overlap = Math.max(0, Math.min(rect_child.right, rect_parent.right) - Math.max(rect_child.left, rect_parent.left));
                    float y_overlap = Math.max(0, Math.min(rect_child.bottom, rect_parent.bottom) - Math.max(rect_child.top, rect_parent.top));
                    float overlapArea = x_overlap * y_overlap;
                    float percent = (overlapArea / rect_parent_area) * 100.0f;

                    if (percent >= 90) {
                        RecyclerView.LayoutManager manager1 = Objects.requireNonNull(cvh).rRecyclerView.getLayoutManager();

                        int firstVisiblePosition1 = ((LinearLayoutManager) Objects.requireNonNull(manager1)).findFirstVisibleItemPosition();
                        int lastVisiblePosition1 = ((LinearLayoutManager) manager1).findLastVisibleItemPosition();

                        if (firstVisiblePosition1 >= 0) {
                            Rect rect_parent1 = new Rect();
                            cvh.rRecyclerView.getGlobalVisibleRect(rect_parent1);

                            for (int j = firstVisiblePosition1; j <= lastVisiblePosition1; j++) {
                                final RecyclerView.ViewHolder holder2 = cvh.rRecyclerView.findViewHolderForAdapterPosition(j);
                                ReelsItemViewHolder cvh1 = (ReelsItemViewHolder) holder2;

                                int[] location1 = new int[2];

                                Objects.requireNonNull(cvh1).item_reels_video.getLocationOnScreen(location1);
                                cvh1.item_reels_image.setVisibility(View.VISIBLE);
                                Rect rect_child1 = new Rect(location1[0], location1[1], location1[0] + cvh1.item_reels_video.getWidth(), location1[1] + cvh1.item_reels_video.getHeight());

                                float rect_parent_area1 = (rect_child1.right - rect_child1.left) * (rect_child1.bottom - rect_child1.top);
                                float x_overlap1 = Math.max(0, Math.min(rect_child1.right, rect_parent1.right) - Math.max(rect_child1.left, rect_parent1.left));
                                float y_overlap1 = Math.max(0, Math.min(rect_child1.bottom, rect_parent1.bottom) - Math.max(rect_child1.top, rect_parent1.top));
                                float overlapArea1 = x_overlap1 * y_overlap1;
                                float percent1 = (overlapArea1 / rect_parent_area1) * 100.0f;

                                if (percent1 >= 90) {
                                    cvh1.item_reels_video.start();
                                    cvh1.item_reels_video.setOnPreparedListener(mp -> {
                                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        new Handler().postDelayed(() -> cvh1.item_reels_image.setVisibility(View.GONE), 500);
                                        mp.setVolume(0f, 0f);
                                        mp.setLooping(true);
                                    });
                                } else {
                                    cvh1.item_reels_video.seekTo(1);
                                    cvh1.item_reels_video.pause();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}