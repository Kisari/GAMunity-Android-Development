package rmitcom.asm1.gamunity.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.comment.EditCommentView;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.components.views.profile.ProfileView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Comment;
import rmitcom.asm1.gamunity.model.Constant;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentRecyclerViewHolder>{
    private final Context context;
    private ArrayList<Comment> commentContent;
    private final FireBaseManager manager = new FireBaseManager();
    private DocumentReference userData, postData, commentData;
    private String postId, commentId, userIds, ownerId, chiefAdminId, forumId;
    private ArrayList<String> commentIds, memberIds, moderatorIds;
    private final Constant constant = new Constant();
    @NonNull
    @Override
    public CommentRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ui_comment_list_view_item, parent, false);
        return new CommentRecyclerViewHolder(view);
    }

    public class CommentRecyclerViewHolder extends RecyclerView.ViewHolder{

        public TextView description, username, timestamp, like, dislike, likeTrue, dislikeTrue, comment, commentReply, moreOptionButton;
        public RecyclerView replyCommentListView;
        public ArrayList<Comment> commentList, replyCommentList;
        public RelativeLayout imageLayout, commentTabProfile;
        public ProgressBar userProgressBar, commentProgressBar;
        public ImageView commentImage, baseImage;
        public ShapeableImageView userImage;

        public CommentRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.commentTabDescription);
            username = itemView.findViewById(R.id.commentTabUsername);
            timestamp = itemView.findViewById(R.id.commentTabTimestamp);

            like = itemView.findViewById(R.id.commentTabLike);
            likeTrue = itemView.findViewById(R.id.commentTabLikeTrue);
            dislike = itemView.findViewById(R.id.commentTabDislike);
            dislikeTrue = itemView.findViewById(R.id.commentTabDislikeTrue);
            comment = itemView.findViewById(R.id.commentTabComment);

            commentReply = itemView.findViewById(R.id.commentTabReply);
            moreOptionButton = itemView.findViewById(R.id.commentTabMoreOption);

            replyCommentListView = itemView.findViewById(R.id.commentTabList);

            imageLayout = itemView.findViewById(R.id.commentTabPicture);
            userProgressBar = itemView.findViewById(R.id.commentTabProgressBar1);
            commentProgressBar = itemView.findViewById(R.id.commentTabProgressBar2);
            commentImage =itemView.findViewById(R.id.commentTabImage);
            userImage = itemView.findViewById(R.id.commentTabUserProfile);

            baseImage = itemView.findViewById(R.id.baseImg);
            commentTabProfile = itemView.findViewById(R.id.commentTabProfile);

        }

        private void setButton(Comment currComment, String chiefAdminId, ArrayList<String> moderatorIds, ArrayList<String> memberIds) {
            String ownerId = currComment.getOwnerId();

            if (Objects.equals(manager.getCurrentUser().getUid(), chiefAdminId) || (moderatorIds != null && moderatorIds.contains(manager.getCurrentUser().getUid()))) {
                moreOptionButton.setVisibility(View.VISIBLE);
//                commentReply.setVisibility(View.VISIBLE);

            } else if (memberIds != null && memberIds.contains(manager.getCurrentUser().getUid())) {
//                commentReply.setVisibility(View.VISIBLE);

                if (Objects.equals(manager.getCurrentUser().getUid(), ownerId)) {
                    moreOptionButton.setVisibility(View.VISIBLE);
                }
                else {
                    moreOptionButton.setVisibility(View.GONE);
                }

            } else {
                moreOptionButton.setVisibility(View.GONE);
//                commentReply.setVisibility(View.VISIBLE);
            }
        }

        private void moreOption(View view, Comment currComment,
                                String chiefAdminId, ArrayList<String> moderatorIds, ArrayList<String> memberIds, int position) {

            PopupMenu popupMenu = new PopupMenu(context, moreOptionButton);
            popupMenu.getMenuInflater().inflate(R.menu.comment_more_option, popupMenu.getMenu());

            MenuItem editComment = popupMenu.getMenu().findItem(R.id.commentUpdate);
            MenuItem deleteComment = popupMenu.getMenu().findItem(R.id.commentDelete);
            MenuItem bannedUser = popupMenu.getMenu().findItem(R.id.commentBanUser);
            MenuItem reportUser = popupMenu.getMenu().findItem(R.id.commentReport);

            String ownerId = currComment.getOwnerId();

            if (Objects.equals(manager.getCurrentUser().getUid(), chiefAdminId) || (moderatorIds != null && moderatorIds.contains(manager.getCurrentUser().getUid()))) {
                deleteComment.setVisible(true);

                editComment.setVisible(Objects.equals(manager.getCurrentUser().getUid(), ownerId));

            } else if (memberIds != null && memberIds.contains(manager.getCurrentUser().getUid())) {
                if (Objects.equals(manager.getCurrentUser().getUid(), ownerId)) {
                    editComment.setVisible(true);
                    deleteComment.setVisible(true);
                }
                else {
                    editComment.setVisible(false);
                    deleteComment.setVisible(false);
                }
            } else {
                editComment.setVisible(false);
                deleteComment.setVisible(false);
            }

            moreOptionButton.setOnClickListener(v -> {
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.commentUpdate) {
                        updateComment(currComment);
                    } else if (itemId == R.id.commentDelete) {
                        deleteCommentAlert(currComment, position);
                    } else if (itemId == R.id.commentBanUser) {

                    } else if (itemId == R.id.commentReport) {

                    }

                    return false;
                });

                popupMenu.show();
            });
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CommentRecyclerViewHolder holder, int position) {
        Comment currComment = commentContent.get(position);

        String format = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

        holder.description.setText(currComment.getDescription());

        if (currComment.getLikeIds() != null) {
            if (currComment.getLikeIds().contains(manager.getCurrentUser().getUid())) {
                holder.like.setVisibility(View.INVISIBLE);
                holder.likeTrue.setVisibility(View.VISIBLE);
                holder.likeTrue.setText(String.valueOf(currComment.getLikeIds().size()));
            }
            else {
                holder.like.setVisibility(View.VISIBLE);
                holder.likeTrue.setVisibility(View.INVISIBLE);
                holder.like.setText(String.valueOf(currComment.getLikeIds().size()));
            }

        }
        else {
            holder.like.setVisibility(View.VISIBLE);
            holder.likeTrue.setVisibility(View.INVISIBLE);
            holder.like.setText("0");
        }

        if (currComment.getDislikeIds() != null) {
            if (currComment.getDislikeIds().contains(manager.getCurrentUser().getUid())) {
                holder.dislike.setVisibility(View.INVISIBLE);
                holder.dislikeTrue.setVisibility(View.VISIBLE);
                holder.dislikeTrue.setText(String.valueOf(currComment.getDislikeIds().size()));
            }
            else {
                holder.dislike.setVisibility(View.VISIBLE);
                holder.dislikeTrue.setVisibility(View.INVISIBLE);
                holder.dislike.setText(String.valueOf(currComment.getDislikeIds().size()));
            }
        }
        else {
            holder.dislike.setVisibility(View.VISIBLE);
            holder.dislikeTrue.setVisibility(View.INVISIBLE);
            holder.dislike.setText("0");
        }

        if (currComment.getReplyCommentIds() != null) {
            holder.comment.setText(String.valueOf(currComment.getReplyCommentIds().size()));
        }
        else {
            holder.comment.setText("0");
        }

        Date timestampDate = currComment.getTimestamp(), updateTimestampDate = currComment.getUpdateTimestamp();
        StringBuilder timestampStr = new StringBuilder();

        if (timestampDate != null) {
            timestampStr.append(sdf.format(timestampDate));
        }

        if (updateTimestampDate != null) {
            timestampStr.append(" (Edited: ").append(sdf.format(updateTimestampDate)).append(")");
        }

        holder.timestamp.setText(timestampStr);

        String commentImgUri = currComment.getImgUri();
        Log.i("Post Tab", "getView - : " + commentImgUri);
        if (commentImgUri != null) {
            try {
                holder.imageLayout.setVisibility(View.VISIBLE);
                new AsyncImage(holder.commentImage, holder.commentProgressBar).loadImage(commentImgUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.imageLayout.setVisibility(View.GONE);
        }

        commentId = currComment.getCommentId();
        commentData = manager.getDb().collection("COMMENTS").document(commentId);

        postId = currComment.getPostId();
        postData = manager.getDb().collection("POSTS").document(postId);

        postData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    forumId = document.getString("forumId");
                    Log.i("TAG comment", "forumId: " + forumId);

                    if (forumId != null) {
                        manager.getDb().collection("FORUMS").document(forumId).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document1 = task1.getResult();

                                if (document1.exists()) {
                                    chiefAdminId = document1.getString("chiefAdmin");

                                    if (document1.get("memberIds") != null) {
                                        memberIds = (ArrayList<String>) document1.get("memberIds");
                                    } else {
                                        memberIds = new ArrayList<>();
                                    }

                                    if (document1.get("moderatorIds") != null) {
                                        moderatorIds = (ArrayList<String>) document1.get("moderatorIds");
                                    } else {
                                        moderatorIds = new ArrayList<>();
                                    }

                                    holder.setButton(currComment, chiefAdminId, moderatorIds, memberIds);
                                    holder.moreOption(holder.itemView, currComment, chiefAdminId, moderatorIds, memberIds, holder.getAdapterPosition());
                                }
                            }
                        });

                    }
                }
            }
        });

        ownerId = currComment.getOwnerId();
        userData = manager.getDb().collection("users").document(ownerId);

        if (ownerId != null) {
            userData.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    String userImgUri;
                    if (document != null) {
                        holder.username.setText((String) document.get("name"));

                        userImgUri = document.getString("profileImgUri");
                        if (userImgUri != null) {
                            try {
                                holder.baseImage.setVisibility(View.INVISIBLE);
                                holder.userProgressBar.setVisibility(View.VISIBLE);
                                holder.userImage.setVisibility(View.VISIBLE);
                                new AsyncImage(holder.userImage, holder.userProgressBar).loadImage(userImgUri);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            holder.baseImage.setVisibility(View.VISIBLE);
                            holder.userProgressBar.setVisibility(View.INVISIBLE);
                            holder.userImage.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }

        setLikeComment(holder, currComment, position);
        setDislikeComment(holder, currComment, position);

        holder.commentTabProfile.setOnClickListener(v -> {
            Intent accessIntent = new Intent(context, ProfileView.class);
            accessIntent.putExtra("userId", currComment.getOwnerId());
            context.startActivity(accessIntent);
        });
    }

    private void toggleLikeDislike(int position, Comment currComment,
                                   ArrayList<String> currentList, ArrayList<String> otherList,
                                   String currField, String otherField, final TextView likeView, final TextView dislikeView,
                                   final TextView likeViewTrue, final TextView dislikeViewTrue) {

        if (currentList != null) {
            ArrayList<String> finalCurrList = currentList;

            if (!currentList.contains(manager.getCurrentUser().getUid())) {
                currentList.add(manager.getCurrentUser().getUid());
                commentData.update(currField, FieldValue.arrayUnion(manager.getCurrentUser().getUid())).addOnSuccessListener(unused -> {
                    likeViewTrue.setVisibility(View.VISIBLE);
                    likeView.setVisibility(View.INVISIBLE);

                    likeViewTrue.setText(String.valueOf(finalCurrList.size()));

                    if (otherList != null && otherList.contains(manager.getCurrentUser().getUid())) {
                        otherList.remove(manager.getCurrentUser().getUid());
                        commentData.update(otherField, FieldValue.arrayRemove(manager.getCurrentUser().getUid())).addOnSuccessListener(unused1 -> {
                            dislikeView.setVisibility(View.VISIBLE);
                            dislikeViewTrue.setVisibility(View.INVISIBLE);

                            dislikeView.setText(String.valueOf(otherList.size()));
                        });
                    } else {
                        dislikeView.setVisibility(View.VISIBLE);
                        dislikeViewTrue.setVisibility(View.INVISIBLE);

                        dislikeView.setText("0");
                    }

                    if (position != RecyclerView.NO_POSITION) {
                        notifyItemChanged(position);
                    }
                });
            } else {
                currentList.remove(manager.getCurrentUser().getUid());
                commentData.update(currField, FieldValue.arrayRemove(manager.getCurrentUser().getUid())).addOnSuccessListener(unused -> {
                    likeView.setVisibility(View.VISIBLE);
                    likeViewTrue.setVisibility(View.INVISIBLE);

                    likeView.setText(String.valueOf(finalCurrList.size()));

                    if (otherList != null) {
                        otherList.remove(manager.getCurrentUser().getUid());
                        dislikeView.setText(String.valueOf(otherList.size()));

                    } else {
                        dislikeView.setText("0");
                    }

                    if (position != RecyclerView.NO_POSITION) {
                        notifyItemChanged(position);
                    }
                });
            }
        }
        else {
            currentList = new ArrayList<>();
            currentList.add(manager.getCurrentUser().getUid());

            Map<String, ArrayList<String>> actionData = new HashMap<>();
            actionData.put(currField, currentList);

            ArrayList<String> finalCurrList = currentList;
            commentData.set(actionData, SetOptions.merge()).addOnSuccessListener(unused -> {
                likeView.setVisibility(View.INVISIBLE);
                likeViewTrue.setVisibility(View.VISIBLE);

                likeViewTrue.setText(String.valueOf(finalCurrList.size()));

                if (otherList != null && otherList.contains(manager.getCurrentUser().getUid())) {
                    otherList.remove(manager.getCurrentUser().getUid());
                    commentData.update(otherField, FieldValue.arrayRemove(manager.getCurrentUser().getUid())).addOnSuccessListener(unused12 -> {
                        dislikeView.setVisibility(View.VISIBLE);
                        dislikeViewTrue.setVisibility(View.INVISIBLE);

                        dislikeView.setText(String.valueOf(otherList.size() - 1));
                    });
                } else {
                    dislikeView.setVisibility(View.VISIBLE);
                    dislikeViewTrue.setVisibility(View.INVISIBLE);

                    dislikeView.setText("0");
                }

                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position);
                }
            });
        }
    }

    private void setLikeComment(CommentRecyclerViewHolder holder, Comment currComment, int position) {

        ArrayList<String> likeIds = currComment.getLikeIds(), dislikeIds = currComment.getDislikeIds();
        holder.like.setOnClickListener(v -> {
            toggleLikeDislike(position, currComment, likeIds, dislikeIds,
                    "likeIds", "dislikeIds",
                    holder.like, holder.dislike, holder.likeTrue, holder.dislikeTrue);

            if (!likeIds.contains(manager.getCurrentUser().getUid())) {
                likeIds.add(manager.getCurrentUser().getUid());

                if (dislikeIds != null) {
                    dislikeIds.remove(manager.getCurrentUser().getUid());
                }
            }
        });

        holder.likeTrue.setOnClickListener(v -> {
            toggleLikeDislike(position, currComment, likeIds, dislikeIds,
                    "likeIds", "dislikeIds",
                    holder.like, holder.dislike, holder.likeTrue, holder.dislikeTrue);

            likeIds.remove(manager.getCurrentUser().getUid());
        });
    }

    private void setDislikeComment(CommentRecyclerViewHolder holder, Comment currComment, int position) {
        ArrayList<String> likeIds = currComment.getLikeIds(), dislikeIds = currComment.getDislikeIds();
        holder.dislike.setOnClickListener(v -> {
            toggleLikeDislike(position, currComment, dislikeIds, likeIds,
                    "dislikeIds", "likeIds",
                    holder.dislike, holder.like, holder.dislikeTrue, holder.likeTrue);

            if (!dislikeIds.contains(manager.getCurrentUser().getUid())) {
                dislikeIds.add(manager.getCurrentUser().getUid());

                if (likeIds != null) {
                    likeIds.remove(manager.getCurrentUser().getUid());
                }
            }
        });

        holder.dislikeTrue.setOnClickListener(v -> {
            toggleLikeDislike(position, currComment, dislikeIds, likeIds,
                    "dislikeIds", "likeIds",
                    holder.dislike, holder.like, holder.dislikeTrue, holder.likeTrue);

            dislikeIds.remove(manager.getCurrentUser().getUid());
        });
    }

    private void updateComment(Comment currComment) {
        Intent updateIntent = new Intent(context, EditCommentView.class);
        if (currComment != null) {
            updateIntent.putExtra("commentId", currComment.getCommentId());
            updateIntent.putExtra("postId", currComment.getPostId());
        }
        ((Activity) context).startActivityForResult(updateIntent, constant.EDIT);

    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void deleteCommentAlert(Comment currComment, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View deleteDialogLayout = inflater.inflate(R.layout.ui_delete_dialog_view, null);

        TextView dialogMessage = deleteDialogLayout.findViewById(R.id.dialogMessage);
        Button cancelButton = deleteDialogLayout.findViewById(R.id.dialogCancel);
        Button deleteButton = deleteDialogLayout.findViewById(R.id.dialogAccept);

        builder.setView(deleteDialogLayout);
        AlertDialog dialog = builder.create();
        dialog.show();

        try {
            dialogMessage.setText("Are you sure you want to delete this comment");
            cancelButton.setOnClickListener(v -> dialog.dismiss());

            deleteButton.setOnClickListener(v -> {
                deleteComment(currComment, position);
                dialog.dismiss();

            });

        } catch (Exception e) {
            Log.e("Comment", "getView: ", e);
            e.printStackTrace();
        }

    }

    private void deleteComment(Comment currComment, int position) {

        String commentId = currComment.getCommentId();

        DocumentReference ownerData = manager.getDb().collection("users").document(ownerId);
        ownerData.update("commentIds", FieldValue.arrayRemove(commentId));

        postData.update("commentIds", FieldValue.arrayRemove(commentId));

        String imgUri = currComment.getImgUri();
        if (imgUri != null) {
            String pattern = "images%2F(.*?)\\?";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(imgUri);

            if (m.find()) {
                String oldUri = m.group(1);

                // Create a reference to the old image and delete it
                StorageReference oldImageRef = manager.getStorageRef().child("images/" + oldUri);
                oldImageRef.delete().addOnSuccessListener(aVoid -> Log.i("Delete image", "Old image deleted successfully")).addOnFailureListener(e -> Log.e("Delete image", "Failed to delete old image: " + e.getMessage()));
            }
        }

        manager.getDb().collection("COMMENTS").document(commentId).delete().addOnSuccessListener(unused -> {
            if (context instanceof PostView) {
                ((PostView) context).refreshPostData(position);
            }
        });

    }

    public void deleteCommentFromPost(String commentId) {
        if (commentId != null) {
            DocumentReference commentData = manager.getDb().collection("COMMENTS").document(commentId);
            commentData.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        String postId = document.getString("postId");
                        String ownerId = document.getString("ownerId");
                        String imgUri = document.getString("image");

                        if (ownerId != null) {
                            DocumentReference ownerData = manager.getDb().collection("users").document(ownerId);
                            ownerData.update("commentIds", FieldValue.arrayRemove(commentId));
                        }
                        if (postId != null) {
                            DocumentReference postData = manager.getDb().collection("POSTS").document(postId);
                            postData.update("commentIds", FieldValue.arrayRemove(commentId));
                        }

                        if (imgUri != null) {
                            String pattern = "images%2F(.*?)\\?";
                            Pattern p = Pattern.compile(pattern);
                            Matcher m = p.matcher(imgUri);

                            if (m.find()) {
                                String oldUri = m.group(1);

                                // Create a reference to the old image and delete it
                                StorageReference oldImageRef = manager.getStorageRef().child("images/" + oldUri);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> Log.i("Delete image", "Old image deleted successfully")).addOnFailureListener(e -> Log.e("Delete image", "Failed to delete old image: " + e.getMessage()));
                            }
                        }
                    }

                }

                commentData.delete().addOnSuccessListener(unused -> Log.i("delete comment", "onSuccess - delete from post: delete successfully"));
            });
        }
    }

    @Override
    public int getItemCount() {
        return commentContent.size();
    }

    public CommentRecyclerViewAdapter(Context context, ArrayList<Comment> commentContent) {
        this.context = context;
        this.commentContent = commentContent;
    }

    public CommentRecyclerViewAdapter(Context context, ArrayList<String> commentIds, boolean forDelete) {
        this.context = context;
        this.commentIds = commentIds;
    }
}
