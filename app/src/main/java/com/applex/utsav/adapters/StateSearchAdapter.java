package com.applex.utsav.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applex.utsav.R;
import com.applex.utsav.models.StateSearchModel;

import java.util.ArrayList;
import java.util.List;

public class StateSearchAdapter extends RecyclerView.Adapter<StateSearchAdapter.ProgrammingViewHolder> {

    private List<StateSearchModel> stateSearchModelList;
    // private String[] instituteSearchModelList;
    private Context mContext;
    String s2, s3;

    private OnClickListener mListener;

    public interface OnClickListener{
        void onClickListener(String name);
    }

    public void onClickListener(OnClickListener listener){
        mListener = listener;
    }


    public StateSearchAdapter(Context context, ArrayList<StateSearchModel> stateSearchModelList){
        this.stateSearchModelList = stateSearchModelList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View v = layoutInflater.inflate(R.layout.item_search_state,viewGroup,false);
        return new ProgrammingViewHolder(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        StateSearchModel currentItem = stateSearchModelList.get(position);
        String state= currentItem.getState();
//        if(inst.charAt(inst.length()-1)==','){
//            s2 = inst.substring(0,inst.length()-1);
//            //line.replace(line.charAt(line.length()-1), 'a');
//        }
        holder.State.setText(state);
//        holder.instcard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RegistrationFormPost.etinstitute.setText(currentItem.getInstitute());
//                Intent i = new Intent(mContext, RegistrationFormPost.class);
//                i.putExtra("institute",currentItem.getInstitute());
//                mContext.startActivity(i);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return stateSearchModelList.size();
    }

    class ProgrammingViewHolder extends RecyclerView.ViewHolder{
        TextView State;
        LinearLayout statecard;

        ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener){
            super(itemView);
            State = itemView.findViewById(R.id.State);
            statecard = itemView.findViewById(R.id.statecard);

            statecard.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        String inst= stateSearchModelList.get(position).getState();
                        listener.onClickListener(inst.trim());
                    }
                }
            });
        }
    }


}
