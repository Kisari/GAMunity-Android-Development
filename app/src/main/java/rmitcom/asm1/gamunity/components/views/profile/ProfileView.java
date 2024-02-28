package rmitcom.asm1.gamunity.components.views.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.User;

public class ProfileView extends AppCompatActivity {
    private final String TAG = "Profile View";

    private final FireBaseManager manager = new FireBaseManager();

    private String targetUserId;

    private String nameStr, dobStr, noFollowStr, noFollowingStr, emailStr,
            profileImageUri, profileBackgroundUri;
    private ArrayList<String> followList;

    // Display data
    private TextView profileName, profileDob, profileFollow, profileFollowing, profileEmail;
    private ProgressBar backgroundProgressBar, iconProgressBar;
    private ImageView profilePicture, profileBackground, backBtn;
    private Button profileActionFollow, profileActionUnFollow;
    private User profileUser;

    private String targetProfileId;

    private String profileUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        Intent getIntent = getIntent();
        targetUserId = getIntent.getStringExtra("userId");
        setUI(targetUserId);
    }

    private void setUI(String profileId) {
        targetProfileId = profileId;

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileDob = findViewById(R.id.profileDOB);
        profileFollow = findViewById(R.id.profileFollow);
        profileFollowing = findViewById(R.id.profileFollowing);
        profileActionFollow = findViewById(R.id.profileActionFollow);
        profileActionUnFollow = findViewById(R.id.profileActionUnFollow);

        profilePicture = findViewById(R.id.profilePicture);
        iconProgressBar = findViewById(R.id.profileProgress1);
        profileBackground = findViewById(R.id.profileBackgroundImage);
        backgroundProgressBar = findViewById(R.id.profileProgress2);
        backBtn = findViewById(R.id.backBtn);

        setProfileData();

        backBtn.setOnClickListener(v -> finish());
    }

    @SuppressLint("SetTextI18n")
    private void setProfileData() {
        manager.getDb().collection("users")
                .whereEqualTo("userId", targetProfileId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                nameStr = document.getString("name");
                                if (nameStr != null) {
                                    profileName.setText(nameStr);
                                }

                                dobStr = document.getString("dob");
                                if (dobStr != null) {
                                    profileDob.setText(dobStr);
                                }

                                emailStr = document.getString("email");
                                if(emailStr != null){
                                    profileEmail.setText(emailStr);
                                }

                                if(document.get("followersIds") != null){
                                    noFollowStr = String.valueOf(((ArrayList<String>) Objects.requireNonNull(document.get("followersIds"))).size());
                                    profileFollow.setText(noFollowStr);
                                }
                                else{
                                    noFollowStr = "0";
                                    profileFollow.setText(noFollowStr);
                                }

                                if(document.get("followingIds") != null){
                                    noFollowingStr = String.valueOf(((ArrayList<String>) Objects.requireNonNull(document.get("followingIds"))).size());
                                    profileFollowing.setText(noFollowingStr);
                                }
                                else{
                                    noFollowingStr = "0";
                                    profileFollowing.setText(noFollowingStr);
                                }

                                profileImageUri = document.getString("profileImgUri");
                                profileBackgroundUri = document.getString("backgroundImgUri");

                                try {
                                    new AsyncImage(profilePicture, iconProgressBar).loadImage(profileImageUri);
                                    new AsyncImage(profileBackground, backgroundProgressBar).loadImage(profileBackgroundUri);
                                } catch (Exception e) {
                                    Log.e(TAG, "setProfileData: ", e);
                                    e.printStackTrace();
                                }

                                profileUser = new User(document.getString("userId"), document.getBoolean("isAdmin"),
                                        document.getString("name"), document.getString("dob"), document.getString("email"),
                                        (ArrayList<String>) document.get("ownedForumIds"), (ArrayList<String>) document.get("adminForumIds"),
                                        (ArrayList<String>) document.get("joinedForumIds"), (ArrayList<String>) document.get("postIds"),
                                        (ArrayList<String>) document.get("commentIds"), (ArrayList<String>) document.get("followersIds"),
                                        (ArrayList<String>) document.get("followingIds"));

                                profileUserRef = document.getId();

                                manager.getDb().collection("users")
                                        .whereEqualTo("userId", manager.getCurrentUser().getUid())
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            for(QueryDocumentSnapshot queryDocumentSnapshot: task1.getResult()){
                                                if(queryDocumentSnapshot.get("followingIds") != null){
                                                    ArrayList<String> currentUserFollowingList = (ArrayList<String>) queryDocumentSnapshot.get("followingIds");

                                                    assert currentUserFollowingList != null;
                                                    if(currentUserFollowingList.contains(profileUser.getUserId())){
                                                        profileActionFollow.setVisibility(View.GONE);
                                                        profileActionUnFollow.setVisibility(View.VISIBLE);
                                                    }
                                                    else{
                                                        profileActionFollow.setVisibility(View.VISIBLE);
                                                        profileActionUnFollow.setVisibility(View.GONE);
                                                    }
                                                }
                                                else{
                                                    profileActionFollow.setVisibility(View.VISIBLE);
                                                    profileActionUnFollow.setVisibility(View.GONE);
                                                }
                                            }
                                        });

                                profileActionFollow.setOnClickListener(v -> followUser());

                                profileActionUnFollow.setOnClickListener(v -> unfollowUser());
                            }
                        }
                    }

        });
    }

    private void followUser() { // Check for error
        manager.getDb().collection("users").document(profileUserRef).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    String[] newFollowList;
                    if(document.get("followersIds") != null){

                        followList = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("followersIds")));

                        newFollowList = new String[followList.size()+1];
                        for (int i = 0; i < followList.size(); i++) {
                            newFollowList[i] = followList.get(i);
                        }
                        newFollowList[followList.size()] = manager.getCurrentUser().getUid();
                    }
                    else{
                        newFollowList = new String[] {manager.getCurrentUser().getUid()};
                    }

                    Map<String, Object> newFollowersList = new HashMap<>();
                    newFollowersList.put("followersIds", Arrays.asList(newFollowList));

                    manager.getDb().collection("users").document(profileUserRef)
                            .set(newFollowersList, SetOptions.merge())
                            .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Log.i(TAG, "Add the followerId to current profile: ");
                            }
                    });

                    manager.getDb().collection("users")
                            .whereEqualTo("userId", manager.getCurrentUser().getUid())
                            .get()
                            .addOnCompleteListener(task12 -> {
                                if(task12.isSuccessful()){
                                    String[] newFollowingList;
                                    for (QueryDocumentSnapshot returnDocument: task12.getResult()){
                                        if(returnDocument.get("followingIds") != null){
                                            ArrayList<String> currentUserFollowingList = new ArrayList<>((List<String>) Objects.requireNonNull(returnDocument.get("followingIds")));

                                            newFollowingList = new String[currentUserFollowingList.size()+1];
                                            for (int i = 0; i < currentUserFollowingList.size(); i++) {
                                                newFollowingList[i] = currentUserFollowingList.get(i);
                                            }
                                            newFollowingList[currentUserFollowingList.size()] = targetProfileId;

                                        }else{
                                            newFollowingList = new String[] {targetProfileId};
                                        }

                                        Map<String, Object> newObject = new HashMap<>();
                                        newObject.put("followingIds", Arrays.asList(newFollowingList));

                                        manager.getDb().collection("users")
                                                .document(returnDocument.getId()).set(newObject, SetOptions.merge())
                                                .addOnCompleteListener(task13 -> {
                                                    recreate();
                                                    Log.i(TAG, "Add the followerId to current user: ");
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }

    private void unfollowUser() { // Check for error
        manager.getDb().collection("users").document(profileUserRef).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {

                    followList = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("followersIds")));
                    String[] newFollowList = removeItemFromArray(followList.toArray(new String[0]), manager.getCurrentUser().getUid());

                    Map<String, Object> newFollowersList = new HashMap<>();
                    newFollowersList.put("followersIds", Arrays.asList(newFollowList));

                    manager.getDb().collection("users").document(profileUserRef)
                            .set(newFollowersList, SetOptions.merge())
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.i(TAG, "Remove the followerId to current profile: ");
                                }
                    });


                    manager.getDb().collection("users")
                            .whereEqualTo("userId", manager.getCurrentUser().getUid())
                            .get()
                            .addOnCompleteListener(task12 -> {
                                if(task12.isSuccessful()){
                                    String[] newFollowingList;
                                    for (QueryDocumentSnapshot returnDocument: task12.getResult()){
                                        ArrayList<String> currentUserFollowingList = new ArrayList<>((List<String>) Objects.requireNonNull(returnDocument.get("followingIds")));
                                        newFollowingList = removeItemFromArray(currentUserFollowingList.toArray(new String[0]), targetProfileId);

                                        Map<String, Object> newObject = new HashMap<>();
                                        newObject.put("followingIds", Arrays.asList(newFollowingList));

                                        manager.getDb().collection("users")
                                                .document(returnDocument.getId()).set(newObject, SetOptions.merge())
                                                .addOnCompleteListener(task13 -> {
                                                    recreate();
                                                    Log.i(TAG, "Remove the followerId to current user: ");
                                                });
                                    }
                                }
                            });

                }
            }
        });
    }

    public static String[] removeItemFromArray(String[] input, String item) {
        if (input == null) {
            return null;
        } else if (input.length == 0) {
            return input;
        } else {
            String[] output = new String[input.length - 1];
            int count = 0;
            for (String i : input) {
                if (!i.equals(item)) {
                    output[count++] = i;
                }
            }
            return output;
        }
    }

}

