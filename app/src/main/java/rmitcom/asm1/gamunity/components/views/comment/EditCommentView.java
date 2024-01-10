package rmitcom.asm1.gamunity.components.views.comment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
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
import rmitcom.asm1.gamunity.model.Constant;

public class EditCommentView extends AppCompatActivity {
    private final String TAG = "Edit Comment View";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference postData, commentData, userData;
    private String postId, commentId, description, commentImageUri, updateDate;
    private EditText commentEditDescription;
    private TextView commentEditImageButton, commentEditConfirmButton, returnBackButton;
    private ImageView commentEditPicture;
    private ProgressBar commentImageProgressBar;
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
        if (getIntent != null) {
            postId = getIntent.getExtras().getString("postId");
            commentId = getIntent.getExtras().getString("commentId");

            postData = db.collection("POSTS").document(postId);
            commentData = db.collection("COMMENTS").document(commentId);
            userData = db.collection("users").document(userId);

            Log.i(TAG, "postId: " + postId);
        }

        commentEditDescription = findViewById(R.id.editCommentDescription);
        commentEditImageButton = findViewById(R.id.editCommentImageButton);
        commentEditConfirmButton = findViewById(R.id.editCommentConfirm);
        commentEditPicture = findViewById(R.id.editCommentPicture);
        commentImageProgressBar = findViewById(R.id.editCommentProgress1);
        editCommentImage = findViewById(R.id.editCommentImage);
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

                    if (commentImageUri != null) {
                        editCommentImage.setVisibility(View.VISIBLE);

                        try {
                            new AsyncImage(commentEditPicture, commentImageProgressBar).loadImage(commentImageUri);

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
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading comment image...");
            progressDialog.show();

            StorageReference storageRef = storage.getReference();
            storageRef.child("images/"+ UUID.randomUUID().toString());
            storageRef.putFile(submitFilePath)
            .addOnSuccessListener(taskSnapshot -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Uploaded Image", Toast.LENGTH_SHORT).show();

                storageRef.getDownloadUrl().addOnCompleteListener(uri -> {
                    commentImageFilePath = uri.getResult();

                });

            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();

            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Uploaded "+(int)progress+"%");

            });

            String pattern = "images%2F(.*?)\\?";

            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(commentImageUri);
            if (m.find()) {
                String uri = m.group(1);
                storageRef.child("images/" + uri);
                storageRef.delete();
            }
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

                uploadCommentImage(commentImageFilePath);
                addDataToFirebase();
            }

        });
    }

    private void addDataToFirebase() {

        Map<String, Object> data = new HashMap<>();
        data.put("description", description);
        data.put("updateDate", updateDate);

        if (commentImageFilePath != null) {
            data.put("commentImage", commentImageFilePath.toString());
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