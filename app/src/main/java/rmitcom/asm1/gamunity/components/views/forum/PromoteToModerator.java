package rmitcom.asm1.gamunity.components.views.forum;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.User;

public class PromoteToModerator extends AppCompatActivity {
    private final String TAG = "Promote View";
    private final FireBaseManager manager = new FireBaseManager();
    private DocumentReference forumData;
    private ImageView returnBackButton;
    private SearchView promoteSearchBar;
    private RecyclerView userListView;
    private String forumId;
    private ArrayList<String> memberIds, moderatorIds;
    private ArrayList<User> moderatorList, memberList;
    private UserRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote_to_moderator);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            forumId = getIntent.getExtras().getString("forumId");
            forumData = manager.getDb().collection("FORUMS").document(forumId);
        }

        returnBackButton = findViewById(R.id.returnBack);
        promoteSearchBar = findViewById(R.id.promoteSearchBar);
        userListView = findViewById(R.id.promoteMemberList);

        setPageData();
        returnToPreviousPage();
    }
    private void setPageData() {
        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                DocumentSnapshot document = task.getResult();

                if (document.exists()) {

                    memberIds = new ArrayList<>();
                    moderatorIds = new ArrayList<>();

                    memberList = new ArrayList<>();
                    moderatorList = new ArrayList<>();

                    if (document.get("memberIds") != null) {
                        memberIds = (ArrayList<String>) document.get("memberIds");
                        if (memberIds != null) {
                            displayList(memberIds, memberList);
                        }
                    }

                    if (document.get("moderatorIds") != null) {
                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");
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
            manager.getDb().collection("users").document(id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    String username = "", userProfileImg = "";

                    ArrayList<String> joinedForumIds = new ArrayList<>(), adminForumIds = new ArrayList<>();
                    if (document.exists()) {
                        username = document.getString("name");
                        userProfileImg = document.getString("profileImgUri");

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
            });
        }
    }

    private void initSearch(ArrayList<User> userList) {
        promoteSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            if (user.getUserId().equals(manager.getCurrentUser().getUid())) {
                return true;
            }
        }
        return false;
    }

    private void setupList(ArrayList<User> userList, RecyclerView userListView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userListView.setLayoutManager(layoutManager);

        adapter = new UserRecyclerViewAdapter(this, userList, true, true, false, forumId);
        userListView.setAdapter(adapter);
    }


    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}