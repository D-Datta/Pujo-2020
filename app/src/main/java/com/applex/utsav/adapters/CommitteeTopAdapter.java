package com.applex.utsav.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applex.utsav.ActivityProfileCommittee;
import com.applex.utsav.R;
import com.applex.utsav.models.BaseUserModel;
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
                    .inflate(R.layout.item_committee_top_card,parent, false);
        }
        else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_committee_top_card, parent, false);
        }

        return new CommitteeTopAdapter.ProgrammingViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CommitteeTopAdapter.ProgrammingViewHolder holder, int position) {
        BaseUserModel currentitem = mList.get(position);

        holder.committeeName.setText(currentitem.getName());

        if(currentitem.getPujoVisits() == 1) {
            holder.com_views.setText(currentitem.getPujoVisits() + " Visit");
        }
        else {
            holder.com_views.setText(currentitem.getPujoVisits() + " Visits");
        }

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

//        if(currentitem.getDp() != null){
//            Picasso.get().load(currentitem.getCoverpic())
//                    .error(R.drawable.image_background_grey)
//                    .placeholder(R.drawable.image_background_grey)
//                    .networkPolicy(NetworkPolicy.OFFLINE)
//                    .into(holder.committeeCover, new Callback() {
//                @Override
//                public void onSuccess() {
//
//                }
//                @Override
//                public void onError(Exception e) {
//                    Picasso.get().load(currentitem.getDp()).into(holder.committeeCover);
//                }
//            });
//
//        }
//        else{
//            holder.committeeDp.setImageResource(R.drawable.community_default);
//        }

        PushDownAnim.setPushDownAnimTo(holder.itemView)
                .setScale(PushDownAnim.MODE_STATIC_DP, 3)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //to be changed
                        Intent i= new Intent(mContext, ActivityProfileCommittee.class);
                        i.putExtra("name", currentitem.getName());
                        i.putExtra("uid", currentitem.getUid());
                        i.putExtra("coverpic",currentitem.getCoverpic());
                        i.putExtra("dp",currentitem.getDp());
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
        ImageView committeeCover;
        TextView com_views;

        ProgrammingViewHolder(@NonNull View itemView) {
            super(itemView);

            committeeName = itemView.findViewById(R.id.committee_name);
            com_views = itemView.findViewById(R.id.members);
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
