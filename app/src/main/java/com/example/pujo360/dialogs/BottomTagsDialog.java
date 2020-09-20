package com.example.pujo360.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pujo360.R;
import com.example.pujo360.adapters.TagAdapter;
import com.example.pujo360.models.TagModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Objects;

public class BottomTagsDialog extends BottomSheetDialogFragment {

    private DatabaseReference databaseTagsRef ;
    private RecyclerView tagrecycler;
    private TagAdapter tagAdapter;
    private ArrayList<TagModel> models;
    private ProgressBar progressBar;
    private BottomSheetListener mListener;
    private EditText search;
    private ArrayList<TagModel> searchModels;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.bottomsheet_tags, container, false);
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        tagrecycler=v.findViewById(R.id.tags_recycler2);
        progressBar = v.findViewById(R.id.progress);
        ImageView dismiss = v.findViewById(R.id.dismiss);
        search = v.findViewById(R.id.search);

        databaseTagsRef = FirebaseDatabase.getInstance().getReference("Tags4");
        databaseTagsRef.keepSynced(true);

        searchModels = new ArrayList<>();
        models=new ArrayList<>();
        tagAdapter = new TagAdapter(models, getContext());
        tagrecycler.setAdapter(tagAdapter);
        buildRecyclerView_tags();

        dismiss.setOnClickListener(v1 -> BottomTagsDialog.super.onDestroyView());
        return  v;
    }

    public interface BottomSheetListener {
        void onTagClicked(TagModel tagModel);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()+" must implement BottomSheetListener");
        }
    }

    private void buildRecyclerView_tags() {
        progressBar.setVisibility(View.VISIBLE);
        tagrecycler.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),2);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        tagrecycler.setLayoutManager(layoutManager);
        tagrecycler.setItemAnimator(new DefaultItemAnimator());
        databaseTagsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot groupSnapshot: dataSnapshot.getChildren()) {
                    TagModel item2= new TagModel();
                    if(groupSnapshot.child("name_tag").exists() && groupSnapshot.child("color_hex").exists()) {
                        item2.setName_tag(Objects.requireNonNull(groupSnapshot.child("name_tag").getValue()).toString());
                        item2.setColor_hex(Objects.requireNonNull(groupSnapshot.child("color_hex").getValue()).toString());
                        models.add(item2);
                    }
                }
                progressBar.setVisibility(View.GONE);
                tagAdapter.onClickListener((position, tag_name, tag_color) -> {
                    TagModel tagModel = new TagModel();
                    tagModel.setName_tag(tag_name);
                    tagModel.setColor_hex(tag_color);

                    mListener.onTagClicked(tagModel);

                    models.remove(position);
                    tagAdapter.notifyItemRemoved(position);
                });

                search.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        searchModels.clear();
                        for(TagModel model : models) {
                            if(model.getName_tag().toLowerCase().contains(s.toString().toLowerCase())){
                                searchModels.add(model);
                            }
                        }
                        tagAdapter = new TagAdapter(searchModels, getContext());
                        tagAdapter.onClickListener((position, tag_name, tag_color) -> {
                            TagModel tagModel = new TagModel();
                            tagModel.setName_tag(tag_name);
                            tagModel.setColor_hex(tag_color);

                            mListener.onTagClicked(tagModel);
                            TagModel searchedItem = searchModels.get(position);
                            models.remove(searchedItem);
                            searchModels.remove(position);
                            tagAdapter.notifyItemRemoved(position);
                        });
                        tagrecycler.setAdapter(tagAdapter);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
