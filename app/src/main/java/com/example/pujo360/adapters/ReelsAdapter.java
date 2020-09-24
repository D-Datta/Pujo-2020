package com.example.pujo360.adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import com.example.pujo360.ActivityProfileCommittee;
import com.example.pujo360.R;
import com.example.pujo360.ReelsActivity;
import com.example.pujo360.dialogs.BottomCommentsDialog;
import com.example.pujo360.dialogs.BottomFlamedByDialog;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.models.ReelsPostModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.util.Utility;
import com.example.pujo360.util.VerticalViewPager;
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

public class ReelsAdapter extends PagerAdapter {

    private ArrayList<ReelsPostModel> models;
    private Context context;
    private VideoView reels_video;
    private ImageView pujo_com_dp;
    private ImageView like_image;
    private ImageView play_image;
    private TextView likesCount;
    private VerticalViewPager verticalViewPager;
    private String COMMITTEE_LOGO, COMMITTEE_NAME;

    public ReelsAdapter(ArrayList<ReelsPostModel> models, Context context, VerticalViewPager verticalViewPager) {
        this.models = models;
        this.context = context;
        this.verticalViewPager = verticalViewPager;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View itemView = layoutInflater.inflate(R.layout.item_view_all_reels, container, false);

        ReelsPostModel currentItem = models.get(position);
        IntroPref introPref = new IntroPref(context);
        COMMITTEE_LOGO = introPref.getUserdp();
        COMMITTEE_NAME = introPref.getFullName();

        reels_video = itemView.findViewById(R.id.reels_video);
        ImageView back_reel = itemView.findViewById(R.id.back_reel);
        ImageView save_reel = itemView.findViewById(R.id.save_reel);
        pujo_com_dp = itemView.findViewById(R.id.pujo_com_dp);
        TextView pujo_com_name = itemView.findViewById(R.id.pujo_com_name);
        com.borjabravo.readmoretextview.ReadMoreTextView pujo_desc = itemView.findViewById(R.id.text_content44);
        TextView pujo_headline = itemView.findViewById(R.id.headline);
        ImageView like = itemView.findViewById(R.id.drumbeat);
        ImageView comment = itemView.findViewById(R.id.comment);
        ImageView share = itemView.findViewById(R.id.share);
        like_image = itemView.findViewById(R.id.like_image);
        likesCount = itemView.findViewById(R.id.likes_count);
        ImageView commentimg = itemView.findViewById(R.id.comment_image);
        TextView commentCount = itemView.findViewById(R.id.comment_count);
        play_image = itemView.findViewById(R.id.play);
        TextView mins_ago = itemView.findViewById(R.id.mins_ago_reels);

        reels_video.setVideoURI(Uri.parse(currentItem.getVideo()));
        reels_video.start();

        pujo_desc.setText(currentItem.getDescription());
        pujo_com_name.setText(currentItem.getCommittee_name());
        pujo_headline.setSelected(true);
        pujo_headline.setSingleLine();

        String timeAgo = Utility.getTimeAgo(currentItem.getTs());
        mins_ago.setText(timeAgo);
        if (timeAgo != null) {
            if (timeAgo.matches("just now")) {
                mins_ago.setTextColor(Color.parseColor("#7700C853"));
            } else {
                mins_ago.setTextColor(ContextCompat.getColor(context, R.color.white_transparent));
            }
        }

        reels_video.setOnCompletionListener(v -> verticalViewPager.setCurrentItem(position + 1, true));

        reels_video.setOnLongClickListener(view -> {
            reels_video.pause();
            play_image.setVisibility(View.VISIBLE);
            return false;
        });

        if(!reels_video.isPlaying()) {
            itemView.setOnClickListener(view -> {
                play_image.setVisibility(View.GONE);
                reels_video.resume();
            });
        }

        back_reel.setOnClickListener(v -> ((ReelsActivity)context).onBackPressed());
        save_reel.setOnClickListener(v -> save_Dialog(Uri.parse(currentItem.getVideo())));

        //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////
        pujo_com_dp.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityProfileCommittee.class);
            intent.putExtra("uid", currentItem.getUid());
            context.startActivity(intent);
        });

        pujo_com_name.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityProfileCommittee.class);
            intent.putExtra("uid", currentItem.getUid());
            context.startActivity(intent);
        });
        //////////////VISITING PROFILE AND USERDP FROM USERNAME FOR CURRENT POST USER///////////////

        if (currentItem.getCommittee_dp() != null && !currentItem.getCommittee_dp().isEmpty()) {
            Picasso.get().load(currentItem.getCommittee_dp()).fit().centerCrop()
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(pujo_com_dp, new Callback() {
                        @Override
                        public void onSuccess() { }

                        @Override
                        public void onError(Exception e) {
                            pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
                        }
                    });
        } else {
            pujo_com_dp.setImageResource(R.drawable.ic_account_circle_black_24dp);
        }

        DocumentReference likeStore;
        likeStore = FirebaseFirestore.getInstance().document("Reels/" + currentItem.getDocID() + "/");

        //INITIAL SETUP//
        if (currentItem.getLikeL() != null) {
            if (currentItem.getLikeL().size() == 0) {
                like_image.setVisibility(View.GONE);
                likesCount.setVisibility(View.GONE);
            } else {
                like_image.setVisibility(View.VISIBLE);
                likesCount.setVisibility(View.VISIBLE);
                likesCount.setText(String.valueOf(currentItem.getLikeL().size()));

                like_image.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Reels", currentItem.getDocID());
                    bottomSheetDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "FlamedBySheet");
                });
                likesCount.setOnClickListener(v -> {
                    BottomFlamedByDialog bottomSheetDialog = new BottomFlamedByDialog("Reels", currentItem.getDocID());
                    bottomSheetDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "FlamedBySheet");
                });
            }
        } else {
            like_image.setVisibility(View.GONE);
            likesCount.setVisibility(View.GONE);
        }
        //INITIAL SETUP//

        /////FLAME/////
        PushDownAnim.setPushDownAnimTo(like)
                .setScale(PushDownAnim.MODE_STATIC_DP, 6)
                .setOnClickListener(v -> {

                    //play animation, play audio

                    if (currentItem.getLikeCheck() >= 0) {//was already liked by current user
                        if (currentItem.getLikeL().size() - 1 == 0) {
                            likesCount.setVisibility(View.GONE);
                            like_image.setVisibility(View.GONE);
                        } else {
                            likesCount.setVisibility(View.VISIBLE);
                            like_image.setVisibility(View.VISIBLE);
                            likesCount.setText(Integer.toString(currentItem.getLikeL().size()));
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

                        batch.commit().addOnSuccessListener(task -> {});
                        ///////////////////BATCH WRITE///////////////////
                    }
                    else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                        Utility.vibrate(context);
                        likesCount.setVisibility(View.VISIBLE);
                        like_image.setVisibility(View.VISIBLE);
                        if (currentItem.getLikeL() != null) {
                            likesCount.setText(Integer.toString(currentItem.getLikeL().size() + 1));
                        } else {
                            likesCount.setText("1");
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
                        batch.commit().addOnSuccessListener(task -> {});
                        ///////////////////BATCH WRITE///////////////////
                    }
                });
        /////FLAME/////

        /////COMMENT/////
        if (currentItem.getCmtNo() > 0) {
            commentimg.setVisibility(View.VISIBLE);
            commentCount.setVisibility(View.VISIBLE);
            commentCount.setText(Long.toString(currentItem.getCmtNo()));

            commentimg.setOnClickListener(v -> {
                BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Reels",currentItem.getDocID(), currentItem.getUid(), 2);
                bottomCommentsDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "CommentsSheet");
            });
            commentCount.setOnClickListener(v -> {
                BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Reels",currentItem.getDocID(), currentItem.getUid(), 2);
                bottomCommentsDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "CommentsSheet");
            });
        }
        else {
            commentimg.setVisibility(View.GONE);
            commentCount.setVisibility(View.GONE);
        }
        /////COMMENT/////

        comment.setOnClickListener(v -> {
            BottomCommentsDialog bottomCommentsDialog = new BottomCommentsDialog("Reels",currentItem.getDocID(), currentItem.getUid(), 1);
            bottomCommentsDialog.show(((ReelsActivity)context).getSupportFragmentManager(), "CommentsSheet");
        });

        container.addView(itemView, 0);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        try{
            super.finishUpdate(container);
        } catch (NullPointerException nullPointerException){
            System.out.println("Catch the NullPointerException in FragmentPagerAdapter.finishUpdate");
        }
    }

    private void save_Dialog(Uri uri) {
        Dialog myDialogue = new Dialog(context);
        myDialogue.setContentView(R.layout.dialog_image_options);
        myDialogue.setCanceledOnTouchOutside(true);
        myDialogue.findViewById(R.id.saveToInternal).setOnClickListener(v -> {
            if(!Utility.checkStoragePermission(context)) {
                Utility.requestStoragePermission(context);
            }
            else {
                boolean bool = Utility.saveVideo(uri, context);
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
