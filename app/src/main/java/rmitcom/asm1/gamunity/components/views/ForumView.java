package rmitcom.asm1.gamunity.components.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ForumListPostAdapter;
import rmitcom.asm1.gamunity.model.Post;

public class ForumView extends AppCompatActivity {
    private final String TAG = "Forum View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
//    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
    private String forumId = "IYvjtX2OyUr5C4DDWS28";
    private String userId = "testUser1";
    private String chiefAdminId, forumTitleStr;
    private ArrayList<String> memberIds, moderatorIds, postIds;
    private ArrayList<Post> postsList = new ArrayList<>();
    private TextView forumTitle, moreInfoButton, addPostButton, returnBackButton;
    private ListView postListView;
    private Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_view);

        Intent getIntent = getIntent();
        if (getIntent != null) {
//            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
//            userData = db.collection("USERS").document(userId);

            Log.i(TAG, "forumId: " + forumId);
        }

        setUI();
    }

    private void setUI() {
        forumTitle = findViewById(R.id.forumTitle);
        moreInfoButton = findViewById(R.id.forumMoreInfo);
        editButton = findViewById(R.id.editForumButton);
        addPostButton = findViewById(R.id.forumAddPost);
        returnBackButton = findViewById(R.id.returnBack);
        postListView = findViewById(R.id.forumPostList);

        setForumData();
        addPost();
        accessMoreInfo();
        returnToPreviousPage();

    }

    private void setForumData() {
        postsList = new ArrayList<>();
        forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        forumTitleStr = (String) document.get("title");
                        chiefAdminId = (String) document.get("chiefAdminId");

                        memberIds = (ArrayList<String>) document.get("memberIds");
                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");
                        postIds = (ArrayList<String>) document.get("postIds");

                        if (forumTitleStr != null) {
                            forumTitle.setText(forumTitleStr);
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

//        for (String postId: postIds) {
//            db.collection("POSTS").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        String postTitle, postOwnerId, postTimestamp;
//                        long noLike, noDislike, noComment;
//                        Date timestamp = new Date();
//
//                        if (document.exists()) {
//                            postTitle = (String) document.get("title");
//                            postOwnerId = (String) document.get("ownerId");
//                            postTimestamp = (String) document.get("timestamp");
//                            noLike = (long) document.get("noLike");
//                            noDislike = (long) document.get("noDislike");
//                            noComment = (long) document.get("noComment");
//
//                            try {
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                if (postTimestamp != null) {
//                                    timestamp = sdf.parse(postTimestamp);
//                                } else {
//                                    timestamp = sdf.parse(String.valueOf(Calendar.getInstance()));
//                                }
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//
//                            Post post = new Post(postId, postOwnerId, forumId, postTitle, noLike, noDislike, noComment);
//                            postsList.add(post);
//                        }
//                    }
//                }
//            });
//        }
//
//        setupList(postsList);

        if (postIds != null) {
            int tasksCount = postIds.size();
            final int[] completedTasks = {0};

            for (String postId : postIds) {
                db.collection("POSTS").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            String postTitle, postOwnerId;
                            long noLike, noDislike, noComment;

                            if (document.exists()) {
                                postTitle = (String) document.get("title");
                                postOwnerId = (String) document.get("ownerId");
                                noLike = (long) document.get("noLike");
                                noDislike = (long) document.get("noDislike");
                                noComment = (long) document.get("noComment");

                                Post post = new Post(postId, postOwnerId, forumId, postTitle, noLike, noDislike, noComment);
                                postsList.add(post);
                            }
                        }

                        // Check if all tasks have completed
                        completedTasks[0]++;
                        if (completedTasks[0] == tasksCount) {
                            // All asynchronous tasks have completed, call setupList
                            setupList(postsList);
                        }
                    }
                });
            }
        }
    }

    private void addPost() {
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moreInfoIntent = new Intent(ForumView.this, AddPostForm.class);
                moreInfoIntent.putExtra("forumId", forumId);
                startActivityForResult(moreInfoIntent, 200);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String postId, postTitle;
        if (requestCode == 200) {
            if (data != null) {
                Date timestamp = new Date();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    timestamp = sdf.parse(String.valueOf(calendar));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                postId = (String) Objects.requireNonNull(data.getExtras()).get("postId");
                postTitle = (String) data.getExtras().get("title");

                Post post = new Post(postId, userId, forumId, postTitle, timestamp);
                postsList.add(post);
            }
        }

        setupList(postsList);
    }

    private void accessMoreInfo() {
        moreInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moreInfoIntent = new Intent(ForumView.this, ForumMoreInfoView.class);
                moreInfoIntent.putExtra("forumId", forumId);
                startActivity(moreInfoIntent);
            }
        });
    }

    private void accessForumChat() {

    }

    private void setupList(ArrayList<Post> postList) {
        ForumListPostAdapter adapter = new ForumListPostAdapter(this, 0, postList);
        postListView.setAdapter(adapter);
    }

    private boolean isSiteAlreadyAdded(String searchText, ArrayList<Post> sites) {
        for (Post post: postsList) {
            if (post.getTitle().equals(searchText)) {
                return true;
            }
        }
        return false;
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