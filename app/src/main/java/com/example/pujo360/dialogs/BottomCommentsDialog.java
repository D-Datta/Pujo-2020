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
import com.example.pujo360.adapters.CommentAdapter;
import com.example.pujo360.models.CommentModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Objects;

public class BottomCommentsDialog extends BottomSheetDialogFragment {

    private RecyclerView commentRecycler;
    private CommentAdapter commentAdapter;
    private ArrayList<CommentModel> models;
    private ProgressBar progressBar;
    private String docID;
    private DocumentSnapshot lastVisible;
    private int checkGetMore = -1;

    private CollectionReference commentRef;

    public BottomCommentsDialog(String docID) {
        this.docID = docID;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.bottomsheetflamedby, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        commentRecycler =v.findViewById(R.id.flamed_recycler);
        progressBar = v.findViewById(R.id.progress5);
        ImageView dismiss = v.findViewById(R.id.dismissflame);
        NestedScrollView nestedScrollView = v.findViewById(R.id.scroll_view);
        nestedScrollView.setNestedScrollingEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRecycler.setLayoutManager(layoutManager);
        commentRecycler.setItemAnimator(new DefaultItemAnimator());
        commentRecycler.setNestedScrollingEnabled(true);
        commentRecycler.setHasFixedSize(false);

        progressBar.setVisibility(View.VISIBLE);

        commentRef = FirebaseFirestore.getInstance().collection("Reels/" + docID + "/commentL/");

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(vv, scrollX, scrollY, oldScrollX, oldScrollY) ->{
            if(vv.getChildAt(vv.getChildCount() - 1) != null) {
                if((scrollY >= (vv.getChildAt(vv.getChildCount() - 1).getMeasuredHeight() - vv.getMeasuredHeight() )) &&
                        scrollY > oldScrollY) {
                    if(checkGetMore != -1){
                        if(progressBar.getVisibility() == View.GONE) {
                            progressBar.setVisibility(View.VISIBLE);
                            fetchMore_flames();//Load more data
                        }
                    }
                }
            }
        });

        buildRecyclerView_flames();

        dismiss.setOnClickListener(v1 -> BottomCommentsDialog.super.onDestroyView());
        return v;
    }

    private void buildRecyclerView_flames(){
        progressBar.setVisibility(View.VISIBLE);
        models = new ArrayList<>();

        commentRef.limit(10).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())){
                    CommentModel commentModel = document.toObject(CommentModel.class);
                    Objects.requireNonNull(commentModel).setDocID(document.getId());
                    models.add(commentModel);
                }
                if (models.size() > 0) {
                    commentAdapter = new CommentAdapter(getActivity(), models, 2);
                    commentRecycler.setAdapter(commentAdapter);

                    if(task.getResult().size() > 0)
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                    if(models.size() < 10) {
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

        commentRef.limit(10).startAfter(lastVisible).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                ArrayList<CommentModel> commentModels = new ArrayList<>();
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                    CommentModel commentModel = document.toObject(CommentModel.class);
                    Objects.requireNonNull(commentModel).setDocID(document.getId());
                    commentModels.add(commentModel);
                }
                if(commentModels.size() > 0) {
                    int lastSize = models.size();
                    models.addAll(commentModels);
                    commentAdapter.notifyItemRangeInserted(lastSize, commentModels.size());
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }
            }
            progressBar.setVisibility(View.GONE);
            if(models.size() < 10){
                checkGetMore = -1;
            }
            else {
                checkGetMore = 0;
            }
        });
    }
}