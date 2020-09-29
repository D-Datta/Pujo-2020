package com.applex.utsav.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applex.utsav.ActivityProfileCommittee;
import com.applex.utsav.R;
import com.applex.utsav.ReelsActivity;
import com.applex.utsav.models.ReelsPostModel;
import com.applex.utsav.utility.BasicUtility;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.Objects;
import static java.lang.Boolean.TRUE;

public class Fragment_Reels extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerview;
    private ProgressBar contentprogressreels,progressmorereels;
    private LinearLayout noneImage;
    private FirestorePagingAdapter adapter;
    private BottomSheetDialog postMenuDialog;
    private ProgressDialog progressDialog;
    private String uid;

    public Fragment_Reels(String uid) {
        this.uid = uid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__reels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        recyclerview = view.findViewById(R.id.recycler_reels);
        contentprogressreels = view.findViewById(R.id.content_progress_reels);
        progressmorereels = view.findViewById(R.id.progress_more_reels);
        noneImage = view.findViewById(R.id.none_image);

        recyclerview.setHasFixedSize(false);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setItemViewCacheSize(10);

        buildRecyclerView();

        swipeRefreshLayout
                .setColorSchemeColors(getResources().getColor(R.color.colorPrimary),getResources()
                        .getColor(R.color.purple));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            buildRecyclerView();
        });
    }

    private void buildRecyclerView() {

        Query query = FirebaseFirestore.getInstance()
                .collection("Reels")
                .whereEqualTo("uid", uid)
                .orderBy("ts", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(5)
                .setPageSize(5)
                .build();

        FirestorePagingOptions<ReelsPostModel> options = new FirestorePagingOptions.Builder<ReelsPostModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, snapshot -> {
                    ReelsPostModel reelsPostModel = snapshot.toObject(ReelsPostModel.class);
                    Objects.requireNonNull(reelsPostModel).setDocID(snapshot.getId());
                    return reelsPostModel;
                })
                .build();

        adapter = new FirestorePagingAdapter<ReelsPostModel, ProgrammingViewHolder>(options) {
            @NonNull
            @Override
            public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View v = layoutInflater.inflate(R.layout.item_reels_committee, parent, false);
                return new ProgrammingViewHolder(v);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position, @NonNull ReelsPostModel currentItem) {
                holder.video_time.setText(currentItem.getDuration());
                holder.item_reels_image.setVisibility(View.VISIBLE);
                if(currentItem.getVideoViews() > 1) {
                    if(currentItem.getVideoViews() > 1000) {
                        holder.video_views.setText(currentItem.getVideoViews()/1000 + "." + (currentItem.getVideoViews() % 1000)/100 + "K views");
                    } else {
                        holder.video_views.setText(currentItem.getVideoViews() + " views");
                    }
                } else {
                    holder.video_views.setText(currentItem.getVideoViews() + " view");
                }

                if(currentItem.getHeadline() != null) {
                    if(currentItem.getHeadline().length() > 15) {
                        holder.headline.setText(currentItem.getHeadline().substring(0, 15) + "...");
                    } else {
                        holder.headline.setText(currentItem.getHeadline());
                    }
                }

                if (currentItem.getFrame() != null && !currentItem.getFrame().isEmpty()) {
                    Picasso.get().load(currentItem.getFrame())
                            .placeholder(R.drawable.image_background_grey)
                            .into(holder.item_reels_image, new Callback() {
                                @Override
                                public void onSuccess() { }

                                @Override
                                public void onError(Exception e) {
                                    holder.item_reels_image.setImageResource(R.drawable.image_background_grey);
                                }
                            });
                } else {
                    holder.item_reels_image.setImageResource(R.drawable.image_background_grey);
                }

                holder.item_reels_image.setOnClickListener(v -> {
                    Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                    intent.putExtra("docID", currentItem.getDocID());
                    intent.putExtra("uid", uid);
                    intent.putExtra("bool", "2");
                    requireActivity().startActivity(intent);
                });

                holder.reels_more.setOnClickListener(v -> {
                    if (currentItem.getUid().matches(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))) {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu_3);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);
                        postMenuDialog.findViewById(R.id.edit_post).setVisibility(View.GONE);

                        postMenuDialog.findViewById(R.id.delete_post).setOnClickListener(v2 -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Are you sure?")
                                    .setMessage("Reel will be deleted permanently")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        progressDialog = new ProgressDialog(requireActivity());
                                        progressDialog.setTitle("Deleting Reel");
                                        progressDialog.setMessage("Please wait...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        FirebaseFirestore.getInstance()
                                                .collection("Reels").document(currentItem.getDocID()).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    ActivityProfileCommittee.delete = 1;
                                                    holder.itemView.setVisibility(View.GONE);
                                                    progressDialog.dismiss();
                                                });
                                        postMenuDialog.dismiss();
                                    })
                                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(true)
                                    .show();
                        });

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v12 -> {
//                            String link = "https://www.utsavapp.in/android/reels/" + currentItem.getDocID();
//                            Intent i = new Intent();
//                            i.setAction(Intent.ACTION_SEND);
//                            i.putExtra(Intent.EXTRA_TEXT, link);
//                            i.setType("text/plain");
//                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v1 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Reels").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Reel has been reported."));
                            postMenuDialog.dismiss();
                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();

                    } else {
                        postMenuDialog = new BottomSheetDialog(requireActivity());
                        postMenuDialog.setContentView(R.layout.dialog_post_menu);
                        postMenuDialog.setCanceledOnTouchOutside(TRUE);

                        postMenuDialog.findViewById(R.id.share_post).setOnClickListener(v13 -> {
//                            String link = "https://www.utsavapp.in/android/reels/" + currentItem.getDocID();
//                            Intent i = new Intent();
//                            i.setAction(Intent.ACTION_SEND);
//                            i.putExtra(Intent.EXTRA_TEXT, link);
//                            i.setType("text/plain");
//                            startActivity(Intent.createChooser(i, "Share with"));
                            postMenuDialog.dismiss();
                        });

                        postMenuDialog.findViewById(R.id.report_post).setOnClickListener(v14 -> {
                            FirebaseFirestore.getInstance()
                                    .collection("Reels").document(currentItem.getDocID())
                                    .update("reportL", FieldValue.arrayUnion(FirebaseAuth.getInstance().getUid()))
                                    .addOnSuccessListener(aVoid -> BasicUtility.showToast(getActivity(), "Reel has been reported."));
                            postMenuDialog.dismiss();
                        });

                        Objects.requireNonNull(postMenuDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        postMenuDialog.show();
                    }
                });
            }

            @Override
            public int getItemViewType(int position) { return position; }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {

                super.onLoadingStateChanged(state);
                switch (state) {
                    case ERROR: BasicUtility.showToast(getContext(), "Something went wrong..."); break;
                    case LOADING_MORE: progressmorereels.setVisibility(View.VISIBLE); break;
                    case LOADED: progressmorereels.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case FINISHED: contentprogressreels.setVisibility(View.GONE);
                        progressmorereels.setVisibility(View.GONE);
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if(adapter!=null && adapter.getItemCount() == 0)
                            noneImage.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };

        contentprogressreels.setVisibility(View.GONE);
        noneImage.setVisibility(View.GONE);
        recyclerview.setAdapter(adapter);
    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout item_reels;
        TextView video_time, video_views, headline;
        ImageView reels_more, item_reels_image;

        ProgrammingViewHolder(View itemView) {
            super(itemView);

            item_reels = itemView.findViewById(R.id.item_reels);
            video_time = itemView.findViewById(R.id.video_time);
            headline = itemView.findViewById(R.id.pujo_headline);
            reels_more =  itemView.findViewById(R.id.reels_more);
            item_reels_image = itemView.findViewById(R.id.item_reels_image);
            video_views = itemView.findViewById(R.id.pujo_video_views);
        }
    }
}