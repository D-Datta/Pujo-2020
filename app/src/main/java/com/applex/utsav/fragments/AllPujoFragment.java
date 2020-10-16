package com.applex.utsav.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.applex.utsav.ActivityProfileCommittee;
import com.applex.utsav.R;
import com.applex.utsav.models.BaseUserModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import java.util.Locale;
import java.util.Objects;

public class AllPujoFragment extends Fragment {

    private RecyclerView cRecyclerView;
    private ProgressBar progress;
    private ProgressBar progressMoreCom;
    private LinearLayout emptyLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirestorePagingAdapter adapter;
    private EditText searchText;
    private Button sName, sCity;
    private int selected_button=1;
    private FloatingActionButton floatingActionButton;
    private ImageView pujoimg;

    public AllPujoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        IntroPref introPref = new IntroPref(getActivity());
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        Objects.requireNonNull(getActivity()).getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        return inflater.inflate(R.layout.fragment_all_pujo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ImageView search = view.findViewById(R.id.search);
        searchText = view.findViewById(R.id.search_text);
        searchText.setOnEditorActionListener(editorActionListener);
        sName = view.findViewById(R.id.Sfirstname);
        sCity = view.findViewById(R.id.Scity);

        progress = view.findViewById(R.id.content_progress);
        progressMoreCom = view.findViewById(R.id.progress_more_comm);
        progress.setVisibility(View.VISIBLE);
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        cRecyclerView = view.findViewById(R.id.community_view_all);
        cRecyclerView.setHasFixedSize(true);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        floatingActionButton = view.findViewById(R.id.to_the_top_pujos);
        pujoimg = view.findViewById(R.id.pujoimg);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cRecyclerView.setLayoutManager(gridLayoutManager);

        ///////////////Set Image Bitmap/////////////////////
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {

            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.dark_mode_login, options);
            pujoimg.setImageBitmap(scaledBitmap);
        } else if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {

            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            int displayWidth = display.getWidth();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            int width = options.outWidth;
            if (width > displayWidth) {
                int widthRatio = Math.round((float) width / (float) displayWidth);
                options.inSampleSize = widthRatio;
            }
            options.inJustDecodeBounds = false;
            Bitmap scaledBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.light_mode_login, options);
            pujoimg.setImageBitmap(scaledBitmap);
        }
        ///////////////Set Image Bitmap/////////////////////

        buildRecyclerView("small_name", null );

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources()
                .getColor(R.color.purple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            searchText.setText(null);
            buildRecyclerView("small_name",null);
        });

        sName.setOnClickListener(view13 -> {
            sName.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            sName.setTextColor(getResources().getColor(R.color.white));

            sCity.setBackgroundResource(R.drawable.add_tags_button_background);
            sCity.setBackgroundTintList(null);
            sCity.setTextColor(getResources().getColor(R.color.black));

            selected_button = 1;
        });

        sCity.setOnClickListener(view1 -> {
            sCity.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            sCity.setTextColor(getResources().getColor(R.color.white));

            sName.setBackgroundResource(R.drawable.add_tags_button_background);
            sName.setBackgroundTintList(null);
            sName.setTextColor(getResources().getColor(R.color.black));

            selected_button = 2;
        });

        search.setOnClickListener(view12 -> {
            if(!searchText.getText().toString().isEmpty()){
                if(selected_button==1)
                    buildRecyclerView("small_name",searchText.getText().toString().trim().toLowerCase());

                else
                    buildRecyclerView("city", searchText.getText().toString().trim());
            }
        });
    }

    private void buildRecyclerView( String field, String search) {

        Query query;
        if(search != null){
            query = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("type", "com")
                    .whereGreaterThanOrEqualTo(field, search)
                    .limit(10);
        }
        else {
            query = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("type", "com")
                    .limit(10);
        }

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setEnablePlaceholders(true)
                .build();

        FirestorePagingOptions<BaseUserModel> options = new FirestorePagingOptions.Builder<BaseUserModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    BaseUserModel committeeModel = new BaseUserModel();
                    if(snapshot.exists()) {
                        committeeModel = snapshot.toObject(BaseUserModel.class);
                    }
                    return Objects.requireNonNull(committeeModel);
                })
                .build();

        adapter = new FirestorePagingAdapter<BaseUserModel, RecyclerView.ViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull BaseUserModel currentItem) {

            ProgrammingViewHolder programmingViewHolder = (ProgrammingViewHolder) holder;

                if(currentItem.getCoverpic() != null){
                    Picasso.get().load(currentItem.getCoverpic())
                            .error(R.drawable.image_background_grey)
                            .placeholder(R.drawable.image_background_grey)
                            .into(programmingViewHolder.committeeCover);
                }
                else {
                    programmingViewHolder.committeeCover.setImageResource(R.drawable.image_background_grey);
                }

                if(currentItem.getDp() != null){
                    Picasso.get().load(currentItem.getDp())
                            .error(R.drawable.image_background_grey)
                            .placeholder(R.drawable.image_background_grey)
                            .into(programmingViewHolder.committeeDp);
                }
                else {
                    programmingViewHolder.committeeDp.setImageResource(R.drawable.image_background_grey);
                }

                if(currentItem.getName() != null){
                    programmingViewHolder.committeeName.setText(currentItem.getName());
                }
                else {
                    programmingViewHolder.committeeName.setVisibility(View.GONE);
                }

                programmingViewHolder.itemView.setOnClickListener(view -> {
                    Intent intent = new Intent(getActivity(), ActivityProfileCommittee.class);
                    intent.putExtra("uid",currentItem.getUid());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_committee_grid, viewGroup, false);
                return new ProgrammingViewHolder(v);
            }

            @Override
            public int getItemViewType(int position) { return position; }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR:
//                        BasicUtility.showToast(getActivity(), "Something went wrong...");
                        break;
                    case LOADING_MORE:
                        progressMoreCom.setVisibility(View.VISIBLE);
                        break;
                    case LOADED:
                        progressMoreCom.setVisibility(View.GONE);
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(adapter.getItemCount() == 0) {
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                        else {
                            emptyLayout.setVisibility(View.GONE);
                        }
                        break;
                    case FINISHED:
                        progress.setVisibility(View.GONE);
                        progressMoreCom.setVisibility(View.GONE);
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(adapter.getItemCount() == 0) {
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                        else {
                            emptyLayout.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        };
        progress.setVisibility(View.GONE);
        progressMoreCom.setVisibility(View.GONE);
        cRecyclerView.setAdapter(adapter);

        final int[] scrollY = {0};
        cRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private static class ProgrammingViewHolder extends RecyclerView.ViewHolder {

        TextView committeeName;
        ImageView committeeCover, committeeDp;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);
            committeeName = itemView.findViewById(R.id.committee_name);
            committeeDp = itemView.findViewById(R.id.committee_dp);
            committeeCover = itemView.findViewById(R.id.committee_cover);
        }
    }

    private TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!searchText.getText().toString().isEmpty()) {
                    if (selected_button == 1)
                        buildRecyclerView("small_name", searchText.getText().toString().toLowerCase());
                    else
                        buildRecyclerView("city", searchText.getText().toString());
                }
            }
            return false;
        }
    };
}