package rmitcom.asm1.gamunity.components.views.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.User;

public class ChatMoreInfo extends AppCompatActivity {
    private final String TAG = "Chat More Info View";
    private final FireBaseManager manager = new FireBaseManager();
    private DocumentReference chatData, userData;
    private String chatId;
    private UserRecyclerViewAdapter adapter;
    private String title, chatIconUri;
    private ArrayList<String> adminIds, memberIds, moderatorIds;
    private ArrayList<User> adminList, moderatorList, memberList;
    private TextView moreInfoTitle, returnBackButton, adminText, moderatorText, memberText;
    private ShapeableImageView chatIcon;
    private ProgressBar chatIconProgressBar;
    private RecyclerView moreInfoModerators, moreInfoMembers, moreInfoAdmins;
    private LinearLayout admin, moderator, member;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_more_info);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            chatId = (String) Objects.requireNonNull(getIntent.getExtras()).get("chatId");
            chatData = manager.getDb().collection("CHATROOMS").document(chatId);
            Log.i(TAG, "chatMoreInfo - chatId: " + chatId);
        }

        setUI();

    }

    private void setUI() {
        moreInfoTitle = findViewById(R.id.moreInfoTitle);
        chatIcon = findViewById(R.id.moreInfoIconImage);
        chatIconProgressBar = findViewById(R.id.moreInfoProgressBar1);

        moreInfoAdmins = findViewById(R.id.moreInfoAdminList);
        moreInfoModerators = findViewById(R.id.moreInfoModeratorList);
        moreInfoMembers = findViewById(R.id.moreInfoMemberList);
        returnBackButton = findViewById(R.id.returnBack);

        admin = findViewById(R.id.admin);
        moderator = findViewById(R.id.moderator);
        member = findViewById(R.id.member);

        adminText = findViewById(R.id.adminText);
        moderatorText = findViewById(R.id.moderatorText);
        memberText = findViewById(R.id.memberText);

        setForumData();
        returnToPreviousPage();
    }

    @SuppressLint("SetTextI18n")
    private void setForumData() {
        chatData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    title = (String) document.get("chatTitle");
                    chatIconUri = document.getString("chatImg");

                    if (title != null) {
                        moreInfoTitle.setText(title);
                    }

                    adminIds = new ArrayList<>();
                    moderatorIds = new ArrayList<>();
                    memberIds = new ArrayList<>();

                    adminList = new ArrayList<>();
                    moderatorList = new ArrayList<>();
                    memberList = new ArrayList<>();

                    if (document.get("adminIds") != null) {
                        adminIds = (ArrayList<String>) document.get("adminIds");
                        Log.i(TAG, "chatMoreInfo - adminIds: " + adminIds);
                        if (adminIds != null) {
                            if (!adminIds.isEmpty()) {
                                admin.setVisibility(View.VISIBLE);
                                adminText.setText("Admin - " + adminIds.size());
                                displayList(adminIds, adminList, moreInfoAdmins);
                            }
                            else {
                                admin.setVisibility(View.GONE);
                            }
                        }
                        else {
                            admin.setVisibility(View.GONE);
                        }
                    }
                    else {
                        admin.setVisibility(View.GONE);
                    }

                    if (document.get("moderatorIds") != null) {
                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");
                        Log.i(TAG, "chatMoreInfo - moderatorIds: " + moderatorIds);
                        if (moderatorIds != null) {
                            if (!moderatorIds.isEmpty()) {
                                moderator.setVisibility(View.VISIBLE);
                                moderatorText.setText("Moderator - " + moderatorIds.size());
                                displayList(moderatorIds, moderatorList, moreInfoModerators);
                            }
                            else {
                                moderator.setVisibility(View.GONE);
                            }
                        }
                        else {
                            moderator.setVisibility(View.GONE);
                        }
                    }
                    else {
                        moderator.setVisibility(View.GONE);
                    }

                    if (document.get("memberIds") != null) {
                        memberIds = (ArrayList<String>) document.get("memberIds");
                        Log.i(TAG, "chatMoreInfo - memberIds: " + memberIds);
                        if (memberIds != null) {
                            if (!memberIds.isEmpty()) {
                                member.setVisibility(View.VISIBLE);
                                memberText.setText("Member - " + memberIds.size());
                                displayList(memberIds, memberList, moreInfoMembers);
                            }
                            else {
                                member.setVisibility(View.GONE);
                            }
                        }
                        else {
                            member.setVisibility(View.GONE);
                        }
                    }
                    else {
                        member.setVisibility(View.GONE);
                    }

                    try {
                        new AsyncImage(chatIcon, chatIconProgressBar).loadImage(chatIconUri);
                    } catch (Exception e){
                        Log.e(TAG, "getView: ", e);
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void displayList(ArrayList<String> userIds, ArrayList<User> userList, RecyclerView userListView) {
        int listLength = userIds.size();
        Log.i(TAG, "displayList - listLength: " + listLength);
        AtomicInteger counter = new AtomicInteger(0);

        for (String userId: userIds) {
            manager.getDb().collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    String username = "", userProfileImg = "";
                    if (document.exists()) {
                        username = document.getString("name");
                        userProfileImg = document.getString("profileImgUri");
                    }

                    User user = new User(userId, username, userProfileImg);
                    userList.add(user);

                    if (counter.incrementAndGet() == listLength) {
                        setupList(userList, userListView);
                    }
                }
            });
        }
    }

    private void setupList(ArrayList<User> userList, RecyclerView userListView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userListView.setLayoutManager(layoutManager);

        adapter = new UserRecyclerViewAdapter(this, userList, false, false, true, chatId);
        userListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}