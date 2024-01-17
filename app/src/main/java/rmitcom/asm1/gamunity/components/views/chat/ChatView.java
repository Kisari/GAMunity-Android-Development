package rmitcom.asm1.gamunity.components.views.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ChatMessageRecyclerViewAdapter;
import rmitcom.asm1.gamunity.adapter.UserRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.GroupChat;
import rmitcom.asm1.gamunity.model.Message;
import rmitcom.asm1.gamunity.model.User;

public class ChatView extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private final String TAG = "Chat view";
    private DocumentReference chatData, forumData, currUserData, otherUserData;
    private CollectionReference chatRef;
    private TextView chatTitle, returnBackBtn, accessBtn;
    private EditText inputMessage;
    private ImageView sendMessageBtn, baseImage;
    private ProgressBar chatProgressBar;
    private ShapeableImageView chatImage;
    private RecyclerView messageBody;
    private String inputMessageStr, chatId, dataId, chatTitleStr, chatImgUri;
    private ArrayList<String> chatMemberIds, chatModeratorIds, chatAdminIds, chatMemberNames;
    private ArrayList<Message> chatMessages;
    private GroupChat groupChat;
    private boolean isGroup;
    private ChatMessageRecyclerViewAdapter adapter;

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
        accessBtn = findViewById(R.id.linkAccess);
        inputMessage = findViewById(R.id.chatWriteMessage);
        baseImage = findViewById(R.id.baseImg);
        sendMessageBtn = findViewById(R.id.messageSubmit);
        messageBody = findViewById(R.id.chatMessages);

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
                    forumData = db.collection("FORUMS").document(dataId);
                }
                else {
                    otherUserData = db.collection("users").document(dataId);
                }
            }

        }

        getOrCreateChatRoom();
        getChatData();

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = inputMessage.getText().toString().trim();

                if (message.isEmpty()) {
                    return;
                }
                sendMessageToChat(message);
            }
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

                                            ArrayList<String> memberIds = (ArrayList<String>) document.get("memberIds");
                                            ArrayList<String> moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                            if (memberIds.contains(userId)) {
                                                chatMemberIds.add(userId);
                                            } else if (moderatorIds.contains(userId)) {
                                                chatModeratorIds.add(userId);
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

                        } else {
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
                                                    newChatroom.put("dataId", dataId);

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
        Query query = chatRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(50);
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
                    // Clear the list before updating with new data
                    chatMessages.clear();
                }

                if (value != null) {
                    for (QueryDocumentSnapshot queryDocument: value) {
                        Message messageInfo = queryDocument.toObject(Message.class);
                        chatMessages.add(messageInfo);
                        Log.i(TAG, "message - existed: " + messageInfo.getMessageContent());
                    }
                }
                setupList(chatMessages);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void sendMessageToChat(String message) {
        Map<String, String> lastSender = new HashMap<>();
        lastSender.put("lastMessageSenderId", userId);
        chatData.set(lastSender, SetOptions.merge());

        Map<String, Timestamp> lastTimestamp = new HashMap<>();
        lastTimestamp.put("lastTimestamp", Timestamp.now());
        chatData.set(lastTimestamp, SetOptions.merge());

        Message newMessage = new Message(message, userId, Timestamp.now());
        chatRef.add(newMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
//                    DocumentReference document = task.getResult();
                inputMessage.setText("");
                Log.i(TAG, "message - just send: " + newMessage.getMessageContent());
                setUI();
            }
        });
    }

    private void setupList(ArrayList<Message> messageList) {
        Log.i(TAG, "setupList: call setupList");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        messageBody.setLayoutManager(layoutManager);

        Log.i(TAG, "setupList: create adapter");
        adapter = new ChatMessageRecyclerViewAdapter(this, messageList);
        messageBody.setAdapter(adapter);

//        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                messageBody.smoothScrollToPosition(0);
            }
        });
    }

    private void returnToPreviousPage() {
        returnBackBtn.setOnClickListener(v -> finish());
    }
}