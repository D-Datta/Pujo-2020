package com.applex.utsav.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applex.utsav.R;
import com.applex.utsav.models.PujoTagModel;
import com.applex.utsav.models.TagModel;

import java.util.ArrayList;

public class PujoTagAdapter extends RecyclerView.Adapter<PujoTagAdapter.ProgrammingViewHolder> {

    private ArrayList<PujoTagModel> mList;
    Context mcontext;

    private OnClickListener mListener;

    public interface OnClickListener {
        void onClickListener(int position, String pujo, String uid);
    }

    public void onClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public PujoTagAdapter() {
    }

    public PujoTagAdapter(ArrayList<PujoTagModel> list, Context context) {
        this.mList = list;
        this.mcontext=context;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tag_pujo,viewGroup, false);
        return new ProgrammingViewHolder(v , mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProgrammingViewHolder programmingViewHolder, int i) {
        PujoTagModel currentItem = mList.get(i);

            programmingViewHolder.tag.setText(currentItem.getPujoName());


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        TextView tag;

        private ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);

            tag.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                            listener.onClickListener(position, mList.get(position).getPujoName() , mList.get(position).getPujoUid());
                    }
                }
            });

        }
    }



}


