//package com.example.pujo360.adapters;
//
//import android.content.Context;
//import android.content.Intent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.cardview.widget.CardView;
//import androidx.recyclerview.widget.RecyclerView;
//
//
//import com.example.pujo360.R;
//import com.example.pujo360.models.PujoCommitteeModel;
//import com.squareup.picasso.Callback;
//import com.squareup.picasso.NetworkPolicy;
//import com.squareup.picasso.OkHttp3Downloader;
//import com.squareup.picasso.Picasso;
//import com.thekhaeng.pushdownanim.PushDownAnim;
//
//import java.util.ArrayList;
//
//public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ProgrammingViewHolder> {
//
//    private ArrayList<PujoCommitteeModel> mList;
//    Context mContext;
//    int bool;
//    public CommunityAdapter() {
//    }
//
//    public CommunityAdapter(ArrayList<PujoCommitteeModel> list, Context context, int bool) {
//        this.mList = list;
//        this.mContext =context;
//        this.bool = bool;
//    }
//
//    @NonNull
//    @Override
//    public CommunityAdapter.ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v;
//        if(bool == 10){
//            v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_community_card,parent, false);
//        }
//        else{
//            v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_grid_community_card, parent, false);
//        }
//
//        return new CommunityAdapter.ProgrammingViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull CommunityAdapter.ProgrammingViewHolder holder, int position) {
//        PujoCommitteeModel currentitem = mList.get(position);
//        holder.com_name.setText(currentitem.getName());
//
//        if(currentitem.getName().matches("Add Community")){
//            holder.com_image.setImageResource(R.drawable.community_create);
//            holder.com_members.setText("");
//        }
//        else{
//            if(currentitem.getmCount() == 1)
//                holder.com_members.setText(  currentitem.getmCount()+" member");
//            else
//                holder.com_members.setText(  currentitem.getmCount()+" members");
//
//
//            if(currentitem.getImage() != null){
//                Picasso.Builder builder = new Picasso.Builder(mContext);
//                builder.downloader(new OkHttp3Downloader(mContext, Integer.MAX_VALUE));
//                Picasso built = builder.build();
//                built.setIndicatorsEnabled(true);
//                built.setLoggingEnabled(true);
//                Picasso.get().load(currentitem.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.com_image, new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//                    @Override
//                    public void onError(Exception e) {
//                        Picasso.get().load(currentitem.getImage()).into(holder.com_image);
//                    }
//                });
//
//            }
//            else{
//                holder.com_image.setImageResource(R.drawable.community_default);
//            }
//        }
//
//
//        PushDownAnim.setPushDownAnimTo(holder.com_card)
//                .setScale(PushDownAnim.MODE_STATIC_DP, 3)
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if(position==0){
//                            Intent i = new Intent(mContext, CreateCommunityActivity.class);
//                            mContext.startActivity(i);
//                        }
//                        else{
//                            Intent i= new Intent(mContext, CommunityActivity.class);
//                            i.putExtra("comID", currentitem.getTs());
//                            i.putExtra("comCover", currentitem.getImage());
//                            i.putExtra("comName", currentitem.getName());
//                            i.putExtra("comMem", Long.toString(currentitem.getmCount()));
//                            i.putExtra("comDesc", currentitem.getDesc());
//                            mContext.startActivity(i);
//                        }
//                    }
//                });
//
//        holder.xxx.setOnClickListener(v -> {
//            if(position==0){
//                Intent i = new Intent(mContext, CreateCommunityActivity.class);
//                mContext.startActivity(i);
//            } else{
//                Intent i= new Intent(mContext, CommunityActivity.class);
//                i.putExtra("comID", currentitem.getTs());
//                i.putExtra("comCover", currentitem.getImage());
//                i.putExtra("comName", currentitem.getName());
//                i.putExtra("comMem", Long.toString(currentitem.getmCount()));
//                i.putExtra("comDesc", currentitem.getDesc());
//                mContext.startActivity(i);
//            } });
//
//        if(bool == 10 ){
//            if(position == mList.size()-1){
//                holder.more.setVisibility(View.VISIBLE);
//                holder.more.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mContext.startActivity(new Intent(mContext, CommunityViewAll.class));
//
//                    }
//                });
//            }
//            else {
//                holder.more.setVisibility(View.GONE);
//            }
//
//        }
//
//    }
//
//    @Override
//    public int getItemCount() {
//      return mList.size();
//    }
//
//    public class ProgrammingViewHolder extends RecyclerView.ViewHolder{
//        TextView com_name, com_members;
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
//}
