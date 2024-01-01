package rmitcom.asm1.gamunity.components.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rmitcom.asm1.gamunity.R;

public class AddForumForm extends AppCompatActivity {
    private final String TAG = "Add Post Form";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
//    private final String userId = userAuth.getUid();
    private final String userId = "testUserId1";
    private Button addButton;
    private EditText inputTitle, inputDescription, inputCategory;
    private String title, description;
    private ArrayList<String> category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_forum_form);

        addButton = findViewById(R.id.confirmAddForum);
        getInitData();
        addForum();
    }

    private void getInitData() {
        inputTitle = findViewById(R.id.forumTitle);
        inputDescription = findViewById(R.id.forumDescription);
    }

    private void convertData() {
        title = inputTitle.getText().toString();
        description = inputDescription.getText().toString();

    }

    private void addForum() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = true;
                convertData();

                if (title.isEmpty()) {
                    inputTitle.setError("Title cannot be empty");
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
        data.put("chiefAdminId", userId);
        data.put("title", title);
        data.put("description", description);

        db.collection("forums").add(data)
        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String forumId = documentReference.getId();

                Intent forumIntent = new Intent(AddForumForm.this, ForumView.class);
                forumIntent.putExtra("forumId", forumId);
                startActivity(forumIntent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
    }
}