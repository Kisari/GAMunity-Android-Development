package rmitcom.asm1.gamunity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.adapter.PostRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.LoginView;
import rmitcom.asm1.gamunity.components.views.profile.EditProfileView;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Forum;
import rmitcom.asm1.gamunity.model.Post;
import rmitcom.asm1.gamunity.model.User;

public class ProfileView extends AppCompatActivity {
    private final String TAG = "Profile View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final String userId = userAuth.getUid();

    // Define correct profile to be displayed
    private String targetUserId;

    // Firebase data
    private DocumentReference userData, forumData, postData;

    // User data
    private String nameStr, dobStr, noFollowStr, noFollowingStr,
            profileImageUri, profileBackgroundUri;
    private ArrayList<String> followList;
    private ArrayList<String> followingList;

    // Forum data
    private ArrayList<String> forumIds;
    private ArrayList<Forum> forumList;
    private ArrayList<String> postIds;
    private ArrayList<Post> postList;

    // Display data
    private TextView profileName, profileDob, profileFollow, profileFollowing,
            moreOptionButton;
    private RecyclerView postListView, forumListView;
    private ProgressBar backgroundProgressBar, iconProgressBar;
    private ImageView profilePicture, profileBackground;

    private PostRecyclerViewAdapter postAdapter;
    private User currentUser;
    private Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        targetUserId = getIntent().getStringExtra("userId");
        setUI(targetUserId);
    }

    private void setUI(String profileId) {
        Intent getIntent = getIntent();

        if (getIntent != null) {
            userData = db.collection("users").document(profileId);
        }

        Log.i(TAG, "profileId: " + userId);

        profileName = findViewById(R.id.profileName);
        profileDob = findViewById(R.id.profileDOB);
        profileFollow = findViewById(R.id.profileFollow);
        profileFollowing = findViewById(R.id.profileFollowing);

        profilePicture = findViewById(R.id.profilePicture);
        iconProgressBar = findViewById(R.id.profileProgress1);
        profileBackground = findViewById(R.id.profileBackgroundImage);
        backgroundProgressBar = findViewById(R.id.profileProgress2);
        moreOptionButton = findViewById(R.id.profileMoreOption);

        postListView = findViewById(R.id.profilePostList);

        setProfileData();
    }

    @SuppressLint("SetTextI18n")
    private void setProfileData() {
        userData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    nameStr = (String) document.get("name");
                    if (nameStr != null) {
                        profileName.setText(nameStr);
                    }

                    dobStr = (String) document.get("dob");
                    if (dobStr != null) {
                        profileDob.setText(dobStr);
                    }

                    noFollowStr = String.valueOf(((ArrayList<String>) document.get("followersIds")).size()); // Check for error
                    profileFollow.setText(noFollowStr);

                    noFollowingStr = String.valueOf(((ArrayList<String>) document.get("followingIds")).size()); // Check for error
                    profileFollowing.setText(noFollowingStr);

                    profileImageUri = (String) document.get("profileImage");
                    profileBackgroundUri = (String) document.get("profileBackground");
                    Log.i(TAG, "profileImageUri: " + profileImageUri);
                    Log.i(TAG, "profileBackgroundUri: " + profileBackgroundUri);
                    try {
                        new AsyncImage(profilePicture, iconProgressBar).loadImage(profileImageUri);
                        new AsyncImage(profileBackground, backgroundProgressBar).loadImage(profileBackgroundUri);
                    } catch (Exception e) {
                        Log.e(TAG, "setProfileData: ", e);
                        e.printStackTrace();
                    }

                    if (document.get("postIds") != null) {
                        postIds = (ArrayList<String>) document.get("postIds");
                        if (postIds != null) {
                            displayPostList(postIds);
                        }
                    }

                    currentUser = new User(document.getString("userId"), document.getBoolean("isAdmin"),
                            document.getString("name"), document.getString("dob"), document.getString("email"),
                            (ArrayList<String>) document.get("ownedForumIds"), (ArrayList<String>) document.get("adminForumIds"),
                            (ArrayList<String>) document.get("joinedForumIds"), (ArrayList<String>) document.get("postIds"),
                            (ArrayList<String>) document.get("commentIds"), (ArrayList<String>) document.get("followersIds"),
                            (ArrayList<String>) document.get("followingIds"));

                    moreOption();

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void displayPostList(ArrayList<String> postIds) {
        postList = new ArrayList<>();

        final int[] listLength = {postIds.size()};
        AtomicInteger counter = new AtomicInteger(0);

        for (String postId : postIds) {
            Log.i(TAG, "displayList - postId: " + postId);

            db.collection("POSTS").document(postId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    String postTitle, postOwnerId, postForumId, postDescription, timestampStr, updateTimestampStr, imgUri;
                    Date timestamp = new Date(), updateTimestamp = new Date();

                    ArrayList<String> postLikeIds, postDislikeIds, postCommentIds;

                    if (document.exists()) {
                        postTitle = (String) document.get("title");
                        postOwnerId = (String) document.get("ownerId");
                        postForumId = (String) document.get("forumId");

                        postDescription = (String) document.get("description");

                        postLikeIds = (ArrayList<String>) document.get("likeIds");
                        postDislikeIds = (ArrayList<String>) document.get("dislikeIds");
                        postCommentIds = (ArrayList<String>) document.get("commentIds");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                        timestampStr = (String) document.get("date");
                        if (timestampStr != null) {
                            try {
                                timestamp = sdf.parse(timestampStr);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        updateTimestampStr = (String) document.get("updateDate");
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
                        } else {
                            imgUri = null;
                        }

                        Log.i(TAG, "onComplete - postId: " + postId);

                        Post post = new Post(postId, postOwnerId, postForumId, postTitle, postDescription, timestamp, updateTimestamp, imgUri, postCommentIds, postLikeIds, postDislikeIds);

                        Collections.sort(postList, (post1, post2)
                                -> post2.getTimestamp().compareTo(post1.getTimestamp()));

                        int index = Collections.binarySearch(postList, post, (post1, post2)
                                -> post2.getTimestamp().compareTo(post1.getTimestamp()));

                        int insertionPoint = (index < 0) ? -index : index;

                        if (insertionPoint >= postList.size()) {
                            postList.add(post);
                        } else {
                            postList.add(insertionPoint, post);
                        }

                        if (counter.incrementAndGet() == listLength[0]) {
                            setUpPostList(postList);
                        }
                    }
                }
            });
        }
    }

    private void setUpPostList(ArrayList<Post> postList) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        postListView.setLayoutManager(layoutManager);

        postAdapter = new PostRecyclerViewAdapter(this, postList);
        postListView.setAdapter(postAdapter);
    }

    // Refresh UI after editing or deleting a post
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == constant.EDIT) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        }

        if (requestCode == constant.DELETE) {
            if (resultCode == RESULT_OK) {
                recreate();
            }

            if (resultCode == 0) {
                setUI(targetUserId);
            }
        }

        if (requestCode == 107 || requestCode == 108 || requestCode == 109) {
            if (resultCode == RESULT_OK) {
                setUI(targetUserId);
                recreate();
            }
        }
    }

    // Display profile's more option menu
    private void moreOption() {
        PopupMenu popupMenu = new PopupMenu(ProfileView.this, moreOptionButton);

        MenuItem viewForumList = popupMenu.getMenu().findItem(R.id.forumListView);
        MenuItem profileUpdate = popupMenu.getMenu().findItem(R.id.profileUpdate);
        MenuItem profileLogout = popupMenu.getMenu().findItem(R.id.profileLogout);
        MenuItem profileFollow = popupMenu.getMenu().findItem(R.id.profileFollow);
        MenuItem profileUnfollow = popupMenu.getMenu().findItem(R.id.profileUnfollow);


        if (Objects.equals(userId, targetUserId)) {
            viewForumList.setVisible(true);
            profileUpdate.setVisible(true);
            profileLogout.setVisible(true);
            profileFollow.setVisible(false);
            profileUnfollow.setVisible(false);
        } else {
            viewForumList.setVisible(false);
            profileUpdate.setVisible(false);
            profileLogout.setVisible(false);

            if (currentUser.getFollowingIds().contains(targetUserId)) {
                profileFollow.setVisible(false);
                profileUnfollow.setVisible(true);
            } else {
                profileFollow.setVisible(true);
                profileUnfollow.setVisible(false);
            }
        }

        moreOptionButton.setOnClickListener(v -> {
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.forumListView) {
//                    viewForumList();

                } else if (itemId == R.id.profileUpdate) {
                    Intent editIntent = new Intent(ProfileView.this, EditProfileView.class);
                    startActivity(editIntent);

                } else if (itemId == R.id.profileLogout) {
                    userAuth.signOut();
                    Intent newIntent = new Intent(ProfileView.this, LoginView.class);
                    startActivity(newIntent);

                } else if (itemId == R.id.profileFollow) {
                    followUser();

                } else if (itemId == R.id.profileUnfollow) {
                    unfollowUser();
                }

                return false;
            });
            popupMenu.show();
        });
    }

    // Check for error
//    private void displayForumList(ArrayList<String> postIds) {
//        forumList = new ArrayList<>();
//
//        final int[] listLength = {forumIds.size()};
//        AtomicInteger counter = new AtomicInteger(0);
//
//        for (String forumId : forumIds) {
//            Log.i(TAG, "displayList - forumId: " + forumId);
//
//            db.collection("FORUMS").document(forumId).get().addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//
//
//                    String forumRef,
//
//                    private String forumId;
//                    private String forumRef;
//                    private String chiefAdmin;
//                    private String title;
//                    private ArrayList<String> category;
//                    private String description;
//                    private ArrayList<String> moderatorIds;
//                    private ArrayList<String> memberIds;
//                    private long noJoined = 0;
//                    private ArrayList<String> postIds;
//                    private String forumBackground;
//                    private String forumIcon;
//                    private String chatId;
//
//                    if (document.exists()) {
//
//
//
//                        postTitle = (String) document.get("title");
//                        postOwnerId = (String) document.get("ownerId");
//                        postForumId = (String) document.get("forumId");
//
//                        postDescription = (String) document.get("description");
//
//                        postLikeIds = (ArrayList<String>) document.get("likeIds");
//                        postDislikeIds = (ArrayList<String>) document.get("dislikeIds");
//                        postCommentIds = (ArrayList<String>) document.get("commentIds");
//
//                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
//
//                        timestampStr = (String) document.get("date");
//                        if (timestampStr != null) {
//                            try {
//                                timestamp = sdf.parse(timestampStr);
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        updateTimestampStr = (String) document.get("updateDate");
//                        if (updateTimestampStr != null) {
//                            try {
//                                updateTimestamp = sdf.parse(updateTimestampStr);
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            updateTimestamp = null;
//                        }
//
//                        if (document.getString("image") != null) {
//                            imgUri = document.getString("image");
//                        } else {
//                            imgUri = null;
//                        }
//
//                        Log.i(TAG, "onComplete - postId: " + postId);
//
//                        Post post = new Post(postId, postOwnerId, postForumId, postTitle, postDescription, timestamp, updateTimestamp, imgUri, postCommentIds, postLikeIds, postDislikeIds);
//
//                        Collections.sort(postList, (post1, post2)
//                                -> post2.getTimestamp().compareTo(post1.getTimestamp()));
//
//                        int index = Collections.binarySearch(postList, post, (post1, post2)
//                                -> post2.getTimestamp().compareTo(post1.getTimestamp()));
//
//                        int insertionPoint = (index < 0) ? -index : index;
//
//                        if (insertionPoint >= postList.size()) {
//                            postList.add(post);
//                        } else {
//                            postList.add(insertionPoint, post);
//                        }
//
//                        if (counter.incrementAndGet() == listLength[0]) {
//                            setUpForumList(forumList);
//                        }
//                    }
//                }
//            });
//        }
//    }
//
//    private void setUpForumList(ArrayList<Post> postList) {
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        forumListView.setLayoutManager(layoutManager);
//
//        postAdapter = new PostRecyclerViewAdapter(this, postList);
//        forumListView.setAdapter(postAdapter);
//    }
//
//    private void viewForumList() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileView.this);
//        View dialogView = getLayoutInflater().inflate(R.layout.ui_profile_forum_list_popup, null);
//
//        forumListView = findViewById(R.id.profileForumList);
//
//        builder.setView(dialogView);
//
//    }

    private void followUser() { // Check for error
        userData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    followList = (ArrayList<String>) document.get("followersIds");
                    assert followList != null;
                    followList.add(userId);

                    userData.update("followersIds", followList).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.i(TAG, "followUser: " + "Followed");
                        }
                    });

                    followingList = (ArrayList<String>) document.get("followingIds");
                    assert followingList != null;
                    followingList.add(targetUserId);

                    userData.update("followingIds", followingList).addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            Log.i(TAG, "followUser: " + "Following");
                        }
                    });
                }
            }
        });
    }

    private void unfollowUser() { // Check for error
        userData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    followList = (ArrayList<String>) document.get("followersIds");
                    assert followList != null;
                    followList.remove(userId);

                    userData.update("followersIds", followList).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.i(TAG, "unfollowUser: " + "Unfollowed");
                        }
                    });

                    followingList = (ArrayList<String>) document.get("followingIds");
                    assert followingList != null;
                    followingList.remove(targetUserId);

                    userData.update("followingIds", followingList).addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            Log.i(TAG, "unfollowUser: " + "Unfollowing");
                        }
                    });
                }
            }
        });
    }
}

