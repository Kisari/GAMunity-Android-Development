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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.comment.CreateCommentForm;
import rmitcom.asm1.gamunity.components.views.comment.EditCommentView;
import rmitcom.asm1.gamunity.model.Comment;
import rmitcom.asm1.gamunity.model.Constant;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private String postId, commentId, userIds, ownerId;
    private DocumentReference userData, postData, commentData;
    private TextView description, username, timestamp, like, dislike, comment, commentReply, moreOptionButton;
    private ListView replyCommentListView;
    private ArrayList<Comment> commentList, replyCommentList;
    private boolean isReply;
    private RelativeLayout imageLayout;
    private ProgressBar userProgressBar, postProgressBar;
    private ImageView postImage;
    private ShapeableImageView userImage;
    private CommentDeleteListener commentDeleteListener;
    private Constant constant = new Constant();

    public interface CommentDeleteListener {
        void onCommentDeleted();
    }

    public CommentListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public CommentListAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public CommentListAdapter(@NonNull Context context, int resource, @NonNull Comment[] objects) {
        super(context, resource, objects);
    }

    public CommentListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull Comment[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
    }

    public CommentListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Comment> objects) {
        super(context, resource, textViewResourceId, objects);
    }
    public void setCommentDeleteListener(CommentDeleteListener listener) {
        this.commentDeleteListener = listener;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == 0) {
                listItem = inflater.inflate(R.layout.ui_comment_list_view_item, parent, false);
            } else {
                Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
            }
        }

        Comment currComment = getItem(position);

        description = listItem.findViewById(R.id.commentTabDescription);
        username = listItem.findViewById(R.id.commentTabUsername);
        timestamp = listItem.findViewById(R.id.commentTabTimestamp);
        like = listItem.findViewById(R.id.commentTabLike);
        dislike = listItem.findViewById(R.id.commentTabDislike);
        comment = listItem.findViewById(R.id.commentTabComment);

        commentReply = listItem.findViewById(R.id.commentTabReply);
        moreOptionButton = listItem.findViewById(R.id.commentTabMoreOption);

        replyCommentListView = listItem.findViewById(R.id.commentTabList);

        imageLayout = listItem.findViewById(R.id.commentTabPicture);
        userProgressBar = listItem.findViewById(R.id.commentTabProgressBar1);
        postProgressBar = listItem.findViewById(R.id.commentTabProgressBar2);
        postImage =listItem.findViewById(R.id.commentTabImage);
        userImage = listItem.findViewById(R.id.commentTabUserProfile);

        if (currComment != null) {
            String format = "dd/MM/yyyy HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

            description.setText(currComment.getDescription());
            like.setText((int) currComment.getNoLike() + "");
            dislike.setText((int) currComment.getNoDislike() + "");
            comment.setText((int) currComment.getNoComment() + "");

            Date timestampDate = currComment.getTimestamp(), updateTimestampDate = currComment.getUpdateTimestamp();
            StringBuilder timestampStr = new StringBuilder();

            if (timestampDate != null) {
                timestampStr.append(sdf.format(timestampDate));
            }

            if (updateTimestampDate != null) {
                timestampStr.append(" (Edited: ").append(sdf.format(updateTimestampDate)).append(")");
            }

            timestamp.setText(timestampStr);

            String postImgUri = currComment.getImgUri();
            Log.i("Post Tab", "getView - : " + postImgUri);
            if (postImgUri != null) {
                try {
                    imageLayout.setVisibility(View.VISIBLE);
                    new AsyncImage(postImage, postProgressBar).loadImage(postImgUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                imageLayout.setVisibility(View.GONE);
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
                            username.setText((String) document.get("name"));

                            userImgUri = document.getString("image");
                            if (userImgUri != null) {
                                try {
                                    new AsyncImage(userImage, userProgressBar).loadImage(userImgUri);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }

            moreOption(currComment);
        }

        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TAG", "onClick: " + currComment.getCommentId());
            }
        });

        return listItem;
    }

//    private void replyComment() {
//        commentReply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), CreateCommentForm.class);
//                if (currComment != null) {
//                    intent.putExtra("commentId", currComment.getCommentId());
//                    intent.putExtra("postId", currComment.getPostId());
//                    intent.putExtra("isReply", true);
//                }
//                ((Activity) getContext()).startActivityForResult(intent, constant.CREATE);
//            }
//        });
//    }

    private void moreOption(Comment currComment) {
        Log.i("TAG", "moreOption: " + currComment.getCommentId());

        PopupMenu popupMenu = new PopupMenu(getContext(), moreOptionButton);
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

    private void updateComment(Comment currComment) {
        Intent updateIntent = new Intent(getContext(), EditCommentView.class);
        if (currComment != null) {
            updateIntent.putExtra("commentId", currComment.getCommentId());
            updateIntent.putExtra("postId", currComment.getPostId());
        }
        ((Activity) getContext()).startActivityForResult(updateIntent, constant.EDIT);

    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void deleteCommentAlert(Comment currComment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

                if (commentDeleteListener != null) {
                    commentDeleteListener.onCommentDeleted();
                }
                dialog.dismiss();
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

//    private void setReplyCommentList() {
//        CommentListAdapter adapter = new CommentListAdapter(getContext(), 0, replyCommentList);
//    }
}
