package rmitcom.asm1.gamunity.components.views.forum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.CommentRecyclerViewAdapter;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Forum;
import rmitcom.asm1.gamunity.model.User;

public class ForumMoreInfoView extends AppCompatActivity {
    private final String TAG = "Forum More Info View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData;
    private String forumId;
    private UserRecyclerViewAdapter adapter;
    private String title, description, chiefAdminId, forumIconUri;
    private ArrayList<String> adminId, memberIds, moderatorIds, category;
    private ArrayList<User> adminList, moderatorList, memberList;
    private TextView moreInfoTitle, moreInfoDescription, returnBackButton, moreInfoCategory, adminText, moderatorText, memberText;
    private ShapeableImageView forumIcon;
    private ProgressBar forumIconProgressBar;
    private RecyclerView moreInfoModerators, moreInfoMembers, moreInfoAdmin;
    private LinearLayout admin, moderator, member;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_more_info_view);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
        }

        setUI();
    }

    private void setUI() {
        moreInfoTitle = findViewById(R.id.moreInfoTitle);
        moreInfoDescription = findViewById(R.id.moreInfoDescription);
        moreInfoCategory = findViewById(R.id.moreInfoCategory);
        moreInfoAdmin = findViewById(R.id.moreInfoAdmin);
        moreInfoModerators = findViewById(R.id.moreInfoModeratorList);
        moreInfoMembers = findViewById(R.id.moreInfoMemberList);
        returnBackButton = findViewById(R.id.returnBack);

        forumIcon = findViewById(R.id.moreInfoIconImage);
        forumIconProgressBar = findViewById(R.id.moreInfoProgressBar1);

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
        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    title = (String) document.get("title");
                    description = (String) document.get("description");

                    forumIconUri = document.getString("forumIcon");

                    if (title != null) {
                        moreInfoTitle.setText(title);
                    }

                    if (description != null) {
                        moreInfoDescription.setText(description);
                    }

                    if (document.get("category") != null) {
                        category = (ArrayList<String>) document.get("category");

                        if (category != null) {
                            StringBuilder categoryStr = new StringBuilder();
                            if (category != null) {
                                for (String cate: category) {
                                    categoryStr.append("#").append(cate).append(" ");
                                }
                                moreInfoCategory.setText(categoryStr);
                            }
                        }
                    }

                    adminId = new ArrayList<>();
                    moderatorIds = new ArrayList<>();
                    memberIds = new ArrayList<>();

                    adminList = new ArrayList<>();
                    moderatorList = new ArrayList<>();
                    memberList = new ArrayList<>();

                    if (document.getString("chiefAdmin") != null) {
                        chiefAdminId = document.getString("chiefAdmin");
                        adminId.add(chiefAdminId);
                        adminText.setText("Admin - " + adminId.size());
                        displayList(adminId, adminList, moreInfoAdmin);
                    }

                    if (document.get("moderatorIds") != null) {
                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");
                        if (moderatorIds != null) {
                            if (!moderatorIds.isEmpty()) {
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
                        new AsyncImage(forumIcon, forumIconProgressBar).loadImage(forumIconUri);
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
            db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
                }
            });
        }
    }

    private void setupList(ArrayList<User> userList, RecyclerView userListView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        userListView.setLayoutManager(layoutManager);

        adapter = new UserRecyclerViewAdapter(this, userList, false, false, false, forumId);
        userListView.setAdapter(adapter);
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}