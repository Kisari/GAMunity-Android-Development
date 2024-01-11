package rmitcom.asm1.gamunity.components.views.post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.CommentListAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.comment.CreateCommentForm;
import rmitcom.asm1.gamunity.model.Comment;
import rmitcom.asm1.gamunity.model.Constant;

public class PostView extends AppCompatActivity implements CommentListAdapter.CommentDeleteListener {
    private final String TAG = "Post View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData, postData;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private String postId, forumId, ownerId, chiefAdminId,
            postUsernameStr, postTimestampStr, postUpdateTimestampStr,
            postTitleStr, postDescriptionStr, postUserImageUri, postImageUri;
    private int noLike, noDislike, noComment;
    private Date postTimestampDate, postUpdateTimestampDate;
    private ArrayList<String> memberIds, moderatorIds, postLikeIds, postDislikeIds, postCommentIds;
    private ArrayList<String> commentLikeIds, commentDislikeIds, replyCommentIds;
    private ArrayList<Comment> commentList;
    private TextView postUsername, postTimestamp, postTitle, postDescription, moreOptionButton,
            postLike, postDislike, postComment, postLikeTrue, postDislikeTrue,
            addCommentButton, returnBackButton;
    private ListView commentListView;
    private RelativeLayout postPicture;
    private ProgressBar userProgressBar, postProgressBar;
    private ImageView postImage;
    private ShapeableImageView postUserImage;
    private CommentListAdapter adapter;
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

        addCommentButton = findViewById(R.id.postAddComment);
        commentListView = findViewById(R.id.postCommentList);

        moreOptionButton = findViewById(R.id.postMoreOption);
        returnBackButton = findViewById(R.id.returnBack);

        setPostData();
        addComment();
        moreOption();
        returnToPreviousPage();
    }

    private void setPostData() {
        postTimestampDate = new Date();
        postUpdateTimestampDate = new Date();

        commentList = new ArrayList<>();

        forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        chiefAdminId = (String) document.get("chiefAdmin");

                        memberIds = (ArrayList<String>) document.get("memberIds");
                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                    }
                }
            }
        });

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

                    String format = "dd/MM/yyyy HH:mm";
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

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

                    if (document.get("noLike") == null) {
                        noLike = 0;
                    } else {
                        noLike = (int) document.get("noLike");
                    }

                    if (document.get("noDislike") == null) {
                        noDislike = 0;
                    } else {
                        noDislike = (int) document.get("noDislike");
                    }

                    if (document.get("noComment") == null) {
                        noComment = 0;
                    } else {
                        noComment = (int) document.get("noComment");
                    }

                    if (document.get("likeIds") != null) {
                        postLikeIds = (ArrayList<String>) document.get("likeIds");
                    } else {
                        postLikeIds = new ArrayList<>();
                    }

                    if (document.get("dislikeIds") != null) {
                        postDislikeIds = (ArrayList<String>) document.get("dislikeIds");
                    } else {
                        postDislikeIds = new ArrayList<>();
                    }

                    if (document.get("commentIds") != null) {
                        postCommentIds = (ArrayList<String>) document.get("commentIds");
                        displayList(postCommentIds);

                    } else {
                        postCommentIds = new ArrayList<>();
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

                    postTimestamp.setText(timestamp);
                    setButton();

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void setButton() {
        if (Objects.equals(userId, chiefAdminId)) {
            moreOptionButton.setVisibility(View.VISIBLE);
            addCommentButton.setVisibility(View.VISIBLE);

        } else if (memberIds.contains(userId) || moderatorIds.contains(userId)) {
            moreOptionButton.setVisibility(View.VISIBLE);
            addCommentButton.setVisibility(View.VISIBLE);

        } else {
            moreOptionButton.setVisibility(View.GONE);
            addCommentButton.setVisibility(View.GONE);
        }
    }

    private void displayList(ArrayList<String> commentIds) {
//        int listLength = commentIds.size();
//        final int[] counter = {0};

        int listLength = commentIds.size();
        AtomicInteger counter = new AtomicInteger(0);

        for (String commentId: commentIds) {
            db.collection("COMMENTS").document(commentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String commentDescription, commentOwnerId, timestampStr, updateTimestampStr, repliedCommentId, imgUri;
                        int noLike, noDislike, noComment;
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

                            if (document.get("noLike") == null) {
                                noLike = 0;
                            } else {
                                noLike = (int) document.get("noLike");
                            }

                            if (document.get("noDislike") == null) {
                                noDislike = 0;
                            } else {
                                noDislike = (int) document.get("noDislike");
                            }

                            if (document.get("noComment") == null) {
                                noComment = 0;
                            } else {
                                noComment = (int) document.get("noComment");
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

                            isReply = repliedCommentId != null;

                            Comment comment = new Comment(commentId, commentOwnerId, postId, commentDescription, repliedCommentId, timestamp, updateTimeStamp, imgUri, commentLikeIds, commentDislikeIds, replyCommentIds, isReply, noLike, noDislike, noComment);
                            commentList.add(comment);

//                            counter[0]++;
//                            if (counter[0] == listLength -1) {
//                                setupList(commentList);
//                            }

                            if (counter.incrementAndGet() == listLength) {
                                setupList(commentList);
                            }
                        }
                    }
                }
            });
        }
    }
    private void addComment() {
        addCommentButton.setOnClickListener(v -> {
            Intent addCommentIntent = new Intent(PostView.this, CreateCommentForm.class);
            addCommentIntent.putExtra("postId", postId);
            addCommentIntent.putExtra("isReply", false);
            startActivityForResult(addCommentIntent, constant.CREATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String commentId, commentDescription, commentTimestamp, commentImgUri;
        if (requestCode == constant.CREATE && resultCode == RESULT_OK) {
            if (data != null) {
                commentId = (String) data.getExtras().get("commentId");
                commentDescription = (String) data.getExtras().get("description");
                commentTimestamp = (String) data.getExtras().get("date");

                Date timestamp = new Date();

                if (commentTimestamp != null) {
                    try {
                        timestamp = sdf.parse(commentTimestamp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if ((String) data.getExtras().get("image") != null) {
                    commentImgUri = (String) data.getExtras().get("image");
                } else {
                    commentImgUri = null;
                }

                ArrayList<String> commentLikeIds = new ArrayList<>(), commentDislikeIds = new ArrayList<>(), replyCommentIds = new ArrayList<>();

                Comment comment = new Comment(commentId, userId, postId, commentDescription, null, timestamp, null, commentImgUri, commentLikeIds, commentDislikeIds, replyCommentIds, false, 0, 0, 0);
                commentList.add(comment);

            }
            setupList(commentList);
        }

        if (requestCode == constant.EDIT && resultCode == RESULT_OK) {
            setPostData();
        }
    }

    private void moreOption() {
        PopupMenu popupMenu = new PopupMenu(PostView.this, moreOptionButton);
        popupMenu.getMenuInflater().inflate(R.menu.post_more_option, popupMenu.getMenu());

        MenuItem editPost = popupMenu.getMenu().findItem(R.id.postUpdate);
        MenuItem deletePost = popupMenu.getMenu().findItem(R.id.postDelete);
        MenuItem bannedUser = popupMenu.getMenu().findItem(R.id.postBanUser);
        MenuItem reportPost = popupMenu.getMenu().findItem(R.id.postReport);

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
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletePost(postId);
                    dialog.dismiss();
                    finish();
                }
            });

        } catch (Exception e) {
            Log.e("Post", "getView: ", e);
            e.printStackTrace();
        }

    }

    private void deletePost(String postId) {
        DocumentReference ownerData = db.collection("users").document(ownerId);
        ownerData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Map<String, ArrayList<String>> userPostIds = new HashMap<>();

                if (document.exists()) {
                    ArrayList<String> postIds = (ArrayList<String>) document.get("postIds");

                    if (postIds != null) {
                        postIds.remove(postId);
                        userPostIds.put("postIds", postIds);
                    }
                }
                ownerData.set(userPostIds, SetOptions.merge());
            }
        });

        DocumentReference forumData = db.collection("FORUMS").document(forumId);
        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Map<String, ArrayList<String>> userPostIds = new HashMap<>();

                if (document.exists()) {
                    ArrayList<String> postIds = (ArrayList<String>) document.get("postIds");

                    if (postIds != null) {
                        postIds.remove(postId);
                        userPostIds.put("postIds", postIds);
                    }
                }
                forumData.set(userPostIds, SetOptions.merge());
            }
        });

        postData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        ArrayList<String> commentIds = (ArrayList<String>) document.get("commentIds");

                        if (commentIds != null) {
                            for (String id: commentIds) {
                                adapter.deleteComment(id);
                            }
                        }
                    }

                    postData.delete();

                }
            }
        });

    }

    public void deletePostFromForum(String postId, Context context) {
        DocumentReference postData = db.collection("POSTS").document(postId);

        postData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String forumId, ownerId;
                    ArrayList<String> commentIdsList = new ArrayList<>();

                    if (document.exists()) {
                        forumId = document.getString("forumId");
                        ownerId = document.getString("ownerId");

                        DocumentReference ownerData = db.collection("users").document(ownerId);
                        ownerData.get().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                DocumentSnapshot userDocument = userTask.getResult();
                                Map<String, ArrayList<String>> userPostIds = new HashMap<>();

                                if (userDocument.exists()) {
                                    ArrayList<String> postIds = (ArrayList<String>) userDocument.get("postIds");

                                    if (postIds != null) {
                                        postIds.remove(postId);
                                        userPostIds.put("postIds", postIds);
                                    }
                                }
                                ownerData.set(userPostIds, SetOptions.merge());
                            }
                        });

                        DocumentReference forumData = db.collection("FORUMS").document(forumId);
                        forumData.get().addOnCompleteListener(forumTask -> {
                            if (forumTask.isSuccessful()) {
                                DocumentSnapshot forumDocument = forumTask.getResult();
                                Map<String, ArrayList<String>> userPostIds = new HashMap<>();

                                if (forumDocument.exists()) {
                                    ArrayList<String> postIds = (ArrayList<String>) forumDocument.get("postIds");

                                    if (postIds != null) {
                                        postIds.remove(postId);
                                        userPostIds.put("postIds", postIds);
                                    }
                                }
                                forumData.set(userPostIds, SetOptions.merge());
                            }
                        });

                        commentIdsList = (ArrayList<String>) document.get("commentIds");

                        if (commentIdsList != null) {
                            ArrayList<Comment> commentList = new ArrayList<>();
                            for (String id: commentIdsList) {
                                Comment comment = new Comment(id);
                                commentList.add(comment);
                            }

                            CommentListAdapter commentListAdapter = new CommentListAdapter(context, R.layout.ui_comment_list_view_item, commentList);
                            for (String id: commentIdsList) {
                                commentListAdapter.deleteCommentFromForum(id);
                            }
                        }
                    }

                    postData.delete();

                }
            }
        });

    }

    private void setupList(ArrayList<Comment> commentList) {
        adapter = new CommentListAdapter(this, R.layout.ui_comment_list_view_item, commentList);
        commentListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onCommentDeleted() {
        setPostData();
    }
}