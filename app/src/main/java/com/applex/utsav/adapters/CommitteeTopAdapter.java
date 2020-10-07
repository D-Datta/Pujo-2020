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
    private Context mContext;
    private int positionItem;

    public CommitteeTopAdapter(ArrayList<BaseUserModel> list, Context context, int position) {
        this.mList = list;
        this.mContext =context;
        this.positionItem = position;
    }

    @NonNull
    @Override
    public CommitteeTopAdapter.ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_committee_top_card,parent, false);
        return new CommitteeTopAdapter.ProgrammingViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CommitteeTopAdapter.ProgrammingViewHolder holder, int position) {
        BaseUserModel currentitem = mList.get(position);

        holder.committeeName.setText(currentitem.getName());

        if(positionItem == 2) {
            if(currentitem.getUpvotes() > 1) {
                if(currentitem.getUpvotes() > 1000) {
                    holder.com_views.setText(currentitem.getUpvotes()/1000 + "." + (currentitem.getUpvotes() % 1000)/100 + "K Upvotes");
                } else {
                    holder.com_views.setText(currentitem.getUpvotes() + " Upvotes");
                }
            } else {
                holder.com_views.setText(currentitem.getUpvotes() + " Upvotes");
            }
        } else {
            if(currentitem.getPujoVisits() > 1) {
                if(currentitem.getPujoVisits() > 1000) {
                    holder.com_views.setText(currentitem.getPujoVisits()/1000 + "." + (currentitem.getPujoVisits() % 1000)/100 + "K Visits");
                } else {
                    holder.com_views.setText(currentitem.getPujoVisits() + " Visits");
                }
            } else {
                holder.com_views.setText(currentitem.getPujoVisits() + " Visit");
            }
        }

        if(currentitem.getDp() != null){
            Picasso.get().load(currentitem.getDp()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.committeeCover, new Callback() {
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
            holder.committeeCover.setImageResource(R.drawable.durga_ma);
        }

        PushDownAnim.setPushDownAnimTo(holder.itemView)
                .setScale(PushDownAnim.MODE_STATIC_DP, 3)
                .setOnClickListener(v -> {
                    //to be changed
                    Intent i= new Intent(mContext, ActivityProfileCommittee.class);
                    i.putExtra("name", currentitem.getName());
                    i.putExtra("uid", currentitem.getUid());
                    i.putExtra("coverpic",currentitem.getCoverpic());
                    i.putExtra("dp",currentitem.getDp());
                    mContext.startActivity(i);
                });
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
}
