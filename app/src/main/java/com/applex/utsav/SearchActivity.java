package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.applex.utsav.adapters.UserSearchAdapter;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.models.UserSearchModel;
import com.applex.utsav.preferences.IntroPref;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private IntroPref introPref;
    private ImageButton back, search;
    private EditText searchKey;
    @SuppressLint("StaticFieldLeak")
    public static ImageView nosearch;
    private ProgressBar progressMore, contentProgress;
    private RecyclerView search_recycler;
    public static RecyclerView history_recycler;
    private Button sName, sCity, sState;
    private LinearLayoutManager layoutManager;
    private int selected_button = 0;
    private boolean present2 = false;
    private boolean present1 = false;
    private String SEARCH;
    private FirestorePagingAdapter adapter1;
    private ArrayList<UserSearchModel> userSearchModelArrayList;
    private ArrayList<BaseUserModel> userList;
    private UserSearchAdapter userSearchAdapter;
    private RelativeLayout search_layout, history_layout;
    private TextView textView;

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

        /////////////////DAY OR NIGHT MODE///////////////////
        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Mode/night_mode")
                    .get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if(task.getResult().getBoolean("night_mode")) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        } else if(introPref.getTheme() == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if(introPref.getTheme() == 3) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_search);

        back = findViewById(R.id.back);
        search = findViewById(R.id.searchButton);
        searchKey = findViewById(R.id.search);
        search_recycler = findViewById(R.id.search_recycler);
        sName = findViewById(R.id.Sfirstname);
        sCity = findViewById(R.id.Scity);
        sState = findViewById(R.id.Sstate);
        nosearch = findViewById(R.id.no_search);
        progressMore = findViewById(R.id.content_progress_search);
        contentProgress = findViewById(R.id.content_progress);
        textView = findViewById(R.id.textview);
        history_recycler = findViewById(R.id.history_recycler);
        search_layout = findViewById(R.id.search_recycler_layout);
        history_layout = findViewById(R.id.history_recycler_layout);

        searchKey.setOnEditorActionListener(editorActionListener);
        userList = new ArrayList<>();
        userSearchModelArrayList = new ArrayList<>();

        back.setOnClickListener(v -> super.onBackPressed());

        if(introPref.getTheme() == 1) {
            FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid())
                    .addSnapshotListener(SearchActivity.this, (value, error) -> {
                        if(value != null) {
                            if(value.getBoolean("listener") != null && value.getBoolean("listener")) {
                                FirebaseFirestore.getInstance().document("Mode/night_mode")
                                        .get().addOnCompleteListener(task -> {
                                    if(task.isSuccessful()) {
                                        if(task.getResult().getBoolean("night_mode")) {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                        } else {
                                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                        }
                                    } else {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    }
                                    new Handler().postDelayed(() -> {
                                        MainActivity.mode_changed = 1;
                                        FirebaseFirestore.getInstance().document("Users/"+ FirebaseAuth.getInstance().getUid()).update("listener", false);
                                        startActivity(new Intent(SearchActivity.this, SearchActivity.class));
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }, 200);
                                });
                            }
                        }
                    });
        }

        ///////////////Set Image Bitmap/////////////////////
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {

            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                options.inSampleSize = Math.round((float) width / (float) displayWidth);
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            nosearch.setImageBitmap(scaledBitmap);
        }
        else if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {

            Display display = getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                options.inSampleSize = Math.round((float) width / (float) displayWidth);
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            nosearch.setImageBitmap(scaledBitmap);
        }
        ///////////////Set Image Bitmap/////////////////////

        buildSearchHistoryRecyclerView();

        search.setOnClickListener(v -> {
            SEARCH = searchKey.getText().toString().trim();
            if(!SEARCH.isEmpty()){
                userList.clear();
                contentProgress.setVisibility(View.VISIBLE);
                buildRecycler("small_name");
            }
        });

        sName.setOnClickListener(v -> {
            sName.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            sName.setTextColor(getResources().getColor(R.color.white));

            sCity.setBackgroundResource(R.drawable.add_tags_button_background);
            sCity.setBackgroundTintList(null);
            sCity.setTextColor(getResources().getColor(R.color.black));

            sState.setBackgroundResource(R.drawable.add_tags_button_background);
            sState.setBackgroundTintList(null);
            sState.setTextColor(getResources().getColor(R.color.black));

            selected_button = 1;

            if (!(searchKey.getText().toString().isEmpty())){
                userList.clear();
                contentProgress.setVisibility(View.VISIBLE);
                SEARCH = searchKey.getText().toString().trim();
                buildRecycler("small_name");
            }

            search.setOnClickListener(v13 -> {
                SEARCH = searchKey.getText().toString().trim();
                if(!SEARCH.isEmpty()){
                    userList.clear();
                    buildRecycler("small_name");
                    contentProgress.setVisibility(View.VISIBLE);
                }
            });
        });

        sCity.setOnClickListener(v -> {
            sCity.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            sCity.setTextColor(getResources().getColor(R.color.white));

            sName.setBackgroundResource(R.drawable.add_tags_button_background);
            sName.setBackgroundTintList(null);
            sName.setTextColor(getResources().getColor(R.color.black));

            sState.setBackgroundResource(R.drawable.add_tags_button_background);
            sState.setBackgroundTintList(null);
            sState.setTextColor(getResources().getColor(R.color.black));

            selected_button = 2;

            if (!(searchKey.getText().toString().isEmpty())){
                userList.clear();
                contentProgress.setVisibility(View.VISIBLE);
                SEARCH = searchKey.getText().toString().trim();
                buildRecycler("city");
            }

            search.setOnClickListener(v12 -> {
                SEARCH = searchKey.getText().toString().trim();
                if(!SEARCH.isEmpty()){
                    contentProgress.setVisibility(View.VISIBLE);
                    userList.clear();
                    buildRecycler("city");
                }
            });
        });

        sState.setOnClickListener(v -> {
            sState.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            sState.setTextColor(getResources().getColor(R.color.white));

            sName.setBackgroundResource(R.drawable.add_tags_button_background);
            sName.setBackgroundTintList(null);
            sName.setTextColor(getResources().getColor(R.color.black));

            sCity.setBackgroundResource(R.drawable.add_tags_button_background);
            sCity.setBackgroundTintList(null);
            sCity.setTextColor(getResources().getColor(R.color.black));

            selected_button = 3;

            if (!(searchKey.getText().toString().isEmpty())){
                userList.clear();
                contentProgress.setVisibility(View.VISIBLE);
                SEARCH = searchKey.getText().toString().trim();
                buildRecycler("state");
            }

            search.setOnClickListener(v1 -> {
                SEARCH = searchKey.getText().toString().trim();
                if(!SEARCH.isEmpty()){
                    contentProgress.setVisibility(View.VISIBLE);
                    userList.clear();
                    buildRecycler("state");
                }
            });
        });

        search_recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        search_recycler.setLayoutManager(layoutManager);
        search_recycler.setItemViewCacheSize(20);
    }

    private void buildSearchHistoryRecyclerView() {
        textView.setText(R.string.Recent_searches);
        search_layout.setVisibility(View.GONE);
        history_layout.setVisibility(View.VISIBLE);

        userSearchModelArrayList = introPref.getRecentSearchHistory();

        if(userSearchModelArrayList == null || userSearchModelArrayList.size() == 0) {
            history_recycler.setVisibility(View.GONE);
            nosearch.setVisibility(View.VISIBLE);
        }
        else {
            nosearch.setVisibility(View.GONE);
            history_recycler.setVisibility(View.VISIBLE);
            if(userSearchModelArrayList.size() == 11) {
                userSearchModelArrayList.remove(0);
            }
        }

        userSearchAdapter  = new UserSearchAdapter(SearchActivity.this, userSearchModelArrayList);
        history_recycler.setAdapter(userSearchAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        history_recycler.setLayoutManager(linearLayoutManager);

        userSearchAdapter.onClickListener((name, uid, type, dp, gender, position) -> {
            userSearchModelArrayList.remove(position);
            userSearchAdapter.notifyItemRemoved(position);
            UserSearchModel userSearchModel = new UserSearchModel();
            userSearchModel.setName(name);
            userSearchModel.setDp(dp);
            userSearchModel.setUid(uid);
            userSearchModel.setType(type);
            userSearchModel.setGender(gender);
            userSearchModelArrayList.add(userSearchModel);
            introPref.setRecentSearchHistory(userSearchModelArrayList);

            if(uid == null || uid.matches("")) {
                searchKey.setText(name);

                if(type.matches("small_name")) {
                    sName.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                    sName.setTextColor(getResources().getColor(R.color.white));

                    sCity.setBackgroundResource(R.drawable.add_tags_button_background);
                    sCity.setBackgroundTintList(null);
                    sCity.setTextColor(getResources().getColor(R.color.black));

                    sState.setBackgroundResource(R.drawable.add_tags_button_background);
                    sState.setBackgroundTintList(null);
                    sState.setTextColor(getResources().getColor(R.color.black));

                    selected_button = 1;
                }
                else if(type.matches("city")) {
                    sCity.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                    sCity.setTextColor(getResources().getColor(R.color.white));

                    sName.setBackgroundResource(R.drawable.add_tags_button_background);
                    sName.setBackgroundTintList(null);
                    sName.setTextColor(getResources().getColor(R.color.black));

                    sState.setBackgroundResource(R.drawable.add_tags_button_background);
                    sState.setBackgroundTintList(null);
                    sState.setTextColor(getResources().getColor(R.color.black));

                    selected_button = 2;
                }
                else if(type.matches("state")) {
                    sState.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
                    sState.setTextColor(getResources().getColor(R.color.white));

                    sName.setBackgroundResource(R.drawable.add_tags_button_background);
                    sName.setBackgroundTintList(null);
                    sName.setTextColor(getResources().getColor(R.color.black));

                    sCity.setBackgroundResource(R.drawable.add_tags_button_background);
                    sCity.setBackgroundTintList(null);
                    sCity.setTextColor(getResources().getColor(R.color.black));

                    selected_button = 3;
                }
                SEARCH = name;
                userList.clear();
                contentProgress.setVisibility(View.VISIBLE);
                buildRecycler(type);
            }
            else {
                Intent intent = new Intent(SearchActivity.this, ActivityProfile.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });
    }

    private final TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (selected_button == 0 || selected_button == 1) {
                    SEARCH = searchKey.getText().toString().trim();
                    if (!SEARCH.isEmpty()) {
                        userList.clear();
                        contentProgress.setVisibility(View.VISIBLE);
                        buildRecycler("small_name");
                    }
                } else if (selected_button == 2) {
                    SEARCH = searchKey.getText().toString().trim();
                    if (!SEARCH.isEmpty()) {
                        userList.clear();
                        contentProgress.setVisibility(View.VISIBLE);
                        buildRecycler("city");
                    }
                } else if (selected_button == 3) {
                    SEARCH = searchKey.getText().toString().trim();
                    if (!SEARCH.isEmpty()) {
                        userList.clear();
                        contentProgress.setVisibility(View.VISIBLE);
                        buildRecycler("state");
                    }
                }
//                    else if(selected_button==3){
//                        SEARCH = searchKey.getText().toString();
//                        if(!SEARCH.isEmpty()){
//
//                            userList.clear();
//
//                            contentProgress.setVisibility(View.VISIBLE);
//                            buildRecycler("username");
//
//
//                        }
//                    }
//                    else if(selected_button==4){
//                        SEARCH = searchKey.getText().toString();
//                        if(!SEARCH.isEmpty()){
//
//                            userList.clear();
//
//                            contentProgress.setVisibility(View.VISIBLE);
//                            buildRecycler("institute");
//
//                        }
//                    }
            }
            return false;
        }
    };

    private void buildRecycler(String type) {
        textView.setText(R.string.results);
        history_layout.setVisibility(View.GONE);
        search_layout.setVisibility(View.VISIBLE);

        if(userSearchModelArrayList != null) {
            for(UserSearchModel userSearchModel: userSearchModelArrayList) {
                if(SEARCH.matches(userSearchModel.getName()) || SEARCH.equals(userSearchModel.getName())) {
                    present1 = true;
                    break;
                }
            }
        }

        if(!present1) {
            UserSearchModel userSearchModel = new UserSearchModel();
            userSearchModel.setName(SEARCH);
            userSearchModel.setDp("");
            userSearchModel.setUid("");
            userSearchModel.setGender("");
            userSearchModel.setType(type);
            if(userSearchModelArrayList == null) {
                userSearchModelArrayList = new ArrayList<>();
            }
            else if(userSearchModelArrayList.size() == 10) {
                userSearchModelArrayList.remove(0);
            }
            userSearchModelArrayList.add(userSearchModel);
            introPref.setRecentSearchHistory(userSearchModelArrayList);
        }

        Query query;
        if(type.matches("small_name")){
            query = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .orderBy(type)
                    .startAt(SEARCH.toLowerCase());
        }
        else {
            query = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .orderBy(type)
                    .startAt(SEARCH);
        }

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<BaseUserModel> options = new FirestorePagingOptions.Builder<BaseUserModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    BaseUserModel user = snapshot.toObject(BaseUserModel.class);
                    user.setUid(snapshot.getId());
                    return user;
                })
                .build();

        adapter1 = new FirestorePagingAdapter<BaseUserModel, ProgrammingViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position, @NonNull BaseUserModel model) {
                holder.PName.setText(model.getName());

                if(model.getCity()!=null || model.getState()!=null){

                    if((model.getCity()!=null && model.getCity().isEmpty())
                            && (model.getState()!=null && !model.getState().isEmpty())){
                        holder.Pcity.setText(model.getState());
                    }
                    else if((model.getState()!=null && model.getState().isEmpty())
                            && (model.getCity()!=null && !model.getCity().isEmpty())){
                        holder.Pcity.setText(model.getCity());
                    }
                    else if((model.getCity()!=null && !model.getCity().isEmpty())
                            && (model.getState()!=null && !model.getState().isEmpty())){
                        holder.Pcity.setText(model.getCity()+", "+model.getState());
                    }
                    else if((model.getCity()!=null && model.getCity().isEmpty())
                            && (model.getState()!=null && model.getState().isEmpty())){
                        holder.Pcity.setVisibility(View.GONE);
                    }

                }
                else if(model.getCity()==null && model.getState()==null){
                    holder.Pcity.setVisibility(View.GONE);
                }

                String userimage_url = model.getDp();
                if(userimage_url!=null){
                        Picasso.get().load(userimage_url).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.userimage);
                }
                else{
                    if(model.getGender()!=null){
                        if (model.getGender().matches("Female") || model.getGender().matches("মহিলা")){
                            holder.userimage.setImageResource(R.drawable.ic_female);
                        }
                        else if (model.getGender().matches("Male") || model.getGender().matches("পুরুষ")){
                            holder.userimage.setImageResource(R.drawable.ic_male);
                        }
                        else if (model.getGender().matches("Others") || model.getGender().matches("অন্যান্য")){
                            holder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                    else {
                        holder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                }
//                holder.card1.setOnClickListener(v -> {
//                    Intent intent = new Intent(SearchActivity.this, ActivityProfileUser.class);
//                    intent.putExtra("uid", model.getUid());
//                    startActivity(intent);
//                });
//
//                holder.card2.setOnClickListener(v -> {
//                    Intent intent = new Intent(SearchActivity.this, ActivityProfileUser.class);
//                    intent.putExtra("uid", model.getUid());
//                    startActivity(intent);
//                });
                holder.card.setOnClickListener(v -> {
                    if(userSearchModelArrayList != null) {
                        for(UserSearchModel userSearchModel: userSearchModelArrayList) {
                            if(model.getUid().matches(userSearchModel.getUid()) || model.getUid().equals(userSearchModel.getUid())) {
                                present2 = true;
                                break;
                            }
                        }
                    }

                    if(!present2) {
                        UserSearchModel userSearchModel = new UserSearchModel();
                        userSearchModel.setName(model.getName());
                        userSearchModel.setDp(model.getDp());
                        userSearchModel.setUid(model.getUid());
                        userSearchModel.setGender(model.getGender());
                        userSearchModel.setType(type);
                        if(userSearchModelArrayList == null) {
                            userSearchModelArrayList = new ArrayList<>();
                        }
                        else {
                            userSearchModelArrayList.remove(userSearchModelArrayList.size() - 1);
                        }
                        userSearchModelArrayList.add(userSearchModel);
                        introPref.setRecentSearchHistory(userSearchModelArrayList);
                    }

                    Intent intent = new Intent(SearchActivity.this, ActivityProfile.class);
                    intent.putExtra("uid", model.getUid());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_search_profile, viewGroup, false);
                return new ProgrammingViewHolder(v);
            }
        };

        contentProgress.setVisibility(View.GONE);
        search_recycler.setAdapter(adapter1);
    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        TextView PName,Pcity,PDescription;
        ImageView userimage;
        LinearLayout card2, card;
        CardView card1;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);
            PName = itemView.findViewById(R.id.Profilename);
            Pcity = itemView.findViewById(R.id.Pcity);
            PDescription = itemView.findViewById(R.id.Pdescription);
            userimage = itemView.findViewById(R.id.Pdp);
            card2 = itemView.findViewById(R.id.profileCard2);
            card1 = itemView.findViewById(R.id.profileCard1);
            card = itemView.findViewById(R.id.profileCard);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        present2 = false;
        present1 = false;
    }

    private String getAlphaNumericString (int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(n);
        for (int i=0; i<n; i++)
        {
            int index = (int) (AlphaNumericString.length()*Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}