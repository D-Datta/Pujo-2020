package com.example.pujo360;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.pujo360.adapters.CommunityAdapter;
import com.example.pujo360.fragments.FeedsFragment;
import com.example.pujo360.preferences.IntroPref;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CommunityViewAll extends AppCompatActivity {

    RecyclerView cRecyclerView;
    ProgressBar progress;
    ProgressBar progressMoreCom;
    public static int changed = 0;
    public static int delete = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<CommunityModel> CommunityGrps;
    private CommunityAdapter communityAdapter;
    private DocumentSnapshot lastVisibleCommunity;
//    private Boolean isScrolling = false;
//    private int currentItems, totalItems, scrollOutItems;

    private int checkGetMoreCommunity = -1;
    private IntroPref introPref;
    //private String CAMPUSNAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_view_all);

        Toolbar toolbar = findViewById(R.id.toolbar6);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        introPref = new IntroPref(CommunityViewAll.this);
        //CAMPUSNAME = introPref.getInstitute();

        progress = findViewById(R.id.progress);
        progressMoreCom = findViewById(R.id.progress_more_comm);
        progress.setVisibility(View.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        cRecyclerView = findViewById(R.id.community_view_all);
        cRecyclerView.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(CommunityViewAll.this, 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cRecyclerView.setLayoutManager(gridLayoutManager);
        CommunityGrps = new ArrayList<>();
        communityAdapter = new CommunityAdapter(CommunityGrps, CommunityViewAll.this, 20);
        cRecyclerView.setAdapter(communityAdapter);

        NestedScrollView nestedScrollView  = findViewById(R.id.scrollView);


        buildCommunityRecyclerView();
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.toolbarStart),getResources()
                .getColor(R.color.md_blue_500));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildCommunityRecyclerView();
        });


        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(v, scrollX, scrollY, oldScrollX, oldScrollY) ->{
            if(v.getChildAt(v.getChildCount() - 1) != null){
                if((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight() )) &&
                        scrollY > oldScrollY){
                    if(checkGetMoreCommunity != -1){
                        if(progressMoreCom.getVisibility() == View.GONE){
                            progressMoreCom.setVisibility(View.VISIBLE);
                            fetchMoreCommunity();//Load more data
                        }
                    }
                }
            }
        });

    }


    private void buildCommunityRecyclerView () {
        CommunityGrps.clear();

        CommunityModel communityModel= new CommunityModel();
        communityModel.setName("Add Community");
        communityModel.setDesc("type about your community");
        communityModel.setmCount((long) 1000);
        CommunityGrps.add(communityModel);

        Query nextQuery =  FirebaseFirestore.getInstance()
                .collection("Home/"+ CAMPUSNAME + "/Communities/")
                .orderBy("mCount", Query.Direction.DESCENDING)
                .limit(10);

        nextQuery.get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(QueryDocumentSnapshot document: task.getResult()) {
                            CommunityModel communityModel1 = document.toObject(CommunityModel.class);
                            CommunityGrps.add(communityModel1);
                        }
                        progressMoreCom.setVisibility(View.GONE);
                        if(CommunityGrps.size()>0) {
                            progress.setVisibility(View.GONE);
                            communityAdapter= new CommunityAdapter(CommunityGrps, CommunityViewAll.this, 20);
                            cRecyclerView.setAdapter(communityAdapter);
                            if(task.getResult().size()>0)
                                 lastVisibleCommunity = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        }
//                        cRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        if(CommunityGrps.size()<10){
                            checkGetMoreCommunity = -1;
                        }
                        else {
                            checkGetMoreCommunity = 0;
                        }
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    else {
                        Toast.makeText(CommunityViewAll.this, "Error", Toast.LENGTH_LONG).show();
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });

    }

    private void fetchMoreCommunity() {
        progressMoreCom.setVisibility(View.VISIBLE);
        Query nextQuery =  FirebaseFirestore.getInstance()
                .collection("Home/"+ CAMPUSNAME + "/Communities/")
                .orderBy("mCount", Query.Direction.DESCENDING)
                .startAfter(lastVisibleCommunity)
                .limit(10);

        nextQuery.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                ArrayList<CommunityModel> cFeedsList2 = new ArrayList<>();
                for (DocumentSnapshot d : t.getResult()) {
                    CommunityModel newPostModel = d.toObject(CommunityModel.class);
                    newPostModel.setDocID(d.getId());
                    cFeedsList2.add(newPostModel);
                }

                if(cFeedsList2.size()>0){
                    int lastSize = CommunityGrps.size();
                    CommunityGrps.addAll(cFeedsList2);
                    communityAdapter.notifyItemRangeInserted(lastSize, cFeedsList2.size());
                    lastVisibleCommunity = t.getResult().getDocuments().get(t.getResult().size() - 1);
                }

                progressMoreCom.setVisibility(View.GONE);

                if(CommunityGrps.size()<10){
                    checkGetMoreCommunity = -1;
                }
            }
        });
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);
//        MenuItem menuItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
//        searchView.setQueryHint("Search Community");
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                ArrayList<CommunityModel> searchedCommunities = new ArrayList<>();
//                for(CommunityModel communityModel: CommunityGrps){
//                    if(communityModel.getName().toLowerCase().contains(newText.toLowerCase())){
//                        searchedCommunities.add(communityModel);
//                    }
//                }
//                communityAdapter = new CommunityAdapter(searchedCommunities, CommunityViewAll.this, 20);
//                cRecyclerView.setAdapter(communityAdapter);
//                return true;
//            }
//        });
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    protected void onResume() {
        if(changed > 0 || delete == 2){
            buildCommunityRecyclerView();
            changed = 0;
            delete = 0;
            FeedsFragment.changed = 0;
        }
        super.onResume();
    }
}
