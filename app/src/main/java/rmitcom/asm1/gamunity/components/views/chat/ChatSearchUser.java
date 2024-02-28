package rmitcom.asm1.gamunity.components.views.chat;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.User;

public class ChatSearchUser extends AppCompatActivity {
    private final FireBaseManager manager = new FireBaseManager();
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
        manager.getDb().collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String userId = document.getId();
                    if (!userId.equals(manager.getCurrentUser().getUid())) {
                        String userName = document.getString("name");
                        String userImg = document.getString("profileImgUri");

                        User user = new User(userId, userName, userImg);
                        userList.add(user);
                    }
                }
                setupList(userList, userListView);
            }
        });
    }

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
            if (user.getUserId().equals(manager.getCurrentUser().getUid())) {
                return true;
            }
        }
        return false;
    }

    private void setupList(ArrayList<User> userList, RecyclerView userListView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userListView.setLayoutManager(layoutManager);

        adapter = new UserRecyclerViewAdapter(this, userList, false, true, true, null);
        userListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}