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
import com.applex.utsav.models.CitySearchModel;

import java.util.ArrayList;
import java.util.List;

public class CitySearchAdapter extends RecyclerView.Adapter<CitySearchAdapter.ProgrammingViewHolder> {

    private List<CitySearchModel> citySearchModelList;
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


    public CitySearchAdapter(Context context, ArrayList<CitySearchModel> citySearchModelList){
        this.citySearchModelList = citySearchModelList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View v = layoutInflater.inflate(R.layout.item_search_city,viewGroup,false);
        return new ProgrammingViewHolder(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        CitySearchModel currentItem = citySearchModelList.get(position);
        String city= currentItem.getCity();
//        if(inst.charAt(inst.length()-1)==','){
//            s2 = inst.substring(0,inst.length()-1);
//            //line.replace(line.charAt(line.length()-1), 'a');
//        }
        holder.City.setText(city);
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
        return citySearchModelList.size();
    }

    class ProgrammingViewHolder extends RecyclerView.ViewHolder{
        TextView City;
        LinearLayout citycard;

        ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener){
            super(itemView);
            City = itemView.findViewById(R.id.City);
            citycard = itemView.findViewById(R.id.citycard);

            citycard.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        String inst= citySearchModelList.get(position).getCity();
                        listener.onClickListener(inst.trim());
                    }
                }
            });
        }
    }

}
