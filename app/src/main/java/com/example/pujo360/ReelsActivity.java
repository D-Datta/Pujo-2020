package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.example.pujo360.models.ReelsPostModel;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.Objects;

public class ReelsActivity extends AppCompatActivity {

    private FirestorePagingAdapter reelsAdapter;
    private RecyclerView reelsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        //////////////RECYCLER VIEW////////////////////
        reelsList  = findViewById(R.id.recyclerReelsViewAll) ;
        reelsList.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ReelsActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        reelsList.setLayoutManager(layoutManager);
        reelsList.setNestedScrollingEnabled(true);

        reelsList.setItemViewCacheSize(10);
        reelsList.setDrawingCacheEnabled(true);

        buildReelsRecyclerView();
        //////////////RECYCLER VIEW////////////////////
    }

    private void buildReelsRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Reels/")
                .orderBy("ts", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .setEnablePlaceholders(true)
                .build();

        FirestorePagingOptions<ReelsPostModel> options = new FirestorePagingOptions.Builder<ReelsPostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    ReelsPostModel reelsPostModel = new ReelsPostModel();
                    if(snapshot.exists()) {
                        reelsPostModel = snapshot.toObject(ReelsPostModel.class);
                        Objects.requireNonNull(reelsPostModel).setDocID(snapshot.getId());
                    }
                    return reelsPostModel;
                })
                .build();

        reelsAdapter = new FirestorePagingAdapter<ReelsPostModel, ReelsItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ReelsItemViewHolder holder, int position, @NonNull ReelsPostModel currentItem) {
                holder.item_reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
                holder.video_time.setText(currentItem.getDuration());
                holder.pujo_com_name.setText(currentItem.getCommittee_name());

                if (currentItem.getCommittee_dp() != null && !currentItem.getCommittee_dp().isEmpty()) {
                    Picasso.get().load(currentItem.getCommittee_dp()).fit().centerCrop()
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(holder.pujo_com_dp, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                                }
                            });

                } else {
                    holder.pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }

            @NonNull
            @Override
            public ReelsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                View v = layoutInflater.inflate(R.layout.item_view_all_reels, viewGroup, false);
                return new ReelsItemViewHolder(v);
            }

            @Override
            public int getItemViewType(int position) { return position; }

        };

        reelsList.setAdapter(reelsAdapter);
    }

    private static class ReelsItemViewHolder extends RecyclerView.ViewHolder {

        VideoView item_reels_video;
        ImageView pujo_com_dp, like_image, comment_image, drumbeat, comment, share;
        TextView pujo_com_name, pujo_headline, like_count, comment_count ;
        com.borjabravo.readmoretextview.ReadMoreTextView pujo_desc;
        com.airbnb.lottie.LottieAnimationView progress;

        ReelsItemViewHolder(View itemView) {
            super(itemView);

            like_count = itemView.findViewById(R.id.likes_count);
            item_reels_video = itemView.findViewById(R.id.item_reels_video);
            video_time = itemView.findViewById(R.id.video_time);
            pujo_com_dp = itemView.findViewById(R.id.pujo_com_dp);
            pujo_com_name = itemView.findViewById(R.id.pujo_com_name);
            reels_more =  itemView.findViewById(R.id.reels_more);
        }
    }
}