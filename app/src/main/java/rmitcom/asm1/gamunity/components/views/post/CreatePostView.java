package rmitcom.asm1.gamunity.components.views.post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.model.Constant;

public class CreatePostView extends AppCompatActivity {
    private final String TAG = "Add Post";
    private WeakReference<Activity> activityReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference forumData, userData;
    //    private String forumId = "IYvjtX2OyUr5C4DDWS28";
    private String forumId;
//    private String userId = "testUser1";
    private String title, description, date, imageUri;
    private ArrayList<String> memberIds, moderatorIds;
    private TextView returnBackButton, addImageButton, createPostButton;
    private EditText inputPostTitle, inputPostDescription;
    private ImageView postImage;
    private Uri postImageFilePath;
    private Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");
            forumData = db.collection("FORUMS").document(forumId);
            userData = db.collection("users").document(userId);
        }

        inputPostTitle = findViewById(R.id.addPostTitle);
        inputPostDescription = findViewById(R.id.addPostDescription);
        addImageButton = findViewById(R.id.addPostImage);
        postImage = findViewById(R.id.addPostPicture);
        createPostButton = findViewById(R.id.addPostConfirm);
        returnBackButton = findViewById(R.id.returnBack);

        addPost();
        addImage();
        returnToPreviousPage();

    }

    private void convertData() {
        title = inputPostTitle.getText().toString();
        description = inputPostDescription.getText().toString();
    }

    private void addImage() {
        addImageButton.setOnClickListener(v -> chooseImageFromFile());
    }

    private void chooseImageFromFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_POST_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == constant.PICK_POST_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                postImageFilePath = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), postImageFilePath);
                    postImage.setVisibility(View.VISIBLE);
                    postImage.setImageBitmap(bitmap);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadPostImage(Uri submitFilePath) {
        if (submitFilePath != null) {
            activityReference = new WeakReference<>(this);

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading post image...");
            progressDialog.show();

            String randomId = UUID.randomUUID().toString();
            Log.i(TAG, "uploadPostImage - randomId: " + randomId);
            StorageReference storageRef = storage.getReference().child("images/" + randomId);

            storageRef.putFile(submitFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        Activity activity = activityReference.get();
                        if (activity != null && !activity.isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(activity, "Uploaded Image", Toast.LENGTH_SHORT).show();

                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            postImageFilePath = uri;
                            imageUri = postImageFilePath.toString();
                            addDataToFirebase();
                            Log.i(TAG, "uploadPostImage - postImageFilePath: " + postImageFilePath);
                        });

                    }).addOnFailureListener(e -> {
                        Activity activity = activityReference.get();
                        if (activity != null && !activity.isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }).addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    });
        }
    }

    private void addPost() {
        createPostButton.setOnClickListener(v -> {
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
                Calendar calendar = Calendar.getInstance();
                String format = "dd/MM/yyyy HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

                date = sdf.format(calendar.getTime());
                Log.i(TAG, "onClick - date: " + date);

                if (postImageFilePath != null) {
                    uploadPostImage(postImageFilePath);
                } else {
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
        data.put("date", date);

        if (postImageFilePath != null) {
            data.put("image", imageUri);
        }

        db.collection("POSTS").add(data)
                .addOnSuccessListener(documentReference -> {
                    String postId = documentReference.getId();

                    db.collection("users").document(userId)
                            .update("ownedPostIds", FieldValue.arrayUnion(postId));
                    db.collection("FORUMS").document(forumId)
                            .update("postIds", FieldValue.arrayUnion(postId));

                    Intent postIntent = new Intent(CreatePostView.this, ForumView.class);
                    postIntent.putExtra("postId", postId);
                    postIntent.putExtra("title", title);
                    postIntent.putExtra("description", description);
                    postIntent.putExtra("date", date);
                    postIntent.putExtra("image", imageUri);
                    setResult(RESULT_OK, postIntent);
                    finish();
                }).addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}