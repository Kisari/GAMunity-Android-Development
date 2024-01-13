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
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.comment.EditCommentView;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.model.Comment;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Post;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentRecyclerViewHolder>{
    private final Context context;
    private ArrayList<Comment> commentContent;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference userData, postData, commentData;
    private String postId, commentId, userIds, ownerId;
    private Constant constant = new Constant();
    @NonNull
    @Override
    public CommentRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ui_comment_list_view_item, parent, false);
        return new CommentRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentRecyclerViewHolder holder, int position) {
        Comment currComment = commentContent.get(position);

        String format = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

        holder.description.setText(currComment.getDescription());
        holder.like.setText((int) currComment.getNoLike() + "");
        holder.dislike.setText((int) currComment.getNoDislike() + "");
        holder.comment.setText((int) currComment.getNoComment() + "");

        Date timestampDate = currComment.getTimestamp(), updateTimestampDate = currComment.getUpdateTimestamp();
        StringBuilder timestampStr = new StringBuilder();

        if (timestampDate != null) {
            timestampStr.append(sdf.format(timestampDate));
        }

        if (updateTimestampDate != null) {
            timestampStr.append(" (Edited: ").append(sdf.format(updateTimestampDate)).append(")");
        }

        holder.timestamp.setText(timestampStr);

        String postImgUri = currComment.getImgUri();
        Log.i("Post Tab", "getView - : " + postImgUri);
        if (postImgUri != null) {
            try {
                holder.imageLayout.setVisibility(View.VISIBLE);
                new AsyncImage(holder.postImage, holder.postProgressBar).loadImage(postImgUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.imageLayout.setVisibility(View.GONE);
        }

        commentId = currComment.getCommentId();
        commentData = db.collection("COMMENTS").document(commentId);

        postId = currComment.getPostId();
        postData = db.collection("POSTS").document(postId);

        ownerId = currComment.getOwnerId();
        Log.i("Comment Tab", "setCommentData - ownerId: " + ownerId);
        userData = db.collection("users").document(ownerId);

        if (ownerId != null) {
            userData.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    String userImgUri;
                    if (document != null) {
                        holder.username.setText((String) document.get("name"));

                        userImgUri = document.getString("image");
                        if (userImgUri != null) {
                            try {
                                new AsyncImage(holder.userImage, holder.userProgressBar).loadImage(userImgUri);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    public class CommentRecyclerViewHolder extends RecyclerView.ViewHolder{

        public TextView description, username, timestamp, like, dislike, comment, commentReply, moreOptionButton;
        public RecyclerView replyCommentListView;
        public ArrayList<Comment> commentList, replyCommentList;
        public RelativeLayout imageLayout;
        public ProgressBar userProgressBar, postProgressBar;
        public ImageView postImage;
        public ShapeableImageView userImage;

        public CommentRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.commentTabDescription);
            username = itemView.findViewById(R.id.commentTabUsername);
            timestamp = itemView.findViewById(R.id.commentTabTimestamp);
            like = itemView.findViewById(R.id.commentTabLike);
            dislike = itemView.findViewById(R.id.commentTabDislike);
            comment = itemView.findViewById(R.id.commentTabComment);

            commentReply = itemView.findViewById(R.id.commentTabReply);
            moreOptionButton = itemView.findViewById(R.id.commentTabMoreOption);

            replyCommentListView = itemView.findViewById(R.id.commentTabList);

            imageLayout = itemView.findViewById(R.id.commentTabPicture);
            userProgressBar = itemView.findViewById(R.id.commentTabProgressBar1);
            postProgressBar = itemView.findViewById(R.id.commentTabProgressBar2);
            postImage =itemView.findViewById(R.id.commentTabImage);
            userImage = itemView.findViewById(R.id.commentTabUserProfile);

            moreOptionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Comment clickedComment = commentContent.get(position);
                        moreOption(v, clickedComment);
                    }
                }
            });
        }

        private void moreOption(View view, Comment currComment) {
            Log.i("TAG", "moreOption: " + currComment.getCommentId());

            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.comment_more_option, popupMenu.getMenu());

            MenuItem editPost = popupMenu.getMenu().findItem(R.id.commentUpdate);
            MenuItem deletePost = popupMenu.getMenu().findItem(R.id.commentDelete);
            MenuItem bannedUser = popupMenu.getMenu().findItem(R.id.commentBanUser);
            MenuItem reportPost = popupMenu.getMenu().findItem(R.id.commentReport);

//        if (!isAdmin && !Objects.equals(userId, ownerId) && !memberIds.contains(userId)) {
//            moreOptionButton.setVisibility(View.GONE);
//        } else {}

            moreOptionButton.setOnClickListener(v -> {
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.commentUpdate) {
                        updateComment(currComment);
                    } else if (itemId == R.id.commentDelete) {
                        deleteCommentAlert(currComment);
                    } else if (itemId == R.id.commentBanUser) {

                    } else if (itemId == R.id.commentReport) {

                    }

                    return false;
                });

                popupMenu.show();
            });
        }
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
    private void deleteCommentAlert(Comment currComment) {
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
                String commentId = currComment.getCommentId();
                deleteComment(commentId);
                dialog.dismiss();
                notifyDataSetChanged();
            });

        } catch (Exception e) {
            Log.e("Comment", "getView: ", e);
            e.printStackTrace();
        }

    }

    public void deleteComment(String commentId) {
        Log.i("Comment Tab", "deleteComment - commentId: " + commentId);

        DocumentReference ownerData = db.collection("users").document(ownerId);
        ownerData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Map<String, ArrayList<String>> userCommentIds = new HashMap<>();

                if (document.exists()) {
                    ArrayList<String> commentIds = (ArrayList<String>) document.get("commentIds");

                    if (commentIds != null) {
                        commentIds.remove(commentId);
                        userCommentIds.put("commentIds", commentIds);
                    }
                }
                ownerData.set(userCommentIds, SetOptions.merge());
            }
        });

        postData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Map<String, ArrayList<String>> postCommentIds = new HashMap<>();

                if (document.exists()) {
                    ArrayList<String> commentIds = (ArrayList<String>) document.get("commentIds");

                    if (commentIds != null) {
                        commentIds.remove(commentId);
                        postCommentIds.put("commentIds", commentIds);
                    }
                }
                postData.set(postCommentIds, SetOptions.merge());
            }
        });

//        if (isReply) {
//            dcommentData.get().addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    Map<String, ArrayList<String>> replyIds = new HashMap<>();
//
//                    if (document.exists()) {
//                        ArrayList<String> replyCommentIds = (ArrayList<String>) document.get("replyCommentIds");
//
//                        if (replyCommentIds != null) {
//                            replyCommentIds.remove(commentId);
//                            replyIds.put("replyCommentIds", replyCommentIds);
//                        }
//                    }
//                    db.collection("COMMENTS").document(commentId)
//                            .set(replyIds, SetOptions.merge());
//                }
//            });
//        }

        DocumentReference commentData = db.collection("COMMENTS").document(commentId);
        commentData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        String imageUrl = document.getString("imageUrl");

                        if (imageUrl != null) {
                            Log.i("Comment Tab", "imageUrl: " + imageUrl);

                            String pattern = "images%2F(.*?)\\?";
                            Pattern p = Pattern.compile(pattern);
                            Matcher m = p.matcher(imageUrl);

                            if (m.find()) {
                                String oldUri = m.group(1);
                                Log.i("Comment Tab", "oldUri: " + oldUri);

                                // Create a reference to the old image and delete it
                                StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                    Log.i("Comment Tab", "Old image deleted successfully");
                                }).addOnFailureListener(e -> {
                                    Log.e("Comment Tab", "Failed to delete old image: " + e.getMessage());
                                });
                            } else {
                                Log.e("Comment Tab", "Regex pattern did not match imageUrl");
                            }
                        }

                        commentData.delete();

                    }
                }
            }
        });
    }

    public void deleteCommentFromForum(String commentId) {
        if (commentId != null) {
            DocumentReference commentData = db.collection("COMMENTS").document(commentId);
            commentData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        String ownerId, postId;
                        if (document.exists()) {
                            postId = document.getString("postId");
                            ownerId = document.getString("ownerId");

                            DocumentReference ownerData = db.collection("users").document(ownerId);
                            ownerData.get().addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    DocumentSnapshot userDocument = userTask.getResult();
                                    Map<String, ArrayList<String>> userCommentIds = new HashMap<>();

                                    if (userDocument.exists()) {
                                        ArrayList<String> commentIds = (ArrayList<String>) userDocument.get("commentIds");

                                        if (commentIds != null) {
                                            commentIds.remove(commentId);
                                            userCommentIds.put("commentIds", commentIds);
                                        }
                                    }
                                    ownerData.set(userCommentIds, SetOptions.merge());
                                }
                            });

                            DocumentReference postData = db.collection("POSTS").document(postId);
                            postData.get().addOnCompleteListener(postTask -> {
                                if (postTask.isSuccessful()) {
                                    DocumentSnapshot postDocument = postTask.getResult();
                                    Map<String, ArrayList<String>> postCommentIds = new HashMap<>();

                                    if (postDocument.exists()) {
                                        ArrayList<String> commentIds = (ArrayList<String>) postDocument.get("commentIds");

                                        if (commentIds != null) {
                                            commentIds.remove(commentId);
                                            postCommentIds.put("commentIds", commentIds);
                                        }
                                    }
                                    postData.set(postCommentIds, SetOptions.merge());
                                }
                            });
                        }

                        commentData.delete();

                    }
                }
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
}
