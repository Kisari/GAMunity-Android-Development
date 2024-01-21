package rmitcom.asm1.gamunity.components.views.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.model.User;

public class RemoveMemberFromGroupChat extends AppCompatActivity {

    private final String TAG = "Remove user View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, chatData, userData;
    private TextView returnBackButton;
    private SearchView removeUserSearchBar;
    private RecyclerView removeUserListView;
    private String chatId;
    private ArrayList<String> memberIds, moderatorIds, adminIds, userIds;
    private ArrayList<User> adminList, moderatorList, memberList, userList;
    private UserRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_member_from_group_chat);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            chatId = getIntent.getExtras().getString("chatId");
            chatData = db.collection("CHATROOMS").document(chatId);
        }

        returnBackButton = findViewById(R.id.returnBack);
        removeUserSearchBar = findViewById(R.id.removeUserSearchBar);
        removeUserListView = findViewById(R.id.removeUserSearchList);

        setupAddPage();

        initSearch();

        returnToPreviousPage();
    }

    private void setupAddPage() {
        chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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

                        CollectionReference userCollection = db.collection("users");
                        userCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    userIds = new ArrayList<>();
                                    userList = new ArrayList<>();

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String currUserId = document.getId();

                                        Log.i(TAG, "removeChatUser - adminIds: " + adminIds);
                                        Log.i(TAG, "removeChatUser - moderatorIds: " + moderatorIds);
                                        Log.i(TAG, "removeChatUser - memberIds: " + memberIds);

                                        if (!adminIds.contains(currUserId) && (moderatorIds.contains(currUserId) || memberIds.contains(currUserId))) {
                                            userIds.add(currUserId);

                                            String userName = document.getString("name");
                                            String userImg = document.getString("profileImgUri");

                                            User user = new User(currUserId, userName, userImg);
                                            userList.add(user);
                                        }
                                    }

                                    setupList(userList, removeUserListView);
                                }
                            }
                        });
                    }
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
            if (user.getUserId().equals(userId)) {
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