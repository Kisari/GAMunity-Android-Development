package rmitcom.asm1.gamunity.components.views.forum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.PostRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.fragments.HomeFragment;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.chat.ChatView;
import rmitcom.asm1.gamunity.components.views.post.CreatePostView;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Forum;
import rmitcom.asm1.gamunity.model.Post;

public class ForumView extends AppCompatActivity {

    private final String TAG = "Forum View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
    private String forumId , chiefAdminId, chatId,
            forumTitleStr, forumBackgroundUri, forumIconUri;
    private ArrayList<String> memberIds, moderatorIds, postIds;
    private ArrayList<Post> postList;
    private TextView forumTitle, userRole, moreOptionButton, moreInfoButton, returnBackButton, forumChat;
    private RecyclerView postListView;
    private ProgressBar backgroundProgressBar, iconProgressBar;
    private ImageView forumBackground;
    private ShapeableImageView forumIcon;
    private ImageButton addPostButton;
    private Button joinButton, joinedButton, ownedButton;
    private PostRecyclerViewAdapter adapter;
    private String isJoinWithForumId = "";
    private Forum currForum;
    private Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
            userData = db.collection("users").document(userId);

        }
        Log.i(TAG, "forumId: " + forumId);

        moreOptionButton = findViewById(R.id.forumMoreOption);
        moreInfoButton = findViewById(R.id.forumMoreInfo);
        forumTitle = findViewById(R.id.forumTitle);
        addPostButton = findViewById(R.id.forumAddPost);
        postListView = findViewById(R.id.forumPostList);
        backgroundProgressBar = findViewById(R.id.forumProgress1);
        iconProgressBar = findViewById(R.id.forumProgress2);
        forumBackground = findViewById(R.id.forumBackgroundImage);
        forumIcon = findViewById(R.id.forumIconImage);

        userRole = findViewById(R.id.userRole);

        joinButton = findViewById(R.id.forumActionJoinButton);
        joinedButton = findViewById(R.id.forumActionJoinedButton);
        ownedButton = findViewById(R.id.forumActionOwnedButton);

        forumChat = findViewById(R.id.forumChat);

        returnBackButton = findViewById(R.id.returnBack);

        setForumData();
        addPost();
        returnToPreviousPage();
    }

    @SuppressLint("SetTextI18n")
    private void setForumData() {
        postList = new ArrayList<>();

        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    forumTitleStr = (String) document.get("title");
                    chiefAdminId = (String) document.get("chiefAdmin");

                    memberIds = (ArrayList<String>) document.get("memberIds");
                    moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                    chatId = document.getString("chatId");

                    if (Objects.equals(userId, chiefAdminId)) {
                        userRole.setText("Admin");
                    } else if (moderatorIds != null && moderatorIds.contains(userId)) {
                        isJoinWithForumId = (String) document.get("forumId");
                        userRole.setText("Moderator");
                    } else if (memberIds != null && memberIds.contains(userId)) {
                        isJoinWithForumId = (String) document.get("forumId");
                        userRole.setText("Member");
                        isJoinWithForumId = forumId;
                    } else {
                        userRole.setText("Guest");
                    }

                    if (document.get("postIds") != null) {
                        postIds = (ArrayList<String>) document.get("postIds");
                        Log.i(TAG, "setForumData - postId: " + postIds);
                        displayList(postIds);
                    }

                    forumBackgroundUri = document.getString("forumBackground");
                    forumIconUri = document.getString("forumIcon");

                    Log.i(TAG, "forumBackgroundUri: " + forumBackgroundUri);
                    Log.i(TAG, "forumIconUri: " + forumIconUri);


                    if (forumTitleStr != null) {
                        forumTitle.setText(forumTitleStr);
                    }

                    try {
                        new AsyncImage(forumIcon, iconProgressBar).loadImage(forumIconUri);
                        new AsyncImage(forumBackground, backgroundProgressBar).loadImage(forumBackgroundUri);

                    } catch (Exception e){
                        Log.e(TAG, "getView: ", e);
                        e.printStackTrace();
                    }

                    setButton();
                    moreOption();

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void displayList(ArrayList<String> postIds) {

        final int[] listLength = {postIds.size()};
        AtomicInteger counter = new AtomicInteger(0);

        final int[] noPostImg = {0};
        final int[] listViewLen = {listLength[0] * 170};

        for (String postId : postIds) {
            Log.i(TAG, "displayList - postId: " + postId);
            db.collection("POSTS").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String postTitle, postDescription, postOwnerId, timestampStr, updateTimestampStr, imgUri;
                        Date timestamp = new Date(), updateTimestamp = new Date();
                        ArrayList<String> postLikeIds, postDislikeIds, postCommentIds;

                        if (document.exists()) {
                            postTitle = (String) document.get("title");
                            postDescription = (String) document.get("description");
                            postOwnerId = (String) document.get("ownerId");

                            postLikeIds = (ArrayList<String>) document.get("likeIds");
                            postDislikeIds = (ArrayList<String>) document.get("dislikeIds");
                            postCommentIds = (ArrayList<String>) document.get("commentIds");

                            timestampStr = (String) document.get("date");
                            updateTimestampStr = (String) document.get("updateDate");
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                            if (timestampStr != null) {
                                try {
                                    timestamp = sdf.parse(timestampStr);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (updateTimestampStr != null) {
                                try {
                                    updateTimestamp = sdf.parse(updateTimestampStr);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                updateTimestamp = null;
                            }

                            if (document.getString("image") != null) {
                                imgUri = document.getString("image");
                                noPostImg[0]++;
                                listViewLen[0] += 200;
                            } else {
                                imgUri = null;
                            }

                            Log.i(TAG, "onComplete - postId: " + postId);
                            Post post = new Post(postId, postOwnerId, forumId, postTitle, postDescription, timestamp, updateTimestamp, imgUri, postCommentIds, postLikeIds, postDislikeIds);
                            postList.add(post);

                            if (counter.incrementAndGet() == listLength[0]) {
                                setupList(postList);
                            }
                        }
                    }
                }
            });
        }
    }

    private void addPost() {
        addPostButton.setOnClickListener(v -> {
            Intent addPostIntent = new Intent(ForumView.this, CreatePostView.class);
            addPostIntent.putExtra("forumId", forumId);
            startActivityForResult(addPostIntent, constant.CREATE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String postId, postTitle, postDescription, postTimestamp, postImgUri;
        if (requestCode == constant.CREATE && resultCode == RESULT_OK) {
            if (data != null) {

                postId = (String) data.getExtras().get("postId");
                postTitle = (String) data.getExtras().get("title");
                postDescription = (String) data.getExtras().get("description");
                postTimestamp = (String) data.getExtras().get("date");

                Date timestamp = new Date();
                Log.i(TAG, "timestamp: " + timestamp);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                if (postTimestamp != null) {
                    try {
                        timestamp = sdf.parse(postTimestamp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (data.getExtras().get("image") != null) {
                    postImgUri = (String) data.getExtras().get("image");
                } else {
                    postImgUri = null;
                }

                ArrayList<String> postLikeIds = new ArrayList<>(), postDislikeIds = new ArrayList<>(), postCommentIds = new ArrayList<>();

                Post post = new Post(postId, userId, forumId, postTitle, postDescription, timestamp, null, postImgUri, postLikeIds, postDislikeIds, postCommentIds);
                postList.add(post);
            }
            setupList(postList);
            recreate();
        }

        if (requestCode == constant.EDIT) {
            if (resultCode == RESULT_OK) {
                setUI();
            }
        }

        if (requestCode == constant.DELETE) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        }

        if (requestCode == 107 || requestCode == 108 || requestCode == 109) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        }
    }

    private void moreOption() {
        PopupMenu popupMenu = new PopupMenu(ForumView.this, moreOptionButton);
        popupMenu.getMenuInflater().inflate(R.menu.forum_more_option, popupMenu.getMenu());

        MenuItem moreInfo = popupMenu.getMenu().findItem(R.id.forumMoreInfo);
        MenuItem editForum = popupMenu.getMenu().findItem(R.id.forumUpdate);
        MenuItem deleteForum = popupMenu.getMenu().findItem(R.id.forumDelete);
        MenuItem addModerator = popupMenu.getMenu().findItem(R.id.forumAddModerator);
        MenuItem removeModerator = popupMenu.getMenu().findItem(R.id.forumRemoveModerator);
        MenuItem removeUser = popupMenu.getMenu().findItem(R.id.forumRemoveUser);

        Log.i(TAG, "moreOption - chiefAdmin: " + chiefAdminId);
        Log.i(TAG, "moreOption - userId: " + userId);
        Log.i(TAG, "moreOption - memberIds: " + memberIds);
        Log.i(TAG, "moreOption - moderatorIds: " + moderatorIds);

        if (Objects.equals(userId, chiefAdminId)) {
            moreInfo.setVisible(true);
            editForum.setVisible(true);
            deleteForum.setVisible(true);
            addModerator.setVisible(true);
            removeModerator.setVisible(true);
            removeUser.setVisible(true);

        } else if (moderatorIds != null && moderatorIds.contains(userId)) {
            moreInfo.setVisible(true);
            editForum.setVisible(true);
            deleteForum.setVisible(false);
            addModerator.setVisible(false);
            removeModerator.setVisible(false);
            removeUser.setVisible(false);
        } else {
            moreOptionButton.setVisibility(View.GONE);
            moreInfoButton.setVisibility(View.VISIBLE);
        }

        moreOptionButton.setOnClickListener(v -> {
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.forumMoreInfo) {
                    accessMoreInfo();
                } else if (itemId == R.id.forumUpdate) {
                    editForum();
                } else if (itemId == R.id.forumDelete) {
                    deleteForumAlert();
                } else if (itemId == R.id.forumAddModerator) {
                    promoteToModerator();
                } else if (itemId == R.id.forumRemoveModerator) {
                    demoteToMember();
                } else if (itemId == R.id.forumRemoveUser) {
                    removeUser();
                }

                return false;
            });
            popupMenu.show();
        });

        moreInfoButton.setOnClickListener(v -> accessMoreInfo());
    }

    private void accessMoreInfo() {
        Intent moreInfoIntent = new Intent(ForumView.this, ForumMoreInfoView.class);
        moreInfoIntent.putExtra("forumId", forumId);
        startActivity(moreInfoIntent);
    }

    private void editForum() {
        Intent editIntent = new Intent(ForumView.this, EditForumView.class);
        editIntent.putExtra("forumId", forumId);
        startActivityForResult(editIntent, constant.EDIT);
    }

    private void deleteForumAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForumView.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View deleteDialogLayout = inflater.inflate(R.layout.ui_delete_dialog_view, null);

        TextView dialogMessage = deleteDialogLayout.findViewById(R.id.dialogMessage);
        Button cancelButton = deleteDialogLayout.findViewById(R.id.dialogCancel);
        Button deleteButton = deleteDialogLayout.findViewById(R.id.dialogAccept);

        builder.setView(deleteDialogLayout);
        AlertDialog dialog = builder.create();
        dialog.show();

        try {
            dialogMessage.setText("Are you sure you want to delete this forum");
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteForum(forumId);
                    dialog.dismiss();

                    Intent deleteIntent = new Intent(ForumView.this, HomeFragment.class);
                    deleteIntent.putExtra("forum", currForum);
                    setResult(RESULT_OK, deleteIntent);
                    finish();
                }
            });

        } catch (Exception e) {
            Log.e("Forum", "getView: ", e);
            e.printStackTrace();
        }
    }

    private void deleteForum(String forumId) {
        forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        String chiefAdmin = document.getString("chiefAdmin");

                        if (chiefAdmin != null) {
                            DocumentReference userData = db.collection("users").document(chiefAdmin);
                            userData.update("ownedForumIds", FieldValue.arrayRemove(forumId));
                        }

                        if (document.get("moderatorIds") != null) {
                            ArrayList<String> moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                            if (moderatorIds != null) {
                                for (String id: moderatorIds) {
                                    DocumentReference userData = db.collection("users").document(id);

                                    userData.update("adminForumIds", FieldValue.arrayRemove(forumId));
                                }
                            }
                        }

                        if (document.get("memberIds") != null) {
                            ArrayList<String> memberIds = (ArrayList<String>) document.get("memberIds");

                            if (memberIds != null) {
                                for (String id: memberIds) {
                                    DocumentReference userData = db.collection("users").document(id);

                                    userData.update("joinedForumIds", FieldValue.arrayRemove(forumId));
                                }
                            }
                        }

                        if (document.get("postIds") != null) {
                            ArrayList<String> postIds = (ArrayList<String>) document.get("postIds");

                            if (postIds != null) {
                                PostView postView = new PostView();
                                for (String id: postIds) {
                                    postView.deletePostFromForum(id, getBaseContext());
                                }
                            }
                        }

                        String chatId = document.getString("chatId");

                        if (chatId != null) {
                            DocumentReference chatData = db.collection("CHATROOMS").document(chatId);

                        }
                    }

                    forumData.delete();

                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButton() {
        if (Objects.equals(userId, chiefAdminId)) {
            ownedButton.setVisibility(View.VISIBLE);
            joinButton.setVisibility(View.GONE);
            joinedButton.setVisibility(View.GONE);

            moreInfoButton.setVisibility(View.GONE);
            moreOptionButton.setVisibility(View.VISIBLE);
            addPostButton.setVisibility(View.VISIBLE);
            forumChat.setVisibility(View.VISIBLE);

        } else if ((moderatorIds != null && moderatorIds.contains(userId))) {
            ownedButton.setVisibility(View.GONE);
            joinButton.setVisibility(View.GONE);
            joinedButton.setVisibility(View.VISIBLE);

            moreInfoButton.setVisibility(View.GONE);
            moreOptionButton.setVisibility(View.VISIBLE);
            addPostButton.setVisibility(View.VISIBLE);
            forumChat.setVisibility(View.VISIBLE);

        } else if (((memberIds != null && memberIds.contains(userId)))) {
            ownedButton.setVisibility(View.GONE);
            joinButton.setVisibility(View.GONE);
            joinedButton.setVisibility(View.VISIBLE);

            moreInfoButton.setVisibility(View.VISIBLE);
            moreOptionButton.setVisibility(View.GONE);
            addPostButton.setVisibility(View.VISIBLE);
            forumChat.setVisibility(View.VISIBLE);

        } else {
            ownedButton.setVisibility(View.GONE);
            joinButton.setVisibility(View.VISIBLE);
            joinedButton.setVisibility(View.GONE);

            moreInfoButton.setVisibility(View.VISIBLE);
            moreOptionButton.setVisibility(View.GONE);
            addPostButton.setVisibility(View.GONE);
            forumChat.setVisibility(View.GONE);
        }

        postListView.setOnTouchListener((v, event) -> event.getAction() == MotionEvent.ACTION_MOVE);

        joinForum();
        unJoinForum();
        accessChatRoom();
    }

    private void joinForum() {
        joinButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UnsafeIntentLaunch")
            @Override
            public void onClick(View v) {
                userData.update("joinedForumIds", FieldValue.arrayUnion(forumId));
                forumData.update("memberIds", FieldValue.arrayUnion(userId));

                joinButton.setVisibility(View.GONE);
                joinedButton.setVisibility(View.VISIBLE);
                recreate();
            }
        });
    }

    private void unJoinForum() {
        joinedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ForumView.this);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View unJoinDialogLayout = inflater.inflate(R.layout.ui_forum_unjoin_dialog_view, null);

                ImageView dialogIcon = unJoinDialogLayout.findViewById(R.id.dialogIcon);
                ProgressBar dialogIconProgress = unJoinDialogLayout.findViewById(R.id.dialogIconProgress);
                TextView dialogMessage = unJoinDialogLayout.findViewById(R.id.dialogMessage);
                TextView dialogCancel = unJoinDialogLayout.findViewById(R.id.dialogCancel);
                TextView dialogAccept = unJoinDialogLayout.findViewById(R.id.dialogAccept);

                builder.setView(unJoinDialogLayout);
                AlertDialog dialog = builder.create();
                dialog.show();

                try {
                    new AsyncImage(dialogIcon, dialogIconProgress).loadImage(forumIconUri);
                    dialogMessage.setText("Are you sure you want to unjoin this forum");
                    dialogMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    dialogCancel.setOnClickListener(v1 -> dialog.dismiss());

                    dialogAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            unJoinFunction();
                            dialog.dismiss();
                            joinButton.setVisibility(View.VISIBLE);
                            joinedButton.setVisibility(View.GONE);
                            recreate();
                        }
                    });

                } catch (Exception e) {
                    Log.e("Forum", "getView: ", e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void unJoinFunction() {

    userData.update("joinedForumIds", FieldValue.arrayRemove(forumId));
    userData.update("adminForumIds", FieldValue.arrayRemove(forumId));

    forumData.update("memberIds", FieldValue.arrayRemove(userId));
    forumData.update("moderatorIds", FieldValue.arrayRemove(userId));

    }

    private void promoteToModerator() {
        Intent promoteIntent = new Intent(ForumView.this, PromoteToModerator.class);
        promoteIntent.putExtra("forumId", forumId);
        startActivityForResult(promoteIntent, 107);
    }

    private void demoteToMember() {
        Intent demoteIntent = new Intent(ForumView.this, DemoteToMember.class);
        demoteIntent.putExtra("forumId", forumId);
        startActivityForResult(demoteIntent, 108);
    }

    private void removeUser() {
        Intent demoteIntent = new Intent(ForumView.this, RemoveUser.class);
        demoteIntent.putExtra("forumId", forumId);
        startActivityForResult(demoteIntent, 109);
    }

    private void accessChatRoom() {
        forumChat.setOnClickListener(v -> {
            Intent chatIntent = new Intent(ForumView.this, ChatView.class);
            chatIntent.putExtra("chatId", chatId);
            chatIntent.putExtra("forumId", forumId);
            startActivity(chatIntent);
        });
    }

    private void setupList(ArrayList<Post> postList) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        postListView.setLayoutManager(layoutManager);

        adapter = new PostRecyclerViewAdapter(this, postList);
        postListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> {
            Intent backIntent = new Intent();
            backIntent.putExtra("isJoinWithForumId", isJoinWithForumId);
            if(!isJoinWithForumId.equals("")){
                setResult(constant.SUCCESS, backIntent);
            }
            finish();
        });
    }
}