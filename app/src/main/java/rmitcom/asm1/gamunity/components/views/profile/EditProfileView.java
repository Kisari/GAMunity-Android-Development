package rmitcom.asm1.gamunity.components.views.profile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.LoginView;
import rmitcom.asm1.gamunity.components.views.chat.ChatView;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.User;

public class EditProfileView extends AppCompatActivity implements FirebaseFetchAndSetUI {

    private final FireBaseManager db = new FireBaseManager();
    private final Constant constant = new Constant();
    private EditText userFirstName, userLastName, userBirth;
    private ImageView userBackground, userBackgroundBtn, editProfileSubmitButton, returnBack;
    private ImageButton userIconBtn;
    private Button deleteButton;
    private ProgressBar backgroundProgress, iconProgress;
    private Uri backgroundFilePath, iconFilePath;
    private String userDocumentRef, backgroundImgUri, profileImgUri;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_view);

        userFirstName = findViewById(R.id.userFirstName);
        userLastName = findViewById(R.id.userLastName);
        userBirth = findViewById(R.id.userBirth);
        userBackground = findViewById(R.id.userBackground);
        userBackgroundBtn = findViewById(R.id.userBackgroundBtn);
        userIconBtn = findViewById(R.id.userIconBtn);
        backgroundProgress = findViewById(R.id.progress1);
        iconProgress = findViewById(R.id.progress2);
        editProfileSubmitButton = findViewById(R.id.editProfileSubmitButton);
        returnBack = findViewById(R.id.returnBack);
        deleteButton = findViewById(R.id.delete_button);

        userBackgroundBtn.setOnClickListener(v -> chooseImageFromFile(true));
        userIconBtn.setOnClickListener(v -> chooseImageFromFile(false));

        editProfileSubmitButton.setOnClickListener( v -> {
            uploadBackgroundImage(backgroundFilePath);
        });

        returnBack.setOnClickListener(v -> {
            finish();
        });

        deleteButton.setOnClickListener(v -> deleteAccount());

        fetchData();
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileView.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View deleteDialogLayout = inflater.inflate(R.layout.ui_delete_dialog_view, null);

        TextView dialogMessage = deleteDialogLayout.findViewById(R.id.dialogMessage);
        Button cancelButton = deleteDialogLayout.findViewById(R.id.dialogCancel);
        Button deleteButton = deleteDialogLayout.findViewById(R.id.dialogAccept);

        builder.setView(deleteDialogLayout);
        AlertDialog dialog = builder.create();
        dialog.show();

        try {
            dialogMessage.setText("Are you sure you want delete this account ? This can not be reverted");
            cancelButton.setOnClickListener(v -> dialog.dismiss());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteProfile(dialog);
//                    db.getCurrentUser().delete()
//                    .addOnCompleteListener(task -> {
//                        if(task.isSuccessful()){
//                            Toast.makeText(EditProfileView.this, "Delete the account", Toast.LENGTH_SHORT).show();
//                            dialog.dismiss();
//
//                            Intent loginIntent = new Intent(EditProfileView.this, LoginView.class);
//                            startActivity(loginIntent);
//                            finish();
//                        }
//                    });
                }
            });

        } catch (Exception e) {
            Log.e("Forum", "getView: ", e);
            e.printStackTrace();
        }
    }

    private void deleteProfile(AlertDialog dialog) {
        DocumentReference userData = db.getDb().collection("users").document(db.getCurrentUser().getUid());
        userData.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {
                                if (document.get("followingIds") != null) {
                                    
                                }

                                if (document.get("joinedForumIds") != null) {
                                    ArrayList<String> memberForumIds = (ArrayList<String>) document.get("joinedForumIds");

                                    if (memberForumIds != null) {
                                        for (String forumId: memberForumIds) {
                                            db.getDb().collection("FORUMS").document(forumId)
                                                    .update("memberIds", FieldValue.arrayRemove(db.getCurrentUser().getUid()));
                                        }
                                    }
                                }

                                if (document.get("adminForumIds") != null) {
                                    ArrayList<String> moderatorForumIds = (ArrayList<String>) document.get("adminForumIds");

                                    if (moderatorForumIds != null) {
                                        for (String forumId: moderatorForumIds) {
                                            db.getDb().collection("FORUMS").document(forumId)
                                                    .update("moderatorIds", FieldValue.arrayRemove(db.getCurrentUser().getUid()));
                                        }
                                    }
                                }

                                if (document.get("ownedForumIds") != null) {
                                    ArrayList<String> adminForumIds = (ArrayList<String>) document.get("ownedForumIds");

                                    if (adminForumIds != null) {
                                        ForumView forumView = new ForumView();
                                        for (String forumId: adminForumIds) {
                                            forumView.deleteForumFromProfile(forumId);
                                        }
                                    }
                                }

                                if (document.get("chatGroupIds") != null) {
                                    ArrayList<String> chatGroupIds = (ArrayList<String>) document.get("chatGroupIds");

                                    if (chatGroupIds != null) {
                                        ChatView chatView = new ChatView();
                                        for (String chatId: chatGroupIds) {
                                            chatView.deleteOrRemoveChatRoomFromProfile(chatId, db.getCurrentUser().getUid());
                                        }
                                    }
                                }
                            }
                            userData.delete();

                            Toast.makeText(EditProfileView.this, "Delete the account", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            Intent loginIntent = new Intent(EditProfileView.this, LoginView.class);
                            startActivity(loginIntent);
                            finish();
                        }
                    }
                });
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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == constant.PICK_IMAGE_BACKGROUND_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            backgroundFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), backgroundFilePath);
                userBackground.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        } else if (requestCode == constant.PICK_IMAGE_ICON_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            iconFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), iconFilePath);
                userIconBtn.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void fetchData() {
        db.getDb().collection("users")
                .whereEqualTo("userId", db.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                            profileImgUri = documentSnapshot.getString("profileImgUri");
                            backgroundImgUri = documentSnapshot.getString("backgroundImgUri");
                            String name = documentSnapshot.getString("name");
                            String dob = documentSnapshot.getString("dob");
                            userDocumentRef = documentSnapshot.getId();

                            currentUser = new User(profileImgUri, backgroundImgUri, name, dob);

                            setUI();
                        }
                    }
                    else{
                        Log.e(TAG, "Can not find the user: ", task.getException());
                    }
                });
    }

    @Override
    public void setUI() {
        userFirstName.setText(currentUser.getName());
        userLastName.setText(currentUser.getName());
        userBirth.setText(currentUser.getDob());

        try{
            if(currentUser.getBackgroundImgUri() != null){
                new AsyncImage(userIconBtn, iconProgress).loadImage(currentUser.getBackgroundImgUri());
            }
            if(currentUser.getProfileImgUri() != null){
                new AsyncImage(userBackground, backgroundProgress).loadImage(currentUser.getProfileImgUri());
            }
        }
        catch (Exception e){
            Log.e(TAG, "fetch user background and icon failed: ", e);
            e.printStackTrace();
        }
    }

    private void uploadBackgroundImage(Uri submitFilePath) {
        String userFirstNameContent = userFirstName.getText().toString();
        String userLastNameContent = userLastName.getText().toString();
        String userBirthContent = userBirth.getText().toString();

        if(userFirstNameContent.isEmpty()){
            userFirstName.setError("User First Name is required");
            return;
        }
        if(userLastNameContent.isEmpty()){
            userLastName.setError("User Last Name is required");
            return;
        }

        if(submitFilePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading user background...");
            progressDialog.show();

            StorageReference ref = db.getStorageRef().child("images/"+ UUID.randomUUID().toString());
            ref.putFile(submitFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileView.this, "Uploaded Image", Toast.LENGTH_SHORT).show();

                        if (backgroundImgUri != null) {
                            String pattern = "images%2F(.*?)\\?";
                            Pattern p = Pattern.compile(pattern);
                            Matcher m = p.matcher(backgroundImgUri);

                            if (m.find()) {
                                String oldUri = m.group(1);

                                StorageReference oldImageRef = db.getStorageRef().child("images/" + oldUri);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                    Log.i("Delete image", "Old image deleted successfully");
                                }).addOnFailureListener(e -> {
                                    Log.e("Delete image", "Failed to delete old image: " + e.getMessage());
                                });
                            }
                        }

                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            backgroundFilePath = uri;
                            uploadIconImage(iconFilePath);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileView.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    });
        }
        else{
            uploadIconImage(iconFilePath);
        }
    }

    private void uploadIconImage(Uri submitFilePath) {
        if(submitFilePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading user icon...");
            progressDialog.show();

            StorageReference ref = db.getStorageRef().child("images/"+ UUID.randomUUID().toString());
            ref.putFile(submitFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileView.this, "Uploaded Image", Toast.LENGTH_SHORT).show();

                        if (profileImgUri != null) {
                            String pattern = "images%2F(.*?)\\?";
                            Pattern p = Pattern.compile(pattern);
                            Matcher m = p.matcher(profileImgUri);

                            if (m.find()) {
                                String oldUri = m.group(1);

                                StorageReference oldImageRef = db.getStorageRef().child("images/" + oldUri);
                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                    Log.i("Delete image", "Old image deleted successfully");
                                }).addOnFailureListener(e -> {
                                    Log.e("Delete image", "Failed to delete old image: " + e.getMessage());
                                });
                            }
                        }

                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            iconFilePath = uri;
                            updateUserInfo();
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileView.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    });
        }
        else{
            updateUserInfo();
        }
    }

    private void updateUserInfo(){

        Map<String, Object> newUserInfo = new HashMap<>();
        if(iconFilePath != null){
            newUserInfo.put("profileImgUri", iconFilePath.toString());
        }

        if(backgroundFilePath != null){
            newUserInfo.put("backgroundImgUri", backgroundFilePath.toString());
        }
        newUserInfo.put("name", userFirstName.getText().toString() + userLastName.getText().toString());
        if(!userBirth.getText().toString().isEmpty()){
            newUserInfo.put("dob", userBirth.getText().toString());
        }

        db.getDb().collection("users")
                .document(userDocumentRef)
                .set(newUserInfo, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(EditProfileView.this, "Update your information", Toast.LENGTH_SHORT).show();
                        Intent backIntent = new Intent();
                        String iconUrl;
                        String backgroundUrl;
                        String dob = userBirth.getText().toString();
                        if(iconFilePath == null){
                            iconUrl = currentUser.getProfileImgUri();
                        }else{
                            iconUrl = iconFilePath.toString();
                        }
                        if(backgroundFilePath == null){
                            backgroundUrl = currentUser.getBackgroundImgUri();
                        }
                        else{
                            backgroundUrl = backgroundFilePath.toString();
                        }
                        if(userBirth.getText().toString().isEmpty()){
                            dob = currentUser.getDob();
                        }
                        else{
                            dob = userBirth.getText().toString();
                        }

                        User returnDataUser = new User(iconUrl, backgroundUrl, userFirstName.getText().toString() + userLastName.getText().toString(), dob);
                        backIntent.putExtra("updatedInfo", returnDataUser);
                        setResult(constant.EDIT, backIntent);
                        finish();
                    }
                });
    }
}