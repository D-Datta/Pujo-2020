package com.applex.utsav.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.applex.utsav.R;
import java.util.ArrayList;

public class TagAdapter2 extends RecyclerView.Adapter<TagAdapter2.ProgrammingViewHolder> {


    private ArrayList<String> mList;
    Context mcontext;

    private OnClickListener mListener;
    private OnLongClickListener Listener;

    public interface OnClickListener {
        void onClickListener(int position, String tag);
    }

    public void onClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnLongClickListener {
        void onLongClickListener(int position, String tag);
    }

    public void onLongClickListener(OnLongClickListener onLongClickListener) {
        Listener= onLongClickListener;
    }



    public TagAdapter2(ArrayList<String> list, Context context) {
        this.mList = list;
        this.mcontext=context;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tag,viewGroup, false);
        return new ProgrammingViewHolder(v , mListener, Listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int i) {
        String currentItem = mList.get(i);

        holder.tag.setText(currentItem);

    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        TextView tag;

        private ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener, OnLongClickListener onLongClickListener) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);

            tag.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        listener.onClickListener(position, mList.get(position));
                    }
                }
            });

            tag.setOnLongClickListener(v -> {
                if(onLongClickListener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        onLongClickListener.onLongClickListener(position, mList.get(position));
                    }
                }
                return true;
            });


        }
    }
}
