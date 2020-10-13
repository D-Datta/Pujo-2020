package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.applex.utsav.models.NotifModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import java.util.Locale;
import java.util.Objects;

public class ActivityNotification extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView notifRecycler;
    private ProgressBar progressMore;
    private ImageView noNotif;
    public static boolean active = false;
    private FirestorePagingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntroPref introPref = new IntroPref(ActivityNotification.this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

//        /////////////////DAY OR NIGHT MODE///////////////////
//        FirebaseFirestore.getInstance().document("Mode/night_mode")
//                .addSnapshotListener(ActivityNotification.this, (value, error) -> {
//                    if(value != null) {
//                        if(value.getBoolean("night_mode")) {
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                        } else {
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                        }
//                        if(value.getBoolean("listener")) {
//                            FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
//                            startActivity(new Intent(MainActivity.this, MainActivity.class));
//                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                            finish();
//                        }
//                    } else {
//                        FirebaseFirestore.getInstance().document("Mode/night_mode").update("listener", false);
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    }
//                });
//        /////////////////DAY OR NIGHT MODE///////////////////

        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        progressMore = findViewById(R.id.progress_more);
        progressMore.setVisibility(View.GONE);
        noNotif = findViewById(R.id.no_recent_notiff);
        notifRecycler = findViewById(R.id.recyclerNotif);

        notifRecycler.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActivityNotification.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notifRecycler.setLayoutManager(linearLayoutManager);
        notifRecycler.setItemAnimator(new DefaultItemAnimator());
        notifRecycler.setItemViewCacheSize(10);

        buildRecyclerView();

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources()
                .getColor(R.color.purple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildRecyclerView();
        });

    }

    public void buildRecyclerView() {
        Query query = FirebaseFirestore.getInstance()
                .collection("Users/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/Notifs/")
                .orderBy("ts", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<NotifModel> options = new FirestorePagingOptions.Builder<NotifModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    NotifModel notifModel = snapshot.toObject(NotifModel.class);
                    Objects.requireNonNull(notifModel).setDocID(snapshot.getId());
                    return notifModel;
                })
                .build();

        adapter = new FirestorePagingAdapter<NotifModel, ProgrammingViewHolder>(options) {
            @NonNull
            @Override
            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View v = layoutInflater.inflate(R.layout.item_notif, parent, false);
                return new ProgrammingViewHolder(v);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position, @NonNull NotifModel model) {

                String userimage_url = model.getDp();
                if(userimage_url!=null){
                    Picasso.get().load(userimage_url).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.dp);
                }
                else{
                    if(model.getGender()!=null){
                        if (model.getGender().matches("Female") || model.getGender().matches("মহিলা")){
                            holder.dp.setImageResource(R.drawable.ic_female);
                        }
                        else if (model.getGender().matches("Male") || model.getGender().matches("পুরুষ")){
                            holder.dp.setImageResource(R.drawable.ic_male);
                        }
                        else if (model.getGender().matches("Others") || model.getGender().matches("অন্যান্য")){
                            holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    }
                    else {
                        holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
//                    holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                holder.title.setText(model.getUsN()+" "+ model.getTitle());

                holder.minsago.setText(BasicUtility.getTimeAgo(model.getTs()));

                if(model.getTitle().contains("commented") || model.getTitle().contains("replied"))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_conch_shell);
                    holder.comment.setVisibility(View.VISIBLE);
                    holder.comment.setText("\""+ model.getComTxt()+"\"");
                }
                if((model.getTitle().contains("liked")|| model.getTitle().contains("flamed")))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
                    holder.comment.setVisibility(View.GONE);
                }
                if(model.getTitle().contains("upvoted"))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_baseline_stars_24);
                    holder.comment.setVisibility(View.GONE);
                }
                if((model.getTitle().contains("liked")|| model.getTitle().contains("flamed")) && model.getTitle().contains("comment"))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
                    holder.comment.setVisibility(View.VISIBLE);
                    holder.comment.setText("\""+ model.getComTxt()+"\"");
                }

                if(!model.isSeen()){
                    holder.notifCard.setBackgroundColor(ActivityNotification.this.getResources().getColor(R.color.colorPrimaryLight));
                }

                holder.notifCard.setOnClickListener(v -> {
                    String postID= model.getPostID();

                    if(model.getBool() == 1){
                        model.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(task -> {
                                    if(!task.isSuccessful())
                                        Log.d("Notif update", "updated"+ model.getDocID());
                                });
                        Intent i= new Intent(ActivityNotification.this, ViewMoreHome.class);
                        i.putExtra("postID", postID);
                        i.putExtra("type", model.getType());
                        i.putExtra("from", "Image");
                        i.putExtra("ts", Long.toString(model.getCom_ts()));
                        i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
                        i.putExtra("gender",model.getGender());
                        startActivity(i);
                        notifyItemChanged(position);
                    }

                    else if(model.getBool() == 2){
                        model.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(task -> {
                                    if(!task.isSuccessful())
                                        Log.d("Notif update", "updated"+ model.getDocID());
                                });
                        Intent i= new Intent(ActivityNotification.this, ViewMoreText.class);
                        i.putExtra("postID", postID);
                        i.putExtra("type", model.getType());
                        i.putExtra("ts", Long.toString(model.getCom_ts()));
                        i.putExtra("from", "Text");
                        i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
                        i.putExtra("gender",model.getGender());

                        startActivity(i);
                        notifyItemChanged(position);
                    }

                    else if(model.getBool() == 3){
                        model.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(task -> {
                                    if(!task.isSuccessful())
                                        Log.d("Notif update", "updated"+ model.getDocID());
                                });
                        Intent i= new Intent(ActivityNotification.this, ReelsActivity.class);
                        i.putExtra("docID", postID);
                        i.putExtra("bool", "1");
                        i.putExtra("ts", Long.toString(model.getCom_ts()));
                        i.putExtra("type", model.getType());
                        i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
                        i.putExtra("gender",model.getGender());

                        startActivity(i);
                        notifyItemChanged(position);
                    }

                    else if(model.getTitle().contains("upvoted")){
                        model.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(task -> {
                                    if(!task.isSuccessful())
                                        Log.d("Notif update", "updated"+ model.getDocID());
                                });
                        Intent i= new Intent(ActivityNotification.this, ActivityProfileCommittee.class);
                        i.putExtra("uid", FirebaseAuth.getInstance().getUid());
                        startActivity(i);
                        notifyItemChanged(position);
                    }
                });
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR: BasicUtility.showToast(ActivityNotification.this, "Something went wrong..."); break;
                    case LOADING_MORE: progressMore.setVisibility(View.VISIBLE); break;
                    case LOADED: progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED: progressMore.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(adapter!=null && adapter.getItemCount() == 0)
                            noNotif.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };

        progressMore.setVisibility(View.GONE);
        noNotif.setVisibility(View.GONE);
        notifRecycler.setAdapter(adapter);

    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        ImageView dp, bottomOfDp, dots;
        LinearLayout notifCard;
        TextView title, minsago, comment;

        ProgrammingViewHolder(@NonNull View itemView){
            super(itemView);
            dp = itemView.findViewById(R.id.dp);
            bottomOfDp = itemView.findViewById(R.id.bottom_of_dp);
            notifCard= itemView.findViewById(R.id.notif_card);
            title= itemView.findViewById(R.id.notif_title);
            minsago= itemView.findViewById(R.id.timestamp);
            comment= itemView.findViewById(R.id.notif_comment);
//            dots = itemView.findViewById(R.id.notif_delete);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }
}