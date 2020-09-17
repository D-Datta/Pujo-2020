package com.example.pujo360.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pujo360.ActivityProfileCommittee;
import com.example.pujo360.ActivityProfileUser;
import com.example.pujo360.CommentReplyActivity;
import com.example.pujo360.LinkPreview.ApplexLinkPreviewShort;
import com.example.pujo360.LinkPreview.ViewListener;
import com.example.pujo360.R;
import com.example.pujo360.models.CommentModel;
import com.example.pujo360.models.FlamedModel;
import com.example.pujo360.preferences.IntroPref;
import com.example.pujo360.dialogs.BottomFlamedByDialog2;
import com.example.pujo360.util.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;
import com.thekhaeng.pushdownanim.PushDownAnim;
import java.util.List;
import java.util.Objects;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ProgrammingViewHolder> {

    private List<CommentModel> itemDatalist;
    private Context mContext;
    private String PROFILEPIC;
    private String USERNAME;
    private int bool;
    private OnClickListener mListener;

    public interface OnClickListener {
        void onClickListener(int position);
    }

    public void onClickListener(OnClickListener listener) {
        mListener= listener;
    }


    public CommentAdapter(Context context, List<CommentModel> itemDatalist, int bool) {
        this.mContext = context;
        this.itemDatalist = itemDatalist;
        this.bool = bool;//1 = ViewMoreHome 2 = ReelsActivity

        IntroPref introPref = new IntroPref(mContext);
        PROFILEPIC = introPref.getUserdp();
        USERNAME = introPref.getFullName();
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View v = layoutInflater.inflate(R.layout.item_comments, viewGroup, false);
        return new ProgrammingViewHolder(v, mListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder programmingViewHolder, int i) {
        CommentModel currentItem = itemDatalist.get(i);
        DocumentReference likeStore = null;
        CollectionReference flamedCol = null;

        if(currentItem.getTs() == -1L) {
            programmingViewHolder.minsago.setText("Failed!");
            programmingViewHolder.minsago.setTextColor(Color.parseColor("#ffff4444"));
        }
        else if(currentItem.getTs() == 0L){
            programmingViewHolder.minsago.setText("Pending...");
            programmingViewHolder.minsago.setTextColor(Color.parseColor("#FF9800"));
        }
        else {
            String timeAgo = Utility.getTimeAgo(currentItem.getTs());
            programmingViewHolder.minsago.setText(timeAgo);
            if(timeAgo != null) {
                if (timeAgo.matches("just now")) {
                    programmingViewHolder.minsago.setTextColor(Color.parseColor("#00C853"));
                }
            }
        }

        //////////////LOADING USERNAME AND USERDP FROM USERNODE/////////////
        String userimage_url = currentItem.getUserdp();
        if(userimage_url!=null) {
            Picasso.get().load(userimage_url)
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .into(programmingViewHolder.userimage);
        }
        else {
            programmingViewHolder.userimage.setImageResource(R.drawable.ic_account_circle_black_24dp);
        }

        programmingViewHolder.username.setText(currentItem.getUsername());
        //////////////LOADING USERNAME AND USERDP FROM USERNODE/////////////

        programmingViewHolder.username.setOnClickListener(v -> {
            if(currentItem.getType().matches("com")) {
                Intent intent = new Intent(mContext, ActivityProfileCommittee.class);
                intent.putExtra("uid", currentItem.getUid());
                mContext.startActivity(intent);
            }
            else {
                Intent intent = new Intent(mContext, ActivityProfileUser.class);
                intent.putExtra("uid", currentItem.getUid());
                mContext.startActivity(intent);
            }
        });

        programmingViewHolder.userimage.setOnClickListener(v -> {
            if(currentItem.getType().matches("com")) {
                Intent intent = new Intent(mContext, ActivityProfileCommittee.class);
                intent.putExtra("uid", currentItem.getUid());
                mContext.startActivity(intent);
            }
            else {
                Intent intent = new Intent(mContext, ActivityProfileUser.class);
                intent.putExtra("uid", currentItem.getUid());
                mContext.startActivity(intent);
            }
        });

        programmingViewHolder.comment.setText(currentItem.getComment());
        if(programmingViewHolder.comment.getUrls().length>0){
            URLSpan urlSnapItem = programmingViewHolder.comment.getUrls()[0];
            String url = urlSnapItem.getURL();
            if(url.contains("http")){
                programmingViewHolder.LinkPreview.setVisibility(View.VISIBLE);
                programmingViewHolder.LinkPreview.setLink(url ,new ViewListener() {
                    @Override
                    public void onSuccess(boolean status) { }

                    @Override
                    public void onError(Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            //do stuff like remove view etc
                            programmingViewHolder.LinkPreview.setVisibility(View.GONE);
                        });
                    }
                });
            }
            else {
                programmingViewHolder.LinkPreview.setVisibility(View.GONE);
            }
        }

        ///////////////////FLAMES///////////////////////
        if(bool == 1) {
            flamedCol = FirebaseFirestore.getInstance()
                    .collection("Feeds/"+currentItem.getPostID()+"/commentL/"+currentItem.getDocID()+"/flameL/");
            likeStore = FirebaseFirestore.getInstance()
                    .document("Feeds/"+currentItem.getPostID()+"/commentL/"+currentItem.getDocID()+"/");
        } else if(bool == 2) {
            flamedCol = FirebaseFirestore.getInstance()
                    .collection("Reels/"+currentItem.getPostID()+"/commentL/"+currentItem.getDocID()+"/flameL/");
            likeStore = FirebaseFirestore.getInstance()
                    .document("Reels/"+currentItem.getPostID()+"/commentL/"+currentItem.getDocID()+"/");
        }

        //INITIAL SETUP//
        if(currentItem.getLikeL() != null){
            /////////////////UPDATNG FLAMED BY NO.//////////////////////
            programmingViewHolder.cFlamedBy.setText(String.valueOf(currentItem.getLikeL().size()));

            for(int j = 0; j < currentItem.getLikeL().size(); j++){
                if(currentItem.getLikeL().get(j).matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))){
                    programmingViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);
                    currentItem.setLikeCheck(j);
                    programmingViewHolder.cFlamedBy.setText(String.valueOf(currentItem.getLikeL().size()));
                    //Position in likeList where the current USer UId is found stored in likeCheck
                }
            }
        } else {
            programmingViewHolder.cFlamedBy.setText("0");
            programmingViewHolder.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
        }
        //INITIAL SETUP//

        CollectionReference finalFlamedCol = flamedCol;
        DocumentReference finalLikeStore = likeStore;
        PushDownAnim.setPushDownAnimTo(programmingViewHolder.flameimg)
            .setScale(PushDownAnim.MODE_STATIC_DP, 6)
            .setOnClickListener(v -> {
                if(currentItem.getLikeCheck() >= 0){//was already liked by current user
                    programmingViewHolder.flameimg.setImageResource(R.drawable.ic_btmnav_notifications);
                    programmingViewHolder.cFlamedBy.setText(String.valueOf(currentItem.getLikeL().size()-1));
                    ///////////REMOVE CURRENT USER LIKE/////////////
                    currentItem.removeFromLikeList(FirebaseAuth.getInstance().getUid());
                    currentItem.setLikeCheck(-1);

                    //likeStore.update("likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));

                    ///////////////////BATCH WRITE///////////////////
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                    DocumentReference flamedDoc = Objects.requireNonNull(finalFlamedCol)
                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                    batch.update(finalLikeStore, "likeL", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid()));
                    batch.delete(flamedDoc);

                    batch.commit().addOnSuccessListener(task -> { });
                    ///////////////////BATCH WRITE///////////////////
                }

                else if(currentItem.getLikeCheck() < 0 && currentItem.getLikeL()!=null){
                    Utility.vibrate(mContext);
                    programmingViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);

                    //////////////ADD CURRENT USER TO LIKELIST//////////////////
                    currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                    currentItem.setLikeCheck(currentItem.getLikeL().size()-1);//For local changes

                    programmingViewHolder.cFlamedBy.setText(String.valueOf(currentItem.getLikeL().size()));

                    ///////////////////BATCH WRITE///////////////////
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    FlamedModel flamedModel = new FlamedModel();
                    long tsLong = System.currentTimeMillis();

                    flamedModel.setPostID(currentItem.getPostID());
                    flamedModel.setDocID(currentItem.getDocID());
                    flamedModel.setTs(tsLong);
                    flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                    flamedModel.setUserdp(PROFILEPIC);
                    flamedModel.setUsername(USERNAME);
                    flamedModel.setPostUid(currentItem.getUid());

                    DocumentReference flamedDoc = Objects.requireNonNull(finalFlamedCol)
                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                    batch.update(finalLikeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                    batch.set(flamedDoc, flamedModel);
                    batch.commit().addOnSuccessListener(task -> { });
                    ///////////////////BATCH WRITE///////////////////
                }

                else { //WHEN CURRENT USER HAS NOT LIKED OR NO ONE HAS LIKED
                    Utility.vibrate(mContext);
                    programmingViewHolder.flameimg.setImageResource(R.drawable.ic_flame_red);

                    //////////////ADD CURRENT USER TO LIKELIST//////////////////
                    currentItem.addToLikeList(FirebaseAuth.getInstance().getUid());
                    currentItem.setLikeCheck(currentItem.getLikeL().size()-1);
                    //For local changes current item like added to remote list end

                    programmingViewHolder.cFlamedBy.setText(String.valueOf(currentItem.getLikeL().size()));

                    ///////////////////BATCH WRITE///////////////////
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    FlamedModel flamedModel = new FlamedModel();
                    long tsLong = System.currentTimeMillis();

                    flamedModel.setPostID(currentItem.getPostID());
                    flamedModel.setDocID(currentItem.getDocID());
                    flamedModel.setTs(tsLong);
                    flamedModel.setUid(FirebaseAuth.getInstance().getUid());
                    flamedModel.setUserdp(PROFILEPIC);
                    flamedModel.setUsername(USERNAME);
                    flamedModel.setPostUid(currentItem.getUid());

                    DocumentReference flamedDoc = Objects.requireNonNull(finalFlamedCol)
                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                    batch.update(finalLikeStore, "likeL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()));
                    batch.set(flamedDoc, flamedModel);
                    batch.commit().addOnSuccessListener(task -> { });
                    ///////////////////BATCH WRITE///////////////////
                }
            });

        programmingViewHolder.cFlamedBy.setOnClickListener(v -> {
            if(currentItem.getLikeL() != null && currentItem.getLikeL().size() > 0){
                BottomFlamedByDialog2 bottomSheetDialog = null;
                    if(bool == 1) {
                        bottomSheetDialog = new BottomFlamedByDialog2("Home", currentItem.getPostID(), currentItem.getDocID());
                    } else if(bool == 2) {
                        bottomSheetDialog = new BottomFlamedByDialog2("Reels", currentItem.getPostID(), currentItem.getDocID());
                    }
                Objects.requireNonNull(bottomSheetDialog).show(((FragmentActivity)mContext).getSupportFragmentManager(), "FlamedBySheet"); }
            else
                Toast.makeText(mContext, "No flames", Toast.LENGTH_SHORT).show();
        });
        ///////////////////FLAMES///////////////////////

        /////////////////COMMENTS REPLY SETUP/////////////////
        if(currentItem.getrCmtNo() > 0) {
            programmingViewHolder.commentimg.setImageResource(R.drawable.comment_yellow);
            programmingViewHolder.cRepliedBy.setText(String.valueOf(currentItem.getrCmtNo()));
            programmingViewHolder.view_replies.setText("View Replies");
            programmingViewHolder.view_replies.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, CommentReplyActivity.class);
                intent.putExtra("username", currentItem.getUsername());
                intent.putExtra("userdp", currentItem.getUserdp());
                intent.putExtra("docID", currentItem.getDocID());
                intent.putExtra("postID", currentItem.getPostID());
                intent.putExtra("postUid", currentItem.getPostUid());
                intent.putExtra("likeL", currentItem.getLikeL());
                intent.putExtra("comment", currentItem.getComment());
                intent.putExtra("ReplyCommentNo", Integer.toString(currentItem.getrCmtNo()));
                intent.putExtra("uid", currentItem.getUid());
                intent.putExtra("bool", Integer.toString(bool));
                intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                intent.putExtra("pComUid", currentItem.getUid());
                mContext.startActivity(intent);
            });
        }
        else {
            programmingViewHolder.cRepliedBy.setText("0");
            programmingViewHolder.commentimg.setImageResource(R.drawable.ic_comment);
            programmingViewHolder.view_replies.setText("Reply");
            programmingViewHolder.view_replies.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, CommentReplyActivity.class);
                intent.putExtra("username", currentItem.getUsername());
                intent.putExtra("userdp", currentItem.getUserdp());
                intent.putExtra("docID", currentItem.getDocID());
                intent.putExtra("postID", currentItem.getPostID());
                intent.putExtra("postUid", currentItem.getPostUid());
                intent.putExtra("likeL", currentItem.getLikeL());
                intent.putExtra("comment", currentItem.getComment());
                intent.putExtra("ReplyCommentNo", Integer.toString(currentItem.getrCmtNo()));
                intent.putExtra("uid", currentItem.getUid());
                intent.putExtra("bool", Integer.toString(bool));
                intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
                intent.putExtra("pComUid", currentItem.getUid());
                mContext.startActivity(intent);
            });
        }

        programmingViewHolder.commentimg.setOnClickListener(v -> {

            Intent intent = new Intent(mContext, CommentReplyActivity.class);
            intent.putExtra("username", currentItem.getUsername());
            intent.putExtra("userdp", currentItem.getUserdp());
            intent.putExtra("docID", currentItem.getDocID());
            intent.putExtra("postID", currentItem.getPostID());
            intent.putExtra("postUid", currentItem.getPostUid());
            intent.putExtra("likeL", currentItem.getLikeL());
            intent.putExtra("comment", currentItem.getComment());
            intent.putExtra("ReplyCommentNo", Integer.toString(currentItem.getrCmtNo()));
            intent.putExtra("uid", currentItem.getUid());
            intent.putExtra("bool", Integer.toString(bool));
            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
            intent.putExtra("pComUid", currentItem.getUid());
            mContext.startActivity(intent);

        });

        programmingViewHolder.cRepliedBy.setOnClickListener(v -> {

            Intent intent = new Intent(mContext, CommentReplyActivity.class);
            intent.putExtra("username", currentItem.getUsername());
            intent.putExtra("userdp", currentItem.getUserdp());
            intent.putExtra("docID", currentItem.getDocID());
            intent.putExtra("postID", currentItem.getPostID());
            intent.putExtra("postUid", currentItem.getPostUid());
            intent.putExtra("likeL", currentItem.getLikeL());
            intent.putExtra("comment", currentItem.getComment());
            intent.putExtra("ReplyCommentNo", Integer.toString(currentItem.getrCmtNo()));
            intent.putExtra("uid", currentItem.getUid());
            intent.putExtra("bool", Integer.toString(bool));
            intent.putExtra("timestamp", Long.toString(currentItem.getTs()));
            intent.putExtra("pComUid", currentItem.getUid());
            mContext.startActivity(intent);
        });
        /////////////////COMMENTS REPLY SETUP/////////////////
    }

    @Override
    public int getItemCount() { return itemDatalist.size(); }

    static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        ImageView userimage, more, flameimg, commentimg;
        TextView minsago, cFlamedBy, cRepliedBy, username, comment, view_replies;
        ApplexLinkPreviewShort LinkPreview;

        ProgrammingViewHolder(@NonNull View itemView, OnClickListener listener) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            userimage = itemView.findViewById(R.id.user_image);
            minsago=itemView.findViewById(R.id.mins_ago);
            comment = itemView.findViewById(R.id.comment);
            more = itemView.findViewById(R.id.comment_more);
            LinkPreview = itemView.findViewById(R.id.LinkPreViewComment);
            flameimg = itemView.findViewById(R.id.flame_comment);
            commentimg = itemView.findViewById(R.id.comment_reply);
            cFlamedBy = itemView.findViewById(R.id.flamed_by_comment);
            cRepliedBy = itemView.findViewById(R.id.replied_by);
            view_replies = itemView.findViewById(R.id.view_replies);

            more.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION ) {
                        listener.onClickListener(position);
                    }
                }
            });

        }
    }
}