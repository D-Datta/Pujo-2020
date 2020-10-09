package com.applex.utsav.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.applex.utsav.CommentEdit;
import com.applex.utsav.R;
import com.applex.utsav.ViewMoreHome;
import com.applex.utsav.ViewMoreText;
import com.applex.utsav.adapters.CommentAdapter;
import com.applex.utsav.adapters.ReelsAdapter;
import com.applex.utsav.fragments.CommitteeFragment;
import com.applex.utsav.fragments.FeedsFragment;
import com.applex.utsav.fragments.Fragment_Posts_Users;
import com.applex.utsav.models.CommentModel;
import com.applex.utsav.preferences.IntroPref;
import com.applex.utsav.utility.BasicUtility;
import com.applex.utsav.utility.InternetConnection;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Objects;
import static com.applex.utsav.adapters.ReelsAdapter.currentItem;
import static java.lang.Boolean.TRUE;

public class BottomCommentsDialog extends DialogFragment {

    private RecyclerView commentRecycler;
    private CommentAdapter commentAdapter;
    private ArrayList<CommentModel> models;
    private ProgressBar progressBar, progressComment;
    private String docID,root,from;
    private DocumentSnapshot lastVisible;
    private int checkGetMore = -1;
    private EditText newComment;
    private ImageView send, commentimg;
    private DocumentReference docRef;
    private LinearLayout no_comment;
    private CollectionReference commentRef;
    private BottomSheetDialog commentMenuDialog;
    private ProgressDialog progressDialog;
    private String uid;
    private String type;
    private String ts;
    private String pCom_ts;
    private long cmntno, finalcmntno;
    private int getBool;

    public BottomCommentsDialog() {
        // Required empty public constructor
    }

    public static BottomCommentsDialog newInstance(String root,String docID, String uid, int bool, String from, String type, long cmntno, String ts, String pCom_ts) {
        Bundle args = new Bundle();
        args.putString("root", root);
        args.putString("docID", docID);
        args.putString("uid", uid);
        args.putInt("bool", bool);
        args.putString("from", from);
        args.putString("type", type);
        args.putLong("cmntno", cmntno);
        args.putString("ts", ts);
        args.putString("pCom_ts", pCom_ts);
        BottomCommentsDialog f = new BottomCommentsDialog();
        f.setArguments(args);
        return f;
    }

//    public BottomCommentsDialog(String root,String docID, String uid, int bool, String from, String type, long cmntno, String ts, String pCom_ts) {
//        this.root = root;
//        this.docID = docID;
//        this.uid = uid;
//        this.bool = bool;
//        this.from = from;
//        this.type = type;
//        this.cmntno = cmntno;
//        this.ts = ts;
//        this.pCom_ts = pCom_ts;
//    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheetcomments, container, false);
        EditText newComment = view.findViewById(R.id.new_comment);

        root = Objects.requireNonNull(getArguments()).getString("root");
        docID = Objects.requireNonNull(getArguments()).getString("docID");
        uid = Objects.requireNonNull(getArguments()).getString("uid");
        int bool = Objects.requireNonNull(getArguments()).getInt("bool");
        from = Objects.requireNonNull(getArguments()).getString("from");
        type = Objects.requireNonNull(getArguments()).getString("type");
        cmntno = Objects.requireNonNull(getArguments()).getLong("cmntno");
        ts = Objects.requireNonNull(getArguments()).getString("ts");
        pCom_ts = Objects.requireNonNull(getArguments()).getString("pCom_ts");

        if(bool == 1) {
            newComment.requestFocus();
            Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        return view;
    }
    
    @SuppressLint("SetTextI18n")
    @Override

    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        commentRecycler =v.findViewById(R.id.flamed_recycler);
        progressBar = v.findViewById(R.id.progress5);
        ImageView dismiss = v.findViewById(R.id.dismissflame);
        NestedScrollView nestedScrollView = v.findViewById(R.id.scroll_view);
        nestedScrollView.setNestedScrollingEnabled(true);

        no_comment = v.findViewById(R.id.emptyLayout);
        commentimg = v.findViewById(R.id.user_image_comment);
        send = v.findViewById(R.id.send_comment);
        progressComment = v.findViewById(R.id.commentProgress);
        newComment = v.findViewById(R.id.new_comment);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRecycler.setLayoutManager(layoutManager);
        commentRecycler.setItemAnimator(new DefaultItemAnimator());
        commentRecycler.setNestedScrollingEnabled(true);
        commentRecycler.setHasFixedSize(false);

        commentRef = FirebaseFirestore.getInstance().collection(root + "/" + docID + "/commentL/");
        docRef = FirebaseFirestore.getInstance().document(root + "/" + docID + "/");
        finalcmntno = cmntno;

        if(root.matches("Feeds")) {
            getBool = 1;
        } else if(root.matches("Reels")) {
            getBool = 2;
        }

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(vv, scrollX, scrollY, oldScrollX, oldScrollY) ->{
            if(vv.getChildAt(vv.getChildCount() - 1) != null) {
                if((scrollY >= (vv.getChildAt(vv.getChildCount() - 1).getMeasuredHeight() - vv.getMeasuredHeight() )) &&
                        scrollY > oldScrollY) {
                    if(checkGetMore != -1){
                        if(progressBar.getVisibility() == View.GONE) {
                            progressBar.setVisibility(View.VISIBLE);
                            fetchMore_flames();//Load more data
                        }
                    }
                }
            }
        });

        Picasso.get()
                .load(new IntroPref(requireActivity()).getUserdp()).fit().centerCrop()
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(commentimg, new Callback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onError(Exception e) {
                        commentimg.setImageResource(R.drawable.ic_account_circle_black_24dp);
                    }
                });

        commentimg.setOnClickListener(v2 -> {
            newComment.requestFocus();
            InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(newComment, InputMethodManager.SHOW_IMPLICIT);
            ///////////ENABLE KEYBOARD//////////
        });

        models = new ArrayList<>();
        commentAdapter = new CommentAdapter(getActivity(), models, getBool, type, ts, pCom_ts);

        send.setOnClickListener(v2 -> {
            if(InternetConnection.checkConnection(requireActivity())) {
                if(newComment.getText().toString().isEmpty()) {
                    BasicUtility.showToast(requireActivity(), "Thoughts need to be typed...");
                }
                else {
                    send.setVisibility(View.GONE);
                    progressComment.setVisibility(View.VISIBLE);
                    String comment = newComment.getText().toString().trim();
                    long tsLong = System.currentTimeMillis();
                    CommentModel commentModel = new CommentModel();

                    commentModel.setComment(comment);
                    commentModel.setType(new IntroPref(requireActivity()).getType());
                    commentModel.setUid(FirebaseAuth.getInstance().getUid());
                    commentModel.setPostUid(uid);
                    commentModel.setUserdp(new IntroPref(requireActivity()).getUserdp());
                    commentModel.setUsername(new IntroPref(requireActivity()).getFullName());
                    commentModel.setTs(0L); ///Pending state
                    commentModel.setPostID(docID);

                    newComment.setText("");
                    models.add(0,commentModel);
                    commentAdapter.notifyItemInserted(0);

                    ///////////////////BATCH WRITE///////////////////
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();

                    DocumentReference cmtDoc = commentRef.document(Long.toString(tsLong));
                    commentModel.setTs(tsLong);
                    commentModel.setDocID(Long.toString(tsLong));

                    batch.set(cmtDoc, commentModel);
                    batch.update(docRef, "cmtNo", FieldValue.increment(1));

                    batch.commit().addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            no_comment.setVisibility(View.GONE);
                            send.setVisibility(View.VISIBLE);
                            progressComment.setVisibility(View.GONE);
                            commentRecycler.setVisibility(View.VISIBLE);
                            buildRecyclerView_comments();

                            if(from.matches("ViewMoreHome")){
                                finalcmntno= new ViewMoreHome().homePostModel[0].getCmtNo()+1;
                                ViewMoreHome.comment_layout.setVisibility(View.VISIBLE);
                                ViewMoreHome.noofcmnts.setText(Long.toString(finalcmntno));
//                                CommitteeFragment.changed=1;
//                                FeedsFragment.changed=1;
//                                ActivityProfileUser.change=1;
                                new ViewMoreHome().homePostModel[0].setCmtNo(finalcmntno);
                            }
                            else if(from.matches("ViewMoreText")){
                                finalcmntno=new ViewMoreText().homeTextModel[0].getCmtNo()+1;
                                ViewMoreText.comment_layout.setVisibility(View.VISIBLE);
                                ViewMoreText.noofcmnts.setText(Long.toString(finalcmntno));
//                                FeedsFragment.changed=1;
//                                ActivityProfileUser.change=1;
                                new ViewMoreText().homeTextModel[0].setCmtNo(finalcmntno);
                            }
                            else if(from.matches("ReelsAdapter")){
                                finalcmntno=currentItem.getCmtNo()+1;
                                ReelsAdapter.ReelsItemViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                ReelsAdapter.ReelsItemViewHolder.commentCount.setText(Long.toString(finalcmntno));
//                                CommitteeFragment.changed=1;
                                currentItem.setCmtNo(finalcmntno);
                            }
                            else {
                                docRef.get().addOnCompleteListener(task1 -> {
                                    finalcmntno = Long.parseLong(Objects.requireNonNull(Objects.requireNonNull(task1.getResult()).get("cmtNo")).toString());
                                    if(from.matches("ActivityProfileUser")) {
                                        Fragment_Posts_Users.ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                        Fragment_Posts_Users.ProgrammingViewHolder.commentCount.setText(Long.toString(finalcmntno));
                                    }
                                    else if(from.matches("CommitteeFragment")) {
                                        CommitteeFragment.ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                        CommitteeFragment.ProgrammingViewHolder.commentCount.setText(Long.toString(finalcmntno));
                                    }
                                    else if(from.matches("FeedsFragment")) {
                                        FeedsFragment.FeedViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                        FeedsFragment.FeedViewHolder.commentCount.setText(Long.toString(finalcmntno));
                                    }
                                });
                            }
                        }
                        else {
                            commentModel.setTs(0L); ///Pending state
                            models.remove(commentModel);
                            commentModel.setTs(-1L);
                            models.add(0, commentModel);
                            commentAdapter.notifyDataSetChanged();
                            send.setVisibility(View.VISIBLE);
                            progressComment.setVisibility(View.GONE);
                            Toast.makeText(requireActivity(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ///////////////////BATCH WRITE///////////////////
                }
            }
            else {
                BasicUtility.showToast(requireActivity(), "Network unavailable...");
            }
        });

        buildRecyclerView_comments();

        dismiss.setOnClickListener(v1 -> {
            BasicUtility.hideKeyboard(requireActivity(), newComment);
            BottomCommentsDialog.super.onDestroyView();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        //Make dialog full screen with transparent background
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }
    }

    @SuppressLint("SetTextI18n")
    private void buildRecyclerView_comments(){
        progressBar.setVisibility(View.VISIBLE);
        models = new ArrayList<>();

        commentRef.orderBy("ts", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())){
                    CommentModel commentModel = document.toObject(CommentModel.class);
                    Objects.requireNonNull(commentModel).setDocID(document.getId());
                    models.add(commentModel);
                }
                if (models.size() > 0) {
                    commentAdapter = new CommentAdapter(getActivity(), models, getBool, type, ts, pCom_ts);
                    commentAdapter.onClickListener(position -> {
                        if( models.get(position).getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                || uid.matches(FirebaseAuth.getInstance().getUid())) {
                            commentMenuDialog= new BottomSheetDialog(requireActivity());
                            commentMenuDialog.setContentView(R.layout.dialog_comment_menu2);
                            if( models.get(position).getUid().matches(FirebaseAuth.getInstance().getUid())) {
                                commentMenuDialog.findViewById(R.id.edit_comment).setVisibility(View.VISIBLE);
                                commentMenuDialog.findViewById(R.id.edit_comment).setOnClickListener(v12 -> {
                                    Intent intent = new Intent(requireActivity(), CommentEdit.class);
                                    intent.putExtra("comment_home",models.get(position).getComment());
                                    intent.putExtra("com_img_home",models.get(position).getUserdp());
                                    intent.putExtra("com_postID_home",models.get(position).getPostID());
                                    intent.putExtra("com_docID_home",models.get(position).getDocID());
                                    intent.putExtra("root", root);
                                    intent.putExtra("from", "no");
                                    startActivity(intent);

                                    commentMenuDialog.dismiss();
                                });
                            }
                            commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                            commentMenuDialog.findViewById(R.id.delete_post).setVisibility(View.VISIBLE);
                            commentMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v12 -> {
                                progressDialog = new ProgressDialog(requireActivity());
                                progressDialog.setTitle("Deleting Comment");
                                progressDialog.setMessage("Please wait...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                ///////////////////BATCH WRITE///////////////////
                                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                int total = models.get(position).getrCmtNo() + 1;

                                DocumentReference cmtDoc = commentRef.document(models.get(position).getDocID());
                                batch.delete(cmtDoc);
                                batch.update(docRef, "cmtNo", FieldValue.increment(-(total)));

                                batch.commit().addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        models.remove(position);
                                        commentAdapter.notifyItemRemoved(position);

                                        if(from.matches("ViewMoreHome")){
                                            finalcmntno = new ViewMoreHome().homePostModel[0].getCmtNo()-total;
                                            new ViewMoreHome().homePostModel[0].setCmtNo(finalcmntno);
                                            if(finalcmntno <= 0) {
                                                ViewMoreHome.comment_layout.setVisibility(View.GONE);
                                            } else {
                                                ViewMoreHome.comment_layout.setVisibility(View.VISIBLE);
                                                ViewMoreHome.noofcmnts.setText(Long.toString(finalcmntno));
                                            }
//                                            CommitteeFragment.changed=1;
//                                            FeedsFragment.changed=1;
//                                            ActivityProfileUser.change=1;
                                        }
                                        else if(from.matches("ViewMoreText")){
                                            finalcmntno = new ViewMoreText().homeTextModel[0].getCmtNo()-total;
                                            new ViewMoreText().homeTextModel[0].setCmtNo(finalcmntno);
                                            if(finalcmntno <= 0) {
                                                ViewMoreText.comment_layout.setVisibility(View.GONE);
                                            } else {
                                                ViewMoreText.comment_layout.setVisibility(View.VISIBLE);
                                                ViewMoreText.noofcmnts.setText(Long.toString(finalcmntno));
                                            }
//                                            FeedsFragment.changed=1;
//                                            ActivityProfileUser.change=1;
                                        }
                                        else if(from.matches("ReelsAdapter")){
                                            finalcmntno=currentItem.getCmtNo()-total;
                                            currentItem.setCmtNo(finalcmntno);
                                            if(finalcmntno <= 0) {
                                                ReelsAdapter.ReelsItemViewHolder.comment_layout.setVisibility(View.GONE);
                                            } else {
                                                ReelsAdapter.ReelsItemViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                                ReelsAdapter.ReelsItemViewHolder.commentCount.setText(Long.toString(finalcmntno));
                                            }
//                                            CommitteeFragment.changed=1;
                                        }
                                        else {
                                            docRef.get().addOnCompleteListener(task2 -> {
                                                finalcmntno = Long.parseLong(Objects.requireNonNull(Objects.requireNonNull(task2.getResult()).get("cmtNo")).toString());
                                                if(finalcmntno <= 0) {
                                                    if(from.matches("ActivityProfileUser")){
                                                        Fragment_Posts_Users.ProgrammingViewHolder.comment_layout.setVisibility(View.GONE);
                                                    }
                                                    else if(from.matches("CommitteeFragment")){
                                                        CommitteeFragment.ProgrammingViewHolder.comment_layout.setVisibility(View.GONE);
                                                    }
                                                    else if(from.matches("FeedsFragment")){
                                                        FeedsFragment.FeedViewHolder.comment_layout.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    if(from.matches("ActivityProfileUser")){
                                                        Fragment_Posts_Users.ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                                        Fragment_Posts_Users.ProgrammingViewHolder.commentCount.setText(Long.toString(finalcmntno));
                                                    }
                                                    else if(from.matches("CommitteeFragment")){
                                                        CommitteeFragment.ProgrammingViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                                        CommitteeFragment.ProgrammingViewHolder.commentCount.setText(Long.toString(finalcmntno));
                                                    }
                                                    else if(from.matches("FeedsFragment")){
                                                        FeedsFragment.FeedViewHolder.comment_layout.setVisibility(View.VISIBLE);
                                                        FeedsFragment.FeedViewHolder.commentCount.setText(Long.toString(finalcmntno));
                                                    }
                                                }
                                            });
                                        }
                                        if(commentAdapter.getItemCount() == 0){
                                            no_comment.setVisibility(View.VISIBLE);
                                        }
                                        progressDialog.dismiss();
                                    }
                                    else {
                                        progressDialog.dismiss();
                                        Toast.makeText(requireActivity(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                ///////////////////BATCH WRITE///////////////////

                                if(models.size() == 0) {
                                    commentimg.setImageResource(R.drawable.ic_comment);
                                    no_comment.setVisibility(View.VISIBLE);
                                }
                                commentMenuDialog.dismiss();
                            });

                            commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                                commentRef.document(models.get(position).getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(requireActivity(), "Comment has been reported."));
                                commentMenuDialog.dismiss();
                            });
                        }
                        else {
                            commentMenuDialog= new BottomSheetDialog(requireActivity());
                            commentMenuDialog.setContentView(R.layout.dialog_comment_menu);
                            commentMenuDialog.setCanceledOnTouchOutside(TRUE);

                            commentMenuDialog.findViewById(R.id.report_post).setOnClickListener(v12 -> {
                                commentRef.document(models.get(position).getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(requireActivity(), "Comment has been reported."));
                                commentMenuDialog.dismiss();
                            });
                        }

                        Objects.requireNonNull(commentMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        commentMenuDialog.show();
                    });

                    commentRecycler.setAdapter(commentAdapter);

                    if(task.getResult().size() > 0)
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                    if(models.size() < 10) {
                        checkGetMore = -1;
                    } else {
                        checkGetMore = 0;
                    }
                }
                else  {
                    no_comment.setVisibility(View.VISIBLE);
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void fetchMore_flames() {
        progressBar.setVisibility(View.VISIBLE);

        commentRef.limit(10).startAfter(lastVisible).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                ArrayList<CommentModel> commentModels = new ArrayList<>();
                for(DocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                    CommentModel commentModel = document.toObject(CommentModel.class);
                    Objects.requireNonNull(commentModel).setDocID(document.getId());
                    commentModels.add(commentModel);
                }
                if(commentModels.size() > 0) {
                    int lastSize = models.size();
                    models.addAll(commentModels);
                    commentAdapter.notifyItemRangeInserted(lastSize, commentModels.size());
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }
            }
            progressBar.setVisibility(View.GONE);
            if(models.size() < 10) {
                checkGetMore = -1;
            }
            else {
                checkGetMore = 0;
            }
        });
    }
}