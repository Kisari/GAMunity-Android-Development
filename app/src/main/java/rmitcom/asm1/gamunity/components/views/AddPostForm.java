package rmitcom.asm1.gamunity.components.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;

public class AddPostForm extends AppCompatActivity {

    private final String TAG = "Add Post";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
//    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
//    private String forumId = "IYvjtX2OyUr5C4DDWS28";
    private String forumId;
    private String userId = "testUser1";
    private String title, description;
    private ArrayList<String> memberIds, moderatorIds, postIds;
    private TextView returnBackButton;
    private EditText inputPostTitle, inputPostDescription;
    private Button addImageButton, createPostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post_form);

        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
//            userData = db.collection("USERS").document(userId);
        }

        setUI();
    }

    private void setUI() {
        inputPostTitle = findViewById(R.id.addPostTitle);
        inputPostDescription = findViewById(R.id.addPostDescription);
        addImageButton = findViewById(R.id.addPostImage);
        createPostButton = findViewById(R.id.confirmAddPost);
        returnBackButton = findViewById(R.id.returnBack);

        addPost();
        returnToPreviousPage();

    }

    private void convertData() {
        title = inputPostTitle.getText().toString();
        description = inputPostDescription.getText().toString();
    }

    private void addPost() {
        createPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = true;
                convertData();

                if (title.isEmpty()) {
                    inputPostTitle.setError("Title cannot be empty");
                    isValid = false;
                }

                if (description.isEmpty()) {
                    inputPostDescription.setError("Description cannot be empty");
                    isValid = false;
                }

                if (isValid) {
                    addDataToFirebase();
                }
            }
        });
    }

    private void addDataToFirebase() {
        Map<String, Object> data = new HashMap<>();
        data.put("ownerId", userId);
        data.put("forumId", forumId);
        data.put("title", title);
        data.put("description", description);
        data.put("noLike", 0);
        data.put("noDislike", 0);
        data.put("noComment", 0);

        db.collection("POSTS").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String postId = documentReference.getId();

//                        db.collection("USERS").document(userId)
//                                .update("ownedPostIds", FieldValue.arrayUnion(postId));
                        db.collection("FORUMS").document(forumId)
                                .update("postIds", FieldValue.arrayUnion(postId));

                        Intent postIntent = new Intent(AddPostForm.this, ForumView.class);
                        postIntent.putExtra("postId", postId);
                        postIntent.putExtra("title", title);
                        setResult(RESULT_OK, postIntent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
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