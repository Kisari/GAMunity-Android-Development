package rmitcom.asm1.gamunity.components.views.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.User;

public class RemoveMemberFromGroupChat extends AppCompatActivity {

    private final String TAG = "Remove user View";
    private final FireBaseManager manager = new FireBaseManager();
    private DocumentReference chatData;
    private TextView returnBackButton;
    private SearchView removeUserSearchBar;
    private RecyclerView removeUserListView;
    private String chatId;
    private ArrayList<String> memberIds, moderatorIds, adminIds, userIds;
    private ArrayList<User> userList;
    private UserRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_member_from_group_chat);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            chatId = getIntent.getExtras().getString("chatId");
            chatData = manager.getDb().collection("CHATROOMS").document(chatId);
        }

        returnBackButton = findViewById(R.id.returnBack);
        removeUserSearchBar = findViewById(R.id.removeUserSearchBar);
        removeUserListView = findViewById(R.id.removeUserSearchList);

        setupAddPage();

        initSearch();

        returnToPreviousPage();
    }

    private void setupAddPage() {
        chatData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    memberIds = new ArrayList<>();
                    moderatorIds = new ArrayList<>();
                    adminIds = new ArrayList<>();

                    if (document.get("adminIds") != null) {
                        adminIds = (ArrayList<String>) document.get("adminIds");
                    }

                    if (document.get("moderatorIds") != null) {
                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");
                    }

                    if (document.get("memberIds") != null) {
                        memberIds = (ArrayList<String>) document.get("memberIds");
                    }

                    CollectionReference userCollection = manager.getDb().collection("users");
                    userCollection.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            userIds = new ArrayList<>();
                            userList = new ArrayList<>();

                            for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                String currUserId = document1.getId();

                                Log.i(TAG, "removeChatUser - adminIds: " + adminIds);
                                Log.i(TAG, "removeChatUser - moderatorIds: " + moderatorIds);
                                Log.i(TAG, "removeChatUser - memberIds: " + memberIds);

                                if (!adminIds.contains(currUserId) && (moderatorIds.contains(currUserId) || memberIds.contains(currUserId))) {
                                    userIds.add(currUserId);

                                    String userName = document1.getString("name");
                                    String userImg = document1.getString("profileImgUri");

                                    User user = new User(currUserId, userName, userImg);
                                    userList.add(user);
                                }
                            }

                            setupList(userList, removeUserListView);
                        }
                    });
                }
            }

        });
    }

    private void initSearch() {
        removeUserSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                            if (!isUserAlreadyAdded(user.getUserId(), users)) {
                                users.add(user);
                            }
                        }
                    }
                }

                setupList(users, removeUserListView);
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

        adapter = new UserRecyclerViewAdapter(this, userList, true, false, true, chatId);
        userListView.setAdapter(adapter);
    }


    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}