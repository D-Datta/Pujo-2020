package com.example.pujo360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.models.ReelsPostModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.Utility;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.util.Objects;

public class ReelsActivity extends AppCompatActivity {

    private RecyclerView reelsList;
    private String COMMITEE_LOGO, COMMITTEE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        IntroPref introPref = new IntroPref(getApplicationContext());
        COMMITEE_LOGO = introPref.getUserdp();
        COMMITTEE_NAME = introPref.getFullName();

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

        SnapHelperOneByOne snapHelperOneByOne = new SnapHelperOneByOne();
        snapHelperOneByOne.attachToRecyclerView(reelsList);
        //////////////RECYCLER VIEW////////////////////
    }

    private static class SnapHelperOneByOne extends LinearSnapHelper {

        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {

            if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
                return RecyclerView.NO_POSITION;
            }

            final View currentView = findSnapView(layoutManager);

            if (currentView == null) {
                return RecyclerView.NO_POSITION;
            }

            LinearLayoutManager myLayoutManager = (LinearLayoutManager) layoutManager;

            int position1 = myLayoutManager.findFirstVisibleItemPosition();
            int position2 = myLayoutManager.findLastVisibleItemPosition();

            int currentPosition = layoutManager.getPosition(currentView);

            if (velocityY > 400) {
                currentPosition = position2;
            } else if (velocityY < 400) {
                currentPosition = position1;
            }

            return currentPosition;
        }
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

        FirestorePagingAdapter reelsAdapter = new FirestorePagingAdapter<ReelsPostModel, ReelsItemViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ReelsItemViewHolder holder, int position, @NonNull ReelsPostModel currentItem) {
                holder.reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
                holder.pujo_desc.setText(currentItem.getDescription());
                holder.pujo_com_name.setText(currentItem.getCommittee_name());

                holder.reels_video.setOnCompletionListener(v -> reelsList.smoothScrollToPosition(position + 1));

                holder.reels_video.setOnLongClickListener(view -> {
                    holder.reels_video.pause();
                    return false;
                });

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

                DocumentReference likeStore;
                likeStore = FirebaseFirestore.getInstance().document("Reels/" + currentItem.getDocID() + "/");

                //INITIAL SETUP//
                if (currentItem.getLikeL() != null) {
                    if (currentItem.getLikeL().size() == 0) {
                        holder.like_image.setVisibility(View.GONE);
                        holder.likesCount.setVisibility(View.GONE);
                    } else {
                        holder.like_image.setVisibility(View.VISIBLE);
                        holder.likesCount.setVisibility(View.VISIBLE);
                        holder.likesCount.setText(currentItem.getLikeL().size());
                    }
                } else {
                    holder.like_image.setVisibility(View.GONE);
                    holder.likesCount.setVisibility(View.GONE);
                }
                //INITIAL SETUP//

                /////FLAME/////
                PushDownAnim.setPushDownAnimTo(holder.like)
                    .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                    .setOnClickListener(v -> {
                        if (currentItem.getLikeCheck() >= 0) {//was already liked by current user
                            if (currentItem.getLikeL().size() - 1 == 0) {
                                holder.likesCount.setVisibility(View.GONE);
                                holder.like_image.setVisibility(View.GONE);
                            } else {
                                holder.likesCount.setVisibility(View.VISIBLE);
                                holder.like_image.setVisibility(View.VISIBLE);
                                holder.likesCount.setText(currentItem.getLikeL().size());
                            }
                            ///////////REMOVE CURRENT USER LIKE/////////////
                            currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                            currentItem.setLikeCheck(-1);

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();

                            DocumentReference flamedDoc = likeStore.collection("flameL")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                            batch.update(likeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                            batch.delete(flamedDoc);

                            batch.commit().addOnSuccessListener(task -> { });
                            ///////////////////BATCH WRITE///////////////////
                        } else if (currentItem.getLikeCheck() < 0 && currentItem.getLikeL() != null) {
                            Utility.vibrate(getApplicationContext());
                            if (currentItem.getLikeL().size() == 0) {
                                holder.like_image.setVisibility(View.GONE);
                                holder.likesCount.setVisibility(View.GONE);

                            } else {
                                holder.like_image.setVisibility(View.VISIBLE);
                                holder.likesCount.setVisibility(View.VISIBLE);
                                holder.likesCount.setText(currentItem.getLikeL().size());
                            }

                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                            currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);//For local changes

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            FlamedModel flamedModel = new FlamedModel();
                            long tsLong = System.currentTimeMillis();

                            flamedModel.setPostID(currentItem.getDocID());
                            flamedModel.setTs(tsLong);
                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                            flamedModel.setUserdp(COMMITEE_LOGO);
                            flamedModel.setUsername(COMMITTEE_NAME);
                            flamedModel.setPostUid(currentItem.getUid());

                            DocumentReference flamedDoc = likeStore.collection("flameL")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if (currentItem.getLikeL().size() % 5 == 0) {
                                batch.update(likeStore, "newTs", tsLong);
                            }
                            batch.commit().addOnSuccessListener(task -> { });
                            ///////////////////BATCH WRITE///////////////////
                        } else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                            Utility.vibrate(getApplicationContext());
                            holder.likesCount.setVisibility(View.VISIBLE);
                            holder.like_image.setVisibility(View.VISIBLE);
                            if (currentItem.getLikeL() != null) {
                                holder.likesCount.setText(currentItem.getLikeL().size() + 1);
                            } else {
                                holder.likesCount.setText("1");
                            }

                            //////////////ADD CURRENT USER TO LIKELIST//////////////////
                            currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                            currentItem.setLikeCheck(currentItem.getLikeL().size() - 1);
                            //For local changes current item like added to remote list end

                            ///////////////////BATCH WRITE///////////////////
                            WriteBatch batch = FirebaseFirestore.getInstance().batch();
                            FlamedModel flamedModel = new FlamedModel();
                            long tsLong = System.currentTimeMillis();

                            flamedModel.setPostID(currentItem.getDocID());
                            flamedModel.setTs(tsLong);
                            flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                            flamedModel.setUserdp(COMMITEE_LOGO);
                            flamedModel.setUsername(COMMITTEE_NAME);
                            flamedModel.setPostUid(currentItem.getUid());

                            DocumentReference flamedDoc = likeStore.collection("flameL")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                            batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                            batch.set(flamedDoc, flamedModel);
                            if (currentItem.getLikeL().size() % 5 == 0) {
                                batch.update(likeStore, "newTs", tsLong);
                            }
                            batch.commit().addOnSuccessListener(task -> { });
                            ///////////////////BATCH WRITE///////////////////
                        }
                    });
                /////FLAME/////

                /////COMMENT/////
                if (currentItem.getCmtNo() > 0) {
                    holder.commentimg.setVisibility(View.VISIBLE);
                    holder.commentCount.setVisibility(View.VISIBLE);
                    holder.commentCount.setText(Long.toString(currentItem.getCmtNo()));
                } else {
                    holder.commentimg.setVisibility(View.GONE);
                    holder.commentCount.setVisibility(View.GONE);
                }
                /////COMMENT/////
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

        VideoView reels_video;
        ImageView pujo_com_dp, like_image, commentimg, like, comment, share,back_reel,save_reel;
        TextView pujo_com_name, pujo_headline, likesCount, commentCount ;
        com.borjabravo.readmoretextview.ReadMoreTextView pujo_desc;
        com.airbnb.lottie.LottieAnimationView progress;

        ReelsItemViewHolder(View itemView) {
            super(itemView);

            reels_video = itemView.findViewById(R.id.reels_video);
            back_reel = itemView.findViewById(R.id.back_reel);
            save_reel = itemView.findViewById(R.id.save_reel);
            pujo_com_dp = itemView.findViewById(R.id.pujo_com_dp);
            pujo_com_name = itemView.findViewById(R.id.pujo_com_name);
            pujo_desc = itemView.findViewById(R.id.text_content44);
            progress = itemView.findViewById(R.id.progressAnim);
            pujo_headline = itemView.findViewById(R.id.headline);
            like = itemView.findViewById(R.id.drumbeat);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.share);
            like_image = itemView.findViewById(R.id.like_image);
            likesCount = itemView.findViewById(R.id.likes_count);
            commentimg = itemView.findViewById(R.id.comment_image);
            commentCount = itemView.findViewById(R.id.comment_count);
        }
    }
}