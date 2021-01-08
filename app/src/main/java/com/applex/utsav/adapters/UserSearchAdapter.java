package com.applex.utsav.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.applex.utsav.R;
import com.applex.utsav.SearchActivity;
import com.applex.utsav.models.UserSearchModel;
import com.applex.utsav.preferences.IntroPref;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ProgrammingViewHolder> {

    private final ArrayList<UserSearchModel> userSearchModelArrayList;
    private final Context mContext;
    private final IntroPref introPref;
    private OnClickListener mListener;

    public interface OnClickListener {
        void onClickListener(String name, String uid, String type, String dp, String gender, int position);
    }

    public void onClickListener(OnClickListener listener){
        mListener = listener;
    }

    public UserSearchAdapter(Context context, ArrayList<UserSearchModel> userSearchModelArrayList){
        this.userSearchModelArrayList = userSearchModelArrayList;
        this.mContext = context;
        introPref = new IntroPref(context);
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View v = layoutInflater.inflate(R.layout.item_search_history,viewGroup,false);
        return new ProgrammingViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        UserSearchModel currentItem = userSearchModelArrayList.get(position);

        if(currentItem.getUid() == null || currentItem.getUid().matches("")) {
            holder.dp.setImageResource(R.drawable.ic_baseline_search_24);
        }
        else {
            String dp = currentItem.getDp();
            if(dp != null) {
                Picasso.get()
                        .load(dp)
                        .error(R.drawable.ic_baseline_search_24)
                        .transform(new CropCircleTransformation()).into(holder.dp);
            }
            else {
                if(currentItem.getGender()!=null){
                    if (currentItem.getGender().matches("Female") || currentItem.getGender().matches("মহিলা")){
                        holder.dp.setImageResource(R.drawable.ic_female);
                    }
                    else if (currentItem.getGender().matches("Male") || currentItem.getGender().matches("পুরুষ")){
                        holder.dp.setImageResource(R.drawable.ic_male);
                    }
                    else if (currentItem.getGender().matches("Others") || currentItem.getGender().matches("অন্যান্য")){
                        holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                }
                else {
                    holder.dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }
        }

        holder.name.setText(currentItem.getName());

        holder.delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.delete_this_search)
                    .setMessage(R.string.alert_message)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        userSearchModelArrayList.remove(position);
                        notifyItemRemoved(position);
                        introPref.setRecentSearchHistory(userSearchModelArrayList);
                        if(userSearchModelArrayList.size() == 0) {
                            SearchActivity.history_recycler.setVisibility(View.GONE);
                            SearchActivity.nosearch.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        });

        holder.item_layout.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.delete_this_search)
                    .setMessage(R.string.alert_message)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        userSearchModelArrayList.remove(position);
                        notifyItemRemoved(position);
                        introPref.setRecentSearchHistory(userSearchModelArrayList);
                        if(userSearchModelArrayList.size() == 0) {
                            SearchActivity.history_recycler.setVisibility(View.GONE);
                            SearchActivity.nosearch.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return userSearchModelArrayList.size();
    }

    class ProgrammingViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView dp, delete;
        RelativeLayout item_layout;

        ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener){
            super(itemView);
            name = itemView.findViewById(R.id.name);
            dp = itemView.findViewById(R.id.dp);
            delete = itemView.findViewById(R.id.delete);
            item_layout = itemView.findViewById(R.id.item_layout);

            item_layout.setOnClickListener(v -> {
                if(listener != null) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ){
                        String name = userSearchModelArrayList.get(position).getName();
                        String uid = userSearchModelArrayList.get(position).getUid();
                        String type = userSearchModelArrayList.get(position).getType();
                        String dp = userSearchModelArrayList.get(position).getDp();
                        String gender = userSearchModelArrayList.get(position).getGender();
                        listener.onClickListener(name.trim(), uid, type, dp, gender, position);
                    }
                }
            });
        }
    }
}
