package rmitcom.asm1.gamunity.components.views.forum;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
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
import rmitcom.asm1.gamunity.adapter.PostListAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.post.CreatePostView;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Post;

public class ForumView extends AppCompatActivity {

    private final String TAG = "Forum View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
    private String forumId = "ZZ7NRNW83lsjBKswutHj", chiefAdminId, forumTitleStr, forumBackgroundUri, forumIconUri;
    private ArrayList<String> memberIds, moderatorIds, postIds;
    private ArrayList<Post> postList;
    private TextView forumTitle, moreOptionButton, returnBackButton;
    private ListView postListView;
    private ProgressBar backgroundProgressBar, iconProgressBar;
    private ImageView forumBackground;
    private ShapeableImageView forumIcon;
    private ImageButton addPostButton;
    private Button joinButton, joinedButton, ownedButton;
    private PostListAdapter adapter;
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
//            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
            userData = db.collection("USERS").document(userId);

        }
        Log.i(TAG, "forumId: " + forumId);

        forumTitle = findViewById(R.id.forumTitle);
        moreOptionButton = findViewById(R.id.forumMoreOption);
        addPostButton = findViewById(R.id.forumAddPost);
        postListView = findViewById(R.id.forumPostList);
        backgroundProgressBar = findViewById(R.id.forumProgress1);
        iconProgressBar = findViewById(R.id.forumProgress2);
        forumBackground = findViewById(R.id.forumBackgroundImage);
        forumIcon = findViewById(R.id.forumIconImage);

        joinButton = findViewById(R.id.forumActionJoinButton);
        joinedButton = findViewById(R.id.forumActionJoinedButton);
        ownedButton = findViewById(R.id.forumActionOwnedButton);

        returnBackButton = findViewById(R.id.returnBack);

        setForumData();
        addPost();
        moreOption();
        returnToPreviousPage();
    }

    private void setForumData() {
        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    forumTitleStr = (String) document.get("title");
                    chiefAdminId = (String) document.get("chiefAdmin");

                    memberIds = (ArrayList<String>) document.get("memberIds");
                    moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                    if (document.get("postIds") != null) {
                        postIds = (ArrayList<String>) document.get("postIds");
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

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void displayList(ArrayList<String> postIds) {
        postList = new ArrayList<>();
//        int listLength = postIds.size();
//        final int[] counter = {0};

        int listLength = postIds.size();
        AtomicInteger counter = new AtomicInteger(0);

        for (String postId : postIds) {
            db.collection("POSTS").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String postTitle, postDescription, postOwnerId, timestampStr, updateTimestampStr;
                        long noLike, noDislike, noComment;
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

                            Log.i(TAG, "onComplete - postId: " + postId);
                            Post post = new Post(postId, postOwnerId, forumId, postTitle, postDescription, timestamp, updateTimestamp, postCommentIds, postLikeIds, postDislikeIds, noLike, noDislike, noComment);
                            postList.add(post);

//                            counter[0]++;
//                            if (counter[0] == listLength -1) {
//                                setupList(postList);
//                            }
                            if (counter.incrementAndGet() == listLength) {
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

        String postId, postTitle, postDescription, postTimestamp;
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

                ArrayList<String> postLikeIds = new ArrayList<>(), postDislikeIds = new ArrayList<>(), postCommentIds = new ArrayList<>();

                Post post = new Post(postId, userId, forumId, postTitle, postDescription, timestamp, null, postLikeIds, postDislikeIds, postCommentIds,0, 0, 0);
                postList.add(post);
            }
            setupList(postList);
        }

        if (requestCode == constant.EDIT && resultCode == RESULT_OK) {
            setForumData();
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

//        if (!isAdmin && !Objects.equals(userId, ownerId) && !memberIds.contains(userId)) {
//            moreOptionButton.setVisibility(View.GONE);
//        } else {}

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

                } else if (itemId == R.id.forumRemoveModerator) {

                }

                return false;
            });
            popupMenu.show();
        });
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
                    finish();
                }
            });

        } catch (Exception e) {
            Log.e("Forum", "getView: ", e);
            e.printStackTrace();
        }
    }

    private void deleteForum(String forumId) {
        if (memberIds != null) {
            for (String id: memberIds) {
                DocumentReference memberData = db.collection("users").document(id);
                memberData.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, ArrayList<String>> joinedForumIds = new HashMap<>();

                        if (document.exists()) {
                            ArrayList<String> joinedForumList = (ArrayList<String>) document.get("joinedForumIds");

                            if (joinedForumList != null) {
                                joinedForumList.remove(forumId);
                                joinedForumIds.put("joinedForumIds", joinedForumList);
                            }
                        }

                        memberData.set(joinedForumIds, SetOptions.merge());
                    }
                });
            }
        }

        if (moderatorIds != null) {
            for (String id: moderatorIds) {
                DocumentReference moderatorData = db.collection("users").document(id);
                moderatorData.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, ArrayList<String>> adminForumIds = new HashMap<>();

                        if (document.exists()) {
                            ArrayList<String> adminForumList = (ArrayList<String>) document.get("adminForumIds");

                            if (adminForumList != null) {
                                adminForumList.remove(forumId);
                                adminForumIds.put("adminForumIds", adminForumList);
                            }
                        }

                        moderatorData.set(adminForumIds, SetOptions.merge());
                    }
                });
            }
        }

        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, ArrayList<String>> ownedForumIds = new HashMap<>();

                    if (document.exists()) {
                        ArrayList<String> ownedForumList = (ArrayList<String>) document.get("ownedForumIds");

                        if (ownedForumList != null) {
                            ownedForumList.remove(forumId);
                            ownedForumIds.put("ownedForumIds", ownedForumList);
                        }
                    }

                    userData.set(ownedForumIds, SetOptions.merge());
                }
            }
        });

        if (postIds != null) {
            for (String id: postIds) {
                PostView postView = new PostView();
                postView.deletePost(id);
            }
        }
    }

    private void setButton() {
        if (Objects.equals(userId, chiefAdminId)) {
            ownedButton.setVisibility(View.VISIBLE);
            joinButton.setVisibility(View.GONE);
            joinedButton.setVisibility(View.GONE);

        } else if (memberIds.contains(userId) || moderatorIds.contains(userId)) {
            ownedButton.setVisibility(View.GONE);
            joinButton.setVisibility(View.GONE);
            joinedButton.setVisibility(View.VISIBLE);

        } else {
            ownedButton.setVisibility(View.GONE);
            joinButton.setVisibility(View.VISIBLE);
            joinedButton.setVisibility(View.GONE);
        }

        joinForum();
        unJoinForum();
    }

    private void joinForum() {
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, ArrayList<String>> joinedForumIds = new HashMap<>();
                            if (document.exists()) {
                                ArrayList<String> joinedForumList;
                                joinedForumList = (ArrayList<String>) document.get("joinedForumIds");

                                if (joinedForumList != null) {
                                    joinedForumList.add(forumId);
                                } else {
                                    joinedForumList = new ArrayList<>();
                                    joinedForumList.add(forumId);
                                }

                                joinedForumIds.put("joinedForumIds", joinedForumList);
                            }
                            userData.set(joinedForumIds, SetOptions.merge());
                        }
                    }
                });

                forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Map<String, ArrayList<String>> memberIdsList = new HashMap<>();
                            if (document.exists()) {
                                ArrayList<String> memberList;
                                memberList = (ArrayList<String>) document.get("memberIds");

                                if (memberList != null) {
                                    memberList.add(userId);
                                } else {
                                    memberList = new ArrayList<>();
                                    memberList.add(userId);
                                }

                                memberIdsList.put("memberIds", memberList);
                            }
                            userData.set(memberIdsList, SetOptions.merge());
                        }
                    }
                });

                memberIds.add(userId);

                joinButton.setVisibility(View.GONE);
                joinedButton.setVisibility(View.VISIBLE);
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
                    dialogCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialogAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            unJoinFunction();
                            dialog.dismiss();
                            joinButton.setVisibility(View.VISIBLE);
                            joinedButton.setVisibility(View.GONE);
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
        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, ArrayList<String>> forumIds = new HashMap<>();
                    if (document.exists()) {
                        ArrayList<String> joinedForumList, adminForumList;
                        joinedForumList = (ArrayList<String>) document.get("joinedForumIds");
                        adminForumList = (ArrayList<String>) document.get("adminForumIds");

                        if (joinedForumList != null) {
                            joinedForumList.remove(forumId);
                        }

                        if (adminForumList != null) {
                            adminForumList.remove(forumId);
                        }

                        forumIds.put("joinedForumIds", joinedForumList);
                        forumIds.put("adminForumIds", adminForumList);
                    }
                    userData.set(forumIds, SetOptions.merge());
                }
            }
        });

        forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Map<String, ArrayList<String>> memberIds = new HashMap<>();
                    if (document.exists()) {
                        ArrayList<String> memberList, moderatorList;
                        memberList = (ArrayList<String>) document.get("memberIds");
                        moderatorList = (ArrayList<String>) document.get("moderatorIds");

                        if (memberList != null) {
                            memberList.remove(forumId);
                        }

                        if (moderatorList != null) {
                            moderatorList.remove(forumId);
                        }

                        memberIds.put("memberIds", memberList);
                        memberIds.put("moderatorIds", moderatorList);
                    }
                    userData.set(memberIds, SetOptions.merge());
                }
            }
        });
    }

    private void promoteToModerator() {

    }

    private void setupList(ArrayList<Post> postList) {
        adapter = new PostListAdapter(this, R.layout.ui_post_list_view_item, postList);
        postListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}