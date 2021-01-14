package com.applex.utsav.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.applex.utsav.R;
import com.applex.utsav.adapters.FlamedByAdapter;
import com.applex.utsav.models.FlamedModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Objects;

public class BottomFlamedByDialog3 extends BottomSheetDialogFragment {

    private RecyclerView flamerecycler;
    private FlamedByAdapter flamedByAdapter;
    private ArrayList<FlamedModel> models;
    private ProgressBar progressBar;
    private String root;
    private String docID;
    private String postID;
    private String pComID;
    private DocumentSnapshot lastVisible;
    private int checkGetMore = -1;
    private CollectionReference flamedList;
    private ShimmerFrameLayout shimmerFrameLayout;

    public BottomFlamedByDialog3(String root, String postID, String pComID, String docID){
        this.docID = docID;
        this.pComID = pComID;
        this.root = root;
        this.postID = postID;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.bottomsheetflamedby, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        shimmerFrameLayout = v.findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

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
        flamerecycler.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);
        flamedList = FirebaseFirestore.getInstance().collection(root + "/" + postID + "/commentL/" + pComID + "/commentL/" +docID+ "/flameL/");

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

        dismiss.setOnClickListener(v1 -> BottomFlamedByDialog3.super.onDestroyView());
        return v;
    }

    private void buildRecyclerView_flames() {
        progressBar.setVisibility(View.GONE);
        models = new ArrayList<>();

        flamedList.limit(15).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                    FlamedModel flamedModel = document.toObject(FlamedModel.class);
                    Objects.requireNonNull(flamedModel).setDocID(document.getId());
                    models.add(flamedModel);
                }
                if (models.size() > 0) {
                    flamedByAdapter = new FlamedByAdapter(getActivity(), models);
                    new Handler().postDelayed(() -> {
                        flamerecycler.setVisibility(View.VISIBLE);
                        flamerecycler.setAdapter(flamedByAdapter);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                    }, 500);

                    if(task.getResult().size() > 0)
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                    if(models.size() < 15) {
                        checkGetMore = -1;
                    } else {
                        checkGetMore = 0;
                    }

                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void fetchMore_flames() {
        progressBar.setVisibility(View.VISIBLE);

        flamedList.limit(10).startAfter(lastVisible).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                ArrayList<FlamedModel> flamedModels = new ArrayList<>();
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                    FlamedModel flamedModel = document.toObject(FlamedModel.class);
                    Objects.requireNonNull(flamedModel).setDocID(document.getId());
                    flamedModels.add(flamedModel);
                }
                if(flamedModels.size() > 0) {
                    int lastSize = models.size();
                    models.addAll(flamedModels);
                    flamedByAdapter.notifyItemRangeInserted(lastSize, flamedModels.size());
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }
            }
            progressBar.setVisibility(View.GONE);
            if(models.size() < 15) {
                checkGetMore = -1;
            }
            else {
                checkGetMore = 0;
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            setupFullHeight(bottomSheetDialog);
        });
        return  dialog;
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(Objects.requireNonNull(bottomSheet));
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
