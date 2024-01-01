package rmitcom.asm1.gamunity.components.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;

public class ForumMoreInfoView extends AppCompatActivity {
    private final String TAG = "Forum More Info View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
//    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
//    private String forumId = "IYvjtX2OyUr5C4DDWS28";
    private String forumId;
    private String userId = "testUser1";
    private String title, description, chiefAdminId;
    private ArrayList<String> memberIds, moderatorIds, category;
    private TextView moreInfoTitle, moreInfoDescription, moreInfoCategory, returnBackButton;
    private ListView moreInfoAdmins, moreInfoMembers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_more_info_view);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
//            userData = db.collection("USERS").document(userId);
        }

        setUI();
    }

    private void setUI() {
        moreInfoTitle = findViewById(R.id.moreInfoTitle);
        moreInfoDescription = findViewById(R.id.moreInfoDescription);
        moreInfoCategory = findViewById(R.id.moreInfoCategory);
        moreInfoAdmins = findViewById(R.id.moreInfoAdminList);
        moreInfoMembers = findViewById(R.id.moreInfoMemberList);
        returnBackButton = findViewById(R.id.returnBack);

        setForumData();
        returnToPreviousPage();
    }

    private void setForumData() {
        forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        title = (String) document.get("title");
                        description = (String) document.get("description");
                        chiefAdminId = (String) document.get("chiefAdminId");

                        category = (ArrayList<String>) document.get("category");
                        memberIds = (ArrayList<String>) document.get("memberIds");
                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                        if (title != null) {
                            moreInfoTitle.setText(title);
                        }

                        if (description != null) {
                            moreInfoDescription.setText(description);
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}