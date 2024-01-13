package rmitcom.asm1.gamunity.components.views.forum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.Constant;

public class ForumMoreInfoView extends AppCompatActivity {
    private final String TAG = "Forum More Info View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
    //    private String forumId = "IYvjtX2OyUr5C4DDWS28";
    private String forumId;
//    private String userId = "testUser1";
    private String title, description, chiefAdminId, forumIconUri;
    private ArrayList<String> memberIds, moderatorIds, category;
    private TextView moreInfoTitle, moreInfoDescription, returnBackButton;
    private ShapeableImageView forumIcon;
    private ProgressBar forumIconProgressBar;
    private RecyclerView moreInfoModerators, moreInfoMembers, moreInfoAdmin, moreInfoCategory;
    private Constant constant = new Constant();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_more_info_view);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
            userData = db.collection("USERS").document(userId);
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

        setForumData();
        returnToPreviousPage();
    }

    private void setForumData() {
        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    title = (String) document.get("title");
                    description = (String) document.get("description");
                    chiefAdminId = (String) document.get("chiefAdminId");

                    category = (ArrayList<String>) document.get("category");
                    memberIds = (ArrayList<String>) document.get("memberIds");
                    moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                    forumIconUri = document.getString("forumIcon");

                    if (title != null) {
                        moreInfoTitle.setText(title);
                    }

                    if (description != null) {
                        moreInfoDescription.setText(description);
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

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}