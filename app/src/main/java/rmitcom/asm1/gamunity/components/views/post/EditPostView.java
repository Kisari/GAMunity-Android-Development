package rmitcom.asm1.gamunity.components.views.post;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.Constant;

public class EditPostView extends AppCompatActivity {
    private final String TAG = "Edit Post";
    private WeakReference<Activity> activityReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, postData, userData;
    private String forumId, postId, title, description, postImageUri, updateDate, imageUri;
    private EditText postEditTitle, postEditDescription;
    private TextView postEditImageButton, postEditConfirmButton, returnBackButton;
    private ImageView postEditPicture;
    private ProgressBar postImageProgressBar;
    private Uri postImageFilePath;
    private RelativeLayout editPostImage;
    private final Constant constant = new Constant();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = Objects.requireNonNull(getIntent.getExtras()).getString("forumId");
            postId = getIntent.getExtras().getString("postId");

            forumData = db.collection("FORUMS").document(forumId);
            postData = db.collection("POSTS").document(postId);
            userData = db.collection("users").document(userId);

            Log.i(TAG, "forumId: " + forumId);
        }

        postEditTitle = findViewById(R.id.editPostTitle);
        postEditDescription = findViewById(R.id.editPostDescription);
        postEditImageButton = findViewById(R.id.editPostImageButton);
        postEditConfirmButton = findViewById(R.id.editPostConfirm);
        postEditPicture = findViewById(R.id.editPostPicture);
        postImageProgressBar = findViewById(R.id.editPostProgress1);
        editPostImage = findViewById(R.id.editPostImage);
        returnBackButton = findViewById(R.id.returnBack);

        setPostData();
        updateImage();
        updatePost();
        returnToPreviousPage();

    }

    private void setPostData() {
        postData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    title = document.getString("title");
                    description = document.getString("description");
                    postImageUri = document.getString("image");

                    if (title != null) {
                        postEditTitle.setText(title);
                    }

                    if (description != null) {
                        postEditDescription.setText(description);
                    }

                    if (postImageUri != null) {
                        editPostImage.setVisibility(View.VISIBLE);

                        try {
                            new AsyncImage(postEditPicture, postImageProgressBar).loadImage(postImageUri);

                        }
                        catch (Exception e) {
                            Log.e(TAG, "getView: ", e);
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void convertData() {
        title = postEditTitle.getText().toString();
        description = postEditDescription.getText().toString();
    }

    private void updateImage() {
        postEditImageButton.setOnClickListener(v -> chooseImageFromFile());
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
                    editPostImage.setVisibility(View.VISIBLE);
                    postEditPicture.setImageBitmap(bitmap);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadPostImage(Uri submitFilePath) {
        if(submitFilePath != null) {
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

                            if (postImageUri != null) {
                                Log.i(TAG, "uploadPostImage - postImageUri: " + postImageUri);
                                String pattern = "images%2F(.*?)\\?";

                                Pattern p = Pattern.compile(pattern);
                                Matcher m = p.matcher(postImageUri);
                                if (m.find()) {
                                    String oldUri = m.group(1);
                                    storageRef.child("images/" + oldUri);
                                    storageRef.delete();
                                }
                            }

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

    private void updatePost() {
        postEditConfirmButton.setOnClickListener(v -> {
            boolean isValid = true;
            convertData();

            if (title.isEmpty()) {
                postEditTitle.setError("Title cannot be empty");
                isValid = false;
            }

            if (description.isEmpty()) {
                postEditDescription.setError("Description cannot be empty");
                isValid = false;
            }

            if (isValid) {
                Calendar calendar = Calendar.getInstance();
                String format = "dd/MM/yyyy HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

                updateDate = sdf.format(calendar.getTime());
                Log.i(TAG, "onClick - date: " + updateDate);

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
        data.put("title", title);
        data.put("description", description);
        data.put("updateDate", updateDate);

        if (postImageFilePath != null) {
            data.put("image", imageUri);
        }

        postData.set(data, SetOptions.merge()).addOnCompleteListener(task -> {
            Intent returnIntent = new Intent(EditPostView.this, PostView.class);
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}