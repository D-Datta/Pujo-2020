package com.applex.utsav.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applex.utsav.R;
import com.applex.utsav.models.Suggestedtag;
import com.applex.utsav.utility.BasicUtility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SuggestedTagAdapterHome extends RecyclerView.Adapter<SuggestedTagAdapterHome.ProgrammingViewHolder> {
    private ArrayList<Suggestedtag> mList;
    Context mcontext;

    private OnClickListener mListener;
//    private OnLongClickListener Listener;

    public interface OnClickListener {
        void onClickListener(int position, String title);
    }

    public void onClickListener(OnClickListener listener) {
        mListener = listener;
    }

//    public interface OnLongClickListener {
//        void onLongClickListener(int position, String tag, String color);
//    }
//
//    public void onLongClickListener(OnLongClickListener onLongClickListener) {
//        Listener= onLongClickListener;
//    }


    public SuggestedTagAdapterHome(ArrayList<Suggestedtag> list, Context context ) {
        this.mList = list;
        this.mcontext=context;
        //0 For new Post Home Suggested Tags, 1 for
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v;
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_suggested_tag_home,viewGroup, false);

        return new ProgrammingViewHolder(v , mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder programmingViewHolder, int i) {

        Suggestedtag currentItem = mList.get(i);

        programmingViewHolder.title.setText(currentItem.getName());
        programmingViewHolder.postCount.setText(currentItem.getValue()+" Posts");
        Picasso.get().load(currentItem.getImg()).into(programmingViewHolder.img);
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        TextView title, postCount;
        ImageView img;

        private ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            title = itemView.findViewById(R.id.title);
            postCount = itemView.findViewById(R.id.postCount);

            img.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        listener.onClickListener(position, mList.get(position).getName());
                    }
                }
            });

//            tag.setOnLongClickListener(v -> {
//                if(onLongClickListener != null){
//                    int position = getAdapterPosition();
//                    if(position != RecyclerView.NO_POSITION ){
//                        onLongClickListener.onLongClickListener(position, mList.get(position).getName_tag() , mList.get(position).getColor_hex());
//                    }
//                }
//                return true;
//            });


        }
    }
}


