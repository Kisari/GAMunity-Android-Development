package rmitcom.asm1.gamunity.components.views.forum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.model.User;

public class DemoteToMember extends AppCompatActivity {
    private final String TAG = "Demote View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
    private ImageView returnBackButton;
    private SearchView demoteSearchBar;
    private RecyclerView userListView;
    private String forumId;
    private ArrayList<String> memberIds, moderatorIds;
    private ArrayList<User> moderatorList, memberList;
    private UserRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demote_to_member);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = getIntent.getExtras().getString("forumId");
            forumData = db.collection("FORUMS").document(forumId);
        }

        returnBackButton = findViewById(R.id.returnBack);
        demoteSearchBar = findViewById(R.id.demoteSearchBar);
        userListView = findViewById(R.id.demoteModeratorList);

        setPageData();
        returnToPreviousPage();
    }

    private void setPageData() {
        forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {

                        memberIds = new ArrayList<>();
                        moderatorIds = new ArrayList<>();

                        memberList = new ArrayList<>();
                        moderatorList = new ArrayList<>();

                        if (document.get("memberIds") != null) {
                            memberIds = (ArrayList<String>) document.get("memberIds");

                        }

                        if (document.get("moderatorIds") != null) {
                            moderatorIds = (ArrayList<String>) document.get("moderatorIds");
                            if (moderatorIds != null) {
                                displayList(moderatorIds, moderatorList);
                            }
                        }
                    }
                }
            }
        });
    }

    private void displayList(ArrayList<String> userIds, ArrayList<User> userList) {
        int listLength = userIds.size();
        Log.i(TAG, "displayList - listLength: " + listLength);
        AtomicInteger counter = new AtomicInteger(0);

        for (String id: userIds) {
            db.collection("users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String username = "", userProfileImg = "";

                        ArrayList<String> joinedForumIds = new ArrayList<>(), adminForumIds = new ArrayList<>();
                        if (document.exists()) {
                            username = document.getString("name");
                            userProfileImg = document.getString("image");

                            if (document.get("joinedForumIds") != null) {
                                joinedForumIds = (ArrayList<String>) document.get("joinedForumIds");
                            }

                            if (document.get("adminForumIds") != null) {
                                adminForumIds = (ArrayList<String>) document.get("adminForumIds");
                            }
                        }

                        User user = new User(id, username, userProfileImg, adminForumIds, joinedForumIds);
                        userList.add(user);

                        if (counter.incrementAndGet() == listLength) {
                            setupList(userList, userListView);
                            initSearch(userList);
                        }
                    }
                }
            });
        }
    }

    private void initSearch(ArrayList<User> userList) {
        demoteSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<User> users = new ArrayList<>();

                if (newText.isEmpty()) {
                    users.addAll(userList);
                }
                else {
                    for (User user : userList) {
                        if (user.getName().toLowerCase().contains(newText.toLowerCase())) {
                            // Add the user if not already added
                            if (!isUserAlreadyAdded(user.getUserId(), users)) {
                                users.add(user);
                            }
                        }
                    }
                }

                setupList(users, userListView);
                return false;
            }
        });
    }

    private boolean isUserAlreadyAdded(String searchText, ArrayList<User> users) {
        for (User user : users) {
            if (user.getName().equals(searchText)) {
                return true;
            }
        }
        return false;
    }

    private void setupList(ArrayList<User> userList, RecyclerView userListView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userListView.setLayoutManager(layoutManager);

        adapter = new UserRecyclerViewAdapter(this, userList, true, false, false, forumId);
        userListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}