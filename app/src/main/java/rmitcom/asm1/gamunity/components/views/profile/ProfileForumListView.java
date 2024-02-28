package rmitcom.asm1.gamunity.components.views.profile;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ProfileForumListAdapter;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Forum;

public class ProfileForumListView extends AppCompatActivity implements FirebaseFetchAndSetUI {
    private final FireBaseManager manager = new FireBaseManager();
    private final Constant constant = new Constant();
    private final ArrayList<Forum> forumList= new ArrayList<>();
    private ProfileForumListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_forum_list_view);

        fetchData();
    }

    @Override
    public void fetchData() {
        manager.getDb().collection(constant.forums)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String forumId = document.getString("forumId");
                            String forumRef = document.getId();
                            String forumTitle = document.getString("title");
                            String forumChiefAdmin = Objects.requireNonNull(document.getString("chiefAdmin"));
                            ArrayList<String> forumCategory = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("category")));
                            ArrayList<String> forumMemberIds = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("memberIds")));
                            ArrayList<String> moderatorIds = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("moderatorIds")));
                            String forumBackground = document.getString("forumBackground");
                            String forumIcon = document.getString("forumIcon");

                            Forum newForum = new Forum(forumId, forumRef, forumChiefAdmin, forumTitle, forumCategory, forumMemberIds, moderatorIds, forumBackground, forumIcon);
                            if(moderatorIds.contains(manager.getCurrentUser().getUid()) || forumMemberIds.contains(manager.getCurrentUser().getUid()) || forumChiefAdmin.equals(manager.getCurrentUser().getUid())){
                                forumList.add(newForum);
                            }
                        }
                        setUI();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    @Override
    public void setUI() {
        ListView profileForumList = findViewById(R.id.profileForumList);

        this.adapter = new ProfileForumListAdapter(forumList);

        profileForumList.setAdapter(adapter);

        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> finish());
    }
}