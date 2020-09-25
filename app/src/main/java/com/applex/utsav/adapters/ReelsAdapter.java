package com.applex.utsav.adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.airbnb.lottie.LottieAnimationView;
import com.applex.utsav.ActivityProfileCommittee;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.dialogs.BottomCommentsDialog;
import com.applex.utsav.dialogs.BottomFlamedByDialog;
import com.applex.utsav.models.FlamedModel;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.util.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.util.ArrayList;
import java.util.Objects;

public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ReelsItemViewHolder> {

    private ArrayList<ReelsPostModel> models;
    private Context context;
    private ViewPager2 verticalViewPager;
    private String COMMITTEE_LOGO, COMMITTEE_NAME;

    public ReelsAdapter(ArrayList<ReelsPostModel> models, Context context, ViewPager2 verticalViewPager) {
        this.models = models;
        this.context = context;
        this.verticalViewPager = verticalViewPager;
    }

    @NonNull
    @Override
    public ReelsItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        @SuppressLint("InflateParams")
        View itemView = layoutInflater.inflate(R.layout.item_view_all_reels, parent, false);
        return new ReelsItemViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReelsItemViewHolder holder, int position) {
        ReelsPostModel currentItem = models.get(position);
        IntroPref introPref = new IntroPref(context);
        COMMITTEE_LOGO = introPref.getUserdp();
        COMMITTEE_NAME = introPref.getFullName();

        holder.play_image.setVisibility(View.VISIBLE);
        holder.reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
        holder.reels_video.start();
        Picasso.get().load(currentItem.getFrame()).fit().into(holder.reels_image);

        holder.pujo_desc.setText(currentItem.getDescription());
        holder.pujo_com_name.setText(currentItem.getCommittee_name());
        holder.pujo_headline.setSelected(true);
        holder.pujo_headline.setSingleLine();

        holder.reels_video.setOnCompletionListener(v -> verticalViewPager.setCurrentItem(position + 1, true));

        holder.reels_video.setOnLongClickListener(view -> {
            holder.reels_video.pause();
            holder.play_image.setVisibility(View.VISIBLE);
            return false;
        });

        holder.play_image.setOnClickListener(view -> {
            holder.play_image.setVisibility(View.GONE);
            holder.reels_video.start();
        });

        String timeAgo = Utility.getTimeAgo(currentItem.getTs());
        holder.mins_ago.setText(timeAgo);
        if (timeAgo != null) {
            if (timeAgo.matches("just now")) {
                holder.mins_ago.setTextColor(Color.parseColor("#7700C853"));
            } else {
                holder.mins_ago.setTextColor(ContextCompat.getColor(context, R.color.white_transparent));
            }
        }

        holder.back_reel.setOnClickListener(v -> ((ReelsActivity)context).onBackPressed());
        holder.save_reel.setOnClickListener(v -> save_Dialog(currentItem.getVideo()));

        //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
        holder.pujo_com_dp.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityProfileCommittee.class);
            intent.putExtra("uid", currentItem.getUid());
            context.startActivity(intent);
        });

        holder.pujo_com_name.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityProfileCommittee.class);
            intent.putExtra("uid", currentItem.getUid());
            context.startActivity(intent);
        });
        //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

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
                holder.likesCount.setText(String.valueOf(currentItem.getLikeL().size()));

                holder.like_image.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Reels", currentItem.getDocID());
                    bottomSheetDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "FlamedBySheet");
                });
                holder.likesCount.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Reels", currentItem.getDocID());
                    bottomSheetDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "FlamedBySheet");
                });
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

                    //play animation, play audio

                    if (currentItem.getLikeCheck() >= 0) {//was already liked by current user
                        if (currentItem.getLikeL().size() - 1 == 0) {
                            holder.likesCount.setVisibility(View.GONE);
                            holder.like_image.setVisibility(View.GONE);
                        } else {
                            holder.likesCount.setVisibility(View.VISIBLE);
                            holder.like_image.setVisibility(View.VISIBLE);
                            holder.likesCount.setText(Integer.toString(currentItem.getLikeL().size()));
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

                        batch.commit().addOnSuccessListener(task -> notifyDataSetChanged());
                        ///////////////////BATCH WRITE///////////////////
                    }
                    else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                        Utility.vibrate(context);
                        holder.likesCount.setVisibility(View.VISIBLE);
                        holder.like_image.setVisibility(View.VISIBLE);
                        if (currentItem.getLikeL() != null) {
                            holder.likesCount.setText(Integer.toString(currentItem.getLikeL().size() + 1));
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
                        flamedModel.setType(new IntroPref(context).getType());
                        flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                        flamedModel.setUserdp(COMMITTEE_LOGO);
                        flamedModel.setUsername(COMMITTEE_NAME);
                        flamedModel.setPostUid(currentItem.getUid());

                        DocumentReference flamedDoc = likeStore.collection("flameL")
                                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                        batch.update(likeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                        batch.set(flamedDoc, flamedModel);
                        if (currentItem.getLikeL().size() % 5 == 0) {
                            batch.update(likeStore, "newTs", tsLong);
                        }
                        batch.commit().addOnSuccessListener(task -> notifyDataSetChanged());
                        ///////////////////BATCH WRITE///////////////////
                    }
                });
        /////FLAME/////

        /////COMMENT/////
        if (currentItem.getCmtNo() > 0) {
            holder.commentimg.setVisibility(View.VISIBLE);
            holder.commentCount.setVisibility(View.VISIBLE);
            holder.commentCount.setText(Long.toString(currentItem.getCmtNo()));

            holder.commentimg.setOnClickListener(v -> {
                BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Reels",currentItem.getDocID(), currentItem.getUid(), 2);
                bottomCommentsDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "CommentsSheet");
            });
            holder.commentCount.setOnClickListener(v -> {
                BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Reels",currentItem.getDocID(), currentItem.getUid(), 2);
                bottomCommentsDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "CommentsSheet");
            });
        }
        else {
            holder.commentimg.setVisibility(View.GONE);
            holder.commentCount.setVisibility(View.GONE);
        }
        /////COMMENT/////

        holder.comment.setOnClickListener(v -> {
            BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Reels",currentItem.getDocID(), currentItem.getUid(), 1);
            bottomCommentsDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "CommentsSheet");
        });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ReelsItemViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.reels_video.pause();
        holder.reels_image.setVisibility(View.VISIBLE);
        holder.play_image.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ReelsItemViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.reels_video.start();
        holder.reels_video.setOnPreparedListener(mediaPlayer -> new Handler().postDelayed(() -> {
            holder.video_playing.playAnimation();
            holder.play_image.setVisibility(View.GONE);
            holder.reels_image.setVisibility(View.GONE);
        }, 500));
    }

    @Override
    public int getItemCount() { return models.size(); }

    public static class ReelsItemViewHolder extends RecyclerView.ViewHolder {

        VideoView reels_video;
        ImageView pujo_com_dp, like_image, commentimg, like, comment, share,back_reel,save_reel, play_image, reels_image;
        TextView pujo_com_name, pujo_headline, likesCount, commentCount, mins_ago;
        com.borjabravo.readmoretextview.ReadMoreTextView pujo_desc;
        LottieAnimationView video_playing;

        ReelsItemViewHolder(View itemView) {
            super(itemView);

            reels_video = itemView.findViewById(R.id.reels_video);
            back_reel = itemView.findViewById(R.id.back_reel);
            save_reel = itemView.findViewById(R.id.save_reel);
            pujo_com_dp = itemView.findViewById(R.id.pujo_com_dp);
            pujo_com_name = itemView.findViewById(R.id.pujo_com_name);
            pujo_desc = itemView.findViewById(R.id.text_content44);
            pujo_headline = itemView.findViewById(R.id.headline);
            like = itemView.findViewById(R.id.drumbeat);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.share);
            like_image = itemView.findViewById(R.id.like_image);
            likesCount = itemView.findViewById(R.id.likes_count);
            commentimg = itemView.findViewById(R.id.comment_image);
            commentCount = itemView.findViewById(R.id.comment_count);
            play_image = itemView.findViewById(R.id.play);
            mins_ago = itemView.findViewById(R.id.mins_ago_reels);
            reels_image = itemView.findViewById(R.id.reels_image);
            video_playing = itemView.findViewById(R.id.progressAnim);
        }
    }

    private void save_Dialog(String url) {
        Dialog myDialogue = new Dialog(context);
        myDialogue.setContentView(R.layout.dialog_image_options);
        myDialogue.setCanceledOnTouchOutside(true);
        myDialogue.findViewById(R.id.saveToInternal).setOnClickListener(v -> {
            if(!Utility.checkStoragePermission(context)) {
                Utility.requestStoragePermission(context);
            }
            else {
                boolean bool = Utility.downloadVideo(url, context);
                if(bool) {
                    Toast.makeText(context, "Saved to device", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(context, "Something went wrong...", Toast.LENGTH_SHORT).show();
                }
                myDialogue.dismiss();
            }
        });
        myDialogue.show();
        Objects.requireNonNull(myDialogue.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
