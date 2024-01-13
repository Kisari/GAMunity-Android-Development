package rmitcom.asm1.gamunity.components.views.forum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ForumTagListAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Notification;

public class EditForumView extends AppCompatActivity implements ForumTagListAdapter.ItemLongClickListener{
    private final String TAG = "Edit Forum View";
    private WeakReference<Activity> activityReference;
    private final FireBaseManager dbManager = new FireBaseManager();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final Constant constant = new Constant();
    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;
    private String forumId, title, description, forumIconUri, forumBackgroundUri, backgroundUri, iconUri, forumNumberId;
    private ArrayList<String> forumMemberIds, forumTagList;
    private ForumTagListAdapter tagListAdapter;
    private EditText forumTitle, forumDescription, forumCategory;
    private ImageView forumBackground, forumBackgroundButton, returnBackButton, confirmEditButton;
    private ImageButton forumIconButton;
    private ShapeableImageView forumIcon;
    private ProgressBar forumIconProgressBar, forumBackgroundProgressBar;
    private Uri backgroundFilePath, iconFilePath;

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
        initializeForumTagSelectionView();
    }

    @SuppressLint("SetTextI18n")
    private void initializeForumTagSelectionView(){
        //get the listview
        RecyclerView forumTagLayout = findViewById(R.id.forumTagsLayout);

        forumCategory.setFocusable(false);
        forumDescription.setOnDragListener((v, event) -> true);
        forumTitle.setOnDragListener((v, event) -> true);

        //initialize adapter
        this.tagListAdapter = new ForumTagListAdapter(this, constant.tagList);

        //set on click listener for every tag
        tagListAdapter.setLongClickListener(this);

        //initialize the Horizontal Scroll View for tag list
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        forumTagLayout.setLayoutManager(layoutManager);

        //set adapter to tags view list
        forumTagLayout.setAdapter(tagListAdapter);

        forumCategory.setOnDragListener((v, event) -> {
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(Color.parseColor("#99006400"));
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundResource(R.drawable.rounded_pill_purple_stroke);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:

                    // Get the item containing the dragged data.
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    // Get the text data from the item.
                    CharSequence dragData = item.getText();

                    // Set the data to edit text
                    EditText pointerView = ((EditText)v);
                    String tag = tagListAdapter.getItem(Integer.parseInt(dragData.toString()));
                    String addNewItemString = "#" + tag;

                    if (forumTagList != null) {
                        forumTagList.add(tag);
                    } else {
                        forumTagList = new ArrayList<>();
                        forumTagList.add(tag);
                    }

                    Log.i(TAG, "initializeForumTagSelectionView: " + addNewItemString);
                    pointerView.setText(pointerView.getText().toString() + " " +  addNewItemString);

                    tagListAdapter.removeAt(Integer.parseInt(dragData.toString()));

                    v.setBackgroundResource(R.drawable.rounded_pill_purple_stroke);
                    v.invalidate();

                    // Return true. DragEvent.getResult() returns true.
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    v.setBackgroundColor(Color.parseColor("#ffffff"));
                    v.invalidate();

                    // Do a getResult() and displays what happens.
                    if (event.getResult()) {
                        Toast.makeText(this, "The drop was handled.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG).show();
                    }

                    // Return true. The value is ignored.
                    return true;
                default:
                    break;
            }
            return false;
        });
    }

    @Override
    public void onItemLongClick(View view, int position){
        view.setTag(String.valueOf(position));
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
        ClipData dragData = new ClipData(
                tagListAdapter.getItem(position),
                new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                item);

        View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
        view.startDrag(dragData, myShadow, null, 0);
        Log.d(TAG, "onItemLongClick: " + tagListAdapter.getItem(position));
    }
    @SuppressWarnings("unchecked")
    private void setForumData() {
        forumData.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    forumTitle.setText((String) document.get("title"));
                    forumDescription.setText((String) document.get("description"));
                    forumNumberId = document.getString("forumId");
                    forumMemberIds = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("memberIds")));

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
        if (forumTagList != null) {
            data.put("category", forumTagList);
        }
        if (backgroundFilePath != null) {
            data.put("forumBackground", backgroundFilePath.toString());
        }

        if (iconFilePath != null) {
            data.put("forumIcon", iconFilePath.toString());
        }


        forumData.set(data, SetOptions.merge()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                dbManager.getDb().collection("users")
                        .whereEqualTo("userId", dbManager.getCurrentUser().getUid())
                        .get()
                        .addOnCompleteListener(checkingUser -> {
                            if(checkingUser.isSuccessful()){
                                for (QueryDocumentSnapshot document: checkingUser.getResult()){
                                    String userName = document.getString("name");
                                    for (int i = 0; i < forumMemberIds.size(); i++) {
                                        String forumReceiverId = forumMemberIds.get(i);
                                        String forumIconUrl = forumIconUri;
                                        if(iconFilePath != null){
                                            forumIconUrl = iconFilePath.toString();
                                        }

                                        String notificationBody = userName + " has edit the content of the forum that you are currently join in " +
                                                title;
                                        Notification newNotification = new Notification("Join the forum", forumIconUrl, notificationBody, dbManager.getCurrentUser().getUid(), forumReceiverId, false, Calendar.getInstance().getTime().toString(), forumNumberId);
                                        dbManager.sendNotificationToDevice(newNotification, userName, constant.EDIT_FORUM);
                                    }
                                    Toast.makeText(EditForumView.this, "Sent notification to members",Toast.LENGTH_SHORT).show();
                                    Intent returnIntent = new Intent(EditForumView.this, ForumView.class);
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    private void returnToPreviousPage() {
        returnBackButton.setOnClickListener(v -> finish());
    }
}