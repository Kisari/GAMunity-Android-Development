package rmitcom.asm1.gamunity.components.views.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.model.User;

public class ChatSearchUser extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String currUserId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private TextView returnBackButton;
    private SearchView chatUserSearchBar;
    private RecyclerView userListView;
    private UserRecyclerViewAdapter adapter;
    private ArrayList<User> userList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_search_user);

        setUI();
    }

    private void setUI() {
        returnBackButton = findViewById(R.id.returnBack);
        chatUserSearchBar = findViewById(R.id.userSearchBar);
        userListView = findViewById(R.id.userSearchList);

        setPageData();

        initSearch();

        returnToPreviousPage();
    }

    private void setPageData() {
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    userList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getId();
                        if (!userId.equals(currUserId)) {
                            String userName = document.getString("name");
                            String userImg = document.getString("profileImgUri");

                            User user = new User(userId, userName, userImg);
                            userList.add(user);
                        }
                    }
                    setupList(userList, userListView);
                }
            }
        });
    }

//    private void initList() {
//        this.adapter = new UserRecyclerViewAdapter(this, userList, false, false, null);
//        userListView.setAdapter(adapter);
//    }

    private void initSearch() {
        chatUserSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

                setupList(users, userListView);
                return false;
            }
        });
    }

    private boolean isUserAlreadyAdded(String searchText, ArrayList<User> users) {
        for (User user : users) {
            if (user.getUserId().equals(currUserId)) {
                return true;
            }
        }
        return false;
    }

    private void setupList(ArrayList<User> userList, RecyclerView userListView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userListView.setLayoutManager(layoutManager);

        adapter = new UserRecyclerViewAdapter(this, userList, false, false, null);
        userListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}