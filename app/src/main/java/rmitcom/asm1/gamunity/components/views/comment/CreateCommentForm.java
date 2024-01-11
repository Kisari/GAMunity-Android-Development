package rmitcom.asm1.gamunity.components.views.comment;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.model.Constant;

public class CreateCommentForm extends AppCompatActivity {
    private final String TAG = "Add Comment";
    private WeakReference<Activity> activityReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference postData, userData;
    //    private String forumId = "IYvjtX2OyUr5C4DDWS28";
    private String postId;
//    private String userId = "testUser1";
    private String description, date, commentRepliedId, imageUri;
    private ArrayList<String> memberIds, moderatorIds;
    private TextView returnBackButton, addImageButton, createCommentButton;
    private EditText inputCommentDescription;
    private ImageView commentImage;
    private Uri commentImageFilePath;
    private boolean isReply;
    private Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_comment_form);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            postId = (String) Objects.requireNonNull(getIntent.getExtras()).get("postId");
            postData = db.collection("POSTS").document(postId);
//            userData = db.collection("users").document(userId);
            isReply = getIntent.getExtras().get("isReply") != null;


            if (isReply) {
                commentRepliedId = (String) getIntent.getExtras().get("commentId");
            }
        }

        inputCommentDescription = findViewById(R.id.addCommentDescription);
        addImageButton = findViewById(R.id.addCommentImage);
        createCommentButton = findViewById(R.id.addCommentConfirm);
        commentImage = findViewById(R.id.addCommentPicture);
        returnBackButton = findViewById(R.id.returnBack);

        addImage();
        addComment();
        returnToPreviousPage();
    }

    private void convertData() {
        description = inputCommentDescription.getText().toString();
    }

    private void addImage() {
        addImageButton.setOnClickListener(v -> chooseImageFromFile());
    }

    private void chooseImageFromFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_COMMENT_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == constant.PICK_COMMENT_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                commentImageFilePath = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), commentImageFilePath);
                    commentImage.setVisibility(View.VISIBLE);
                    commentImage.setImageBitmap(bitmap);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadCommentImage(Uri submitFilePath) {
        if(submitFilePath != null) {
            activityReference = new WeakReference<>(this);

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading post image...");
            progressDialog.show();

            String randomId = UUID.randomUUID().toString();
            Log.i(TAG, "uploadCommentImage - randomId: " + randomId);
            StorageReference storageRef = storage.getReference().child("images/" + randomId);

            storageRef.putFile(submitFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        Activity activity = activityReference.get();
                        if (activity != null && !activity.isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(activity, "Uploaded Image", Toast.LENGTH_SHORT).show();

                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            commentImageFilePath = uri;
                            imageUri = commentImageFilePath.toString();
                            addDataToFirebase();
                            Log.i(TAG, "uploadCommentImage - commentImageFilePath: " + commentImageFilePath);
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

    private void addComment() {
        createCommentButton.setOnClickListener(v -> {
            boolean isValid = true;
            convertData();

            if (description.isEmpty()) {
                inputCommentDescription.setError("Description cannot be empty");
                isValid = false;
            }
            if (isValid) {
                Calendar calendar = Calendar.getInstance();
                String format = "dd/MM/yyyy HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

                date = sdf.format(calendar.getTime());
                Log.i(TAG, "onClick - date: " + date);

                if (commentImageFilePath != null) {
                    uploadCommentImage(commentImageFilePath);
                } else {
                    addDataToFirebase();
                }
            }

        });
    }

    private void addDataToFirebase() {

        Map<String, Object> data = new HashMap<>();
        data.put("ownerId", userId);
        data.put("postId", postId);
        data.put("description", description);
        data.put("date", date);

        if (commentImageFilePath != null) {
            data.put("image", commentImageFilePath.toString());
        }

        if (isReply) {
            data.put("commentRepliedId", commentRepliedId);
        }

        db.collection("COMMENTS").add(data)
                .addOnSuccessListener(documentReference -> {
                    String commentId = documentReference.getId();

                    db.collection("users").document(userId)
                            .update("ownedCommentIds", FieldValue.arrayUnion(postId));
                    db.collection("POSTS").document(postId)
                            .update("commentIds", FieldValue.arrayUnion(commentId));

                    if (isReply) {
                        db.collection("COMMENTS").document(commentId)
                                .update("replyCommentIds", FieldValue.arrayUnion(commentRepliedId));
                    }

                    Intent commentIntent = new Intent(CreateCommentForm.this, PostView.class);
                    commentIntent.putExtra("commentId", commentId);
                    commentIntent.putExtra("description", description);
                    commentIntent.putExtra("date", date);
                    commentIntent.putExtra("image", imageUri);
                    setResult(RESULT_OK, commentIntent);
                    finish();
                }).addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }

}