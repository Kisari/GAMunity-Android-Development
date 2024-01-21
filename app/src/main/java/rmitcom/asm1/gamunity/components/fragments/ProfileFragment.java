package rmitcom.asm1.gamunity.components.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import rmitcom.asm1.gamunity.adapter.PostRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.HomeView;
import rmitcom.asm1.gamunity.components.views.LoginView;
import rmitcom.asm1.gamunity.components.views.profile.EditProfileView;
import rmitcom.asm1.gamunity.components.views.profile.ProfileForumListView;
import rmitcom.asm1.gamunity.components.views.profile.ProfileView;
import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.User;

public class ProfileFragment extends Fragment implements FirebaseFetchAndSetUI {
    private final FireBaseManager db = new FireBaseManager();
    private Constant constant = new Constant();

    private String nameStr, dobStr, noFollowStr, noFollowingStr, emailStr,
            profileImageUri, profileBackgroundUri;
    private TextView profileName, profileDob, profileFollow, profileFollowing, profileEmail,
            moreOptionButton;
    private RecyclerView postListView, forumListView;
    private ProgressBar backgroundProgressBar, iconProgressBar;
    private ImageView profilePicture, profileBackground;

    private PostRecyclerViewAdapter postAdapter;

    private User currentUser;

    View currentView;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        currentView = view;


        profileName = currentView.findViewById(R.id.profileName);
        profileDob = currentView.findViewById(R.id.profileDOB);
        profileFollow = currentView.findViewById(R.id.profileFollow);
        profileFollowing = currentView.findViewById(R.id.profileFollowing);
        profileEmail = currentView.findViewById(R.id.profileEmail);

        profilePicture = currentView.findViewById(R.id.profilePicture);
        iconProgressBar = currentView.findViewById(R.id.profileProgress1);
        profileBackground = currentView.findViewById(R.id.profileBackgroundImage);
        backgroundProgressBar = currentView.findViewById(R.id.profileProgress2);
        moreOptionButton = currentView.findViewById(R.id.profileMoreOption);

        fetchData();

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == constant.PROFILE_REQUEST) {
            if (resultCode == constant.EDIT) {
                Intent resultIntent = new Intent();
                User newUserData = (User) resultIntent.getSerializableExtra("updatedInfo");

                assert newUserData != null;
                profileName.setText(newUserData.getName());
                profileDob.setText(newUserData.getDob());
                new AsyncImage(profilePicture, iconProgressBar).loadImage(newUserData.getProfileImgUri());
                new AsyncImage(profileBackground, backgroundProgressBar).loadImage(newUserData.getBackgroundImgUri());
            }
        }

    }

    @Override
    public void fetchData() {
        db.getDb().collection("users")
                .whereEqualTo("userId", db.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document: task.getResult()){
                            nameStr = (String) document.get("name");
                            if (nameStr != null) {
                                profileName.setText(nameStr);
                            }

                            emailStr = (String) document.get("email");
                            if (emailStr != null) {
                                profileEmail.setText(emailStr);
                            }

                            dobStr = (String) document.get("dob");
                            if (dobStr != null) {
                                profileDob.setText(dobStr);
                            }

                            if(document.get("followersIds") != null){
                                noFollowStr = String.valueOf(((ArrayList<String>) document.get("followersIds")).size());
                                profileFollow.setText(noFollowStr);
                            }
                            else{
                                noFollowStr = "0";
                                profileFollow.setText(noFollowStr);
                            }

                            if(document.get("followingIds") != null){
                                noFollowingStr = String.valueOf(((ArrayList<String>) document.get("followingIds")).size());
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

                            currentUser = new User(document.getString("userId"), document.getBoolean("isAdmin"),
                                    document.getString("name"), document.getString("dob"), document.getString("email"),
                                    (ArrayList<String>) document.get("ownedForumIds"), (ArrayList<String>) document.get("adminForumIds"),
                                    (ArrayList<String>) document.get("joinedForumIds"), (ArrayList<String>) document.get("postIds"),
                                    (ArrayList<String>) document.get("commentIds"), (ArrayList<String>) document.get("followersIds"),
                                    (ArrayList<String>) document.get("followingIds"));

                            setUI();
                        }
                    }
                });
    }

    @Override
    public void setUI() {
        moreOption();

//        Intent intent = new Intent(currentView.getContext(), ProfileView.class);
//        intent.putExtra("userId", db.getCurrentUser().getUid());
//        startActivityForResult(intent, constant.PROFILE_REQUEST);
    }

    private void moreOption() {
        PopupMenu popupMenu = new PopupMenu(currentView.getContext(), moreOptionButton);
        popupMenu.getMenuInflater().inflate(R.menu.profile_more_option, popupMenu.getMenu());

        MenuItem viewForumList = popupMenu.getMenu().findItem(R.id.forumListView);
        MenuItem profileUpdate = popupMenu.getMenu().findItem(R.id.profileUpdate);
        MenuItem profileLogout = popupMenu.getMenu().findItem(R.id.profileLogout);

        viewForumList.setVisible(true);
        profileUpdate.setVisible(true);
        profileLogout.setVisible(true);

        moreOptionButton.setOnClickListener(v -> {
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.forumListView) {
//                    viewForumList();
                    Intent userForumList = new Intent(currentView.getContext(), ProfileForumListView.class);
                    userForumList.putExtra("userId", db.getCurrentUser().getUid());
                    startActivity(userForumList);

                } else if (itemId == R.id.profileUpdate) {
                    Intent editIntent = new Intent(currentView.getContext(), EditProfileView.class);
                    startActivityForResult(editIntent, constant.PROFILE_REQUEST);

                } else if (itemId == R.id.profileLogout) {
                    db.getAuthProvider().signOut();
                    Intent newIntent = new Intent(currentView.getContext(), LoginView.class);
                    startActivity(newIntent);
                    ((HomeView) currentView.getContext()).finish();
                }
                return false;
            });
            popupMenu.show();
        });
    }
}