package com.applex.utsav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
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
import android.widget.Toast;

import com.applex.utsav.fragments.Fragment_Posts;
import com.applex.utsav.models.HomePostModel;
import com.applex.utsav.models.NotifModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

public class ActivityNotification extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView notifRecycler;
    private ProgressBar progressMore;

    private ImageView noNotif;

    public static int removeNotif = -1;
    public static boolean active = false;
    IntroPref introPref;

    private FirestorePagingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPref = new IntroPref(ActivityNotification.this);
        String lang= introPref.getLanguage();
        Locale locale= new Locale(lang);
        Locale.setDefault(locale);
        Configuration config= new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                .collection("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Notifs/")
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

            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position, @NonNull NotifModel model) {

                NotifModel currentItem  = model;

                String userimage_url = currentItem.getDp();
                if(userimage_url!=null){
                    Picasso.get().load(userimage_url).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.dp);
                }
                else{
                    holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

                holder.title.setText(currentItem.getUsN()+" "+currentItem.getTitle());

                holder.minsago.setText(BasicUtility.getTimeAgo(currentItem.getTs()));

                if(currentItem.getTitle().contains("commented") || currentItem.getTitle().contains("replied"))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_conch_shell);
                    holder.comment.setVisibility(View.VISIBLE);
                    holder.comment.setText("\""+currentItem.getComTxt()+"\"");
                }
                if((currentItem.getTitle().contains("liked")|| currentItem.getTitle().contains("flamed")))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
                    holder.comment.setVisibility(View.GONE);
                }
                if(currentItem.getTitle().contains("upvoted"))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_baseline_stars_24);
                    holder.comment.setVisibility(View.GONE);
                }
                if((currentItem.getTitle().contains("liked")|| currentItem.getTitle().contains("flamed")) && currentItem.getTitle().contains("comment"))
                {
                    holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
                    holder.comment.setVisibility(View.VISIBLE);
                    holder.comment.setText("\""+currentItem.getComTxt()+"\"");
                }

                if(!currentItem.isSeen()){
                    holder.notifCard.setBackgroundColor(ActivityNotification.this.getResources().getColor(R.color.colorPrimaryLight));
                }

                holder.notifCard.setOnClickListener(v -> {
                    String postID= currentItem.getPostID();

                    if(currentItem.getBool() == 1){
                        currentItem.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+currentItem.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful())
                                    Log.d("Notif update", "updated"+currentItem.getDocID());
                            }
                        });
                        Intent i= new Intent(ActivityNotification.this, ViewMoreHome.class);
                        i.putExtra("postID", postID);
                        i.putExtra("type", currentItem.getType());
                        i.putExtra("campus", "Image");

                        startActivity(i);
                        notifyItemChanged(position);
                    }

                    else if(currentItem.getBool() == 2){
                        currentItem.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+currentItem.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful())
                                    Log.d("Notif update", "updated"+currentItem.getDocID());
                            }
                        });
                        Intent i= new Intent(ActivityNotification.this, ViewMoreText.class);
                        i.putExtra("postID", postID);
                        i.putExtra("type", currentItem.getType());
                        i.putExtra("campus", "Text");

                        startActivity(i);
                        notifyItemChanged(position);
                    }

                    else if(currentItem.getBool() == 3){
                        currentItem.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+currentItem.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful())
                                    Log.d("Notif update", "updated"+currentItem.getDocID());
                            }
                        });
                        Intent i= new Intent(ActivityNotification.this, ReelsActivity.class);
                        i.putExtra("docID", postID);
                        i.putExtra("bool", "1");
                        i.putExtra("type", currentItem.getType());

                        startActivity(i);
                        notifyItemChanged(position);
                    }

                    else if(currentItem.getTitle().contains("upvoted")){
                        currentItem.setSeen(true);
                        FirebaseFirestore.getInstance()
                                .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+currentItem.getDocID()+"/")
                                .update("seen", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful())
                                    Log.d("Notif update", "updated"+currentItem.getDocID());
                            }
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