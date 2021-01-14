package com.applex.utsav.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applex.utsav.ActivityNotification;
import com.applex.utsav.ActivityProfile;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.models.NotifModel;
import com.applex.utsav.utility.BasicUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.ProgrammingViewHolder> {
    private Context mContext;
    private ArrayList<NotifModel> notifModels;

    private NotifAdapter.OnClickListener mListener;

    public interface OnClickListener{
        void onClickListener(int value);
    }

    public void onClickListener(NotifAdapter.OnClickListener listener){
        mListener = listener;
    }

    public NotifAdapter(Context context, ArrayList<NotifModel> models){
        this.mContext= context;
        this.notifModels= models;
    }

    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.item_notif,parent,false);
        return new NotifAdapter.ProgrammingViewHolder(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        NotifModel model  = notifModels.get(position);

        String userimage_url = model.getDp();
        if(userimage_url!=null){
            Picasso.get().load(userimage_url).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.dp);
        }
        else{
            if(model.getGender()!=null){
                if (model.getGender().matches("Female") || model.getGender().matches("মহিলা")){
                    holder.dp.setImageResource(R.drawable.ic_female);
                }
                else if (model.getGender().matches("Male") || model.getGender().matches("পুরুষ")){
                    holder.dp.setImageResource(R.drawable.ic_male);
                }
                else if (model.getGender().matches("Others") || model.getGender().matches("অন্যান্য")){
                    holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
            else {
                holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
            }
        }

        String titleString = "<b>"+model.getUsN()+"</b> "+model.getTitle();
        holder.title.setText(Html.fromHtml(titleString));

        holder.minsago.setText(BasicUtility.getTimeAgo(model.getTs()));

        if(model.getTitle().contains("commented") || model.getTitle().contains("replied"))
        {
            holder.bottomOfDp.setBackgroundResource(R.drawable.ic_conch_shell);
            holder.comment.setVisibility(View.VISIBLE);
            holder.comment.setText("\""+ model.getComTxt()+"\"");
        }
        if((model.getTitle().contains("liked")|| model.getTitle().contains("flamed")))
        {
            holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
            holder.comment.setVisibility(View.GONE);
        }
        if(model.getTitle().contains("upvoted"))
        {
            holder.bottomOfDp.setBackgroundResource(R.drawable.ic_baseline_stars_24);
            holder.comment.setVisibility(View.GONE);
        }
        if((model.getTitle().contains("liked")|| model.getTitle().contains("flamed")) && model.getTitle().contains("comment"))
        {
            holder.bottomOfDp.setBackgroundResource(R.drawable.ic_drum);
            holder.comment.setVisibility(View.VISIBLE);
            holder.comment.setText("\""+ model.getComTxt()+"\"");
        }

        if(!model.isSeen()){
            holder.notifCard.setBackground(mContext.getResources().getDrawable(R.drawable.notif_unseen_bg));
        }

        holder.notifCard.setOnClickListener(v -> {
            String postID= model.getPostID();

            if(model.getBool() == 1){
                model.setSeen(true);
                FirebaseFirestore.getInstance()
                        .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                        .update("seen", true).addOnCompleteListener(task -> {
                    if(!task.isSuccessful())
                        Log.d("Notif update", "updated"+ model.getDocID());
                });
                Intent i= new Intent(mContext, ViewMoreHome.class);
                i.putExtra("postID", postID);
                i.putExtra("type", model.getType());
                i.putExtra("from", "Image");
                i.putExtra("ts", Long.toString(model.getCom_ts()));
                i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
                i.putExtra("gender",model.getGender());
                i.putExtra("position", Integer.toString(position));
                mContext.startActivity(i);
                notifyItemChanged(position);
            }

            else if(model.getBool() == 2){
                model.setSeen(true);
                FirebaseFirestore.getInstance()
                        .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                        .update("seen", true).addOnCompleteListener(task -> {
                    if(!task.isSuccessful())
                        Log.d("Notif update", "updated"+ model.getDocID());
                });
                Intent i= new Intent(mContext, ViewMoreText.class);
                i.putExtra("postID", postID);
                i.putExtra("type", model.getType());
                i.putExtra("ts", Long.toString(model.getCom_ts()));
                i.putExtra("from", "Text");
                i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
                i.putExtra("gender",model.getGender());
                i.putExtra("position", Integer.toString(position));

                mContext.startActivity(i);
                notifyItemChanged(position);
            }

            else if(model.getBool() == 3){
                model.setSeen(true);
                FirebaseFirestore.getInstance()
                        .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                        .update("seen", true).addOnCompleteListener(task -> {
                    if(!task.isSuccessful())
                        Log.d("Notif update", "updated"+ model.getDocID());
                });
                Intent i= new Intent(mContext, ReelsActivity.class);
                i.putExtra("docID", postID);
                i.putExtra("bool", "1");
                i.putExtra("ts", Long.toString(model.getCom_ts()));
                i.putExtra("type", model.getType());
                i.putExtra("pCom_ts", Long.toString(model.getpCom_ts()));
                i.putExtra("gender",model.getGender());
                i.putExtra("position", Integer.toString(position));

                mContext.startActivity(i);
                notifyItemChanged(position);
            }
            else if(model.getTitle().contains("upvoted")){
                model.setSeen(true);
                FirebaseFirestore.getInstance()
                        .document("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/Notifs/"+ model.getDocID()+"/")
                        .update("seen", true).addOnCompleteListener(task -> {
                    if(!task.isSuccessful())
                        Log.d("Notif update", "updated"+ model.getDocID());
                });

                Intent i= new Intent(mContext, ActivityProfile.class);
                i.putExtra("uid", FirebaseAuth.getInstance().getUid());
                i.putExtra("to", "profile");
                i.putExtra("position", Integer.toString(position));
                mContext.startActivity(i);
                notifyItemChanged(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notifModels.size();
    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{
        ImageView dp, bottomOfDp, dots;
        LinearLayout notifCard;
        TextView title, minsago, comment;


        ProgrammingViewHolder(@NonNull View itemView, NotifAdapter.OnClickListener listener){
            super(itemView);
            dp = itemView.findViewById(R.id.dp);
            bottomOfDp = itemView.findViewById(R.id.bottom_of_dp);
            notifCard= itemView.findViewById(R.id.notif_card);
            title= itemView.findViewById(R.id.notif_title);
            minsago= itemView.findViewById(R.id.timestamp);
            comment= itemView.findViewById(R.id.notif_comment);
//            dots = itemView.findViewById(R.id.notif_delete);
//            dots.setOnClickListener(v -> {
//                if(listener != null){
//                    int position = getAdapterPosition();
//                    if(position != RecyclerView.NO_POSITION ){
//
//                        listener.onClickListener(position);
//                    }
//                }
//            });

        }
    }
}
