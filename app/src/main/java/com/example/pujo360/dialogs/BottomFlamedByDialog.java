package com.example.pujo360.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pujo360.R;
import com.example.pujo360.adapters.FlamedByAdapter;
import com.example.pujo360.models.FlamedModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Objects;

public class BottomFlamedByDialog extends BottomSheetDialogFragment {
    private RecyclerView flamerecycler;
    private FlamedByAdapter flamedByAdapter;
    private ArrayList<FlamedModel> models;

    private ProgressBar progressBar;

    private String root;
    private String docID;
    private DocumentSnapshot lastVisible;
    private int checkGetMore = -1;

    private CollectionReference flamedList;

    public BottomFlamedByDialog(String root, String docID) {
        this.docID = docID;
        this.root = root;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.bottomsheetflamedby, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        flamerecycler=v.findViewById(R.id.flamed_recycler);
        progressBar = v.findViewById(R.id.progress5);
        ImageView dismiss = v.findViewById(R.id.dismissflame);
        NestedScrollView nestedScrollView = v.findViewById(R.id.scroll_view);
        nestedScrollView.setNestedScrollingEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        flamerecycler.setLayoutManager(layoutManager);
        flamerecycler.setItemAnimator(new DefaultItemAnimator());
        flamerecycler.setNestedScrollingEnabled(true);
        flamerecycler.setHasFixedSize(false);

        progressBar.setVisibility(View.VISIBLE);

        if(root.matches("Feeds")) {
            flamedList = FirebaseFirestore.getInstance().collection("Feeds/" + docID + "/flameL/");
        }
        else if(root.matches("Reels")) {
            flamedList = FirebaseFirestore.getInstance().collection("Reels/" + docID + "/flameL/");
        }

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(vv, scrollX, scrollY, oldScrollX, oldScrollY) ->{
            if(vv.getChildAt(vv.getChildCount() - 1) != null){
                if((scrollY >= (vv.getChildAt(vv.getChildCount() - 1).getMeasuredHeight() - vv.getMeasuredHeight() )) &&
                        scrollY > oldScrollY){
                    if(checkGetMore != -1){
                        if(progressBar.getVisibility() == View.GONE){
                            progressBar.setVisibility(View.VISIBLE);
                            fetchMore_flames();//Load more data
                        }
                    }
                }
            }
        });

        buildRecyclerView_flames();

        dismiss.setOnClickListener(v1 -> BottomFlamedByDialog.super.onDestroyView());
        return v;
    }

    private void buildRecyclerView_flames(){
        progressBar.setVisibility(View.VISIBLE);
        models = new ArrayList<>();

        flamedList.limit(15).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())){
                    FlamedModel flamedModel = document.toObject(FlamedModel.class);
                    Objects.requireNonNull(flamedModel).setDocID(document.getId());
                    models.add(flamedModel);
                }
                if (models.size()>0){
                    flamedByAdapter = new FlamedByAdapter(getActivity(), models);
                    flamerecycler.setAdapter(flamedByAdapter);

                    if(task.getResult().size()>0)
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                    if(models.size()<15){
                        checkGetMore = -1;
                    } else {
                        checkGetMore = 0;
                    }
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void fetchMore_flames(){
        progressBar.setVisibility(View.VISIBLE);

        flamedList.limit(10).startAfter(lastVisible).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                ArrayList<FlamedModel> flamedModels = new ArrayList<>();
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                    FlamedModel flamedModel = document.toObject(FlamedModel.class);
                    Objects.requireNonNull(flamedModel).setDocID(document.getId());
                    flamedModels.add(flamedModel);
                }
                if(flamedModels.size()>0) {
                    int lastSize = models.size();
                    models.addAll(flamedModels);
                    flamedByAdapter.notifyItemRangeInserted(lastSize, flamedModels.size());
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }
            }
            progressBar.setVisibility(View.GONE);
            if(models.size()<15){
                checkGetMore = -1;
            }
            else {
                checkGetMore = 0;
            }
        });
    }
}