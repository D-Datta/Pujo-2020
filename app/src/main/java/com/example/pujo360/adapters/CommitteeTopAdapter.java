package com.example.pujo360.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujo360.MainActivity;
import com.example.pujo360.R;
import com.example.pujo360.models.BaseUserModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;

public class CommitteeTopAdapter extends RecyclerView.Adapter<CommitteeTopAdapter.ProgrammingViewHolder> {

    private ArrayList<BaseUserModel> mList;
    Context mContext;
    int bool;
    public CommitteeTopAdapter() {
    }

    public CommitteeTopAdapter(ArrayList<BaseUserModel> list, Context context, int bool) {
        this.mList = list;
        this.mContext =context;
        this.bool = bool;
    }

    @NonNull
    @Override
    public CommitteeTopAdapter.ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if(bool == 10){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_committee_grid,parent, false);
        }
        else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_committee_grid, parent, false);
        }

        return new CommitteeTopAdapter.ProgrammingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommitteeTopAdapter.ProgrammingViewHolder holder, int position) {
        BaseUserModel currentitem = mList.get(position);

        holder.committeeName.setText(currentitem.getName());

        if(currentitem.getCoverpic() != null){
            Picasso.get().load(currentitem.getCoverpic()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.committeeCover, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError(Exception e) {
                    Picasso.get().load(currentitem.getCoverpic()).into(holder.committeeCover);
                }
            });

        }
        else{
            holder.committeeCover.setImageResource(R.drawable.community_default);
        }

        if(currentitem.getDp() != null){
            Picasso.get().load(currentitem.getCoverpic())
                    .error(R.drawable.image_background_grey)
                    .placeholder(R.drawable.image_background_grey)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.committeeCover, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError(Exception e) {
                    Picasso.get().load(currentitem.getDp()).into(holder.committeeCover);
                }
            });

        }
        else{
            holder.committeeDp.setImageResource(R.drawable.community_default);
        }

        PushDownAnim.setPushDownAnimTo(holder.itemView)
                .setScale(PushDownAnim.MODE_STATIC_DP, 3)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //to be changed
                        Intent i= new Intent(mContext, MainActivity.class);
                        i.putExtra("comID", currentitem.getUid());
                        mContext.startActivity(i);

                    }
                });


//        if(bool == 10 ){
//            if(position == mList.size()-1){
//                holder.more.setVisibility(View.VISIBLE);
//                holder.more.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mContext.startActivity(new Intent(mContext, CommitteeViewAll.class));
//                    }
//                });
//            }
//            else {
//                holder.more.setVisibility(View.GONE);
//            }
//
//        }

    }

    @Override
    public int getItemCount() {
      return mList.size();
    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        TextView committeeName;
        ImageView committeeCover, committeeDp;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);

            committeeName = itemView.findViewById(R.id.committee_name);
            committeeDp = itemView.findViewById(R.id.committee_dp);
            committeeCover = itemView.findViewById(R.id.committee_cover);

        }
    }
//    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{
//        TextView committ, com_members;
//        ImageView com_image, more;
//        CardView com_card;
//        LinearLayout xxx;
//
//        ProgrammingViewHolder(@NonNull View itemView) {
//            super(itemView);
//            com_name= itemView.findViewById(R.id.community_name);
//            com_image= itemView.findViewById(R.id.community_image);
//            if(bool == 10)
//                more = itemView.findViewById(R.id.more);
//
//            com_members= itemView.findViewById(R.id.members);
//            com_card = itemView.findViewById(R.id.com_card);
//            xxx = itemView.findViewById(R.id.xxx);
//
//
//        }
//    }
}
