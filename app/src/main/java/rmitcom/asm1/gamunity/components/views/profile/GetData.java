package rmitcom.asm1.gamunity.components.views.profile;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GetData {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference userData;
    private Map<String, Map<String, ArrayList<String>>> infoData;
    private Map<String, ArrayList<String>> forum_posts;
    private ArrayList<String> posts;
    private ArrayList<String> forumIds, postIds, adminForum, moderatorForum, memberForum;

    private void getData() {
        userData = db.collection("users").document(userId);

        infoData = new HashMap<>();
        forum_posts = new HashMap<>();

        forumIds = new ArrayList<>();
        adminForum = new ArrayList<>();
        moderatorForum = new ArrayList<>();
        memberForum = new ArrayList<>();

        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {

                        if (document.get("ownedForumIds") != null) {
                            adminForum = (ArrayList<String>) document.get("ownedForumIds");

                            Map<String, ArrayList<String>> forum_postIds = new HashMap<>();

                            if (adminForum != null) {
                                forumIds.addAll(adminForum);

                                for (String id: adminForum) {
                                    ArrayList<String> postIds = new ArrayList<>();
                                    forum_postIds.put(id, postIds);
                                }

                                infoData.put("admin", forum_postIds);
                            }
                        }

                        if (document.get("adminForumIds") != null) {
                            moderatorForum = (ArrayList<String>) document.get("adminForumIds");

                            Map<String, ArrayList<String>> forum_postIds = new HashMap<>();

                            if (moderatorForum != null) {
                                forumIds.addAll(moderatorForum);

                                for (String id: moderatorForum) {
                                    ArrayList<String> postIds = new ArrayList<>();
                                    forum_postIds.put(id, postIds);
                                }

                                infoData.put("moderator", forum_postIds);
                            }
                        }

                        if (document.get("joinedForumIds") != null) {
                            memberForum = (ArrayList<String>) document.get("joinedForumIds");

                            if (memberForum != null) {
                                forumIds.addAll(memberForum);
                            }
                        }

                        if (document.get("postIds") != null) {
                            postIds = (ArrayList<String>) document.get("postIds");

                            if (postIds != null) {
                                for (String id: postIds) {

                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private ArrayList<String> getPostDataFromForum(String id, ArrayList<String> postList) {
        db.collection("FORUMS").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> postIds = new ArrayList<>();

                    if (document.exists()) {
                        if (document.get("postIds") != null) {
                            postIds = (ArrayList<String>) document.get("postIds");

                            if (postIds != null) {
                                ArrayList<String> finalPostIds = postIds;

                                for (String id: postIds) {
                                    db.collection("POSTS").document(id)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();

                                                if (document.exists()) {
                                                    String postOwner = document.getString("ownerId");

                                                    if (Objects.equals(postOwner, userId)) {
                                                        finalPostIds.add(document.getId());
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });

        return postList;
    }
}
