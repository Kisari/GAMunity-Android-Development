package rmitcom.asm1.gamunity.components.views.forum;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.Constant;

public class EditForumView extends AppCompatActivity {
    private final String TAG = "Edit Forum View";
    private WeakReference<Activity> activityReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
    private String forumId, title, description, forumIconUri, forumBackgroundUri, backgroundUri, iconUri;
    private ArrayList<String> category;
    private EditText forumTitle, forumDescription, forumCategory;
    private ImageView forumBackground, forumBackgroundButton, returnBackButton, confirmEditButton;
    private ImageButton forumIconButton;
    private ShapeableImageView forumIcon;
    private ProgressBar forumIconProgressBar, forumBackgroundProgressBar;
    private Uri backgroundFilePath, iconFilePath;
    private final Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_forum_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();
        if (getIntent != null) {
            forumId = (String) Objects.requireNonNull(getIntent.getExtras()).get("forumId");

            forumData = db.collection("FORUMS").document(forumId);
            userData = db.collection("USERS").document(userId);

            Log.i(TAG, "forumId: " + forumId);
        }

        forumTitle = findViewById(R.id.editForumTitle);
        forumDescription = findViewById(R.id.editForumDescription);
        forumCategory = findViewById(R.id.editForumCategories);

        forumBackground = findViewById(R.id.forumBackground);
        forumBackgroundButton = findViewById(R.id.editForumBackgroundButton);
        forumIcon = findViewById(R.id.editForumIcon);
        forumIconButton = findViewById(R.id.editForumIconButton);
        forumBackgroundProgressBar = findViewById(R.id.editForumProgress1);
        forumIconProgressBar = findViewById(R.id.editForumProgress2);

        confirmEditButton = findViewById(R.id.editForumSubmitButton);
        returnBackButton = findViewById(R.id.returnBack);

        setForumData();
        addImage();
        updatePost();
        returnToPreviousPage();
    }

    private void setForumData() {
        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    forumTitle.setText((String) document.get("title"));
                    forumDescription.setText((String) document.get("description"));

                    forumIconUri = document.getString("forumIcon");
                    forumBackgroundUri = document.getString("forumBackground");

                    if (forumIconUri!= null && forumBackgroundUri != null) {
                        try {
                            new AsyncImage(forumIcon, forumIconProgressBar).loadImage(forumIconUri);
                            new AsyncImage(forumBackground, forumBackgroundProgressBar).loadImage(forumBackgroundUri);
                        } catch (Exception e){
                            Log.e(TAG, "getView: ", e);
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void convertData() {
        title = forumTitle.getText().toString();
        description = forumDescription.getText().toString();

    }

    private void updatePost() {
        confirmEditButton.setOnClickListener(v -> {
            boolean isValid = true;
            convertData();

            if(title.isEmpty()) {
                isValid = false;
                forumTitle.setError("Title cannot be empty");
            }

            if (description.isEmpty()) {
                isValid = false;
                forumDescription.setError("Description cannot be empty");
            }

            if (forumBackgroundUri.isEmpty()) {
                isValid = false;
                Toast.makeText(EditForumView.this, "Background image cannot be empty", Toast.LENGTH_SHORT).show();
            }

            if (forumIconUri.isEmpty()) {
                isValid = false;
                Toast.makeText(EditForumView.this, "Icon image cannot be empty", Toast.LENGTH_SHORT).show();
            }

            if (isValid) {
                uploadImage(backgroundFilePath, true);
                uploadImage(iconFilePath, false);
                addDataToFirebase();
            }
        });
    }

    private void addImage() {
        forumIconButton.setOnClickListener(v -> chooseImageFromFile(false));

        forumBackgroundButton.setOnClickListener(v -> chooseImageFromFile(true));
    }

    private void chooseImageFromFile(Boolean isBackground){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if(isBackground){
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_IMAGE_BACKGROUND_REQUEST);
        }
        else {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_IMAGE_ICON_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == constant.PICK_IMAGE_BACKGROUND_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                backgroundFilePath = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), backgroundFilePath);
                    forumBackground.setImageBitmap(bitmap);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        } else if(requestCode == constant.PICK_IMAGE_ICON_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                iconFilePath = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), iconFilePath);
                    forumIcon.setImageBitmap(bitmap);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadImage(Uri submitFilePath, Boolean isBackground) {
        if(submitFilePath != null) {
            activityReference = new WeakReference<>(this);
            final ProgressDialog progressDialog = new ProgressDialog(this);

            if (isBackground) {
                progressDialog.setTitle("Uploading new background image...");
            } else {
                progressDialog.setTitle("Uploading new icon image...");
            }

            progressDialog.show();

            StorageReference storageRef = storage.getReference().child("images/"+ UUID.randomUUID().toString());

            storageRef.putFile(submitFilePath).addOnSuccessListener(taskSnapshot -> {
                Activity activity = activityReference.get();

                if (activity != null && !activity.isFinishing() && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(this, "Uploaded Image", Toast.LENGTH_SHORT).show();

                if (isBackground) {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (forumBackgroundUri != null) {
                            String pattern = "images%2F(.*?)\\?";
                            Pattern p = Pattern.compile(pattern);
                            Matcher m = p.matcher(forumBackgroundUri);

                            if (m.find()) {
                                String oldUri = m.group(1);

                                StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                    Log.i(TAG, "Old image deleted successfully");
                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to delete old image: " + e.getMessage());
                                });
                            }
                        }

                        backgroundFilePath = uri;
                        backgroundUri = backgroundFilePath.toString();
                    });

                } else {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (forumIconUri != null) {
                            String pattern = "images%2F(.*?)\\?";
                            Pattern p = Pattern.compile(pattern);
                            Matcher m = p.matcher(forumIconUri);

                            if (m.find()) {
                                String oldUri = m.group(1);

                                StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                    Log.i(TAG, "Old image deleted successfully");
                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to delete old image: " + e.getMessage());
                                });
                            }
                        }

                        iconFilePath = uri;
                        forumIconUri = iconFilePath.toString();
                    });
                }

//                addDataToFirebase();

            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();

            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Uploaded "+(int)progress+"%");

            });
        }
    }

    private void addDataToFirebase() {

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);
//        data.put("category", Arrays.asList(forumTagList));
        if (backgroundFilePath != null) {
            data.put("forumBackground", backgroundFilePath.toString());
        }

        if (iconFilePath != null) {
            data.put("forumIcon", iconFilePath.toString());
        }


        forumData.set(data, SetOptions.merge()).addOnCompleteListener(task -> {
            Intent returnIntent = new Intent(EditForumView.this, ForumView.class);
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}