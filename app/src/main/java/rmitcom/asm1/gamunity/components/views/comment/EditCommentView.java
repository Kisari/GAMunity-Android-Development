package rmitcom.asm1.gamunity.components.views.comment;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Constant;

public class EditCommentView extends AppCompatActivity {
    private final String TAG = "Edit Comment View";
    private WeakReference<Activity> activityReference;
    private final FireBaseManager manager = new FireBaseManager();
    private DocumentReference postData, commentData, userData;
    private String postId, commentId, description, commentImageUri, updateDate, imageUri;
    private EditText commentEditDescription;
    private TextView commentEditImageButton, commentEditConfirmButton, returnBackButton;
    private ImageView commentEditPicture, baseImage;
    private ProgressBar commentProgressBar, userProgressBar;
    private ShapeableImageView commentUserImage;
    private Uri commentImageFilePath;
    private RelativeLayout editCommentImage;
    private final Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            postId = getIntent.getExtras().getString("postId");
            commentId = getIntent.getExtras().getString("commentId");

            postData = manager.getDb().collection("POSTS").document(postId);
            commentData = manager.getDb().collection("COMMENTS").document(commentId);
            userData = manager.getDb().collection("users").document(manager.getCurrentUser().getUid());

            Log.i(TAG, "postId: " + postId);
        }

        commentEditDescription = findViewById(R.id.editCommentDescription);
        commentEditImageButton = findViewById(R.id.editCommentImageButton);
        commentEditConfirmButton = findViewById(R.id.editCommentConfirm);

        commentEditPicture = findViewById(R.id.editCommentPicture);
        commentProgressBar = findViewById(R.id.editCommentProgress1);
        editCommentImage = findViewById(R.id.editCommentImage);

        baseImage = findViewById(R.id.baseImg);
        userProgressBar = findViewById(R.id.editCommentProgressBar1);
        commentUserImage = findViewById(R.id.editCommentUserProfile);

        returnBackButton = findViewById(R.id.returnBack);

        setCommentData();
        updateImage();
        updateComment();
        returnToPreviousPage();
    }

    private void setCommentData() {
        commentData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    description = document.getString("description");
                    commentImageUri = document.getString("image");

                    if (description != null) {
                        commentEditDescription.setText(description);
                    }

                    if (commentImageUri != null) {
                        editCommentImage.setVisibility(View.VISIBLE);

                        try {
                            new AsyncImage(commentEditPicture, commentProgressBar).loadImage(commentImageUri);
                        }
                        catch (Exception e) {
                            Log.e(TAG, "getView: ", e);
                            e.printStackTrace();
                        }
                    }

                    userData.get().addOnSuccessListener(userDocument -> {
                        if (userDocument.exists()) {
                            String profileImg = userDocument.getString("profileImgUri");

                            if (profileImg != null) {
                                try {
                                    baseImage.setVisibility(View.INVISIBLE);
                                    commentUserImage.setVisibility(View.VISIBLE);
                                    userProgressBar.setVisibility(View.VISIBLE);
                                    new AsyncImage(commentUserImage, userProgressBar).loadImage(profileImg);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                baseImage.setVisibility(View.VISIBLE);
                                commentUserImage.setVisibility(View.INVISIBLE);
                                userProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void convertData() {
        description = commentEditDescription.getText().toString();
    }

    private void updateImage() {
        commentEditImageButton.setOnClickListener(v -> chooseImageFromFile());
    }

    private void chooseImageFromFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_COMMENT_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == constant.PICK_COMMENT_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                commentImageFilePath = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), commentImageFilePath);
                    editCommentImage.setVisibility(View.VISIBLE);
                    commentEditPicture.setImageBitmap(bitmap);
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
            StorageReference storageRef = manager.getStorageRef().child("images/" + randomId);

            storageRef.putFile(submitFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        Activity activity = activityReference.get();
                        if (activity != null && !activity.isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(activity, "Uploaded Image", Toast.LENGTH_SHORT).show();

                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            if (commentImageUri != null) {
                                String pattern = "images%2F(.*?)\\?";
                                Pattern p = Pattern.compile(pattern);
                                Matcher m = p.matcher(commentImageUri);

                                if (m.find()) {
                                    String oldUri = m.group(1);
                                    Log.i(TAG, "uploadPostImage - oldUri: " + oldUri);

                                    // Create a reference to the old image and delete it
                                    StorageReference oldImageRef = manager.getStorageRef().child("images/" + oldUri);
                                    oldImageRef.delete().addOnSuccessListener(aVoid -> Log.i(TAG, "Old image deleted successfully")).addOnFailureListener(e -> Log.e(TAG, "Failed to delete old image: " + e.getMessage()));
                                }
                            }

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

    private void updateComment() {
        commentEditConfirmButton.setOnClickListener(v -> {
            boolean isValid = true;
            convertData();

            if (description.isEmpty()) {
                commentEditDescription.setError("Description cannot be empty");
                isValid = false;
            }
            if (isValid) {
                Calendar calendar = Calendar.getInstance();
                String format = "dd/MM/yyyy HH:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

                updateDate = sdf.format(calendar.getTime());
                Log.i(TAG, "onClick - date: " + updateDate);

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
        data.put("description", description);
        data.put("updateDate", updateDate);

        if (commentImageFilePath != null) {
            data.put("image", commentImageFilePath.toString());
        }

        commentData.set(data, SetOptions.merge()).addOnCompleteListener(task -> {
            Intent returnIntent = new Intent(EditCommentView.this, PostView.class);
            setResult(RESULT_OK, returnIntent);
            finish();
        });

    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}
