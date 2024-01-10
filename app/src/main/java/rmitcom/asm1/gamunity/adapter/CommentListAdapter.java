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
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.views.comment.CreateCommentForm;
import rmitcom.asm1.gamunity.components.views.comment.EditCommentView;
import rmitcom.asm1.gamunity.model.Comment;
import rmitcom.asm1.gamunity.model.Constant;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private Comment currComment;
    private String postId, commentId, userIds, ownerId;
    private DocumentReference userData, postData;
    private TextView description, username, timestamp, like, dislike, comment, commentReply, moreOptionButton;
    private ListView replyCommentListView;
    private ArrayList<Comment> replyCommentList;
    private boolean isReply;
    private Constant constant = new Constant();

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

        currComment = getItem(position);

        description = listItem.findViewById(R.id.commentTabDescription);
        username = listItem.findViewById(R.id.commentTabUsername);
        timestamp = listItem.findViewById(R.id.commentTabTimestamp);
        like = listItem.findViewById(R.id.commentTabLike);
        dislike = listItem.findViewById(R.id.commentTabDislike);
        comment = listItem.findViewById(R.id.commentTabComment);

        commentReply = listItem.findViewById(R.id.commentTabReply);
        moreOptionButton = listItem.findViewById(R.id.commentTabMoreOption);

        replyCommentListView = listItem.findViewById(R.id.commentTabList);

        setCommentData();
        replyComment();
        moreOption();

        return listItem;
    }

    private void setCommentData() {
        if (currComment != null) {
            String format = "dd/MM/yyyy HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

            description.setText(currComment.getDescription());
            like.setText((int) currComment.getNoLike() + "");
            dislike.setText((int) currComment.getNoDislike() + "");
            comment.setText((int) currComment.getNoComment() + "");

            if (currComment.getTimestamp() != null) {
                timestamp.setText(sdf.format(currComment.getTimestamp()));
            } else {
                timestamp.setText("N/A");  // Or set it to some default value
            }


            postId = currComment.getPostId();
            postData = db.collection("POSTS").document(postId);

            ownerId = currComment.getOwnerId();
            userData = db.collection("USERS").document(ownerId);

            userData.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document != null) {
                        username.setText((String) document.get("username"));
                    }
                }
            });
        }
    }

    private void replyComment() {
        commentReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateCommentForm.class);
                if (currComment != null) {
                    intent.putExtra("commentId", currComment.getCommentId());
                    intent.putExtra("postId", currComment.getPostId());
                    intent.putExtra("isReply", true);
                }
                ((Activity) getContext()).startActivityForResult(intent, constant.CREATE);
            }
        });
    }

    public void onReplyCommentResult() {

    }

    private void moreOption() {
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
                    updateComment();
                } else if (itemId == R.id.commentDelete) {
                    deleteCommentAlert();
                } else if (itemId == R.id.commentBanUser) {

                } else if (itemId == R.id.commentReport) {

                }

                return false;
            });

            popupMenu.show();
        });
    }

    private void updateComment() {
        Intent updateIntent = new Intent(getContext(), EditCommentView.class);
        if (currComment != null) {
            updateIntent.putExtra("commentId", currComment.getCommentId());
            updateIntent.putExtra("postId", currComment.getPostId());
        }
        ((Activity) getContext()).startActivityForResult(updateIntent, constant.EDIT);

    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void deleteCommentAlert() {
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
                deleteComment(commentId);
                dialog.dismiss();
            });

        } catch (Exception e) {
            Log.e("Comment", "getView: ", e);
            e.printStackTrace();
        }

    }

    public void deleteComment(String commentId) {
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

        DocumentReference postData = db.collection("POSTS").document(postId);
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

        if (isReply) {
            db.collection("COMMENTS").document(commentId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, ArrayList<String>> replyIds = new HashMap<>();

                    if (document.exists()) {
                        ArrayList<String> replyCommentIds = (ArrayList<String>) document.get("replyCommentIds");

                        if (replyCommentIds != null) {
                            replyCommentIds.remove(commentId);
                            replyIds.put("replyCommentIds", replyCommentIds);
                        }
                    }
                    db.collection("COMMENTS").document(commentId)
                            .set(replyIds, SetOptions.merge());
                }
            });
        }

        db.collection("COMMENTS").document(commentId).delete();
    }

    private void setReplyCommentList() {
        CommentListAdapter adapter = new CommentListAdapter(getContext(), 0, replyCommentList);
    }
}
