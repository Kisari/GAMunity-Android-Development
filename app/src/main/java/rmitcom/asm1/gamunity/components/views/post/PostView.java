package rmitcom.asm1.gamunity.components.views.post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.CommentRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.comment.CreateCommentForm;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.model.Comment;
import rmitcom.asm1.gamunity.model.Constant;

public class PostView extends AppCompatActivity {
    private final String TAG = "Post View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData, postData;
    private String postId, forumId, ownerId, chiefAdminId,
            postUsernameStr, postTimestampStr, postUpdateTimestampStr,
            postTitleStr, postDescriptionStr, postUserImageUri, postImageUri;
    private int noLike, noDislike, noComment;
    private Date postTimestampDate, postUpdateTimestampDate;
    private ArrayList<String> memberIds, moderatorIds, postLikeIds, postDislikeIds, commentIds;
    private ArrayList<String> commentLikeIds, commentDislikeIds, replyCommentIds;
    private ArrayList<Comment> commentList;
    private TextView postUsername, postTimestamp, postTitle, postDescription, moreOptionButton,
            postLike, postDislike, postComment, postLikeTrue, postDislikeTrue, postForumName,
            addCommentButton, returnBackButton;
    private RecyclerView commentListView;
    private RelativeLayout postPicture;
    private ProgressBar userProgressBar, postProgressBar;
    private ImageView postImage, baseImage;
    private ShapeableImageView postUserImage;
    private CommentRecyclerViewAdapter adapter;
    private Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            postId = getIntent.getExtras().getString("postId");
            forumId = getIntent.getExtras().getString("forumId");

            postData = db.collection("POSTS").document(postId);
            forumData = db.collection("FORUMS").document(forumId);
            userData = db.collection("users").document(userId);
        }

        postUsername = findViewById(R.id.postUserName);
        postTimestamp = findViewById(R.id.postTimestamp);
        postTitle = findViewById(R.id.postTitle);
        postDescription = findViewById(R.id.postDescription);

        postLike = findViewById(R.id.postLike);
        postDislike = findViewById(R.id.postDislike);
        postComment = findViewById(R.id.postComment);
        postLikeTrue = findViewById(R.id.postLikeTrue);
        postDislikeTrue = findViewById(R.id.postDislikeTrue);

        postPicture = findViewById(R.id.postPicture);
        postImage  =findViewById(R.id.postImage);
        postUserImage = findViewById(R.id.postUserImage);
        userProgressBar = findViewById(R.id.postProgressBar1);
        postProgressBar = findViewById(R.id.postProgressBar2);
        baseImage = findViewById(R.id.baseImg);

        postForumName = findViewById(R.id.postForumName);

        addCommentButton = findViewById(R.id.postAddComment);
        commentListView = findViewById(R.id.postCommentList);

        moreOptionButton = findViewById(R.id.postMoreOption);
        returnBackButton = findViewById(R.id.returnBack);

        setPostData();
        addComment();
        returnToPreviousPage();
    }

    private void setPostData() {
        commentList = new ArrayList<>();

        postTimestampDate = new Date();
        postUpdateTimestampDate = new Date();

        postData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    ownerId = (String) document.get("ownerId");
                    postTitleStr = (String) document.get("title");
                    postDescriptionStr = (String) document.get("description");
                    postTimestampStr = (String) document.get("date");
                    postUpdateTimestampStr = (String) document.get("updateDate");
                    postImageUri = document.getString("image");

                    if(postImageUri != null) {
                        try {
                            postPicture.setVisibility(View.VISIBLE);
                            new AsyncImage(postImage, postProgressBar).loadImage(postImageUri);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                    if (postTimestampStr != null) {
                        try {
                            postTimestampDate = sdf.parse(postTimestampStr);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (postUpdateTimestampStr != null) {
                        try {
                            postUpdateTimestampDate = sdf.parse(postUpdateTimestampStr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (document.get("likeIds") != null) {
                        postLikeIds = (ArrayList<String>) document.get("likeIds");

                        if (postLikeIds != null) {
                            int likeCount = postLikeIds.size();

                            if (postLikeIds.contains(userId)) {
                                postLike.setVisibility(View.INVISIBLE);
                                postLikeTrue.setVisibility(View.VISIBLE);

                                postLikeTrue.setText(String.valueOf(likeCount));
                            }
                            else {
                                postLike.setVisibility(View.VISIBLE);
                                postLikeTrue.setVisibility(View.INVISIBLE);

                                postLike.setText(String.valueOf(likeCount));
                            }
                        } else {
                            postLike.setVisibility(View.VISIBLE);
                            postLikeTrue.setVisibility(View.INVISIBLE);
                            postLike.setText("0");
                        }

                    }

                    if (document.get("dislikeIds") != null) {
                        postDislikeIds = (ArrayList<String>) document.get("dislikeIds");

                        if (postDislikeIds != null) {
                            int dislikeCount = postDislikeIds.size();

                            if (postDislikeIds.contains(userId)) {
                                postDislike.setVisibility(View.INVISIBLE);
                                postDislikeTrue.setVisibility(View.VISIBLE);

                                postDislikeTrue.setText(String.valueOf(dislikeCount));
                            }
                            else {
                                postDislike.setVisibility(View.VISIBLE);
                                postDislikeTrue.setVisibility(View.INVISIBLE);

                                postDislike.setText(String.valueOf(dislikeCount));
                            }
                        }
                        else {
                            postDislike.setVisibility(View.VISIBLE);
                            postDislikeTrue.setVisibility(View.INVISIBLE);
                            postDislike.setText("0");
                        }

                    }

                    if (ownerId != null) {
                        db.collection("users").document(ownerId).get()
                            .addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()) {
                                    DocumentSnapshot userDocument = userTask.getResult();

                                    if (userDocument.exists()) {
                                        postUsernameStr = (String) userDocument.get("name");
                                        postUserImageUri = userDocument.getString("image");

                                        if (postUsernameStr != null) {
                                            postUsername.setText(postUsernameStr);
                                        }

                                        if (postUserImageUri != null) {
                                            try {
                                                baseImage.setVisibility(View.INVISIBLE);
                                                postUserImage.setVisibility(View.VISIBLE);
                                                userProgressBar.setVisibility(View.VISIBLE);
                                                new AsyncImage(postUserImage, userProgressBar).loadImage(postUserImageUri);
                                            }
                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else {
                                            baseImage.setVisibility(View.VISIBLE);
                                            postUserImage.setVisibility(View.INVISIBLE);
                                            userProgressBar.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                }
                            });
                    }

                    if (postTitleStr != null) {
                        postTitle.setText(postTitleStr);
                    }

                    if (postDescriptionStr != null) {
                        postDescription.setText(postDescriptionStr);
                    }

                    StringBuilder timestamp = new StringBuilder();

                    if (postTimestampStr != null) {
                        timestamp.append(postTimestampStr);
                    }

                    if (postUpdateTimestampStr != null) {
                        timestamp.append(" (Edited: ").append(postUpdateTimestampStr).append(")");
                    }

                    postTimestamp.setText(timestamp.toString());

                    if (document.get("commentIds") != null) {
                        commentIds = (ArrayList<String>) document.get("commentIds");

                        if (commentIds != null) {
                            displayList(commentIds);
                            postComment.setText(String.valueOf(commentIds.size()));
                        }
                        else {
                            postComment.setText("0");
                        }

                    }

                    forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                if (document.exists()) {
                                    postForumName.setText(document.getString("title"));

                                    chiefAdminId = (String) document.get("chiefAdmin");

                                    if (document.get("moderatorIds") != null) {
                                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                    } else {
                                        moderatorIds = new ArrayList<>();
                                    }

                                    if (document.get("memberIds") != null) {
                                        memberIds = (ArrayList<String>) document.get("memberIds");

                                    } else {
                                        memberIds = new ArrayList<>();
                                    }
                                }

                                setButton(chiefAdminId, moderatorIds, memberIds, ownerId);
                                moreOption(chiefAdminId, moderatorIds, memberIds, ownerId);
                            }
                        }
                    });
                }
            }
        });

        setLikePost();
        setDislikePost();

//        postUserImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent accessIntent = new Intent(PostView.this, Profile.class);
//                accessIntent.putExtra("userId", ownerId);
//                startActivity(accessIntent);
//            }
//        });
    }

    private void displayList(ArrayList<String> commentIds) {
        final int[] listLength = {commentIds.size()};
        AtomicInteger counter = new AtomicInteger(0);

        for (String commentId: commentIds) {
            Log.i(TAG, "displayList - commentId: " + commentId);
            db.collection("COMMENTS").document(commentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String commentDescription, commentOwnerId, timestampStr, updateTimestampStr, repliedCommentId, imgUri;
                        boolean isReply;
                        Date timestamp = new Date(), updateTimeStamp = new Date();

                        if (document.exists()) {
                            commentDescription = (String) document.get("description");
                            commentOwnerId = (String) document.get("ownerId");

                            repliedCommentId = (String) document.get("repliedCommentId");

                            if (document.getString("image") != null) {
                                imgUri = document.getString("image");
                            } else {
                                imgUri = null;
                            }

                            if (document.get("likeIds") != null) {
                                commentLikeIds = (ArrayList<String>) document.get("likeIds");
                            } else {
                                commentLikeIds = new ArrayList<>();
                            }

                            if (document.get("dislikeIds") != null) {
                                commentDislikeIds = (ArrayList<String>) document.get("dislikeIds");
                            } else {
                                commentDislikeIds = new ArrayList<>();
                            }

//                            if (document.get("replyCommentIds") != null) {
//                                replyCommentIds = (ArrayList<String>) document.get("replyCommentIds");
//                                displayList(replyCommentIds);
//
//                            } else {
//                                replyCommentIds = new ArrayList<>();
//                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                            timestampStr = (String) document.get("date");
                            updateTimestampStr = (String) document.get("updateDate");

                            if (timestampStr != null) {
                                try {
                                    timestamp = sdf.parse(timestampStr);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (updateTimestampStr != null) {
                                try {
                                    updateTimeStamp = sdf.parse(updateTimestampStr);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                updateTimeStamp = null;
                            }

//                            isReply = repliedCommentId != null;

                            Comment comment = new Comment(commentId, commentOwnerId, postId, commentDescription, repliedCommentId, timestamp, updateTimeStamp, imgUri, commentLikeIds, commentDislikeIds, replyCommentIds);

                            Collections.sort(commentList, (comment1, comment2)
                                    -> comment2.getTimestamp().compareTo(comment1.getTimestamp()));

                            int index = Collections.binarySearch(commentList, comment, (comment1, comment2)
                                    -> comment2.getTimestamp().compareTo(comment1.getTimestamp()));


                            int insertionPoint = (index < 0) ? -index : index;

                            if (insertionPoint >= commentList.size()) {
                                commentList.add(comment);
                            } else {
                                commentList.add(insertionPoint, comment);
                            }

                            if (counter.incrementAndGet() == listLength[0]) {
                                setupList(commentList);
                            }
                        }
                    }
                }
            });
        }
    }

    private void setButton(String chiefAdminId, ArrayList<String> moderatorIds, ArrayList<String> memberIds, String ownerId) {
        Log.i(TAG, "setButton - chiefAdminId: " + chiefAdminId);
        Log.i(TAG, "setButton - moderatorIds: " + moderatorIds);
        Log.i(TAG, "setButton - memberIds: " + memberIds);
        Log.i(TAG, "setButton - ownerId: " + ownerId);
        Log.i(TAG, "setButton - userId: " + userId);

        if (Objects.equals(userId, chiefAdminId) || (moderatorIds != null && moderatorIds.contains(userId))) {
            moreOptionButton.setVisibility(View.VISIBLE);
            addCommentButton.setVisibility(View.VISIBLE);

        } else if (memberIds != null && memberIds.contains(userId)) {
            addCommentButton.setVisibility(View.VISIBLE);
//            moreOptionButton.setVisibility(View.VISIBLE);

            if (Objects.equals(userId, ownerId)) {
                moreOptionButton.setVisibility(View.VISIBLE);
            }
            else {
                moreOptionButton.setVisibility(View.GONE);
            }

        } else {
            moreOptionButton.setVisibility(View.GONE);
            addCommentButton.setVisibility(View.GONE);
        }
    }

    private void toggleLikeDislike(ArrayList<String> currentList, ArrayList<String> otherList,
                                   String currField, String otherField, final TextView likeView, final TextView dislikeView,
                                   final TextView likeViewTrue, final TextView dislikeViewTrue) {
        if (currentList != null) {
            if (!currentList.contains(userId)) {
                ArrayList<String> finalCurrList = currentList;
                postData.update(currField, FieldValue.arrayUnion(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        likeViewTrue.setVisibility(View.VISIBLE);
                        likeView.setVisibility(View.INVISIBLE);

                        likeViewTrue.setText(String.valueOf(finalCurrList.size()));

                        if (otherList != null && otherList.contains(userId)) {
                            postData.update(otherField, FieldValue.arrayRemove(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dislikeView.setVisibility(View.VISIBLE);
                                    dislikeViewTrue.setVisibility(View.INVISIBLE);

                                    dislikeView.setText(String.valueOf(otherList.size() - 1));
                                }
                            });
                        } else {
                            dislikeView.setVisibility(View.VISIBLE);
                            dislikeViewTrue.setVisibility(View.INVISIBLE);

                            dislikeView.setText("0");
                        }
//                        recreate();
                        setUI();
                    }
                });
            } else {
                currentList.remove(userId);
                ArrayList<String> finalCurrList = currentList;
                postData.update(currField, FieldValue.arrayRemove(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        likeView.setVisibility(View.VISIBLE);
                        likeViewTrue.setVisibility(View.INVISIBLE);

                        likeView.setText(String.valueOf(finalCurrList.size()));

                        if (otherList != null) {
                            dislikeView.setText(String.valueOf(otherList.size()));
                        } else {
                            dislikeView.setText("0");
                        }

//                        recreate();
                        setUI();
                    }
                });
            }
        }
        else {
            currentList = new ArrayList<>();
            currentList.add(userId);

            Map<String, ArrayList<String>> actionData = new HashMap<>();
            actionData.put(currField, currentList);

            ArrayList<String> finalCurrList = currentList;
            postData.set(actionData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    likeView.setVisibility(View.INVISIBLE);
                    likeViewTrue.setVisibility(View.VISIBLE);

                    likeViewTrue.setText(String.valueOf(finalCurrList.size()));

                    if (otherList != null && otherList.contains(userId)) {
                        postData.update(otherField, FieldValue.arrayRemove(userId)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dislikeView.setVisibility(View.VISIBLE);
                                dislikeViewTrue.setVisibility(View.INVISIBLE);

                                dislikeView.setText(String.valueOf(otherList.size() - 1));
                            }
                        });
                    } else {
                        dislikeView.setVisibility(View.VISIBLE);
                        dislikeViewTrue.setVisibility(View.INVISIBLE);

                        dislikeView.setText("0");
                    }

//                    recreate();
                    setUI();
                }
            });
        }
    }

    private void setLikePost() {
        postLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeDislike(postLikeIds, postDislikeIds, "likeIds", "dislikeIds",
                        postLike, postDislike, postLikeTrue, postDislikeTrue);
            }
        });

        postLikeTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeDislike(postLikeIds, postDislikeIds, "likeIds", "dislikeIds",
                        postLike, postDislike, postLikeTrue, postDislikeTrue);
            }
        });
    }

    private void setDislikePost() {
        postDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeDislike(postDislikeIds, postLikeIds, "dislikeIds", "likeIds",
                        postDislike, postLike, postDislikeTrue, postLikeTrue);
            }
        });

        postDislikeTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeDislike(postDislikeIds, postLikeIds, "dislikeIds", "likeIds",
                        postDislike, postLike, postDislikeTrue, postLikeTrue);
            }
        });
    }
    private void addComment() {
        addCommentButton.setOnClickListener(v -> {
            Intent addCommentIntent = new Intent(PostView.this, CreateCommentForm.class);
            addCommentIntent.putExtra("postId", postId);
            addCommentIntent.putExtra("isReply", false);
            startActivityForResult(addCommentIntent, constant.CREATE);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String commentId, commentDescription, commentTimestamp, commentImgUri;

        if (requestCode == constant.CREATE && resultCode == RESULT_OK) {
            if (data != null) {
                commentId = (String) data.getExtras().get("commentId");
                commentDescription = (String) data.getExtras().get("description");
                commentTimestamp = (String) data.getExtras().get("date");

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                Date timestamp = new Date();

                if (commentTimestamp != null) {
                    try {
                        timestamp = sdf.parse(commentTimestamp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (data.getExtras().get("image") != null) {
                    commentImgUri = (String) data.getExtras().get("image");
                } else {
                    commentImgUri = null;
                }

                ArrayList<String> commentLikeIds = new ArrayList<>(), commentDislikeIds = new ArrayList<>(), replyCommentIds = new ArrayList<>();

                Comment comment = new Comment(commentId, userId, postId, commentDescription, null, timestamp, null, commentImgUri, commentLikeIds, commentDislikeIds, replyCommentIds);

                if (commentList == null) {
                    commentList = new ArrayList<>();
                }

                Collections.sort(commentList, (comment1, comment2)
                        -> comment2.getTimestamp().compareTo(comment1.getTimestamp()));

                int index = Collections.binarySearch(commentList, comment, (comment1, comment2)
                        -> comment2.getTimestamp().compareTo(comment1.getTimestamp()));


                int insertionPoint = (index < 0) ? -index : index;

                if (insertionPoint >= commentList.size()) {
                    commentList.add(comment);
                } else {
                    commentList.add(insertionPoint, comment);
                }

            }
            setupList(commentList);

            recreate();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        if (requestCode == constant.EDIT && resultCode == RESULT_OK) {
            recreate();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void moreOption(String chiefAdminId, ArrayList<String> moderatorIds, ArrayList<String> memberIds, String ownerId) {
        PopupMenu popupMenu = new PopupMenu(PostView.this, moreOptionButton);
        popupMenu.getMenuInflater().inflate(R.menu.post_more_option, popupMenu.getMenu());

        MenuItem editPost = popupMenu.getMenu().findItem(R.id.postUpdate);
        MenuItem deletePost = popupMenu.getMenu().findItem(R.id.postDelete);
        MenuItem bannedUser = popupMenu.getMenu().findItem(R.id.postBanUser);
        MenuItem reportPost = popupMenu.getMenu().findItem(R.id.postReport);

        Log.i(TAG, "moreOption - chiefAdminId: " + chiefAdminId);
        Log.i(TAG, "moreOption - moderatorIds: " + moderatorIds);
        Log.i(TAG, "moreOption - memberIds: " + memberIds);
        Log.i(TAG, "moreOption - ownerId: " + ownerId);
        Log.i(TAG, "moreOption - userId: " + userId);

        if (Objects.equals(userId, chiefAdminId) || (moderatorIds != null && moderatorIds.contains(userId))) {
            deletePost.setVisible(true);

            editPost.setVisible(Objects.equals(userId, ownerId));

        } else if (memberIds != null && memberIds.contains(userId)) {
            if (Objects.equals(userId, ownerId)) {
                editPost.setVisible(true);
                deletePost.setVisible(true);
            }
            else {
                editPost.setVisible(false);
                deletePost.setVisible(false);
            }
        }
        else {
            deletePost.setVisible(false);
            editPost.setVisible(false);
        }

        moreOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.postUpdate) {
                            updatePost();
                        } else if (itemId == R.id.postDelete) {
                            deletePostAlert();
                        } else if (itemId == R.id.postBanUser) {

                        } else if (itemId == R.id.postReport) {

                        }

                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void updatePost() {
        Intent updateIntent = new Intent(PostView.this, EditPostView.class);
        updateIntent.putExtra("forumId", forumId);
        updateIntent.putExtra("postId", postId);
        startActivityForResult(updateIntent, constant.EDIT);

    }

    private void deletePostAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostView.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View deleteDialogLayout = inflater.inflate(R.layout.ui_delete_dialog_view, null);

        TextView dialogMessage = deleteDialogLayout.findViewById(R.id.dialogMessage);
        Button cancelButton = deleteDialogLayout.findViewById(R.id.dialogCancel);
        Button deleteButton = deleteDialogLayout.findViewById(R.id.dialogAccept);

        builder.setView(deleteDialogLayout);
        AlertDialog dialog = builder.create();
        dialog.show();

        try {
            dialogMessage.setText("Are you sure you want to delete this post");
            cancelButton.setOnClickListener(v -> dialog.dismiss());

            deleteButton.setOnClickListener(v -> {
                deletePost(postId);
                dialog.dismiss();

                Intent deleteIntent = new Intent(PostView.this, ForumView.class);
                deleteIntent.putExtra("forumId", forumId);
                deleteIntent.putExtra("postId", postId);
                setResult(RESULT_OK, deleteIntent);
                finish();
            });

        } catch (Exception e) {
            Log.e("Post", "getView: ", e);
            e.printStackTrace();
        }

    }

    private void deletePost(String postId) {
        DocumentReference ownerData = db.collection("users").document(ownerId);
        ownerData.update("postIds", FieldValue.arrayRemove(postId));

        DocumentReference forumData = db.collection("FORUMS").document(forumId);
        forumData.update("postIds", FieldValue.arrayRemove(postId));

        if (postImageUri != null) {
            String pattern = "images%2F(.*?)\\?";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(postImageUri);

            if (m.find()) {
                String oldUri = m.group(1);

                // Create a reference to the old image and delete it
                StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                    Log.i("Delete image", "Old image deleted successfully");
                }).addOnFailureListener(e -> {
                    Log.e("Delete image", "Failed to delete old image: " + e.getMessage());
                });
            }
        }

        postData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        ArrayList<String> commentIds = new ArrayList<>();

                        if (document.get("commentIds") != null) {
                            commentIds = new ArrayList<>((ArrayList<String>) document.get("commentIds"));
                            Log.i(TAG, "onComplete: commentIds: " + commentIds);

                            for (String id : commentIds) {
                                adapter.deleteCommentFromPost(id);
                            }
                        }

                        postData.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i(TAG, "onSuccess: delete post");
                            }
                        });
                    }
                }
            }
        });
    }

    public void deletePostFromForum(String postId) {
        DocumentReference postData = db.collection("POSTS").document(postId);

        postData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String forumId, ownerId, imgUri;

                    if (document.exists()) {
                        forumId = document.getString("forumId");
                        ownerId = document.getString("ownerId");

                        ArrayList<String> commentIds = new ArrayList<>();

                        if(ownerId != null) {
                            DocumentReference ownerData = db.collection("users").document(ownerId);
                            ownerData.update("postIds", FieldValue.arrayRemove(postId));
                        }

                        if (forumId != null) {
                            DocumentReference forumData = db.collection("FORUMS").document(forumId);
                            forumData.update("postIds", FieldValue.arrayRemove(postId));
                        }

                        if (document.get("commentIds") != null) {
                            commentIds = new ArrayList<>((ArrayList<String>) document.get("commentIds"));
                            CommentRecyclerViewAdapter adapter = new CommentRecyclerViewAdapter(getBaseContext(), commentIds, true);

                            Log.i(TAG, "onComplete: commentIds: " + commentIds);

                            for (String id : commentIds) {
                                adapter.deleteCommentFromPost(id);
                            }
                        }

                        imgUri = document.getString("image");
                        if (imgUri != null) {
                            String pattern = "images%2F(.*?)\\?";
                            Pattern p = Pattern.compile(pattern);
                            Matcher m = p.matcher(imgUri);

                            if (m.find()) {
                                String oldUri = m.group(1);

                                // Create a reference to the old image and delete it
                                StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                    Log.i("Delete image", "Old image deleted successfully");
                                }).addOnFailureListener(e -> {
                                    Log.e("Delete image", "Failed to delete old image: " + e.getMessage());
                                });
                            }
                        }
                    }

                    postData.delete();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshPostData(int position) {
        recreate();

        Log.i(TAG, "refreshPostData - position: " + position + " - adapter.getItemCount(): " + adapter.getItemCount());

        if (adapter != null) {
            adapter.notifyItemRemoved(position);
            adapter.notifyDataSetChanged();
        }

    }

    private void setupList(ArrayList<Comment> commentList) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentListView.setLayoutManager(layoutManager);

        adapter = new CommentRecyclerViewAdapter(this, commentList);
        commentListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> {
            Intent returnIntent = new Intent(PostView.this, ForumView.class);
            returnIntent.putExtra("forumId", forumId);
            returnIntent.putExtra("postId", postId);
            setResult(0, returnIntent);
            finish();
        });
    }
}
