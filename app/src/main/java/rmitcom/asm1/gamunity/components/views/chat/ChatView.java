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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private EditText inputMessage, editChatTitle;
    private ImageView sendMessageBtn, baseImage, chatImageBtn;
    private ImageButton editChatIcon;
    private ProgressBar chatProgressBar, editChatProgressBar;
    private ShapeableImageView chatImage, editChatImage;
    private RecyclerView messageBody;
    private String inputMessageStr, chatId, dataId, forumId, chatTitleStr, chatImgUri, chatIconUri;
    private ArrayList<String> chatMemberIds, chatModeratorIds, chatAdminIds;
    private ArrayList<Message> chatMessages;
    private GroupChat groupChat;
    private Uri chatImageFilePath, chatIconFilePath;
    private boolean isGroup, isNew, isIconSelected, isImageUpdate = false;
    private int lastActionCode;
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

            if (isGroup) {
                if (dataId != null && !dataId.isEmpty()) {
                    forumId = dataId;
                    forumData = db.collection("FORUMS").document(dataId);
                }
            }
            else {
                if (dataId != null && !dataId.isEmpty()) {
                    otherUserData = db.collection("users").document(dataId);
                }
            }
        }

        getOrCreateChatRoom();
        getChatData();

        sendMessageBtn.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (message.isEmpty()) {
                return;
            }
            sendMessageToChat(message);
        });

        chatImageBtn.setOnClickListener(v -> {
            chooseImage(false);
        });

        returnToPreviousPage();
    }

    private void getOrCreateChatRoom() {
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
                        chatIconUri = document.getString("chatImg");
                        Log.i(TAG, "chatView - chatIconUri: " + chatIconUri);

                        if (chatIconUri != null) {
                            try {
                                baseImage.setVisibility(View.GONE);
                                chatImage.setVisibility(View.VISIBLE);
                                chatProgressBar.setVisibility(View.VISIBLE);

                                new AsyncImage(chatImage, chatProgressBar).loadImage(chatIconUri);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            baseImage.setVisibility(View.VISIBLE);
                            chatImage.setVisibility(View.GONE);
                            chatProgressBar.setVisibility(View.GONE);
                        }

                        chatAdminIds = new ArrayList<>();
                        chatModeratorIds = new ArrayList<>();
                        chatMemberIds = new ArrayList<>();

                        if (document.get("adminIds") != null) {
                            chatAdminIds = (ArrayList<String>) document.get("adminIds");
                        }

                        if (document.get("moderatorIds") != null) {
                            chatModeratorIds = (ArrayList<String>) document.get("moderatorIds");
                        }

                        if (document.get("memberIds") != null) {
                            chatMemberIds = (ArrayList<String>) document.get("memberIds");
                        }

                        setMoreInfo();
                    }
                    else {
                        Log.i(TAG, "chatView getOrCreateChatRoom: empty doc");
                        isNew = true;

                        lastActionCode = constant.CREATE;

                        Map<String, Object> newChatroom = new HashMap<>();

                        chatMemberIds = new ArrayList<>();
                        chatModeratorIds = new ArrayList<>();
                        chatAdminIds = new ArrayList<>();

                        newChatroom.put("isGroup", isGroup);

                        if (isGroup) {
                            if (forumId != null) {
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

                                                ArrayList<String> memberIds = (ArrayList<String>) document.get("memberIds");
                                                ArrayList<String> moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                                if (memberIds != null) {
                                                    if (memberIds.contains(userId)) {
                                                        chatMemberIds.add(userId);
                                                    }
                                                }

                                                if (moderatorIds != null) {
                                                    if (moderatorIds.contains(userId)) {
                                                        chatModeratorIds.add(userId);
                                                    }
                                                }

                                                chatTitleStr = forumName + "'s Group Chat";
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

                                                db.collection("users").document(chiefAdminId).update("chatGroupIds", FieldValue.arrayUnion(chatId));

                                                setMoreInfo();

                                            }
                                        }
                                    }
                                });
                            }

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
                                                    chatTitleStr = name;
                                                    Log.i(TAG, "chatName: " + name);

                                                    newChatroom.put("chatTitle", name);
                                                    chatTitle.setText(name);

                                                    chatAdminIds.add(userId);
                                                    chatAdminIds.add(dataId);

                                                    newChatroom.put("adminIds", chatAdminIds);
                                                    newChatroom.put("lastTimestamp", Timestamp.now());
                                                    newChatroom.put("lastMessageSenderId", "");
                                                    newChatroom.put("chatImg", null);

                                                    chatData.set(newChatroom, SetOptions.merge());

                                                    setMoreInfo();
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
                    messageBody.scrollToPosition(adapter.getItemCount() - 1);
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
        messageBody.scrollToPosition(adapter.getItemCount() - 1);
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

    private void chooseImage(boolean isIcon){
        isIconSelected = isIcon;
        if (isIconSelected) {
            isImageUpdate = true;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.putExtra("isIcon", isIcon);
        Log.i(TAG, "chatView chooseImage: called - isIcon: " + isIcon);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_CHAT_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == constant.PICK_CHAT_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Log.i(TAG, "chatView resultCode: Result is ok");
            if (data != null && data.getData() != null) {
                Log.i(TAG, "chatView data: " + data.getData());
//                boolean isIcon = getIntent().getExtras().getBoolean("isIcon");
                Log.i(TAG, "chatView getIsIcon: " + isIconSelected);
                if (isIconSelected) {
                    Log.i(TAG, "chatView getPathFile: called - for chatIcon");
                    chatIconFilePath = data.getData();
                    Log.i(TAG, "chatView chatImageFilePath: " + chatIconFilePath.toString());
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), chatIconFilePath);
                        editChatImage.setImageBitmap(bitmap);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else {
                    Log.i(TAG, "chatView getPathFile: called - for message");
                    chatImageFilePath = data.getData();
                    Log.i(TAG, "chatView chatImageFilePath: " + chatImageFilePath.toString());
                    uploadChatImage(chatImageFilePath, false, null);
                }
            }
        }

        if (requestCode == 117 && resultCode == RESULT_OK) {
            String newChatId = data.getStringExtra("chatId");

            Intent chatIntent = new Intent(ChatView.this, ChatView.class);
            chatIntent.putExtra("chatId", newChatId);
            chatIntent.putExtra("isGroup", true);
            chatIntent.putExtra("dataId", "");
            startActivity(chatIntent);
            finish();
        }

        if (requestCode == 118 && resultCode == RESULT_OK) {
            setUI();
            recreate();
        }
    }

    private void uploadChatImage(Uri submitFilePath, boolean isIcon, AlertDialog dialog) {
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

                        Toast.makeText(this, "Uploaded Image", Toast.LENGTH_SHORT).show();

                        if (isIcon) {
                            if (chatIconUri != null) {
                                String pattern = "images%2F(.*?)\\?";
                                Pattern p = Pattern.compile(pattern);
                                Matcher m = p.matcher(chatIconUri);

                                if (m.find()) {
                                    String oldUri = m.group(1);

                                    StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                                    oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                        Log.i("Delete image", "Old image deleted successfully");
                                    }).addOnFailureListener(e -> {
                                        Log.e("Delete image", "Failed to delete old image: " + e.getMessage());
                                    });
                                }
                            }

                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                chatIconFilePath = uri;
                                chatIconUri = chatIconFilePath.toString();
                                Log.i(TAG, "chatView - update image chatIconUri: " + chatIconUri);

                                updateChatRoom(editChatTitle);

                                if (dialog != null) {
                                    dialog.dismiss();
                                    recreate();
                                }
                            });
                        }
                        else {
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Log.i(TAG, "chatView uploadChatImage: called - is message");
                                chatImageFilePath = uri;
                                Log.i(TAG, "chatView URI: " + uri);
                                chatImgUri = chatImageFilePath.toString();
                                Log.i(TAG, "chatView chatImgURI: " + chatImgUri);

                                Message newMessage = new Message(chatImgUri, userId, Timestamp.now(), true);
                                messageBody.scrollToPosition(adapter.getItemCount() - 1);
                                Log.i(TAG, "chatView newMess: " + newMessage.getMessageContent());
                                chatRef.add(newMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Log.i(TAG, "chatView newMess - just send: " + newMessage.getMessageContent());
                                            adapter.notifyDataSetChanged();
//                                        setUI();
                                        }
                                    }
                                });

                                Log.i(TAG, "uploadCommentImage - commentImageFilePath: " + chatImageFilePath);
                            });
                        }

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
        Log.i(TAG, "setMoreInfo - call setupMoreInfo");
        Log.i(TAG, "setMoreInfo - forumId: " + forumId);

        Log.i(TAG, "setMoreInfo - chatAdminIds: " + chatAdminIds);
        Log.i(TAG, "setMoreInfo - chatModeratorIds: " + chatModeratorIds);
        Log.i(TAG, "setMoreInfo - chatMemberIds: " + chatMemberIds);

        if (isGroup) {
            if (forumId != null) {
                groupChat = new GroupChat(chatId, chatTitleStr, chatIconUri, true, forumId, null);
            }
            else {
                groupChat = new GroupChat(chatId, chatTitleStr, chatIconUri, true, null, null);
            }
        }
        else {
            groupChat = new GroupChat(chatId, chatTitleStr, chatIconUri, false, null, null);
        }

        if ((chatAdminIds != null && chatAdminIds.contains(userId))) {
            moreOptionBtn.setVisibility(View.VISIBLE);
            moreInfoBtn.setVisibility(View.GONE);

        } else {
            moreOptionBtn.setVisibility(View.GONE);
            moreInfoBtn.setVisibility(View.VISIBLE);
        }

        PopupMenu popupMenu = new PopupMenu(ChatView.this, moreOptionBtn);
        popupMenu.getMenuInflater().inflate(R.menu.chat_more_option, popupMenu.getMenu());

        MenuItem moreInfo = popupMenu.getMenu().findItem(R.id.chatMoreInfo);
        MenuItem editChat = popupMenu.getMenu().findItem(R.id.chatUpdate);
        MenuItem deleteChat = popupMenu.getMenu().findItem(R.id.chatDelete);
        MenuItem addMember = popupMenu.getMenu().findItem(R.id.chatAddUser);
        MenuItem removeMember = popupMenu.getMenu().findItem(R.id.chatRemoveUser);

        if (chatAdminIds != null && chatAdminIds.contains(userId)) {
            moreInfo.setVisible(true);
            deleteChat.setVisible(true);
            if (isGroup) {
                editChat.setVisible(forumId == null);
                addMember.setVisible(forumId == null);
            }
            removeMember.setVisible(isGroup);

        }
//        else if (chatModeratorIds != null && chatModeratorIds.contains(userId)) {
//            moreInfo.setVisible(true);
//            if (isGroup) {
//                editChat.setVisible(forumId == null);
//            }
//            deleteChat.setVisible(false);
//            if (isGroup) {
//                addMember.setVisible(forumId == null);
//            }
//
//        }
//        else {
//            moreOptionBtn.setVisibility(View.GONE);
//            moreInfoBtn.setVisibility(View.VISIBLE);
//        }

        moreInfoBtn.setOnClickListener(v -> accessChatMoreInfo());

        moreOptionBtn.setOnClickListener(v -> {
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.chatMoreInfo) {
                    accessChatMoreInfo();
                } else if (itemId == R.id.chatUpdate) {
                    updateChatRoomPopup();
                } else if (itemId == R.id.chatDelete) {
                    deleteChatAlert();
                } else if (itemId == R.id.chatAddUser) {
                    addMember();
                } else if (itemId == R.id.chatRemoveUser) {
                    removeMember();
                }

                return false;
            });

            popupMenu.show();
        });
    }

    private void accessChatMoreInfo() {
        Intent moreInfoIntent = new Intent(ChatView.this, ChatMoreInfo.class);
        moreInfoIntent.putExtra("chatId", chatId);
        startActivity(moreInfoIntent);
    }

    @SuppressLint("InflateParams")
    private void updateChatRoomPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatView.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View editDialog = inflater.inflate(R.layout.ui_edit_chat_view, null);

        TextView dialogMessage = editDialog.findViewById(R.id.dialogMessage);
        Button cancelButton = editDialog.findViewById(R.id.dialogCancel);
        Button updateButton = editDialog.findViewById(R.id.dialogAccept);

        editChatTitle = editDialog.findViewById(R.id.editChatTitle);
        editChatIcon = editDialog.findViewById(R.id.editChatIcon);
        editChatProgressBar = editDialog.findViewById(R.id.editChatProgressBar);
        editChatImage = editDialog.findViewById(R.id.editChatImage);

        editChatTitle.setText(chatTitleStr);

        builder.setView(editDialog);
        AlertDialog dialog = builder.create();
        dialog.show();

        if (chatIconUri != null) {
            Log.i(TAG, "chatView - chatIconUri: " + chatIconUri);
            try {
                new AsyncImage(editChatImage, editChatProgressBar).loadImage(chatIconUri);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            editChatProgressBar.setVisibility(View.GONE);
        }

        editChatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(true);
            }
        });

        try {
            dialogMessage.setText("Update Chat Infomation");

            cancelButton.setOnClickListener(v -> dialog.dismiss());

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isImageUpdate) {
                        uploadChatImage(chatIconFilePath, true, dialog);
                    }
                    else {
                        updateChatRoom(editChatTitle);
                        dialog.dismiss();
                        recreate();
                    }

                }
            });

        } catch (Exception e) {
            Log.e("Post", "getView: ", e);
            e.printStackTrace();
        }

    }

    private void updateChatRoom(TextView editTitleView) {

        boolean isValid = true;

        String editTitle = editTitleView.getText().toString();

        if (editTitle.isEmpty()) {
            isValid = false;
            editTitleView.setError("Chat title cannot be empty");
        }

        if (isValid) {
            Map<String, String> data = new HashMap<>();
            data.put("chatTitle", editTitle);

            if (chatIconFilePath != null) {
                Log.i(TAG, "chatView update - chatIconFilePath: " + chatIconFilePath);
                Log.i(TAG, "chatView - in update chatIconUri: " + chatIconUri);
                data.put("chatImg", chatIconUri);
            }
            chatData.set(data, SetOptions.merge());
        }

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
                deleteChatRoom(dialog);
            });

        } catch (Exception e) {
            Log.e("Post", "getView: ", e);
            e.printStackTrace();
        }

    }

    private void deleteChatRoom(AlertDialog dialog) {
        ArrayList<Task<Void>> userTasks = new ArrayList<>();

        if (chatAdminIds != null) {
            for (String id : chatAdminIds) {
                Log.i(TAG, "setMoreInfo - userId: " + id);
                Task<Void> task = db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayRemove(chatId));
                userTasks.add(task);
            }

        }

        if (chatModeratorIds != null) {
            for (String id : chatModeratorIds) {
                Log.i(TAG, "setMoreInfo - userId: " + id);
                Task<Void> task = db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayRemove(chatId));
                userTasks.add(task);
            }

        }

        if (chatMemberIds != null) {
            for (String id : chatMemberIds) {
                Log.i(TAG, "setMoreInfo - userId: " + id);
                Task<Void> task = db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayRemove(chatId));
                userTasks.add(task);
            }
        }

        Tasks.whenAllSuccess(userTasks).addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful()) {
                ArrayList<Task<Void>> forumTasks = new ArrayList<>();

                if (isGroup && forumId != null) {
                    Task<Void> task = db.collection("FORUMS").document(forumId).update("chatId", null);
                    forumTasks.add(task);
                }

                Tasks.whenAllSuccess(forumTasks).addOnCompleteListener(forumTask -> {
                    if (forumTask.isSuccessful()) {
                        // Handle success
                        ArrayList<Task<Void>> messageTasks = new ArrayList<>();

                        chatRef.get().continueWithTask(task -> {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String messageId = document.getId();

                                boolean isImage = Boolean.TRUE.equals(document.getBoolean("image"));

                                if (isImage) {
                                    String imageUri = document.getString("messageContent");
                                    String pattern = "images%2F(.*?)\\?";
                                    Pattern p = Pattern.compile(pattern);
                                    Matcher m = p.matcher(imageUri);

                                    if (m.find()) {
                                        String oldUri = m.group(1);

                                        StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                                        oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                            Log.i("Delete image", "Old image deleted successfully");
                                        }).addOnFailureListener(e -> {
                                            Log.e("Delete image", "Failed to delete old image: " + e.getMessage());
                                        });
                                    }
                                }

                                Task<Void> deleteTask = chatRef.document(messageId).delete();
                                messageTasks.add(deleteTask);
                            }

                            return Tasks.whenAllSuccess(messageTasks);
                        }).addOnCompleteListener(messageTask -> {
                            if (messageTask.isSuccessful()) {
                                chatData.delete();

                                lastActionCode = constant.DELETE;
                                dialog.dismiss();

                                Intent deleteIntent;
                                if (isGroup) {
                                    if (forumId != null) {
                                        deleteIntent = new Intent(ChatView.this, ForumView.class);
                                        deleteIntent.putExtra("forumId", forumId);
                                        deleteIntent.putExtra("chatId", chatId);
                                    } else {
                                        deleteIntent = new Intent(ChatView.this, ChatFragment.class);
                                    }
                                } else {
                                    deleteIntent = new Intent(ChatView.this, ChatFragment.class);
                                }

                                setResult(lastActionCode, deleteIntent);
                                finish();

                            } else {
                                Log.e(TAG, "Failed to delete chat messages: " + messageTask.getException().getMessage());
                            }
                        });
                    } else {
                        Log.e(TAG, "Failed to update forum: " + forumTask.getException().getMessage());
                    }
                });
            } else {
                Log.e(TAG, "Failed to update users: " + userTask.getException().getMessage());
            }
        });
    }

    public void deleteChatRoomFromForum(String chatId) {
        DocumentReference chatData = db.collection("CHATROOMS").document(chatId);
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

                        CollectionReference chatRef = chatData.collection("CHATS");
                        chatRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<Message> messageList = new ArrayList<>();
                                    String messageId;

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        messageId = document.getId();
                                        Log.i(TAG, "chatView - messageId: " + messageId);
                                        boolean isImage = Boolean.TRUE.equals(document.getBoolean("image"));

                                        if (isImage) {
                                            String imageUri = document.getString("messageContent");
                                            String pattern = "images%2F(.*?)\\?";
                                            Pattern p = Pattern.compile(pattern);
                                            Matcher m = p.matcher(imageUri);

                                            if (m.find()) {
                                                String oldUri = m.group(1);

                                                StorageReference oldImageRef = storage.getReference().child("images/" + oldUri);
                                                oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                                    Log.i("Delete image", "Old image deleted successfully");
                                                }).addOnFailureListener(e -> {
                                                    Log.e("Delete image", "Failed to delete old image: " + e.getMessage());
                                                });
                                            }
                                        }
                                        chatRef.document(messageId).delete()
                                                .addOnSuccessListener(unused -> Log.i(TAG, "chatView - delete chat message: success"))
                                                .addOnFailureListener(e -> Log.i(TAG, "chatView - delete chat message: failed"));
                                    }
                                }
                            }
                        });
                    }

                    chatData.delete();
                }
            }
        });
    }

    public void addMember() {
        Intent moreInfoIntent = new Intent(ChatView.this, AddMemberToGroupChat.class);
        moreInfoIntent.putExtra("chatId", chatId);
        startActivityForResult(moreInfoIntent, 117);
    }

    public void removeMember() {
        Intent moreInfoIntent = new Intent(ChatView.this, RemoveMemberFromGroupChat.class);
        moreInfoIntent.putExtra("chatId", chatId);
        startActivityForResult(moreInfoIntent, 118);
    }

    private void returnToPreviousPage() {
        returnBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent();
                backIntent.putExtra("newGroupChat", groupChat);
                setResult(lastActionCode, backIntent);
                finish();
            }
        });
    }
}