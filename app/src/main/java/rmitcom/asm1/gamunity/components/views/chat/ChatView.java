package rmitcom.asm1.gamunity.components.views.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ChatMessageRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.fragments.ChatFragment;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.GroupChat;
import rmitcom.asm1.gamunity.model.Message;

public class ChatView extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final String TAG = "Chat view";
    private WeakReference<Activity> activityReference;
    private DocumentReference chatData, forumData, currUserData, otherUserData;
    private CollectionReference chatRef;
    private TextView chatTitle, returnBackBtn, moreInfoBtn, moreOptionBtn;
    private EditText inputMessage;
    private ImageView sendMessageBtn, baseImage, chatImageBtn;
    private ProgressBar chatProgressBar;
    private ShapeableImageView chatImage;
    private RecyclerView messageBody;
    private String inputMessageStr, chatId, dataId, forumId, chatTitleStr, chatImgUri;
    private ArrayList<String> chatMemberIds, chatModeratorIds, chatAdminIds, chatUserIds;
    private ArrayList<Message> chatMessages;
    private GroupChat groupChat;
    private Uri chatImageFilePath;
    private boolean isGroup, isNew;
    private ChatMessageRecyclerViewAdapter adapter;
    private final Constant constant = new Constant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();

        chatTitle = findViewById(R.id.chatTitle);
        chatProgressBar = findViewById(R.id.chatProgressBar1);
        chatImage = findViewById(R.id.chatImage);
        returnBackBtn = findViewById(R.id.returnBack);
        moreInfoBtn = findViewById(R.id.chatMoreInfo);
        moreOptionBtn = findViewById(R.id.chatMoreOption);
        inputMessage = findViewById(R.id.chatWriteMessage);
        baseImage = findViewById(R.id.baseImg);
        sendMessageBtn = findViewById(R.id.messageSubmit);
        messageBody = findViewById(R.id.chatMessages);
        chatImageBtn = findViewById(R.id.addChatImage);

        if (getIntent != null) {
            chatId = Objects.requireNonNull(getIntent.getExtras()).getString("chatId");
            Log.i(TAG, "chatView id: " + chatId);
            chatData = db.collection("CHATROOMS").document(chatId);
            chatRef = chatData.collection("CHATS");

            currUserData = db.collection("users").document(userId);

            isGroup = getIntent.getExtras().getBoolean("isGroup");

            dataId = getIntent.getExtras().getString("dataId");

            if (dataId != null) {
                if (isGroup) {
                    forumId = dataId;
                    forumData = db.collection("FORUMS").document(dataId);
                }
                else {
                    otherUserData = db.collection("users").document(dataId);
                }
            }
        }

        getOrCreateChatRoom();
        getChatData();

        sendMessageBtn.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            messageBody.scrollToPosition(adapter.getItemCount() - 1);

            if (message.isEmpty()) {
                return;
            }
            sendMessageToChat(message);
        });

        chatImageBtn.setOnClickListener(v -> {
            messageBody.scrollToPosition(adapter.getItemCount() - 1);
            sendImageToChat();
        });

        returnToPreviousPage();
    }

    private void getOrCreateChatRoom() {
        chatUserIds = new ArrayList<>();
        chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists() && document.getData() != null && !document.getData().isEmpty()) {
                        Log.i(TAG, "chatView getOrCreateChatRoom: not empty doc");
                        isNew = false;

                        chatTitleStr = document.getString("chatTitle");
                        chatTitle.setText(chatTitleStr);
                        chatImgUri = document.getString("chatImg");

                        if (chatImgUri != null) {
                            try {
                                baseImage.setVisibility(View.INVISIBLE);
                                chatImage.setVisibility(View.VISIBLE);
                                chatProgressBar.setVisibility(View.VISIBLE);

                                new AsyncImage(chatImage, chatProgressBar).loadImage(chatImgUri);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            baseImage.setVisibility(View.VISIBLE);
                            chatImage.setVisibility(View.INVISIBLE);
                            chatProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                    else {
                        Log.i(TAG, "chatView getOrCreateChatRoom: empty doc");
                        isNew = true;

                        Map<String, Object> newChatroom = new HashMap<>();

                        chatMemberIds = new ArrayList<>();
                        chatModeratorIds = new ArrayList<>();
                        chatAdminIds = new ArrayList<>();

                        newChatroom.put("isGroup", isGroup);

                        if (isGroup) {
                            forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();

                                        if (document.exists()) {
                                            String forumName = document.getString("title");
                                            String forumIcon = document.getString("forumIcon");

                                            String chiefAdminId = document.getString("chiefAdmin");
                                            chatAdminIds.add(chiefAdminId);
                                            chatUserIds.add(chiefAdminId);

                                            ArrayList<String> memberIds = (ArrayList<String>) document.get("memberIds");
                                            ArrayList<String> moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                            if (memberIds != null) {
                                                chatUserIds.addAll(memberIds);
                                                if (memberIds.contains(userId)) {
                                                    chatMemberIds.add(userId);
                                                }
                                            }

                                            if (moderatorIds != null) {
                                                chatUserIds.addAll(moderatorIds);
                                                if (moderatorIds.contains(userId)) {
                                                    chatModeratorIds.add(userId);
                                                }
                                            }

                                            newChatroom.put("chatTitle", forumName + "'s Group Chat");
                                            chatTitle.setText(forumName + "'s Group Chat");

                                            newChatroom.put("memberIds", chatMemberIds);
                                            newChatroom.put("moderatorIds", chatModeratorIds);
                                            newChatroom.put("adminIds", chatAdminIds);

                                            newChatroom.put("lastTimestamp", Timestamp.now());
                                            newChatroom.put("lastMessageSenderId", "");
                                            newChatroom.put("chatImg", forumIcon);
                                            newChatroom.put("dataId", dataId);

                                            chatData.set(newChatroom, SetOptions.merge());

                                            if (forumIcon != null) {
                                                try {
                                                    baseImage.setVisibility(View.INVISIBLE);
                                                    chatImage.setVisibility(View.VISIBLE);
                                                    chatProgressBar.setVisibility(View.VISIBLE);

                                                    new AsyncImage(chatImage, chatProgressBar).loadImage(forumIcon);
                                                }
                                                catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else {
                                                baseImage.setVisibility(View.VISIBLE);
                                                chatImage.setVisibility(View.INVISIBLE);
                                                chatProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    }
                                }
                            });

                        }
                        else {
                            StringBuilder dataName = new StringBuilder();
                            Log.i(TAG, "chatName: " + dataName);
                            currUserData.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot document) {
                                    if (document.exists()) {
                                        String currUserName = document.getString("name");
                                        Log.i(TAG, "chatName - curr: " + currUserName);
                                        dataName.append(currUserName);
                                        Log.i(TAG, "chatName: " + dataName);

                                        dataName.append(" - ");
                                        Log.i(TAG, "chatName: " + dataName);

                                        otherUserData.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot document) {
                                                if (document.exists()) {
                                                    String otherUserName = document.getString("name");
                                                    Log.i(TAG, "chatName - other: " + otherUserName);
                                                    dataName.append(otherUserName);
                                                    Log.i(TAG, "chatName: " + dataName);

                                                    String name = dataName + "'s Chat";
                                                    Log.i(TAG, "chatName: " + name);

                                                    newChatroom.put("chatTitle", name);
                                                    chatTitle.setText(name);

                                                    chatAdminIds.add(userId);
                                                    chatAdminIds.add(dataId);

                                                    newChatroom.put("adminIds", chatAdminIds);
                                                    newChatroom.put("lastTimestamp", Timestamp.now());
                                                    newChatroom.put("lastMessageSenderId", "");
                                                    newChatroom.put("chatImg", "");

                                                    chatData.set(newChatroom, SetOptions.merge());
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

    private void getChatData() {
        Query query = chatRef.orderBy("timestamp", Query.Direction.ASCENDING).limit(50);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }

                if (chatMessages == null) {
                    chatMessages = new ArrayList<>();
                } else {
                    chatMessages.clear();
                }

                if (value != null) {
                    for (QueryDocumentSnapshot queryDocument : value) {
                        Message messageInfo = queryDocument.toObject(Message.class);
                        chatMessages.add(messageInfo);
                        Log.i(TAG, "message - existed: " + messageInfo.getMessageContent());
                    }

                    setupList(chatMessages);

                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendMessageToChat(String message) {
        Map<String, String> lastSender = new HashMap<>();
        lastSender.put("lastMessageSenderId", userId);
        chatData.set(lastSender, SetOptions.merge());

        Map<String, Timestamp> lastTimestamp = new HashMap<>();
        lastTimestamp.put("lastTimestamp", Timestamp.now());
        chatData.set(lastTimestamp, SetOptions.merge());

        Message newMessage = new Message(message, userId, Timestamp.now(), false);
        chatRef.add(newMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
//                    DocumentReference document = task.getResult();
                inputMessage.setText("");
                Log.i(TAG, "message - just send: " + newMessage.getMessageContent());
                adapter.notifyDataSetChanged();
//                setUI();
            }
        });
    }

    private void sendImageToChat(){
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
                chatImageFilePath = data.getData();
                uploadChatImage(chatImageFilePath);

//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), chatImageFilePath);
//                    commentImage.setVisibility(View.VISIBLE);
//                    commentImage.setImageBitmap(bitmap);
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
            }
        }
    }

    private void uploadChatImage(Uri submitFilePath) {
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
                            chatImageFilePath = uri;
                            chatImgUri = chatImageFilePath.toString();

                            Message newMessage = new Message(chatImgUri, userId, Timestamp.now(), true);
                            chatRef.add(newMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "message - just send: " + newMessage.getMessageContent());
                                        adapter.notifyDataSetChanged();
//                                        setUI();
                                    }
                                }
                            });

                            Log.i(TAG, "uploadCommentImage - commentImageFilePath: " + chatImageFilePath);
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

    private void setupList(ArrayList<Message> messageList) {
        Log.i(TAG, "setupList: call setupList");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(false);
        messageBody.setLayoutManager(layoutManager);

        Log.i(TAG, "setupList: create adapter");
        adapter = new ChatMessageRecyclerViewAdapter(this, messageList);
        messageBody.setAdapter(adapter);

        messageBody.scrollToPosition(adapter.getItemCount() - 1);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                messageBody.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    private void setMoreInfo() {

//        if

        PopupMenu popupMenu = new PopupMenu(ChatView.this, moreInfoBtn);
        popupMenu.getMenuInflater().inflate(R.menu.chat_more_option, popupMenu.getMenu());

        MenuItem moreInfo = popupMenu.getMenu().findItem(R.id.chatMoreInfo);
        MenuItem editChat = popupMenu.getMenu().findItem(R.id.chatUpdate);
        MenuItem deleteChat = popupMenu.getMenu().findItem(R.id.chatDelete);
        MenuItem addMember = popupMenu.getMenu().findItem(R.id.chatAddUser);

        if (isGroup) {
            if (forumId != null) {
                if (chatAdminIds.contains(userId)) {
                    moreInfo.setVisible(true);
                    editChat.setVisible(true);
                    deleteChat.setVisible(true);
                    addMember.setVisible(false);

                } else if (chatModeratorIds != null && chatModeratorIds.contains(userId)) {
                    moreInfo.setVisible(true);
                    editChat.setVisible(true);
                    deleteChat.setVisible(false);
                    addMember.setVisible(false);

                } else {
//                    moreOptionButton.setVisibility(View.GONE);
//                    moreInfoButton.setVisibility(View.VISIBLE);
                }
            }
        }
        else {

        }

        moreInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.chatMoreInfo) {

                        } else if (itemId == R.id.chatUpdate) {

                        } else if (itemId == R.id.chatDelete) {
                            deleteChatAlert();
                        } else if (itemId == R.id.chatAddUser) {

                        }

                        return false;
                    }
                });
            }
        });
    }

    private void deleteChatAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatView.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View deleteDialogLayout = inflater.inflate(R.layout.ui_delete_dialog_view, null);

        TextView dialogMessage = deleteDialogLayout.findViewById(R.id.dialogMessage);
        Button cancelButton = deleteDialogLayout.findViewById(R.id.dialogCancel);
        Button deleteButton = deleteDialogLayout.findViewById(R.id.dialogAccept);

        builder.setView(deleteDialogLayout);
        AlertDialog dialog = builder.create();
        dialog.show();

        try {
            dialogMessage.setText("Are you sure you want to delete this post");
            cancelButton.setOnClickListener(v -> dialog.dismiss());

            deleteButton.setOnClickListener(v -> {
                deleteChatRoom();
                dialog.dismiss();

                Intent deleteIntent = new Intent();
               if (isGroup) {
                   if (forumId != null) {
                       deleteIntent = new Intent(ChatView.this, ForumView.class);
                       deleteIntent.putExtra("forumId", forumId);
                       deleteIntent.putExtra("chatId", chatId);
                   }
                   else {
                       deleteIntent = new Intent(ChatView.this, ChatFragment.class);
                   }
               }
               else {
                   deleteIntent = new Intent(ChatView.this, ChatFragment.class);
               }
                setResult(RESULT_OK, deleteIntent);
                finish();
            });

        } catch (Exception e) {
            Log.e("Post", "getView: ", e);
            e.printStackTrace();
        }

    }

    private void deleteChatRoom() {
        if (chatUserIds != null) {
            for (String id: chatUserIds) {
                db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayRemove(chatId));
            }
        }

        if (isGroup) {
            if (forumId != null) {
                db.collection("FORUMS").document(forumId).update("chatId", null);
            }
        }

        chatData.delete();

    }

    private void deleteChatRoomFromForum(String chatId) {
        chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    ArrayList<String> adminIds = new ArrayList<>(), moderatorIds = new ArrayList<>(), memberIds = new ArrayList<>();

                    if (document.exists()) {

                        if (document.get("adminIds") != null) {
                            adminIds = (ArrayList<String>) document.get("adminIds");

                            if (adminIds != null) {
                                for (String id: adminIds) {
                                    db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayRemove(chatId));
                                }
                            }
                        }

                        if (document.get("moderatorIds") != null) {
                            moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                            if (moderatorIds != null) {
                                for (String id: moderatorIds) {
                                    db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayRemove(chatId));
                                }
                            }
                        }

                        if (document.get("memberIds") != null) {
                            memberIds = (ArrayList<String>) document.get("memberIds");

                            if (memberIds != null) {
                                for (String id: memberIds) {
                                    db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayRemove(chatId));
                                }
                            }
                        }
                    }

                    chatData.delete();
                }
            }
        });
    }

    private void returnToPreviousPage() {
        returnBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNew) {
                    finish();
                }
                else {

                }
            }
        });
    }
}